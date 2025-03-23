package com.gumeng.controller.sys;

import com.gumeng.domain.pages.Carousel;
import com.gumeng.service.CarouselService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 功能：
 * 作者：Z
 * 日期：2025/3/22 下午8:38
 */

@RestController
@RequestMapping("/sys") //给当前控制器下的所有接口添加前缀
public class CarouselController {

    @Resource
    private CarouselService carouselService;

    //获取轮播图数据
    @GetMapping("/getCarousel")
    public List<Carousel> getCarousel() {

        return carouselService.getCarousel();
    }



}
