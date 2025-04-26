package com.gumeng.entity.vo.user;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 功能：用户资产
 * 作者：Z
 * 日期：2025/4/26 下午11:30
 */
@Data
public class UserAssetVO {

    /**
     * 用户id
     */
    @TableField(value = "user_id")
    private Integer userId;

    /**
     * 总积分
     */
    @TableField(value = "total_points")
    private Integer totalPoints;

    /**
     * 可用积分
     */
    @TableField(value = "current_points")
    private Integer currentPoints;

    /**
     * 总充值金额（累计）
     */
    @TableField(value = "total_amount")
    private BigDecimal totalAmount;

    /**
     * 当前可用余额
     */
    @TableField(value = "current_amount")
    private BigDecimal currentAmount;
}
