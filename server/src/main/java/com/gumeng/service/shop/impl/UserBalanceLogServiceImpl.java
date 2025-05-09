package com.gumeng.service.shop.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gumeng.domain.shop.UserBalanceLog;
import com.gumeng.service.shop.UserBalanceLogService;
import com.gumeng.mapper.shop.UserBalanceLogMapper;
import org.springframework.stereotype.Service;


/**
* @author Chine
* @description 针对表【user_balance_log(金额流水记录表)】的数据库操作Service实现
* @createDate 2025-04-26 23:23:52
*/
@Service
public class UserBalanceLogServiceImpl extends ServiceImpl<UserBalanceLogMapper, UserBalanceLog>
    implements UserBalanceLogService {

    @Override
    public IPage<UserBalanceLog> getPageByUserId(Integer userId, IPage<UserBalanceLog> page) {
        return lambdaQuery()
                .eq(UserBalanceLog::getUserId, userId)
                .orderByDesc(UserBalanceLog::getCreateTime)
                .page(page);
    }
}




