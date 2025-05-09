import {http} from "@/utils/http.js";

//获取轮播图列表
export const getCarouselAPI = async () => {
    return http.get("/web/getCarousel")
}

//获取非遗政策相关信息
export const getPolicyList = async ({current: current,size: size}) => {
    return http.get("/web/policy/getList", {current: current,size: size})
}

//根据id获取政策详细信息
export const getPolicyInfoAPI = async (id) => {
    return http.get("/web/policy/getInfo",{id:id})
}

//下载文件
export const downloadPolicyAPI = async (id) => {
    return http.get(`/web/policy/download?id=${id}`)
}