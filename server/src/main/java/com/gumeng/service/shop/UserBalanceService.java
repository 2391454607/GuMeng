package com.gumeng.service.shop;

import com.gumeng.domain.shop.UserBalance;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.List;

/**
* @author Chine
* @description 针对表【user_balance(用户金额账户表)】的数据库操作Service
* @createDate 2025-04-26 23:23:52
*/
public interface UserBalanceService extends IService<UserBalance> {


    UserBalance getUserBalance(Integer userId);

    void addBalance(Integer userId, BigDecimal amount);

    void subtractBalance(Integer userId, BigDecimal amount);
}
