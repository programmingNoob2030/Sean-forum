<template>
  <div class="recent-post-item" @click="$emit('click-detail', post.id)">
    <div class="item-main">
      <div class="item-header">
        <el-avatar :size="16" :src="post.communityIcon" icon="User" />
        <span class="community-name">r/{{ post.communityName || 'backend_dev' }}</span>
        <span class="dot">•</span>
        <span class="post-time">{{ displayTime }}</span>
      </div>

      <h4 class="item-title">{{ post.title }}</h4>

      <div class="item-footer">
        <span class="footer-data">{{ post.likeCount || 0 }} 评分</span>
        <span class="dot">•</span>
        <span class="footer-data">{{ post.commentCount || 0 }} 评论</span>
      </div>
    </div>

    <div class="item-media" v-if="post.thumbnail">
      <el-image :src="post.thumbnail" fit="cover" />
    </div>
    <div class="item-media placeholder" v-else>
      <el-icon><Picture /></el-icon>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { Picture } from '@element-plus/icons-vue';
import { formatPostTime } from '@/utils/timeFormat';
import type { PostVO } from '@/models/post/postTypes';

const props = defineProps<{ post: PostVO }>();
defineEmits(['click-detail']);

const displayTime = computed(() => formatPostTime(props.post.createTime));
</script>

<style scoped>
.recent-post-item {
  display: flex;
  padding: 12px;
  gap: 12px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.2s;
}

.recent-post-item:hover {
  background-color: #f6f7f8;
}

.item-main {
  flex: 1;
  min-width: 0; /* 防止标题过长撑破布局 */
}

.item-header {
  display: flex;
  align-items: center;
  font-size: 11px;
  color: #787c7e;
  margin-bottom: 4px;
}

.community-name {
  color: #1a1a1b;
  font-weight: 600;
  margin: 0 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-title {
  font-size: 14px;
  font-weight: 500;
  color: #1a1a1b;
  margin: 4px 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2; /* 标题最多两行 */
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.item-footer {
  font-size: 11px;
  color: #787c7e;
  display: flex;
  align-items: center;
}

.dot { margin: 0 4px; }

.item-media {
  width: 64px;
  height: 48px;
  border-radius: 4px;
  overflow: hidden;
  flex-shrink: 0;
  background-color: #f0f0f0;
}

.item-media.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ccc;
  font-size: 20px;
}
</style>