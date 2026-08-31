package com.tutorassist.auth.controller;

import com.tutorassist.auth.dto.CreateUserRequest;
import com.tutorassist.auth.dto.ResetUserPasswordRequest;
import com.tutorassist.auth.dto.SystemUserVO;
import com.tutorassist.auth.dto.UpdateUserStatusRequest;
import com.tutorassist.auth.service.UserManagementService;
import com.tutorassist.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "系统用户管理", description = "管理员新建、查询、重置密码及启用或禁用系统用户")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserManagementService userManagementService;

    @Operation(summary = "查询系统用户")
    @GetMapping
    public Result<List<SystemUserVO>> list(Authentication authentication) {
        return Result.success(userManagementService.listUsers(currentUserId(authentication)));
    }

    @Operation(summary = "新建系统用户")
    @PostMapping
    public Result<SystemUserVO> create(Authentication authentication,
                                       @Valid @RequestBody CreateUserRequest request) {
        return Result.success(userManagementService.createUser(currentUserId(authentication), request));
    }

    @Operation(summary = "管理员重置用户密码")
    @PutMapping("/{userId}/password")
    public Result<Void> resetPassword(Authentication authentication,
                                      @PathVariable Long userId,
                                      @Valid @RequestBody ResetUserPasswordRequest request) {
        userManagementService.resetPassword(currentUserId(authentication), userId, request.getNewPassword());
        return Result.success();
    }

    @Operation(summary = "启用或禁用用户")
    @PutMapping("/{userId}/enabled")
    public Result<Void> updateEnabled(Authentication authentication,
                                     @PathVariable Long userId,
                                     @Valid @RequestBody UpdateUserStatusRequest request) {
        userManagementService.updateEnabled(currentUserId(authentication), userId, request.getEnabled());
        return Result.success();
    }

    private Long currentUserId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }
}
