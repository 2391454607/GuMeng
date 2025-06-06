<script setup>
import {reactive, ref, onMounted} from "vue";
import {Message} from '@arco-design/web-vue';
import {deleteProductAPI, getGoodsListAPI} from "@/api/manage/Shop.js";

//分页器状态
const status = reactive({
  current: 1,
  size: 10
})
const total = ref(0);

//表格数据
const GoodsList = ref([])
const loading = ref(true);

// 分页处理函数
const handlePageChange = (current) => {
  status.current = current;
  loading.value = true;
  getGoodsListAPI(status).then(res => {
    GoodsList.value = res.data.records;
    total.value = res.data.total;
    loading.value = false;
  });
};

//获取商品信息列表（初始化表格）
onMounted(()=>{
  getGoodsListAPI(status).then(res=>{
    GoodsList.value = res.data.records;
    loading.value = false;
  })
})

//添加商品信息
//定义新增的表单数据
const newGoods = reactive({
  id: "",
  imageUrl: "",
});
const addGoods = ref(false);
const GoodsAddClick = async () => {
  addGoods.value = true;
};

const addOk = async () => {

  addGoods.value = false;
};

//修改操作
const updateGoods = ref(false);
// 用于存储正在编辑的商品信息数据
const updateGoodsData = reactive({
  id: "",
  imageUrl: "",
});
const record = reactive({
  id: "",
  imageUrl: "",
})
const updateGoodsClick = async (record) => {
  // 将当前行数据赋值填充到表单中
  updateGoodsData.id = record.id;
  updateGoodsData.imageUrl = record.imageUrl;
  updateGoods.value = true;
};
const updateOk = () => {
  console.log(updateGoodsData)

  updateGoods.value = false;
};

// 删除操作
const del = ref(false);
const delClick = () => {
  del.value = true;
};
const delOk = (record) => {
  deleteProductAPI( {id:record.id} ).then((res) => {
    if (res.code === 200) {
      Message.success(res.msg)
      loading.value = true;
      //更新商品信息列表
      getGoodsListAPI(status).then((res) => {
        GoodsList.value = res.data;
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
        <a-button class="add-button" type="outline" @click="GoodsAddClick">
          <template #icon>
            <icon-plus/>
          </template>
          新增
        </a-button>
      </div>

    </div>

    <div class="form">
      <a-table v-model:loading="loading" :bordered="false" :data="GoodsList"
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
          <a-table-column align="center" data-index="name" title="商品名称"></a-table-column>
          <a-table-column align="center" data-index="priceMoney" title="原价"></a-table-column>
          <a-table-column align="center" data-index="mixedPriceMoney" title="混合价格"></a-table-column>
          <a-table-column align="center" data-index="mixedPricePoints" title="混合积分"></a-table-column>
          <a-table-column align="center" data-index="stock" title="库存"></a-table-column>
          <a-table-column align="center" data-index="isAvailable" title="是否上架"></a-table-column>

          <a-table-column align="center" data-index="optional" title="操作">
            <template #cell="{record}" class="option">

              <a-button class="edit-button" type="text" @click="updateGoodsClick(record)">
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
  <a-modal v-model:visible="addGoods" @ok="addOk">
    <template #title>
      新增商品信息
    </template>
    <a-form :model="newGoods" :style="{ width: '450px' }">
      <a-form-item field="imageUrl" label="商品信息路径">
        <a-input v-model="newGoods.imageUrl"/>
      </a-form-item>
      <a-form-item label="图片预览">
        <a-image
            v-if="newGoods.imageUrl"
            :src="newGoods.imageUrl"
            :width="200"
            :height="120"
            fit="cover"
        />
        <span v-else>无预览图</span>
      </a-form-item>
    </a-form>
  </a-modal>

  <!--修改-->
  <a-modal v-model:visible="updateGoods" @ok="updateOk">
    <template #title>
      修改商品信息
    </template>
    <a-form :model="updateGoodsData" :style="{ width: '450px' }">
      <a-form-item field="id" label="id">
        <a-input v-model="updateGoodsData.id" disabled/>
      </a-form-item>
      <a-form-item field="imageUrl" label="商品信息路径">
        <a-input v-model="updateGoodsData.imageUrl"/>
      </a-form-item>
      <a-form-item label="图片预览">
        <a-image
            v-if="updateGoodsData.imageUrl"
            :src="updateGoodsData.imageUrl"
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