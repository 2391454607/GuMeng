<script setup>
import {onMounted, ref} from 'vue';
import {useCollapsedStore} from '@/stores/CollapsedStore.js';
import {router} from "@/router/index.js";
import Message from "@ant-design/icons-vue/lib/utils.js";
import {getAdminMenuAPI} from "@/api/Menu.js";

//导入图标
import {
  HomeOutlined,
  PictureOutlined,
  UserOutlined,

} from "@ant-design/icons-vue"


// 获取 collapsed store
const collapsedStore = useCollapsedStore();

const onClickMenuItem = (menu) => {
  if (menu.url) {
    router.push("/sys" + menu.url);
  } else {
    Message.warning('该菜单项没有配置跳转地址');
  }
}

// 定义响应式菜单数据
const menuData = ref([]);
const loading = ref(true);

onMounted(() => {
  getAdminMenuAPI().then(res => {
    console.log(res);
    menuData.value = res;
    loading.value = false;  // 数据加载完成
  })
})

// 图标映射
const iconMap = {
  Home: HomeOutlined,
  Picture: PictureOutlined,
  User: UserOutlined,
};
</script>

<template>

  <a-layout-sider :collapsed="collapsedStore.collapsed"
                  style="background: #FFFFFF"
                  @collapse="collapsedStore.toggleCollapsed"
  >

    <div class="system_logo">
      <!--      <img src="@/assets/images/logo.png" alt="logo">-->
    </div>

    <a-menu class="menu" theme="dark" mode="inline">
      <!--左侧列表-->
      <template v-for="menu in menuData" :key="menu.id">
        <!-- 动态渲染菜单项 -->
        <a-menu-item
            v-if="menu.type === 'menu'"
            :key="menu.id"
            :url="menu.url"
            @click="onClickMenuItem(menu)"
        >
          <template #icon>
            <component :is="iconMap[menu.icon]"/>
          </template>
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
                @click="onClickMenuItem(children)"
            >
              <template #icon>
                <component :is="iconMap[children.icon]"/>
              </template>
              {{ children.name }}
            </a-menu-item>
          </template>
        </a-sub-menu>
      </template>

    </a-menu>
  </a-layout-sider>

</template>

<style scoped>

.system_logo {
  height: 64px;
  display: flex;
  justify-content: center;
  overflow: hidden;
  font-size: 15px;
  background: #9e9e9e;

  span {
    margin-top: 25px;
  }
}

.menu{
  height: calc(100vh - 64px);
}

/* 折叠状态下的样式 */
.ant-menu-inline-collapsed .menu-text,
.ant-menu-inline-collapsed .sub-menu-title {
  display: none;
}


.loading {
  width: 100%;
  height: 80%;
}

</style>