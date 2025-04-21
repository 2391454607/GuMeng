package com.gumeng.controller.Forum;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gumeng.code.HttpResponse;
import com.gumeng.entity.DTO.ForumPostDTO;
import com.gumeng.entity.vo.ForumPostVO;
import com.gumeng.service.ForumPostService;
import com.gumeng.service.ForumTopicService;
import com.gumeng.utils.ThreadLocalUtil;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    @PostMapping("/posts")
    public HttpResponse createPost(@RequestBody @Valid ForumPostDTO postDTO) {
        Integer postId = forumPostService.createPost(postDTO);
        return HttpResponse.success(postId);
    }

    /**
     * 删除帖子
     */
    @DeleteMapping("/posts/{id}")
    public HttpResponse deletePost(@PathVariable Integer id) {
        boolean result = forumPostService.deletePost(id);
        return result ? HttpResponse.success() : HttpResponse.error("删除失败，帖子不存在或您没有权限");
    }

    /**
     * 点赞帖子
     */
    @PostMapping("/posts/{id}/like")
    public HttpResponse likePost(@PathVariable Integer id) {
        boolean result = forumPostService.likePost(id);
        return result ? HttpResponse.success() : HttpResponse.error("点赞失败");
    }

    /**
     * 取消点赞帖子
     */
    @PostMapping("/posts/{id}/unlike")
    public HttpResponse unlikePost(@PathVariable Integer id) {
        boolean result = forumPostService.unlikePost(id);
        return result ? HttpResponse.success() : HttpResponse.error("取消点赞失败");
    }
    
    /**
     * 获取话题列表
     */
    @GetMapping("/getTopics")
    public HttpResponse getTopics() {
        return HttpResponse.success(forumTopicService.getTopicList());
    }
}