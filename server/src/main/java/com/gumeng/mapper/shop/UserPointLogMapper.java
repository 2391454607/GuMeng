package com.gumeng.mapper.shop;

import com.gumeng.domain.shop.UserPointLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
* @author Chine
* @description 针对表【user_point_log(积分明细表)】的数据库操作Mapper
* @createDate 2025-04-26 23:23:52
* @Entity com.gumeng.domain.shop.UserPointLog
*/
public interface UserPointLogMapper extends BaseMapper<UserPointLog> {

    List<UserPointLog> getUserPointLog(Integer userId);
}




