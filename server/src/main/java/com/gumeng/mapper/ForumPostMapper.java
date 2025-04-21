package com.gumeng.mapper;

import com.gumeng.domain.ForumPost;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Lorn
 * @description 针对表【forum_post(帖子表)】的数据库操作Mapper
 * @createDate 2025-04-15 08:30:54
 * @Entity com.gumeng.domain.ForumPost
 */
@Mapper
public interface ForumPostMapper extends BaseMapper<ForumPost> {
    /**
     * 增加评论数
     */
    void incrementCommentCount(Integer postId);

    /**
     * 减少评论数
     */
    void decrementCommentCount(Integer postId);

    /**
     * 增加点赞数
     */
    void incrementLikeCount(Integer postId);

    /**
     * 减少点赞数
     */
    void decrementLikeCount(Integer postId);

    /**
     * 增加浏览量
     */
    void incrementViewCount(Integer postId);
}