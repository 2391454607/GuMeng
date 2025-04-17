<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { Message } from '@arco-design/web-vue';
import { IconSearch, IconEye, IconPlus, IconHeart, IconMessage, IconHeartFill } from '@arco-design/web-vue/es/icon';
import { formatDate } from '@/utils/format';
import { getPostsListAPI, getPostsTopicListAPI } from "@/api/web/Web.js";

const router = useRouter();

// 搜索和筛选
const searchText = ref('');
const activeTab = ref('all');

// 数据加载
const loading = ref(true);

// 存储数据列表
const postsTopicList = ref();
const postsList = ref({
  id: "",
  title: "",
  content: " ",
  topic: "",
  images: "",
  commonNum: "",
  thumbsUpNum: "",
  viewCount: "",
  createTime: "",
  userId: "",
  username: "",
  avatar: "",
  isLiked: ""
});

//初始化数据
onMounted(() => {
  //获取帖子话题列表
  getPostsTopicListAPI().then((res)=>{
    if (res.code === 200) {
      postsTopicList.value = res.data;
    }
  })
  //获取帖子列表
  getPostsListAPI().then((res)=>{
    if (res.code === 200) {
      postsList.value = res.data.records;
    }
    console.log(res.data.records);
  })

  loading.value = false;
});

//处理点赞

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
          >
            <template #prefix>
              <icon-search />
            </template>
          </a-input>
          <a-button type="primary" class="search-btn">
            搜索
          </a-button>
        </div>
        <a-button type="primary" class="post-btn">
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
          >
            全部话题
          </div>
          <div 
            v-for="topic in postsTopicList"
            :key="topic.id"
            class="topic-item"
            :class="{ active: activeTab === topic.id }"
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

        <!-- 帖子列表 -->
        <div v-else class="post-list">
          <div v-for="records in postsList" :key="records.id" class="post-item">
            <!-- 帖子内容 -->
            <div class="post-info">
              <div class="post-author">
                <img :src="records.avatar || 'src/assets/image/gumeng.png'" alt="头像" class="author-avatar" />
                <span class="author-name">{{ records.username }}</span>
                <span class="post-time">{{ formatDate(records.createTime) }}</span>
              </div>

              <h3 class="post-title">{{ records.title }}</h3>
              <p class="post-content">{{ records.content }}</p>

              <!-- 帖子图片 -->
              <div v-if="records.images && records.images.length > 0" class="post-images">
                <div
                  v-for="(image, index) in records.images.slice(0, 3)"
                  :key="index"
                  class="post-image-wrapper"
                >
                  <img :src="image" :alt="`图片${index+1}`" class="post-image" />
                </div>
                <div v-if="records.images.length > 3" class="post-image-more">
                  +{{ records.images.length - 3 }}
                </div>
              </div>

              <!-- 帖子底部信息 -->
              <div class="post-footer">
                <div class="post-topic" v-if="records.topic">
                  <span class="topic-tag">{{ records.topic }}</span>
                </div>
                <div class="post-stats">
                  <span class="stat-item">
                    <icon-eye />
                    {{ records.viewCount || 0 }}
                  </span>
                  <span class="stat-item"  :class="{ 'liked': records.isLiked }">
                    <icon-heart-fill v-if="records.isLiked" />
                    <icon-heart v-else />
                    {{ records.thumbsUpNum || 0 }}
                  </span>
                  <span class="stat-item">
                    <icon-message />
                    {{ records.commonNum || 0 }}
                  </span>
                </div>
              </div>
            </div>
          </div>

        </div>
      </div>
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
  position: absolute;
  right: 120px;
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