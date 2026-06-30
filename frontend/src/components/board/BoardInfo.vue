<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { apiGetBoardDetail } from '@/api/board' 
import type { BoardVO } from '@/models/board/boardTypes' 

// 1. 定义 Props：严格接收 boardId 和静态资源 baseUrl
const props = withDefaults(defineProps<{
  boardId: number
  baseUrl?: string
  refreshKey?: number
}>(), {
  baseUrl: '',
  refreshKey: 0
})

const boardData = ref<BoardVO | null>(null)
const loading = ref(false)

// 2. 核心数据获取逻辑：根据传入的 id 异步请求后端
const fetchBoardDetail = async (id: number) => {
  if (!id) return
  loading.value = true
  try {
    boardData.value = await apiGetBoardDetail(id)
  } catch (error) {
    console.error('获取社区详情失败:', error)
  } finally {
    loading.value = false
  }
}

// 3. 生命周期与侦听器
onMounted(() => {
  fetchBoardDetail(props.boardId)
})

// 监听 boardId 变化，防止在社区详情页之间连续跳转时组件不刷新
watch(() => [props.boardId, props.refreshKey], ([newId]) => {
  fetchBoardDetail(Number(newId))
})

// 4. 辅助函数：将后端角色枚举转换为中文标签
const getRoleLabel = (role: string) => {
  const map: Record<string, string> = {
    'CREATOR': '社区创建者',
    'ADMIN': '管理员',
    'MEMBER': '社区成员'
  }
  return map[role] || '访客'
}
</script>

<template>
  <aside class="sidebar-section" v-loading="loading">
    <div class="about-card" v-if="boardData">
      
      <div class="card-header">
        <span class="header-title">关于社区</span>
        <span class="board-name">r/{{ boardData.name }}</span>
      </div>

      <div class="card-body">
        
        <div class="user-identity-tag" v-if="boardData.currentUserRole">
          <span :class="['role-badge', boardData.currentUserRole.toLowerCase()]">
            {{ getRoleLabel(boardData.currentUserRole) }}
          </span>
        </div>

        <p class="desc">{{ boardData.description }}</p>
        
        <div class="stats-row">
          <div class="stat-item">
            <span class="stat-num">{{ boardData.memberCount }}</span>
            <span class="stat-label">成员</span>
          </div>
          <div class="stat-item">
            <span class="stat-num">{{ boardData.postCount }}</span>
            <span class="stat-label">帖子</span>
          </div>
        </div>

        <div class="creator-info">
          <div class="info-label">创始人</div>
          <div class="creator-detail">
            <img :src="baseUrl + boardData.creatorAvatar" class="mini-avatar" v-if="boardData.creatorAvatar" />
            <span class="creator-name">u/{{ boardData.creatorName }}</span>
          </div>
        </div>
        <div class="create-date">
          📅 创建于 {{ boardData.createTime ? new Date(boardData.createTime).toLocaleDateString() : '' }}
        </div>
      </div>
    </div>
  </aside>
</template>

<style scoped>
/* 右侧卡片基础样式 */
.about-card {
  background: white;
  border-radius: 4px;
  border: 1px solid #ccc;
}

/* 🌟 核心改动：使用 flex 布局让它们左右并排 */
.card-header {
  background: white;
  color: #1c1c1c;
  padding: 12px;
  border-radius: 4px 4px 0 0;
  border-bottom: 1px solid #eee;
  
  display: flex;
  justify-content: space-between; /* 左右两端对齐，如果想紧挨着可以改成 gap: 8px */
  align-items: center;
}

.header-title {
  font-weight: 700;
  font-size: 14px;
}

/* 🌟 社区名字稍微做一点点颜色淡化或字号微调，凸显 Reddit 质感 */
.board-name {
  font-size: 13px;
  color: #7c7c7c;
  font-weight: 500;
}

.card-body {
  padding: 12px;
}

.desc {
  font-size: 14px;
  line-height: 21px;
  margin-bottom: 16px;
  color: #1c1c1c;
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

.role-badge.creator {
  background-color: #ffe8e8;
  color: #ff4500;
  border: 1px solid #ff4500;
}

.role-badge.admin {
  background-color: #e8f4ff;
  color: #0079d3;
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
  font-size: 12px;
  color: #7c7c7c;
}
</style>