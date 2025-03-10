import {http} from "@/utils/http.js"

//查询用户信息
export const userListAPI = async () => {
    return await http.get("/user/findAll")
}

//添加用户信息


//编辑用户信息


//删除用户信息

