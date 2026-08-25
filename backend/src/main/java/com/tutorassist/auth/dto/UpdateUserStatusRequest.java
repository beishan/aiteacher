package com.tutorassist.auth.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserStatusRequest {

    @NotNull(message = "账号状态不能为空")
    private Boolean enabled;
}
