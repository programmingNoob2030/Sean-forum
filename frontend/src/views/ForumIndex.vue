<template>
  <div class="reddit-app-wrapper">
    <NavBar 
      v-model="searchQuery" 
      @create-post="handleCreatePost" 
      @refresh="handleRefresh" 
    />

    <div class="main-viewport">
      <el-container class="app-layout-container">
        <el-aside width="270px" class="side-rail hidden-sm-and-down">
          <LeftSideBar/>
        </el-aside>

        <el-main class="reddit-main-feed">
          <div class="feed-container" v-loading="loading">
            <el-empty v-if="filteredPosts.length === 0" description="No posts found" />
            <div class="post-list">
              <PostCard 
                v-for="post in filteredPosts" 
                :key="post.id" 
                :post="post"
                @click-detail="goToDetail"
              />
            </div>
            <div class="pagination-wrapper">
            <el-pagination 
              background 
              layout="prev, pager, next, jumper" 
              :total="postStore.total"
              v-model:current-page="postStore.dto.pageNum"
              @current-change="handlePageChange"
            />
          </div>
          </div>
          
        </el-main>

        <el-aside width="312px" class="side-rail hidden-md-and-down">
          <RightSideBar />
        </el-aside>
      </el-container>
    </div>

    <PostEdit ref="postEditRef" @success="onPostSuccess" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { usePostStore } from '@/models/post/postStore'
import { ElMessage } from 'element-plus'

// 导入新组件
import NavBar from '@/components/index/NavBar.vue'
import LeftSideBar from '@/components/index/LeftSideBar.vue'
import RightSideBar from '@/components/index/RightSideBar.vue'
import PostCard from '@/components/post/PostCard.vue'
import PostEdit from '@/components/post/PostEdit.vue'

const router = useRouter()
const postStore = usePostStore()
const postEditRef = ref()
const searchQuery = ref('')
const loading = ref(false)

const hotTags = [
  { name: 'Java', type: '' },
  { name: 'Spring_Boot', type: 'success' },
  { name: 'Vue3', type: 'danger' },
  { name: 'Algorithm', type: 'info' }
]

// 核心逻辑保持不变，但模板变干净了
const loadData = async () => {
  loading.value = true
  await postStore.getPosts()
  loading.value = false
}

const filteredPosts = computed(() => {
  return postStore.posts.filter(post => 
    post.title.toLowerCase().includes(searchQuery.value.toLowerCase()) && !post.isDeleted
  )
})

const handleCreatePost = () => postEditRef.value.open()
const handleRefresh = () => loadData()
const goToDetail = (id: number) => router.push(`/post/${id}`)
const handlePageChange = () => {
  postStore.getPosts()
  window.scrollTo(0, 0)
}
const onPostSuccess = () => {
  ElMessage.success('发布成功')
  loadData()
}

onMounted(() => loadData())
</script>

<style scoped>
/* 此处保留 index.vue 原有的布局相关 CSS，
   具体的组件内部样式建议迁移到各自的 .vue 文件中 */
.pagination-wrapper {
  display: flex; 
  justify-content: center; 
  margin: 30px 0;
}
</style>

<style scoped>
/* 基础背景 */
.reddit-app-wrapper {
  background-color: #DAE0E6; 
  min-height: 100vh;
}

/* 整体居中容器 */
.main-viewport {
  display: flex;
  justify-content: center;
  padding: 20px 24px;
}

.app-layout-container {
  max-width: 1280px;
  width: 100%;
  gap: 24px;
}

/* 侧边栏通用容器（确保 aside 能正确撑开） */
.side-rail {
  overflow: visible;
}

/* 中间帖子流宽度控制 */
.reddit-main-feed {
  padding: 0;
  max-width: 640px;
  flex: 1;
}

.pagination-wrapper {
  display: flex; 
  justify-content: center; 
  margin: 30px 0;
}
</style>