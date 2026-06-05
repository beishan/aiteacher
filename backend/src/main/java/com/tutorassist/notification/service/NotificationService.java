package com.tutorassist.notification.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutorassist.common.PageResult;
import com.tutorassist.common.exception.BusinessException;
import com.tutorassist.notification.dto.NotificationVO;
import com.tutorassist.notification.entity.Notification;
import com.tutorassist.notification.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationMapper notificationMapper;
    private final WeChatService weChatService;

    public PageResult<NotificationVO> listNotifications(String recipientType, Long recipientId,
                                                         String status, Integer page, Integer size) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();

        if (recipientType != null) {
            wrapper.eq(Notification::getRecipientType, recipientType);
        }
        if (recipientId != null) {
            wrapper.eq(Notification::getRecipientId, recipientId);
        }
        if (status != null) {
            wrapper.eq(Notification::getStatus, status);
        }

        wrapper.orderByDesc(Notification::getCreatedAt);

        Page<Notification> pageResult = notificationMapper.selectPage(
                new Page<>(page, size),
                wrapper
        );

        List<NotificationVO> records = pageResult.getRecords().stream()
                .map(this::toVO)
                .toList();

        return new PageResult<>(records, pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize());
    }

    public Long getUnreadCount(String recipientType, Long recipientId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getRecipientType, recipientType)
                .eq(Notification::getRecipientId, recipientId)
                .eq(Notification::getStatus, "UNREAD");
        return notificationMapper.selectCount(wrapper);
    }

    @Transactional
    public void markAsRead(Long id, Long userId) {
        Notification notification = notificationMapper.selectById(id);
        if (notification == null) {
            throw new BusinessException(404, "通知不存在");
        }
        notification.setStatus("READ");
        notificationMapper.updateById(notification);
    }

    @Transactional
    public void markAllAsRead(String recipientType, Long recipientId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getRecipientType, recipientType)
                .eq(Notification::getRecipientId, recipientId)
                .eq(Notification::getStatus, "UNREAD");

        List<Notification> notifications = notificationMapper.selectList(wrapper);
        for (Notification n : notifications) {
            n.setStatus("READ");
            notificationMapper.updateById(n);
        }
    }

    @Transactional
    public Notification createNotification(String type, String title, String content,
                                            String recipientType, Long recipientId, String channel) {
        Notification notification = new Notification();
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRecipientType(recipientType);
        notification.setRecipientId(recipientId);
        notification.setChannel(channel);
        notification.setStatus("UNREAD");

        notificationMapper.insert(notification);

        // 如果是企业微信渠道，发送消息
        if ("WECHAT".equals(channel)) {
            boolean sent = weChatService.sendText(title + "\n\n" + content);
            notification.setStatus(sent ? "SENT" : "FAILED");
            notification.setSendTime(LocalDateTime.now());
            notificationMapper.updateById(notification);
        }

        return notification;
    }

    @Transactional
    public void createCourseReminder(String studentName, String subject, String time,
                                      String location, Long recipientId) {
        String title = "课程提醒：" + studentName + " - " + subject;
        String content = String.format("学生：%s\n科目：%s\n时间：%s\n地点：%s",
                studentName, subject, time, location);

        createNotification("COURSE_REMINDER", title, content, "USER", recipientId, "IN_APP");

        // 同时发送企业微信
        weChatService.sendCourseReminder(studentName, subject, time, location);
    }

    @Transactional
    public void createHomeworkReminder(String studentName, String homeworkTitle,
                                        String dueDate, Long recipientId) {
        String title = "作业提醒：" + homeworkTitle;
        String content = String.format("学生：%s\n作业：%s\n截止日期：%s",
                studentName, homeworkTitle, dueDate);

        createNotification("HOMEWORK_DUE", title, content, "USER", recipientId, "IN_APP");
    }

    private NotificationVO toVO(Notification notification) {
        return NotificationVO.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .content(notification.getContent())
                .recipientType(notification.getRecipientType())
                .recipientId(notification.getRecipientId())
                .channel(notification.getChannel())
                .status(notification.getStatus())
                .sendTime(notification.getSendTime())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
