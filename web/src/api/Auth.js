import { http } from '@/utils/http.js'

//发送验证码
export const SendEmailApi = async ({email:email}) => {
    return http.post('/auth/sendCode', {email: email})
}

//用户注册接口
export const userRegisterAPI = async ( data ) => {
    return http.post('/auth/register', data)
}

//用户登录接口
export const userLoginAPI = async ({username: username,password: password}) => {
    return http.post('/auth/login',{username: username,password: password})
}


