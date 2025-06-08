package com.gumeng.AI;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class GLMChatService {
    private static final String API_KEY = "f09b8b33e373c994668d2117243349a0.KPJ6Jeloxn1biPvj";
    private static final String API_URL = "https://open.bigmodel.cn/api/paas/v4/chat/completions";
    private final OkHttpClient client;

    public GLMChatService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }

    public String chat(List<JSONObject> messages) throws Exception {
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "glm-4v-flash");
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);
        requestBody.put("stream", false);

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .post(RequestBody.create(
                    MediaType.parse("application/json; charset=utf-8"),
                    requestBody.toString()
                ))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("请求失败，状态码: " + response.code());
            }

            String responseBody = response.body().string();
            JSONObject jsonResponse = JSON.parseObject(responseBody);
            return jsonResponse.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");
        }
    }
    
    /**
     * 识别图片中的非遗内容并生成相关图片
     * @param imageUrl 图片URL
     * @return 识别结果和生成的图片URL
     * @throws Exception 如果请求失败
     */
    public String recognizeImage(String imageUrl) throws Exception {
        List<JSONObject> messages = new ArrayList<>();
        
        // 构建包含图片的消息
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        
        // 构建包含图片和文本的内容
        JSONObject imageContent = new JSONObject();
        imageContent.put("type", "image_url");
        
        JSONObject imageUrl_obj = new JSONObject();
        imageUrl_obj.put("url", imageUrl);
        
        imageContent.put("image_url", imageUrl_obj);
        
        JSONObject textContent = new JSONObject();
        textContent.put("type", "text");
        textContent.put("text", "请识别这张图片中是否包含非物质文化遗产内容。如果是非遗项目，请详细介绍其名称、历史、特点和文化价值。同时，请在介绍后生成一张与该非遗相关的精美图片。如果不是非遗项目，请简单介绍图片内容，并推荐一个相似的非遗项目，生成该非遗项目的图片。");
        
        List<JSONObject> contentList = new ArrayList<>();
        contentList.add(textContent);
        contentList.add(imageContent);
        
        userMessage.put("content", contentList);
        messages.add(userMessage);
        
        // 调用API并返回结果
        return chat(messages);
    }

}