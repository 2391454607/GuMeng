<template>
  <div class="matching-container">
    <div class="matching-header">
      <h1>非遗脉络・经纬溯源局</h1>
      <p>将非遗项目与对应的民族或地区正确配对</p>
    </div>
    <div class="matching-game-box">
      <div class="matching-column left">
        <h2>非遗项目</h2>
        <div
          v-for="item in leftItems"
          :key="item.id"
          class="matching-item"
          :class="{ selected: selectedLeft && selectedLeft.id === item.id, matched: item.matched }"
          @click="selectLeft(item)"
        >
          {{ item.name }}
        </div>
      </div>
      <div class="matching-column right">
        <h2>民族/地区</h2>
        <div
          v-for="item in rightItems"
          :key="item.id"
          class="matching-item"
          :class="{ selected: selectedRight && selectedRight.id === item.id, matched: item.matched }"
          @click="selectRight(item)"
        >
          {{ item.name }}
        </div>
      </div>
    </div>

    <div class="game-info">
      <p>得分：{{ score }}</p>
      <div v-if="gameEnd" class="game-end-message">
        <p>恭喜你完成配对！</p>
        <p>你的最终得分是：<span>{{ score }}</span></p>
        <a-button type="primary" @click="restartGame">重新挑战</a-button>
      </div>
    </div>
    <!-- 音效audio标签 -->
    <audio ref="successAudio" src="/music/连线成功.mp3"></audio>
    <audio ref="errorAudio" src="/music/操作失误音.mp3"></audio>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { Message } from '@arco-design/web-vue';

// 示例配对数据
const matchingPairsData = [
  { id: 1, left: '白族扎染技艺', right: '大理白族自治州', matchId: 1 },
  { id: 2, left: '傣族泼水节', right: '西双版纳傣族自治州', matchId: 2 },
  { id: 3, left: '哈尼梯田文化系统', right: '红河哈尼族彝族自治州', matchId: 3 },
  { id: 4, left: '纳西古乐', right: '丽江市', matchId: 4 },
  { id: 5, left: '云南普洱茶制作技艺', right: '普洱市', matchId: 5 },
  { id: 6, left: '彝族火把节', right: '楚雄彝族自治州', matchId: 6 },
  // 添加更多配对数据
];

const leftItems = ref([]);
const rightItems = ref([]);
const selectedLeft = ref(null);
const selectedRight = ref(null);
const score = ref(0);
const gameEnd = ref(false);
// 音效相关
const successAudio = ref(null);
const errorAudio = ref(null);
function playSuccess() { successAudio.value && successAudio.value.play && successAudio.value.play(); }
function playError() { errorAudio.value && errorAudio.value.play && errorAudio.value.play(); }

// 初始化游戏
const initGame = () => {
  const shuffledPairs = matchingPairsData.sort(() => Math.random() - 0.5);

  leftItems.value = shuffledPairs.map(pair => ({
    id: pair.id,
    name: pair.left,
    matchId: pair.matchId,
    matched: false,
    disabled: false
  }));

  // 随机打乱右侧顺序
  const rightShuffled = [...shuffledPairs].sort(() => Math.random() - 0.5);
  rightItems.value = rightShuffled.map(pair => ({
    id: pair.id,
    name: pair.right,
    matchId: pair.matchId,
    matched: false,
    disabled: false
  }));

  score.value = 0;
  gameEnd.value = false;
  selectedLeft.value = null;
  selectedRight.value = null;
};

// 选择左侧项目
const selectLeft = (item) => {
  if (item.matched || gameEnd.value) return; // 已配对或游戏结束，不可选
  if (selectedLeft.value && selectedLeft.value.id === item.id) {
      selectedLeft.value = null; // 取消选中
  } else {
      selectedLeft.value = item;
      if (selectedRight.value) {
          checkMatch(); // 如果右侧已选中，则尝试配对
      }
  }
};

