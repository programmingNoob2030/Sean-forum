<template>
  <div class="post-card-inner">
    <div class="vote-sidebar">
      <el-icon
        class="vote-up"
        :class="{ active: post.postRatingType == 1 }"
        @click="doRatingUp"
      >
        <CaretTop />
      </el-icon>
      <span class="vote-count">{{ post.likeCount || 0 }}</span>
      <el-icon
        class="vote-down"
        :class="{ active: post.postRatingType == -1 }"
        @click="doRatingDown"
      >
        <CaretBottom />
      </el-icon>
    </div>

    <div class="post-content-area" @click="$emit('click-detail', post.id)">
      <div class="post-header-meta">
        <img :src="baseUrl + post.boardCover" class="cover" />
        <span class="community-name">r/{{ post.boardName }}</span>
        <span class="dot">•</span>
        <span class="post-author">
          Posted by
          <img
            :src="post.creatorAvatar ? baseUrl + post.creatorAvatar : '/default-user-avatar.png'"
            class="avatar"
          />
          u/{{ post.creatorName || 'SeanLi' }}
        </span>
        <span class="post-time">{{ displayTime }}</span>
      </div>

      <h3 class="post-title">{{ post.title }}</h3>

      <div v-if="summaryView.textPreview || previewImagePath" class="post-summary">
        <div v-if="summaryView.textPreview" class="summary-text">
          {{ summaryView.textPreview }}
        </div>
        <div v-if="previewImagePath" class="summary-media">
          <img
            class="summary-image"
            :src="resolveImageSrc(previewImagePath)"
            alt="帖子首图"
            loading="lazy"
            @error="handleImageError"
          />
        </div>
      </div>

      <div class="post-footer-actions">
        <div class="action-btn">
          <el-icon><ChatDotRound /></el-icon>
          <span>{{ post.commentCount || 0 }}</span>
        </div>
        <div class="action-btn">
          <el-icon><Share /></el-icon>
          <span>Share</span>
        </div>

        <div
          v-if="post.creatorName === userStore.userInfo?.name"
          class="action-btn delete-btn"
          @click.stop="handleDeletePost"
        >
          <el-icon><Delete /></el-icon>
          <span>删除</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { CaretBottom, CaretTop, ChatDotRound, Delete, Share } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { RatingDTO } from '@/models/rating/ratingTypes'
import type { PostVO } from '@/models/post/postTypes'
import { useRatingStore } from '@/models/rating/ratingStore'
import { useUserStore } from '@/models/user/userStore'
import { ensureLogin } from '@/utils/auth'
import { apiDeletePost } from '@/api/post'
import { formatPostTime } from '@/utils/timeFormat'
import { resolvePostContentView } from '@/utils/postContent'

const baseUrl = import.meta.env.VITE_RESOURCE_URL
const ratingForm = ref<RatingDTO>({
  target: '',
  targetId: 0,
  action: 0,
})

const props = defineProps<{
  post: PostVO
}>()

const emit = defineEmits(['click-detail', 'refresh'])
const ratingStore = useRatingStore()
const userStore = useUserStore()
const imageLoadFailed = ref(false)

const displayTime = computed(() => formatPostTime(props.post.createTime))

const summaryView = computed(() =>
  resolvePostContentView(
    props.post.content,
    props.post.contentFormat,
    props.post.contentNodes,
    props.post.contentTextPreview,
    props.post.firstImagePath,
  ),
)

const previewImagePath = computed(() => (imageLoadFailed.value ? '' : summaryView.value.firstImagePath))

const resolveImageSrc = (path: string) => {
  const resourceBaseUrl = import.meta.env.VITE_RESOURCE_URL ?? ''
  if (/^(https?:)?\/\//i.test(path) || path.startsWith('data:')) {
    return path
  }
  return `${resourceBaseUrl}${path}`
}

const handleImageError = () => {
  imageLoadFailed.value = true
}

const doRatingUp = async () => {
  ratingForm.value.action = 1
  ratingForm.value.target = 'POST'
  ratingForm.value.targetId = props.post.id
  ensureLogin(async () => {
    const res = await ratingStore.toggleRating(ratingForm.value)
    if (res) {
      props.post.likeCount = res.likeCount
      props.post.postRatingType = res.type
    }
  })
}

const doRatingDown = async () => {
  ratingForm.value.action = -1
  ratingForm.value.target = 'POST'
  ratingForm.value.targetId = props.post.id
  ensureLogin(async () => {
    const res = await ratingStore.toggleRating(ratingForm.value)
    if (res) {
      props.post.likeCount = res.likeCount
      props.post.postRatingType = res.type
    }
  })
}

const handleDeletePost = () => {
  ElMessageBox.confirm(
    '确定要永久删除这篇帖子吗？此操作执行逻辑删除。',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    },
  ).then(async () => {
    try {
      await apiDeletePost({ id: props.post.id })
      ElMessage.success('帖子删除成功!')
      emit('refresh')
    } catch (error) {
      console.error(error)
    }
  }).catch(() => {})
}
</script>

<style scoped>
.post-card-inner {
  display: flex;
  background-color: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: border-color 0.2s;
  overflow: hidden;
}

.post-card-inner:hover {
  border-color: #a0a0a0;
}

.vote-sidebar {
  width: 40px;
  background-color: #f8f9fa;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px 4px;
  border-right: 1px solid #e0e0e0;
}

.vote-count {
  font-size: 12px;
  font-weight: 700;
  color: #1a1a1b;
  margin: 4px 0;
}

.vote-up,
.vote-down {
  font-size: 18px;
  color: #878a8c;
  cursor: pointer;
}

.vote-up:hover {
  color: #ff4500;
}

.vote-down:hover {
  color: #7193ff;
}

.vote-up.active {
  color: #ff4500;
}

.vote-down.active {
  color: #7193ff;
}

.post-content-area {
  flex: 1;
  padding: 8px 12px;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.post-header-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #7c7c7c;
}

.community-name {
  font-weight: 700;
  color: #1a1a1b;
  cursor: pointer;
}

.community-name:hover {
  text-decoration: underline;
}

.dot {
  margin: 0 2px;
}

.post-author {
  display: flex;
  align-items: center;
  font-weight: 400;
}

.post-title {
  font-size: 18px;
  font-weight: 600;
  color: #222;
  margin: 0 0 10px 0;
  line-height: 1.4;
}

.post-summary {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  font-size: 14px;
  color: #4f4f4f;
  line-height: 1.6;
  margin-bottom: 12px;
  overflow: hidden;
}

.summary-text {
  flex: 1;
  min-width: 0;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  white-space: pre-wrap;
  word-break: break-word;
}

.summary-media {
  width: 120px;
  height: 90px;
  flex-shrink: 0;
}

.summary-image {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 4px;
  background: #f5f7fa;
}

.post-footer-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: auto;
}

.action-btn {
  display: flex;
  align-items: center;
  padding: 6px 8px;
  border-radius: 2px;
  color: #878a8c;
  font-size: 12px;
  font-weight: 700;
}

.action-btn:hover {
  background-color: #f6f7f8;
}

.delete-btn:hover {
  background-color: #ffe6e6 !important;
  color: #ff4d4d !important;
}

.action-btn .el-icon {
  font-size: 16px;
  margin-right: 6px;
}

.cover {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  object-fit: cover;
}

.avatar {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  object-fit: cover;
  margin: 0 2px;
  opacity: 0.8;
}
</style>
