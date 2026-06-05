package com.tutorassist.notification.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tutorassist.ai.entity.SystemSetting;
import com.tutorassist.ai.mapper.SystemSettingMapper;
import com.tutorassist.notification.dto.WeChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeChatService {

    private final SystemSettingMapper settingMapper;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    public boolean sendText(String content) {
        String webhookUrl = getWebhookUrl();
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            log.warn("企业微信 Webhook 未配置");
            return false;
        }

        try {
            WeChatMessage message = WeChatMessage.builder()
                    .msgtype("text")
                    .text(WeChatMessage.TextContent.builder()
                            .content(content)
                            .build())
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(message), headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    webhookUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            log.info("企业微信发送结果: {}", response.getBody());
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("企业微信发送失败", e);
            return false;
        }
    }

    public boolean sendCourseReminder(String studentName, String subject, String time, String location) {
        String content = String.format(
                "📚 课程提醒\n\n学生：%s\n科目：%s\n时间：%s\n地点：%s\n\n请准时上课！",
                studentName, subject, time, location
        );
        return sendText(content);
    }

    public boolean sendHomeworkReminder(String studentName, String homeworkTitle, String dueDate) {
        String content = String.format(
                "📝 作业提醒\n\n学生：%s\n作业：%s\n截止日期：%s\n\n请及时完成！",
                studentName, homeworkTitle, dueDate
        );
        return sendText(content);
    }

    public boolean sendFeeReminder(String studentName, String message) {
        String content = String.format(
                "💰 费用提醒\n\n学生：%s\n%s",
                studentName, message
        );
        return sendText(content);
    }

    private String getWebhookUrl() {
        LambdaQueryWrapper<SystemSetting> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemSetting::getKey, "notification.wechat_webhook");
        SystemSetting setting = settingMapper.selectOne(wrapper);
        return (setting != null) ? setting.getValue() : null;
    }
}
