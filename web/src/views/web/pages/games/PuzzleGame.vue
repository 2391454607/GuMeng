<template>
  <div class="puzzle-game-container">
    <h1>榫卯画忆・非遗拼图志</h1>
    <p>当前难度：{{ difficulty }}</p>
    <p>用时：{{ formatTime(elapsedTime) }} | 步数：{{ steps }}</p>
    <p>当前图片：{{ imageSrc }} <button @click="loadImageAndSplit">手动加载图片</button></p>

    <div ref="puzzleArea" class="puzzle-area" :style="puzzleAreaStyle" :key="imageSrc">
      <!-- <canvas ref="puzzleCanvas"></canvas> -->
      <!-- 拼图碎片将在这里渲染 -->
      <img
        v-for="piece in pieces"
        :key="piece.id"
        :src="piece.imageUrl"
        :style="piece.style"
        class="puzzle-piece"
        @mousedown="startDrag(piece, $event)"
        @touchstart="startDrag(piece, $event)"
        :data-id="piece.id"
      />
      <!-- 新增：完整原图覆盖 -->
      <img
        v-if="showFullImageOverlay"
        :src="imageSrc"
        alt="完整拼图图片"
        class="full-image-overlay"
        :style="puzzleAreaStyle"
      />
    </div>
    <a-modal v-model:visible="showCompletionModal" title="恭喜你！" :footer="false" width="340px">
      <div style="text-align: center; padding: 20px;">
        <p style="font-size: 20px; font-weight: bold; color: green;">拼图完成！</p>
        <p style="font-size: 16px; margin-top: 10px;">您的用时：{{ formatTime(elapsedTime) }}</p>
        <p style="font-size: 16px;">总步数：{{ steps }}</p>
        <!-- 后续可以添加用时、步数等信息 -->
        <a-button type="primary" @click="restartGame" style="margin-top: 20px;">再玩一次</a-button>
      </div>
    </a-modal>
    <!-- 音效audio标签 -->
    <audio ref="moveAudio" src="/music/拼图移动声.mp3"></audio>
    <audio ref="finishAudio" src="/music/连线成功.mp3"></audio>
    <audio ref="errorAudio" src="/music/操作失误音.mp3"></audio>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick, watch } from 'vue';
import { useRoute } from 'vue-router';
import { Modal } from '@arco-design/web-vue';

const route = useRoute();
const difficulty = ref(route.query.difficulty || 'easy'); // 从路由获取难度参数，默认为 easy
const puzzleCanvas = ref(null); // 引用 canvas 元素
const puzzleArea = ref(null); // 引用拼图区域 div
const imageSrc = ref(route.query.image || '/background/gameImage/g1.jpg'); // 默认图片源或来自路由参数
const pieces = ref([]); // 存储拼图碎片数据
const grid = ref({ rows: 3, cols: 3 }); // 默认 3x3 网格

// 拖动相关状态
const isDragging = ref(false);
const currentPiece = ref(null);
const startX = ref(0);
const startY = ref(0);
const startLeft = ref(0);
const startTop = ref(0);

// 计时器相关状态
const timer = ref(null); // 存储计时器 ID
const startTime = ref(0); // 存储开始时间戳
const elapsedTime = ref(0); // 存储经过的时间（秒）

// 步数统计
const steps = ref(0); // 新增：存储步数

const puzzleAreaStyle = ref({}); // 拼图区域的样式，用于设置宽度和高度
const snapThreshold = 20; // 吸附阈值（像素）
const showCompletionModal = ref(false);
const showFullImageOverlay = ref(false); // 新增：控制完整原图覆盖显示

// 音效相关
const moveAudio = ref(null);
const finishAudio = ref(null);
const errorAudio = ref(null);

function playMove() { moveAudio.value && moveAudio.value.play && moveAudio.value.play(); }
function playFinish() { finishAudio.value && finishAudio.value.play && finishAudio.value.play(); }
function playError() { errorAudio.value && errorAudio.value.play && errorAudio.value.play(); }

