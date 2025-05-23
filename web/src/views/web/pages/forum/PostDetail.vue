<script setup>
import { ref, computed, onMounted, nextTick } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { Message } from '@arco-design/web-vue';
import { 
  IconLeft, 
  IconEye, 
  IconHeart, 
  IconHeartFill, 
  IconMessage, 
  IconDelete,
  IconLoading 
} from '@arco-design/web-vue/es/icon';
import { 
  getPostDetailAPI, 
  likePostAPI, 
  unlikePostAPI, 
  deletePostAPI, 
  getCommentsAPI, 
  addCommentAPI, 
  deleteCommentAPI, 
  likeCommentAPI, 
  unlikeCommentAPI,
  checkSensitiveWordsAPI
} from '@/api/forum';
import { useUserStore } from '@/stores/userStore.js';
import { formatDate } from '@/utils/format';
import Footer from "@/views/web/layout/Footer.vue";
// 导入Markdown查看器
import { Viewer } from '@/views/web/pages/forum/bytemd';
// 导入ByteMD插件
import gfm from '@bytemd/plugin-gfm'
import highlight from '@bytemd/plugin-highlight'
import gemoji from '@bytemd/plugin-gemoji'
// 导入ByteMD样式
import 'bytemd/dist/index.css'

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

// ByteMD插件
const plugins = [
  gfm(),
  highlight(),
  gemoji(),
]

// 用户登录状态
const isLogin = computed(() => userStore.isLogin);

// 帖子ID
const postId = computed(() => route.params.id);

// 帖子详情
const post = ref({});
const loading = ref(true);
const error = ref(false);

// 评论列表
const comments = ref([]);
const commentContent = ref('');
const replyingTo = ref(null);
const replyContent = ref('');
const parentComment = ref(null);

// 点赞防抖控制 - 防止重复点击
const likingPostId = ref(null);
const likingCommentIds = ref(new Set());

// 分页
const hasMore = ref(false);
const page = ref(1);
const pageSize = ref(10);

// 弹窗控制
const showDeleteConfirm = ref(false);
const showCommentDeleteConfirm = ref(false);
const deleteCommentModalVisible = ref(false);
const commentToDelete = ref(null);

// 敏感词相关
const checkingSensitiveWords = ref(false);
const sensitiveWordsError = ref('');

// 计算评论总数（包括所有回复）
const totalCommentCount = computed(() => {
  let count = comments.value.length;
  // 加上所有子评论
  comments.value.forEach(comment => {
    if (comment.children && comment.children.length > 0) {
      count += comment.children.length;
    }
  });
  return count;
});

// 是否可以删除帖子
const canDelete = computed(() => {
  console.log('删除帖子权限检查:');
  console.log('- 是否管理员:', userStore.isAdmin);
  console.log('- 当前用户ID:', userStore.userInfo?.id);
  console.log('- 帖子用户ID字段:', post.value.userId);
  console.log('- 帖子作者ID:', post.value.userId);
  
  return userStore.isAdmin || (post.value.userId === userStore.userInfo?.id);
});

// 返回上一页
const goBack = () => {
  router.push('/forum');
};

// 获取帖子详情
const fetchPostDetail = async () => {
  loading.value = true;
  error.value = false;
  let retryCount = 0;
  const maxRetries = 3;
  
  const attemptFetch = async () => {
    try {
      console.log(`获取帖子详情: ${postId.value}, 尝试次数: ${retryCount + 1}/${maxRetries + 1}`);
      const res = await getPostDetailAPI(postId.value);
      console.log('帖子详情响应:', res);
      
      if (res.code === 200) {
        post.value = res.data;
        
        // 确保用户名和头像字段正确
        post.value.username = post.value.username || post.value.authorName || '匿名用户';
        post.value.avatar = post.value.avatar || post.value.userPic || '/avatar/default-avatar.png';
        
        // 将图片字符串转为数组
        if (post.value.images && typeof post.value.images === 'string') {
          post.value.images = post.value.images.split(',').filter(img => img);
        } else if (!post.value.images) {
          post.value.images = [];
        }
        
        // 获取评论列表
        fetchComments();
        
        // 成功获取数据，设置loading为false
        loading.value = false;
      } else {
        // 获取失败，判断是否重试
        if (retryCount < maxRetries) {
          retryCount++;
          console.log(`获取帖子详情失败，将在1秒后进行第${retryCount + 1}次尝试`);
          setTimeout(attemptFetch, 1000);
        } else {
          // 达到最大重试次数，显示错误
          error.value = true;
          Message.error(res.msg || '获取帖子详情失败');
          loading.value = false;
        }
      }
    } catch (err) {
      console.error('获取帖子详情出错:', err);
      
      // 发生错误，判断是否重试
      if (retryCount < maxRetries) {
        retryCount++;
        console.log(`获取帖子详情出错，将在1秒后进行第${retryCount + 1}次尝试`);
        setTimeout(attemptFetch, 1000);
      } else {
        // 达到最大重试次数，显示错误
        error.value = true;
        Message.error('获取帖子详情失败，请稍后重试');
        loading.value = false;
      }
    }
  };
  
  // 开始第一次尝试
  attemptFetch();
};

