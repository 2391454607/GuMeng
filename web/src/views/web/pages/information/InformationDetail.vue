<script setup>
import { ref, onMounted, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getProjectDetail } from '@/api/web/IchProject.js';
import { Message } from '@arco-design/web-vue';
import { IconArrowLeft, IconCalendar, IconEye, IconLoading } from '@arco-design/web-vue/es/icon';
import Footer from "@/views/web/layout/Footer.vue";
import BaikeAssistant from "@/components/BaikeAssistant.vue";
// 导入ByteMD的Viewer组件
import { Viewer } from '@/views/manage/sys/project/bytemd';
// 导入ByteMD插件
import gfm from '@bytemd/plugin-gfm';
import highlight from '@bytemd/plugin-highlight';
import gemoji from '@bytemd/plugin-gemoji';
// 导入ByteMD样式
import 'bytemd/dist/index.css';

// ByteMD插件
const plugins = [
  gfm(),
  highlight(),
  gemoji()
];

const route = useRoute();
const router = useRouter();
const loading = ref(true);
const projectDetail = ref({});
const qiniuUrls = ref([]);
const viewerQiniuUrls = ref([]);

onMounted(() => {
  const id = route.params.id;
  if (!id) {
    Message.error('项目ID不存在');
    router.push('/information');
    return;
  }
  
  fetchProjectDetail(id);
});


const extractQiniuUrls = (content) => {
  // 优先使用项目的images字段
  if (projectDetail.value && projectDetail.value.images) {
    if (typeof projectDetail.value.images === 'string') {
      const imageArray = projectDetail.value.images.split(',');
      if (imageArray.length > 0) {
        return imageArray;
      }
    }
  }
  
  if (!content) return [];

  // 匹配七牛云域名图片
  const qiniuDomainPattern = /https?:\/\/[^)\s"'<>]+?(?:clouddn\.com|hn-bkt\.clouddn\.com)[^)\s"'<>]*?\.(?:png|jpg|jpeg|gif|webp)/gi;
  let qiniuLinks = content.match(qiniuDomainPattern) || [];
  

  if (qiniuLinks.length === 0) {
    const genericImagePattern = /https?:\/\/[^)\s"'<>]+\.(?:png|jpg|jpeg|gif|webp)/gi;
    qiniuLinks = content.match(genericImagePattern) || [];
  }
  
  return qiniuLinks;
};

// 修复图片显示的函数
const fixImages = () => {
  const viewer = document.querySelector('.information-detail-container .markdown-body');
  if (!viewer) return;

  const images = viewer.querySelectorAll('img');
  if (!images.length) return;

  if (qiniuUrls.value.length === 0) {
    qiniuUrls.value = extractQiniuUrls(projectDetail.value.content || '');
  }

  if (qiniuUrls.value.length === 0) return;

  images.forEach((img, index) => {
    const src = img.getAttribute('src');
    const isInvalidUrl = !src || 
                         src === 'undefined' || 
                         src.includes('localhost') || 
                         src.includes('undefined') ||
                         src.includes('/information/detail/');
    
    if (isInvalidUrl) {
      const newSrc = index < qiniuUrls.value.length ? 
        qiniuUrls.value[index] : 
        qiniuUrls.value[0];
      
      if (newSrc) {
        img.src = newSrc;
        img.style.maxWidth = '45%';
        img.style.width = 'auto';
        img.style.height = 'auto';
        img.style.margin = '15px auto';
        img.style.display = 'block';
      }
    }
  });
};

const fetchProjectDetail = async (id) => {
  try {
    loading.value = true;
    const res = await getProjectDetail(id);
    if (res.code === 200) {
      projectDetail.value = res.data;
      document.title = `${projectDetail.value.name} - 非遗百科 - 古梦`;
      
      // 从localStorage获取详细内容
      const PROJECT_CONTENT_KEY_PREFIX = 'project_content_';
      const content = localStorage.getItem(`${PROJECT_CONTENT_KEY_PREFIX}${id}`);
      if (content) {
        projectDetail.value.content = content;
      }

      qiniuUrls.value = extractQiniuUrls(projectDetail.value.content || '');

      nextTick(() => {
        setTimeout(fixImages, 300);
      });
    } else {
      Message.error(res.msg || '获取项目详情失败');
      router.push('/information');
    }
  } catch (error) {
    Message.error('获取项目详情失败，请稍后重试');
    router.push('/information');
  } finally {
    loading.value = false;
  }
};

