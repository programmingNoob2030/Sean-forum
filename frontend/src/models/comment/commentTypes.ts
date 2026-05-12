/**
 * 帖子相关的类型定义 (相当于后端的 VO 和 DTO)
 */

// 1. 基准模型：包含所有可能出现在 UI 上的字段
export interface Comment {
  id: number;
  creator:string;
  target: string;
  targetId: number;
  content: string;
  isDeleted: number; // 0-正常, 1-逻辑删除
  createTime: string; 
  rootId: number;
  rootType: string;
  parentId: number;
  likeCount: number;
}


// 发布评论
export type CommentDTO = Pick<Comment, 'target' | 'targetId' | 'content' | 'rootId' | 'rootType'>;

// 删除评论
export type DeleteCommentDTO = Pick<Comment, 'id'>;

// 恢复评论
export type RestoreCommentDTO = Pick<Comment, 'id'>;

// 返回每个帖子的评论
export type CommentVO = Comment & {commentRatingType:number, creatorName:string, children:CommentVO[],creatorAvatar:number}