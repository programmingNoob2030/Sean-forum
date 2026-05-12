<template>
  <div class="comment-card-wrapper">
    <div class="comment-left-sidebar">
      <img 
          :src="comment.creatorAvatar ? (baseUrl + comment.creatorAvatar) : '/default-user-avatar.png'" 
          class="avatar" 
        />   
    </div>

    <div class="comment-right-main">
      <div class="comment-header">
        <span class="username">{{ comment.creatorName || '此用户已注销' }}</span>
        <span class="time">{{ displayTime }}</span>
      </div>
      
      <div class="comment-body">
        <p class="comment-content">{{ comment.content }}</p>
      </div>
      
      <div class="comment-actions">
        <div class="vote-capsule">
          <button 
            class="vote-btn up" 
            :class="{ active: comment.commentRatingType === 1 }" 
            @click="handleVote(1)"
          >
            <el-icon><CaretTop /></el-icon>
          </button>
          
          <span class="vote-num">{{ comment.likeCount || 0 }}</span>
          
          <button 
            class="vote-btn down" 
            :class="{ active: comment.commentRatingType === -1 }" 
            @click="handleVote(-1)"
          >
            <el-icon><CaretBottom /></el-icon>
          </button>
        </div>

        <el-button link class="action-btn" @click="toggleReplyInput">
          <el-icon><ChatDotRound /></el-icon> 
          {{ isReplyInputVisible ? '取消回复' : '回复' }}
        </el-button>
        
        <el-button link class="action-btn">
          <el-icon><Share /></el-icon> 分享
        </el-button>
        
        <el-button link class="action-btn report-btn">
          <el-icon><Warning /></el-icon> 举报
        </el-button>
      </div>
      <transition name="el-zoom-in-top">
        <div v-if="isReplyInputVisible" class="reply-input-container">
          <el-input
            v-model="replyText"
            type="textarea"
            :rows="3"
            :placeholder="`回复 @${comment.creatorName}...`"
            resize="none"
            class="custom-textarea"
          />
          <div class="reply-footer">
            <span class="reply-tip">请自觉遵守社区规范</span>
            <div class="reply-btns">
              <el-button size="small" round @click="isReplyInputVisible = false">取消</el-button>
              <el-button 
                size="small" 
                type="primary" 
                round 
                :disabled="!replyText.trim()"
                @click="handleReplySubmit"
              >
                发布回复
              </el-button>
            </div>
          </div>
        </div>
      </transition>
      
      <div class="replies-section">
        <sub-comment-item 
          v-for="reply in displayedChildren" 
          :key="reply.id" 
          :comment="reply" 
        />
        <el-button 
          v-if="comment.children.length > 2 && !isExpanded" 
          link 
          type="primary"
          @click="isExpanded = true"
        >
          展开全部 {{ comment.children.length }} 条回复
        </el-button>
        
        <el-button 
          v-if="isExpanded && comment.children.length > 2" 
          link 
          @click="isExpanded = false"
        >
          收起回复
        </el-button>
      </div>
    </div>
    
    
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { CaretTop, CaretBottom, ChatDotRound, Share, Warning } from '@element-plus/icons-vue'
import type { CommentVO } from '@/models/comment/commentTypes'
import { formatPostTime } from '@/utils/timeFormat'
import { useRatingStore } from '@/models/rating/ratingStore'
import { ensureLogin } from '@/utils/auth'
import { apiCreateComment } from '@/api/comment'
import { useCommentStore } from '@/models/comment/commentStore'
import SubCommentItem from '@/components/comment/SubCommentItem.vue'
import { ElMessage } from 'element-plus'
const baseUrl = import.meta.env.VITE_RESOURCE_URL
const props = defineProps<{
  comment: CommentVO 
  rootId?: number
}>()

// 是否展开
const isExpanded = ref(false)
// 回复相关状态
const route = useRoute()
const isReplyInputVisible = ref(false)
const replyText = ref('')
const commentStore = useCommentStore()
const toggleReplyInput = () => {
  isReplyInputVisible.value = !isReplyInputVisible.value
  if (!isReplyInputVisible.value) {
    replyText.value = ''
  }
}
const displayedChildren = computed(() => {
  if (isExpanded.value) {
    return props.comment.children
  }
  return props.comment.children.slice(0, 2).filter(Boolean)
})

