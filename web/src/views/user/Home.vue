<script setup>
import Header from "@/views/user/layout/Header.vue";
import Footer from "@/views/user/layout/Footer.vue";
import {onMounted, onUnmounted, ref, watch} from "vue";
import {useRoute} from "vue-router";

// 获取当前路由
const route = useRoute();

// 控制 Header 显示/隐藏的状态
const isHeaderVisible = ref(true);
let lastScrollPosition = 0;

// 判断当前路由是否需要启用 Header 显示/隐藏效果
const isHeaderEffectEnabled = ref(false);

// 监听路由变化
watch(
    () => route.path,
    (newPath) => {
      // 在这里设置需要启用效果的路由
      const enabledRoutes = ["/", "/about"]; // 示例路由
      isHeaderEffectEnabled.value = enabledRoutes.includes(newPath);

      // 如果当前路由不需要效果，强制显示 Header
      if (!isHeaderEffectEnabled.value) {
        isHeaderVisible.value = true;
      }
    },
    { immediate: true } // 立即执行一次
);

// 监听滚动事件
const handleScroll = () => {
  if (!isHeaderEffectEnabled.value) return; // 如果当前路由不需要效果，直接返回

  const currentScrollPosition = window.scrollY || document.documentElement.scrollTop;

  if (currentScrollPosition > lastScrollPosition) {
    // 向下滚动，隐藏 Header
    isHeaderVisible.value = false;
  } else {
    // 向上滚动，显示 Header
    isHeaderVisible.value = true;
  }
  // 更新上一次滚动位置
  lastScrollPosition = currentScrollPosition;
};

// 组件挂载时添加滚动监听
onMounted(() => {
  window.addEventListener("scroll", handleScroll);
});

// 组件卸载时移除滚动监听
onUnmounted(() => {
  window.removeEventListener("scroll", handleScroll);
});
</script>

<template>
  <a-layout>

    <a-layout class="layout">
      <Header :class="{ 'header-hidden': !isHeaderVisible && isHeaderEffectEnabled }"></Header>

      <a-layout-content class="content">
        <router-view></router-view>
      </a-layout-content>

    </a-layout>

    <Footer class="footer"></Footer>
  </a-layout>

</template>

<style scoped>
.layout {
  min-height: 100vh; /* 确保布局占满整个视口高度 */
}

/* Header 隐藏时的动画效果 */
.header-hidden {
  transform: translateY(-100%);
  transition: transform 1s ease-in-out;
}

/* Header 显示时的动画效果 */
Header {
  transform: translateY(0);
  transition: transform 1s ease-in-out;
  z-index: 1000; /* 确保 Header 在最上层 */
}

.content {
}

</style>