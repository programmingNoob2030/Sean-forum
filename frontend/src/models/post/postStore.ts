import { defineStore } from "pinia";
import type { PostVO } from "./postTypes";
import { ref } from 'vue'
import { apiGetPosts } from "@/api/post";
import type { PageRequestDTO } from "../pages";
export const usePostStore = defineStore('post', ()=>{

  const posts = ref<PostVO[]>([])
  const total = ref(0)
  const dto = ref<PageRequestDTO>({
    pageNum: 1,
    pageSize: 10
  })
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
  return {
    dto,
    total,
    posts,
    getPosts
  }
})