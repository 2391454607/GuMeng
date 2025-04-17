package com.gumeng.entity.DTO;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * @Author:Lorn(黎龙)
 * @Project：GuMeng
 * @Date：2025/4/15
 * @Time:8:36
 */
@Data
public class CommentDTO {
    /**
     * 帖子ID
     */
    @NotNull(message = "帖子ID不能为空")
    private Integer pageId;

    /**
     * 评论内容
     */
    @NotBlank(message = "评论内容不能为空")
    private String content;

    /**
     * 父评论ID，顶级评论为null
     */
    private String parent;

    /**
     * 回复的用户ID
     */
    private String forUser;
}
