import type { PageRequestDTO, PageResult } from '@/models/pages'
import type { MessageVO } from '@/models/message/messageTypes'
import request from '@/utils/requests'


// 获取未读信息条数
export const apiQueryUnreadMessageCount = () => request.get<any, number>('/message/unread-count')

// 获取评论信息
export const apiQueryMessages = (dto: PageRequestDTO) => request.get<any, PageResult<MessageVO>>('/messages', {params:dto})
