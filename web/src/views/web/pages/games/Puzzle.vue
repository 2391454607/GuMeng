<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue';
import { Message } from '@arco-design/web-vue';
import { useRoute } from 'vue-router';

const route = useRoute();
const ROWS = ref(3);
const COLS = ref(3);
const totalPieces = computed(() => ROWS.value * COLS.value);
const imgUrl = ref('/background/BB.jpg');

// 初始计算棋盘大小，确保在组件挂载前就有值
const initialMaxBoardSize = 960;
const initialPadding = 80;
const initialAvailableWidth = window.innerWidth - initialPadding * 2;
const initialBoardSizeValue = Math.min(initialMaxBoardSize, Math.max(0, initialAvailableWidth));
const boardSize = ref(initialBoardSizeValue); // 响应式棋盘大小，初始值有效

const pieceSize = computed(() => {
  const currentBoardSize = boardSize.value;
  const currentCOLS = COLS.value;

  // 确保 boardSize.value 和 COLS.value 都是有效数字且 COLS.value 不为 0
  if (typeof currentBoardSize !== 'number' || isNaN(currentBoardSize) || typeof currentCOLS !== 'number' || isNaN(currentCOLS) || currentCOLS <= 0) {
    console.warn('Puzzle.vue: Invalid values for pieceSize calculation (fallback)', { boardSize: currentBoardSize, COLS: currentCOLS });
    return 0; // 返回 0 避免 NaN
  }
  const calculatedPieceSize = currentBoardSize / currentCOLS;
  console.log(`Puzzle.vue: Calculated pieceSize: boardSize=${currentBoardSize}, COLS=${currentCOLS}, result=${calculatedPieceSize}`);
  return Math.round(calculatedPieceSize);
});

const updateBoardSize = () => {
  const maxBoardSize = 960; // 最大棋盘尺寸
  const padding = 80; // 页面左右padding
  let currentInnerWidth = window.innerWidth; // 获取当前窗口宽度

  if (typeof currentInnerWidth !== 'number' || isNaN(currentInnerWidth) || currentInnerWidth <= 0) {
    console.warn('Puzzle.vue: Invalid window.innerWidth, falling back to maxBoardSize');
    currentInnerWidth = maxBoardSize + padding * 2; // 回退到一个安全值
  }

  const availableWidth = currentInnerWidth - padding * 2; // 可用宽度
  boardSize.value = Math.min(maxBoardSize, Math.max(0, availableWidth)); // 取较小值，并确保不为负数
  console.log(`Puzzle.vue: boardSize updated to ${boardSize.value}`);
};

// 根据难度设置拼图大小
const setDifficulty = (level) => {
  console.log(`Puzzle.vue: Setting difficulty to ${level}`);
  switch(level) {
    case 'easy':
      ROWS.value = 3;
      COLS.value = 3;
      break;
    case 'medium':
      ROWS.value = 4;
      COLS.value = 4;
      break;
    case 'hard':
      ROWS.value = 5;
      COLS.value = 5;
      break;
    default:
      ROWS.value = 3;
      COLS.value = 3;
  }
  console.log(`Puzzle.vue: ROWS=${ROWS.value}, COLS=${COLS.value} after setDifficulty`);
  order.value = getInitOrder();
  shuffle();
  finished.value = false;
  updateBoardSize(); // 难度改变时也更新棋盘大小
};

// 生成初始顺序
const getInitOrder = () => Array.from({ length: totalPieces.value }, (_, i) => i);

const order = ref(getInitOrder());
const draggingIndex = ref(null);
const finished = ref(false);

// 音效相关
const moveAudio = ref(null);
const finishAudio = ref(null);
const errorAudio = ref(null);

function playMove() { moveAudio.value && moveAudio.value.play && moveAudio.value.play(); }
function playFinish() { finishAudio.value && finishAudio.value.play && finishAudio.value.play(); }
function playError() { errorAudio.value && errorAudio.value.play && errorAudio.value.play(); }

function shuffle() {
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
  playMove();
}
function onDrop(idx) {
  if (draggingIndex.value === null) return;
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
    playFinish();
    Message.success('拼图完成！恭喜你拼出了云南非遗美景！');
  }
}
function reset() {
  order.value = getInitOrder();
  finished.value = false;
}

// Watchers for debugging
watch(boardSize, (newVal) => {
  console.log('Puzzle.vue: boardSize watcher triggered, newValue:', newVal);
});
watch(COLS, (newVal) => {
  console.log('Puzzle.vue: COLS watcher triggered, newValue:', newVal);
});

