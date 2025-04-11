<script setup>
import { ref, onMounted } from 'vue';
import { router } from "@/router/index.js";
import { Message } from "@arco-design/web-vue";
import { useRoute } from 'vue-router';
const route = useRoute();

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
  setInterval(() => {
    begin = begin + (end - begin) * 0.1;
    position.value = begin;
  }, 10);
});


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
</style>