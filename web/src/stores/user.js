import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    userId: null,
    isAdmin: false,
    userInfo: null
  }),
  getters: {
    getUserId: (state) => state.userId,
    getIsAdmin: (state) => state.isAdmin,
    getUserInfo: (state) => state.userInfo,
    isLogin: (state) => !!state.userInfo
  },
  actions: {
    setUserInfo(userInfo) {
      this.userInfo = userInfo
      this.userId = userInfo?.id || null
      this.isAdmin = userInfo?.isAdmin || false
    }
  }
}) 