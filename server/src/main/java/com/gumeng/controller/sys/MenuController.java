package com.gumeng.controller.sys;

import com.gumeng.code.HttpResponse;
import com.gumeng.service.sys.SysMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 功能：获取管理端菜单
 * 作者：Z
 * 日期：2025/4/13 上午12:22
 */
@RestController
@RequestMapping("/sys") //给当前控制器下的所有接口添加前缀
public class MenuController {

    @Autowired
    private SysMenuService sysMenuService;

    @GetMapping("/getSysMenu")
    public HttpResponse getSysMenu() {
        Object sysMenu = sysMenuService.getSysMenu();
        return HttpResponse.success(sysMenu);
    }
}
