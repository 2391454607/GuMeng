<script setup>
import Footer from "@/views/web/layout/Footer.vue";
import {onMounted, reactive, ref} from 'vue';
import {getProjectList} from "@/api/web/IchProject.js";
import { IconSearch } from '@arco-design/web-vue/es/icon';
import { Message } from "@arco-design/web-vue";
import { useRouter } from 'vue-router';


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

const categories = ref([
  { name: '地域', items: ['四川', '云南', '福建'] },
  { name: '级别', items: ['国家级', '省级', '市级', '县级'] },
  { name: '类型', items: ['民间文学', '传统音乐', '传统舞蹈', '传统戏剧', '曲艺', '传统体育、游艺与杂技', '传统美术', '民俗', '传统技艺', '传统医药'] }
]);

const router = useRouter();

// 添加查看详情方法
const viewDetail = (id) => {
  Message.success("查看详情")
}

</script>

<!-- 修改搜索框模板部分 -->
<template>

  <div class="project-title">
    123
  </div>

  <div class="information-container">
    <div class="search-section">
      <div class="search-wrapper">
        <IconSearch class="search-icon"/>
        <input type="text" placeholder="请输入关键词搜索" class="search-input">
      </div>
    </div>

    <!-- 分类筛选 -->
    <div class="categories-section">
      <div v-for="category in categories" :key="category.name" class="category-group">
        <span class="category-name">{{ category.name }}</span>
        <div class="category-items">
          <span v-for="item in category.items"
                :key="item"
                class="category-item">
            {{ item }}
          </span>
        </div>
      </div>
    </div>

    <!-- 修改文章列表部分 -->
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
        </div>
        <div class="project-content">
          <h3>{{ project.name }}</h3>
          <p>{{ project.summary }}</p>
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

    <!-- 添加分页器 -->
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
</template>

<style scoped>
.project-title{
  width: 100vw;
  height: calc(100vh - 700px);
  display: flex;
  align-items: center;
  justify-content: center;
}

.information-container {
  padding: 20px;
  max-width: 100vw;
  min-height: calc(100vh - 169px);
  margin: 0 auto;
  background-color: #fff;
}

.search-section {
  margin: 20px 0;
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
  margin: 20px 0;
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

.category-item:hover {
  background: #d32f2f;
  color: #fff;
}

.project-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin: 20px 0;
}

.project-card {
  border: 1px solid #ffcdd2;
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.3s;
  background: #fff;
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
  background: rgba(33, 33, 33, 0.75);  /* 修改为灰色遮罩 */
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
</style>