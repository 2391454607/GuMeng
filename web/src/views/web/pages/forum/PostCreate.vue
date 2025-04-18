<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { Message } from '@arco-design/web-vue';
import { getTopicsAPI, createPostAPI, getPostDetailAPI, updatePostAPI } from '@/api/forum';
import { useUserStore } from '@/stores';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

// 判断是新建还是编辑
const isEdit = computed(() => route.name === 'EditPost');
const postId = computed(() => route.params.id);

// 表单数据
const postForm = reactive({
  title: '',
  topic: '',
  content: ''
});

// 自定义话题相关
const useCustomTopic = ref(false);
const customTopicName = ref('');

// 表单校验规则
const rules = {
  title: [
    { required: true, message: '请输入标题' },
    { minLength: 2, maxLength: 50, message: '标题长度在2到50个字符之间' }
  ],
  topic: [
    { required: true, message: '请选择或输入话题' }
  ],
  content: [
    { required: true, message: '请输入内容' },
    { minLength: 10, maxLength: 2000, message: '内容长度在10到2000个字符之间' }
  ]
};

// 话题列表
const topics = ref([]);
const submitting = ref(false);
const postFormRef = ref(null);

// 切换话题输入模式
const toggleTopicInputMode = () => {
  useCustomTopic.value = !useCustomTopic.value;
  if (!useCustomTopic.value) {
    postForm.topic = '';
    customTopicName.value = '';
  } else {
    postForm.topic = customTopicName.value;
  }
};

// 获取话题列表
const fetchTopics = async () => {
  try {
    const res = await getTopicsAPI();
    if (res.code === 200) {
      topics.value = res.data || [];
    } else {
      Message.warning(res.msg || '获取话题失败');
    }
  } catch (err) {
    console.error('获取话题列表出错:', err);
    Message.error('获取话题失败，请稍后重试');
  }
};

// 如果是编辑模式，获取帖子详情
const fetchPostDetail = async () => {
  if (!isEdit.value) return;
  
  try {
    const res = await getPostDetailAPI(postId.value);
    if (res.code === 200) {
      const post = res.data;
      
      postForm.title = post.title;
      
      // 检查是否是预设话题
      const foundTopic = topics.value.find(t => t.id == post.topicId);
      if (foundTopic) {
        postForm.topic = post.topicId;
        useCustomTopic.value = false;
      } else {
        // 自定义话题
        customTopicName.value = post.topic || '';
        postForm.topic = post.topic || '';
        useCustomTopic.value = true;
      }
      
      postForm.content = post.content;
    } else {
      Message.error(res.msg || '获取帖子详情失败');
      goBack();
    }
  } catch (err) {
    console.error('获取帖子详情出错:', err);
    Message.error('获取帖子详情失败，请稍后重试');
    goBack();
  }
};

