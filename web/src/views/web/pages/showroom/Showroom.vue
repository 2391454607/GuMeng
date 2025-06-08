<script setup>
import {onMounted, ref} from 'vue';
import Footer from "@/views/web/layout/Footer.vue";
import {
  getModelList,
  getBalance,
  textToModel,
  imageToModel,
  uploadSts,
  taskPolling,
  taskText,
  getModelInfo
} from "@/api/web/Model.js";
import {useRouter} from 'vue-router';
import { Modal } from '@arco-design/web-vue';

const models = ref([
  {
    id: '',
    name: '',
    modelPic: '',
    author: '',
    views: ''
  },
]);

onMounted(() => {
  getModelList().then(res => {
    if (res.code === 200) {
      models.value = res.data;
    }
  })
})

const router = useRouter();

const handleModelClick = (id) => {
  router.push({
    path: '/show3D',
    query: {modelId: id}
  });
};

//查询模型生成信息列表
const modelInfoListVisible = ref(false);
const modelInfoList = ref()

const openModelInfoList = async () => {
  getModelInfo().then(res => {
    if (res.code === 200) {
      modelInfoList.value = res.data;
    }
  })
  modelInfoListVisible.value = true;
}

// 查询余额
const balanceVisible = ref(false); // 控制余额显示对话框
const currentBalance = ref(0); // 当前余额

const checkBalance = async () => {
  try {
    const res = await getBalance();
    if (res.code === 0) {
      currentBalance.value = res.data.balance;
      balanceVisible.value = true;
    }
  } catch (error) {
    console.error('查询余额失败:', error);
    Modal.error({
      title: '错误',
      content: '查询余额失败'
    });
  }
};

//生成模型
const visible = ref(false); // 控制弹出框显示
const generateType = ref('text'); // 生成类型：text 或 image
const queryVisible = ref(false);
const textFormData = ref({
  prompt: ''
});
const imageFormData = ref({
  prompt: '',
  file: {
    file_token: ''
  }
});

// 添加预览图片的响应式变量
const previewImage = ref('');

// 处理图片上传
const handleImageUpload = async (image) => {
  try {
    const formData = new FormData();
    console.log(image);
    formData.append('file', image.fileItem.file);
    const res = await uploadSts(formData);
    if (res.code === 0) {
      imageFormData.value.file.file_token = res.data.image_token;
      // 设置预览图片URL
      previewImage.value = URL.createObjectURL(image.fileItem.file);
      image.onSuccess();
    } else {
      image.onError();
      Modal.error({
        title: '错误',
        content: res.msg || '上传失败'
      });
    }
  } catch (error) {
    console.error('上传失败:', error);
    image.onError();
    Modal.error({
      title: '错误',
      content: '上传失败'
    });
  }
};

// 处理生成请求
const handleGenerate = async () => {
  if (generateType.value === 'text') {
    if (!textFormData.value.prompt) {
      Modal.error({
        title: '错误',
        content: '请输入生成提示'
      });
      return;
    }
    try {
      const res = await textToModel({ prompt: textFormData.value.prompt });
      if (res.code === 0) {
        Modal.success({
          title: '成功',
          content: `任务ID：${res.data.task_id}`
        });
        visible.value = false;
        textFormData.value.prompt = '';
      } else {
        Modal.error({
          title: '错误',
          content: res.msg || '生成失败'
        });
      }
    } catch (error) {
      console.error('生成失败:', error);
      Modal.error({
        title: '错误',
        content: '生成失败'
      });
    }
  } else {
    if (!imageFormData.value.prompt || !imageFormData.value.file.file_token) {
      Modal.error({
        title: '错误',
        content: '请输入生成提示并上传图片'
      });
      return;
    }
    try {
      const res = await imageToModel({
        prompt: imageFormData.value.prompt,
        file: imageFormData.value.file
      });
      if (res.code === 0) {
        Modal.success({
          title: '成功',
          content: `任务ID：${res.data.task_id}`
        });
        visible.value = false;
        imageFormData.value.prompt = '';
        imageFormData.value.file.file_token = '';
      } else {
        Modal.error({
          title: '错误',
          content: res.msg || '生成失败'
        });
      }
    } catch (error) {
      console.error('生成失败:', error);
      Modal.error({
        title: '错误',
        content: '生成失败'
      });
    }
  }
};
// 打开生成对话框
const openGenerateDialog = () => {
  visible.value = true;
};

