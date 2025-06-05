<script setup>
import { ref, computed } from 'vue';
import { Message } from '@arco-design/web-vue';

const ROWS = 3;
const COLS = 3;
const totalPieces = ROWS * COLS;
const imgUrl = '/background/BB.jpg';

// 生成初始顺序
const getInitOrder = () => Array.from({ length: totalPieces }, (_, i) => i);

const order = ref(getInitOrder());
const draggingIndex = ref(null);
const finished = ref(false);

function shuffle() {
  // 洗牌算法
  const arr = getInitOrder();
  for (let i = arr.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [arr[i], arr[j]] = [arr[j], arr[i]];
  }
  order.value = arr;
  finished.value = false;
}

function onDragStart(idx) {
  draggingIndex.value = idx;
}
function onDrop(idx) {
  if (draggingIndex.value === null) return;
  // 交换拼图块
  const newOrder = [...order.value];
  [newOrder[draggingIndex.value], newOrder[idx]] = [newOrder[idx], newOrder[draggingIndex.value]];
  order.value = newOrder;
  draggingIndex.value = null;
  checkFinish();
}
function onDragOver(e) {
  e.preventDefault();
}
function checkFinish() {
  if (order.value.every((v, i) => v === i)) {
    finished.value = true;
    Message.success('拼图完成！恭喜你拼出了云南非遗美景！');
  }
}
function reset() {
  order.value = getInitOrder();
  finished.value = false;
}

const pieceSize = computed(() => 320); // 单块拼图大小
const boardSize = computed(() => pieceSize.value * COLS);

</script>

<template>
  <div class="puzzle-container">
    <div class="puzzle-header">
      <h1>非遗拼图挑战</h1>
      <p>拖动拼图块，拼出完整的云南非遗美景。点击"打乱"开始挑战！</p>
      <div class="puzzle-actions">
        <a-button type="primary" @click="shuffle">打乱</a-button>
        <a-button type="outline" @click="reset">复原</a-button>
      </div>
    </div>
    <div class="puzzle-board" :style="{ width: boardSize + 'px', height: boardSize + 'px' }">
      <div
        v-for="(piece, idx) in order"
        :key="idx"
        class="puzzle-piece"
        :style="{
          width: pieceSize + 'px',
          height: pieceSize + 'px',
          backgroundImage: `url(${imgUrl})`,
          backgroundSize: `${pieceSize * COLS}px ${pieceSize * ROWS}px`,
          backgroundPosition: `-${(piece % COLS) * pieceSize}px -${Math.floor(piece / COLS) * pieceSize}px`,
          opacity: finished && piece === idx ? 1 : 0.98
        }"
        draggable="true"
        @dragstart="onDragStart(idx)"
        @drop="onDrop(idx)"
        @dragover="onDragOver"
      >
      </div>
    </div>
    <div v-if="finished" class="puzzle-finish">
      <div class="finish-text">🎉 恭喜你完成拼图！</div>
      <a-button type="primary" @click="shuffle">再来一局</a-button>
    </div>
  </div>
</template>

<style scoped>
.puzzle-container {
  min-height: 100vh;
  background: #f7f3ea;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 40px;
}
.puzzle-header {
  text-align: center;
  margin-bottom: 32px;
}
.puzzle-header h1 {
  font-size: 36px;
  color: #C2101C;
  margin-bottom: 10px;
  font-family: 'FZKai-Z03', 'KaiTi', 'STKaiti', serif;
}
.puzzle-header p {
  color: #666;
  font-size: 18px;
  margin-bottom: 18px;
}
.puzzle-actions {
  display: flex;
  gap: 18px;
  justify-content: center;
  margin-bottom: 10px;
}
.puzzle-board {
  display: flex;
  flex-wrap: wrap;
  border: 3px solid #C2101C;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 4px 18px rgba(194,16,28,0.08);
  margin-bottom: 24px;
  position: relative;
}
.puzzle-piece {
  box-sizing: border-box;
  border: 1.5px solid #f5e6d6;
  cursor: grab;
  transition: box-shadow 0.2s;
  background-repeat: no-repeat;
  background-color: #fdf6e3;
}
.puzzle-piece:active {
  box-shadow: 0 0 8px #C2101C;
}
.puzzle-finish {
  text-align: center;
  margin-top: 18px;
}
.finish-text {
  font-size: 22px;
  color: #C2101C;
  margin-bottom: 16px;
  font-family: 'FZKai-Z03', 'KaiTi', 'STKaiti', serif;
}
@media (max-width: 1100px) {
  .puzzle-board {
    transform: scale(0.7);
  }
}
@media (max-width: 700px) {
  .puzzle-board {
    transform: scale(0.45);
  }
}
</style> 