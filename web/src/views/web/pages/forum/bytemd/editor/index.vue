<script setup>
import { Editor } from 'bytemd';
import { ref, watch, defineEmits, onMounted, computed, nextTick, onUnmounted } from 'vue';

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
  
  // 标题和话题相关属性
  title: String, // 文章标题
  topicId: [String, Number], // 当前选择的话题ID
  topics: Array, // 话题列表
});

// 创建本地副本用于实现双向绑定效果
const localTitle = ref(props.title || '');
const localTopicId = ref(props.topicId || '');

// 监听props变化
watch(() => props.title, (newVal) => {
  localTitle.value = newVal;
});

watch(() => props.topicId, (newVal) => {
  localTopicId.value = newVal;
});

// 监听本地数据变化并向父组件传递
watch(localTitle, (newVal) => {
  emit('titleChange', newVal);
});

watch(localTopicId, (newVal) => {
  emit('topicChange', newVal);
});

const emit = defineEmits(["change", "titleChange", "topicChange"]);

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
    props
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
    clearInterval(intervalId);
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
      
      // 2. 如果没找到匹配，使用索引匹配
      if (!replacementUrl && index < possibleImageUrls.length) {
        replacementUrl = possibleImageUrls[index];
      }
      
      // 3. 如果还是没找到，使用第一个可用URL
      if (!replacementUrl && possibleImageUrls.length > 0) {
        replacementUrl = possibleImageUrls[0];
      }
      
      // 如果找到了替换URL
      if (replacementUrl) {
        img.src = replacementUrl;
        
        // 确保图片可见
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
      // 确保图片正常显示
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
    <!-- 顶部信息栏：集成标题输入和话题选择 -->
    <div class="editor-header">
      <div class="editor-header-left">
        <input 
          v-model="localTitle" 
          class="editor-title-input" 
          placeholder="请输入帖子标题（2-50字）"
          maxlength="50"
        >
      </div>
      <div class="editor-header-right">
        <select v-model="localTopicId" class="editor-topic-select">
          <option value="" disabled>请选择话题</option>
          <option v-for="topic in props.topics" :key="topic.id" :value="topic.id">
            {{ topic.name }}
          </option>
        </select>
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
  flex: 0.9;
  margin-right: 20px;
}

.editor-header-right {
  min-width: 200px;
}

.editor-title-input {
  width: 100%;
  padding: 10px 12px;
  font-size: 18px;
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

.editor-topic-select {
  padding: 10px 12px;
  border: 1px solid #D6C6AF;
  border-radius: 4px;
  background-color: #FFFDF7;
  color: #582F0E;
  font-family: "STKaiti", "楷体", serif;
  width: 100%;
  min-width: 180px;
  cursor: pointer;
  transition: all 0.3s;
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='16' height='16' viewBox='0 0 24 24' fill='none' stroke='%23582F0E' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='M6 9l6 6 6-6'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 8px center;
  background-size: 16px;
  padding-right: 30px;
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.05);
}

.editor-topic-select:focus {
  outline: none;
  border-color: #8C1F28;
  box-shadow: 0 0 0 2px rgba(140, 31, 40, 0.1);
}

.editor-topic-select option {
  background-color: #FFFDF7;
  color: #582F0E;
  padding: 8px;
}

.md-editor {
  flex: 1;
  overflow: hidden;
}

.bytemd {
  height: 100% !important;
  border-radius: 0;
  border: none !important;
  min-height: 600px !important; /* 增加最小高度 */
  max-height: none !important; /* 移除最大高度限制 */
  resize: none !important; /* 禁用调整大小功能 */
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
  min-height: 600px !important; /* 确保预览区域也有足够高度 */
}

/* 确保编辑与预览区域高度相同且固定 */
.bytemd-split .bytemd-editor, 
.bytemd-split .bytemd-preview {
  height: 600px !important;
  min-height: 600px !important;
  max-height: 600px !important;
  overflow-y: auto !important;
}

