package com.gumeng.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gumeng.domain.UserRole;
import com.gumeng.service.UserRoleService;
import com.gumeng.mapper.UserRoleMapper;
import org.springframework.stereotype.Service;

/**
* @author Chine
* @description 针对表【user_role(用户角色关联表)】的数据库操作Service实现
* @createDate 2025-04-03 21:53:56
*/
@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole>
    implements UserRoleService{

}




