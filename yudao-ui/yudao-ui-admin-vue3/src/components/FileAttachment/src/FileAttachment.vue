<template>
  <div class="file-attachment">
    <!-- 附件列表 -->
    <div v-if="fileList.length > 0" class="file-attachment__list">
      <div
        v-for="file in fileList"
        :key="file.id"
        class="file-attachment__item"
      >
        <div class="file-attachment__info" @click="handlePreview(file)">
          <Icon :icon="getFileIcon(file.name)" class="file-attachment__icon" />
          <div class="file-attachment__details">
            <div class="file-attachment__name">{{ file.name }}</div>
            <div class="file-attachment__meta">
              <span>{{ formatSize(file.size) }}</span>
              <span v-if="file.createTime">{{ file.createTime }}</span>
            </div>
          </div>
        </div>
        <div class="file-attachment__actions">
          <el-button link type="primary" @click="handleDownload(file)">
            <Icon icon="ep:download" />
          </el-button>
          <el-button
            v-if="!disabled"
            link
            type="danger"
            @click="handleDelete(file)"
          >
            <Icon icon="ep:delete" />
          </el-button>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="file-attachment__empty">
      <Icon icon="ep:folder-opened" size="24" class="text-[var(--erp-slate-300)]" />
      <span class="text-[var(--erp-slate-400)] text-sm">暂无附件</span>
    </div>

    <!-- 上传按钮 -->
    <div v-if="!disabled" class="file-attachment__upload">
      <el-upload
        ref="uploadRef"
        :action="uploadUrl"
        :before-upload="beforeUpload"
        :on-success="handleUploadSuccess"
        :on-error="handleUploadError"
        :show-file-list="false"
        :http-request="httpRequest"
        multiple
      >
        <el-button type="primary" plain>
          <Icon icon="ep:upload" class="mr-5px" />
          上传附件
        </el-button>
      </el-upload>
    </div>

    <!-- 文件预览弹窗 -->
    <FilePreviewDialog ref="previewRef" />
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUpload } from '@/components/UploadFile/src/useUpload'
import { FilePreviewDialog } from '@/components/FilePreview'
import request from '@/config/axios'

defineOptions({ name: 'FileAttachment' })

interface FileInfo {
  id: number
  name: string
  url: string
  size?: number
  type?: string
  createTime?: string
}

const props = defineProps<{
  bizType: string  // 业务类型：EXPENSE / CONTRACT / INVOICE 等
  bizId?: number   // 业务ID
  bizNo?: string   // 业务单号
  disabled?: boolean
  limit?: number   // 文件数量限制
}>()

const emit = defineEmits(['change'])

const fileList = ref<FileInfo[]>([])
const previewRef = ref<InstanceType<typeof FilePreviewDialog>>()
const { uploadUrl, httpRequest } = useUpload()

// 加载文件列表
const loadFiles = async () => {
  if (!props.bizId || !props.bizType) return
  
  try {
    const data = await request.get({
      url: '/infra/file-biz-rel/list-by-biz',
      params: { bizType: props.bizType, bizId: props.bizId }
    })
    
    if (data && data.length > 0) {
      const fileIds = data.map((rel: any) => rel.fileId)
      // 批量获取文件信息
      const files = await request.get({
        url: '/infra/file/list-by-ids',
        params: { ids: fileIds.join(',') }
      })
      fileList.value = files || []
    } else {
      fileList.value = []
    }
  } catch (e) {
    console.error('加载附件列表失败', e)
  }
}

// 允许的文件类型白名单
const ALLOWED_FILE_TYPES = [
  // 文档
  '.doc', '.docx', '.xls', '.xlsx', '.ppt', '.pptx', '.pdf', '.txt', '.csv',
  // 图片
  '.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp', '.svg',
  // 压缩包
  '.zip', '.rar', '.7z', '.tar', '.gz',
  // 其他
  '.mp3', '.mp4', '.wav'
]

