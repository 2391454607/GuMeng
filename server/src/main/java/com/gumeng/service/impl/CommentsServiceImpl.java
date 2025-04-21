package com.gumeng.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gumeng.domain.Comments;
import com.gumeng.domain.User;
import com.gumeng.entity.DTO.CommentDTO;
import com.gumeng.entity.vo.CommentVO;
import com.gumeng.mapper.CommentsMapper;
import com.gumeng.mapper.ForumPostMapper;
import com.gumeng.mapper.UserMapper;
import com.gumeng.service.CommentsService;
import com.gumeng.service.ThumbsUpService;
import com.gumeng.utils.ThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Lorn
 * @description 针对表【comments(帖子评论表)】的数据库操作Service实现
 * @createDate 2025-04-15 08:38:21
 */
@Service
@Slf4j
public class CommentsServiceImpl extends ServiceImpl<CommentsMapper, Comments>
        implements CommentsService{

    @Autowired
    private CommentsMapper commentsMapper;

    @Autowired
    private ForumPostMapper forumPostMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ThumbsUpService thumbsUpService;

    @Override
    @Transactional
    public Integer addComment(CommentDTO commentDTO) {
        // 获取当前用户ID
        Map<String, Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");

        // 创建评论对象
        Comments comment = new Comments();
        comment.setUserId(userId);
        comment.setPageId(commentDTO.getPageId());
        comment.setParent(commentDTO.getParent());
        comment.setContent(commentDTO.getContent());
        comment.setForUser(commentDTO.getForUser());
        comment.setThumbsUp("0");
        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());
        comment.setDelete("0");

        // 保存评论
        this.save(comment);

        // 更新帖子评论数
        forumPostMapper.incrementCommentCount(commentDTO.getPageId());

        return comment.getId();
    }

    @Override
    public List<CommentVO> getCommentsByPostId(Integer postId) {
        // 获取当前用户ID
        Map<String, Object> map = ThreadLocalUtil.get();
        Long currentUserId = null;
        if (map != null && map.get("id") != null) {
            currentUserId = ((Integer) map.get("id")).longValue();
        }

        // 获取根评论
        List<Comments> rootComments = commentsMapper.getRootCommentsByPageId(postId);
        if (rootComments == null || rootComments.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取所有评论用户ID
        List<Integer> userIds = rootComments.stream()
                .map(Comments::getUserId)
                .collect(Collectors.toList());

        // 查询回复的评论
        LambdaQueryWrapper<Comments> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comments::getPageId, postId)
                .isNotNull(Comments::getParent)
                .ne(Comments::getParent, "")
                .eq(Comments::getDelete, "0");
        List<Comments> childComments = this.list(queryWrapper);

        // 添加子评论的用户ID
        userIds.addAll(childComments.stream()
                .map(Comments::getUserId)
                .collect(Collectors.toList()));
                
        // 添加被回复用户的ID
        List<String> forUserIds = childComments.stream()
                .filter(c -> c.getForUser() != null && !c.getForUser().isEmpty())
                .map(Comments::getForUser)
                .collect(Collectors.toList());
                
        for (String forUserId : forUserIds) {
            try {
                userIds.add(Integer.parseInt(forUserId));
            } catch (NumberFormatException e) {
                // 忽略转换异常
            }
        }

        // 查询所有涉及到的用户
        LambdaQueryWrapper<User> userQueryWrapper = new LambdaQueryWrapper<>();
        userQueryWrapper.in(User::getId, userIds);
        List<User> users = userMapper.selectList(userQueryWrapper);
        Map<Integer, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // 构建评论树
        Map<String, List<Comments>> childrenMap = childComments.stream()
                .collect(Collectors.groupingBy(Comments::getParent));

        // 将根评论转换为VO
        final Long finalCurrentUserId = currentUserId;
        List<CommentVO> result = rootComments.stream()
                .map(comment -> convertToVO(comment, childrenMap, userMap, finalCurrentUserId))
                .collect(Collectors.toList());

        return result;
    }

    /**
     * 将评论实体转换为VO
     */
    private CommentVO convertToVO(Comments comment, Map<String, List<Comments>> childrenMap,
                                  Map<Integer, User> userMap, Long currentUserId) {
        CommentVO vo = new CommentVO();
        BeanUtils.copyProperties(comment, vo);

        // 设置父评论ID
        vo.setParentId(comment.getParent());
        
        // 转换点赞数为整数
        try {
            vo.setThumbsUp(Integer.parseInt(comment.getThumbsUp()));
        } catch (NumberFormatException e) {
            vo.setThumbsUp(0);
        }

        // 设置发表评论用户信息
        User user = userMap.get(comment.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setUserPic(user.getUserPic());
        }

        // 设置回复用户信息
        if (comment.getForUser() != null && !comment.getForUser().isEmpty()) {
            try {
                Integer forUserId = Integer.parseInt(comment.getForUser());
                User forUser = userMap.get(forUserId);
                if (forUser != null) {
                    vo.setForUserId(comment.getForUser());
                    vo.setForUsername(forUser.getUsername());
                }
            } catch (NumberFormatException e) {
                // 忽略转换异常
            }
        }

        // 检查当前用户是否点赞
        if (currentUserId != null) {
            vo.setIsLiked(thumbsUpService.checkUserLiked(currentUserId, "comment", comment.getId()));
        } else {
            vo.setIsLiked(false);
        }

        // 处理子评论
        List<Comments> children = childrenMap.get(comment.getId().toString());
        if (children != null && !children.isEmpty()) {
            List<CommentVO> childrenVOs = children.stream()
                    .map(child -> convertToVO(child, childrenMap, userMap, currentUserId))
                    .collect(Collectors.toList());
            vo.setChildren(childrenVOs);
        } else {
            vo.setChildren(new ArrayList<>());
        }

        return vo;
    }

    @Override
    @Transactional
    public boolean deleteComment(Integer commentId) {
        // 获取当前用户ID
        Map<String, Object> map = ThreadLocalUtil.get();
        Integer currentUserId = (Integer) map.get("id");

        // 查询评论
        Comments comment = this.getById(commentId);
        if (comment == null || "1".equals(comment.getDelete())) {
            return false;
        }

        // 验证用户是否是评论的发表者
        if (!comment.getUserId().equals(currentUserId)) {
            return false;
        }

        // 获取帖子ID
        Integer postId = comment.getPageId();

        // 逻辑删除评论
        comment.setDelete("1");
        boolean updated = this.updateById(comment);

        // 减少帖子评论数
        if (updated && postId != null) {
            forumPostMapper.decrementCommentCount(postId);
        }

        // 查找所有子评论并删除
        LambdaQueryWrapper<Comments> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comments::getParent, commentId.toString())
                .eq(Comments::getDelete, "0");
        List<Comments> children = this.list(queryWrapper);

        if (!children.isEmpty()) {
            for (Comments child : children) {
                child.setDelete("1");
                this.updateById(child);
                
                // 为每个删除的子评论减少帖子评论数
                if (postId != null) {
                    forumPostMapper.decrementCommentCount(postId);
                }
            }
        }

        return updated;
    }

    @Override
    @Transactional
    public boolean likeComment(Integer commentId) {
        try {
            // 获取当前用户ID
            Map<String, Object> map = ThreadLocalUtil.get();
            if (map == null || map.get("id") == null) {
                return false;
            }
            
            Integer userId = (Integer) map.get("id");
            Long userIdLong = userId.longValue();
            
            // 获取评论信息，验证其是否存在
            Comments comment = this.getById(commentId);
            if (comment == null || "1".equals(comment.getDelete())) {
                return false;
            }

            // 调用点赞服务并增加评论点赞数
            if (thumbsUpService.addThumbsUp(userIdLong, "comment", commentId)) {
                // 增加评论点赞数
                commentsMapper.incrementLikeCount(commentId);
                return true;
            }
            
            return false;
        } catch (Exception e) {
            log.error("点赞评论失败: " + e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean unlikeComment(Integer commentId) {
        try {
            // 获取当前用户ID
            Map<String, Object> map = ThreadLocalUtil.get();
            if (map == null || map.get("id") == null) {
                return false;
            }
            
            Integer userId = (Integer) map.get("id");
            Long userIdLong = userId.longValue();
            
            // 获取评论信息，验证其是否存在
            Comments comment = this.getById(commentId);
            if (comment == null || "1".equals(comment.getDelete())) {
                return false;
            }
            
            // 确保用户已点赞
            if (!thumbsUpService.checkUserLiked(userIdLong, "comment", commentId)) {
                return true; // 用户没有点赞，直接返回成功
            }

            // 调用取消点赞服务并减少评论点赞数
            if (thumbsUpService.removeThumbsUp(userIdLong, "comment", commentId)) {
                commentsMapper.decrementLikeCount(commentId);
                return true;
            }
            
            return false;
        } catch (Exception e) {
            log.error("取消点赞评论失败: " + e.getMessage(), e);
            return false;
        }
    }
}