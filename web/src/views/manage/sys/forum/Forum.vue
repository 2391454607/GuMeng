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
          <a-table-column align="center" data-index="createTime" title="发布时间">
            <template #cell="{ record }">
              {{ formatDateTime(record.createTime) }}
            </template>
          </a-table-column>
          <a-table-column align="center" data-index="thumbsUpNum" title="点赞数"></a-table-column>
          <a-table-column align="center" data-index="commonNum" title="评论数"></a-table-column>
          <a-table-column align="center" data-index="viewCount" title="浏览量"></a-table-column>
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
          <span>时间: {{ formatDateTime(postDetail.createTime) }}</span>
        </div>
        <div class="post-stats">
          <span>点赞: {{ postDetail.thumbsUpNum }}</span>
          <span>评论: {{ postDetail.commonNum }}</span>
          <span>浏览: {{ postDetail.viewCount }}</span>
        </div>
        <div class="post-content" v-html="postDetail.content"></div>
        <div class="post-images" v-if="postDetail.images && postDetail.images.length > 0">
          <h3>附图</h3>
          <div class="image-list">
            <a-image
                v-for="(img, index) in postDetail.images"
                :key="index"
                :src="img"
                :width="150"
                fit="cover"
                :preview="{
                src: img
              }"
            />
          </div>
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

.image-list {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 10px;
}

.topic-list {
  margin: 20px;
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
</style>