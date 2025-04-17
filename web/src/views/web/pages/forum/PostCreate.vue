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
        // 可能是自定义话题
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
    <a-page-header @back="goBack" :title="isEdit ? '编辑帖子' : '发布帖子'" class="page-header">
      <template #default>
        <span class="header-title">{{ isEdit ? '编辑帖子' : '发布帖子' }}</span>
      </template>
    </a-page-header>
    
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
        />
      </a-form-item>
      
      <a-form-item label="话题" field="topic">
        <div class="topic-input-container">
          <a-select 
            v-if="!useCustomTopic"
            v-model="postForm.topic"
            placeholder="请选择话题分类"
            class="topic-select"
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
            class="custom-topic-input"
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
        />
      </a-form-item>
      
      <a-form-item>
        <a-button type="primary" @click="submitForm" :loading="submitting" class="submit-btn">
          {{ isEdit ? '保存修改' : '发布帖子' }}
        </a-button>
        <a-button @click="goBack" class="cancel-btn">取消</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<style scoped>
.create-post-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  background-color: #f7f8fa;
}

.page-header {
  margin-bottom: 20px;
  background-color: #fff;
  padding: 16px 20px;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
}

.header-title {
  font-size: 20px;
  font-weight: 600;
  color: #1d2129;
  margin-left: 10px;
}

.post-form {
  background-color: #fff;
  padding: 30px;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
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
  background-color: #f2f3f5;
  color: #4e5969;
  border: 1px solid #e5e6eb;
}

.toggle-topic-btn:hover {
  background-color: #e5e6eb;
  color: #1d2129;
}

/* Arco组件样式覆盖 */
.post-form :deep(.arco-form-item-label) {
  font-weight: 500;
  color: #1d2129;
  font-size: 15px;
  margin-bottom: 8px;
}

.post-form :deep(.arco-input),
.post-form :deep(.arco-textarea),
.post-form :deep(.arco-select-view) {
  border-radius: 6px;
  border-color: #e5e6eb;
  transition: all 0.2s;
  padding: 8px 12px;
}

.post-form :deep(.arco-input:focus),
.post-form :deep(.arco-textarea:focus),
.post-form :deep(.arco-select-view:focus) {
  border-color: #165dff;
  box-shadow: 0 0 0 2px rgba(22, 93, 255, 0.1);
}

.submit-btn {
  background-color: #165dff;
  border-color: #165dff;
  border-radius: 6px;
  padding: 0 20px;
  height: 40px;
  transition: all 0.2s;
  font-size: 16px;
  font-weight: 500;
  margin-right: 10px;
}

.submit-btn:hover {
  background-color: #3c7eff;
  border-color: #3c7eff;
}

.cancel-btn {
  border-radius: 6px;
  padding: 0 20px;
  height: 40px;
  font-size: 16px;
  border: 1px solid #e5e6eb;
  color: #4e5969;
}

.cancel-btn:hover {
  border-color: #c9cdd4;
  color: #1d2129;
  background-color: #f7f8fa;
}

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
}
</style>