// 打开查询状态对话框
const openQueryDialog = () => {
  queryVisible.value = true;
};

const taskId = ref(''); // 添加 taskId 响应式变量

// 查询状态并更新保存信息
const handleQuery = async () => {
  if (!taskId.value) {
    Modal.error({
      title: '错误',
      content: '请输入任务ID'
    });
    return;
  }

  try {
    const res = await taskPolling({id: taskId.value});
    if (res.success === true) {
      const status = res.data.status;
      if (status === 'success') {
        Modal.success({
          title: '查询结果',
          content: `${res.data.output.pbr_model}`
        });
        await taskText({id: taskId.value});
      } else {
        Modal.info({
          title: '查询结果',
          content: '模型生成未完成'
        });
      }
      queryVisible.value = false;
      taskId.value = '';
    } else {
      Modal.error({
        title: '错误',
        content: res.msg || '查询失败'
      });
    }
  } catch (error) {
    console.error('查询状态失败:', error);
    Modal.error({
      title: '错误',
      content: '查询状态失败'
    });
  }
};

// 打开查询模型对话框
const openViewDialog = () => {
  viewVisible.value = true;
};

const viewVisible = ref(false);
const modelUrl = ref('');

const handleView = () => {
  if (!modelUrl.value) {
    Modal.error({
      title: '错误',
      content: '请输入模型链接'
    });
    return;
  }

  router.push({
    path: '/show3D',
    query: { modelUrl: modelUrl.value }
  });
  viewVisible.value = false;
  modelUrl.value = '';
};

// 添加复制功能
const copyToClipboard = async (text) => {
  try {
    await navigator.clipboard.writeText(text);
    Modal.success({
      title: '成功',
      content: '复制成功'
    });
  } catch (err) {
    Modal.error({
      title: '错误',
      content: '复制失败'
    });
  }
};
</script>

