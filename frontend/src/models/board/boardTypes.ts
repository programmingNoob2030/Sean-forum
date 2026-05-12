/**
 * 社区相关的类型定义 (相当于后端的 VO 和 DTO)
 */

// 1. 基准模型：包含所有可能出现在 UI 上的字段
export interface Board {
  id: number;
  name:string;
  cover:string;
  type:string;
  description:string;
  banner:string;
  postCount:number;
  memberCount:number;
  createTime:Date;
}
type CommunityType = 'PUBLIC' | 'PRIVATE' | 'STRICT';
// 创建社区
export type BoardDTO = Pick<Board, 'name' | 'cover'  | 'description'> & {type:CommunityType};

// 完整的社区信息
export type BoardVO = Board & {currentUserRole:string, creatorName:string, creatorAvatar:string};

// 简略的社区信息
export type BriefBoardVO = Pick<Board, 'id' | 'name' | 'cover'>

// 广场的社区信息
export type SquareBoardVO = Pick<Board, 'id' | 'name' | 'cover' | 'description'> & {weeklyVisitor:number, role:string}


