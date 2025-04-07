import { http } from '@/utils/http.js'

//用户注册接口
export const userRegisterAPI = async ({username:username, password:password}) => {
    return http.post('/auth/register', {username: username, password: password})
}

//用户登录接口
export const userLoginAPI = async ({username: username,password: password}) => {
    return http.post('/auth/login',{username: username,password: password})
}


