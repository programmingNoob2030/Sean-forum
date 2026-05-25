import { defineStore } from "pinia";
import { apiGetCommentsByPostId } from "@/api/comment";
import { ref } from 'vue'
import type { CommentVO, Comment } from '@/models/comment/commentTypes'
import type { GetPostCommentsDTO } from "../pages";
export const useCommentStore = defineStore('comment', ()=>{
  const commentList = ref<CommentVO[]>([]) // 数据存这里
  const total = ref(0)
  
  const dto = ref<GetPostCommentsDTO>({
    pageNum: 1,
    pageSize: 10,
    postId:0
  })
  const getCommentsByPostId = async() => {
    try{
      if (dto.value){
        const res = await apiGetCommentsByPostId(dto.value)
        console.log("拿到的数据", res)
        commentList.value = res.list// 直接更新 store 里的状态
        total.value = res.total
      }
    }catch(error){
      console.log("获取评价信息失败", error)
    }
  }
  const reset = () => {
    commentList.value = []
    total.value = 0
  }
  return {
    reset,
    dto,
    total,
    commentList,
    getCommentsByPostId
  }
})