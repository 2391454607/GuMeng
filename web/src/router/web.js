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
        component: ()=> import("@/views/web/pages/information/Information.vue")
    },
    {
        path: "/information/detail/:id",
        name: "InformationDetail",
        component: () => import("@/views/web/pages/information/InformationDetail.vue")
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
        component: () => import("@/views/web/pages/interaction/Interaction.vue")
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
        name: "webPolicy",
        component: () => import("@/views/web/pages/policy/Policy.vue"),
    },
    {
        path: "/policy/info",
        name: "PolicyInfo",
        component: () => import("@/views/web/pages/policy/PolicyInfo.vue")
    },
    {
        path: "/shop",
        name: "shop",
        component: () => import("@/views/web/pages/shop/Shop.vue"),
    },
    {
        path: "/shop/commodity",
        name: "Commodity",
        component: () => import("@/views/error/Error404.vue")
    },
    {
        path: '/aides',
        name: "Aides",
        component: () => import("@/views/web/pages/interaction/Aides.vue")
    },
    {
        path: '/games/space',
        name: 'GameSpace',
        component: () => import('@/views/web/pages/games/GameSpace.vue')
    },
    {
        path: '/games/puzzle',
        name: 'Puzzle',
        component: () => import('@/views/web/pages/games/Puzzle.vue')
    },
    {
        path: '/games/quiz',
        name: 'Quiz',
        component: () => import('@/views/web/pages/games/Quiz.vue')
    },
    {
        path: '/games/matching',
        name: 'Matching',
        component: () => import('@/views/web/pages/games/Matching.vue')
    },
    {
        path: '/games/map',
        name: 'MapChallenge',
        component: () => import('@/views/web/pages/games/MapChallenge.vue')
    },
    {
        path: '/games/DiffGame',
        name: 'DiffGame',
        component: () => import('@/views/web/pages/games/DiffGame.vue')
    }
];

export {webRouters};