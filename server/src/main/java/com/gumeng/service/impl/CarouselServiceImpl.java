package com.gumeng.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gumeng.domain.pages.Carousel;
import com.gumeng.service.CarouselService;
import com.gumeng.mapper.CarouselMapper;
import com.gumeng.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* @author Chine
* @description 针对表【carousel(轮播图表)】的数据库操作Service实现
* @createDate 2025-03-23 15:05:27
*/
@Service
public class CarouselServiceImpl extends ServiceImpl<CarouselMapper, Carousel>
    implements CarouselService {

    @Autowired
    private CarouselMapper carouselMapper;

    @Override
    public List<Carousel> getCarousel() {
        return carouselMapper.getCarousel();
    }

    @Override
    public Carousel findById(Integer id) {
        return carouselMapper.findById(id);
    }

    @Override
    public void insertCarousel(Carousel carousel) {
        carouselMapper.insertCarousel(carousel);
    }

    @Override
    public void updateCarousel(Integer id, String imageUrl) {
        carouselMapper.updateCarousel(id, imageUrl);
    }

    @Override
    public void deleteCarousel(Integer id) {
        carouselMapper.deleteCarousel(id);
    }


}




