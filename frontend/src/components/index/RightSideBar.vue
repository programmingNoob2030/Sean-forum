<template>
  <aside class="sidebar-container">
    <div class="sidebar-card">
      <div class="sidebar-header">
        <span class="header-title">最近浏览</span>
        <el-button link type="primary" size="small" @click="clearHistory">
          清除记录
        </el-button>
      </div>

      <div class="history-list-container">
        <div class="history-list" v-if="postStore.recentPostList.length > 0">
          <template v-for="(post, index) in postStore.recentPostList" :key="post.id">
            <!-- 渲染帖子项 -->
            <RecentPostItem 
              class="history-item"
              :post="post"
              @click-detail="handleDetail"
            />
            <!-- 显式分割线：仅在非最后一项后显示 -->
            <div 
              v-if="index !== postStore.recentPostList.length - 1" 
              class="explicit-divider"
            ></div>
          </template>
        </div>
        <!-- 把你的空状态逻辑接回来 -->
        <div class="empty-state" v-else>
          <el-empty description="暂无浏览记录" :image-size="60" />
        </div>
      </div>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import RecentPostItem from '@/components/post/RecentPostCard.vue';
import { ElMessageBox, ElMessage } from 'element-plus';
import router from '@/router'
import { usePostStore } from '@/models/post/postStore';
import { useUserStore } from '@/models/user/userStore';
import { apiDeleteRecentPosts, apiGetRecentPosts } from '@/api/post';


const postStore = usePostStore()
const userStore = useUserStore()
const handleDetail = (id: number) => {
  router.push(`/post/${id}`)
};

onMounted(async()=>{
  if (userStore.token){
    await postStore.getRecentPosts()
  }
})
const clearHistory = () => {
  ElMessageBox.confirm('确定要清除所有最近浏览记录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    await apiDeleteRecentPosts()
    ElMessage.success("清除成功")
    postStore.recentPostList = []
  });
};
</script>

<style scoped>
.sidebar-container {
  width: 312px; /* Reddit 侧边栏标准宽度 */
  /* 可选：给整体加一点上边距，防止贴顶 */
  margin-top: 16px; 
}

.sidebar-card {
  background: #fff;
  border: 1px solid #e0e0e0;
  /* 1. 调整圆角，比之前的 4px 稍微大一点，更柔和 */
  border-radius: 8px;
  overflow: hidden;
  /* 1. 可选：加入微弱的阴影，增加层次感 */
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.sidebar-header {
  /* 2. 增加 Padding：上下由 12px 增加到 16px，左右保持 12px 或也稍微增加 */
  padding: 16px 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #f0f0f0;
}

.header-title {
  /* 保持原样，原设定的字体大小和粗细挺好 */
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  color: #787c7e;
  letter-spacing: 0.5px;
}

/* --- 新增：专门针对列表项的间距调整 --- */
.history-list {
  /* 3. 增加整个列表的内边距，让里面的 Item 不直接贴在卡片边缘 */
  padding: 8px 12px; 
}

.history-item {
  /* 3. 这是关键：给每个 Item 之间增加底边距 */
  margin-bottom: 12px; 
  /* 移除原本子组件可能带有的底边框（如果在子组件里写了的话），改用间距来分隔 */
  border-bottom: none !important; 
}

/* 去掉最后一项的 margin-bottom，保持底部整洁 */
.history-item:last-child {
  margin-bottom: 0;
}
/* ------------------------------------ */

.empty-state {
  /* 增加空状态的 Padding，让它看起来更居中、松弛 */
  padding: 32px 0;
}

.explicit-divider {
  height: 1px;
  /* 颜色用 Reddit 这种浅灰色，非常显眼 */
  background-color: #edeff1; 
  /* 如果你想要线短一点，不顶到边，就加点左右 margin，比如 margin: 0 12px; */
  margin: 0; 
  border: none;
}
/* 以下是你的代码中未使用的部分，我也做了相应调整以备后用 */
.sidebar-footer {
  padding: 16px 14px; /* 增加内边距 */
  border-top: 1px solid #f0f0f0;
}

.view-all-btn {
  width: 100%;
  font-weight: 700;
  background-color: #0079d3;
  color: white;
  border: none;
  /* 增加按钮高度和内边距 */
  padding: 10px 0; 
  border-radius: 999px; /* 圆角按钮更像图2风格 */
}

.view-all-btn:hover {
  background-color: #3293db;
}
</style>