// 提交表单
const submitForm = async () => {
  if (!userStore.isLogin) {
    Message.warning('请先登录再发布帖子');
    return;
  }
  
  // 表单验证
  if (!postFormRef.value) {
    Message.warning('表单引用不存在');
    return;
  }
  
  submitting.value = true;
  
  try {
    // 手动验证表单，避免使用validate()方法可能导致的问题
    let isValid = true;
    
    // 手动验证标题
    if (!postForm.title || postForm.title.length < 2 || postForm.title.length > 50) {
      isValid = false;
      Message.error('标题长度在2到50个字符之间');
    }
    
    // 手动验证话题
    if (useCustomTopic.value) {
      if (!customTopicName.value || customTopicName.value.trim().length < 2) {
        isValid = false;
        Message.error('自定义话题长度至少为2个字符');
      }
    } else if (!postForm.topic) {
      isValid = false;
      Message.error('请选择话题');
    }
    
    // 手动验证内容
    if (!postForm.content || postForm.content.length < 10 || postForm.content.length > 2000) {
      isValid = false;
      Message.error('内容长度在10到2000个字符之间');
    }
    
    if (!isValid) {
      submitting.value = false;
      return;
    }
    
    // 准备提交数据前确保话题数据正确
    let topicValue;
    if (useCustomTopic.value) {
      topicValue = customTopicName.value.trim();
    } else {
      topicValue = postForm.topic;
    }
    
    const formData = {
      title: postForm.title,
      content: postForm.content,
      topic: topicValue
    };
    
    console.log('提交表单数据:', formData);
    
    let res;
    
    if (isEdit.value) {
      res = await updatePostAPI(postId.value, formData);
    } else {
      res = await createPostAPI(formData);
    }
    
    if (res.code === 200) {
      Message.success(isEdit.value ? '帖子更新成功' : '帖子发布成功');
      // 跳转到详情页或列表页
      if (isEdit.value) {
        router.push(`/forum/detail/${postId.value}`);
      } else {
        router.push(`/forum/detail/${res.data}`);
      }
    } else {
      Message.error(res.msg || (isEdit.value ? '更新失败' : '发布失败'));
    }
  } catch (err) {
    console.error(isEdit.value ? '更新帖子出错:' : '发布帖子出错:', err);
    Message.error(isEdit.value ? '更新失败，请稍后重试' : '发布失败，请稍后重试');
  } finally {
    submitting.value = false;
  }
};

// 返回上一页
const goBack = () => {
  router.push('/forum/list');
};

// 监听器
watch(customTopicName, (newVal) => {
  if (useCustomTopic.value) {
    postForm.topic = newVal;
  }
});

watch(() => postForm.topic, (newVal) => {
  if (newVal === 'custom') {
    useCustomTopic.value = true;
    postForm.topic = customTopicName.value;
  }
});

onMounted(() => {
  fetchTopics();
  fetchPostDetail();
});
</script>

<template>
  <div class="create-post-container">
    <div class="page-header">
      <a-button @click="goBack" class="back-btn">
        <i class="iconfont icon-arrow-left"></i> 返回
      </a-button>
      <div class="header-title">{{ isEdit ? '编辑帖子' : '发布帖子' }}</div>
    </div>
    
    <a-form
      ref="postFormRef"
      :model="postForm"
      :rules="rules"
      label-position="top"
      class="post-form"
    >
      <a-form-item label="标题" field="title">
        <a-input 
          v-model="postForm.title"
          placeholder="请输入帖子标题（2-50字）"
          :maxLength="50"
          show-word-limit
          class="custom-input"
        />
      </a-form-item>
      
      <a-form-item label="话题" field="topic">
        <div class="topic-input-container">
          <a-select 
            v-if="!useCustomTopic"
            v-model="postForm.topic"
            placeholder="请选择话题分类"
            class="topic-select custom-select"
            allow-clear
          >
            <a-option
              v-for="topic in topics"
              :key="topic.id"
              :label="topic.name"
              :value="topic.id"
            />
            <a-option value="custom" label="自定义话题..."></a-option>
          </a-select>
          <a-input 
            v-else
            v-model="customTopicName"
            placeholder="请输入自定义话题（2-20字）"
            class="custom-topic-input custom-input"
            :maxLength="20"
            show-word-limit
          />
          <a-button 
            class="toggle-topic-btn" 
            @click="toggleTopicInputMode"
            type="outline"
          >
            {{ useCustomTopic ? '选择预设话题' : '自定义话题' }}
          </a-button>
        </div>
      </a-form-item>
      
      <a-form-item label="内容" field="content">
        <a-textarea
          v-model="postForm.content"
          :rows="10"
          placeholder="请输入帖子内容（10-2000字）"
          :maxLength="2000"
          show-word-limit
          class="custom-textarea"
        />
      </a-form-item>
      
      <a-form-item>
        <div class="action-buttons">
          <a-button type="primary" @click="submitForm" :loading="submitting" class="submit-btn">
            {{ isEdit ? '保存修改' : '发布帖子' }}
          </a-button>
          <a-button @click="goBack" class="cancel-btn">取消</a-button>
        </div>
      </a-form-item>
    </a-form>
  </div>
</template>