// 根据难度设置网格
function setDifficultyGrid(level) {
  switch (level) {
    case 'easy':
      grid.value = { rows: 3, cols: 3 };
      break;
    case 'medium':
      grid.value = { rows: 4, cols: 4 };
      break;
    case 'hard':
      grid.value = { rows: 5, cols: 5 };
      break;
    default:
      grid.value = { rows: 3, cols: 3 };
  }
}

// 加载图片并分割
function loadImageAndSplit() {
  console.log('[loadImageAndSplit] 开始加载图片:', imageSrc.value); // 添加日志
  // 在加载新图片前清空碎片，确保旧碎片不显示
  pieces.value = [];
  showFullImageOverlay.value = false; // 隐藏完整图覆盖

  const img = new Image();
  img.onload = () => {
    console.log('[loadImageAndSplit] 图片加载成功, 尺寸:', img.width, img.height); // 添加日志
    const canvas = document.createElement('canvas');
    const ctx = canvas.getContext('2d');

    // 确保 puzzleArea 已经被渲染
    if (!puzzleArea.value) {
      console.error('[loadImageAndSplit] puzzleArea DOM element not available!');
      return;
    }
    console.log('[loadImageAndSplit] puzzleArea 宽度:', puzzleArea.value.clientWidth); // 添加日志

    const containerWidth = puzzleArea.value.clientWidth;
    const scale = containerWidth / img.width;
    canvas.width = containerWidth;
    canvas.height = img.height * scale;

    puzzleAreaStyle.value = {
      width: `${canvas.width}px`,
      height: `${canvas.height}px`,
    };

    ctx.drawImage(img, 0, 0, canvas.width, canvas.height);

    const pieceWidth = canvas.width / grid.value.cols;
    const pieceHeight = canvas.height / grid.value.rows;
    console.log('[loadImageAndSplit] 计算碎片尺寸:', pieceWidth, pieceHeight); // 添加日志

    createPuzzlePieces(canvas, pieceWidth, pieceHeight);
    shufflePieces(); // 打乱碎片位置

    // 图片加载并分割完成后启动计时器并初始化步数
    startTimer();
    steps.value = 0; // 初始化步数
    console.log('[loadImageAndSplit] 图片处理完成，碎片数量:', pieces.value.length); // 添加日志
    nextTick(() => {
      console.log('[loadImageAndSplit] nextTick 回调，检查 DOM 中的碎片数量:', puzzleArea.value.querySelectorAll('.puzzle-piece').length); // 添加日志
    });

  };
  img.onerror = (error) => { // 添加图片加载错误处理
    console.error('[loadImageAndSplit] 图片加载失败:', imageSrc.value, error); // 添加日志
    // 可以添加用户提示，例如显示一个错误信息
  };
  // 防止浏览器缓存
  img.src = imageSrc.value + `?t=${Date.now()}`; // 添加时间戳
}

// 创建拼图碎片数据
function createPuzzlePieces(canvas, pieceWidth, pieceHeight) {
  console.log('[createPuzzlePieces] 开始创建碎片'); // 添加日志
  const ctx = canvas.getContext('2d');
  pieces.value = [];
  let idCounter = 0;
  for (let r = 0; r < grid.value.rows; r++) {
    for (let c = 0; c < grid.value.cols; c++) {
      const x = c * pieceWidth;
      const y = r * pieceHeight;

      const pieceCanvas = document.createElement('canvas');
      pieceCanvas.width = pieceWidth;
      pieceCanvas.height = pieceHeight;
      const pieceCtx = pieceCanvas.getContext('2d');
      pieceCtx.drawImage(
        canvas,
        x, y, pieceWidth, pieceHeight,
        0, 0, pieceWidth, pieceHeight
      );
      const imageUrl = pieceCanvas.toDataURL();

      pieces.value.push({
        id: idCounter++,
        correctRow: r,
        correctCol: c,
        currentRow: r, // 初始设置为正确位置，之后会在 shufflePieces 中更新
        currentCol: c,
        imageUrl: imageUrl,
        pieceWidth: pieceWidth, // 存储碎片宽度
        pieceHeight: pieceHeight, // 存储碎片高度
        style: {
          width: `${pieceWidth}px`,
          height: `${pieceHeight}px`,
          position: 'absolute',
        },
        isSolved: false,
      });
    }
  }
  console.log('[createPuzzlePieces] 碎片创建完成，数量:', pieces.value.length); // 添加日志
}

