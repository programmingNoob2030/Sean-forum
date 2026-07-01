<template>
  <el-main class="reddit-main-feed">
    <div class="feed-container" v-loading="loading">
      <el-empty v-if="filteredPosts.length === 0" description="No posts found" />
      <div class="post-list">
        <PostCard 
          v-for="post in filteredPosts" 
          :key="post.id" 
          :post="post"
          @click-detail="goToDetail"
          @refresh="loadData"
        />
      </div>
      <div class="pagination-wrapper">
        <el-pagination 
          background 
          layout="prev, pager, next, jumper" 
          :total="postStore.indexTotal"
          :page-size="postStore.indexPostDTO.pageSize"
          v-model:current-page="postStore.indexPostDTO.pageNum"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </el-main>

  <el-aside width="312px" class="side-rail hidden-md-and-down">
    <RightSideBar />
  </el-aside>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { usePostStore } from '@/models/post/postStore'
import PostCard from '@/components/post/PostCard.vue'
import RightSideBar from '@/components/index/RightSideBar.vue'

const router = useRouter()
const postStore = usePostStore()
const loading = ref(false)
const searchQuery = ref('') // 可通过状态管理或父子组件传值同步

const loadData = async () => {
  loading.value = true
  try {
    await postStore.getIndexPosts()
  } finally {
    loading.value = false
  }
}

const filteredPosts = computed(() => {
  return postStore.indexPosts.filter(post => 
    post.title.toLowerCase().includes(searchQuery.value.toLowerCase()) && !post.isDeleted
  )
})

const goToDetail = (id: number) => router.push(`/post/${id}`)
const handlePageChange = async () => {
  loading.value = true
  try {
    await postStore.getIndexPosts()
    window.scrollTo(0, 0)
  } finally {
    loading.value = false
  }
}

onMounted(() => loadData())
</script>

<style scoped>
.reddit-main-feed { padding: 0; max-width: 640px; flex: 1; }
.side-rail { overflow: visible; }
.pagination-wrapper { display: flex; justify-content: center; margin: 30px 0; }
</style>