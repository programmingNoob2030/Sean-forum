<template>
  <div class="post-card-inner">
    <div class="vote-sidebar">
      <el-icon
        class="vote-up" 
        :class="{ 'active': post.postRatingType == 1 }"
        @click=doRatingUp><CaretTop />
      </el-icon>
      <span class="vote-count" >{{ post.likeCount || 0 }}</span>
      <el-icon 
        class="vote-down"
        :class="{ 'active': post.postRatingType == -1 }"
        @click=doRatingDown><CaretBottom />
      </el-icon>
    </div>
    
    <div class="post-content-area" @click="$emit('click-detail', post.id)">
      <div class="post-header-meta">
        <img
          :src="baseUrl + post.boardCover"
          
          class="cover"
        />      
        <span class="community-name">r/{{ post.boardName }}</span>
        <span class="dot">•</span>  
        <span class="post-author">
          Posted by 
          <!-- 新增的头像图片 -->
          <img :src="post.creatorAvatar ? (baseUrl + post.creatorAvatar) : '/default-user-avatar.png'" class="avatar" />
          u/{{ post.creatorName || 'SeanLi' }}
        </span>
        <span class="post-time">{{ displayTime }}</span>
      </div>
      
      <h3 class="post-title">{{ post.title }}</h3>
      
      <div class="post-summary" v-html="post.content"></div>
      
      <div class="post-footer-actions">
        <div class="action-btn">
          <el-icon><ChatDotRound /></el-icon>
          <span>{{ post.commentCount || 0 }}</span>
        </div>
        <div class="action-btn">
          <el-icon><Share /></el-icon>
          <span>Share</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { CaretTop, CaretBottom, ChatDotRound, Share } from '@element-plus/icons-vue'
import type { PostVO } from '@/models/post/postTypes'
import { formatPostTime } from '@/utils/timeFormat';
import { computed } from 'vue';
import { useRatingStore } from '@/models/rating/ratingStore';
import { ref } from 'vue'
import type { RatingDTO } from '@/models/rating/ratingTypes';
import { ElMessage } from 'element-plus';
import { ensureLogin } from '@/utils/auth';
const baseUrl = import.meta.env.VITE_RESOURCE_URL
const ratingForm = ref<RatingDTO>({
  target:'',
  targetId:0,
  action:0
})
// 1. 定义接收的参数 (Props)
const props = defineProps<{
  post: PostVO
}>()
const ratingSotre = useRatingStore()
const doRatingUp = async()=>{
  ratingForm.value.action = 1
  ratingForm.value.target = 'POST'
  ratingForm.value.targetId = props.post.id
  ensureLogin(async()=>{
    const res = await ratingSotre.toggleRating(ratingForm.value)
    if (res){
      props.post.likeCount = res.likeCount
      props.post.postRatingType = res.type
    }
    console.log("喜欢成功")
  })

}
const doRatingDown = async()=>{
  ratingForm.value.action = -1
  ratingForm.value.target = 'POST'
  ratingForm.value.targetId = props.post.id
  
  ensureLogin(async()=>{
    const res = await ratingSotre.toggleRating(ratingForm.value)
    if (res){
      props.post.likeCount = res.likeCount
      props.post.postRatingType = res.type
    }
    console.log("拉踩成功")
  })
  
}
// 2. 定义向外发送的事件 (Emits)
defineEmits(['click-detail'])
// 使用计算属性，不仅逻辑清晰，而且性能好
const displayTime = computed(() => {
  return formatPostTime(props.post.createTime);
});
</script>

<style scoped>

/* 1. 最外层卡片容器 */
.post-card-inner {
  display: flex;          /* 开启 Flex，把点赞区和内容区分开 */
  background-color: #fff; /* 纯白背景 */
  border: 1px solid #e0e0e0; /* 淡淡的边框，比 #ccc 更高级 */
  border-radius: 4px;     /* 圆角 */
  margin-bottom: 12px;    /* 关键：卡片之间的“呼吸感” */
  cursor: pointer;        /* 鼠标指上去变小手 */
  transition: border-color 0.2s; /* 鼠标悬停动画 */
  overflow: hidden;       /* 防止内容溢出圆角 */
}

/* 鼠标悬停时边框变深 */
.post-card-inner:hover {
  border-color: #a0a0a0;
}