// 上传前校验
const beforeUpload = (file: File) => {
  if (props.limit && fileList.value.length >= props.limit) {
    ElMessage.error(`最多上传 ${props.limit} 个文件`)
    return false
  }
  
  const maxSize = 50 * 1024 * 1024 // 50MB
  if (file.size > maxSize) {
    ElMessage.error('文件大小不能超过 50MB')
    return false
  }
  
  // 校验文件类型
  const fileName = file.name.toLowerCase()
  const isAllowed = ALLOWED_FILE_TYPES.some(ext => fileName.endsWith(ext))
  if (!isAllowed) {
    ElMessage.error(`不支持的文件类型，允许的类型：${ALLOWED_FILE_TYPES.join('、')}`)
    return false
  }
  
  return true
}

// 上传成功
const handleUploadSuccess = async (response: any, file: any) => {
  const fileId = response.data
  
  // 创建业务关联
  if (props.bizId && props.bizType) {
    try {
      await request.post({
        url: '/infra/file-biz-rel/create',
        data: {
          fileId,
          bizType: props.bizType,
          bizId: props.bizId,
          bizNo: props.bizNo
        }
      })
    } catch (e) {
      console.error('创建文件关联失败', e)
    }
  }
  
  ElMessage.success('上传成功')
  await loadFiles()
  emit('change', fileList.value)
}

// 上传失败
const handleUploadError = () => {
  ElMessage.error('上传失败')
}

// 预览
const handlePreview = (file: FileInfo) => {
  previewRef.value?.open({
    name: file.name,
    url: file.url,
    size: file.size
  })
}

// 下载
const handleDownload = (file: FileInfo) => {
  const link = document.createElement('a')
  link.href = file.url
  link.download = file.name
  link.click()
}

// 删除
const handleDelete = async (file: FileInfo) => {
  try {
    await ElMessageBox.confirm('确认删除该附件？', '提示', { type: 'warning' })
    
    // 删除业务关联
    if (props.bizId && props.bizType) {
      await request.delete({
        url: '/infra/file-biz-rel/delete',
        params: { fileId: file.id, bizType: props.bizType, bizId: props.bizId }
      })
    }
    
    // 删除文件本身
    await request.delete({
      url: '/infra/file/delete',
      params: { id: file.id }
    })
    
    ElMessage.success('删除成功')
    await loadFiles()
    emit('change', fileList.value)
  } catch {
    // 用户取消
  }
}

// 获取文件图标
const getFileIcon = (name: string) => {
  const ext = name?.split('.').pop()?.toLowerCase()
  const iconMap: Record<string, string> = {
    pdf: 'ep:document',
    doc: 'ep:document',
    docx: 'ep:document',
    xls: 'ep:grid',
    xlsx: 'ep:grid',
    jpg: 'ep:picture',
    jpeg: 'ep:picture',
    png: 'ep:picture',
    gif: 'ep:picture',
    zip: 'ep:folder',
    rar: 'ep:folder',
  }
  return iconMap[ext || ''] || 'ep:document'
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
  return `${size.toFixed(1)} ${units[unitIndex]}`
}

// 监听 bizId 变化
watch(() => props.bizId, () => {
  loadFiles()
})

onMounted(() => {
  loadFiles()
})

defineExpose({ loadFiles })
</script>

<style scoped lang="scss">
.file-attachment {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.file-attachment__list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.file-attachment__item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  background: var(--erp-slate-50);
  border: 1px solid var(--erp-slate-100);
  border-radius: 8px;
  transition: all 0.2s;

  &:hover {
    border-color: var(--erp-primary-200);
    background: var(--erp-primary-50);
  }
}

.file-attachment__info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  flex: 1;
  min-width: 0;
}

.file-attachment__icon {
  font-size: 20px;
  color: var(--erp-slate-400);
  flex-shrink: 0;
}

.file-attachment__details {
  flex: 1;
  min-width: 0;
}

.file-attachment__name {
  font-size: 13px;
  font-weight: 500;
  color: var(--erp-slate-700);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-attachment__meta {
  display: flex;
  gap: 8px;
  font-size: 12px;
  color: var(--erp-slate-400);
  margin-top: 2px;
}

.file-attachment__actions {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
}

.file-attachment__empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 24px;
  background: var(--erp-slate-50);
  border: 1px dashed var(--erp-slate-200);
  border-radius: 8px;
}

.file-attachment__upload {
  display: flex;
  justify-content: flex-start;
}
</style>
