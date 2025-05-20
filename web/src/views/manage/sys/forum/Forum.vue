<script setup>
import { ref, reactive, onMounted } from "vue";
import { Message } from '@arco-design/web-vue';
import {
  getForumPostsAPI,
  getPostDetailAPI,
  deletePostAPI,
  getAllTopicsAPI
} from "@/api/manage/Forum.js";

// 表格数据
const postList = ref([]);
const loading = ref(true);
const topics = ref([]);

// 分页参数
const status = reactive({
  current: 1,
  size: 10,
  keyword: "",
  topic: null
});
const total = ref(0);

// 初始化加载数据
onMounted(() => {
  getPostList();
  getTopics();
});

// 获取话题列表
const getTopics = async () => {
  try {
    const res = await getAllTopicsAPI();
    if (res.code === 200) {
      topics.value = res.data || [];
    } else {
      Message.error(res.msg || "获取话题列表失败");
    }
  } catch (err) {
    console.error("获取话题列表出错:", err);
    Message.error("获取话题列表失败");
  }
};

// 获取帖子列表
const getPostList = () => {
  loading.value = true;
  getForumPostsAPI(status).then(res => {
    if (res.code === 200) {
      postList.value = res.data.records;
      total.value = res.data.total;
    } else {
      Message.error(res.msg || "获取帖子列表失败");
    }
    loading.value = false;
  }).catch(err => {
    console.error("获取帖子列表出错:", err);
    Message.error("获取帖子列表失败");
    loading.value = false;
  });
};

// 分页处理
const handlePageChange = (current) => {
  status.current = current;
  getPostList();
};

// 搜索功能
const handleSearch = () => {
  status.current = 1;
  getPostList();
};

// 重置搜索
const handleReset = () => {
  status.current = 1;
  status.keyword = "";
  status.topic = null;
  getPostList();
};

// 帖子详情相关
const postDetail = ref(null);
const postDetailVisible = ref(false);

const viewPostDetail = (id) => {
  console.log("查看帖子详情:", id);
  getPostDetailAPI(id).then(res => {
    if (res.code === 200) {
      postDetail.value = res.data;
      
      // 处理图片字符串为数组
      if (postDetail.value.images && typeof postDetail.value.images === 'string') {
        postDetail.value.images = postDetail.value.images.split(',').filter(img => img);
      } else if (!postDetail.value.images) {
        postDetail.value.images = [];
      }
      
      postDetailVisible.value = true;
    } else {
      Message.error(res.msg || "获取帖子详情失败");
    }
  }).catch(err => {
    console.error("获取帖子详情出错:", err);
    Message.error("获取帖子详情失败");
  });
};

// 删除帖子
const deletePost = (id) => {
  deletePostAPI(id).then(res => {
    if (res.code === 200) {
      Message.success("删除成功");
      getPostList();
    } else {
      Message.error(res.msg || "删除失败");
    }
  }).catch(err => {
    console.error("删除帖子出错:", err);
    Message.error("删除失败");
  });
};

// 日期时间格式化函数
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return '';
  
  // 将ISO格式时间转换为更友好的格式
  const date = new Date(dateTimeStr);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  const seconds = String(date.getSeconds()).padStart(2, '0');
  
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
};

// 添加计算图片网格样式的函数
const getGridClass = (imageCount) => {
  if (imageCount <= 0) return '';
  return `grid-${Math.min(imageCount, 9)}`;
};
</script>

