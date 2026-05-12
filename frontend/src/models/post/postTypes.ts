/**
 * 帖子相关的类型定义 (相当于后端的 VO 和 DTO)
 */

import type { BriefBoardVO } from "@/models/board/boardTypes";

// 1. 基准模型：包含所有可能出现在 UI 上的字段
export interface Post {
  id: number;
  creator:string;
  title: string;
  content: string;
  isDeleted: number; // 0-正常, 1-逻辑删除
  createTime: string; 
  likeCount:number;
  commentCount:number;
}

// 2. 列表展示用的 VO (目前可以直接复用 Post)
export type PostVO = Post & {postRatingType:number, 
                             creatorName:string, 
                             boardName:string, 
                             boardCover:string,
                             creatorAvatar:string};
                             

// 3. 创建帖子用的 DTO (剔除 ID 和系统生成的字段)
// 语法解释：从 Post 中剔除 'id', 'isDeleted' 等字段
export type CreatePostDTO = Pick<Post, 'title' | 'content'> & {boardId:number};


export type BrifePostDTO = Pick<Post, 'title' | 'content'>;

// 4. 删除
export type DeletePostDTO = Pick<Post, 'id'>;

// 5.恢复
export type RestorePostDTO = Pick<Post, 'id'>;

// 6.发帖窗口
export type PostEditDTO = BrifePostDTO & BriefBoardVO;


