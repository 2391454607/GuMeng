// web/src/api/manage/Comment.js
import { http } from '@/utils/http.js'

// 获取评论列表（支持分页和筛选）
export const getCommentsAPI = (params) => {
  return http.get('/sys/comments', params)
}

// 获取评论详情
export const getCommentDetailAPI = (id) => {
  return http.get(`/sys/comments/${id}`)
}

// 删除评论
export const deleteCommentAPI = (id) => {
  return http.delete(`/sys/comments/${id}`)
}

// 批量删除评论
export const batchDeleteCommentsAPI = (ids) => {
  return http.delete('/sys/comments/batch', { data: ids })
}

// 获取评论统计数据
export const getCommentStatsAPI = () => {
  return http.get('/sys/comments/stats')
}