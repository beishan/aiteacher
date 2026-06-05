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
public class OpenAIGateway implements AIGateway {

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    private String apiKey = "";
    private String baseUrl = "https://api.openai.com/v1";
    private String model = "gpt-4o";

    public void configure(String apiKey, String baseUrl, String model) {
        this.apiKey = apiKey;
        if (baseUrl != null && !baseUrl.isEmpty()) this.baseUrl = baseUrl;
        if (model != null && !model.isEmpty()) this.model = model;
    }

    @Override
    public String getName() {
        return "openai";
    }

    @Override
    public String chat(String systemPrompt, List<ChatMessage> messages) {
        try {
            ObjectNode request = objectMapper.createObjectNode();
            request.put("model", model);
            request.put("temperature", 0.7);

            ArrayNode messagesNode = request.putArray("messages");

            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                ObjectNode systemMsg = objectMapper.createObjectNode();
                systemMsg.put("role", "system");
                systemMsg.put("content", systemPrompt);
                messagesNode.add(systemMsg);
            }

            for (ChatMessage msg : messages) {
                ObjectNode msgNode = objectMapper.createObjectNode();
                msgNode.put("role", msg.role());
                msgNode.put("content", msg.content());
                messagesNode.add(msgNode);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(request), headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + "/chat/completions",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            JsonNode responseJson = objectMapper.readTree(response.getBody());
            return responseJson.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            log.error("OpenAI 调用失败", e);
            throw new RuntimeException("AI 调用失败: " + e.getMessage());
        }
    }
}
