package com.tutorassist.ai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tutorassist.ai.entity.SystemSetting;
import com.tutorassist.ai.gateway.AIGatewayFactory;
import com.tutorassist.ai.mapper.SystemSettingMapper;
import com.tutorassist.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Tag(name = "系统设置", description = "AI 模型配置、通知配置等")
@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SystemSettingMapper settingMapper;
    private final AIGatewayFactory gatewayFactory;

    @Operation(summary = "获取所有设置")
    @GetMapping
    public Result<List<SystemSetting>> getSettings() {
        LambdaQueryWrapper<SystemSetting> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SystemSetting::getId);
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
