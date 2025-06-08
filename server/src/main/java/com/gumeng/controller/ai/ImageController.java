package com.gumeng.controller.ai;

import com.gumeng.AI.GLMChatService;
import com.gumeng.code.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class ImageController {

    @Autowired
    private GLMChatService glmChatService;

    /**
     * 识别图片中的非遗内容
     * @param imageUrl 图片URL
     * @return 识别结果
     */
    @PostMapping("/recognize")
    public HttpResponse recognizeImage(@RequestParam(value = "imageUrl", required = false) String imageUrl,
                                      @RequestBody(required = false) Map<String, String> body) {
        try {
            // 尝试从请求体获取imageUrl（如果请求参数中没有）
            if (imageUrl == null && body != null) {
                imageUrl = body.get("imageUrl");
            }
            
            if (imageUrl == null || imageUrl.isEmpty()) {
                return HttpResponse.error("缺少必需的参数imageUrl");
            }
            
            String recognitionResult = glmChatService.recognizeImage(imageUrl);
            Map<String, Object> data = new HashMap<>();
            data.put("result", recognitionResult);
            return HttpResponse.success(data);
        } catch (Exception e) {
            e.printStackTrace();
            return HttpResponse.error("图像识别失败: " + e.getMessage());
        }
    }

} 