package com.gumeng.mapper.user;

import com.gumeng.domain.user.UserRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author Chine
* @description 针对表【user_role(用户角色关联表)】的数据库操作Mapper
* @createDate 2025-04-03 21:53:56
* @Entity com.gumeng.domain.user.UserRole
*/
public interface UserRoleMapper extends BaseMapper<UserRole> {

    void setDefaultRole(UserRole userRoleRelation);
}




