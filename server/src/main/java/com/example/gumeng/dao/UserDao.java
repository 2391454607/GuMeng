package com.example.gumeng.dao;

import com.example.gumeng.entily.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 功能：
 * 作者：Z
 * 日期：2025/3/4 下午7:57
 */

@Mapper
public interface UserDao {

    @Select("SELECT * FROM user")
    List<User> findAll();

    @Insert("INSERT INTO user (nickname,username,password,city,email) VALUES (#{nickname},#{username},#{password},#{city},#{email})")
    void insert(User user);

    @Update("UPDATE user set nickname=#{nickname},username=#{username},password=#{password},city=#{city},email=#{email} WHERE id=#{id}")
    void update(User user);

    @Delete("DELETE FROM user WHERE id=#{id}")
    void delete(Integer id);
}
