package com.gumeng.interceptors;

import com.gumeng.utils.JwtUtil;
import com.gumeng.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

/**
 * 功能：用户 & 管理员登录拦截器
 * 作者：Z
 * 日期：2025/3/10 下午10:01
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {


        //从请求头中获取token
        String token = request.getHeader("Authorization");
        //验证token
        try{
            //从redis中获取相同的token
            ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
            String redisToken = operations.get(token);
            if (redisToken == null) {
                //token失效
                throw new RuntimeException("Token 无效或已过期");
            }

            //解析 Token
            Map<String, Object> claims = JwtUtil.parseToken(token);

            //将业务数据存储到 ThreadLocal 中
            ThreadLocalUtil.set(claims);

            //验证成功放行
            return true;
        }catch (Exception e){
            //http响应状态401
            response.setStatus(401);
            response.getWriter().write("Token 无效或已过期");
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
