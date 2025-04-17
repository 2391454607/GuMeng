package com.gumeng.entity.vo;


import lombok.Data;
import java.time.LocalDateTime;

/**
 * @Author:Lorn(黎龙)
 * @Project：GuMeng
 * @Date：2025/4/15
 * @Time:8:36
 */
@Data
public class ForumPostVO {
    /**
     * 帖子ID
     */
    private Integer id;

    /**
     * 帖子标题
     */
    private String title;

    /**
     * 帖子内容
     */
    private String content;

    /**
     * 话题分类
     */
    private String topic;

    /**
     * 图片JSON数组
     */
    private String images;

    /**
     * 评论数
     */
    private Integer commonNum;

    /**
     * 点赞数
     */
    private Integer thumbsUpNum;

    /**
     * 浏览量
     */
    private Integer viewCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 发帖用户ID
     */
    private Integer userId;

    /**
     * 发帖用户名
     */
    private String username;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 当前用户是否点赞
     */
    private Boolean isLiked;
}
