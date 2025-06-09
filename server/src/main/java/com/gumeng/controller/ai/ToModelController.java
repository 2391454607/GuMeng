package com.gumeng.controller.ai;

import com.gumeng.code.HttpResponse;
import com.gumeng.config.Al.Tripo3d;
import com.gumeng.domain.ModelInfo;
import com.gumeng.entity.DTO.ToModels.ImageToModelRequest;
import com.gumeng.entity.DTO.ToModels.TextToModelRequest;
import com.gumeng.entity.bo.ModelBo;
import com.gumeng.entity.bo.TaskBo;
import com.gumeng.service.coze.AiModelService;
import com.gumeng.service.coze.ModelInfoService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * 功能：tripo3d模型生成
 * 作者：Z
 * 日期：2025/6/3 上午9:24
 */
@Slf4j
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

    //获取模型生成信息
    @GetMapping("/getModelInfo")
    public HttpResponse getModelInfo() {
        Object ToModelList = modelInfoService.getToModelList();
        return HttpResponse.success(ToModelList);
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

    //图片上传
    @PostMapping("/uploadSts")
    public Object uploadSts(@RequestParam("file") MultipartFile multipartFile) {
        final String apiKey = tripo3d.getKey();
        final String uploadUrl = tripo3d.getUploadSts();

        log.info("上传 Tripo3D 模型请求收到文件：{}", multipartFile.getOriginalFilename());

        File tempFile = null;
        try {
            // 将 MultipartFile 写入本地临时文件
            tempFile = File.createTempFile("upload-", multipartFile.getOriginalFilename());
            multipartFile.transferTo(tempFile);

            // 构建 multipart/form-data 请求体
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(tempFile));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.setBearerAuth(apiKey);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // 调用 Tripo3D 上传接口
            ResponseEntity<String> response = restTemplate.exchange(
                    uploadUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            log.info("Tripo3D 上传响应：{}", response.getBody());

            return response.getBody();

        } catch (IOException e) {
            log.error("文件处理异常", e);
            return Map.of("code", 500, "msg", "上传失败：" + e.getMessage());
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    //图像生成模型
    @PostMapping("/image-to-model")
    public Object imageToModel(@Valid @RequestBody ImageToModelRequest imageToModelRequest) {

        final String apiKey = tripo3d.getKey();
        final String generateUrl = tripo3d.getGenerationUrl();

        log.info("收到模型生成请求，图片地址：{}，参数：{}", imageToModelRequest.getUrl(), imageToModelRequest);

        // 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        // 构建请求实体
        HttpEntity<ImageToModelRequest> requestEntity = new HttpEntity<>(imageToModelRequest, headers);

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
        log.warn("模型生成请求异常，状态码：{}，响应体：{}",
                responseEntity.getStatusCode(),
                responseEntity.getBody());

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

    //模型状态查询并保存
    @GetMapping("/polling")
    public ModelBo<TaskBo> taskPolling(@RequestParam("id") String taskId) {
        return aiModelService.taskPolling(taskId);
    }

    //模型状态查询并保存
    @GetMapping("/save")
    public HttpResponse taskText(@RequestParam("id") String taskId) {
        return aiModelService.taskText(taskId);
    }
}
