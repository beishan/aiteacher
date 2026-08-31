package com.tutorassist.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateUserRoleRequest {

    @NotBlank(message = "用户角色不能为空")
    @Pattern(regexp = "ADMIN|TEACHER|VIEWER", message = "用户角色不合法")
    private String role;
}
