import { createRouter, createWebHistory } from "vue-router";
import Home from "@/views/home/Home.vue";

const routes = [
    {
        path: "/",
        name: "Home",
        component: Home,
    },
    {
        path: "/login",
        name: "Login",
        component: () => import("@/views/login/Login.vue"),
    },
    {
        path: "/about",
        name: "About",
        component: () => import("@/views/About.vue"),
    },
    {
        path: "/manage",
        name: "Manage",
        component: () => import("@/views/manage/Home.vue"),
        children: [
            {
                path: "/user",
                name: "User",
                component:() => import("@/views/manage/User.vue")
            }
        ]
    }
];

const router =createRouter({
    history: createWebHistory(),
    routes,
});

export default router;