package com.gumeng.config;

import com.anji.captcha.service.CaptchaService;
import com.anji.captcha.service.impl.CaptchaServiceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class CaptchaConfig {

    @Bean
    public CaptchaService captchaService() {
        Properties config = new Properties();
        config.setProperty("captcha.cacheType", "local");
        config.setProperty("captcha.type", "default");
        config.setProperty("captcha.interference.options", "2");
        config.setProperty("captcha.font.style", "1");
        config.setProperty("captcha.slip.offset", "5");
        config.setProperty("captcha.water.mark", "故梦阑珊");
        // 使用系统默认字体
        config.setProperty("captcha.font.type", "宋体");

        return CaptchaServiceFactory.getInstance(config);
    }
}