package com.gumeng.controller.ai;

import com.gumeng.code.HttpResponse;
import com.gumeng.config.Al.Tripo3d;
import com.gumeng.domain.ModelInfo;
import com.gumeng.entity.DTO.ToModels.TextToModelRequest;
import com.gumeng.entity.vo.model.AiModelInfoVO;
import com.gumeng.entity.vo.model.ModelInfoVO;
import com.gumeng.service.coze.AiModelService;
import com.gumeng.service.coze.ModelInfoService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 功能：tripo3d模型生成
 * 作者：Z
 * 日期：2025/6/3 上午9:24
 */

@RestController
@RequestMapping("/model")
public class ToModelController {

    private final Tripo3d tripo3d;
    private final AiModelService aiModelService;
    private final ModelInfoService modelInfoService;

    @Resource
    private RestTemplate restTemplate;

    @Autowired
    public ToModelController(Tripo3d tripo3d, AiModelService aiModelService, ModelInfoService modelInfoService) {
        this.tripo3d = tripo3d;
        this.aiModelService = aiModelService;
        this.modelInfoService = modelInfoService;
    }

    //文本生成模型
    @PostMapping("/text-to-model")
    public Object textToModel(@Valid @RequestBody TextToModelRequest textToModelRequest) {

        final String apiKey = tripo3d.getKey();
        final String generateUrl = tripo3d.getGenerationUrl();

        // 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        // 构建请求实体
        HttpEntity<TextToModelRequest> requestEntity = new HttpEntity<>(textToModelRequest, headers);

        // 发送请求
        ResponseEntity<Object> responseEntity = restTemplate.exchange(
                generateUrl,
                HttpMethod.POST,
                requestEntity,
                Object.class
        );

        // 处理响应
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            Object body = responseEntity.getBody();
            if (body instanceof Map<?, ?> bodyMap) {
                Object dataObj = bodyMap.get("data");
                if (dataObj instanceof Map<?, ?> dataMap) {
                    Object taskIdObj = dataMap.get("task_id");
                    if (taskIdObj instanceof String taskId) {
                        ModelInfo modelInfo = new ModelInfo();
                        modelInfo.setTaskId(taskId);
                        modelInfo.setStatus("init");
                        modelInfoService.save(modelInfo);
                    }
                }
            }
            return responseEntity.getBody();
        }
        return null;
    }

    //查询账户余额
    @GetMapping("/balance")
    public Object getBalance() {
        final String apiKey = tripo3d.getKey();
        final String balanceUrl = tripo3d.getBalanceUrl();

        // 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        // 构建请求实体
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        //发送请求
        ResponseEntity<Object> responseEntity = restTemplate.exchange(
                balanceUrl,
                HttpMethod.GET,
                requestEntity,
                Object.class
        );

        // 处理响应
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            return responseEntity.getBody();
        }
        return null;
    }

    @GetMapping("/task")
    public HttpResponse taskText(@RequestParam("id") String taskId) {
        return aiModelService.taskText(taskId);
    }
}
