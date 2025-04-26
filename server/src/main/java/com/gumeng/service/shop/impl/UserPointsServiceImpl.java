package com.gumeng.service.shop.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gumeng.domain.shop.UserPoints;
import com.gumeng.service.shop.UserPointsService;
import com.gumeng.mapper.shop.UserPointsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Chine
* @description 针对表【user_points(用户积分表)】的数据库操作Service实现
* @createDate 2025-04-26 23:23:52
*/
@Service
public class UserPointsServiceImpl extends ServiceImpl<UserPointsMapper, UserPoints>
    implements UserPointsService{

    @Autowired
    private UserPointsMapper userPointsMapper;


    @Override
    public UserPoints getUserPoints(Integer userId) {
        return userPointsMapper.getPoints(userId);
    }
}




