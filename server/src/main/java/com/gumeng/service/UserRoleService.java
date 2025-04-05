package com.gumeng.service;

import com.gumeng.domain.UserRole;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Chine
* @description 针对表【user_role(用户角色关联表)】的数据库操作Service
* @createDate 2025-04-03 21:53:56
*/
public interface UserRoleService extends IService<UserRole> {

    void setDefaultRole(Integer id);
}
