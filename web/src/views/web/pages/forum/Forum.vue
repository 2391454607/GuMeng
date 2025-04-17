<script setup>
// 论坛模块主容器组件
</script>

<template>
  <div class="forum-container">
    <router-view />
  </div>
</template>

<style scoped>
.forum-container {
  width: 100%;
  min-height: calc(100vh - 64px);
  background-color: #f6f6f6;
}
</style>