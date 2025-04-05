package com.gumeng.entity.DTO;

import jakarta.validation.constraints.NotEmpty;

/**
 * 功能：
 * 作者：Z
 * 日期：2025/4/5 上午2:40
 */
public class UserRegisterDTO {

    @NotEmpty(message = "账号不能为空")
    private String username;
    @NotEmpty(message = "密码不能为空")
    private String password;

    public @NotEmpty(message = "账号不能为空") String getUsername() {
        return username;
    }

    public @NotEmpty(message = "密码不能为空") String getPassword() {
        return password;
    }

    public void setUsername(@NotEmpty(message = "账号不能为空") String username) {
        this.username = username;
    }

    public void setPassword(@NotEmpty(message = "密码不能为空") String password) {
        this.password = password;
    }

}
