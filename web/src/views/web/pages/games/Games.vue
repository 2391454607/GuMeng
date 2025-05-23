<script setup>
import { ref } from 'vue';
import { Message } from '@arco-design/web-vue';
import { IconRobot, IconComputer, IconUser, IconRight } from '@arco-design/web-vue/es/icon';
import Footer from "@/views/web/layout/Footer.vue";
import { useRouter } from 'vue-router';

const games = ref([
  {
    id: 1,
    title: '动态非遗拼图',
    description: '将非遗元素拆分为动态拼图，在限定时间内完成拼合，解锁非遗故事。',
    icon: IconComputer,
    type: 'puzzle'
  },
  {
    id: 2,
    title: '非遗文化答题赛',
    description: '涵盖非遗技艺、节日习俗、地方戏曲等知识的趣味答题游戏。',
    icon: IconRobot,
    type: 'quiz'
  },
  {
    id: 3,
    title: '非遗连线',
    description: '将相关的非遗元素进行连线配对，考验您的非遗知识储备。',
    icon: IconUser,
    type: 'matching'
  }
]);

const router = useRouter();

const handleGameClick = (game) => {
  Message.success(`即将开始${game.title}游戏`);
  // TODO: 根据游戏类型跳转到具体游戏页面
};

const handleClick = (type) => {
  Message.success(`即将体验${type}`);
  if (type === '非遗小游戏') {
    router.push({ name: 'Games' });
  }
};
</script>

<template>
  <div class="games-container">
    <div class="games-header">
      <h1>非遗小游戏</h1>
      <p>寓教于乐，在游戏中了解非遗文化</p>
    </div>
    
    <div class="games-grid">
      <div v-for="game in games" :key="game.id" class="game-card" @click="handleGameClick(game)">
        <div class="icon-wrapper">
          <component :is="game.icon" />
        </div>
        <h2>{{ game.title }}</h2>
        <p>{{ game.description }}</p>
        <a-button type="primary" class="start-button">
          开始游戏
          <template #icon><icon-right /></template>
        </a-button>
      </div>
    </div>
  </div>

  <Footer class="footer"/>
</template>

<style scoped>
.games-container {
  min-height: calc(100vh - 249px);
  padding: 40px 20px;
  background: #faf6f0;
}

.games-header {
  text-align: center;
  margin-bottom: 40px;
}

.games-header h1 {
  font-size: 36px;
  color: #C2101C;
  margin-bottom: 16px;
}

.games-header p {
  font-size: 18px;
  color: #666;
}

.games-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 30px;
  padding: 0 50px;
}

.game-card {
  background: #fff;
  border-radius: 12px;
  padding: 30px;
  text-align: center;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(194, 16, 28, 0.08);
  border: 1px solid rgba(194, 16, 28, 0.1);
  cursor: pointer;
}

.game-card:hover {
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
  background: linear-gradient(135deg, #C2101C, #ff4d4d);
}

.icon-wrapper :deep(svg) {
  font-size: 36px;
  color: white;
}

.game-card h2 {
  font-size: 24px;
  margin-bottom: 16px;
  color: #C2101C;
}

.game-card p {
  color: #666;
  margin-bottom: 24px;
  line-height: 1.6;
}

.start-button {
  background: #C2101C;
  border: none;
  padding: 8px 24px;
  border-radius: 20px;
  transition: all 0.3s ease;
}

.start-button:hover {
  background: #ff4d4d;
  transform: translateX(5px);
}

@media (max-width: 768px) {
  .games-grid {
    grid-template-columns: 1fr;
    padding: 0 20px;
  }
}

.footer {
  display: flex;
  bottom: 0;
}
</style> 