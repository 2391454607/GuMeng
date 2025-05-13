package com.gumeng.controller.web;

import com.gumeng.code.HttpResponse;
import com.gumeng.domain.pages.IchCategory;
import com.gumeng.domain.pages.IchLevel;
import com.gumeng.entity.vo.IchProjectListVO;
import com.gumeng.service.IchCategoryService;
import com.gumeng.service.IchLevelService;
import com.gumeng.service.IchProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 功能：非遗资源
 * 作者：Z
 * 日期：2025/3/26 上午10:30
 */
@RestController
@RequestMapping("/web/ich") //给当前控制器下的所有接口添加前缀
public class IchController {

    @Autowired
    IchLevelService ichLevelService;

    @Autowired
    IchCategoryService ichCategoryService;

    @Autowired
    IchProjectService ichProjectService;


    //获取非遗保护级别
    @GetMapping("/getLevel")
    public HttpResponse getLevel() {
        List<IchLevel> ichLevels = ichLevelService.list();
        return HttpResponse.success(ichLevels);
    }

    //获取非遗类别
    @GetMapping("/getCategory")
    public HttpResponse getCategory() {
        List<IchCategory> ichCategories = ichCategoryService.list();
        return HttpResponse.success(ichCategories);
    }

    //获取非遗项目列表
    @GetMapping("/getProject")
    public HttpResponse getIchProject() {
        List<IchProjectListVO> ichProjects = ichProjectService.getIchProject();
        return HttpResponse.success(ichProjects);
    }


}
