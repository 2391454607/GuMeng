package com.gumeng.config.Al;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 功能：
 * 作者：Z
 * 日期：2025/6/3 上午9:12
 */
@Component
@Data
@ConfigurationProperties(prefix = "tripo3d.api")
public class Tripo3d {
    private String key;
    private String uploadUrl;
    private String generationUrl;
    private String balanceUrl;
    private String polling;
    private String uploadSts;
    private String stsUrl;
}
