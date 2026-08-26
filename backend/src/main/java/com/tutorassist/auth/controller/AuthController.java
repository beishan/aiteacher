package com.tutorassist.auth.controller;

import com.tutorassist.auth.dto.LoginRequest;
import com.tutorassist.auth.dto.LoginResponse;
import com.tutorassist.auth.dto.PasswordRequest;
import com.tutorassist.auth.dto.ProfileRequest;
import com.tutorassist.auth.service.AuthService;
import com.tutorassist.auth.service.UserAvatarService;
import com.tutorassist.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "认证管理", description = "用户登录、登出、信息查询")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserAvatarService userAvatarService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public Result<LoginResponse> me(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(authService.getCurrentUser(userId));
    }

    @Operation(summary = "更新当前用户个人信息")
    @PutMapping("/profile")
    public Result<LoginResponse> updateProfile(Authentication authentication,
                                                @Valid @RequestBody ProfileRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(authService.updateProfile(userId, request));
    }

    @Operation(summary = "上传当前用户头像")
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<LoginResponse> uploadAvatar(Authentication authentication,
                                               @RequestParam("file") MultipartFile file) {
        Long userId = (Long) authentication.getPrincipal();
        userAvatarService.upload(userId, file);
        return Result.success(authService.getCurrentUser(userId));
    }

    @Operation(summary = "移除当前用户头像")
    @DeleteMapping("/avatar")
    public Result<LoginResponse> removeAvatar(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        userAvatarService.remove(userId);
        return Result.success(authService.getCurrentUser(userId));
    }

    @Operation(summary = "读取用户头像")
    @GetMapping(value = "/avatar/{userId}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<InputStreamResource> getAvatar(@PathVariable Long userId) {
        if (!userAvatarService.exists(userId)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .contentType(MediaType.IMAGE_PNG)
                .body(new InputStreamResource(userAvatarService.getAvatar(userId)));
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> changePassword(Authentication authentication,
                                        @Valid @RequestBody PasswordRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        authService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
        return Result.success();
    }
}
