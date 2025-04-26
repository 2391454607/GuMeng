package com.gumeng.mapper.shop;

import com.gumeng.domain.shop.UserPoints;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;


/**
* @author Chine
* @description 针对表【user_points(用户积分表)】的数据库操作Mapper
* @createDate 2025-04-26 23:23:52
* @Entity com.gumeng.domain.shop.UserPoints
*/
@Mapper
public interface UserPointsMapper extends BaseMapper<UserPoints> {


    UserPoints getPoints(Integer userId);
}




