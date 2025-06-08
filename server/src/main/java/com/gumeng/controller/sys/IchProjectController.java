package com.gumeng.controller.sys;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 功能：非遗项目控制器
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
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer regionId) {
        Page<IchProjectListVO> ichProjects = ichProjectService.getIchProject(current, size, levelId, categoryId, regionId);
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

    //新增非遗项目信息
    @PostMapping("/addProject")
    public HttpResponse addIchProject(@RequestParam("projectInfo") String projectInfo) {
        try {
            System.out.println("接收到的项目信息: " + projectInfo);
            // 将JSON字符串转换为IchProject对象
            ObjectMapper mapper = new ObjectMapper();
            IchProject ichProject = mapper.readValue(projectInfo, IchProject.class);
            
            // 处理多图片URL
            if (ichProject.getImages() != null && !ichProject.getImages().isEmpty()) {
                String imagesStr = ichProject.getImages();

                if (imagesStr.startsWith("[") && imagesStr.endsWith("]")) {
                    try {
                        List<String> imagesList = mapper.readValue(imagesStr, new TypeReference<List<String>>() {});
                        ichProject.setImages(String.join(",", imagesList));
                        System.out.println("成功处理JSON数组格式的图片URL: " + ichProject.getImages());
                    } catch (Exception e) {
                        System.out.println("处理JSON数组失败，尝试其他方法: " + e.getMessage());

                        imagesStr = imagesStr.replace("[", "")
                                   .replace("]", "")
                                   .replace("\"", "");

                        String[] urls = imagesStr.split(",");
                        List<String> cleanUrls = new ArrayList<>();
                        for (String url : urls) {
                            String cleanUrl = url.trim();
                            if (!cleanUrl.isEmpty()) {
                                cleanUrls.add(cleanUrl);
                            }
                        }
                        ichProject.setImages(String.join(",", cleanUrls));
                        System.out.println("使用简单字符串处理方法成功: " + ichProject.getImages());
                    }
                }

                else if (imagesStr.contains(",")) {
                    List<String> imagesList = Arrays.stream(imagesStr.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toList());
                    ichProject.setImages(String.join(",", imagesList));
                    System.out.println("直接处理逗号分隔字符串: " + ichProject.getImages());
                }
                
                System.out.println("最终图片URL: " + ichProject.getImages());
            }

            // 检查并记录视频URL
            if (ichProject.getVideo() != null && !ichProject.getVideo().isEmpty()) {
                System.out.println("保存项目视频URL: " + ichProject.getVideo());
            }
            
            // 检查保存地区ID
            if (ichProject.getRegionId() != null) {
                System.out.println("保存项目地区ID: " + ichProject.getRegionId());
            } else {
                System.out.println("警告: 地区ID为空");
            }

            // 设置创建和更新时间
            ichProject.setCreateTime(LocalDateTime.now());
            ichProject.setUpdateTime(LocalDateTime.now());

            // 保存项目信息
            boolean result = ichProjectService.save(ichProject);
            if (result) {
                System.out.println("项目保存成功，ID: " + ichProject.getId() + ", 视频URL: " + ichProject.getVideo() + ", 地区ID: " + ichProject.getRegionId());
                return HttpResponse.success(ichProject.getId());
            }
            
            System.out.println("项目保存失败");
            return HttpResponse.error("项目信息保存失败");

        } catch (IOException e) {
            e.printStackTrace();
            return HttpResponse.error("操作失败：" + e.getMessage());
        }
    }

    //修改非遗项目信息
    @PostMapping("/updateProject")
    public HttpResponse updateProject(@RequestParam("projectInfo") String projectInfo) {
        try {
            System.out.println("接收到的更新项目信息: " + projectInfo);
            ObjectMapper mapper = new ObjectMapper();
            IchProject ichProject = mapper.readValue(projectInfo, IchProject.class);

            IchProject oldProject = ichProjectService.getById(ichProject.getId());
            if (oldProject == null) {
                return HttpResponse.error("项目不存在");
            }

            // 处理多图片URL
            if (ichProject.getImages() != null && !ichProject.getImages().isEmpty()) {
                String imagesStr = ichProject.getImages();

                if (imagesStr.startsWith("[") && imagesStr.endsWith("]")) {
                    try {
                        List<String> imagesList = mapper.readValue(imagesStr, new TypeReference<List<String>>() {});
                        ichProject.setImages(String.join(",", imagesList));
                        System.out.println("更新项目：成功处理JSON数组格式的图片URL: " + ichProject.getImages());
                    } catch (Exception e) {
                        System.out.println("更新项目：处理JSON数组失败，尝试其他方法: " + e.getMessage());

                        imagesStr = imagesStr.replace("[", "")
                                   .replace("]", "")
                                   .replace("\"", "");

                        String[] urls = imagesStr.split(",");
                        List<String> cleanUrls = new ArrayList<>();
                        for (String url : urls) {
                            String cleanUrl = url.trim();
                            if (!cleanUrl.isEmpty()) {
                                cleanUrls.add(cleanUrl);
                            }
                        }
                        ichProject.setImages(String.join(",", cleanUrls));
                        System.out.println("更新项目：使用简单字符串处理方法成功: " + ichProject.getImages());
                    }
                } 

                else if (imagesStr.contains(",")) {
                    List<String> imagesList = Arrays.stream(imagesStr.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toList());
                    ichProject.setImages(String.join(",", imagesList));
                    System.out.println("更新项目：直接处理逗号分隔字符串: " + ichProject.getImages());
                }
                
                System.out.println("更新项目：最终图片URL: " + ichProject.getImages());
            }

            if (ichProject.getImages() == null || ichProject.getImages().isEmpty()) {
                ichProject.setImages(oldProject.getImages());
            }

            // 视频URL处理和地区ID处理
            if (ichProject.getVideo() == null || ichProject.getVideo().isEmpty()) {
                ichProject.setVideo(oldProject.getVideo());
                System.out.println("更新项目：使用旧视频URL: " + ichProject.getVideo());
            } else {
                System.out.println("更新项目：使用新视频URL: " + ichProject.getVideo());
            }
            
            // 处理地区ID
            if (ichProject.getRegionId() == null) {
                ichProject.setRegionId(oldProject.getRegionId());
                System.out.println("更新项目：使用旧地区ID: " + ichProject.getRegionId());
            } else {
                System.out.println("更新项目：使用新地区ID: " + ichProject.getRegionId());
            }

            // 更新时间
            ichProject.setUpdateTime(LocalDateTime.now());
            
            // 更新项目信息
            boolean result = ichProjectService.updateById(ichProject);
            if (result) {
                System.out.println("项目更新成功，ID: " + ichProject.getId() + ", 视频URL: " + ichProject.getVideo() + ", 地区ID: " + ichProject.getRegionId());
                return HttpResponse.success("项目信息更新成功");
            }
            
            System.out.println("项目更新失败");
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

            // 删除项目相关的所有图片
            if (project.getImages() != null && !project.getImages().isEmpty()) {
                String[] imageUrls = project.getImages().split(",");
                
                for (String imageUrl : imageUrls) {
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
                        qiniuUtils.delete(fileName);
                    }
                }
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
