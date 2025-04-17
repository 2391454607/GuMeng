import {createRouter, createWebHistory} from "vue-router";
import { Message } from '@arco-design/web-vue';
import WebHome from "@/views/web/Home.vue";
import {webRouters} from "@/router/web.js"
import SysHome from "@/views/manage/Home.vue"
import {sysRouters} from "@/router/sysIndex.js"

/**
 * @description 导出路由配置
 * @type {Router}
 */
export const router = createRouter({
    history: createWebHistory(),
    routes: [
        /**
         * @description 用户端路由
         */
        {
            path: "/",
            name: "web",
            component: WebHome,
            children: webRouters,
            meta: { requiresAuth: false } // 不需要登录
        },
        /**
         * @description 管理端路由
         */
        {
            path: "/sys",
            name: "sys",
            component: SysHome,
            children: sysRouters,
            meta: {
                requiresAuth: true,  // 需要登录
                roles: ['admin', 'superAdmin'] // 允许的角色
            }
        },
        /**
         * @description 登录界面
         */
        {
            path: "/login",
            name: "Login",
            component: () => import("@/views/auth/Login.vue"),
            meta: { requiresAuth: false } // 不需要登录
        },
        /**
         * @description 注册界面
         */
        {
            path: "/register",
            name: "Register",
            component: () => import("@/views/auth/Register.vue"),
            meta: { requiresAuth: false } // 不需要登录
        },
        /**
         * @description 用户个人信息界面
         */
        {
            path: "/userInfo",
            name: "UserInfo",
            component: () => import("@/views/auth/UserInfo.vue"),
            meta: { requiresAuth: true } // 需要登录
        },
        /**
         * @description 401未认证或token无效界面
         */
        {
            path: "/401",
            name: "401",
            component: () => import("@/views/error/Error401.vue")
        },
        /**
         * @description 403权限不足
         * 界面
         */
        {
            path: "/403",
            name: "403",
            component: () => import("@/views/error/Error403.vue")
        }
    ]
})

// 添加 JWT 解析函数
function parseJwt(token) {
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        return JSON.parse(window.atob(base64));
    } catch (error) {
        console.error('Token解析失败:', error);
        return null;
    }
}

/**
 * @description 全局路由守卫
 */
router.beforeEach(async (to, from, next) => {
    const token = localStorage.getItem('token');
    
    if (to.meta.requiresAuth) {
        if (!token) {
            Message.warning('请先登录');
            next('/401');
            return;
        }

        // 从 token 中解析用户信息
        const decodedToken = parseJwt(token);
        if (!decodedToken || !decodedToken.claims) {
            Message.error('无效的用户信息');
            localStorage.removeItem('token');
            next('/401');
            return;
        }

        const userRole = decodedToken.claims.role || [];

        if (to.meta.roles && !to.meta.roles.some(role => userRole.includes(role))) {
            Message.error('权限不足');
            next('/403');
        } else {
            next();
        }
    } else {
        next();
    }
});