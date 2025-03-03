<script setup>
import { MenuFoldOutlined, MenuUnfoldOutlined } from '@ant-design/icons-vue';
import { computed } from 'vue';
import { useCollapsedStore } from '@/stores/index.js'; // 引入 Pinia Store
import { UserOutlined } from "@ant-design/icons-vue";

const collapsedStore = useCollapsedStore();
const collapsed = computed(() => collapsedStore.collapsed); // 使用 computed 确保响应性

const toggleCollapsed = () => {
  collapsedStore.toggleCollapsed(); // 切换 collapsed 状态
};

console.log('collapsed:', collapsed.value); // 在 Header 组件中
</script>

<template>
  <a-layout-header style="background: #fff; padding: 0; display: flex; justify-content: space-between; align-items: center;">
    <div>
      <menu-unfold-outlined
          v-if="collapsed"
          class="trigger"
          @click="toggleCollapsed"
      />
      <menu-fold-outlined v-else class="trigger" @click="toggleCollapsed" />
    </div>
    <a-space class="avatar" direction="vertical" :size="32">
      <a-space wrap :size="16">
        <a-avatar size="large">
          <template #icon><UserOutlined /></template>
        </a-avatar>
      </a-space>
    </a-space>
  </a-layout-header>
</template>

<style scoped>
.trigger {
  font-size: 18px;
  line-height: 64px;
  padding: 0 24px;
  cursor: pointer;
  transition: color 0.3s;
}

.trigger:hover {
  color: #1890ff;
}

.avatar {
  margin-right: 64px; /* 调整头像与右侧的距离 */
}
</style>