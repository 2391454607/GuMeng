package com.gumeng.domain.dataset;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 非遗传承人数据实体类
 */
@Data
@TableName("dataset_gumeng")
public class DatasetEntity {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    
    /**
     * 传承人姓名/标题
     */
    @TableField("title")
    private String title;
    
    /**
     * 相关链接
     */
    @TableField("title_link")
    private String titleLink;
    
    /**
     * 性别
     */
    @TableField("gender")
    private String gender;
    
    /**
     * 级别
     */
    @TableField("level")
    private String level;
    
    /**
     * 民族
     */
    @TableField("ethnic")
    private String ethnic;
    
    /**
     * 批次
     */
    @TableField("batch")
    private String batch;
    
    /**
     * 地区
     */
    @TableField("region")
    private String region;
    
    /**
     * 详细介绍
     */
    @TableField("introduce")
    private String introduce;
} 