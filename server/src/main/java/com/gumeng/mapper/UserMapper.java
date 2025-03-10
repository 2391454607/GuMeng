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
    @Insert("INSERT INTO user (username,password,create_time,update_time) VALUES (#{username},#{password},now(),now())")
    void add(Map<String, Object> params);


    @Select("SELECT * FROM user")
    List<User> findAll();

    @Insert("INSERT INTO user (username,password,create_time,update_time) VALUES (#{username},#{password},now(),now())")
    void insert(User user);

    @Update("UPDATE user set nickname=#{nickname},username=#{username},password=#{password},address=#{address},email=#{email} WHERE id=#{id}")
    void update(User user);

    @Delete("DELETE FROM user WHERE id=#{id}")
    void delete(Integer id);

    @Select("SELECT * FROM user WHERE username LIKE CONCAT('%',#{username},'%')")
    List<User> selectUserByUsername(@Param("username") String username);


}
