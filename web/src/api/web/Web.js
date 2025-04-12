import {http} from "@/utils/http.js";

//获取轮播图列表
export const getCarouselAPI = async () => {
    return http.get("/web/getCarousel")
}

//获取非遗政策相关信息
export const getPolicyList = async () => {
    return http.get("/web/getPolicyList")
}

//根据id获取政策详细信息
export const getPolicyInfoAPI = async () => {
    return http.get("/web/getPolicyAPI")
}