package com.gumeng.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gumeng.domain.ForumPost;
import com.gumeng.entity.DTO.ForumPostDTO;
import com.gumeng.entity.vo.ForumPostVO;

/**
 * @author Lorn
 * @description 针对表【forum_post(帖子表)】的数据库操作Service
 * @createDate 2025-04-15 08:30:54
 */
public interface ForumPostService extends IService<ForumPost> {
    /**
     * 创建帖子
     */
    Integer createPost(ForumPostDTO forumPostDTO);

    /**
     * 获取帖子列表
     */
    Page<ForumPostVO> getPostList(Integer page, Integer size, String topic);

    /**
     * 获取帖子列表(带关键词搜索)
     */
    Page<ForumPostVO> getPostList(Integer page, Integer size, String topic, String keyword);

    /**
     * 获取帖子详情
     */
    ForumPostVO getPostDetail(Integer id);

    /**
     * 删除帖子
     */
    boolean deletePost(Integer id);

    /**
     * 点赞帖子
     */
    boolean likePost(Integer id);

    /**
     * 取消点赞帖子
     */
    boolean unlikePost(Integer id);
}