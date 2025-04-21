import * as path from "node:path";

const visualizationRouters = [
    {
        path: "",
        name: "visualizationHome",
        component: () => import("@/views/visualization/Visualization.vue"),
    },


];

export {visualizationRouters};