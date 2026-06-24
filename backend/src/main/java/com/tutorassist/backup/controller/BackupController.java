package com.tutorassist.backup.controller;

import com.tutorassist.backup.entity.BackupRecord;
import com.tutorassist.backup.service.BackupService;
import com.tutorassist.common.PageResult;
import com.tutorassist.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Tag(name = "数据备份", description = "数据库备份、恢复、自动备份管理")
@RestController
@RequestMapping("/api/v1/backups")
@RequiredArgsConstructor
public class BackupController {

    private final BackupService backupService;

    @Operation(summary = "创建手动备份")
    @PostMapping
    public Result<BackupRecord> createBackup(@RequestBody(required = false) Map<String, String> body) {
        String remark = (body != null && body.containsKey("remark")) ? body.get("remark") : "手动备份";
        BackupRecord record = backupService.createManualBackup(remark);
        return Result.success(record);
    }

    @Operation(summary = "备份记录列表")
    @GetMapping
    public Result<PageResult<BackupRecord>> listBackups(
            @RequestParam(required = false) String backupType,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        return Result.success(backupService.listBackups(backupType, status, page, size));
    }

    @Operation(summary = "删除备份")
    @DeleteMapping("/{id}")
    public Result<Void> deleteBackup(@PathVariable Long id) {
        backupService.deleteBackup(id);
        return Result.success();
    }

    @Operation(summary = "恢复备份")
    @PostMapping("/{id}/restore")
    public Result<BackupRecord> restoreBackup(@PathVariable Long id) {
        BackupRecord record = backupService.restoreBackup(id);
        return Result.success(record);
    }

    @Operation(summary = "下载备份文件")
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadBackup(@PathVariable Long id) {
        String filePath = backupService.getBackupFilePath(id);
        Resource resource = new FileSystemResource(filePath);

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        String encodedFileName = URLEncoder.encode(resource.getFilename(), StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedFileName)
                .body(resource);
    }

    @Operation(summary = "获取自动备份配置")
    @GetMapping("/config")
    public Result<Map<String, Object>> getAutoBackupConfig() {
        Map<String, Object> config = Map.of(
                "enabled", backupService.isAutoBackupEnabled(),
                "maxKeep", backupService.getMaxKeepBackups(),
                "backupDir", backupService.getBackupDirectory()
        );
        return Result.success(config);
    }
}
