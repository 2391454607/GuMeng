<script setup>
// 导入API和工具
import { ref, reactive, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { Message } from '@arco-design/web-vue';
import { IconSearch, IconEye, IconPlus, IconHeart, IconMessage, IconHeartFill, IconLoading } from '@arco-design/web-vue/es/icon';
import { getPostListAPI as getPostsAPI, getTopicsAPI, likePostAPI, unlikePostAPI } from '@/api/forum';
import { useUserStore } from '@/stores/userStore.js';
import { formatDate } from '@/utils/format';
import Footer from "@/views/web/layout/Footer.vue";
// import { Viewer } from '@/views/web/pages/forum/bytemd';
// import gfm from '@bytemd/plugin-gfm'
// import highlight from '@bytemd/plugin-highlight'
// import gemoji from '@bytemd/plugin-gemoji'

// import 'bytemd/dist/index.css'

const router = useRouter();
const userStore = useUserStore();


// const plugins = [
//   gfm(),
//   highlight(),
//   gemoji(),
// ]

// 用户登录状态
const isLogin = computed(() => userStore.isLogin);

// 搜索和筛选
const searchText = ref('');
const activeTab = ref('all');

// 数据加载
// 分页，一页分12条
const loading = ref(true);
const pageNum = ref(1);
const pageSize = ref(12);
const total = ref(0);

// 数据
const posts = ref([]);
const topics = ref([]);

// 点赞防抖控制 - 防止重复点击
const likingPosts = ref(new Set());

// 调试信息
const showDebugInfo = ref(false); // 控制是否显示调试信息
const toggleDebugInfo = () => {
  showDebugInfo.value = !showDebugInfo.value;
};

// 测试后端API
const testBackendConnection = async () => {
  try {
    console.log('测试后端连接...');
    const res = await http.get('/forum/debug/token');
    console.log('后端连接测试结果:', res);
    Message.success('后端连接成功');
  } catch (err) {
    console.error('后端连接测试失败:', err);
    Message.error('后端连接失败');
  }
};

// 添加防抖函数
const debounce = (fn, delay = 300) => {
  let timer = null;
  return function(...args) {
    if (timer) clearTimeout(timer);
    timer = setTimeout(() => {
      fn.apply(this, args);
    }, delay);
  };
};

// 处理搜索
const handleSearch = debounce(() => {
  pageNum.value = 1;
  // 添加延迟，先设置loading状态，然后再请求数据
  loading.value = true;
  setTimeout(() => {
    fetchPosts();
  }, 300);
}, 300);

// 切换话题
const switchTopic = debounce((topicId) => {
  activeTab.value = topicId;
  pageNum.value = 1;
  // 添加延迟，先设置loading状态，然后再请求数据
  loading.value = true;
  setTimeout(() => {
    fetchPosts();
  }, 300);
}, 200);

// 获取帖子列表
const fetchPosts = async () => {
  try {
    const params = {
      page: pageNum.value,
      size: pageSize.value,
      keyword: searchText.value,
    };
    
    if (activeTab.value !== 'all') {
      const selectedTopic = topics.value.find(t => t.id === activeTab.value);
      if (selectedTopic) {
        params.topic = selectedTopic.name;
      } else {
        params.topic = activeTab.value;
      }
    }
    
    const res = await getPostsAPI(params);
    if (res.code === 200) {
      const newPosts = res.data.records || [];
      
      // 使用批量处理减少重排重绘
      const processedPosts = newPosts.map(post => {
        // 图片处理
        const images = typeof post.images === 'string' 
          ? post.images.split(',').filter(img => img) 
          : [];
        
        // 内容处理 - 优化正则表达式
        let previewText = '';
        if (post.content) {
          // 首先检查是否是HTML内容
          const isHtmlContent = post.content.includes('<p>') || 
                                post.content.includes('<div>') || 
                                post.content.includes('<h') || 
                                post.content.includes('<span>');
          
          let processedContent = post.content;
          
          if (isHtmlContent) {
            // 1. 移除所有HTML标签
            processedContent = processedContent
              .replace(/<img.*?>/g, '[图片]') // 替换图片标签为[图片]文本
              .replace(/<[^>]*>/g, ' ') // 移除其他所有HTML标签
              .replace(/&nbsp;/g, ' ') // 替换HTML空格
              .replace(/\s{2,}/g, ' ') // 合并多个空格为一个
              .trim();
          } else {
            // 处理Markdown内容
            processedContent = processedContent
              .replace(/!\[.*?\]\((undefined|.*?undefined.*?)\)/g, '') // 移除无效图片
              .replace(/!\[.*?\]\(.*?\)/g, '[图片]') // 替换图片标记
              .replace(/\n{3,}/g, '\n\n') // 合并多个换行
              .replace(/#{1,6}\s+/g, '') // 移除标题标记
              .replace(/(\*\*|__)(.*?)(\*\*|__)/g, '$2') // 移除加粗标记
              .replace(/(\*|_)(.*?)(\*|_)/g, '$2') // 移除斜体标记
              .replace(/`{1,3}(.*?)`{1,3}/g, '$1') // 移除代码标记
              .replace(/\[([^\]]+)\]\(([^)]+)\)/g, '$1'); // 将链接替换为纯文本
          }
          
          // 截取预览文本
          previewText = processedContent.length > 150 
            ? processedContent.slice(0, 150) + '...' 
            : processedContent;
        }
        
        return {
          ...post,
          images,
          thumbsUpNum: post.thumbsUpNum || 0,
          commonNum: post.commonNum || 0,
          isLiked: !!post.isLiked,
          authorName: post.username || '匿名用户',
          authorAvatar: post.avatar || '/image/gumeng.png',
          previewText,
        };
      });
      
      // 一次性更新状态，减少重渲染
      posts.value = processedPosts;
      total.value = res.data.total || 0;
    } else {
      Message.warning(res.msg || '获取帖子列表失败');
    }
  } catch (err) {
    console.error('获取帖子列表出错:', err);
    Message.warning('获取帖子列表失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 获取话题列表
const fetchTopics = async () => {
  try {
    const res = await getTopicsAPI();
    if (res.code === 200) {
      topics.value = res.data || [];
    }
  } catch (err) {
    console.error('获取话题列表出错:', err);
  }
};

// 处理分页变化
const handlePageChange = debounce((page) => {
  pageNum.value = page;
  // 添加延迟，先设置loading状态，然后再请求数据
  loading.value = true;
  setTimeout(() => {
    fetchPosts();
  }, 300);
  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  });
}, 200);

// 跳转到帖子详情
const goToDetail = (id) => {
  // 检查用户是否已登录
  if (!isLogin.value) {
    // 未登录时，显示提示并重定向到登录页面
    Message.warning('请先登录后查看帖子详情');
    router.push('/login');
    return;
  }
  
  // 已登录则正常跳转到帖子详情页
  router.push(`/forum/detail/${id}`);
};

// 创建新帖子
const createPost = () => {
  if (!isLogin.value) {
    Message.warning('请先登录再发布帖子');
    return;
  }
  router.push('/forum/create');
};

// 处理帖子点赞
const handleLikePost = async (post) => {
  if (!isLogin.value) {
    Message.warning('请先登录再进行操作');
    return;
  }
  
  // 防止重复点击
  if (likingPosts.value.has(post.id)) {
    return;
  }
  
  likingPosts.value.add(post.id);
  
  try {
    let res;
    if (post.isLiked) {
      res = await unlikePostAPI(post.id);
      if (res.code === 200) {
        post.isLiked = false;
        post.thumbsUpNum = Math.max(0, (post.thumbsUpNum || 1) - 1);
        Message.success('已取消点赞');
      }
    } else {
      res = await likePostAPI(post.id);
      if (res.code === 200) {
        post.isLiked = true;
        post.thumbsUpNum = (post.thumbsUpNum || 0) + 1;
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
    likingPosts.value.delete(post.id);
  }
};

// 优化搜索文本监听
watch(searchText, debounce((newVal, oldVal) => {
  if (oldVal && !newVal) {
    handleSearch();
  }
}, 500));

onMounted(() => {
  fetchTopics();
  fetchPosts();
});
</script>

<template>
  <div>
    <!-- 顶部图片标题区域 -->
    <div class="project-title">
      <div class="title-content">
        <h1>非遗论坛</h1>
        <p>传承非遗文化 · 共建交流社区</p>
      </div>
    </div>

    <div class="forum-container">
      <!-- 搜索和发布区域 -->
      <div class="search-section">
        <div class="search-wrapper">
          <icon-search class="search-icon"/>
          <input 
            v-model="searchText"
            type="text" 
            placeholder="搜索您感兴趣的话题..." 
            class="search-input"
            @keyup.enter="handleSearch"
          >
        </div>
        <a-button type="primary" @click="createPost" class="post-btn">
          <icon-plus />
          发布帖子
        </a-button>
      </div>

      <!-- 话题分类区域 -->
      <div class="categories-section">
        <div class="category-group">
          <span class="category-name">话题分类</span>
          <div class="category-items">
            <span 
              class="category-item" 
              :class="{ 'category-item-active': activeTab === 'all' }"
              @click="switchTopic('all')"
            >
              全部话题
            </span>
            <span 
              v-for="topic in topics" 
              :key="topic.id"
              class="category-item"
              :class="{ 'category-item-active': activeTab === topic.id }"
              @click="switchTopic(topic.id)"
            >
              {{ topic.name }}
            </span>
          </div>
        </div>
      </div>

      <!-- 内容包装器，固定高度防止闪烁 -->
      <div class="content-wrapper">
        <a-spin :loading="loading" :size="32" tip="加载中">
          <template #icon><icon-loading /></template>
          
          <!-- 空状态显示 -->
          <div v-if="posts.length === 0 && !loading" class="empty-state">
            <a-empty description="暂无符合条件的帖子">
              <template #extra>
                <a-button type="primary" @click="createPost">立即发布</a-button>
              </template>
            </a-empty>
          </div>
          
          <!-- 帖子网格布局 -->
          <div v-else class="post-grid">
            <div v-for="post in posts" :key="post.id" class="post-card" @click="goToDetail(post.id)">
              <!-- 帖子图片区域 -->
              <div class="post-image" v-if="post.images && post.images.length > 0">
                <img 
                  :src="post.images[0]" 
                  :alt="post.title"
                  @error="(e) => e.target.src = '/image/gumeng.png'"
                  loading="lazy"
                >
                <div class="post-overlay" @click.stop="goToDetail(post.id)">
                  <div class="overlay-content">
                    <icon-eye class="overlay-icon" />
                    <span>查看详情</span>
                  </div>
                </div>
              </div>
              <div class="post-image" v-else>
                <img 
                  src="/image/gumeng.png" 
                  :alt="post.title"
                  loading="lazy"
                >
                <div class="post-overlay" @click.stop="goToDetail(post.id)">
                  <div class="overlay-content">
                    <icon-eye class="overlay-icon" />
                    <span>查看详情</span>
                  </div>
                </div>
              </div>
              
              <!-- 帖子内容区域 -->
              <div class="post-content">
                <h3>{{ post.title }}</h3>
                
                <div class="post-info-row">
                  <!-- 用户信息区域 -->
                  <div class="author-info">
                    <div class="author-avatar">
                      <img 
                        :src="post.authorAvatar" 
                        :alt="post.authorName"
                        @error="(e) => e.target.src = '/image/gumeng.png'"
                      >
                    </div>
                    <span class="author-name">{{ post.authorName }}</span>
                  </div>
                  
                  <div class="post-tags">
                    <a-tag>{{ post.topic }}</a-tag>
                  </div>
                </div>
                
                <div class="post-card-bottom">
                  <div class="post-stats">
                    <span class="stat-item">
                      <icon-eye />
                      {{ post.viewCount || 0 }}
                    </span>
                    <span 
                      class="stat-item" 
                      @click.stop="handleLikePost(post)" 
                      :class="{ 'liked': post.isLiked }"
                    >
                      <icon-heart-fill v-if="post.isLiked" />
                      <icon-heart v-else />
                      {{ post.thumbsUpNum || 0 }}
                    </span>
                    <span class="stat-item">
                      <icon-message />
                      {{ post.commonNum || 0 }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </a-spin>
      </div>
      
      <!-- 分页器 -->
      <div v-if="!loading && total > 0" class="pagination-container">
        <span class="page-indicator">第 {{ pageNum }} 页</span>
        <a-pagination
          :total="total"
          :current="pageNum"
          :page-size="pageSize"
          @change="handlePageChange"
          :hide-on-single-page="false"
        />
      </div>

      <!-- 调试按钮 -->
      <div class="debug-button" v-if="showDebugInfo" @click="toggleDebugInfo">
        {{ showDebugInfo ? '隐藏调试信息' : '显示调试信息' }}
      </div>
    </div>
    
    <!-- 页脚 -->
    <Footer class="footer"></Footer>
  </div>
</template>

<style scoped>
/* 顶部图片标题区域 */
.project-title {
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-image: url('/image/forumPost.webp');
  background-size: 100% 100%;
  background-position: center;
  background-repeat: no-repeat;
  position: relative;
}

.title-content {
  text-align: center;
  padding: 10px;
  z-index: 2;
  width: 100%;
}

.title-content h1 {
  font-size: clamp(2em, 4vw, 2.8em);
  color: #8C1F28;
  margin-bottom: 8px;
  font-weight: bold;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
  font-family: "STKaiti", "楷体", serif;
  position: relative;
  display: inline-block;
  animation: titleFadeIn 1.2s ease-out forwards;
}

.title-content p {
  font-size: clamp(1em, 2vw, 1.2em);
  color: #594433;
  font-family: "STFangsong", "仿宋", serif;
  letter-spacing: 2px;
  margin-top: 0;
  animation: subtitleFadeIn 1.5s ease-out forwards;
  position: relative;
}

/* 添加动画关键帧 */
@keyframes titleFadeIn {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes subtitleFadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 主容器 */
.forum-container {
  padding: 20px;
  max-width: 1200px;
  min-height: calc(100vh - 169px);
  margin: 0 auto;
  font-family: "SimSun", "宋体", serif;
}

/* 添加内容包装器，固定高度，防止闪烁 */
.content-wrapper {
  min-height: 500px;
  position: relative;
}

/* 搜索区域样式 */
.search-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-wrapper {
  position: relative;
  width: 100%;
  max-width: 600px;
  display: flex;
  align-items: center;
}

.search-icon {
  position: absolute;
  left: 15px;
  top: 50%;
  transform: translateY(-50%);
  color: #8C1F28;
  font-size: 20px;
}

.search-input {
  width: 100%;
  padding: 12px 20px 12px 45px;
  border: 2px solid #8C1F28;
  border-radius: 24px;
  font-size: 16px;
  transition: all 0.3s ease;
  background-color: #fff;
}

.search-input:focus {
  outline: none;
  border-color: #582F0E;
  box-shadow: 0 0 8px rgba(140, 31, 40, 0.3);
}

.search-input::placeholder {
  color: #999;
}

.post-btn {
  background-color: #8C1F28;
  border-color: #8C1F28;
  height: 40px;
  display: flex;
  align-items: center;
  gap: 8px;
  border-radius: 20px;
  padding: 0 20px;
  font-size: 16px;
  transition: all 0.3s ease;
}

.post-btn:hover {
  background-color: #582F0E;
  border-color: #582F0E;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

/* 分类区域样式 */
.categories-section {
  margin: 20px 0;
}

.category-group {
  margin-bottom: 15px;
}

.category-name {
  font-weight: bold;
  margin-right: 15px;
  color: #8C1F28;
  font-size: 16px;
}

.category-items {
  display: inline-flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 10px;
}

.category-item {
  padding: 6px 16px;
  background: #fff;
  border: 1px solid #8C1F28;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s;
  color: #8C1F28;
  font-size: 14px;
}

.category-item-active {
  background: #8C1F28;
  color: #fff;
}

.category-item:hover {
  background: #8C1F28;
  color: #fff;
  transform: translateY(-2px);
}

/* 空状态样式 */
.empty-state {
  text-align: center;
  padding: 50px 0;
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

:deep(.arco-empty) {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

:deep(.arco-empty-image) {
  margin-bottom: 16px;
  color: #8C1F28;
}

:deep(.arco-empty-description) {
  color: #8C1F28;
  font-size: 16px;
  margin-bottom: 16px;
}

/* 帖子网格布局 */
.post-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin: 20px auto;
  width: 100%;
}

.post-card {
  border: 1px solid #E4D9C3;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s;
  background: #fff;
  height: 100%;
  display: flex;
  flex-direction: column;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.post-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
  border-color: #8C1F28;
}

.post-image {
  height: 0;
  padding-bottom: 66.67%;
  overflow: hidden;
  position: relative;
}

.post-image img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
  transition: transform 0.5s ease;
}

.post-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(33, 33, 33, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: all 0.3s ease;
}

.post-card:hover .post-overlay {
  opacity: 1;
}

.overlay-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: white;
  transform: translateY(20px);
  transition: all 0.3s ease;
}

.post-card:hover .overlay-content {
  transform: translateY(0);
}

.overlay-icon {
  font-size: 24px;
  margin-bottom: 8px;
}

.post-card:hover .post-image img {
  transform: scale(1.05);
}

/* 作者信息样式 */
.author-info {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.author-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  overflow: hidden;
  margin-right: 8px;
  border: 1px solid #8C1F28;
  box-shadow: 0 2px 4px rgba(140, 31, 40, 0.2);
}

.author-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.author-name {
  font-size: 13px;
  color: #8C1F28;
  font-weight: bold;
  font-family: "STKaiti", "楷体", serif;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 80px;
  line-height: 1.2;
}

.post-content {
  padding: 16px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.post-content h3 {
  margin: 0 0 12px 0;
  font-size: 16px;
  color: #582F0E;
  font-weight: bold;
  line-height: 1.4;
  font-family: "STKaiti", "楷体", serif;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  min-height: 44px;
}

.post-info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  width: 100%;
}

.post-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-left: 10px;
  max-width: 60%;
}

.post-tags :deep(.arco-tag) {
  border-color: #8C1F28;
  color: #8C1F28;
  background: #FFF7E9;
  white-space: normal;
  line-height: 1.2;
  font-size: 12px;
  padding: 2px 6px;
  height: auto;
  max-width: 100%;
  word-break: break-word;
}

.post-card-bottom {
  margin-top: auto;
  padding-top: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.post-stats {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
  color: #7F4F24;
  font-size: 12px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  transition: all 0.2s;
}

.stat-item.liked {
  color: #8C1F28;
}

.stat-item:hover {
  color: #8C1F28;
}

/* 分页容器 */
.pagination-container {
  padding: 24px;
  text-align: center;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
}

.page-indicator {
  color: #8C1F28;
  font-weight: bold;
  font-size: 14px;
  padding: 5px 10px;
  background-color: #FFF7E9;
  border-radius: 4px;
}

/* 调试按钮 */
.debug-button {
  position: fixed;
  bottom: 20px;
  right: 20px;
  padding: 10px 20px;
  background-color: #8C1F28;
  color: #fff;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
  z-index: 100;
}

.debug-button:hover {
  background-color: #582F0E;
}

.footer {
  display: flex;
  bottom: 0;
}

/* 加载动画样式 */
:deep(.arco-spin) {
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

:deep(.arco-spin-loading) {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: rgba(255, 247, 233, 0.8);
  padding: 20px;
  border-radius: 8px;
  z-index: 10;
}

:deep(.arco-spin-icon) {
  color: #8C1F28;
  font-size: 24px;
}

:deep(.arco-spin-tip) {
  color: #8C1F28;
  font-size: 14px;
  margin-top: 8px;
}

/* 响应式设计 */
@media screen and (max-width: 1200px) {
  .post-grid {
    grid-template-columns: repeat(3, 1fr);
  }
  
  .author-name {
    max-width: 100px;
  }
}

@media screen and (max-width: 992px) {
  .post-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .author-name {
    max-width: 120px;
  }
}

@media screen and (max-width: 768px) {
  .project-title {
    min-height: 150px;
  }
  
  .title-content h1 {
    font-size: 1.8em;
  }
  
  .title-content p {
    font-size: 0.9em;
  }
  
  .forum-container {
    padding: 15px;
  }
  
  .search-section {
    flex-direction: column;
    gap: 15px;
  }
  
  .search-wrapper {
    width: 100%;
  }
  
  .post-btn {
    width: 100%;
  }
  
  .category-items {
    overflow-x: auto;
    flex-wrap: nowrap;
    padding-bottom: 10px;
    width: 100%;
  }
  
  .category-item {
    white-space: nowrap;
  }
  
  .post-grid {
    grid-template-columns: 1fr;
  }
  
  .post-image {
    padding-bottom: 56.25%;
  }
  
  .post-info-row {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .post-tags {
    margin-left: 0;
    margin-top: 8px;
  }
  
  .author-info {
    width: 100%;
    margin-bottom: 8px;
  }
  
  .author-name {
    max-width: 160px;
  }
}
</style>