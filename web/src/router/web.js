
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
        path: "/forum",
        name: "Forum",
        component: () => import("@/views/web/pages/forum/Forum.vue")
    },
    {
        path: "/interaction",
        name: "Interaction",
        component: () => import("@/views/web/pages/interaction/Interaction.vue")
    },
    {
        path: "/showroom",
        name: "ShowRoom",
        component: () => import("@/views/web/pages/showroom/Showroom.vue")
    },
    {
        path: "/policy",
        name: "Policy",
        component: () => import("@/views/web/pages/policy/policy.vue")
    },
    {
        path: "/encyclopedia",
        name: "Encyclopedia",
        component: () => import("@/views/web/pages/encyclopedia/Encyclopedia.vue")
    }


];

export {webRouters};