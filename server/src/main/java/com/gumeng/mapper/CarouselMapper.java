package com.gumeng.mapper;

import com.gumeng.domain.pages.Carousel;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
* @author Chine
* @description 针对表【carousel(轮播图表)】的数据库操作Mapper
* @createDate 2025-03-23 15:05:27
* @Entity com.gumeng.domain.pages.Carousel
*/

@Mapper
public interface CarouselMapper extends BaseMapper<Carousel> {

    //获取轮播图
    List<Carousel> getCarousel();
    //根据Id查询轮播图
    Carousel findById(Integer id);
    //新增
    void addCarousel(Carousel carousel);
    //修改
    void updateCarousel(Carousel carousel);
    //删除
    void deleteCarousel(Carousel carousel);
}




