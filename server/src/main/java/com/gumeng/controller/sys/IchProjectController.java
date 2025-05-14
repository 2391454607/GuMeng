package com.gumeng.controller.sys;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gumeng.code.HttpResponse;
import com.gumeng.domain.pages.IchCategory;
import com.gumeng.domain.pages.IchLevel;
import com.gumeng.domain.pages.IchProject;
import com.gumeng.entity.vo.IchProjectListVO;
import com.gumeng.service.IchCategoryService;
import com.gumeng.service.IchLevelService;
import com.gumeng.service.IchProjectService;
import com.gumeng.utils.QiniuUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
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

    @Resource
    private QiniuUtils qiniuUtils;  // 注入七牛云工具类

    //获取非遗项目信息
    @GetMapping("/getProject")
    public HttpResponse getIchProject(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer levelId,
            @RequestParam(required = false) Integer categoryId) {
        Page<IchProjectListVO> ichProjects = ichProjectService.getIchProject(current, size, levelId, categoryId);
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

    //新增非遗项目信息和文件
    @PostMapping("/addProject")
    public HttpResponse addIchProject(@RequestParam("file") MultipartFile file,
                                      @RequestParam("projectInfo") String projectInfo) {
        try {
            // 将JSON字符串转换为IchProject对象
            ObjectMapper mapper = new ObjectMapper();
            IchProject ichProject = mapper.readValue(projectInfo, IchProject.class);

            // 处理文件上传
            if (!file.isEmpty()) {
                String originalFilename = file.getOriginalFilename();
                String fileName = System.currentTimeMillis() + "_" + originalFilename;

                // 上传到七牛云
                String url = qiniuUtils.upload(file.getBytes(), fileName);

                if (url != null && !url.isEmpty()) {
                    // 设置文件URL到项目信息中
                    ichProject.setCoverImage(url);
                } else {
                    return HttpResponse.error("文件上传失败");
                }
            }

            ichProject.setCreateTime(LocalDateTime.now());
            ichProject.setUpdateTime(LocalDateTime.now());

            // 保存项目信息
            boolean result = ichProjectService.save(ichProject);
            if (result) {
                return HttpResponse.success("项目信息保存成功");
            }
            return HttpResponse.error("项目信息保存失败");

        } catch (IOException e) {
            e.printStackTrace();
            return HttpResponse.error("操作失败：" + e.getMessage());
        }
    }

}
