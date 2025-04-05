package com.gumeng.mapper;

import com.gumeng.domain.Permission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
* @author Chine
* @description 针对表【permission(权限表)】的数据库操作Mapper
* @createDate 2025-04-03 21:53:56
* @Entity com.gumeng.domain.Permission
*/
public interface PermissionMapper extends BaseMapper<Permission> {
    //查询用户角色
    List<String> selectRoleNamesByUserId(Integer userId);

    //查询用户权限
    List<String> selectPermissionNamesByUserId(Integer userId);

}




