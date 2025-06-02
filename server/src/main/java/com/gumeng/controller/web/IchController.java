package com.gumeng.controller.web;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gumeng.code.HttpResponse;
import com.gumeng.domain.pages.IchCategory;
import com.gumeng.domain.pages.IchLevel;
import com.gumeng.entity.vo.IchProjectListVO;
import com.gumeng.entity.vo.IchProjectDetailVO;
import com.gumeng.service.IchCategoryService;
import com.gumeng.service.IchLevelService;
import com.gumeng.service.IchProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 功能：非遗资源
 * 作者：Z
 * 日期：2025/3/26 上午10:30
 */
@RestController
@RequestMapping("/web/ich")
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
    public HttpResponse getIchProject(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer levelId,
            @RequestParam(required = false) Integer categoryId) {
        Page<IchProjectListVO> ichProjects = ichProjectService.getIchProject(current, size, levelId, categoryId);
        return HttpResponse.success(ichProjects);
    }

    //获取非遗项目详情
    @GetMapping("/getProjectDetail/{id}")
    public HttpResponse getProjectDetail(@PathVariable("id") Integer id) {
        IchProjectDetailVO projectDetail = ichProjectService.getProjectDetail(id);
        if (projectDetail != null) {
            return HttpResponse.success(projectDetail);
        }
        return HttpResponse.error("项目不存在");
    }
}
