package com.gumeng.service;

import com.gumeng.domain.Role;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author Chine
* @description 针对表【role(角色表)】的数据库操作Service
* @createDate 2025-04-03 21:53:56
*/
public interface RoleService extends IService<Role> {

    List<String> getRoleByUserId(Integer id);

}
