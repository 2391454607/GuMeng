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
      <a-menu-item class="item" key="1">
        <user-outlined/>
        <span>nav 1</span>
      </a-menu-item>
      <a-menu-item class="item" key="2">
        <user-outlined/>
        <span>nav 2</span>
      </a-menu-item>
      <a-menu-item class="item" key="3">
        <user-outlined/>
        <span>nav 3</span>
      </a-menu-item>
    </a-menu>
  </a-layout-sider>
</template>

<style scoped>
.sider {
  min-height: calc(100vh);
}
.logo {
  height: 32px;
  background: rgba(255, 255, 255, 0.3);
  margin: 16px;
}


</style>