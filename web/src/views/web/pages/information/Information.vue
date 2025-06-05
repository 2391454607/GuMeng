<script setup>
import Footer from "@/views/web/layout/Footer.vue";
import {onMounted, reactive, ref} from 'vue';
import {getProjectList} from "@/api/web/IchProject.js";
import { IconSearch, IconLoading, IconPlayArrowFill } from '@arco-design/web-vue/es/icon';
import { Message } from "@arco-design/web-vue";
import { useRouter } from 'vue-router';
import BaikeAssistant from "@/components/BaikeAssistant.vue"; // 引入非遗小助手

// 初始化路由器
const router = useRouter();

//分页器状态
const status = reactive({
  current: 1,
  size: 10
})
//总数
const total = ref(0);
// 分页处理函数
const handlePageChange = (page) => {
  status.current = page;
  getProjectList(status).then((res)=>{
    ichProject.value = res.data.records;
    total.value = res.data.total;
  })
}

const loading = ref(true);
const ichProject = ref({})

onMounted(()=>{
  getProjectList(status).then((res)=>{
    ichProject.value = res.data.records;
    total.value = res.data.total;
    loading.value = false;
  })
})

// 搜索关键词状态
const searchKeyword = ref('');

// 搜索处理方法
const handleSearch = () => {
  loading.value = true;
  // 将搜索条件添加到查询参数中
  const params = {
    ...status,
    keyword: searchKeyword.value,
    area: selectedCategories.value.地域 === '全部' ? '' : selectedCategories.value.地域,
    level: selectedCategories.value.级别 === '全部' ? '' : selectedCategories.value.级别,
    category: selectedCategories.value.类型 === '全部' ? '' : selectedCategories.value.类型
  };

  getProjectList(params).then((res) => {
    ichProject.value = res.data.records;
    total.value = res.data.total;
    loading.value = false;
  });
}
// 添加选中处理方法
const handleCategorySelect = (categoryName, item) => {
  selectedCategories.value[categoryName] = item;
  // 这里可以添加筛选逻辑
}

const categories = ref([
  { name: '所在地区', items: ['全部', '四川', '云南', '福建'] },
  { name: '保护级别', items: ['全部','国家级', '省级', '市级', '县级'] },
  { name: '非遗类型', items: ['全部','民间文学', '传统音乐', '传统舞蹈', '传统戏剧', '曲艺', '传统体育、游艺与杂技', '传统美术', '民俗', '传统技艺', '传统医药'] }
]);

// 添加选中状态管理
const selectedCategories = ref({
  所在地区: '全部',
  保护级别: '全部',
  非遗类型: '全部'
});

// 添加查看详情方法
const viewDetail = (id) => {
  router.push(`/information/detail/${id}`);
}

</script>


<template>
  <div>
    <div class="project-title">
      <div class="title-content">
        <h1>非遗百科</h1>
        <p>传承文化精髓 · 守护非遗瑰宝</p>
      </div>
    </div>

    <div class="information-container">
      <div class="search-section">
        <div class="search-wrapper">
          <IconSearch class="search-icon"/>
          <input 
            v-model="searchKeyword"
            type="text" 
            placeholder="请输入关键词搜索" 
            class="search-input"
            @keyup.enter="handleSearch"
          >
        </div>
      </div>

      <!-- 分类筛选 -->
      <div class="categories-section">
        <div v-for="category in categories" :key="category.name" class="category-group">
          <span class="category-name">{{ category.name }}</span>
          <div class="category-items">
            <span v-for="item in category.items"
                  :key="item"
                  :class="['category-item', { 'category-item-active': selectedCategories[category.name] === item }]"
                  @click="handleCategorySelect(category.name, item)">
              {{ item }}
            </span>
          </div>
        </div>
      </div>

      <a-spin :loading="loading" :size="32" tip="加载中">
        <template #icon><icon-loading /></template>
        <div class="project-grid">
          <a-card
            v-for="project in ichProject"
            :key="project.id"
            class="project-card"
            :bordered="false"
            hoverable
          >
            <div class="project-image">
              <img :src="project.coverImage" :alt="project.name">
              <div class="project-overlay" @click="viewDetail(project.id)">
                <div class="overlay-content">
                  <icon-eye class="overlay-icon" />
                  <span>查看详情</span>
                </div>
              </div>
              <div class="video-badge" v-if="project.video">
                <icon-play-arrow-fill class="video-icon" />
              </div>
            </div>
            <div class="project-content">
              <h3>{{ project.name }}</h3>
              <div class="project-tags">
                <a-tag>{{ project.levelName }}</a-tag>
                <a-tag>{{ project.categoryName }}</a-tag>
              </div>
    
              <div class="project-card-bottom">
                <div class="create-time">
                  {{ new Date(project.createTime).toLocaleDateString() }}
                </div>
                <div>
                  <a-space class="view-count">
                    <icon-eye />
                    {{ project.viewCount }}
                  </a-space>
                </div>
              </div>
            </div>
          </a-card>
        </div>
      </a-spin>

      <!-- 分页器 -->
      <div class="pagination-container">
        <a-pagination
          :total="total"
          :current="status.current"
          :page-size="status.size"
          @change="handlePageChange"
        />
      </div>
    </div>

    <Footer class="footer"/>
    
    <!-- 添加非遗百科助手 -->
    <BaikeAssistant />
  </div>
</template>

<style scoped>
.project-title {
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-image: url('/image/information.png');
  background-size: 100% 100%;  /* 完全覆盖 */
  background-position: center;
  background-repeat: no-repeat;
}

.title-content {
  text-align: center;
  padding: 10px;  /* 从20px减小到10px */
  z-index: 2;
  width: 100%;
}

