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
 * 功能：UserDetails 实现,封装用户信息，供 Spring Security 进行身份认证和授权。
 * 作者：Z
 * 日期：2025/4/3 下午9:06
 */
@Data
public class CustomUserDetails implements UserDetails {

    private Integer id;
    private String username;
    private String password;
    private List<String> role;
    private List<String> permission;
    private boolean enabled = true;

    public static CustomUserDetails fromUser(User user, List<String> roles, List<String> permissions) {
        CustomUserDetails details = new CustomUserDetails();
        details.setId(user.getId());
        details.setUsername(user.getUsername());
        details.setPassword(user.getPassword());
        details.setRole(roles);
        details.setPermission(permissions);
        return details;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        // 添加角色，添加 ROLE_ 前缀
        if (role != null) {
            authorities.addAll(role.stream()
                    .map(r -> {
                        // 移除已有的 ROLE_ 前缀（如果有）
                        String roleName = r.startsWith("ROLE_") ? r.substring(5) : r;
                        return new SimpleGrantedAuthority(roleName.toUpperCase());
                    })
                    .collect(Collectors.toList()));
        }
        
        // 添加权限
        if (permission != null) {
            authorities.addAll(permission.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList()));
        }
        return authorities;
    }

    // 账户不过期
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 账户不锁定
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // 凭证不过期
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 账户是否启用
    @Override
    public boolean isEnabled() {
        return this.enabled;
    }


}
