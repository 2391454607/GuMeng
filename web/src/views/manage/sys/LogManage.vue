<template>
  <div class="container">
    <a-card title="系统日志管理">
      <!-- 搜索栏 -->
      <a-row class="search-bar" :gutter="16">
        <a-col :span="6">
          <a-input v-model="searchParams.username" placeholder="用户名" allow-clear @clear="handleSearch" />
        </a-col>
        <a-col :span="5">
          <a-select v-model="searchParams.module" placeholder="选择模块" allow-clear style="width: 100%">
            <a-option v-for="item in modules" :key="item" :value="item">{{ item }}</a-option>
          </a-select>
        </a-col>
        <a-col :span="5">
          <a-select v-model="searchParams.operation" placeholder="操作类型" allow-clear style="width: 100%">
            <a-option v-for="item in operations" :key="item" :value="item">{{ item }}</a-option>
          </a-select>
        </a-col>
        <a-col :span="8">
          <a-range-picker
              v-model="dateRange"
              style="width: 100%"
              :show-time="{ format: 'HH:mm:ss' }"
              format="YYYY-MM-DD HH:mm:ss"
          />
        </a-col>
      </a-row>
      <a-row class="button-bar" :gutter="16">
        <a-col :span="24">
          <a-space>
            <a-button type="primary" @click="handleSearch">查询</a-button>
            <a-button @click="resetSearch">重置</a-button>
            <a-button danger @click="handleClearLog" v-if="isAdmin">清空日志</a-button>
          </a-space>
        </a-col>
      </a-row>

      <!-- 日志表格 -->
      <a-table
          :data="logs.records"
          :loading="loading"
          :pagination="pagination"
          @page-change="onPageChange"
          @page-size-change="onPageSizeChange"
      >
        <template #columns>
          <a-table-column title="ID" data-index="id" :width="80" />
          <a-table-column title="用户名" data-index="username" :width="120" />
          <a-table-column title="模块" data-index="module" :width="120" />
          <a-table-column title="操作类型" data-index="operation" :width="150" />
          <a-table-column title="IP地址" data-index="ip" :width="120" />
          <a-table-column title="IP归属地" data-index="location" :width="150" />
          <a-table-column title="状态" data-index="status" :width="100">
            <template #cell="{ record }">
              <a-tag :color="record.status === 1 ? 'green' : 'red'">
                {{ record.status === 1 ? '成功' : '失败' }}
              </a-tag>
            </template>
          </a-table-column>
          <a-table-column title="操作时间" data-index="createTime" :width="180" />
          <a-table-column title="操作" :width="120">
            <template #cell="{ record }">
              <a-button type="text" @click="handleDelete(record.id)" status="danger">删除</a-button>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { Message, Modal } from '@arco-design/web-vue';
import { http } from '@/utils/http.js';
import { useUserStore } from '@/stores/userStore';

// 用户信息
const userStore = useUserStore();
const isAdmin = computed(() => userStore.isAdmin);

// 搜索参数
const searchParams = reactive({
  username: '',
  module: '',
  operation: '',
  startTime: null,
  endTime: null,
  pageNum: 1,
  pageSize: 10
});

// 日期范围
const dateRange = ref([]);

// 表格数据
const logs = ref({ records: [], total: 0 });
const loading = ref(false);
const modules = ref([]);
const operations = ref([]);

// 分页配置
const pagination = reactive({
  total: 0,
  current: 1,
  pageSize: 10,
  showTotal: true,
  showJumper: true,
  showPageSize: true,
  pageSizeOptions: [10, 20, 50, 100],
});

// 获取日志列表
const fetchLogs = async () => {
  loading.value = true;
  try {
    // 处理日期范围
    if (dateRange.value && dateRange.value.length === 2) {
      searchParams.startTime = formatDateTime(dateRange.value[0]);
      searchParams.endTime = formatDateTime(dateRange.value[1]);
    } else {
      searchParams.startTime = null;
      searchParams.endTime = null;
    }

    const res = await http.get('/sys/log/list', searchParams);
    if (res.code === 200) {
      logs.value = res.data;
      pagination.total = res.data.total;
    } else {
      Message.error(res.message || '获取日志列表失败');
    }
  } catch (error) {
    console.error('获取日志列表出错:', error);
    Message.error('获取日志列表失败');
  } finally {
    loading.value = false;
  }
};

// 获取模块列表
const fetchModules = async () => {
  try {
    const res = await http.get('/sys/log/modules');
    if (res.code === 200) {
      modules.value = res.data;
    }
  } catch (error) {
    console.error('获取模块列表出错:', error);
  }
};

// 获取操作类型列表
const fetchOperations = async () => {
  try {
    const res = await http.get('/sys/log/operations');
    if (res.code === 200) {
      operations.value = res.data;
    }
  } catch (error) {
    console.error('获取操作类型列表出错:', error);
  }
};

// 删除日志
const handleDelete = async (id) => {
  Modal.confirm({
    title: '确认删除',
    content: '是否确认删除该条日志记录？',
    onOk: async () => {
      try {
        const res = await http.delete(`/sys/log/${id}`);
        if (res.code === 200) {
          Message.success('删除成功');
          fetchLogs();
        } else {
          Message.error(res.message || '删除失败');
        }
      } catch (error) {
        console.error('删除日志出错:', error);
        Message.error('删除失败');
      }
    }
  });
};

// 清空日志
const handleClearLog = () => {
  Modal.confirm({
    title: '确认清空',
    content: '是否确认清空所有日志记录？此操作不可恢复！',
    onOk: async () => {
      try {
        const res = await http.delete('/sys/log/clear');
        if (res.code === 200) {
          Message.success('清空日志成功');
          fetchLogs();
        } else {
          Message.error(res.message || '清空日志失败');
        }
      } catch (error) {
        console.error('清空日志出错:', error);
        Message.error('清空日志失败');
      }
    }
  });
};

// 搜索处理
const handleSearch = () => {
  searchParams.pageNum = 1;
  fetchLogs();
};

// 重置搜索
const resetSearch = () => {
  Object.assign(searchParams, {
    username: '',
    module: '',
    operation: '',
    startTime: null,
    endTime: null,
    pageNum: 1,
    pageSize: 10
  });
  dateRange.value = [];
  fetchLogs();
};

// 页码变化
const onPageChange = (page) => {
  searchParams.pageNum = page;
  pagination.current = page;
  fetchLogs();
};

// 每页条数变化
const onPageSizeChange = (pageSize) => {
  searchParams.pageSize = pageSize;
  pagination.pageSize = pageSize;
  fetchLogs();
};

// 格式化日期时间
const formatDateTime = (date) => {
  if (!date) return null;

  // 处理ISO格式的字符串 (如 2025-05-16T09:09:09)
  let d;
  if (typeof date === 'string' && date.includes('T')) {
    d = new Date(date.replace('T', ' ').substring(0, 19));
  } else {
    d = new Date(date);
  }

  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  const hours = String(d.getHours()).padStart(2, '0');
  const minutes = String(d.getMinutes()).padStart(2, '0');
  const seconds = String(d.getSeconds()).padStart(2, '0');
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
};

// 页面初始化
onMounted(() => {
  fetchLogs();
  fetchModules();
  fetchOperations();
});
</script>

<style scoped>
.container {
  padding: 16px;
}
.search-bar {
  margin-bottom: 16px;
}
.button-bar {
  margin-bottom: 16px;
}
</style>