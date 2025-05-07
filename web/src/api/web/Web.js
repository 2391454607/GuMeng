import {http} from "@/utils/http.js";

//获取轮播图列表
export const getCarouselAPI = async () => {
    return http.get("/web/getCarousel")
}

//获取非遗政策相关信息
export const getPolicyList = async () => {
    return http.get("/web/policy/getList")
}

//根据id获取政策详细信息
export const getPolicyInfoAPI = async () => {
    return http.get("/web/policy/getInfo")
}

//下载文件
export const downloadPolicyAPI = async (id) => {
    return http.get(`/web/policy/download?id=${id}`)
}