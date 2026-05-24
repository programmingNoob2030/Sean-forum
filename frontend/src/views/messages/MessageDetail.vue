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
        <el-menu-item index="like">
          <span class="menu-dot" :class="{ active: activeMenu === 'like' }"></span>
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
              v-for="item in replyMessages" 
              :key="'reply-' + item.id" 
              :item="item" 
            />
          </template>

          <template v-if="activeMenu === 'like'">
            <MessageItem 
              v-for="item in likeMessages" 
              :key="'like-' + item.id" 
              :item="item" 
            />
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
  await messageStore.getMessages() 
})

// 🎯 核心分流 A：从 Store 里的总数据源中，过滤出属于【评论/回复】的消息
const replyMessages = computed(() => {
  // 假设你的总列表叫 messageList，或者叫 commentMessageList
  // 筛选出 action 为 COMMENT 的通知
  return messageStore.commentMessageList.filter(msg => msg.action === 'COMMENT')
})

// 🎯 核心分流 B：过滤出属于【点赞/拉踩】的消息
const likeMessages = computed(() => {
  // 筛选出 action 为 LIKE 或者 DISLIKE 的通知，丢进“收到的赞”栏目里
  return messageStore.commentMessageList.filter(msg => msg.action === 'LIKE' || msg.action === 'DISLIKE')
})

// 🎯 动态掌控：根据当前选中的 Tab，决定有没有数据需要展示
const hasData = computed(() => {
  if (activeMenu.value === 'reply') {
    return replyMessages.value.length > 0
  } else {
    return likeMessages.value.length > 0
  }
})

const currentTitle = computed(() => {
  return activeMenu.value === 'reply' ? '回复我的' : '收到的赞'
})

const handleSelect = (index: string) => {
  activeMenu.value = index
}
</script>

<style scoped>
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
  padding: 8px 0;
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