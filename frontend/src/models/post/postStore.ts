import { defineStore } from "pinia";
import type { RecentPostVO,  PostVO } from "./postTypes";
import { ref } from 'vue'
import { apiGetPosts, apiGetRecentPosts } from "@/api/post";
import type { PageRequestDTO } from "../pages";
export const usePostStore = defineStore('post', ()=>{

  const posts = ref<PostVO[]>([])
  const total = ref(0)
  const dto = ref<PageRequestDTO>({
    pageNum: 1,
    pageSize: 10
  })
  const recentPostList = ref<RecentPostVO[]>([])
  const getPosts = async() => {
    try{
      if (dto.value){
        const res = await apiGetPosts(dto.value)
        posts.value = res.list
        total.value = res.total
      }
    }catch(error){
      console.log("获取帖子失败", error)
    }
  }
  const getRecentPosts = async()=>{
    try{
      const res = await apiGetRecentPosts()
      recentPostList.value = res
    }catch(error){
      console.log("获取帖子失败", error)
    }
  }
  return {
    dto,
    total,
    posts,
    getPosts,
    recentPostList,
    getRecentPosts
    
  }
})