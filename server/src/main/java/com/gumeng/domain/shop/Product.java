package com.gumeng.domain.shop;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 文创商品表
 * @TableName product
 */
@TableName(value ="product")
@Data
public class Product implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商品名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 商品封面图
     */
    @TableField(value = "image_url")
    private String imageUrl;

    /**
     * 纯金额价格
     */
    @TableField(value = "price_money")
    private Integer priceMoney;

    /**
     * 混合支付的金额价格
     */
    @TableField(value = "mixed_price_money")
    private BigDecimal mixedPriceMoney;

    /**
     * 混合支付的所需积分
     */
    @TableField(value = "mixed_price_points")
    private Integer mixedPricePoints;

    /**
     * 库存数量
     */
    @TableField(value = "stock")
    private Integer stock;

    /**
     * 是否上架（0上架、1下架）
     */
    @TableField(value = "is_available")
    private Integer isAvailable;

    /**
     * 创建时间
     */
    @TableField(value = "created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        Product other = (Product) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getName() == null ? other.getName() == null : this.getName().equals(other.getName()))
            && (this.getImageUrl() == null ? other.getImageUrl() == null : this.getImageUrl().equals(other.getImageUrl()))
            && (this.getPriceMoney() == null ? other.getPriceMoney() == null : this.getPriceMoney().equals(other.getPriceMoney()))
            && (this.getMixedPriceMoney() == null ? other.getMixedPriceMoney() == null : this.getMixedPriceMoney().equals(other.getMixedPriceMoney()))
            && (this.getMixedPricePoints() == null ? other.getMixedPricePoints() == null : this.getMixedPricePoints().equals(other.getMixedPricePoints()))
            && (this.getStock() == null ? other.getStock() == null : this.getStock().equals(other.getStock()))
            && (this.getIsAvailable() == null ? other.getIsAvailable() == null : this.getIsAvailable().equals(other.getIsAvailable()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + ((getImageUrl() == null) ? 0 : getImageUrl().hashCode());
        result = prime * result + ((getPriceMoney() == null) ? 0 : getPriceMoney().hashCode());
        result = prime * result + ((getMixedPriceMoney() == null) ? 0 : getMixedPriceMoney().hashCode());
        result = prime * result + ((getMixedPricePoints() == null) ? 0 : getMixedPricePoints().hashCode());
        result = prime * result + ((getStock() == null) ? 0 : getStock().hashCode());
        result = prime * result + ((getIsAvailable() == null) ? 0 : getIsAvailable().hashCode());
        result = prime * result + ((getCreatedAt() == null) ? 0 : getCreatedAt().hashCode());
        result = prime * result + ((getUpdatedAt() == null) ? 0 : getUpdatedAt().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", name=").append(name);
        sb.append(", imageUrl=").append(imageUrl);
        sb.append(", priceMoney=").append(priceMoney);
        sb.append(", mixedPriceMoney=").append(mixedPriceMoney);
        sb.append(", mixedPricePoints=").append(mixedPricePoints);
        sb.append(", stock=").append(stock);
        sb.append(", isAvailable=").append(isAvailable);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}