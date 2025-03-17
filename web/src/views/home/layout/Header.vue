<script setup>
import { h, ref } from 'vue';
import { HomeFilled, AppstoreOutlined, SettingOutlined, LogoutOutlined, UserOutlined } from '@ant-design/icons-vue';

const current = ref([]);

const items = ref([
  {
    key: 'Home',
    icon: () => h(HomeFilled),
    label: '首页',
  },
  {
    key: '12',
    icon: () => h(AppstoreOutlined),
    label: '导航二',
  },
  {
    key: '45',
    icon: () => h(SettingOutlined),
    label: '导航三',
  },
]);

// 下拉菜单点击事件
const handleMenuClick = (e) => {
  console.log('点击菜单项:', e.key);
  switch (e.key) {
    case '1':
      // 处理个人中心逻辑
      break;
    case '2':
      // 处理设置逻辑
      break;
    case '3':
      // 处理退出登录逻辑
      break;
    default:
      break;
  }
};

defineProps({
    isScrolled: Boolean
});
</script>

<template>
  <a-layout-header style="background-color: #ffffff">
    <div class="menu-container">
      <a-menu v-model:selectedKeys="current" mode="horizontal" :items="items" class="menu" />

      <a-dropdown placement="bottom">
        <a-space class="avatar" direction="vertical" :size="32">
          <a-space wrap :size="16">
            <a-avatar size="large" class="avatar-icon">
              <template #icon><UserOutlined /></template>
            </a-avatar>
          </a-space>
        </a-space>
        <template #overlay>
          <a-menu @click="handleMenuClick" class="dropdown-menu">
            <a-menu-item key="1">
              <user-outlined />
              个人中心
            </a-menu-item>
            <a-menu-item key="2">
              <setting-outlined />
              设置
            </a-menu-item>
            <a-menu-divider />
            <a-menu-item key="3">
              <logout-outlined />
              退出登录
            </a-menu-item>
          </a-menu>
        </template>
      </a-dropdown>

    </div>
  </a-layout-header>
</template>

<style scoped>

.menu-container {
  width: 100%;
  height: 64px;
  display: flex;
  justify-content: center; /* 水平居中 */
  align-items: center; /* 垂直居中 */
  position: relative; /* 相对定位 */
}


.avatar {
  position: absolute; /* 绝对定位 */
  right: 48px; /* 右侧距离 */
}


</style>