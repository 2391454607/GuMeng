<script setup>
import {reactive, ref, onMounted, getCurrentInstance} from "vue";

import {Message} from '@arco-design/web-vue';
import {addCarouselAPI, deleteCarouselAPI, getCarouselAPI, updateCarouselAPI} from "@/api/manage/Carousel.js";

//表格数据
const CarouseList = ref([])
const loading = ref(true);

//获取轮播图列表（初始化表格）
onMounted(()=>{
  getCarouselAPI().then(res=>{
    CarouseList.value = res.data;
    loading.value = false;
  })
})

//添加轮播图
//定义新增的表单数据
const newCarouse = reactive({
  id: "",
  imageUrl: "",
});
const addCarouse = ref(false);
const CarouseAddClick = async () => {
  addCarouse.value = true;
};

const addOk = async () => {
  addCarouselAPI(newCarouse).then((res) => {
    if (res.code === 200) {
      Message.success(res.msg)
      loading.value = true;
      //更新轮播图列表
      getCarouselAPI().then((res) => {
        CarouseList.value = res.data;
        loading.value = false;
      })
    } else {
      Message.error(res.msg)
    }
  })
  addCarouse.value = false;
};

//修改操作
const updateCarouse = ref(false);
// 用于存储正在编辑的轮播图数据
const updateCarouseData = reactive({
  id: "",
  imageUrl: "",
});
const record = reactive({
  id: "",
  imageUrl: "",
})
const updateCarouseClick = async (record) => {
  // 将当前行数据赋值填充到表单中
  updateCarouseData.id = record.id;
  updateCarouseData.imageUrl = record.imageUrl;
  updateCarouse.value = true;
};
const updateOk = () => {
  console.log(updateCarouseData)
  updateCarouselAPI(updateCarouseData).then((res) => {
    if (res.code === 200) {
      Message.success(res.msg)
      loading.value = true;
      //更新轮播图列表
      getCarouselAPI().then((res) => {
        CarouseList.value = res.data;
        loading.value = false;
      })
    } else {
      Message.error(res.msg)
    }
  })
  updateCarouse.value = false;
};

// 删除操作
const del = ref(false);
const delClick = () => {
  del.value = true;
};
const delOk = (record) => {
  deleteCarouselAPI( {id:record.id} ).then((res) => {
    if (res.code === 200) {
      Message.success(res.msg)
      loading.value = true;
      //更新轮播图列表
      getCarouselAPI().then((res) => {
        CarouseList.value = res.data;
        loading.value = false
      })
    } else {
      Message.error(res.msg)
    }
  })
  del.value = false;
};
</script>

<template>

  <div>
    <div class="top">

      <div class="add">
        <a-button class="add-button" type="outline" @click="CarouseAddClick">
          <template #icon>
            <icon-plus/>
          </template>
          新增
        </a-button>
      </div>

    </div>

    <div class="form">
      <a-table v-model:loading="loading" :bordered="false" :data="CarouseList"
               :pagination="{showJumper: true}"
               :size="'small'"
      >
        <template #columns>
          <a-table-column align="center" data-index="id" title="ID"></a-table-column>
          <a-table-column align="center" data-index="imageUrl" title="图片">
            <template #cell="{ record }">
              <a-image
                  :src="record.imageUrl"
                  :preview="true"
                  :width="100"
                  :height="60"
                  fit="cover"
              />
            </template>
          </a-table-column>
          <a-table-column align="center" data-index="imageUrl" title="图片路径"></a-table-column>
          <a-table-column align="center" data-index="createdTime" title="创建时间"></a-table-column>
          <a-table-column align="center" data-index="updatedTime" title="更新时间"></a-table-column>

          <a-table-column align="center" data-index="optional" title="操作">
            <template #cell="{record}" class="option">

              <a-button class="edit-button" type="text" @click="updateCarouseClick(record)">
                <template #icon>
                  <icon-edit/>
                </template>
                修改
              </a-button>

              <a-popconfirm content="此操作不可逆，你确定要删除吗？" position="tr" type="warning" @ok="delOk(record)">
                <a-button class="delete-button" status="danger" type="text" @click="delClick">
                  <template #icon>
                    <icon-delete/>
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

  <!--新增-->
  <a-modal v-model:visible="addCarouse" @ok="addOk">
    <template #title>
      新增轮播图
    </template>
    <a-form :model="newCarouse" :style="{ width: '450px' }">
      <a-form-item field="imageUrl" label="轮播图路径">
        <a-input v-model="newCarouse.imageUrl"/>
      </a-form-item>
      <a-form-item label="图片预览">
        <a-image
            v-if="newCarouse.imageUrl"
            :src="newCarouse.imageUrl"
            :width="200"
            :height="120"
            fit="cover"
        />
        <span v-else>无预览图</span>
      </a-form-item>
    </a-form>
  </a-modal>

  <!--修改-->
  <a-modal v-model:visible="updateCarouse" @ok="updateOk">
    <template #title>
      修改轮播图
    </template>
    <a-form :model="updateCarouseData" :style="{ width: '450px' }">
      <a-form-item field="id" label="id">
        <a-input v-model="updateCarouseData.id" disabled/>
      </a-form-item>
      <a-form-item field="imageUrl" label="轮播图路径">
        <a-input v-model="updateCarouseData.imageUrl"/>
      </a-form-item>
      <a-form-item label="图片预览">
        <a-image
            v-if="updateCarouseData.imageUrl"
            :src="updateCarouseData.imageUrl"
            :width="200"
            :height="120"
            fit="cover"
        />
        <span v-else>无预览图</span>
      </a-form-item>
    </a-form>
  </a-modal>


</template>

<style scoped>
.form {
  margin: 20px;

}

.top {
  display: flex;
  justify-content: end;

}

.add{
  margin-top: 30px;
  margin-right: 20px;
}

.delete-button {
  color: rgb(var(--danger-6));

  &:hover {
    background-color: rgb(var(--danger-1));
    color: rgb(var(--danger-7));
  }
}
</style>