package com.gumeng.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gumeng.domain.ThumbsUp;

/**
* @author Lorn
* @description 针对表【thumbs_up(点赞表)】的数据库操作Service
* @createDate 2025-04-15 08:50:33
*/

/**
 * 点赞服务接口
 */
public interface ThumbsUpService {
    /**
     * 添加点赞
     * @param userId 用户ID
     * @param forType 点赞类型 (post/comment)
     * @param forUid 被点赞对象ID
     * @return 是否成功
     */
    boolean addThumbsUp(Long userId, String forType, Integer forUid);

    /**
     * 移除点赞
     * @param userId 用户ID
     * @param forType 点赞类型 (post/comment)
     * @param forUid 被点赞对象ID
     * @return 是否成功
     */
    boolean removeThumbsUp(Long userId, String forType, Integer forUid);

    /**
     * 检查用户是否点赞
     * @param userId 用户ID
     * @param forType 点赞类型 (post/comment)
     * @param forUid 被点赞对象ID
     * @return 是否已点赞
     */
    boolean checkUserLiked(Long userId, String forType, Integer forUid);
}
