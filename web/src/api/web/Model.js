import {http} from "@/utils/http.js";

//获取模型列表
export const getModelList = () => {
    return http.get("/web/getModelList");
}

//获取模型详情（3d展览）
export const getModelDetail = ({modelId: modelId}) => {
    return http.get(`/web/getModel`, {modelId: modelId});
}
