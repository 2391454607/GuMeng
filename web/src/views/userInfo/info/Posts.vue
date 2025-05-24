<script setup>
import { ref, onMounted } from 'vue';
import { Message } from '@arco-design/web-vue';
import { getMyPostsAPI, deletePostAPI } from '@/api/forum';
import { useRouter } from 'vue-router';
import { formatDate } from '@/utils/format';

const router = useRouter();
const posts = ref([]);
const loading = ref(false);
const pagination = ref({
  current: 1,
  pageSize: 4,
  total: 0
});

// 删除确认框
const showDeleteConfirm = ref(false);
const postToDelete = ref(null);

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

// 获取我的帖子列表
const fetchMyPosts = async () => {
  loading.value = true;
  try {
    const res = await getMyPostsAPI({
      page: pagination.value.current,
      size: pagination.value.pageSize,
      userId: 'current' // 显式请求当前用户的帖子
    });
    
    console.log('获取我的帖子响应:', res);
    
    if (res.code === 200) {
      // 处理返回数据
      if (res.data && res.data.records) {
        posts.value = res.data.records;
        pagination.value.total = res.data.total || 0;
      } else {
        posts.value = res.data || [];
        pagination.value.total = posts.value.length;
      }
      console.log('处理后的帖子列表:', posts.value);
    } else {
      Message.error(res.msg || '获取帖子列表失败');
    }
  } catch (error) {
    console.error('获取我的帖子出错:', error);
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
    console.error('删除帖子出错:', error);
    Message.error('删除失败，请稍后重试');
  } finally {
    showDeleteConfirm.value = false;
    postToDelete.value = null;
  }
};

// 分页变化
const handlePageChange = (page) => {
  pagination.value.current = page;
  fetchMyPosts();
};

// 组件挂载后加载数据
onMounted(() => {
  fetchMyPosts();
});
</script>

<template>
  <div class="my-posts">
    <div class="section-header">
      <h2 class="page-title">我的帖子</h2>
      <a-button type="primary" @click="router.push('/forum/create')" class="new-post-btn">
        <span class="btn-content">
          <span class="btn-icon">撰</span>
          <span>发布新帖</span>
        </span>
      </a-button>
    </div>
    
    <a-spin :loading="loading">
      <div v-if="posts.length === 0" class="empty-data">
        <div class="empty-scroll">
          <div class="scroll-content">
            <p class="empty-text">您尚未发表任何帖子</p>
            <a-button type="primary" @click="router.push('/forum/create')" class="empty-btn">
              即刻挥毫
            </a-button>
          </div>
        </div>
      </div>
      
      <div v-else class="posts-list">
        <div v-for="post in posts" :key="post.id" class="post-card">
          <div class="post-content" @click="viewPost(post)">
            <div class="post-header">
              <h3 class="post-title">{{ post.title }}</h3>
              <div class="post-topic" v-if="post.topic">{{ post.topic }}</div>
            </div>
            
            <div class="post-desc">
              {{ getPlainText(post.content).substr(0, 150) + (post.content.length > 150 ? '...' : '') }}
            </div>
            
            <div class="post-info">
              <span class="post-time">{{ formatDate(post.createTime) }}</span>
              
              <div class="post-stats">
                <span class="stat-item">
                  <i class="icon icon-view"></i>
                  <span>{{ post.viewCount || 0 }}</span>
                </span>
                <span class="stat-item">
                  <i class="icon icon-like"></i>
                  <span>{{ post.thumbsUpNum || 0 }}</span>
                </span>
                <span class="stat-item">
                  <i class="icon icon-comment"></i>
                  <span>{{ post.commentCount || 0 }}</span>
                </span>
              </div>
            </div>
          </div>
          
          <div class="post-actions">
            <div class="action-btn view-btn" @click.stop="viewPost(post)">
              <i class="action-icon">阅</i>
              <span>查看</span>
            </div>
            <div class="action-btn edit-btn" @click.stop="editPost(post)">
              <i class="action-icon">修</i>
              <span>编辑</span>
            </div>
            <div class="action-btn delete-btn" @click.stop="openDeleteConfirm(post)">
              <i class="action-icon">删</i>
              <span>删除</span>
            </div>
          </div>
        </div>
        
        <div class="pagination">
          <a-pagination
            :current="pagination.current"
            :total="pagination.total"
            :page-size="pagination.pageSize"
            @page-change="handlePageChange"
            show-total
            size="medium"
          />
        </div>
      </div>
    </a-spin>
    
    <!-- 删除确认框 -->
    <a-modal
      :visible="showDeleteConfirm"
      v-model="showDeleteConfirm"
      @ok="confirmDelete"
      title="确认删除"
      :ok-button-props="{ status: 'danger' }"
      :width="350"
    >
      <div class="delete-confirm-content">
        <div class="seal-icon">删</div>
        <p>确定要删除这篇帖子吗？此操作无法撤销。</p>
      </div>
    </a-modal>
  </div>
