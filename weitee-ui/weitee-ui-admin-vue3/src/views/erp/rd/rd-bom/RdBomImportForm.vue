<template>
  <Dialog v-model="dialogVisible" title="研发 BOM 导入" width="720px" :close-on-click-modal="false">
    <div v-if="!importResult">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="成品" prop="productId">
          <el-select-v2
            v-model="formData.productId"
            filterable
            :loading="productLoading"
            :options="productSelectOptions"
            placeholder="自动识别（可手动选择覆盖）"
            class="w-full"
          />
        </el-form-item>
        <el-form-item label="BOM 编码" prop="bomCode">
          <el-input v-model="formData.bomCode" placeholder="自动识别（可手动输入覆盖）" />
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
            :class="precheckStatusClass"
            class="rounded-full px-10px py-2px text-12px"
          >
            {{ precheckConclusionText }}
          </span>
        </div>

        <template v-if="precheckBaselineMissing.length">
          <div class="mb-5px mt-8px flex items-center justify-between">
            <span class="text-13px font-medium text-amber-600">
              较上一版{{ precheckBaselineLabel }}减少 {{ precheckBaselineMissing.length }} 个物料，默认阻止导入
            </span>
            <el-button link type="primary" size="small" @click="copyBaselineCodes(precheckBaselineMissing)">
              <Icon class="mr-3px" icon="ep:copy-document" />
              复制编号
            </el-button>
          </div>
          <el-table :data="precheckBaselineMissing" :stripe="true" size="small" max-height="200" class="mb-8px">
            <el-table-column label="物料编号" min-width="150">
              <template #default="{ row }">
                <span class="font-mono">{{ row.materialCode }}</span>
              </template>
            </el-table-column>
            <el-table-column label="名称" min-width="140">
              <template #default="{ row }">{{ row.productName || '—' }}</template>
            </el-table-column>
          </el-table>
          <div
