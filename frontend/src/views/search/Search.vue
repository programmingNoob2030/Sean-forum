<template>
  <el-main class="search-main">
    <div class="search-container" v-loading="loading">
      <div class="search-heading">
        <div>
          <h2>搜索结果</h2>
          <p v-if="keyword">包含“{{ keyword }}”的帖子，共 {{ total }} 条</p>
          <p v-else>全部帖子，共 {{ total }} 条</p>
        </div>

        <el-radio-group v-model="sort" @change="handleSortChange" class="sort-group">
          <el-radio-button value="RECENT">最新</el-radio-button>
          <el-radio-button value="POPULAR">最受欢迎</el-radio-button>
          <el-radio-button value="COMMENTS">评论数</el-radio-button>
          <el-radio-button value="HOT">最热门</el-radio-button>
        </el-radio-group>
      </div>

      <el-alert
        v-if="errorMessage"
        :title="errorMessage"
        type="error"
        show-icon
        :closable="false"
        class="search-error"
      />

      <el-empty
        v-if="!loading && !errorMessage && posts.length === 0"
        :description="keyword ? '没有找到匹配的帖子' : 'No posts found'"
      />

      <div v-else class="post-list">
        <PostCard
          v-for="post in posts"
          :key="post.id"
          :post="post"
          @click-detail="goToDetail"
          @refresh="loadPosts"
        />
      </div>

      <div v-if="total > pageSize" class="pagination-wrapper">
        <el-pagination
          background
          layout="prev, pager, next, jumper"
          :total="total"
          :page-size="pageSize"
          v-model:current-page="pageNum"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </el-main>

  <el-aside width="312px" class="side-rail hidden-md-and-down">
    <RightSideBar />
  </el-aside>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { apiGetPosts } from '@/api/post'
import type { PostSort, PostVO } from '@/models/post/postTypes'
import PostCard from '@/components/post/PostCard.vue'
import RightSideBar from '@/components/index/RightSideBar.vue'

const route = useRoute()
const router = useRouter()
const posts = ref<PostVO[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 10
const sort = ref<PostSort>('RECENT')
const loading = ref(false)
const errorMessage = ref('')
let requestSequence = 0

const keyword = computed(() => {
  const queryKeyword = route.query.keyword
  const value = Array.isArray(queryKeyword) ? queryKeyword[0] : queryKeyword
  return typeof value === 'string' ? value.trim() : ''
})

const loadPosts = async () => {
  const sequence = ++requestSequence
  loading.value = true
  errorMessage.value = ''
  try {
    const result = await apiGetPosts({
      keyword: keyword.value || undefined,
      pageNum: pageNum.value,
      pageSize,
      sort: sort.value
    })
    if (sequence !== requestSequence) return
    posts.value = result.list
    total.value = result.total
  } catch (error) {
    if (sequence !== requestSequence) return
    posts.value = []
    total.value = 0
    errorMessage.value = '搜索请求失败，请稍后重试'
    ElMessage.error(errorMessage.value)
  } finally {
    if (sequence === requestSequence) {
      loading.value = false
    }
  }
}

const handlePageChange = () => {
  loadPosts()
  window.scrollTo(0, 0)
}

const handleSortChange = () => {
  pageNum.value = 1
  loadPosts()
}

const goToDetail = (id: number) => router.push(`/post/${id}`)

watch(
  () => route.query.keyword,
  () => {
    pageNum.value = 1
    loadPosts()
  },
  { immediate: true }
)
</script>

<style scoped>
.search-main {
  padding: 0;
  max-width: 640px;
  flex: 1;
}

.search-container {
  min-height: 400px;
}

.search-heading {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-bottom: 20px;
}

.search-heading h2 {
  margin: 0 0 6px;
  color: #1c1c1c;
  font-size: 22px;
}

.search-heading p {
  margin: 0;
  color: #787c7e;
  font-size: 13px;
}

.sort-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.sort-group :deep(.el-radio-button__inner) {
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  box-shadow: none;
  padding: 8px 16px;
  color: #606266;
  background-color: #fff;
}

.sort-group :deep(.el-radio-button:first-child .el-radio-button__inner) {
  border-left: 1px solid #dcdfe6;
}

.sort-group :deep(.el-radio-button__inner:hover) {
  color: #409eff;
  border-color: #409eff;
}

.sort-group :deep(.el-radio-button.is-active .el-radio-button__inner) {
  color: #409eff;
  background-color: #ecf5ff;
  border-color: #409eff;
  box-shadow: none;
}

.search-error {
  margin-bottom: 16px;
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin: 30px 0;
}

.side-rail {
  overflow: visible;
}
</style>
