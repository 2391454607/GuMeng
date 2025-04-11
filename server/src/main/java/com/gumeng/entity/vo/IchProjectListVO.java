package com.gumeng.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IchProjectListVO {
    /**
     * 非遗项目id
     */
    private Integer id;

    /**
     * 项目名称
     */
    private String name;

    /**
     * 所属类别name
     */
    private String categoryName;

    /**
     * 保护级别
     */
    private String levelName;

    /**
     * 封面
     */
    private String coverImage;

    /**
     * 项目简介
     */
    private String summary;

    /**
     * 浏览次数
     */
    private Long viewCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
