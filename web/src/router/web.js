import * as path from "node:path";
import { visualizationRouters } from "./visualization.js";

const webRouters = [
    {
        path: "/",
        name: "Home",
        component: () => import("@/views/web/pages/index/Index.vue"),
    },
    {
        path: "/information",
        name: "Information",
        //component: ()=> import("@/views/web/pages/information/Information.vue")
        component: () => import("@/views/error/Error404.vue")
    },
    {
        path: "/forum",
        name: "Forum",
        component: () => import("@/views/web/pages/forum/Forum.vue"),
        children: [
            {
                path: "",
                name: "ForumHome",
                component: () => import("@/views/web/pages/forum/ForumList.vue"),
            },
            {
                path: "list",
                name: "ForumList",
                component: () => import("@/views/web/pages/forum/ForumList.vue"),
            },
            {
                path: "detail/:id",
                name: "PostDetail",
                component: () => import("@/views/web/pages/forum/PostDetail.vue"),
            },
            {
                path: "create",
                name: "CreatePost",
                component: () => import("@/views/web/pages/forum/PostCreate.vue"),
            },
            {
                path: "edit/:id",
                name: "EditPost",
                component: () => import("@/views/web/pages/forum/PostCreate.vue"),
            }
        ]
    },
    {
        path: "/interaction",
        name: "Interaction",
        //component: () => import("@/views/web/pages/interaction/Interaction.vue")
        component: () => import("@/views/error/Error404.vue")
    },
    {
        path: "/showroom",
        name: "ShowRoom",
        component: () => import("@/views/web/pages/showroom/Showroom.vue"),
    },
    {
        path: "/visualization",
        name: "Visualization",
        component: () => import("@/views/visualization/Visualization.vue"),
        children: visualizationRouters[0].children
    },
    {
        path: "/policy",
        name: "Policy",
        component: () => import("@/views/web/pages/policy/policy.vue")
    },
    {
        path: "/shop",
        name: "shop",
        //component: () => import("@/views/web/pages/shop/Shop.vue")
        component: () => import("@/views/error/Error404.vue")
    }
];

export {webRouters};