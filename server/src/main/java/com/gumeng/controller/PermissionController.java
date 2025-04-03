package com.gumeng.controller;

import com.gumeng.code.HttpResponse;
import com.gumeng.domain.RolePermission;
import com.gumeng.service.PermissionService;
import com.gumeng.service.RolePermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/permission")
@PreAuthorize("hasRole('ADMIN')")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;
    
    @Autowired
    private RolePermissionService rolePermissionService;

    // 为角色分配权限
    @PostMapping("/assign/{roleId}/{permissionId}")
    public HttpResponse assignPermission(@PathVariable Integer roleId,
                                         @PathVariable Integer permissionId) {
        RolePermission rolePermission = new RolePermission();
        rolePermission.setRoleId(roleId);
        rolePermission.setPermissionId(permissionId);
        rolePermissionService.save(rolePermission);
        return HttpResponse.success();
    }

    // 查询角色的所有权限
    @GetMapping("/role/{roleId}")
    public HttpResponse getRolePermissions(@PathVariable Integer roleId) {
        List<String> permissions = permissionService.getPermissionByRoleId(roleId);
        return HttpResponse.success(permissions);
    }
} 