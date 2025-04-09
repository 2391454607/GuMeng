<script setup>
import { ref, onMounted } from 'vue';
import { router } from "@/router/index.js";
import { Message } from "@arco-design/web-vue";

const position = ref(0);
let begin = 0;
let end = 0;
let beginX = 0;

const menuItems = [
  { name: '首页', url: '/' },
  { name: '资讯', url: '/information' },
  { name: '非遗论坛', url: ''},
  { name: '虚拟展厅', url: ''},
  { name: '政策', url: ''},
  { name: '百科', url: ''},
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
        <nav class="nav">
          <span class="t-mall" :style="{ left: position + 'px' }"></span>
          <ul>
            <li v-for="(item, index) in menuItems"
                :key="index"
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
  width: 88px;
  height: 63px;
  text-align: center;
  line-height: 63px;
  cursor: pointer;
  list-style: none;
}

.t-mall {
  width: 88px;
  height: 63px;
  border-radius: 10px;
  background: url("@/assets/image/logo.png") no-repeat;
  position: absolute;
  transition: left 0.1s ease;
}
</style>