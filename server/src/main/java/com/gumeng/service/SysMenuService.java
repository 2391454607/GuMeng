package com.gumeng.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gumeng.domain.menu.SysMenu;

import java.util.List;

/**
* @author Chine
* @description 针对表【sys_menu(管理端目录表)】的数据库操作Service
* @createDate 2025-04-13 00:23:46
*/
public interface SysMenuService extends IService<SysMenu> {

    List<SysMenu> getSysMenu();

}
