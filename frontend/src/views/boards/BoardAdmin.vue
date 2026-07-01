<template>
  <div class="board-admin-page" v-loading="loading">
    <div class="admin-shell">
      <section class="admin-hero">
        <div class="hero-banner" :style="bannerPreviewStyle">
          <div class="hero-overlay"></div>
        </div>
        <div class="hero-content">
          <div class="cover-preview">
            <img v-if="coverPreview" :src="coverPreview" alt="社区封面" />
            <span v-else>{{ boardInitial }}</span>
          </div>
          <div>
            <p class="eyebrow">社区管理</p>
            <h1>{{ form.name || '未命名社区' }}</h1>
          </div>
        </div>
      </section>

      <el-card class="admin-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span>编辑资料</span>
          </div>
        </template>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          class="admin-form"
        >
          <div class="media-grid">
            <el-form-item label="封面" prop="cover">
              <el-upload
                class="cover-uploader"
                :show-file-list="false"
                :http-request="uploadCoverImage"
                :before-upload="beforeImageUpload"
              >
                <div class="image-upload-box">
                  <img v-if="coverPreview" :src="coverPreview" alt="社区封面" />
                  <el-icon v-else class="upload-icon"><Plus /></el-icon>
                </div>
              </el-upload>
              <el-input v-model="form.cover" placeholder="封面图片地址" clearable />
            </el-form-item>

            <el-form-item label="横幅" prop="banner">
              <el-upload
                class="banner-uploader"
                :show-file-list="false"
                :http-request="uploadBannerImage"
                :before-upload="beforeImageUpload"
              >
                <div class="image-upload-box">
                  <img v-if="bannerPreview" :src="bannerPreview" alt="社区横幅" />
                  <el-icon v-else class="upload-icon"><Plus /></el-icon>
                </div>
              </el-upload>
              <el-input v-model="form.banner" placeholder="横幅图片地址" clearable />
            </el-form-item>
          </div>

          <el-form-item label="社区名称" prop="name">
            <el-input v-model="form.name" maxlength="50" show-word-limit clearable />
          </el-form-item>

          <el-form-item label="社区类型" prop="type">
            <el-select v-model="form.type" class="type-select">
              <el-option
                v-for="item in typeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="社区简介" prop="description">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="5"
              maxlength="500"
              show-word-limit
              resize="none"
            />
          </el-form-item>

          <div class="form-actions">
            <el-button @click="handleCancel">取消</el-button>
            <el-button type="primary" :loading="submitting" @click="submitEdit">
              提交编辑
            </el-button>
          </div>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules, UploadRequestOptions } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { apiGetBoardDetail, apiUpdateBoard, apiUploadBoardImage } from '@/api/board'
import type { BoardUpdateDTO, CommunityType } from '@/models/board/boardTypes'

const route = useRoute()
const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const submitting = ref(false)
const resourceBaseUrl = import.meta.env.VITE_RESOURCE_URL || ''

const boardId = computed(() => Number(route.params.id))

const form = reactive<BoardUpdateDTO>({
  name: '',
  cover: '',
  type: 'PUBLIC',
  description: '',
  banner: ''
})

const typeOptions: Array<{ label: string; value: CommunityType }> = [
  { label: '公开社区', value: 'PUBLIC' },
  { label: '私密社区', value: 'PRIVATE' },
  { label: '严格审核', value: 'STRICT' }
]

const rules: FormRules = {
  name: [
    { required: true, message: '请输入社区名称', trigger: 'blur' },
    { min: 2, max: 50, message: '名称长度需为 2-50 个字符', trigger: 'blur' }
  ],
  type: [{ required: true, message: '请选择社区类型', trigger: 'change' }],
  description: [{ max: 500, message: '简介不能超过 500 个字符', trigger: 'blur' }]
}

const buildPreviewUrl = (url?: string) => {
  if (!url) return ''
  if (/^(https?:|blob:|data:)/.test(url)) return url
  return `${resourceBaseUrl}${url.startsWith('/') ? url : `/${url}`}`
}

const coverPreview = computed(() => buildPreviewUrl(form.cover))
const bannerPreview = computed(() => buildPreviewUrl(form.banner))
const bannerPreviewStyle = computed(() => (
  bannerPreview.value ? { backgroundImage: `url(${bannerPreview.value})` } : {}
))
const boardInitial = computed(() => (form.name || '社').slice(0, 1))

const beforeImageUpload = (file: File) => {
  const allowType = ['image/jpeg', 'image/png', 'image/webp'].includes(file.type)
  const allowSize = file.size / 1024 / 1024 < 5

  if (!allowType) {
    ElMessage.error('仅支持 JPG、PNG 或 WEBP 图片')
  }
  if (!allowSize) {
    ElMessage.error('图片大小不能超过 5MB')
  }
  return allowType && allowSize
}

