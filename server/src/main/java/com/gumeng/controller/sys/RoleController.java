package com.gumeng.controller.sys;

import com.gumeng.code.HttpResponse;
import com.gumeng.domain.user.Role;
import com.gumeng.domain.user.UserRole;
import com.gumeng.service.user.RolePermissionService;
import com.gumeng.service.user.RoleService;
import com.gumeng.service.user.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 功能：
 * 作者：Z
 * 日期：2025/4/3 下午10:47
 */
@RestController
@RequestMapping("/sys/role")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private RolePermissionService rolePermissionService;

    // 查询所有角色
    @GetMapping("/list")
    public HttpResponse list() {
        List<Role> roles = roleService.list();
        return HttpResponse.success(roles);
    }

    // 为用户分配角色
    @PostMapping("/assign/{userId}/{roleId}")
    public HttpResponse assignRole(@PathVariable Integer userId, @PathVariable Integer roleId) {
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        userRoleService.save(userRole);
        return HttpResponse.success();
    }

    // 查询用户的角色
    @GetMapping("/user/{userId}")
    public HttpResponse getUserRoles(@PathVariable Integer userId) {
        List<String> roles = roleService.getRoleByUserId(userId);
        return HttpResponse.success(roles);
    }
}
