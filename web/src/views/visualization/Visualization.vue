<script setup>
import { useRouter, useRoute } from 'vue-router';
import { ref, watch } from 'vue';

const router = useRouter();
const route = useRoute();

const activeTab = ref('trend');

const tabs = [
  { key: 'trend', title: '趋势分析', path: '/visualization/trend' },
  { key: 'overview', title: '数据概览', path: '/visualization/overview' },
  { key: 'content', title: '内容分析', path: '/visualization/content' },
  { key: 'relation', title: '关联分析', path: '/visualization/relation' }
];

// 根据当前路由设置激活的标签
watch(() => route.path, (newPath) => {
  const tab = tabs.find(tab => tab.path === newPath);
  if (tab) {
    activeTab.value = tab.key;
  } else if (newPath === '/visualization') {
    activeTab.value = 'trend';
  }
}, { immediate: true });

const handleTabChange = (key) => {
  const tab = tabs.find(tab => tab.key === key);
  if (tab) {
    router.push(tab.path);
  }
};
</script>

<template>
  <div class="visualization-container">
    <a-layout>
      <div class="visualization-header">
        <h2 class="page-title">大数据可视化分析</h2>
        <a-tabs
          :active-key="activeTab"
          @change="handleTabChange"
          type="rounded"
          size="large"
        >
          <a-tab-pane
            v-for="tab in tabs"
            :key="tab.key"
            :title="tab.title"
          />
        </a-tabs>
      </div>
      <a-layout-content>
        <RouterView></RouterView>

        <!--        <router-view v-slot="{ Component }">-->
<!--          <transition name="fade" mode="out-in">-->
<!--            <keep-alive>-->
<!--              <component :is="Component" />-->
<!--            </keep-alive>-->
<!--          </transition>-->
<!--        </router-view>-->
      </a-layout-content>
    </a-layout>
  </div>
</template>

<style scoped>
.visualization-container {
  min-height: calc(100vh - 64px);
  background-color: #f5f5f5;
}

.visualization-header {
  background-color: #fff;
  padding: 16px 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.page-title {
  color: #1D2129;
  margin: 0 0 16px;
  font-size: 24px;
}

:deep(.arco-layout-content) {
  padding: 20px;
}

:deep(.arco-tabs-nav-tab) {
  margin-bottom: 0;
}

:deep(.arco-tabs-tab) {
  font-size: 16px;
  padding: 8px 24px;
}

:deep(.arco-tabs-tab-active) {
  color: #C2101C;
}

:deep(.arco-tabs-ink-bar) {
  background-color: #C2101C;
}

/* 页面切换动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>