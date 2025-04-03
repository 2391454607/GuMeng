package com.gumeng.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 功能：BCrypt 密码加密工具类
 * 作者：Z
 * 日期：2025/4/3 下午10:24
 */
public class BCryptUtil {
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * 加密密码
     * @param password 明文密码
     * @return 加密后的密码
     */
    public static String hash(String password) {
        return encoder.encode(password);
    }

    /**
     * 验证密码
     * @param password 明文密码
     * @param hashedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean verify(String password, String hashedPassword) {
        return encoder.matches(password, hashedPassword);
    }
} 