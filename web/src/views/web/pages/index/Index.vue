<script setup>
import {onMounted, onUnmounted, ref} from 'vue'
import {getCarouselAPI} from "@/api/web/Web.js";
import Footer from "@/views/web/layout/Footer.vue";
import Category from "@/views/web/pages/index/Category.vue"


const currentSection = ref(0);
let isScrolling = false;
const sections = ref([]);

// 优化后的滚动处理函数
const handleScroll = (direction) => {
  if (isScrolling) return;

  if (direction === 'down' && currentSection.value < sections.value.length - 1) {
    currentSection.value++;
    scrollToSection(currentSection.value);
  } else if (direction === 'up' && currentSection.value > 0) {
    currentSection.value--;
    scrollToSection(currentSection.value);
  }
};

// 滚轮事件处理
const handleWheel = (e) => {
  e.preventDefault();
  const direction = e.deltaY > 0 ? 'down' : 'up';
  handleScroll(direction);
};

// 键盘事件处理
const handleKeydown = (e) => {
  if (e.key === 'ArrowDown' || e.key === 'ArrowUp') {
    e.preventDefault();
    handleScroll(e.key === 'ArrowDown' ? 'down' : 'up');
  }
};

const scrollToSection = (index) => {
  isScrolling = true;
  sections.value[index].scrollIntoView({
    behavior: 'smooth',
    block: 'start'
  });

  // 优化滚动锁定时间
  setTimeout(() => {
    isScrolling = false;
  }, 800);
};

//轮播图列表
const carouselList = ref([
  {
    id: "default",
    imageUrl: "/image/gumeng.png"  // 默认轮播图
  }
])

onMounted(() => {
  sections.value = document.querySelectorAll('.section');
  window.addEventListener('wheel', handleWheel, {passive: false});
  window.addEventListener('keydown', handleKeydown);
  
  getCarouselAPI().then((res => {
    carouselList.value = res.data
  }))
});

onUnmounted(() => {
  window.removeEventListener('wheel', handleWheel);
  window.removeEventListener('keydown', handleKeydown);
});
</script>

<template>
  <a-layout-content class="content">
    <div class="full_page-container">
      <!-- 页面内容 -->
      <section class="section">
        <a-carousel 
          class="carousel" 
          :auto-play="{interval: 3000, hoverToPause: false }"
          indicator-type="line"
          show-arrow="hover"
          :move-speed="2000"
        >
          <a-carousel-item v-for="item in carouselList" :key="item.id">
            <div class="image-container">
              <img class="carousel-item" :src="item.imageUrl" alt=""/>
            </div>
          </a-carousel-item>
        </a-carousel>
        <!-- 添加下滑提示 -->
        <div class="scroll-hint">
          <p>下滑获取更多内容</p>
          <div class="scroll-arrow"></div>
        </div>
      </section>

      <section class="section">
        <Category/>
      </section>

      <section class="section">
        第三屏
      </section>

      <section class="section">
        第四屏
      </section>

      <section class="section">
        <Footer class="footer"></Footer>
      </section>
      
    </div>
  </a-layout-content>
</template>

<style scoped>
.content {
  text-align: center;
  background-color: #fff;
  height: calc(100vh - 64px);
  overflow: hidden;
  position: relative;
}

.full_page-container {
  height: 100%;
  scroll-snap-type: y mandatory;
  overflow-y: auto;
  scroll-behavior: smooth;
}

.section {
  height: 100%;
  scroll-snap-align: start;
  scroll-snap-stop: always;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 2em;
  transition: transform 0.5s ease;
}

.carousel{
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.carousel-item {
  display: flex;
  align-items: center;
  justify-content: center;
  height: calc(100vh - 64px);
  width: 100vw;
}

.image-container {
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
}

.section:nth-child(1) {
  background-color: #f5f5f5;
  transition: all 0.5s ease-out;
  padding: 0;
  overflow: hidden;
  position: relative;
}

.section:nth-child(5) {
  background-color: #34f4aa;
  transition: all 0.5s ease-out;
  padding: 0;
  overflow: hidden;
  position: relative;
}

/* 修改滚动条样式 */
.full_page-container::-webkit-scrollbar {
  display: none;
}

.full_page-container {
  -ms-overflow-style: none;  /* IE and Edge */
  scrollbar-width: none;  /* Firefox */
}

.scroll-hint {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  text-align: center;
  color: #ffffff;
  font-size: 16px;
  animation: bounce 2s infinite;
  z-index: 10; /* 提高层级 */
  pointer-events: none; /* 防止遮挡点击事件 */
}

.scroll-arrow {
  width: 20px;
  height: 20px;
  border-right: 2px solid #ffffff;
  border-bottom: 2px solid #ffffff;
  transform: rotate(45deg);
  margin: 10px auto;
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% {
    transform: translateY(0) translateX(-50%);
  }
  40% {
    transform: translateY(-10px) translateX(-50%);
  }
  60% {
    transform: translateY(-5px) translateX(-50%);
  }
}

:deep(.arco-carousel-arrow-left) {
  left: 30px;
  width: 50px;
  height: 50px;
}

:deep(.arco-carousel-arrow-right) {
  right: 30px;
  width: 50px;
  height: 50px;
}

:deep(.arco-icon) {
  font-size: 30px;
  color: #ffffff;
}

.footer{
  position: absolute;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  bottom: 0;
}

</style>