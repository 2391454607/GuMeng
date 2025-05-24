package com.gumeng.controller.Forum;

import com.gumeng.annotation.LogOperation;
import com.gumeng.code.HttpResponse;
import com.gumeng.entity.DTO.CommentDTO;
import com.gumeng.entity.vo.CommentVO;
import com.gumeng.service.CommentsService;
import com.gumeng.service.ContentAuditService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    
    @Autowired
    private ContentAuditService contentAuditService;

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
    @LogOperation(module = "论坛", operation = "添加评论")
    @PostMapping("/posts/{postId}/comments")
    public HttpResponse addComment(
            @PathVariable Integer postId,
            @RequestBody @Valid CommentDTO commentDTO) {
        
        commentDTO.setPageId(postId);
        
        // 对评论内容进行智能审核
        ContentAuditService.AuditResult auditResult = contentAuditService.auditContent(commentDTO.getContent());
        
        // 如果评论中包含敏感词且AI判断不合规，则拒绝发布
        if (!auditResult.getSensitiveWords().isEmpty() && !auditResult.isPassed()) {
            Map<String, Object> result = new HashMap<>();
            result.put("sensitiveWords", auditResult.getSensitiveWords());
            result.put("message", "评论包含敏感词且上下文不合规，请修改后重试");
            return HttpResponse.error("评论包含敏感词，请修改后重试").setData(result);
        }
        
        // 使用审核后的内容
        commentDTO.setContent(auditResult.getFilteredContent());
        
        // 添加评论
        Integer commentId = commentsService.addComment(commentDTO);
        
        // 如果存在敏感词，但AI判断无害，告知用户
        if (!auditResult.getSensitiveWords().isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("commentId", commentId);
            result.put("message", "评论中包含敏感词，但经AI判断上下文合规，已允许发布");
            return HttpResponse.success(result);
        }
        
        return HttpResponse.success(commentId);
    }

    /**
     * 删除评论
     */
    @LogOperation(module = "论坛", operation = "删除评论")
    @DeleteMapping("/comments/{id}")
    public HttpResponse deleteComment(@PathVariable Integer id) {
        boolean result = commentsService.deleteComment(id);
        return result ? HttpResponse.success() : HttpResponse.error("删除失败，评论不存在或您没有权限");
    }

    /**
     * 评论点赞
     */
    @LogOperation(module = "论坛", operation = "点赞评论")
    @PostMapping("/comments/{id}/like")
    public HttpResponse likeComment(@PathVariable Integer id) {
        boolean result = commentsService.likeComment(id);
        return result ? HttpResponse.success() : HttpResponse.error("点赞失败");
    }

    /**
     * 取消评论点赞
     */
    @LogOperation(module = "论坛", operation = "取消评论点赞")
    @PostMapping("/comments/{id}/unlike")
    public HttpResponse unlikeComment(@PathVariable Integer id) {
        boolean result = commentsService.unlikeComment(id);
        return result ? HttpResponse.success() : HttpResponse.error("取消点赞失败");
    }
}