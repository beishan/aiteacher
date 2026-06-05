package com.tutorassist.ai.service;

import com.tutorassist.ai.dto.GradeReport;
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
public class AIHomeworkService {

    private final AIGatewayFactory gatewayFactory;
    private final AIRecordMapper recordMapper;
    private final SystemSettingMapper settingMapper;
    private final ObjectMapper objectMapper;

    private static final String GRADE_PROMPT = """
            你是一个专业的作业批改助手。请根据以下信息批改作业：

            科目：{subject}
            年级：{grade}
            作业内容：{content}
            学生答案：{answer}

            请按以下 JSON 格式返回批改结果：
            {
                "totalScore": 总分,
                "correctCount": 正确题数,
                "wrongCount": 错误题数,
                "questions": [
                    {
                        "questionNumber": 题号,
                        "questionContent": 题目内容,
                        "studentAnswer": 学生答案,
                        "correctAnswer": 正确答案,
                        "isCorrect": true/false,
                        "explanation": 解析
                    }
                ],
                "summary": 总结评语,
                "knowledgePoints": ["涉及的知识点1", "知识点2"],
                "suggestion": "改进建议"
            }
            """;

    public GradeReport gradeHomework(Long userId, String subject, String grade,
                                      String content, String answer) {
        long startTime = System.currentTimeMillis();

        String modelName = getSettingValue("ai.default_model", "claude");
        AIGateway gateway = gatewayFactory.getGateway(modelName);

        String prompt = GRADE_PROMPT
                .replace("{subject}", subject)
                .replace("{grade}", grade)
                .replace("{content}", content)
                .replace("{answer}", answer);

        List<AIGateway.ChatMessage> messages = List.of(
                new AIGateway.ChatMessage("user", prompt)
        );

        String response = gateway.chat(null, messages);

        long duration = System.currentTimeMillis() - startTime;

        // 记录调用
        AIRecord record = new AIRecord();
        record.setUserId(userId);
        record.setModelName(modelName);
        record.setFunctionType("HOMEWORK_GRADE");
        record.setDurationMs((int) duration);
        record.setRequestText(content);
        record.setResponseText(response);
        recordMapper.insert(record);

        try {
            // 提取 JSON 部分
            String json = extractJson(response);
            return objectMapper.readValue(json, GradeReport.class);
        } catch (Exception e) {
            log.error("解析批改结果失败", e);
            return GradeReport.builder()
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
