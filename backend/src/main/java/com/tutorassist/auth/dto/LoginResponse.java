package com.tutorassist.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {

    private String token;
    private Long userId;
    private String username;
    private String displayName;
    private String role;
    private String avatarUrl;
}
