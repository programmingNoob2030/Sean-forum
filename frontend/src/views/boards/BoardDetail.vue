<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { apiGetBoardDetail } from '@/api/board'
import type { BoardVO } from '@/models/board/boardTypes'
import { usePostStore } from '@/models/post/postStore'
import PostEdit from '@/components/post/PostEdit.vue'
import BoardInfo from '@/components/board/BoardInfo.vue'
import PostCard from '@/components/post/PostCard.vue'

const route = useRoute()
const router = useRouter()
const postStore = usePostStore()
const board = ref<BoardVO>()
const loading = ref(true)
const baseUrl = import.meta.env.VITE_RESOURCE_URL
const postEditRef = ref()
const boardId = computed(() => Number(route.params.id))

const loadBoardPosts = async () => {
  if (!boardId.value) return
  await postStore.getBoardPosts(boardId.value)
}

onMounted(async () => {
  loading.value = true
  try {
    postStore.boardPosts = []
    board.value = await apiGetBoardDetail(boardId.value)
    postStore.boardPostDTO.pageNum = 1
    await loadBoardPosts()
  } finally {
    loading.value = false
  }
})

const handleRefresh = async () => {
  await loadBoardPosts()
}
const handlePageChange = async () => {
  await loadBoardPosts()
  window.scrollTo(0, 0)
}

const goToDetail = (id: number) => router.push(`/post/${id}`)
</script>

<template>
  <div v-if="board" class="board-detail-container" v-loading="loading">
    <div 
      class="banner-section" 
      :style="{ 
        backgroundImage: board.banner ? `url(${baseUrl + board.banner})` : 'none',
        backgroundColor: board.banner ? 'transparent' : '#33a8ff' 
      }"
    ></div>

    <div class="header-wrapper">
      <div class="header-content">
        <div class="info-left">
          <img :src="baseUrl + board.cover" class="board-avatar" />
          <h1 class="board-title">r/{{ board.name }}</h1>
        </div>
        
        <div class="action-right">
          <button class="btn-outline" @click="postEditRef.open(board.id, board.name, board.cover)">+ 创建帖子</button>
          
          <button v-if="board.currentUserRole === 'CREATOR' || board.currentUserRole === 'ADMIN'" class="btn-manage">
            ⚙️ 管理社区
          </button>
          <button v-else-if="board.currentUserRole === 'MEMBER'" class="btn-joined">
            已加入
          </button>
          <button v-else class="btn-join">
            加入
          </button>
        </div>
      </div>
    </div>
    <PostEdit ref="postEditRef" @success="handleRefresh" />

    <div class="main-content">
      <div class="post-list-section">
        <el-empty v-if="postStore.boardPosts.length === 0" description="No posts found" />
        <div v-else class="post-list">
          <PostCard
            v-for="post in postStore.boardPosts"
            :key="post.id"
            :post="post"
            @click-detail="goToDetail"
          />
        </div>
        <div class="pagination-wrapper" v-if="postStore.boardTotal > postStore.boardPostDTO.pageSize">
          <el-pagination
            background
            layout="prev, pager, next, jumper"
            :total="postStore.boardTotal"
            :page-size="postStore.boardPostDTO.pageSize"
            v-model:current-page="postStore.boardPostDTO.pageNum"
            @current-change="handlePageChange"
          />
        </div>
      </div>
      <BoardInfo :board-id="board.id" :baseUrl="baseUrl" />
    </div>
  </div>
</template>

<style scoped>
/* ==========================================================================
   1. 基础布局容器 (Variables & Global Layout)
   ========================================================================== */
.board-detail-container {
  --reddit-bg: #dae0e6;
  --reddit-blue: #0079d3;
  --text-dark: #1c1c1c;
  --border-color: #ccc;
  
  background-color: var(--reddit-bg);
  min-height: 100vh;
}

/* 模块公共最大宽度限制 */
.header-content,
.main-content {
  max-width: 980px;
  margin: 0 auto;
  padding: 0 16px;
}

/* ==========================================================================
   2. Banner & Header 视图
   ========================================================================== */
.banner-section {
  height: 160px;
  background-position: center;
  background-size: cover;
}

.header-wrapper {
  background: #ffffff;
  margin-bottom: 20px;
}

.header-content {
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

/* 🧠 核心重构：利用相对定位上移头像，内部通过 Flex 干净对齐，彻底干掉 margin-left */
.info-left {
  display: flex;
  align-items: flex-end;
  gap: 16px; /* 依靠间距自然撑开头像与标题 */
}

.board-avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  border: 4px solid #ffffff;
  background: #eeeeee;
  position: relative;
  top: -14px; /* 优雅抬高，无需绝对定位脱离文档流 */
}

.board-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-dark);
  padding-bottom: 12px; /* 完美的对齐视线 */
}

/* ==========================================================================
   3. 按钮动作组 (Action Buttons)
   ========================================================================== */
.action-right {
  display: flex;
  gap: 12px;
}

/* 按钮基础通用样式 */
.action-right button {
  padding: 6px 20px;
  border-radius: 20px;
  font-weight: 700;
  font-size: 14px;
  cursor: pointer;
  border: none;
  transition: all 0.2s ease;
}

.btn-outline {
  border: 1px solid var(--reddit-blue) !important;
  color: var(--reddit-blue);
  background: transparent;
}
.btn-outline:hover {
  background: #e8f4ff;
}

.btn-join {
  background: var(--reddit-blue);
  color: #ffffff;
}
.btn-join:hover {
  background: #33a8ff;
}

.btn-joined {
  background: transparent;
  color: var(--reddit-blue);
  border: 1px solid var(--reddit-blue) !important;
}
.btn-joined:hover {
  background: #fff0f0;
  color: #ff4500;
  border-color: #ff4500 !important;
}

.btn-manage {
  background: #f6f7f8;
  color: var(--text-dark);
  border: 1px solid var(--border-color) !important;
}
.btn-manage:hover {
  background: #e8f4ff;
  border-color: var(--reddit-blue) !important;
}

/* ==========================================================================
   4. 主体分栏 & 卡片 (Main Content Layout)
   ========================================================================== */
.main-content {
  display: grid;
  grid-template-columns: 1fr 312px;
  gap: 24px;
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin: 30px 0;
}
</style>
