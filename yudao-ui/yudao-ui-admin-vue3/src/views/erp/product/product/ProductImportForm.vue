<template>
  <Dialog v-model="dialogVisible" title="产品导入" width="600">
    <div class="import-wizard">
      <div class="wizard-step mb-25px">
        <div class="step-header mb-10px flex items-center">
          <div
            class="step-badge mr-10px flex h-24px w-24px items-center justify-center rounded-full bg-primary text-sm font-bold text-white"
          >
            1
          </div>
          <span class="text-16px font-bold text-slate-800">下载导入模板</span>
        </div>
        <div class="step-body pl-34px">
          <p class="mb-12px text-13px text-slate-500">
            请下载系统提供的数据模板，按规定格式填写完成后再上传。
          </p>
          <el-button plain type="primary" @click="importTemplate">
            <Icon class="mr-5px" icon="ep:download" />
            下载标准导入模板
          </el-button>
        </div>
      </div>

      <div class="wizard-step">
        <div class="step-header mb-10px flex items-center">
          <div
            class="step-badge mr-10px flex h-24px w-24px items-center justify-center rounded-full bg-primary text-sm font-bold text-white"
          >
            2
          </div>
          <span class="text-16px font-bold text-slate-800">上传数据文件</span>
        </div>
        <div class="step-body pl-34px">
          <el-upload
            ref="uploadRef"
            v-model:file-list="fileList"
            v-loading="uploadLoading"
            :action="importUrl"
            :auto-upload="false"
            :disabled="uploadLoading"
            :headers="uploadHeaders"
            :limit="1"
            :on-error="submitFormError"
            :on-exceed="handleExceed"
            :on-success="submitFormSuccess"
            accept=".xlsx, .xls"
            class="upload-full"
            drag
          >
            <Icon :size="60" class="text-slate-300" icon="ep:upload-filled" />
            <div class="el-upload__text text-14px">
              将填写完成的文件拖到此处，或<em class="font-bold text-primary">点击上传</em>
            </div>
            <div class="mt-5px text-12px text-slate-400">
              仅支持 .xls / .xlsx 格式，建议文件大小不超过 5MB
            </div>
            <template #tip>
              <div
                class="mt-15px flex items-center rounded border border-orange-100 bg-orange-50/60 p-12px"
              >
                <el-checkbox v-model="updateSupport" class="!mr-0">
                  <span class="font-medium text-slate-700">
                    如遇到已存在的产品编码，是否自动覆盖现有数据？
                  </span>
                </el-checkbox>
              </div>
            </template>
          </el-upload>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="flex flex-wrap justify-end gap-12px">
        <el-button :disabled="!canSubmit" class="w-120px" type="primary" @click="submitFileForm">
          <Icon class="mr-5px" icon="ep:upload" />
          开始导入
        </el-button>
        <el-button :disabled="uploadLoading" @click="dialogVisible = false">取消</el-button>
      </div>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import { getAccessToken } from '@/utils/auth'
import download from '@/utils/download'
import request from '@/config/axios'
import type { UploadInstance, UploadProps, UploadUserFile } from 'element-plus'

defineOptions({ name: 'ProductImportForm' })

const message = useMessage()

const dialogVisible = ref(false)
const uploadLoading = ref(false)
const updateSupport = ref(false)
const fileList = ref<UploadUserFile[]>([])
const uploadHeaders = ref<Record<string, string>>({})
const uploadRef = ref<UploadInstance>()

const importUrl = computed(() => {
  return (
    import.meta.env.VITE_BASE_URL +
    import.meta.env.VITE_API_URL +
    '/erp/product/import?updateSupport=' +
    (updateSupport.value ? 1 : 0)
  )
})

const hasSelectedFile = computed(() => fileList.value.length > 0)
const canSubmit = computed(() => hasSelectedFile.value && !uploadLoading.value)

/** 打开弹窗 */
const open = async () => {
  dialogVisible.value = true
  await resetForm()
}
defineExpose({ open })

/** 提交表单 */
const submitFileForm = () => {
  if (!hasSelectedFile.value) {
    message.error('请上传文件')
    return
  }
  uploadHeaders.value = buildUploadHeaders()
  uploadLoading.value = true
  uploadRef.value?.submit()
}

const emit = defineEmits(['success'])

/** 文件上传成功 */
const submitFormSuccess: UploadProps['onSuccess'] = (response: any) => {
  uploadLoading.value = false
  if (response.code !== 0) {
    message.error(response.msg || '导入失败，请稍后重试')
    return
  }
  dialogVisible.value = false
  emit('success')
  message.success('导入成功！')
  resetForm()
}

/** 上传错误提示 */
const submitFormError: UploadProps['onError'] = () => {
  message.error('上传失败，请您重新上传！')
  uploadLoading.value = false
}

/** 文件数超出提示 */
const handleExceed: UploadProps['onExceed'] = () => {
  message.error('最多只能上传一个文件！')
}

/** 重置表单 */
const resetForm = async () => {
  uploadLoading.value = false
  updateSupport.value = false
  fileList.value = []
  uploadHeaders.value = buildUploadHeaders()
  await nextTick()
  uploadRef.value?.clearFiles()
}

/** 组装上传请求头 */
const buildUploadHeaders = () => {
  const headers: Record<string, string> = {
    Authorization: 'Bearer ' + getAccessToken()
  }

  return headers
}

/** 下载模板操作 */
const importTemplate = async () => {
  const res = await request.download({ url: '/erp/product/get-import-template' })
  download.excel(res, '产品导入模板.xls')
}
</script>

<style scoped lang="scss">
.upload-full {
  width: 100%;

  :deep(.el-upload) {
    width: 100%;
  }

  :deep(.el-upload-dragger) {
    width: 100%;
    padding: 30px 0;
    border-color: var(--erp-slate-200);
    background-color: var(--erp-slate-50);
    transition: all 0.2s ease;

    &:hover {
      border-color: var(--el-color-primary);
      background-color: #fff;
    }
  }
}
</style>
