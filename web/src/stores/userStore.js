import {defineStore} from 'pinia';
import {Message} from '@arco-design/web-vue';
import {getUserInfoAPI, userLoginAPI, userLogoutAPI} from "@/api/user/Auth.js";

export const useUserStore = defineStore('user', {
    state: () => ({
        token: localStorage.getItem('token') || '',
        userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}'),
        dailyCleanupTimeout: null, // 添加定时器引用
    }),

    getters: {
        // 通用的角色判断方法
        isUserInRoles: (state) => (allowedRoles) => {
            const userRoles = state.userInfo?.role || [];
            return userRoles.some(role => allowedRoles.includes(role));
        },

        isAdmin: (state) => {
            return state.isUserInRoles(['admin', 'superAdmin']);
        },
    },

    actions: {

        // setUserInfo 方法
        setUserInfo(userInfo) {
            this.userInfo = userInfo;
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

        // 路由守卫逻辑
        async handleRouteGuard(to) {
            if (to.meta.requiresAuth) {
                if (!this.token) {
                    Message.warning('请先登录');
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

        // setToken 方法
        setToken(token) {
            this.token = token;
            localStorage.setItem('token', token);

            const decodedToken = this.parseJwt(token);
            if (decodedToken?.claims?.role) {
                this.setUserInfo(decodedToken.claims);
            }
        },

        // 获取用户信息
        async fetchUserInfo() {
            try {
                const token = localStorage.getItem('token');
                if (!token) return null;

                const res = await getUserInfoAPI();
                if (res.code === 200) {
                    this.setUserInfo(res.data);
                    // 解析 token 并更新角色信息
                    const decodedToken = this.parseJwt(token);
                    if (decodedToken?.claims?.role) {
                        this.userInfo.role = decodedToken.claims.role;
                    }
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
        },

        // 清理 clearUserState 方法
        clearUserState() {
            this.token = '';
            this.userInfo = {};
            localStorage.removeItem('token');
            localStorage.removeItem('userInfo');
        },

        // 用户登出方法
        async logout() {
          try {
            const res = await userLogoutAPI();
            if (res.code === 200) {
              Message.success('退出登录成功');
            } else {
              console.warn('后端登出失败，仅清理本地状态');
            }
          } catch (error) {
            console.error('退出登录错误:', error);
          } finally {
            // 无论后端是否成功，都清理本地状态
            this.clearUserState();
            
            // 延时刷新页面
            setTimeout(() => {
              window.location.reload();
            }, 1000);
          }
        },

    }
});