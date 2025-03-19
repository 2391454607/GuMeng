import { http } from '@/utils/http.js'

//用户注册接口

//用户登录接口
export const userLoginAPI = async ({username: username,password: password}) => {
    return http.post('/user/login',{username: username,password: password})
}

//管理员登录接口
export const adminLoginAPI = async ({name: name,password: password}) => {
    return http.post('/admin/login',{name: name,password: password})
}