// 获取评论列表
const fetchComments = async (loadMore = false) => {
  try {
    console.log('获取评论列表, 帖子ID:', postId.value);
    const currentPage = loadMore ? page.value : 1;
    
    const res = await getCommentsAPI({
      postId: postId.value,
      pageNum: currentPage,
      pageSize: pageSize.value
    });
    
    console.log('评论列表响应:', res);
    
    if (res.code === 200) {
      // 确保返回的数据是数组
      let newComments = res.data || [];
      
      // 如果返回的是分页对象，则取records属性
      if (res.data && res.data.records) {
        newComments = res.data.records;
      }
      
      console.log('处理后的评论数据:', newComments);
      
      if (loadMore) {
        comments.value = [...comments.value, ...newComments];
      } else {
        comments.value = newComments;
      }
      
      // 判断是否还有更多评论
      hasMore.value = res.data && res.data.total ? 
        comments.value.length < res.data.total : 
        false;
      
      if (loadMore) {
        page.value++;
      } else {
        page.value = 2; // 下次加载从第2页开始
      }
    } else {
      Message.warning(res.msg || '获取评论失败');
    }
  } catch (err) {
    console.error('获取评论列表出错:', err);
    Message.warning('获取评论失败，请稍后重试');
  }
};

// 加载更多评论
const loadMoreComments = () => {
  if (hasMore.value) {
    fetchComments(true);
  }
};

// 点赞/取消点赞
const handleLike = async () => {
  if (!userStore.isLogin) {
    Message.warning('请先登录再进行操作');
    return;
  }
  
  // 防止重复点击
  if (likingPostId.value === postId.value) {
    return;
  }
  
  likingPostId.value = postId.value;
  
  try {
    let res;
    if (post.value.isLiked) {
      res = await unlikePostAPI(postId.value);
      if (res.code === 200) {
        post.value.isLiked = false;
        post.value.thumbsUpNum = Math.max(0, (post.value.thumbsUpNum || 1) - 1);
        Message.success('已取消点赞');
      }
    } else {
      res = await likePostAPI(postId.value);
      if (res.code === 200) {
        post.value.isLiked = true;
        post.value.thumbsUpNum = (post.value.thumbsUpNum || 0) + 1;
        Message.success('点赞成功');
      }
    }
    
    if (res.code !== 200) {
      Message.error(res.msg || '操作失败');
    }
  } catch (err) {
    console.error('点赞操作出错:', err);
    Message.error('操作失败，请稍后重试');
  } finally {
    // 无论成功失败，都移除锁定状态
    likingPostId.value = null;
  }
};

// 删除帖子
const deletePost = async () => {
  try {
    const res = await deletePostAPI(postId.value);
    if (res.code === 200) {
      Message.success('删除成功');
      router.push('/forum');
    } else {
      Message.error(res.msg || '删除失败');
    }
    showDeleteConfirm.value = false;
  } catch (err) {
    console.error('删除帖子出错:', err);
    Message.error('删除失败，请稍后重试');
    showDeleteConfirm.value = false;
  }
};

// 提交评论
const submitComment = async () => {
  if (!userStore.isLogin) {
    Message.warning('请先登录再进行操作');
    return;
  }
  
  if (!commentContent.value.trim()) {
    Message.warning('评论内容不能为空');
    return;
  }
  
  // 检查敏感词
  checkingSensitiveWords.value = true;
  sensitiveWordsError.value = '';
  
  // 添加重试计数器
  let retryCount = 0;
  const maxRetries = 2;
  
  const attemptCheck = async () => {
    try {
      const checkResult = await checkSensitiveWordsAPI({ text: commentContent.value.trim() });
      console.log('内容审核结果:', checkResult);
      
      // 处理多种审核失败情况
      if (checkResult.code === 200) {
        // 处理完整审核结果
        const approved = checkResult.data?.approved;
        
        // 如果审核不通过
        if (!approved) {
          // 统一错误提示，不暴露具体审核原因
          sensitiveWordsError.value = checkResult.data?.message || "内容审核不通过，请修改后重试";
          Message.error(sensitiveWordsError.value);
          checkingSensitiveWords.value = false;
          return false;
        }
        
        // 通过审核，继续提交
        return true;
      } else {
        // API调用失败，根据重试次数决定是否重试
        if (retryCount < maxRetries) {
          retryCount++;
          console.log(`内容审核请求失败，第${retryCount}次重试`);
          // 延迟500ms后重试
          await new Promise(resolve => setTimeout(resolve, 500));
          return attemptCheck();
        } else {
          // 达到最大重试次数，显示错误
          Message.warning('内容审核服务暂不可用，请稍后重试');
          checkingSensitiveWords.value = false;
          return false;
        }
      }
    } catch (err) {
      console.error('内容审核出错:', err);
      
      // 检查是否需要重试
      if (retryCount < maxRetries) {
        retryCount++;
        console.log(`内容审核异常，第${retryCount}次重试`);
        // 延迟500ms后重试
        await new Promise(resolve => setTimeout(resolve, 500));
        return attemptCheck();
      } else {
        // 达到最大重试次数，允许提交
        console.warn('内容审核服务异常，允许提交');
        return true;
      }
    }
  };
  
  // 开始检查
  const checkPassed = await attemptCheck();
  if (!checkPassed) {
    checkingSensitiveWords.value = false;
    return;
  }
  
  // 内容审核通过，继续提交评论
  checkingSensitiveWords.value = false;
  
  try {
    const res = await addCommentAPI({
      postId: postId.value,
      pageId: postId.value,  // 同时提供pageId参数，确保后端能正确识别
      content: commentContent.value.trim(),
      parentId: 0
    });
    
    if (res.code === 200) {
      Message.success('评论成功');
      commentContent.value = '';
      sensitiveWordsError.value = '';
      // 重新获取评论列表
      fetchComments();
      // 更新帖子评论数
      post.value.commentCount = (post.value.commentCount || 0) + 1;
    } else {
      Message.error(res.msg || '评论失败');
    }
  } catch (err) {
    console.error('提交评论出错:', err);
    Message.error('评论失败，请稍后重试');
  }
};

