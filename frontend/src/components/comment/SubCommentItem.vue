<template>
  <div class="sub-comment-item">
    <div class="sub-left-sidebar">
      <el-avatar :size="32" :src="defaultAvatarUrl" />
    </div>
    <div class="sub-right-sidebar">
    <div class="sub-header">
      <span class="sub-username">{{ comment.creatorName || '此用户已注销' }}</span>
      <span class="sub-time">{{ displayTime }}</span>
    </div>
    <div class="sub-content">
      {{ comment.content }}
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
  </div>
  </div>
</template>

<script setup lang="ts">
import type { CommentVO } from '@/models/comment/commentTypes';
import { formatPostTime } from '@/utils/timeFormat';
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { CaretTop, CaretBottom, ChatDotRound, Share, Warning } from '@element-plus/icons-vue'

import { useRatingStore } from '@/models/rating/ratingStore'
import { ensureLogin } from '@/utils/auth'
import { apiCreateComment } from '@/api/comment'
import { useCommentStore } from '@/models/comment/commentStore'
import { ElMessage } from 'element-plus';
const props = defineProps<{
  comment: CommentVO // 或者使用你定义的 CommentVO 接口
}>()
const displayTime = computed(() => {
  if (props.comment){
    return formatPostTime(props.comment.createTime,true);
  }
});

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
// 发送子评论
const handleReplySubmit = async () => {
  ensureLogin(async () => {
    try {
      const content = `回复 @${props.comment.creatorName}: ` + replyText.value
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
      ElMessage.success("评论发送成功!")
    } finally {
      replyText.value = ''
      isReplyInputVisible.value = false
    }
  })
}

// 点赞逻辑
const ratingStore = useRatingStore()
const defaultAvatarUrl = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

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
.sub-comment-item {
  display: flex;
  gap: 12px;
  padding: 16px 8px;
  border-bottom: 1px solid #f2f2f2;
  transition: background-color 0.2s;
}
.sub-left-sidebar {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 32px;
  flex-shrink: 0;
}

.comment-right-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0; /* 防止长文本撑破 flex 布局 */
}
.sub-comment-item:last-child {
  border-bottom: none;
}
.sub-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}
.sub-username {
  font-size: 13px;
  font-weight: 600;
  color: #333;
}
.sub-time {
  font-size: 12px;
  color: #999;
}
.sub-content {
  font-size: 13px;
  line-height: 1.5;
  color: #1a1a1b;
  white-space: pre-wrap;
}
.sub-action-btn {
  font-size: 12px;
  color: #878a8c !important;
  padding: 0 !important;
}
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
</style>