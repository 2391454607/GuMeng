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
                                      @RequestParam("projectInfo") String projectInfo,
                                      @RequestParam(value = "videoFile", required = false) MultipartFile videoFile) {
        try {
            // 将JSON字符串转换为IchProject对象
            ObjectMapper mapper = new ObjectMapper();
            IchProject ichProject = mapper.readValue(projectInfo, IchProject.class);

            // 处理封面图片上传
            if (!file.isEmpty()) {
                // 验证文件类型是否为图片
                String contentType = file.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    return HttpResponse.error("只允许上传图片文件");
                }

                // 验证文件大小
                long fileSize = file.getSize();
                if (fileSize > 10 * 1024 * 1024) { // 10MB
                    return HttpResponse.error("文件大小不能超过10MB");
                }
                
                String originalFilename = file.getOriginalFilename();
                String fileName = "image/" + System.currentTimeMillis() + "_" + originalFilename;

                // 上传到七牛云
                String url = qiniuUtils.upload(file.getBytes(), fileName);

                if (url != null && !url.isEmpty()) {
                    // 设置文件URL到项目信息中
                    ichProject.setCoverImage(url);
                } else {
                    return HttpResponse.error("封面图片上传失败");
                }
            }
            
            // 处理视频文件上传
            if (videoFile != null && !videoFile.isEmpty()) {
                // 验证文件类型是否为视频
                String contentType = videoFile.getContentType();
                if (contentType == null || !contentType.startsWith("video/")) {
                    return HttpResponse.error("只允许上传视频文件");
                }

                // 验证视频文件大小
                long fileSize = videoFile.getSize();
                if (fileSize > 1000 * 1024 * 1024) { 
                    return HttpResponse.error("视频文件大小不能超过1000MB");
                }
                
                String originalFilename = videoFile.getOriginalFilename();
                String fileName = "video/" + System.currentTimeMillis() + "_" + originalFilename;

                // 上传到七牛云
                String videoUrl = qiniuUtils.upload(videoFile.getBytes(), fileName);

                if (videoUrl != null && !videoUrl.isEmpty()) {
                    // 设置视频URL到项目信息中
                    ichProject.setVideo(videoUrl);
                } else {
                    return HttpResponse.error("视频上传失败");
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
                                    @RequestParam("projectInfo") String projectInfo,
                                    @RequestParam(value = "videoFile", required = false) MultipartFile videoFile) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            IchProject ichProject = mapper.readValue(projectInfo, IchProject.class);

            IchProject oldProject = ichProjectService.getById(ichProject.getId());
            if (oldProject == null) {
                return HttpResponse.error("项目不存在");
            }

            // 处理封面图片更新
            if (file != null && !file.isEmpty()) {
                // 验证文件类型是否为图片
                String contentType = file.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    return HttpResponse.error("只允许上传图片文件");
                }

                // 验证文件大小
                long fileSize = file.getSize();
                if (fileSize > 10 * 1024 * 1024) { // 10MB
                    return HttpResponse.error("文件大小不能超过10MB");
                }
                
                // 有原来的图片，先删除
                if (oldProject.getCoverImage() != null && !oldProject.getCoverImage().isEmpty()) {
                    String oldFileName = oldProject.getCoverImage().substring(oldProject.getCoverImage().lastIndexOf("/") + 1);
                    qiniuUtils.delete(oldFileName);
                }

                // 上传新文件
                String originalFilename = file.getOriginalFilename();
                String fileName = "image/" + System.currentTimeMillis() + "_" + originalFilename;
                String url = qiniuUtils.upload(file.getBytes(), fileName);

                if (url != null && !url.isEmpty()) {
                    ichProject.setCoverImage(url);
                } else {
                    return HttpResponse.error("封面图片上传失败");
                }
            } else {
                ichProject.setCoverImage(oldProject.getCoverImage());
            }
            
            // 处理视频文件更新
            if (videoFile != null && !videoFile.isEmpty()) {
                // 验证文件类型是否为视频
                String contentType = videoFile.getContentType();
                if (contentType == null || !contentType.startsWith("video/")) {
                    return HttpResponse.error("只允许上传视频文件");
                }

                // 验证视频文件大小
                long fileSize = videoFile.getSize();
                if (fileSize > 1000 * 1024 * 1024) {
                    return HttpResponse.error("视频文件大小不能超过1000MB");
                }
                
                // 有原来的视频，先删除
                if (oldProject.getVideo() != null && !oldProject.getVideo().isEmpty()) {
                    String oldVideoFileName = oldProject.getVideo().substring(oldProject.getVideo().lastIndexOf("/") + 1);
                    qiniuUtils.delete(oldVideoFileName);
                }

                // 上传新视频文件
                String originalFilename = videoFile.getOriginalFilename();
                String fileName = "video/" + System.currentTimeMillis() + "_" + originalFilename;
                String videoUrl = qiniuUtils.upload(videoFile.getBytes(), fileName);

                if (videoUrl != null && !videoUrl.isEmpty()) {
                    ichProject.setVideo(videoUrl);
                } else {
                    return HttpResponse.error("视频上传失败");
                }
            } else {
                ichProject.setVideo(oldProject.getVideo());
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
    @PostMapping("/deleteProject")
    public HttpResponse deleteProject(@RequestParam Integer id) {
        try {
            // 获取项目信息
            IchProject project = ichProjectService.getById(id);
            if (project == null) {
                return HttpResponse.error("项目不存在");
            }

            // 有封面图片，删除七牛云上的文件
            if (project.getCoverImage() != null && !project.getCoverImage().isEmpty()) {
                String fileName = project.getCoverImage().substring(project.getCoverImage().lastIndexOf("/") + 1);
                qiniuUtils.delete(fileName);
            }
            
            // 有视频，删除七牛云上的文件
            if (project.getVideo() != null && !project.getVideo().isEmpty()) {
                String videoFileName = project.getVideo().substring(project.getVideo().lastIndexOf("/") + 1);
                qiniuUtils.delete(videoFileName);
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
    
    // 单独上传视频文件接口
    @PostMapping("/uploadVideo")
    public HttpResponse uploadVideo(@RequestParam("file") MultipartFile file, 
                                    @RequestParam(value = "projectId", required = false) String projectIdStr) {
        try {
            // 验证参数
            if (file.isEmpty()) {
                return HttpResponse.error("上传文件不能为空");
            }
            
            // 验证文件类型是否为视频
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("video/")) {
                return HttpResponse.error("只允许上传视频文件");
            }
            
            // 验证视频文件大小
            long fileSize = file.getSize();
            if (fileSize > 1000 * 1024 * 1024) {
                return HttpResponse.error("视频文件大小不能超过1000MB");
            }
            
            // 处理projectId参数
            Integer projectId = null;
            if (projectIdStr != null && !projectIdStr.equals("undefined") && !projectIdStr.isEmpty()) {
                try {
                    projectId = Integer.parseInt(projectIdStr);
                } catch (NumberFormatException e) {
                    return HttpResponse.error("项目ID格式不正确");
                }
                
                // 查询项目是否存在
                IchProject project = ichProjectService.getById(projectId);
                if (project == null) {
                    return HttpResponse.error("项目不存在");
                }
                
                // 原来有视频，先删除
                if (project.getVideo() != null && !project.getVideo().isEmpty()) {
                    String oldVideoFileName = project.getVideo().substring(project.getVideo().lastIndexOf("/") + 1);
                    qiniuUtils.delete(oldVideoFileName);
                }
            }
            
            // 上传视频文件到七牛云
            String originalFilename = file.getOriginalFilename();
            String fileName = "video/" + System.currentTimeMillis() + "_" + originalFilename;
            String videoUrl = qiniuUtils.upload(file.getBytes(), fileName);
            
            if (videoUrl == null || videoUrl.isEmpty()) {
                return HttpResponse.error("视频上传失败");
            }
            
            // 更新项目信息
            if (projectId != null) {
                IchProject project = ichProjectService.getById(projectId);
                project.setVideo(videoUrl);
                project.setUpdateTime(LocalDateTime.now());
                boolean result = ichProjectService.updateById(project);
                
                if (!result) {
                    return HttpResponse.error("视频信息更新失败");
                }
            }
            
            // 返回视频URL
            return HttpResponse.success(videoUrl);
            
        } catch (Exception e) {
            e.printStackTrace();
            return HttpResponse.error("视频上传失败：" + e.getMessage());
        }
    }
    
    // 添加单独删除文件的API
    @GetMapping("/deleteFile")
    public HttpResponse deleteFile(@RequestParam String fileName) {
        try {
            if (fileName == null || fileName.isEmpty()) {
                return HttpResponse.error("文件名不能为空");
            }
            
            // 从URL中提取文件名
            if (fileName.startsWith("http")) {
                // 尝试获取域名后面的路径
                String path = fileName;
                try {
                    if (fileName.contains("://")) {
                        path = fileName.split("://")[1];
                        if (path.contains("/")) {
                            path = path.substring(path.indexOf("/") + 1);
                        }
                    }
                } catch (Exception e) {
                    // 解析失败
                    path = fileName.substring(fileName.lastIndexOf("/") + 1);
                }
                fileName = path;
                System.out.println("提取的文件名: " + fileName);
            }
            
            boolean result = qiniuUtils.delete(fileName);
            
            if (result) {
                return HttpResponse.success("文件删除成功或文件已不存在");
            } else {
                return HttpResponse.error("文件删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return HttpResponse.error("文件删除失败: " + e.getMessage());
        }
    }
}
