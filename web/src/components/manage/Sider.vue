<script setup>
import { ref, computed } from "vue";
import { UserOutlined } from "@ant-design/icons-vue";
import { useCollapsedStore } from '@/stores/index.js'; // 引入 Pinia Store

const collapsedStore = useCollapsedStore();
const collapsed = computed(() => collapsedStore.collapsed); // 使用 computed 确保响应性
const selectedKeys = ref(['1']);

console.log('collapsed:', collapsed.value); // 在 Sider 组件中
</script>

<template>
  <a-layout-sider class="sider" :collapsed="collapsed" :trigger="null" collapsible>
    <div class="logo"/>
    <a-menu class="menu" v-model:selectedKeys="selectedKeys" mode="inline" theme="dark">
      <a-menu-item key="1" class="item">
        <user-outlined/>
        <span>nav 1</span>
      </a-menu-item>
      <a-menu-item key="2" class="item">
        <user-outlined/>
        <span>nav 2</span>
      </a-menu-item>
      <a-menu-item key="3" class="item">
        <user-outlined/>
        <span>nav 3</span>
      </a-menu-item>
    </a-menu>
  </a-layout-sider>
</template>

<style scoped>
.sider {
  min-height: calc(100vh);
  background-color: #2e2249; /* 深紫色背景 */
}

.logo {
  height: 32px;
  background: rgba(255, 255, 255, 0.3); /* 透明背景 */
  margin: 16px;
}

.menu {
  background-color: #2e2249; /* 深紫色背景 */
}

/* 使用深度选择器覆盖 Ant Design 的样式 */
:deep(.menu .ant-menu-item) {
  color: #a8a8a8; /* 默认文字颜色 */
}

:deep(.menu .ant-menu-item-selected) {
  background-color: #7c5cff; /* 选中背景色 */
  color: #ffffff; /* 选中文字颜色 */
}

:deep(.menu .ant-menu-item::after) {
  border-right: 3px solid #7c5cff; /* 选中项右侧高亮颜色 */
}
</style>