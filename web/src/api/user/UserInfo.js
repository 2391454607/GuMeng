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

//获取用户积分流动信息
export const getPointLogAPI = async ({current: current,size: size}) => {
    return http.get('/user/getPointLog',{current: current,size: size})
}

//获取用户余额流动信息
export const getBalanceLogAPI = async ({current: current,size: size}) => {
    return http.get('/user/getBalanceLog', {current: current,size: size})
}
