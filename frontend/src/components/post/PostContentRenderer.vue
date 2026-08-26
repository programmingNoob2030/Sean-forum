<template>
  <div class="post-content-renderer">
    <template v-if="renderedContent.nodes.length > 0">
      <template
        v-for="(node, index) in renderedContent.nodes"
        :key="`${node.type}-${index}`"
      >
        <span v-if="node.type === 'text'" class="content-text">{{ node.text }}</span>

        <div v-else class="content-image-block">
          <img
            v-if="!failedImages.has(index)"
            class="content-image"
            :src="resolveImageSrc(node.path)"
            alt="帖子正文图片"
            loading="lazy"
            @error="handleImageError(index)"
          />
          <div v-else class="content-image-fallback">
            图片加载失败
          </div>
        </div>
      </template>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, watch } from 'vue'
import type { PostContentFormat, PostContentNode } from '@/models/post/postTypes'
import { resolvePostContentView } from '@/utils/postContent'

const props = withDefaults(defineProps<{
  content?: string
  contentFormat?: PostContentFormat
  contentNodes?: PostContentNode[]
  contentTextPreview?: string
  firstImagePath?: string
}>(), {
  content: '',
  contentFormat: undefined,
  contentNodes: undefined,
  contentTextPreview: '',
  firstImagePath: '',
})

const resourceBaseUrl = import.meta.env.VITE_RESOURCE_URL ?? ''
const failedImages = reactive(new Set<number>())

const renderedContent = computed(() => resolvePostContentView(
  props.content,
  props.contentFormat,
  props.contentNodes,
  props.contentTextPreview,
  props.firstImagePath,
))

watch(
  () => [
    props.content,
    props.contentFormat,
    props.contentTextPreview,
    props.firstImagePath,
    JSON.stringify(props.contentNodes ?? []),
  ],
  () => {
    failedImages.clear()
  },
)

function resolveImageSrc(path: string) {
  if (/^(https?:)?\/\//i.test(path) || path.startsWith('data:')) {
    return path
  }
  return `${resourceBaseUrl}${path}`
}

function handleImageError(index: number) {
  failedImages.add(index)
}
</script>

<style scoped>
.post-content-renderer {
  font-size: 15px;
  line-height: 1.8;
  color: #1a1a1b;
  word-break: break-word;
  white-space: pre-wrap;
}

.content-text {
  white-space: pre-wrap;
}

.content-image-block {
  margin: 12px 0;
}

.content-image {
  display: block;
  max-width: 100%;
  height: auto;
  border-radius: 4px;
}

.content-image-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 160px;
  padding: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background: #f5f7fa;
  color: #909399;
  font-size: 13px;
}
</style>
