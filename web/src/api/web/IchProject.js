import {http} from "@/utils/http.js";


//获取非遗项目列表
export const getProjectList = async ({current: current,size: size}) => {
    return http.get("/web/ich/getProject", {current: current,size: size})
}

//获取非遗项目详情
export const getProjectDetail = async (id) => {
    return http.get(`/web/ich/getProjectDetail/${id}`);
}

