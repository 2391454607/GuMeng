package com.gumeng.controller.ai;

import com.gumeng.code.HttpResponse;
import com.gumeng.service.coze.CozeService;
import com.gumeng.utils.JwtUtil;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/coze/ai")
public class CozeController {

    private final CozeService cozeService;

    public CozeController(CozeService cozeService) {
        this.cozeService = cozeService;
    }

    // 新增：创建会话接口
    @PostMapping("/conversation/create")
    public Mono<HttpResponse> createConversation(@RequestBody Map<String, String> body, @RequestHeader("Authorization") String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return Mono.just(HttpResponse.error("未提供有效的认证信息"));
        }
        try {
            String actualToken = token.substring(7);
            Map<String, Object> claims = JwtUtil.parseToken(actualToken);
            String userId = String.valueOf(claims.get("userId"));
            String bot = body.getOrDefault("bot", "aides");
            return cozeService.createConversation(bot, userId);
        } catch (Exception e) {
            return Mono.just(HttpResponse.error("无效的认证信息"));
        }
    }

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestBody Map<String, String> body, @RequestHeader("Authorization") String token) {
        // 验证并解析 token
        if (token == null || !token.startsWith("Bearer ")) {
            return Flux.error(new IllegalArgumentException("未提供有效的认证信息"));
        }
        
        try {
            String actualToken = token.substring(7);
            Map<String, Object> claims = JwtUtil.parseToken(actualToken);
            String userId = String.valueOf(claims.get("userId"));

            String bot = body.getOrDefault("bot", "aides");
            String message = body.get("message");

            if (message == null || message.trim().isEmpty()) {
                return Flux.error(new IllegalArgumentException("message 不能为空"));
            }

            // 新增：支持 conversation_id 透传
            String conversationId = body.get("conversation_id");
            return cozeService.chat(bot, message, userId, conversationId);
        } catch (Exception e) {
            return Flux.error(new IllegalArgumentException("无效的认证信息"));
        }
    }

}
