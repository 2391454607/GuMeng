<script setup>


import {onMounted, ref, reactive} from "vue";
import {addCarouselAPI, getCarouselAPI} from "@/api/manage/Carousel.js";

const columns = [
  {
    title: 'id',
    dataIndex: 'id',
  },
  {
    title: '图片',
    dataIndex: 'imageUrl',
    key: 'imageUrl',
  },
  {
    title: '创建时间',
    dataIndex: 'createdTime',
  },
  {
    title: '更新时间',
    dataIndex: 'updatedTime',
  },
];
const carouselList = ref([])
const carousel = reactive[{
  id:"",
  imageUrl:"",
  createdTime:"",
  updatedTime:"",
}]
const loading = ref(true)
// 分页相关状态
const pagination = reactive({
  current: 1,
  pageSize: 5,
  total: 0,
  showTotal: (total) => `共 ${total} 条`,
});
// 获取轮播图数据
const fetchCarouselData = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.current,
      pageSize: pagination.pageSize
    };
    const res = await getCarouselAPI(params);
    carouselList.value = res.data || [];
    pagination.total = res.total || 0;
  } catch (error) {
    console.error('获取轮播图数据失败:', error);
  } finally {
    loading.value = false;
  }
};
// 分页变化回调
const handleTableChange = (pag) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  fetchCarouselData();
};

onMounted(() => {
  getCarouselAPI().then((res) => {
    console.log(res)
    carouselList.value = res;
    loading.value = false;
  })
})

//新增表单
const addForm = ref(false)
// 新增按钮点击事件
const CarouselAddClick = async () => {
  addForm.value = true;
  console.log('点击了新增按钮');
};
const addOk = () => {

}

</script>

<template>
  <div class="area">
    <div class="top">

      <div class="add">
        <a-button class="add-button" @click="CarouselAddClick">
          <template #icon>
            <icon-plus/>
          </template>
          新增
        </a-button>
      </div>

    </div>

    <div class="form">
      <a-table :columns="columns" :data-source="carouselList"
               :pagination="pagination"
               :loading="loading"
               rowKey="id"
               @change="handleTableChange">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'imageUrl'">
            <a-image
                :preview="true"
                :src="record.imageUrl"
                :width="50"
                :height="50"
                style="border-radius: 4px; object-fit: cover;"
            />
          </template>
        </template>
      </a-table>
    </div>
  </div>

  <a-modal v-model:visible="addForm" @ok="addOk">
    <a-form-item label="Upload">
      <a-upload action="/upload.do" list-type="picture-card">
        <div>
          <PlusOutlined />
          <div style="margin-top: 8px">Upload</div>
        </div>
      </a-upload>
    </a-form-item>
  </a-modal>

</template>

<style scoped>
.form {
  margin: 20px;
}

.top {
  display: flex;
  justify-content: end;

  button {
    margin-left: 10px;
  }

}

.add {
  margin-top: 30px;
  margin-right: 20px;
}


</style>