<script setup>
import { Editor } from 'bytemd';
import { ref, watch, defineEmits, onMounted, computed, nextTick, onUnmounted } from 'vue';
import { Message } from '@arco-design/web-vue';
import { uploadImageAPI, uploadVideoAPI, batchUploadImagesAPI } from '@/api/manage/IchProject';

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
  levelOptions: Array, // 保护级别选项
  categoryOptions: Array, // 项目类别选项
  video: String, // 当前视频URL
  images: String, // 图片JSON数组
});

// 创建本地副本用于实现双向绑定效果
const localName = ref(props.name || '');
const localLevelId = ref(props.levelId || '');
const localCategoryId = ref(props.categoryId || '');
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

const emit = defineEmits([
  "change", 
  "nameChange", 
  "levelChange", 
  "categoryChange",
  "videoChange",
  "back", 
  "save",
  "cleanupVideo",
  "fileChange"
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
  const editorElement = document.querySelector('.CodeMirror');
  if (!editorElement || !editorElement.CodeMirror) return;
  
  const editorValue = editorElement.CodeMirror.getValue() || '';
  
  // 使用更宽松的正则表达式匹配所有可能的图片URL模式
  const markdownImageRegex = /!\[.*?\]\((.*?)\)/g;
  const htmlImageRegex = /<img.*?src=["'](.*?)["']/g;
  const rawUrlRegex = /(https?:\/\/[^\s"'<>]+\.(jpg|jpeg|png|gif|webp|svg))/gi;
  
  let match;
  
  // 匹配Markdown格式图片
  while ((match = markdownImageRegex.exec(editorValue)) !== null) {
    if (match[1] && match[1].startsWith('http')) {
      possibleImageUrls.push(match[1]);
    }
  }
  
  // 匹配HTML格式图片
  while ((match = htmlImageRegex.exec(editorValue)) !== null) {
    if (match[1] && match[1].startsWith('http')) {
      possibleImageUrls.push(match[1]);
    }
  }
  
  // 匹配原始URL
  while ((match = rawUrlRegex.exec(editorValue)) !== null) {
    if (match[0]) {
      possibleImageUrls.push(match[0]);
    }
  }
  
  // 从最近上传的图片列表中添加URL
  if (recentUploadedImages.length > 0) {
    possibleImageUrls.push(...recentUploadedImages);
  }
  
  // 使用缓存机制减少日志输出
  const urlsHash = possibleImageUrls.length + '-' + (possibleImageUrls[0] || '');
  if (!fixPreviewImages.lastHash || fixPreviewImages.lastHash !== urlsHash) {
    fixPreviewImages.lastHash = urlsHash;
    console.log('找到图片URL数量:', possibleImageUrls.length);
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
        if (img.src !== replacementUrl) {
          img.src = replacementUrl;
          
          // 图片可见
          applyImageStyle(img);
        }
      } else {
        // 应用应急样式
        applyPlaceholderStyle(img, alt);
      }
    } else {
      // 图片正常显示
      applyImageStyle(img);
    }
    
    // 添加错误处理
    if (!img._hasErrorHandler) {
      img._hasErrorHandler = true;
      img.onerror = function() {
        console.error('图片加载失败:', this.src);
        
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
        applyErrorStyle(this, this.alt || '未知图片');
      };
    }
  });
  

  if (images.length === 0 && possibleImageUrls.length > 0 && !fixPreviewImages.hasCreated) {
    fixPreviewImages.hasCreated = true;
    createPreviewImages(previewElement, possibleImageUrls);
  }
};

// 添加手动创建预览图片的函数
const createPreviewImages = (container, urls) => {
  if (!container || !urls.length) return;
  
  urls.forEach(url => {
    const imgWrapper = document.createElement('div');
    imgWrapper.className = 'manually-added-img-wrapper';
    
    const img = document.createElement('img');
    img.src = url;
    img.alt = '上传的图片';
    applyImageStyle(img);
    
    imgWrapper.appendChild(img);
    container.appendChild(imgWrapper);
    
    console.log('手动添加图片到预览区:', url);
  });
};

// 存储最近上传的图片URL
const recentUploadedImages = [];

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

    try {
      // 显示上传中提示
      const loadingMsg = Message.loading({
        content: '正在上传图片...',
        duration: 0
      });
      
      // 使用批量上传API
      const res = await batchUploadImagesAPI(validFiles);
      
      // 关闭加载提示
      loadingMsg.close();
      
      if (res.code === 200 && res.data && Array.isArray(res.data)) {
        const imageUrls = res.data;
        console.log('批量上传图片成功，URL列表:', imageUrls);
        

        imageUrls.forEach(url => {
          if (url && typeof url === 'string') {
            recentUploadedImages.push(url);
          }
        });
        

        if (recentUploadedImages.length > 20) {
          recentUploadedImages.splice(0, recentUploadedImages.length - 20);
        }
        

        if (imageUrls.length > 0) {
          emit('fileChange', imageUrls);
          console.log('通过fileChange事件发送图片URL列表:', imageUrls);
        }
        
        // 尝试修复预览区图片
        setTimeout(() => {
          fixPreviewImages();
        }, 100);
        
        // 返回URL数组供编辑器使用
        return imageUrls.filter(url => url && url !== 'undefined');
      } else {
        Message.warning('图片批量上传失败');
        console.error('批量上传失败，响应:', res);
        return [];
      }
    } catch (error) {
      console.error('批量上传图片失败:', error);
      Message.error('批量上传图片失败: ' + (error.message || '未知错误'));
      return [];
    }
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
  }, 1000); // 减少间隔时间，更频繁检查
  
  // 组件销毁时清除定时器
  onUnmounted(() => {
    if (videoUrl.value && videoUrl.value.startsWith('blob:')) {
      URL.revokeObjectURL(videoUrl.value);
    }
    
    if (intervalId) {
      clearInterval(intervalId);
    }
  });
  
  // 初始化后立即触发一次图片修复
  setTimeout(() => {
    fixPreviewImages();
  }, 300);
});

