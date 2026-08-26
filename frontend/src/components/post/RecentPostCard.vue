<template>
  <div class="recent-post-item" @click="$emit('click-detail', post.id)">
    <div class="item-main">
      <!-- 头部信息：板块图标 + 名称 + 时间 -->
      <div class="item-header">
        <el-avatar 
          :size="16" 
          :src="baseUrl + post.boardCover" 
          class="board-icon"
        >
          <!-- 如果没加载出图片，显示一个默认字符 -->
          {{ post.boardName?.charAt(0).toUpperCase() }}
        </el-avatar>
        <span class="community-name">r/{{ post.boardName || 'backend_dev' }}</span>
        <span class="dot">•</span>
        <span class="post-time">{{ displayTime }}</span>
      </div>

      <!-- 标题：限制显示行数 -->
      <h4 class="item-title">{{ post.title }}</h4>

      <div
        v-if="summaryView.textPreview"
        class="item-summary"
      >
        <div class="summary-text">{{ summaryView.textPreview }}</div>
      </div>

      <!-- 底部数据：点赞与评论 -->
      <div class="item-footer">
        <span class="footer-data">{{ post.likeCount || 0 }} 评分</span>
        <span class="dot">•</span>
        <span class="footer-data">{{ post.commentCount || 0 }} 评论</span>
      </div>
    </div>

    <!-- 右侧缩略图：优先使用正文首图 -->
    <div class="item-media" v-if="summaryView.firstImagePath && !imageLoadFailed">
      <el-image
        :src="resolveImageSrc(summaryView.firstImagePath)"
        fit="cover"
        class="media-img"
        loading="lazy"
        @error="handleImageError"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { formatPostTime } from '@/utils/timeFormat';
import type { RecentPostVO } from '@/models/post/postTypes';
import { resolvePostContentView } from '@/utils/postContent'

const props = defineProps<{ post: RecentPostVO }>();
defineEmits(['click-detail']);
const baseUrl = import.meta.env.VITE_RESOURCE_URL;
const imageLoadFailed = ref(false)

const displayTime = computed(() => formatPostTime(props.post.createTime));

const summaryView = computed(() => resolvePostContentView(
  props.post.content,
  props.post.contentFormat,
  props.post.contentNodes,
  props.post.contentTextPreview,
  props.post.firstImagePath,
))

const resolveImageSrc = (path: string) => {
  if (/^(https?:)?\/\//i.test(path) || path.startsWith('data:')) {
    return path
  }
  return `${baseUrl}${path}`
}

const handleImageError = () => {
  imageLoadFailed.value = true
}
</script>

<style scoped>
.recent-post-item {
  display: flex;
  /* 增加左右 padding 以补偿父容器去掉的 padding，确保文字不贴边 */
  padding: 16px 14px; 
  gap: 16px;
  cursor: pointer;
  transition: all 0.2s ease;
  align-items: flex-start;
  
  /* 核心修改：加深分割线颜色，确保在白色背景上清晰可见 */
  border-bottom: 1px solid #edeff1; 
  
  /* 悬停时稍微圆角化，看起来更精致 */
  background-color: #ffffff;
}

/* 最后一项去掉边框 */
.recent-post-item:last-child {
  border-bottom: none;
}

.recent-post-item:hover {
  background-color: #f6f7f8;
}

.item-main {
  flex: 1;
  min-width: 0;
}

.item-header {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #787c7e;
  margin-bottom: 4px;
}

.board-icon {
  margin-right: 4px;
  flex-shrink: 0;
}

.community-name {
  color: #1a1a1b;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-title {
  font-size: 14px;
  font-weight: 500;
  color: #1a1a1b;
  margin: 6px 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-word;
}

.item-summary {
  margin-bottom: 8px;
}

.summary-text {
  font-size: 12px;
  color: #4f4f4f;
  line-height: 1.6;
  max-height: 64px;
  overflow: hidden;
  white-space: pre-wrap;
  word-break: break-word;
}

.item-footer {
  font-size: 11px;
  color: #787c7e;
  display: flex;
  align-items: center;
}

.dot { margin: 0 4px; font-size: 10px; }

.item-media {
  width: 80px;
  height: 60px;
  border-radius: 4px;
  overflow: hidden;
  flex-shrink: 0;
  background-color: #f6f7f8;
  /* 图片也要有个极细的边框，防止全白图片看不清边缘 */
  border: 1px solid #edeff1;
}

.media-img {
  width: 100%;
  height: 100%;
}
</style>
