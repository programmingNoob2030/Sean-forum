<script setup lang="ts">
import { useRouter } from 'vue-router';

const baseUrl = import.meta.env.VITE_RESOURCE_URL
/**
 * BriefBoardItem 组件属性定义
 * 采用接口形式，方便后期扩展
 */
interface Props {
  id: string | number;
  name: string;
  cover?: string;
  role?: string; // 是否为创建者
  isOnlyShow?: boolean; // 1. 把它定义在接口里，加个问号表示可选
}

// 设置默认值：如果 cover 为空，展示默认占位图
const props = withDefaults(defineProps<Props>(), {
  cover: '/default-board-avatar.png', // 确保你 public 目录下有这张图
  role: "MEMBER",
  isOnlyShow: false
});

const router = useRouter();

/**
 * 跳转到对应的社区详情页
 * 路径设计遵循 /r/community_name 的规范
 */
const handleNavigate = (id: number) => {
  if (props.isOnlyShow) {return}
  router.push(`/board/${id}`)
};
</script>

<template>
  <div 
    class="brief-board-item" 
    @click="handleNavigate(Number(id))"
    :title="`前往 r/${name}`"
  >
    <!-- 左侧社区 Logo -->
    <div class="avatar-wrapper">
      <img 
        :src="baseUrl + cover" 
        :alt="name"
        class="board-icon"
        @error="(e: any) => e.target.src = '/default-board-avatar.png'"
      />
    </div>
    
    <!-- 右侧社区名称与角色 -->
    <div class="board-info">
      <span class="board-prefix">r/</span>
      <span class="board-name">{{ name }}</span>
      
      <!-- 创建者标识（皇冠） -->
      <span v-if="role=='CREATOR'" class="owner-badge" title="你创建的社区">
        <svg viewBox="0 0 24 24" class="crown-icon">
          <path d="M5 16L3 5L8.5 10L12 4L15.5 10L21 5L19 16H5M19 19C19 19.6 18.6 20 18 20H6C5.4 20 5 19.6 5 19V18H19V19Z" />
        </svg>
      </span>
    </div>
  </div>
</template>

<style scoped>
.brief-board-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  margin: 2px 8px;
  cursor: pointer;
  border-radius: 4px;
  transition: background-color 0.15s ease;
  /* 禁止文字被选中 */
  user-select: none;
}

/* 悬停效果：模仿 Reddit 的浅灰色背景 */
.brief-board-item:hover {
  background-color: #f2f4f5;
}

/* 点击时的反馈效果 */
.brief-board-item:active {
  background-color: #e3e6e8;
}

.avatar-wrapper {
  flex-shrink: 0;
  display: flex;
  align-items: center;
}

.board-icon {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  object-fit: cover;
  background-color: #edeff1;
}

.board-info {
  flex: 1;
  display: flex;
  align-items: center;
  min-width: 0; /* 必须加这一行，否则 text-overflow 不生效 */
}

.board-prefix {
  font-size: 13px;
  color: #7c7c7c;
}

.board-name {
  font-size: 13px;
  font-weight: 500;
  color: #1a1a1b;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 皇冠图标样式 */
.owner-badge {
  margin-left: 4px;
  display: flex;
  align-items: center;
}

.crown-icon {
  width: 14px;
  height: 14px;
  fill: #ff4500; /* Reddit 标志性的橙红色 */
}
</style>