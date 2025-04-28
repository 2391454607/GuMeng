package com.gumeng.service.shop.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gumeng.domain.shop.UserBalanceLog;
import com.gumeng.service.shop.UserBalanceLogService;
import com.gumeng.mapper.shop.UserBalanceLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Chine
* @description 针对表【user_balance_log(金额流水记录表)】的数据库操作Service实现
* @createDate 2025-04-26 23:23:52
*/
@Service
public class UserBalanceLogServiceImpl extends ServiceImpl<UserBalanceLogMapper, UserBalanceLog>
    implements UserBalanceLogService{

    @Autowired
    private UserBalanceLogMapper userBalanceLogMapper;

    @Override
    public List<UserBalanceLog> getLogsByUserId(Integer userId) {
        return userBalanceLogMapper.getUserBalanceLog(userId);
    }
}




