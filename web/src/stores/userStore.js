import {defineStore} from 'pinia';
import {Message} from '@arco-design/web-vue';
import {getUserInfoAPI, userLoginAPI, userLogoutAPI} from "@/api/user/Auth.js";

export const useUserStore = defineStore('user', {
    state: () => {
        const token = localStorage.getItem('token') || '';
        const store = {
            token,
            userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}'),
            dailyCleanupTimeout: null,
        };

        // 初始化时检查令牌状态
        if (token) {
            setTimeout(() => {
                const userStore = useUserStore();
                userStore.setupTokenExpirationTimer(token);
            }, 0);
        }

        return store;
    },

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

        // 获取令牌过期时间
        getTokenExpirationTime(token) {
            const decodedToken = this.parseJwt(token);
            if (decodedToken && decodedToken.exp) {
                return decodedToken.exp * 1000; // 转换为毫秒
            }
            return null;
        },

        // 设置令牌过期定时器
        setupTokenExpirationTimer(token) {
            const expirationTime = this.getTokenExpirationTime(token);
            if (!expirationTime) return;

            // 清除之前的定时器
            if (this.dailyCleanupTimeout) {
                clearTimeout(this.dailyCleanupTimeout);
                this.dailyCleanupTimeout = null;
            }

            const timeUntilExpiration = expirationTime - Date.now();
            if (timeUntilExpiration > 0) {
                this.dailyCleanupTimeout = setTimeout(() => {
                    Message.warning('登录已过期，请重新登录');
                    // 直接调用登出方法，确保完整的清理流程
                    this.logout();
                }, timeUntilExpiration);
            } else {
                // 如果令牌已经过期，立即执行登出
                Message.warning('登录已过期，请重新登录');
                this.logout();
            }
        },

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
            // 设置令牌过期定时器
            this.setupTokenExpirationTimer(token);
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
            // 清除定时器
            if (this.dailyCleanupTimeout) {
                clearTimeout(this.dailyCleanupTimeout);
                this.dailyCleanupTimeout = null;
            }

            this.token = '';
            this.userInfo = {};
            localStorage.removeItem('token');
            localStorage.removeItem('userInfo');

            // 直接跳转到首页，不使用 setTimeout
            window.location.href = '/';
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
                this.clearUserState();
            }
        },

    }
});