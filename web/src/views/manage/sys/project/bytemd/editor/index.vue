<script setup>
import { Editor } from 'bytemd';
import { ref, watch, defineEmits, onMounted, computed, nextTick, onUnmounted } from 'vue';
import { Message } from '@arco-design/web-vue';
import { uploadImageAPI } from '@/api/forum'; // 引入图片上传API
import { uploadVideoAPI } from '@/api/manage/IchProject'; // 引入视频上传API

const props = defineProps({
  value: String,//内容控件的值
  plugins: Array,//插件数组
  sanitize: Object,//策略模式
  remarkRehype: Object,//文档备注介入选项
  mode: String,//模式
  previewDebounce: Number,//指定debounce时间
  placeholder: String,//占位符
  editorConfig: Object, //编辑配置
  locale: Object,//语言类
  uploadImages: Function,//支持上传图片(指定如何上次图片)
  split: Boolean,//是否启用分屏模式
  
  // 非遗项目信息相关属性
  projectId: [String, Number], // 项目ID，用于视频上传
  name: String, // 项目名称
  levelId: [String, Number], // 保护级别ID
  categoryId: [String, Number], // 项目类别ID
  summary: String, // 项目简介
  levelOptions: Array, // 保护级别选项
  categoryOptions: Array, // 项目类别选项
  coverImage: String, // 当前封面图片URL
  video: String, // 当前视频URL
});

// 创建本地副本用于实现双向绑定效果
const localName = ref(props.name || '');
const localLevelId = ref(props.levelId || '');
const localCategoryId = ref(props.categoryId || '');
const localSummary = ref(props.summary || '');
const imageFile = ref(null);
const imagePreview = ref(props.coverImage || ''); // 添加图片预览URL
const videoFile = ref(null);
const videoUrl = ref(props.video || '');
const uploading = ref(false);
const uploadProgress = ref(0); // 添加上传进度

// 监听props变化
watch(() => props.name, (newVal) => {
  localName.value = newVal;
});

watch(() => props.levelId, (newVal) => {
  localLevelId.value = newVal;
});

watch(() => props.categoryId, (newVal) => {
  localCategoryId.value = newVal;
});

watch(() => props.summary, (newVal) => {
  localSummary.value = newVal;
});

watch(() => props.video, (newVal) => {
  videoUrl.value = newVal;
});

// 监听本地数据变化并向父组件传递
watch(localName, (newVal) => {
  emit('nameChange', newVal);
});

watch(localLevelId, (newVal) => {
  emit('levelChange', newVal);
});

watch(localCategoryId, (newVal) => {
  emit('categoryChange', newVal);
});

watch(localSummary, (newVal) => {
  emit('summaryChange', newVal);
});

const emit = defineEmits([
  "change", 
  "nameChange", 
  "levelChange", 
  "categoryChange", 
  "summaryChange", 
  "fileChange", 
  "videoChange",
  "back", 
  "save",
  "cleanupVideo"
]);

// 添加返回和保存按钮的点击处理函数
const handleBack = () => {
  if (videoUrl.value && videoUrl.value.startsWith('http')) {
    emit('cleanupVideo', videoUrl.value);
  }
  
  if (videoUrl.value && videoUrl.value.startsWith('blob:')) {
    URL.revokeObjectURL(videoUrl.value);
  }
  
  emit('back');
};

const handleSave = () => {
  emit('save');
};

// 处理图片选择
const handleFileChange = (event) => {
  const file = event.target.files[0];
  if (!file) return;
  
  // 验证文件类型和大小
  const isImage = file.type.startsWith('image/');
  const isValidSize = file.size <= 10 * 1024 * 1024; // 10MB限制
  
  if (!isImage) {
    Message.warning(`文件 ${file.name} 不是有效的图片格式`);
    return;
  }
  
  if (!isValidSize) {
    Message.warning(`文件 ${file.name} 超过10MB大小限制`);
    return;
  }
  
  imageFile.value = file;
  
  // 创建本地预览
  const reader = new FileReader();
  reader.onload = (e) => {
    imagePreview.value = e.target.result;
  };
  reader.readAsDataURL(file);
  
  emit('fileChange', imageFile.value);
};

