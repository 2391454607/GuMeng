package com.gumeng.domain.pages;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 非遗地区表
 * @TableName ich_region
 */
@TableName(value ="ich_region")
@Data
public class IchRegion implements Serializable {
    /**
     * 地区id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 地区名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 地区描述
     */
    @TableField(value = "description")
    private String description;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
} 