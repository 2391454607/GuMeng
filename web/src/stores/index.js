import { defineStore } from 'pinia';

export const useCollapsedStore = defineStore('collapsed', {
    state: () => ({
        collapsed: false, // 定义 collapsed 状态
    }),
    actions: {
        toggleCollapsed() {
            this.collapsed = !this.collapsed; // 切换 collapsed 状态
        },
    },
});