// 处理视频选择
const handleVideoChange = async (event) => {
  const file = event.target.files[0];
  if (!file) return;
  
  // 验证文件类型和大小
  const isVideo = file.type.startsWith('video/');
  const isValidSize = file.size <= 1000 * 1024 * 1024; // 1000MB限制
  
  if (!isVideo) {
    Message.warning(`文件 ${file.name} 不是有效的视频格式`);
    return;
  }
  
  if (!isValidSize) {
    Message.warning(`文件 ${file.name} 超过1000MB大小限制`);
    return;
  }
  
  videoFile.value = file;
  
  // 如果已有上传的视频URL，先记录用于清理
  const oldVideoUrl = videoUrl.value;
  
  // 创建本地预览
  try {
    const localVideoUrl = URL.createObjectURL(file);
    videoUrl.value = localVideoUrl; // 先设置为本地预览URL
  } catch (error) {
    console.error('创建视频预览失败:', error);
  }
  
  try {
    uploading.value = true;
    uploadProgress.value = 0;
    
    // 使用专门的视频上传API
    const res = await uploadVideoAPI(file, props.projectId || '', (progress) => {
      uploadProgress.value = progress; // 更新上传进度
    });
    
    if (res.code === 200 && res.data) {
      if (oldVideoUrl && oldVideoUrl.startsWith('http')) {
        emit('cleanupVideo', oldVideoUrl);
      }
      
      if (videoUrl.value && videoUrl.value.startsWith('blob:')) {
        URL.revokeObjectURL(videoUrl.value);
      }
      
      videoUrl.value = res.data; // 更新为服务器返回的URL
      emit('videoChange', videoUrl.value);
      Message.success('视频上传成功');
    } else {
      Message.warning('视频上传失败');
      console.error('视频上传失败，响应:', res);
      if (videoUrl.value && videoUrl.value.startsWith('blob:')) {
        URL.revokeObjectURL(videoUrl.value);
        videoUrl.value = '';
      }
    }
  } catch (error) {
    console.error('视频上传失败:', error);
    Message.error(`视频 ${file.name} 上传失败：${error.message || '网络错误'}`);
    if (videoUrl.value && videoUrl.value.startsWith('blob:')) {
      URL.revokeObjectURL(videoUrl.value);
      videoUrl.value = '';
    }
  } finally {
    uploading.value = false;
  }
};

// 编辑器图片上传处理函数
const handleUploadImages = async (files) => {
  try {
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
    
    const urls = [];
    
    // 逐个上传文件
    for (const file of validFiles) {
      // 创建FormData对象
      const formData = new FormData();
      formData.append('file', file);
      
      try {
        // 上传到服务器
        const res = await uploadImageAPI(formData);
        
        if (res.code === 200 && res.data) {
          // 保证URL是绝对URL
          let imageUrl = res.data;
          
          if (imageUrl && typeof imageUrl === 'string') {
            // 将URL添加到结果数组 - 这些URL将直接被ByteMD使用
            urls.push(imageUrl);
            console.log('图片上传成功，URL:', imageUrl);
          }
        } else {
          Message.warning(`图片 ${file.name} 上传失败`);
          console.error('图片上传失败，响应:', res);
        }
      } catch (error) {
        console.error('上传单张图片失败:', error);
        Message.error(`图片 ${file.name} 上传失败`);
      }
    }
    
    // 返回URL数组，不包含undefined
    return urls.filter(url => url && url !== 'undefined');
  } catch (error) {
    console.error('图片上传处理错误:', error);
    Message.error('图片上传过程中发生错误');
    return [];
  }
};

//当前编辑器的el
const el = ref(null);
const editorRef = ref(null);

//监听props的值，去除为undefined的值
watch(() => props, newValue => {
  const copy = { ...newValue };
  for (let k in copy) {
    if (copy[k] === undefined) {
      delete copy[k];
    }
  }
  editorRef.value?.$set(copy);
}, {
  deep: true
});

