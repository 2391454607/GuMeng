package com.gumeng.interceptors;

import com.gumeng.utils.JwtUtil;
import com.gumeng.utils.ThreadLocalUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


import java.util.Map;

/**
 * 功能：拦截器
 * 作者：Z
 * 日期：2025/3/10 下午10:01
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //令牌验证
        String token = request.getHeader("Authorization");
        //验证token
        try{
            Map<String, Object> claims = JwtUtil.parseToken(token);

            //将业务数据存储到 ThreadLocal 中
            ThreadLocalUtil.set(claims);

            //验证成功放行
            return true;
        }catch (Exception e){
            //http响应状态401
            response.setStatus(401);
            //验证失败不放行
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

        //清除 ThreadLocal 中的数据
        ThreadLocalUtil.remove();
    }
}
