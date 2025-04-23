package com.gumeng.config;

import com.gumeng.utils.SensitiveWordFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 敏感词过滤器配置类
 */
@Configuration
public class SensitiveWordConfig {
    
    @Value("${custom.sensitive-word.path:sensitive_word/sensitive-words.txt}")
    private String sensitiveWordPath;
    
    /**
     * 初始化敏感词过滤器
     */
    @Bean
    public SensitiveWordFilter sensitiveWordFilter() {
        // 设置敏感词文件路径
        SensitiveWordFilter.setSensitiveWordFilePath(sensitiveWordPath);
        // 获取单例实例
        return SensitiveWordFilter.getInstance();
    }
} 