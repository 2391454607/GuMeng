package com.gumeng.config.coze;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 功能：
 * 作者：Z
 * 日期：2025/5/26 下午4:31
 */
@Data
@Component
@ConfigurationProperties(prefix = "coze")
public class CozeBotsProperties {

    private Map<String, BotConfig> bots;

    @Data
    public static class BotConfig {
        private String botId;
        private String accessToken;
    }

    public BotConfig getBot(String name) {
        return bots.get(name);
    }
}