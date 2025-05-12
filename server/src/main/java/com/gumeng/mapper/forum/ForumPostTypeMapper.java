package com.gumeng.mapper.forum;

import com.gumeng.domain.forum.ForumPostType;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @description 针对表【forum_post_type(帖子类型)】的数据库操作Mapper
 */
@Mapper
public interface ForumPostTypeMapper extends BaseMapper<ForumPostType> {
} 