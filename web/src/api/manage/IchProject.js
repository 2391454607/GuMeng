import {http} from "@/utils/http.js"

//获取非遗项目
export const getIchProjectAPI = async ({current: current,size: size,levelId: levelId,categoryId: categoryId}) => {
    return http.get('/sys/getProject',{current: current,size: size,levelId: levelId,categoryId: categoryId})
}

//新增非遗项目
export const addProjectAPI = async (formData) => {
    return http.post('/sys/addProject',formData)
}

//修改非遗项目
export const updateProjectAPI = async (formData) => {
    return http.post('/sys/updateProject',formData)
}

//删除
export const deleteProjectAPI = async (id) => {
    return http.post(`/sys/deleteProject?${id}`)
}