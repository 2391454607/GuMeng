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

/**
 * 上传单个图片
 */
export function uploadImageAPI(file) {
  // 处理单个文件上传
  if (file instanceof File) {
    const formData = new FormData();
    formData.append('file', file);
    
    return http.post('/sys/file/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
  }
  
  // 处理FormData
  if (file instanceof FormData) {
    return http.post('/sys/file/upload', file, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
  }
  
  // 其他情况
  console.error('uploadImageAPI 参数错误:', file);
  return Promise.reject(new Error('上传参数错误'));
}

/**
 * 批量上传图片
 */
export function batchUploadImagesAPI(files) {
  if (!files || files.length === 0) {
    return Promise.reject(new Error('未选择文件'));
  }
  
  const formData = new FormData();
  for (let i = 0; i < files.length; i++) {
    formData.append('files', files[i]);
  }
  
  return http.post('/sys/file/batch-upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}

// 单独上传视频
export const uploadVideoAPI = async (file, projectId = '', onProgress) => {
    // 处理文件上传
    const formData = new FormData();
    
    if (file instanceof File) {
        formData.append('file', file);
    } else if (file instanceof FormData) {
        return http.post('/sys/file/uploadVideo', file, {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        });
    } else {
        return Promise.reject(new Error('参数错误'));
    }
    
    // 添加项目ID
    if (projectId) {
        formData.append('projectId', projectId);
    }
    
    // 配置请求
    const config = {
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    };
    
    if (onProgress) {
        config.onUploadProgress = progressEvent => {
            const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total);
            console.log(`视频上传进度: ${percentCompleted}%`);
            onProgress(percentCompleted);
        };
    }
    
    return http.post('/sys/file/uploadVideo', formData, config);
}

/**
 * 删除文件
 */
export function deleteFileAPI(fileName) {
  if (fileName.startsWith('/')) {
    return http.get(fileName);
  }

  return http.delete(`/sys/file/delete?fileName=${encodeURIComponent(fileName)}`);
}

/**
 * 批量删除文件
 */
export function batchDeleteFilesAPI(fileNames) {
  if (!fileNames || fileNames.length === 0) {
    return Promise.reject(new Error('未提供需要删除的文件'));
  }
  
  return http.delete('/sys/file/batch-delete', {
    data: { fileNames }
  });
}