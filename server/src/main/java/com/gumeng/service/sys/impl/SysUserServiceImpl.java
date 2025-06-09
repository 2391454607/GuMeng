package com.gumeng.service.sys.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gumeng.domain.user.User;
import com.gumeng.mapper.user.UserMapper;
import com.gumeng.service.sys.SysUserService;
import org.springframework.stereotype.Service;

/**
 * 功能：
 * 作者：Z
 * 日期：2025/4/21 下午7:56
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<UserMapper, User> implements SysUserService {

}
