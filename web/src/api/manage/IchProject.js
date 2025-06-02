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

// 单独上传视频
export const uploadVideoAPI = async (file, projectId, onProgress) => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('projectId', projectId);
    return http.post('/sys/uploadVideo', formData, {
        headers: {
            'Content-Type': 'multipart/form-data'
        },
        timeout: 600000, // 增加超时时间为10分钟
        onUploadProgress: (progressEvent) => {
            const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total);
            console.log(`上传进度: ${percentCompleted}%`);
            // 调用进度回调函数
            if (onProgress && typeof onProgress === 'function') {
                onProgress(percentCompleted);
            }
        }
    });
}

// 添加删除文件的API函数
export const deleteFileAPI = async (url) => {
    return http.get(url);
}