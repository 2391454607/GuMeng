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

    //充值
    @Override
    public void addBalance(Integer userId, BigDecimal amount) {
        // 获取用户当前余额信息
        UserBalance userBalance = this.getUserBalance(userId);

        if (userBalance == null) {
            // 如果用户没有余额记录，创建新记录
            userBalance = new UserBalance();
            userBalance.setUserId(userId);
            userBalance.setTotalAmount(amount);
            userBalance.setCurrentAmount(amount);
            userBalanceMapper.addBalance(userBalance);
        } else {
            // 更新现有余额记录
            userBalance.setTotalAmount(userBalance.getTotalAmount().add(amount));
            userBalance.setCurrentAmount(userBalance.getCurrentAmount().add(amount));
            userBalanceMapper.updateBalance(userBalance);
        }
    }

    //提现
    @Override
    public void subtractBalance(Integer userId, BigDecimal amount) {
        // 获取用户当前余额信息
        UserBalance userBalance = this.getUserBalance(userId);

        //
        if (userBalance == null || userBalance.getCurrentAmount().compareTo(amount) < 0) {
            throw new RuntimeException("余额不足");
        }

        // 更新余额
        userBalance.setCurrentAmount(userBalance.getCurrentAmount().subtract(amount));
        baseMapper.updateById(userBalance);
    }
}




