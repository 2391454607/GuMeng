package com.gumeng.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gumeng.domain.ForumPostType;

import java.util.List;

/**
 * 帖子话题服务接口
 */
public interface ForumTopicService extends IService<ForumPostType> {
    
    /**
     * 获取话题列表
     */
    List<ForumPostType> getTopicList();
} 