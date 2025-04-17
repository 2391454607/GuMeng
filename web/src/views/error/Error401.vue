<script setup>
import { useRouter } from 'vue-router';

const router = useRouter();

const goHome = () => {
  router.push('/');
};

const goLogin = () => {
  router.push('/login');
};
</script>

<template>
  <div class="unauthorized-container">
    <div class="content-wrapper">
      <div class="image-container">
        <img src="@/assets/image/gumeng.png" alt="传统龙纹" class="dragon-image" />
      </div>
      <div class="text-container">
        <h1 class="error-code">401</h1>
        <h2 class="error-title">访问未授权</h2>
        <p class="error-message">非遗文化是中华民族的瑰宝，需要登录后才能继续探索～</p>
        <div class="button-group">
          <a-button type="primary" @click="goLogin" class="custom-button login-btn">
            立即登录
          </a-button>
          <a-button @click="goHome" class="custom-button home-btn">
            返回首页
          </a-button>
        </div>
      </div>
    </div>
    <div class="pattern-overlay"></div>
  </div>
</template>

<style scoped>
.unauthorized-container {
  min-height: 100vh;
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

.dragon-image {
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

.login-btn {
  background-color: #C2101C;
  border-color: #C2101C;
}

.login-btn:hover {
  background-color: #A50E18;
  border-color: #A50E18;
}

.pattern-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: url('@/assets/image/gumeng.png');
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
</style>