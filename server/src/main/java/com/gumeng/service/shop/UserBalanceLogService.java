package com.gumeng.service.shop;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gumeng.domain.shop.UserBalanceLog;
import com.baomidou.mybatisplus.extension.service.IService;


/**
* @author Chine
* @description 针对表【user_balance_log(金额流水记录表)】的数据库操作Service
* @createDate 2025-04-26 23:23:52
*/
public interface UserBalanceLogService extends IService<UserBalanceLog> {
    /**
     * 分页获取用户余额变动记录
     */
    IPage<UserBalanceLog> getPageByUserId(Integer userId, IPage<UserBalanceLog> page);
}
