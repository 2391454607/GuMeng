package com.gumeng.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gumeng.domain.Admin;
import com.gumeng.domain.menu.SysMenu;
import com.gumeng.service.AdminService;
import com.gumeng.mapper.AdminMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Chine
* @description 针对表【admin】的数据库操作Service实现
* @createDate 2025-03-19 20:00:07
*/
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin>
    implements AdminService{

    @Resource
    private AdminMapper adminMapper;

    //根据用户名查询管理员
    @Override
    public Admin findByAdminName(String name) {
        return adminMapper.findByAdminName(name);
    }
    //查询管理员信息
    @Override
    public List<Admin> findAll() {
        return adminMapper.findAll();
    }
    //获取管理菜单
    @Override
    public List<SysMenu> getMenu() {
        return adminMapper.getMenu();
    }
}




