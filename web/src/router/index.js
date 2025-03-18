import { createRouter, createWebHistory } from "vue-router";
import Home from "@/views/user/Home.vue";

const routes = [
    {
        path: "",
        name: "Home",
        component: Home,
        children: [
            {
                path: "",
                name: "Index",
                component: () => import("@/views/user/pages/index/Index.vue"),
                meta: {
                    auth: true,
                    role: "index"
                }
            },
            {
                path: "/information",
                name: "Information",
                component: ()=> import("@/views/user/pages/information/Information.vue")
            },
        ]
    },
    {
        path: "/login",
        name: "Login",
        component: () => import("@/views/login/Login.vue"),
    },

];

const router =createRouter({
    history: createWebHistory(),
    routes,
});

export default router;