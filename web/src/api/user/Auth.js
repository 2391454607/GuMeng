import { http } from '@/utils/http.js'

//发送验证码
export const SendEmailApi = async ({email:email}) => {
    return http.post('/auth/sendCode', {email: email})
}

//用户注册接口
export const userRegisterAPI = async ( data ) => {
    return http.post('/auth/register', data)
}

//获取滑动验证码
export const getCaptchaAPI = async ( data ) => {
    return http.post('/auth/captcha/get', data)
}

//验证滑动验证码
export const checkCaptchaAPI = async ( data ) => {
    return http.post('/auth/captcha/check', data)
}

//用户登录接口
export const userLoginAPI = async ( data ) => {
    return http.post('/auth/login',data)
}

//忘记密码


//用户登录获取用户信息接口
export const getUserInfoAPI = async () => {
    return http.get('/user/getInfo')
}

//用户登出接口
export const userLogoutAPI = async () => {
    return http.post('/auth/logout')
}

