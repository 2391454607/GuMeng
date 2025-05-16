package com.gumeng.controller.user;

import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaService;
import com.gumeng.annotation.LogOperation;
import com.gumeng.code.HttpResponse;
import com.gumeng.domain.user.User;
import com.gumeng.entity.DTO.UserRegisterDTO;
import com.gumeng.security.CustomUserDetails;
import com.gumeng.service.user.AuthService;
import com.gumeng.service.user.UserRoleService;
import com.gumeng.utils.JwtUtil;
import com.gumeng.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.Random;

/**
 * 功能：认证接口（登录/注册）
 * 作者：Z
 * 日期：2025/4/3 下午9:04
 */
@RestController
@RequestMapping("/auth") //给当前控制器下的所有接口添加前缀
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CaptchaService captchaService;

    //发送邮箱验证码
    @PostMapping("/sendCode")
    public HttpResponse sendVerificationCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isEmpty()) {
            return HttpResponse.error("邮箱不能为空");
        }

        // 验证邮箱格式
        String emailRegex = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
        if (!email.matches(emailRegex)) {
            return HttpResponse.error("邮箱格式不正确");
        }

        // 检查邮箱是否已被注册
        User emailUser = authService.findByEmail(email);
        if (emailUser != null) {
            return HttpResponse.error("该邮箱已被注册！");
        }

        // 生成6位随机验证码
        String code = String.format("%06d", new Random().nextInt(1000000));
        
        try {
            // 发送验证码
            authService.sendVerificationCode(email, code);
            
            // 将验证码存入Redis，设置5分钟过期
            stringRedisTemplate.opsForValue().set(
                "email:code:" + email,
                code,
                5,
                TimeUnit.MINUTES
            );
            
            return HttpResponse.success();
        } catch (Exception e) {
            e.printStackTrace(); // 查看具体错误信息
            return HttpResponse.error("验证码发送失败：请检查您的邮箱是否正确");
        }
    }

    @LogOperation(module = "注册", operation = "用户注册")
    //用户注册
    @PostMapping("/register")
    public HttpResponse register(@RequestBody @Validated UserRegisterDTO user) {
        // 验证验证码
        String cachedCode = stringRedisTemplate.opsForValue().get("email:code:" + user.getEmail());
        if (cachedCode == null || !cachedCode.equals(user.getCode())) {
            return HttpResponse.error("验证码错误或已过期");
        }

        // 查询用户是否注册
        User u = authService.findByUserName(user.getUsername());
        if (u == null) {
            // 注册
            authService.register(user.getUsername(), user.getPassword(),user.getEmail());
            // 获取新注册用户
            User newUser = authService.findByUserName(user.getUsername());
            // 注册自动分配用户user权限
            userRoleService.setDefaultRole(newUser.getId());
            
            // 注册成功后删除验证码
            stringRedisTemplate.delete("email:code:" + user.getEmail());
            
            return HttpResponse.success();
        } else {
            return HttpResponse.error("该用户名已被注册！");
        }
    }

    @PostMapping("/captcha/get")
    public HttpResponse getCaptcha(@RequestBody CaptchaVO captchaVO) {
        return HttpResponse.success(captchaService.get(captchaVO));
    }

    // 校验验证码
    @PostMapping("/captcha/check")
    public HttpResponse checkCaptcha(@RequestBody CaptchaVO captchaVO) {
        return HttpResponse.success(captchaService.check(captchaVO));
    }

    @LogOperation(module = "登录" , operation = "用户登录")
    //用户登录
    @PostMapping("/login")
    public HttpResponse login(@RequestBody Map<String,String> loginRequest) {
        // 验证滑动验证码
        CaptchaVO captchaVO = new CaptchaVO();
        captchaVO.setCaptchaVerification(loginRequest.get("captchaVerification"));
        ResponseModel response = captchaService.verification(captchaVO);
        if (!response.isSuccess()) {
            return HttpResponse.error("请完成滑动验证");
        }

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
            claims.put("role", userDetails.getRole());
            claims.put("permission", userDetails.getPermission());
            String token = JwtUtil.genToken(claims);
            // 存储到 Redis
            ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
            //设置Redis过期时间和令牌一致
            operations.set(token, token, 12, TimeUnit.HOURS);

            return HttpResponse.success(token);
        } catch (AuthenticationException e) {
            if (loginRequest.get("username") == null || loginRequest.get("password") == null){
                return HttpResponse.error("用户名或密码不能为空！");
            }else {
                return HttpResponse.error("用户名或密码错误");
            }
        }
    }
    @LogOperation(module = "登出", operation = "用户登出")
    //用户登出
    @PostMapping("/logout")
    public HttpResponse logout(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            // 去除Bearer 前缀
            token = token.substring(7);
            try {
                // 从Redis中删除token
                stringRedisTemplate.delete(token);
                // 清除安全上下文
                SecurityContextHolder.clearContext();
                // 清除ThreadLocal
                ThreadLocalUtil.remove();
                return HttpResponse.success("登出成功");
            } catch (Exception e) {
                return HttpResponse.error("登出失败");
            }
        }
        return HttpResponse.error("无效的token");
    }
}