// 应用图片样式函数
const applyImageStyle = (img) => {
  img.style.display = 'block';
  img.style.maxWidth = '80%';
  img.style.height = 'auto'; 
  img.style.margin = '10px auto';
  img.style.border = '1px solid #E4D9C3';
  img.style.borderRadius = '4px';
  img.style.visibility = 'visible';
  img.style.opacity = '1';
};

// 应用占位符样式函数
const applyPlaceholderStyle = (img, alt) => {
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
};

// 应用错误样式函数
const applyErrorStyle = (img, alt) => {
  img.style.border = '1px dashed #ff6b6b';
  img.style.padding = '10px';
  img.alt = alt || '图片加载失败';
  
  // 显示错误信息
  if (!img.nextSibling || !img.nextSibling.classList?.contains('img-error-text')) {
    const errorText = document.createElement('div');
    errorText.textContent = `图片加载失败: ${alt || '未知图片'}`;
    errorText.className = 'img-error-text';
    errorText.style.textAlign = 'center';
    errorText.style.fontSize = '12px';
    errorText.style.color = '#ff6b6b';
    errorText.style.padding = '5px';
    img.parentNode.insertBefore(errorText, img.nextSibling);
  }
};

// 处理图片上传
const getFile = (imageUrls) => {
  if (!imageUrls) return;
  
  if (Array.isArray(imageUrls)) {

    newProject.images = JSON.stringify(imageUrls);
  } else if (typeof imageUrls === 'string') {

    newProject.images = JSON.stringify([imageUrls]);
  }
  console.log('已设置项目图片数据:', newProject.images);
};
</script>

