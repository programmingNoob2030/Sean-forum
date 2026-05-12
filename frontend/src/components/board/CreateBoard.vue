<template>
  <el-dialog 
    :model-value="modelValue" 
    append-to-body
    @update:model-value="$emit('update:modelValue', $event)"
    title="创建新社区" 
    width="520px"
    @closed="resetForm"
  >
    <el-steps :active="activeStep" finish-status="success" simple style="margin-bottom: 25px">
      <el-step title="选择类型" />
      <el-step title="填写详情" />
    </el-steps>

    <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
      <div v-if="activeStep === 0">
        <el-form-item label="社区类型" prop="type">
          <el-radio-group v-model="form.type" class="type-selector">
            <el-radio value="PUBLIC" border>公开</el-radio>
            <el-radio value="PRIVATE" border>私密</el-radio>
            <el-radio value="STRICT" border>严格</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <div class="type-info-box">
          <el-icon><InfoFilled /></el-icon>
          <span>{{ currentDescription }}</span>
        </div>
      </div>

      <div v-else>
        <el-form-item label="社区封面">
          <div class="cover-upload-container">
            <el-upload
              class="cover-uploader"
              action="#"
              :http-request="customUpload" 
              :show-file-list="false"
              :before-upload="beforeCoverUpload"
            >
              <div class="uploader-display-area">
                <div v-if="coverPreview || form.cover" class="image-preview-wrapper">
                  <img :src="coverPreview || `${baseUrl}${form.cover}`" class="cover-image" />
                  <div class="upload-mask">
                    <el-icon><Camera /></el-icon>
                    <span>Change</span>
                  </div>
                </div>
                <el-icon v-else class="center-plus-icon"><Plus /></el-icon>
              </div>
            </el-upload>
            
            <div class="upload-tip-content">
              <div class="tip-title">
                <el-icon><InfoFilled /></el-icon> Upload Instructions
              </div>
              <div class="tip-text">Click anywhere in the box to upload</div>
              <div class="tip-text">JPG/PNG/WebP supported, max <span class="highlight">2MB</span></div>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="社区名称" prop="name">
          <el-input v-model="form.name" placeholder="例如: Java学习小组" />
        </el-form-item>
        
        <el-form-item label="社区描述" prop="description">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            :rows="3" 
            placeholder="描述一下这个社区的目的..." 
          />
        </el-form-item>
      </div>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button v-if="activeStep === 1" @click="activeStep = 0" >上一步</el-button>
        <el-button v-if="activeStep === 0" type="primary" @click="activeStep = 1" >下一步</el-button>
        <el-button v-else type="primary" @click="handleConfirm"  >确定创建</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Camera, Plus, InfoFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { BoardDTO } from '@/models/board/boardTypes'
import { apiCreateBoard, apiUploadCover } from '@/api/board'

const props = defineProps({
  modelValue: {
    type: Boolean,
    required: true,
    default: false
  }
})
const emit = defineEmits(['update:modelValue', 'success'])

const baseUrl = import.meta.env.VITE_RESOURCE_URL

const activeStep = ref(0)
const formRef = ref<any>(null)
const coverPreview = ref('')
const form = ref<BoardDTO>({
  type: 'PUBLIC',
  name: '',
  description: '',
  cover: '' 
})

const typeDescriptions = {
  PUBLIC: '任何人都可以查看和加入社区，帖子对所有人可见。',
  PRIVATE: '只有受邀成员可以查看社区内容和发帖。',
  STRICT: '任何人都可以查看社区，但只有经过审核的成员才能发帖。'
}

const currentDescription = computed(() => {
  return typeDescriptions[form.value.type] || '请选择一个社区类型'
})

const rules = {
  name: [{ required: true, message: '名称不能为空', trigger: 'blur' }],
  description: [{ required: true, message: '描述不能为空', trigger: 'blur' }]
}

const beforeCoverUpload = (rawFile: any) => {
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
    
    // Using your user-info logic here
    const res = await apiUploadCover(formData); 

    form.value.cover = res; 
    coverPreview.value = baseUrl + res; 
    ElMessage.success('Image uploaded to server (Draft)');
  } catch (error) {
    ElMessage.error('Upload failed');
  }
}

const resetForm = () => {
  activeStep.value = 0
  form.value = { type: 'PUBLIC', name: '', description: '', cover: '' }
  coverPreview.value = ''
}

const handleConfirm = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate()
  if (valid) {
    await apiCreateBoard(form.value)
    emit('success')
    emit('update:modelValue', false)
    ElMessage.success('社区创建成功！')
    resetForm()
  }
}
</script>

<style scoped>
.cover-upload-container {
  display: flex;
  align-items: center;
  gap: 20px;
}

/* Force the el-upload trigger to be a full-size flex container */
.cover-uploader :deep(.el-upload) {
  width: 240px;
  height: 130px;
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  background-color: #fafafa;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: border-color 0.3s;
}

.cover-uploader :deep(.el-upload:hover) {
  border-color: #409eff;
}

/* Internal wrapper to handle centering and preview */
.uploader-display-area {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.center-plus-icon {
  font-size: 40px; /* Slightly larger for better visibility */
  color: #8c939d;
}

.image-preview-wrapper {
  width: 100%;
  height: 100%;
  position: relative;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.upload-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: white;
  opacity: 0;
  transition: opacity 0.3s;
}

.image-preview-wrapper:hover .upload-mask {
  opacity: 1;
}

.upload-tip-content {
  flex: 1;
}

.tip-title {
  font-weight: bold;
  font-size: 14px;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 5px;
}

.tip-text {
  font-size: 12px;
  color: #666;
  line-height: 1.6;
}

.highlight {
  color: #f56c6c;
  font-weight: bold;
}

.type-info-box {
  margin-top: 15px;
  padding: 12px;
  background-color: #f0f7ff;
  border-radius: 6px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #409eff;
  font-size: 13px;
  border: 1px solid #d9ecff;
}

.type-selector {
  display: flex;
  width: 100%;
}

.type-selector :deep(.el-radio) {
  flex: 1;
  margin-right: 0;
  text-align: center;
}
</style>