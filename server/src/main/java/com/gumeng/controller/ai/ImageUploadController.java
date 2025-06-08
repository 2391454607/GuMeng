package com.gumeng.controller.ai;

import com.gumeng.code.HttpResponse;
import com.gumeng.utils.QiniuUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * 图片上传控制器，用于处理AI图像识别的图片上传
 */
@RestController
@RequestMapping("/ai/image")
@Slf4j
public class ImageUploadController {

    @Autowired
    private QiniuUtils qiniuUtils;

    /**
     * 上传图片到七牛云，用于AI图像识别
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
            String fileName = "ai/" + UUID.randomUUID().toString().replace("-", "") + suffix;

            // 上传到七牛云并获取URL
            String url = qiniuUtils.upload(file.getBytes(), fileName);
            if (url == null) {
                return HttpResponse.error("文件上传失败");
            }

            log.info("AI图像识别文件上传成功，URL: {}", url);
            HttpResponse response = HttpResponse.success(url);
            response.setMsg("上传成功");
            return response;
        } catch (Exception e) {
            log.error("AI图像识别文件上传异常: ", e);
            return HttpResponse.error("文件上传失败: " + e.getMessage());
        }
    }
} 