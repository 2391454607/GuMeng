package com.gumeng.controller.sys;

import com.gumeng.code.HttpResponse;
import com.gumeng.domain.pages.Carousel;
import com.gumeng.service.sys.CarouselService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


/**
 * 功能：
 * 作者：Z
 * 日期：2025/3/22 下午8:38
 */

@RestController
@RequestMapping("/sys") //给当前控制器下的所有接口添加前缀
@PreAuthorize("hasAnyAuthority('admin','superAdmin')")
public class CarouselController {

    @Autowired
    private CarouselService carouselService;

    //根据id查询轮播图
    @GetMapping("/CarouselInfo")
    public HttpResponse CarouselInfo(@RequestParam Integer id) {
        Carousel carousel = carouselService.findById(id);
        return HttpResponse.success(carousel);
    }

    //新增轮播图
    @PostMapping("/addCarousel")
    public HttpResponse setCarousel(@RequestBody Carousel carousel) {
        carouselService.addCarousel(carousel);
        return HttpResponse.success();
    }

    //修改轮播图
    @PostMapping("/updateCarousel")
    public HttpResponse updateCarousel(@RequestBody Carousel carousel) {
        carouselService.updateCarousel(carousel);
        return HttpResponse.success();
    }

    //删除
    @PostMapping("/deleteCarousel")
    public HttpResponse deleteCarousel(@RequestBody Carousel carousel) {
        carouselService.deleteCarousel(carousel);
        return HttpResponse.success();
    }



}
