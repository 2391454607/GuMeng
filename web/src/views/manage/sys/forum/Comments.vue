<script setup>
import { ref, reactive, onMounted } from "vue";
import { Message } from '@arco-design/web-vue';
import {
  getCommentsAPI,
  getCommentDetailAPI,
  deleteCommentAPI,
  batchDeleteCommentsAPI,
  getCommentStatsAPI
} from "@/api/manage/Comment.js";

// 表格数据
const commentList = ref([]);
const loading = ref(true);
const selectedKeys = ref([]);

// 分页参数
const status = reactive({
  current: 1,
  size: 10,
  postId: null,
  userId: null
});
const total = ref(0);

// 统计数据
const stats = reactive({
  totalComments: 0,
  todayComments: 0
});

// 初始化加载数据
onMounted(() => {
  getCommentList();
  getCommentStats();
});

// 获取评论列表
const getCommentList = () => {
  loading.value = true;
  getCommentsAPI(status).then(res => {
    if (res.code === 200) {
      commentList.value = res.data.records;
      total.value = res.data.total;
    } else {
      Message.error(res.msg || "获取评论列表失败");
    }
    loading.value = false;
  }).catch(err => {
    console.error("获取评论列表出错:", err);
    Message.error("获取评论列表失败");
    loading.value = false;
  });
};

// 获取评论统计数据
const getCommentStats = () => {
  getCommentStatsAPI().then(res => {
    if (res.code === 200) {
      stats.totalComments = res.data.totalComments;
      stats.todayComments = res.data.todayComments;
    }
  }).catch(err => {
    console.error("获取评论统计出错:", err);
  });
};

// 分页处理
const handlePageChange = (current) => {
  status.current = current;
  getCommentList();
};

// 搜索功能
const handleSearch = () => {
  status.current = 1;
  getCommentList();
};

// 重置搜索
const handleReset = () => {
  status.current = 1;
  status.postId = null;
  status.userId = null;
  getCommentList();
};

// 评论详情相关
const commentDetail = ref(null);
const commentDetailVisible = ref(false);

const viewCommentDetail = (id) => {
  getCommentDetailAPI(id).then(res => {
    if (res.code === 200) {
      commentDetail.value = res.data;
      commentDetailVisible.value = true;
    } else {
      Message.error(res.msg || "获取评论详情失败");
    }
  }).catch(err => {
    console.error("获取评论详情出错:", err);
    Message.error("获取评论详情失败");
  });
};

// 删除评论
const deleteComment = (id) => {
  deleteCommentAPI(id).then(res => {
    if (res.code === 200) {
      Message.success("删除成功");
      getCommentList();
      getCommentStats();
    } else {
      Message.error(res.msg || "删除失败");
    }
  }).catch(err => {
    console.error("删除评论出错:", err);
    Message.error("删除失败");
  });
};

// 批量删除评论
const batchDeleteComments = () => {
  if (selectedKeys.value.length === 0) {
    Message.warning("请先选择要删除的评论");
    return;
  }

  batchDeleteCommentsAPI(selectedKeys.value).then(res => {
    if (res.code === 200) {
      Message.success(res.msg || "批量删除成功");
      selectedKeys.value = [];
      getCommentList();
      getCommentStats();
    } else {
      Message.error(res.msg || "批量删除失败");
    }
  }).catch(err => {
    console.error("批量删除评论出错:", err);
    Message.error("批量删除失败");
  });
};

// 选择行变化事件
const handleSelectionChange = (rowKeys) => {
  selectedKeys.value = rowKeys;
};
</script>

