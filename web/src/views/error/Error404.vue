<script setup>
import { useRouter } from 'vue-router';
import Footer from "@/views/web/layout/Footer.vue";

const router = useRouter();

const goHome = () => {
  router.push('/');
};

const goBack = () => {
  router.back();
};
</script>

<template>
  <div class="not-found-container">
    <div class="content-wrapper">
      <div class="image-container">
        <img src="/image/gumeng.png" alt="故梦阑珊" class="not-found-image" />
      </div>
      <div class="text-container">
        <h1 class="error-code">404</h1>
        <h2 class="error-title">页面未找到</h2>
        <p class="error-message">非遗文化浩如烟海，这片区域还在建设中，我们一起探索其他非遗瑰宝～</p>
        <div class="button-group">
          <a-button type="primary" @click="goHome" class="custom-button home-btn">
            返回首页
          </a-button>
          <a-button @click="goBack" class="custom-button back-btn">
            返回上页
          </a-button>
        </div>
      </div>
    </div>
    <div class="pattern-overlay"></div>
  </div>

  <Footer class="footer"></Footer>

</template>

<style scoped>
.not-found-container {
  min-height: calc(100vh - 64px);
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #FAFAFA;
  position: relative;
  overflow: hidden;
}

.content-wrapper {
  display: flex;
  align-items: center;
  gap: 60px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 16px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08);
  position: relative;
  z-index: 2;
  max-width: 1000px;
  margin: 20px;
}

.image-container {
  flex: 0 0 400px;
}

.not-found-image {
  width: 100%;
  height: auto;
  opacity: 0.8;
}

.text-container {
  flex: 1;
  text-align: left;
}

.error-code {
  font-size: 72px;
  color: #C2101C;
  margin: 0;
  font-family: "STXingkai", "华文行楷", serif;
  line-height: 1;
}

.error-title {
  font-size: 32px;
  color: #333;
  margin: 20px 0;
  font-weight: bold;
}

.error-message {
  font-size: 16px;
  color: #666;
  margin-bottom: 40px;
  line-height: 1.6;
}

.button-group {
  display: flex;
  gap: 16px;
}

.custom-button {
  min-width: 120px;
  height: 40px;
  font-size: 16px;
}

.home-btn {
  background-color: #C2101C;
  border-color: #C2101C;
}

.home-btn:hover {
  background-color: #A50E18;
  border-color: #A50E18;
}

.pattern-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: url('/image/gumeng.png');
  opacity: 0.05;
  z-index: 1;
}

@media (max-width: 768px) {
  .content-wrapper {
    flex-direction: column;
    gap: 30px;
    padding: 20px;
  }

  .image-container {
    flex: 0 0 auto;
    width: 200px;
  }

  .error-code {
    font-size: 48px;
  }

  .error-title {
    font-size: 24px;
    margin: 15px 0;
  }

  .error-message {
    font-size: 14px;
    margin-bottom: 30px;
  }
}

.footer{
  position: absolute;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  bottom: 0;
  z-index: 10;
}
</style>