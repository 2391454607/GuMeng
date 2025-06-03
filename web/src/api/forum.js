import { http } from "@/utils/http.js";

// 获取帖子列表
export function getPostListAPI(params) {
  // 转换参数名称以匹配后端期望的参数
  const convertedParams = {
    page: params.page || 1,
    size: params.size || 10,
    topic: params.topic || '',
    keyword: params.keyword || ''
  };
  
  // console.log('API调用参数:', convertedParams);    return http.get('/forum/getPosts', convertedParams);
}

// 获取帖子详情
export function getPostDetailAPI(id) {
  return http.get(`/forum/posts/${id}`);
}

// 创建帖子
export function createPostAPI(data) {
  return http.post('/forum/posts', data);
}

// 更新帖子
export function updatePostAPI(id, data) {
  return http.post(`/forum/posts/${id}/update`, data);
}

// 删除帖子
export function deletePostAPI(id) {
  return http.delete(`/forum/posts/${id}`);
}

// 点赞帖子
export function likePostAPI(id) {
  return http.post(`/forum/posts/${id}/like`);
}

// 取消点赞帖子
export function unlikePostAPI(id) {
  return http.post(`/forum/posts/${id}/unlike`);
}

// 获取帖子评论列表
export function getCommentsAPI(postId) {
  // 支持传入对象或直接传入帖子ID
  const id = typeof postId === 'object' ? postId.postId : postId;
  return http.get(`/forum/posts/${id}/comments`);
}

// 添加评论
export function addCommentAPI(postId, content, parentComment = null) {
  // 支持直接传入对象参数
  if (typeof postId === 'object') {
    const data = postId;
    const id = data.postId;

    if (!data.pageId && data.postId) {
      data.pageId = data.postId;
    }
    
    return http.post(`/forum/posts/${id}/comments`, data);
  }
  
  // 支持分开传入参数
  const data = {
    pageId: postId,
    content: content
  };
  
  // 是回复评论，添加父评论ID
  if (parentComment) {
    data.parent = parentComment.id.toString();
    if (parentComment.userId) {
      data.forUser = parentComment.userId.toString();
    }
  }
  
  return http.post(`/forum/posts/${postId}/comments`, data);
}

// 删除评论
export function deleteCommentAPI(commentId) {
  return http.delete(`/forum/comments/${commentId}`);
}

// 评论点赞
export function likeCommentAPI(commentId) {
  return http.post(`/forum/comments/${commentId}/like`);
}

// 取消评论点赞
export function unlikeCommentAPI(commentId) {
  return http.post(`/forum/comments/${commentId}/unlike`);
}

// 热门帖子列表
export function getHotPostsAPI() {
  return http.get('/forum/posts/hot');
}

// 我的帖子列表
export function getMyPostsAPI(params) {
  // 获取当前用户的帖子
  const convertedParams = {
    page: params.page || 1,
    size: params.size || 10,
    selfOnly: true  // 明确指定只查询当前用户的帖子
  };
  
  console.log('获取我的帖子参数:', convertedParams);
  return http.get('/forum/getPosts', convertedParams);
}

// 我点赞的帖子列表
export function getMyLikedPostsAPI(params) {
  return http.get('/forum/posts/liked', params);
}

// 获取话题列表
export function getTopicsAPI() {
  return http.get('/forum/getTopics');
}

// 上传图片
export function uploadImageAPI(file, type = 'post') {
  // 传入的是文件对象，创建FormData
  if (file instanceof File) {
    const formData = new FormData();
    formData.append('file', file);
    if (type) {
      formData.append('type', type);
    }
    return http.post('/forum/file/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
  }
  
  // 传入的已经是FormData，直接发送
  if (file instanceof FormData) {
    return http.post('/forum/file/upload', file, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
  }
  
  // 其他情况
  console.error('uploadImageAPI 参数错误:', file);
  return Promise.reject(new Error('上传参数错误'));
}

/**
 * 检测文本是否包含敏感词
 * @param {Object} data 
 * @param {string} data.text - 待检测的文本内容
 * @returns {Promise} 返回检测结果
 */
export function checkSensitiveWordsAPI(data) {
  return http.post('/forum/checkSensitiveWords', data);
}

// 论坛路由配置
export const forumRoutes = {
  path: '/forum',
  name: 'Forum',
  redirect: '/forum/list',
  component: () => import('@/views/web/Home.vue'),
  meta: {
    requireAuth: false,
    locale: '非遗论坛',
    icon: 'icon-message',
  },
  children: [
    {
      path: 'list',
      name: 'ForumList',
      component: () => import('@/views/web/pages/forum/ForumList.vue'),
      meta: {
        requireAuth: false,
        title: '非遗论坛',
      },
    },
    {
      path: 'detail/:id',
      name: 'ForumDetail',
      component: () => import('@/views/web/pages/forum/PostDetail.vue'),
      meta: {
        requireAuth: false,
        title: '帖子详情',
      },
    },
    {
      path: 'publish',
      name: 'PublishPost',
      component: () => import('@/views/web/pages/forum/PostCreate.vue'),
      meta: {
        requireAuth: true,
        title: '发布帖子',
      },
    },
    {
      path: 'edit/:id',
      name: 'EditPost',
      component: () => import('@/views/web/pages/forum/PostCreate.vue'),
      meta: {
        requireAuth: true,
        title: '编辑帖子',
      },
    },
    {
      path: 'post/:id',
      name: 'PostDetail',
      component: () => import('@/views/web/pages/forum/PostDetail.vue'),
      meta: {
        requireAuth: false,
        title: '帖子详情',
      },
    },
    {
      path: 'create',
      name: 'CreatePost',
      component: () => import('@/views/web/pages/forum/PostCreate.vue'),
      meta: {
        requireAuth: true,
        title: '发布帖子',
      },
    },
  ],
};

// 默认导出路由配置 (与原 router/forum.js 保持一致)
export default forumRoutes; 