package com.gumeng.service;

import com.gumeng.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gumeng.domain.menu.UserMenu;
import org.springframework.stereotype.Service;

import java.util.List;


/**
* @author Chine
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2025-03-19 20:00:07
*/

@Service
public interface UserService extends IService<User> {

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

    //获取用户界面的菜单数据
    List<UserMenu> getMenu();

}