</template>

<style scoped>
.my-posts {
  padding: 20px;
  height: 100%;
  background-color: #FFF7E9;
  background-image: url("data:image/svg+xml,%3Csvg width='80' height='80' viewBox='0 0 80 80' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='%23D6C6AF' fill-opacity='0.05'%3E%3Cpath d='M50 50c0-5.523 4.477-10 10-10s10 4.477 10 10-4.477 10-10 10c0 5.523-4.477 10-10 10s-10-4.477-10-10 4.477-10 10-10zM10 10c0-5.523 4.477-10 10-10s10 4.477 10 10-4.477 10-10 10c0 5.523-4.477 10-10 10S0 25.523 0 20s4.477-10 10-10zm10 8c4.418 0 8-3.582 8-8s-3.582-8-8-8-8 3.582-8 8 3.582 8 8 8zm40 40c4.418 0 8-3.582 8-8s-3.582-8-8-8-8 3.582-8 8 3.582 8 8 8z' /%3E%3C/g%3E%3C/svg%3E");
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  border-bottom: 2px solid #8C1F28;
  padding-bottom: 10px;
}

.page-title {
  color: #8C1F28;
  font-size: 28px;
  font-weight: 500;
  position: relative;
  display: inline-block;
  font-family: "STKaiti", "楷体", "FangSong", serif;
  letter-spacing: 2px;
  margin: 0;
}

.page-title::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: -8px;
  width: 80px;
  height: 3px;
  background-color: #8C1F28;
  display: none; /* 不需要下划线，因为已有边框 */
}

.new-post-btn {
  background-color: #8C1F28;
  border-color: #8C1F28;
  transition: all 0.3s;
  height: 42px;
  border-radius: 4px;
  overflow: hidden;
  position: relative;
  font-family: "STKaiti", "楷体", serif;
}

.new-post-btn:hover {
  background-color: #a52a2a;
  border-color: #a52a2a;
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(140, 31, 40, 0.3);
}

