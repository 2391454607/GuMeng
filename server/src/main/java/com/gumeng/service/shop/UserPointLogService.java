package com.gumeng.service.shop;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gumeng.domain.shop.UserPointLog;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author Chine
* @description 针对表【user_point_log(积分明细表)】的数据库操作Service
* @createDate 2025-04-26 23:23:52
*/
public interface UserPointLogService extends IService<UserPointLog> {

    /**
     * 分页获取用户积分变动记录
     */
    IPage<UserPointLog> getPageByUserId(Integer userId, IPage<UserPointLog> page);
}
