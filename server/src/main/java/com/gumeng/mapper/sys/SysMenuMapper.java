package com.gumeng.mapper.sys;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gumeng.domain.menu.SysMenu;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
* @author Chine
* @description 针对表【sys_menu(管理端目录表)】的数据库操作Mapper
* @createDate 2025-04-13 00:23:46
* @Entity generator.domain.SysMenu
*/
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    List<SysMenu> getSysMenu();
}




