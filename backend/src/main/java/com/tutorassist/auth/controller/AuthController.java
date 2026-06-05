package com.tutorassist.auth.controller;

import com.tutorassist.auth.dto.LoginRequest;
import com.tutorassist.auth.dto.LoginResponse;
import com.tutorassist.auth.dto.PasswordRequest;
import com.tutorassist.auth.service.AuthService;
import com.tutorassist.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证管理", description = "用户登录、登出、信息查询")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

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

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> changePassword(Authentication authentication,
                                        @Valid @RequestBody PasswordRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        authService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
        return Result.success();
    }
}
