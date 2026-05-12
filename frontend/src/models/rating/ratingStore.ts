import { defineStore } from "pinia";
import { apiToggleRating } from "@/api/rating";
import type { RatingDTO } from "@/models/rating/ratingTypes"; 
export const useRatingStore = defineStore('rating', ()=>{


  const toggleRating = async(dto:RatingDTO) => {
    try{
      const res = await apiToggleRating(dto)
      console.log("评价返回的数据", res)
      return res
    }catch(error){
      console.log("获取评价信息失败", error)
    }
  }
  return {
    toggleRating
  }
})