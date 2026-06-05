package com.tutorassist.notification.controller;

import com.tutorassist.common.PageResult;
import com.tutorassist.common.Result;
import com.tutorassist.notification.dto.NotificationVO;
import com.tutorassist.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "通知管理", description = "站内通知、企业微信推送")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "通知列表")
    @GetMapping
    public Result<PageResult<NotificationVO>> listNotifications(
            Authentication authentication,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(notificationService.listNotifications("USER", userId, status, page, size));
    }

    @Operation(summary = "未读数量")
    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(notificationService.getUnreadCount("USER", userId));
    }

    @Operation(summary = "标记已读")
    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(Authentication authentication, @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        notificationService.markAsRead(id, userId);
        return Result.success();
    }

    @Operation(summary = "全部已读")
    @PostMapping("/read-all")
    public Result<Void> markAllAsRead(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        notificationService.markAllAsRead("USER", userId);
        return Result.success();
    }
}
