package com.gumeng.controller.user;

import com.gumeng.code.HttpResponse;
import com.gumeng.domain.shop.UserBalance;
import com.gumeng.domain.shop.UserPoints;
import com.gumeng.entity.vo.user.UserAssetVO;
import com.gumeng.service.shop.UserBalanceService;
import com.gumeng.service.shop.UserPointsService;
import com.gumeng.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 功能：用户信息控制类
 * 作者：Z
 * 日期：2025/4/26 下午11:34
 */
@RestController
@RequestMapping("/user")
public class UserInfoController {

    //积分
    @Autowired
    private UserPointsService userPointsService;
    //金额
    @Autowired
    private UserBalanceService userBalanceService;


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
            return HttpResponse.error("获取积分信息失败");
        }
        userAssetVO.setTotalPoints(points.getTotalPoints());     // 总积分
        userAssetVO.setCurrentPoints(points.getCurrentPoints()); // 可用积分
        
        // 获取用户余额信息
        UserBalance balance = userBalanceService.getUserBalance(userId);
        if (balance == null) {
            return HttpResponse.error("获取余额信息失败");
        }
        userAssetVO.setTotalAmount(balance.getTotalAmount());     // 累计余额
        userAssetVO.setCurrentAmount(balance.getCurrentAmount()); // 可用余额
        
        return HttpResponse.success(userAssetVO);
    }
}