// 回复评论
const replyToComment = (comment, parent = null) => {
  if (!userStore.isLogin) {
    Message.warning('请先登录再进行操作');
    return;
  }
  
  replyingTo.value = comment;
  parentComment.value = parent || comment;
  replyContent.value = '';
  
  // 滚动到回复框
  nextTick(() => {
    const replyBox = document.querySelector(`.reply-form-${comment.id}`);
    if (replyBox) {
      replyBox.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
  });
};

// 取消回复
const cancelReply = () => {
  replyingTo.value = null;
  parentComment.value = null;
  replyContent.value = '';
};

// 提交回复
const submitReply = async () => {
  if (!userStore.isLogin) {
    Message.warning('请先登录再进行操作');
    return;
  }
  
  if (!replyContent.value.trim()) {
    Message.warning('回复内容不能为空');
    return;
  }
  
  // 检查敏感词
  checkingSensitiveWords.value = true;
  sensitiveWordsError.value = '';
  
  // 添加重试计数器
  let retryCount = 0;
  const maxRetries = 2;
  
  const attemptCheck = async () => {
    try {
      const checkResult = await checkSensitiveWordsAPI({ text: replyContent.value.trim() });
      console.log('内容审核结果:', checkResult);
      
      // 处理审核结果
      if (checkResult.code === 200) {
        // 处理完整审核结果
        const approved = checkResult.data?.approved;
        
        // 如果审核不通过
        if (!approved) {
          // 统一错误提示，不暴露具体审核原因
          sensitiveWordsError.value = checkResult.data?.message || "内容审核不通过，请修改后重试";
          Message.error(sensitiveWordsError.value);
          checkingSensitiveWords.value = false;
          return false;
        }
        
        // 通过审核，继续提交
        return true;
      } else {
        // API调用失败，根据重试次数决定是否重试
        if (retryCount < maxRetries) {
          retryCount++;
          console.log(`内容审核请求失败，第${retryCount}次重试`);
          // 延迟500ms后重试
          await new Promise(resolve => setTimeout(resolve, 500));
          return attemptCheck();
        } else {
          // 达到最大重试次数，显示错误
          Message.warning('内容审核服务暂不可用，请稍后重试');
          checkingSensitiveWords.value = false;
          return false;
        }
      }
    } catch (err) {
      console.error('内容审核出错:', err);
      
      // 检查是否需要重试
      if (retryCount < maxRetries) {
        retryCount++;
        console.log(`内容审核异常，第${retryCount}次重试`);
        // 延迟500ms后重试
        await new Promise(resolve => setTimeout(resolve, 500));
        return attemptCheck();
      } else {
        // 达到最大重试次数，允许提交
        console.warn('内容审核服务异常，允许提交');
        return true;
      }
    }
  };
  
  // 开始检查
  const checkPassed = await attemptCheck();
  if (!checkPassed) {
    checkingSensitiveWords.value = false;
    return;
  }
  
  // 内容审核通过，继续提交回复
  checkingSensitiveWords.value = false;
  
  try {
    const res = await addCommentAPI({
      postId: postId.value,
      pageId: postId.value,  // 同时提供pageId参数，确保后端能正确识别
      content: replyContent.value.trim(),
      parentId: parentComment.value.id,
      parent: parentComment.value.id.toString(),  // 确保parent参数正确传递
      replyToId: replyingTo.value.userId,
      forUser: replyingTo.value.userId.toString()  // 同时提供forUser参数
    });
    
    if (res.code === 200) {
      Message.success('回复成功');
      replyContent.value = '';
      replyingTo.value = null;
      parentComment.value = null;
      sensitiveWordsError.value = '';
      // 重新获取评论列表
      fetchComments();
      // 更新帖子评论数
      post.value.commentCount = (post.value.commentCount || 0) + 1;
    } else {
      Message.error(res.msg || '回复失败');
    }
  } catch (err) {
    console.error('提交回复出错:', err);
    Message.error('回复失败，请稍后重试');
  }
};

// 判断是否可以删除评论
const canDeleteComment = (comment) => {
  // 检查用户是否登录
  if (!userStore.userInfo) {
    console.log('用户未登录，无法删除评论');
    return false;
  }
  
  // 检查评论是否有用户ID
  if (!comment.userId) {
    console.log('评论缺少用户ID，无法删除');
    return false;
  }
  
  // 管理员可以删除任何评论
  if (userStore.isAdmin) {
    console.log('管理员权限，可以删除评论');
    return true;
  }
  
  // 确保ID是字符串格式进行比较
  const currentUserId = String(userStore.userInfo.id);
  const commentUserId = String(comment.userId);
  
  console.log('删除评论权限检查:');
  console.log('- 当前用户ID:', currentUserId, '类型:', typeof userStore.userInfo.id);
  console.log('- 评论用户ID:', commentUserId, '类型:', typeof comment.userId);
  console.log('- 是否相等:', currentUserId === commentUserId);
  
  return currentUserId === commentUserId;
};

// 显示删除评论确认框
const showDeleteCommentConfirm = (comment) => {
  console.log('准备删除评论:', comment);
  console.log('评论ID:', comment.id);
  console.log('评论用户ID:', comment.userId);
  console.log('当前用户ID:', userStore.userInfo?.id);
  
  const canDelete = canDeleteComment(comment);
  console.log('是否可以删除:', canDelete);
  
  if (canDelete) {
    commentToDelete.value = comment;
    deleteCommentModalVisible.value = true;
    console.log('显示删除确认框，状态:', deleteCommentModalVisible.value);
  } else {
    Message.warning('您没有权限删除此评论');
  }
};

// 删除评论
const confirmDeleteComment = async () => {
  console.log('开始执行删除操作');
  if (!commentToDelete.value) {
    console.log('没有要删除的评论');
    return;
  }
  
  try {
    console.log('删除评论ID:', commentToDelete.value.id);
    const res = await deleteCommentAPI(commentToDelete.value.id);
    console.log('删除评论响应:', res);
    
    if (res.code === 200) {
      Message.success('删除成功');
      // 重新获取评论列表
      fetchComments();
      // 更新帖子评论数
      post.value.commentCount = Math.max((post.value.commentCount || 0) - 1, 0);
    } else {
      Message.error(res.msg || '删除失败');
      console.error('删除评论失败，返回:', res);
    }
    deleteCommentModalVisible.value = false;
    commentToDelete.value = null;
  } catch (err) {
    console.error('删除评论出错:', err);
    Message.error('删除失败，请稍后重试');
    deleteCommentModalVisible.value = false;
    commentToDelete.value = null;
  }
};

// 评论点赞/取消点赞
const handleLikeComment = async (comment) => {
  if (!userStore.isLogin) {
    Message.warning('请先登录再进行操作');
    return;
  }
  
  // 防止重复点击
  if (likingCommentIds.value.has(comment.id)) {
    return;
  }
  
  likingCommentIds.value.add(comment.id);
  
  try {
    let res;
    if (comment.isLiked) {
      res = await unlikeCommentAPI(comment.id);
      if (res.code === 200) {
        comment.isLiked = false;
        comment.thumbsUp = Math.max(0, (comment.thumbsUp || 1) - 1);
      }
    } else {
      res = await likeCommentAPI(comment.id);
      if (res.code === 200) {
        comment.isLiked = true;
        comment.thumbsUp = (comment.thumbsUp || 0) + 1;
      }
    }
    
    if (res.code !== 200) {
      Message.error(res.msg || '操作失败');
    }
  } catch (err) {
    console.error('评论点赞操作出错:', err);
    Message.error('操作失败，请稍后重试');
  } finally {
    // 无论成功失败，都移除锁定状态
    likingCommentIds.value.delete(comment.id);
  }
};

// 获取图片展示样式
const getGridClass = (imageCount) => {
  return `grid-${Math.min(imageCount, 9)}`;
};

onMounted(() => {
  fetchPostDetail();
});
</script>

<template>
  <div>
    <div class="post-detail-container">
      <!-- 返回按钮 -->
      <div class="back-nav">
        <a-button @click="goBack" class="back-btn">
          <icon-left />返回论坛
        </a-button>
      </div>

      <a-spin :loading="loading" size="large">
        <template #icon><icon-loading /></template>
        <div class="post-detail-content">
          <!-- 错误状态 -->
          <div v-if="error && !loading" class="error-container">
            <a-result status="error" title="获取帖子详情失败" subtitle="请稍后重试">
              <template #extra>
                <a-button type="primary" @click="fetchPostDetail">重试</a-button>
              </template>
            </a-result>
          </div>

          <!-- 帖子内容 -->
          <template v-else-if="!loading">
            <a-card class="post-card">
              <div class="post-header">
                <div class="post-topic" v-if="post.topic">
                  <span class="topic-tag">{{ post.topic }}</span>
                </div>
                <h1 class="post-title">{{ post.title }}</h1>
                <div class="post-meta">
                  <div class="author-info">
                    <img :src="post.avatar || '/avatar/default-avatar.png'" alt="作者头像" class="author-avatar">
                    <div class="author-detail">
                      <div class="author-name">{{ post.username }}</div>
                      <div class="post-time">{{ formatDate(post.createTime) }}</div>
                    </div>
                  </div>
                </div>
              </div>

              <div class="post-body">
                <!-- 使用Markdown渲染器替换原始文本显示 -->
                <div class="content-markdown">
                  <Viewer :value="post.content" :plugins="plugins" />
                </div>
                
                <!-- 图片展示 -->
                <div v-if="post.images && post.images.length > 0" class="post-images">
                  <a-image-preview-group infinite>
                    <div class="image-grid" :class="getGridClass(post.images.length)">
                      <div v-for="(img, index) in post.images" :key="index" class="image-wrapper">
                        <a-image 
                          :src="img" 
                          :alt="`图片${index+1}`"
                          fit="contain"
                          class="post-image"
                        />
                      </div>
                    </div>
                  </a-image-preview-group>
                </div>
              </div>
              
              <div class="post-footer">
                <div class="post-stats">
                  <span class="views">
                    <icon-eye />
                    <span>{{ post.viewCount || 0 }} 阅读</span>
                  </span>
                </div>
                <div class="interaction-actions">
                  <a-button type="outline" @click="showDeleteConfirm = true" class="action-btn" v-if="canDelete">
                    <icon-delete />
                    删除
                  </a-button>
                  <a-button 
                    :type="post.isLiked ? 'primary' : 'outline'" 
                    @click="handleLike" 
                    class="action-btn" 
                    :class="{ 'liked': post.isLiked }"
                  >
                    <icon-heart-fill v-if="post.isLiked" />
                    <icon-heart v-else />
                    {{ post.thumbsUpNum || 0 }} 点赞
                  </a-button>
                </div>
              </div>
            </a-card>
          </template>
        </div>
      </a-spin>

      <!-- 评论区 -->
      <div id="comment-section" class="comment-section" v-if="!loading && !error">
        <a-card>
          <template #title>
            <div class="section-header">
              <h2 class="section-title">评论区 ({{ totalCommentCount }})</h2>
            </div>
          </template>
          
          <!-- 评论输入框 -->
          <div class="comment-container">
            <a-textarea
              v-model="commentContent"
              placeholder="留下你的精彩评论吧"
              :disabled="!isLogin"
              :auto-size="{ minRows: 3, maxRows: 5 }"
              class="comment-textarea"
            />
            <!-- 敏感词错误提示 -->
            <a-alert v-if="sensitiveWordsError" type="error" :content="sensitiveWordsError" style="margin: 10px 0;" />
            <a-button 
              type="primary" 
              @click="submitComment" 
              :loading="checkingSensitiveWords"
              :disabled="!commentContent.trim() || !userStore.isLogin || checkingSensitiveWords" 
              class="comment-submit-btn"
            >
              {{ userStore.isLogin ? (checkingSensitiveWords ? '检测中...' : '发表评论') : '请先登录' }}
            </a-button>
          </div>
          
          <!-- 评论列表 -->
          <div v-if="comments.length === 0" class="empty-comments">
            <a-empty description="暂无评论，快来发表第一条评论吧！" />
          </div>
          <div v-else class="comments-list">
            <div v-for="comment in comments" :key="comment.id" class="comment-item">
              <div class="comment-content">
                <div class="comment-author">
                  <img :src="comment.userPic || '@/assets/avatar/default-avatar.png'" alt="头像" class="comment-avatar" />
                  <div class="author-info">
                    <div class="user-name">{{ comment.username }}</div>
                    <div class="comment-time">{{ formatDate(comment.createTime) }}</div>
                  </div>
                </div>
                <div class="comment-text">
                  <template v-if="comment.forUsername">
                    <span class="reply-to">回复 <span class="reply-name">@{{ comment.forUsername }}</span>：</span>
                  </template>
                  <span>{{ comment.content }}</span>
                </div>
                <div class="comment-actions">
                  <a-button type="text" size="small" @click="replyToComment(comment)" class="action-link">
                    <icon-message />回复
                  </a-button>
                  <a-button type="text" size="small" @click="handleLikeComment(comment)" class="action-link" :class="{ 'active': comment.isLiked }">
                    <icon-heart-fill v-if="comment.isLiked" />
                    <icon-heart v-else />
                    {{ comment.thumbsUp || 0 }}
                  </a-button>
                  <a-button v-if="canDeleteComment(comment)" type="text" size="small" @click="showDeleteCommentConfirm(comment)" class="action-link delete">
                    <icon-delete />删除
                  </a-button>
                </div>
                
                <!-- 回复输入框 -->
                <div v-if="replyingTo && replyingTo.id === comment.id" :class="['reply-form', `reply-form-${comment.id}`]">
                  <a-textarea
                    v-model="replyContent"
                    placeholder="输入您想回复的内容"
                    :auto-size="{ minRows: 2, maxRows: 4 }"
                    class="reply-textarea"
                  />
                  <!-- 敏感词错误提示 -->
                  <a-alert v-if="sensitiveWordsError" type="error" :content="sensitiveWordsError" style="margin: 5px 0;" />
                  <a-button size="small" @click="cancelReply" class="reply-cancel-btn">
                    取消
                  </a-button>
                  <a-button 
                    type="primary" 
                    size="small" 
                    @click="submitReply" 
                    :loading="checkingSensitiveWords"
                    :disabled="!replyContent.trim() || checkingSensitiveWords" 
                    class="reply-submit-btn"
                  >
                    {{ checkingSensitiveWords ? '检测中...' : '回复' }}
                  </a-button>
                </div>
                
                <!-- 子评论 -->
                <div v-if="comment.children && comment.children.length > 0" class="child-comments">
                  <div v-for="child in comment.children" :key="child.id" class="child-comment-item">
                    <div class="comment-author">
                      <img :src="child.userPic || '@/assets/avatar/default-avatar.png'" alt="头像" class="reply-avatar" />
                      <div class="author-info">
                        <div class="user-name">{{ child.username }}</div>
                        <div class="comment-time">{{ formatDate(child.createTime) }}</div>
                      </div>
                    </div>
                    <div class="comment-text">
                      <template v-if="child.forUsername">
                        <span class="reply-to">回复 <span class="reply-name">@{{ child.forUsername }}</span>：</span>
                      </template>
                      <span>{{ child.content }}</span>
                    </div>
                    <div class="comment-actions">
                      <a-button type="text" size="small" @click="replyToComment(child, comment)" class="action-link">
                        <icon-message />回复
                      </a-button>
                      <a-button type="text" size="small" @click="handleLikeComment(child)" class="action-link" :class="{ 'active': child.isLiked }">
                        <icon-heart-fill v-if="child.isLiked" />
                        <icon-heart v-else />
                        {{ child.thumbsUp || 0 }}
                      </a-button>
                      <a-button v-if="canDeleteComment(child)" type="text" size="small" @click="showDeleteCommentConfirm(child)" class="action-link delete">
                        <icon-delete />删除
                      </a-button>
                    </div>
                    
                    <!-- 回复输入框 -->
                    <div v-if="replyingTo && replyingTo.id === child.id" :class="['reply-form', `reply-form-${child.id}`]">
                      <a-textarea
                        v-model="replyContent"
                        placeholder="输入您想回复的内容"
                        :auto-size="{ minRows: 2, maxRows: 4 }"
                        class="reply-textarea"
                      />
                      <!-- 敏感词错误提示 -->
                      <a-alert v-if="sensitiveWordsError" type="error" :content="sensitiveWordsError" style="margin: 5px 0;" />
                      <a-button size="small" @click="cancelReply" class="reply-cancel-btn">
                        取消
                      </a-button>
                      <a-button 
                        type="primary" 
                        size="small" 
                        @click="submitReply" 
                        :loading="checkingSensitiveWords"
                        :disabled="!replyContent.trim() || checkingSensitiveWords" 
                        class="reply-submit-btn"
                      >
                        {{ checkingSensitiveWords ? '检测中...' : '回复' }}
                      </a-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 加载更多 -->
          <div v-if="hasMore" class="load-more">
            <a-button @click="loadMoreComments">
              加载更多评论
            </a-button>
          </div>
        </a-card>
      </div>
      
      <!-- 删除评论确认框 -->
      <a-modal
        :visible="deleteCommentModalVisible"
        @before-ok="confirmDeleteComment"
        @before-cancel="() => { deleteCommentModalVisible = false }"
        @close="deleteCommentModalVisible = false"
        title="确认删除"
        simple
        :width="350"
      >
        <p style="margin: 0; padding: 5px 0;">确定要删除这条评论吗？此操作无法撤销。</p>
      </a-modal>
      
      <!-- 删除帖子确认框 -->
      <a-modal
        :visible="showDeleteConfirm"
        @before-ok="deletePost"
        @before-cancel="() => { showDeleteConfirm = false }"
        @close="showDeleteConfirm = false"
        title="确认删除"
        simple
        :width="350"
      >
        <p style="margin: 0; padding: 5px 0;">确定要删除这篇帖子吗？此操作无法撤销。</p>
      </a-modal>
    </div>
    
    <!-- 页脚 -->
    <Footer class="footer"></Footer>
  </div>
</template>

<style scoped>
.post-detail-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
  background-color: #fffbf0;
  font-family: "SimSun", "宋体", serif;
  min-height: calc(100vh - 176px);
}

