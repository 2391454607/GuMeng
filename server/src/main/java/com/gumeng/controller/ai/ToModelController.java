package com.gumeng.controller.ai;

import com.gumeng.config.Al.Tripo3d;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * 功能：tripo3d模型生成
 * 作者：Z
 * 日期：2025/6/3 上午9:24
 */

@RestController
@RequestMapping("/model")
public class ToModelController {

    private final Tripo3d tripo3d;

    @Resource
    private RestTemplate restTemplate;

    @Autowired
    public ToModelController(Tripo3d tripo3d) {
        this.tripo3d = tripo3d;
    }

    //

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
}
