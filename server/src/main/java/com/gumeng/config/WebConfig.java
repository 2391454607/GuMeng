package com.gumeng.config;

import com.gumeng.interceptors.AdminInterceptor;
import com.gumeng.interceptors.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 功能：
 * 作者：Z
 * 日期：2025/3/10 下午10:07
 */

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Autowired
    private AdminInterceptor adminInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //用户端拦截器
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/user/**")   //拦截所有 /user 开头的请求
                .excludePathPatterns("/user/login", "/user/register", "/user/getMenu");  //排除不需要拦截的请求

//        //管理端拦截器
//        registry.addInterceptor(adminInterceptor)
//                .addPathPatterns("/sys/**")   //拦截所有 /sys 开头的请求
//                .excludePathPatterns("/sys/login");  //排除不需要拦截的请求
    }
}
