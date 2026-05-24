<template>
  <el-header class="reddit-header">
    <div class="nav-container">
      <div class="brand" @click="$emit('refresh')">
        <div class="reddit-logo">S</div>
        <span class="logo-text">Sean's Forum</span>
      </div>
      
      <el-input 
        v-model="search" 
        placeholder="Search Sean's Forum" 
        class="reddit-search"
        clearable
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>

      <div class="user-actions">
        <button class="btn-outline" @click="$emit('create-post')"> + 创建帖子</button>
        <div class="notification-icon" @click="handleNotificationClick">
          <el-badge :value="messageStore.unreadCount" :hidden="messageStore.unreadCount === 0" :max="99" class="badge-item">
            <el-icon :size="22" class="bell-icon"><Bell /></el-icon>
          </el-badge>
        </div>
        <el-avatar 
          :size="32" 
          :src="baseUrl + userStore.userInfo?.avatar || '/default-user-avatar.png'" 
          :icon="!userStore.userInfo?.avatar ? User : ''"
          style="margin-left: 15px; cursor: pointer;" 
          @click="handleUserClick" 
        />
        
      </div>
    </div>
  </el-header>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { Search, User, Bell } from '@element-plus/icons-vue'
import { useUserStore } from '@/models/user/userStore'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ensureLogin } from '@/utils/auth'
import { apiQueryUnreadMessageCount } from '@/api/message'
import { useMessageStore } from '@/models/message/messageStore'

const props = defineProps(['modelValue'])
const emit = defineEmits(['update:modelValue', 'create-post', 'refresh'])

const baseUrl = import.meta.env.VITE_RESOURCE_URL
const userStore = useUserStore()
const router = useRouter()
const messageStore = useMessageStore()
// 这里的 search 使用计算属性实现 v-model 绑定
const search = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const handleNotificationClick = ()=>{
  ensureLogin(()=>{
      router.push('/message-detail')
  })
}
const handleUserClick = () => {
  if (userStore.token && userStore.userInfo) {
    router.push('/profile')
  } else {
    router.push('/login')
  }
}
onMounted(async()=>{
  await messageStore.getUnreadCount()
})
</script>
<style scoped>
.reddit-header {
  background: #ffffff;
  border-bottom: 1px solid #edeff1;
  height: 48px !important;
  position: sticky;
  top: 0;
  z-index: 2000;
  padding: 0 20px;
}

.nav-container {
  max-width: 1280px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.brand { cursor: pointer; display: flex; align-items: center; }
.reddit-logo {
  background: #FF4500; color: white; width: 32px; height: 32px;
  border-radius: 50%; display: flex; align-items: center; justify-content: center;
  font-weight: bold; margin-right: 8px;
}
.logo-text { font-size: 18px; font-weight: 600; color: #1c1c1c; }

.reddit-search { flex: 1; max-width: 600px; margin: 0 40px; }

.user-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
}
.btn-outline {
  border: 1px solid #0079d3;
  color: #0079d3;
  background: transparent;
  padding: 6px 16px;
  border-radius: 20px;
  font-weight: 700;
  cursor: pointer;
}
.notification-icon {
  margin-left: 15px;
  cursor: pointer;
  display: flex;
  align-items: center;
  color: #1a1a1b; /* 保持跟 Reddit 风格一致的深色 */
  transition: color 0.2s;
}

.notification-icon:hover {
  color: #0079d3; /* 悬停变蓝色 */
}

.bell-icon {
  vertical-align: middle;
}
</style>