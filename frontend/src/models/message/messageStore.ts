import { defineStore } from "pinia";
import { ref } from 'vue'
import { apiQueryMessages, apiQueryUnreadMessageCount } from "@/api/message";
import type { PageRequestDTO } from "@/models/pages";
import type { MessageVO } from "./messageTypes";
export const useMessageStore = defineStore('message', ()=>{

  const unreadCount = ref(0)
  const dto = ref<PageRequestDTO>({
    pageNum: 1,
    pageSize: 10
  })
  const commentMessageList = ref<MessageVO[]>([])

  const getUnreadCount = async() => {
    try{
      unreadCount.value = await apiQueryUnreadMessageCount()
    }
    catch(error){
      console.log("获取信息失败", error)
    }
  }
  const getMessages = async()=>{
    try{
      const res = await apiQueryMessages(dto.value)
      commentMessageList.value = res.list
    }
    catch(error){
      console.log("获取信息失败", error)
    }

  }
  return {
    unreadCount,
    commentMessageList,
    getUnreadCount,
    getMessages
  }
})