<template>
  <div>
    <div class="top">
      <div class="stats-cards">
        <a-card class="stat-card" :style="{ width: '260px' }">
          <template #title>总评论数</template>
          <div class="stat-value">{{ stats.totalComments }}</div>
        </a-card>
        <a-card class="stat-card" :style="{ width: '260px' }">
          <template #title>今日评论数</template>
          <div class="stat-value">{{ stats.todayComments }}</div>
        </a-card>
      </div>

      <a-space>
        <a-popconfirm content="确定要删除所选评论吗？" position="tr" type="warning" @ok="batchDeleteComments">
          <a-button class="delete-button" status="danger" :disabled="selectedKeys.length === 0">
            <template #icon>
              <icon-delete />
            </template>
            批量删除
          </a-button>
        </a-popconfirm>
      </a-space>
    </div>

    <!-- 搜索区域 -->
    <div class="search-area">
      <a-form :model="status" layout="inline">
        <a-form-item field="postId" label="帖子ID">
          <a-input-number v-model="status.postId" placeholder="输入帖子ID" allow-clear style="width: 150px" />
        </a-form-item>
        <a-form-item field="userId" label="用户ID">
          <a-input-number v-model="status.userId" placeholder="输入用户ID" allow-clear style="width: 150px" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">搜索</a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </div>

    <!-- 评论列表 -->
    <div class="form">
      <a-table
          :loading="loading"
          :bordered="false"
          :data="commentList"
          :pagination="{
          total: total,
          current: status.current,
          pageSize: status.size,
          showTotal: true,
        }"
          :size="'small'"
          :row-selection="{
          type: 'checkbox',
          showCheckedAll: true,
          onlyCurrent: false,
          onChange: handleSelectionChange
        }"
          @page-change="handlePageChange"
      >
        <template #columns>
          <a-table-column align="center" data-index="commentId" title="ID" width="60"></a-table-column>
          <a-table-column align="center" data-index="content" title="内容" ellipsis tooltip></a-table-column>
          <a-table-column align="center" data-index="username" title="评论者"></a-table-column>
          <a-table-column align="center" data-index="postTitle" title="所属帖子" ellipsis tooltip>
            <template #cell="{ record }">
              <div v-if="record.postTitle">
                <a-tooltip :content="record.postTitle">
                  <a-tag>{{ record.postId }}</a-tag>
                  {{ record.postTitle.length > 10 ? record.postTitle.slice(0, 10) + '...' : record.postTitle }}
                </a-tooltip>
              </div>
              <span v-else>{{ record.postId || '-' }}</span>
            </template>
          </a-table-column>
          <a-table-column align="center" data-index="isReply" title="是否回复">
            <template #cell="{ record }">
              <a-tag v-if="record.isReply" color="blue">回复评论</a-tag>
              <a-tag v-else color="green">原始评论</a-tag>
            </template>
          </a-table-column>
          <a-table-column align="center" data-index="thumbsUp" title="点赞数"></a-table-column>
          <a-table-column align="center" data-index="createTime" title="发布时间"></a-table-column>
          <a-table-column align="center" title="操作" width="180">
            <template #cell="{record}">
              <a-button class="edit-button" type="text" @click="viewCommentDetail(record.commentId)">
                <template #icon>
                  <icon-eye />
                </template>
                查看
              </a-button>

              <a-popconfirm content="此操作不可逆，你确定要删除吗？" position="tr" type="warning" @ok="deleteComment(record.commentId)">
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

    <!-- 评论详情弹窗 -->
    <a-modal v-model:visible="commentDetailVisible" title="评论详情" :footer="false" :width="600">
      <div v-if="commentDetail" class="comment-detail">
        <div class="comment-user">
          <div class="user-avatar">
            <a-avatar :size="40" :image-url="commentDetail.userPic">
              {{ commentDetail.username ? commentDetail.username.substring(0, 1) : 'U' }}
            </a-avatar>
          </div>
          <div class="user-info">
            <div class="username">{{ commentDetail.username }}</div>
            <div class="user-id">用户ID: {{ commentDetail.userId }}</div>
          </div>
        </div>

        <div class="comment-post">
          所属帖子: <a-tag>{{ commentDetail.postId }}</a-tag> {{ commentDetail.postTitle }}
        </div>

        <div v-if="commentDetail.isReply" class="parent-comment">
          <div class="reply-label">回复评论:</div>
          <div class="parent-content">
            <div class="parent-username">{{ commentDetail.parentUsername }}</div>
            <div class="parent-text">{{ commentDetail.parentContent }}</div>
          </div>
        </div>

        <div class="comment-content">
          {{ commentDetail.content }}
        </div>

        <div class="comment-meta">
          <span>时间: {{ commentDetail.createTime }}</span>
          <span>点赞: {{ commentDetail.thumbsUp }}</span>
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
  align-items: center;
  margin: 30px 20px 0;
}

.stats-cards {
  display: flex;
  gap: 20px;
}

.stat-card {
  border-radius: 4px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #1d63e3;
  text-align: center;
}

.search-area {
  margin: 20px 20px 0;
}

.comment-detail {
  padding: 10px;
}

.comment-user {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.username {
  font-weight: bold;
}

.user-id {
  font-size: 12px;
  color: #999;
}

.comment-post {
  margin: 10px 0;
  padding: 5px 10px;
  background: #f8f8f8;
  border-radius: 4px;
}

.parent-comment {
  margin: 10px 0;
  padding: 10px;
  background: #f1f3fa;
  border-radius: 4px;
  border-left: 3px solid #1d63e3;
}

.reply-label {
  font-size: 12px;
  color: #666;
  margin-bottom: 5px;
}

.parent-username {
  font-weight: bold;
  margin-bottom: 5px;
}

.comment-content {
  margin: 15px 0;
  padding: 10px;
  background: #f8f8f8;
  border-radius: 4px;
  min-height: 60px;
}

.comment-meta {
  display: flex;
  gap: 20px;
  color: #666;
  font-size: 12px;
}

.delete-button {
  color: rgb(var(--danger-6));
}

.delete-button:hover {
  background-color: rgb(var(--danger-1));
  color: rgb(var(--danger-7));
}
</style>