//监听编辑器值的变化
onMounted(() => {
  const editor = new Editor({
    target: el.value,
    props: {
      ...props,
      uploadImages: props.uploadImages || handleUploadImages // 使用传入的uploadImages或自己的处理函数
    }
  });
  editor.$on("change", e => {
    emit("change", e.detail.value);

    nextTick(() => {
      fixPreviewImages();
    });
  });
  editorRef.value = editor;
  
  // 添加定时器，每隔一段时间检查预览区图片
  const intervalId = setInterval(() => {
    fixPreviewImages();
  }, 3000);
  
  // 组件销毁时清除定时器
  onUnmounted(() => {
    if (videoUrl.value && videoUrl.value.startsWith('blob:')) {
      URL.revokeObjectURL(videoUrl.value);
    }
    
    if (intervalId) {
      clearInterval(intervalId);
    }
  });
});

// 增强fixPreviewImages函数
const fixPreviewImages = () => {
  // 查找预览区域
  const previewElement = document.querySelector('.bytemd-preview');
  if (!previewElement) return;
  
  // 找到所有图片
  const images = previewElement.querySelectorAll('img');
  if (images.length === 0) return;
  
  // 收集出现的图片URL路径
  const possibleImageUrls = [];
  
  // 查找编辑器中所有的图片URL
  const editorValue = document.querySelector('.CodeMirror').CodeMirror?.getValue() || '';
  const matches = editorValue.match(/!\[.*?\]\((.*?)\)/g) || [];
  
  if (matches.length > 0) {
    matches.forEach(match => {
      const urlMatch = match.match(/!\[.*?\]\((.*?)\)/);
      if (urlMatch && urlMatch[1] && urlMatch[1].startsWith('http')) {
        possibleImageUrls.push(urlMatch[1]);
      }
    });
  }
  
  // 处理每个图片
  images.forEach((img, index) => {
    // 检查图片是否正常显示
    const src = img.getAttribute('src');
    if (!src || src === 'undefined' || src.includes('undefined') || src.startsWith('http://localhost')) {
      
      // 尝试使用alt文本作为线索查找正确URL
      const alt = img.getAttribute('alt') || '';
      
      // 尝试找到匹配的图片URL
      let replacementUrl = null;
      
      // 1. 尝试通过alt文本匹配
      replacementUrl = possibleImageUrls.find(url => 
        url && url.includes(alt.replace('.jpg', '').replace('.png', '').replace('.gif', ''))
      );
      
      // 2. 没找到匹配，使用索引匹配
      if (!replacementUrl && index < possibleImageUrls.length) {
        replacementUrl = possibleImageUrls[index];
      }
      
      // 3. 没找到，使用第一个可用URL
      if (!replacementUrl && possibleImageUrls.length > 0) {
        replacementUrl = possibleImageUrls[0];
      }
      
      // 替换URL
      if (replacementUrl) {
        img.src = replacementUrl;
        
        // 图片可见
        img.style.display = 'block';
        img.style.maxWidth = '80%'; // 调整为更小的尺寸
        img.style.height = 'auto';
        img.style.margin = '10px auto';
        img.style.border = '1px solid #E4D9C3';
        img.style.borderRadius = '4px';
        img.style.visibility = 'visible';
        img.style.opacity = '1';
      } else {
        // 应用应急样式
        img.style.border = '1px dashed #ff6b6b';
        img.style.padding = '10px';
        img.style.height = '120px';
        img.style.width = '120px';
        img.style.display = 'block';
        img.style.margin = '10px auto';
        img.style.backgroundColor = '#fff7e9';
        img.style.position = 'relative';
        
        // 显示替代文本
        if (!img.nextSibling || !img.nextSibling.classList?.contains('img-placeholder-text')) {
          const placeholder = document.createElement('div');
          placeholder.textContent = `图片 [${alt || '未命名'}] 正在加载中...`;
          placeholder.className = 'img-placeholder-text';
          placeholder.style.textAlign = 'center';
          placeholder.style.fontSize = '12px';
          placeholder.style.color = '#8C1F28';
          placeholder.style.padding = '5px';
          img.parentNode.insertBefore(placeholder, img.nextSibling);
        }
      }
    } else {
      // 图片正常显示
      img.style.display = 'block';
      img.style.maxWidth = '80%'; // 调整为更小的尺寸
      img.style.height = 'auto';
      img.style.margin = '10px auto';
      img.style.border = '1px solid #E4D9C3';
      img.style.borderRadius = '4px';
      img.style.visibility = 'visible';
      img.style.opacity = '1';
    }
    
    // 添加错误处理
    if (!img._hasErrorHandler) {
      img._hasErrorHandler = true;
      img.onerror = function() {
        // 尝试使用替代URL
        if (possibleImageUrls.length > 0) {
          let replacementUrl;
          
          // 优先使用相同索引的URL
          if (index < possibleImageUrls.length) {
            replacementUrl = possibleImageUrls[index];
          } else {
            replacementUrl = possibleImageUrls[0];
          }
          
          if (replacementUrl && replacementUrl !== this.src) {
            this.src = replacementUrl;
            return;
          }
        }
        
        // 应用错误样式
        this.style.border = '1px dashed #ff6b6b';
        this.style.padding = '10px';
        this.alt = this.alt || '图片加载失败';
        
        // 显示错误信息
        if (!this.nextSibling || !this.nextSibling.classList?.contains('img-error-text')) {
          const errorText = document.createElement('div');
          errorText.textContent = `图片加载失败: ${this.alt || '未知图片'}`;
          errorText.className = 'img-error-text';
          errorText.style.textAlign = 'center';
          errorText.style.fontSize = '12px';
          errorText.style.color = '#ff6b6b';
          errorText.style.padding = '5px';
          this.parentNode.insertBefore(errorText, this.nextSibling);
        }
      };
    }
  });
};
</script>

