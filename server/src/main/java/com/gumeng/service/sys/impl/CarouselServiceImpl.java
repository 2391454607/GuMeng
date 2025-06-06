package com.gumeng.service.sys.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gumeng.domain.pages.Carousel;
import com.gumeng.service.sys.CarouselService;
import com.gumeng.mapper.sys.CarouselMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


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
    public void addCarousel(Carousel carousel) {
        carouselMapper.addCarousel(carousel);
    }

    @Override
    public void updateCarousel(Carousel carousel) {
        carouselMapper.updateCarousel(carousel);
    }

    @Override
    public void deleteCarousel(Carousel carousel) {
        carouselMapper.deleteCarousel(carousel);
    }


}




