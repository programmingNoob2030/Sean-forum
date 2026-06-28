import type { CreatePostDTO, DeletePostDTO, GetPostsDTO, PostVO, RecentPostVO, RestorePostDTO } from '@/models/post/postTypes'
import request from '@/utils/requests'
import type { PageResult } from '@/models/pages'
// 1. 获取所有帖子 (含逻辑删除)
export const apiGetPosts = (dto: GetPostsDTO) =>
  request.get<any, PageResult<PostVO>>('/posts', { params: dto })
// 创建帖子
export const apiCreatePost = (dto: CreatePostDTO) => request.post('/posts', dto)

// 删除帖子
export const apiDeletePost = (dto: DeletePostDTO) => request.delete('/posts', {data: dto})

// 恢复帖子
export const apiRestorePost = (dto: RestorePostDTO) => request.patch('/posts', dto) 

// 根据ID获取帖子信息
export const apiGetPostById = (id: number) => request.get<any,PostVO>(`/post/${id}`) 

// 获取最近浏览帖子
export const apiGetRecentPosts = () => request.get<any,RecentPostVO[]>(`/posts/history`) 

// 清空最近浏览的帖子
export const apiDeleteRecentPosts = () => request.delete<any,string>(`/posts/history`) 

