package com.gumeng.controller.sys;

import com.gumeng.code.HttpResponse;
import com.gumeng.domain.pages.IchProject;
import com.gumeng.service.IchProjectService;
import com.gumeng.utils.QiniuUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

/**
 * 非遗百科文件上传控制器
 */
@RestController
@RequestMapping("/sys/file")
@Slf4j
public class IchFileController {

    @Autowired
    private QiniuUtils qiniuUtils;
    
    @Autowired
    private IchProjectService ichProjectService;

    /**
     * 上传图片到七牛云
     * @param file 图片文件
     * @return 图片URL
     */
    @PostMapping("/upload")
    public HttpResponse uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            // 验证文件是否为空
            if (file.isEmpty()) {
                return HttpResponse.error("上传文件不能为空");
            }

            // 验证文件类型是否为图片
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return HttpResponse.error("只允许上传图片文件");
            }

            // 验证文件大小（限制为5MB）
            long fileSize = file.getSize();
            if (fileSize > 5 * 1024 * 1024) {
                return HttpResponse.error("文件大小不能超过5MB");
            }

            // 生成唯一文件名（避免重名覆盖）
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename != null ? 
                    originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String fileName = "ichp/" + UUID.randomUUID().toString().replace("-", "") + suffix;

            // 上传到七牛云并获取URL
            String url = qiniuUtils.upload(file.getBytes(), fileName);
            if (url == null) {
                return HttpResponse.error("文件上传失败");
            }

            log.info("百科文件上传成功，URL: {}", url);
            HttpResponse response = HttpResponse.success(url);
            response.setMsg("上传成功");
            return response;
        } catch (Exception e) {
            log.error("百科文件上传异常: ", e);
            return HttpResponse.error("文件上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量上传图片到七牛云
     * @param files 多个图片文件
     * @return 图片URL列表
     */
    @PostMapping("/batch-upload")
    public HttpResponse batchUploadImages(@RequestParam("files") MultipartFile[] files) {
        try {
            if (files == null || files.length == 0) {
                return HttpResponse.error("未选择文件");
            }
            
            List<String> urls = new ArrayList<>();
            StringBuilder errorMsg = new StringBuilder();
            
            for (MultipartFile file : files) {
                // 验证文件是否为空
                if (file.isEmpty()) {
                    errorMsg.append("文件").append(file.getOriginalFilename()).append("为空; ");
                    continue;
                }
                
                // 验证文件类型是否为图片
                String contentType = file.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    errorMsg.append("文件").append(file.getOriginalFilename()).append("不是图片格式; ");
                    continue;
                }
                
                // 验证文件大小
                if (file.getSize() > 5 * 1024 * 1024) {
                    errorMsg.append("文件").append(file.getOriginalFilename()).append("超过5MB大小限制; ");
                    continue;
                }
                
                // 生成唯一文件名
                String originalFilename = file.getOriginalFilename();
                String suffix = originalFilename != null ? 
                        originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
                String fileName = "ichp/" + UUID.randomUUID().toString().replace("-", "") + suffix;
                
                // 上传到七牛云
                try {
                    String url = qiniuUtils.upload(file.getBytes(), fileName);
                    if (url != null && !url.isEmpty()) {
                        urls.add(url);
                        log.info("批量上传图片成功: {}", url);
                    } else {
                        errorMsg.append("文件").append(file.getOriginalFilename()).append("上传失败; ");
                    }
                } catch (Exception e) {
                    log.error("上传文件异常: {}", e.getMessage());
                    errorMsg.append("文件").append(file.getOriginalFilename()).append("上传异常: ").append(e.getMessage()).append("; ");
                }
            }
            
            if (urls.isEmpty()) {
                return HttpResponse.error("所有文件上传失败: " + errorMsg);
            }
            
            HttpResponse response = HttpResponse.success(urls);
            if (errorMsg.length() > 0) {
                response.setMsg("部分文件上传成功，失败原因: " + errorMsg);
            } else {
                response.setMsg("全部文件上传成功");
            }
            
            return response;
        } catch (Exception e) {
            log.error("批量上传图片异常: ", e);
            return HttpResponse.error("批量上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 上传视频到七牛云
     * @param file 视频文件
     * @param projectIdStr 项目ID
     * @return 视频URL
     */
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
            String fileName = "ichp/" + System.currentTimeMillis() + "_" + originalFilename;
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
            
            log.info("视频上传成功，URL: {}", videoUrl);
            
            // 返回视频URL
            return HttpResponse.success(videoUrl);
            
        } catch (Exception e) {
            log.error("视频上传异常: ", e);
            return HttpResponse.error("视频上传失败: " + e.getMessage());
        }
    }

    /**
     * 删除七牛云图片
     * @param fileName 文件名
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    public HttpResponse deleteImage(@RequestParam("fileName") String fileName) {
        try {
            // 从URL提取文件名
            if (fileName.contains("/")) {
                fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
            }
            
            boolean result = qiniuUtils.delete(fileName);
            if (result) {
                log.info("文件删除成功: {}", fileName);
                return HttpResponse.success();
            } else {
                log.warn("文件删除失败: {}", fileName);
                return HttpResponse.error("删除失败");
            }
        } catch (Exception e) {
            log.error("百科删除文件异常: ", e);
            return HttpResponse.error("删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量删除七牛云文件
     * @param fileNames 文件名列表
     * @return 删除结果
     */
    @DeleteMapping("/batch-delete")
    public HttpResponse batchDeleteFiles(@RequestParam("fileNames") List<String> fileNames) {
        try {
            if (fileNames == null || fileNames.isEmpty()) {
                return HttpResponse.error("未提供需要删除的文件");
            }
            
            List<String> successList = new ArrayList<>();
            List<String> failList = new ArrayList<>();
            
            for (String fileName : fileNames) {
                if (fileName == null || fileName.isEmpty()) {
                    failList.add(fileName);
                    continue;
                }
                
                // 从URL提取文件名
                if (fileName.contains("/")) {
                    fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
                }
                
                boolean result = qiniuUtils.delete(fileName);
                if (result) {
                    successList.add(fileName);
                } else {
                    failList.add(fileName);
                }
            }
            
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("success", successList);
            resultMap.put("fail", failList);
            
            HttpResponse response = HttpResponse.success(resultMap);
            if (failList.isEmpty()) {
                response.setMsg("所有文件删除成功");
            } else {
                response.setMsg("部分文件删除成功，部分失败");
            }
            
            return response;
        } catch (Exception e) {
            log.error("批量删除文件异常: ", e);
            return HttpResponse.error("批量删除失败: " + e.getMessage());
        }
    }
} 