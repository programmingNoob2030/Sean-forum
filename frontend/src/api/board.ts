import type { BoardDTO, BoardMemberActionDTO, BoardMembershipVO, BoardUpdateDTO, BoardVO, BriefBoardVO, SquareBoardVO } from '@/models/board/boardTypes'
import request from '@/utils/requests'


// 创建社区
export const apiCreateBoard = (dto: BoardDTO) => {
  return request.post<any,BoardVO>('/boards',  dto)
  
}
// 上传封面
export const apiUploadBoardImage = (data: FormData) => request.post<any, string>('/boards/images', data)
export const apiUploadCover = apiUploadBoardImage

// 获取用户 "我的社区"
export const apiGetMyBoards = () => request.get<any, BriefBoardVO[]>('/boards/mine')

// 获取社区详情
export const apiGetBoardDetail = (id:number) => request.get<any, BoardVO>(`/boards/${id}`)

export const apiUpdateBoard = (id: number, dto: BoardUpdateDTO) => request.put<any, BoardVO>(`/boards/${id}`, dto)

export const apiToggleBoardMembership = (dto: BoardMemberActionDTO) => request.put<any, BoardMembershipVO>('/board/membership', dto)

// 搜索社区
export const apiSearchBoard = (keyword:string) => request.get<any, BriefBoardVO[]>('/board/search', {params:{keyword:keyword}})

// 获取广场社区
export const apiGetSquareBoards = () => request.get<any, SquareBoardVO[]>('/boards')

// 获取最近浏览社区
export const apiGetRecentBoards = () => request.get<any, BriefBoardVO[]>('/boards/history')


