package com.tutorassist.notification.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tutorassist.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("notifications")
public class Notification extends BaseEntity {

    private String type;
    private String title;
    private String content;
    private String recipientType;
    private Long recipientId;
    private String channel;
    private String status;
    private LocalDateTime sendTime;
}
