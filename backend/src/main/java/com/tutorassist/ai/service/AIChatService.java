package com.tutorassist.ai.service;

import com.tutorassist.ai.dto.ChatRequest;
import com.tutorassist.ai.dto.ChatResponse;
import com.tutorassist.ai.entity.AIRecord;
import com.tutorassist.ai.gateway.AIGateway;
import com.tutorassist.ai.gateway.AIGatewayFactory;
import com.tutorassist.ai.mapper.AIRecordMapper;
import com.tutorassist.ai.mapper.SystemSettingMapper;
import com.tutorassist.ai.entity.SystemSetting;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIChatService {

    private final AIGatewayFactory gatewayFactory;
    private final AIRecordMapper recordMapper;
    private final SystemSettingMapper settingMapper;

    private static final String SYSTEM_PROMPT = """
            你是一个专业的家教助手，帮助家教老师管理教学事务。
            你可以回答关于学生信息、排课安排、作业批改、成绩分析等问题。
            请用中文回答，语气亲切专业。

            回答规则：
            1. 如果问题涉及具体数据查询，说明需要查询系统数据
            2. 如果是教学建议，基于教育学原理给出专业建议
            3. 如果是作业批改请求，按照标准格式给出批改结果
            4. 保持回答简洁明了，避免冗长
            """;

    public ChatResponse chat(Long userId, ChatRequest request) {
        long startTime = System.currentTimeMillis();

        // 获取默认模型
        String modelName = getSettingValue("ai.default_model", "claude");
        AIGateway gateway = gatewayFactory.getGateway(modelName);

        // 构建消息列表
        List<AIGateway.ChatMessage> messages = new ArrayList<>();

        // 添加历史消息
        if (request.getHistory() != null) {
            for (ChatRequest.ChatMessage msg : request.getHistory()) {
                messages.add(new AIGateway.ChatMessage(msg.getRole(), msg.getContent()));
            }
        }

        // 添加当前消息
        messages.add(new AIGateway.ChatMessage("user", request.getMessage()));

        // 调用 AI
        String reply = gateway.chat(SYSTEM_PROMPT, messages);

        long duration = System.currentTimeMillis() - startTime;

        // 记录调用
        AIRecord record = new AIRecord();
        record.setUserId(userId);
        record.setModelName(modelName);
        record.setFunctionType("CHAT");
        record.setDurationMs((int) duration);
        record.setRequestText(request.getMessage());
        record.setResponseText(reply);
        recordMapper.insert(record);

        return ChatResponse.builder()
                .reply(reply)
                .modelName(modelName)
                .durationMs((int) duration)
                .build();
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