const uploadImage = async (options: UploadRequestOptions, target: 'cover' | 'banner') => {
  const formData = new FormData()
  formData.append('file', options.file)

  try {
    const imageUrl = await apiUploadBoardImage(formData)
    form[target] = imageUrl
    options.onSuccess?.(imageUrl)
    ElMessage.success('图片上传成功')
  } catch (error) {
    ElMessage.error('图片上传失败')
  }
}

const uploadCoverImage = (options: UploadRequestOptions) => uploadImage(options, 'cover')
const uploadBannerImage = (options: UploadRequestOptions) => uploadImage(options, 'banner')

const loadBoard = async () => {
  if (!boardId.value) {
    ElMessage.error('社区不存在')
    router.replace('/board-manage')
    return
  }

  loading.value = true
  try {
    const board = await apiGetBoardDetail(boardId.value)
    form.name = board.name || ''
    form.cover = board.cover || ''
    form.type = typeOptions.some(item => item.value === board.type) ? board.type as CommunityType : 'PUBLIC'
    form.description = board.description || ''
    form.banner = board.banner || ''
  } catch (error) {
    ElMessage.error('社区信息加载失败')
  } finally {
    loading.value = false
  }
}

const submitEdit = async () => {
  if (!formRef.value || !boardId.value) return

  await formRef.value.validate(async valid => {
    if (!valid) return

    submitting.value = true
    try {
      await apiUpdateBoard(boardId.value, { ...form })
      ElMessage.success('社区资料已更新')
      router.push(`/board/${boardId.value}`)
    } catch (error) {
      ElMessage.error('提交失败，请稍后重试')
    } finally {
      submitting.value = false
    }
  })
}

const handleCancel = () => {
  if (boardId.value) {
    router.push(`/board/${boardId.value}`)
  } else {
    router.push('/board-manage')
  }
}

onMounted(loadBoard)
</script>

<style scoped>
.board-admin-page {
  min-height: calc(100vh - 64px);
  background: #f5f7fb;
  padding: 24px;
}

.admin-shell {
  max-width: 980px;
  margin: 0 auto;
}

.admin-hero {
  position: relative;
  overflow: hidden;
  border-radius: 8px;
  min-height: 240px;
  background: linear-gradient(135deg, #1f2937, #475569);
  color: #ffffff;
  margin-bottom: 20px;
}

.hero-banner {
  position: absolute;
  inset: 0;
  background-size: cover;
  background-position: center;
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(15, 23, 42, 0.18), rgba(15, 23, 42, 0.74));
}

.hero-content {
  position: absolute;
  left: 28px;
  right: 28px;
  bottom: 24px;
  display: flex;
  align-items: center;
  gap: 18px;
}

.cover-preview {
  width: 96px;
  height: 96px;
  border-radius: 8px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.18);
  border: 2px solid rgba(255, 255, 255, 0.72);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  font-weight: 700;
}

.cover-preview img,
.image-upload-box img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.eyebrow {
  margin: 0 0 6px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.78);
}

h1 {
  margin: 0;
  font-size: 32px;
  line-height: 1.2;
  font-weight: 700;
}

.admin-card {
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.card-header {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
}

.admin-form {
  max-width: 760px;
}

.media-grid {
  display: grid;
  grid-template-columns: minmax(180px, 220px) minmax(280px, 1fr);
  gap: 24px;
  align-items: start;
}

.image-upload-box {
  border: 1px dashed #cbd5e1;
  border-radius: 8px;
  background: #f8fafc;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  margin-bottom: 12px;
  transition: border-color 0.2s ease, background-color 0.2s ease;
}

.image-upload-box:hover {
  border-color: #409eff;
  background: #eff6ff;
}

.cover-uploader .image-upload-box {
  width: 148px;
  height: 148px;
}

.banner-uploader .image-upload-box {
  width: 360px;
  max-width: 100%;
  height: 154px;
}

.upload-icon {
  font-size: 28px;
  color: #64748b;
}

.type-select {
  width: 240px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 8px;
}

@media (max-width: 720px) {
  .board-admin-page {
    padding: 16px;
  }

  .admin-hero {
    min-height: 220px;
  }

  .hero-content {
    left: 18px;
    right: 18px;
    bottom: 18px;
  }

  .cover-preview {
    width: 76px;
    height: 76px;
    font-size: 28px;
  }

  h1 {
    font-size: 24px;
  }

  .media-grid {
    grid-template-columns: 1fr;
  }

  .cover-uploader .image-upload-box,
  .banner-uploader .image-upload-box {
    width: 100%;
  }

  .form-actions {
    flex-direction: column-reverse;
  }

  .form-actions .el-button {
    width: 100%;
    margin-left: 0;
  }
}
</style>
