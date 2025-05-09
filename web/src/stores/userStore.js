import { defineStore } from 'pinia';
import { Message } from '@arco-design/web-vue';
import {getUserInfoAPI, userLoginAPI, userLogoutAPI} from "@/api/user/Auth.js";

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}'),
    isAdminRole: false,
  }),

  getters: {
    isLogin: (state) => {
      const hasToken = !!state.token;
      const hasUserInfo = state.userInfo && Object.keys(state.userInfo).length > 0;
      return hasToken && hasUserInfo;
    },
    isAdmin: (state) => {
      const decodedToken = state.token ? this.parseJwt(state.token) : null;
      return decodedToken?.claims?.role?.includes('admin') || decodedToken?.claims?.role?.includes('superAdmin') || false;
    },
    userRoles: (state) => {
      const decodedToken = state.token ? this.parseJwt(state.token) : null;
      return decodedToken?.claims?.role || [];
    },
  },

  actions: {
    setToken(token) {
      this.token = token;
      localStorage.setItem('token', token);
    },

    setUserInfo(userInfo) {
      this.userInfo = userInfo;
      // 更新管理员状态检查
      this.isAdminRole = userInfo?.claims?.role?.some(role => role === 'admin' || role === 'superAdmin') || false;
      localStorage.setItem('userInfo', JSON.stringify(userInfo));
    },


    parseJwt(token) {
      try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        return JSON.parse(window.atob(base64));
      } catch (error) {
        console.error('Token解析失败:', error);
        return null;
      }
    },

    checkTokenExpiration(token) {
      const decodedToken = this.parseJwt(token);
      if (!decodedToken || !decodedToken.exp) return true;
      const currentTime = Math.floor(Date.now() / 1000);
      return decodedToken.exp < currentTime;
    },

    // 路由守卫逻辑
    async handleRouteGuard(to) {
      if (to.meta.requiresAuth) {
        if (!this.token) {
          Message.warning('请先登录');
          return '/';
        }

        if (this.checkTokenExpiration(this.token)) {
          Message.error('登录已过期，请重新登录');
          this.logout();
          return '/';
        }

        const decodedToken = this.parseJwt(this.token);
        if (!decodedToken || !decodedToken.claims) {
          Message.error('无效的用户信息');
          this.logout();
          return '/';
        }

        // 更新路由权限检查逻辑
        if (to.meta.roles) {
          const userRoles = decodedToken.claims.role || [];
          if (!to.meta.roles.some(role => userRoles.includes(role))) {
            Message.error('权限不足');
            return '/403';
          }
        }
      }
      return true;
    },

    async login(loginParams) {
      try {
        const res = await userLoginAPI(loginParams);
        if (res.code === 200) {
          this.setToken(res.data);
          
          const decodedToken = this.parseJwt(res.data);
          if (decodedToken && decodedToken.claims) {
            this.setUserInfo(decodedToken.claims);
            
            // 根据角色决定跳转路径
            const userRoles = decodedToken.claims.role || [];
            const isAdmin = userRoles.some(role => ['admin', 'superAdmin'].includes(role));
            
            Message.success('登录成功');
            return {
              success: true,
              isAdmin
            };
          }
        }
        Message.error(res.msg || '登录失败');
        return {
          success: false,
          isAdmin: false
        };
      } catch (error) {
        console.error('登录失败:', error);
        Message.error('登录失败，请稍后重试');
        return {
          success: false,
          isAdmin: false
        };
      }
    },

    async logout() {
      try {
        const res = await userLogoutAPI();
        if (res.code === 200) {
          // 清除状态
          this.token = '';
          this.userInfo = {};
          // 清除本地存储
          localStorage.removeItem('token');
          localStorage.removeItem('userInfo');
          Message.success('退出登录成功');

          // 延时刷新页面
          setTimeout(() => {
            window.location.reload();
          }, 1000);

          return true;
        } else {
          Message.error(res.msg || '退出登录失败');
          return false;
        }
      } catch (error) {
        console.error('退出登录错误:', error);
        Message.error('退出登录失败');
        return false;
      }
    },

    async fetchUserInfo() {
      try {
        const token = localStorage.getItem('token');
        if (!token) return null;

        const res = await getUserInfoAPI();
        if (res.code === 200) {
          this.setUserInfo(res.data);
          return res.data;
        } else {
          Message.error('获取用户信息失败');
          return null;
        }
      } catch (error) {
        console.error('获取用户信息错误:', error);
        Message.error('获取用户信息失败');
        return null;
      }
    }
  }
});