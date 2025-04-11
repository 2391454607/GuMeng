package com.gumeng.mapper;

import com.gumeng.domain.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;


/**
* @author Chine
* @description 针对表【user(用户表)】的数据库操作Mapper
* @createDate 2025-03-19 20:00:07
* @Entity com.gumeng.domain.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {
    //根据用户名查询用户
    User findByUserName(String username);
    //添加注册用户
    void add(String username, String passwordHash);
    //更新用户信息
    void update(User user);
    //更新用户头像
    void updateAvatar(String avatarUrl, Integer id);
    //更新用户密码
    void updatePwd(String hashedNewPwd, Integer id);


}




