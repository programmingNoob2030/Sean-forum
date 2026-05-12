<template>
  <div class="profile-main-layout">
    <div class="profile-wrapper">
      <div class="profile-header">
        <div class="cover-photo"></div>
        
        <div class="hero-section" v-if="userStore.userInfo">
          <div class="avatar-wrapper">
            <el-avatar 
              :size="120" 
              :src="`${baseUrl}${userStore.userInfo.avatar}` || defaultAvatar" 
              class="profile-avatar-styled"
            />
          </div>
          <div class="basic-info">
            <h1 class="user-name">{{ userStore.userInfo.name }}</h1>
            <p class="user-email">
              <el-icon><Message /></el-icon>
              {{ userStore.userInfo.email || '未绑定邮箱' }}
            </p>
          </div>
          <div class="header-actions">
            <el-button type="primary" size="small" @click="openEditDialog">
              <el-icon><Edit /></el-icon>编辑资料
            </el-button>
            <el-button type="danger" plain size="small" @click="handleLogout" class="logout-btn">
              <el-icon><SwitchButton /></el-icon>退出登录
            </el-button>
          </div>
        </div>
      </div>

      <div v-if="userStore.userInfo" class="profile-content">
        <el-card shadow="never" class="stats-card-panel">
          <template #header>
            <div class="content-header">
              <span class="title">账户概览</span>
            </div>
          </template>

          <div class="stats-grid">
            <div class="stats-item">
              <div class="icon-box posts-icon">
                <el-icon><Document /></el-icon>
              </div>
              <div class="stats-data">
                <div class="label">发帖总数</div>
                <div class="value">{{ userStore.userInfo.postCount }}</div>
              </div>
            </div>

            <div class="stats-item">
              <div class="icon-box register-icon">
                <el-icon><Calendar /></el-icon>
              </div>
              <div class="stats-data">
                <div class="label">注册时间</div>
                <div class="value">{{ userStore.userInfo.registerTime.split(' ')[0] }}</div>
              </div>
            </div>

            <div class="stats-item">
              <div class="icon-box login-icon">
                <el-icon><Timer /></el-icon>
              </div>
              <div class="stats-data">
                <div class="label">最后登录</div>
                <div class="value">{{ userStore.userInfo.lastLoginTime }}</div>
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <el-card v-else shadow="never" class="empty-card">
        <el-empty description="请先登录以查看个人信息">
          <el-button type="primary" size="large" @click="$router.push('/login')">去登录</el-button>
        </el-empty>
      </el-card>
    </div>
  </div>

  <el-dialog 
  v-model="editDialogVisible" 
  title="Edit Profile" 
  width="520px"
  append-to-body
  destroy-on-close
>
  <el-form :model="editForm" label-width="80px" style="padding: 10px 0">
    <el-form-item label="Username">
      <el-input v-model="editForm.name" placeholder="Pick a cool name" />
    </el-form-item>

    <el-form-item label="Avatar">
      <div class="avatar-upload-container">
        <el-upload
          class="avatar-uploader"
          action="#"
          :http-request="customUpload" 
          :show-file-list="false"
          :before-upload="beforeAvatarUpload"
        >
          <div v-if="previewUrl || editForm.avatar" class="image-preview-wrapper">
            <img :src="previewUrl || `${baseUrl}${userStore?.userInfo?.avatar}`" class="avatar-preview" />
            <div class="upload-mask">
              <el-icon><Camera /></el-icon>
              <span>Change</span>
            </div>
          </div>
          <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
        </el-upload>
        
        <div class="upload-tip-content">
          <div class="tip-title">
            <el-icon><InfoFilled /></el-icon> Upload Instructions
          </div>
          <div class="tip-text">Click the circle to select an image</div>
          <div class="tip-text">JPG/PNG supported, max <span class="highlight">2MB</span></div>
        </div>
      </div>
    </el-form-item>

    <el-form-item label="Email">
      <el-input v-model="editForm.email" placeholder="Email for contact" />
    </el-form-item>
  </el-form>
  
  <template #footer>
    <div class="dialog-footer">
      <el-button @click="handleCancel">Cancel</el-button>
      <el-button type="primary" @click="submitEdit" :loading="submitting">
        Confirm & Save
      </el-button>
    </div>
  </template>
</el-dialog>
</template>

<script setup lang="ts">
import { 
  Edit, Message, SwitchButton, Document, 
  Calendar, Timer, Plus, InfoFilled, Camera 
} from '@element-plus/icons-vue'
import defaultAvatar from '@/assets/default_avater.svg'
import { useUserStore } from '@/models/user/userStore'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { ref } from 'vue'
import type { UpdateInfoDTO } from '@/models/user/userTypes'
import { apiUpdateUserInfo, apiUploadAvatar } from '@/api/user' // Adjust paths
import { useBoardStore } from '@/models/board/boardStore'

const userStore = useUserStore()
const router = useRouter()

const editDialogVisible = ref(false)
const submitting = ref(false)
const previewUrl = ref('') 
const baseUrl = import.meta.env.VITE_RESOURCE_URL
const boardStore = useBoardStore()
// 表单响应式数据
const editForm = ref<UpdateInfoDTO>({
  name: '',
  avatar: '',
  email: ''
})

