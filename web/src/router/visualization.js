import * as path from "node:path";

const visualizationRouters = [
    {
        path: "",
        name: "visualizationHome",
        component: () => import("@/views/visualization/Visualization.vue"),
        children: [
            {
                path: "",
                name: "Trend",
                component: () => import("@/views/visualization/pages/Trend.vue")
            },
            {
                path: "overview",
                name: "Overview",
                component: () => import("@/views/visualization/pages/Overview.vue")
            },
            {
                path: "content",
                name: "Content",
                component: () => import("@/views/visualization/pages/Content.vue")
            },
            {
                path: "relation",
                name: "Relation",
                component: () => import("@/views/visualization/pages/Relation.vue")
            }
        ]
    }
];

export {visualizationRouters};