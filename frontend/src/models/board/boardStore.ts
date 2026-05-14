import { defineStore } from "pinia";
import { ref } from 'vue'
import type { BriefBoardVO } from '@/models/board/boardTypes'
import { apiGetMyBoards, apiGetRecentBoards } from "@/api/board";
export const useBoardStore = defineStore('board', ()=>{
  const briefBoardList = ref<BriefBoardVO[]>([])
  const recentBoardList = ref<BriefBoardVO[]>([])

  const getBoardListByUserId = async() =>{
    try{
      const res = await apiGetMyBoards()
      briefBoardList.value = res
    }catch(error){
      console.log("获取评价信息失败", error)
    }
  }
  const getRecentBoardList = async() =>{
    try{
      const res = await apiGetRecentBoards()
      recentBoardList.value = res
    }catch(error){
      console.log("获取评价信息失败", error)
    }
  }


  return {
    briefBoardList,
    recentBoardList,
    getBoardListByUserId,
    getRecentBoardList
  }
})