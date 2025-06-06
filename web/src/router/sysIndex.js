
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
    },
    {
        path: "shop",
        name: "Shop",
        component: () => import("@/views/manage/sys/shop/Shop.vue")
    },
    {
        path: "forum",
        name: "sysForum",
        component: () => import("@/views/manage/sys/forum/Forum.vue")
    },
    {
        path: "comments",
        name: "Comments",
        component: () => import("@/views/manage/sys/forum/Comments.vue")
    },
    {
        path: "project",
        name: "Project",
        component: () => import("@/views/manage/sys/project/Project.vue")
    },
    {
        path: 'log',
        name: 'SysLog',
        component: () => import('@/views/manage/sys/LogManage.vue'),
        meta: {
            requiresAuth: true,
            roles: ['admin', 'superAdmin']
        }
    },

];

export {sysRouters} ;