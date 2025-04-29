package com.gumeng.service.shop.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gumeng.domain.shop.UserPointLog;
import com.gumeng.service.shop.UserPointLogService;
import com.gumeng.mapper.shop.UserPointLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Chine
* @description 针对表【user_point_log(积分明细表)】的数据库操作Service实现
* @createDate 2025-04-26 23:23:52
*/
@Service
public class UserPointLogServiceImpl extends ServiceImpl<UserPointLogMapper, UserPointLog>
    implements UserPointLogService{

    @Override
    public IPage<UserPointLog> getPageByUserId(Integer userId, IPage<UserPointLog> page) {
        return lambdaQuery()
                .eq(UserPointLog::getUserId, userId)
                .orderByDesc(UserPointLog::getCreateTime)
                .page(page);
    }
}




