import {http} from "@/utils/http.js"

//获取用户列表
export const getUserList = async ({ current: current, size: size }) => {
    return await http.get("/sys/user/getUserList", { current: current, size: size })
}

//添加用户信息


//编辑用户信息


//删除用户信息