.back-nav {
  margin-bottom: 20px;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  background-color: #8C1F28;
  border-color: #8C1F28;
  color: #FFFBF0;
}

.back-btn:hover {
  background-color: #A52A2A;
  border-color: #A52A2A;
  color: #FFFBF0;
}

.post-detail-content {
  min-height: 300px;
  width: 100%;
}

.error-container {
  background-color: #FFF7E9;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  margin-bottom: 20px;
  border: 1px solid #D6C6AF;
}

:deep(.arco-spin) {
  width: 100%;
  display: flex;
  justify-content: center;
}

:deep(.arco-spin-icon) {
  color: #8C1F28;
  font-size: 24px;
}

/* 帖子内容区域 */
.post-card {
  margin-bottom: 20px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  background-color: #FFFBF0;
  border: 1px solid #D6C6AF;
}

.post-header {
  padding: 24px 24px 16px;
  border-bottom: 1px solid #E4D9C3;
  background-color: #8C1F28;
  color: #F9F3E9;
  position: relative;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.post-header::before {
  content: none;
}

.post-topic {
  margin-bottom: 16px;
  position: relative;
  z-index: 1;
}

.topic-tag {
  padding: 4px 12px;
  background-color: rgba(255, 251, 240, 0.2);
  color: #FFFBF0;
  font-size: 14px;
  border-radius: 4px;
  display: inline-block;
  border: 1px solid rgba(255, 251, 240, 0.3);
}

.post-title {
  font-size: 24px;
  font-weight: bold;
  color: #FFFBF0;
  margin: 0 0 16px;
  line-height: 1.4;
  font-family: "STKaiti", "楷体", serif;
  position: relative;
  z-index: 1;
  text-shadow: 1px 1px 3px rgba(0, 0, 0, 0.3);
  letter-spacing: 1px;
}

.post-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  z-index: 1;
}

