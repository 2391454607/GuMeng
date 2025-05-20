package com.gumeng.controller.Forum;

import com.gumeng.code.HttpResponse;
import com.gumeng.utils.QiniuUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * 论坛文件上传控制器
 */
@RestController
@RequestMapping("/forum/file")
@Slf4j
public class ForumFileController {

    @Autowired
    private QiniuUtils qiniuUtils;

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
            String fileName = "forum/" + UUID.randomUUID().toString().replace("-", "") + suffix;

            // 上传到七牛云并获取URL
            String url = qiniuUtils.upload(file.getBytes(), fileName);
            if (url == null) {
                return HttpResponse.error("文件上传失败");
            }

            log.info("文件上传成功，URL: {}", url);
            HttpResponse response = HttpResponse.success(url);
            response.setMsg("上传成功");
            return response;
        } catch (Exception e) {
            log.error("文件上传异常: ", e);
            return HttpResponse.error("文件上传失败: " + e.getMessage());
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
                return HttpResponse.success();
            } else {
                return HttpResponse.error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除文件异常: ", e);
            return HttpResponse.error("删除失败: " + e.getMessage());
        }
    }
} 