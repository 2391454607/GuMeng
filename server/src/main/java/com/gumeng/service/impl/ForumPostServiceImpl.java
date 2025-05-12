package com.gumeng.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gumeng.domain.ForumPost;
import com.gumeng.domain.user.User;
import com.gumeng.entity.DTO.ForumPostDTO;
import com.gumeng.entity.vo.ForumPostVO;
import com.gumeng.mapper.forum.ForumPostMapper;
import com.gumeng.mapper.user.UserMapper;
import com.gumeng.service.ForumPostService;
import com.gumeng.service.ThumbsUpService;
import com.gumeng.utils.ThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Lorn
 * @description 针对表【forum_post(帖子表)】的数据库操作Service实现
 * @createDate 2025-04-15 08:30:54
 */
@Service
@Slf4j
public class ForumPostServiceImpl extends ServiceImpl<ForumPostMapper, ForumPost>
        implements ForumPostService{

    @Autowired
    private ForumPostMapper forumPostMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ThumbsUpService thumbsUpService;

    @Override
    public Integer createPost(ForumPostDTO forumPostDTO) {
        // 获取当前用户ID
        Map<String, Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");

        // 创建帖子对象
        ForumPost post = new ForumPost();
        BeanUtils.copyProperties(forumPostDTO, post);

        // 设置用户ID和初始值
        post.setUserId(userId);
        post.setCommonNum(0);
        post.setThumbsUpNum(0);
        post.setViewCount(0);
        post.setCreateTime(LocalDateTime.now());
        post.setUpdateTime(LocalDateTime.now());
        post.setDeleted("0");

        // 保存帖子
        try {
            this.save(post);
            log.info("成功创建帖子，ID: {}, 标题: {}", post.getId(), post.getTitle());
            return post.getId();
        } catch (Exception e) {
            log.error("创建帖子失败: " + e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Page<ForumPostVO> getPostList(Integer page, Integer size, String topic) {
        // 调用带关键词的方法，传入null作为关键词
        return getPostList(page, size, topic, null);
    }

    @Override
    public Page<ForumPostVO> getPostList(Integer page, Integer size, String topic, String keyword) {
        log.info("获取帖子列表 - 页码: {}, 大小: {}, 话题: {}, 关键词: {}", page, size, topic, keyword);
        
        // 构建查询条件
        LambdaQueryWrapper<ForumPost> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ForumPost::getDeleted, "0");

        // 如果指定了话题，则按话题筛选
        if (StrUtil.isNotBlank(topic)) {
            queryWrapper.eq(ForumPost::getTopic, topic);
        }

        // 如果指定了关键词，则进行模糊搜索（标题或内容包含关键词）
        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                    .like(ForumPost::getTitle, keyword)
                    .or()
                    .like(ForumPost::getContent, keyword));
        }

        // 按时间倒序排序
        queryWrapper.orderByDesc(ForumPost::getCreateTime);

        // 分页查询
        Page<ForumPost> postPage = new Page<>(page, size);
        Page<ForumPost> result = this.page(postPage, queryWrapper);

        // 转换为VO对象
        return convertToPostVOPage(result);
    }
    
    /**
     * 将ForumPost分页结果转换为ForumPostVO分页结果
     */
    private Page<ForumPostVO> convertToPostVOPage(Page<ForumPost> postPage) {
        // 创建VO分页对象
        Page<ForumPostVO> voPage = new Page<>();
        BeanUtils.copyProperties(postPage, voPage, "records");

        // 获取用户ID列表
        List<Integer> userIds = postPage.getRecords().stream()
                .map(ForumPost::getUserId)
                .collect(Collectors.toList());

        // 查询用户信息
        LambdaQueryWrapper<User> userQuery = new LambdaQueryWrapper<>();
        userQuery.in(userIds != null && !userIds.isEmpty(), User::getId, userIds);
        List<User> users = userMapper.selectList(userQuery);

        // 用户ID到用户信息的映射
        Map<Integer, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, user -> user, (k1, k2) -> k1));

        // 获取当前用户ID
        Map<String, Object> threadLocalMap = ThreadLocalUtil.get();
        Long currentUserId = null;
        if (threadLocalMap != null) {
            Integer userId = (Integer) threadLocalMap.get("id");
            if (userId != null) {
                currentUserId = userId.longValue();
            }
        }

        // 填充VO对象列表
        final Long finalCurrentUserId = currentUserId;
        List<ForumPostVO> voList = postPage.getRecords().stream().map(post -> {
            ForumPostVO vo = new ForumPostVO();
            BeanUtils.copyProperties(post, vo);

            // 填充用户信息
            User user = userMap.get(post.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
                vo.setAvatar(user.getUserPic());
            }

            // 判断当前用户是否点赞这个帖子
            if (finalCurrentUserId != null) {
                vo.setIsLiked(thumbsUpService.checkUserLiked(finalCurrentUserId, "post", post.getId()));
            } else {
                vo.setIsLiked(false);
            }

            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public ForumPostVO getPostDetail(Integer id) {
        // 查询帖子
        ForumPost post = this.getById(id);
        if (post == null || "1".equals(post.getDeleted())) {
            return null;
        }

        try {
            // 增加浏览次数
            forumPostMapper.incrementViewCount(id);
            
            // 更新本地对象的浏览量，避免返回旧数据
            post.setViewCount(post.getViewCount() == null ? 1 : post.getViewCount() + 1);
        } catch (Exception e) {
            log.error("增加浏览量失败", e);
            // 继续执行，不影响获取帖子详情
        }

        // 查询发帖用户
        User user = userMapper.selectById(post.getUserId());

        // 构建VO对象
        ForumPostVO vo = new ForumPostVO();
        BeanUtils.copyProperties(post, vo);

        // 添加用户信息
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setAvatar(user.getUserPic());
        }

        // 判断当前用户是否点赞这个帖子
        Map<String, Object> threadLocalMap = ThreadLocalUtil.get();
        if (threadLocalMap != null) {
            Integer userId = (Integer) threadLocalMap.get("id");
            if (userId != null) {
                Long currentUserId = userId.longValue();
                vo.setIsLiked(thumbsUpService.checkUserLiked(currentUserId, "post", post.getId()));
            } else {
                vo.setIsLiked(false);
            }
        } else {
            vo.setIsLiked(false);
        }

        return vo;
    }

    @Override
    public boolean deletePost(Integer id) {
        // 获取当前用户ID
        Map<String, Object> map = ThreadLocalUtil.get();
        Integer currentUserId = (Integer) map.get("id");

        // 查询帖子
        ForumPost post = this.getById(id);
        if (post == null || "1".equals(post.getDeleted())) {
            return false;
        }

        // 验证用户是否是帖子的发布者
        if (!post.getUserId().equals(currentUserId)) {
            return false;
        }

        // 逻辑删除
        post.setDeleted("1");
        return this.updateById(post);
    }

    @Override
    @Transactional
    public boolean likePost(Integer id) {
        try {
            // 获取当前用户ID
            Map<String, Object> map = ThreadLocalUtil.get();
            if (map == null || map.get("id") == null) {
                return false;
            }
            
            Integer userId = (Integer) map.get("id");
            Long userIdLong = userId.longValue();

            // 查询帖子
            ForumPost post = this.getById(id);
            if (post == null || "1".equals(post.getDeleted())) {
                return false;
            }

            // 调用点赞服务
            boolean result = thumbsUpService.addThumbsUp(userIdLong, "post", id);
            
            if (result) {
                // 增加帖子点赞数
                forumPostMapper.incrementLikeCount(id);
            }
            
            return result;
        } catch (Exception e) {
            log.error("点赞帖子失败: " + e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean unlikePost(Integer id) {
        try {
            // 获取当前用户ID
            Map<String, Object> map = ThreadLocalUtil.get();
            if (map == null || map.get("id") == null) {
                return false;
            }
            
            Integer userId = (Integer) map.get("id");
            Long userIdLong = userId.longValue();

            // 查询帖子
            ForumPost post = this.getById(id);
            if (post == null || "1".equals(post.getDeleted())) {
                return false;
            }

            // 调用取消点赞服务
            boolean result = thumbsUpService.removeThumbsUp(userIdLong, "post", id);
            
            if (result) {
                // 减少帖子点赞数
                forumPostMapper.decrementLikeCount(id);
            }
            
            return result;
        } catch (Exception e) {
            log.error("取消点赞帖子失败: " + e.getMessage(), e);
            return false;
        }
    }
}