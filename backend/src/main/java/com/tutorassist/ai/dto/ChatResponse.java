package com.tutorassist.ai.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatResponse {

    private String reply;
    private String modelName;
    private Integer inputTokens;
    private Integer outputTokens;
    private Integer durationMs;
}