<template>
  <div class="editor-container">
    <!-- 左侧编辑器 -->
    <div class="editor-left-section">
      <!-- 编辑器主体 -->
      <div ref="el" class="md-editor"></div>
    </div>
    
    <!-- 右侧表单 -->
    <div class="editor-right-section">
      <div class="form-header">
        <div class="form-title">项目信息</div>
      </div>
      
      <div class="form-item form-item-fixed">
        <label class="form-label">项目名称</label>
        <input 
          v-model="localName" 
          class="form-input" 
          placeholder="请输入项目名称"
          maxlength="50"
        >
      </div>
      
      <div class="form-item form-item-fixed">
        <label class="form-label">保护级别</label>
        <select v-model="localLevelId" class="form-select">
          <option value="" disabled>请选择保护级别</option>
          <option v-for="option in props.levelOptions" :key="option.value" :value="option.value">
            {{ option.label }}
          </option>
        </select>
      </div>
      
      <div class="form-item form-item-fixed">
        <label class="form-label">项目类别</label>
        <select v-model="localCategoryId" class="form-select">
          <option value="" disabled>请选择项目类别</option>
          <option v-for="option in props.categoryOptions" :key="option.value" :value="option.value">
            {{ option.label }}
          </option>
        </select>
      </div>
      
      <div class="form-item">
        <label class="form-label">项目视频</label>
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
      
      <div class="form-actions">
        <button @click="handleBack" class="editor-button back-button">
          返回
        </button>
        <button @click="handleSave" class="editor-button publish-button">
          发布项目
        </button>
      </div>
    </div>
  </div>
</template>

<style>
.editor-container {
  display: flex;
  height: 100%;
  border-radius: 8px;
  background-color: #ffffff;
  overflow: hidden;
  position: relative;
  width: 100%;
}

/* 左侧编辑器区域 */
.editor-left-section {
  flex: 2;
  height: 100%;
  overflow: hidden;
  border-right: 1px solid #D6C6AF;
}

.md-editor {
  height: 100%;
  width: 100%;
}

/* 右侧表单区域 */
.editor-right-section {
  flex: 1;
  height: 100%;
  overflow-y: auto;
  padding: 4px;
  display: flex;
  flex-direction: column;
  background-color: #fafbfc;
  position: relative;
  padding-bottom: 0px; /* 为底部按钮留出空间 */
}

.form-header {
  margin-bottom: 16px;
  border-bottom: 1px solid #eaeaea;
  padding-bottom: 8px;
}

.form-title {
  font-size: 15px;
  font-weight: 500;
  color: #333;
  font-family: "STKaiti", "楷体", serif;
}

.form-item {
  margin-bottom: 12px;
}

.form-label {
  display: block;
  font-size: 13px;
  color: #555;
  margin-bottom: 4px;
  font-weight: 400;
  font-family: "STKaiti", "楷体", serif;
}

.form-input, .form-select {
  width: 100%;
  padding: 6px 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background-color: #fff;
  color: #333;
  font-family: "STKaiti", "楷体", serif;
  font-size: 13px;
  transition: all 0.2s ease;
}

.form-select {
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='14' height='14' viewBox='0 0 24 24' fill='none' stroke='%23777' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='M6 9l6 6 6-6'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 10px center;
  background-size: 14px;
  padding-right: 28px;
}

.form-input:focus, .form-select:focus {
  outline: none;
  border-color: #165dff;
  box-shadow: 0 0 0 2px rgba(22, 93, 255, 0.1);
}