/* 增强编辑区域样式 */
.CodeMirror {
  font-family: "SimSun", "宋体", serif !important;
  font-size: 16px !important;
  line-height: 1.6 !important;
  padding: 0 10px !important;
  height: auto !important;
  min-height: 600px !important; /* 确保代码编辑区域高度充足 */
}

.CodeMirror-lines {
  padding: 16px 0 !important;
}

/* 增强编辑器内容样式 */
.cm-header {
  color: #8C1F28 !important;
  font-family: "STKaiti", "楷体", serif !important;
}

.cm-strong {
  color: #582F0E !important;
}

.cm-url, .cm-link {
  color: #8C1F28 !important;
}

.cm-quote {
  color: #7F4F24 !important;
  font-style: italic;
}

/* 优化滚动条 */
.CodeMirror-vscrollbar::-webkit-scrollbar,
.bytemd-preview::-webkit-scrollbar {
  width: 8px;
}

.CodeMirror-vscrollbar::-webkit-scrollbar-thumb,
.bytemd-preview::-webkit-scrollbar-thumb {
  background-color: rgba(140, 31, 40, 0.3);
  border-radius: 4px;
}

.CodeMirror-vscrollbar::-webkit-scrollbar-thumb:hover,
.bytemd-preview::-webkit-scrollbar-thumb:hover {
  background-color: rgba(140, 31, 40, 0.5);
}

/* 隐藏水平滚动条 */
.bytemd-preview, 
.bytemd-editor, 
.CodeMirror, 
.bytemd-split {
  overflow-x: hidden !important;
}

/* 隐藏水平滚动条 - Webkit浏览器 */
.bytemd-preview::-webkit-scrollbar-horizontal,
.CodeMirror::-webkit-scrollbar-horizontal,
.CodeMirror-hscrollbar::-webkit-scrollbar {
  display: none !important;
  height: 0 !important;
}

/* 响应式样式 */
@media screen and (max-width: 768px) {
  .editor-header {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
    padding: 10px;
  }
  
  .editor-header-right {
    width: 100%;
  }
  
  .editor-header-left {
    margin-right: 0;
  }
  
  .editor-title-input {
    font-size: 16px;
    padding: 8px 10px;
  }
  
  .editor-topic-select {
    font-size: 14px;
    padding: 8px 10px;
    padding-right: 28px;
    width: 100%;
  }
  
  /* 移动端也保持编辑器高度 */
  .bytemd,
  .CodeMirror,
  .bytemd-preview,
  .bytemd-split .bytemd-editor,
  .bytemd-split .bytemd-preview {
    min-height: 500px !important;
    height: 500px !important;
    max-height: 500px !important;
  }
}

/* 增强预览区域的图片显示 */
:deep(.bytemd-preview img) {
  display: block !important;
  max-width: 100% !important;
  height: auto !important;
  visibility: visible !important;
  opacity: 1 !important;
  margin: 10px auto !important;
  border-radius: 4px !important;
  border: 1px solid #E4D9C3 !important;
}

/* 修复预览区域滚动和尺寸问题 */
:deep(.bytemd-preview) {
  overflow-y: auto !important;
  overflow-x: hidden !important;
  height: auto !important;
  min-height: 500px !important;
  padding: 15px !important;
  box-sizing: border-box !important;
}

:deep(.markdown-body) {
  overflow-wrap: break-word !important;
  word-break: break-word !important;
}

/* 确保图片在编辑器预览和实时预览中都能正确显示 */
:deep(.markdown-body img) {
  max-width: 100% !important;
  box-sizing: content-box !important;
  border-radius: 4px !important;
  border: 1px solid #E4D9C3 !important;
  margin: 10px auto !important;
  display: block !important;
  height: auto !important;
  visibility: visible !important;
  opacity: 1 !important;
}
</style> 