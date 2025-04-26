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
    // {
    //     path: "",
    //     name: "",
    //     component: () => import("")
    // },
    {
        path: "settings",
        name: "settings",
        component: () => import("@/views/userInfo/info/Settings.vue")
    }
];

export {userInfoRouters};