package com.gumeng.service.shop.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gumeng.domain.shop.UserBalance;
import com.gumeng.service.shop.UserBalanceService;
import com.gumeng.mapper.shop.UserBalanceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
* @author Chine
* @description 针对表【user_balance(用户金额账户表)】的数据库操作Service实现
* @createDate 2025-04-26 23:23:52
*/
@Service
public class UserBalanceServiceImpl extends ServiceImpl<UserBalanceMapper, UserBalance>
    implements UserBalanceService{

    @Autowired
    private UserBalanceMapper userBalanceMapper;

    //获取用户余额
    @Override
    public UserBalance getUserBalance(Integer userId) {
        return userBalanceMapper.getBalance(userId);
    }
}




