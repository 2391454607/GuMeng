// web/src/api/manage/Forum.js
import { http } from '@/utils/http.js'

// 获取所有论坛帖子（支持分页、关键词搜索）
export const getForumPostsAPI = (params) => {
  return http.get('/sys/forum/posts', params)
}

// 获取帖子详情
export const getPostDetailAPI = (id) => {
  return http.get(`/sys/forum/posts/${id}`)
}

// 删除帖子
export const deletePostAPI = (id) => {
  return http.delete(`/sys/forum/posts/${id}`)
}

// 获取所有话题类型
export const getAllTopicsAPI = () => {
  return http.get('/sys/forum/topics')
}

// 添加话题类型
export const addTopicAPI = (data) => {
  return http.post('/sys/forum/topics', data)
}

// 删除话题类型
export const deleteTopicAPI = (id) => {
  return http.delete(`/sys/forum/topics/${id}`)
}