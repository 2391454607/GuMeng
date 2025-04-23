package com.gumeng.service.user;

import com.gumeng.domain.user.Permission;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author Chine
* @description 针对表【permission(权限表)】的数据库操作Service
* @createDate 2025-04-03 21:53:56
*/
public interface PermissionService extends IService<Permission> {

    List<String> getPermissionByUserId(Integer id);

    List<String> getPermissionByRoleId(Integer roleId);
}
