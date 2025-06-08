import {http} from "@/utils/http.js";

//获取模型列表
export const getModelList = () => {
    return http.get("/web/getModelList");
}

//获取模型详情（3d展览）
export const getModelDetail = ({modelId: modelId}) => {
    return http.get(`/web/getModel`, {modelId: modelId});
}

//查询所有模型生成信息
export const getModelInfo = () => {
    return http.get("/model/getModelInfo");
}

//查询账户余额
export const getBalance = () => {
    return http.get("/model/balance");
}

//文本生成模型
export const textToModel = (prompt) => {
    return http.post("/model/text-to-model", prompt )
}

//图片生成模型
//上传图片
export const uploadSts = (formData) => {
    return http.post("/model/uploadSts",formData,{
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    });
}
//提交图片生成模型请求
export const imageToModel = (data) => {
    return http.post("/model/image-to-model", data);
}

//模型生成状态查询
export const taskPolling = ({id: taskId}) => {
    return http.get("/model/polling",{id: taskId});
}

//模型状态查询并保存
export const taskText = ({id: taskId}) => {
    return http.get("/model/save",{id: taskId});
}

