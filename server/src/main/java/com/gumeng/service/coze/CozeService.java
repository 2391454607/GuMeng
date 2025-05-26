package com.gumeng.service.coze;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gumeng.config.coze.CozeBotsProperties;
import com.gumeng.config.coze.CozeBotsProperties.BotConfig;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CozeService {

    private final CozeBotsProperties botsProperties;
    private final ObjectMapper objectMapper;

    public CozeService(CozeBotsProperties botsProperties) {
        this.botsProperties = botsProperties;
        this.objectMapper = new ObjectMapper();
    }

    public Flux<String> chat(String botName, String message, String userId) {
        BotConfig config = botsProperties.getBot(botName);
        if (config == null) {
            return Flux.error(new IllegalArgumentException("未配置的 bot: " + botName));
        }

        Map<String, Object> additionalMessage = new HashMap<>();
        additionalMessage.put("content", message);
        additionalMessage.put("content_type", "text");
        additionalMessage.put("role", "user");
        additionalMessage.put("type", "question");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("bot_id", config.getBotId());
        requestBody.put("user_id", userId);  // 使用从 JWT 中获取的用户 ID
        requestBody.put("additional_messages", List.of(additionalMessage));
        requestBody.put("auto_save_history", true);
        requestBody.put("stream", true);

        WebClient client = WebClient.builder()
                .baseUrl("https://api.coze.cn")
                .defaultHeader("Authorization", "Bearer " + config.getAccessToken())
                .build();

        return client.post()
                .uri("/v3/chat")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(String.class)
                .map(chunk -> {
                    if (chunk.startsWith("data: ")) {
                        chunk = chunk.substring(6);
                    }
                    try {
                        JsonNode jsonNode = objectMapper.readTree(chunk);
                        JsonNode contentNode = jsonNode.get("content");
                        return contentNode != null ? contentNode.asText() : "";
                    } catch (Exception e) {
                        return "";
                    }
                })
                .filter(content -> !content.isEmpty());
    }
}
