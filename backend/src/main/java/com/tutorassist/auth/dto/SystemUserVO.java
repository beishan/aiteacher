package com.tutorassist.auth.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SystemUserVO {

    private Long id;
    private String username;
    private String displayName;
    private String role;
    private Boolean enabled;
    private LocalDateTime createdAt;
}
