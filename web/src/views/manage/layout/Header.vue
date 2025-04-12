<script setup>
import {ref} from "vue";
import {router} from "@/router/index.js";
import {IconCaretLeft, IconCaretRight, IconHome} from "@arco-design/web-vue/es/icon";

const props = defineProps({
  coll: {
    type: Function
  },
  collapsed: {
    type: Boolean,
    required: true
  },
  onClickButton: Function
})
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
      <a-avatar :size="40" shape="square" :image-url="1"></a-avatar>
      <a-dropdown>
        <template #content>
          <a-doption>退出登录</a-doption>
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

.header-left {
  display: flex;
  align-items: center;
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