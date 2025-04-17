<script setup>
// 导入API和工具
import { ref, reactive, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { Message } from '@arco-design/web-vue';
import { IconSearch, IconEye, IconPlus, IconHeart, IconMessage, IconHeartFill } from '@arco-design/web-vue/es/icon';
import { getPostListAPI as getPostsAPI, getTopicsAPI, likePostAPI, unlikePostAPI } from '@/api/forum';
import { useUserStore } from '@/stores';
import { formatDate } from '@/utils/format';

const router = useRouter();
const userStore = useUserStore();

// 用户登录状态
const isLogin = computed(() => userStore.isLogin);

// 搜索和筛选
const searchText = ref('');
const activeTab = ref('all');

// 数据加载
const loading = ref(true);
const loadingMore = ref(false);
const pageNum = ref(1);
const pageSize = ref(10);
const total = ref(0);
const hasMore = computed(() => posts.value.length < total.value);

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
  posts.value = [];
  fetchPosts();
};

// 切换话题
const switchTopic = (topicId) => {
  activeTab.value = topicId;
  pageNum.value = 1;
  posts.value = [];
  fetchPosts();
};

// 获取帖子列表
const fetchPosts = async () => {
  if (pageNum.value === 1) {
    loading.value = true;
  } else {
    loadingMore.value = true;
  }
  
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: searchText.value,
    };
    
    // 添加话题筛选
    if (activeTab.value !== 'all') {
      params.topicId = activeTab.value;
    }
    
    const res = await getPostsAPI(params);
    if (res.code === 200) {
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
      });
      
      if (pageNum.value === 1) {
        posts.value = newPosts;
      } else {
        posts.value = [...posts.value, ...newPosts];
      }
      
      total.value = res.data.total || 0;
    } else {
      Message.warning(res.msg || '获取帖子列表失败');
    }
  } catch (err) {
    console.error('获取帖子列表出错:', err);
    Message.warning('获取帖子列表失败，请稍后重试');
  } finally {
    loading.value = false;
    loadingMore.value = false;
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

// 加载更多
const loadMore = () => {
  if (hasMore.value && !loadingMore.value) {
    pageNum.value++;
    fetchPosts();
  }
};

// 跳转到帖子详情
const goToDetail = (id) => {
  console.log('跳转到帖子详情:', id);
  router.push(`/forum/detail/${id}`);
};

// 创建新帖子
const createPost = () => {
  console.log('创建帖子, 登录状态:', isLogin.value);
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

    <!-- 主体区域 -->
    <div class="forum-body">
      <!-- 左侧话题栏 -->
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
                <img :src="post.authorAvatar || '@/assets/avatar/default-avatar.png'" alt="头像" class="author-avatar" />
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
                <div class="post-topic" v-if="post.topicName">
                  <span class="topic-tag">{{ post.topicName }}</span>
                </div>
                <div class="post-stats">
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
          
          <!-- 加载更多 -->
          <div class="more-button" v-if="hasMore">
            <a-button @click="loadMore" :loading="loadingMore">
              加载更多
            </a-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 调试按钮 -->
    <div class="debug-button" v-if="showDebugInfo" @click="toggleDebugInfo">
      {{ showDebugInfo ? '隐藏调试信息' : '显示调试信息' }}
    </div>
  </div>
</template>

<style scoped>
/* 主容器 */
.forum-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: "Microsoft YaHei", sans-serif;
}

/* 顶部区域 */
.forum-header {
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.forum-title {
  display: flex;
  flex-direction: column;
}

.main-title {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
}

.sub-title {
  font-size: 14px;
  color: #666;
}

.forum-search-post {
  display: flex;
  gap: 16px;
}

.search-box {
  display: flex;
  width: 300px;
}

.search-btn {
  margin-left: -1px;
  border-radius: 0 4px 4px 0;
  background-color: #C2101C;
  border-color: #C2101C;
}

.post-btn {
  background-color: #C2101C;
  border-color: #C2101C;
  display: flex;
  align-items: center;
  gap: 8px;
}

.post-btn:hover, .search-btn:hover {
  background-color: #A50E18;
  border-color: #A50E18;
}

/* 主体区域 */
.forum-body {
  display: flex;
  gap: 20px;
}

/* 左侧话题栏 */
.forum-sidebar {
  width: 200px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.topic-header {
  padding: 15px;
  font-size: 16px;
  font-weight: bold;
  color: #333;
  border-bottom: 1px solid #f0f0f0;
  background-color: #f9f9f9;
}

.topic-list {
  padding: 10px 0;
}

.topic-item {
  padding: 12px 15px;
  font-size: 14px;
  color: #333;
  cursor: pointer;
  transition: all 0.3s;
  border-left: 3px solid transparent;
}

.topic-item:hover {
  background-color: #f9f9f9;
  color: #C2101C;
}

.topic-item.active {
  background-color: #FFF0F0;
  color: #C2101C;
  border-left-color: #C2101C;
  font-weight: bold;
}

/* 右侧帖子列表 */
.forum-content {
  flex: 1;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  overflow: hidden;
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
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.3s;
}

.post-item:hover {
  background-color: #f9f9f9;
}

.post-info {
  flex: 1;
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
}

.author-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-right: 10px;
}

.post-time {
  font-size: 12px;
  color: #999;
}

.post-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin: 0 0 12px;
  line-height: 1.5;
}

.post-content {
  font-size: 14px;
  color: #666;
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
  border-radius: 8px;
  overflow: hidden;
  position: relative;
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
  background-color: rgba(0, 0, 0, 0.5);
  color: #fff;
  font-size: 18px;
  border-radius: 8px;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.topic-tag {
  padding: 4px 8px;
  background-color: #FFF0F0;
  color: #C2101C;
  font-size: 12px;
  border-radius: 4px;
}

.post-stats {
  display: flex;
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  color: #999;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.stat-item:hover {
  color: #C2101C;
}

.stat-item.liked {
  color: #C2101C;
}

.stat-item svg {
  margin-right: 5px;
}

.more-button {
  padding: 20px;
  text-align: center;
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
  }
  
  .forum-search-post {
    width: 100%;
    flex-direction: column;
  }
  
  .search-box {
    width: 100%;
  }
  
  .forum-body {
    flex-direction: column;
  }
  
  .forum-sidebar {
    width: 100%;
    margin-bottom: 16px;
  }
  
  .topic-list {
    display: flex;
    flex-wrap: wrap;
    padding: 10px;
    gap: 10px;
  }
  
  .topic-item {
    padding: 8px 12px;
    border-left: none;
    background-color: #f9f9f9;
    border-radius: the 4px;
  }
  
  .topic-item.active {
    background-color: #FFF0F0;
    border-left: none;
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
}

/* 调试按钮 */
.debug-button {
  position: fixed;
  bottom: 20px;
  right: 20px;
  padding: 10px 20px;
  background-color: #C2101C;
  color: #fff;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.debug-button:hover {
  background-color: #A50E18;
}
</style>