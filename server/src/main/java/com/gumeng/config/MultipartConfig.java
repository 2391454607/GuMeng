package com.gumeng.config;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;


@Configuration
public class MultipartConfig {
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        // 设置最大文件大小
        factory.setMaxFileSize(DataSize.ofMegabytes(1500));
        // 设置最大请求大小
        factory.setMaxRequestSize(DataSize.ofMegabytes(1500));
        return factory.createMultipartConfig();
    }
}