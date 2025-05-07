package com.gumeng.controller.web;

import com.gumeng.code.HttpResponse;
import com.gumeng.domain.Policy;
import com.gumeng.service.PolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;


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

    //根据id查询政策
    @GetMapping("/getInfo")
    public HttpResponse policyInfo(@RequestParam Integer id) {
        Policy policy = policyService.findById(id);
        return HttpResponse.success(policy);
    }

    // 根据ID下载PDF文件
    @GetMapping("/download")
    public HttpResponse downloadPolicy(@RequestParam Integer id) {
        Policy policy = policyService.findById(id);
        if (policy == null || policy.getContent() == null) {
            return HttpResponse.error("文件不存在");
        }

        String base64Content = Base64.getEncoder().encodeToString(policy.getContent());
        return HttpResponse.success(base64Content);
    }

}
