package com.tutorassist.material.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tutorassist.config.OnlyOfficeConfig;
import com.tutorassist.material.dto.MaterialVO;
import com.tutorassist.material.entity.Material;
import com.tutorassist.material.mapper.MaterialMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.SecretKey;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OnlyOfficeService {

    private final OnlyOfficeConfig onlyOfficeConfig;
    private final MaterialMapper materialMapper;
    private final MinioService minioService;
    private final ObjectMapper objectMapper;

    /**
     * 构建 OnlyOffice 编辑器配置
     */
    public Map<String, Object> buildEditorConfig(MaterialVO material) {
        // OnlyOffice 容器内部访问后端，用 Docker 内部地址
        String fileUrl = "http://backend:8080/api/v1/materials/" + material.getId() + "/download";

        // 回调也用 Docker 内部地址
        String callbackUrl = "http://backend:8080/api/v1/onlyoffice/callback";

        // 文档 key：materialId + 更新时间戳，用于版本控制
        String documentKey = "doc_" + material.getId() + "_"
                + (material.getUpdatedAt() != null ? material.getUpdatedAt().getNano() : System.currentTimeMillis());

        // 文件扩展名
        String ext = material.getFileType() != null ? material.getFileType() : "docx";

        // 构建文档配置
        Map<String, Object> document = new LinkedHashMap<>();
        document.put("fileType", ext);
        document.put("key", documentKey);
        document.put("title", material.getTitle() + "." + ext);
        document.put("url", fileUrl);

        Map<String, Object> userInfo = new LinkedHashMap<>();
        userInfo.put("name", "教师");
        document.put("user", userInfo);

        // 编辑器配置
        Map<String, Object> editorConfig = new LinkedHashMap<>();
        editorConfig.put("callbackUrl", callbackUrl);
        editorConfig.put("mode", "edit");
        editorConfig.put("lang", "zh-CN");

        Map<String, Object> user = new LinkedHashMap<>();
        user.put("id", "1");
        user.put("name", "教师");
        editorConfig.put("user", user);

        // 自定义界面
        Map<String, Object> customization = new LinkedHashMap<>();
        customization.put("forcesave", true);
        editorConfig.put("customization", customization);

        // 顶层配置
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("document", document);
        config.put("editorConfig", editorConfig);
        config.put("type", "desktop");
        config.put("width", "100%");
        config.put("height", "100%");
        config.put("documentServerApiUrl", onlyOfficeConfig.getPublicUrl() + "/web-apps/apps/api/documents/api.js");

        // 生成 JWT token
        String token = generateToken(document, editorConfig);
        config.put("token", token);

        return config;
    }

    /**
     * 生成 JWT token（OnlyOffice 验证请求合法性）
     */
    private String generateToken(Map<String, Object> document, Map<String, Object> editorConfig) {
        try {
            // OnlyOffice 使用 HS256
            byte[] secretBytes = onlyOfficeConfig.getJwtSecret().getBytes(StandardCharsets.UTF_8);
            SecretKey key = Keys.hmacShaKeyFor(secretBytes);

            // 构建 payload
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("document", document);
            payload.put("editorConfig", editorConfig);

            // 用 setClaims 设置整个 payload，signWith 强制 HS256
            return Jwts.builder()
                    .setClaims(payload)
                    .signWith(key, io.jsonwebtoken.SignatureAlgorithm.HS256)
                    .compact();
        } catch (Exception e) {
            log.error("生成 OnlyOffice JWT token 失败", e);
            return "";
        }
    }

    /**
     * 处理 OnlyOffice 回调
     * status 含义：
     * 0 - 无文档打开
     * 1 - 文档已打开（编辑中）
     * 2 - 文档已编辑但未保存
     * 3 - 文档已保存
     * 4 - 文档保存出错
     * 6 - 文档正在编辑，但已保存
     * 7 - 强制保存时发生错误
     */
    public void handleCallback(Map<String, Object> body) {
        Integer status = (Integer) body.get("status");

        // status 2, 3, 6 表示需要保存
        if (status != null && (status == 2 || status == 3 || status == 6)) {
            String downloadUrl = (String) body.get("url");
            // key 格式：doc_{materialId}_{timestamp}
            String key = (String) body.get("key");

            if (downloadUrl == null || key == null) {
                log.warn("OnlyOffice 回调缺少 url 或 key");
                return;
            }

            // 从 key 中提取 materialId
            Long materialId = parseMaterialId(key);
            if (materialId == null) {
                log.warn("无法从 key 解析 materialId: {}", key);
                return;
            }

            log.info("OnlyOffice 回调保存：materialId={}, status={}, url={}", materialId, status, downloadUrl);

            // 将 localhost:8081 替换为 Docker 内部地址 onlyoffice:80
            // 因为后端容器无法访问 localhost:8081
            String internalUrl = downloadUrl.replace("localhost:8081", "onlyoffice:80");
            log.info("转换后的内部 URL：{}", internalUrl);

            try {
                // 从 OnlyOffice 下载编辑后的文件
                URL url = new URL(internalUrl);
                try (InputStream is = url.openStream()) {
                    // 获取当前 material 的 filePath
                    Material material = materialMapper.selectById(materialId);
                    if (material == null || material.getFilePath() == null) {
                        log.warn("material 不存在或无文件：{}", materialId);
                        return;
                    }

                    // 删除旧文件，上传新文件到同一 key
                    minioService.deleteFile(material.getFilePath());

                    // 用 RestTemplate 上传（需要包装为 MultipartFile）
                    byte[] fileBytes = is.readAllBytes();
                    String objectKey = uploadBytes(material.getFilePath(), fileBytes,
                            material.getTitle(), material.getFileType());

                    // 更新 material 记录
                    material.setFilePath(objectKey);
                    material.setFileSize((long) fileBytes.length);
                    material.setUpdatedAt(LocalDateTime.now());
                    materialMapper.updateById(material);

                    log.info("OnlyOffice 回调保存成功：materialId={}, size={}", materialId, fileBytes.length);
                }
            } catch (Exception e) {
                log.error("OnlyOffice 回调保存文件失败：materialId={}", materialId, e);
            }
        }
    }

    /**
     * 上传字节数组到 MinIO
     */
    private String uploadBytes(String objectKey, byte[] bytes, String title, String fileType) {
        try {
            // 使用 MinIO 的 putObject 直接上传字节
            io.minio.MinioClient minioClient = getMinioClient();
            String bucket = getBucket();

            minioClient.putObject(
                    io.minio.PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .stream(new java.io.ByteArrayInputStream(bytes), bytes.length, -1)
                            .contentType(getContentType(fileType))
                            .build()
            );
            return objectKey;
        } catch (Exception e) {
            log.error("上传文件到 MinIO 失败", e);
            throw new RuntimeException("文件上传失败", e);
        }
    }

    private String getContentType(String fileType) {
        if (fileType == null) return "application/octet-stream";
        return switch (fileType.toLowerCase()) {
            case "pdf" -> "application/pdf";
            case "doc", "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls", "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "ppt", "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            default -> "application/octet-stream";
        };
    }

    private Long parseMaterialId(String key) {
        try {
            // key 格式：doc_{materialId}_{timestamp}
            String[] parts = key.split("_");
            if (parts.length >= 2) {
                return Long.parseLong(parts[1]);
            }
        } catch (NumberFormatException ignored) {
        }
        return null;
    }

    // 注入 MinioClient（通过 MinioService 间接访问不方便，直接注入）
    private final io.minio.MinioClient minioClient;
    private final com.tutorassist.config.MinioConfig minioConfig;

    private io.minio.MinioClient getMinioClient() {
        return minioClient;
    }

    private String getBucket() {
        return minioConfig.getBucket();
    }
}
