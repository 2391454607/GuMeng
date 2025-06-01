<script setup>

</script>

<template>
  <div class="app-root">
    <RouterView />
    <button v-if="$route.path.startsWith('/visualization')" class="back-home-btn" @click="$router.push('/')">返回首页</button>
  </div>
</template>

<style>
.back-home-btn {
  position: fixed;
  top: 32px;
  right: 48px;
  z-index: 9999;
  background: #fffbe6;
  border: 2px solid #cbb279;
  border-radius: 18px 0 18px 0;
  font-family: 'FZKai-Z03', 'KaiTi', 'STKaiti', serif;
  font-size: 20px;
  color: #8b1f1f;
  padding: 10px 28px;
  box-shadow: 0 2px 8px rgba(194,16,28,0.08);
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
}
.back-home-btn:hover {
  background: #f5e6d6;
  color: #a67c52;
}
</style>
