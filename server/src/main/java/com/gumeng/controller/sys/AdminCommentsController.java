package com.gumeng.controller.sys;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gumeng.code.HttpResponse;
import com.gumeng.domain.forum.Comments;
import com.gumeng.domain.forum.ForumPost;
import com.gumeng.domain.user.User;
import com.gumeng.entity.vo.CommentVO;
import com.gumeng.mapper.user.UserMapper;
import com.gumeng.service.CommentsService;
import com.gumeng.service.ForumPostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author:Lorn(黎龙)
 * @Project：GuMeng
 * @Date：2025/5/13
 * @Time:8:20
 */
@RestController
@RequestMapping("/sys/comments")
@PreAuthorize("hasAnyAuthority('admin','superAdmin')")
@Slf4j
public class AdminCommentsController {

    @Autowired
    private CommentsService commentsService;
    
    @Autowired
    private ForumPostService forumPostService;
    
    @Autowired
    private UserMapper userMapper;
    
    /**
     * 分页获取所有评论，支持按照帖子ID和用户ID筛选
     */
    @GetMapping
    public HttpResponse getAllComments(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "postId", required = false) Integer postId,
            @RequestParam(value = "userId", required = false) Integer userId) {
        
        try {
            // 构建查询条件
            LambdaQueryWrapper<Comments> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Comments::getDelete, "0"); // 只查询未删除的评论
            
            // 按帖子ID筛选
            if (postId != null) {
                queryWrapper.eq(Comments::getPageId, postId);
            }
            
            // 按用户ID筛选
            if (userId != null) {
                queryWrapper.eq(Comments::getUserId, userId);
            }
            
            // 按创建时间降序排序
            queryWrapper.orderByDesc(Comments::getCreateTime);
            
            // 执行分页查询
            Page<Comments> commentsPage = commentsService.page(new Page<>(page, size), queryWrapper);
            
            // 获取用户信息和帖子信息，用于评论展示
            List<Comments> records = commentsPage.getRecords();
            
            if (records.isEmpty()) {
                // 如果没有记录，直接返回空页
                return HttpResponse.success(new Page<>());
            }
            
            // 收集所有涉及的用户ID
            List<Integer> userIds = records.stream()
                    .map(Comments::getUserId)
                    .collect(Collectors.toList());
            
            // 收集所有涉及的帖子ID
            List<Integer> postIds = records.stream()
                    .map(Comments::getPageId)
                    .collect(Collectors.toList());
            
            // 查询所有涉及的用户信息
            LambdaQueryWrapper<User> userQuery = new LambdaQueryWrapper<>();
            userQuery.in(User::getId, userIds);
            List<User> users = userMapper.selectList(userQuery);
            Map<Integer, User> userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, user -> user));
            
            // 查询所有涉及的帖子信息
            LambdaQueryWrapper<ForumPost> postQuery = new LambdaQueryWrapper<>();
            postQuery.in(ForumPost::getId, postIds);
            List<ForumPost> posts = forumPostService.list(postQuery);
            Map<Integer, ForumPost> postMap = posts.stream()
                    .collect(Collectors.toMap(ForumPost::getId, post -> post));
            
            // 构建返回结果
            List<Map<String, Object>> result = new ArrayList<>();
            for (Comments comment : records) {
                Map<String, Object> item = new HashMap<>();
                item.put("commentId", comment.getId());
                item.put("content", comment.getContent());
                item.put("createTime", comment.getCreateTime());
                item.put("thumbsUp", comment.getThumbsUp());
                
                // 添加用户信息
                User user = userMap.get(comment.getUserId());
                if (user != null) {
                    item.put("userId", user.getId());
                    item.put("username", user.getUsername());
                    item.put("userPic", user.getUserPic());
                }
                
                // 添加帖子信息
                ForumPost post = postMap.get(comment.getPageId());
                if (post != null) {
                    item.put("postId", post.getId());
                    item.put("postTitle", post.getTitle());
                }
                
                // 添加回复信息
                if (comment.getParent() != null && !comment.getParent().isEmpty()) {
                    item.put("isReply", true);
                    item.put("parentId", comment.getParent());
                } else {
                    item.put("isReply", false);
                }
                
                result.add(item);
            }
            
            // 构建返回的分页对象
            Page<Map<String, Object>> resultPage = new Page<>(commentsPage.getCurrent(), commentsPage.getSize(), commentsPage.getTotal());
            resultPage.setRecords(result);
            
            return HttpResponse.success(resultPage);
            
        } catch (Exception e) {
            log.error("获取评论列表失败", e);
            return HttpResponse.error("获取评论列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取特定评论详情
     */
    @GetMapping("/{id}")
    public HttpResponse getCommentDetail(@PathVariable Integer id) {
        Comments comment = commentsService.getById(id);
        if (comment == null || "1".equals(comment.getDelete())) {
            return HttpResponse.error("评论不存在或已删除");
        }
        
        // 获取评论相关的用户和帖子信息
        User user = userMapper.selectById(comment.getUserId());
        ForumPost post = forumPostService.getById(comment.getPageId());
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", comment.getId());
        result.put("content", comment.getContent());
        result.put("createTime", comment.getCreateTime());
        result.put("thumbsUp", comment.getThumbsUp());
        
        if (user != null) {
            result.put("userId", user.getId());
            result.put("username", user.getUsername());
            result.put("userPic", user.getUserPic());
        }
        
        if (post != null) {
            result.put("postId", post.getId());
            result.put("postTitle", post.getTitle());
        }
        
        if (comment.getParent() != null && !comment.getParent().isEmpty()) {
            result.put("isReply", true);
            result.put("parentId", comment.getParent());
            
            // 获取父评论信息
            Comments parentComment = commentsService.getById(comment.getParent());
            if (parentComment != null && "0".equals(parentComment.getDelete())) {
                User parentUser = userMapper.selectById(parentComment.getUserId());
                if (parentUser != null) {
                    result.put("parentUsername", parentUser.getUsername());
                }
                result.put("parentContent", parentComment.getContent());
            }
        } else {
            result.put("isReply", false);
        }
        
        return HttpResponse.success(result);
    }
    
    /**
     * 删除评论
     */
    @DeleteMapping("/{id}")
    public HttpResponse deleteComment(@PathVariable Integer id) {
        boolean result = commentsService.deleteComment(id);
        return result ? HttpResponse.success() : HttpResponse.error("删除失败，评论不存在");
    }
    
    /**
     * 批量删除评论
     */
    @DeleteMapping("/batch")
    public HttpResponse batchDeleteComments(@RequestBody List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return HttpResponse.error("请选择要删除的评论");
        }
        
        int success = 0;
        for (Integer id : ids) {
            if (commentsService.deleteComment(id)) {
                success++;
            }
        }
        
        if (success == ids.size()) {
            return HttpResponse.success("成功删除所有选中评论");
        } else {
            return HttpResponse.success(String.format("成功删除%d条评论，失败%d条", success, ids.size() - success));
        }
    }
    
    /**
     * 获取评论数量统计
     */
    @GetMapping("/stats")
    public HttpResponse getCommentStats() {
        try {
            // 统计总评论数
            LambdaQueryWrapper<Comments> totalQuery = new LambdaQueryWrapper<>();
            totalQuery.eq(Comments::getDelete, "0");
            long totalComments = commentsService.count(totalQuery);
            
            // 统计今日评论数
            LambdaQueryWrapper<Comments> todayQuery = new LambdaQueryWrapper<>();
            todayQuery.eq(Comments::getDelete, "0")
                    .ge(Comments::getCreateTime, java.time.LocalDate.now().atStartOfDay());
            long todayComments = commentsService.count(todayQuery);
            
            Map<String, Object> result = new HashMap<>();
            result.put("totalComments", totalComments);
            result.put("todayComments", todayComments);
            
            return HttpResponse.success(result);
        } catch (Exception e) {
            log.error("获取评论统计失败", e);
            return HttpResponse.error("获取评论统计失败: " + e.getMessage());
        }
    }
}
