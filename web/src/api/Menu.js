import { http } from "@/utils/http.js";

//获取用户界面菜单
export const getUserMenuAPI = async () => {
    return http.get("/user/getMenu")
}

//获取管理界面菜单
export const getAdminMenuAPI = async () => {
    return http.get("/sys/getMenu")
}