.author-info {
  display: flex;
  flex-direction: row;
  align-items: center;
}

.author-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  margin-right: 12px;
  border: 2px solid rgba(255, 251, 240, 0.7);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
}

.author-detail {
  display: flex;
  flex-direction: column;
}

.author-name {
  font-size: 16px;
  font-weight: 500;
  color: #FFFBF0;
  margin-right: 15px;
  margin-bottom: 0;
}

.post-time {
  font-size: 14px;
  color: rgba(255, 251, 240, 0.8);
  min-width: 120px;
}

.post-actions button {
  color: #FFFBF0;
  border-color: rgba(255, 251, 240, 0.3);
}

.post-body {
  padding: 24px;
  background-color: #FFF7E9;
}

.content-text {
  font-size: 16px;
  line-height: 1.8;
  color: #582F0E;
  margin-bottom: 24px;
  white-space: pre-wrap;
  word-break: break-word;
  text-align: justify;
}

/* 添加Markdown内容的样式 */
.content-markdown {
  font-size: 16px;
  line-height: 1.8;
  color: #582F0E;
  margin-bottom: 24px;
}

/* Markdown渲染器 */
.content-markdown :deep(.markdown-body) {
  background-color: transparent;
  font-family: "SimSun", "宋体", serif;
  color: #582F0E;
}