// 打乱碎片位置并更新 currentRow/currentCol
function shufflePieces() {
  console.log('[shufflePieces] 开始打乱碎片'); // 添加日志
  // 创建所有正确位置的数组
  const positions = [];
  for (let r = 0; r < grid.value.rows; r++) {
    for (let c = 0; c < grid.value.cols; c++) {
      positions.push({ row: r, col: c });
    }
  }

  // 打乱位置数组 (Fisher-Yates Shuffle)
  for (let i = positions.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [positions[i], positions[j]] = [positions[j], positions[i]];
  }

  // 将打乱后的位置分配给每个碎片
  pieces.value.forEach((piece, index) => {
    piece.currentRow = positions[index].row;
    piece.currentCol = positions[index].col;
  });

  console.log('[shufflePieces] 碎片打乱完成'); // 添加日志
  updatePiecePositions(); // 根据新的 currentRow/currentCol 更新样式
}

// 更新碎片在页面上的位置样式 (根据 currentRow, currentCol)
function updatePiecePositions() {
  if (!puzzleArea.value) {
    return;
  }
  console.log('[updatePiecePositions] 更新碎片位置'); // 添加日志
  const puzzleRect = puzzleArea.value.getBoundingClientRect();
  // 使用碎片自己的尺寸来计算正确位置
  const pieceWidth = puzzleRect.width / grid.value.cols;
  const pieceHeight = puzzleRect.height / grid.value.rows;

  pieces.value.forEach(piece => {
    const newTop = piece.currentRow * pieceHeight;
    const newLeft = piece.currentCol * pieceWidth;
    piece.style.top = `${newTop}px`;
    piece.style.left = `${newLeft}px`;
    piece.style.transition = 'top 0.3s ease-out, left 0.3s ease-out';
  });
  setTimeout(() => {
    pieces.value.forEach(piece => {
      piece.style.transition = '';
    });
  }, 300);
  console.log('[updatePiecePositions] 碎片位置更新完成'); // 添加日志
}

// 开始拖动
function startDrag(piece, event) {
  // 添加检查：如果碎片不存在或已解决，则不可拖动
  if (!piece || piece.isSolved) {
    console.log('[startDrag] 阻止拖动: 碎片不存在或已解决'); // 添加日志
    return;
  }

  isDragging.value = true;
  currentPiece.value = piece;

  const clientX = event.type === 'mousedown' ? event.clientX : event.touches[0].clientX;
  const clientY = event.type === 'mousedown' ? event.clientY : event.touches[0].clientY;

  startX.value = clientX;
  startY.value = clientY;

  // 获取当前碎片的精确位置 (考虑 transform 等可能影响)
  const pieceRect = event.target.getBoundingClientRect();
  const puzzleRect = puzzleArea.value.getBoundingClientRect();

  // 计算相对于 puzzleArea 的当前位置
  startLeft.value = pieceRect.left - puzzleRect.left;
  startTop.value = pieceRect.top - puzzleRect.top;

  piece.style.zIndex = 100;

  console.log(`[startDrag] 开始拖动碎片 ${piece.id} 从 (${startLeft.value}, ${startTop.value})`); // 添加日志
  playMove();
}

// 拖动中
function onDrag(event) {
  if (!isDragging.value || !currentPiece.value) return;

  const clientX = event.type === 'mousemove' ? event.clientX : event.touches[0].clientX;
  const clientY = event.type === 'mousemove' ? event.clientY : event.touches[0].clientY;

  const deltaX = clientX - startX.value;
  const deltaY = clientY - startY.value;

  // 更新碎片位置
  currentPiece.value.style.left = `${startLeft.value + deltaX}px`;
  currentPiece.value.style.top = `${startTop.value + deltaY}px`;

  event.preventDefault();
}

