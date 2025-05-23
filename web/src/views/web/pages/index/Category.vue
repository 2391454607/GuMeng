<script setup>
import { ref } from 'vue'
import '@arco-design/web-vue/dist/arco.css'


// 定义图片内容数据
const items = [
  {
    name: '民间文学',
    description: '以口头传承为主的民间故事、神话、史诗、谚语等，反映民众智慧与生活哲学',
    image: '/CategoryImg/1_民间文学_白蛇传.png'
  },
  {
    name: '传统美术',
    description: '涵盖绘画、雕塑、剪纸、年画等，具有鲜明民族风格与工艺特色',
    image: '/CategoryImg/7_传统美术_剪纸.png'
  },
  {
    name: '民俗',
    description: '与节庆、礼仪、信仰相关的民间习俗，反映社会生活方式与文化认同',
    image: '/CategoryImg/8_民俗.jpg'
  },
  {
    name: '传统技艺',
    description: '世代传承的手工技术，如陶瓷、织染、金属加工等，体现精湛工艺与智慧',
    image: '/CategoryImg/9_传统技艺_扎染.png'
  },
  {
    name: '传统医药',
    description: '以中医理论为基础，包括针灸、推拿、草药等，具有独特诊疗体系与养生理念',
    image: '/CategoryImg/10_传统医药_药材.jpg'
  },
  {
    name: '传统音乐',
    description: '各民族世代相传的声乐、器乐及演奏形式，体现独特的音乐风格与文化内涵',
    image: '/CategoryImg/2_传统音乐_古琴.jpg'
  },
  {
    name: '传统舞蹈',
    description: '源于生产生活、祭祀庆典的肢体艺术，展现民族性格与审美情趣',
    image: '/CategoryImg/3_传统舞蹈_孔雀舞.png'
  },
  {
    name: '传统戏剧',
    description: '融合唱腔、表演、音乐、美术的综合性舞台艺术，如京剧、昆曲等',
    image: '/CategoryImg/4_传统戏剧_皮影戏.jpg'
  },
  {
    name: '曲艺',
    description: '以说唱为主的表演艺术，如评书、相声、快板等，兼具娱乐性与文化传承功能',
    image: '/CategoryImg/5_曲艺_陕北说书.png'
  },
  {
    name: '传统体育、游艺与杂技',
    description: '包括武术、棋类、竞技游戏及杂耍等，兼具健身、竞技与娱乐价值',
    image: '/CategoryImg/6_传统体育_赛龙舟.png'
  },
]

const slideContainer = ref(null)
const isAnimating = ref(false)

// 防抖函数
const debounce = (fn, delay) => {
  let timer = null
  return function (...args) {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      fn.apply(this, args)
    }, delay)
  }
}

// 处理点击事件
const handleItemClick = (event) => {
  const clickedItem = event.currentTarget
  const items = Array.from(slideContainer.value.querySelectorAll('.item'))
  const clickedIndex = items.indexOf(clickedItem)

  // 只处理第3、4、5个图片的点击事件
  if (clickedIndex >= 2 && clickedIndex <= 4) {
    for(let i = 0; i < clickedIndex - 1; i++) {
      const currentItems = slideContainer.value.querySelectorAll('.item')
      slideContainer.value.appendChild(currentItems[0])
    }
  }
}

// 右箭头点击
const handleRightClick = debounce(() => {
  if (isAnimating.value) return

  isAnimating.value = true
  const items = slideContainer.value.querySelectorAll('.item')
  slideContainer.value.appendChild(items[0])

  setTimeout(() => {
    isAnimating.value = false
  }, 500)
})

// 左箭头点击
const handleLeftClick = debounce(() => {
  if (isAnimating.value) return

  isAnimating.value = true
  const items = slideContainer.value.querySelectorAll('.item')
  slideContainer.value.prepend(items[items.length - 1])

  setTimeout(() => {
    isAnimating.value = false
  }, 500)
})
</script>

