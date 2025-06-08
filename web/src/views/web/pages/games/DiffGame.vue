<template>
  <div class="diff-game-container">
    <div class="diff-header">
      <h1>非遗微察・景致辨真游</h1>
      <p>在两张图中找出6处不同点，点击图片标记！</p>
      <div class="diff-actions">
        <a-button v-for="(group, idx) in groups" :key="idx" :type="currentGroupIdx === idx ? 'primary' : 'outline'" @click="switchGroup(idx)">
          第{{ idx + 1 }}组
        </a-button>
      </div>
    </div>
    <div class="diff-images-box">
      <div class="diff-image-wrapper">
        <img :src="currentGroup.left" class="diff-image" @click="onImageClick($event, 'left')" />
        <div v-for="(point, idx) in foundPoints.left" :key="'l'+idx" class="diff-point" :style="pointStyle(point)" />
      </div>
      <div class="diff-image-wrapper">
        <img :src="currentGroup.right" class="diff-image" @click="onImageClick($event, 'right')" />
        <div v-for="(point, idx) in foundPoints.right" :key="'r'+idx" class="diff-point" :style="pointStyle(point)" />
      </div>
    </div>
    <div class="diff-info">
      <span>已找出：{{ foundCount }} / 6</span>
      <a-button type="primary" @click="restart">重新挑战</a-button>
    </div>
    <a-modal v-model:visible="showSuccess" title="恭喜！" :footer="false">
      <div style="text-align:center;padding:20px;">
        <p style="font-size:20px;color:#C2101C;">全部找对啦！</p>
        <a-button type="primary" @click="restart">再玩一次</a-button>
      </div>
    </a-modal>
    <audio ref="successAudio" src="/music/找不同成功音.mp3"></audio>
    <audio ref="errorAudio" src="/music/操作失误音.mp3"></audio>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue';
import { Message } from '@arco-design/web-vue';

// 两组图片及不同点坐标（相对比例，便于自适应）
const groups = [
  {
    left: '/background/gameImage/找不同左1.jpg',
    right: '/background/gameImage/找不同右1.png',
    // 6个不同点，格式：{x:0-1, y:0-1}
    points: [
      { x: 0.18, y: 0.18 },
      { x: 0.32, y: 0.80 },
      { x: 0.52, y: 0.40 },
      { x: 0.70, y: 0.22 },
      { x: 0.80, y: 0.65 },
      { x: 0.60, y: 0.85 },
    ]
  },
  {
    left: '/background/gameImage/找不同左2.jpg',
    right: '/background/gameImage/找不同右2.jpg',
    points: [
      { x: 0.22, y: 0.22 },
      { x: 0.38, y: 0.78 },
      { x: 0.50, y: 0.38 },
      { x: 0.68, y: 0.18 },
      { x: 0.80, y: 0.60 },
      { x: 0.62, y: 0.82 },
    ]
  }
];
const currentGroupIdx = ref(0);
const currentGroup = computed(() => groups[currentGroupIdx.value]);
const foundPoints = reactive({ left: [], right: [] });
const foundCount = ref(0);
const showSuccess = ref(false);
const successAudio = ref(null);
const errorAudio = ref(null);

function switchGroup(idx) {
  currentGroupIdx.value = idx;
  restart();
}
function restart() {
  foundPoints.left = [];
  foundPoints.right = [];
  foundCount.value = 0;
  showSuccess.value = false;
}
function playSuccess() { successAudio.value && successAudio.value.play && successAudio.value.play(); }
function playError() { errorAudio.value && errorAudio.value.play && errorAudio.value.play(); }

function onImageClick(e, side) {
  const rect = e.target.getBoundingClientRect();
  const x = (e.clientX - rect.left) / rect.width;
  const y = (e.clientY - rect.top) / rect.height;
  // 判断是否点中未找出的不同点
  const group = currentGroup.value;
  let hit = false;
  group.points.forEach((p, idx) => {
    if (
      Math.abs(p.x - x) < 0.05 &&
      Math.abs(p.y - y) < 0.05 &&
      !foundPoints.left.some(fp => Math.abs(fp.x - p.x) < 0.05 && Math.abs(fp.y - p.y) < 0.05)
    ) {
      foundPoints.left.push(p);
      foundPoints.right.push(p);
      foundCount.value++;
      hit = true;
    }
  });
  if (hit) {
    playSuccess();
    Message.success('找对了！');
    if (foundCount.value === 6) {
      showSuccess.value = true;
    }
  } else {
    playError();
    Message.error('这里不是不同点哦~');
  }
}
function pointStyle(point) {
  return {
    position: 'absolute',
    left: (point.x * 100) + '%',
    top: (point.y * 100) + '%',
    width: '24px',
    height: '24px',
    background: 'rgba(194,16,28,0.7)',
    borderRadius: '50%',
    border: '2px solid #fff',
    transform: 'translate(-50%, -50%)',
    pointerEvents: 'none',
    zIndex: 2
  };
}
</script>

<style scoped>
.diff-game-container {
  min-height: 100vh;
  background: #f7f3ea;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 40px;
}
.diff-header {
  text-align: center;
  margin-bottom: 24px;
}
.diff-header h1 {
  font-size: 36px;
  color: #C2101C;
  margin-bottom: 10px;
  font-family: 'FZKai-Z03', 'KaiTi', 'STKaiti', serif;
}
.diff-header p {
  color: #666;
  font-size: 18px;
  margin-bottom: 18px;
}
.diff-actions {
  display: flex;
  gap: 18px;
  justify-content: center;
  margin-bottom: 10px;
}
.diff-images-box {
  display: flex;
  gap: 32px;
  justify-content: center;
  margin-bottom: 24px;
}
.diff-image-wrapper {
  position: relative;
  width: 360px;
  height: 360px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 18px rgba(194,16,28,0.08);
  overflow: hidden;
}
.diff-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 12px;
  user-select: none;
}
.diff-point {
  pointer-events: none;
}
.diff-info {
  margin-top: 18px;
  font-size: 20px;
  color: #C2101C;
  text-align: center;
}
@media (max-width: 900px) {
  .diff-images-box { flex-direction: column; gap: 18px; }
  .diff-image-wrapper { width: 90vw; height: 40vw; min-width: 220px; min-height: 120px; }
}
</style> 