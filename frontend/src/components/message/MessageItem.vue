<template>
  <div class="message-item">
    <div 
      class="msg-avatar" 
      :class="{ 
        'like-avatar-bg': item.action === 'LIKE',
        'dislike-avatar-bg': item.action === 'DISLIKE' 
      }"
    >
      <img v-if="item.avatar" :src="baseUrl + item.avatar" class="user-avatar-img" alt="avatar" />
      <el-icon v-else>
        <User v-if="item.action === 'COMMENT'" />
        <Pointer :class="{ 'dislike-icon-flip': item.action === 'DISLIKE' }" v-else />
      </el-icon>
    </div>
    
    <div class="msg-main">
      <div class="msg-meta">
        <span class="username">{{ item.name || '神秘用户' }}</span>
        
        <span class="action-text">{{ actionLabel }}</span>
        
        <span class="msg-time">{{ displayTime }}</span>
      </div>
      
      <div v-if="item.action === 'COMMENT'" class="msg-body">
        {{ item.content }}
      </div>
      
      <div v-if="item.reference" class="parent-post-quote" :class="{ 'dislike-border': item.action === 'DISLIKE' }">
        <span class="quote-prefix-text">{{ quotePrefix }}</span>
        
        <span class="reference-content">“{{ item.reference }}”</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { formatPostTime } from '@/utils/timeFormat';
import { User, Pointer } from '@element-plus/icons-vue'

const baseUrl = import.meta.env.VITE_RESOURCE_URL

// 声明 Props，完美契合后端扁平化数据契约
const props = defineProps({
  item: {
    type: Object, // 映射后端的 MessageVO 结构
    required: true
  }
})

// 💡 避坑指南：将时间格式化包裹在 computed 里，防止翻页或刷新时数据死板
const displayTime = computed(() => {
  return formatPostTime(props.item.createTime, true)
})

// ================== 🎯 状态机分流：消除 HTML 中的文案判定重复 ==================

// 🔥 核心计算属性 A：负责上方核心消息提示文本的流转
const actionLabel = computed(() => {
  const isPost = props.item.target === 'POST';
  
  switch (props.item.action) {
    case 'COMMENT':
      return isPost ? '回复了你的帖子' : '回复了你的评论';
    case 'LIKE':
      return isPost ? '点赞了你的帖子' : '点赞了你的评论';
    case 'DISLIKE':
      return isPost ? '踩了你的帖子' : '踩了你的评论';
    default:
      return '互动了你的内容'; // 兜底防御
  }
})

// 🔥 核心计算属性 B：负责下方引用框框里的前缀文本流转
// 🔥 视觉瘦身：去掉重复的动词，让引用前缀回归单纯的上下文载体
const quotePrefix = computed(() => {
  const isPost = props.item.target === 'POST';
  
  // 上面已经提示过“点赞/踩/回复”了，下面这里只负责告诉用户被互动的【载体类型】即可
  return isPost ? '原帖：' : '原评论：';
})
</script>

<style scoped>
.message-item {
  display: flex;
  padding: 16px 20px;
  border-bottom: 1px solid #f6f7f8;
  gap: 12px;
  transition: background-color 0.2s;
}

.message-item:hover {
  background-color: #f8f9fa;
}

.msg-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background-color: #edeff1;
  color: #7c7c7c;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
  overflow: hidden; /* 🎯 确保用户真实头像被切成完美的圆形 */
}

.user-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* ✨ 点赞状态下的头像专属背景色（亮橙色/暖色调） */
.like-avatar-bg {
  background-color: #ffeef0;
  color: #ff4500;
}

/* ✨ 拉踩状态下的头像专属背景色（高级深空灰/警告色） */
.dislike-avatar-bg {
  background-color: #f0f2f5;
  color: #666666;
}

/* ✨ 灵魂样式：如果是 DISLIKE，利用 CSS 属性将大拇指图标直接垂直翻转 180 度看向地面！ */
.dislike-icon-flip {
  transform: rotate(180deg);
}

.msg-main {
  display: flex;
  flex-direction: column;
  gap: 6px;
  width: 100%;
}

.msg-meta {
  display: flex;
  align-items: center;
  font-size: 13px;
  gap: 8px;
}

.username {
  font-weight: 600;
  color: #1c1c1c;
}

.action-text {
  color: #7c7c7c;
}

.msg-time {
  color: #a8a8a8;
  font-size: 12px;
  margin-left: auto;
}

.msg-body {
  font-size: 14px;
  color: #1c1c1c;
  line-height: 1.4;
  word-break: break-all;
}

.parent-post-quote {
  font-size: 12px;
  color: #7c7c7c;
  background-color: #f6f7f8;
  padding: 8px 12px;
  border-left: 3px solid #0079d3; /* 默认回复、点赞的高亮 Reddit 蓝边 */
  border-radius: 0 4px 4px 0;
  margin-top: 4px;
  word-break: break-all;
}

/* ✨ 拉踩状态下，引用框框的左侧竖线自适应变为灰色，强化视觉暗示 */
.dislike-border {
  border-left: 3px solid #666666 !important;
}

.quote-prefix-text {
  font-weight: 500;
}

.reference-content {
  font-style: italic; /* 让引用的原文略微倾斜，更显精致 */
}
</style>