import axios from "axios";

/**
 * @description axios基础配置
 * @type {axios.AxiosInstance}
 */
const request = axios.create({
    baseURL: "/api",
    timeout: 30000,
    headers: {
        "Content-Type": "application/json;charset=UTF-8",
        'Accept': 'application/json',
    },
    responseType: "json",
    withCredentials: true,
});



/**
 * @description axios请求拦截器
 */
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
      // 如果是 FormData，则删除 Content-Type（让浏览器自动生成 boundary）
      if (config.data instanceof FormData) {
          delete config.headers['Content-Type'];
      }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

/**
 * @description axios请求后拦截器
 */
// 响应拦截器
request.interceptors.response.use(
    (response) => {
        return response.data;
    },
    (error) => {
        if (error.response && error.response.status === 401) {
            // token 过期或无效，清除本地存储
            localStorage.removeItem('token');
            localStorage.removeItem('userInfo');
        }
        console.error("请求失败:", error);
        return Promise.reject(error);
    }
);

/**
 * @description 封装http对象包含GET、POST、PUT与DELETE方法
 */
export const http = {
    get: (url, data) => {
        return request.request({
            url: url,
            method: "GET",
            params: data,
        });
    },
    post: (url, data) => {
        return request.request({
            url: url,
            method: "POST",
            data: data,
        });
    },
    put: (url, data) => {
        return request.request({
            url: url,
            method: "PUT",
            data: data,
        });
    },
    delete: (url, data) => {
        return request.request({
            url: url,
            method: "DELETE",
            data: data,
        });
    },
};