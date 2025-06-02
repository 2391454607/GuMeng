package com.gumeng.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 传承人视图对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InheritorVO {
    /**
     * 传承人ID
     */
    private Integer id;
    
    /**
     * 传承人姓名
     */
    private String name;
    
    /**
     * 性别
     */
    private String gender;
    
    /**
     * 民族
     */
    private String ethnic;
    
    /**
     * 出生年份
     */
    private Integer birthYear;
    
    /**
     * 传承人级别
     */
    private String level;
    
    /**
     * 所属地区
     */
    private String region;
    
    /**
     * 项目ID
     */
    private Integer projectId;
    
    /**
     * 传承人简介
     */
    private String description;
    
    /**
     * 传承人头像
     */
    private String avatar;
} 