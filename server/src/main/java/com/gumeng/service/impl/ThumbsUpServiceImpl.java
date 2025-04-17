package com.gumeng.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gumeng.domain.ThumbsUp;
import com.gumeng.mapper.ThumbsUpMapper;
import com.gumeng.service.ThumbsUpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * @author Lorn
 * @description 针对表【thumbs_up(点赞表)】的数据库操作Service实现
 * @createDate 2025-04-15 08:50:33
 */

/**
 * 点赞服务实现类
 */
@Service
public class ThumbsUpServiceImpl extends ServiceImpl<ThumbsUpMapper, ThumbsUp>
        implements ThumbsUpService {

    @Autowired
    private ThumbsUpMapper thumbsUpMapper;

    @Override
    public boolean addThumbsUp(Long userId, String forType, Integer forUid) {
        // 检查是否已经点赞
        if (checkUserLiked(userId, forType, forUid)) {
            return false;
        }

        // 创建点赞记录
        ThumbsUp thumbsUp = new ThumbsUp();
        thumbsUp.setUserId(userId);
        thumbsUp.setForType(forType);
        thumbsUp.setForUid(forUid);
        thumbsUp.setDateTime(LocalDateTime.now());
        thumbsUp.setDeleted("0");

        return this.save(thumbsUp);
    }

    @Override
    public boolean removeThumbsUp(Long userId, String forType, Integer forUid) {
        LambdaQueryWrapper<ThumbsUp> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ThumbsUp::getUserId, userId)
                .eq(ThumbsUp::getForType, forType)
                .eq(ThumbsUp::getForUid, forUid)
                .eq(ThumbsUp::getDeleted, "0");

        // 查找点赞记录
        ThumbsUp thumbsUp = this.getOne(queryWrapper);
        if (thumbsUp == null) {
            return false;
        }

        // 逻辑删除
        thumbsUp.setDeleted("1");
        return this.updateById(thumbsUp);
    }

    @Override
    public boolean checkUserLiked(Long userId, String forType, Integer forUid) {
        // 调用Mapper中的方法检查
        int count = thumbsUpMapper.checkUserLiked(userId, forType, forUid);
        return count > 0;
    }
}