const customPlugins = () => {
  return plugins.concat([
    {
      viewerEffect({ markdownBody }) {
        nextTick(() => {
          fixImages();
        });
      }
    }
  ]);
};

const goBack = () => {
  router.push('/information');
};

// 修复图片显示函数
const fixViewerImages = () => {
  setTimeout(() => {
    const viewer = document.querySelector('.project-view-container .markdown-body');
    if (!viewer) return;

    const images = viewer.querySelectorAll('img');
    if (!images.length) return;

    if (viewerQiniuUrls.value.length === 0) {
      viewerQiniuUrls.value = extractQiniuUrls(projectDetail.value.content || '');
    }

    if (viewerQiniuUrls.value.length === 0) return;

    images.forEach((img, index) => {
      const src = img.getAttribute('src');
      const isInvalidUrl = !src || 
                          src === 'undefined' || 
                          src.includes('localhost') || 
                          src.includes('undefined');
      
      if (isInvalidUrl) {
        const newSrc = index < viewerQiniuUrls.value.length ? 
          viewerQiniuUrls.value[index] : 
          viewerQiniuUrls.value[0];
        
        if (newSrc) {
          img.src = newSrc;
          img.style.maxWidth = '45%';
          img.style.width = 'auto';
          img.style.height = 'auto';
          img.style.margin = '15px auto';
          img.style.display = 'block';
        }
      }
    });
  }, 300);
};
</script>

<template>
  <div>
    <div class="information-detail-container">
      <a-spin :loading="loading" :size="40">
        <template #icon><icon-loading /></template>
        
        <div class="detail-header">
          <div class="back-button" @click="goBack">
            <icon-arrow-left />
            <span>返回列表</span>
          </div>
          <h1 class="project-title">{{ projectDetail.name }}</h1>
          
          <!-- 基本信息整合在标题下方 -->
          <div class="project-meta">
            <span v-if="projectDetail.levelName" class="meta-item">{{ projectDetail.levelName }}</span>
            <span v-if="projectDetail.categoryName" class="meta-item">{{ projectDetail.categoryName }}</span>
            <span class="meta-item">
              <icon-eye /> {{ projectDetail.viewCount || 0 }} 次浏览
            </span>
            <span class="meta-item">
              <icon-calendar /> 创建时间: {{ projectDetail.createTime ? new Date(projectDetail.createTime).toLocaleDateString('zh-CN', {year: 'numeric', month: '2-digit', day: '2-digit'}) : '未知日期' }}
            </span>
          </div>
        </div>
        
        <div class="detail-content">
          <div class="detail-main">
            <div class="detail-info">
              
              <div class="info-section" v-if="projectDetail.video">
                <h2>项目视频</h2>
                <div class="project-video">
                  <video 
                    :src="projectDetail.video" 
                    controls 
                    class="video-player"
                    preload="metadata"
                  ></video>
                </div>
              </div>
              
              <div v-if="projectDetail.content">
                <div class="content">
                  <Viewer :value="projectDetail.content" :plugins="customPlugins()" />
                </div>
              </div>
              
              <div class="info-section" v-if="projectDetail.inheritors && projectDetail.inheritors.length > 0">
                <h2>传承人</h2>
                <div class="inheritors-list">
                  <div v-for="(inheritor, index) in projectDetail.inheritors" :key="index" class="inheritor-item">
                    <div class="inheritor-avatar">
                      <img :src="inheritor.avatar || '/image/default-avatar.png'" :alt="inheritor.name">
                    </div>
                    <div class="inheritor-info">
                      <h3>{{ inheritor.name }}</h3>
                      <p>{{ inheritor.level }}</p>
                      <p>{{ inheritor.description }}</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </a-spin>
    </div>
    
    <Footer class="footer"/>
    
    <BaikeAssistant />
  </div>