<template>
  <div class="editor-container">
    <!-- 顶部信息栏：集成项目信息输入 -->
    <div class="editor-header">
      <div class="editor-header-left">
        <button @click="handleBack" class="editor-button back-button">
          <i class="iconfont icon-arrow-left"></i> 返回
        </button>
        <input 
          v-model="localName" 
          class="editor-title-input" 
          placeholder="请输入项目名称"
          maxlength="50"
        >
      </div>
      <div class="editor-header-right">
        <button @click="handleSave" class="editor-button publish-button">
          保存项目
        </button>
      </div>
    </div>
    
    <!-- 项目信息栏 -->
    <div class="project-info-container">
      <div class="project-info-row">
        <div class="project-info-item">
          <label>保护级别</label>
          <select v-model="localLevelId" class="project-select">
            <option value="" disabled>请选择保护级别</option>
            <option v-for="option in props.levelOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </option>
          </select>
        </div>
        <div class="project-info-item">
          <label>项目类别</label>
          <select v-model="localCategoryId" class="project-select">
            <option value="" disabled>请选择项目类别</option>
            <option v-for="option in props.categoryOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </option>
          </select>
        </div>
      </div>

      <div class="project-info-row">
        <div class="project-info-item">
          <label>封面图片</label>
          <div class="image-uploader">
            <div v-if="imagePreview" class="current-image">
              <img :src="imagePreview" class="preview-image" alt="当前封面图片">
            </div>
            <input 
              type="file" 
              accept="image/*" 
              class="file-input" 
              id="cover-image"
              @change="handleFileChange"
            >
            <label for="cover-image" class="file-input-label">
              选择图片
            </label>
            <span class="selected-filename">{{ imageFile ? imageFile.name : '未选择文件' }}</span>
          </div>
        </div>
        <div class="project-info-item">
          <label>项目视频</label>
          <div class="video-uploader">
            <div v-if="videoUrl" class="current-video">
              <video 
                :src="videoUrl" 
                class="preview-video" 
                controls 
                alt="项目视频"
                width="160"
                height="90"
              ></video>
            </div>
            <div class="video-upload-controls">
              <input 
                type="file" 
                accept="video/*" 
                class="file-input" 
                id="project-video"
                @change="handleVideoChange"
              >
              <label for="project-video" class="file-input-label" :class="{ 'disabled': uploading }">
                {{ uploading ? '上传中...' : '选择视频' }}
              </label>
              <span class="selected-filename">{{ videoFile ? videoFile.name : videoUrl ? '已上传视频' : '未选择文件' }}</span>
            </div>
            <!-- 添加上传进度条 -->
            <div v-if="uploading" class="upload-progress-container">
              <div class="upload-progress-bar" :style="{ width: uploadProgress + '%' }"></div>
              <span class="upload-progress-text">{{ uploadProgress }}%</span>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 编辑器主体 -->
    <div ref="el" class="md-editor"></div>
  </div>