// 结束拖动
function endDrag() {
  if (!isDragging.value || !currentPiece.value) return;

  isDragging.value = false;
  // 恢复 z-index
  currentPiece.value.style.zIndex = '';

  // 获取拖动结束时碎片的最终位置
  const pieceElement = event.target; // 获取触发事件的元素
  const pieceRect = pieceElement.getBoundingClientRect();
  const puzzleRect = puzzleArea.value.getBoundingClientRect();

  // 计算相对于 puzzleArea 的最终 dropped 位置
  const droppedLeft = pieceRect.left - puzzleRect.left;
  const droppedTop = pieceRect.top - puzzleRect.top;

  // 检查是否接近正确位置并吸附
  checkAndSnap(currentPiece.value, droppedLeft, droppedTop);

  // 增加步数 (无论是否吸附成功，只要有拖动释放就算一步)
  steps.value++;

  currentPiece.value = null;

  // 检查是否完成拼图
  checkCompletion();

  console.log('[endDrag] 结束拖动'); // 添加日志
}

// 检查并吸附碎片
function checkAndSnap(piece, droppedLeft, droppedTop) {
  console.log('[checkAndSnap] 检查并吸附碎片:', piece.id); // 添加日志
  if (!puzzleArea.value) return;
  const puzzleRect = puzzleArea.value.getBoundingClientRect();
  // 使用碎片自己的尺寸来计算正确位置
  const pieceWidth = piece.pieceWidth;
  const pieceHeight = piece.pieceHeight;

  // 计算正确位置的左上角坐标
  const correctLeft = piece.correctCol * pieceWidth;
  const correctTop = piece.correctRow * pieceHeight;

  // 计算 dropped 位置与正确位置左上角的距离
  const distance = Math.sqrt(
    Math.pow(droppedLeft - correctLeft, 2) +
    Math.pow(droppedTop - correctTop, 2)
  );

  console.log(`[checkAndSnap] 碎片 ${piece.id} dropped 位置: (${droppedLeft}, ${droppedTop}), 正确位置: (${correctLeft}, ${correctTop}), 距离: ${distance}`); // 添加日志

  // 如果距离小于吸附阈值，则吸附到正确位置
  if (distance < snapThreshold) {
    piece.style.left = `${correctLeft}px`;
    piece.style.top = `${correctTop}px`;
    piece.isSolved = true; // 标记为已解决
    console.log(`[checkAndSnap] 碎片 ${piece.id} 吸附到正确位置`); // 添加日志
    // 更新碎片的当前位置信息以反映吸附后的状态 (尽管已经 solved)
    piece.currentRow = piece.correctRow;
    piece.currentCol = piece.correctCol;
    // 添加吸附成功后的视觉反馈，例如添加一个 class
    // const pieceElement = document.querySelector(`.puzzle-piece[data-id='${piece.id}']`);
    // if (pieceElement) { pieceElement.classList.add('solved'); }
  } else {
    // 如果没有吸附，将碎片留在当前位置
    piece.style.left = `${droppedLeft}px`;
    piece.style.top = `${droppedTop}px`;
    console.log(`[checkAndSnap] 碎片 ${piece.id} 未吸附，停留在当前位置`); // 添加日志
  }
}

// 检查拼图是否完成
function checkCompletion() {
  console.log('[checkCompletion] 检查拼图完成状态'); // 添加日志
  const allSolved = pieces.value.every(piece => piece.isSolved);
  if (allSolved) {
    console.log('[checkCompletion] 拼图完成！'); // 添加日志
    stopTimer(); // 停止计时器
    showCompletionModal.value = true; // 显示完成模态框
    showFullImageOverlay.value = true; // 显示完整原图覆盖
    playFinish();
  } else {
    console.log('[checkCompletion] 拼图未完成'); // 添加日志
  }
}

// 启动计时器
function startTimer() {
  startTime.value = Date.now();
  timer.value = setInterval(() => {
    elapsedTime.value = Math.floor((Date.now() - startTime.value) / 1000);
  }, 1000);
}

// 停止计时器
function stopTimer() {
  if (timer.value) {
    clearInterval(timer.value);
    timer.value = null;
  }
}

