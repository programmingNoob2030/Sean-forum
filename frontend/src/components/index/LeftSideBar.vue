<template>
  <div class="sticky-container">
    <div class="menu-group">
      <div class="menu-item active"><el-icon><HomeFilled /></el-icon> 主页</div>
      <div class="menu-item"><el-icon><TrendCharts /></el-icon> 热门</div>
      <div class="menu-item" @click=goToBoardSquare><el-icon><Compass /></el-icon> 浏览社区</div>
      <div class="menu-item" @click="isDialogOpen = true">
      <el-icon><Plus /></el-icon> 创建社区
      </div>
      <CreateBoard v-model="isDialogOpen" @success="handleRefresh" />
      <hr class="separator">

    </div>

    <div class="menu-group">

      <div class="menu-item section-header" @click="isRecentOpen = !isRecentOpen">
        <span class="label-text">最近浏览</span>
        <el-icon :class="{ 'is-active': isRecentOpen }" class="arrow-icon"><ArrowDown /></el-icon>
      </div>
      
      <div v-show="isRecentOpen" class="sub-menu">
        <div class="my-boards-section">
          <BriefBoardItem 
            v-for="board in boardStore.recentBoardList" 
            :key="board.id"
            v-bind="board" 
          />
        </div>
      </div>
    </div>
    <div class="menu-group">

      <div class="menu-item section-header" @click="isCommunityOpen = !isCommunityOpen">
        <span class="label-text">我的社区</span>
        <el-icon :class="{ 'is-active': isCommunityOpen }" class="arrow-icon"><ArrowDown /></el-icon>
      </div>

      <div v-show="isCommunityOpen" class="sub-menu">
        <div class="menu-item sub-item"><el-icon><Setting /></el-icon> 管理社区</div>
        <div class="my-boards-section">
          <BriefBoardItem 
            v-for="board in boardStore.briefBoardList" 
            :key="board.id"
            v-bind="board" 
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { 
  HomeFilled, TrendCharts, Compass, 
  Plus, ArrowDown, Setting 
} from '@element-plus/icons-vue'

import { useBoardStore } from '@/models/board/boardStore'
import CreateBoard from '@/components/board/CreateBoard.vue'
import BriefBoardItem from '@/components/board/BriefBoardItem.vue'
import { useUserStore } from '@/models/user/userStore'
import {watch }from'vue'
import router from '@/router'
const isRecentOpen = ref(true)
const isCommunityOpen = ref(true)
const isDialogOpen = ref(false)
const boardStore = useBoardStore()
const userStore = useUserStore()
const handleRefresh = async() => {
    await boardStore.getBoardListByUserId()
}
onMounted(async()=>{
  if (userStore.token) {
    await boardStore.getBoardListByUserId()
    await boardStore.getRecentBoardList()
  }
})
watch(() => userStore.token, async (newId) => {
  if (newId) {
    await boardStore.getBoardListByUserId()
  }
}, { immediate: true })
const goToBoardSquare = () =>{
  router.push("/board-square")
}
</script>

<style scoped>

.sticky-container {
  position: sticky;
  top: 68px;
}

/* 1. 基础菜单项：回归原样 */
.menu-item {
  padding: 10px 12px;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 12px;
  color: #1c1c1c; /* 回归纯黑 */
  font-size: 14px;
  transition: background 0.2s;
}

/* 2. Hover：回归你最初的灰色 */
.menu-item:hover {
  background: #e8e9ea; 
}

/* 3. Active：保持原有的淡灰色 */
.menu-item.active {
  background: #f6f7f8;
  font-weight: 600;
}

/* 4. 重点：只让分割线和 Logo 色（#FF4500）一致 */
hr.separator {
  border: none;
  height: 1px;
  background-color: #FF4500;
  /* 显式声明宽度和居中 */
  width: 90%; 
  /* 关键：取消 margin 的自动合并，设置固定的垂直距离 */
  margin: 8px auto !important; 
  /* 降低一点不透明度，能缓解颜色的刺眼感，让线条显得更细更精致 */
  opacity: 0.5; 
}
/* 5. 标题行：保持原样，悬停时不加背景 */
.section-header {
  justify-content: space-between;
  color: #7c7c7c;
  font-size: 14px; /* Reddit 风格的标签通常很小 */
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.section-header:hover {
  background: transparent; 
}

.sub-item {
  padding-left: 24px;
}

.arrow-icon {
  transition: transform 0.3s;
}
.arrow-icon.is-active {
  transform: rotate(180deg);
}
</style>