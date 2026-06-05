package com.tutorassist.ai.service;

import com.tutorassist.ai.dto.TeachingPlan;
import com.tutorassist.ai.entity.AIRecord;
import com.tutorassist.ai.gateway.AIGateway;
import com.tutorassist.ai.gateway.AIGatewayFactory;
import com.tutorassist.ai.mapper.AIRecordMapper;
import com.tutorassist.ai.mapper.SystemSettingMapper;
import com.tutorassist.ai.entity.SystemSetting;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AITeachingPlanService {

    private final AIGatewayFactory gatewayFactory;
    private final AIRecordMapper recordMapper;
    private final SystemSettingMapper settingMapper;
    private final ObjectMapper objectMapper;

    private static final String PLAN_PROMPT = """
            你是一个专业的教学计划制定助手。请根据以下信息制定教学计划：

            学生信息：
            - 姓名：{studentName}
            - 年级：{grade}
            - 科目：{subject}
            - 当前水平：{level}
            - 学习目标：{goal}

            请按以下 JSON 格式返回教学计划：
            {
                "title": "计划标题",
                "goal": "总体目标",
                "phases": [
                    {
                        "phase": "阶段名称",
                        "duration": "持续时间",
                        "tasks": ["任务1", "任务2"],
                        "expectedOutcome": "预期成果"
                    }
                ],
                "materials": ["推荐资料1", "推荐资料2"],
                "summary": "计划总结"
            }
            """;

    public TeachingPlan generatePlan(Long userId, String studentName, String grade,
                                      String subject, String level, String goal) {
        long startTime = System.currentTimeMillis();

        String modelName = getSettingValue("ai.default_model", "claude");
        AIGateway gateway = gatewayFactory.getGateway(modelName);

        String prompt = PLAN_PROMPT
                .replace("{studentName}", studentName)
                .replace("{grade}", grade)
                .replace("{subject}", subject)
                .replace("{level}", level)
                .replace("{goal}", goal);

        List<AIGateway.ChatMessage> messages = List.of(
                new AIGateway.ChatMessage("user", prompt)
        );

        String response = gateway.chat(null, messages);

        long duration = System.currentTimeMillis() - startTime;

        // 记录调用
        AIRecord record = new AIRecord();
        record.setUserId(userId);
        record.setModelName(modelName);
        record.setFunctionType("TEACHING_PLAN");
        record.setDurationMs((int) duration);
        record.setRequestText(prompt);
        record.setResponseText(response);
        recordMapper.insert(record);

        try {
            String json = extractJson(response);
            return objectMapper.readValue(json, TeachingPlan.class);
        } catch (Exception e) {
            log.error("解析教学计划失败", e);
            return TeachingPlan.builder()
                    .title("教学计划")
                    .summary(response)
                    .build();
        }
    }

    private String extractJson(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
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
