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
</script>

<template>
  <a-layout-header class="header">
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
.header {
  background: #fff;
  padding: 0 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1); /* 添加阴影 */
}

.menu-container {
  width: 100%;
  height: 64px;
  display: flex;
  justify-content: center; /* 水平居中 */
  align-items: center; /* 垂直居中 */
  position: relative; /* 相对定位 */
}

.menu {
  border-bottom: none; /* 移除默认边框 */
  position: absolute; /* 绝对定位 */
  left: 50%; /* 水平居中 */
  transform: translateX(-50%); /* 水平居中 */
}

.avatar {
  position: absolute; /* 绝对定位 */
  height: 45px;
  right: 24px; /* 右侧距离 */
  cursor: pointer;
}

.avatar-icon {
  transition: transform 0.3s, box-shadow 0.3s;
}

.avatar-icon:hover {
  transform: scale(1.1); /* 悬停时放大 */
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15); /* 悬停时添加阴影 */
}

.dropdown-menu {
  border-radius: 8px; /* 圆角 */
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15); /* 阴影 */
}

.dropdown-menu .ant-menu-item {
  padding: 8px 16px; /* 调整菜单项内边距 */
  transition: background-color 0.3s;
}

.dropdown-menu .ant-menu-item:hover {
  background-color: #f5f5f5; /* 悬停背景色 */
}

.dropdown-menu .ant-menu-item-selected {
  background-color: #e6f7ff; /* 选中背景色 */
  color: #1890ff; /* 选中文字颜色 */
}
</style>