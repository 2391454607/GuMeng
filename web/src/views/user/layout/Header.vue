<script setup>
import {h, onMounted, ref, computed, watch} from 'vue';
import {getUserMenuAPI} from "@/api/Menu.js";
import {router} from "@/router/index.js";
import { message } from 'ant-design-vue';

import {
  HomeOutlined,
  SettingOutlined,
  LogoutOutlined,
  UserOutlined,

} from '@ant-design/icons-vue';
import {useRoute} from "vue-router";

const menuData = ref();

onMounted(() => {
  getUserMenuAPI().then(res => {
    menuData.value = buildMenuTree(res); // 转换为树形结构
    console.log(menuData.value);
  })
})

// 将扁平数据转换为树形结构
const buildMenuTree = (flatData, parentId = 0) => {
  return flatData
      .filter((item) => item.parent === parentId) // 过滤出当前层级的菜单项
      .map((item) => {
        const children = buildMenuTree(flatData, item.id); // 递归构建子菜单
        if (children.length > 0) {
          item.children = children; // 如果有子菜单，添加到 children 字段
        }
        return item;
      });
};

// 点击菜单项
const onClickMenuItem = (menu) => {
  if (menu.url) {
    router.push(menu.url);
  } else {
    message.warning('该菜单项没有配置跳转地址');
  }
};


// 图标映射
const iconMap = {
  home: HomeOutlined,
  settings: SettingOutlined,
  user: UserOutlined,
};

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
    <div class="container">
      <!-- 渲染动态菜单 -->
      <a-menu mode="horizontal" class="menu">
        <template v-for="menu in menuData" :key="menu.id">
          <a-sub-menu v-if="menu.children && menu.children.length" :key="menu.id">
            <template #title>
              <component :is="iconMap[menu.icon]" v-if="menu.icon" />
              {{ menu.name }}
            </template>
            <a-menu-item v-for="sub in menu.children" :key="sub.id" @click="onClickMenuItem(sub)">
              <component :is="iconMap[sub.icon]" v-if="sub.icon" />
              {{ sub.name }}
            </a-menu-item>
          </a-sub-menu>

          <a-menu-item v-else :key="menu.id" @click="onClickMenuItem(menu)">
            <component :is="iconMap[menu.icon]" v-if="menu.icon" />
            {{ menu.name }}
          </a-menu-item>
        </template>
      </a-menu>

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

.header{
  background-color: rgb(255, 255, 255);

}

.container {
  width: 100%;
  height: 64px;
  display: flex;
  justify-content: center; /* 水平居中 */
  align-items: center; /* 垂直居中 */
  position: relative; /* 相对定位 */
}

.menu{
  background-color: rgba(255, 255, 255, 0);
}


.avatar {
  position: absolute; /* 绝对定位 */
  right: 48px; /* 右侧距离 */
}


</style>