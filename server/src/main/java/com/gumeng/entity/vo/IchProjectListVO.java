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
     * 地区ID
     */
    private Integer regionId;

    /**
     * 地区名称
     */
    private String regionName;

    /**
     * 图片JSON数组
     */
    private String images;

    /**
     * 视频URL
     */
    private String video;

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
