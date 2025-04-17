// 统一导出所有store
import { defineStore } from 'pinia';

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}')
  }),
  
  getters: {
    isLogin: (state) => {
      const hasToken = !!state.token;
      const hasUserInfo = state.userInfo && Object.keys(state.userInfo).length > 0;
      const isLoggedIn = hasToken && hasUserInfo;
      console.log('isLogin检查 - token存在:', hasToken, '- token值:', state.token);
      console.log('isLogin检查 - userInfo存在:', hasUserInfo, '- userInfo:', state.userInfo);
      return isLoggedIn;
    },
    isAdmin: (state) => {
      if (!state.userInfo || !state.userInfo.roles) return false;
      return state.userInfo.roles.some(role => role === 'admin' || role === 'superAdmin');
    }
  },
  
  actions: {
    setToken(token) {
      this.token = token;
      localStorage.setItem('token', token);
      console.log('Token已设置:', token);
    },
    
    setUserInfo(userInfo) {
      this.userInfo = userInfo;
      localStorage.setItem('userInfo', JSON.stringify(userInfo));
      console.log('UserInfo已设置:', userInfo);
    },
    
    logout() {
      this.token = '';
      this.userInfo = {};
      localStorage.removeItem('token');
      localStorage.removeItem('userInfo');
      console.log('用户已登出');
    }
  }
}); 