import { defineStore } from "pinia";
import { ref } from 'vue'
import { apiQueryMessages, apiQueryUnreadMessageCount } from "@/api/message";
import type { MessageVO, QueryMessageDTO } from "./messageTypes";
export const useMessageStore = defineStore('message', ()=>{

  const unreadCount = ref(0)
  const commentTotal = ref(0)
  const ratingTotal = ref(0)
  const commentDTO = ref<QueryMessageDTO>({
    pageNum: 1,
    pageSize: 10,
    actions:["COMMENT"]
  })
  const ratingDTO = ref<QueryMessageDTO>({
    pageNum: 1,
    pageSize: 10,
    actions:["LIKE", 'DISLIKE']
  })

  const commentMessageList = ref<MessageVO[]>([])
  const ratingMessageList = ref<MessageVO[]>([])
  const getUnreadCount = async() => {
    try{
      unreadCount.value = await apiQueryUnreadMessageCount()
    }
    catch(error){
      console.log("获取信息失败", error)
    }
  }
  const getCommentMessages = async()=>{
    try{
      const res = await apiQueryMessages(commentDTO.value)
      commentMessageList.value = res.list
      commentTotal.value = res.total
    }
    catch(error){
      console.log("获取信息失败", error)
    }
  }
  const getRatingMessages = async()=>{
    try{
      const res = await apiQueryMessages(ratingDTO.value)
      ratingMessageList.value = res.list
      ratingTotal.value = res.total
    }
    catch(error){
      console.log("获取信息失败", error)
    }
  }
  const resetStore =()=>{
    unreadCount.value = 0
    commentTotal.value = 0
    ratingTotal.value = 0
  
    commentDTO.value = {
      pageNum: 1,
      pageSize: 10,
      actions: ['COMMENT']
    }

    ratingDTO.value = {
      pageNum: 1,
      pageSize: 10,
      actions: ['LIKE', 'DISLIKE']
    }

    commentMessageList.value = []
    ratingMessageList.value = []
  }
  return {
    unreadCount,
    commentMessageList,
    ratingMessageList,
    getUnreadCount,
    commentDTO,
    commentTotal,
    getCommentMessages,
    ratingDTO,
    ratingTotal,
    getRatingMessages,
    resetStore
  }
})