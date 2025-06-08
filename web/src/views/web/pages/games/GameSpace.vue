<script setup>
import { ref, onMounted, nextTick, onBeforeUnmount } from 'vue';
import { useRouter } from 'vue-router';
import { IconComputer, IconRobot, IconUser, IconStar, IconFire } from '@arco-design/web-vue/es/icon';
import { Modal } from '@arco-design/web-vue';

const router = useRouter();
const showDifficultyModal = ref(false);
const selectedDifficulty = ref(null);
const showComingSoonModal = ref(false);
const showQuizDifficultyModal = ref(false);
const quizDifficulty = ref('简单');

// 新增：图片选择相关
const showImageSelectModal = ref(false);
const selectedImage = ref(null);
const availableImages = [
  '/background/gameImage/g1.jpg',
  '/background/gameImage/g2.jpg',
  '/background/gameImage/g3.jpg',
  '/background/gameImage/g4.jpg',
  '/background/gameImage/g5.jpg',
];

// 音乐控制相关
const bgmAudio = ref(null);
const isPlaying = ref(false);
const isMuted = ref(false);
const volume = ref(0.7);
const showMusicDropdown = ref(false);

const games = [
  {
    id: 1,
    title: '榫卯画忆・非遗拼图志',
    desc: '拖动拼图块，拼出云南非遗美景，锻炼观察力和动手能力。',
    img: '/background/gameLoge/pingtu.png',
    buttonText: '开始拼图',
    route: '/games/puzzle'
  },
  {
    id: 2,
    title: '非遗问对・文心解码赛',
    desc: '答对更多云南非遗知识题目，赢取"非遗守护者"称号。',
    img: '/background/gameLoge/zhishijinsai.png',
    buttonText: '开始答题',
    route: '/games/quiz'
  },
  {
    id: 3,
    title: '非遗脉络・经纬溯源局',
    desc: '将非遗项目与民族、地区正确连线，考验你的知识储备。',
    img: '/background/gameLoge/lianxian.jpg',
    buttonText: '开始连线',
    route: '/games/matching'
  },
  {
    id: 4,
    title: '非遗微察・景致辨真游',
    desc: '在两张非遗场景图中找出不同之处，锻炼你的观察力。',
    img: '/background/gameLoge/zhaobutong.png',
    buttonText: '开始找不同',
    route: '/games/DiffGame'
  },
  {
    id: 5,
    title: '云图探遗・地标寻珍记',
    desc: '在云南地图上点选非遗项目分布地，了解地理与文化。',
    img: '/background/gameLoge/feiyiditu.png',
    buttonText: '开始挑战',
    route: '/games/map'
  },
  {
    id: 6,
    title: '瓷绘釉语・纹章赋色笺',
    desc: '为精美陶瓷器物涂上独特色彩，创造你的艺术品。',
    img: '/background/gameLoge/taocishangse.png',
    buttonText: '敬请期待',
    route: null
  },
  {
    id: 7,
    title: '扎染绎彩・经纬染梦簿',
    desc: '体验传统扎染工艺，设计属于你的个性化布艺。',
    img: '/background/gameLoge/zharan.png',
    buttonText: '敬请期待',
    route: null
  }
];

function handleGameClick(game) {
  if (game.id === 1) {
    showDifficultyModal.value = true;
  } else if (game.id === 2) {
    showQuizDifficultyModal.value = true;
  } else if (game.route === null) {
    showComingSoonModal.value = true;
  } else {
    router.push(game.route);
  }
}

function selectDifficulty(level) {
  selectedDifficulty.value = level;
  showDifficultyModal.value = false;
  showImageSelectModal.value = true;
}

// 新增：选择图片并跳转到游戏页面
function selectImage(imageUrl) {
  selectedImage.value = imageUrl;
  showImageSelectModal.value = false;
  // 跳转到拼图页面并传递难度和图片参数
  router.push({ 
    path: '/games/puzzle', 
    query: { 
      difficulty: selectedDifficulty.value, 
      image: selectedImage.value 
    } 
  });
}

function selectQuizDifficulty(level) {
  quizDifficulty.value = level;
  showQuizDifficultyModal.value = false;
  router.push({ path: '/games/quiz', query: { difficulty: quizDifficulty.value } });
}

function togglePlay() {
  if (!bgmAudio.value) return;
  if (isPlaying.value) {
    bgmAudio.value.pause();
    isPlaying.value = false;
  } else {
    bgmAudio.value.play();
    isPlaying.value = true;
  }
}
function toggleMute() {
  if (!bgmAudio.value) return;
  isMuted.value = !isMuted.value;
  bgmAudio.value.muted = isMuted.value;
}
function setVolume(val) {
  if (!bgmAudio.value) return;
  volume.value = val;
  bgmAudio.value.volume = val;
}

// 自动播放和同步状态
function onAudioPlay() { isPlaying.value = true; }
function onAudioPause() { isPlaying.value = false; }

function openMusicDropdown() { showMusicDropdown.value = true; }
function closeMusicDropdown() { showMusicDropdown.value = false; }

// 点击外部关闭下拉框
function handleClickOutside(event) {
  const dropdown = document.getElementById('music-dropdown-panel');
  const icon = document.getElementById('music-setting-icon');
  if (dropdown && !dropdown.contains(event.target) && icon && !icon.contains(event.target)) {
    showMusicDropdown.value = false;
  }
}

