package com.gumeng.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gumeng.domain.ForumPostType;
import com.gumeng.mapper.forum.ForumPostTypeMapper;
import com.gumeng.service.ForumTopicService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 帖子话题服务实现类
 */
@Service
@Slf4j
public class ForumTopicServiceImpl extends ServiceImpl<ForumPostTypeMapper, ForumPostType>
        implements ForumTopicService {

    @Autowired
    private ForumPostTypeMapper forumPostTypeMapper;

    @Override
    public List<ForumPostType> getTopicList() {
        LambdaQueryWrapper<ForumPostType> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ForumPostType::getDeleted, false);
        queryWrapper.orderByAsc(ForumPostType::getId);
        return list(queryWrapper);
    }
} 