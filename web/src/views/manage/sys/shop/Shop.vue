<script setup>
import {reactive, ref, onMounted} from "vue";
import {Message} from '@arco-design/web-vue';
import {addProductInfoAPI, deleteProductAPI, getGoodsListAPI, uploadGoodImagesAPI} from "@/api/manage/Shop.js";

//分页器状态
const status = reactive({
  current: 1,  // 当前页码
  size: 10     // 每页显示数量
})
const total = ref(0);  // 总数据量

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
    total.value = res.data.total;
    loading.value = false;
  })
})

//新增表单视窗
const addGoods = ref(false)
const GoodsAddClick = () => {
  addGoods.value = true;
};

//定义新增的表单数据
const newGoods = reactive({
  name: "",
  imageUrl: "",
  priceMoney: "",
  mixedPriceMoney: "",
  mixedPricePoints: "",
  stock: "",
  isAvailable: 0  // 默认为0（上架）
});

// 存储上传图片的时间戳
const uploadTimestamp = ref(null);

// 存储选择的文件
const selectedFiles = ref([]);

// 文件选择处理函数
const handleUpload = (files) => {
  selectedFiles.value = files;
};

// 文件上传处理函数
const uploadFiles = async () => {
  if (!selectedFiles.value.length) {
    Message.warning('请先选择图片');
    return;
  }

  try {
    // 创建FormData对象
    const formData = new FormData();
    // 将所有选中的文件添加到FormData中
    selectedFiles.value.forEach(file => {
      formData.append('files', file.file);  // 注意：Arco Design的文件对象中，实际文件在file属性中
    });

    const res = await uploadGoodImagesAPI(formData);
    if (res.code === 200) {
      newGoods.imageUrl = res.data.urls.join(',');
      uploadTimestamp.value = res.data.timestamp;  // 保存后端返回的时间戳
      Message.success('图片上传成功');
      selectedFiles.value = [];
    } else {
      Message.error(res.msg || '图片上传失败');
    }
  } catch (error) {
    Message.error('图片上传失败：' + error.message);
  }
};

// 添加商品函数
const addOk = async () => {
  if (!newGoods.imageUrl) {
    Message.error('请上传商品图片');
    return;
  }

  loading.value = true;
  try {
    const res = await addProductInfoAPI(newGoods, uploadTimestamp.value);
    if (res.code === 200) {
      Message.success(res.msg);
      // 重新加载商品列表
      await getGoodsListAPI(status).then(res => {
        GoodsList.value = res.data.records;
      });
      // 清空表单
      Object.keys(newGoods).forEach(key => {
        newGoods[key] = typeof newGoods[key] === 'boolean' ? false :
            typeof newGoods[key] === 'number' ? 0 : "";
      });
      uploadTimestamp.value = null;
    } else {
      Message.error(res.msg);
    }
  } catch (error) {
    Message.error('添加失败：' + error.message);
  } finally {
    loading.value = false;
    addGoods.value = false;
  }
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
               :pagination="{
                 total: total,
                 current: status.current,
                 pageSize: status.size,
                 onChange: handlePageChange
               }"
               :size="'small'"
      >
        <template #columns>
          <a-table-column align="center" data-index="id" title="ID"></a-table-column>
          <a-table-column align="center" data-index="imageUrl" title="图片">
            <template #cell="{ record }">
              <a-image
                  :src="record.imageUrl.split(',')[0]"
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
          <a-table-column align="center" data-index="isAvailable" title="是否上架">
            <template #cell="{ record }">
              <a-tag :color="record.isAvailable === 0 ? 'blue' : 'red'">
                {{ record.isAvailable === 0 ? '上架' : '下架' }}
              </a-tag>
            </template>
          </a-table-column>

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
  <a-modal v-model:visible="addGoods" @ok="addOk" @cancel=" addGoods = false">
    <template #title>
      新增商品信息
    </template>
    <a-form :model="newGoods" :rules="{  imageUrl: [{ required: true, message: '点击上传预览商品图片' }]}">
      <a-form-item field="name" label="商品名称" required>
        <a-input v-model="newGoods.name" placeholder="请输入商品名称"/>
      </a-form-item>
      
      <a-form-item field="imageUrl" label="商品图片">
        <a-upload
          accept="image/*"
          :multiple="true"
          :limit="5"
          :size="5120"
          :auto-upload="false"
          :show-file-list="false"
          @change="handleUpload"
          @exceed-limit="() => Message.warning('最多上传5张图片')"
          @exceed-size="() => Message.warning('图片大小不能超过10MB')"
        >
          <template #upload-button>
            <a-button>选择图片</a-button>
          </template>
        </a-upload>
      </a-form-item>

      <div style="margin-left: 21%;margin-bottom: 10px">
        <a-button type="primary" style="margin-top: 8px;" @click="uploadFiles">上传图片</a-button>
      </div>

      <a-form-item label="图片预览">
        <div class="image-preview">
          <template v-if="newGoods.imageUrl">
            <a-image
              v-for="(url, index) in newGoods.imageUrl.split(',')"
              :key="index"
              :src="url"
              :width="100"
              :height="100"
              fit="cover"
              style="margin: 0 10px 10px 0; border-radius: 4px;"
            />
          </template>
          <span v-else class="no-preview">无预览图</span>
        </div>
      </a-form-item>
      
      <a-form-item field="priceMoney" label="原价" required>
        <a-input
          v-model="newGoods.priceMoney"
          placeholder="请输入商品原价"
        />
      </a-form-item>
      
      <a-form-item field="mixedPriceMoney" label="混合价格" required>
        <a-input
          v-model="newGoods.mixedPriceMoney"
          placeholder="请输入混合价格"
        />
      </a-form-item>
      
      <a-form-item field="mixedPricePoints" label="混合积分" required>
        <a-input
          v-model="newGoods.mixedPricePoints"
          placeholder="请输入混合积分"
        />
      </a-form-item>
      
      <a-form-item field="stock" label="库存" required>
        <a-input
          v-model="newGoods.stock"
          placeholder="请输入库存"
        />
      </a-form-item>

      <a-form-item field="isAvailable" label="是否上架">
        <a-switch
            v-model="newGoods.isAvailable"
            :checked-value="0"
            :unchecked-value="1"
            checked-text="上架"
            unchecked-text="下架"
        />
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

.image-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 8px;
  background-color: var(--color-fill-2);
  border-radius: 4px;
}

.no-preview {
  color: var(--color-text-3);
  padding: 8px;
}
</style>
