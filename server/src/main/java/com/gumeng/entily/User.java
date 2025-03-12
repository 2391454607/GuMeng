package com.gumeng.entily;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;


import java.time.LocalDateTime;

/**
 * 功能：
 * 作者：Z
 * 日期：2025/3/4 下午8:02
 */
@Data
public class User {

    private Integer id;  //主键ID

    private String nickname;  //昵称

    private String username;  //用户名

    @JsonIgnore //让springMVC把当前对象转换成json字符，忽略/ 用户获取信息的时候不打印该字符
    private String password;  //密码

    private String userPic;  //用户头像地址

    private String address;  //地址

    private String email;  //邮件

    private LocalDateTime createTime;  //创建时间

    private LocalDateTime updateTime;  //更新时间
}
