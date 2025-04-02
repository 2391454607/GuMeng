package com.gumeng.controller.sys;

import com.gumeng.domain.Result;
import com.gumeng.domain.pages.Carousel;
import com.gumeng.service.CarouselService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

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

    //根据id查询轮播图
    @GetMapping("/CarouselInfo")
    public Result<Carousel> CarouselInfo(@RequestParam Integer id) {
        Carousel carousel = carouselService.findById(id);
        return Result.success(carousel);
    }

    //新增轮播图
    @PostMapping("/addCarousel")
    public Result setCarousel(@RequestBody Carousel carousel) {
        carouselService.addCarousel(carousel);
        return Result.success();
    }

    //修改轮播图
    @PostMapping("/updateCarousel")
    public Result updateCarousel(@RequestParam Integer id, String imageUrl) {
        carouselService.updateCarousel(id, imageUrl);
        return Result.success();
    }

    //删除
    @PostMapping("/deleteCarousel")
    public Result deleteCarousel(@RequestParam Integer id) {
        carouselService.deleteCarousel(id);
        return Result.success();
    }



}