.post-images {
  margin-bottom: 24px;
  max-width: 100%;
  overflow: hidden;
}

.image-grid {
  display: grid;
  grid-gap: 8px;
  width: 100%;
  max-width: 700px;
  margin: 0;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
}

.image-wrapper {
  position: relative;
  padding-bottom: 75%;
  height: 0;
  overflow: hidden;
  border-radius: 4px;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
}

.post-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: contain;
  transition: transform 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  max-width: 100%;
  max-height: 100%;
}

:deep(.arco-image) {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.arco-image-img) {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.post-image:hover {
  transform: scale(1.02);
}

/* 根据图片数量定义网格布局 */
.grid-1 {
  grid-template-columns: minmax(0, 300px);
  max-width: 300px;
}

.grid-2 {
  grid-template-columns: repeat(2, 1fr);
  max-width: 500px;
}

.grid-3, .grid-5, .grid-6, .grid-7, .grid-8, .grid-9 {
  grid-template-columns: repeat(3, 1fr);
  max-width: 600px;
}

.post-footer {
  padding: 16px 24px;
  border-top: 1px solid #E4D9C3;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #FFFBF0;
}

.post-stats {
  font-size: 14px;
  color: #7F4F24;
}

.views {
  display: flex;
  align-items: center;
  gap: 8px;
}

.topic-tag-footer {
  padding: 4px 12px;
  background-color: #FBF0E9;
  color: #8C1F28;
  font-size: 12px;
  border-radius: 4px;
  display: inline-block;
  border: 1px solid #E4D9C3;
}

.interaction-actions {
  display: flex;
  gap: 16px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s ease;
  color: #582F0E;
  border-color: #D6C6AF;
  background-color: #FFFBF0;
}

.action-btn.liked {
  color: #FFFBF0;
  background-color: #8C1F28;
  border-color: #8C1F28;
}

.action-btn:hover {
  transform: translateY(-2px);
  background-color: #8C1F28;
  color: #FFFBF0;
  border-color: #8C1F28;
}

/* 评论区域 */
.comment-section {
  margin-bottom: 20px;
}

.section-header {
  margin-bottom: 16px;
}

.section-title {
  font-size: 20px;
  font-weight: bold;
  color: #582F0E;
  position: relative;
  display: inline-block;
  font-family: "STKaiti", "楷体", serif;
  letter-spacing: 1px;
}

.section-title::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: -8px;
  width: 40px;
  height: 3px;
  background-color: #8C1F28;
}

