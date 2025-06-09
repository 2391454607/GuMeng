<script setup>
import { ref, onMounted, watch } from 'vue';
import { Message } from '@arco-design/web-vue';
import { getMyPostsAPI, deletePostAPI } from '@/api/forum';
import { useRouter } from 'vue-router';
import { formatDate } from '@/utils/format';

const router = useRouter();
const posts = ref([]);
const loading = ref(false);
const pagination = ref({
  current: 1,
  pageSize: 6, 
  total: 0
});

// 删除确认框
const showDeleteConfirm = ref(false);
const postToDelete = ref(null);

// 图片加载状态
const imageLoaded = ref({});

// 处理图片加载完成
const handleImageLoaded = (postId) => {
  imageLoaded.value[postId] = true;
};

// 处理Markdown纯文本展示
const getPlainText = (markdown) => {
  if (!markdown) return '';
  // 移除Markdown语法，保留纯文本
  return markdown
    .replace(/#+\s/g, '') // 去除标题
    .replace(/\*\*/g, '') // 去除粗体
    .replace(/\*/g, '')   // 去除斜体
    .replace(/\[\s*([^\]]*)\s*\]\([^)]*\)/g, '$1') // 处理链接，保留文字
    .replace(/```[\s\S]*?```/g, '') // 移除代码块
    .replace(/`([^`]+)`/g, '$1') // 移除行内代码
    .replace(/~~([^~]+)~~/g, '$1') // 移除删除线
    .replace(/>\s(.*)/g, '$1') // 移除引用
    .replace(/!\[\s*([^\]]*)\s*\]\([^)]*\)/g, '[图片]') // 替换图片为[图片]文本
    .replace(/\n+/g, ' ') // 多个换行替换为一个空格
    .trim();
};

// 从Markdown中提取第一张图片URL
const extractFirstImageUrl = (markdown) => {
  if (!markdown) return null;
  
  // 检查是否是HTML内容
  const isHtmlContent = markdown.includes('<p>') || 
                       markdown.includes('<div>') || 
                       markdown.includes('<h') || 
                       markdown.includes('<span>');
  
  let imgUrl = null;
  
  if (isHtmlContent) {
    // 从HTML内容中提取图片
    const imgRegex = /<img.*?src="(.*?)".*?>/;
    const match = markdown.match(imgRegex);
    imgUrl = match ? match[1] : null;
  } else {
    // 从Markdown内容中提取图片
    const imgRegex = /!\[.*?\]\((.*?)\)/;
    const match = markdown.match(imgRegex);
    imgUrl = match ? match[1] : null;
  }
  
  return imgUrl;
};

// 处理图片数组
const getPostImages = (post) => {
  // 帖子有images字段且不为空
  if (post.images) {
    return typeof post.images === 'string' 
      ? post.images.split(',').filter(img => img) 
      : Array.isArray(post.images) ? post.images : [];
  }
  
  // 否则尝试从内容中提取图片
  const imageUrl = extractFirstImageUrl(post.content);
  return imageUrl ? [imageUrl] : [];
};

// 获取封面图，没有则返回默认图片
const getCoverImage = (post) => {
  const images = getPostImages(post);
  
  if (images && images.length > 0) {
    return images[0];
  }
  
  // 返回默认封面图，基于帖子ID选择不同的默认图片
  return `/image/default-cover-${post.id % 5 + 1}.jpg`;
};

// 获取我的帖子列表
const fetchMyPosts = async () => {
  loading.value = true;
  try {
    const res = await getMyPostsAPI({
      page: pagination.value.current,
      size: pagination.value.pageSize,
      selfOnly: true // 确保只获取当前用户的帖子
    });
    
    if (res.code === 200) {
      // 处理返回数据
      if (res.data && res.data.records) {
        posts.value = res.data.records;
        pagination.value.total = res.data.total || 0;
      } else {
        posts.value = res.data || [];
        pagination.value.total = posts.value.length;
      }
    } else {
      Message.error(res.msg || '获取帖子列表失败');
    }
  } catch (error) {
    Message.error('获取帖子列表失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 查看帖子详情
const viewPost = (post) => {
  router.push(`/forum/detail/${post.id}`);
};

// 编辑帖子
const editPost = (post) => {
  // 修改为正确的编辑路由格式
  router.push(`/forum/edit/${post.id}`);
};

// 打开删除确认框
const openDeleteConfirm = (post) => {
  postToDelete.value = post;
  showDeleteConfirm.value = true;
};

// 确认删除帖子
const confirmDelete = async () => {
  if (!postToDelete.value) return;
  
  try {
    const res = await deletePostAPI(postToDelete.value.id);
    if (res.code === 200) {
      Message.success('删除成功');
      fetchMyPosts(); // 重新加载列表
    } else {
      Message.error(res.msg || '删除失败');
    }
  } catch (error) {
    Message.error('删除失败，请稍后重试');
  } finally {
    showDeleteConfirm.value = false;
    postToDelete.value = null;
  }
};

// 分页变化处理
const handlePageChange = (page) => {
  pagination.value.current = page;
  fetchMyPosts();
};

5// 监听分页参数变化
watch(() => pagination.value.current, (newVal, oldVal) => {
  if (newVal !== oldVal) {
    // 页码变化时处理
  }
});

// 组件挂载后加载数据
onMounted(() => {
  fetchMyPosts();
});
</script>

<template>
  <div class="my-posts-container">
    <!-- 页面头部 -->
    <div class="header-section">
      <div class="title-wrapper">
        <h2 class="page-title">我的文章</h2>
        <span class="post-count">共 {{ pagination.total }} 篇</span>
      </div>
      
      <a-button type="primary" @click="router.push('/forum/create')" class="create-btn">
        <span class="btn-icon">撰</span>
        <span>发布文章</span>
      </a-button>
    </div>
    
    <div class="content-area">
      <a-spin :loading="loading" class="centered-spin">
        <!-- 空状态 -->
        <div v-if="posts.length === 0" class="empty-state">
          <div class="empty-icon">📝</div>
          <div class="empty-text">您还没有发布过文章</div>
          <a-button type="primary" @click="router.push('/forum/create')" class="empty-btn">
            立即创作
          </a-button>
        </div>
        
        <!-- 帖子列表 -->
        <div v-else>
          <div class="posts-list">
            <div v-for="post in posts" :key="post.id" class="post-item">
              <!-- 顶部封面 -->
              <div class="post-cover" @click="viewPost(post)">
                <div class="image-placeholder" v-if="!imageLoaded[post.id]"></div>
                <img 
                  :src="getCoverImage(post)" 
                  alt="文章封面" 
                  class="cover-img"
                  :class="{ 'img-loaded': imageLoaded[post.id] }"
                  @error="(e) => e.target.src = '/image/gumeng.png'"
                  @load="handleImageLoaded(post.id)"
                  loading="lazy"
                />
                <div class="cover-overlay">
                  <span class="view-text">查看详情</span>
                </div>
              </div>
              
              <!-- 内容区 -->
              <div class="post-content" @click="viewPost(post)">
                <div class="post-header">
                  <h3 class="post-title">{{ post.title }}</h3>
                  <div v-if="post.topic" class="post-topic">{{ post.topic }}</div>
                </div>
                
                <div class="post-meta">
                  <div class="post-time">
                    <span class="meta-icon">⏱</span>
                    <span>{{ formatDate(post.createTime) }}</span>
                  </div>
                  <div class="post-stats">
                    <div class="stat-item">
                      <span class="meta-icon">👁</span>
                      <span>{{ post.viewCount || 0 }}</span>
                    </div>
                    <div class="stat-item">
                      <span class="meta-icon">👍</span>
                      <span>{{ post.thumbsUpNum || 0 }}</span>
                    </div>
                    <div class="stat-item">
                      <span class="meta-icon">💬</span>
                      <span>{{ post.commentCount || 0 }}</span>
                    </div>
                  </div>
                </div>
              </div>
              
              <!-- 底部操作区 -->
              <div class="post-actions">
                <a-button-group>
                  <a-tooltip content="查看文章">
                    <a-button @click.stop="viewPost(post)" type="text" size="medium">
                      <span class="btn-text">阅</span>
                    </a-button>
                  </a-tooltip>
                  <a-tooltip content="编辑文章">
                    <a-button @click.stop="editPost(post)" type="text" size="medium">
                      <span class="btn-text">改</span>
                    </a-button>
                  </a-tooltip>
                  <a-tooltip content="删除文章">
                    <a-button @click.stop="openDeleteConfirm(post)" type="text" status="danger" size="medium">
                      <span class="btn-text">删</span>
                    </a-button>
                  </a-tooltip>
                </a-button-group>
              </div>
            </div>
          </div>
          
          <!-- 分页 -->
          <div class="pagination-wrapper">
            <div class="page-summary">
              <span class="page-indicator">第 {{ pagination.current }} 页 / 共 {{ Math.ceil(pagination.total / pagination.pageSize) }} 页</span>
              <span class="total-indicator">共 {{ pagination.total }} 篇文章</span>
            </div>
            <a-pagination
              :current="pagination.current"
              :total="pagination.total"
              :page-size="pagination.pageSize"
              @change="handlePageChange"
              :hide-on-single-page="false"
              size="medium"
            />
          </div>
        </div>
      </a-spin>
    </div>
    
    <!-- 删除确认对话框 -->
    <a-modal
      :visible="showDeleteConfirm"
      @cancel="showDeleteConfirm = false"
      @ok="confirmDelete"
      :ok-button-props="{ status: 'danger' }"
      :mask-closable="false"
      unmountOnClose
    >
      <template #title>
        确认删除文章
      </template>
      
      <div class="delete-confirm-content">
        <div class="delete-icon">⚠️</div>
        <div class="delete-message">
          <div class="delete-warning">您确定要删除以下文章吗？</div>
          <div class="delete-title">{{ postToDelete?.title }}</div>
          <div class="delete-note">此操作不可恢复，删除后数据将无法找回。</div>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<style scoped>
.my-posts-container {
  padding: 24px;
  background-color: var(--color-bg-2, #f5f5f5);
  min-height: 100%;
  background-color: #FFF7E9;
  background-image: url("data:image/svg+xml,%3Csvg width='80' height='80' viewBox='0 0 80 80' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='%23D6C6AF' fill-opacity='0.05'%3E%3Cpath d='M50 50c0-5.523 4.477-10 10-10s10 4.477 10 10-4.477 10-10 10c0 5.523-4.477 10-10 10s-10-4.477-10-10 4.477-10 10-10zM10 10c0-5.523 4.477-10 10-10s10 4.477 10 10-4.477 10-10 10c0 5.523-4.477 10-10 10S0 25.523 0 20s4.477-10 10-10zm10 8c4.418 0 8-3.582 8-8s-3.582-8-8-8-8 3.582-8 8 3.582 8 8 8zm40 40c4.418 0 8-3.582 8-8s-3.582-8-8-8-8 3.582-8 8 3.582 8 8 8z' /%3E%3C/g%3E%3C/svg%3E");
}

/* 头部区域 */
.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid #8C1F28;
}

.title-wrapper {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.page-title {
  font-size: 28px;
  font-weight: 500;
  color: #8C1F28;
  font-family: "STKaiti", "楷体", "FangSong", serif;
  letter-spacing: 2px;
  margin: 0;
}

.post-count {
  font-size: 16px;
  color: #A86032;
  font-family: "SimSun", "宋体", serif;
}

.create-btn {
  background-color: #8C1F28;
  border-color: #8C1F28;
  transition: all 0.3s ease;
  font-family: "STKaiti", "楷体", serif;
  height: 42px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 16px;
}

.create-btn:hover {
  background-color: #a52a2a;
  border-color: #a52a2a;
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(140, 31, 40, 0.2);
}

.btn-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 4px;
  background-color: rgba(255, 255, 255, 0.2);
  font-family: "STKaiti", "楷体", serif;
  font-weight: bold;
  font-size: 16px;
}

/* 内容区域 */
.content-area {
  position: relative;
  min-height: 300px; /* 确保有足够的高度显示加载动画 */
}

.centered-spin {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
}

:deep(.arco-spin) {
  width: 100%;
}

:deep(.arco-spin-loading) {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 10;
}

:deep(.arco-spin-mask) {
  background-color: rgba(255, 247, 233, 0.6);
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  background-color: #FFF7E9;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  position: relative;
}

.empty-state::before, 
.empty-state::after {
  content: "";
  position: absolute;
  left: 0;
  width: 100%;
  height: 10px;
}

.empty-state::before {
  top: 0;
  background: linear-gradient(90deg, #8C1F28, #A86032, #8C1F28);
  opacity: 0.8;
  border-radius: 8px 8px 0 0;
}

.empty-state::after {
  bottom: 0;
  background: linear-gradient(90deg, #8C1F28, #A86032, #8C1F28);
  opacity: 0.8;
  border-radius: 0 0 8px 8px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 20px;
  color: #582F0E;
  font-family: "STKaiti", "楷体", serif;
  margin-bottom: 24px;
  letter-spacing: 2px;
}

.empty-btn {
  background-color: #8C1F28;
  border-color: #8C1F28;
  transition: all 0.3s ease;
  font-family: "STKaiti", "楷体", serif;
  font-size: 16px;
  letter-spacing: 2px;
  height: 40px;
  padding: 0 24px;
}

.empty-btn:hover {
  background-color: #a52a2a;
  border-color: #a52a2a;
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(140, 31, 40, 0.2);
}

/* 帖子列表 - 网格布局 */
.posts-list {
  display: grid;
  grid-template-columns: repeat(1, 1fr);
  gap: 24px;
  margin-bottom: 24px;
}

.post-item {
  display: flex;
  flex-direction: column;
  background-color: #FFF7E9;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  border: 1px solid transparent;
  position: relative;
  background-image: linear-gradient(#FFF7E9, #F8ECD9);
}

.post-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
  border-color: #E4D9C3;
}

.post-item::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 8px;
  background: linear-gradient(90deg, #8C1F28, #A86032, #8C1F28);
  opacity: 0.8;
}

/* 封面 */
.post-cover {
  width: 100%;
  height: 180px;
  overflow: hidden;
  cursor: pointer;
  position: relative;
  background-color: #f5f5f5;
}

.image-placeholder {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, #f0f0f0 25%, #f8f8f8 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  z-index: 1;
}

@keyframes shimmer {
  0% {
    background-position: -200% 0;
  }
  100% {
    background-position: 200% 0;
  }
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease, opacity 0.3s ease;
  opacity: 0;
}

.img-loaded {
  opacity: 1;
  z-index: 2;
  position: relative;
}

.post-cover:hover .cover-img {
  transform: scale(1.05);
}

.cover-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: 3;
}

.post-cover:hover .cover-overlay {
  opacity: 1;
}

.view-text {
  padding: 6px 16px;
  background-color: rgba(255, 255, 255, 0.9);
  color: #1d2129;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.post-cover:hover .view-text:hover {
  background-color: #8C1F28;
  color: white;
}

/* 内容区 */
.post-content {
  flex: 1;
  padding: 16px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  border-bottom: 1px dashed #D6C6AF;
}

.post-header {
  display: flex;
  align-items: flex-start;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: space-between;
}

.post-title {
  font-size: 20px;
  font-weight: 500;
  color: #582F0E;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.4;
  flex: 1;
  min-width: 0;
  transition: color 0.2s ease;
  font-family: "STKaiti", "楷体", serif;
  position: relative;
  padding-left: 16px;
}

.post-title::before {
  content: "";
  position: absolute;
  left: 0;
  top: 6px;
  bottom: 6px;
  width: 4px;
  background-color: #8C1F28;
  border-radius: 2px;
}

.post-topic {
  padding: 2px 10px;
  background-color: rgba(140, 31, 40, 0.1);
  color: #8C1F28;
  border-radius: 12px;
  font-size: 13px;
  white-space: nowrap;
  border: 1px solid #8C1F28;
  font-family: "STKaiti", "楷体", serif;
}

.post-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: auto;
  padding-top: 12px;
}

.post-time {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #A86032;
  font-size: 13px;
  font-family: "SimSun", "宋体", serif;
}

.post-stats {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #A86032;
  font-size: 13px;
}

.meta-icon {
  font-size: 16px;
}

/* 操作区 */
.post-actions {
  display: flex;
  justify-content: flex-end;
  padding: 10px 16px;
  background-color: rgba(248, 236, 217, 0.5);
}

.post-actions :deep(.arco-btn) {
  transition: all 0.2s ease;
}

.btn-text {
  font-family: "STKaiti", "楷体", serif;
  font-size: 14px;
  font-weight: bold;
}

/* 分页 */
.pagination-wrapper {
  margin-top: 32px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  background-color: rgba(255, 247, 233, 0.8);
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  border: 1px solid #E4D9C3;
}

.page-summary {
  display: flex;
  gap: 16px;
  justify-content: center;
  align-items: center;
  margin-bottom: 8px;
}

.page-indicator {
  font-family: "STKaiti", "楷体", serif;
  color: #582F0E;
  font-size: 16px;
  position: relative;
}

.page-indicator::after {
  content: "";
  position: absolute;
  right: -10px;
  top: 50%;
  height: 14px;
  width: 1px;
  background-color: #D6C6AF;
  transform: translateY(-50%);
}

.total-indicator {
  font-family: "STKaiti", "楷体", serif;
  color: #A86032;
  font-size: 16px;
}

.pagination-total {
  color: #A86032;
  font-family: "SimSun", "宋体", serif;
}

:deep(.arco-pagination-item-active) {
  background-color: #8C1F28;
  border-color: #8C1F28;
  color: white;
}

:deep(.arco-pagination-item:hover) {
  color: #8C1F28;
  border-color: #8C1F28;
}

:deep(.arco-pagination-jumper-input:focus),
:deep(.arco-select-view:focus),
:deep(.arco-select-view:hover) {
  border-color: #8C1F28;
}

:deep(.arco-select-option-active),
:deep(.arco-select-option-hover) {
  background-color: rgba(140, 31, 40, 0.1);
}

:deep(.arco-select-option-selected) {
  color: #8C1F28;
  font-weight: bold;
}

:deep(.arco-pagination-options) {
  margin-left: 16px;
}

:deep(.arco-select-view-value) {
  color: #582F0E;
}

/* 删除确认对话框 */
.delete-confirm-content {
  display: flex;
  align-items: flex-start;
  padding: 16px 0;
}

.delete-icon {
  font-size: 24px;
  margin-right: 16px;
  margin-top: 2px;
}

.delete-message {
  flex: 1;
}

.delete-warning {
  font-weight: 500;
  margin-bottom: 8px;
}

.delete-title {
  background-color: #f7f8fa;
  padding: 8px 12px;
  border-radius: 4px;
  margin-bottom: 12px;
  font-weight: 500;
  color: #4e5969;
}

.delete-note {
  font-size: 13px;
  color: #86909c;
}

/* 响应式调整 */
@media (min-width: 768px) {
  .posts-list {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (min-width: 1200px) {
  .posts-list {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .my-posts-container {
    padding: 16px;
  }
  
  .header-section {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .create-btn {
    width: 100%;
  }
  
  .post-cover {
    height: 160px;
  }
  
  .post-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .post-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .post-stats {
    width: 100%;
    justify-content: space-between;
  }
}
</style> 