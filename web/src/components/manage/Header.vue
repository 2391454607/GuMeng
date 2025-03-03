<script setup>
import { MenuFoldOutlined, MenuUnfoldOutlined } from '@ant-design/icons-vue';
import { computed } from 'vue';
import { useCollapsedStore } from '@/stores/index.js'; // 引入 Pinia Store

const collapsedStore = useCollapsedStore();
const collapsed = computed(() => collapsedStore.collapsed); // 使用 computed 确保响应性

const toggleCollapsed = () => {
  collapsedStore.toggleCollapsed(); // 切换 collapsed 状态
};

console.log('collapsed:', collapsed.value); // 在 Header 组件中
</script>

<template>
  <a-layout-header style="background: #fff; padding: 0">
    <menu-unfold-outlined
        v-if="collapsed"
        class="trigger"
        @click="toggleCollapsed"
    />
    <menu-fold-outlined v-else class="trigger" @click="toggleCollapsed" />
  </a-layout-header>
</template>

<style scoped>
.trigger {
  font-size: 18px;
  line-height: 64px;
  padding: 0 24px;
  cursor: pointer;
  transition: color 0.3s;
}

.trigger:hover {
  color: #1890ff;
}
</style>