<template>
  <div>
    <!-- 搜索区域 -->
    <div class="search-area">
      <a-form :model="status" layout="inline">
        <a-form-item field="topic" label="话题分类">
          <a-select v-model="status.topic" placeholder="请选择话题" allow-clear style="width: 160px">
            <a-option value="">全部话题</a-option>
            <a-option v-for="item in topics" :key="item.id" :value="item.name">{{ item.name }}</a-option>
          </a-select>
        </a-form-item>
        <a-form-item field="keyword" label="关键词">
          <a-input v-model="status.keyword" placeholder="搜索标题或内容" allow-clear style="width: 200px" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">搜索</a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </div>

    <!-- 帖子列表 -->
    <div class="form">
      <a-table
          :loading="loading"
          :bordered="false"
          :data="postList"
          :pagination="{
          total: total,
          current: status.current,
          pageSize: status.size,
          showTotal: true,
          showJumper: true
        }"
          :size="'small'"
          @page-change="handlePageChange"
      >
        <template #columns>
          <a-table-column align="center" data-index="id" title="ID" width="60"></a-table-column>
          <a-table-column align="center" data-index="title" title="标题" ellipsis tooltip></a-table-column>
          <a-table-column align="center" data-index="topic" title="话题">
            <template #cell="{ record }">
              <a-tag color="blue">{{ record.topic }}</a-tag>
            </template>
          </a-table-column>
          <a-table-column align="center" data-index="username" title="发布者"></a-table-column>
          <a-table-column align="center" data-index="createTime" title="发布时间" width="150">
            <template #cell="{ record }">
              <span class="time-cell">{{ formatDateTime(record.createTime) }}</span>
            </template>
          </a-table-column>
          <a-table-column align="center" data-index="thumbsUpNum" title="点赞数" width="80"></a-table-column>
          <a-table-column align="center" data-index="commonNum" title="评论数" width="80"></a-table-column>
          <a-table-column align="center" data-index="viewCount" title="浏览量" width="80"></a-table-column>
          <a-table-column align="center" title="操作" width="180">
            <template #cell="{record}">
              <div class="operation-buttons">
                <a-button class="edit-button" type="text" @click="viewPostDetail(record.id)">
                  <template #icon>
                    <icon-eye />
                  </template>
                  查看
                </a-button>

                <a-popconfirm content="此操作不可逆，你确定要删除吗？" position="tr" type="warning" @ok="deletePost(record.id)">
                  <a-button class="delete-button" status="danger" type="text">
                    <template #icon>
                      <icon-delete />
                    </template>
                    删除
                  </a-button>
                </a-popconfirm>
              </div>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </div>

    <!-- 帖子详情弹窗 -->
    <a-modal :visible="postDetailVisible" @ok="postDetailVisible = false" @cancel="postDetailVisible = false" title="帖子详情" :footer="false" :width="700">
      <div v-if="postDetail" class="post-detail">
        <h2>{{ postDetail.title }}</h2>
        <div class="post-meta">
          <span>作者: {{ postDetail.username }}</span>
          <span>话题: {{ postDetail.topic }}</span>
          <span class="time-cell">时间: {{ formatDateTime(postDetail.createTime) }}</span>
        </div>
        <div class="post-stats">
          <span>点赞: {{ postDetail.thumbsUpNum }}</span>
          <span>评论: {{ postDetail.commonNum }}</span>
          <span>浏览: {{ postDetail.viewCount }}</span>
        </div>
        <div class="post-content" v-html="postDetail.content"></div>
        
        <!-- 图片展示部分 -->
        <div class="post-images" v-if="postDetail.images && postDetail.images.length > 0">
          <h3>附图</h3>
          <a-image-preview-group infinite>
            <div class="image-grid" :class="getGridClass(postDetail.images.length)">
              <div v-for="(img, index) in postDetail.images" :key="index" class="image-wrapper">
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
    </a-modal>
  </div>
</template>

<style scoped>
.form {
  margin: 20px;
}

.top {
  display: flex;
  justify-content: space-between;
  margin: 30px 20px 0;
}

.search-area {
  margin: 20px 20px 0;
}

.post-detail {
  padding: 10px;
}

.post-meta, .post-stats {
  display: flex;
  gap: 20px;
  margin: 10px 0;
  color: #666;
  font-size: 14px;
}

.post-content {
  margin: 20px 0;
  padding: 15px;
  background: #f8f8f8;
  border-radius: 4px;
  min-height: 100px;
}

.post-images {
  margin: 20px 0;
}

.image-grid {
  display: grid;
  grid-gap: 10px;
  width: 100%;
  max-width: 650px;
}

.image-wrapper {
  position: relative;
  padding-bottom: 70%; /* 进一步降低高度，减少空白 */
  height: 0;
  overflow: hidden;
  border-radius: 4px;
  border: none; /* 移除边框 */
  background-color: transparent; /* 移除背景色 */
  display: flex;
  align-items: center;
  justify-content: center;
}

.post-image {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  margin: auto;
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
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

/* 根据图片数量定义网格布局 */
.grid-1 {
  grid-template-columns: minmax(0, 250px);
  max-width: 250px;
}

.grid-2 {
  grid-template-columns: repeat(2, 1fr);
  max-width: 450px;
}

.grid-3, .grid-4, .grid-5, .grid-6 {
  grid-template-columns: repeat(3, 1fr);
  max-width: 550px;
}

.grid-7, .grid-8, .grid-9 {
  grid-template-columns: repeat(3, 1fr);
  max-width: 600px;
}

.delete-button {
  color: rgb(var(--danger-6));
}

.delete-button:hover {
  background-color: rgb(var(--danger-1));
  color: rgb(var(--danger-7));
}

.operation-buttons {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
}

.time-cell {
  white-space: nowrap;
  display: inline-block;
}
</style>