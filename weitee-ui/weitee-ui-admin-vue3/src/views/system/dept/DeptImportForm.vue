<template>
  <Dialog v-model="dialogVisible" title="部门导入" width="400">
    <el-upload
      ref="uploadRef"
      v-model:file-list="fileList"
      :action="importUrl"
      :auto-upload="false"
      :disabled="formLoading"
      :headers="uploadHeaders"
      :limit="1"
      :on-error="submitFormError"
      :on-exceed="handleExceed"
      :on-success="submitFormSuccess"
      accept=".xlsx, .xls"
      drag
    >
      <Icon icon="ep:upload" />
      <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
      <template #tip>
        <div class="el-upload__tip text-center">
          <div class="el-upload__tip">
            <el-checkbox v-model="updateSupport" />
            是否更新已经存在的部门数据
          </div>
          <span>仅允许导入 xls、xlsx 格式文件。</span>
          <el-link
            :underline="false"
            style="font-size: 12px; vertical-align: baseline"
            type="primary"
            @click="importTemplate"
          >
            下载模板
          </el-link>
        </div>
      </template>
    </el-upload>
    <template #footer>
      <el-button :disabled="!canSubmit" type="primary" @click="submitForm">确 定</el-button>
      <el-button :disabled="formLoading" @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import * as DeptApi from '@/api/system/dept'
import { getAccessToken, getTenantId } from '@/utils/auth'
import download from '@/utils/download'
import type { UploadInstance, UploadProps, UploadUserFile } from 'element-plus'

defineOptions({ name: 'SystemDeptImportForm' })

const message = useMessage()

const dialogVisible = ref(false)
const formLoading = ref(false)
const updateSupport = ref(false)
const fileList = ref<UploadUserFile[]>([])
const uploadHeaders = ref<Record<string, string>>({})
const uploadRef = ref<UploadInstance>()

const importUrl = computed(() => {
  return (
    import.meta.env.VITE_BASE_URL +
    import.meta.env.VITE_API_URL +
    '/system/dept/import?updateSupport=' +
    (updateSupport.value ? 1 : 0)
  )
})

const hasSelectedFile = computed(() => fileList.value.length > 0)
const canSubmit = computed(() => hasSelectedFile.value && !formLoading.value)

/** 打开弹窗 */
const open = async () => {
  dialogVisible.value = true
  await resetForm()
}
defineExpose({ open })

/** 提交表单 */
const submitForm = () => {
  if (!hasSelectedFile.value) {
    message.error('请上传文件')
    return
  }
  uploadHeaders.value = buildUploadHeaders()
  formLoading.value = true
  uploadRef.value?.submit()
}

const emit = defineEmits(['success'])

/** 文件上传成功 */
const submitFormSuccess: UploadProps['onSuccess'] = (response: any) => {
  formLoading.value = false
  if (response.code !== 0) {
    message.error(response.msg || '导入失败，请稍后重试')
    return
  }
  const data = response.data
  let text = '新增成功数量：' + data.createDeptNames.length + '；'
  for (const name of data.createDeptNames) {
    text += '< ' + name + ' >'
  }
  text += '更新成功数量：' + data.updateDeptNames.length + '；'
  for (const name of data.updateDeptNames) {
    text += '< ' + name + ' >'
  }
  text += '更新失败数量：' + Object.keys(data.failureDeptNames).length + '；'
  for (const name in data.failureDeptNames) {
    text += '< ' + name + ': ' + data.failureDeptNames[name] + ' >'
  }
  message.alert(text)
  dialogVisible.value = false
  emit('success')
  resetForm()
}

/** 上传错误提示 */
const submitFormError: UploadProps['onError'] = () => {
  message.error('上传失败，请您重新上传！')
  formLoading.value = false
}

/** 重置表单 */
const resetForm = async () => {
  formLoading.value = false
  updateSupport.value = false
  fileList.value = []
  uploadHeaders.value = buildUploadHeaders()
  await nextTick()
  uploadRef.value?.clearFiles()
}

/** 文件数超出提示 */
const handleExceed: UploadProps['onExceed'] = () => {
  message.error('最多只能上传一个文件！')
}

/** 组装上传请求头 */
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

/** 下载模板 */
const importTemplate = async () => {
  const res = await DeptApi.importDeptTemplate()
  download.excel(res, '部门导入模板.xls')
}
</script>
