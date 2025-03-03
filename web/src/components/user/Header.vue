<script setup>
import { h, ref } from 'vue';
import { HomeFilled, AppstoreOutlined, SettingOutlined } from '@ant-design/icons-vue';
const current = ref([

]);
const items = ref([
  {
    key: 'Home',
    icon: () => h(HomeFilled),
    label: '首页',
  },
  {
    key: '12',
    icon: () => h(AppstoreOutlined),
    label: 'Navigation Two',
  },
  {
    key: '45',
    icon: () => h(SettingOutlined),
    label: 'Navigation Three - Submenu',
  },
]);
</script>

<template>
  <a-layout-header class="header">
    <div class="menu-container">
      <a-menu v-model:selectedKeys="current" mode="horizontal" :items="items" />
    </div>
  </a-layout-header>
</template>

<style scoped>
.header {
  width: 100%;
  height: 64px;
  display: flex;
  justify-content: center; /* 水平居中 */
  align-items: center; /* 垂直居中 */
}
.menu-container {
  display: inline-block; /* 让菜单容器宽度自适应内容 */
}
</style>