<style scoped>
.create-post-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  background-color: #F9F3E9; /* 米色纸张质感背景 */
  font-family: "SimSun", "宋体", serif; /* 使用宋体增加复古感 */
}

.page-header {
  margin-bottom: 20px;
  background-color: #8C1F28; /* 暗红色 */
  padding: 16px 20px;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: center;
  color: #F9F3E9;
  position: relative;
}

.back-btn {
  margin-right: 16px;
  background-color: transparent;
  border: 1px solid #F9F3E9;
  color: #F9F3E9;
}

.back-btn:hover {
  background-color: rgba(249, 243, 233, 0.1);
}

.header-title {
  font-size: 20px;
  font-weight: bold;
  flex: 1;
  text-align: center;
  letter-spacing: 2px;
}

.post-form {
  background-color: #FFFBF0; /* 浅米色 */
  padding: 30px;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border: 1px solid #D6C6AF; /* 棕边框 */
}

.topic-input-container {
  display: flex;
  gap: 10px;
  align-items: center;
}

.topic-select, .custom-topic-input {
  flex: 1;
}

.toggle-topic-btn {
  flex-shrink: 0;
  background-color: #D4A373; /* 木色 */
  color: #582F0E; /* 深棕色 */
  border: 1px solid #A77E58;
}

.toggle-topic-btn:hover {
  background-color: #C68B59; /* 深木色 */
  color: #F9F3E9;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 20px;
}

/* 自定义输入框样式 */
.custom-input, .custom-textarea, .custom-select {
  border: 1px solid #D6C6AF !important;
  background-color: #FFFDF7 !important;
  color: #582F0E !important;
  border-radius: 4px !important;
  font-family: "SimSun", "宋体", serif !important;
}

.custom-input:focus, .custom-textarea:focus, .custom-select:focus {
  border-color: #8C1F28 !important;
  box-shadow: 0 0 0 2px rgba(140, 31, 40, 0.1) !important;
}

/* 表单标签样式 */
.post-form :deep(.arco-form-item-label) {
  font-weight: 500;
  color: #582F0E;
  font-size: 16px;
  margin-bottom: 8px;
  font-family: "STKaiti", "楷体", serif;
}

/* 按钮样式 */
.submit-btn {
  background-color: #8C1F28 !important;
  border-color: #8C1F28 !important;
  border-radius: 4px;
  padding: 0 30px;
  height: 40px;
  font-size: 16px;
  font-weight: 500;
  letter-spacing: 1px;
}

.submit-btn:hover {
  background-color: #A52A2A !important;
  border-color: #A52A2A !important;
}

.cancel-btn {
  border-radius: 4px;
  padding: 0 30px;
  height: 40px;
  font-size: 16px;
  border: 1px solid #D6C6AF;
  color: #582F0E;
  background-color: #F9F3E9;
}

.cancel-btn:hover {
  border-color: #8C1F28;
  color: #8C1F28;
  background-color: #FFFBF0;
}

/* 复古纸张质感效果 */
.post-form::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: url("data:image/svg+xml,%3Csvg width='100' height='100' viewBox='0 0 100 100' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='%23D6C6AF' fill-opacity='0.1'%3E%3Cpath d='M11 18c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm48 25c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm-43-7c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm63 31c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM34 90c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm56-76c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM12 86c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm28-65c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm23-11c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-6 60c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm29 22c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zM32 63c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm57-13c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-9-21c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM60 91c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM35 41c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM12 60c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2z'/%3E%3C/g%3E%3C/svg%3E");
  z-index: -1;
  opacity: 0.3;
}

/* 响应式设计 */
@media screen and (max-width: 768px) {
  .create-post-container {
    padding: 10px;
  }
  
  .post-form {
    padding: 20px;
  }
  
  .topic-input-container {
    flex-direction: column;
    align-items: stretch;
  }
  
  .toggle-topic-btn {
    margin-top: 8px;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 10px;
  }
  
  .submit-btn, .cancel-btn {
    width: 100%;
  }
}
</style>