package com.gumeng.controller.sys;

import com.gumeng.code.HttpResponse;
import com.gumeng.domain.Policy;
import com.gumeng.domain.pages.Carousel;
import com.gumeng.entity.vo.PolicyVO;
import com.gumeng.service.PolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 功能：
 * 作者：Z
 * 日期：2025/4/11 上午9:20
 */
@RestController
@RequestMapping("/sys") //给当前控制器下的所有接口添加前缀
@PreAuthorize("hasAnyAuthority('admin','superAdmin')")
public class policyController {

    @Autowired
    private PolicyService policyService;

    //根据id查询政策
    @GetMapping("/policyInfo")
    public HttpResponse policyInfo(@RequestParam Integer id) {
        Policy policy = policyService.findById(id);
        return HttpResponse.success(policy);
    }

    //新增政策信息
    @PostMapping("/addPolicy")
    public HttpResponse addPolicy(@RequestBody Policy policy) {
        policyService.addPolicy(policy);
        return HttpResponse.success();
    }

    //修改政策信息
    @PostMapping("/updatePolicy")
    public HttpResponse updatePolicy(@RequestBody Policy policy) {
        policyService.updatePolicy(policy);
        return HttpResponse.success();
    }

    //删除
    @PostMapping("/deletePolicy")
    public HttpResponse deletePolicy(@RequestParam Integer id) {
        policyService.deletePolicy(id);
        return HttpResponse.success();
    }
}
