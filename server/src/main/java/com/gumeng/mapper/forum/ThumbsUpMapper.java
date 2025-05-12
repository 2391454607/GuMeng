package com.gumeng.mapper.forum;

import com.gumeng.domain.forum.ThumbsUp;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
* @author Lorn
* @description 针对表【thumbs_up(点赞表)】的数据库操作Mapper
* @createDate 2025-04-15 08:50:33
* @Entity generator.domain.ThumbsUp
*/


/**
 * 点赞Mapper接口
 */
@Mapper
public interface ThumbsUpMapper extends BaseMapper<ThumbsUp> {

    /**
     * 检查用户是否点赞
     */
    int checkUserLiked(@Param("userId") Long userId,
                       @Param("forType") String forType,
                       @Param("forUid") Integer forUid);
}




