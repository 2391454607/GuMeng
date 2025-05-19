<script setup>
import { ref } from 'vue';
import { Message } from '@arco-design/web-vue';
import { IconRobot, IconComputer, IconUser } from '@arco-design/web-vue/es/icon';
import Footer from "@/views/web/layout/Footer.vue";

const handleClick = (type) => {
  Message.success(`即将体验${type}`);
};
</script>

<template>
  <div class="features-container">
    <div class="features-grid">
      <!-- 非遗智能体卡片 -->
      <div class="feature-card">
        <div class="icon-wrapper blue">
          <icon-robot />
        </div>
        <h2>非遗智能体</h2>
        <p>智能问答助手，为您解答非遗相关问题，提供专业的非遗知识服务。</p>
        <a-button type="outline" class="feature-button" @click="handleClick('非遗智能体')">
          开始对话
          <template #icon><icon-right /></template>
        </a-button>
      </div>

      <!-- 非遗小游戏卡片 -->
      <div class="feature-card">
        <div class="icon-wrapper green">
          <icon-computer />
        </div>
        <h2>非遗小游戏</h2>
        <p>趣味互动游戏，寓教于乐，让您在游戏中了解非遗文化知识。</p>
        <a-button type="outline" class="feature-button" @click="handleClick('非遗小游戏')">
          开始游戏
          <template #icon><icon-right /></template>
        </a-button>
      </div>

      <!-- 非遗虚拟人卡片 -->
      <div class="feature-card">
        <div class="icon-wrapper purple">
          <icon-user />
        </div>
        <h2>非遗虚拟人</h2>
        <p>数字化非遗传承人，带您身临其境体验非遗技艺的魅力。</p>
        <a-button type="outline" class="feature-button" @click="handleClick('非遗虚拟人')">
          虚拟体验
          <template #icon><icon-right /></template>
        </a-button>
      </div>
    </div>
  </div>

  <Footer class="footer"/>
</template>

<style scoped>
.features-container {
  min-height: calc(100vh - 249px);
  padding: 60px 20px;
  background: #faf6f0;  /* 米色背景 */
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 30px;
  padding: 80px 50px 0 50px;
}

.feature-card {
  background: #fff;
  border-radius: 8px;
  padding: 30px;
  text-align: center;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(194, 16, 28, 0.08);
  border: 1px solid rgba(194, 16, 28, 0.1);
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(194, 16, 28, 0.15);
  border-color: #C2101C;
}

.icon-wrapper {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  transition: all 0.3s ease;
  background-size: 200% auto;
}

.icon-wrapper :deep(svg) {
  font-size: 36px;
  color: white;
  transition: transform 0.3s ease;
}

.blue {
  background-image: linear-gradient(135deg, #C2101C, #ff4d4d, #C2101C);
}

.green {
  background-image: linear-gradient(135deg, #C2101C, #ff6b6b, #C2101C);
}

.purple {
  background-image: linear-gradient(135deg, #C2101C, #ff8585, #C2101C);
}

.feature-card:hover .icon-wrapper {
  background-position: right center;
}

.feature-card:hover .icon-wrapper :deep(svg) {
  transform: scale(1.1);
}

.feature-card h2 {
  font-size: 24px;
  margin-bottom: 16px;
  color: #C2101C;
  font-weight: bold;
}

.feature-card p {
  color: #666;
  margin-bottom: 24px;
  line-height: 1.6;
}

.feature-button {
  padding: 8px 24px;
  border-radius: 20px;
  transition: all 0.3s ease;
  color: #C2101C;
  border-color: #C2101C;
}

.feature-button:hover {
  transform: translateX(5px);
  background-color: #C2101C;
  color: white;
  border-color: #C2101C;
}

@media (max-width: 768px) {
  .features-grid {
    grid-template-columns: 1fr;
  }

  .feature-card {
    padding: 20px;
  }
}

.footer{
  display: flex;
  bottom: 0;
}
</style>