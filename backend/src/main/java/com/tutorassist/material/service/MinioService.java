package com.tutorassist.material.service;

import com.tutorassist.config.MinioConfig;
import io.minio.*;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    @PostConstruct
    public void initBucket() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(minioConfig.getBucket()).build()
            );
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(minioConfig.getBucket()).build()
                );
                log.info("创建 MinIO bucket: {}", minioConfig.getBucket());
            }
        } catch (Exception e) {
            log.error("初始化 MinIO bucket 失败", e);
        }
    }

    /**
     * 上传文件，返回对象 key
     */
    public String uploadFile(MultipartFile file, String directory) {
        try {
            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String objectKey = (directory != null ? directory + "/" : "") + UUID.randomUUID() + ext;

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioConfig.getBucket())
                            .object(objectKey)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            log.info("文件已上传到 MinIO: {}", objectKey);
            return objectKey;
        } catch (Exception e) {
            log.error("上传文件到 MinIO 失败", e);
            throw new RuntimeException("文件上传失败", e);
        }
    }

    /**
     * 获取文件流
     */
    public InputStream getFile(String objectKey) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioConfig.getBucket())
                            .object(objectKey)
                            .build()
            );
        } catch (Exception e) {
            log.error("从 MinIO 获取文件失败: {}", objectKey, e);
            throw new RuntimeException("文件获取失败", e);
        }
    }

    /**
     * 获取文件信息
     */
    public StatObjectResponse getFileInfo(String objectKey) {
        try {
            return minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minioConfig.getBucket())
                            .object(objectKey)
                            .build()
            );
        } catch (Exception e) {
            log.error("获取文件信息失败: {}", objectKey, e);
            throw new RuntimeException("文件信息获取失败", e);
        }
    }

    /**
     * 生成预签名下载 URL（7天有效）
     */
    public String getPresignedUrl(String objectKey) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(minioConfig.getBucket())
                            .object(objectKey)
                            .expiry(7, TimeUnit.DAYS)
                            .build()
            );
        } catch (Exception e) {
            log.error("生成预签名 URL 失败: {}", objectKey, e);
            throw new RuntimeException("生成下载链接失败", e);
        }
    }

    /**
     * 删除文件
     */
    public void deleteFile(String objectKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioConfig.getBucket())
                            .object(objectKey)
                            .build()
            );
            log.info("已从 MinIO 删除文件: {}", objectKey);
        } catch (Exception e) {
            log.error("删除 MinIO 文件失败: {}", objectKey, e);
        }
    }
}
