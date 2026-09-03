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

        <router-view></router-view>
        
      </el-container>
    </div>

    <PostEdit ref="postEditRef" @success="onPostSuccess" />
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import NavBar from '@/components/index/NavBar.vue'
import LeftSideBar from '@/components/index/LeftSideBar.vue'
import PostEdit from '@/components/post/PostEdit.vue'
import { usePostStore } from '@/models/post/postStore'
import { ElMessage } from 'element-plus'

const searchQuery = ref('')
const route = useRoute()
const postEditRef = ref()
const postStore = usePostStore()

watch(
  () => route.query.keyword,
  (value) => {
    const keyword = Array.isArray(value) ? value[0] : value
    searchQuery.value = typeof keyword === 'string' ? keyword.trim() : ''
  },
  { immediate: true }
)

const handleCreatePost = () => postEditRef.value.open()
// 刷新事件通知可以通过事件总线/状态管理或者简单地重载
const handleRefresh = () => window.location.reload() 
const onPostSuccess = async () => {
  await postStore.getIndexPosts()
}
</script>

<style scoped>
/* 保持原有的外壳样式不变 */
.reddit-app-wrapper { background-color: #DAE0E6; min-height: 100vh; }
.main-viewport { display: flex; justify-content: center; padding: 20px 24px; }
.app-layout-container { max-width: 1280px; width: 100%; gap: 24px; }
.side-rail { overflow: visible; }
</style>