.comment-container {
  position: relative;
  margin-bottom: 24px;
}

.comment-textarea {
  background-color: #FFFDF7;
  border: 1px solid #D6C6AF;
  border-radius: 4px;
  transition: all 0.3s ease;
  color: #582F0E;
  resize: none;
  width: 100%;
  padding-right: 100px;
  padding-bottom: 15px;
}

.comment-textarea:hover, .comment-textarea:focus {
  border-color: #8C1F28;
}

.comment-submit-btn {
  position: absolute;
  right: 10px;
  bottom: 10px;
  background-color: #8C1F28;
  border-color: #8C1F28;
  color: #FFFBF0;
  z-index: 2;
}

.comment-submit-btn:hover {
  background-color: #A52A2A;
  border-color: #A52A2A;
}

.empty-comments {
  text-align: center;
  padding: 20px 0;
  background-color: #FFF7E9;
  border-radius: 8px;
  border: 1px solid #D6C6AF;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.comment-item {
  position: relative;
}

.comment-content {
  background-color: #FFF7E9;
  border-radius: 8px;
  padding: 16px;
  border-left: 3px solid #8C1F28;
  transition: all 0.3s ease;
  border: 1px solid #D6C6AF;
}

.comment-content:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.comment-author {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.comment-avatar, .reply-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
  margin-right: 12px;
  border: 2px solid #E4D9C3;
  box-shadow: 0 2px 6px rgba(0,0,0,0.1);
}

