package com.tutorassist.ai.gateway.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tutorassist.ai.gateway.AIGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClaudeGateway implements AIGateway {

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    private String apiKey = "";
    private String model = "claude-sonnet-4-20250514";

    public void configure(String apiKey, String model) {
        this.apiKey = apiKey;
        if (model != null && !model.isEmpty()) this.model = model;
    }

    @Override
    public String getName() {
        return "claude";
    }

    @Override
    public String chat(String systemPrompt, List<ChatMessage> messages) {
        try {
            ObjectNode request = objectMapper.createObjectNode();
            request.put("model", model);
            request.put("max_tokens", 4096);

            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                request.put("system", systemPrompt);
            }

            ArrayNode messagesNode = request.putArray("messages");
            for (ChatMessage msg : messages) {
                ObjectNode msgNode = objectMapper.createObjectNode();
                msgNode.put("role", msg.role());
                msgNode.put("content", msg.content());
                messagesNode.add(msgNode);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey);
            headers.set("anthropic-version", "2023-06-01");

            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(request), headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    "https://api.anthropic.com/v1/messages",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            JsonNode responseJson = objectMapper.readTree(response.getBody());
            return responseJson.path("content").get(0).path("text").asText();
        } catch (Exception e) {
            log.error("Claude 调用失败", e);
            throw new RuntimeException("AI 调用失败: " + e.getMessage());
        }
    }
}
