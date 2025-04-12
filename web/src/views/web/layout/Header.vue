<script setup>
import { ref, onMounted } from 'vue';
import { router } from "@/router/index.js";
import { Message, Avatar, Dropdown, Doption } from "@arco-design/web-vue";
import { IconUser, IconExport, IconSettings } from '@arco-design/web-vue/es/icon';

const position = ref(0);
let begin = 0;
let end = 0;
let beginX = 0;

const menuItems = [
  { name: '首页', url: '/' },
  { name: '资讯', url: '/information' },
  { name: '非遗论坛', url: '/forum'},
  { name: '非遗互动', url: '/interaction'},
  { name: '虚拟展厅', url: '/showroom'},
  { name: '政策', url: '/policy'},
  { name: '百科', url: '/encyclopedia'},
];

// 点击菜单项
const onClickMenuItem = (menuItems) => {
  if (menuItems.url) {
    router.push(menuItems.url);
  } else {
    Message.warning('该菜单项没有配置跳转地址');
  }
};

const handleMouseOver = (event) => {
  end = event.target.offsetLeft;
};

const handleMouseDown = (event) => {
  beginX = event.target.offsetLeft;
};

const handleMouseOut = () => {
  end = beginX;
};

onMounted(() => {
  // 获取当前路由对应的菜单项
  const currentMenuItem = menuItems.find(item => item.url === router.currentRoute.value.path);
  if (currentMenuItem) {
    // 获取当前菜单项的DOM元素
    const currentElement = document.querySelector(`li:nth-child(${menuItems.indexOf(currentMenuItem) + 1})`);
    if (currentElement) {
      // 设置初始位置
      begin = currentElement.offsetLeft;
      end = begin;
      beginX = begin;
      position.value = begin;
    }
  }

  // 继续保持动画效果
  setInterval(() => {
    begin = begin + (end - begin) * 0.1;
    position.value = begin;
  }, 10);
});

// 添加登出处理函数
const handleLogout = () => {
  Message.success('退出登录成功');
  // 这里可以添加登出逻辑
};
</script>

<template>
  <a-layout-header class="header">

      <div class="container">
        <div class="header-left">
          故梦阑珊
        </div>

        <nav class="nav">
          <span class="t-mall" :style="{ left: position + 'px' }"></span>
          <ul>
            <li v-for="(item, index) in menuItems"
                :key="index"
                :class="{ active: $route.path === item.url }"
                @mouseover="handleMouseOver"
                @mousedown="handleMouseDown"
                @mouseout="handleMouseOut"
                @click="onClickMenuItem(item)">
              {{ item.name }}
            </li>
          </ul>
        </nav>

        <div class="header-right">
          <a-dropdown trigger="hover" position="bottom">
            <a-avatar :size="40" style="cursor: pointer">
              <icon-user />
            </a-avatar>
            <template #content>
              <a-doption @click="router.push('/')">
                <icon-user />个人中心
              </a-doption>
              <a-doption @click="handleLogout">
                <icon-export />退出登录
              </a-doption>
            </template>
          </a-dropdown>
        </div>
      </div>

  </a-layout-header>
</template>

<style scoped>
.header{
  background: #C2101C;
}

.container {
  width: 100%;
  height: 64px;
  display: flex;
  justify-content: center; /* 水平居中 */
  align-items: center; /* 垂直居中 */
  position: relative; /* 相对定位 */
}

.header-left{
  position: absolute;
  left: 24px;  /* 增加左边距 */
  display: flex;
  align-items: center;  /* 垂直居中 */
  color: white;
  height: 64px;
  font-size: 24px;  /* 设置字体大小 */
  font-weight: bold;  /* 加粗 */
  letter-spacing: 2px;  /* 字间距 */
  font-family: 'Courier New', Courier, monospace; /* 设置字体 */
  border: none;  /* 移除边框 */
}

.header-right{
  position: absolute;
  right: 24px;  /* 增加右边距 */
  display: flex;
  align-items: center;  /* 垂直居中 */
  color: white;
  height: 64px;
  font-size: 24px;  /* 设置字体大小 */
  font-weight: bold;  /* 加粗 */
  letter-spacing: 2px;  /* 字间距 */
  font-family: 'Courier New', Courier, monospace; /* 设置字体 */
  border: none;  /* 移除边框 */
}

.nav {
  border-radius: 10px;
  position: relative;
}

.nav ul {
  position: relative;
  display: flex;
  margin: 0;
  padding: 0;
}

.nav ul li {
  width: 90px;
  height: 64px;
  text-align: center;
  color: #eddede;
  font-weight: bold;
  line-height: 64px;
  cursor: pointer;
  list-style: none;
  transition: color 0.3s; /* 添加颜色过渡效果 */
}
.nav ul li:hover {
  color: #ffd700; /* 添加悬停效果 */
  font-weight: bold;
}
.nav ul li.active {
  color: #ffd700;
  font-weight: bold;
}

.t-mall {
  width: 90px;
  height: 64px;
  border-radius: 10px;
  background: url("@/assets/image/dh.png") no-repeat;
  position: absolute;
  transition: left 0.1s ease;
}
.header-right :deep(.arco-dropdown-item) {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
}

.header-right :deep(.arco-icon) {
  font-size: 16px;
}
</style>