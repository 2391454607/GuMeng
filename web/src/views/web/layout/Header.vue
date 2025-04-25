<script setup>
import {ref, onMounted} from 'vue';
import {router} from "@/router/index.js";
import {Message} from "@arco-design/web-vue";
import {IconUser, IconExport} from '@arco-design/web-vue/es/icon';
import {getUserInfoAPI, userLogoutAPI} from "@/api/Auth.js";

const position = ref(0);
let begin = 0;
let end = 0;
let beginX = 0;

const menuItems = [
  {name: '首页', url: '/'},
  {name: '非遗百科', url: '/information'},
  {name: '非遗论坛', url: '/forum'},
  {name: '非遗互动', url: '/interaction'},
  {name: '虚拟展厅', url: '/showroom'},
  {name: '相关政策', url: '/policy'},
  {name: '非遗文创', url: '/shop'},
];

// 点击菜单项
const onClickMenuItem = (menuItems) => {
  if (menuItems.url) {
    router.push(menuItems.url);
  } else {
    Message.warning('该菜单项没有配置跳转地址');
  }
};

const handleMouseOver = (event) => end = event.target.offsetLeft;
const handleMouseDown = (event) => beginX = event.target.offsetLeft;
const handleMouseOut = () => end = beginX;

const token = localStorage.getItem('token');
// 存储用户信息
const userInfo = ref({});

const isAdmin = ref(false);

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
  //获取用户登录信息
  try {
    const token = localStorage.getItem('token');
    if (token) {
      // 从 token 中解析用户角色
      const tokenParts = token.split('.');
      const claims = JSON.parse(atob(tokenParts[1]));
      isAdmin.value = claims.claims.role.includes('superAdmin') || claims.claims.role.includes('admin');

      getUserInfoAPI().then(res => {
        console.log(res.data);
        if (res.code === 200) {
          userInfo.value = res.data;
          // 更新本地存储的用户信息
          localStorage.setItem('userInfo', JSON.stringify(res.data));
        } else {
          Message.error('获取用户信息失败');
        }
      });
    }
  } catch (error) {
    console.error('获取用户信息错误:', error);
    Message.error('获取用户信息失败');
  }
});

// 退出登录
const handleLogout = () => {
  const token = localStorage.getItem('token');
  if (token) {
    userLogoutAPI().then(res => {
      if (res.code === 200) {
        localStorage.removeItem('token');
        localStorage.removeItem('userInfo');
        Message.success('退出登录成功');
        // 延时刷新当前页面
        setTimeout(() => {
          window.location.reload();
        }, 1000);
      } else {
        Message.error(res.msg || '退出登录失败');
      }
    }).catch(error => {
      Message.error('退出登录失败');
      console.error('退出登录错误:', error);
    });
  } else {
    Message.warning('您尚未登录');
    router.push('/login');
  }
};

</script>

<template>
  <a-layout-header class="header">

    <div class="container">
      <div class="header-left">
        故梦阑珊
      </div>

      <nav class="nav">
        <span :style="{ left: position + 'px' }" class="t-mall"></span>
        <ul>
          <li v-for="(item, index) in menuItems"
              :key="index"
              :class="{ active: $route.path === item.url }"
              @click="onClickMenuItem(item)"
              @mousedown="handleMouseDown"
              @mouseout="handleMouseOut"
              @mouseover="handleMouseOver">
            <template v-if="item.children">
              <a-dropdown trigger="hover">
                <span>{{ item.name }}</span>
                <template #content>
                  <a-doption v-for="(child, childIndex) in item.children"
                            :key="childIndex"
                            @click="onClickMenuItem(child)">
                    {{ child.name }}
                  </a-doption>
                </template>
              </a-dropdown>
            </template>
            <template v-else>
              {{ item.name }}
            </template>
          </li>
        </ul>
      </nav>

      <div class="header-right">
        <template v-if="token">
          <a-dropdown position="bottom" trigger="hover">
            <a-space>
              <a-avatar :image-url="userInfo.userPic || '/image/gumeng.png'" :size="40"
                        style="cursor: pointer">
                <template #fallback>
                  <icon-user/>
                </template>
              </a-avatar>
              <span style="color: white">{{ userInfo.nickname || '用户' }}</span>
            </a-space>
            <template #content>
              <a-doption @click="router.push('/userInfo')">
                <icon-user/>
                个人中心
              </a-doption>
              <a-doption v-if="isAdmin" @click="router.push('/sys')">
                <icon-settings/>
                管理系统
              </a-doption>
              <a-doption @click="router.push('/visualization/trend')">
                <icon-settings/>
                非遗数据大屏
              </a-doption>
              <a-doption @click="handleLogout">
                <icon-export/>
                退出登录
              </a-doption>
            </template>
          </a-dropdown>
        </template>
        <template v-else>
          <a-space>
            <a-button type="outline" @click="router.push('/login')">
              登录
            </a-button>
            <a-button type="outline" @click="router.push('/register')">
              注册
            </a-button>
          </a-space>
        </template>
      </div>
    </div>

  </a-layout-header>
</template>

<style scoped>
.header {
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

.header-left {
  position: absolute;
  left: 24px; /* 增加左边距 */
  display: flex;
  align-items: center; /* 垂直居中 */
  color: white;
  height: 64px;
  font-size: 24px; /* 设置字体大小 */
  font-weight: bold; /* 加粗 */
  letter-spacing: 2px; /* 字间距 */
  font-family: 'Courier New', Courier, monospace; /* 设置字体 */
  border: none; /* 移除边框 */
}

.header-right {
  position: absolute;
  right: 24px; /* 增加右边距 */
  display: flex;
  align-items: center; /* 垂直居中 */
  color: white;
  height: 64px;
  font-size: 24px; /* 设置字体大小 */
  font-weight: bold; /* 加粗 */
  letter-spacing: 2px; /* 字间距 */
  font-family: 'Courier New', Courier, monospace; /* 设置字体 */
  border: none; /* 移除边框 */
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
  background: url("/image/dh.png") no-repeat;
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

.header-right :deep(.arco-btn) {
  color: white;
  border-color: white;
}

.header-right :deep(.arco-btn:hover) {
  color: #ffd700;
  border-color: #ffd700;
}
</style>