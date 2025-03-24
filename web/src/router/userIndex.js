
const userRouters = [
    {
        path: "/",
        name: "Home",
        component: () => import("@/views/user/pages/index/Index.vue"),
    },
    {
        path: "/information",
        name: "Information",
        component: ()=> import("@/views/user/pages/information/Information.vue")
    },


];

export {userRouters};