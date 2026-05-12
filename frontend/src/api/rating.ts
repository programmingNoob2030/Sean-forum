import type {Rating, RatingDTO, RatingVO} from '@/models/rating/ratingTypes'
import request from '@/utils/requests'


// 发送评价
export const apiToggleRating = (dto:RatingDTO) => {
  return request.put<any,RatingVO>('/ratings',dto)
}




