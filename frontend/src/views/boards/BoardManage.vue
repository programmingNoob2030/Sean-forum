<template>
  <div class="board-page-container">
    <div class="page-header">
      <h1 class="page-title">管理社区</h1>
      <p class="page-desc">打理或查看你所归属的社区空间</p>
    </div>

    <el-tabs v-model="activeTab" class="desktop-boards-tabs">
      <el-tab-pane label="我加入的" name="joined">
        <div v-loading="loading" class="boards-list-container">
          <div
            v-for="board in joinedBoardList"
            :key="board.id"
            class="board-manage-row"
            @click="handleNavigate(board.id)"
          >
            <div class="row-left">
              <BriefBoardItem
                :id="board.id"
                :name="board.name"
                :cover="board.cover"
                :role="board.role || undefined"
                class="embedded-item"
                :isOnlyShow="true"
              />
              <p class="row-description">
                {{ board.description || '暂无社区简介。探索更多关于 r/' + board.name + ' 的有趣内容。' }}
              </p>
            </div>

            <div class="row-right">
              <span class="visitor-count">每周访客: {{ formatNumber(board.weeklyVisitor) }}</span>
              <el-button
                class="btn-action btn-joined"
                round
                size="small"
                :loading="leavingBoardId === board.id"
                @click.stop="handleLeave(board)"
              >
                已加入
              </el-button>
            </div>
          </div>

          <div v-if="!loading && joinedBoardList.length === 0" class="empty-hint">
            暂无加入的社区
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="我管理的" name="managed">
        <div v-loading="loading" class="boards-list-container">
          <div
            v-for="board in managedBoardList"
            :key="board.id"
            class="board-manage-row"
            @click="handleNavigate(board.id)"
          >
            <div class="row-left">
              <BriefBoardItem
                :id="board.id"
                :name="board.name"
                :cover="board.cover"
                :role="board.role || undefined"
                class="embedded-item"
                :isOnlyShow="true"
              />
              <p class="row-description">
                {{ board.description || '暂无社区简介。探索更多关于 r/' + board.name + ' 的有趣内容。' }}
              </p>
            </div>

            <div class="row-right">
              <span class="visitor-count">每周访客: {{ formatNumber(board.weeklyVisitor) }}</span>
              <el-button
                class="btn-action btn-manage"
                round
                size="small"
                @click.stop="handleManage(board.id)"
              >
                <el-icon><Setting /></el-icon>
                <span>管理社区</span>
              </el-button>
            </div>
          </div>

          <div v-if="!loading && managedBoardList.length === 0" class="empty-hint">
            暂无管理的社区
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Setting } from '@element-plus/icons-vue'
import BriefBoardItem from '@/components/board/BriefBoardItem.vue'
import { apiGetSquareBoards, apiToggleBoardMembership } from '@/api/board'
import type { SquareBoardVO } from '@/models/board/boardTypes'
import { ensureLogin } from '@/utils/auth'

const router = useRouter()
const activeTab = ref<'joined' | 'managed'>('joined')
const boards = ref<SquareBoardVO[]>([])
const loading = ref(false)
const leavingBoardId = ref<number | null>(null)

const joinedBoardList = computed(() => boards.value.filter(board => board.role === 'MEMBER'))
const managedBoardList = computed(() => boards.value.filter(board => board.role === 'CREATOR' || board.role === 'ADMIN'))

const formatNumber = (num?: number) => {
  if (!num) return '0'
  return num >= 10000 ? `${(num / 10000).toFixed(1)}万` : String(num)
}

const fetchBoards = async () => {
  loading.value = true
  try {
    boards.value = await apiGetSquareBoards()
  } catch (error) {
    console.error('获取社区列表失败', error)
    ElMessage.error('获取社区列表失败')
  } finally {
    loading.value = false
  }
}

const handleNavigate = (id: number) => {
  router.push(`/board/${id}`)
}

const handleLeave = (board: SquareBoardVO) => {
  ensureLogin(async () => {
    try {
      await ElMessageBox.confirm(`确定要退出 r/${board.name} 社区吗？`, '提示', {
        confirmButtonText: '确定退出',
        cancelButtonText: '留着吧',
        type: 'warning'
      })

      leavingBoardId.value = board.id
      const res = await apiToggleBoardMembership({ boardId: board.id, action: -1 })
      const target = boards.value.find(item => item.id === board.id)
      if (target) {
        target.role = res.role
        target.memberCount = res.memberCount
      }
      ElMessage.success('已成功退出该社区')
    } catch (error) {
      if (error !== 'cancel' && error !== 'close') {
        console.error('退出社区失败', error)
        ElMessage.error('退出社区失败')
      }
    } finally {
      leavingBoardId.value = null
    }
  })
}

const handleManage = (id: number) => {
  router.push(`/board/${id}/admin`)
}

onMounted(fetchBoards)
</script>

<style scoped>
.board-page-container {
  padding: 40px;
  max-width: 1200px;
  margin: 0 auto;
  box-sizing: border-box;
}

.page-header {
  margin-bottom: 28px;
}

.page-title {
  font-size: 26px;
  font-weight: 600;
  color: #1a1a1b;
  margin: 0 0 6px 0;
}

.page-desc {
  font-size: 13px;
  color: #7c7c7c;
  margin: 0;
}

.desktop-boards-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background-color: #edeff1;
}

.desktop-boards-tabs :deep(.el-tabs__item) {
  font-size: 14px;
  padding: 0 16px;
}

.boards-list-container {
  display: flex;
  flex-direction: column;
  min-height: 96px;
  background-color: #ffffff;
  border: 1px solid #edeff1;
  border-radius: 4px;
  margin-top: 12px;
}

.board-manage-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f2f4f5;
  cursor: pointer;
  transition: background-color 0.15s ease;
}

.board-manage-row:last-child {
  border-bottom: none;
}

.board-manage-row:hover {
  background-color: #f8f9fa;
}

.row-left {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.embedded-item {
  padding: 0 !important;
  margin: 0 !important;
  background-color: transparent !important;
}

.row-description {
  margin: 0 0 0 28px;
  font-size: 13px;
  color: #575757;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.row-right {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-left: 24px;
  flex-shrink: 0;
}

.visitor-count {
  font-size: 12px;
  color: #7c7c7c;
}

.btn-action {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-weight: 700 !important;
  font-size: 12px !important;
  padding: 0 16px !important;
  height: 28px !important;
  border: none;
  transition: all 0.2s ease-in-out;
}

.btn-joined {
  background-color: transparent !important;
  color: #0079d3 !important;
  border: 1px solid #0079d3 !important;
}

.btn-joined:hover {
  background-color: #f0f8ff !important;
}

.btn-manage {
  background-color: #f6f7f8 !important;
  color: #1c1c1c !important;
  border: 1px solid #ccc !important;
}

.btn-manage:hover {
  background-color: #e8f4ff !important;
  border-color: #0079d3 !important;
  color: #0079d3 !important;
}

.empty-hint {
  text-align: center;
  font-size: 13px;
  color: #7c7c7c;
  padding: 32px 0;
}

@media (max-width: 768px) {
  .board-page-container {
    padding: 24px 16px;
  }

  .board-manage-row {
    align-items: flex-start;
    flex-direction: column;
    gap: 12px;
  }

  .row-description {
    margin-left: 0;
    white-space: normal;
  }

  .row-right {
    width: 100%;
    justify-content: space-between;
    margin-left: 0;
    gap: 12px;
  }
}
</style>
