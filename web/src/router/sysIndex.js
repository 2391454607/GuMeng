
const sysRouters = [

    {
        path: "",
        name: "SysHome",
        component: () => import("@/views/manage/sys/index/Index.vue"),
    },
    {
        path: "carousel",
        name: "Carousel",
        component: () => import("@/views/manage/sys/carousel/Carousel.vue")
    },
    {
        path: "user",
        name: "User",
        component: () => import("@/views/manage/sys/user/User.vue")
    },
    {
        path: "policy",
        name: "Policy",
        component: () => import("@/views/manage/sys/policy/Policy.vue")
    }

];

export {sysRouters} ;