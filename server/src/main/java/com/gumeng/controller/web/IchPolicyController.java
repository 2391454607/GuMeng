package com.gumeng.controller.web;

import com.gumeng.code.HttpResponse;
import com.gumeng.service.PolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * 功能：用户非遗政策相关功能
 * 作者：Z
 * 日期：2025/5/5 下午5:01
 */
@RestController
@RequestMapping("/web/policy")
public class IchPolicyController {

    @Autowired
    private PolicyService policyService;

    //获取政策列表
    @GetMapping("/getList")
    public HttpResponse getPolicy() {
        Object getPolicy = policyService.getPolicyList();
        return HttpResponse.success(getPolicy);
    }

}
