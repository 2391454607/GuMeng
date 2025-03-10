package com.gumeng.controller;

import com.gumeng.entily.Result;
import com.gumeng.utils.JwtUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 功能：
 * 作者：Z
 * 日期：2025/3/10 下午9:48
 */
@RestController
@RequestMapping("/pages")
public class PagesController {
    @GetMapping("/list")
    public Result<String> list(/*@RequestHeader(name = "Authorization") String token, HttpServletResponse response*/) {
//        //验证token
//        try{
//            Map<String, Object> claims = JwtUtil.parseToken(token);
//            return Result.success("获取内容...");
//        }catch (Exception e){
//            //http响应状态401
//            response.setStatus(401);
//            return Result.error("未登录");
//        }

        return Result.success("访问数据");
    }
}
