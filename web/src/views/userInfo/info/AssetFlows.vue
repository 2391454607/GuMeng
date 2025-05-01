<script setup>
import {onMounted, reactive, ref} from "vue";
import {getPointLogAPI, getBalanceLogAPI} from "@/api/user/UserInfo.js";
import {h} from 'vue';
import { formatDate } from '@/utils/format';

const loading = ref(false);

const selectedType = ref('points'); // 替换原来的 activeTab

//分页器状态
const status = reactive({
  current: 1,
  size: 10
})


// 积分记录数据
const pointLogs = ref([]);
const pointLogsTotalRecords = ref(0);  // 用于保存总记录数
// 积分记录数据
const pointLogsHandleChangePage = (current) => {
  loading.value = true;
  getPointLogAPI({
    current: current,
    size: status.size
  }).then((res) => {
    pointLogs.value = res.data.records;
    pointLogsTotalRecords.value = res.data.total;
    loading.value = false;
  })
}

// 余额记录数据
const balanceLogs = ref([]);
const balanceLogsTotalRecords = ref(0);  // 用于保存总记录数
const balanceLogsHandleChangePage = (current) => {
  loading.value = true;
  status.current = current;
  getBalanceLogAPI(status).then((res) => {
    balanceLogs.value = res.data.records;
    balanceLogsTotalRecords.value = res.data.total;
    loading.value = false;
  })
}

// 分页处理函数
const handlePageChange = (current) => {
  if (selectedType.value === 'points') {
    pointLogsHandleChangePage(current);
  } else {
    balanceLogsHandleChangePage(current);
  }
};

// 标签切换处理
const handleTypeChange = (value) => {
  selectedType.value = value;
  status.current = 1;  // 重置页码
  handlePageChange(1); // 重新加载数据
};


onMounted(() => {
  loading.value = true;
  getPointLogAPI(status).then((res) => {
    pointLogs.value = res.data.records;
    pointLogsTotalRecords.value = res.data.total;
  }).finally(() => {
    loading.value = false;
  });

  loading.value = true;
  getBalanceLogAPI(status).then((res) => {
    balanceLogs.value = res.data.records;
    balanceLogsTotalRecords.value = res.data.total;
  }).finally(() => {
    loading.value = false;
  });
});


// 定义积分表格列
const pointColumns = [
  {title: '类型', dataIndex: 'changeType', align: 'center'},
  {
    title: '积分变动',
    dataIndex: 'changeValue',
    align: 'center',
    render: (params) => {
      const value = Number(params.record?.changeValue) || 0;
      return h('span', {
        style: {
          color: value > 0 ? '#52c41a' : '#ff4d4f',
          fontWeight: 500
        }
      }, `${value > 0 ? '+' : ''}${value}`);
    }
  },
  {title: '描述', dataIndex: 'description', align: 'center'},
  {
    title: '变动时间',
    dataIndex: 'createTime', 
    align: 'center',
    render: (params) => {
      return formatDate(params.record?.createTime);
    }
  }
];

// 定义余额表格列
const balanceColumns = [
  {title: '类型', dataIndex: 'changeType', align: 'center'},
  {
    title: '金额变动',
    dataIndex: 'changeAmount',
    align: 'center',
    render: (params) => {
      const value = Number(params.record?.changeAmount) || 0;
      return h('span', {
        style: {
          color: value > 0 ? '#52c41a' : '#ff4d4f',
          fontWeight: 500
        }
      }, `${value > 0 ? '+' : ''}${value}`);
    }
  },
  {title: '描述', dataIndex: 'description', align: 'center'},
  {
    title: '变动时间',
    dataIndex: 'createTime', 
    align: 'center',
    render: (params) => {
      return formatDate(params.record?.createTime);
    }
  }
];
</script>

<template>
  <div class="container">
    <div class="header">
      <a-select
          v-model="selectedType"
          style="width: 200px; margin-bottom: 16px;"
          @change="handleTypeChange"
      >
        <a-option value="points">积分流动记录</a-option>
        <a-option value="balance">余额流动记录</a-option>
      </a-select>
    </div>

    <div class="table">
      <a-table
          :bordered="false"
          :columns="selectedType === 'points' ? pointColumns : balanceColumns"
          :data="selectedType === 'points' ? pointLogs : balanceLogs"
          :loading="loading"
          :pagination="{
    total: selectedType === 'points' ? pointLogsTotalRecords : balanceLogsTotalRecords,
    current: status.current,
    pageSize: status.size,
    showTotal: true,
  }"
          size="small"
          @page-change="handlePageChange"
      />
    </div>
  </div>
</template>

<style scoped>
.container {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  background-color: #FFF7E9;
}

.header {
  width: 100%;
  display: flex;
  justify-content: flex-end;
}

.table {
  width: 100%;
  display: flex;
  flex-direction: column;
}

:deep(.arco-table-th) {
  background-color: #FFE5CA !important;
  font-weight: 600;
  color: #333;
}

:deep(.arco-table-tr) {
  background-color: #FFF7E9;
}

:deep(.arco-table-td) {
  border-bottom: 1px solid #FFE5CA !important;
  color: #666;
  background-color: #FFF7E9;
}

:deep(.arco-table-tr:hover .arco-table-td) {
  color: #333;
  background-color: #FFE5CA !important;
}

:deep(.arco-pagination-item) {
  background-color: #FFF7E9;
}

:deep(.arco-select-view) {
  background-color: #FFF7E9;
}

:deep(.arco-pagination) {
  display: flex;
  justify-content: center;
  width: 100%;
  color: #333;
}

:deep(.arco-pagination-item:hover),
:deep(.arco-pagination-item.arco-pagination-item-active) {
  background-color: #FFE5CA;
  border-color: #FFE5CA;
  color: #333;
}
</style>
