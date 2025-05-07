package com.gumeng.controller.web;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gumeng.code.HttpResponse;
import com.gumeng.domain.Policy;
import com.gumeng.service.PolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public HttpResponse getPolicy(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        Page<Policy> page = new Page<>(current, size);
        QueryWrapper<Policy> queryWrapper = new QueryWrapper<Policy>()
                .select("id", "title", "type", "document_number", "publish_org",
                        "publish_date", "effective_date")
                .orderByDesc("publish_date");

        IPage<Policy> policyPage = policyService.page(page, queryWrapper);
        return HttpResponse.success(policyPage);
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
