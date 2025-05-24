import * as path from "node:path";

const userInfoRouters = [
    {
        path: "",
        name: "info",
        component: () => import("@/views/userInfo/info/Info.vue")
    },
    {
        path: "order",
        name: "order",
        component: () => import("@/views/userInfo/info/Order.vue")
    },
    {
        path: "collection",
        name: "collection",
        component:() => import("@/views/userInfo/info/Collection.vue")
    },
    {
        path: "posts",
        name: "posts",
        component:() => import("@/views/userInfo/info/Posts.vue")
    },
    {
        path: "history",
        name: "history",
        component:() => import("@/views/userInfo/info/History.vue")
    },
    {
        path: "assetFlows",
        name: "assetFlows",
        component: () => import("@/views/userInfo/info/AssetFlows.vue")
    },
    {
        path: "settings",
        name: "settings",
        component: () => import("@/views/userInfo/info/Settings.vue")
    }
];

export {userInfoRouters};