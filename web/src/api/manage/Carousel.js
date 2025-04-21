import { http } from "@/utils/http.js"

//获取轮播图列表
export const getCarouselAPI = async () => {
    return http.get("/web/getCarousel")
}

//添加轮播图
export const addCarouselAPI = async ({imageUrl:imageUrl}) => {
    return http.post("/sys/addCarousel",{imageUrl:imageUrl})
}

//修改
export const updateCarouselAPI = async (data) => {
    return http.post("/sys/updateCarousel",data)
}

//删除
export const deleteCarouselAPI = async ( {id:id} ) => {
    return http.post("/sys/deleteCarousel",{id:id})
}