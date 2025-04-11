package com.gumeng.controller;

import com.gumeng.code.HttpResponse;
import com.gumeng.service.PolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 功能：web业务接口
 * 作者：Z
 * 日期：2025/4/3 下午9:05
 */
@RestController
@RequestMapping("/web") //给当前控制器下的所有接口添加前缀
public class WebController {

    @Autowired
    private PolicyService policyService;

    @GetMapping("/getPolicyList")
    public HttpResponse getPolicy() {
        Object getPolicy = policyService.getPolicyList();
        return HttpResponse.success(getPolicy);
    }
}
