<template>
  <Dialog v-model="dialogVisible" title="设备导入" width="600">
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
            请先下载模板，按模板格式填写后再上传。
          </p>
          <el-button plain type="primary" @click="importTemplate">
            <Icon class="mr-5px" icon="ep:download" />
            下载导入模板
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
          <span class="text-16px font-bold text-slate-800">上传设备文件</span>
        </div>
        <div class="step-body pl-34px">
          <el-upload
            ref="uploadRef"
            v-model:file-list="fileList"
            v-loading="formLoading"
            class="upload-full"
            drag
            :action="importUrl + '?updateSupport=' + (updateSupport ? 1 : 0)"
            :auto-upload="false"
            :disabled="formLoading"
            :headers="uploadHeaders"
            :limit="1"
            :on-error="submitFormError"
            :on-exceed="handleExceed"
            :on-success="submitFormSuccess"
            accept=".xlsx, .xls"
          >
            <Icon :size="60" class="text-slate-300" icon="ep:upload-filled" />
            <div class="el-upload__text text-14px">
              将填写完成的文件拖到此处，或<em class="font-bold text-primary">点击上传</em>
            </div>
            <div class="mt-5px text-12px text-slate-400">
              仅支持 .xls / .xlsx 格式
            </div>
            <template #tip>
              <div
                class="mt-15px flex items-center rounded border border-orange-100 bg-orange-50/60 p-12px"
              >
                <el-checkbox v-model="updateSupport" class="!mr-0">
                  <span class="font-medium text-slate-700">
                    是否更新已经存在的设备数据
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
        <el-button :disabled="!canSubmit" type="primary" @click="submitForm">
          <Icon class="mr-5px" icon="ep:upload" />
          开始导入
        </el-button>
        <el-button :disabled="formLoading" @click="dialogVisible = false">取消</el-button>
      </div>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import type { UploadInstance, UploadProps, UploadUserFile } from 'element-plus'
import { DeviceApi } from '@/api/iot/device/device'
import { getAccessToken, getTenantId } from '@/utils/auth'
import download from '@/utils/download'

defineOptions({ name: 'IoTDeviceImportForm' })

const message = useMessage()

const dialogVisible = ref(false)
const formLoading = ref(false)
const updateSupport = ref(false)
const fileList = ref<UploadUserFile[]>([])
const uploadHeaders = ref<Record<string, string>>({})
const uploadRef = ref<UploadInstance>()

const importUrl =
  import.meta.env.VITE_BASE_URL + import.meta.env.VITE_API_URL + '/iot/device/import'

const hasSelectedFile = computed(() => fileList.value.length > 0)
const canSubmit = computed(() => hasSelectedFile.value && !formLoading.value)

const buildUploadHeaders = () => {
  const headers: Record<string, string> = {
    Authorization: 'Bearer ' + getAccessToken()
  }
  const tenantId = getTenantId()
  if (tenantId !== undefined && tenantId !== null && tenantId !== '') {
    headers['tenant-id'] = String(tenantId)
  }
  return headers
}

const resetForm = async () => {
  formLoading.value = false
  updateSupport.value = false
  fileList.value = []
  uploadHeaders.value = buildUploadHeaders()
  await nextTick()
  uploadRef.value?.clearFiles()
}

const open = async () => {
  dialogVisible.value = true
  await resetForm()
}

defineExpose({ open })

const emit = defineEmits(['success'])

const submitForm = async () => {
  if (!hasSelectedFile.value) {
    message.error('请上传文件')
    return
  }
  uploadHeaders.value = buildUploadHeaders()
  formLoading.value = true
  uploadRef.value?.submit()
}

const submitFormSuccess: UploadProps['onSuccess'] = (response: any) => {
  formLoading.value = false
  if (response.code !== 0) {
    message.error(response.msg || '导入失败，请稍后重试')
    return
  }

  const data = response.data || {}
  let text = `上传成功数量：${data.createDeviceNames?.length || 0};`
  for (const deviceName of data.createDeviceNames || []) {
    text += `< ${deviceName} >`
  }
  text += `更新成功数量：${data.updateDeviceNames?.length || 0};`
  for (const deviceName of data.updateDeviceNames || []) {
    text += `< ${deviceName} >`
  }
  text += `更新失败数量：${Object.keys(data.failureDeviceNames || {}).length || 0};`
  for (const deviceName in data.failureDeviceNames || {}) {
    text += `< ${deviceName}: ${data.failureDeviceNames[deviceName]} >`
  }
  message.alert(text)
  dialogVisible.value = false
  emit('success')
  resetForm()
}

const submitFormError: UploadProps['onError'] = () => {
  message.error('上传失败，请您重新上传！')
  formLoading.value = false
}

const handleExceed: UploadProps['onExceed'] = () => {
  message.error('最多只能上传一个文件！')
}

const importTemplate = async () => {
  const res = await DeviceApi.importDeviceTemplate()
  download.excel(res, '设备导入模板.xls')
}
</script>

<style scoped>
.upload-full {
  width: 100%;

  :deep(.el-upload) {
    width: 100%;
  }

  :deep(.el-upload-dragger) {
    width: 100%;
    padding: 30px 0;
    border-color: #e2e8f0;
    background-color: #f8fafc;
    transition: all 0.2s ease;
  }

  :deep(.el-upload-dragger:hover) {
    border-color: var(--el-color-primary);
    background-color: #fff;
  }
}
</style>
