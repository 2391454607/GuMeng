package com.gumeng.service;

import com.gumeng.domain.pages.Carousel;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Chine
* @description 针对表【carousel(轮播图表)】的数据库操作Service
* @createDate 2025-03-23 15:05:27
*/

@Service
public interface CarouselService extends IService<Carousel> {

    //获取轮播图
    List<Carousel> getCarousel();
}
