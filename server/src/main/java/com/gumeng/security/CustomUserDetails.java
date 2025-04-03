package com.gumeng.security;

import com.gumeng.domain.User;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 功能：UserDetails 实现
 * 作者：Z
 * 日期：2025/4/3 下午9:06
 */
@Data
public class CustomUserDetails implements UserDetails {

    private Integer id;
    private String username;
    private String password;
    private List<String> roles;
    private List<String> permissions;
    private boolean enabled = true;

    public static CustomUserDetails fromUser(User user, List<String> roles, List<String> permissions) {
        CustomUserDetails details = new CustomUserDetails();
        details.setId(user.getId());
        details.setUsername(user.getUsername());
        details.setPassword(user.getPassword());
        details.setRoles(roles);
        details.setPermissions(permissions);
        return details;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        
        // 添加角色，不再添加 ROLE_ 前缀
        if (roles != null) {
            authorities.addAll(roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList()));
        }
        
        // 添加权限
        if (permissions != null) {
            authorities.addAll(permissions.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList()));
        }
        
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }


}
