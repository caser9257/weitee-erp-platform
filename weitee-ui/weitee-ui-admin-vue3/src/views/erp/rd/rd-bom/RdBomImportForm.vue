<template>
  <Dialog v-model="dialogVisible" title="研发 BOM 导入" width="720px" :close-on-click-modal="false">
    <div v-if="!importResult">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="成品" prop="productId">
          <el-select
            v-model="formData.productId"
            filterable
            :loading="productLoading"
            placeholder="自动识别（可手动选择覆盖）"
            class="w-full"
          >
            <el-option v-for="item in productList" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="BOM 编码" prop="bomCode">
          <el-input v-model="formData.bomCode" placeholder="自动识别（可手动输入覆盖）" />
        </el-form-item>
        <el-form-item label="版本">
          <el-input v-model="formData.version" placeholder="自动识别（可手动输入，如 V1.0）" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="2" placeholder="可选" />
        </el-form-item>
      </el-form>

      <div class="mb-15px">
        <el-button plain type="primary" @click="downloadTemplate">
          <Icon class="mr-5px" icon="ep:download" />
          下载导入模板
        </el-button>
        <span class="ml-10px text-12px text-slate-400">
          智能识别：上传即自动解析表头与层级明细，无需手填
        </span>
      </div>

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
        drag
      >
        <Icon :size="48" class="text-slate-300" icon="ep:upload-filled" />
        <div class="el-upload__text text-14px">
          将填写完成的文件拖到此处，或<em class="font-bold text-primary">点击上传</em>
        </div>
        <div class="mt-5px text-12px text-slate-400">仅支持 .xls / .xlsx 格式</div>
      </el-upload>
    </div>

    <div v-else class="import-result">
      <el-alert
        :title="importResult.bomId ? `已创建研发 BOM（编号 ${importResult.bomId}）` : '未创建研发 BOM'"
        :type="importResult.bomId ? 'success' : 'warning'"
        :closable="false"
        class="mb-15px"
      />
      <div class="mb-15px flex gap-20px text-13px text-slate-600">
        <span>总行数：{{ importResult.totalCount || 0 }}</span>
        <span class="text-emerald-600">成功：{{ importResult.successCount || 0 }}</span>
        <span class="text-rose-600">失败：{{ importResult.failCount || 0 }}</span>
      </div>

      <template v-if="importResult.failDetails && importResult.failDetails.length">
        <div class="mb-8px text-13px font-medium text-rose-600">解析失败明细</div>
        <el-table :data="importResult.failDetails" :stripe="true" max-height="220" class="mb-15px">
          <el-table-column label="行号" prop="rowNumber" width="80" align="center" />
          <el-table-column label="物料编号" prop="materialCode" min-width="140" />
          <el-table-column label="失败原因" prop="reason" min-width="240" />
        </el-table>
      </template>

      <template v-if="importResult.validationIssues && importResult.validationIssues.length">
        <div class="mb-8px text-13px font-medium text-amber-600">完整性校验问题</div>
        <el-table :data="importResult.validationIssues" :stripe="true" max-height="240">
          <el-table-column label="行号" prop="rowIndex" width="80" align="center" />
          <el-table-column label="物料" min-width="180">
            <template #default="{ row }">
              <span>{{ row.materialName || '—' }}</span>
              <span v-if="row.materialId" class="ml-8px font-mono text-gray-400">{{ row.materialId }}</span>
            </template>
          </el-table-column>
          <el-table-column label="严重程度" width="110" align="center">
            <template #default="{ row }">
              <el-tag :type="row.severity === 'ERROR' ? 'danger' : 'warning'" effect="light">
                {{ row.severity === 'ERROR' ? '错误' : '警告' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="问题类型" width="160">
            <template #default="{ row }">{{ issueTypeLabel(row.issueType) }}</template>
          </el-table-column>
          <el-table-column label="描述" prop="message" min-width="240" />
        </el-table>
      </template>

      <div v-if="!importResult.validationIssues || !importResult.validationIssues.length" class="text-13px text-emerald-600">
        完整性校验通过，未发现问题
      </div>
    </div>

    <template #footer>
      <div class="flex flex-wrap justify-end gap-12px">
        <el-button v-if="!importResult" type="primary" :disabled="!canSubmit" @click="submitFileForm">
          <Icon class="mr-5px" icon="ep:upload" />
          开始导入
        </el-button>
        <el-button v-else type="primary" @click="finish">完成</el-button>
        <el-button :disabled="uploadLoading" @click="closeDialog">关闭</el-button>
      </div>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import { getAccessToken, getTenantId } from '@/utils/auth'
import download from '@/utils/download'
import { ProductApi, type ProductVO } from '@/api/erp/product/product'
import { RdBomApi, type RdBomImportResultVO } from '@/api/erp/rd/bom'
import { genFileId } from 'element-plus'
import type { UploadInstance, UploadProps, UploadUserFile } from 'element-plus'
import { useMessage } from '@/hooks/web/useMessage'

defineOptions({ name: 'RdBomImportForm' })

const message = useMessage()
const emit = defineEmits(['success'])

const dialogVisible = ref(false)
const uploadLoading = ref(false)
const fileList = ref<UploadUserFile[]>([])
const uploadHeaders = ref<Record<string, string>>({})
const uploadRef = ref<UploadInstance>()

const productLoading = ref(false)
const productList = ref<ProductVO[]>([])
const importResult = ref<RdBomImportResultVO | null>(null)

const formData = reactive({
  productId: undefined as number | undefined,
  bomCode: '',
  version: '',
  remark: ''
})

const ISSUE_TYPE_LABELS: Record<string, string> = {
  FLOATING_MATERIAL: '悬浮件',
  USAGE_INVALID: '用量无效',
  DESIGNATOR_ON_ASSEMBLY: '装配体位号',
  FLOATING_ASSEMBLY: '悬空装配体',
  MISSING_DESIGNATOR: '缺少位号',
  DESIGNATOR_COUNT_MISMATCH: '位号数不符'
}
const issueTypeLabel = (type?: string) => (type && ISSUE_TYPE_LABELS[type]) || `类型${type}`

const formRules = {
  productId: [{ required: false }],
  bomCode: [{ required: false }]
}
const formRef = ref()

const importUrl = computed(() => {
  const params = new URLSearchParams()
  if (formData.productId != null) params.set('productId', String(formData.productId))
  if (formData.bomCode) params.set('bomCode', formData.bomCode)
  if (formData.version) params.set('version', formData.version)
  if (formData.remark) params.set('remark', formData.remark)
  return (
    import.meta.env.VITE_BASE_URL +
    import.meta.env.VITE_API_URL +
    '/erp/rd-bom/import?' +
    params.toString()
  )
})

const hasSelectedFile = computed(() => fileList.value.length > 0)
const canSubmit = computed(() => hasSelectedFile.value && !uploadLoading.value)

const open = async () => {
  dialogVisible.value = true
  await resetForm()
  await loadProductList()
}
defineExpose({ open })

const loadProductList = async () => {
  productLoading.value = true
  try {
    productList.value = await ProductApi.getApprovedProductSimpleList()
  } finally {
    productLoading.value = false
  }
}

const submitFileForm = async () => {
  if (!hasSelectedFile.value) {
    message.error('请上传文件')
    return
  }
  uploadHeaders.value = buildUploadHeaders()
  uploadLoading.value = true
  uploadRef.value?.submit()
}

const submitFormSuccess: UploadProps['onSuccess'] = (response: any) => {
  uploadLoading.value = false
  if (response.code !== 0) {
    message.error(response.msg || '导入失败，请稍后重试')
    return
  }
  importResult.value = response.data as RdBomImportResultVO
  if (importResult.value?.bomId) {
    message.success('导入成功，已创建研发 BOM 草稿')
  } else {
    message.warning('导入完成，但没有可创建的有效明细行')
  }
}

const submitFormError: UploadProps['onError'] = () => {
  message.error('上传失败，请您重新上传！')
  uploadLoading.value = false
}

const handleExceed: UploadProps['onExceed'] = (files) => {
  uploadRef.value?.clearFiles()
  const file = files[0] as UploadUserFile
  file.uid = genFileId()
  uploadRef.value?.handleStart(file as any)
}

const downloadTemplate = async () => {
  const res = await RdBomApi.downloadImportTemplate()
  download.excel(res, '研发BOM明细导入模板.xlsx')
}

const finish = () => {
  dialogVisible.value = false
  emit('success')
}

const closeDialog = () => {
  dialogVisible.value = false
}

const resetForm = async () => {
  uploadLoading.value = false
  importResult.value = null
  fileList.value = []
  formData.productId = undefined
  formData.bomCode = ''
  formData.version = ''
  formData.remark = ''
  uploadHeaders.value = buildUploadHeaders()
  await nextTick()
  uploadRef.value?.clearFiles()
}

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
</script>

<style scoped lang="scss">
:deep(.el-upload),
:deep(.el-upload-dragger) {
  width: 100%;
}
</style>
