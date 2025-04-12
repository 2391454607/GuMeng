package com.gumeng.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gumeng.domain.menu.SysMenu;
import com.gumeng.service.SysMenuService;
import com.gumeng.mapper.SysMenuMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Chine
* @description 针对表【sys_menu(管理端目录表)】的数据库操作Service实现
* @createDate 2025-04-13 00:23:46
*/
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu>
    implements SysMenuService{

    @Autowired
    private SysMenuMapper sysMenuMapper;

    @Override
    public List<SysMenu> getSysMenu() {
        return sysMenuMapper.getSysMenu();
    }
}




