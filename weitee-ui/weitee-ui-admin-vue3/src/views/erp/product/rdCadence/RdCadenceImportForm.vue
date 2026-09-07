<template>
  <Dialog v-model="dialogVisible" title="批量补充 Cadence 数据" width="640">
    <div class="import-wizard">
      <div class="wizard-step mb-20px">
        <div class="step-header mb-10px flex items-center">
          <div class="step-badge mr-10px flex h-24px w-24px items-center justify-center rounded-full bg-primary text-sm font-bold text-white">1</div>
          <span class="text-16px font-bold text-slate-800">下载导入模板</span>
        </div>
        <div class="step-body pl-34px">
          <p class="mb-12px text-13px text-slate-500">
            请下载系统提供的英文表头模板，按规定格式填写完成后再上传。
          </p>
          <el-button plain type="primary" @click="downloadTemplate">
            <Icon class="mr-5px" icon="ep:download" />
            下载 Cadence 导入模板
          </el-button>
        </div>
      </div>

      <div class="wizard-step">
        <div class="step-header mb-10px flex items-center">
          <div class="step-badge mr-10px flex h-24px w-24px items-center justify-center rounded-full bg-primary text-sm font-bold text-white">2</div>
          <span class="text-16px font-bold text-slate-800">上传数据文件</span>
        </div>
        <div class="step-body pl-34px">
          <el-upload
            ref="uploadRef"
            v-model:file-list="fileList"
            v-loading="uploadLoading"
            :action="dummyAction"
            :auto-upload="false"
            :disabled="uploadLoading"
            :limit="1"
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
          </el-upload>
          <div class="mt-12px flex items-center gap-12px rounded-lg border border-amber-100 bg-amber-50/60 p-12px">
            <el-switch v-model="markAllAsPcb" />
            <span class="text-13px text-slate-600">将所有导入物料标记为 PCB 元器件（随审批一并生效，无需在模板中逐行填写 PCB_Component 列）</span>
          </div>
          <div class="mt-12px flex flex-wrap gap-12px">
            <el-button :disabled="!canSubmit" type="warning" plain @click="handlePrecheck">
              <Icon class="mr-5px" icon="ep:view" />
              预检查（不导入）
            </el-button>
            <el-button :disabled="!canSubmit" type="primary" @click="handleImport">
              <Icon class="mr-5px" icon="ep:upload" />
              开始导入
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="flex justify-end">
        <el-button :disabled="uploadLoading" @click="dialogVisible = false">关闭</el-button>
      </div>
    </template>
  </Dialog>

  <Dialog v-model="resultVisible" title="导入结果" width="680">
    <div class="mb-16px grid grid-cols-3 gap-12px">
      <div class="rounded-lg border border-slate-100 bg-slate-50 px-16px py-12px text-center">
        <div class="font-mono text-24px font-bold text-slate-700">{{ resultData.totalCount }}</div>
        <div class="mt-2px text-12px text-slate-500">总行数</div>
      </div>
      <div class="rounded-lg border border-emerald-100 bg-emerald-50 px-16px py-12px text-center">
        <div class="font-mono text-24px font-bold text-emerald-600">{{ resultData.successCount }}</div>
        <div class="mt-2px text-12px text-slate-500">成功行数</div>
      </div>
      <div class="rounded-lg border border-rose-100 bg-rose-50 px-16px py-12px text-center">
        <div class="font-mono text-24px font-bold text-rose-600">{{ resultData.failCount }}</div>
        <div class="mt-2px text-12px text-slate-500">失败行数</div>
      </div>
    </div>

    <el-alert
      v-if="resultData.ignoredColumns && resultData.ignoredColumns.length"
      class="mb-12px"
      type="info"
      :closable="false"
      show-icon
      title="以下列未被写入（仅作参考或未识别），已如实上报"
    >
      <template #default>
        <span class="font-mono text-12px">{{ resultData.ignoredColumns.join('、') }}</span>
      </template>
    </el-alert>

    <el-table
      v-if="resultData.failDetails && resultData.failDetails.length"
      :data="resultData.failDetails"
      :max-height="320"
      border
    >
      <el-table-column align="center" label="行号" width="80">
        <template #default="{ row }">
          <span class="font-mono">{{ row.rowNumber }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="物料编号" width="160">
        <template #default="{ row }">
          <span class="font-mono">{{ row.barCode || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="失败原因" min-width="240" show-overflow-tooltip>
        <template #default="{ row }">
          <span>{{ row.reason || '-' }}</span>
        </template>
      </el-table-column>
    </el-table>
    <template #footer>
      <el-button type="primary" @click="resultVisible = false">知道了</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import download from '@/utils/download'
import request from '@/config/axios'
import type { UploadInstance, UploadUserFile } from 'element-plus'
import { RdCadenceApi, RdCadenceImportResult } from '@/api/erp/product/rdCadence'

defineOptions({ name: 'RdCadenceImportForm' })

const message = useMessage()

const dialogVisible = ref(false)
const uploadLoading = ref(false)
const markAllAsPcb = ref(false)
const fileList = ref<UploadUserFile[]>([])
const uploadRef = ref<UploadInstance>()
const resultVisible = ref(false)
const resultData = ref<RdCadenceImportResult>({
  totalCount: 0,
  successCount: 0,
  failCount: 0,
  failDetails: [],
  successCategoryIds: [],
  ignoredColumns: []
})

// el-upload 仅作为选择器（auto-upload=false），实际上传走 axios，避免代理长响应被掐断
const dummyAction = '/erp/product/rd-cadence/import'

const hasSelectedFile = computed(() => fileList.value.length > 0)
const canSubmit = computed(() => hasSelectedFile.value && !uploadLoading.value)

const open = async () => {
  dialogVisible.value = true
  await resetForm()
}
defineExpose({ open })

const resetForm = async () => {
  uploadLoading.value = false
  fileList.value = []
  await nextTick()
  uploadRef.value?.clearFiles()
}

const downloadTemplate = async () => {
  const res = await RdCadenceApi.downloadTemplate()
  download.excel(res, 'Cadence导入模板.xlsx')
}

const getFile = (): File | undefined => {
  const raw = fileList.value[0]?.raw as File | undefined
  return raw
}

const handlePrecheck = async () => {
  const file = getFile()
  if (!file) {
    message.error('请先上传文件')
    return
  }
  uploadLoading.value = true
  try {
    const res = await RdCadenceApi.precheck(file, markAllAsPcb.value)
    showResult(res)
  } catch (e: any) {
    message.error(e?.msg || e?.message || '预检查失败')
  } finally {
    uploadLoading.value = false
  }
}

const handleImport = async () => {
  const file = getFile()
  if (!file) {
    message.error('请先上传文件')
    return
  }
  uploadLoading.value = true
  try {
    const res = await RdCadenceApi.importData(file, markAllAsPcb.value)
    showResult(res, true)
  } catch (e: any) {
    message.error(e?.msg || e?.message || '导入失败')
  } finally {
    uploadLoading.value = false
  }
}

const emit = defineEmits(['success'])

const showResult = (response: any, isCommit = false) => {
  if (response.code !== 0) {
    message.error(response.msg || '操作失败，请稍后重试')
    return
  }
  const data: RdCadenceImportResult = {
    totalCount: response.data?.totalCount ?? 0,
    successCount: response.data?.successCount ?? 0,
    failCount: response.data?.failCount ?? 0,
    failDetails: response.data?.failDetails ?? [],
    successCategoryIds: response.data?.successCategoryIds ?? [],
    ignoredColumns: response.data?.ignoredColumns ?? []
  }
  resultData.value = data
  if (data.successCount > 0) {
    emit('success', data)
  }
  if (data.failCount > 0 || data.ignoredColumns?.length) {
    resultVisible.value = true
    if (isCommit && data.successCount === 0) {
      message.warning('本次导入全部失败，请根据失败原因修正后重新上传')
    } else if (isCommit) {
      message.success(`已提交导入，成功 ${data.successCount} 条，进入物料修改审批`)
    }
  } else if (data.totalCount === 0) {
    message.warning('文件中没有可导入的数据行')
  } else if (!isCommit) {
    message.success(`预检查通过，共 ${data.successCount} 条可导入`)
  }
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
    border-color: #e2e8f0;
    background-color: #f8fafc;
    transition: all 0.2s ease;

    &:hover {
      border-color: var(--el-color-primary);
      background-color: #fff;
    }
  }
}
</style>
