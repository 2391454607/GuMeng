<script setup>
import {ref, onMounted, onUnmounted} from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getPolicyInfoAPI } from '@/api/web/Web.js';
import { Message } from '@arco-design/web-vue';
import { IconLeft,IconDownload } from '@arco-design/web-vue/es/icon';
import Footer from "@/views/web/layout/Footer.vue";
import * as pdfjsLib from 'pdfjs-dist';


const route = useRoute();
const router = useRouter();
const loading = ref(true);
const policyDetail = ref([]);
const pdfContainer = ref(null);
const totalPages = ref(0);
let pdfDoc = null;

// 返回上一页面函数
const goBack = () => {
  router.back();
};

// 下载文件函数
const downloadPDF = () => {
  if (!policyDetail.value.content) {
    Message.error('文件不存在');
    return;
  }

  try {
    const binary = atob(policyDetail.value.content);
    const bytes = new Uint8Array(binary.length);
    for (let i = 0; i < binary.length; i++) {
      bytes[i] = binary.charCodeAt(i);
    }

    const blob = new Blob([bytes], { type: 'application/pdf' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `${policyDetail.value.title || '政策文件'}.pdf`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  } catch (error) {
    console.error('下载失败:', error);
    Message.error('下载失败');
  }
};

// 返回顶部功能
const showBackTop = ref(false);
const handleScroll = () => {
  showBackTop.value = window.scrollY > 300;
};
const scrollToTop = () => {
  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  });
};

// 设置 PDF.js worker
pdfjsLib.GlobalWorkerOptions.workerSrc = new URL(
    'pdfjs-dist/build/pdf.worker.mjs',
    import.meta.url
).toString();

// PDF 渲染函数
const renderAllPages = async () => {
  try {
    if (pdfContainer.value) {
      pdfContainer.value.innerHTML = '';
      
      // 创建一个容器来存放所有页面
      const container = document.createElement('div');
      container.style.display = 'flex';
      container.style.flexDirection = 'column';
      container.style.gap = '20px';
      container.style.backgroundColor = '#FFF7E9';
      
      // 渲染所有页面
      for (let pageNum = 1; pageNum <= pdfDoc.numPages; pageNum++) {
        const page = await pdfDoc.getPage(pageNum);
        const viewport = page.getViewport({ scale: 1.5 });

        const canvas = document.createElement('canvas');
        const context = canvas.getContext('2d');
        canvas.height = viewport.height;
        canvas.width = viewport.width;

        // 设置背景色
        context.fillStyle = '#FFF7E9';
        context.fillRect(0, 0, canvas.width, canvas.height);

        const renderContext = {
          canvasContext: context,
          viewport: viewport
        };

        await page.render(renderContext).promise;
        container.appendChild(canvas);
      }

      pdfContainer.value.appendChild(container);
    }
  } catch (error) {
    console.error('PDF 渲染失败:', error);
    Message.error('PDF 渲染失败');
  }
};

// PDF 加载函数
const loadPDF = async (base64Data) => {
  try {
    // 创建 Uint8Array 来处理二进制数据
    const binaryString = atob(base64Data);
    const bytes = new Uint8Array(binaryString.length);
    for (let i = 0; i < binaryString.length; i++) {
      bytes[i] = binaryString.charCodeAt(i);
    }

    const loadingTask = pdfjsLib.getDocument({
      data: bytes,
      cMapUrl: 'https://unpkg.com/pdfjs-dist@latest/cmaps/',
      cMapPacked: true,
      standardFontDataUrl: 'https://unpkg.com/pdfjs-dist@latest/standard_fonts/'
    });
    
    pdfDoc = await loadingTask.promise;
    totalPages.value = pdfDoc.numPages;
    await renderAllPages();
  } catch (error) {
    console.error('PDF 加载失败:', error);
    console.error('错误详情:', error.message);
    Message.error('PDF 加载失败');
  }
};

