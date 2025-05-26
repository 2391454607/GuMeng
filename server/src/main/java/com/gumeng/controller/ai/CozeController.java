package com.gumeng.controller.ai;

import com.gumeng.service.coze.CozeService;
import com.gumeng.utils.JwtUtil;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class CozeController {

    private final CozeService cozeService;

    public CozeController(CozeService cozeService) {
        this.cozeService = cozeService;
    }

    @PostMapping("/chat")
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

            return cozeService.chat(bot, message, userId);
        } catch (Exception e) {
            return Flux.error(new IllegalArgumentException("无效的认证信息"));
        }
    }
}
