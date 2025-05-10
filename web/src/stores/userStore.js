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

        // 静默登出方法
        async silentLogout() {
            this.clearUserState();
            Message.error('登录已过期，请重新登录');
        },

        // 检查用户认证状态
        async checkAuthStatus() {
            if (!this.token) return;
            
            try {
                const res = await getUserInfoAPI();
                if (res.code !== 200) {
                    await this.silentLogout();
                }
            } catch (error) {
                console.error('验证用户状态失败:', error);
                await this.silentLogout();
            }
        },

        // 初始化用户状态
        async initializeAuth() {
            await this.checkAuthStatus();
            this.setupDailyCleanup();
        },

        // 设置每日清理
        setupDailyCleanup() {
            const now = new Date();
            const nextCleanup = new Date(now);

            nextCleanup.setHours(4, 0, 0, 0);
            if (now.getHours() >= 4) {
                nextCleanup.setDate(nextCleanup.getDate() + 1);
            }

            const timeUntilCleanup = nextCleanup - now;
            console.log('下次清理时间：', nextCleanup.toLocaleString());

            if (this.dailyCleanupTimeout) {
                clearTimeout(this.dailyCleanupTimeout);
            }

            this.dailyCleanupTimeout = setTimeout(() => {
                // 检查用户活跃状态
                const lastActivity = localStorage.getItem('lastActivityTime');
                const isActive = lastActivity && (Date.now() - parseInt(lastActivity) < 30 * 60 * 1000); // 30分钟内有活动

                if (isActive) {
                    Message.warning({
                        content: '系统将在 5 分钟后自动退出登录，请及时保存工作',
                        duration: 300000 // 5分钟
                    });

                    setTimeout(() => {
                        this.silentLogout();
                        window.location.href = '/'; // 重定向到首页
                    }, 300000);
                } else {
                    this.silentLogout();
                    window.location.href = '/';
                }

                this.setupDailyCleanup();
            }, timeUntilCleanup);
        },

        // 清理 clearUserState 方法
        clearUserState() {
            this.token = '';
            this.userInfo = {};
            localStorage.removeItem('token');
            localStorage.removeItem('userInfo');

            // 清理定时器
            if (this.dailyCleanupTimeout) {
                clearTimeout(this.dailyCleanupTimeout);
                this.dailyCleanupTimeout = null;
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

        // 更新用户活跃时间
        updateActivityTime() {
            localStorage.setItem('lastActivityTime', Date.now().toString());
        },

        async login(loginParams) {
            try {
                const res = await userLoginAPI(loginParams);
                if (res.code === 200) {
                    this.setToken(res.data);
                    this.updateActivityTime(); // 登录时记录活跃时间

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