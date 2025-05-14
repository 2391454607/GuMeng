package com.gumeng.controller.sys;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gumeng.code.HttpResponse;
import com.gumeng.domain.pages.IchCategory;
import com.gumeng.domain.pages.IchLevel;
import com.gumeng.entity.vo.IchProjectListVO;
import com.gumeng.service.IchCategoryService;
import com.gumeng.service.IchLevelService;
import com.gumeng.service.IchProjectService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 功能：
 * 作者：Z
 * 日期：2025/3/26 上午10:23
 */
@RestController
@RequestMapping("/sys") //给当前控制器下的所有接口添加前缀
public class IchProjectController {

    @Resource
    private IchProjectService ichProjectService;

    @Resource
    private IchCategoryService ichCategoryService;

    @Resource
    private IchLevelService ichLevelService;

    //获取非遗项目信息
    @GetMapping("/getIchProject")
    public HttpResponse getIchProject(@RequestParam(defaultValue = "1") Integer current,
                                      @RequestParam(defaultValue = "10") Integer size) {
        Page<IchProjectListVO> ichProjects = ichProjectService.getIchProject(current, size);
        return HttpResponse.success(ichProjects);
    }

    //获取非遗项目类别
    @GetMapping("/getIchCategory")
    public List<IchCategory> getIchCategory() {
        return ichCategoryService.getIchCategory();
    }

    //获取非遗项目保护级别
    @GetMapping("/getIchLevel")
    public List<IchLevel> getIchLevel() {
        return ichLevelService.getIchLevel();
    }

}