<template>
  <div class="home">
    <div class="showroom">
      <div class="generate-button-container">
        <a-button class="generate-button" type="primary" @click="openModelInfoList">
          <template #icon>
            <icon-search/>
          </template>
          查询模型生成信息
        </a-button>
        <a-button class="generate-button" type="primary" @click="checkBalance">
          <template #icon>
            <icon-search/>
          </template>
          查询余额
        </a-button>
        <a-button class="generate-button" type="primary" @click="openGenerateDialog">
          <template #icon>
            <icon-plus/>
          </template>
          生成模型
        </a-button>
        <a-button class="generate-button" type="primary" @click="openQueryDialog">
          <template #icon>
            <icon-search/>
          </template>
          查询状态
        </a-button>
        <a-button class="generate-button" type="primary" @click="openViewDialog">
          <template #icon>
            <icon-eye/>
          </template>
          查看模型
        </a-button>
      </div>

      <!-- 模型生成信息列表对话框 -->
      <a-modal
          v-model:visible="modelInfoListVisible"
          title="模型生成信息列表"
          @cancel="modelInfoListVisible = false"
          :footer="false"
          width="800px"
      >
        <a-table :data="modelInfoList" :bordered="false">
          <template #columns>
            <a-table-column title="iD" data-index="id"></a-table-column>
            <a-table-column title="任务ID" data-index="taskId">
              <template #cell="{ record }">
                <a-tooltip content="点击复制">
                  <div style="cursor: pointer" @click="copyToClipboard(record.taskId)">
                    {{ record.taskId }}
                  </div>
                </a-tooltip>
              </template>
            </a-table-column>
            <a-table-column title="状态" data-index="status">
              <template #cell="{ record }">
                <a-tag :color="record.status === 'success' ? 'green' : 'blue'">
                  {{ record.status === 'success' ? '已完成' : '生成中' }}
                </a-tag>
              </template>
            </a-table-column>
            <!-- 预览图列的显示方式 -->
            <a-table-column title="预览图" data-index="renderUrl">
                <template #cell="{ record }">
                    <img 
                        v-if="record.renderUrl"
                        :src="record.renderUrl"
                        :alt="record.taskId"
                        style="width: 100px; height: 100px; object-fit: cover; border-radius: 4px;"
                    />
                    <span v-else>暂无预览图</span>
                </template>
            </a-table-column>

            <a-table-column title="模型路径" data-index="pbrModelUrl">
              <template #cell="{ record }">
                <a-tooltip content="点击复制">
                  <div style="cursor: pointer" @click="copyToClipboard(record.pbrModelUrl)">
                    {{ record.pbrModelUrl }}
                  </div>
                </a-tooltip>
              </template>
            </a-table-column>
          </template>
        </a-table>
      </a-modal>

      <!-- 余额显示对话框 -->
      <a-modal v-model:visible="balanceVisible" @cancel="balanceVisible = false" title="余额查询">
        <p>当前余额：{{ currentBalance }}</p>
      </a-modal>

      <!-- 生成模型对话框 -->
      <a-modal
          v-model:visible="visible"
          title="生成模型"
          @ok="handleGenerate"
      >
        <div class="generate-form">
          <!-- 生成类型切换 -->
          <div class="generate-type">
            <a-radio-group v-model="generateType" type="button" class="mb-4">
              <a-radio value="text">文本生成</a-radio>
              <a-radio value="image">图片生成</a-radio>
            </a-radio-group>
          </div>

          <!-- 在模板中修改表单绑定 -->
          <a-form :model="generateType === 'text' ? textFormData : imageFormData">
            <!-- 文本生成表单 -->
            <template v-if="generateType === 'text'">
              <a-form-item label="生成提示">
                <a-textarea
                    v-model="textFormData.prompt"
                    placeholder="请输入生成提示，例如：生成一个青花瓷"
                    :auto-size="{ minRows: 4, maxRows: 8 }"
                    allow-clear
                />
              </a-form-item>
            </template>

            <!-- 图片生成表单 -->
            <template v-if="generateType === 'image'">
              <a-form-item label="生成提示">
                <a-textarea
                    v-model="imageFormData.prompt"
                    placeholder="请输入生成提示，例如：生成一个糖画"
                    :auto-size="{ minRows: 4, maxRows: 8 }"
                    allow-clear
                />
              </a-form-item>

              <a-form-item label="上传图片">
                <a-upload
                    v-model="imageFormData.file_token"
                    :max-count="1"
                    accept="image/*"
                    :show-file-list="false"
                    :custom-request="handleImageUpload"
                >
                    <template #upload-button>
                        <div v-if="!previewImage" class="upload-button">
                            <icon-upload />
                            <span>点击上传图片</span>
                        </div>
                        <div v-else class="preview-image">
                            <img :src="previewImage" style="width: 100px; height: 100px; object-fit: cover; border-radius: 4px" />
                        </div>
                    </template>
                </a-upload>
              </a-form-item>
            </template>
          </a-form>
        </div>
      </a-modal>

      <!-- 查询状态对话框 -->
      <a-modal
          v-model:visible="queryVisible"
          title="查询状态"
          @ok="handleQuery"
      >
        <div class="query-form">
          <a-form-item label="任务ID">
            <a-input
                v-model="taskId"
                placeholder="请输入任务ID"
                allow-clear
            />
          </a-form-item>
        </div>
      </a-modal>

      <!-- 查看模型对话框 -->
      <a-modal
          v-model:visible="viewVisible"
          title="查看模型"
          @ok="handleView"
      >
        <div class="view-form">
          <a-form-item label="模型连接">
            <a-input
                v-model="modelUrl"
                placeholder="请输入模型连接"
                allow-clear
            />
          </a-form-item>
        </div>
      </a-modal>

      <!-- 分类导航 -->
      <div class="categories">
        <a-space size="large">
          <a-link :class="{ active: true }" class="category-link">全部</a-link>
          <a-link>云南省</a-link>
          <a-link>其他</a-link>
        </a-space>
      </div>

      <!-- 热门标签 -->
      <div class="hot-tags">
        <a-space size="small">
          <a-tag class="custom-tag" hoverable>全部</a-tag>
          <a-tag class="custom-tag" hoverable>陶瓷</a-tag>
          <a-tag class="custom-tag" hoverable>服饰</a-tag>
          <a-tag class="custom-tag" hoverable>金属</a-tag>
          <a-tag class="custom-tag" hoverable>织绣</a-tag>
          <a-tag class="custom-tag" hoverable>雕刻</a-tag>
          <a-tag class="custom-tag" hoverable>编织</a-tag>
          <a-tag class="custom-tag" hoverable>漆器</a-tag>
          <a-tag class="custom-tag" hoverable>纸艺</a-tag>
          <a-tag class="custom-tag" hoverable>其他</a-tag>
        </a-space>
      </div>

      <!-- 模型列表 -->
      <div class="model-grid">
        <div v-for="model in models" :key="model.id" class="model-card" @click="handleModelClick(model.id)">
          <div class="model-image">
            <img :alt="model.name" :src="model.modelPic">
            <div class="model-overlay">
              <span class="view-detail">查看详情</span>
            </div>
          </div>
          <div class="model-info">
            <div class="model-name">{{ model.name }}</div>
            <div class="model-meta">
            <span class="author">
              <icon-user/> {{ model.author }}
            </span>
              <span class="views">
              <icon-eye/> {{ model.views }}
            </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div>
      <Footer class="footer"></Footer>
    </div>
  </div>

