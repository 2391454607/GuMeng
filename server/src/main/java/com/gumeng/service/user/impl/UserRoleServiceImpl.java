package com.gumeng.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gumeng.domain.user.Role;
import com.gumeng.domain.user.UserRole;
import com.gumeng.mapper.user.RoleMapper;
import com.gumeng.mapper.user.UserRoleMapper;
import com.gumeng.service.user.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
* @author Chine
* @description 针对表【user_role(用户角色关联表)】的数据库操作Service实现
* @createDate 2025-04-03 21:53:56
*/
@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole>
    implements UserRoleService {

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    //根据新注册用户id添加默认user角色
    @Override
    public void setDefaultRole(Integer id) {
        //查询user角色
        Role userRole = roleMapper.selectOne(
                new LambdaQueryWrapper<Role>()
                        .eq(Role::getName, "user")
        );

        if (userRole != null) {
            UserRole userRoleRelation = new UserRole();
            userRoleRelation.setUserId(id);
            userRoleRelation.setRoleId(userRole.getId());
            userRoleMapper.setDefaultRole(userRoleRelation);
        }
    }
}




