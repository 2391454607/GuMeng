<script setup>
// 导入API和工具
import { ref, reactive, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { Message } from '@arco-design/web-vue';
import { IconSearch, IconEye, IconPlus, IconHeart, IconMessage, IconHeartFill } from '@arco-design/web-vue/es/icon';
import { getPostListAPI as getPostsAPI, getTopicsAPI, likePostAPI, unlikePostAPI } from '@/api/forum';
import { useUserStore } from '@/stores/userStore.js';
import { formatDate } from '@/utils/format';
import Footer from "@/views/web/layout/Footer.vue";

const router = useRouter();
const userStore = useUserStore();

// 用户登录状态
const isLogin = computed(() => userStore.isLogin);

// 搜索和筛选
const searchText = ref('');
const activeTab = ref('all');

// 数据加载
// 分页，一页分四条
const loading = ref(true);
const pageNum = ref(1);
const pageSize = ref(4);
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

// 处理搜索
const handleSearch = () => {
  pageNum.value = 1;
  fetchPosts();
};

// 切换话题
const switchTopic = (topicId) => {
  activeTab.value = topicId;
  pageNum.value = 1;
  fetchPosts();
};

// 获取帖子列表
const fetchPosts = async () => {
  loading.value = true;
  
  try {
    const params = {
      page: pageNum.value,
      size: pageSize.value,
      keyword: searchText.value,
    };
    
    // 添加话题筛选
    if (activeTab.value !== 'all') {
      // 查找选中话题的名称
      const selectedTopic = topics.value.find(t => t.id === activeTab.value);
      if (selectedTopic) {
        // 使用话题名称传递而非ID
        params.topic = selectedTopic.name;
      } else {
        params.topic = activeTab.value;
      }
    }
    
    // console.log('发送分页请求参数:', params);
    const res = await getPostsAPI(params);
    if (res.code === 200) {
      // console.log('获取到分页数据:', res.data);
      const newPosts = res.data.records || [];
      
      // 处理图片字段和确保评论数正确
      newPosts.forEach(post => {
        if (post.images && typeof post.images === 'string') {
          post.images = post.images.split(',').filter(img => img);
        } else {
          post.images = [];
        }
        
        // 确保点赞数和评论数不为空
        post.thumbsUpNum = post.thumbsUpNum || 0;
        post.commonNum = post.commonNum || 0;
        
        // 确保用户名和头像字段正确
        post.authorName = post.username || '匿名用户';
        post.authorAvatar = post.avatar || '/avatar/default-avatar.png';
      });
      
      posts.value = newPosts;
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
const handlePageChange = (page) => {
  pageNum.value = page;
  fetchPosts();
  // 滚动到顶部
  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  });
};

// 跳转到帖子详情
const goToDetail = (id) => {
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

// 监听搜索文本变化
watch(searchText, (newVal, oldVal) => {
  if (oldVal && !newVal) {
    // 清空搜索时重新加载
    handleSearch();
  }
});

onMounted(() => {
  fetchTopics();
  fetchPosts();
});
</script>

<template>
  <div class="forum-container">
    <!-- 顶部区域 -->
    <div class="forum-header">
      <div class="forum-title">
        <span class="main-title">非遗论坛</span>
        <span class="sub-title">传承非遗文化，共建交流社区</span>
      </div>
      <div class="forum-search-post">
        <div class="search-box">
          <a-input
            v-model="searchText"
            placeholder="搜索帖子..."
            allow-clear
            @press-enter="handleSearch"
            class="custom-input"
          >
            <template #prefix>
              <icon-search />
            </template>
          </a-input>
          <a-button type="primary" @click="handleSearch" class="search-btn">
            搜索
          </a-button>
        </div>
        <a-button type="primary" @click="createPost" class="post-btn">
          <icon-plus />
          发布帖子
        </a-button>
      </div>
    </div>

    <!-- 话题导航 -->
    <div class="topics-nav">
      <div 
        class="topic-item" 
        :class="{ active: activeTab === 'all' }"
        @click="switchTopic('all')"
      >
        全部话题
      </div>
      <div 
        v-for="topic in topics" 
        :key="topic.id"
        class="topic-item"
        :class="{ active: activeTab === topic.id }"
        @click="switchTopic(topic.id)"
      >
        {{ topic.name }}
      </div>
    </div>

    <!-- 主体区域 -->
    <div class="forum-body">
      <!-- 左侧话题栏 - 平板和电脑端显示 -->
      <div class="forum-sidebar">
        <div class="topic-header">话题分类</div>
        <div class="topic-list">
          <div 
            class="topic-item" 
            :class="{ active: activeTab === 'all' }"
            @click="switchTopic('all')"
          >
            全部话题
          </div>
          <div 
            v-for="topic in topics" 
            :key="topic.id"
            class="topic-item"
            :class="{ active: activeTab === topic.id }"
            @click="switchTopic(topic.id)"
          >
            {{ topic.name }}
          </div>
        </div>
      </div>

      <!-- 右侧帖子列表 -->
      <div class="forum-content">
        <!-- 加载状态 -->
        <div v-if="loading" class="loading-container">
          <a-skeleton :rows="10" animation />
        </div>
        
        <!-- 空状态 -->
        <div v-else-if="posts.length === 0" class="empty-container">
          <a-empty description="暂无帖子">
            <template #extra>
              <a-button type="primary" @click="createPost">立即发布</a-button>
            </template>
          </a-empty>
        </div>
        
        <!-- 帖子列表 -->
        <div v-else class="post-list">
          <div v-for="post in posts" :key="post.id" class="post-item" @click="goToDetail(post.id)">
            <!-- 帖子内容 -->
            <div class="post-info">
              <div class="post-author">
                <img :src="post.authorAvatar || '/avatar/default-avatar.png'" alt="头像" class="author-avatar" />
                <span class="author-name">{{ post.authorName }}</span>
                <span class="post-time">{{ formatDate(post.createTime) }}</span>
              </div>
              <h3 class="post-title">{{ post.title }}</h3>
              <p class="post-content">{{ post.content }}</p>
              
              <!-- 帖子图片 -->
              <div v-if="post.images && post.images.length > 0" class="post-images">
                <div 
                  v-for="(image, index) in post.images.slice(0, 3)" 
                  :key="index" 
                  class="post-image-wrapper"
                >
                  <img :src="image" :alt="`图片${index+1}`" class="post-image" />
                </div>
                <div v-if="post.images.length > 3" class="post-image-more">
                  +{{ post.images.length - 3 }}
                </div>
              </div>
              
              <!-- 帖子底部信息 -->
              <div class="post-footer">
                <div class="post-stats">
                  <span class="stat-item topic-badge">
                    <span class="topic-tag">{{ post.topic }}</span>
                  </span>
                  <span class="stat-item">
                    <icon-eye />
                    {{ post.viewCount || 0 }}
                  </span>
                  <span class="stat-item" @click.stop="handleLikePost(post)" :class="{ 'liked': post.isLiked }">
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
          
          <!-- 分页 -->
          <div class="pagination-container" v-if="total > 0">
            <a-pagination
              :current="pageNum"
              :total="total"
              :page-size="pageSize"
              show-total
              show-jumper
              @change="handlePageChange"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- 调试按钮 -->
    <div class="debug-button" v-if="showDebugInfo" @click="toggleDebugInfo">
      {{ showDebugInfo ? '隐藏调试信息' : '显示调试信息' }}
    </div>
    
    <!-- 页脚 -->
    <Footer class="footer"></Footer>
  </div>
</template>

<style scoped>
/* 主容器 */
.forum-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: "SimSun", "宋体", serif;
  background-color: #F9F3E9;
  min-height: calc(100vh - 64px);
}

/* 顶部区域 */
.forum-header {
  background-color: #8C1F28;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #F9F3E9;
  position: relative;
}

/* 模拟中国传统纹饰 */
.forum-header::before {
  content: none;
}

.forum-title {
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 1;
}

.main-title {
  font-size: 28px;
  font-weight: bold;
  color: #FFFBF0;
  margin-bottom: 8px;
  letter-spacing: 3px;
  text-shadow: 1px 1px 3px rgba(0, 0, 0, 0.3);
  font-family: "STKaiti", "楷体", serif;
}

.sub-title {
  font-size: 14px;
  color: #F9F3E9;
  letter-spacing: 1px;
}

.forum-search-post {
  display: flex;
  gap: 16px;
  position: relative;
  z-index: 1;
}

.search-box {
  display: flex;
  width: 300px;
}

.custom-input {
  background-color: rgba(249, 243, 233, 0.9) !important;
  border: 1px solid #D6C6AF !important;
  border-radius: 4px 0 0 4px !important;
  color: #582F0E !important;
}

.search-btn {
  margin-left: -1px;
  border-radius: 0 4px 4px 0;
  background-color: #582F0E;
  border-color: #582F0E;
  color: #F9F3E9;
}

.post-btn {
  background-color: #582F0E;
  border-color: #582F0E;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #F9F3E9;
  border-radius: 4px;
}

.post-btn:hover, .search-btn:hover {
  background-color: #7F4F24;
  border-color: #7F4F24;
  color: #F9F3E9;
}

/* 话题导航 - 移动端 */
.topics-nav {
  display: none;
  background-color: #F1EDDA;
  border-radius: 4px;
  padding: 10px;
  margin-bottom: 20px;
  overflow-x: auto;
  white-space: nowrap;
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE and Edge */
  border: 1px solid #D6C6AF;
}

.topics-nav::-webkit-scrollbar {
  display: none; /* Chrome, Safari, Opera */
}

/* 主体区域 */
.forum-body {
  display: flex;
  gap: 20px;
}

/* 左侧话题栏 */
.forum-sidebar {
  width: 200px;
  background-color: #FFF7E9;
  border-radius: 4px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  border: 1px solid #D6C6AF;
}

.topic-header {
  padding: 15px;
  font-size: 16px;
  font-weight: bold;
  color: #582F0E;
  border-bottom: 1px solid #D6C6AF;
  background-color: #F1EDDA;
  text-align: center;
  letter-spacing: 1px;
  font-family: "STKaiti", "楷体", serif;
}

.topic-list {
  padding: 10px 0;
}

.topic-item {
  padding: 12px 15px;
  font-size: 14px;
  color: #582F0E;
  cursor: pointer;
  transition: all 0.3s;
  border-left: 3px solid transparent;
}

.topic-item:hover {
  background-color: #F9F5EB;
  color: #8C1F28;
}

.topic-item.active {
  background-color: #FFF0E5;
  color: #8C1F28;
  border-left-color: #8C1F28;
  font-weight: bold;
}

/* 右侧帖子列表 */
.forum-content {
  flex: 1;
  background-color: #FFFBF0;
  border-radius: 4px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  border: 1px solid #D6C6AF;
}

.loading-container, .empty-container {
  padding: 40px;
  text-align: center;
}

.post-list {
  padding: 0;
}

.post-item {
  padding: 20px;
  border-bottom: 1px solid #E4D9C3;
  cursor: pointer;
  transition: background-color 0.3s;
  position: relative;
}

.post-item:hover {
  background-color: #FFF7E9;
}

/* 仿古纸张样式 */
.post-item::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: url("data:image/svg+xml,%3Csvg width='100' height='100' viewBox='0 0 100 100' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='%23D6C6AF' fill-opacity='0.06'%3E%3Cpath d='M11 18c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm48 25c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm-43-7c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm63 31c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM34 90c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm56-76c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM12 86c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm28-65c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm23-11c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-6 60c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm29 22c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zM32 63c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm57-13c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-9-21c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM60 91c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM35 41c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM12 60c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2z'/%3E%3C/g%3E%3C/svg%3E");
  opacity: 0.1;
  z-index: 0;
  pointer-events: none;
}

.post-info {
  flex: 1;
  position: relative;
  z-index: 1;
}

.post-author {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.author-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
  margin-right: 10px;
  border: 2px solid #E4D9C3;
}

.author-name {
  font-size: 14px;
  font-weight: 500;
  color: #582F0E;
  margin-right: 10px;
}

.post-time {
  font-size: 12px;
  color: #7F4F24;
}

.post-title {
  font-size: 18px;
  font-weight: bold;
  color: #582F0E;
  margin: 0 0 12px;
  line-height: 1.5;
  font-family: "STKaiti", "楷体", serif;
}

.post-content {
  font-size: 14px;
  color: #6B4F2E;
  margin: 0 0 15px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.6;
}

.post-images {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.post-image-wrapper {
  width: 120px;
  height: 120px;
  border-radius: 4px;
  overflow: hidden;
  position: relative;
  border: 2px solid #E4D9C3;
}

.post-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.post-image-wrapper:hover .post-image {
  transform: scale(1.05);
}

.post-image-more {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 120px;
  height: 120px;
  background-color: rgba(88, 47, 14, 0.6);
  color: #FFF7E9;
  font-size: 18px;
  border-radius: 4px;
  border: 2px solid #E4D9C3;
}

.post-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

.post-stats {
  display: flex;
  gap: 16px;
  align-items: center;
}

.stat-item {
  display: flex;
  align-items: center;
  color: #7F4F24;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.stat-item:hover {
  color: #8C1F28;
}

.stat-item.liked {
  color: #8C1F28;
}

.stat-item svg {
  margin-right: 5px;
}

.topic-badge {
  cursor: default;
}

.topic-badge:hover {
  color: #7F4F24;
}

.topic-tag {
  padding: 4px 8px;
  background-color: #FBF0E9;
  color: #8C1F28;
  font-size: 12px;
  border-radius: 4px;
  border: 1px solid #E4D9C3;
}

.pagination-container {
  padding: 20px;
  text-align: center;
  display: flex;
  justify-content: center;
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
  transition: background-color 0.3s;
}

.debug-button:hover {
  background-color: #A52A2A;
}

/* 响应式设计 */
@media screen and (max-width: 768px) {
  .forum-container {
    padding: 10px;
  }
  
  .forum-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
    padding: 15px;
  }
  
  .forum-search-post {
    width: 100%;
    flex-direction: column;
  }
  
  .search-box {
    width: 100%;
    margin-bottom: 10px;
  }
  
  .post-btn {
    width: 100%;
  }
  
  /* 显示水平话题导航，隐藏侧边栏 */
  .topics-nav {
    display: flex;
    gap: 10px;
  }
  
  .forum-sidebar {
    display: none;
  }
  
  .post-images {
    flex-wrap: wrap;
  }
  
  .post-image-wrapper {
    width: calc(33.333% - 7px);
    height: 100px;
  }
  
  .post-image-more {
    width: calc(33.333% - 7px);
    height: 100px;
  }
  
  .pagination-container :deep(.arco-pagination-jumper) {
    display: none;
  }
}

.footer{
  display: flex;
  bottom: 0;
}
</style>