onMounted(() => {
  document.addEventListener('mousedown', handleClickOutside);
  nextTick(() => {
    if (bgmAudio.value) {
      bgmAudio.value.volume = volume.value;
      bgmAudio.value.muted = isMuted.value;
      const playPromise = bgmAudio.value.play();
      if (playPromise && playPromise.catch) {
        playPromise.catch(() => {});
      }
      isPlaying.value = true;
    }
  });
});

// 组件卸载时移除事件
onBeforeUnmount(() => {
  document.removeEventListener('mousedown', handleClickOutside);
});
</script>

<template>
  <div class="game-space-container">
    <!-- 只显示音乐设置按钮，点击后弹出下拉框 -->
    <img
      id="music-setting-icon"
      class="music-setting-icon"
      src="/music/音乐设置-音乐.png"
      alt="音乐设置"
      @click="openMusicDropdown"
      style="position:absolute;top:80px;right:60px;z-index:120;cursor:pointer;width:36px;height:36px;"
    />
    <transition name="fade">
      <div
        v-if="showMusicDropdown"
        id="music-dropdown-panel"
        class="music-dropdown-panel"
        style="position:absolute;top:100px;right:40px;z-index:130;"
      >
        <div class="music-control-bar" style="position:relative;box-shadow:none;top:0;right:0;">
          <img
            class="music-btn music-icon"
            :src="isPlaying ? '/music/音乐开.png' : '/music/音乐关.png'"
            @click="togglePlay"
            :alt="isPlaying ? '暂停音乐' : '播放音乐'"
            title="播放/暂停"
          />
          <img
            class="music-btn music-icon"
            :src="isMuted ? '/music/音乐关.png' : '/music/音乐设置-音乐.png'"
            @click="toggleMute"
            :alt="isMuted ? '取消静音' : '静音'"
            title="静音/取消静音"
          />
          <img
            class="music-slider-icon music-icon"
            src="/music/音乐 控制器 调音 菜单.png"
            alt="音量调节"
            style="margin-right:2px;"
          />
          <input type="range" min="0" max="1" step="0.01" v-model="volume" @input="setVolume($event.target.valueAsNumber)" style="vertical-align: middle; width: 80px;" />
        </div>
      </div>
    </transition>
    <audio ref="bgmAudio" src="/music/游戏空间背景音.mp3" loop :volume="volume" @play="onAudioPlay" @pause="onAudioPause" :muted="isMuted" style="display:none"></audio>
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

    <a-modal v-model:visible="showImageSelectModal" title="选择拼图图片" :footer="false" width="600px">
      <div class="image-select-grid">
        <div v-for="image in availableImages" :key="image" class="image-item" @click="selectImage(image)">
          <img :src="image" alt="拼图图片"/>
        </div>
      </div>
    </a-modal>

    <a-modal v-model:visible="showQuizDifficultyModal" title="选择答题难度" :footer="false" width="340px">
      <div class="difficulty-select-box">
        <a-button long size="large" @click="selectQuizDifficulty('简单')">简单（6题）</a-button>
        <a-button long size="large" @click="selectQuizDifficulty('中级')" style="margin: 18px 0;">中级（10题）</a-button>
        <a-button long size="large" @click="selectQuizDifficulty('高级')">高级（14题）</a-button>
      </div>
    </a-modal>

    <a-modal v-model:visible="showComingSoonModal" title="提示" :footer="false" width="400px">
      <div style="text-align: center; padding: 20px; position: relative;">
        <img src="/public/image/gumenglogo.png" alt="故梦阑珊 Logo" style="position: absolute; top: -50px; right: 0px; width: 80px; height: auto;">
        <p style="font-size: 18px; line-height: 1.8;">
          嘿～非遗的故事里，藏着 <b>"等待" 的智慧</b>：<br/>
          蓝靛草晒足月光才显色，油纸伞候透桐油方成型。<br/>
          您想解锁的功能，此刻正躲在开发间，偷学非遗的诗意表达呢！<br/>
          等它学成了最美的模样，第一时间跳出来见您～<br/>
          ——故梦阑珊开发团队
        </p>
        <p style="font-size: 20px; margin-top: 25px; font-style: italic;">
          别急，我们和美好，都在奔赴您的路上 ✨
        </p>
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

.image-select-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 15px;
  padding: 10px;
}

.image-item {
  border: 2px solid transparent;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
}

.image-item:hover {
  border-color: #C2101C;
  transform: scale(1.05);
}

.image-item img {
  display: block;
  width: 100%;
  height: auto;
}

@media (max-width: 900px) {
  .game-space-grid { grid-template-columns: 1fr; padding: 0 10px; }
  .game-space-header h1 { font-size: 28px; }
}

.music-setting-icon {
  transition: filter 0.2s;
}
.music-setting-icon:hover {
  filter: brightness(0.7);
}
.music-dropdown-panel {
  background: rgba(255,255,255,0.95);
  border-radius: 24px;
  box-shadow: 0 2px 16px rgba(194,16,28,0.13);
  padding: 12px 28px 12px 18px;
  border: 1.5px solid #e6c9a8;
  min-width: 220px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.2s;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
.music-icon {
  width: 24px !important;
  height: 24px !important;
}
</style> 