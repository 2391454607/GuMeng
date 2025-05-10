<script setup>
import { useUserStore } from '@/stores/userStore';
import { onMounted, onUnmounted } from 'vue';

const userStore = useUserStore();

// 用户活跃度监听
const handleUserActivity = () => {
  if (userStore.token) {
    userStore.updateActivityTime();
  }
};

onMounted(() => {
  userStore.initializeAuth();

  // 添加活跃度监听
  window.addEventListener('mousemove', handleUserActivity);
  window.addEventListener('keydown', handleUserActivity);
  window.addEventListener('click', handleUserActivity);
});

onUnmounted(() => {
  // 清理监听器
  window.removeEventListener('mousemove', handleUserActivity);
  window.removeEventListener('keydown', handleUserActivity);
  window.removeEventListener('click', handleUserActivity);
});
</script>

<template>
  <RouterView></RouterView>
</template>

<style scoped>

</style>
