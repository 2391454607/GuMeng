import { http } from "@/utils/http.js";

//获取管理界面菜单
export const getAdminMenuAPI = async () => {
    return http.get("/sys/getSysMenu")
}