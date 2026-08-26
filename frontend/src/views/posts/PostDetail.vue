<template>
  <div v-if="post && isVisible" class="post-detail-container" v-loading="loading">
    
    <div class="main-content">
      
      <div class="post-main-column">
        
        <article class="post-article-card">
          <div class="author-info">
            <img 
              :src="post.creatorAvatar ? (baseUrl + post.creatorAvatar) : '/default-user-avatar.jpg'" 
              class="avatar" 
            />       
            <div class="meta">
              <span class="name">{{ 'u/' + (post.creatorName || 'SeanLi') }}</span>
              <span class="time">{{ displayTime }}</span>
            </div>
          </div>

          <h1 class="post-title">{{ post.title }}</h1>
          
          <PostContentRenderer
            class="post-content"
            :content="post.content"
            :content-format="post.contentFormat"
            :content-nodes="post.contentNodes"
          />

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

            <el-button 
              v-if="post.creatorName === userStore.userInfo?.name" 
              class="text-action-btn delete-btn" 
              link
              @click="handleDeletePostDetail"
            >
              <el-icon><Delete /></el-icon> 删除
            </el-button>
          </div>
        </article>

        <div class="comment-section-wrapper">
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

          <div v-if="commentStore.total > 0" class="pagination-wrapper">
            <el-pagination 
              background 
              layout="prev, pager, next, jumper" 
              :total="commentStore.total"
              v-model:current-page="commentStore.dto.pageNum"
              @current-change="handlePageChange"
            />
          </div>
        </div>

      </div>

      <BoardInfo :board-id="post.boardId" :baseUrl="baseUrl" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router' 
import { useRatingStore } from '@/models/rating/ratingStore'
import { apiGetPostById, apiDeletePost } from '@/api/post' 
import { useUserStore } from '@/models/user/userStore' 
import type { PostVO } from '@/models/post/postTypes'
import { formatPostTime } from '@/utils/timeFormat';
import { CaretTop, CaretBottom, Share, Delete } from '@element-plus/icons-vue'
import CommentCard from '@/components/comment/CommentCard.vue'
import BoardInfo from '@/components/board/BoardInfo.vue'
import PostContentRenderer from '@/components/post/PostContentRenderer.vue'
import { useCommentStore } from '@/models/comment/commentStore'
import type { GetPostCommentsDTO} from '@/models/pages'
import { ensureLogin } from '@/utils/auth'
import { apiCreateComment } from '@/api/comment'
import { ElMessage, ElMessageBox } from 'element-plus' 

const baseUrl = import.meta.env.VITE_RESOURCE_URL
const commentStore = useCommentStore()
const userStore = useUserStore() 
const route = useRoute()
const router = useRouter() 
const ratingStore = useRatingStore()
const loading = ref(true)
const post = ref<PostVO>()
const postId = route.params.id

// 🌟 核心状态：页面级视觉隐身控制器
const isVisible = ref(true)

const dto = ref<GetPostCommentsDTO>({
  postId: Number(postId),
  pageNum: 1,
  pageSize: 10
})
const mainCommentContent = ref('')
const submitting = ref(false)

// 🌟 新增：最精简的详情页逻辑删除函数
const handleDeletePostDetail = () => {
  ElMessageBox.confirm('确定要将这篇帖子彻底下架吗？', '提示', {
    confirmButtonText: '确定下架',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      loading.value = true
      await apiDeletePost({ id: Number(postId) })
      ElMessage.success('帖子已成功下架!')
      
      isVisible.value = false // 物理隐身
      router.back() // 啪，直接秒退回列表页，不留一点脏数据痕迹
    } catch (error) {
      console.error(error)
    } finally {
      loading.value = false
    }
  }).catch(() => {})
}

