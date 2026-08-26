<template>
  <div class="post-content-editor">
    <div class="editor-actions">
      <el-tooltip content="插入图片" placement="top">
        <el-button
          circle
          plain
          size="small"
          :disabled="disabled"
          @click="openFilePicker"
        >
          <el-icon><Picture /></el-icon>
        </el-button>
      </el-tooltip>
      <el-tooltip content="清空正文" placement="top">
        <el-button
          circle
          plain
          size="small"
          :disabled="disabled || isEmpty"
          @click="clear"
        >
          <el-icon><Delete /></el-icon>
        </el-button>
      </el-tooltip>
      <span v-if="pendingUploadCount > 0" class="upload-state">图片上传中</span>
    </div>

    <div class="editor-shell" :class="{ 'is-disabled': disabled }">
      <div v-if="isEmpty" class="editor-placeholder">{{ placeholder }}</div>
      <div
        ref="editorRef"
        class="editor-surface"
        :contenteditable="!disabled"
        spellcheck="false"
        :aria-disabled="disabled"
        @input="handleInput"
        @keydown="handleKeydown"
        @paste="handlePaste"
        @drop="handleDrop"
        @dragover.prevent
        @mouseup="captureSelection"
        @keyup="captureSelection"
        @blur="captureSelection"
      ></div>
    </div>

    <input
      ref="fileInputRef"
      type="file"
      accept=".jpg,.jpeg,.png,image/jpeg,image/png"
      multiple
      hidden
      @change="handleFilePick"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Delete, Picture } from '@element-plus/icons-vue'
import { apiUploadPostImage } from '@/api/post'
import type {
  PostContentDocument,
  PostContentNode,
  PostContentTextNode,
  PostImageUploadResponse,
} from '@/models/post/postTypes'

export interface PostContentEditorExpose {
  serialize: () => string
  clear: () => void
  focus: () => void
  hasPendingUpload: () => boolean
  hasContent: () => boolean
}

const props = withDefaults(defineProps<{
  modelValue?: string
  placeholder?: string
  disabled?: boolean
}>(), {
  modelValue: '',
  placeholder: '写下正文',
  disabled: false,
})

const emit = defineEmits<{
  (event: 'update:modelValue', value: string): void
}>()

const editorRef = ref<HTMLDivElement>()
const fileInputRef = ref<HTMLInputElement>()
const pendingUploadCount = ref(0)
const savedRange = ref<Range | null>(null)
const activeUploadIds = new Set<string>()
const resourceBaseUrl = import.meta.env.VITE_RESOURCE_URL ?? ''

const isEmpty = computed(() => pendingUploadCount.value === 0 && !hasMeaningfulContent())

function focus() {
  editorRef.value?.focus()
}

function hasPendingUpload() {
  return pendingUploadCount.value > 0
}

function hasMeaningfulContent() {
  return getDocumentContent() !== null
}

function openFilePicker() {
  if (props.disabled) {
    return
  }
  captureSelection()
  if (fileInputRef.value) {
    fileInputRef.value.value = ''
    fileInputRef.value.click()
  }
}

function captureSelection() {
  const editor = editorRef.value
  const selection = window.getSelection()
  if (!editor || !selection || selection.rangeCount === 0) {
    return
  }

  const range = selection.getRangeAt(0)
  if (editor.contains(range.commonAncestorContainer)) {
    savedRange.value = range.cloneRange()
  }
}

function getWritableRange() {
  const editor = editorRef.value
  const selection = window.getSelection()
  if (editor && selection && selection.rangeCount > 0) {
    const currentRange = selection.getRangeAt(0)
    if (editor.contains(currentRange.commonAncestorContainer)) {
      return currentRange.cloneRange()
    }
  }

  if (savedRange.value) {
    return savedRange.value.cloneRange()
  }

  const range = document.createRange()
  if (editor) {
    range.selectNodeContents(editor)
    range.collapse(false)
  }
  return range
}

function placeCaretAfter(node: Node) {
  const selection = window.getSelection()
  if (!selection) {
    return
  }
  const range = document.createRange()
  range.setStartAfter(node)
  range.collapse(true)
  selection.removeAllRanges()
  selection.addRange(range)
  savedRange.value = range.cloneRange()
}

function insertNodeAtSelection(node: Node) {
  const range = getWritableRange()
  range.deleteContents()
  range.insertNode(node)
  placeCaretAfter(node)
}

