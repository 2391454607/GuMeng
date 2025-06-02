<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getProjectDetail } from '@/api/web/IchProject.js';
import { Message } from '@arco-design/web-vue';
import { IconArrowLeft, IconCalendar, IconEye, IconLoading } from '@arco-design/web-vue/es/icon';
import Footer from "@/views/web/layout/Footer.vue";

const route = useRoute();
const router = useRouter();
const loading = ref(true);
const projectDetail = ref({});

onMounted(() => {
  const id = route.params.id;
  if (!id) {
    Message.error('项目ID不存在');
    router.push('/information');
    return;
  }
  
  fetchProjectDetail(id);
});

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
    } else {
      Message.error(res.msg || '获取项目详情失败');
      router.push('/information');
    }
  } catch (error) {
    console.error('获取项目详情错误:', error);
    Message.error('获取项目详情失败，请稍后重试');
    router.push('/information');
  } finally {
    loading.value = false;
  }
};

const goBack = () => {
  router.push('/information');
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
            <!-- 移除了封面图片部分 -->
            
            <div class="detail-info">
              
              <div class="info-section" v-if="projectDetail.video">
                <h2>项目视频</h2>
                <div class="project-video">
                  <video 
                    :src="projectDetail.video" 
                    controls 
                    class="video-player"
                    :poster="projectDetail.coverImage"
                    preload="metadata"
                  ></video>
                </div>
              </div>
              
              <div class="info-section" v-if="projectDetail.content">
                <h2>详细介绍</h2>
                <div class="content" v-html="projectDetail.content"></div>
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
}

/* 扩展内容显示样式 */
.content :deep(h1),
.content :deep(h2),
.content :deep(h3),
.content :deep(h4) {
  color: #8C1F28;
  font-family: "STKaiti", "楷体", serif;
  margin: 20px 0 10px;
  padding-bottom: 5px;
}

.content :deep(h1),
.content :deep(h2) {
  border-bottom: 1px solid #E4D9C3;
}

.content :deep(img) {
  max-width: 100%;
  display: block;
  margin: 15px auto;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.content :deep(p) {
  margin: 16px 0;
  line-height: 1.8;
}

.content :deep(ul),
.content :deep(ol) {
  padding-left: 20px;
  margin: 16px 0;
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