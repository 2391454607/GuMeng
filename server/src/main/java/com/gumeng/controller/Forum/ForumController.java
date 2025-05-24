package com.gumeng.controller.Forum;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gumeng.annotation.LogOperation;
import com.gumeng.code.HttpResponse;
import com.gumeng.entity.DTO.ForumPostDTO;
import com.gumeng.entity.vo.ForumPostVO;
import com.gumeng.service.ContentAuditService;
import com.gumeng.service.ForumPostService;
import com.gumeng.service.ForumTopicService;
import com.gumeng.utils.SensitiveWordFilter;
import com.gumeng.utils.ThreadLocalUtil;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 非遗论坛控制器
 * @Author:Lorn(黎龙)
 * @Project：GuMeng
 * @Date：2025/4/15
 * @Time:8:37
 */
@RestController
@RequestMapping("/forum")
@Slf4j
public class ForumController {

    @Autowired
    private ForumPostService forumPostService;
    
    @Autowired
    private ForumTopicService forumTopicService;
    
    @Autowired
    private ContentAuditService contentAuditService;

    /**
     * 调试端点 - 用于验证权限和Token
     */
    @GetMapping("/debug/token")
    public HttpResponse debugToken() {
        Map<String, Object> userInfo = ThreadLocalUtil.get();
        log.info("调试端点访问 - 用户信息: {}", userInfo);
        
        if (userInfo == null || userInfo.isEmpty()) {
            return HttpResponse.error("未检测到用户信息，请检查Token");
        }
        
        return HttpResponse.success(userInfo);
    }

    /**
     * 获取帖子列表
     */
    @LogOperation(module = "论坛", operation = "获取帖子列表")
    @GetMapping("/getPosts")
    public HttpResponse getPosts(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "topic", required = false) String topic,
            @RequestParam(value = "keyword", required = false) String keyword) {
        
        // 记录用户信息和请求
        Map<String, Object> userInfo = ThreadLocalUtil.get();
        log.info("获取帖子列表 - 用户信息: {}, 页码: {}, 大小: {}, 话题: {}, 关键词: {}", 
                 userInfo, page, size, topic, keyword);
        
        Page<ForumPostVO> posts;
        if (keyword != null && !keyword.isEmpty()) {
            posts = forumPostService.getPostList(page, size, topic, keyword);
        } else {
            posts = forumPostService.getPostList(page, size, topic);
        }
        
        return HttpResponse.success(posts);
    }

    /**
     * 获取帖子详情
     */
    @LogOperation(module = "论坛", operation = "查看帖子详情")
    @GetMapping("/posts/{id}")
    public HttpResponse getPostDetail(@PathVariable Integer id) {
        ForumPostVO post = forumPostService.getPostDetail(id);
        if (post == null) {
            return HttpResponse.error("帖子不存在或已删除");
        }
        return HttpResponse.success(post);
    }

    /**
     * 创建帖子
     */
    @LogOperation(module = "论坛", operation = "创建新帖子")
    @PostMapping("/posts")
    public HttpResponse createPost(@RequestBody @Valid ForumPostDTO postDTO) {
        // 内容审核
        ContentAuditService.AuditResult titleAudit = contentAuditService.auditContent(postDTO.getTitle());
        ContentAuditService.AuditResult contentAudit = contentAuditService.auditContent(postDTO.getContent());
        
        // 如果标题或内容中有敏感词且AI判断不合规，则拒绝发布
        if ((!titleAudit.getSensitiveWords().isEmpty() && !titleAudit.isPassed()) || 
            (!contentAudit.getSensitiveWords().isEmpty() && !contentAudit.isPassed())) {
            Map<String, Object> result = new HashMap<>();
            result.put("sensitiveWordsInTitle", titleAudit.getSensitiveWords());
            result.put("sensitiveWordsInContent", contentAudit.getSensitiveWords());
            result.put("message", "内容包含敏感词且上下文不合规，请修改后重试");
            return HttpResponse.error("内容包含敏感词，请修改后重试").setData(result);
        }
        
        // 使用过滤后的内容（如果AI判断不合规）
        postDTO.setTitle(titleAudit.getFilteredContent());
        postDTO.setContent(contentAudit.getFilteredContent());
        
        Integer postId = forumPostService.createPost(postDTO);
        
        // 如果存在敏感词，但AI判断无害，告知用户
        if (!titleAudit.getSensitiveWords().isEmpty() || !contentAudit.getSensitiveWords().isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("postId", postId);
            result.put("message", "内容中包含敏感词，但经AI判断上下文合规，已允许发布");
            return HttpResponse.success(result);
        }
        
        return HttpResponse.success(postId);
    }

    /**
     * 删除帖子
     */
    @LogOperation(module = "论坛", operation = "删除帖子")
    @DeleteMapping("/posts/{id}")
    public HttpResponse deletePost(@PathVariable Integer id) {
        boolean result = forumPostService.deletePost(id);
        return result ? HttpResponse.success() : HttpResponse.error("删除失败，帖子不存在或您没有权限");
    }

    /**
     * 点赞帖子
     */
    @LogOperation(module = "论坛", operation = "点赞帖子")
    @PostMapping("/posts/{id}/like")
    public HttpResponse likePost(@PathVariable Integer id) {
        boolean result = forumPostService.likePost(id);
        return result ? HttpResponse.success() : HttpResponse.error("点赞失败");
    }

    /**
     * 取消点赞帖子
     */
    @LogOperation(module = "论坛", operation = "取消点赞")
    @PostMapping("/posts/{id}/unlike")
    public HttpResponse unlikePost(@PathVariable Integer id) {
        boolean result = forumPostService.unlikePost(id);
        return result ? HttpResponse.success() : HttpResponse.error("取消点赞失败");
    }
    
    /**
     * 获取话题列表
     */
    @LogOperation(module = "论坛", operation = "获取话题列表")
    @GetMapping("/getTopics")
    public HttpResponse getTopics() {
        return HttpResponse.success(forumTopicService.getTopicList());
    }

    /**
     * 检测文本是否包含敏感词（智能检测）
     */
    @LogOperation(module = "论坛", operation = "敏感词智能检测")
    @PostMapping("/checkSensitiveWords")
    public HttpResponse checkSensitiveWords(@RequestBody Map<String, String> params) {
        String text = params.get("text");
        
        if (text == null || text.isEmpty()) {
            return HttpResponse.error("文本内容不能为空");
        }
        
        // 使用智能内容审核服务
        ContentAuditService.AuditResult auditResult = contentAuditService.auditContent(text);
        
        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("containsSensitiveWords", !auditResult.getSensitiveWords().isEmpty());
        result.put("sensitiveWords", auditResult.getSensitiveWords());
        result.put("aiApproved", auditResult.isPassed());
        
        return HttpResponse.success(result);
    }
}