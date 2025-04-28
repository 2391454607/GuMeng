package com.gumeng.service.shop;

import com.gumeng.domain.shop.UserBalanceLog;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author Chine
* @description 针对表【user_balance_log(金额流水记录表)】的数据库操作Service
* @createDate 2025-04-26 23:23:52
*/
public interface UserBalanceLogService extends IService<UserBalanceLog> {

    List<UserBalanceLog> getLogsByUserId(Integer userId);
}
