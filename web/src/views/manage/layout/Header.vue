<script setup>
import {ref, onMounted} from "vue";
import {router} from "@/router/index.js";
import {Message} from '@arco-design/web-vue';
import {IconCaretLeft, IconCaretRight, IconHome, IconUser, IconExport} from "@arco-design/web-vue/es/icon";
import {getUserInfoAPI} from "@/api/user/Auth.js";
import {useUserStore} from "@/stores/userStore.js";

const props = defineProps({
  coll: {
    type: Function
  },
  collapsed: {
    type: Boolean,
    required: true
  }
})

// 用户信息
const userInfo = ref({
  id: "",
  nickname: '',
  userPic: ''
});

onMounted(() => {
  try {
    const token = localStorage.getItem('token');
    if (token){
      getUserInfoAPI().then(res => {
        console.log(res.data);
        if (res.code === 200) {
          userInfo.value = res.data;
          // 更新本地存储的用户信息
          localStorage.setItem('userInfo', JSON.stringify(res.data));
        }else {
          Message.error('获取用户信息失败');
        }
      });
    }
  }catch(error) {
    console.error('获取用户信息错误:', error);
    Message.error('获取用户信息失败');
  }
});

// 获取 store 实例
const userStore = useUserStore();

// 退出登录
const handleLogout = () => {
  const token = localStorage.getItem('token');
  if (token) {
    userStore.logout();
  } else {
    Message.warning('您尚未登录');
    router.push('/login');
  }
};
</script>

<template>
  <a-layout-header class="header">
    <a-button shape="round" @click="props.coll" class="collapse-btn">
      <IconCaretRight v-if="props.collapsed"/>
      <IconCaretLeft v-else/>
    </a-button>

    <a-space size="large" class="header-right">
      <a-button type="text" class="e-button" @click="router.push('/')">
        <IconHome :size="25"/>
        首页
      </a-button>
      <a-dropdown trigger="hover">
        <a-space>
          <a-avatar 
            :size="40" 
            :image-url="userInfo.userPic || '/src/assets/image/gumeng.png'"
          >
            <template #fallback>
              <IconUser />
            </template>
          </a-avatar>
          <span>{{ userInfo.nickname || '用户' }}</span>
        </a-space>
        <template #content>
          <a-doption @click="router.push('/userInfo')">
            <IconUser />个人信息
          </a-doption>
          <a-doption @click="handleLogout">
            <IconExport />退出登录
          </a-doption>
        </template>
      </a-dropdown>
    </a-space>
  </a-layout-header>
</template>

<style scoped>
.header {
  background: #fff;
  height: 64px;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0;
  z-index: 99;
  transition: all 0.3s ease-in-out;
}


.header-right {
  margin-right: 16px;
}

.collapse-btn {
  margin-left: 16px;
  transition: transform 0.3s ease-in-out;

  &:hover {
    transform: scale(1.1);
  }
}

.e-button {
  transition: all 0.3s ease-in-out;
}

:deep(.arco-btn-icon) {
  transition: transform 0.3s ease-in-out;
}
</style>