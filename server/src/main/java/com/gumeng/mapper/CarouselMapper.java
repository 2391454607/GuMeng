package com.gumeng.mapper;

import com.gumeng.domain.pages.Carousel;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

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

}




