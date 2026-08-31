import { defineStore } from "pinia";
import { ref } from "vue";
import { apiGetPosts, apiGetRecentPosts } from "@/api/post";
import type { GetPostsDTO, PostSort, PostVO, RecentPostVO } from "./postTypes";

export const usePostStore = defineStore("post", () => {
  const indexPosts = ref<PostVO[]>([]);
  const boardPosts = ref<PostVO[]>([]);
  const indexTotal = ref(0);
  const boardTotal = ref(0);
  const indexPostDTO = ref<GetPostsDTO>({
    pageNum: 1,
    pageSize: 10,
    sort: 'RECENT'
  });
  const boardPostDTO = ref<GetPostsDTO>({
    pageNum: 1,
    pageSize: 10,
    boardId: 0,
  });
  const recentPostList = ref<RecentPostVO[]>([]);

  const getIndexPosts = async (sort?: PostSort) => {
    try {
      if (sort != null) indexPostDTO.value.sort = sort
      const res = await apiGetPosts(indexPostDTO.value);
      indexPosts.value = res.list;
      indexTotal.value = res.total;
    } catch (error) {
      console.log("Failed to fetch posts", error);
    }
  };

  const getBoardPosts = async (boardId: number) => {
    boardPostDTO.value.boardId = boardId;

    try {
      const res = await apiGetPosts(boardPostDTO.value);
      boardPosts.value = res.list;
      boardTotal.value = res.total;
    } catch (error) {
      console.log("Failed to fetch board posts", error);
    }
  };

  const getRecentPosts = async () => {
    try {
      const res = await apiGetRecentPosts();
      recentPostList.value = res;
    } catch (error) {
      console.log("Failed to fetch recent posts", error);
    }
  };

  return {
    indexTotal,
    indexPosts,
    indexPostDTO,
    getIndexPosts,
    boardTotal,
    boardPosts,
    boardPostDTO,
    getBoardPosts,
    recentPostList,
    getRecentPosts,
  };
});
