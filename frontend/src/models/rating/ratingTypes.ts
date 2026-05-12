/**
 * 帖子相关的类型定义 (相当于后端的 VO 和 DTO)
 */

// 1. 基准模型：包含所有可能出现在 UI 上的字段
export interface Rating {
  id: number;
  creator:string;
  title: string;
  target: string;
  targetId: number;
  type: number;
}


// 发送评价的DTO
export type RatingDTO = Pick<Rating, 'target' | 'targetId'> & {action:number};

// 评价之后的返回值
export type RatingVO = Rating & {likeCount:number}
