package com.gumeng.controller.user;

import com.gumeng.code.HttpResponse;
import com.gumeng.domain.shop.UserBalance;
import com.gumeng.domain.shop.UserBalanceLog;
import com.gumeng.domain.shop.UserPoints;
import com.gumeng.domain.shop.UserPointLog;
import com.gumeng.entity.vo.user.UserAssetVO;
import com.gumeng.service.shop.UserBalanceLogService;
import com.gumeng.service.shop.UserBalanceService;
import com.gumeng.service.shop.UserPointLogService;
import com.gumeng.service.shop.UserPointsService;
import com.gumeng.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 功能：用户资产信息控制类
 * 作者：Z
 * 日期：2025/4/26 下午11:34
 */
@RestController
@RequestMapping("/user")
public class UserInfoController {

    //积分
    @Autowired
    private UserPointsService userPointsService;

    //积分记录
    @Autowired
    private UserPointLogService userPointLogService;

    //金额
    @Autowired
    private UserBalanceService userBalanceService;

    //金额记录
    @Autowired
    private UserBalanceLogService userBalanceLogService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    //获取用户资产信息
    @GetMapping("/getAsset")
    public HttpResponse getAsset() {
        UserAssetVO userAssetVO = new UserAssetVO();
        
        // 从ThreadLocal中获取当前登录用户ID
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");
        userAssetVO.setUserId(userId);
        
        // 获取用户积分信息
        UserPoints points = userPointsService.getUserPoints(userId);
        if (points == null) {
            userAssetVO.setTotalPoints(0);     // 设置默认值0
            userAssetVO.setCurrentPoints(0);   // 设置默认值0
        } else {
            userAssetVO.setTotalPoints(points.getTotalPoints());     // 总积分
            userAssetVO.setCurrentPoints(points.getCurrentPoints()); // 可用积分
        }
        
        // 获取用户余额信息
        UserBalance balance = userBalanceService.getUserBalance(userId);
        if (balance == null) {
            userAssetVO.setTotalAmount(BigDecimal.valueOf(0.00));     // 设置默认值0
            userAssetVO.setCurrentAmount(BigDecimal.valueOf(0.00));   // 设置默认值0
        } else {
            userAssetVO.setTotalAmount(balance.getTotalAmount());     // 累计余额
            userAssetVO.setCurrentAmount(balance.getCurrentAmount()); // 可用余额
        }
        
        return HttpResponse.success(userAssetVO);
    }


    //用户每日签到
    @PostMapping("/dailySign")
    public HttpResponse dailySign() {
        // 获取当前登录用户ID
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");

        // 生成Redis key
        String signKey = "user:sign:" + userId;

        // 检查是否已签到
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(signKey))) {
            return HttpResponse.error("今日已签到");
        }

        try {
            // 执行签到
            Integer rewardPoints = 10; // 签到奖励积分
            
            // 添加积分记录
            UserPointLog pointLog = new UserPointLog();
            pointLog.setUserId(userId);
            pointLog.setChangeType("签到");
            pointLog.setChangeValue(rewardPoints);
            pointLog.setDescription("每日签到奖励");
            pointLog.setCreateTime(LocalDateTime.now());
            userPointLogService.save(pointLog);
            
            // 更新用户总积分
            userPointsService.addPoints(userId, rewardPoints);

            // 记录签到状态，设置过期时间到今天结束
            LocalDateTime midnight = LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            Duration duration = Duration.between(LocalDateTime.now(), midnight);
            stringRedisTemplate.opsForValue().set(signKey, "1", duration);

            return HttpResponse.success("签到成功，获得" + rewardPoints + "积分");
        } catch (Exception e) {
            return HttpResponse.error("签到失败：" + e.getMessage());
        }
    }

    //充值


    //提现


    //获取用户资产流动信息
    @GetMapping("/getAssetLog")
    public HttpResponse getAssetLog() {
        // 获取当前登录用户ID
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");

        try {
            // 获取积分变动记录
            List<UserPointLog> pointLogs = userPointLogService.getLogsByUserId(userId);
            
            // 获取余额变动记录
            List<UserBalanceLog> balanceLogs = userBalanceLogService.getLogsByUserId(userId);
            
            // 封装返回数据
            Map<String, Object> result = new HashMap<>();
            result.put("pointLogs", pointLogs);
            result.put("balanceLogs", balanceLogs);
            
            return HttpResponse.success(result);
        } catch (Exception e) {
            return HttpResponse.error("获取资产流动信息失败：" + e.getMessage());
        }
    }


}
