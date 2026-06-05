package com.tutorassist.ai.gateway;

import java.util.List;

public interface AIGateway {

    String getName();

    String chat(String systemPrompt, List<ChatMessage> messages);

    record ChatMessage(String role, String content) {}
}
