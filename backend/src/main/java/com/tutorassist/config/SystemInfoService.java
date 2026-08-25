package com.tutorassist.config;

import com.tutorassist.config.dto.ServiceStatusVO;
import com.tutorassist.config.dto.SystemInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SystemInfoService {

    private static final String STATUS_UP = "UP";
    private static final String STATUS_DOWN = "DOWN";
    private static final long BYTES_PER_MB = 1024L * 1024L;
    private static final long BYTES_PER_GB = BYTES_PER_MB * 1024L;

    private final DataSource dataSource;
    private final Environment environment;
    private final OnlyOfficeConfig onlyOfficeConfig;

    @Value("${app.version:1.0.0}")
    private String applicationVersion;

    @Value("${storage.local.root:/opt/tutor-assist/materials}")
    private String storageRoot;

    public SystemInfoVO getSystemInfo() {
        Runtime runtime = Runtime.getRuntime();
        long uptimeMillis = java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime();
        LocalDateTime startedAt = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(java.lang.management.ManagementFactory.getRuntimeMXBean().getStartTime()),
                ZoneId.systemDefault()
        );

        List<ServiceStatusVO> services = new ArrayList<>();
        services.add(status("backend", "后端服务", STATUS_UP, "运行正常"));
        services.add(databaseStatus());
        services.add(storageStatus());
        services.add(onlyOfficeStatus());

        boolean allUp = services.stream().allMatch(item -> STATUS_UP.equals(item.getStatus()));
        long memoryUsed = runtime.totalMemory() - runtime.freeMemory();

        return SystemInfoVO.builder()
                .applicationName("家教助手")
                .version(applicationVersion)
                .overallStatus(allUp ? STATUS_UP : "DEGRADED")
                .environment(activeEnvironment())
                .startedAt(startedAt)
                .serverTime(LocalDateTime.now())
                .uptimeSeconds(uptimeMillis / 1000)
                .javaVersion(System.getProperty("java.version"))
                .operatingSystem(System.getProperty("os.name") + " " + System.getProperty("os.arch"))
                .processors(runtime.availableProcessors())
                .memoryUsedMb(memoryUsed / BYTES_PER_MB)
                .memoryMaxMb(runtime.maxMemory() / BYTES_PER_MB)
                .services(services)
                .build();
    }

    private ServiceStatusVO databaseStatus() {
        try (Connection connection = dataSource.getConnection()) {
            boolean valid = connection.isValid(2);
            if (!valid) {
                return status("database", "PostgreSQL", STATUS_DOWN, "连接不可用");
            }
            int major = connection.getMetaData().getDatabaseMajorVersion();
            int minor = connection.getMetaData().getDatabaseMinorVersion();
            return status("database", "PostgreSQL", STATUS_UP, "已连接 · " + major + "." + minor);
        } catch (Exception e) {
            return status("database", "PostgreSQL", STATUS_DOWN, "连接失败");
        }
    }

    private ServiceStatusVO storageStatus() {
        try {
            Path path = Path.of(storageRoot).toAbsolutePath().normalize();
            boolean available = Files.isDirectory(path) && Files.isReadable(path) && Files.isWritable(path);
            if (!available) {
                return status("storage", "NAS 本地存储", STATUS_DOWN, "目录不可读写");
            }
            FileStore fileStore = Files.getFileStore(path);
            long usableGb = fileStore.getUsableSpace() / BYTES_PER_GB;
            return status("storage", "NAS 本地存储", STATUS_UP, "可读写 · 可用 " + usableGb + " GB");
        } catch (IOException | RuntimeException e) {
            return status("storage", "NAS 本地存储", STATUS_DOWN, "状态检查失败");
        }
    }

    private ServiceStatusVO onlyOfficeStatus() {
        HttpURLConnection connection = null;
        try {
            String baseUrl = onlyOfficeConfig.getUrl();
            if (baseUrl == null || baseUrl.isBlank()) {
                return status("onlyoffice", "OnlyOffice", STATUS_DOWN, "未配置");
            }
            String healthUrl = baseUrl.replaceAll("/+$", "") + "/healthcheck";
            connection = (HttpURLConnection) URI.create(healthUrl).toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(1500);
            connection.setReadTimeout(1500);
            int responseCode = connection.getResponseCode();
            return responseCode >= 200 && responseCode < 300
                    ? status("onlyoffice", "OnlyOffice", STATUS_UP, "文档服务可用")
                    : status("onlyoffice", "OnlyOffice", STATUS_DOWN, "响应异常");
        } catch (Exception e) {
            return status("onlyoffice", "OnlyOffice", STATUS_DOWN, "无法连接");
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String activeEnvironment() {
        String[] profiles = environment.getActiveProfiles();
        return profiles.length == 0 ? "default" : String.join(", ", profiles);
    }

    private ServiceStatusVO status(String key, String name, String state, String detail) {
        return ServiceStatusVO.builder()
                .key(key)
                .name(name)
                .status(state)
                .detail(detail)
                .build();
    }
}