/* 2. 左侧：点赞/投票侧边栏 */
.vote-sidebar {
  width: 40px;            /* 固定宽度 */
  background-color: #f8f9fa; /* 淡淡的灰色背景，突出区域 */
  display: flex;
  flex-direction: column; /* 垂直排列 */
  align-items: center;    /* 水平居中 */
  padding: 8px 4px;       /* 上下留白 */
  border-right: 1px solid #e0e0e0; /* 和内容区的分界线 */
}

.vote-count {
  font-size: 12px;
  font-weight: 700;       /* 加粗数字 */
  color: #1a1a1b;         /* Reddit 经典黑 */
  margin: 4px 0;          /* 数字上下留白 */
}

/* 点赞图标样式 */
.vote-up, .vote-down {
  font-size: 18px;
  color: #878a8c;         /* 默认灰色 */
  cursor: pointer;
}
.vote-up:hover { color: #ff4500; } /* 橙色点赞 */
.vote-down:hover { color: #7193ff; } /* 蓝色踩 */
.vote-up.active{
  font-size: 18px;
  color: #ff4500;         /* 默认灰色 */
  cursor: pointer;
}
.vote-down.active{
  font-size: 18px;
  color: #7193ff;         /* 默认灰色 */
  cursor: pointer;
}

/* 3. 右侧：内容主区域 */
.post-content-area {
  flex: 1;                /* 占据剩余所有宽度 */
  padding: 8px 12px;      /* 内容区的内边距 */
  display: flex;
  flex-direction: column; /* 内容垂直排列 */
}

/* 容器：确保所有元素垂直对齐 */
.post-header-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px; /* 整体字号稍微缩小，显得更专业 */
  color: #7c7c7c;  /* 辅助文字颜色 */
}

/* 社区名称：加深颜色并加粗，强调归属 */
.community-name {
  font-weight: 700;
  color: #1a1a1b; /* 接近黑色 */
  cursor: pointer;
}
.community-name:hover {
  text-decoration: underline;
}

/* 小圆点间距 */
.dot {
  margin: 0 2px;
}
/* 作者名字和 Posted by：保持细体、浅色 */
.post-author {
  display: flex;
  align-items: center;
  font-weight: 400;
}

/* 5. 内容区：标题 */
.post-title {
  font-size: 18px;
  font-weight: 600;       /* 标题加粗 */
  color: #222;
  margin: 0 0 10px 0;     /* 标题下方的距离 */
  line-height: 1.4;       /* 行高，防止标题太挤 */
}

/* 6. 内容区：正文缩略 (关键) */
.post-summary {
  font-size: 14px;
  color: #4f4f4f;
  line-height: 1.6;       /* 正文行高，易于阅读 */
  margin-bottom: 12px;    /* 距离底部的距离 */
  /* 如果是富文本，这里可以限制高度并加 ellipsis，防止帖子太长 */
  max-height: 100px;
  overflow: hidden;
  position: relative;
}

/* 7. 内容区：底部操作按钮区 */
.post-footer-actions {
  display: flex;
  align-items: center;
  gap: 8px;               /* 按钮之间的间距 */
  margin-top: auto;       /* 自动推到底部 */
}

.action-btn {
  display: flex;
  align-items: center;
  padding: 6px 8px;       /* 按钮内边距 */
  border-radius: 2px;
  color: #878a8c;
  font-size: 12px;
  font-weight: 700;
}

/* 按钮悬停效果 */
.action-btn:hover {
  background-color: #f6f7f8;
}

.action-btn .el-icon {
  font-size: 16px;
  margin-right: 6px;      /* 图标和文字的间距 */
}
/* 社区封面：加大，作为视觉第一中心 */
.cover {
  width: 28px;
  height: 28px;
  border-radius: 50%; /* 也可以用 6px 圆角，看你喜好 */
  object-fit: cover;
}

/* 3. 头像样式优化 */
/* 作者头像：保持小而精 */
/* 作者头像：缩小，降低存在感 */
.avatar {
  width: 16px;  /* 比社区封面小一圈 */
  height: 16px;
  border-radius: 50%;
  object-fit: cover;
  margin: 0 2px;
  opacity: 0.8; /* 稍微透明一点，不抢戏 */
}
</style>