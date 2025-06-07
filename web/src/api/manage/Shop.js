import { http } from '@/utils/http.js'

//获取商品列表(管理端)
export const getGoodsListAPI = ({ current: current, size: size }) => {
    return http.get("/sys/shop/getProductList", { current: current, size: size })
}

//上传商品图片
export const uploadGoodImagesAPI = (formData) => {
    return http.post("/sys/shop/uploadImages", formData, {
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

//新增商品
export const addProductInfoAPI = (data, timestamp) => {
    return http.post("/sys/shop/addProductInfo", data, {
        params: {
            timestamp: timestamp
        }
    })
}

//修改商品
export const updateProductAPI = (data) => {
    return http.post("/sys/shop/updateProduct",data)
}

//删除商品
export const deleteProductAPI = (id) => {
    return http.post("/sys/shop/deleteProduct", null, {
        params: { id }
    })
}