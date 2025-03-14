package com.gumeng.service.impl;

import com.gumeng.mapper.UserMapper;
import com.gumeng.entily.User;
import com.gumeng.service.UserService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.gumeng.utils.Argon2Util;
import com.gumeng.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    //根据用户名查询用户
    @Override
    public User findByUserName(String username) {
        User u = userMapper.findByUserName(username);
        return u;
    }
    //注册
    @Override
    public void register(String username, String password) {
        //对密码使用 Argon2 加密
        String hashedPassword = Argon2Util.hash(password);
        //添加注册用户
        userMapper.add(username,hashedPassword);
    }
    //更新用户信息
    @Override
    public void update(User user) {
        user.setUpdateTime(LocalDateTime.now());
        userMapper.update(user);
    }
    //更新用户头像
    @Override
    public void updateAvatar(String avatarUrl) {
        Map<String, Object> map = ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");
        userMapper.updateAvatar(avatarUrl,id);
    }
    //更新用户密码
    @Override
    public void updatePwd(String newPwd) {
        Map<String, Object> map = ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");
        userMapper.updatePwd(newPwd,id);
    }


    @Override
    public List<User> findAll() {
        return userMapper.findAll();
    }

    @Override
    public void insert(User user) {
        userMapper.insert(user);
    }

    @Override
    public void delete(Integer[] ids) {
        for (Integer id : ids) {
            userMapper.delete(id);
        }
    }

    @Override
    public PageInfo<User> findPage(Integer pageNum, Integer pageSize, String username) {
        //分页查询
        PageHelper.startPage(pageNum,pageSize);
        //模糊查询
        List<User> userList;
        if(username != null && !username.isEmpty()){
            userList = userMapper.selectUserByUsername(username);
        }else{
            userList = userMapper.findAll();
        }

        PageInfo<User> userPageInfo = new PageInfo<>(userList);
        return userPageInfo;
    }

}
