package com.gumeng.service.shop.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gumeng.domain.shop.UserPoints;
import com.gumeng.mapper.shop.UserPointsMapper;
import com.gumeng.service.shop.UserPointsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Chine
 * @description 针对表【user_points(用户积分表)】的数据库操作Service实现
 * @createDate 2025-04-26 23:23:52
 */
@Service
public class UserPointsServiceImpl extends ServiceImpl<UserPointsMapper, UserPoints>
        implements UserPointsService {

    @Autowired
    private UserPointsMapper userPointsMapper;


    @Override
    public UserPoints getUserPoints(Integer userId) {
        return userPointsMapper.getPoints(userId);
    }

    @Override
    public void addPoints(Integer userId, Integer rewardPoints) {
        // 获取用户当前积分信息
        UserPoints userPoints = this.getUserPoints(userId);

        if (userPoints == null) {
            // 如果用户没有积分记录，创建新记录
            userPoints = new UserPoints();
            userPoints.setUserId(userId);
            userPoints.setTotalPoints(rewardPoints);
            userPoints.setCurrentPoints(rewardPoints);
            userPointsMapper.addUserPoints(userPoints);
        } else {
            // 更新现有积分记录
            userPoints.setTotalPoints(userPoints.getTotalPoints() + rewardPoints);
            userPoints.setCurrentPoints(userPoints.getCurrentPoints() + rewardPoints);
            userPointsMapper.updateUserPointsById(userPoints);
        }
    }
}