.title-content h1 {
  font-size: clamp(2em, 4vw, 2.8em);
  color: #8C1F28;
  margin-bottom: 8px;
  font-weight: bold;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
  font-family: "STKaiti", "楷体", serif;
  position: relative;
  display: inline-block;
  animation: titleFadeIn 1.2s ease-out forwards;
}

.title-content p {
  font-size: clamp(1em, 2vw, 1.2em);
  color: #594433;
  font-family: "STFangsong", "仿宋", serif;
  letter-spacing: 2px;
  margin-top: 0;
  animation: subtitleFadeIn 1.5s ease-out forwards;
  position: relative;
}

/* 添加动画关键帧 */
@keyframes titleFadeIn {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes subtitleFadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}


.information-container {
  padding: 20px;
  max-width: 100vw;
  min-height: calc(100vh - 169px);
  margin: 0 auto;
  background-color: #FFF7E9;
}

.search-section {
  display: flex;
  justify-content: center;
}

.search-wrapper {
  position: relative;
  width: 100%;
  max-width: 600px;
}

.search-icon {
  position: absolute;
  left: 15px;
  top: 50%;
  transform: translateY(-50%);
  color: #d32f2f;
  font-size: 20px;
}

.search-input {
  width: 100%;
  padding: 12px 20px 12px 45px;
  border: 2px solid #d32f2f;
  border-radius: 24px;
  font-size: 16px;
  transition: all 0.3s ease;
  background-color: #fff;
}

.search-input:focus {
  outline: none;
  border-color: #b71c1c;
  box-shadow: 0 0 8px rgba(211, 47, 47, 0.3);
}

.search-input::placeholder {
  color: #999;
}

.categories-section {
  margin: 20px 0 0 20px;
}

.category-group {
  margin-bottom: 15px;
}

.category-name {
  font-weight: bold;
  margin-right: 15px;
  color: #b71c1c;
}

.category-items {
  display: inline-flex;
  flex-wrap: wrap;
  gap: 10px;
}

.category-item {
  padding: 4px 12px;
  background: #fff;
  border: 1px solid #d32f2f;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s;
  color: #d32f2f;
}

.category-item-active {
  background: #d32f2f;
  color: #fff;
}

.category-item:hover {
  background: #d32f2f;
  color: #fff;
}

.project-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 24px;
  margin: 20px auto;
  width: 100vw;
  padding: 0 20px;
}

.project-card {
  border: 1px solid #ffcdd2;
  border-radius: 12px;
  overflow: hidden;
  transition: transform 0.3s;
  background: #fff;
  height: 100%;  /* 确保所有卡片高度一致 */
  display: flex;
  flex-direction: column;
}

.project-content {
  padding: 20px;
  flex: 1;  /* 让内容区域占据剩余空间 */
  display: flex;
  flex-direction: column;
}

.project-content p {
  flex: 1;  /* 让描述文字占据剩余空间 */
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
}

.project-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(211, 47, 47, 0.15);
  border-color: #d32f2f;
}

.project-image {
  height: 200px;
  overflow: hidden;
  position: relative;
}

.project-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(33, 33, 33, 0.75);  /* 灰色遮罩 */
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: all 0.3s ease;
  cursor: pointer;
}

.project-card:hover .project-overlay {
  opacity: 1;
}

.overlay-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: white;
  transform: translateY(20px);
  transition: all 0.3s ease;
}

.project-card:hover .overlay-content {
  transform: translateY(0);
}

.overlay-icon {
  font-size: 24px;
  margin-bottom: 8px;
}

.project-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.project-content {
  padding: 15px;
}

.project-content h3 {
  margin: 0 0 10px 0;
  font-size: 18px;
  color: #b71c1c;
}

.project-content p {
  color: #666;
  margin: 0 0 10px 0;
  font-size: 14px;
}

.project-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.project-tags :deep(.ant-tag) {
  border-color: #d32f2f;
  color: #d32f2f;
  background: #fff;
}

.project-card-bottom{
  margin-top: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.view-count {
  color: #d32f2f;
}

.create-time {
  color: #888;
}

.footer {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  bottom: 0;
  color: #b71c1c;
}

:deep(.arco-pagination) {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

:deep(.arco-pagination-item:hover),
:deep(.arco-pagination-item-active) {
  color: #fff !important;
  background-color: #C2101C !important;
  border-color: #C2101C !important;
}

:deep(.arco-pagination-item-previous:hover),
:deep(.arco-pagination-item-next:hover) {
  color: #C2101C !important;
  background-color: #fff !important;
  border-color: #C2101C !important;
}

:deep(.arco-pagination-jumper-input:focus) {
  border-color: #C2101C !important;
  box-shadow: 0 0 0 2px rgba(194, 16, 28, 0.2) !important;
}

:deep(.arco-pagination-jumper-input:hover) {
  border-color: #C2101C !important;
}

:deep(.arco-pagination-total) {
  color: #86909c;
}

/* 加载动画相关样式 */
:deep(.arco-spin) {
  width: 100%;
  display: flex;
  justify-content: center;
}

:deep(.arco-spin-icon) {
  color: #C2101C;
  font-size: 24px;
}

/* 加载提示文字样式 */
:deep(.arco-spin-tip) {
  color: #C2101C;
  font-size: 14px;
  margin-top: 8px;
}

/* 添加视频标识样式 */
.video-badge {
  position: absolute;
  bottom: 8px;
  right: 8px;
  background-color: rgba(211, 47, 47, 0.85);
  padding: 4px;
  border-radius: 50%;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2;
}

.video-icon {
  color: white;
  font-size: 18px;
}
</style>