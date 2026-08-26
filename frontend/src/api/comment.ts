import type { CommentDTO,DeleteCommentDTO,RestoreCommentDTO, Comment as PostComment, CommentVO } from '@/models/comment/commentTypes'
import type { GetPostCommentsDTO } from '@/models/pages'
import request from '@/utils/requests'
import type { PageResult } from '@/models/pages'

// 1、根据帖子获取评论
export const apiGetCommentsByPostId = (dto: GetPostCommentsDTO) => {
  
  return request.get<any,PageResult<CommentVO>>('/post-comments', {params: dto})
}

// 2、发送评论
export const apiCreateComment = (dto: CommentDTO) => {
  
  return request.post<any,CommentVO>('/comments',  dto)
}

// 3、删除评论
export const apiDeleteComment = (dto: DeleteCommentDTO) => request.delete('/comments', {data: dto})

