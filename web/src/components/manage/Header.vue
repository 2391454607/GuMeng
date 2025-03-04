<script setup>
import { MenuFoldOutlined, MenuUnfoldOutlined } from '@ant-design/icons-vue';
import { computed } from 'vue';
import { useCollapsedStore } from '@/stores/index.js'; // 引入 Pinia Store
import { UserOutlined, SettingOutlined, LogoutOutlined } from "@ant-design/icons-vue";

const collapsedStore = useCollapsedStore();
const collapsed = computed(() => collapsedStore.collapsed); // 使用 computed 确保响应性

const toggleCollapsed = () => {
  collapsedStore.toggleCollapsed(); // 切换 collapsed 状态
};

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
    <div>
      <menu-unfold-outlined
          v-if="collapsed"
          class="trigger"
          @click="toggleCollapsed"
      />
      <menu-fold-outlined v-else class="trigger" @click="toggleCollapsed" />
    </div>
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
  </a-layout-header>
</template>

<style scoped>
.header{
  background: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.trigger {
  font-size: 24px;
  margin-left: -24px;
  cursor: pointer;
  transition: color 0.3s;
}

.trigger:hover {
  color: #1890ff;
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
</style>