const submitComment = async () => {
  const content = mainCommentContent.value
  if (!content?.trim()) return ElMessage.warning('内容不能为空')

  ensureLogin(async () => {
    submitting.value = true
    try {
      const commentDTO = {    
        target: 'POST',
        targetId: Number(postId),
        rootId: Number(postId),      
        rootType: 'POST',
        content: content,
        parentId: 0
      }
      await apiCreateComment(commentDTO)
      await getPostDetail()
      await commentStore.getCommentsByPostId()
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

const handleVote = async (action: number) => {
  ensureLogin(async () => {
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
  if (post.value) {
    return formatPostTime(post.value.createTime);
  }
  return ''
})

const handlePageChange = (val: number) => {
  commentStore.getCommentsByPostId(); 
  window.scrollTo(0, 0);
};

onMounted(() => {
  commentStore.reset()
  getPostDetail()
  commentStore.dto = dto.value
  commentStore.getCommentsByPostId()
})
</script>
<style scoped>
/* ==========================================================================
   1. 基础容器与经典三栏网格控制
   ========================================================================== */
.post-detail-container {
  --reddit-bg: #dae0e6;
  --reddit-blue: #0079d3;
  --text-dark: #1c1c1c;
  --border-color: #ccc;
  
  background-color: var(--reddit-bg);
  min-height: 100vh;
  padding-top: 20px;
  box-sizing: border-box;
  width: 100%;
}

.main-content {
  width: 100%;
  padding: 0 40px 0 24px; /* 右边 40px 贴边，左边与你的菜单栏保持呼吸间距 */
  box-sizing: border-box;
  
  display: grid;
  /* 🌟 大气舒展网格魔法：
     1fr: 自动吃掉左侧剩余空间，把帖子主体往右推到“视觉正中间”
     minmax(650px, 920px): 帖子黄金阅读宽度，上限扩充到 920px，饱满且有牌面
     312px: 右侧社区卡片固定宽度
  */
  grid-template-columns: 1fr minmax(650px, 920px) 312px;
  /* 强制把三栏分别推向两端与正中间 */
  justify-content: space-between; 
  align-items: flex-start;
  gap: 32px; /* 拉大两栏之间的间距，增加大局呼吸感 */
}

/* ==========================================================================
   2. 各大主力栏目轨道定位
   ========================================================================== */

/* 🌟 左/中间大主栏：锁定在 Grid 的第二轨（正中间），彻底解放宽度限制 */
.post-main-column {
  grid-column: 2;       
  width: 100%;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 20px; /* 帖子卡片和评论区之间的间距稍微拉开，不再局促 */
}

/* 🚀 右侧社区栏：锁定在第三轨，由于外层 space-between，它会被死死钉在屏幕最右侧 */
:deep(.board-info-card),
.main-content > :nth-child(2) {
  grid-column: 3;       
  width: 312px !important;
  flex-shrink: 0;
}

/* 🌟 核心重构：让帖子和评论卡片 100% 顺应中间 920px 轨道的排版 */
.post-article-card,
.comment-section-wrapper {
  width: 100%;
  background-color: #ffffff;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  padding: 28px 32px; /* 秤杆配秤砣，卡片变宽后内边距同步加大，大气不贴边 */
  box-sizing: border-box;
}

/* ==========================================================================
   3. 帖子正文及元数据大方派头排版
   ========================================================================== */
.post-title {
  font-size: 26px; /* 微微放大字号，压得住 920px 的大宽卡片 */
  font-weight: 600;
  margin: 18px 0;
  color: var(--text-dark);
  line-height: 1.3;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid #f0f0f0;
  background-color: #f8f9fa;
}

.meta {
  display: flex;
  flex-direction: column;
}

.name {
  font-weight: 700;
  font-size: 13px;
  color: var(--text-dark);
}

.time {
  font-size: 12px;
  color: #7c7c7c;
  margin-top: 2px;
}

.post-content {
  line-height: 1.7; /* 拉大行高，字数变宽后不容易看串行 */
  font-size: 15px;  /* 稍微提升到 15px，长时间阅读不易疲劳 */
  color: #1a1a1b;
  margin: 24px 0;
  word-break: break-word; /* 彻底防止长串无空格代码/英文撑破页面 */
}

/* ==========================================================================
   4. 底部操作动作条 & 投票胶囊 & 危险红悬停样式
   ========================================================================== */
.action-bar {
  display: flex;
  align-items: center;
  gap: 16px;
}

.delete-btn:hover {
  background-color: #ffe6e6 !important;
  color: #ff4d4d !important;
}

.vote-capsule {
  display: flex;
  align-items: center;
  background-color: #f6f7f8;
  border-radius: 20px;
  padding: 4px;
}

.vote-btn {
  border: none;
  background: transparent;
  padding: 4px 10px;
  cursor: pointer;
  display: flex;
  align-items: center;
  border-radius: 50%;
  transition: all 0.2s ease;
  color: #878a8c;
}

.vote-btn.up:hover,
.vote-btn.up.active { 
  color: #ff4500; 
  background-color: #ffe9e0; 
}

.vote-btn.down:hover,
.vote-btn.down.active { 
  color: #7193ff; 
  background-color: #e8eeff; 
}

.vote-num {
  font-size: 13px;
  font-weight: 700;
  min-width: 20px;
  text-align: center;
  color: var(--text-dark);
}

.text-action-btn {
  color: #878a8c !important;
  font-size: 13px;
  font-weight: 700;
}

/* ==========================================================================
   5. 评论区大框架及发布框细节
   ========================================================================== */
.comment-section-wrapper {
  margin-bottom: 40px;
}

.comment-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  color: var(--text-dark);
}

.reply-container {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background-color: #ffffff;
  overflow: hidden;
  margin-bottom: 24px;
}

.reply-container:focus-within {
  border-color: var(--reddit-blue);
}

:deep(.el-textarea__inner) {
  border: none !important;
  box-shadow: none !important;
  padding: 14px; /* 微微加深输入框的呼吸感 */
  background: transparent;
  color: var(--text-dark);
  font-size: 14px;
}

.footer-actions {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 8px 12px;
  gap: 12px;
  background-color: #fafafa;
  border-top: 1px solid #f0f0f0;
}

.comment-list {
  background-color: #ffffff;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 32px 0 12px;
  border-top: 1px solid #f0f0f0;
  margin-top: 24px;
}
</style>
