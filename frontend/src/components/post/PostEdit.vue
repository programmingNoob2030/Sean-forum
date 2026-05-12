<!-- PostEdit.vue -->
<template>
  <el-dialog
    v-model="dialogVisible"
    title="发布帖子"
    width="600px"
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
          placeholder="直接输入名字搜社区"
          remote
          :remote-method="handleSearch"
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
        <el-input
          v-model="formData.content"
          type="textarea"
          placeholder="请输入内容"
          :rows="6"
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
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import type { BriefBoardVO } from '@/models/board/boardTypes'
import BriefBoardItem from '@/components/board/BriefBoardItem.vue'
import { debounce } from 'lodash';
import { apiSearchBoard } from '@/api/board'
import type { PostEditDTO } from '@/models/post/postTypes'
import { apiCreatePost } from '@/api/post'
// 控制弹窗显示
const dialogVisible = ref(false)
const baseUrl = import.meta.env.VITE_RESOURCE_URL
// 提交状态
const submitting = ref(false)
const loading = ref(false)
// 表单引用
const formRef = ref<FormInstance>()

const communityOptions = ref<BriefBoardVO[]>([
])
const handleSearch = (query: string) => {
  if (query) {
    loading.value = true
    // 使用防抖，避免频繁请求后端
    debouncedFetch(query)
  } else {
    communityOptions.value = []
  }
}
const handleSelectChange = (val:number) => {
  // val 是选中的 ID
  const item = communityOptions.value.find(i => i.id === val)
  if (item) {
    formData.value.id = item.id
    formData.value.name = item.name   // 保存名字供后续显示或提交
    formData.value.cover = item.cover // 立即更新封面
  }
}
// 真正的后端请求请求
const debouncedFetch = debounce(async (query: string) => {
  try {
    const res = await apiSearchBoard(query)
    communityOptions.value = res
  } catch (error) {
    console.error('搜索社区失败', error)
  } finally {
    loading.value = false
  }
}, 300) // 300ms 的延迟

// 表单数据
const formData = ref<PostEditDTO>({
  id: 0, 
  name:'',
  cover: '',
  title: '',
  content: ''
})

// 表单验证规则（全部必填）
const formRules = {
  community: [
    { required: true, message: '请选择社区', trigger: 'change' }
  ],
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入内容', trigger: 'blur' }
  ]
}

// 打开弹窗（供父组件调用）
const open = (id?:number,board?:string, cover?: string) => {
  // 重置表单
  formData.value.id = (id || undefined) as any
  formData.value.name = board || ''
  formData.value.cover = cover || ''
  if (cover && board && id){
    communityOptions.value = [{
        id:id,
        name: board, // 这里的名字就是显示在框里的 label
        cover: cover
    }]
  }
  formData.value.title = ''
  formData.value.content = ''
  formRef.value?.clearValidate()
  dialogVisible.value = true
}

// 关闭弹窗
const handleClose = () => {
  dialogVisible.value = false
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    // 验证表单
    await formRef.value
    submitting.value = true
    // TODO: 调用发帖 API
    const postData = ({
       boardId: formData.value.id,
       title: formData.value.title,
       content: formData.value.content
    })
    console.log("传入的值",postData)
    const res = await apiCreatePost(postData)  
    ElMessage.success('发布成功！')
    // 关闭弹窗
    dialogVisible.value = false
    // 触发成功事件，让父组件刷新列表等
    emit('success', { ...formData })
  } catch (error) {
    console.error('发布失败:', error)
    ElMessage.error('发布失败，请重试')
  } finally {
    submitting.value = false
  }
}

// 定义事件
const emit = defineEmits(['success'])

// 暴露方法给父组件
defineExpose({
  open
})
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>