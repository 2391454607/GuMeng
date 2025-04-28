import { http } from '@/utils/http.js'


//获取用户资产
export const getUserAssetAPI = async () => {
    return http.get('/user/getAsset')
}

//用户签到
export const dailySignAPI = async () => {
    return http.post('/user/dailySign')
}

//用户充值
export const rechargeAPI = async ( amount ) => {
    return http.post('/user/recharge', amount)
}

//用户提现
export const withdrawAPI = async ( amount ) => {
    return http.post('/user/withdraw', amount)
}

//获取用户资产流动信息
export const getAssetLogoAPI = async () => {
    return http.get('/user/getAssetLog')
}