.reply-avatar {
  width: 24px;
  height: 24px;
}

.user-name {
  font-weight: 600;
  color: #582F0E;
  margin-right: 15px;
  margin-bottom: 0;
}

.comment-time {
  font-size: 12px;
  color: #7F4F24;
  min-width: 120px;
}

.comment-text {
  font-size: 15px;
  line-height: 1.6;
  color: #582F0E;
  margin-bottom: 12px;
  word-break: break-word;
}

.reply-to {
  color: #7F4F24;
}

.reply-name {
  color: #8C1F28;
  font-weight: 500;
}

.comment-actions {
  display: flex;
  gap: 16px;
}

.action-link {
  color: #582F0E;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.action-link:hover {
  background-color: #8C1F28;
  transform: translateY(-1px);
  color: #FFFBF0;
}

.action-link.active {
  color: #8C1F28;
}

.action-link.delete:hover {
  color: #FFFBF0;
  background-color: #8C1F28;
}

.reply-form {
  position: relative;
  margin-top: 16px;
  margin-bottom: 10px;
}

.reply-textarea {
  background-color: #FFFDF7;
  border: 1px solid #D6C6AF;
  border-radius: 4px;
  transition: all 0.3s ease;
  color: #582F0E;
  resize: none;
  width: 100%;
  padding-right: 160px;
  padding-bottom: 15px;
}

.reply-textarea:hover, .reply-textarea:focus {
  border-color: #8C1F28;
}

.reply-submit-btn {
  position: absolute;
  right: 10px;
  bottom: 10px;
  background-color: #8C1F28;
  border-color: #8C1F28;
  color: #FFFBF0;
  z-index: 2;
}

.reply-cancel-btn {
  position: absolute;
  right: 70px;
  bottom: 10px;
  z-index: 2;
}

.reply-submit-btn:hover {
  background-color: #A52A2A;
  border-color: #A52A2A;
}

.child-comments {
  margin-top: 16px;
  padding-left: 12px;
  border-left: 2px solid #E4D9C3;
}

.child-comment-item {
  margin-bottom: 16px;
}

.child-comment-item:last-child {
  margin-bottom: 0;
}

.load-more {
  text-align: center;
  margin-top: 24px;
}

.load-more :deep(.arco-btn) {
  background-color: #8C1F28;
  border-color: #8C1F28;
  color: #FFFBF0;
}

.load-more :deep(.arco-btn:hover) {
  background-color: #A52A2A;
  border-color: #A52A2A;
}

/* 响应式设计 */
@media screen and (max-width: 768px) {
  .post-detail-container {
    padding: 12px;
  }
  
  .post-title {
    font-size: 20px;
  }
  
  .post-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .post-footer {
    flex-direction: column;
    gap: 16px;
  }
  
  .interaction-actions {
    width: 100%;
    justify-content: space-between;
  }
  
  .comment-container {
    flex-direction: column;
    gap: 12px;
  }
  
  .input-area {
    display: none;
  }
  
  .image-grid {
    grid-gap: 8px;
  }
  
  .post-image {
    min-height: 150px;
  }
}

.footer{
  display: flex;
  bottom: 0;
}
</style>