</template>

<style scoped>
.home {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.showroom {
  flex: 1;
  max-width: 100vw;
  padding: 40px;
  background-color: #fdf6e3; /* 更改为米色背景 */
  min-height: calc(100vh - 80px);
}

.categories {
  margin-bottom: 30px;
  padding: 15px 0;
  border-bottom: 1px solid #8b1f1f; /* 更改边框颜色为深红色 */
  position: sticky;
  top: 0;
  background: #fdf6e3;
  z-index: 100;
}

.category-link {
  font-size: 16px;
  padding: 6px 12px;
  border-radius: 4px;
  transition: all 0.3s;
  color: #8b1f1f; /* 更改链接颜色为深红色 */
}

.category-link:hover,
.category-link.active {
  background: rgba(139, 31, 31, 0.1); /* 更改悬浮背景为深红色透明版 */
  color: #8b1f1f;
}

.custom-tag {
  padding: 6px 12px;
  border-radius: 16px;
  transition: all 0.3s;
  cursor: pointer;
  border: 1px solid #8b1f1f; /* 添加深红色边框 */
  color: #8b1f1f;
}

.custom-tag:hover {
  background: rgba(139, 31, 31, 0.1);
  color: #8b1f1f;
}

.model-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 10px rgba(139, 31, 31, 0.1); /* 更改阴影颜色 */
}

.model-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 8px 20px rgba(139, 31, 31, 0.15);
}

.hot-tags {
  margin-bottom: 30px;
}

.custom-tag {
  padding: 6px 12px;
  border-radius: 16px;
  transition: all 0.3s;
  cursor: pointer;
}

.custom-tag:hover {
  background: var(--color-primary-light-1);
  color: var(--color-primary);
}

.model-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 30px;
  padding: 20px 0;
}

.model-card {
  background: var(--color-bg-2);
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.model-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.model-image {
  width: 100%;
  height: 220px;
  overflow: hidden;
  position: relative;
}

.model-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.model-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: all 0.3s;
}

.model-card:hover .model-overlay {
  opacity: 1;
}

.model-card:hover .model-image img {
  transform: scale(1.05);
}

.view-detail {
  color: white;
  font-size: 16px;
  padding: 8px 16px;
  border: 2px solid white;
  border-radius: 20px;
}

.model-info {
  padding: 16px;
}

.model-name {
  font-size: 18px;
  font-weight: 500;
  margin-bottom: 12px;
  color: #8b1f1f; /* 更改标题颜色 */
}

.model-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #666; /* 更改元信息颜色 */
  font-size: 14px;
}

.author, .views {
  display: flex;
  align-items: center;
  gap: 4px;
}

.generate-button-container {
  display: flex;
  gap: 16px;
  justify-content: flex-end;
  margin-bottom: 20px;
}

.generate-button {
  background-color: #8b1f1f;
  border-color: #8b1f1f;
}

.generate-button:hover {
  background-color: #a52424;
  border-color: #a52424;
}

.generate-button:disabled {
  background-color: rgba(139, 31, 31, 0.5);
  border-color: rgba(139, 31, 31, 0.5);
}

.generate-type{
  display: flex;
  justify-content: center;
}

.mb-4 {
  margin-bottom: 20px;
}

.upload-button {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  cursor: pointer;
}

.upload-button:hover {
  border-color: #8b1f1f;
  color: #8b1f1f;
}

</style>
