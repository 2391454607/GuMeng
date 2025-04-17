package com.gumeng.entity.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author:Lorn(黎龙)
 * @Project：GuMeng
 * @Date：2025/4/15
 * @Time:8:36
 */

@Data
public class CommentVO {
    /**
     * 评论ID
     */
    private Integer id;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 点赞数
     */
    private Integer thumbsUp;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 评论用户ID
     */
    private Integer userId;

    /**
     * 评论用户名
     */
    private String username;

    /**
     * 评论用户头像
     */
    private String userPic;

    /**
     * 帖子ID
     */
    private Integer pageId;

    /**
     * 父评论ID
     */
    private String parentId;

    /**
     * 回复的用户ID
     */
    private String forUserId;

    /**
     * 回复的用户名
     */
    private String forUsername;

    /**
     * 当前用户是否点赞
     */
    private Boolean isLiked;

    /**
     * 子评论列表
     */
    private List<CommentVO> children;
}
