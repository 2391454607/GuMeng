import { http } from '@/utils/http';

/**
 * 上传图片
 * @param {File} file 图片文件
 * @returns {Promise<Object>} 包含上传后的图片URL的对象
 */
export function uploadImage(file) {
  const formData = new FormData();
  formData.append('file', file);
  
  return http.post('/ai/image/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}

/**
 * 识别图片中的非遗内容
 * @param {string} imageUrl 图片URL
 * @returns {Promise<Object>} 识别结果
 */
export function recognizeImage(imageUrl) {
  return http.post('/ai/recognize', null, {
    params: { imageUrl }
  });
} 