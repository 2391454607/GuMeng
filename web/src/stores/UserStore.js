import { defineStore } from 'pinia';

export const useUserStore = defineStore('user', {
    state: () => ({
        username: '',
        role: '',       // 用户角色: 'user' 或 'admin'
        token: '',      // 认证token
        permissions: [], // 用户权限列表
        // 其他用户信息...
    }),
    actions: {
        // 设置用户信息
        setUserInfo(userData) {
            this.username = userData.username;
            this.role = userData.role;
            this.token = userData.token;
            this.permissions = userData.permissions || [];
            // 可以设置其他用户信息
        },
        // 清除用户信息(退出登录时使用)
        clearUser() {
            this.$reset();
            localStorage.removeItem('rememberedUser');
        },
        // 检查是否有权限
        hasPermission(permission) {
            return this.permissions.includes(permission);
        }
    },
    persist: true // 开启持久化，需要安装 pinia-plugin-persist
});