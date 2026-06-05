package com.tutorassist.notification.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationVO {

    private Long id;
    private String type;
    private String title;
    private String content;
    private String recipientType;
    private Long recipientId;
    private String channel;
    private String status;
    private LocalDateTime sendTime;
    private LocalDateTime createdAt;
}
