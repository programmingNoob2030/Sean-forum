<template>
  <div class="message-center-container">
    <div class="sidebar">
      <div class="sidebar-title">
        <el-icon class="title-icon"><ChatLineRound /></el-icon>
        <span>消息中心</span>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        class="sidebar-menu"
        @select="handleSelect"
      >
        <el-menu-item index="reply">
          <span class="menu-dot" :class="{ active: activeMenu === 'reply' }"></span>
          <span>回复我的</span>
        </el-menu-item>
        <el-menu-item index="rating">
          <span class="menu-dot" :class="{ active: activeMenu === 'rating' }"></span>
          <span>收到的赞</span>
        </el-menu-item>
      </el-menu>
    </div>

    <div class="main-content">
      <div class="content-card">
        <div class="content-header">
          <span class="header-title">{{ currentTitle }}</span>
        </div>
        
        <div v-if="hasData" class="message-list">
          
          <template v-if="activeMenu === 'reply'">
            <MessageItem 
              v-for="item in messageStore.commentMessageList" 
              :key="'reply-' + item.id" 
              :item="item" 
            />
            
            <div class="pagination-wrapper">
              <el-pagination 
                background 
                layout="prev, pager, next, jumper" 
                :total="messageStore.commentTotal"
                v-model:current-page = "messageStore.commentDTO.pageNum"
                @current-change="handlePageChange('reply')"
              />
            </div>
          </template>

          <template v-if="activeMenu === 'rating'">
            <MessageItem 
              v-for="item in messageStore.ratingMessageList" 
              :key="'rating-' + item.id" 
              :item="item" 
            />
            <div class="pagination-wrapper">
              <el-pagination 
                background 
                layout="prev, pager, next, jumper" 
                :total="messageStore.ratingTotal"
                v-model:current-page = "messageStore.ratingDTO.pageNum"
                @current-change="handlePageChange('rating')"
              />
            </div>
          </template>

        </div>

        <div v-else class="empty-state">
          <div class="empty-illustration">
            <el-icon class="folder-icon"><Folder /></el-icon>         
          </div>
          <p class="empty-text">暂无消息记录</p>
          <p class="empty-subtext">快找小伙伴聊天吧 ( °- °)つロ</p>
        </div>
      </div>
    </div>
    
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ChatLineRound, Folder } from '@element-plus/icons-vue'
import MessageItem from '@/components/message/MessageItem.vue'
import { useMessageStore } from '@/models/message/messageStore'

const activeMenu = ref('reply')
const messageStore = useMessageStore()

// 🎯 核心变动：页面挂载时，触发获取消息通知的异步请求
onMounted(async () => {
  // 假设你的 store 里是统一获取当前用户的所有通知
  await messageStore.getCommentMessages() 
})

const hasData = computed(() => {
  if (activeMenu.value === 'reply') {
    return messageStore.commentMessageList.length > 0
  } else {
    return messageStore.ratingMessageList.length > 0
  }
})

const currentTitle = computed(() => {
  return activeMenu.value === 'reply' ? '回复我的' : '收到的赞'
})
const handlePageChange = (index:string) => {
  if (index === 'reply') messageStore.getCommentMessages()
  if (index === 'rating') messageStore.getRatingMessages()
  window.scrollTo(0, 0)
}
const handleSelect = async(index: string) => {
  activeMenu.value = index
  if (activeMenu.value === 'rating'){
    await messageStore.getRatingMessages()
  }
  if (activeMenu.value === 'reply'){
    await messageStore.getCommentMessages()
  }
}
</script>

<style scoped>
.pagination-wrapper {
  display: flex; 
  justify-content: center; 
  margin: 30px 0;
}
/* 父组件的 CSS 瞬间少了一大半，只剩下大框架布局 */
.message-center-container {
  display: flex;
  width: 100%;
  min-height: calc(100vh - 60px);
  background-color: #dae0e6;
  padding: 20px;
  box-sizing: border-box;
  gap: 20px;
}

.sidebar {
  width: 220px;
  background-color: #ffffff;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  padding: 16px 0;
  flex-shrink: 0;
  height: fit-content;
}

.sidebar-title {
  display: flex;
  align-items: center;
  padding: 0 20px 12px 20px;
  font-size: 14px;
  font-weight: 700;
  color: #1c1c1c;
  border-bottom: 1px solid #edeff1;
  margin-bottom: 8px;
}

.title-icon {
  margin-right: 8px;
  font-size: 16px;
  color: #ff4500;
}

.sidebar-menu {
  border-right: none !important;
  background-color: transparent !important;
}

:deep(.el-menu-item) {
  height: 40px !important;
  line-height: 40px !important;
  color: #5c5c5c !important;
  padding-left: 20px !important;
  font-size: 14px;
  margin: 4px 8px;
  border-radius: 4px;
}

:deep(.el-menu-item:hover) {
  background-color: #f6f7f8 !important;
  color: #1c1c1c !important;
}

:deep(.el-menu-item.is-active) {
  color: #0079d3 !important;
  background-color: #f6f7f8 !important;
  font-weight: 600;
}

.menu-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: transparent;
  margin-right: 10px;
  display: inline-block;
  transition: background-color 0.2s;
}

.menu-dot.active {
  background-color: #0079d3;
}

.main-content {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}

.content-card {
  background-color: #ffffff;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  min-height: 600px;
  display: flex;
  flex-direction: column;
}

.content-header {
  height: 48px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  border-bottom: 1px solid #edeff1;
}

.header-title {
  font-size: 15px;
  font-weight: 600;
  color: #1c1c1c;
}

.message-list {
  display: flex;
  flex-direction: column;
  padding: 12px;
  background-color: #f6f7f8;
}

.empty-state {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding-bottom: 60px;
}

.empty-illustration {
  margin-bottom: 16px;
}

.folder-icon {
  font-size: 56px;
  color: #eeeeee;
}

.empty-text {
  font-size: 14px;
  color: #7c7c7c;
  margin: 0 0 8px 0;
  font-weight: 500;
}

.empty-subtext {
  font-size: 12px;
  color: #a8a8a8;
  margin: 0;
}
</style>