package com.gumeng.service.user.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gumeng.domain.user.Role;
import com.gumeng.mapper.user.RoleMapper;
import com.gumeng.service.user.RoleService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Chine
* @description 针对表【role(角色表)】的数据库操作Service实现
* @createDate 2025-04-03 21:53:56
*/
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role>
    implements RoleService {

    @Resource
    private RoleMapper roleMapper;

    //根据用户id获取用户角色
    @Override
    public List<String> getRoleByUserId(Integer userId) {
        return roleMapper.selectRoleNamesByUserId(userId);
    }

}




