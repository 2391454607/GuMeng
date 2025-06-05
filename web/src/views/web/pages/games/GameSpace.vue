<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { IconComputer, IconRobot, IconUser, IconStar, IconFire } from '@arco-design/web-vue/es/icon';
import { Modal } from '@arco-design/web-vue';

const router = useRouter();
const showDifficultyModal = ref(false);
const selectedDifficulty = ref(null);

const games = [
  {
    id: 1,
    title: '非遗拼图挑战',
    desc: '拖动拼图块，拼出云南非遗美景，锻炼观察力和动手能力。',
    img: '/background/gameLoge/pingtu.png',
    buttonText: '开始拼图',
    route: '/games/puzzle'
  },
  {
    id: 2,
    title: '非遗知识答题',
    desc: '答对更多云南非遗知识题目，赢取"非遗守护者"称号。',
    img: '/background/gameLoge/zhishijinsai.png',
    buttonText: '开始答题',
    route: '/games/quiz'
  },
  {
    id: 3,
    title: '非遗连线配对',
    desc: '将非遗项目与民族、地区正确连线，考验你的知识储备。',
    img: '/background/gameLoge/lianxian.jpg',
    buttonText: '开始连线',
    route: '/games/matching'
  },
  {
    id: 4,
    title: '找不同',
    desc: '在两张非遗场景图中找出不同之处，锻炼你的观察力。',
    img: '/background/gameLoge/zhaobutong.png',
    buttonText: '开始找不同',
    route: '/games/diff'
  },
  {
    id: 5,
    title: '非遗地图挑战',
    desc: '在云南地图上点选非遗项目分布地，了解地理与文化。',
    img: '/background/gameLoge/feiyiditu.png',
    buttonText: '开始挑战',
    route: '/games/map'
  }
];

function handleGameClick(game) {
  if (game.id === 1) {
    showDifficultyModal.value = true;
  } else {
    router.push(game.route);
  }
}

function selectDifficulty(level) {
  selectedDifficulty.value = level;
  showDifficultyModal.value = false;
  // 跳转到拼图页面并传递难度参数
  router.push({ path: '/games/puzzle', query: { difficulty: level } });
}
</script>

<template>
  <div class="game-space-container">
    <div class="game-space-header">
      <h1>非遗互动游戏空间</h1>
      <p>在这里体验多种趣味非遗互动游戏，沉浸式了解云南非遗文化！</p>
    </div>
    <div class="game-space-grid">
      <div v-for="game in games" :key="game.id" class="game-space-card" @click="handleGameClick(game)">
        <div class="img-wrapper">
          <img :src="game.img" :alt="game.title" />
        </div>
        <h2>{{ game.title }}</h2>
        <p>{{ game.desc }}</p>
        <a-button type="primary" class="start-button">{{ game.buttonText }}</a-button>
      </div>
    </div>
    <a-modal v-model:visible="showDifficultyModal" title="选择拼图难度" :footer="false" width="340px">
      <div class="difficulty-select-box">
        <a-button long size="large" @click="selectDifficulty('easy')">简单（3×3）</a-button>
        <a-button long size="large" @click="selectDifficulty('medium')" style="margin: 18px 0;">中等（4×4）</a-button>
        <a-button long size="large" @click="selectDifficulty('hard')">困难（5×5）</a-button>
      </div>
    </a-modal>
  </div>
</template>

<style scoped>
.game-space-container {
  min-height: 100vh;
  background: #f7f3ea;
  padding: 60px 0 40px 0;
}
.game-space-header {
  text-align: center;
  margin-bottom: 40px;
}
.game-space-header h1 {
  font-size: 40px;
  color: #C2101C;
  margin-bottom: 12px;
  font-family: 'FZKai-Z03', 'KaiTi', 'STKaiti', serif;
}
.game-space-header p {
  font-size: 20px;
  color: #666;
}
.game-space-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(340px, 1fr));
  gap: 40px;
  padding: 0 80px;
}
.game-space-card {
  background: #fff;
  border-radius: 16px;
  padding: 36px 24px;
  text-align: center;
  transition: all 0.3s;
  box-shadow: 0 6px 24px rgba(194, 16, 28, 0.10);
  border: 1.5px solid rgba(194, 16, 28, 0.12);
  cursor: pointer;
}
.game-space-card:hover {
  transform: translateY(-8px) scale(1.03);
  box-shadow: 0 12px 32px rgba(194, 16, 28, 0.18);
  border-color: #C2101C;
}
.img-wrapper {
  width: 90px;
  height: 90px;
  border-radius: 50%;
  overflow: hidden;
  margin: 0 auto 22px;
  box-shadow: 0 2px 12px rgba(194,16,28,0.10);
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
}
.img-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.game-space-card h2 {
  font-size: 26px;
  margin-bottom: 14px;
  color: #C2101C;
  font-weight: bold;
}
.game-space-card p {
  color: #666;
  margin-bottom: 28px;
  line-height: 1.7;
}
.start-button {
  background: #C2101C;
  border: none;
  padding: 10px 32px;
  border-radius: 22px;
  font-size: 18px;
  transition: all 0.3s;
}
.start-button:hover {
  background: #ff4d4d;
  transform: translateX(6px);
}
.difficulty-select-box {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 0;
  padding: 12px 0 6px 0;
}
@media (max-width: 900px) {
  .game-space-grid { grid-template-columns: 1fr; padding: 0 10px; }
  .game-space-header h1 { font-size: 28px; }
}
</style> 