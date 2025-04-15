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
    @NotEmpty(message = "邮箱不能为空")
    private String email;

    @NotEmpty(message = "验证码不能为空")
    private String code;

    public @NotEmpty(message = "账号不能为空") String getUsername() {
        return username;
    }

    public @NotEmpty(message = "密码不能为空") String getPassword() {
        return password;
    }

    public @NotEmpty(message = "邮箱不能为空") String getEmail() {
        return email;
    }

    public @NotEmpty(message = "验证码不能为空") String getCode() {
        return code;
    }

    public void setUsername(@NotEmpty(message = "账号不能为空") String username) {
        this.username = username;
    }

    public void setPassword(@NotEmpty(message = "密码不能为空") String password) {
        this.password = password;
    }

    public void setEmail(@NotEmpty(message = "邮箱不能为空") String email) {
        this.email = email;
    }

    public void setCode(@NotEmpty(message = "验证码不能为空") String code) {
        this.code = code;
    }
}
