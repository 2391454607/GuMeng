package com.gumeng.mapper;

import com.gumeng.domain.Role;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
* @author Chine
* @description 针对表【role(角色表)】的数据库操作Mapper
* @createDate 2025-04-03 21:53:56
* @Entity com.gumeng.domain.Role
*/
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    //根据用户id查询用户角色
    List<String> selectRoleNamesByUserId(Integer userId);

}




