<script setup lang="ts">
import { apiGetBoardDetail } from '@/api/board'
import type { BoardVO } from '@/models/board/boardTypes'
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import PostEdit from '@/components/post/PostEdit.vue'
const route = useRoute()
const board = ref<BoardVO>()
const loading = ref(true)
const baseUrl = import.meta.env.VITE_RESOURCE_URL
const postEditRef = ref()
// 辅助函数：将后端角色枚举转换为中文标签
const getRoleLabel = (role: string) => {
  const map: Record<string, string> = {
    'CREATOR': '社区创建者',
    'ADMIN': '管理员',
    'MEMBER': '社区成员'
  }
  return map[role] || '访客'
}
// 获取数据逻辑
onMounted(async () => {
  const id = route.params.id
  board.value = await apiGetBoardDetail(Number(id))
})
const handleRefresh = () => {
  // 发帖成功后的刷新逻辑
  console.log('列表刷新中...')
}
</script>

<template>
  <div v-if="board" class="board-detail-container">
    <!-- 1. Banner 区域 -->
    <div class="banner-section" :style="{ backgroundImage: `url(${board.banner})` }"></div>

    <!-- 2. Header 区域 (头像、名称、按钮) -->
    <div class="header-wrapper">
      <div class="header-content">
        <div class="info-left">
          <img :src="baseUrl + board.cover" class="board-avatar" />
          <div class="title-area">
            <h1 class="board-title">r/{{ board.name }}</h1>
          </div>
        </div>
        <div class="action-right">
          <button class="btn-outline" @click="postEditRef.open(board.id,board.name, board.cover)">+ 创建帖子</button>
          <button class="btn-join">加入</button>
        </div>
      </div>
    </div>
    <PostEdit ref="postEditRef" @success="handleRefresh" />

    <!-- 3. 主体内容区 (左右分栏) -->
    <div class="main-content">
      <!-- 左侧：帖子列表占位 -->
      <div class="post-list-section">
        <div class="temp-post-card">帖子列表加载中...</div>
      </div>

      <!-- 右侧：社区信息卡片 -->
      <aside class="sidebar-section">
      <div class="about-card">
        <div class="card-header">关于社区</div>
        <div class="card-body">
          <div class="user-identity-tag" v-if="board.currentUserRole">
            <span :class="['role-badge', board.currentUserRole.toLowerCase()]">
              {{ getRoleLabel(board.currentUserRole) }}
            </span>
          </div>

          <p class="desc">{{ board.description }}</p>
          
          <div class="stats-row">
            <div class="stat-item">
              <span class="stat-num">{{ board.memberCount }}</span>
              <span class="stat-label">成员</span>
            </div>
            <div class="stat-item">
              <span class="stat-num">{{ board.postCount }}</span>
              <span class="stat-label">帖子</span>
            </div>
          </div>

          <div class="creator-info">
            <div class="info-label">创始人</div>
            <div class="creator-detail">
              <img :src="baseUrl +board.creatorAvatar" class="mini-avatar" />
              <span class="creator-name">u/{{ board.creatorName }}</span>
            </div>
          </div>

          <div class="create-date">
            📅 创建于 {{ board.createTime }}
          </div>
        </div>
      </div>
    </aside>
    </div>
  </div>
</template>

<style scoped>
.board-detail-container {
  background-color: #dae0e6; /* Reddit 背景灰 */
  min-height: 100vh;
}

/* Banner 样式 */
.banner-section {
  height: 160px;
  background-color: #33a8ff;
  background-position: center;
  background-size: cover;
}

/* Header 样式 */
.header-wrapper {
  background: white;
  height: 80px;
  margin-bottom: 20px;
}

.header-content {
  max-width: 980px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
  padding: 0 16px;
  position: relative;
}

.info-left {
  display: flex;
  align-items: flex-end;
}

.board-avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  border: 4px solid white;
  position: absolute;
  top: -24px; /* 向上悬浮效果 */
  background: #eee;
}

.title-area {
  margin-left: 88px; /* 给头像留出空间 */
  padding-bottom: 8px;
}

.board-title {
  font-size: 24px;
  font-weight: 700;
  color: #1c1c1c;
}

.action-right {
  display: flex;
  gap: 12px;
}

/* 按钮样式 */
.btn-outline {
  border: 1px solid #0079d3;
  color: #0079d3;
  background: transparent;
  padding: 6px 16px;
  border-radius: 20px;
  font-weight: 700;
  cursor: pointer;
}

.btn-join {
  background: #0079d3;
  color: white;
  border: none;
  padding: 6px 24px;
  border-radius: 20px;
  font-weight: 700;
  cursor: pointer;
}

/* 内容布局 */
.main-content {
  max-width: 980px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr 312px; /* 经典 Reddit 比例 */
  gap: 24px;
  padding: 0 16px;
}

/* 右侧卡片 */
.about-card {
  background: white;
  border-radius: 4px;
  border: 1px solid #ccc;
}

/* 修改后的右侧卡片头部样式 */
.card-header {
  background: white;        /* 改为白色 */
  color: #1c1c1c;           /* 文字改为深黑色 */
  padding: 12px;
  font-weight: 700;
  font-size: 14px;
  border-radius: 4px 4px 0 0;
  border-bottom: 1px solid #eee; /* 既然背景是白的，加个浅色边框区分标题和内容 */
}

.card-body {
  padding: 12px;
}

.desc {
  font-size: 14px;
  line-height: 21px;
  margin-bottom: 16px;
}

.stats-row {
  display: flex;
  border-bottom: 1px solid #eee;
  padding-bottom: 16px;
  margin-bottom: 16px;
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.stat-num {
  font-weight: 700;
  font-size: 16px;
}

.stat-label {
  font-size: 12px;
  color: #7c7c7c;
}

.create-date {
  font-size: 14px;
  color: #1c1c1c;
}

.temp-post-card {
  background: white;
  height: 200px;
  border: 1px solid #ccc;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #7c7c7c;
}

/* 身份标识徽章 */
.user-identity-tag {
  margin-bottom: 12px;
}

.role-badge {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 12px;
  font-weight: 600;
}

/* 根据角色赋予不同颜色 */
.role-badge.creator {
  background-color: #ffe8e8;
  color: #ff4500; /* 创始人用橙红色 */
  border: 1px solid #ff4500;
}

.role-badge.admin {
  background-color: #e8f4ff;
  color: #0079d3; /* 管理员用蓝色 */
  border: 1px solid #0079d3;
}

.role-badge.member {
  background-color: #f6f7f8;
  color: #7c7c7c;
  border: 1px solid #ccc;
}

/* 创始人展示区域 */
.creator-info {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #eee;
  margin-bottom: 12px;
}

.info-label {
  font-size: 12px;
  color: #7c7c7c;
  margin-bottom: 8px;
}

.creator-detail {
  display: flex;
  align-items: center;
  gap: 8px;
}

.mini-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background-color: #ddd;
}

.creator-name {
  font-size: 14px;
  font-weight: 500;
  color: #1c1c1c;
}

.create-date {
  font-size: 12px; /* 稍微缩小一点日期，突出创始人 */
  color: #7c7c7c;
}
</style>