package com.tutorassist.ai.gateway;

import com.tutorassist.ai.gateway.impl.ClaudeGateway;
import com.tutorassist.ai.gateway.impl.OllamaGateway;
import com.tutorassist.ai.gateway.impl.OpenAIGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AIGatewayFactory {

    private final ClaudeGateway claudeGateway;
    private final OpenAIGateway openAIGateway;
    private final OllamaGateway ollamaGateway;

    private final Map<String, AIGateway> gateways = new HashMap<>();

    public AIGateway getGateway(String modelName) {
        return switch (modelName.toLowerCase()) {
            case "claude" -> claudeGateway;
            case "openai", "gpt" -> openAIGateway;
            case "ollama", "local" -> ollamaGateway;
            default -> claudeGateway;
        };
    }

    public void configureClaude(String apiKey, String model) {
        claudeGateway.configure(apiKey, model);
    }

    public void configureOpenAI(String apiKey, String baseUrl, String model) {
        openAIGateway.configure(apiKey, baseUrl, model);
    }

    public void configureOllama(String baseUrl, String model) {
        ollamaGateway.configure(baseUrl, model);
    }
}