.btn-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-icon {
  display: flex;
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

.posts-list {
  margin-top: 24px;
  display: grid;
  grid-template-columns: repeat(1, 1fr);
  gap: 24px;
}

.post-card {
  background-color: #FFF7E9;
  border-radius: 8px;
  transition: all 0.3s ease;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  border: none;
  position: relative;
  border: 1px solid transparent;
  /* 卷轴风格 */
  background-image: linear-gradient(#FFF7E9, #F8ECD9);
}

.post-card::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 8px;
  background: linear-gradient(90deg, #8C1F28, #A86032, #8C1F28);
  opacity: 0.8;
}

.post-card:hover {
  box-shadow: 0 4px 16px rgba(140, 31, 40, 0.15);
  transform: translateY(-3px);
  border-color: #E4D9C3;
}

.post-content {
  cursor: pointer;
  padding: 24px;
  position: relative;
  flex: 1;
  border-bottom: 1px dashed #D6C6AF;
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  position: relative;
}

.post-title {
  margin: 0;
  font-size: 20px;
  color: #582F0E;
  font-weight: 500;
  flex: 1;
  font-family: "STKaiti", "楷体", serif;
  letter-spacing: 1px;
  line-height: 1.4;
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
  font-size: 13px;
  padding: 2px 10px;
  background-color: rgba(140, 31, 40, 0.1);
  color: #8C1F28;
  border-radius: 12px;
  margin-left: 12px;
  white-space: nowrap;
  font-family: "STKaiti", "楷体", serif;
  border: 1px solid #8C1F28;
}

.post-desc {
  font-size: 15px;
  color: #7F4F24;
  margin-bottom: 16px;
  line-height: 1.8;
  font-family: "SimSun", "宋体", serif;
  text-align: justify;
  position: relative;
  padding-left: 16px;
}

.post-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #7F4F24;
  padding-top: 12px;
}

.post-time {
  font-size: 13px;
  font-family: "SimSun", "宋体", serif;
  color: #A86032;
}

.post-stats {
  display: flex;
  gap: 20px;
}

.stat-item {
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 6px;
  color: #A86032;
}

.icon {
  display: inline-block;
  width: 20px;
  height: 20px;
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
  position: relative;
  font-family: "STKaiti", "楷体", serif;
  font-size: 14px;
  line-height: 20px;
  text-align: center;
  color: #A86032;
}

/* 使用中国传统风格的图标文字 */
.icon-view::before {
  content: "览";
}

.icon-like::before {
  content: "赞";
}

.icon-comment::before {
  content: "评";
}

.post-actions {
  display: flex;
  padding: 16px;
  justify-content: flex-end;
  gap: 16px;
  background-color: rgba(248, 236, 217, 0.5);
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
  font-family: "STKaiti", "楷体", serif;
  font-size: 14px;
  border: 1px solid #D6C6AF;
}

.view-btn {
  color: #4D6A6D; /* 青瓷色 */
  background-color: rgba(77, 106, 109, 0.1);
}

.view-btn:hover {
  background-color: rgba(77, 106, 109, 0.2);
}

.edit-btn {
  color: #8C4B00; /* 赭石色 */
  background-color: rgba(140, 75, 0, 0.1);
}

.edit-btn:hover {
  background-color: rgba(140, 75, 0, 0.2);
}

.delete-btn {
  color: #8C1F28; /* 砖红色 */
  background-color: rgba(140, 31, 40, 0.1);
}

.delete-btn:hover {
  background-color: rgba(140, 31, 40, 0.2);
}

.action-icon {
  display: inline-block;
  width: 24px;
  height: 24px;
  border-radius: 4px;
  text-align: center;
  line-height: 24px;
  font-style: normal;
  font-family: "STKaiti", "楷体", serif;
  font-weight: bold;
  font-size: 14px;
  position: relative;
}

.pagination {
  margin-top: 32px;
  display: flex;
  justify-content: center;
}

:deep(.arco-pagination-item-active) {
  background-color: #8C1F28;
  border-color: #8C1F28;
}

:deep(.arco-pagination-item:hover) {
  color: #8C1F28;
}

.empty-data {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  min-height: 320px;
}

.empty-scroll {
  width: 100%;
  max-width: 400px;
  background-color: #FFF7E9;
  border-radius: 8px;
  position: relative;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  border: 1px solid #E4D9C3;
  /* 卷轴风格 */
  background-image: url("data:image/svg+xml,%3Csvg width='100' height='100' viewBox='0 0 100 100' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='%23D6C6AF' fill-opacity='0.2'%3E%3Cpath d='M11 18c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm48 25c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm-43-7c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm63 31c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM34 90c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm56-76c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM12 86c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm28-65c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm23-11c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-6 60c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm29 22c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zM32 63c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm57-13c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-9-21c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM60 91c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM35 41c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM12 60c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2z'/%3E%3C/g%3E%3C/svg%3E");
}

.empty-scroll::before,
.empty-scroll::after {
  content: "";
  position: absolute;
  left: 0;
  width: 100%;
  height: 10px;
}

.empty-scroll::before {
  top: 0;
  background: linear-gradient(90deg, #8C1F28, #A86032, #8C1F28);
  opacity: 0.8;
  border-radius: 8px 8px 0 0;
}

.empty-scroll::after {
  bottom: 0;
  background: linear-gradient(90deg, #8C1F28, #A86032, #8C1F28);
  opacity: 0.8;
  border-radius: 0 0 8px 8px;
}

.scroll-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 30px 0;
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
  box-shadow: 0 2px 8px rgba(140, 31, 40, 0.3);
}

/* 删除确认框样式 */
.delete-confirm-content {
  display: flex;
  align-items: center;
  padding: 10px 0;
}

.seal-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #8C1F28;
  color: white;
  font-family: "STKaiti", "楷体", serif;
  font-size: 18px;
  margin-right: 16px;
}

:deep(.arco-modal-header-title) {
  font-family: "STKaiti", "楷体", serif;
  color: #8C1F28;
  font-size: 18px;
}

:deep(.arco-btn-primary) {
  background-color: #8C1F28;
  border-color: #8C1F28;
}

:deep(.arco-btn-primary:hover) {
  background-color: #a52a2a;
  border-color: #a52a2a;
}

/* 响应式布局 */
@media screen and (min-width: 768px) {
  .posts-list {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .post-card {
    height: 100%;
  }
}

@media screen and (max-width: 768px) {
  .post-header, .post-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .post-actions {
    flex-wrap: wrap;
    justify-content: space-between;
  }
  
  .action-btn {
    flex: 1;
    justify-content: center;
    min-width: 80px;
  }
}
</style> 