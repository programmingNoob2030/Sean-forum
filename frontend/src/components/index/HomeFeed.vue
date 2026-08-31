<template>
  <el-main class="reddit-main-feed">
    <div class="feed-container" v-loading="loading">
      <el-radio-group
        v-model="postStore.indexPostDTO.sort"
        @change="postStore.getIndexPosts"
        class="sort-group"
      >
        <el-radio-button value="RECENT">最新</el-radio-button>
        <el-radio-button value="POPULAR">最受欢迎</el-radio-button>
        <el-radio-button value="COMMENTS">评论数</el-radio-button>
        <el-radio-button value="HOT">最热门</el-radio-button>
      </el-radio-group> 
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

/* 选择框样式 */
.sort-group {
  margin-bottom: 20px;
  gap: 8px;
}

.sort-group :deep(.el-radio-button__inner) {
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  box-shadow: none;
  padding: 8px 16px;
  color: #606266;
  background-color: #fff;
}

.sort-group :deep(.el-radio-button:first-child .el-radio-button__inner) {
  border-left: 1px solid #dcdfe6;
}

.sort-group :deep(.el-radio-button__inner:hover) {
  color: #409eff;
  border-color: #409eff;
}

.sort-group :deep(.el-radio-button.is-active .el-radio-button__inner) {
  color: #409eff;
  background-color: #ecf5ff;
  border-color: #409eff;
  box-shadow: none;
}
</style>