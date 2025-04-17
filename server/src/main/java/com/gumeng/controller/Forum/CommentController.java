package com.gumeng.controller.Forum;

import com.gumeng.code.HttpResponse;
import com.gumeng.entity.DTO.CommentDTO;
import com.gumeng.entity.vo.CommentVO;
import com.gumeng.service.CommentsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author:Lorn(黎龙)
 * @Project：GuMeng
 * @Date：2025/4/15
 * @Time:8:42
 */
@RestController
@RequestMapping("/forum")
public class CommentController {

    @Autowired
    private CommentsService commentsService;

    /**
     * 获取帖子评论
     */
    @GetMapping("/posts/{postId}/comments")
    public HttpResponse getComments(@PathVariable Integer postId) {
        List<CommentVO> comments = commentsService.getCommentsByPostId(postId);
        return HttpResponse.success(comments);
    }

    /**
     * 添加评论
     */
    @PostMapping("/posts/{postId}/comments")
    public HttpResponse addComment(
            @PathVariable Integer postId,
            @RequestBody @Valid CommentDTO commentDTO) {
        commentDTO.setPageId(postId);
        Integer commentId = commentsService.addComment(commentDTO);
        return HttpResponse.success(commentId);
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/comments/{id}")
    public HttpResponse deleteComment(@PathVariable Integer id) {
        boolean result = commentsService.deleteComment(id);
        return result ? HttpResponse.success() : HttpResponse.error("删除失败，评论不存在或您没有权限");
    }

    /**
     * 评论点赞
     */
    @PostMapping("/comments/{id}/like")
    public HttpResponse likeComment(@PathVariable Integer id) {
        boolean result = commentsService.likeComment(id);
        return result ? HttpResponse.success() : HttpResponse.error("点赞失败");
    }

    /**
     * 取消评论点赞
     */
    @PostMapping("/comments/{id}/unlike")
    public HttpResponse unlikeComment(@PathVariable Integer id) {
        boolean result = commentsService.unlikeComment(id);
        return result ? HttpResponse.success() : HttpResponse.error("取消点赞失败");
    }
}