<script setup>
import { getProcessor } from 'bytemd';
import { defineProps, ref, computed, nextTick, watch, onMounted, onUnmounted } from 'vue';

const props = defineProps(["value", "plugins", "sanitize", "remarkRehype"]);

const markdownBody = ref(null);
const cbs = ref([]);

//获取文档对象
const file = computed(() => {
  //同步处理文档对象的值
  return getProcessor(props).processSync(props.value || '');
});

const needUpdate = computed(() => {
  return [file.value, props.plugins, props.sanitize, props.remarkRehype];
});

watch(() => needUpdate.value, () => {
  off();
  nextTick(() => {
    on();
    // 触发图片修复
    fixImages();
  });
}, {
  deep: true
});

onMounted(() => {
  on();
  // 在组件挂载后延迟执行图片修复
  setTimeout(() => {
    fixImages();
  }, 300);
});

onUnmounted(() => {
  off();
});

// 添加图片修复函数
const fixImages = () => {
  if (!markdownBody.value) return;
  
  // 查找所有图片并应用样式
  const images = markdownBody.value.querySelectorAll('img');
  console.log('Viewer中找到图片数量:', images.length);
  
  images.forEach((img, index) => {
    // 强制应用样式，图片可见
    img.style.display = 'block';
    img.style.visibility = 'visible';
    img.style.opacity = '1';
    img.style.maxWidth = '100%';
    img.style.height = 'auto';
    img.style.margin = '10px auto';
    img.style.border = '1px solid #E4D9C3';
    img.style.borderRadius = '4px';
    
    // 添加图片加载错误处理
    img.onerror = function() {
      console.error('图片加载失败:', img.src);
      this.onerror = null;
      // 尝试重新加载
      setTimeout(() => {
        const newSrc = this.src.includes('?') ? 
          this.src + '&reload=' + new Date().getTime() : 
          this.src + '?reload=' + new Date().getTime();
        this.src = newSrc;
      }, 300);
    };
    
    console.log(`预览图片 ${index+1}:`, img.src);
  });
};

const handleClick = e => {
  const target = e.target;
  if (target.tagName !== 'A') return;
  const href = target.getAttribute("href");
  if (!href || !href.startsWith('#')) return;
  const dest = markdownBody.value.querySelector('#user-content-' + href.slice(1));
  if (dest) dest.scrollIntoView();
};

const off = () => {
  if (cbs.value.length) {
    cbs.value.forEach(cb => cb && cb());
  }
};

const on = () => {
  if (props.plugins && file.value) {
    cbs.value = props.plugins.map(({ viewerEffect }) => viewerEffect && viewerEffect({ markdownBody: markdownBody.value, file: file.value }));
  }
};
</script>

<template>
  <div v-html="file.toString()" class="markdown-body" @click="handleClick" ref="markdownBody">
  </div>
</template>

<style>
.bytemd {
  height: 100% !important;
  width: 100% !important;
  overflow-x: hidden !important;
}

/* 增强预览区域样式 */
.markdown-body {
  font-family: "SimSun", "宋体", serif;
  color: #582F0E;
  padding: 15px;
  overflow-wrap: break-word;
  overflow-x: hidden !important;
  width: 100% !important;
  max-width: 100% !important;
  word-break: break-word !important;
}

.markdown-body h1,
.markdown-body h2,
.markdown-body h3,
.markdown-body h4,
.markdown-body h5,
.markdown-body h6 {
  color: #8C1F28;
  font-family: "STKaiti", "楷体", serif;
  margin-top: 24px;
  margin-bottom: 16px;
  font-weight: 600;
  border-bottom: 1px solid #E4D9C3;
  padding-bottom: 0.3em;
}

.markdown-body a {
  color: #8C1F28;
  text-decoration: none;
  word-break: break-word !important;
  overflow-wrap: break-word !important;
}

.markdown-body a:hover {
  text-decoration: underline;
}

.markdown-body blockquote {
  padding: 0 1em;
  color: #6B5B45;
  border-left: 0.25em solid #E4D9C3;
}

.markdown-body pre {
  background-color: #FFF7E9;
  border: 1px solid #E4D9C3;
  border-radius: 4px;
  overflow-x: auto !important;
  max-width: 100% !important;
  white-space: pre-wrap !important;
}

.markdown-body code {
  background-color: #FFF7E9;
  color: #8C1F28;
  padding: 0.2em 0.4em;
  border-radius: 3px;
  font-family: "SFMono-Regular", Consolas, "Liberation Mono", Menlo, monospace;
  white-space: pre-wrap !important; /* 允许代码换行 */
  word-break: break-word !important; /* 在适当的位置换行 */
}

.markdown-body img {
  max-width: 100% !important;
  box-sizing: content-box !important;
  border-radius: 4px !important;
  border: 1px solid #E4D9C3 !important;
  margin: 10px 0 !important;
  display: block !important;
  height: auto !important;
  object-fit: contain !important;
  visibility: visible !important;
  opacity: 1 !important;
}

/* 添加表格样式，防止溢出 */
.markdown-body table {
  display: block !important;
  width: 100% !important;
  overflow-x: auto !important;
  max-width: 100% !important;
}
</style> 