package com.gumeng.controller.sys;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gumeng.code.HttpResponse;
import com.gumeng.entity.vo.ForumPostVO;
import com.gumeng.domain.forum.ForumPost;
import com.gumeng.domain.forum.ForumPostType;
import com.gumeng.service.ForumPostService;
import com.gumeng.service.ForumTopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author:Lorn(黎龙)
 * @Project：GuMeng
 * @Date：2025/5/13
 * @Time:8:20
 */
@RestController
@RequestMapping("/sys/forum")
public class AdminForumController {
    
    @Autowired
    private ForumPostService forumPostService;
    
    @Autowired
    private ForumTopicService forumTopicService;
    
    /**
     * 获取所有论坛帖子，支持分页查询、话题筛选和关键词搜索
     */
    @GetMapping("/posts")
    public HttpResponse getForumPosts(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "topic", required = false) String topic,
            @RequestParam(value = "keyword", required = false) String keyword) {
        
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
     * 删除帖子
     */
    @DeleteMapping("/posts/{id}")
    public HttpResponse deletePost(@PathVariable Integer id) {
        // 管理员删除帖子直接通过帖子ID删除
        boolean result = forumPostService.deletePost(id);
        return result ? HttpResponse.success() : HttpResponse.error("删除失败，帖子不存在");
    }
    
    /**
     * 获取所有话题类型
     */
    @GetMapping("/topics")
    public HttpResponse getAllTopics() {
        List<ForumPostType> topics = forumTopicService.getTopicList();
        return HttpResponse.success(topics);
    }
    
    /**
     * 添加话题类型
     */
    @PostMapping("/topics")
    public HttpResponse addTopic(@RequestBody ForumPostType topic) {
        // 确保新话题的创建时间和未删除状态
        topic.setCreateTime(java.time.LocalDateTime.now());
        topic.setUpdateTime(java.time.LocalDateTime.now());
        topic.setDeleted(false);
        
        boolean result = forumTopicService.save(topic);
        return result ? HttpResponse.success(topic.getId()) : HttpResponse.error("添加话题失败");
    }
    
    /**
     * 删除话题类型
     */
    @DeleteMapping("/topics/{id}")
    public HttpResponse deleteTopic(@PathVariable Long id) {
        // 获取话题
        ForumPostType topic = forumTopicService.getById(id);
        if (topic == null) {
            return HttpResponse.error("话题不存在");
        }
        
        // 设置为删除状态
        topic.setDeleted(true);
        topic.setUpdateTime(java.time.LocalDateTime.now());
        
        boolean result = forumTopicService.updateById(topic);
        return result ? HttpResponse.success() : HttpResponse.error("删除话题失败");
    }
}