// 发送子评论
const handleReplySubmit = async () => {
  ensureLogin(async () => {
    try {
      const content = replyText.value
      const commentDTO = {    
        target: 'COMMENT',
        targetId:  props.comment.id,
        rootId: Number(route.params.id),
        rootType: 'POST',
        content: content,
        parentId: props.comment.parentId === 0 ? props.comment.id : props.comment.parentId
      }
      await apiCreateComment(commentDTO)
      await commentStore.getCommentsByPostId() // 刷新列表
      ElMessage.success("评论发布成功!")

    } finally {
      replyText.value = ''
      isReplyInputVisible.value = false
    }
  })
}

// 点赞逻辑
const ratingStore = useRatingStore()
const defaultAvatarUrl = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const displayTime = computed(() => {
  return props.comment ? formatPostTime(props.comment.createTime) : ''
})

const handleVote = async (action: number) => {
  ensureLogin(async () => {
    const res = await ratingStore.toggleRating({
      targetId: props.comment.id,
      target: 'COMMENT',
      action: action
    })
    if (res) {
      props.comment.commentRatingType = res.type
      props.comment.likeCount = res.likeCount
    }
  })
}
</script>

<style scoped>
/* 1. 基础布局容器 */
.comment-card-wrapper {
  display: flex;
  gap: 12px;
  padding: 16px 8px;
  border-bottom: 1px solid #f2f2f2;
  background-color: #fff;
  transition: background-color 0.2s;
}

.comment-card-wrapper:hover {
  background-color: #fbfbfb;
}

/* 2. 左侧头像区 */
.comment-left-sidebar {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 32px;
  flex-shrink: 0;
}
.avatar {
  /* 尺寸：评论区标准尺寸通常在 28px - 32px */
  width: 32px;
  height: 32px;
  
  /* 基础形状 */
  border-radius: 50%;
  object-fit: cover;
  
  /* 核心布局属性 */
  flex-shrink: 0; /* 必须加！防止评论字数多时头像被挤扁 */
  display: block;
  
  /* 视觉微调 */
  background-color: #f0f2f5; /* 占位色：图片加载前或加载失败时的底色 */
  
  /* 交互 */
  cursor: pointer;
  transition: filter 0.2s ease;
}

/* 交互反馈 */
.avatar:hover {
  filter: brightness(0.9); /* 悬停时稍微变暗，比调透明度更具质感 */
}

/* 3. 右侧主轴区 (核心修正点) */
.comment-right-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0; /* 防止长文本撑破 flex 布局 */
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.username {
  font-weight: bold;
  font-size: 13px;
  color: #333;
}

.time {
  font-size: 12px;
  color: #999;
}

.comment-body {
  padding-right: 20px;
}

.comment-content {
  font-size: 14px;
  line-height: 1.6;
  color: #1a1a1b;
  margin: 0;
  white-space: pre-wrap;
}

/* 4. 操作栏 */
.comment-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 8px;
}

.vote-capsule {
  display: flex;
  align-items: center;
  background-color: #f6f7f8;
  border-radius: 20px;
  padding: 2px;
}

.vote-btn {
  border: none;
  background: transparent;
  padding: 4px 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  transition: all 0.2s;
  color: #878a8c;
}

.vote-btn.up:hover { background-color: #ffe9e0; color: #ff4500; }
.vote-btn.up.active { background-color: #ff4500; color: #ffffff; }
.vote-btn.down:hover { background-color: #e8eeff; color: #7193ff; }
.vote-btn.down.active { background-color: #7193ff; color: #ffffff; }

.vote-num {
  font-size: 12px;
  font-weight: 700;
  color: #1a1a1b;
  min-width: 20px;
  text-align: center;
}

.action-btn {
  color: #878a8c !important;
  font-size: 12px;
  padding: 4px 8px !important;
  font-weight: 600;
}

/* 5. 回复输入框 */
.reply-input-container {
  margin-top: 12px;
  padding: 12px;
  background-color: #f6f7f8;
  border-radius: 8px;
  border: 1px solid #edeff1;
}

.custom-textarea :deep(.el-textarea__inner) {
  box-shadow: none;
  border: 1px solid #edeff1;
  font-size: 14px;
}

.custom-textarea :deep(.el-textarea__inner:focus) {
  border-color: #0079d3;
}

.reply-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

.reply-tip {
  font-size: 12px;
  color: #999;
}

.reply-btns {
  display: flex;
  gap: 8px;
}

/* 6. 子评论展示区 (新加) */
.replies-section {
  margin-top: 12px;
  padding: 8px 12px;
  background-color: #f8f8f8; /* 建立视觉层级感 */
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.more-btn {
  font-size: 13px;
  margin-top: 4px;
  padding: 0 !important;
  justify-content: flex-start;
}
</style>