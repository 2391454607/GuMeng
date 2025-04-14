import {createRouter, createWebHistory} from "vue-router";
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
        },
        /**
         * @description 管理端路由
         */
        {
            path: "/sys",
            name: "sys",
            component: SysHome,
            children: sysRouters,
        },
        {
            path: "/login",
            name: "Login",
            component: () => import("@/views/login/Login.vue"),
        },
        {
            path: "/register",
            name: "Register",
            component: () => import("@/views/login/Register.vue")
        }

    ]
})

/**
 * @description 全局路由守卫
 */
router.beforeEach((to, from, next) => {
    next()
})