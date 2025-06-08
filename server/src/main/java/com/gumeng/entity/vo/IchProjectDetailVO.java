package com.gumeng.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 非遗项目详情视图对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IchProjectDetailVO {
    /**
     * 非遗项目id
     */
    private Integer id;

    /**
     * 项目名称
     */
    private String name;

    /**
     * 所属类别id
     */
    private Integer categoryId;

    /**
     * 所属类别name
     */
    private String categoryName;

    /**
     * 保护级别id
     */
    private Integer levelId;

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
     * 封面图片URL
     */
    private String coverImage;

    /**
     * 视频URL
     */
    private String video;

    /**
     * 详细内容
     */
    private String content;

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

    /**
     * 传承人列表
     */
    private List<InheritorVO> inheritors;

    /**
     * 相关项目列表
     */
    private List<IchProjectListVO> relatedProjects;
} 