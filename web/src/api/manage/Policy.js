import {http} from "@/utils/http.js"

//获取非遗列表
export const getPolicyListAPI = ({current: current,size: size}) => {
    return http.get("/sys/policy/getList",{current: current,size: size})
}

//新增非遗政策信息
export const addPolicyAPI = (data) => {
    return http.post("/sys/policy/upload",data)
}

//更新非遗政策信息
export const updatePolicyAPI = async (data) => {
    return http.post("/sys/policy/update",data)
}

//删除政策信息
export const deletePolicyAPI = (id) => {
    return http.post("/sys/policy/deletePolicy",id,{
        params: { id }
    })
}