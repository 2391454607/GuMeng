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
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
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
    @PostMapping("/recharge")
    public HttpResponse recharge(@RequestBody Map<String, BigDecimal> params) {
        BigDecimal amount = params.get("amount");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return HttpResponse.error("充值金额必须大于0");
        }

        // 获取当前登录用户ID
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");

        try {
            // 添加余额变动记录
            UserBalanceLog balanceLog = new UserBalanceLog();
            balanceLog.setUserId(userId);
            balanceLog.setChangeType("充值");
            balanceLog.setChangeAmount(amount);
            balanceLog.setDescription("用户充值");
            balanceLog.setCreateTime(LocalDateTime.now());
            userBalanceLogService.save(balanceLog);

            // 更新用户余额
            userBalanceService.addBalance(userId, amount);

            return HttpResponse.success("充值成功");
        } catch (Exception e) {
            return HttpResponse.error("充值失败：" + e.getMessage());
        }
    }

    //提现
    @PostMapping("/withdraw")
    public HttpResponse withdraw(@RequestBody Map<String, BigDecimal> params) {
        BigDecimal amount = params.get("amount");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return HttpResponse.error("提现金额必须大于0");
        }

        // 获取当前登录用户ID
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");

        try {
            // 检查余额是否足够
            UserBalance balance = userBalanceService.getUserBalance(userId);
            if (balance == null || balance.getCurrentAmount().compareTo(amount) < 0) {
                return HttpResponse.error("余额不足");
            }

            // 添加余额变动记录
            UserBalanceLog balanceLog = new UserBalanceLog();
            balanceLog.setUserId(userId);
            balanceLog.setChangeType("提现");
            balanceLog.setChangeAmount(amount.negate());  // 使用负数表示减少
            balanceLog.setDescription("用户提现");
            balanceLog.setCreateTime(LocalDateTime.now());
            userBalanceLogService.save(balanceLog);

            // 更新用户余额
            userBalanceService.subtractBalance(userId, amount);

            return HttpResponse.success("提现申请成功，请等待审核");
        } catch (Exception e) {
            return HttpResponse.error("提现失败：" + e.getMessage());
        }
    }


    //获取用户积分流动信息
    @GetMapping("/getPointLog")
    public HttpResponse getPointLog(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        // 获取当前登录用户ID
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");

        try {
            // 获取积分变动记录
            IPage<UserPointLog> pointLogPage = new Page<>(pageNum, pageSize);
            IPage<UserPointLog> pointLogs = userPointLogService.getPageByUserId(userId, pointLogPage);
            
            return HttpResponse.success(pointLogs);
        } catch (Exception e) {
            return HttpResponse.error("获取积分流动信息失败：" + e.getMessage());
        }
    }

    //获取用户余额流动信息
    @GetMapping("/getBalanceLog")
    public HttpResponse getBalanceLog(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        // 获取当前登录用户ID
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");

        try {
            // 获取余额变动记录
            IPage<UserBalanceLog> balanceLogPage = new Page<>(pageNum, pageSize);
            IPage<UserBalanceLog> balanceLogs = userBalanceLogService.getPageByUserId(userId, balanceLogPage);

            return HttpResponse.success(balanceLogs);
        } catch (Exception e) {
            return HttpResponse.error("获取余额流动信息失败：" + e.getMessage());
        }
    }


}
