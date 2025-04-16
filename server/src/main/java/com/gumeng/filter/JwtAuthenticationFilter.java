package com.gumeng.filter;

import com.gumeng.security.CustomUserDetails;
import com.gumeng.utils.JwtUtil;
import com.gumeng.utils.ThreadLocalUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
*功能：Jwt过滤器
*作者：Z
*日期：2025/4/3 下午9:18
*/
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String token = request.getHeader("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                // 先检查 Redis 中是否存在该 token
                String redisToken = stringRedisTemplate.opsForValue().get(token);
                if (redisToken == null) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }

                Map<String, Object> claims = JwtUtil.parseToken(token);
                
                // 将用户信息存入ThreadLocal
                ThreadLocalUtil.set(claims);

                CustomUserDetails userDetails = new CustomUserDetails();
                userDetails.setId((Integer) claims.get("id"));
                userDetails.setUsername((String) claims.get("username"));
                userDetails.setRole((List<String>) claims.get("role"));
                userDetails.setPermission((List<String>) claims.get("permission"));

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        try {
            chain.doFilter(request, response);
        } finally {
            // 清理ThreadLocal
            ThreadLocalUtil.remove();
        }
    }
}
