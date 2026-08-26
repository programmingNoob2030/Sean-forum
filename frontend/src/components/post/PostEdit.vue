<template>
  <el-dialog
    v-model="dialogVisible"
    title="发布帖子"
    width="720px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="80px"
    >
      <el-form-item label="社区" prop="id">
        <el-select
          v-model="formData.id"
          filterable
          remote
          :remote-method="handleSearch"
          :loading="loading"
          placeholder="直接输入名字搜社区"
          @change="handleSelectChange"
        >
          <template #prefix v-if="formData.id">
            <div style="display: flex; align-items: center;">
              <el-avatar v-if="formData.cover" :size="20" :src="baseUrl + formData.cover" />
              <span style="margin-left: 5px; color: #409eff; font-weight: bold;">r/</span>
            </div>
          </template>
          <el-option
            v-for="item in communityOptions"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          >
            <BriefBoardItem
              :id="item.id"
              :name="item.name"
              :cover="item.cover"
              is-only-show
            />
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item label="标题" prop="title">
        <el-input
          v-model="formData.title"
          placeholder="请输入标题"
          clearable
        />
      </el-form-item>

      <el-form-item label="内容" prop="content">
        <PostContentEditor
          ref="contentEditorRef"
          v-model="formData.content"
          :disabled="submitting"
          placeholder="写下正文"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          发布
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { nextTick, ref } from 'vue'
import { debounce } from 'lodash'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import BriefBoardItem from '@/components/board/BriefBoardItem.vue'
import PostContentEditor, { type PostContentEditorExpose } from '@/components/post/PostContentEditor.vue'
import { apiSearchBoard } from '@/api/board'
import { apiCreatePost } from '@/api/post'
import type { BriefBoardVO } from '@/models/board/boardTypes'
import type { CreatePostDTO } from '@/models/post/postTypes'

interface PostFormData {
  id?: number
  name: string
  cover: string
  title: string
  content: string
}

const dialogVisible = ref(false)
const baseUrl = import.meta.env.VITE_RESOURCE_URL
const submitting = ref(false)
const loading = ref(false)
const formRef = ref<FormInstance>()
const contentEditorRef = ref<PostContentEditorExpose | null>(null)

const communityOptions = ref<BriefBoardVO[]>([])

const formData = ref<PostFormData>({
  id: undefined,
  name: '',
  cover: '',
  title: '',
  content: '',
})

const formRules: FormRules<PostFormData> = {
  id: [
    { required: true, message: '请选择社区', trigger: 'change' },
  ],
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
  ],
  content: [
    { required: true, message: '请输入内容', trigger: 'change' },
  ],
}

const emit = defineEmits(['success'])

const debouncedFetch = debounce(async (query: string) => {
  try {
    const res = await apiSearchBoard(query)
    communityOptions.value = res
  } catch (error) {
    console.error('搜索社区失败', error)
  } finally {
    loading.value = false
  }
}, 300)

const handleSearch = (query: string) => {
  if (query) {
    loading.value = true
    debouncedFetch(query)
  } else {
    communityOptions.value = []
  }
}

const handleSelectChange = (val: number) => {
  const item = communityOptions.value.find((i) => i.id === val)
  if (item) {
    formData.value.id = item.id
    formData.value.name = item.name
    formData.value.cover = item.cover
  }
}

const resetForm = () => {
  formData.value = {
    id: undefined,
    name: '',
    cover: '',
    title: '',
    content: '',
  }
  communityOptions.value = []
  formRef.value?.clearValidate()
  contentEditorRef.value?.clear()
}

const open = async (id?: number, board?: string, cover?: string) => {
  resetForm()
  if (id && board) {
    formData.value.id = id
    formData.value.name = board
    formData.value.cover = cover || ''
    communityOptions.value = [{
      id,
      name: board,
      cover: cover || '',
    }]
  }
  dialogVisible.value = true
  await nextTick()
  contentEditorRef.value?.clear()
}

const handleClose = () => {
  resetForm()
  dialogVisible.value = false
}

const handleSubmit = async () => {
  if (!formRef.value) {
    return
  }

  const isValid = await formRef.value.validate().then(() => true).catch(() => false)
  if (!isValid) {
    return
  }

  if (contentEditorRef.value?.hasPendingUpload()) {
    ElMessage.warning('图片上传中，请稍后')
    return
  }

  const content = contentEditorRef.value?.serialize() || ''
  if (!content) {
    ElMessage.warning('请输入内容')
    return
  }

  submitting.value = true
  try {
    const postData: CreatePostDTO = {
      boardId: formData.value.id as number,
      title: formData.value.title,
      content,
    }
    await apiCreatePost(postData)
    ElMessage.success('发布成功！')
    dialogVisible.value = false
    emit('success', { ...formData.value, content })
    resetForm()
  } catch (error) {
    console.error('发布失败:', error)
    ElMessage.error('发布失败，请重试')
  } finally {
    submitting.value = false
  }
}

defineExpose({
  open,
})
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
