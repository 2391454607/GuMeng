package com.gumeng.mapper.user;

import com.gumeng.domain.user.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;


/**
 * @author Chine
 * @description 针对表【user(用户表)】的数据库操作Mapper
 * @createDate 2025-03-19 20:00:07
 * @Entity com.gumeng.domain.user.User
 */
@Mapper
public interface AuthMapper extends BaseMapper<User> {
    //根据用户名查询用户
    User findByUserName(String username);
    //查询邮箱是否被注册
    User findByUserEmail(String email);
    //添加注册用户
    void add(String username, String passwordHash, String email);

}