</template>

<style>
.editor-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  border-radius: 8px;
  background-color: #FFFDF7;
  overflow: hidden;
  position: relative;
  width: 100%;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background-color: #F9F3E9;
  border-bottom: 1px solid #D6C6AF;
}

.editor-header-left {
  flex: 0.7;
  display: flex;
  align-items: center;
  gap: 35px;
}

.editor-header-right {
  flex: 0.3;
  display: flex;
  align-items: center;
  gap: 20px;
  justify-content: flex-end;
}

.editor-title-input {
  width: 100%;
  padding: 10px 12px;
  font-size: 20px;
  border: 1px solid #D6C6AF;
  border-radius: 4px;
  background-color: #FFFDF7;
  color: #582F0E;
  font-family: "STKaiti", "楷体", serif;
  transition: all 0.3s;
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.05);
}

.editor-title-input:focus {
  outline: none;
  border-color: #8C1F28;
  box-shadow: 0 0 0 2px rgba(140, 31, 40, 0.1);
}

.editor-title-input::placeholder {
  color: #A89D84;
  opacity: 0.8;
}

.editor-button {
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-family: "STKaiti", "楷体", serif;
  font-size: 16px;
  transition: all 0.3s;
  white-space: nowrap;
  height: 38px;
  display: flex;
  align-items: center;
}

.back-button {
  background-color: #FFFDF7;
  color: #582F0E;
  border: 1px solid #D6C6AF;
  padding: 22px 16px;
  font-size: 20px;
}

.back-button:hover {
  border-color: #8C1F28;
  background-color: #FFF7E9;
}

.publish-button {
  background-color: #8C1F28;
  color: #FFFDF7;
  border: 1px solid #8C1F28;
  padding: 8px 16px;
  font-weight: bold;
}

.publish-button:hover {
  background-color: #A52A2A;
  border-color: #A52A2A;
}

.md-editor {
  flex: 1;
  overflow: hidden;
  width: 100%;
}

/* 项目信息样式 */
.project-info-container {
  background-color: #FFF7E9;
  padding: 16px;
  border-bottom: 1px solid #D6C6AF;
}

.project-info-row {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
}

.project-info-row:last-child {
  margin-bottom: 0;
}

