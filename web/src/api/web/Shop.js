import {http} from "@/utils/http.js"


//获取文创商城列表
export const getGoodsListAPI = async ({current: current,size: size}) => {
    return http.get('/web/shop/getList', {current: current, size: size})
}