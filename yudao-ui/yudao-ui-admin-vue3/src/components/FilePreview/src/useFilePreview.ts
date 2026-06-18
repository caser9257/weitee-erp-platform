import { ref } from 'vue'

interface FileInfo {
  name: string
  url: string
  size?: number
  type?: string
  createTime?: string
}

/**
 * 文件预览 Hook
 *
 * 使用方式：
 * ```vue
 * <script setup>
 * const { previewRef, openPreview } = useFilePreview()
 * </script>
 *
 * <template>
 *   <FilePreviewDialog ref="previewRef" />
 *   <el-button @click="openPreview({ name: '合同.pdf', url: '...' })">预览</el-button>
 * </template>
 * ```
 */
export function useFilePreview() {
  const previewRef = ref<any>(null)

  /**
   * 打开文件预览
   *
   * @param file 文件信息
   */
  const openPreview = (file: FileInfo) => {
    previewRef.value?.open(file)
  }

  /**
   * 打开图片预览
   *
   * @param url 图片URL
   * @param name 图片名称（可选）
   */
  const openImagePreview = (url: string, name?: string) => {
    openPreview({
      name: name || '图片预览',
      url,
    })
  }

  /**
   * 打开PDF预览
   *
   * @param url PDF URL
   * @param name PDF名称（可选）
   */
  const openPdfPreview = (url: string, name?: string) => {
    openPreview({
      name: name || 'PDF预览',
      url,
    })
  }

  return {
    previewRef,
    openPreview,
    openImagePreview,
    openPdfPreview,
  }
}