// 封装每个拼图块的样式计算
const getPieceStyle = computed(() => (piece, idx) => {
  const currentPieceSize = pieceSize.value;
  const currentBoardSize = boardSize.value;
  const currentCOLS = COLS.value;

  // 再次检查防止 NaN 传递到 CSS 属性
  if (typeof currentPieceSize !== 'number' || isNaN(currentPieceSize) || currentPieceSize <= 0) {
    console.error('Puzzle.vue: getPieceStyle: pieceSize is invalid', currentPieceSize); // 记录错误
    return {}; // 返回空对象，避免样式错误
  }
  if (typeof currentBoardSize !== 'number' || isNaN(currentBoardSize) || currentBoardSize <= 0) {
    console.error('Puzzle.vue: getPieceStyle: boardSize is invalid', currentBoardSize); // 记录错误
    return {};
  }
  if (typeof currentCOLS !== 'number' || isNaN(currentCOLS) || currentCOLS <= 0) {
    console.error('Puzzle.vue: getPieceStyle: COLS is invalid', currentCOLS); // 记录错误
    return {};
  }

  const posX = -(piece % currentCOLS) * currentPieceSize;
  const posY = -Math.floor(piece / currentCOLS) * currentPieceSize;

  console.log(`Puzzle.vue: Piece ${idx} style: posX=${posX}px, posY=${posY}px, pieceSize=${currentPieceSize}, boardSize=${currentBoardSize}, COLS=${currentCOLS}`);

  return {
    width: currentPieceSize + 'px',
    height: currentPieceSize + 'px',
    backgroundImage: `url(${imgUrl.value})`,
    backgroundSize: `${currentBoardSize}px ${currentBoardSize}px`,
    backgroundPosition: `${posX}px ${posY}px`,
    opacity: finished.value && piece === idx ? 1 : 0.98
  };
});

onMounted(() => {
  console.log('Puzzle.vue: onMounted triggered');
  // 监听窗口大小变化以更新棋盘尺寸
  window.addEventListener('resize', updateBoardSize);

  // 从路由参数获取难度和图片
  const difficulty = route.query.difficulty;
  const image = route.query.image;
  
  console.log('Puzzle.vue: Received difficulty', difficulty);
  console.log('Puzzle.vue: Received image', image);

  // 优先处理难度参数，它会影响 COLS.value
  if (difficulty) {
    setDifficulty(difficulty); 
  } else {
    // 如果没有难度参数，则使用默认值初始化
    ROWS.value = 3;
    COLS.value = 3;
    order.value = getInitOrder();
    shuffle();
    finished.value = false;
    updateBoardSize(); // 确保在默认情况下也更新 boardSize
  }

  // 处理图片参数
  if (image) {
    imgUrl.value = `${image}?t=${new Date().getTime()}`;
  }
  
  console.log('Puzzle.vue: Final state check - ROWS', ROWS.value, 'COLS', COLS.value, 'boardSize', boardSize.value, 'imgUrl', imgUrl.value);
});

// 页面卸载时移除监听器
onBeforeUnmount(() => {
  window.removeEventListener('resize', updateBoardSize);
});

// 用于强制重新渲染 puzzle-board 的 key
const puzzleBoardKey = computed(() => `${ROWS.value}-${COLS.value}-${imgUrl.value}-${boardSize.value}`);

</script>

<template>
  <div class="puzzle-container">
    <!-- 音效audio标签 -->
    <audio ref="moveAudio" src="/music/拼图移动声.mp3" preload="auto"></audio>
    <audio ref="finishAudio" src="/music/连线成功.mp3" preload="auto"></audio>
    <audio ref="errorAudio" src="/music/操作失误音.mp3" preload="auto"></audio>
    <div class="puzzle-header">
      <h1>榫卯画忆・非遗拼图志</h1>
      <p>拖动拼图块，拼出完整的云南非遗美景。点击"打乱"开始挑战！</p>
      <div class="puzzle-actions">
        <a-button type="primary" @click="shuffle">打乱</a-button>
        <a-button type="outline" @click="reset">复原</a-button>
      </div>
    </div>
    <div 
      class="puzzle-board"
      :style="{ width: boardSize + 'px', height: boardSize + 'px' }"
      :key="puzzleBoardKey"
    >
      <div
        v-for="(piece, idx) in order"
        :key="idx"
        class="puzzle-piece"
        :style="getPieceStyle(piece, idx)"
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
  border: 1.5px solid #f5e6d6; /* 确保边框清晰 */
  cursor: grab;
  transition: box-shadow 0.2s;
  background-repeat: no-repeat;
  background-color: #fdf6e3;
  border: 1px solid rgba(0, 0, 0, 0.1); /* 新增：为每个拼图块添加一个明显的边框 */
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
  /* 移除 transform: scale */
}
@media (max-width: 700px) {
  /* 移除 transform: scale */
}
</style> 