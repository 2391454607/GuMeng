package com.gumeng.controller.web;

import com.gumeng.code.HttpResponse;
import com.gumeng.domain.Policy;
import com.gumeng.service.sys.CarouselService;
import com.gumeng.service.PolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    private CarouselService carouselService;


    //获取轮播图列表
    @GetMapping("/getCarousel")
    public HttpResponse getCarousel() {
        Object carousel = carouselService.getCarousel();
        return HttpResponse.success(carousel);
    }


}
