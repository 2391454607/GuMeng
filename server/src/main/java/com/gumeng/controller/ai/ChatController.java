package com.gumeng.controller.ai;

import com.alibaba.fastjson.JSONObject;
import com.gumeng.AI.GLMChatService;
import com.gumeng.code.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 功能：AI聊天控制器
 */
@RestController
@RequestMapping("/ai")
public class ChatController {

    @Autowired
    private GLMChatService glmChatService;

    /**
     * 非遗百科助手聊天接口
     * @param requestBody 请求体，包含messages字段
     * @return 返回AI回复
     */
    @PostMapping("/chat")
    public HttpResponse chat(@RequestBody Map<String, List<JSONObject>> requestBody) {
        try {
            List<JSONObject> messages = requestBody.get("messages");
            
            if (messages == null || messages.isEmpty()) {
                return HttpResponse.error("消息不能为空");
            }
            
            // 添加系统提示词，定制非遗百科助手的角色
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", "你是一个专业的非遗百科助手，精通中国非物质文化遗产的各个方面。" +
                    "你的回答应该专注于中国传统文化、非物质文化遗产保护、传承和发展等方面的问题。" +
                    "当用户询问非遗相关问题时，请提供准确、专业和有深度的回答。" +
                    "你的语言风格应该温和友善，富有文化底蕴，体现出对传统文化的尊重。" +
                    "请确保你的回复简洁明了，长度适中，避免冗长的解释。");
            
            messages.add(0, systemMessage);
            
            // 调用智谱AI服务
            String response = glmChatService.chat(messages);
            return HttpResponse.success(response);
        } catch (Exception e) {
            e.printStackTrace();
            return HttpResponse.error("AI服务异常：" + e.getMessage());
        }
    }
} 