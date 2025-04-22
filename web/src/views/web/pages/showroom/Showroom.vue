<script setup>
import {onMounted, ref} from 'vue';
import Footer from "@/views/web/layout/Footer.vue";
import {getModelList} from "@/api/web/Model.js";

const models = ref([
  {
    id: '',
    name: '',
    modelPic: '',
    author: '',
    views: ''
  },
]);

onMounted(()=>{

  getModelList().then(res =>{
    if (res.code === 200){
      models.value = res.data;
    }
  })
})

const handleModelClick = (id) => {
  window.location.href = `/show3D?model=${id}`;
};
</script>

<template>
  <div class="home">

  <div class="showroom">
    <!-- 分类导航 -->
    <div class="categories">
      <a-space size="large">
        <a-link class="category-link" :class="{ active: true }">全部</a-link>
        <a-link>云南省</a-link>
        <a-link>其他</a-link>
      </a-space>
    </div>

    <!-- 热门标签 -->
    <div class="hot-tags">
      <a-space size="small">
        <a-tag class="custom-tag" hoverable>全部</a-tag>
        <a-tag class="custom-tag" hoverable>陶瓷</a-tag>
        <a-tag class="custom-tag" hoverable>服饰</a-tag>
        <a-tag class="custom-tag" hoverable>金属</a-tag>
        <a-tag class="custom-tag" hoverable>织绣</a-tag>
        <a-tag class="custom-tag" hoverable>雕刻</a-tag>
        <a-tag class="custom-tag" hoverable>编织</a-tag>
        <a-tag class="custom-tag" hoverable>漆器</a-tag>
        <a-tag class="custom-tag" hoverable>纸艺</a-tag>
        <a-tag class="custom-tag" hoverable>其他</a-tag>
      </a-space>
    </div>

    <!-- 模型列表 -->
    <div class="model-grid">
      <div v-for="model in models" :key="model.id" class="model-card" @click="handleModelClick(model.id)">
        <div class="model-image">
          <img :src="model.modelPic" :alt="model.name">
          <div class="model-overlay">
            <span class="view-detail">查看详情</span>
          </div>
        </div>
        <div class="model-info">
          <div class="model-name">{{ model.name }}</div>
          <div class="model-meta">
            <span class="author">
              <icon-user /> {{ model.author }}
            </span>
            <span class="views">
              <icon-eye /> {{ model.views }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>

  <div>
    <Footer class="footer"></Footer>
  </div>
  </div>
</template>

<style scoped>
.home {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.showroom {
  flex: 1;
  max-width: 100vw;
  padding: 40px;
  background-color: #fdf6e3; /* 更改为米色背景 */
  min-height: calc(100vh - 80px);
}

.categories {
  margin-bottom: 30px;
  padding: 15px 0;
  border-bottom: 1px solid #8b1f1f; /* 更改边框颜色为深红色 */
  position: sticky;
  top: 0;
  background: #fdf6e3;
  z-index: 100;
}

.category-link {
  font-size: 16px;
  padding: 6px 12px;
  border-radius: 4px;
  transition: all 0.3s;
  color: #8b1f1f; /* 更改链接颜色为深红色 */
}

.category-link:hover,
.category-link.active {
  background: rgba(139, 31, 31, 0.1); /* 更改悬浮背景为深红色透明版 */
  color: #8b1f1f;
}

.custom-tag {
  padding: 6px 12px;
  border-radius: 16px;
  transition: all 0.3s;
  cursor: pointer;
  border: 1px solid #8b1f1f; /* 添加深红色边框 */
  color: #8b1f1f;
}

.custom-tag:hover {
  background: rgba(139, 31, 31, 0.1);
  color: #8b1f1f;
}

.model-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 10px rgba(139, 31, 31, 0.1); /* 更改阴影颜色 */
}

.model-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 8px 20px rgba(139, 31, 31, 0.15);
}

.hot-tags {
  margin-bottom: 30px;
}

.custom-tag {
  padding: 6px 12px;
  border-radius: 16px;
  transition: all 0.3s;
  cursor: pointer;
}

.custom-tag:hover {
  background: var(--color-primary-light-1);
  color: var(--color-primary);
}

.model-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 30px;
  padding: 20px 0;
}

.model-card {
  background: var(--color-bg-2);
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}

.model-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 8px 20px rgba(0,0,0,0.1);
}

.model-image {
  width: 100%;
  height: 220px;
  overflow: hidden;
  position: relative;
}

.model-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.model-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: all 0.3s;
}

.model-card:hover .model-overlay {
  opacity: 1;
}

.model-card:hover .model-image img {
  transform: scale(1.05);
}

.view-detail {
  color: white;
  font-size: 16px;
  padding: 8px 16px;
  border: 2px solid white;
  border-radius: 20px;
}

.model-info {
  padding: 16px;
}

.model-name {
  font-size: 18px;
  font-weight: 500;
  margin-bottom: 12px;
  color: #8b1f1f; /* 更改标题颜色 */
}

.model-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #666; /* 更改元信息颜色 */
  font-size: 14px;
}

.author, .views {
  display: flex;
  align-items: center;
  gap: 4px;
}

</style>