<template>
  <el-dialog
    v-model="visible"
    :title="file?.name || '文件预览'"
    width="80%"
    top="5vh"
    destroy-on-close
    @closed="handleClosed"
  >
    <div class="file-preview">
      <!-- 图片预览 -->
      <div v-if="isImage" class="file-preview__image">
        <el-image
          :src="file?.url"
          fit="contain"
          :preview-src-list="[file?.url]"
          :initial-index="0"
          class="file-preview__img"
        />
      </div>

      <!-- PDF 预览 -->
      <div v-else-if="isPdf" class="file-preview__pdf">
        <iframe
          :src="file?.url"
          class="file-preview__iframe"
          frameborder="0"
        ></iframe>
      </div>

      <!-- 其他文件 -->
      <div v-else class="file-preview__other">
        <div class="file-preview__info">
          <div class="file-preview__icon">
            <Icon :icon="fileIcon" size="48" />
          </div>
          <div class="file-preview__details">
            <div class="file-preview__name">{{ file?.name }}</div>
            <div class="file-preview__meta">
              <span v-if="file?.size">大小：{{ formatSize(file.size) }}</span>
              <span v-if="file?.createTime">上传时间：{{ file.createTime }}</span>
            </div>
          </div>
        </div>
        <div class="file-preview__actions">
          <el-button type="primary" @click="download">
            <Icon icon="ep:download" class="mr-5px" />
            下载文件
          </el-button>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="file-preview__footer">
        <el-button @click="visible = false">关闭</el-button>
        <el-button v-if="!isImage && !isPdf" type="primary" @click="download">
          <Icon icon="ep:download" class="mr-5px" />
          下载
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

defineOptions({ name: 'FilePreviewDialog' })

interface FileInfo {
  name: string
  url: string
  size?: number
  type?: string
  createTime?: string
}

const visible = ref(false)
const file = ref<FileInfo | null>(null)

// 文件类型判断
const isImage = computed(() => {
  if (!file.value) return false
  const imageExtensions = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg']
  const ext = file.value.name?.split('.').pop()?.toLowerCase()
  return imageExtensions.includes(ext || '')
})

const isPdf = computed(() => {
  if (!file.value) return false
  return file.value.name?.toLowerCase().endsWith('.pdf') || false
})

const fileIcon = computed(() => {
  if (!file.value) return 'ep:document'
  const ext = file.value.name?.split('.').pop()?.toLowerCase()
  const iconMap: Record<string, string> = {
    pdf: 'ep:document',
    doc: 'ep:document',
    docx: 'ep:document',
    xls: 'ep:grid',
    xlsx: 'ep:grid',
    ppt: 'ep:picture',
    pptx: 'ep:picture',
    zip: 'ep:folder',
    rar: 'ep:folder',
  }
  return iconMap[ext || ''] || 'ep:document'
})

// 打开预览
const open = (fileInfo: FileInfo) => {
  file.value = fileInfo
  visible.value = true
}

// 关闭
const handleClosed = () => {
  file.value = null
}

// 下载
const download = () => {
  if (!file.value?.url) return
  const link = document.createElement('a')
  link.href = file.value.url
  link.download = file.value.name
  link.click()
}

// 格式化文件大小
const formatSize = (bytes?: number) => {
  if (!bytes) return '-'
  const units = ['B', 'KB', 'MB', 'GB']
  let size = bytes
  let unitIndex = 0
  while (size >= 1024 && unitIndex < units.length - 1) {
    size /= 1024
    unitIndex++
  }
  return `${size.toFixed(2)} ${units[unitIndex]}`
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.file-preview {
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.file-preview__image {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.file-preview__img {
  max-height: 70vh;
  max-width: 100%;
}

.file-preview__pdf {
  width: 100%;
  height: 70vh;
}

.file-preview__iframe {
  width: 100%;
  height: 100%;
  border: none;
}

.file-preview__other {
  width: 100%;
  text-align: center;
  padding: 40px 20px;
}

.file-preview__info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.file-preview__icon {
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--erp-slate-50);
  border-radius: 12px;
  color: var(--erp-primary-600);
}

.file-preview__details {
  text-align: center;
}

.file-preview__name {
  font-size: 18px;
  font-weight: 600;
  color: var(--erp-slate-900);
  margin-bottom: 8px;
}

.file-preview__meta {
  display: flex;
  gap: 16px;
  font-size: 14px;
  color: var(--erp-slate-500);
}

.file-preview__footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
