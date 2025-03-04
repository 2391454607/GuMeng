package com.example.gumeng.controller;

import com.example.gumeng.service.UserService;
import org.springframework.web.bind.annotation.*;

import com.example.gumeng.entily.User;

import javax.annotation.Resource;
import java.util.List;

/**
 * 功能：
 * 作者：Z
 * 日期：2025/3/4 下午7:59
 */

@RequestMapping("/user") //给当前控制器下的所有接口添加前缀
@RestController
public class UserController {

    @Resource
    private UserService userService;

    //查询用户信息
    @GetMapping("findAll")
    public List<User> findAll() {
        //数据返回
        return userService.findAll();
    }

    //插入用户信息
    @PostMapping("insert")
    public Boolean insert(@RequestBody User user) {
        userService.insert(user);
        return true;
    }

    //编辑用户信息
    @PutMapping("update")
    public Boolean update(@RequestBody User user) {
        userService.update(user);
        return true;
    }

    //删除用户信息
    @DeleteMapping("delete")
    public Boolean delete(@RequestBody Integer[] ids) { // 通过 id 删除用户
        userService.delete(ids);
        return true;
    }


}
