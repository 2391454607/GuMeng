package com.gumeng.config;

import com.github.jarod.qqwry.QQWry;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * 纯真IP库配置类
 */
@Slf4j
@Configuration
public class IpConfig {

    /**
     * 纯真IP库配置属性
     */
    @Data
    @ConfigurationProperties(prefix = "custom.ip.qqwry")
    public static class QQWryProperties {
        /**
         * 是否启用
         */
        private boolean enabled = true;
        
        /**
         * 纯真IP库路径
         */
        private String path = "chunZhenIp/qqwry.dat";
        
        /**
         * 是否自动加载
         */
        private boolean autoLoad = true;
        
        /**
         * 编码格式
         */
        private String charset = "gbk";
    }
    
    /**
     * 注册纯真IP库配置属性
     */
    @Bean
    public QQWryProperties qqWryProperties() {
        return new QQWryProperties();
    }

    /**
     * 初始化纯真IP库
     */
    @Bean
    public QQWry qqWry(QQWryProperties properties) {
        if (!properties.isEnabled()) {
            log.info("纯真IP库未启用");
            return null;
        }
        
        try {
            // 从classpath加载IP库文件
            ClassPathResource resource = new ClassPathResource(properties.getPath());
            
            // 将数据库文件复制到临时目录
            Path tempFile = Files.createTempFile("qqwry", ".dat");
            Files.copy(resource.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);
            
            // 注册JVM关闭钩子，退出时删除临时文件
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException e) {
                    log.error("删除临时文件失败", e);
                }
            }));
            
            // 初始化IP库
            QQWry qqWry = new QQWry(tempFile);
            log.info("纯真IP库加载成功，IP库版本：{}", qqWry.getDatabaseVersion());
            return qqWry;
        } catch (Exception e) {
            log.error("纯真IP库加载失败", e);
            return null;
        }
    }
} 