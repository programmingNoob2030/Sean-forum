<template>
  <div class="post-detail-container" v-loading="loading">
    <el-card v-if="post" class="post-card">
      <div class="author-info">
        <img 
          :src="post.creatorAvatar ? (baseUrl + post.creatorAvatar) : '/default-user-avatar.jpg'" 
          class="avatar" 
        />       
        <div class="meta">
          <span class="name">{{ 'u/' + post.creatorName || 'SeanLi'}}</span>
          <span class="time">{{ displayTime }}</span>
        </div>
      </div>
      <h1 class="post-title">{{ post.title }}</h1>
      <el-divider />

      <div class="post-content" v-html="post.content"></div>

      <el-divider />

      <div class="action-bar">
        <div class="vote-capsule">
          <button class="vote-btn up" :class="{ active: post.postRatingType === 1 }" @click="handleVote(1)">
            <el-icon><CaretTop /></el-icon>
          </button>
          <span class="vote-num">{{ post.likeCount || 0 }}</span>
          <button class="vote-btn down" :class="{ active: post.postRatingType === -1 }" @click="handleVote(-1)">
            <el-icon><CaretBottom /></el-icon>
          </button>
        </div>
        <el-button class="text-action-btn" link>
          <el-icon><Share /></el-icon> 分享
        </el-button>
      </div>
    </el-card>

    <div class="comment-section-wrapper" v-if="post">
      <h3 class="comment-title">评论 ({{ post.commentCount || 0 }})</h3>
      
      <div class="reply-container">
        <el-input
          v-model="mainCommentContent"
          type="textarea"
          :rows="3"
          placeholder="写下你的评论..."
          resize="none"
        />
        <div class="footer-actions">
          <el-button v-if="mainCommentContent" link @click="mainCommentContent = ''">取消</el-button>
          <el-button 
            type="primary" 
            size="small" 
            :disabled="!mainCommentContent.trim()"
            @click="submitComment()"
          >
            发表评论
          </el-button>
        </div>
      </div>
      <div class="comment-list">
        <CommentCard 
          v-for="item in commentStore.commentList" 
          :key="item.id" 
          :comment="item"
        />    
      </div>
    </div>
  </div>
  <div v-if="commentStore.total > 0" style="display: flex; justify-content: center; margin: 30px 0;">
            <el-pagination 
              background 
              layout="prev, pager, next, jumper" 
              :total=commentStore.total
              v-model:current-page=commentStore.dto.pageNum
              @current-change=handlePageChange
            />
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useRatingStore } from '@/models/rating/ratingStore' // 假设你的 store 路径
import { apiGetPostById } from '@/api/post'
import type { PostVO } from '@/models/post/postTypes'
import { formatPostTime } from '@/utils/timeFormat';
import { CaretTop, CaretBottom,Share} from '@element-plus/icons-vue'
import CommentCard from '@/components/comment/CommentCard.vue' // 引入组件
import { useCommentStore } from '@/models/comment/commentStore'
import type { GetPostCommentsDTO } from '@/models/pages'
import { ensureLogin } from '@/utils/auth'
import { ElMessage } from 'element-plus'
import { apiCreateComment } from '@/api/comment'

const baseUrl = import.meta.env.VITE_RESOURCE_URL
const commentStore = useCommentStore()
const route = useRoute()
const ratingStore = useRatingStore()
const loading = ref(true)
const post = ref<PostVO>()
const postId = route.params.id
const dto = ref<GetPostCommentsDTO>({
  postId: Number(postId),
  pageNum: 1,
  pageSize: 10
})
const mainCommentContent = ref('')
const submitting = ref(false)

// 发送最顶级评论
const submitComment = async () => {
  const content = mainCommentContent.value
  if (!content?.trim()) {
    return ElMessage.warning('内容不能为空')
  }
  ensureLogin(async () => {
    submitting.value = true
    try {
      const commentDTO = {    
        target: 'POST',
        targetId: Number(route.params.id), // 当前帖子的ID
        rootId: Number(route.params.id),      
        rootType:'POST',
        content: content,
        parentId: 0 // 0 代表主评，非 0 代表回复
      }
      await apiCreateComment(commentDTO)
      await getPostDetail()
      await commentStore.getCommentsByPostId() // 刷新列表

      ElMessage.success("评论发布成功!")
    } finally {
      mainCommentContent.value = ''
      submitting.value = false
    }
  })
}
const getPostDetail = async () => {
  try {
    const res = await apiGetPostById(Number(postId))
    post.value = res
  } finally {
    loading.value = false
  }
}
// 2. 复用点赞逻辑
const handleVote = async (action: number) => {
  ensureLogin(async()=>{
      const res = await ratingStore.toggleRating({
      targetId: Number(postId),
      target: 'POST',
      action: action
    })
    if (res && post.value) {
      post.value.postRatingType = res.type
      post.value.likeCount = res.likeCount
    }
  })
}
const displayTime = computed(() => {
  if (post.value){
    return formatPostTime(post.value.createTime);
  }
});
const handlePageChange = (val: number) => {
  // val 就是点击后的新页码，虽然 v-model 已经改了值，但我们得手动重新发请求
  commentStore.getCommentsByPostId(); 
  
  // 战神提示：翻页后通常建议让页面滚回顶部
  window.scrollTo(0, 0);
};

