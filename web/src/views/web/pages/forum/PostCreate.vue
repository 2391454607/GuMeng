<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { Message } from '@arco-design/web-vue';
import { getTopicsAPI, createPostAPI, getPostDetailAPI, updatePostAPI, checkSensitiveWordsAPI, uploadImageAPI } from '@/api/forum';
import { useUserStore } from '@/stores/userStore.js';
import Footer from "@/views/web/layout/Footer.vue";

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
  content: '',
  images: [] // 添加图片数组字段
});

// 图片上传相关
const fileInputRef = ref(null);
const uploading = ref(false);
const imageFiles = ref([]); // 存储选择的图片文件
const previewImages = ref([]); // 存储图片预览URL
const uploadProgress = ref(0); // 上传进度

// 敏感词相关
const checkingSensitiveWords = ref(false);
const sensitiveWordsError = ref('');

// 表单校验规则
const rules = {
  title: [
    { required: true, message: '请输入标题' },
    { minLength: 2, maxLength: 50, message: '标题长度在2到50个字符之间' }
  ],
  topic: [
    { required: true, message: '请选择话题' }
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
      
      // 检查话题并设置
      const foundTopic = topics.value.find(t => t.name === post.topic);
      if (foundTopic) {
        postForm.topic = foundTopic.id;
      } else {
        // 如果找不到对应话题，尝试查找默认话题
        const defaultTopic = topics.value.length > 0 ? topics.value[0].id : '';
        postForm.topic = defaultTopic;
      }
      
      postForm.content = post.content;
      
      // 处理已有的图片
      if (post.images && post.images.length > 0) {
        // 如果是字符串，分割成数组
        if (typeof post.images === 'string') {
          postForm.images = post.images.split(',').filter(img => img);
        } else {
          postForm.images = post.images;
        }
        
        // 设置预览图片
        previewImages.value = [...postForm.images];
      }
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

// 触发文件选择
const triggerFileInput = () => {
  if (fileInputRef.value) {
    fileInputRef.value.click();
  }
};

// 处理图片选择
const handleFileChange = (event) => {
  const files = event.target.files;
  if (!files.length) return;
  
  // 检查已有图片数量
  if (previewImages.value.length + files.length > 9) {
    Message.warning('最多只能上传9张图片');
    return;
  }
  
  // 检查文件类型和大小
  const validFiles = Array.from(files).filter(file => {
    const isImage = file.type.startsWith('image/');
    const isValidSize = file.size <= 5 * 1024 * 1024; // 5MB限制
    
    if (!isImage) {
      Message.warning(`文件 ${file.name} 不是有效的图片格式`);
    }
    
    if (!isValidSize) {
      Message.warning(`文件 ${file.name} 超过5MB大小限制`);
    }
    
    return isImage && isValidSize;
  });
  
  if (!validFiles.length) return;
  
  // 添加到文件列表
  imageFiles.value = [...imageFiles.value, ...validFiles];
  
  // 创建预览
  validFiles.forEach(file => {
    const reader = new FileReader();
    reader.onload = (e) => {
      previewImages.value.push(e.target.result);
    };
    reader.readAsDataURL(file);
  });
  
  // 清除input的value，允许重复选择同一文件
  event.target.value = '';
};

// 删除预览图片
const removeImage = (index) => {
  // 如果是已上传的图片
  if (index < postForm.images.length) {
    postForm.images.splice(index, 1);
  }
  
  // 如果是新选择的图片
  const newImageIndex = index - postForm.images.length;
  if (newImageIndex >= 0 && newImageIndex < imageFiles.value.length) {
    imageFiles.value.splice(newImageIndex, 1);
  }
  
  // 无论是哪种情况，都从预览中移除
  previewImages.value.splice(index, 1);
};

// 上传图片
const uploadImages = async () => {
  if (!imageFiles.value.length) return [];
  
  try {
    uploading.value = true;
    const uploadedUrls = [];
    
    // 逐个上传文件
    for (let i = 0; i < imageFiles.value.length; i++) {
      const file = imageFiles.value[i];
      
      // 创建FormData对象
      const formData = new FormData();
      formData.append('file', file);
      
      // 上传到服务器
      const res = await uploadImageAPI(formData);
      
      if (res.code === 200 && res.data) {
        uploadedUrls.push(res.data);
      } else {
        Message.warning(`图片 ${file.name} 上传失败`);
      }
      
      // 更新进度
      uploadProgress.value = Math.round(((i + 1) / imageFiles.value.length) * 100);
    }
    
    return uploadedUrls;
  } catch (err) {
    console.error('上传图片出错:', err);
    Message.error('上传图片失败，请稍后重试');
    return [];
  } finally {
    uploading.value = false;
    uploadProgress.value = 0;
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
    // 手动验证表单
    let isValid = true;
    
    // 手动验证标题
    if (!postForm.title || postForm.title.length < 2 || postForm.title.length > 50) {
      isValid = false;
      Message.error('标题长度在2到50个字符之间');
    }
    
    // 手动验证话题
    if (!postForm.topic) {
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
    
    // 准备提交前检查敏感词
    checkingSensitiveWords.value = true;
    sensitiveWordsError.value = '';
    
    // 合并标题和内容进行检查
    const textToCheck = postForm.title + ' ' + postForm.content;
    
    try {
      const checkResult = await checkSensitiveWordsAPI({ text: textToCheck });
      console.log('敏感词检查结果:', checkResult);
      
      if (checkResult.code === 200 && checkResult.data.containsSensitiveWords) {
        const sensitiveWords = checkResult.data.sensitiveWords || [];
        sensitiveWordsError.value = `内容包含敏感词: ${sensitiveWords.join(', ')}，请修改后重新提交`;
        Message.error(sensitiveWordsError.value);
        submitting.value = false;
        checkingSensitiveWords.value = false;
        return;
      }
    } catch (err) {
      console.error('敏感词检查出错:', err);
      // 敏感词检查出错，继续提交流程
    } finally {
      checkingSensitiveWords.value = false;
    }
    
    // 上传新选择的图片
    let newUploadedImages = [];
    if (imageFiles.value.length > 0) {
      newUploadedImages = await uploadImages();
      if (!newUploadedImages.length && imageFiles.value.length > 0) {
        Message.error('图片上传失败，请稍后重试');
        submitting.value = false;
        return;
      }
    }
    
    // 合并已有图片和新上传的图片
    const allImages = [...postForm.images, ...newUploadedImages];
    
    // 准备话题数据
    // 查找选定话题的名称
    const selectedTopic = topics.value.find(t => t.id === postForm.topic);
    const topicValue = selectedTopic ? selectedTopic.name : '';
    
    const formData = {
      title: postForm.title,
      content: postForm.content,
      topic: topicValue,
      images: allImages.join(',') // 将图片URL数组转换为逗号分隔的字符串
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

onMounted(() => {
  fetchTopics();
  fetchPostDetail();
});
</script>

<template>
  <div>
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
            </a-select>
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
        
        <!-- 图片上传区域 -->
        <a-form-item label="添加图片">
          <div class="image-upload-container">
            <div class="image-previews">
              <div 
                v-for="(image, index) in previewImages" 
                :key="index"
                class="image-preview-item"
              >
                <img :src="image" alt="预览图片" class="preview-image" />
                <div class="image-actions">
                  <a-button 
                    type="primary" 
                    status="danger" 
                    size="mini" 
                    circle 
                    class="delete-btn"
                    @click.stop="removeImage(index)"
                  >
                    <i class="icon-delete"></i>
                    <span class="delete-icon">×</span>
                  </a-button>
                </div>
              </div>
              
              <div 
                v-if="previewImages.length < 9"
                class="image-upload-trigger"
                @click="triggerFileInput"
              >
                <i class="upload-icon">+</i>
                <div class="upload-text">添加图片</div>
              </div>
            </div>
            
            <input 
              type="file" 
              ref="fileInputRef"
              accept="image/*" 
              multiple 
              class="file-input" 
              @change="handleFileChange"
            />
            
            <div class="upload-tips">
              支持.jpg/.png/.gif等格式，单张图片不超过5MB，最多上传9张
            </div>
            
            <!-- 上传进度条 -->
            <div v-if="uploading" class="upload-progress">
              <a-progress :percent="uploadProgress" />
              <div class="progress-text">正在上传图片: {{ uploadProgress }}%</div>
            </div>
          </div>
        </a-form-item>
        
        <!-- 敏感词错误提示 -->
        <a-form-item v-if="sensitiveWordsError">
          <a-alert type="error" :content="sensitiveWordsError" />
        </a-form-item>
        
        <a-form-item>
          <div class="action-buttons">
            <a-button 
              type="primary" 
              @click="submitForm" 
              :loading="submitting || uploading" 
              :disabled="submitting || uploading"
              class="submit-btn"
            >
              {{ isEdit ? '保存修改' : '发布帖子' }}
            </a-button>
            <a-button @click="goBack" class="cancel-btn">取消</a-button>
          </div>
        </a-form-item>
      </a-form>
    </div>
    
    <!-- 页脚 -->
    <Footer class="footer"></Footer>
  </div>
</template>

<style scoped>
.create-post-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  background-color: #fffbf0; /* 修改背景颜色 */
  font-family: "SimSun", "宋体", serif; /* 使用宋体增加复古感 */
  min-height: calc(100vh - 176px);
  display: flex;
  flex-direction: column;
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
  max-width: 800px;
  margin: 0 auto;
}

.topic-input-container {
  display: flex;
  gap: 10px;
  align-items: center;
}

.topic-select {
  width: 100%;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 20px;
}

/* 图片上传相关样式 */
.image-upload-container {
  margin-bottom: 15px;
}

.image-previews {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 15px;
}

.image-preview-item {
  width: 120px;
  height: 120px;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
  border: 1px solid #D6C6AF;
  background-color: #FFFDF7;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-actions {
  position: absolute;
  top: 5px;
  right: 5px;
  display: flex;
  gap: 5px;
}

.delete-btn {
  width: 24px;
  height: 24px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(140, 31, 40, 0.8);
  border-color: transparent;
}

.delete-icon {
  font-size: 16px;
  line-height: 1;
}

.image-upload-trigger {
  width: 120px;
  height: 120px;
  border: 1px dashed #D6C6AF;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  background-color: #FFFDF7;
  transition: all 0.3s;
}

.image-upload-trigger:hover {
  border-color: #8C1F28;
  background-color: #FFF7E9;
}

.upload-icon {
  font-size: 28px;
  color: #8C1F28;
  margin-bottom: 5px;
}

.upload-text {
  font-size: 12px;
  color: #582F0E;
}

.file-input {
  display: none;
}

.upload-tips {
  font-size: 12px;
  color: #7F4F24;
  margin-top: 5px;
}

.upload-progress {
  margin-top: 15px;
}

.progress-text {
  font-size: 14px;
  color: #582F0E;
  margin-top: 5px;
  text-align: center;
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
  
  .action-buttons {
    flex-direction: column;
    gap: 10px;
  }
  
  .submit-btn, .cancel-btn {
    width: 100%;
  }
  
  .image-previews {
    gap: 10px;
  }
  
  .image-preview-item,
  .image-upload-trigger {
    width: calc(33.333% - 7px);
    height: 100px;
  }
}

.footer{
  display: flex;
  bottom: 0;
}
</style>