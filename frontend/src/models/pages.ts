/**
 * 前端的 PageResult 接口定义
 * 必须和后端 PageResult 类的属性名完全一致！
 */
export interface PageResult<T> {
  list: T[];        // 数据列表
  total: number;    // 总条数
  pageNum: number;  // 当前页码
  pageSize: number; // 每页大小
}

export interface PageRequestDTO {
  pageNum: number;
  pageSize: number;
}

export type GetPostCommentsDTO = PageRequestDTO & {postId:number}