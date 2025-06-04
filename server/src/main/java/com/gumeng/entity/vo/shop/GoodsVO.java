package com.gumeng.entity.vo.shop;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 功能：
 * 作者：Z
 * 日期：2025/6/4 下午4:04
 */
@Data
public class GoodsVO {
    /**
     * 主键
     */
    private Long id;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品封面图
     */
    private String imageUrl;

    /**
     * 纯金额价格
     */
    private Integer priceMoney;

    /**
     * 混合支付的金额价格
     */
    private BigDecimal mixedPriceMoney;

    /**
     * 混合支付的所需积分
     */
    private Integer mixedPricePoints;

    /**
     * 库存数量
     */
    private Integer stock;

    /**
     * 是否上架（0上架、1下架）
     */
    private Integer isAvailable;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
