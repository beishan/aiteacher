package com.tutorassist.ai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tutorassist.ai.entity.SystemSetting;
import com.tutorassist.ai.gateway.AIGatewayFactory;
import com.tutorassist.ai.mapper.SystemSettingMapper;
import com.tutorassist.ai.service.DockIconService;
import com.tutorassist.ai.service.SiteIconService;
import com.tutorassist.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Tag(name = "系统设置", description = "AI 模型配置、通知配置等")
@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
public class SettingsController {

    private static final List<String> UI_SETTING_KEYS = List.of(
            "ui.theme",
            "ui.content_width",
            "ui.dock.size",
            "ui.dock.opacity",
            "ui.dock.magnification",
            "ui.dock.blur",
            "ui.dock.icon_style",
            "ui.site_icon_url"
    );

    private final SystemSettingMapper settingMapper;
    private final AIGatewayFactory gatewayFactory;
    private final SiteIconService siteIconService;
    private final DockIconService dockIconService;

    @Operation(summary = "获取自定义 Dock 图标")
    @GetMapping("/dock-icons")
    public Result<Map<String, String>> getDockIcons() {
        return Result.success(dockIconService.getIconUrls());
    }

    @Operation(summary = "上传自定义 Dock 图标")
    @PostMapping(value = "/dock-icons/{name}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<String> uploadDockIcon(@PathVariable String name, @RequestParam("file") MultipartFile file) {
        return Result.success(dockIconService.upload(name, file));
    }

    @Operation(summary = "移除自定义 Dock 图标")
    @DeleteMapping("/dock-icons/{name}")
    public Result<Void> removeDockIcon(@PathVariable String name) {
        dockIconService.remove(name);
        return Result.success();
    }

    @Operation(summary = "读取自定义 Dock 图标")
    @GetMapping(value = "/dock-icons/{name}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<InputStreamResource> getDockIcon(@PathVariable String name) {
        if (!dockIconService.exists(name)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable())
                .contentType(MediaType.IMAGE_PNG)
                .body(new InputStreamResource(dockIconService.getIcon(name)));
    }

    @Operation(summary = "上传网站图标")
    @PostMapping(value = "/site-icon", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<String> uploadSiteIcon(@RequestParam("file") MultipartFile file) {
        return Result.success(siteIconService.upload(file));
    }

    @Operation(summary = "获取网站图标")
    @GetMapping(value = "/site-icon", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<InputStreamResource> getSiteIcon() {
        if (!siteIconService.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .contentType(MediaType.IMAGE_PNG)
                .body(new InputStreamResource(siteIconService.getIcon()));
    }

    @Operation(summary = "获取所有设置")
    @GetMapping
    public Result<List<SystemSetting>> getSettings() {
        LambdaQueryWrapper<SystemSetting> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SystemSetting::getId);
        return Result.success(settingMapper.selectList(wrapper));
    }

    @Operation(summary = "获取非敏感界面偏好")
    @GetMapping("/ui-preferences")
    public Result<List<SystemSetting>> getUiPreferences() {
        LambdaQueryWrapper<SystemSetting> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SystemSetting::getKey, UI_SETTING_KEYS).orderByAsc(SystemSetting::getId);
        return Result.success(settingMapper.selectList(wrapper));
    }

    @Operation(summary = "获取单个设置")
    @GetMapping("/{key}")
    public Result<SystemSetting> getSetting(@PathVariable String key) {
        LambdaQueryWrapper<SystemSetting> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemSetting::getKey, key);
        SystemSetting setting = settingMapper.selectOne(wrapper);
        return Result.success(setting);
    }

    @Operation(summary = "更新设置")
    @PutMapping
    public Result<Void> updateSettings(@RequestBody Map<String, String> settings) {
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            LambdaQueryWrapper<SystemSetting> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SystemSetting::getKey, entry.getKey());
            SystemSetting setting = settingMapper.selectOne(wrapper);

            if (setting != null) {
                setting.setValue(entry.getValue());
                setting.setUpdatedAt(LocalDateTime.now());
                settingMapper.updateById(setting);
            }
        }

        // 重新配置 AI 网关
        configureAIGateway();

        return Result.success();
    }

    private void configureAIGateway() {
        String claudeKey = getSettingValue("ai.claude.api_key", "");
        String claudeModel = getSettingValue("ai.claude.model", "claude-sonnet-4-20250514");
        gatewayFactory.configureClaude(claudeKey, claudeModel);

        String openaiKey = getSettingValue("ai.openai.api_key", "");
        String openaiUrl = getSettingValue("ai.openai.base_url", "https://api.openai.com/v1");
        String openaiModel = getSettingValue("ai.openai.model", "gpt-4o");
        gatewayFactory.configureOpenAI(openaiKey, openaiUrl, openaiModel);

        String ollamaUrl = getSettingValue("ai.ollama.base_url", "http://localhost:11434");
        String ollamaModel = getSettingValue("ai.ollama.model", "qwen2.5");
        gatewayFactory.configureOllama(ollamaUrl, ollamaModel);
    }

    private String getSettingValue(String key, String defaultValue) {
        LambdaQueryWrapper<SystemSetting> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemSetting::getKey, key);
        SystemSetting setting = settingMapper.selectOne(wrapper);
        return (setting != null && setting.getValue() != null && !setting.getValue().isEmpty())
                ? setting.getValue()
                : defaultValue;
    }
}
