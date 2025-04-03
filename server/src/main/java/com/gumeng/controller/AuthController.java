package com.gumeng.controller;

import com.gumeng.code.HttpResponse;
import com.gumeng.domain.User;
import com.gumeng.security.CustomUserDetails;
import com.gumeng.service.UserService;
import com.gumeng.utils.JwtUtil;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 功能：认证接口（登录/注册）
 * 作者：Z
 * 日期：2025/4/3 下午9:04
 */
@RestController
@RequestMapping("/auth") //给当前控制器下的所有接口添加前缀
@PreAuthorize("hasAnyAuthority('ADMIN', 'superAdmin')")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private AuthenticationManager authenticationManager;


    //用户注册
    @PostMapping("/register")
    public HttpResponse register(@RequestParam @Pattern(regexp = "^\\S{5,16}$") String username,
                                 @RequestParam @Pattern(regexp = "^(?=.*[A-Za-z])\\S{8,16}$") String password) {
        //查询用户是否注册
        User u = userService.findByUserName(username);
        if (u==null){
            //注册
            userService.register(username,password);
            return HttpResponse.success();
        }else{
            //该用户名已被注册
            return HttpResponse.error("该用户名已被注册！");
        }
    }

    //用户登录
    @PostMapping("/login")
    public HttpResponse login(@RequestBody Map<String,String> loginRequest) {

        try {
            // 获取用户凭证
            String username = loginRequest.get("username");
            String password = loginRequest.get("password");

            // 使用 Spring Security 进行认证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            // 认证成功,获取用户详情
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            // 生成 JWT Token
            Map<String,Object> claims = new HashMap<>();
            claims.put("id", userDetails.getId());
            claims.put("username", userDetails.getUsername());
            claims.put("roles", userDetails.getRoles());
            claims.put("permissions", userDetails.getPermissions());
            String token = JwtUtil.genToken(claims);

            // 存储到 Redis
            ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
            operations.set(token, token, 1, TimeUnit.HOURS);

            return HttpResponse.success(token);
        } catch (AuthenticationException e) {
            return HttpResponse.error("用户名或密码错误");
        }
    }
}
