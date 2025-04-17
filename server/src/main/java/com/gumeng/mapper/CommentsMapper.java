package com.gumeng.mapper;

import com.gumeng.domain.Comments;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author Lorn
 * @description 针对表【comments(帖子评论表)】的数据库操作Mapper
 * @createDate 2025-04-15 08:30:54
 * @Entity com.gumeng.domain.Comments
 */
@Mapper
public interface CommentsMapper extends BaseMapper<Comments> {
    /**
     * 获取帖子下的根评论
     */
    List<Comments> getRootCommentsByPageId(Integer postId);

    /**
     * 增加评论点赞数
     */
    void incrementLikeCount(Integer commentId);

    /**
     * 减少评论点赞数
     */
    void decrementLikeCount(Integer commentId);
}