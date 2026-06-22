import { defineStore } from "pinia";
import { ref } from 'vue'
import type { BriefBoardVO } from '@/models/board/boardTypes'
import { apiGetMyBoards, apiGetRecentBoards, apiGetBoardDetail } from "@/api/board";
import { ca } from "element-plus/es/locale/index.mjs";
import { ElMessage } from "element-plus";
export const useBoardStore = defineStore('board', ()=>{
  const briefBoardList = ref<BriefBoardVO[]>([])
  const recentBoardList = ref<BriefBoardVO[]>([])

  const getBoardListByUserId = async() =>{
    try{
      const res = await apiGetMyBoards()
      briefBoardList.value = res
    }catch(error){
      console.log("获取社区信息失败", error)
    }
  }
  const getRecentBoardList = async() =>{
    try{
      const res = await apiGetRecentBoards()
      recentBoardList.value = res
    }catch(error){
      console.log("获取社区信息失败", error)
    }
  }
  const getBoardDetailById = async(id:number) =>{
    try{
      const res = await apiGetBoardDetail(id)
      if(res)return res
      else ElMessage.error("没有找到此社区的信息")
    }catch(error){
      console.log("获取社区信息失败", error)

    }
  }


  return {
    briefBoardList,
    recentBoardList,
    getBoardListByUserId,
    getRecentBoardList,
    getBoardDetailById
  }
})