<script setup>
import {onMounted, ref} from 'vue';
import {router} from "@/router/index.js";
import {getAdminMenuAPI} from "@/api/Menu.js";
import {Message} from "@arco-design/web-vue";
//导入图标
import {
  IconHome,
  IconUser,
  IconCommon,
  IconDashboard,
  IconFileImage,
  IconBook,
  IconMessage,
  IconCalendarClock
}
  from '@arco-design/web-vue/es/icon';

const hidden = defineModel("hidden", {required: true, type: Boolean, default: false});
defineProps({
  onClickMenuItem: Function
})

// 添加选中菜单的状态管理
const selectedKeys = ref([]);
const openKeys = ref([]);

// 根据当前路由设置选中菜单
const setSelectedMenu = (menuList) => {
  const currentPath = router.currentRoute.value.path.replace('/sys', '');
  const findMenu = (list) => {
    for (const item of list) {
      if (item.url === currentPath) {
        selectedKeys.value = [item.id];
        return;
      }
      if (item.children?.length) {
        findMenu(item.children);
      }
    }
  }
  findMenu(menuList);
}

onMounted(() => {
  getAdminMenuAPI().then(res => {
    menuData.value = res.data;
    loading.value = false;
    setSelectedMenu(res.data);
  })
})

const onClickMenuItem = (menu) => {
  if (menu.url !== undefined && menu.url !== null && menu.url !== '') {
    selectedKeys.value = [menu.id];
    router.push("/sys" + menu.url);
  } else {
    Message.warning('该菜单项没有配置跳转地址!');
  }
}

// 定义响应式菜单数据
const menuData = ref([]);
const loading = ref(true);

onMounted(() => {
  getAdminMenuAPI().then(res => {
    menuData.value = res.data;
    loading.value = false;  // 数据加载完成
  })
})

// 图标映射
const iconMap = {
  home: IconHome,
  user: IconUser,
  shop: IconCommon,
  dashboard: IconDashboard,
  fileImage: IconFileImage,
  forum: IconBook,
  comments: IconMessage,
  log:IconCalendarClock
};
</script>

<template>

  <a-layout-sider 
    :collapsed="hidden"
    class="layout-sider"
  >
    <div class="system_logo">
      <span class="logo-text" :class="{ 'collapsed': hidden }">
        {{ hidden ? '故' : '故梦阑珊' }}
      </span>
    </div>

    <a-spin :loading="loading" :size="28" class="loading">
      <a-menu 
        :collapsed="hidden"
        :selected-keys="selectedKeys"
        :open-keys="openKeys"
        auto-open
        class="side-menu"
      >
        <!--左侧列表-->
        <template v-for="menu in menuData" :key="menu.id">
          <!-- 动态渲染菜单项 -->
          <a-menu-item
              v-if="menu.type === 'menu'"
              :key="menu.id"
              :url="menu.url"
              @click="onClickMenuItem(menu)"
          >
            <component :is="iconMap[menu.icon]"/>
            {{ menu.name }}
          </a-menu-item>

          <!-- 处理子菜单 -->
          <a-sub-menu
              v-if="menu.children && menu.children.length > 0"
              :key="menu.id"
              :title="menu.name"
          >
            <template #icon>
              <component :is="iconMap[menu.icon]"/>
            </template>
            <template v-for="children in menu.children" :key="children.id">
              <a-menu-item
                  :url="children.url"
                  @click="onClickMenuItem(children)">
                <component :is="iconMap[children.icon]"/>
                {{ children.name }}
              </a-menu-item>
            </template>
          </a-sub-menu>
        </template>

      </a-menu>
    </a-spin>
  </a-layout-sider>
</template>

<style scoped>
.layout-sider {
  position: fixed;
  left: 0;
  top: 0;
  z-index: 100;
  box-shadow: 2px 0 8px 0 rgba(29,35,41,.05);
  transition: all 0.2s cubic-bezier(0.34, 0.69, 0.1, 1);
}

.system_logo {
  height: 64px;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  background: rgb(var(--primary-6));
}

.logo-text {
  color: #fff;
  font-size: 18px;
  font-weight: 500;
  transition: all 0.2s cubic-bezier(0.34, 0.69, 0.1, 1);
  white-space: nowrap;
  
  &.collapsed {
    font-size: 24px;
  }
}

.side-menu {
  height: calc(100vh - 64px);
  width: 200px;
  transition: all 0.2s cubic-bezier(0.34, 0.69, 0.1, 1);
}

.loading {
  height: calc(100vh - 64px);
}

:deep(.arco-menu-inline-header) {
  margin: 4px 8px;
  border-radius: 4px;
  
  &.arco-menu-selected {
    background-color: rgb(var(--primary-1));
    color: rgb(var(--primary-6));
  }
  
  &:hover {
    background-color: rgba(var(--primary-1), 0.5);
  }
}

:deep(.arco-menu-item .arco-icon) {
  margin-right: 10px;
  font-size: 16px;
}
</style>