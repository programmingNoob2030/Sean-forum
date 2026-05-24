/**
 * 消息相关的类型定义 (相当于后端的 VO 和 DTO)
 */

// 1. 基准模型：包含所有可能出现在 UI 上的字段
export interface Message {
  id: number;
  senderId:number;
  action: string;
  targetId: number;
  receiverId: number; 
  isRead: boolean; 
  content:string;
  createTime:Date;
}

// 评论信息
export type MessageVO = Pick<Message, 'id' | 'content' | 'createTime'> & 
                        {name:string, avatar:string, action:string, reference:string}

