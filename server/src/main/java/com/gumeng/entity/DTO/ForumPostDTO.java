package com.gumeng.entity.DTO;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * @Author:Lorn(黎龙)
 * @Project：GuMeng
 * @Date：2025/4/15
 * @Time:8:36
 */
@Data
public class ForumPostDTO {
    /**
     * 帖子标题
     */
    @NotBlank(message = "标题不能为空")
    @Size(min = 2, max = 50, message = "标题长度需在2-50个字符之间")
    private String title;

    /**
     * 帖子内容
     */
    @NotBlank(message = "内容不能为空")
    @Size(min = 10, max = 5000, message = "内容长度需在10-5000个字符之间")
    private String content;

    /**
     * 话题分类
     */
    @NotBlank(message = "请选择话题")
    private String topic;

    /**
     * 图片JSON数组
     */
    private String images;
}
