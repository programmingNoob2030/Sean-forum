<template>
  <aside class="sidebar-container">
    <div class="sidebar-card">
      <div class="sidebar-header">
        <span class="header-title">最近浏览</span>
        <el-button link type="primary" size="small" @click="clearHistory">
          清除记录
        </el-button>
      </div>

      <div class="history-list" v-if="historyPosts.length > 0">
        <RecentPostItem 
          v-for="post in historyPosts" 
          :key="post.id" 
          :post="post"
          @click-detail="handleDetail"
        />
      </div>

      <div class="empty-state" v-else>
        <el-empty description="暂无浏览记录" :image-size="60" />
      </div>
      
      <div class="sidebar-footer" v-if="historyPosts.length > 0">
        <el-button class="view-all-btn" round>查看全部</el-button>
      </div>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import RecentPostItem from '@/components/post/RecentPostCard.vue';
import type { PostVO } from '@/models/post/postTypes';
import { ElMessageBox } from 'element-plus';

// 模拟数据，实际开发中建议从 Pinia 或 LocalStorage 获取
const historyPosts = ref<PostVO[]>([
  // ... 你的 PostVO 数据
]);

const handleDetail = (id: number) => {
  console.log("跳转到帖子详情:", id);
  // router.push(`/post/${id}`)
};

const clearHistory = () => {
  ElMessageBox.confirm('确定要清除所有最近浏览记录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    historyPosts.value = [];
  });
};
</script>

<style scoped>
.sidebar-container {
  width: 312px; /* Reddit 侧边栏标准宽度 */
}

.sidebar-card {
  background: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
}

.sidebar-header {
  padding: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #f0f0f0;
}

.header-title {
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  color: #787c7e;
  letter-spacing: 0.5px;
}

.empty-state {
  padding: 20px 0;
}

.sidebar-footer {
  padding: 12px;
}

.view-all-btn {
  width: 100%;
  font-weight: 700;
  background-color: #0079d3;
  color: white;
  border: none;
}

.view-all-btn:hover {
  background-color: #3293db;
}
</style>