package com.gumeng.service;

import com.gumeng.entily.User;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface UserService {
    //根据用户名查询用户
    User findByUserName(String username);
    //注册
    void register(String username, String password);
    //更新用户信息
    void update(User user);
    //更新用户头像
    void updateAvatar(String avatarUrl);
    //更新用户密码
    void updatePwd(String newPwd);

    List<User> findAll();

    void insert(User user);

    void delete(Integer[] ids);

    PageInfo<User> findPage(Integer pageNum, Integer pageSize, String username);

}