v-if="!missingMaterials.length && !unapprovedMaterials.length && !rowIssues.length"
               class="mb-10px flex items-center gap-8px">
            <el-checkbox v-model="acknowledgeBaselineMissing">
              已确认减少的物料属正常改版，放行导入
            </el-checkbox>
          </div>
        </template>

        <template v-if="duplicateMaterialCodes.length">
          <div class="mb-5px mt-8px text-13px font-medium text-amber-600">
            产品编码重复，请再次核对清单后进行提交
          </div>
          <el-table :data="duplicateMaterialCodes" :stripe="true" size="small" max-height="200" class="mb-10px">
            <el-table-column label="物料编号" min-width="150">
              <template #default="{ row }">
                <span class="font-mono">{{ row.materialCode }}</span>
              </template>
            </el-table-column>
            <el-table-column label="行号" min-width="110">
              <template #default="{ row }">
                <span class="font-mono text-12px text-slate-500">{{ (row.rowNumbers || []).join('、') }}</span>
              </template>
            </el-table-column>
          </el-table>
        </template>

        <template v-if="duplicateBom">
          <div class="mb-5px mt-8px text-13px font-medium text-rose-600">
            BOM 编码和版本已存在，请修改版本或检查已有草稿
          </div>
          <div class="mb-10px rounded border border-rose-100 bg-rose-50 px-10px py-8px text-13px text-rose-700">
            编码 {{ duplicateBom.bomCode || '—' }}，版本 {{ duplicateBom.version || '草稿' }}，已有 BOM {{ duplicateBom.id || '—' }}
          </div>
        </template>

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
            行问题（{{ rowIssues.length }}）
          </div>
          <el-table :data="rowIssues" :stripe="true" size="small" max-height="200">
            <el-table-column label="行号" prop="rowNumber" width="70" align="center" />
            <el-table-column label="物料编号" min-width="140">
              <template #default="{ row }">
                <span class="font-mono">{{ row.materialCode || '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="分类" width="140" align="center">
              <template #default="{ row }">
                <el-tag :type="issueTagType(row.issueType)" effect="light" size="small">
                  {{ issueTypeLabel(row.issueType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="原因" prop="reason" min-width="220" show-overflow-tooltip />
          </el-table>
        </template>

        <div
          v-if="precheckReady && !duplicateMaterialCodes.length"
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

      <template v-if="importBaselineMissing.length">
        <div class="mb-5px flex items-center justify-between">
          <span class="text-13px font-medium text-amber-600">
            较上一版{{ importResult.baselineDiff?.baselineVersion ? `（${importResult.baselineDiff.baselineVersion}）` : '' }}减少
            {{ importBaselineMissing.length }} 个物料，请确认是否漏行
          </span>
          <el-button link type="primary" size="small" @click="copyBaselineCodes(importBaselineMissing)">
            <Icon class="mr-3px" icon="ep:copy-document" />
            复制编号
          </el-button>
        </div>
        <el-table :data="importBaselineMissing" :stripe="true" size="small" max-height="200" class="mb-15px">
          <el-table-column label="物料编号" min-width="150">
            <template #default="{ row }">
              <span class="font-mono">{{ row.materialCode }}</span>
            </template>
          </el-table-column>
          <el-table-column label="名称" min-width="140">
            <template #default="{ row }">{{ row.productName || '—' }}</template>
          </el-table-column>
        </el-table>
      </template>

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
import { ProductApi, type ProductSimpleVO } from '@/api/erp/product/product'
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
const productList = ref<ProductSimpleVO[]>([])
/** 万级物料必须走虚拟滚动（el-select-v2），全量 el-option 会把主线程卡死 */
const productSelectOptions = computed(() =>
  productList.value.map((product) => ({ label: product.name, value: product.id }))
)
const importResult = ref<RdBomImportResultVO | null>(null)
const precheckResult = ref<RdBomPrecheckResultVO | null>(null)

const formData = reactive({
  productId: undefined as number | undefined,
  bomCode: '',
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
  SPEC_MISMATCH: '规格不符',
  CADENCE_DATA_INCOMPLETE: 'Cadence 数据不完整',
  FORMAT_ERROR: '格式错误',
  FILE_EMPTY: '无明细行'
}
const issueTypeLabel = (type?: string) => (type && ISSUE_TYPE_LABELS[type]) || `类型${type}`
const issueTagType = (type?: string) => {
  if (
    type === 'MISSING_MATERIAL' ||
    type === 'MATERIAL_DISABLED' ||
    type === 'SPEC_MISMATCH' ||
    type === 'CADENCE_DATA_INCOMPLETE' ||
    type === 'FILE_EMPTY'
  )
    return 'danger'
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
  if (formData.remark) params.set('remark', formData.remark)
  if (acknowledgeBaselineMissing.value) params.set('allowBaselineMissing', 'true')
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
const duplicateMaterialCodes = computed(() => precheckResult.value?.duplicateMaterialCodes ?? [])
const duplicateBom = computed(() => precheckResult.value?.duplicateBom)
const precheckReady = computed(() => precheckResult.value?.readyToImport === true)
const duplicateNoticeKey = ref('')

/** 预检结论唯一来源：绿/红胶囊与结论文案同源，杜绝互相矛盾的提示同屏 */
const precheckConclusionText = computed(() => {
  if (duplicateBom.value) return 'BOM 编码和版本已存在，导入已阻止'
  if (precheckReady.value && duplicateMaterialCodes.value.length) return '存在重复产品编码，请核对后提交'
  if (precheckReady.value) return '预检通过，可直接导入'
  if (precheckBaselineMissing.value.length) return '较上一版减少物料，需确认后放行'
  if (missingMaterials.value.length) return '存在待建档物料，导入已阻止'
  if (unapprovedMaterials.value.length) return '存在待审核或停用物料，导入已阻止'
  if (rowIssues.value.length) return '存在行问题，请查看下方明细'
  return '预检未通过，请查看下方明细'
})
const precheckStatusClass = computed(() => {
  if (precheckReady.value && duplicateMaterialCodes.value.length) return 'bg-amber-50 text-amber-600'
  return precheckReady.value ? 'bg-emerald-50 text-emerald-600' : 'bg-rose-50 text-rose-600'
})

const precheckBaselineMissing = computed(() => precheckResult.value?.baselineDiff?.missingItems ?? [])
const precheckBaselineLabel = computed(() =>
  precheckResult.value?.baselineDiff?.baselineVersion
    ? `（${precheckResult.value.baselineDiff.baselineVersion}）`
    : ''
)
const importBaselineMissing = computed(() => importResult.value?.baselineDiff?.missingItems ?? [])
/** 缺料差异需人工确认放行；勾选后导入请求携带 allowBaselineMissing=true */
const acknowledgeBaselineMissing = ref(false)
/** 未跑预检查时保持原有可直接导入行为；跑过且存在阻断项时禁止导入 */
const importBlocked = computed(() => precheckResult.value !== null && !precheckReady.value)

const invalidatePrecheck = () => {
  precheckResult.value = null
  acknowledgeBaselineMissing.value = false
  duplicateNoticeKey.value = ''
}

watch(
  () => [formData.productId, formData.bomCode, formData.remark],
  () => {
    invalidatePrecheck()
    // 参数变化后已有文件则自动重跑，保证结论与当前参数一致
    scheduleAutoPrecheck()
  }
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
    const result = await RdBomApi.precheckRdBomImport(
      {
        productId: formData.productId,
        bomCode: formData.bomCode || undefined,
        remark: formData.remark || undefined
      },
      rawFile
    )
    precheckResult.value = result
    const duplicates = result?.duplicateMaterialCodes ?? []
    const noticeKey = duplicates
      .map((item) => `${item.materialCode ?? ''}:${(item.rowNumbers ?? []).join(',')}`)
      .join('|')
    if (noticeKey && noticeKey !== duplicateNoticeKey.value) {
      duplicateNoticeKey.value = noticeKey
      message.warning('产品编码重复，请再次核对清单后进行提交')
    }
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

const copyBaselineCodes = async (items: { materialCode?: string }[]) => {
  const codes = items.map((item) => item.materialCode).filter((code): code is string => !!code)
  if (!codes.length) return
  try {
    await navigator.clipboard.writeText(codes.join(','))
    message.success('已复制缺失物料编号')
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

const onFileChange: UploadProps['onChange'] = (file) => {
  invalidatePrecheck()
  // 上传即自动预检：选择/更换文件后无需手动点按钮，绿灯亮起才放行导入
  if (file?.raw) {
    scheduleAutoPrecheck()
  }
}

/** 防抖自动预检：连续换文件只跑最后一次，避免无谓的全量解析 */
let autoPrecheckTimer: ReturnType<typeof setTimeout> | undefined
const scheduleAutoPrecheck = () => {
  if (autoPrecheckTimer) clearTimeout(autoPrecheckTimer)
  autoPrecheckTimer = setTimeout(() => {
    if (!importResult.value && fileList.value.length) {
      runPrecheck()
    }
  }, 500)
}

const downloadTemplate = async () => {
  const res = await RdBomApi.downloadImportTemplate()
  download.excel(res, '研发BOM明细导入模板.xlsx')
}

const successEmitted = ref(false)

const finish = () => {
  dialogVisible.value = false
  emit('success')
  successEmitted.value = true
}

const closeDialog = () => {
  dialogVisible.value = false
  // 导入已产生 BOM 但用户直接点"关闭"时，补发 success 让父列表刷新，避免新建 BOM"消失"
  if (importResult.value?.bomId && !successEmitted.value) {
    emit('success')
    successEmitted.value = true
  }
}

const resetForm = async () => {
  uploadLoading.value = false
  checking.value = false
  importResult.value = null
  invalidatePrecheck()
  fileList.value = []
  formData.productId = undefined
  formData.bomCode = ''
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
