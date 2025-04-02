
const userRouters = [
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


];

export {userRouters};