function insertTextAtSelection(text: string) {
  const textNode = document.createTextNode(text)
  insertNodeAtSelection(textNode)
}

function createUploadPlaceholder(uploadId: string) {
  const placeholder = document.createElement('span')
  placeholder.className = 'image-placeholder'
  placeholder.setAttribute('contenteditable', 'false')
  placeholder.dataset.uploadId = uploadId
  placeholder.textContent = '上传中'
  return placeholder
}

function createImageNode(path: string) {
  const image = document.createElement('img')
  image.className = 'post-image-node'
  image.setAttribute('contenteditable', 'false')
  image.setAttribute('draggable', 'false')
  image.dataset.postImage = 'true'
  image.dataset.path = path
  image.src = `${resourceBaseUrl}${path}`
  image.alt = '帖子正文图片'
  return image
}

function getDocumentContent() {
  const editor = editorRef.value
  if (!editor) {
    return null
  }

  const nodes: PostContentNode[] = []
  let textBuffer = ''

  const flushText = () => {
    if (textBuffer.length === 0) {
      return
    }
    nodes.push({ type: 'text', text: textBuffer } satisfies PostContentTextNode)
    textBuffer = ''
  }

  const walk = (current: ChildNode) => {
    if (current.nodeType === Node.TEXT_NODE) {
      textBuffer += (current.textContent ?? '').replace(/\u200B/g, '')
      return
    }

    if (current.nodeType !== Node.ELEMENT_NODE) {
      return
    }

    const element = current as HTMLElement

    if (element.dataset.uploadId) {
      return
    }

    if (element.dataset.postImage === 'true') {
      const path = element.dataset.path
      if (path) {
        flushText()
        nodes.push({ type: 'image', path })
      }
      return
    }

    Array.from(element.childNodes).forEach(walk)

    if (element.tagName === 'DIV' || element.tagName === 'P') {
      textBuffer += '\n'
    }
  }

  Array.from(editor.childNodes).forEach(walk)
  flushText()

  const hasMeaningfulText = nodes.some((node) => node.type === 'image' || node.type === 'text' && node.text.trim().length > 0)
  if (!hasMeaningfulText) {
    return null
  }

  const documentContent: PostContentDocument = {
    version: 1,
    nodes,
  }

  return documentContent
}

function emitDocument() {
  const documentContent = getDocumentContent()
  emit('update:modelValue', documentContent ? JSON.stringify(documentContent) : '')
}

function hasContent() {
  return hasMeaningfulContent()
}

function clear() {
  const editor = editorRef.value
  if (editor) {
    editor.innerHTML = ''
  }
  savedRange.value = null
  activeUploadIds.clear()
  pendingUploadCount.value = 0
  fileInputRef.value && (fileInputRef.value.value = '')
  emit('update:modelValue', '')
}

function reset() {
  clear()
}

function handleInput() {
  captureSelection()
  emitDocument()
}

function handleKeydown(event: KeyboardEvent) {
  if (props.disabled) {
    return
  }
  if (event.key === 'Enter') {
    event.preventDefault()
    insertTextAtSelection('\n')
    emitDocument()
  }
}

function handlePaste(event: ClipboardEvent) {
  if (props.disabled) {
    return
  }
  event.preventDefault()
  const text = event.clipboardData?.getData('text/plain') ?? ''
  if (text) {
    insertTextAtSelection(text)
    emitDocument()
  }
}

function isSupportedImage(file: File) {
  return ['image/jpeg', 'image/png'].includes(file.type) || /\.(jpe?g|png)$/i.test(file.name)
}

function getDropRange(event: DragEvent) {
  const doc = document as Document & {
    caretRangeFromPoint?: (x: number, y: number) => Range | null
    caretPositionFromPoint?: (x: number, y: number) => { offsetNode: Node; offset: number } | null
  }

  if (doc.caretRangeFromPoint) {
    return doc.caretRangeFromPoint(event.clientX, event.clientY)
  }

  if (doc.caretPositionFromPoint) {
    const caret = doc.caretPositionFromPoint(event.clientX, event.clientY)
    if (caret) {
      const range = document.createRange()
      range.setStart(caret.offsetNode, caret.offset)
      range.collapse(true)
      return range
    }
  }

  return null
}

