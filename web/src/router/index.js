import {createRouter, createWebHistory} from "vue-router";
import Home from "@/views/web/Home.vue";
import {userRouters} from "@/router/web.js"
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
            name: "index",
            component: Home,
            children: userRouters,
        },
        /**
         * @description 管理端路由
         */
        {
            path: "/sys",
            name: "system",
            component: SysHome,
            children: sysRouters,
        },
        {
            path: "/login",
            name: "Login",
            component: () => import("@/views/login/Login.vue"),
        },

    ]
})

/**
 * @description 全局路由守卫
 */
router.beforeEach((to, from, next) => {
    next()
})