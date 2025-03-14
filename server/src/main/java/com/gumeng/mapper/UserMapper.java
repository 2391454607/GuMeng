package com.gumeng.mapper;

import com.gumeng.entily.User;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * 功能：
 * 作者：Z
 * 日期：2025/3/4 下午7:57
 */

@Mapper
public interface UserMapper {
    //根据用户名查询用户
    @Select("SELECT * FROM user WHERE username=#{username}")
    User findByUserName(String username);
    //添加注册用户
    @Insert("INSERT INTO user (username,password,create_time,update_time) VALUES (#{username},#{passwordHash},now(),now())")
    void add(String username, String passwordHash);
    //更新用户信息
    @Update("UPDATE user SET nickname=#{nickname},username=#{username},address=#{address},email=#{email},update_time=#{updateTime} WHERE id=#{id}")
    void update(User user);
    //更新用户头像
    @Update("UPDATE user SET user_pic=#{avatarUrl},update_time=now() WHERE id=#{id}")
    void updateAvatar(String avatarUrl, Integer id);
    //更新用户密码
    @Update("UPDATE user SET password=#{hashedNewPwd},update_time=now() WHERE id=#{id}")
    void updatePwd(String hashedNewPwd, Integer id);


    @Select("SELECT * FROM user")
    List<User> findAll();

    @Insert("INSERT INTO user (username,password,create_time,update_time) VALUES (#{username},#{password},now(),now())")
    void insert(User user);

    @Delete("DELETE FROM user WHERE id=#{id}")
    void delete(Integer id);

    @Select("SELECT * FROM user WHERE username LIKE CONCAT('%',#{username},'%')")
    List<User> selectUserByUsername(@Param("username") String username);


}
