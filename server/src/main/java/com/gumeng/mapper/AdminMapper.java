package com.gumeng.mapper;

import com.gumeng.domain.Admin;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gumeng.domain.menu.SysMenu;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
* @author Chine
* @description 针对表【admin】的数据库操作Mapper
* @createDate 2025-03-19 20:00:07
* @Entity com.gumeng.domain.Admin
*/

@Mapper
public interface AdminMapper extends BaseMapper<Admin> {
    //根据用户名查询管理员
    Admin findByAdminName(String username);




