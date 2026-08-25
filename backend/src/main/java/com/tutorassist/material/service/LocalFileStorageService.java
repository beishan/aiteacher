package com.tutorassist.material.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
public class LocalFileStorageService {

    private final Path storageRoot;

    public LocalFileStorageService(
            @Value("${storage.local.root:/opt/tutor-assist/materials}") String storageRoot) {
        this.storageRoot = Path.of(storageRoot).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void initialize() {
        try {
            Files.createDirectories(storageRoot);
            log.info("本地资料存储目录：{}", storageRoot);
        } catch (IOException e) {
            throw new IllegalStateException("无法初始化本地资料存储目录", e);
        }
    }

    public String uploadFile(MultipartFile file, String directory) {
        String extension = fileExtension(file.getOriginalFilename());
        String prefix = directory == null || directory.isBlank() ? "" : directory + "/";
        String objectKey = prefix + UUID.randomUUID() + extension;

        try (InputStream inputStream = file.getInputStream()) {
            writeAtomically(resolve(objectKey), inputStream);
            log.info("文件已保存到本地存储：{}", objectKey);
            return objectKey;
        } catch (IOException e) {
            throw new IllegalStateException("文件上传失败", e);
        }
    }

    public InputStream getFile(String objectKey) {
        try {
            return Files.newInputStream(resolve(objectKey));
        } catch (IOException e) {
            throw new IllegalStateException("文件获取失败", e);
        }
    }

    public void saveFile(String objectKey, byte[] bytes) {
        try (InputStream inputStream = new java.io.ByteArrayInputStream(bytes)) {
            writeAtomically(resolve(objectKey), inputStream);
            log.info("本地文件已更新：{}", objectKey);
        } catch (IOException e) {
            throw new IllegalStateException("文件保存失败", e);
        }
    }

    public void deleteFile(String objectKey) {
        try {
            Files.deleteIfExists(resolve(objectKey));
            log.info("已删除本地文件：{}", objectKey);
        } catch (IOException e) {
            log.error("删除本地文件失败：{}", objectKey, e);
        }
    }

    public boolean exists(String objectKey) {
        return Files.isRegularFile(resolve(objectKey));
    }

    public long lastModifiedMillis(String objectKey) {
        try {
            return Files.getLastModifiedTime(resolve(objectKey)).toMillis();
        } catch (IOException e) {
            throw new IllegalStateException("无法读取文件修改时间", e);
        }
    }

    private void writeAtomically(Path destination, InputStream inputStream) throws IOException {
        Files.createDirectories(destination.getParent());
        Path temporaryFile = Files.createTempFile(destination.getParent(), ".upload-", ".tmp");
        try {
            Files.copy(inputStream, temporaryFile, StandardCopyOption.REPLACE_EXISTING);
            try {
                Files.move(temporaryFile, destination,
                        StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporaryFile, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    private Path resolve(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            throw new IllegalArgumentException("文件路径不能为空");
        }
        Path resolved = storageRoot.resolve(objectKey).normalize();
        if (!resolved.startsWith(storageRoot)) {
            throw new IllegalArgumentException("非法文件路径");
        }
        return resolved;
    }

    private String fileExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int separator = fileName.lastIndexOf('.');
        if (separator < 0) {
            return "";
        }
        String extension = fileName.substring(separator).toLowerCase();
        return extension.matches("\\.[a-z0-9]{1,10}") ? extension : "";
    }
}
