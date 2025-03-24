package com.gumeng.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gumeng.domain.Admin;
import com.gumeng.domain.menu.SysMenu;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Chine
* @description 针对表【admin】的数据库操作Service
* @createDate 2025-03-19 20:00:07
*/
@Service
public interface AdminService extends IService<Admin> {

    Admin findByAdminName(String username);

    List<Admin> findAll();

    List<SysMenu> getMenu();
}
