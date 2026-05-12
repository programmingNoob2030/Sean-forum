import { defineStore } from "pinia";
import { ref } from 'vue'
import type { BriefBoardVO } from '@/models/board/boardTypes'
import { apiGetMyBoards } from "@/api/board";
export const useBoardStore = defineStore('board', ()=>{
  const briefBoardList = ref<BriefBoardVO[]>([])

  const getBoardListByUserId = async() =>{
    try{
      const res = await apiGetMyBoards()
      briefBoardList.value = res
    }catch(error){
      console.log("获取评价信息失败", error)
    }
  }


  return {
    briefBoardList,
    getBoardListByUserId
  }
})