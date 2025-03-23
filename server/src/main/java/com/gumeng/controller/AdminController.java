package com.gumeng.controller;

import com.gumeng.domain.Admin;
import com.gumeng.domain.Result;
import com.gumeng.domain.menu.SysMenu;
import com.gumeng.service.AdminService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;



/**
 * 功能：
 * 作者：Z
 * 日期：2025/3/5 下午3:55
 */
@RequestMapping("/sys")
@RestController
public class AdminController {

    @Resource
    private AdminService adminService;

    //用户登录
    @PostMapping("/login")
    public Result<String> login(String username, String password) {
        //根据用户名查询管理员
        Admin loginAdmin = adminService.findByAdminName(username);
        //判断管理员是否存在
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
    public List<Admin> findAll() {
        //数据返回
        return adminService.findAll();
    }

    //查询管理菜单
    @GetMapping("/getMenu")
    public List<SysMenu> getMenu() {
        //数据返回
        return adminService.getMenu();
    }


}
