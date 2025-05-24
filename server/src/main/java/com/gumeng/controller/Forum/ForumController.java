package com.gumeng.controller.Forum;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gumeng.annotation.LogOperation;
import com.gumeng.code.HttpResponse;
import com.gumeng.domain.forum.ForumPost;
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

import java.time.LocalDateTime;
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
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "selfOnly", required = false) Boolean selfOnly) {
        
        // 记录用户信息和请求
        Map<String, Object> userInfo = ThreadLocalUtil.get();
        log.info("获取帖子列表 - 用户信息: {}, 页码: {}, 大小: {}, 话题: {}, 关键词: {}, 仅自己: {}", 
                 userInfo, page, size, topic, keyword, selfOnly);
        
        // 获取当前用户ID
        Integer currentUserId = null;
        if (userInfo != null && userInfo.get("id") != null) {
            currentUserId = (Integer) userInfo.get("id");
        }
        
        // 如果请求只查看自己的帖子，但用户未登录，返回空列表
        if (Boolean.TRUE.equals(selfOnly) && currentUserId == null) {
            return HttpResponse.error("请先登录");
        }
        
        Page<ForumPostVO> posts;
        try {
            // 如果selfOnly为true，则只查询当前用户的帖子
            if (Boolean.TRUE.equals(selfOnly) && currentUserId != null) {
                // 添加用户ID作为查询条件
                posts = forumPostService.getUserPosts(page, size, topic, keyword, currentUserId);
            } else if (keyword != null && !keyword.isEmpty()) {
                posts = forumPostService.getPostList(page, size, topic, keyword);
            } else {
                posts = forumPostService.getPostList(page, size, topic);
            }
            
            return HttpResponse.success(posts);
        } catch (Exception e) {
            log.error("获取帖子列表失败", e);
            return HttpResponse.error("获取帖子列表失败，请稍后重试");
        }
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
        try {
            // 第一步：传统敏感词检测和AI上下文审核
            ContentAuditService.AuditResult titleAudit = contentAuditService.auditContent(postDTO.getTitle());
            ContentAuditService.AuditResult contentAudit = contentAuditService.auditContent(postDTO.getContent());
            
            // 第二步：对全文进行AI审核（可以捕获不在敏感词库中的不良内容）
            boolean titleAiFullCheck = contentAuditService.checkFullContentWithAI(postDTO.getTitle());
            boolean contentAiFullCheck = contentAuditService.checkFullContentWithAI(postDTO.getContent());
            
            // 检查是否有任何不通过的情况
            if ((!titleAudit.isPassed() || !contentAudit.isPassed()) || 
                (!titleAiFullCheck || !contentAiFullCheck)) {
                
                Map<String, Object> result = new HashMap<>();
                // 内部记录敏感词和审核状态，但对用户隐藏具体原因
                result.put("titleAudit", titleAudit.isPassed());
                result.put("contentAudit", contentAudit.isPassed());
                result.put("titleAiFullCheck", titleAiFullCheck);
                result.put("contentAiFullCheck", contentAiFullCheck);
                
                // 统一错误提示
                result.put("message", "内容审核不通过，请修改后重试");
                
                return HttpResponse.error("内容审核不通过，请修改后重试").setData(result);
            }
            
            // 使用过滤后的内容
            postDTO.setTitle(titleAudit.getFilteredContent());
            postDTO.setContent(contentAudit.getFilteredContent());
            
            Integer postId = forumPostService.createPost(postDTO);
            
            return HttpResponse.success(postId);
        } catch (Exception e) {
            log.error("创建帖子时审核内容出错: {}", e.getMessage());
            return HttpResponse.error("内容审核服务异常，请稍后重试");
        }
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
    @LogOperation(module = "论坛", operation = "内容合规检测")
    @PostMapping("/checkSensitiveWords")
    public HttpResponse checkSensitiveWords(@RequestBody Map<String, String> params) {
        String text = params.get("text");
        
        if (text == null || text.isEmpty()) {
            return HttpResponse.error("文本内容不能为空");
        }
        
        try {
            // 第一步：使用传统敏感词过滤 + AI上下文审核
            ContentAuditService.AuditResult auditResult = contentAuditService.auditContent(text);
            
            // 第二步：使用AI进行全文审核，可以捕获不在敏感词表中的不良内容
            boolean fullTextAiApproved = contentAuditService.checkFullContentWithAI(text);
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            
            // 仅返回审核总体结果，不暴露具体机制
            boolean finalApproved = auditResult.isPassed() && fullTextAiApproved;
            result.put("approved", finalApproved);
            
            // 添加提示信息
            if (!finalApproved) {
                result.put("message", "内容审核不通过，请修改后重试");
            }
            
            return HttpResponse.success(result);
        } catch (Exception e) {
            log.error("内容检测过程出错: {}", e.getMessage());
            
            return HttpResponse.error("内容审核服务异常，请稍后重试");
        }
    }

    /**
     * 使用AI审核全文内容（不仅限于敏感词）
     */
    @LogOperation(module = "论坛", operation = "内容合规检查")
    @PostMapping("/aiContentCheck")
    public HttpResponse aiContentCheck(@RequestBody Map<String, String> params) {
        String text = params.get("text");
        
        if (text == null || text.isEmpty()) {
            return HttpResponse.error("文本内容不能为空");
        }
        
        try {
            // 调用AI进行全文审核
            boolean aiApproved = contentAuditService.checkFullContentWithAI(text);
            
            Map<String, Object> result = new HashMap<>();
            result.put("approved", aiApproved);
            
            if (!aiApproved) {
                result.put("message", "内容审核不通过，请修改后重试");
            }
            
            return HttpResponse.success(result);
        } catch (Exception e) {
            log.error("全文内容审核出错: {}", e.getMessage());
            return HttpResponse.error("内容审核服务异常，请稍后重试");
        }
    }

    /**
     * 更新帖子
     */
    @LogOperation(module = "论坛", operation = "更新帖子")
    @PostMapping("/posts/{id}/update")
    public HttpResponse updatePost(@PathVariable Integer id, @RequestBody @Valid ForumPostDTO postDTO) {
        try {
            // 确保当前用户是该帖子的作者
            Map<String, Object> userInfo = ThreadLocalUtil.get();
            Integer currentUserId = (Integer) userInfo.get("id");
            
            // 查询帖子
            ForumPost post = forumPostService.getById(id);
            if (post == null || "1".equals(post.getDeleted())) {
                return HttpResponse.error("帖子不存在或已删除");
            }
            
            // 验证用户是否是帖子的发布者
            if (!post.getUserId().equals(currentUserId)) {
                return HttpResponse.error("您没有权限修改此帖子");
            }

            // 敏感词和AI审核
            ContentAuditService.AuditResult titleAudit = contentAuditService.auditContent(postDTO.getTitle());
            ContentAuditService.AuditResult contentAudit = contentAuditService.auditContent(postDTO.getContent());
            
            boolean titleAiFullCheck = contentAuditService.checkFullContentWithAI(postDTO.getTitle());
            boolean contentAiFullCheck = contentAuditService.checkFullContentWithAI(postDTO.getContent());
            
            // 检查是否有任何不通过的情况
            if ((!titleAudit.isPassed() || !contentAudit.isPassed()) || 
                (!titleAiFullCheck || !contentAiFullCheck)) {
                
                Map<String, Object> result = new HashMap<>();
                // 内部记录详情，对用户隐藏
                result.put("titleAudit", titleAudit.isPassed());
                result.put("contentAudit", contentAudit.isPassed());
                result.put("titleAiFullCheck", titleAiFullCheck);
                result.put("contentAiFullCheck", contentAiFullCheck);
                
                return HttpResponse.error("内容审核不通过，请修改后重试").setData(result);
            }
            
            // 更新帖子
            post.setTitle(titleAudit.getFilteredContent());
            post.setContent(contentAudit.getFilteredContent());
            post.setTopic(postDTO.getTopic());
            post.setImages(postDTO.getImages());
            post.setUpdateTime(LocalDateTime.now());
            
            boolean result = forumPostService.updateById(post);
            
            if (result) {
                return HttpResponse.success(post.getId());
            } else {
                return HttpResponse.error("更新帖子失败");
            }
        } catch (Exception e) {
            log.error("更新帖子时出错: {}", e.getMessage(), e);
            return HttpResponse.error("更新帖子异常，请稍后重试");
        }
    }
}