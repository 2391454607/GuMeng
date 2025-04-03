package com.gumeng.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gumeng.domain.User;
import com.gumeng.domain.menu.UserMenu;
import com.gumeng.service.UserService;
import com.gumeng.mapper.UserMapper;
import com.gumeng.utils.BCryptUtil;
import com.gumeng.utils.ThreadLocalUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
* @author Chine
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2025-03-19 20:00:07
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Resource
    private UserMapper userMapper;

    @Override
    public User findByUserName(String username) {
        return userMapper.findByUserName(username);
    }

    @Override
    public void register(String username, String password) {
        //对密码使用 BCrypt 加密
        String hashedPassword = BCryptUtil.hash(password);
        //添加注册用户
        userMapper.add(username,hashedPassword);
    }

    @Override
    public void update(User user) {
        user.setUpdateTime(LocalDateTime.now());
        userMapper.update(user);
    }

    @Override
    public void updateAvatar(String avatarUrl) {
        Map<String, Object> map = ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");
        userMapper.updateAvatar(avatarUrl,id);
    }

    @Override
    public void updatePwd(String newPwd) {
        Map<String, Object> map = ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");
        userMapper.updatePwd(newPwd,id);
    }

    @Override
    public List<UserMenu> getMenu() {
        return userMapper.getMenu();
    }
}




