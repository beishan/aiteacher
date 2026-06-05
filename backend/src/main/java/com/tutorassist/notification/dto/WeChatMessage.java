package com.tutorassist.notification.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WeChatMessage {

    private String msgtype;
    private TextContent text;

    @Data
    @Builder
    public static class TextContent {
        private String content;
    }
}
