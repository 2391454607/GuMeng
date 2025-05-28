package com.gumeng.service.coze;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gumeng.code.HttpResponse;
import com.gumeng.config.coze.CozeBotsProperties;
import com.gumeng.config.coze.CozeBotsProperties.BotConfig;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

    // 新增：创建会话方法，返回会话ID
    public Mono<HttpResponse> createConversation(String botName, String userId) {
        BotConfig config = botsProperties.getBot(botName);
        if (config == null) {
            return Mono.just(HttpResponse.error("未配置的 bot: " + botName));
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("bot_id", config.getBotId());
        requestBody.put("user_id", userId);

        WebClient client = WebClient.builder()
                .baseUrl("https://api.coze.cn")
                .defaultHeader("Authorization", "Bearer " + config.getAccessToken())
                .build();

        return client.post()
                .uri("/v1/conversation/create")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    try {
                        JsonNode root = objectMapper.readTree(response);
                        JsonNode dataNode = root.get("data");
                        if (dataNode != null && dataNode.get("id") != null) {
                            return HttpResponse.success(dataNode.get("id").asText());
                        }
                        return HttpResponse.error("创建会话失败");
                    } catch (Exception e) {
                        return HttpResponse.error("创建会话失败: " + e.getMessage());
                    }
                });
    }

    //支持 conversationId 的 chat 方法
    public Flux<String> chat(String botName, String message, String userId, String conversationId) {
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
        requestBody.put("user_id", userId);
        requestBody.put("additional_messages", List.of(additionalMessage));
        requestBody.put("auto_save_history", true);
        requestBody.put("stream", true);

        // 新增：如果有 conversationId，加入请求体
        if (conversationId != null && !conversationId.isEmpty()) {
            requestBody.put("conversation_id", conversationId);
        }

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
                        String content = contentNode != null ? contentNode.asText() : "";
                        // SSE格式返回
                        return content.isEmpty() ? "" : "data: " + content + "\n";
                    } catch (Exception e) {
                        return "";
                    }
                })
                .filter(content -> !content.isEmpty());
    }

}