<template>
  <div class="container">
    <div class="page-title">
      <h1>国家级非物质文化遗产代表性项目名录</h1>
      <p>传承千年文明，守护民族瑰宝</p>
    </div>
    <div class="slide" ref="slideContainer">
      <div 
        v-for="(item, index) in items" 
        :key="index"
        :class="['item', `item${index + 1}`]"
        :style="{ backgroundImage: `url(${item.image})` }"
        @click="handleItemClick"
      >
        <div class="content">
          <div class="name">{{ item.name }}</div>
          <div class="des">{{ item.description }}</div>
          <a-button class="view-more-btn" type="primary" size="large">
            <span class="btn-text">查看更多</span>
          </a-button>
        </div>
      </div>
    </div>
    <!-- 按钮 -->
    <div class="btns">
      <a-button class="s-btn" shape="circle" @click="handleLeftClick">
        <icon-left />
      </a-button>
      <a-button class="s-btn" shape="circle" @click="handleRightClick">
        <icon-right />
      </a-button>
    </div>
  </div>
</template>

<style scoped>
.container {
  width: 100vw;
  height: calc(100vh - 64px);
  filter: drop-shadow(0 0 10px #dbdbdb);
  background: linear-gradient(135deg, #8B1818 0%, #A52A2A 100%);
}

.item:nth-child(1),
.item:nth-child(2) {
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  margin-top: 0;
  border-radius: 4px;
  box-shadow: none;
  background-color: rgba(139, 24, 24, 0.7);  /* 使用半透明深红色 */
}

.slide {
  width: 100%;
  height: 100%;
  display: flex;
  position: relative;
}

.item {
  background-repeat: no-repeat;
  width: 200px;
  height: 300px;
  background-position: center;
  background-size: cover;
  position: absolute;
  top: 50%;
  margin-top: -150px;
  border-radius: 20px;
  transition: 0.5s;
  left: 55%;
}

.item:nth-child(3),
.item:nth-child(4),
.item:nth-child(5) {
  pointer-events: auto;
  cursor: pointer;
  animation: float 3s ease-in-out infinite;
  animation-delay: calc(var(--i, 0) * 0.2s);
}

.item:nth-child(3) { --i: 0; }
.item:nth-child(4) { --i: 1; }
.item:nth-child(5) { --i: 2; }

@keyframes float {
  0%, 100% {
    transform: translateY(-10px);
  }
  50% {
    transform: translateY(10px);
  }
}

.item:nth-child(1),
.item:nth-child(2) {
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  margin-top: 0;
  border-radius: 4px;
  box-shadow: none;
}

.item:nth-child(3) {
  left: 55%;
}

.item:nth-child(4) {
  left: calc(55% + 220px);
}

.item:nth-child(5) {
  left: calc(55% + 440px);
}

.item:nth-child(n + 6) {
  opacity: 0;
  left: calc(55% + 660px);
}

.item .content {
  position: absolute;
  width: 35%;
  left: 100px;
  top: 50%;
  transform: translateY(-50%);
  color: #eee;
  display: none;
}

.item:nth-child(2) .content {
  display: block;
}

.item .name {
  font-size: 40px;
  font-weight: 900;
  opacity: 0;
  /* 动画名 执行时间 动画曲线 执行1次 保持最后执行的状态 */
  animation: show 1s ease-in-out 1 forwards;
}

.item .des {
  margin: 20px 0;
  opacity: 0;
  /* 动画名 执行时间 动画曲线 延时 执行1次 保持最后执行的状态 */
  animation: show 1s ease-in-out 0.3s 1 forwards;
}


/* Arco 按钮样式覆盖 */
:deep(.arco-btn) {
  background-color: rgba(139, 24, 24, 0.2);
  border-color: rgba(255, 255, 255, 0.8);
  opacity: 0;
  animation: show 1s ease-in-out 0.6s 1 forwards;
}

:deep(.arco-btn:hover) {
  background-color: #fff;
  color: #8B1818;
  border-color: transparent;
  transform: translateY(-3px);
  box-shadow: 0 10px 20px rgba(139, 24, 24, 0.3);
}

:deep(.arco-btn:active) {
  transform: translateY(-1px);
  box-shadow: 0 5px 10px rgba(139, 24, 24, 0.2);
}

.btns {
  width: 100%;
  position: fixed;
  bottom: 50px;
  left: 0;
  display: flex;
  justify-content: center;
  z-index: 100;
}

.s-btn {
  width: 45px !important;
  height: 45px !important;
  margin: 0 50px;
  background-color: rgba(139, 24, 24, 0.5) !important;
  border: 2px solid rgba(255, 255, 255, 0.5) !important;
  color: #fff !important;
  backdrop-filter: blur(5px);
}

:deep(.s-btn .arco-icon) {
  font-size: 20px;
}

.s-btn:hover {
  background-color: rgba(246, 120, 120, 0.9) !important;
  color: #8B1818 !important;
  border-color: transparent !important;
  transform: scale(1.1);
  box-shadow: 0 0 20px rgba(139, 24, 24, 0.3);
}

@keyframes show {
  from {
    opacity: 0;
    transform: translateY(100px);
    filter: blur(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
    filter: blur(0);
  }
}

.page-title {
  position: absolute;
  top: 40px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 10;
  text-align: center;
  color: #fff;
}

.page-title h1 {
  font-size: 42px;
  font-weight: 900;
  margin-bottom: 16px;
  opacity: 0;
  animation: show 1s ease-in-out 0.3s forwards;
  text-shadow: 2px 2px 0 #000,
              -2px -2px 0 #000,
              2px -2px 0 #000,
              -2px 2px 0 #000,
              0 2px 0 #000,
              2px 0 0 #000,
              0 -2px 0 #000,
              -2px 0 0 #000;
  font-family: "华文中宋", "STZhongsong", "方正书宋", "FZShuSong", serif;
  color: #FAFAD2;  /* 浅黄色 */
}

.page-title p {
  font-size: 20px;
  opacity: 0;
  animation: show 1s ease-in-out 0.6s forwards;
  font-family: "楷体", "楷体_GB2312", "KaiTi", serif;
  color: #F0E68C;  /* 卡其色 */
  text-shadow: 1.5px 1.5px 0 #000,
              -1.5px -1.5px 0 #000,
              1.5px -1.5px 0 #000,
              -1.5px 1.5px 0 #000;
}

.item .name {
  font-size: 40px;
  font-weight: 900;
  opacity: 0;
  animation: show 1s ease-in-out 1 forwards;
  font-family: "华文中宋", "STZhongsong", "方正书宋", "FZShuSong", serif;
  color: #FAFAD2;  /* 浅黄色 */
  text-shadow: 2px 2px 0 #000,
              -2px -2px 0 #000,
              2px -2px 0 #000,
              -2px 2px 0 #000,
              0 2px 0 #000,
              2px 0 0 #000,
              0 -2px 0 #000,
              -2px 0 0 #000;
}

.item .des {
  margin: 20px 0;
  opacity: 0;
  animation: show 1s ease-in-out 0.3s 1 forwards;
  font-family: "楷体", "楷体_GB2312", "KaiTi", serif;
  letter-spacing: 1px;
  color: #F0E68C;  /* 卡其色 */
  text-shadow: 1.5px 1.5px 0 #000,
              -1.5px -1.5px 0 #000,
              1.5px -1.5px 0 #000,
              -1.5px 1.5px 0 #000;
}

.view-more-btn {
  background: rgba(139, 24, 24, 0.1) !important;
  border: 2px solid #FFF5B8 !important;
  color: #FFF5B8 !important;
  font-family: "楷体", "楷体_GB2312", "KaiTi", serif !important;
  font-size: 20px !important;
  letter-spacing: 4px !important;
  padding: 6px 30px !important;
  height: auto !important;
  transition: all 0.3s ease !important;
  position: relative;
  overflow: hidden;
}

.view-more-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    120deg,
    transparent,
    rgba(255, 245, 184, 0.2),
    transparent
  );
  transition: 0.5s;
}

.view-more-btn:hover {
  background: rgba(255, 245, 184, 0.2) !important;
  transform: translateY(-3px);
  box-shadow: 0 10px 20px rgba(255, 245, 184, 0.3);
}

.view-more-btn:hover::before {
  left: 100%;
}

.view-more-btn:active {
  transform: translateY(-1px);
  box-shadow: 0 5px 10px rgba(255, 245, 184, 0.2);
}

.btn-text {
  position: relative;
  z-index: 1;
}
</style>