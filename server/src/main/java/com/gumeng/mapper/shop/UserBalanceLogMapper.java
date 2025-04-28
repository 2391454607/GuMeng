package com.gumeng.mapper.shop;

import com.gumeng.domain.shop.UserBalanceLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
* @author Chine
* @description 针对表【user_balance_log(金额流水记录表)】的数据库操作Mapper
* @createDate 2025-04-26 23:23:52
* @Entity com.gumeng.domain.shop.UserBalanceLog
*/
public interface UserBalanceLogMapper extends BaseMapper<UserBalanceLog> {

    List<UserBalanceLog> getUserBalanceLog(Integer userId);
}