onMounted(async () => {
  try {
    const res = await getPolicyInfoAPI(route.query.id);
    if (res.code === 200) {
      policyDetail.value = res.data;
      if (policyDetail.value.content) {
        console.log('开始加载PDF');
        await loadPDF(policyDetail.value.content);
      }
    } else {
      Message.error('获取详情失败');
    }
  } catch (error) {
    console.error('获取详情失败:', error);
    Message.error('获取详情失败');
  } finally {
    loading.value = false;
  }
  // 滚动监听
  window.addEventListener('scroll', handleScroll);
});

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll);
});

// 日期格式化函数
const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}年${month}月${day}日`;
};


</script>

<template>
  <div class="policy-detail-container">
    <div class="detail-header">
      <div class="header-content">
        <a-button class="back-btn" @click="goBack">
          <template #icon><icon-left /></template>
          返回
        </a-button>
        <a-button class="download-btn" @click="downloadPDF">
          <template #icon><icon-download /></template>
          下载文件
        </a-button>
      </div>
    </div>

    <a-spin :loading="loading" :size="32">
      <div class="detail-content" v-if="policyDetail">
        <div class="content-header">
          <h1 class="title">{{ policyDetail.title }}</h1>
          <h2 class="type">{{ policyDetail.type }}</h2>
          <div class="meta">
            <span v-if="policyDetail.documentNumber">发文字号：{{ policyDetail.documentNumber }}</span>
            <span v-if="policyDetail.publishOrg">发布机构：{{ policyDetail.publishOrg }}</span>
            <span v-if="policyDetail.publishDate">颁布日期：{{ formatDate(policyDetail.publishDate) }}</span>
            <span v-if="policyDetail.effectiveDate">生效日期：{{ formatDate(policyDetail.effectiveDate) }}</span>
          </div>
        </div>
        <div ref="pdfContainer" class="pdf-container"></div>
      </div>
    </a-spin>
    
    <!-- 添加返回顶部按钮 -->
    <a-button 
      v-show="showBackTop" 
      class="back-top-btn" 
      shape="circle"
      @click="scrollToTop"
    >
      <template #icon><icon-arrow-rise /></template>
    </a-button>
  </div>
  <Footer class="footer"></Footer>
</template>

<style scoped>
.policy-detail-container {
  background: #FFF7E9;
  padding: 40px 24px;
}

.detail-header {
  max-width: 1200px;
  margin: 0 auto 24px;
  width: 100%;
}

.back-btn {
  color: #C2101C;
  border-color: #C2101C;
  padding: 4px 16px;
}

.back-btn:hover {
  background-color: #C2101C;
  color: #FFF7E9;
}

.detail-content {
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

.content-header {
  background: #FFF7E9;
  border-radius: 8px;
  margin-bottom: 24px;
}

.title {
  font-size: 28px;
  margin-bottom: 16px;
  text-align: center;
  font-weight: 600;
}

.type {
  font-size: 20px;
  text-align: center;
  margin-bottom: 24px;
  font-weight: 500;
}

.meta {
  display: flex;
  gap: 32px;
  justify-content: center;
  color: #86909c;
  flex-wrap: wrap;
  font-size: 15px;
}

.pdf-container {
  background: #FFF7E9;
  padding: 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.pdf-container :deep(canvas) {
  width: 100%;
  height: auto;
  margin: 0 auto;
}

:deep(.arco-spin) {
  display: block;
  width: 100%;
  background: #FFF7E9;
  min-height: calc(100vh - 200px);
}

:deep(.arco-spin-mask) {
  background-color: rgba(255, 247, 233, 0.6);
}

:deep(.arco-spin-dot) {
  color: #C2101C;
}

.footer{
  display: flex;
  bottom: 0;
}

.back-top-btn {
  position: fixed;
  right: 80px;
  bottom: 60px;
  width: 60px;
  height: 60px;
  color: #C2101C;
  border-color: #C2101C;
  background: #FFF7E9;
  z-index: 100;
  font-size: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.back-top-btn:hover {
  background: #C2101C;
  color: #FFF7E9;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(194, 16, 28, 0.2);
}
.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

.download-btn {
  color: #C2101C;
  border-color: #C2101C;
  padding: 4px 16px;
}

.download-btn:hover {
  background-color: #C2101C;
  color: #FFF7E9;
}
</style>