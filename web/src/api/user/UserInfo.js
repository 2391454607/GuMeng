import { http } from '@/utils/http.js'


//获取用户资产
export const getUserAssetAPI = async () => {
    return http.get('/user/getAsset')
}

//用户签到
export const dailySignAPI = async () => {
    return http.post('/user/dailySign')
}