async function handleDrop(event: DragEvent) {
  if (props.disabled) {
    return
  }

  const rawFiles = Array.from(event.dataTransfer?.files ?? [])
  const files = rawFiles.filter(isSupportedImage)
  if (files.length === 0) {
    return
  }
  if (files.length !== rawFiles.length) {
    ElMessage.warning('仅支持 jpg/jpeg/png 图片')
  }

  event.preventDefault()
  captureSelection()
  const range = getDropRange(event) ?? savedRange.value?.cloneRange() ?? getWritableRange()
  await queueUploadFiles(files, range)
}

async function handleFilePick(event: Event) {
  if (props.disabled) {
    return
  }

  const target = event.target as HTMLInputElement
  const rawFiles = Array.from(target.files ?? [])
  const files = rawFiles.filter(isSupportedImage)
  target.value = ''
  if (files.length === 0) {
    return
  }
  if (files.length !== rawFiles.length) {
    ElMessage.warning('仅支持 jpg/jpeg/png 图片')
  }

  captureSelection()
  const range = savedRange.value?.cloneRange() ?? getWritableRange()
  await queueUploadFiles(files, range)
}

async function queueUploadFiles(files: File[], baseRange: Range) {
  let nextRange = baseRange.cloneRange()

  for (const file of files) {
    const uploadId = `upload_${Date.now()}_${Math.random().toString(16).slice(2)}`
    const placeholder = createUploadPlaceholder(uploadId)
    activeUploadIds.add(uploadId)
    pendingUploadCount.value += 1

    const insertRange = nextRange.cloneRange()
    insertRange.deleteContents()
    insertRange.insertNode(placeholder)
    placeCaretAfter(placeholder)
    nextRange = getWritableRange()

    void uploadImage(file, uploadId)
  }

  emitDocument()
}

async function uploadImage(file: File, uploadId: string) {
  const formData = new FormData()
  formData.append('file', file)

  try {
    const path = await apiUploadPostImage(formData) as PostImageUploadResponse
    if (!activeUploadIds.has(uploadId)) {
      return
    }
    replacePlaceholder(uploadId, path)
    emitDocument()
  } catch (error) {
    if (activeUploadIds.has(uploadId)) {
      removePlaceholder(uploadId)
      ElMessage.error('该图片未能上传，未加入正文')
      emitDocument()
    }
    console.error(error)
  } finally {
    if (activeUploadIds.has(uploadId)) {
      activeUploadIds.delete(uploadId)
      pendingUploadCount.value = Math.max(0, pendingUploadCount.value - 1)
    }
  }
}

function replacePlaceholder(uploadId: string, path: string) {
  const placeholder = editorRef.value?.querySelector(`[data-upload-id="${uploadId}"]`)
  if (!placeholder || !editorRef.value) {
    return
  }

  const image = createImageNode(path)
  placeholder.replaceWith(image)
}

function removePlaceholder(uploadId: string) {
  const placeholder = editorRef.value?.querySelector(`[data-upload-id="${uploadId}"]`)
  placeholder?.remove()
}

function serialize() {
  const documentContent = getDocumentContent()
  return documentContent ? JSON.stringify(documentContent) : ''
}

function triggerSyncFromModel() {
  if (!props.modelValue) {
    clear()
  }
}

defineExpose<PostContentEditorExpose>({
  serialize,
  clear,
  focus,
  hasPendingUpload,
  hasContent,
})

nextTick(() => {
  triggerSyncFromModel()
})
</script>

<style scoped>
.post-content-editor {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
}

.editor-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.upload-state {
  font-size: 12px;
  color: #7a7a7a;
}

.editor-shell {
  position: relative;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  background: #fff;
  min-height: 320px;
  overflow: hidden;
}

.editor-shell:focus-within {
  border-color: #409eff;
}

.editor-shell.is-disabled {
  background: #f5f7fa;
}

.editor-placeholder {
  position: absolute;
  top: 14px;
  left: 14px;
  color: #a8abb2;
  font-size: 14px;
  pointer-events: none;
}

.editor-surface {
  min-height: 320px;
  padding: 14px;
  outline: none;
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.75;
  font-size: 14px;
  color: #303133;
  caret-color: #409eff;
}

.image-placeholder {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 96px;
  min-height: 64px;
  padding: 0 12px;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  background: #f5f7fa;
  color: #909399;
  font-size: 12px;
  vertical-align: middle;
}

.post-image-node {
  display: block;
  max-width: 100%;
  height: auto;
  margin: 8px 0;
  border-radius: 4px;
}
</style>
