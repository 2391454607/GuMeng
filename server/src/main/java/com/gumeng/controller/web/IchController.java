package com.gumeng.controller.web;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gumeng.annotation.LogOperation;
import com.gumeng.code.HttpResponse;
import com.gumeng.domain.pages.IchCategory;
import com.gumeng.domain.pages.IchLevel;
import com.gumeng.domain.pages.IchRegion;
import com.gumeng.entity.vo.IchProjectListVO;
import com.gumeng.entity.vo.IchProjectDetailVO;
import com.gumeng.service.IchCategoryService;
import com.gumeng.service.IchLevelService;
import com.gumeng.service.IchProjectService;
import com.gumeng.service.IchRegionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
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
    
    @Autowired
    IchRegionService ichRegionService;


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
    
    //获取地区信息
    @GetMapping("/getRegion")
    public HttpResponse getRegion() {
        List<IchRegion> ichRegions = ichRegionService.list();
        return HttpResponse.success(ichRegions);
    }

    //获取非遗项目列表
    @LogOperation(module = "非遗百科", operation = "获取非遗项目列表")
    @GetMapping("/getProject")
    public HttpResponse getIchProject(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer levelId,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer regionId,
            @RequestParam(required = false) String keyword) {
        
        try {
            Page<IchProjectListVO> ichProjects;
            
            // 如果有关键词搜索，使用关键词搜索方法
            if (keyword != null && !keyword.trim().isEmpty()) {
                ichProjects = ichProjectService.searchIchProject(current, size, keyword);
            } else {
                // 否则使用常规筛选方法
                ichProjects = ichProjectService.getIchProject(current, size, levelId, categoryId, regionId);
            }
            
            // 确保返回结果中records字段不为null
            if (ichProjects.getRecords() == null) {
                ichProjects.setRecords(new ArrayList<>());
            }
            
            return HttpResponse.success(ichProjects);
        } catch (Exception e) {
            e.printStackTrace();
            // 创建一个空的分页对象作为返回结果
            Page<IchProjectListVO> emptyPage = new Page<>(current, size);
            emptyPage.setRecords(new ArrayList<>());
            emptyPage.setTotal(0);
            return HttpResponse.success(emptyPage);
        }
    }

    @LogOperation(module = "非遗百科",operation = "获取非遗项目详情")
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
