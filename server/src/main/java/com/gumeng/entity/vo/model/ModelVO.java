package com.gumeng.entity.vo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 功能：model基本信息
 * 描述：用于前端页面概览信息
 * 作者：Z
 * 日期：2025/4/22 下午10:42
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModelVO {

    /**
     *id
     */
    private Integer id;

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
     * 浏览次数
     */
    private String views;

}
