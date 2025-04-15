package com.gumeng.service;

import com.gumeng.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;


/**
 * @author Chine
 * @description 针对表【user(用户表)】的数据库操作Service
 * @createDate 2025-03-19 20:00:07
 */

@Service
public interface AuthService extends IService<User> {

    //根据用户名查询用户
    User findByUserName(String username);
    //查询邮箱
    User findByEmail(String email);
    //验证码
    void sendVerificationCode(String email, String code);
    //注册
    void register(String username, String password, String email);

}
