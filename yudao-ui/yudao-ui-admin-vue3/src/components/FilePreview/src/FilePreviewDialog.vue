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

      <!-- Word/Excel/PPT 预览（通过 Office Online 或第三方服务） -->
      <div v-else-if="isOffice" class="file-preview__office">
        <iframe
          :src="officePreviewUrl"
          class="file-preview__iframe"
          frameborder="0"
        ></iframe>
      </div>

      <!-- 视频预览 -->
      <div v-else-if="isVideo" class="file-preview__video">
        <video
          :src="file?.url"
          controls
          class="file-preview__video-player"
        >
          您的浏览器不支持视频播放
        </video>
      </div>

      <!-- 音频预览 -->
      <div v-else-if="isAudio" class="file-preview__audio">
        <div class="file-preview__audio-info">
          <Icon icon="ep:microphone" size="48" class="text-primary-500" />
          <div class="file-preview__name">{{ file?.name }}</div>
        </div>
        <audio
          :src="file?.url"
          controls
          class="file-preview__audio-player"
        >
          您的浏览器不支持音频播放
        </audio>
      </div>

      <!-- 文本文件预览 -->
      <div v-else-if="isText" class="file-preview__text">
        <div v-if="textLoading" class="file-preview__loading">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>加载中...</span>
        </div>
        <pre v-else class="file-preview__text-content">{{ textContent }}</pre>
      </div>

      <!-- 代码文件预览 -->
      <div v-else-if="isCode" class="file-preview__code">
        <div v-if="textLoading" class="file-preview__loading">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>加载中...</span>
        </div>
        <pre v-else class="file-preview__code-content"><code>{{ textContent }}</code></pre>
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
        <el-button v-if="!isImage && !isPdf && !isOffice" type="primary" @click="download">
          <Icon icon="ep:download" class="mr-5px" />
          下载
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import request from '@/config/axios'

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
const textContent = ref('')
const textLoading = ref(false)

// 文件类型判断
const isImage = computed(() => {
  if (!file.value) return false
  const imageExtensions = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg', 'ico']
  const ext = file.value.name?.split('.').pop()?.toLowerCase()
  return imageExtensions.includes(ext || '')
})

const isPdf = computed(() => {
  if (!file.value) return false
  return file.value.name?.toLowerCase().endsWith('.pdf') || false
})

const isOffice = computed(() => {
  if (!file.value) return false
  const officeExtensions = ['doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx']
  const ext = file.value.name?.split('.').pop()?.toLowerCase()
  return officeExtensions.includes(ext || '')
})

const isVideo = computed(() => {
  if (!file.value) return false
  const videoExtensions = ['mp4', 'webm', 'ogg', 'mov', 'avi', 'wmv']
  const ext = file.value.name?.split('.').pop()?.toLowerCase()
  return videoExtensions.includes(ext || '')
})

const isAudio = computed(() => {
  if (!file.value) return false
  const audioExtensions = ['mp3', 'wav', 'ogg', 'aac', 'flac', 'm4a']
  const ext = file.value.name?.split('.').pop()?.toLowerCase()
  return audioExtensions.includes(ext || '')
})

const isText = computed(() => {
  if (!file.value) return false
  const textExtensions = ['txt', 'csv', 'log', 'ini', 'conf', 'yml', 'yaml', 'xml', 'json', 'md']
  const ext = file.value.name?.split('.').pop()?.toLowerCase()
  return textExtensions.includes(ext || '')
})

const isCode = computed(() => {
  if (!file.value) return false
  const codeExtensions = ['js', 'ts', 'jsx', 'tsx', 'vue', 'java', 'py', 'go', 'rs', 'c', 'cpp', 'h', 'css', 'scss', 'less', 'html', 'sql', 'sh', 'bat']
  const ext = file.value.name?.split('.').pop()?.toLowerCase()
  return codeExtensions.includes(ext || '')
})

// Office 在线预览 URL（使用 Microsoft Office Online 或 Google Docs）
const officePreviewUrl = computed(() => {
  if (!file.value?.url) return ''
  // 方案1: 使用 Microsoft Office Online（需要文件可公开访问）
  // return `https://view.officeapps.live.com/op/embed.aspx?src=${encodeURIComponent(file.value.url)}`
  
  // 方案2: 使用 Google Docs Viewer
  // return `https://docs.google.com/gview?url=${encodeURIComponent(file.value.url)}&embedded=true`
  
  // 方案3: 使用 kkFileView（自部署）
  // return `http://your-server:8012/onlinePreview?url=${encodeURIComponent(file.value.url)}`
  
  // 默认使用 Microsoft Office Online
  return `https://view.officeapps.live.com/op/embed.aspx?src=${encodeURIComponent(file.value.url)}`
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
    mp4: 'ep:video-camera',
    mp3: 'ep:microphone',
  }
  return iconMap[ext || ''] || 'ep:document'
})

// 打开预览
const open = async (fileInfo: FileInfo) => {
  file.value = fileInfo
  visible.value = true
  
  // 如果是文本或代码文件，加载内容
  if (isText.value || isCode.value) {
    await loadTextContent(fileInfo.url)
  }
}

// 加载文本内容
const loadTextContent = async (url: string) => {
  textLoading.value = true
  textContent.value = ''
  try {
    const response = await fetch(url)
    if (response.ok) {
      textContent.value = await response.text()
      // 限制显示长度，避免超大文件卡顿
      if (textContent.value.length > 100000) {
        textContent.value = textContent.value.substring(0, 100000) + '\n\n... [文件内容过长，仅显示前 100000 字符]'
      }
    } else {
      textContent.value = '无法加载文件内容'
    }
  } catch (e) {
    textContent.value = '加载文件内容失败：' + (e as Error).message
  } finally {
    textLoading.value = false
  }
}

// 关闭
const handleClosed = () => {
  file.value = null
  textContent.value = ''
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

.file-preview__pdf,
.file-preview__office {
  width: 100%;
  height: 70vh;
}

.file-preview__iframe {
  width: 100%;
  height: 100%;
  border: none;
}

.file-preview__video {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.file-preview__video-player {
  max-width: 100%;
  max-height: 70vh;
}

.file-preview__audio {
  width: 100%;
  padding: 40px 20px;
  text-align: center;
}

.file-preview__audio-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.file-preview__audio-player {
  width: 100%;
  max-width: 400px;
}

.file-preview__text,
.file-preview__code {
  width: 100%;
  max-height: 70vh;
  overflow: auto;
}

.file-preview__text-content,
.file-preview__code-content {
  padding: 16px;
  margin: 0;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-wrap: break-word;
  background: var(--erp-slate-50);
  border-radius: 8px;
  color: var(--erp-slate-800);
}

.file-preview__code-content {
  background: var(--erp-slate-900);
  color: var(--erp-slate-100);
}

.file-preview__loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 40px;
  color: var(--erp-slate-500);
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
