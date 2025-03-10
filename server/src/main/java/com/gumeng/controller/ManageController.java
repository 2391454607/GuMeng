package com.gumeng.controller;

import com.gumeng.entily.Manage;
import com.gumeng.service.ManageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;


/**
 * 功能：
 * 作者：Z
 * 日期：2025/3/5 下午3:55
 */
@RequestMapping("/manage")
@RestController
public class ManageController {

    @Resource
    private ManageService manageService;

    //查询管理员信息
    @GetMapping("findAll")
    public List<Manage> findAll() {
        //数据返回
        return manageService.findAll();
    }


}
