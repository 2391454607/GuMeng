package com.gumeng.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gumeng.domain.Permission;
import com.gumeng.service.PermissionService;
import com.gumeng.mapper.PermissionMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Chine
* @description 针对表【permission(权限表)】的数据库操作Service实现
* @createDate 2025-04-03 21:53:56
*/
@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission>
    implements PermissionService{

    @Resource
    private PermissionMapper permissionMapper;

    //根据用户id获取角色权限
    @Override
    public List<String> getPermissionByUserId(Integer userId) {
        return permissionMapper.selectRoleNamesByUserId(userId);
    }

    @Override
    public List<String> getPermissionByRoleId(Integer roleId) {
        return permissionMapper.selectPermissionNamesByUserId(roleId);
    }

}




