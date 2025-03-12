package com.gumeng.controller;

import com.gumeng.entily.Result;
import com.gumeng.service.UserService;
import com.github.pagehelper.PageInfo;
import com.gumeng.entily.User;
import com.gumeng.utils.JwtUtil;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 功能：
 * 作者：Z
 * 日期：2025/3/4 下午7:59
 */

@RestController
@RequestMapping("/user") //给当前控制器下的所有接口添加前缀
@Validated
public class UserController {

    @Autowired
    private UserService userService;

    //用户注册
    @PostMapping("/register")
    public Result register(@Pattern(regexp = "^\\S{5,16}$") String username, @Pattern(regexp = "^\\S{5,16}$") String password) {
        //查询用户是否注册
        User u = userService.findByUserName(username);
        if (u==null){
            //注册
            userService.register(username,password);
            return Result.success();
        }else{
            //该用户名已被注册
            return Result.error("该用户名已被注册！");
        }
    }

    //用户登录
    @PostMapping("/login")
    public Result<String> login(@Pattern(regexp = "^\\S{5,16}$") String username, @Pattern(regexp = "^\\S{5,16}$") String password) {
        //根据用户名查询用户
        User loginUser = userService.findByUserName(username);
        //判断用户是否存在
        if (loginUser==null){
            return Result.error("用户名不存在");
        }
        //判断密码是否正确
        if (password.equals(loginUser.getPassword())){
            //登录成功
            Map<String,Object> claims = new HashMap<>();
            claims.put("id",loginUser.getId());
            claims.put("username",loginUser.getUsername());
            String token = JwtUtil.genToken(claims);
            return Result.success(token);
        }
        return Result.error("密码错误");
    }

    //查询用户信息
    @GetMapping("/findAll")
    public List<User> findAll() {
        //数据返回
        return userService.findAll();
    }

    //插入用户信息
    @PostMapping("/insert")
    public Boolean insert(@RequestBody User user) {
        userService.insert(user);
        return true;
    }

    //编辑用户信息
    @PutMapping("/update")
    public Boolean update(@RequestBody User user) {
        userService.update(user);
        return true;
    }

    //删除用户信息
    @DeleteMapping("/delete")
    public Boolean delete(@RequestBody Integer[] ids) { // 通过 id 删除用户
        userService.delete(ids);
        return true;
    }

    //分页查询
    @GetMapping("/findPage")
    public PageInfo<User> findPage(
            @RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(name = "username", defaultValue = "") String username
            ){
        return userService.findPage(pageNum,pageSize,username);
    }


}
