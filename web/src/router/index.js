import {createRouter, createWebHistory} from "vue-router";
import { useUserStore } from "@/stores/userStore";
import WebHome from "@/views/web/Home.vue";
import {webRouters} from "@/router/web.js"
import SysHome from "@/views/manage/Home.vue"
import {sysRouters} from "@/router/sysIndex.js"
import VisualizationHome from "@/views/visualization/Visualization.vue"
import {visualizationRouters} from "@/router/visualization.js"
import UserInfo from "@/views/userInfo/UserInfo.vue"
import {userInfoRouters} from "@/router/userInfo.js";

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
         * @description 数据可视化路由
         */
        {
            path: "/visualization",
            name: "visualization",
            component: VisualizationHome,
            children: visualizationRouters,
            meta: {
                requiresAuth: false,
                //roles: ['admin', 'superAdmin'] // 允许的角色
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
            component: UserInfo,
            children: userInfoRouters,
            meta: { requiresAuth: true } // 需要登录
        },
        /**
         * @description 3D界面
         */
        {
            path: "/show3D",
            name: "Show3D",
            component: () => import("@/views/web/pages/showroom/Show3D.vue"),
            meta: { requiresAuth: false } // 不需要登录
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
        },
        /**
         * @description 404资源不存在
         * 界面
         */
        {
            path: "/404",
            name: "404",
            component: () => import("@/views/error/Error404.vue")
        }
    ]
})


/**
 * @description 全局路由守卫
 */
router.beforeEach(async (to, from, next) => {
    const userStore = useUserStore();
    const redirectPath = await userStore.handleRouteGuard(to);

    if (redirectPath === true) {
        next();
    } else {
        next(redirectPath);
    }
});
