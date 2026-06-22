<template>
  <div v-if="board" class="board-card" @click="handleNavigate">
    <div class="card-header">
      <img 
        :src="board.cover ? (baseUrl + board.cover) : '/default-board-avatar.png'" 
        class="card-avatar"
        @error="(e:any) => e.target.src = '/default-board-avatar.png'"
      />
      
      <div class="card-meta">
        <h3 class="card-name">r/{{ board.name }}</h3>
        <p class="card-stats">{{ formatNumber(board.weeklyVisitor) }} 个每周访客</p>
      </div>

      <el-button 
        v-if="!board.role"
        class="btn-join"
        round 
        size="small" 
        @click.stop="onJoin"
      >
        加入
      </el-button>

      <el-button 
        v-else-if="['CREATOR', 'ADMIN'].includes(board.role)"
        class="btn-manage"
        round 
        size="small" 
        @click.stop="onManage"
      >
        ⚙️ 管理
      </el-button>

      <el-button 
        v-else
        class="btn-joined"
        round 
        size="small" 
        @click.stop="onLeave"
      >
        已加入
      </el-button>
    </div>

    <p class="card-description">
      {{ board.description || '探索更多关于 ' + board.name + ' 的有趣内容。' }}
    </p>
  </div>
</template>

<script setup lang="ts">
import type { SquareBoardVO } from '@/models/board/boardTypes';
import { useRouter } from 'vue-router';
const baseUrl = import.meta.env.VITE_RESOURCE_URL

const props = defineProps<{
  board: SquareBoardVO;
}>();

const emit = defineEmits(['join','leave','manage']);
const router = useRouter();

const formatNumber = (num?: number) => {
  if (!num) return 0;
  return num > 10000 ? (num / 10000).toFixed(1) + '万' : num;
};

const handleNavigate = () => {
  router.push(`/board/${props.board.id}`);
};

const onJoin = () => { emit('join', props.board.id); };
const onLeave = () => { emit('leave', props.board.id); };
const onManage = () => { emit('manage', props.board.id); };
</script>

<style scoped>
/* ==========================================================================
   1. 局部作用域 CSS 变量 (与 BoardDetail 保持严格一致)
   ========================================================================== */
.board-card {
  --reddit-blue: #0079d3;
  --text-dark: #1c1c1c;
  --text-gray: #7c7c7c;
  --border-color: #ccc;
  
  background-color: #ffffff;
  color: var(--text-dark);
  padding: 20px; 
  border: 1px solid var(--border-color);
  border-radius: 4px;
  box-shadow: none; 
  cursor: pointer;
  transition: border-color 0.1s ease-in-out;
  
  display: flex;
  flex-direction: column;
  gap: 12px;
  height: 100%;
  box-sizing: border-box;
}

.board-card:hover {
  border-color: #898989; 
}

/* ==========================================================================
   2. 头部元数据
   ========================================================================== */
.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.card-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
  background-color: #ffffff;
  border: 1px solid #edeff1;
}

.card-meta {
  flex: 1;
  min-width: 0;
}

.card-name {
  margin: 0;
  color: var(--text-dark);
  font-size: 16px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-stats {
  margin: 2px 0 0;
  color: var(--text-gray);
  font-size: 12px;
}

.card-description {
  margin: 0;
  color: var(--text-dark);
  font-size: 14px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ==========================================================================
   3. 核心重构：覆盖 Element Plus 按钮默认皮肤
   ========================================================================== */
/* 统一覆盖 el-button 的原生全局底色，确保完全由我们手写的类名接管 */
.card-header :deep(.el-button) {
  font-weight: 700 !important;
  font-size: 12px !important;
  padding: 6px 16px !important;
  height: auto !important;
  border: none;
  transition: all 0.2s ease;
}

/* 情况 A：加入按钮 (深蓝底白字) */
.btn-join {
  background-color: var(--reddit-blue) !important;
  color: #ffffff !important;
}
.btn-join:hover {
  background-color: #33a8ff !important;
}

/* 情况 B：管理按钮 (浅灰底黑字加边框) */
.btn-manage {
  background-color: #f6f7f8 !important;
  color: var(--text-dark) !important;
  border: 1px solid var(--border-color) !important;
}
.btn-manage:hover {
  background-color: #e8f4ff !important;
  border-color: var(--reddit-blue) !important;
}

/* 情况 C：已加入按钮 (悬浮时变红提示退出) */
.btn-joined {
  background-color: transparent !important;
  color: var(--reddit-blue) !important;
  border: 1px solid var(--reddit-blue) !important;
}
.btn-joined:hover {
  background-color: #fff0f0 !important;
  color: #ff4500 !important;
  border-color: #ff4500 !important;
}
</style>