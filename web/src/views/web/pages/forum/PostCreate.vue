<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { Message } from '@arco-design/web-vue';
import { getTopicsAPI, createPostAPI, getPostDetailAPI, updatePostAPI, checkSensitiveWordsAPI, uploadImageAPI } from '@/api/forum';
import { useUserStore } from '@/stores/userStore.js';
import Footer from "@/views/web/layout/Footer.vue";

// 导入自定义Markdown编辑器和查看器
import { Editor, Viewer } from '@/views/web/pages/forum/bytemd';
// 导入ByteMD插件
import gfm from '@bytemd/plugin-gfm'
import highlight from '@bytemd/plugin-highlight'
import gemoji from '@bytemd/plugin-gemoji'
import zhHans from 'bytemd/locales/zh_Hans.json'
// 导入ByteMD样式
import 'bytemd/dist/index.css'

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

// ByteMD插件
const plugins = [
  gfm(),
  highlight(),
  gemoji(),
]

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

// ByteMD编辑器上传图片处理函数
const handleUploadBytemdImages = async (files) => {
  try {
    // 检查已有图片数量
    const imageRegex = /!\[.*?\]\((.*?)\)/g;
    const matches = [...(editorValue.value || '').matchAll(imageRegex)];
    const currentImages = matches.length;
    
    if (currentImages + files.length > 9) {
      Message.warning('最多只能上传9张图片');
      return [];
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
    
    if (!validFiles.length) return [];
    
    // 开始上传
    uploading.value = true;
    const urls = [];
    
    // 逐个上传文件
    for (let i = 0; i < validFiles.length; i++) {
      const file = validFiles[i];
      
      // 创建FormData对象
      const formData = new FormData();
      formData.append('file', file);
      
      try {
        // 上传到服务器
        const res = await uploadImageAPI(formData);
        
        if (res.code === 200 && res.data) {
          // 将URL添加到结果数组 - 这些URL将直接被ByteMD使用
          urls.push(res.data);
          
          // 确保URL也被添加到表单数据中
          if (!postForm.images.includes(res.data)) {
            postForm.images.push(res.data);
          }
        } else {
          Message.warning(`图片 ${file.name} 上传失败`);
        }
      } catch (error) {
        console.error('上传单张图片失败:', error);
        Message.error(`图片 ${file.name} 上传失败`);
      }
      
      // 更新进度
      uploadProgress.value = Math.round(((i + 1) / validFiles.length) * 100);
    }
    
    uploading.value = false;
    uploadProgress.value = 0;
    
    // 返回URL数组，ByteMD将使用这些URL创建图片Markdown语法
    return urls;
  } catch (err) {
    console.error('上传编辑器图片出错:', err);
    Message.error('上传图片失败，请稍后重试');
    uploading.value = false;
    uploadProgress.value = 0;
    return [];
  }
}

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
    { minLength: 10, maxLength: 10000, message: '内容长度在10到10000个字符之间' }
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
      // 同时更新editorValue      
      editorValue.value = post.content;
      
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
    if (!postForm.content || postForm.content.length < 10 || postForm.content.length > 10000) {
      isValid = false;
      Message.error('内容长度在10到10000个字符之间');
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
    
    // 提取内容中所有的图片URL，确保图片不重复提交
    const imageRegex = /!\[.*?\]\((.*?)\)/g;
    const contentMatches = [...(postForm.content.matchAll(imageRegex))];
    const contentImageUrls = contentMatches.map(match => match[1]);
    
    // 合并所有图片URL并去重
    const allImageUrls = [...new Set([...postForm.images, ...newUploadedImages, ...contentImageUrls])];
    
    // 过滤出服务器的图片地址（即去除base64和其他非服务器图片）
    const serverImageUrls = allImageUrls.filter(url => 
      url.startsWith('http') && !url.includes('base64')
    );
    
    // 准备话题数据
    // 查找选定话题的名称
    const selectedTopic = topics.value.find(t => t.id === postForm.topic);
    const topicValue = selectedTopic ? selectedTopic.name : '';
    
    const formData = {
      title: postForm.title,
      content: postForm.content, // 保持原始Markdown格式
      topic: topicValue,
      images: serverImageUrls.join(',') // 将去重后的图片URL数组转换为逗号分隔的字符串
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

// 创建预览区域
const editorValue = ref('');  // 用于存储编辑器内容
// 计算编辑器内容的字数
const contentWordCount = computed(() => {
  // 如果没有内容返回0
  if (!editorValue.value) return 0;
  
  // 去除markdown图片语法后计算字数
  const textOnly = editorValue.value.replace(/!\[.*?\]\(.*?\)/g, '图片');
  return textOnly.length;
});

// 监听编辑器内容变化
watch(editorValue, (newVal) => {
  postForm.content = newVal;
  console.log('编辑器内容已更新，字数:', contentWordCount.value);
}, { immediate: true, deep: true });

// 处理编辑器内容变化的事件处理函数
const handleEditorChange = (value) => {
  editorValue.value = value;
  postForm.content = value;
  console.log('编辑器内容变更，当前字数:', value.length);
  
  // 手动更新状态栏字数统计
  updateWordCount();
};

// 更新编辑器状态栏的字数统计
const updateWordCount = () => {
  setTimeout(() => {
    const statusElement = document.querySelector('.bytemd-status-left');
    if (statusElement) {
      // 去除图片标记后的内容长度
      const textContent = editorValue.value.replace(/!\[.*?\]\(.*?\)/g, '图片');
      statusElement.innerHTML = `字数: ${textContent.length}`;
    }
  }, 10);
};

// 处理编辑器标题变化
const handleTitleChange = (value) => {
  postForm.title = value;
};

// 处理编辑器话题变化
const handleTopicChange = (value) => {
  postForm.topic = value;
};

onMounted(() => {
  fetchTopics();
  fetchPostDetail();
  
  // 初始化时设置状态栏
  updateWordCount();
});
</script>

<template>
  <div class="create-post-page">
    <div class="create-post-container">
      <div class="page-header">
        <div class="header-left">
          <a-button @click="goBack" class="back-btn">
            <i class="iconfont icon-arrow-left"></i> 返回
          </a-button>
        </div>
        <div class="header-title">{{ isEdit ? '编辑帖子' : '发布帖子' }}</div>
        <div class="header-right">
          <!-- 只保留发布按钮 -->
          <a-button 
            type="primary" 
            @click="submitForm" 
            :loading="submitting || uploading" 
            :disabled="submitting || uploading"
            class="submit-btn"
          >
            {{ isEdit ? '保存修改' : '发布帖子' }}
          </a-button>
        </div>
      </div>
      
      <div class="main-content">
        <a-form
          ref="postFormRef"
          :model="postForm"
          :rules="rules"
          class="post-form"
        >
          <!-- 集成式Markdown编辑器 -->
          <a-form-item field="content" class="editor-form-item">
            <div class="markdown-editor-wrapper">
              <Editor
                :value="editorValue"
                :plugins="plugins"
                :locale="zhHans"
                placeholder="请输入帖子内容（支持Markdown格式）"
                :uploadImages="handleUploadBytemdImages"
                @change="handleEditorChange"
                :split="true"
                :title="postForm.title"
                :topicId="postForm.topic"
                :topics="topics"
                @titleChange="handleTitleChange"
                @topicChange="handleTopicChange"
              />
            </div>
          </a-form-item>
        </a-form>
        
        <!-- 敏感词错误提示，保留该部分 -->
        <div class="sidebar" v-if="sensitiveWordsError || uploading">
          <div class="sidebar-content">
            <!-- 上传进度条 -->
            <div v-if="uploading" class="upload-progress">
              <a-progress :percent="uploadProgress" />
              <div class="progress-text">正在上传图片: {{ uploadProgress }}%</div>
            </div>
            
            <!-- 敏感词错误提示 -->
            <div v-if="sensitiveWordsError" class="sensitive-words-alert">
              <a-alert type="error" :content="sensitiveWordsError" />
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 页脚 -->
    <Footer class="footer"></Footer>
  </div>
</template>

<style scoped>
.create-post-page {
  width: 100%;
  min-height: 100vh;
  background-color: #FFF7E9;
  background-image: url("data:image/svg+xml,%3Csvg width='100' height='100' viewBox='0 0 100 100' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='%23D6C6AF' fill-opacity='0.05'%3E%3Cpath d='M11 18c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm48 25c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm-43-7c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm63 31c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM34 90c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm56-76c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM12 86c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm28-65c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm23-11c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-6 60c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm29 22c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zM32 63c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm57-13c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-9-21c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM60 91c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM35 41c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM12 60c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2z'/%3E%3C/g%3E%3C/svg%3E");
  overflow-x: hidden;
}

.create-post-container {
  width: 98%;
  max-width: 1900px;
  margin: 0 auto;
  padding: 0;
  display: flex;
  flex-direction: column;
}

.page-header {
  background-color: #8C1F28;
  padding: 14px 20px;
  display: flex;
  align-items: center;
  color: #F9F3E9;
  position: relative;
  z-index: 10;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  margin-bottom: 15px;
  border-radius: 0 0 8px 8px;
}

.header-left {
  width: 80px;
}

.header-title {
  font-size: 20px;
  font-weight: bold;
  flex: 1;
  text-align: center;
  letter-spacing: 2px;
  font-family: "STKaiti", "楷体", serif;
}

.header-right {
  width: 120px;
  display: flex;
  justify-content: flex-end;
}

.back-btn {
  background-color: transparent;
  border: 1px solid #F9F3E9;
  color: #F9F3E9;
}

.back-btn:hover {
  background-color: rgba(249, 243, 233, 0.1);
}

/* 主内容区布局 */
.main-content {
  display: flex;
  height: calc(100vh - 130px);
  position: relative;
}

.post-form {
  flex: 1;
  overflow: hidden;
  margin-right: 15px;
  min-width: 0; /* 确保flex子项不会溢出 */
}

.editor-form-item {
  height: 100%;
}

/* 移除表单项内边距 */
:deep(.arco-form-item-content) {
  margin-bottom: 0;
}

.markdown-editor-wrapper {
  height: 100%;
  overflow: hidden;
  border: 1px solid #D6C6AF;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  min-height: 550px !important; /* 确保编辑器容器有足够的高度 */
}

/* 确保ByteMD编辑器不会被缩放行为影响 */
:deep(.bytemd) {
  height: 100% !important;
  min-height: 500px !important;
  max-height: none !important;
  resize: none !important; /* 禁用调整大小功能 */
}

:deep(.CodeMirror) {
  height: auto !important;
  min-height: 500px !important;
}

:deep(.bytemd-preview) {
  height: auto !important;
  min-height: 500px !important;
}

/* 确保编辑与预览区域高度固定 */
:deep(.bytemd-split .bytemd-editor),
:deep(.bytemd-split .bytemd-preview) {
  height: 500px !important;
  min-height: 500px !important;
  overflow-y: auto !important;
}

/* 侧边栏样式 */
.sidebar {
  width: 300px;
  background-color: #FFFDF7;
  overflow-y: auto;
  padding: 0;
  flex-shrink: 0;
  margin-left: 15px;
}

.sidebar-content {
  padding: 15px;
  background-color: #FFFDF7;
  border-radius: 8px;
  border: 1px solid #D6C6AF;
}

.sidebar-title {
  font-family: "STKaiti", "楷体", serif;
  color: #582F0E;
  margin-top: 0;
  margin-bottom: 15px;
  border-bottom: 1px solid #E4D9C3;
  padding-bottom: 10px;
}

/* 图片上传相关样式 */
.image-upload-container {
  margin-bottom: 20px;
}

.image-previews {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 15px;
}

.image-preview-item {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
  border: 1px solid #D6C6AF;
  background-color: #FFFDF7;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s;
}

.image-preview-item:hover {
  transform: scale(1.05);
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
  width: 20px;
  height: 20px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(140, 31, 40, 0.8);
  border-color: transparent;
}

.delete-icon {
  font-size: 14px;
  line-height: 1;
}

.image-upload-trigger {
  width: 80px;
  height: 80px;
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
  font-size: 24px;
  color: #8C1F28;
  margin-bottom: 2px;
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
  margin-bottom: 15px;
}

.progress-text {
  font-size: 14px;
  color: #582F0E;
  margin-top: 5px;
  text-align: center;
}

/* 敏感词警告 */
.sensitive-words-alert {
  margin-top: 0;
}

/* 按钮样式 */
.submit-btn {
  background-color: #8C1F28 !important;
  border-color: #8C1F28 !important;
  border-radius: 4px;
  padding: 0 16px;
  height: 36px;
  font-size: 14px;
  font-weight: 500;
  box-shadow: 0 2px 4px rgba(140, 31, 40, 0.2);
  transition: all 0.3s ease;
  margin-top: 4px;
}

.submit-btn:hover {
  background-color: #A52A2A !important;
  border-color: #A52A2A !important;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(140, 31, 40, 0.3);
}

.footer {
  display: flex;
  justify-content: center;
  margin-top: auto;
  padding: 10px 0;
  background-color: transparent;
}

/* Markdown预览样式增强 */
:deep(.markdown-body) {
  font-family: "SimSun", "宋体", serif;
  color: #582F0E;
}

:deep(.markdown-body h1),
:deep(.markdown-body h2),
:deep(.markdown-body h3),
:deep(.markdown-body h4),
:deep(.markdown-body h5),
:deep(.markdown-body h6) {
  color: #8C1F28;
  font-family: "STKaiti", "楷体", serif;
  margin-top: 24px;
  margin-bottom: 16px;
  font-weight: 600;
  border-bottom: 1px solid #E4D9C3;
  padding-bottom: 0.3em;
}

:deep(.markdown-body a) {
  color: #8C1F28;
  text-decoration: none;
}

:deep(.markdown-body a:hover) {
  text-decoration: underline;
}

:deep(.markdown-body blockquote) {
  padding: 0 1em;
  color: #6B5B45;
  border-left: 0.25em solid #E4D9C3;
}

:deep(.markdown-body pre) {
  background-color: #FFF7E9;
  border: 1px solid #E4D9C3;
  border-radius: 4px;
}

:deep(.markdown-body code) {
  background-color: #FFF7E9;
  color: #8C1F28;
  padding: 0.2em 0.4em;
  border-radius: 3px;
  font-family: "SFMono-Regular", Consolas, "Liberation Mono", Menlo, monospace;
}

:deep(.markdown-body img) {
  max-width: 100%;
  box-sizing: content-box;
  border-radius: 4px;
  border: 1px solid #E4D9C3;
}

/* 响应式样式 */
@media screen and (max-width: 1200px) {
  .sidebar {
    width: 250px;
  }
}

@media screen and (max-width: 900px) {
  .main-content {
    flex-direction: column;
    height: auto;
  }
  
  .markdown-editor-wrapper {
    height: 500px;
    margin-bottom: 10px;
  }
  
  .post-form {
    margin-right: 0;
    margin-bottom: 10px;
  }
  
  .sidebar {
    width: 100%;
    margin-left: 0;
  }
}

@media screen and (max-width: 768px) {
  .create-post-container {
    width: 98%;
  }
  
  .page-header {
    padding: 12px;
  }
  
  .header-title {
    font-size: 18px;
  }
  
  .image-preview-item,
  .image-upload-trigger {
    width: calc(33.333% - 7px);
  }
}
</style>