// 校验逻辑
const beforeAvatarUpload = (rawFile: any) => {
  const isJPGorPNG = ['image/jpeg', 'image/png', 'image/webp'].includes(rawFile.type);
  const isLt2M = rawFile.size / 1024 / 1024 < 2;
  if (!isJPGorPNG) {
    ElMessage.error('头像只能是 JPG/PNG/WebP 格式!');
    return false;
  }
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过 2MB!');
    return false;
  }
  return true;
}
const customUpload = async (options: any) => {
  try {
    const formData = new FormData();
    formData.append('file', options.file);
    
    // Call API that ONLY saves file to disk and returns the path
    const res = await apiUploadAvatar(formData); 
    const relativePath = res; 

    // Update Draft State
    editForm.value.avatar = relativePath;
    // Update Preview State (using our helper)
    previewUrl.value = baseUrl + relativePath; 
    ElMessage.success('Image uploaded to server (Draft)');
  } catch (error) {
    ElMessage.error('Upload failed');
  }
}

// 3. Submit Edit: The "Commit" step
const submitEdit = async () => {
  submitting.value = true;
  try {
    // This sends the updated username, email, and the NEW avatar path to DB
    await apiUpdateUserInfo(editForm.value);
    editDialogVisible.value = false;
    // Refresh global user info (Pinia)
    userStore.setUserInfo(editForm.value)
    ElMessage.success('Profile updated successfully!');
  } catch (error) {
    console.error(error);
  } finally {
    submitting.value = false;
  }
}
// 4. Handle Cancel: Forget the draft
const handleCancel = () => {
  editDialogVisible.value = false;
  previewUrl.value = ''; // Clear the preview
  // The database remains unchanged!
}


const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    type: 'warning'
  }).then(() => {
    boardStore.briefBoardList = []
    userStore.handleLogout()
    router.push('/login')
  }).catch(() => {})
}
const openEditDialog = () => {
  if (userStore.userInfo) {
    editForm.value = { ...userStore.userInfo };
    editDialogVisible.value = true
  }
}

</script>

<style scoped>
/* 核心：全区域点击的头像上传器 */
.avatar-upload-container {
  display: flex;
  align-items: center;
  gap: 24px;
  background-color: #f8f9fa;
  padding: 16px;
  border-radius: 12px;
  border: 1px solid #f0f2f5;
}

.avatar-uploader {
  width: 100px;
  height: 100px;
  border: 2px dashed #dcdfe6;
  border-radius: 50%;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.645, 0.045, 0.355, 1);
  background-color: #fff;
}

/* 关键：穿透组件内部容器，让热区铺满 */
:deep(.el-upload) {
  width: 100% !important;
  height: 100% !important;
  display: flex !important;
  justify-content: center;
  align-items: center;
  border-radius: 50%;
  position: relative;
}

.avatar-uploader:hover {
  border-color: #409eff;
  transform: scale(1.02);
}

.image-preview-wrapper {
  width: 100%;
  height: 100%;
  position: relative;
}

.avatar-preview {
  width: 100px;
  height: 100px;
  object-fit: cover;
  display: block;
}

/* 悬浮蒙层样式 */
.upload-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.45);
  color: #fff;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.3s;
  font-size: 12px;
}

.image-preview-wrapper:hover .upload-mask {
  opacity: 1;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}

/* 说明文字样式 */
.upload-tip-content {
  flex: 1;
}
.tip-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 6px;
  display: flex;
  align-items: center;
  gap: 5px;
}
.tip-text {
  font-size: 12px;
  color: #909399;
  line-height: 1.8;
}
.highlight { color: #f56c6c; font-weight: bold; }

/* 基础布局代码 */
.profile-main-layout { min-height: 100vh; background-color: #f5f7fa; padding-bottom: 40px; }
.profile-wrapper { max-width: 800px; margin: 0 auto; }
.profile-header { background-color: #fff; border-radius: 0 0 12px 12px; box-shadow: 0 2px 12px rgba(0,0,0,0.05); overflow: hidden; margin-bottom: 24px; }
.cover-photo { height: 160px; background: linear-gradient(135deg, #409eff 0%, #a0cfff 100%); }
.hero-section { display: flex; padding: 0 30px 25px; align-items: flex-end; margin-top: -60px; position: relative; z-index: 1; }
.avatar-wrapper { border: 5px solid #fff; border-radius: 50%; box-shadow: 0 4px 12px rgba(0,0,0,0.15); }
.basic-info { flex: 1; margin-left: 25px; padding-bottom: 5px; }
.user-name { font-size: 28px; font-weight: 700; color: #303133; margin: 0 0 8px; }
.user-email { font-size: 14px; color: #909399; display: flex; align-items: center; gap: 6px; }
.header-actions { display: flex; align-items: center; gap: 12px; padding-bottom: 10px; }
.stats-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
.stats-item { display: flex; align-items: center; background-color: #f9fafc; padding: 20px; border-radius: 8px; border: 1px solid #ebeef5; }
.icon-box { width: 48px; height: 48px; border-radius: 12px; display: flex; justify-content: center; align-items: center; font-size: 24px; margin-right: 15px; }
.posts-icon { background-color: #ecf5ff; color: #409eff; }
.register-icon { background-color: #f0f9eb; color: #67c23a; }
.login-icon { background-color: #fdf6ec; color: #e6a23c; }
.stats-data .value { font-size: 20px; font-weight: 700; color: #303133; }

@media (max-width: 600px) {
  .stats-grid { grid-template-columns: 1fr; }
  .avatar-upload-container { flex-direction: column; text-align: center; }
}
</style>