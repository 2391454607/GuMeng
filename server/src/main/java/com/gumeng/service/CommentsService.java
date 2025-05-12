package com.gumeng.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gumeng.domain.forum.Comments;
import com.gumeng.entity.DTO.CommentDTO;
import com.gumeng.entity.vo.CommentVO;

import java.util.List;

/**
 * @author Lorn
 * @description 针对表【comments(帖子评论表)】的数据库操作Service
 * @createDate 2025-04-15 08:38:21
 */
public interface CommentsService extends IService<Comments> {
    /**
     * 添加评论
     */
    Integer addComment(CommentDTO commentDTO);

    /**
     * 获取帖子评论
     */
    List<CommentVO> getCommentsByPostId(Integer postId);

    /**
     * 删除评论
     */
    boolean deleteComment(Integer commentId);

    /**
     * 点赞评论
     */
    boolean likeComment(Integer commentId);

    /**
     * 取消点赞评论
     */
    boolean unlikeComment(Integer commentId);
}