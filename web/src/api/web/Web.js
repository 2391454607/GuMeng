import {http} from "@/utils/http.js";

//获取轮播图列表
export const getCarouselAPI = async () => {
    return http.get("/web/getCarousel")
}

//获取帖子话题列表
export const getPostsTopicListAPI = async () => {
    return http.get("/forum/topics")
}
//获取论坛帖子列表
export const getPostsListAPI = async () => {
    return http.get("/forum/getPosts")
}

//获取非遗政策相关信息
export const getPolicyList = async () => {
    return http.get("/web/getPolicyList")
}

//根据id获取政策详细信息
export const getPolicyInfoAPI = async () => {
    return http.get("/web/getPolicyAPI")
}