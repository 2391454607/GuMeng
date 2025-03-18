package com.gumeng.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 功能：管理员拦截器
 * 作者：Z
 * 日期：2025/3/18 下午5:41
 */
@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        //判断是否是管理员访问
        if (uri.startsWith("/sys")) {
            return true; // 管理员接口直接放行，不需要 Token
        }

//        // 判断是否是管理员接口
//        if (uri.startsWith("/sys")) {
//            // 假设管理员角色标识通过请求头传递，例如 "role" 或通过请求的某个属性标识
//            String role = request.getHeader("role");  // 通过请求头获取角色信息
//
//            // 如果没有角色或角色不是管理员，返回 401
//            if (role == null || !role.equals("admin")) {
//                response.setStatus(401);
//                response.getWriter().write("权限不足");
//                return false;  // 拦截请求
//            }
//
//            return true; // 如果是管理员角色，放行请求
//        }

        return true; // 如果不是管理员接口，直接放行
    }
}
