import {http} from "@/utils/http.js";

//点赞
export const likeAPI = async ( {id: id}) => {
    return http.post("/forum/posts/{id}/like",{id: id})
}

//取消点赞
export const unlikeAPI = async ( {id: id}) => {
    return http.post("/forum/posts/{id}/unlike",{id: id})
}
//评论
export const commentsAPI = async ( ) => {
    return http.post("/forum/posts/{id}/comments",)
}
//删除评论