</template>

<style scoped>
.information-detail-container {
  background-color: #FFF7E9;
  min-height: calc(100vh - 169px);
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.detail-header, .detail-content {
  width: 100%;
  max-width: 900px;
}

.detail-header {
  margin-bottom: 20px;
  position: relative;
}

.back-button {
  display: inline-flex;
  align-items: center;
  color: #d32f2f;
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 20px;
  background-color: white;
  border: 1px solid #d32f2f;
  transition: all 0.3s;
  margin-bottom: 15px;
}

.back-button:hover {
  background-color: #d32f2f;
  color: white;
}

.back-button span {
  margin-left: 8px;
}

.project-title {
  font-size: 2rem;
  color: #8C1F28;
  text-align: center;
  margin-bottom: 10px;
  font-family: "STKaiti", "楷体", serif;
}

.footer-spacer {
  height: 50px; 
}

/* 页脚样式 */
.footer {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  bottom: 0;
  color: #b71c1c;
}

/* 新增标题下方元数据样式 */
.project-meta {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 15px;
  margin-bottom: 20px;
  color: #666;
}

.meta-item {
  display: inline-flex;
  align-items: center;
  font-size: 1rem;
  margin-right: 5px;
}

.meta-item .arco-icon {
  margin-right: 4px;
}

.detail-content {
  background-color: #FFF7E9;
  padding: 20px 0;
}

.detail-main {
  display: flex;
  flex-direction: column;
}

.detail-info {
  flex: 1;
}

.info-section {
  margin-bottom: 30px;
}

.info-section h2 {
  font-size: 1.3rem;
  color: #8C1F28;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
  font-family: "STKaiti", "楷体", serif;
}

.content {
  max-width: 100%;
  overflow-wrap: break-word;
  padding: 15px;
  border-radius: 8px;
  margin: 0 auto;
}

/* 自定义Viewer组件样式 */
:deep(.markdown-body) {
  background-color: transparent;
  font-family: "SimSun", "宋体", serif;
  line-height: 1.8;
  color: #333;
  display: flex;
  flex-direction: column;
  align-items: center;
}

:deep(.markdown-body img) {
  max-width: 60% !important;
  width: auto !important;
  height: auto !important;
  display: block !important;
  margin: 15px auto !important;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

:deep(.markdown-body h1),
:deep(.markdown-body h2),
:deep(.markdown-body h3),
:deep(.markdown-body h4) {
  color: #8C1F28;
  font-family: "STKaiti", "楷体", serif;
  margin: 20px 0 10px;
  padding-bottom: 5px;
  border-bottom: none;
  width: 100%;
}

:deep(.markdown-body h1, .markdown-body h2) {
  border-bottom: none;
}

:deep(.markdown-body p) {
  margin: 16px 0;
  line-height: 1.8;
  width: 100%;
}

:deep(.markdown-body ul),
:deep(.markdown-body ol) {
  padding-left: 20px;
  margin: 16px 0;
  width: 100%;
}

.project-video {
  width: 100%;
  margin: 10px 0;
  border-radius: 8px;
  overflow: hidden;
  background-color: #000;
}

.video-player {
  width: 100%;
  max-height: 450px;
  display: block;
  margin: 0 auto;
}

.inheritors-list {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.inheritor-item {
  display: flex;
  background-color: #f9f9f9;
  padding: 15px;
  border-radius: 8px;
  width: 100%;
  max-width: 500px;
}

.inheritor-avatar {
  width: 80px;
  height: 80px;
  margin-right: 15px;
}

.inheritor-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

.inheritor-info {
  flex: 1;
}

.inheritor-info h3 {
  margin: 0 0 8px 0;
  color: #8C1F28;
}

.inheritor-info p {
  margin: 5px 0;
  color: #666;
}
</style> 