<template>
  <div class="board-square-container">
    <header class="page-header">
      <h1 class="page-title">浏览社区</h1>
    </header>

    <section class="section-container">
      <h2 class="section-title">为你推荐</h2>
      
      <div class="board-grid">
        <BoardSquareItem 
          v-for="item in boards" 
          :key="item.id" 
          :board="item"
          @join="handleJoinRequest"
          @leave=""
          @manage=""
        />
      </div>

      <div v-if="loading" class="loading-state">
        <el-icon class="is-loading"><Loading /></el-icon> 正在探索新社区...
      </div>

      <div v-if="!loading && boards.length > 0" class="load-more-row">
        <el-button round class="load-more-btn">显示更多推荐</el-button>
      </div>
      
      <el-empty v-if="!loading && boards.length === 0" description="广场上暂时没有新社区" />
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { Loading } from '@element-plus/icons-vue';
import BoardSquareItem from '@/components/board/BoardSquareItem.vue';
import type { SquareBoardVO } from '@/models/board/boardTypes';
import { apiGetSquareBoards } from '@/api/board';

const boards = ref<SquareBoardVO[]>([]);
const loading = ref(false);

const fetchSquareData = async () => {
  loading.value = true;
  try {
    // 💡 这里的接口请对应你刚才写好的后端方法
    const res = await apiGetSquareBoards()
    boards.value = res;
  } catch (error) {
    console.error("获取广场数据失败", error);
  } finally {
    loading.value = false;
  }
};

const handleJoinRequest = (id: number | string) => {
  console.log("用户点击加入，ID:", id);
  // 这里写调用后端加入接口的逻辑
};

onMounted(fetchSquareData);
</script>

<style scoped>
/* 💡 修改：页面主背景，对齐 image_1.png 的淡冷灰 */
.board-square-container {
  padding: 40px 60px;
  background-color: #dae0e6; /* 经典的 Reddit 淡蓝色/淡灰背景 */
  min-height: 100vh;
}

.page-header {
  margin-bottom: 32px;
}

/* 💡 修改：标题颜色恢复为清晰的深色 */
.page-title {
  color: #1c1c1c; 
  font-size: 28px;
  font-weight: 700;
}

.section-container {
  margin-bottom: 40px;
}

/* 💡 修改：副标题颜色 */
.section-title {
  color: #7c7c7c; 
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 16px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.board-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  
  /* 💡 垂直间距设为 32px，水平 16px，保持透气感 */
  row-gap: 32px; 
  column-gap: 16px; 
  width: 100%;
}

.loading-state {
  text-align: center;
  padding: 40px;
  color: #7c7c7c;
}

.load-more-row {
  text-align: center;
  margin-top: 32px;
}

/* 💡 修改：加载更多按钮，模仿 image_1.png 的清晰风格 */
.load-more-btn {
  background-color: #0079d3;
  color: #ffffff;
  border: none;
  font-weight: 700;
  padding: 10px 24px;
}

.load-more-btn:hover {
  background-color: #1484d6;
  color: #fff;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .board-square-container {
    padding: 20px 16px;
  }
}
</style>