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
        :on-change="onFileChange"
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

      <!-- 预检查结果 -->
      <div
        v-if="precheckResult"
        v-loading="checking"
        class="mt-15px rounded-lg border border-slate-100 bg-slate-50 p-12px"
      >
        <div class="mb-10px flex flex-wrap items-center justify-between gap-8px">
          <div class="flex flex-wrap gap-16px text-13px font-mono text-slate-600">
            <span>总行数 {{ precheckResult.totalCount || 0 }}</span>
            <span class="text-emerald-600">可导入 {{ precheckResult.readyCount || 0 }}</span>
            <span class="text-rose-600">被阻断 {{ precheckResult.blockedCount || 0 }}</span>
            <span>格式问题 {{ precheckResult.issueCount || 0 }}</span>
          </div>
          <span
            :class="precheckReady ? 'bg-emerald-50 text-emerald-600' : 'bg-rose-50 text-rose-600'"
            class="rounded-full px-10px py-2px text-12px"
          >
            {{ precheckReady ? '预检通过，可直接导入' : '存在待建档或待审核物料，导入已阻止' }}
          </span>
        </div>

        <template v-if="missingMaterials.length">
          <div class="mb-5px mt-8px flex items-center justify-between">
            <span class="text-13px font-medium text-rose-600">
              待建档物料（{{ missingMaterials.length }}）
            </span>
            <el-button link type="primary" size="small" @click="copyMissingCodes">
              <Icon class="mr-3px" icon="ep:copy-document" />
              复制编号
            </el-button>
          </div>
          <el-table :data="missingMaterials" :stripe="true" size="small" max-height="200" class="mb-10px">
            <el-table-column label="物料编号" min-width="150">
              <template #default="{ row }">
                <span class="font-mono">{{ row.materialCode }}</span>
                <el-tag v-if="row.topLevel" type="danger" effect="light" size="small" class="ml-6px">顶层</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="名称" min-width="140">
              <template #default="{ row }">{{ row.materialName || '—' }}</template>
            </el-table-column>
            <el-table-column label="行号" min-width="110">
              <template #default="{ row }">
                <span class="font-mono text-12px text-slate-500">{{ (row.rowNumbers || []).join('、') }}</span>
              </template>
            </el-table-column>
          </el-table>
        </template>

        <template v-if="unapprovedMaterials.length">
          <div class="mb-5px mt-8px text-13px font-medium text-amber-600">
            待审核物料（{{ unapprovedMaterials.length }}）
          </div>
          <el-table :data="unapprovedMaterials" :stripe="true" size="small" max-height="200" class="mb-10px">
            <el-table-column label="物料编号" min-width="150">
              <template #default="{ row }">
                <span class="font-mono">{{ row.materialCode }}</span>
              </template>
            </el-table-column>
            <el-table-column label="名称" min-width="130">
              <template #default="{ row }">{{ row.productName || '—' }}</template>
            </el-table-column>
            <el-table-column label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.disabled ? 'danger' : 'warning'" effect="light" size="small">
                  {{ row.disabled ? '已停用' : auditStatusLabel(row.auditStatus) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="行号" min-width="110">
              <template #default="{ row }">
                <span class="font-mono text-12px text-slate-500">{{ (row.rowNumbers || []).join('、') || '—' }}</span>
              </template>
            </el-table-column>
          </el-table>
        </template>

        <template v-if="rowIssues.length">
          <div class="mb-5px mt-8px text-13px font-medium text-slate-600">
            格式问题（{{ rowIssues.length }}）
          </div>
          <el-table :data="rowIssues" :stripe="true" size="small" max-height="200">
            <el-table-column label="行号" prop="rowNumber" width="70" align="center" />
            <el-table-column label="物料编号" min-width="140">
              <template #default="{ row }">
                <span class="font-mono">{{ row.materialCode || '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="原因" prop="reason" min-width="220" show-overflow-tooltip />
          </el-table>
        </template>

        <div
          v-if="!missingMaterials.length && !unapprovedMaterials.length && !rowIssues.length"
          class="mt-5px text-13px text-emerald-600"
        >
          预检通过，未发现问题
        </div>
      </div>
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
          <el-table-column label="物料编号" min-width="140">
            <template #default="{ row }">
              <span class="font-mono">{{ row.materialCode }}</span>
            </template>
          </el-table-column>
          <el-table-column label="原因" min-width="180" show-overflow-tooltip>
            <template #default="{ row }">{{ row.reason }}</template>
          </el-table-column>
          <el-table-column label="分类" width="110" align="center">
            <template #default="{ row }">
              <el-tag :type="issueTagType(row.issueType)" effect="light" size="small">
                {{ issueTypeLabel(row.issueType) }}
              </el-tag>
            </template>
          </el-table-column>
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
        <template v-if="!importResult">
          <el-button plain :loading="checking" :disabled="!canSubmit" @click="runPrecheck">
            <Icon v-if="!checking" class="mr-5px" icon="ep:search" />
            预检查
          </el-button>
          <el-button
            type="primary"
            :disabled="!canSubmit || importBlocked"
            @click="submitFileForm"
          >
            <Icon class="mr-5px" icon="ep:upload" />
            开始导入
          </el-button>
        </template>
        <el-button v-else type="primary" @click="finish">完成</el-button>
        <el-button :disabled="uploadLoading || checking" @click="closeDialog">关闭</el-button>
      </div>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import { getAccessToken, getTenantId } from '@/utils/auth'
import download from '@/utils/download'
import { ProductApi, type ProductVO } from '@/api/erp/product/product'
import {
  RdBomApi,
  type RdBomImportResultVO,
  type RdBomPrecheckResultVO
} from '@/api/erp/rd/bom'
import { genFileId } from 'element-plus'
import type { UploadInstance, UploadProps, UploadUserFile } from 'element-plus'
import { useMessage } from '@/hooks/web/useMessage'

defineOptions({ name: 'RdBomImportForm' })

const message = useMessage()
const emit = defineEmits(['success'])

const dialogVisible = ref(false)
const uploadLoading = ref(false)
const checking = ref(false)

const fileList = ref<UploadUserFile[]>([])
const uploadHeaders = ref<Record<string, string>>({})
const uploadRef = ref<UploadInstance>()

const productLoading = ref(false)
const productList = ref<ProductVO[]>([])
const importResult = ref<RdBomImportResultVO | null>(null)
const precheckResult = ref<RdBomPrecheckResultVO | null>(null)

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
  DESIGNATOR_COUNT_MISMATCH: '位号数不符',
  MISSING_MATERIAL: '未建档',
  MATERIAL_NOT_APPROVED: '未审核',
  MATERIAL_DISABLED: '已停用',
  FORMAT_ERROR: '格式错误'
}
const issueTypeLabel = (type?: string) => (type && ISSUE_TYPE_LABELS[type]) || `类型${type}`
const issueTagType = (type?: string) => {
  if (type === 'MISSING_MATERIAL' || type === 'MATERIAL_DISABLED') return 'danger'
  if (type === 'MATERIAL_NOT_APPROVED') return 'warning'
  return 'info'
}

const AUDIT_STATUS_LABELS: Record<number, string> = {
  0: '草稿',
  10: '未审核',
  20: '已审核',
  30: '已驳回',
  40: '已结转',
  50: '已作废',
  60: '处理失败'
}
const auditStatusLabel = (status?: number) =>
  (status != null && AUDIT_STATUS_LABELS[status]) || `状态${status ?? ''}`

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
const canSubmit = computed(() => hasSelectedFile.value && !uploadLoading.value && !checking.value)

const missingMaterials = computed(() => precheckResult.value?.missingMaterials ?? [])
const unapprovedMaterials = computed(() => precheckResult.value?.unapprovedMaterials ?? [])
const rowIssues = computed(() => precheckResult.value?.rowIssues ?? [])
const precheckReady = computed(() => precheckResult.value?.readyToImport === true)
/** 未跑预检查时保持原有可直接导入行为；跑过且存在阻断项时禁止导入 */
const importBlocked = computed(() => precheckResult.value !== null && !precheckReady.value)

const invalidatePrecheck = () => {
  precheckResult.value = null
}

watch(
  () => [formData.productId, formData.bomCode, formData.version, formData.remark],
  invalidatePrecheck
)

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

const runPrecheck = async () => {
  const rawFile = fileList.value[0]?.raw as File | undefined
  if (!rawFile) {
    message.error('请上传文件')
    return
  }
  checking.value = true
  try {
    precheckResult.value = await RdBomApi.precheckRdBomImport(
      {
        productId: formData.productId,
        bomCode: formData.bomCode || undefined,
        version: formData.version || undefined,
        remark: formData.remark || undefined
      },
      rawFile
    )
  } catch (e: any) {
    invalidatePrecheck()
    message.error(e?.msg || '预检查失败，请稍后重试')
  } finally {
    checking.value = false
  }
}

const copyMissingCodes = async () => {
  const codes = missingMaterials.value
    .map((item) => item.materialCode)
    .filter((code): code is string => !!code)
  if (!codes.length) return
  try {
    await navigator.clipboard.writeText(codes.join(','))
    message.success('已复制待建档物料编号')
  } catch {
    message.error('复制失败，请手动复制')
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
  invalidatePrecheck()
  const file = files[0] as UploadUserFile
  file.uid = genFileId()
  uploadRef.value?.handleStart(file as any)
}

const onFileChange: UploadProps['onChange'] = () => {
  invalidatePrecheck()
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
  checking.value = false
  importResult.value = null
  invalidatePrecheck()
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
