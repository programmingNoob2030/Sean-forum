<template>
  <div v-if="board" class="board-card" @click="handleNavigate">
    <div class="card-header">
      <img 
        :src="board.cover ? (baseUrl + board.cover) : '/default-board-avatar.png'" 
        class="card-avatar"
        @error="(e:any) => e.target.src = '/default-board-avatar.png'"
      />
      
      <div class="card-meta">
        <h3 class="card-name">{{ board.name }}</h3>
        <p class="card-stats">{{ formatNumber(board.weeklyVisitor) }} 个每周访客</p>
      </div>

      <!-- 情况 A：游客，显示加入 -->
      <el-button 
        v-if="!board.role"
        type="primary" 
        round 
        size="small" 
        @click.stop="onJoin"
      >
        加入
      </el-button>

      <!-- 情况 B：创建者或管理员，显示管理 -->
      <el-button 
        v-else-if="['CREATOR', 'ADMIN'].includes(board.role)"
        type="warning" 
        round 
        size="small" 
        @click.stop="onManage"
      >
        管理
      </el-button>

      <!-- 情况 C：普通成员，显示退出 -->
      <el-button 
        v-else
        type="info" 
        plain
        round 
        size="small" 
        @click.stop="onLeave"
      >
        退出
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
// 这里定义接收数据的结构


const props = defineProps<{
  board: SquareBoardVO;
}>();

const emit = defineEmits(['join','leave','manage']);
const router = useRouter();

// 数字格式化，12000 -> 1.2万
const formatNumber = (num?: number) => {
  if (!num) return 0;
  return num > 10000 ? (num / 10000).toFixed(1) + '万' : num;
};

const handleNavigate = () => {
  router.push(`/board/${props.board.id}`);
};

const onJoin = () => {
  emit('join', props.board.id);
};
const onLeave = () => {
  emit('leave', props.board.id);
};
const onManage = () => {
  emit('manage', props.board.id);
};
</script>

<style scoped>
.board-card {
  background-color: #ffffff;
  color: #1c1c1c;
  padding: 20px; 
  
  /* 1. 边框颜色调回 Reddit 标志性的淡灰色 */
  border: 1px solid #ccc;
  border-radius: 4px; /* Reddit 的圆角其实偏小，4px 更硬朗 */
  
  /* 2. 移除明显的阴影，改用极淡的投影或不加阴影 */
  box-shadow: none; 
  
  cursor: pointer;
  /* 3. 缩短过渡时间，让反馈更干脆 */
  transition: border-color 0.1s ease-in-out;
  
  display: flex;
  flex-direction: column;
  gap: 12px;
  height: 100%;
  box-sizing: border-box;
}

.board-card:hover {
  /* 4. 关键：悬停时不改阴影、不位移，只让边框颜色深一点点 */
  border-color: #898989; 
  transform: none; /* 彻底移除位移效果 */
}

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
  background-color: #fff;
  border: 1px solid #edeff1;
}

.card-meta {
  flex: 1;
  min-width: 0;
}

.card-name {
  margin: 0;
  color: #1c1c1c;
  font-size: 16px;
  font-weight: 600; /* Reddit 的标题没那么黑粗，用 600 刚好 */
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-stats {
  margin: 2px 0 0;
  color: #7c7c7c;
  font-size: 12px;
}

/* 按钮逻辑也要根据你的 role 字段动态切换 */
.card-join-btn {
  /* 使用黑色或深蓝色，但不要发光效果 */
  background-color: #0079d3 !important; 
  color: #ffffff !important;
  border: none !important;
  font-weight: 700 !important;
  padding: 8px 16px !important;
}

.card-join-btn:hover {
  background-color: #1484d6 !important; /* 悬停稍微亮一点点即可 */
}

.card-description {
  margin: 0;
  color: #1c1c1c; /* 保持文字颜色统一 */
  font-size: 14px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>