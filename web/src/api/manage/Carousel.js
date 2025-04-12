import { http } from "@/utils/http.js"

//添加轮播图
export const addCarouselAPI = async ({imageUrl:imageUrl}) => {
    return http.post("/sys/addCarousel",{imageUrl:imageUrl})
}

//修改
export const updateCarouselAPI = async ({id:id, imageUrl:imageUrl}) => {
    return http.post("/sys/updateCarousel",{id:id, imageUrl:imageUrl})
}

//删除
export const deleteCarouselAPI = async ({id:id}) => {
    return http.post("/sys/deleteCarousel",{id:id})
}