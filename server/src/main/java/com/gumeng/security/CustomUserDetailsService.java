package com.gumeng.security;

import com.gumeng.domain.User;
import com.gumeng.service.PermissionService;
import com.gumeng.service.RoleService;
import com.gumeng.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 功能：用户登录时，加载用户的角色和权限
 * 作者：Z
 * 日期：2025/4/3 下午9:06
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private PermissionService permissionService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 查询用户
        User user = userService.findByUserName(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }

        // 查询用户角色
        List<String> role = roleService.getRoleByUserId(user.getId());

        // 查询用户权限
        List<String> permission = permissionService.getPermissionByUserId(user.getId());
        return CustomUserDetails.fromUser(user, role, permission);
    }
}
