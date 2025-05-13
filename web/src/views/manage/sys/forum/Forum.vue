<script setup>
import { ref, reactive, onMounted } from "vue";
import { Message } from '@arco-design/web-vue';
import {
  getForumPostsAPI,
  getPostDetailAPI,
  deletePostAPI,
  getAllTopicsAPI,
  addTopicAPI,
  deleteTopicAPI
} from "@/api/manage/Forum.js";

// 表格数据
const postList = ref([]);
const loading = ref(true);

// 分页参数
const status = reactive({
  current: 1,
  size: 10,
  topic: "",
  keyword: ""
});
const total = ref(0);

// 话题数据
const topicList = ref([]);

// 初始化加载数据
onMounted(() => {
  getPostList();
  getTopicList();
});

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

// 获取话题列表
const getTopicList = () => {
  getAllTopicsAPI().then(res => {
    if (res.code === 200) {
      topicList.value = res.data;
    } else {
      Message.error(res.msg || "获取话题列表失败");
    }
  }).catch(err => {
    console.error("获取话题列表出错:", err);
    Message.error("获取话题列表失败");
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
  status.topic = "";
  status.keyword = "";
  getPostList();
};

// 帖子详情相关
const postDetail = ref(null);
const postDetailVisible = ref(false);

const viewPostDetail = (id) => {
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

// 话题管理相关
const newTopic = reactive({
  name: ""
});
const addTopicVisible = ref(false);

const showAddTopic = () => {
  newTopic.name = "";
  addTopicVisible.value = true;
};

const addTopic = () => {
  if (!newTopic.name.trim()) {
    Message.warning("话题名称不能为空");
    return;
  }

  addTopicAPI(newTopic).then(res => {
    if (res.code === 200) {
      Message.success("添加成功");
      getTopicList();
      addTopicVisible.value = false;
    } else {
      Message.error(res.msg || "添加失败");
    }
  }).catch(err => {
    console.error("添加话题出错:", err);
    Message.error("添加失败");
  });
};

const deleteTopic = (id) => {
  deleteTopicAPI(id).then(res => {
    if (res.code === 200) {
      Message.success("删除成功");
      getTopicList();
    } else {
      Message.error(res.msg || "删除失败");
    }
  }).catch(err => {
    console.error("删除话题出错:", err);
    Message.error("删除失败");
  });
};
</script>

<template>
  <div>
    <div class="top">
      <a-space>
        <a-button class="add-button" type="outline" @click="showAddTopic">
          <template #icon>
            <icon-plus />
          </template>
          添加话题
        </a-button>
      </a-space>
    </div>

    <!-- 搜索区域 -->
    <div class="search-area">
      <a-form :model="status" layout="inline">
        <a-form-item field="topic" label="话题">
          <a-select v-model="status.topic" placeholder="选择话题" allow-clear style="width: 200px">
            <a-option v-for="topic in topicList" :key="topic.id" :value="topic.name">{{ topic.name }}</a-option>
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
        }"
          :size="'small'"
          @page-change="handlePageChange"
      >
        <template #columns>
          <a-table-column align="center" data-index="id" title="ID" width="60"></a-table-column>
          <a-table-column align="center" data-index="title" title="标题" ellipsis tooltip></a-table-column>
          <a-table-column align="center" data-index="topic" title="话题"></a-table-column>
          <a-table-column align="center" data-index="username" title="发布者"></a-table-column>
          <a-table-column align="center" data-index="createTime" title="发布时间"></a-table-column>
          <a-table-column align="center" data-index="thumbsUpNum" title="点赞数"></a-table-column>
          <a-table-column align="center" data-index="commonNum" title="评论数"></a-table-column>
          <a-table-column align="center" data-index="viewCount" title="浏览量"></a-table-column>
          <a-table-column align="center" title="操作" width="180">
            <template #cell="{record}">
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
            </template>
          </a-table-column>
        </template>
      </a-table>
    </div>

    <!-- 帖子详情弹窗 -->
    <a-modal v-model:visible="postDetailVisible" title="帖子详情" :footer="false" :width="700">
      <div v-if="postDetail" class="post-detail">
        <h2>{{ postDetail.title }}</h2>
        <div class="post-meta">
          <span>作者: {{ postDetail.username }}</span>
          <span>话题: {{ postDetail.topic }}</span>
          <span>时间: {{ postDetail.createTime }}</span>
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

    <!-- 添加话题弹窗 -->
    <a-modal v-model:visible="addTopicVisible" title="添加话题" @ok="addTopic">
      <a-form :model="newTopic">
        <a-form-item field="name" label="话题名称">
          <a-input v-model="newTopic.name" placeholder="请输入话题名称" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 话题管理列表 -->
    <div class="topic-list">
      <a-divider>话题管理</a-divider>
      <a-table
          :loading="loading"
          :bordered="false"
          :data="topicList"
          :pagination="false"
          :size="'small'"
      >
        <template #columns>
          <a-table-column align="center" data-index="id" title="ID" width="60"></a-table-column>
          <a-table-column align="center" data-index="name" title="话题名称"></a-table-column>
          <a-table-column align="center" data-index="createTime" title="创建时间"></a-table-column>
          <a-table-column align="center" title="操作" width="120">
            <template #cell="{record}">
              <a-popconfirm content="此操作不可逆，你确定要删除吗？" position="tr" type="warning" @ok="deleteTopic(record.id)">
                <a-button class="delete-button" status="danger" type="text">
                  <template #icon>
                    <icon-delete />
                  </template>
                  删除
                </a-button>
              </a-popconfirm>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </div>
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
</style>