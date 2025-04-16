package com.gumeng.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gumeng.domain.User;
import com.gumeng.service.AuthService;
import com.gumeng.mapper.AuthMapper;
import com.gumeng.utils.BCryptUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * @author Chine
 * @description 针对表【user(用户表)】的数据库操作Service实现
 * @createDate 2025-03-19 20:00:07
 */
@Service
public class AuthServiceImpl extends ServiceImpl<AuthMapper, User>
        implements AuthService{

    @Resource
    private AuthMapper AuthMapper;

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public User findByUserName(String username) {
        return AuthMapper.findByUserName(username);
    }

    @Override
    public User findByEmail(String email) {
        return AuthMapper.findByUserEmail(email);
    }

    @Value("${spring.mail.username}")
    private String from;

    @Override
    public void sendVerificationCode(String to, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject("故梦阑珊网站注册验证码");
            message.setText("您的验证码是：" + code + "，有效期为5分钟。请勿将验证码泄露给他人。");
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("邮件发送失败：" + e.getMessage());
        }
    }

    @Override
    public void register(String username, String password, String email) {
        //对密码使用 BCrypt 加密
        String hashedPassword = BCryptUtil.hash(password);
        //添加注册用户
        AuthMapper.add(username,hashedPassword,email);
    }



}