onMounted(()=>{
    commentStore.reset()
    getPostDetail()
    commentStore.dto = dto.value
    commentStore.getCommentsByPostId()
  
})
</script>

<style scoped>
/* 1. 主容器：负责全局对齐和宽度控制 */
.post-detail-container {
  max-width: 800px;
  margin: 20px auto;
  padding: 0 15px;
}

.post-card {
  margin-bottom: 24px;
  border-radius: 8px;
}

.post-title {
  font-size: 28px;
  margin-bottom: 20px;
  color: #1a1a1b;
}

/* 作者信息 */
.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.meta {
  display: flex;
  flex-direction: column;
}

.name {
  font-weight: bold;
  font-size: 14px;
}

.time {
  font-size: 12px;
  color: #909399;
}

/* 正文内容 */
.post-content {
  line-height: 1.8;
  font-size: 16px;
  color: #1a1a1b;
}

/* 2. 操作栏与胶囊投票器 */
.action-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 16px;
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
  padding: 6px 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  border-radius: 18px;
  transition: all 0.2s;
  color: #878a8c;
}

.vote-btn.up:hover { color: #ff4500; background-color: #ffe9e0; }
.vote-btn.down:hover { color: #7193ff; background-color: #e8eeff; }

.vote-btn.up.active { color: #ff4500; background-color: #ffe9e0; }
.vote-btn.down.active { color: #7193ff; background-color: #e8eeff; }

.vote-num {
  font-size: 13px;
  font-weight: 700;
  min-width: 24px;
  text-align: center;
  color: #1a1a1b;
}
.avatar {
  /* 基础尺寸 */
  width: 44px;
  height: 44px;
  
  /* 形状与展示 */
  border-radius: 50%;     /* 圆形 */
  object-fit: cover;      /* 核心：确保图片不被拉伸，自动裁切 */
  display: block;         /* 消除 inline 元素底部的微小间隙 */
  
  /* 细节提升 */
  border: 1.5px solid #f0f0f0; /* 给头像加一个极细的浅色边，防止白色头像融入白色背景 */
  background-color: #f8f9fa;   /* 图片加载出来前的背景占位色 */
  
  /* 交互感 */
  cursor: pointer;
  transition: opacity 0.2s ease; /* 鼠标悬停时的平滑过渡 */
}

/* 鼠标悬停效果：轻轻变淡，暗示可以点击 */
.avatar:hover {
  opacity: 0.85;
}

.text-action-btn {
  color: #878a8c !important;
  font-size: 14px;
  font-weight: 600;
}

/* 3. 评论区布局：继承父级宽度 */
.comment-section-wrapper {
  margin-top: 32px;
}

.comment-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #1a1a1b;
}

.comment-list {
  background-color: #fff;
  border-radius: 8px;
  /* 如果希望评论区也有边框感，可以取消下面注释 */
  /* border: 1px solid #ebeef5; */
}

/* 4. 分页居中 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}

/* 统一的输入框容器 */
.reply-container {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background-color: #fff;
  overflow: hidden; /* 保证子元素不会超出圆角 */
  margin-bottom: 20px;
}

.reply-container:focus-within {
  border-color: #409eff;
}

/* 去掉输入框自带的边框 */
:deep(.el-textarea__inner) {
  border: none !important;
  box-shadow: none !important;
  padding: 12px;
  background: transparent;
}

/* 按钮区域：直接靠标准流排列在输入框下方 */
.footer-actions {
  display: flex;
  justify-content: flex-end; /* 按钮靠右 */
  align-items: center;
  padding: 4px 12px;
  gap: 12px;
  background-color: #fafafa; /* 给按钮区一个微弱的背景色，区分输入区 */
  border-top: 1px solid #f0f0f0; /* 可选：加一条分割线 */
}

/* 二级迷你回复框可以复用上面的结构，只需要把 margin 调小一点即可 */
.mini-reply-container {
  @extend .reply-container; /* 如果用 scss 可以直接继承 */
  margin-top: 10px;
  font-size: 13px;
}
</style>