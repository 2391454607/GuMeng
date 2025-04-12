
//获取轮播图列表
import {http} from "@/utils/http.js";


export const getCarouselAPI = async () => {
    return http.get("/web/getCarousel")
}