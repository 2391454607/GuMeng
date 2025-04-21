package com.gumeng.service.user.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gumeng.domain.user.RolePermission;
import com.gumeng.mapper.user.RolePermissionMapper;
import com.gumeng.service.user.RolePermissionService;
import org.springframework.stereotype.Service;

/**
* @author Chine
* @description 针对表【role_permission(角色权限关联表)】的数据库操作Service实现
* @createDate 2025-04-03 21:53:56
*/
@Service
public class RolePermissionServiceImpl extends ServiceImpl<RolePermissionMapper, RolePermission>
    implements RolePermissionService {

}