/* 图片上传相关样式 */
.video-uploader {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.current-video {
  width: 100%;
  max-width: 160px;
  height: 90px;
  border: 1px solid #eaeaea;
  border-radius: 4px;
  overflow: hidden;
  background-color: #f7f7f7;
  display: flex;
  align-items: center;
  justify-content: center;
}

.preview-video {
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
  padding: 7px 12px;
  background-color: #fff;
  color: #165dff;
  border: 1px solid #165dff;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
  display: inline-flex;
  align-items: center;
  white-space: nowrap;
  max-width: fit-content;
}

.file-input-label:hover {
  background-color: rgba(22, 93, 255, 0.05);
}

.file-input-label.disabled {
  background-color: #f7f7f7;
  color: #aaa;
  cursor: not-allowed;
  border-color: #ddd;
}

.selected-filename {
  font-size: 13px;
  color: #666;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-top: 4px;
}

.video-upload-controls {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* 按钮样式 */
.form-actions {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 10px 12px;
  border-top: 1px solid #eaeaea;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  background-color: #fafbfc;
  z-index: 10; /* 确保按钮在最上层 */
}

.editor-button {
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-family: "STKaiti", "楷体", serif;
  font-size: 14px;
  transition: all 0.2s;
  white-space: nowrap;
}

.back-button {
  background-color: #fff;
  color: #666;
  border: 1px solid #ddd;
}

.back-button:hover {
  border-color: #bbb;
  color: #333;
  background-color: #f7f7f7;
}

.publish-button {
  background-color: #165dff;
  color: #fff;
  border: 1px solid #165dff;
}

.publish-button:hover {
  background-color: #0e4cbf;
}

/* 添加上传进度条样式 */
.upload-progress-container {
  width: 100%;
  height: 6px;
  background-color: #eaeaea;
  border-radius: 3px;
  overflow: hidden;
  margin-top: 10px;
  position: relative;
}

.upload-progress-bar {
  height: 100%;
  background-color: #165dff;
  border-radius: 3px;
  transition: width 0.3s;
}

.upload-progress-text {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #fff;
  font-size: 10px;
  font-weight: bold;
  text-shadow: 0 0 2px rgba(0, 0, 0, 0.5);
}

/* ByteMD编辑器样式 */
.bytemd {
  height: 100% !important;
  border-radius: 0;
  border: none !important;
  min-height: 100% !important;
  max-height: none !important;
  resize: none !important;
  width: 100% !important;
  overflow-x: hidden !important;
}

/* 增强编辑器样式 */
.bytemd-toolbar {
  background-color: #f9f9f9;
  border-bottom: 1px solid #eaeaea;
  padding: 4px 6px;
  display: flex;
  flex-wrap: nowrap;
  overflow-x: auto;
  justify-content: flex-start;
  gap: 1px;
}

.bytemd-toolbar::-webkit-scrollbar {
  height: 3px;
}

.bytemd-toolbar::-webkit-scrollbar-thumb {
  background-color: rgba(0, 0, 0, 0.1);
  border-radius: 3px;
}

.bytemd-toolbar-icon {
  transition: all 0.2s;
  border-radius: 3px;
  width: 28px;
  height: 28px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.bytemd-toolbar-icon:hover {
  background-color: rgba(140, 31, 40, 0.1);
}

.bytemd-toolbar-icon.bytemd-tippy-active {
  color: #8C1F28;
  background-color: rgba(140, 31, 40, 0.08);
}

.bytemd-status {
  background-color: #ffffff;
  border-top: 1px solid #D6C6AF;
  padding: 6px 12px;
  color: #6B5B45;
}

/* 预览区域样式 */
.bytemd-preview {
  background-color: #ffffff;
  padding: 20px !important;
  min-height: 100% !important;
}

/* 编辑与预览区域高度相同且占满容器 */
.bytemd-split .bytemd-editor, 
.bytemd-split .bytemd-preview {
  height: 100% !important;
  min-height: 100% !important;
  max-height: none !important;
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
  height: 100% !important;
  min-height: 100% !important;
  word-wrap: break-word !important;
  white-space: pre-wrap !important;
  overflow-x: hidden !important;
  width: 100% !important;
  background-color: #ffffff !important;
}

.CodeMirror-scroll {
  overflow-x: hidden !important; /* 防止水平溢出 */
}

.CodeMirror-lines {
  padding: 30px 0 !important;
}

/* 响应式调整 */
@media screen and (max-width: 768px) {
  .editor-container {
    flex-direction: column;
  }
  
  .editor-left-section, 
  .editor-right-section {
    flex: none;
    width: 100%;
    max-height: 50%;
  }
  
  .editor-left-section {
    border-right: none;
    border-bottom: 1px solid #D6C6AF;
  }
}

/* 表单项特定样式 */
.form-item-fixed {
  max-width: 100%;
  width: 100%;
  box-sizing: border-box;
}

.form-item-fixed .form-input,
.form-item-fixed .form-select {
  width: 100%;
  box-sizing: border-box;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 确保输入框文字不会溢出 */
input.form-input, select.form-select {
  width: 100%;
  box-sizing: border-box;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 视频上传控件样式 */
.video-upload-controls {
}
</style> 