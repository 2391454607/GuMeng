<script setup>
import {ref} from "vue";
import { useCollapsedStore } from '@/stores/CollapsedStore.js';


import {router} from "@/router/index.js";

// 获取 collapsed store
const collapsedStore = useCollapsedStore();

// 处理用户头像下拉菜单点击事件
const handleMenuClick = ({ key }) => {
  switch (key) {
    case '1':
      router.push('/profile'); // 跳转到个人中心
      break;
    case '2':
      router.push('/settings'); // 跳转到设置
      break;
    case '3':
      // 处理退出登录逻辑
      message.success('退出登录成功');
      router.push('/login');
      break;
    default:
      break;
  }
};

</script>

<template>

    <a-layout-header class="header">
      <a-space class="flex">
        <MenuUnfoldOutlined
            v-if="collapsedStore.collapsed"
            class="trigger"
            @click="collapsedStore.toggleCollapsed"/>
        <MenuFoldOutlined
            v-else
            class="trigger"
            @click="collapsedStore.toggleCollapsed" />

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

      </a-space>

    </a-layout-header>

</template>

<style scoped>
.header{
  background: #FFFFFF;
  .flex{
    display: flex;
    justify-content: space-between;
  }
}

.trigger {
  margin-left: -30px;
  font-size: 24px;
  transition: color 0.3s;
}

.avatar{
  margin-right: 30px;
}

.trigger:hover {
  color: #1890ff;
}
</style>