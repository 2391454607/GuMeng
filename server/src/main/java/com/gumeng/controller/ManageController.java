package com.gumeng.controller;

import com.gumeng.entity.Manage;
import com.gumeng.entity.Result;
import com.gumeng.entity.User;
import com.gumeng.service.ManageService;
import com.gumeng.utils.Argon2Util;
import com.gumeng.utils.JwtUtil;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


/**
 * 功能：
 * 作者：Z
 * 日期：2025/3/5 下午3:55
 */
@RequestMapping("/sys")
@RestController
public class ManageController {

    @Resource
    private ManageService manageService;

    //用户登录
    @PostMapping("/login")
    public Result<String> login(String name, String password) {
        //根据用户名查询用户
        Manage loginAdmin = manageService.findByAdminName(name);
        //判断用户是否存在
        if (loginAdmin==null){
            return Result.error("用户名不存在");
        }
        //判断密码是否正确
        if (Objects.equals(password, loginAdmin.getPassword())){
            //登录成功

            return Result.success();
        }
        return Result.error("密码错误");
    }

    //查询管理员信息
    @GetMapping("/findAll")
    public List<Manage> findAll() {
        //数据返回
        return manageService.findAll();
    }


}