.project-info-item {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.project-info-item.full-width {
  width: 100%;
}

.project-info-item label {
  font-size: 14px;
  color: #582F0E;
  margin-bottom: 4px;
  font-weight: 500;
  font-family: "STKaiti", "楷体", serif;
}

.project-select {
  padding: 8px 10px;
  border: 1px solid #D6C6AF;
  border-radius: 4px;
  background-color: #FFFDF7;
  color: #582F0E;
  font-family: "STKaiti", "楷体", serif;
  font-size: 14px;
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='16' height='16' viewBox='0 0 24 24' fill='none' stroke='%23582F0E' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='M6 9l6 6 6-6'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 8px center;
  background-size: 16px;
  padding-right: 30px;
}


/* 图片上传相关样式 */
.image-uploader, .video-uploader {
  display: flex;
  align-items: center;
  gap: 15px;
}

.current-image, .current-video {
  width: 100px;
  height: 60px;
  border: 1px solid #D6C6AF;
  border-radius: 4px;
  overflow: hidden;
  background-color: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.preview-image, .preview-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.file-input {
  width: 0.1px;
  height: 0.1px;
  opacity: 0;
  overflow: hidden;
  position: absolute;
  z-index: -1;
}

.file-input-label {
  padding: 8px 12px;
  background-color: #FFFDF7;
  color: #582F0E;
  border: 1px solid #D6C6AF;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
  display: inline-flex;
  align-items: center;
  white-space: nowrap;
}

.file-input-label:hover {
  background-color: #FFF7E9;
  border-color: #8C1F28;
}

.file-input-label.disabled {
  background-color: #e9e9e9;
  color: #999;
  cursor: not-allowed;
  border-color: #ccc;
}

.selected-filename {
  font-size: 14px;
  color: #7F4F24;
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ByteMD编辑器样式 */
.bytemd {
  height: 100% !important;
  border-radius: 0;
  border: none !important;
  min-height: 400px !important;
  max-height: none !important;
  resize: none !important;
  width: 100% !important;
  overflow-x: hidden !important;
}

/* 增强编辑器样式 */
.bytemd-toolbar {
  background-color: #FFFBF0;
  border-bottom: 1px solid #D6C6AF;
  padding: 8px;
}

.bytemd-toolbar-icon {
  transition: all 0.2s;
  border-radius: 4px;
  width: 34px;
  height: 34px;
  margin: 0 1px;
}

.bytemd-toolbar-icon:hover {
  background-color: rgba(140, 31, 40, 0.1);
}

.bytemd-toolbar-icon.bytemd-tippy-active {
  color: #8C1F28;
  background-color: rgba(140, 31, 40, 0.08);
}

.bytemd-status {
  background-color: #FFFBF0;
  border-top: 1px solid #D6C6AF;
  padding: 6px 12px;
  color: #6B5B45;
}

/* 预览区域样式 */
.bytemd-preview {
  background-color: #FFFDF7;
  padding: 20px !important;
  min-height: 400px !important;
}

/* 编辑与预览区域高度相同且固定 */
.bytemd-split .bytemd-editor, 
.bytemd-split .bytemd-preview {
  height: 400px !important;
  min-height: 400px !important;
  max-height: 400px !important;
  overflow-y: auto !important;
  overflow-x: hidden !important; /* 禁止水平滚动 */
  width: 50% !important;
}

/* 增强编辑区域样式 */
.CodeMirror {
  font-family: "SimSun", "宋体", serif !important;
  font-size: 16px !important;
  line-height: 1.6 !important;
  padding: 0 10px !important;
  height: auto !important;
  min-height: 400px !important;
  word-wrap: break-word !important;
  white-space: pre-wrap !important;
  overflow-x: hidden !important;
  width: 100% !important;
}

.CodeMirror-scroll {
  overflow-x: hidden !important; /* 防止水平溢出 */
}

.CodeMirror-lines {
  padding: 30px 0 !important;
}

/* 响应式样式 */
@media screen and (max-width: 768px) {
  .editor-header {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
    padding: 10px;
  }
  
  .editor-header-left, 
  .editor-header-right {
    width: 100%;
    flex-direction: row;
    flex-wrap: wrap;
  }
  
  .editor-title-input {
    font-size: 16px;
    padding: 8px 10px;
    order: 2;
  }
  
  .back-button {
    order: 1;
  }
  
  .publish-button {
    flex-shrink: 0;
    width: 100%;
    justify-content: center;
  }
  
  .project-info-row {
    flex-direction: column;
    gap: 10px;
  }
  
  .image-uploader, .video-uploader {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .selected-filename {
    max-width: 100%;
  }
}

/* 添加上传进度条样式 */
.upload-progress-container {
  width: 100%;
  height: 16px;
  background-color: #f0f0f0;
  border-radius: 8px;
  overflow: hidden;
  margin-top: 10px;
  position: relative;
}

.upload-progress-bar {
  height: 100%;
  background-color: #8C1F28;
  border-radius: 8px;
  transition: width 0.3s;
}

.upload-progress-text {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #fff;
  font-size: 12px;
  font-weight: bold;
  text-shadow: 1px 1px 1px rgba(0, 0, 0, 0.5);
}

.video-upload-controls {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.current-video {
  margin-bottom: 10px;
  width: 160px;
  height: 90px;
  border: 1px solid #D6C6AF;
  border-radius: 4px;
  overflow: hidden;
}

.preview-image, .preview-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
</style> 