// 格式化时间显示 (可选，用于更友好的时间展示)
function formatTime(seconds) {
  const minutes = Math.floor(seconds / 60);
  const remainingSeconds = seconds % 60;
  const paddedSeconds = remainingSeconds < 10 ? '0' + remainingSeconds : remainingSeconds;
  return `${minutes}:${paddedSeconds}`;
}

// 新增：重新开始游戏
function restartGame() {
  console.log('重新开始游戏');
  stopTimer(); // 停止当前计时器
  elapsedTime.value = 0; // 重置时间
  steps.value = 0; // 重置步数
  pieces.value = []; // 清空碎片数组
  showCompletionModal.value = false; // 隐藏完成模态框
  showFullImageOverlay.value = false; // 隐藏完整图覆盖

  // 等待 DOM 更新（碎片清空）后再重新加载图片和分割碎片
  nextTick(() => {
    loadImageAndSplit();
  });
}

onMounted(() => {
  setDifficultyGrid(difficulty.value);
  loadImageAndSplit();

  document.addEventListener('mousemove', onDrag);
  document.addEventListener('mouseup', endDrag);
  document.addEventListener('touchmove', onDrag, { passive: false }); // touchmove 可能需要 preventDefault
  document.addEventListener('touchend', endDrag);
});

onBeforeUnmount(() => {
  document.removeEventListener('mousemove', onDrag);
  document.removeEventListener('mouseup', endDrag);
  document.removeEventListener('touchmove', onDrag);
  document.removeEventListener('touchend', endDrag);
  stopTimer(); // 组件卸载时停止计时器
});

// 监听路由参数中的 image 变化
watch(() => route.query.image, async (newImage, oldImage) => {
  if (newImage && newImage !== oldImage) {
    console.log(`[watch:image] 图片路径变化：${oldImage} -> ${newImage}`); // 添加日志
    imageSrc.value = newImage; // 更新 imageSrc ref
    // 清空碎片数组并等待 DOM 更新
    pieces.value = [];
    showFullImageOverlay.value = false; // 隐藏完整图覆盖，避免闪烁
    await nextTick(); // 等待 DOM 更新
    
    // 重新加载图片和分割碎片
    loadImageAndSplit();
  }
});

// 监听难度变化，如果难度变化，重新加载并打乱碎片
watch(() => difficulty.value, (newDifficulty, oldDifficulty) => {
  if (newDifficulty !== oldDifficulty) {
    console.log(`[watch:difficulty] 难度变化：${oldDifficulty} -> ${newDifficulty}`); // 添加日志
    setDifficultyGrid(newDifficulty);
    loadImageAndSplit(); // 根据新难度重新加载和分割
  }
});

</script>

<style scoped>
.puzzle-game-container {
  padding: 20px;
  text-align: center;
}

.puzzle-area {
  position: relative; /* 使碎片可以绝对定位 */
  display: inline-block; /* 让区域包裹 canvas */
  margin-top: 20px;
  border: 1px solid #ccc; /* 添加边框以示区域 */
  /* 后续可以根据 canvas 尺寸设置 border 或 background */
}

/* canvas { */
/*   display: block; */
/*   opacity: 0; */
/*   position: absolute; */
/* } */

.puzzle-piece {
  position: absolute; /* 绝对定位 */
  cursor: grab; /* 鼠标样式为可抓取 */
  box-shadow: 2px 2px 5px rgba(0, 0, 0, 0.3); /* 添加阴影 */
  transition: box-shadow 0.1s ease; /* 阴影过渡效果 */
}

.puzzle-piece:active {
    cursor: grabbing; /* 拖动时鼠标样式为抓取中 */
    box-shadow: 4px 4px 10px rgba(0, 0, 0, 0.5); /* 拖动时阴影加深 */
}

/* 碎片在正确位置的样式 */
.puzzle-piece.solved {
  /* 可以添加一些视觉反馈，例如边框变色 */
  border: 2px solid lightgreen; /* 示例 */
}

/* 新增：完整原图覆盖样式 */
.full-image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 50; /* 确保在碎片下方，在 puzzle-area 内部 */
  /* 可以添加过渡效果 */
  transition: opacity 1s ease-in-out;
}
</style> 