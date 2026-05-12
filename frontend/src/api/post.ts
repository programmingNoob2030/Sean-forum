import type { CreatePostDTO, DeletePostDTO, RestorePostDTO } from '@/models/post/postTypes'
import request from '@/utils/requests'
import type { PostVO } from '@/models/post/postTypes'
import type { PageResult } from '@/models/pages'
import type { PageRequestDTO } from '@/models/pages'

// 1. 获取所有帖子 (含逻辑删除)
export const apiGetPosts = (dto: PageRequestDTO) => {
  
  return request.get<any,PageResult<PostVO>>('/posts', {params: dto})
}
// 创建帖子
export const apiCreatePost = (dto: CreatePostDTO) => request.post('/posts', dto)

// 删除帖子
export const apiDeletePost = (dto: DeletePostDTO) => request.delete('/posts', {data: dto})

// 恢复帖子
export const apiRestorePost = (dto: RestorePostDTO) => request.patch('/posts', dto) 

// 根据ID获取帖子信息
export const apiGetPostById = (id: number) => request.get<any,PostVO>(`/post/${id}`) 
