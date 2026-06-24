package com.tutorassist.backup.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutorassist.ai.mapper.SystemSettingMapper;
import com.tutorassist.ai.entity.SystemSetting;
import com.tutorassist.backup.entity.BackupRecord;
import com.tutorassist.backup.mapper.BackupRecordMapper;
import com.tutorassist.common.PageResult;
import com.tutorassist.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class BackupService {

    private final BackupRecordMapper backupRecordMapper;
    private final SystemSettingMapper systemSettingMapper;

    @Value("${spring.datasource.url:jdbc:postgresql://localhost:5432/tutor_assist}")
    private String datasourceUrl;

    @Value("${spring.datasource.username:postgres}")
    private String dbUsername;

    @Value("${spring.datasource.password:memoryvault}")
    private String dbPassword;

    private static final String DEFAULT_BACKUP_DIR = "/opt/tutor-assist/backups";
    private static final long BACKUP_TIMEOUT_MINUTES = 10;
    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    // ==================== 手动备份 ====================

    @Transactional
    public BackupRecord createManualBackup(String remark) {
        return executeBackup("MANUAL", remark);
    }

    // ==================== 自动备份（定时任务） ====================

    @Scheduled(cron = "${backup.auto.cron:0 0 2 * * ?}")
    public void autoBackup() {
        if (!isAutoBackupEnabled()) {
            return;
        }
        log.info("自动备份任务开始执行...");
        BackupRecord record = executeBackup("AUTO", "自动定时备份");

        // 清理旧备份
        int maxKeep = getMaxKeepBackups();
        cleanupOldBackups(maxKeep);

        log.info("自动备份任务完成, 状态: {}", record.getStatus());
    }

    // ==================== 备份列表 ====================

    public PageResult<BackupRecord> listBackups(String backupType, String status,
                                                 Integer page, Integer size) {
        LambdaQueryWrapper<BackupRecord> wrapper = new LambdaQueryWrapper<>();
        if (backupType != null && !backupType.isEmpty()) {
            wrapper.eq(BackupRecord::getBackupType, backupType);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(BackupRecord::getStatus, status);
        }
        wrapper.orderByDesc(BackupRecord::getCreatedAt);

        Page<BackupRecord> pageResult = backupRecordMapper.selectPage(new Page<>(page, size), wrapper);
        return new PageResult<>(pageResult.getRecords(), pageResult.getTotal(),
                pageResult.getCurrent(), pageResult.getSize());
    }

    // ==================== 删除备份 ====================

    @Transactional
    public void deleteBackup(Long id) {
        BackupRecord record = backupRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("备份记录不存在");
        }

        // 删除物理文件
        try {
            Path filePath = Paths.get(record.getFilePath());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("已删除备份文件: {}", record.getFilePath());
            }
        } catch (IOException e) {
            log.error("删除备份文件失败: {}", e.getMessage());
        }

        // 删除数据库记录（软删除）
        backupRecordMapper.deleteById(id);
    }

    // ==================== 恢复备份 ====================

    @Transactional
    public BackupRecord restoreBackup(Long id) {
        BackupRecord record = backupRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("备份记录不存在");
        }

        Path filePath = Paths.get(record.getFilePath());
        if (!Files.exists(filePath)) {
            throw new BusinessException("备份文件不存在: " + record.getFilePath());
        }

        log.info("开始恢复数据库, 备份文件: {}", record.getFileName());

        String dbHost = extractDbParam("host", "localhost");
        String dbPort = extractDbParam("port", "5432");
        String dbName = extractDbName();

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "pg_restore",
                    "--no-owner",
                    "--no-privileges",
                    "--clean",
                    "--if-exists",
                    "-h", dbHost,
                    "-p", dbPort,
                    "-U", dbUsername,
                    "-d", dbName,
                    record.getFilePath()
            );
            pb.environment().put("PGPASSWORD", dbPassword);
            pb.redirectErrorStream(true);

            Process process = pb.start();
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            boolean finished = process.waitFor(5, TimeUnit.MINUTES);
            if (!finished) {
                process.destroyForcibly();
                throw new BusinessException("恢复操作超时");
            }

            // pg_restore 返回码 1 表示有警告但成功
            int exitCode = process.exitValue();
            if (exitCode > 1) {
                log.warn("pg_restore 退出码: {}, 输出: {}", exitCode, output);
            }

            log.info("数据库恢复完成, 退出码: {}", exitCode);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("恢复备份失败: {}", e.getMessage(), e);
            throw new BusinessException("恢复备份失败: " + e.getMessage());
        }

        return record;
    }

    // ==================== 下载备份文件路径 ====================

    public String getBackupFilePath(Long id) {
        BackupRecord record = backupRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("备份记录不存在");
        }

        Path filePath = Paths.get(record.getFilePath());
        if (!Files.exists(filePath)) {
            throw new BusinessException("备份文件不存在");
        }

        return record.getFilePath();
    }

    // ==================== 自动备份配置 ====================

    public boolean isAutoBackupEnabled() {
        return "true".equals(getSettingValue("backup.auto.enabled", "false"));
    }

    public int getMaxKeepBackups() {
        try {
            return Integer.parseInt(getSettingValue("backup.auto.max_keep", "5"));
        } catch (NumberFormatException e) {
            return 5;
        }
    }

    public String getBackupDirectory() {
        return getSettingValue("backup.directory", DEFAULT_BACKUP_DIR);
    }

    // ==================== 核心备份逻辑 ====================

    private BackupRecord executeBackup(String backupType, String remark) {
        String backupDir = getBackupDirectory();
        String fileName = "backup_" + LocalDateTime.now().format(FILE_DATE_FORMAT) + ".dump";
        String filePath = Paths.get(backupDir, fileName).toString();

        BackupRecord record = new BackupRecord();
        record.setFileName(fileName);
        record.setFilePath(filePath);
        record.setBackupType(backupType);
        record.setStatus("IN_PROGRESS");
        record.setRemark(remark);
        backupRecordMapper.insert(record);

        long startTime = System.currentTimeMillis();

        try {
            // 确保目录存在
            Files.createDirectories(Paths.get(backupDir));

            String dbHost = extractDbParam("host", "localhost");
            String dbPort = extractDbParam("port", "5432");
            String dbName = extractDbName();

            log.info("开始备份数据库: {}:{}/{} -> {}", dbHost, dbPort, dbName, filePath);

            ProcessBuilder pb = new ProcessBuilder(
                    "pg_dump",
                    "-h", dbHost,
                    "-p", dbPort,
                    "-U", dbUsername,
                    "-d", dbName,
                    "-Fc",  // 自定义格式（支持 pg_restore）
                    "-f", filePath
            );
            pb.environment().put("PGPASSWORD", dbPassword);
            pb.redirectErrorStream(true);

            Process process = pb.start();
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            boolean finished = process.waitFor(BACKUP_TIMEOUT_MINUTES, TimeUnit.MINUTES);
            if (!finished) {
                process.destroyForcibly();
                throw new RuntimeException("备份操作超时");
            }

            int exitCode = process.exitValue();
            long durationMs = System.currentTimeMillis() - startTime;
            record.setDurationMs(durationMs);

            if (exitCode == 0) {
                // 获取文件大小
                File backupFile = new File(filePath);
                record.setFileSize(backupFile.length());
                record.setStatus("COMPLETED");

                log.info("数据库备份成功: {} ({} bytes, {} ms)", fileName, record.getFileSize(), durationMs);
            } else {
                record.setStatus("FAILED");
                record.setErrorMessage("pg_dump 退出码: " + exitCode + "\n" + output);
                log.error("数据库备份失败: exitCode={}, output={}", exitCode, output);
            }

        } catch (Exception e) {
            long durationMs = System.currentTimeMillis() - startTime;
            record.setDurationMs(durationMs);
            record.setStatus("FAILED");
            record.setErrorMessage(e.getMessage());
            log.error("数据库备份异常: {}", e.getMessage(), e);
        }

        backupRecordMapper.updateById(record);
        return record;
    }

    private void cleanupOldBackups(int maxKeep) {
        if (maxKeep <= 0) return;

        LambdaQueryWrapper<BackupRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BackupRecord::getBackupType, "AUTO");
        wrapper.eq(BackupRecord::getStatus, "COMPLETED");
        wrapper.orderByDesc(BackupRecord::getCreatedAt);

        List<BackupRecord> allBackups = backupRecordMapper.selectList(wrapper);
        if (allBackups.size() <= maxKeep) return;

        List<BackupRecord> toDelete = allBackups.subList(maxKeep, allBackups.size());
        for (BackupRecord backup : toDelete) {
            try {
                Path path = Paths.get(backup.getFilePath());
                if (Files.exists(path)) {
                    Files.delete(path);
                }
                backupRecordMapper.deleteById(backup.getId());
                log.info("已清理旧自动备份: {}", backup.getFileName());
            } catch (IOException e) {
                log.error("清理旧备份失败: {}", e.getMessage());
            }
        }
    }

    // ==================== 工具方法 ====================

    private String extractDbParam(String param, String defaultValue) {
        // 从 jdbc:postgresql://host:port/dbname 解析参数
        try {
            String url = datasourceUrl;
            if ("host".equals(param)) {
                String withoutProtocol = url.replace("jdbc:postgresql://", "");
                String hostPart = withoutProtocol.split("[:/?]")[0];
                return hostPart.isEmpty() ? defaultValue : hostPart;
            } else if ("port".equals(param)) {
                String withoutProtocol = url.replace("jdbc:postgresql://", "");
                String[] parts = withoutProtocol.split("[:/?]");
                return parts.length > 1 ? parts[1] : defaultValue;
            }
        } catch (Exception e) {
            log.warn("解析数据库参数失败: {}", e.getMessage());
        }
        return defaultValue;
    }

    private String extractDbName() {
        try {
            String url = datasourceUrl;
            int lastSlash = url.lastIndexOf('/');
            String dbName = url.substring(lastSlash + 1);
            // 去除 URL 参数
            int queryStart = dbName.indexOf('?');
            if (queryStart > 0) {
                dbName = dbName.substring(0, queryStart);
            }
            return dbName.isEmpty() ? "tutor_assist" : dbName;
        } catch (Exception e) {
            return "tutor_assist";
        }
    }

    private String getSettingValue(String key, String defaultValue) {
        try {
            LambdaQueryWrapper<SystemSetting> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SystemSetting::getKey, key);
            SystemSetting setting = systemSettingMapper.selectOne(wrapper);
            return (setting != null && setting.getValue() != null && !setting.getValue().isEmpty())
                    ? setting.getValue()
                    : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
