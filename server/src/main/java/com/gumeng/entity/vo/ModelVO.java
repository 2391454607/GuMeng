package com.gumeng.entity.vo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 功能：
 * 作者：Z
 * 日期：2025/4/22 下午6:44
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModelVO {
    /**
     *
     */
    private Integer id;

    /**
     * 所属地区
     */
    private String area;

    /**
     * 所属类别
     */
    private String type;

    /**
     * 名称
     */
    private String name;

    /**
     * 模型预览图
     */
    private String modelPic;

    /**
     * 作者
     */
    private String author;

    /**
     * 模型路径
     */
    private String modelUrl;

    /**
     * 浏览次数
     */
    private String views;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 逻辑删除
     */
    private String isDelete;
}
