package com.tutorassist.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateUserRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度须为3到50位")
    @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "用户名只能包含字母、数字、点、下划线和连字符")
    private String username;

    @NotBlank(message = "显示名称不能为空")
    @Size(max = 50, message = "显示名称不能超过50个字符")
    private String displayName;

    @NotBlank(message = "初始密码不能为空")
    @Size(min = 6, max = 72, message = "密码长度须为6到72位")
    private String password;
}