// 选择右侧项目
const selectRight = (item) => {
    if (item.matched || gameEnd.value) return; // 已配对或游戏结束，不可选
    if (selectedRight.value && selectedRight.value.id === item.id) {
        selectedRight.value = null; // 取消选中
    } else {
        selectedRight.value = item;
        if (selectedLeft.value) {
            checkMatch(); // 如果左侧已选中，则尝试配对
        }
    }
};

// 检查配对
const checkMatch = () => {
  if (selectedLeft.value && selectedRight.value) {
    if (selectedLeft.value.matchId === selectedRight.value.matchId) {
      // 配对成功
      playSuccess();
      Message.success('配对成功！');
      score.value++;

      // 更新 matched 状态
      const leftIndex = leftItems.value.findIndex(item => item.id === selectedLeft.value.id);
      leftItems.value[leftIndex].matched = true;

      const rightIndex = rightItems.value.findIndex(item => item.id === selectedRight.value.id);
      rightItems.value[rightIndex].matched = true;

      // 清空选中
      selectedLeft.value = null;
      selectedRight.value = null;

      // 检查游戏是否结束
      checkGameEnd();

    } else {
      // 配对失败
      playError();
      Message.error('配对错误！');
      // 错误后延迟清空选中，给用户看错的机会
      setTimeout(() => {
        selectedLeft.value = null;
        selectedRight.value = null;
      }, 800);
    }
  }
};

// 检查游戏是否结束
const checkGameEnd = () => {
  if (leftItems.value.every(item => item.matched)) {
    gameEnd.value = true;
    Message.info('恭喜你完成所有配对！');
  }
};

// 重新开始游戏
const restartGame = () => {
  initGame();
};

onMounted(() => {
  initGame();
});
</script>

<style scoped>
.matching-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  background: #f7f3ea;
  min-height: 100vh;
}

.matching-header {
  text-align: center;
  margin-bottom: 20px;
}

.matching-header h1 {
  font-size: 36px;
  color: #C2101C;
  margin-bottom: 10px;
  font-family: 'FZKai-Z03', 'KaiTi', 'STKaiti', serif;
}

.matching-header p {
  color: #666;
  font-size: 18px;
}

.matching-game-box {
  display: flex;
  justify-content: center;
  gap: 40px; /* 列之间的间距 */
  width: 100%;
  max-width: 800px; /* 最大宽度 */
}

.matching-column {
  flex: 1; /* 每列占据可用空间的1/2 */
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.matching-column h2 {
  font-size: 24px;
  color: #8b1f1f;
  margin-bottom: 15px;
  text-align: center;
}

.matching-item {
  background: #fdf6e3;
  border: 1.5px solid #e6c9a8;
  border-radius: 6px;
  padding: 10px 15px;
  margin-bottom: 10px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.2s ease-in-out;
  user-select: none; /* 防止文本被选中 */
}

.matching-item:hover {
  background: #ffeccc;
  border-color: #C2101C;
}

.matching-item.selected {
  background: #ffe6e6;
  border-color: #C2101C;
  box-shadow: 0 1px 4px rgba(194, 16, 28, 0.2);
}

.matching-item.matched {
  background: #e8f5e9; /* 配对成功的背景色 */
  border-color: #4caf50; /* 配对成功的边框色 */
  color: #388e3c; /* 配对成功的文字颜色 */
  cursor: default; /* 配对成功后改变鼠标样式 */
  opacity: 0.8; /* 稍微降低透明度 */
}

.game-info {
  margin-top: 30px;
  font-size: 18px;
  text-align: center;
}

.game-info span {
    color: #C2101C;
    font-weight: bold;
}

.game-end-message {
    margin-top: 20px;
    text-align: center;
}

.game-end-message p {
    margin-bottom: 10px;
}

@media (max-width: 600px) {
  .matching-game-box {
    flex-direction: column; /* 小屏幕下改为垂直布局 */
    gap: 20px;
  }
}
</style> 