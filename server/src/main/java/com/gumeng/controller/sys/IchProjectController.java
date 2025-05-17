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

    //新增非遗项目信息和图片文件
    @PostMapping("/addProject")
    public HttpResponse addIchProject(@RequestParam("file") MultipartFile file,
                                      @RequestParam("projectInfo") String projectInfo) {
        try {
            // 将JSON字符串转换为IchProject对象
            ObjectMapper mapper = new ObjectMapper();
            IchProject ichProject = mapper.readValue(projectInfo, IchProject.class);

            // 处理文件上传
            if (!file.isEmpty()) {
                // 验证文件类型是否为图片
                String contentType = file.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    return HttpResponse.error("只允许上传图片文件");
                }

                // 验证文件大小（如果需要在代码中再次验证）
                long fileSize = file.getSize();
                if (fileSize > 10 * 1024 * 1024) { // 10MB
                    return HttpResponse.error("文件大小不能超过10MB");
                }
                
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

    //修改非遗项目信息
    @PostMapping("/updateProject")
    public HttpResponse updateProject(@RequestParam(value = "file", required = false) MultipartFile file,
                                    @RequestParam("projectInfo") String projectInfo) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            IchProject ichProject = mapper.readValue(projectInfo, IchProject.class);

            IchProject oldProject = ichProjectService.getById(ichProject.getId());
            if (oldProject == null) {
                return HttpResponse.error("项目不存在");
            }

            if (file != null && !file.isEmpty()) {
                // 验证文件类型是否为图片
                String contentType = file.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    return HttpResponse.error("只允许上传图片文件");
                }

                // 验证文件大小（如果需要在代码中再次验证）
                long fileSize = file.getSize();
                if (fileSize > 10 * 1024 * 1024) { // 10MB
                    return HttpResponse.error("文件大小不能超过10MB");
                }
                
                // 如果原来有图片，先删除
                if (oldProject.getCoverImage() != null && !oldProject.getCoverImage().isEmpty()) {
                    String oldFileName = oldProject.getCoverImage().substring(oldProject.getCoverImage().lastIndexOf("/") + 1);
                    qiniuUtils.delete(oldFileName);
                }

                // 上传新文件
                String originalFilename = file.getOriginalFilename();
                String fileName = System.currentTimeMillis() + "_" + originalFilename;
                String url = qiniuUtils.upload(file.getBytes(), fileName);

                if (url != null && !url.isEmpty()) {
                    ichProject.setCoverImage(url);
                } else {
                    return HttpResponse.error("文件上传失败");
                }
            } else {
                ichProject.setCoverImage(oldProject.getCoverImage());
            }

            ichProject.setUpdateTime(LocalDateTime.now());
            boolean result = ichProjectService.updateById(ichProject);
            if (result) {
                return HttpResponse.success("项目信息更新成功");
            }
            return HttpResponse.error("项目信息更新失败");

        } catch (IOException e) {
            e.printStackTrace();
            return HttpResponse.error("操作失败：" + e.getMessage());
        }
    }

    //删除非遗项目信息
    @PostMapping("/deleteProject/{id}")
    public HttpResponse deleteProject(@PathVariable Integer id) {
        try {
            // 获取项目信息
            IchProject project = ichProjectService.getById(id);
            if (project == null) {
                return HttpResponse.error("项目不存在");
            }

            // 如果有封面图片，删除七牛云上的文件
            if (project.getCoverImage() != null && !project.getCoverImage().isEmpty()) {
                String fileName = project.getCoverImage().substring(project.getCoverImage().lastIndexOf("/") + 1);
                qiniuUtils.delete(fileName);
            }

            // 删除项目信息
            boolean result = ichProjectService.removeById(id);
            if (result) {
                return HttpResponse.success("项目删除成功");
            }
            return HttpResponse.error("项目删除失败");
            
        } catch (Exception e) {
            e.printStackTrace();
            return HttpResponse.error("删除失败：" + e.getMessage());
        }
    }


}
