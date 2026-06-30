<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="900px" scroll maxHeight="80vh" @closed="clearDialogState">
    <div class="finance-shell__context-card finance-shell__dialog-card">
      <div class="finance-shell__context-main">
        <div class="finance-shell__context-title">{{ dialogTitle }}</div>
      </div>
      <div class="finance-shell__page-chip">{{ formType === 'create' ? '新增' : '编辑' }}</div>
    </div>

    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="88px" v-loading="dialogLoading" :disabled="formDisabled" class="finance-report-item-form">
      <div class="finance-shell__dialog-grid">
        <el-form-item label="账簿" prop="ledgerId">
          <el-select v-model="formData.ledgerId" placeholder="请选择账簿" filterable class="!w-full">
            <el-option v-for="item in ledgerOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="报表类型" prop="reportType">
          <el-select v-model="formData.reportType" placeholder="请选择报表类型" class="!w-full">
            <el-option v-for="item in REPORT_TYPE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="项目分类" prop="itemCategory">
          <el-select v-model="formData.itemCategory" placeholder="请选择项目分类" class="!w-full">
            <el-option v-for="item in REPORT_ITEM_CATEGORY_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="项目编码" prop="itemCode">
          <el-input v-model="formData.itemCode" placeholder="请输入项目编码" />
        </el-form-item>
        <el-form-item label="项目名称" prop="itemName">
          <el-input v-model="formData.itemName" placeholder="请输入项目名称" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio v-for="item in COMMON_STATUS_OPTIONS" :key="item.value" :value="item.value">
              {{ item.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="formData.sort" :min="0" controls-position="right" class="!w-full" />
        </el-form-item>
      </div>

      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" type="textarea" :rows="2" placeholder="请输入备注" />
      </el-form-item>

      <div class="finance-shell__dialog-section">
        <div class="finance-shell__dialog-section-title">取数映射</div>
        <div class="finance-shell__mapping-list">
          <div v-for="(row, index) in formData.subjects" :key="index" class="finance-shell__mapping-row">
            <el-select v-model="row.subjectCode" placeholder="请选择科目" filterable class="!w-full">
              <el-option v-for="item in subjectOptions" :key="item.id" :label="`${item.subjectCode} ${item.subjectName}`" :value="item.subjectCode" />
            </el-select>
            <el-select v-model="row.amountRule" placeholder="取数规则" class="!w-full">
              <el-option v-for="item in AMOUNT_RULE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
            <el-select v-model="row.amountSign" placeholder="符号" class="!w-full">
              <el-option label="加" :value="1" />
              <el-option label="减" :value="-1" />
            </el-select>
            <el-button link type="danger" @click="removeMapping(index)">删除</el-button>
          </div>
        </div>
        <el-button plain @click="addMapping">
          <Icon icon="ep:plus" class="mr-5px" />
          添加映射
        </el-button>
      </div>
    </el-form>
    <template #footer>
      <el-button type="primary" :loading="submitLoading" :disabled="dialogLoading" @click="submitForm">确定</el-button>
      <el-button :disabled="submitLoading" @click="dialogVisible = false">取消</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import type { FormRules } from 'element-plus'
import { COMMON_STATUS_OPTIONS, REPORT_ITEM_CATEGORY_OPTIONS, REPORT_TYPE_OPTIONS } from '@/views/erp/finance/shared/accounting'
import { ErpFinanceLedgerVO, FinanceLedgerApi } from '@/api/erp/finance/ledger'
import { ErpFinanceReportItemSaveReqVO, FinanceReportItemApi } from '@/api/erp/finance/report-item'
import { ErpFinanceSubjectVO, FinanceSubjectApi } from '@/api/erp/finance/subject'
import { findDemoReportItem, getDemoSubjectOptions, reportItemDemoLedgers } from './demo'

defineOptions({ name: 'ReportItemForm' })

type FormType = 'create' | 'update'

const AMOUNT_RULE_OPTIONS = [
  { label: '期初借方', value: 10 },
  { label: '期初贷方', value: 20 },
  { label: '本期借方', value: 30 },
  { label: '本期贷方', value: 40 },
  { label: '期末借方', value: 50 },
  { label: '期末贷方', value: 60 }
]

const createEmptyFormData = (): ErpFinanceReportItemSaveReqVO => ({
  id: undefined,
  ledgerId: undefined,
  reportType: undefined,
  itemCategory: undefined,
  itemCode: undefined,
  itemName: undefined,
  status: 0,
  sort: 1,
  remark: '',
  subjects: []
})

const message = useMessage()
const { t } = useI18n()
const dialogVisible = ref(false)
const dialogTitle = ref('')
const dialogLoading = ref(false)
const submitLoading = ref(false)
const formType = ref<FormType>('create')
const formData = ref<ErpFinanceReportItemSaveReqVO>(createEmptyFormData())
const formRef = ref()
const ledgerOptions = ref<ErpFinanceLedgerVO[]>([])
const subjectOptions = ref<ErpFinanceSubjectVO[]>([])

const formDisabled = computed(() => dialogLoading.value || submitLoading.value)
const formRules: FormRules<ErpFinanceReportItemSaveReqVO> = {
  ledgerId: [{ required: true, message: '账簿不能为空', trigger: 'change' }],
  reportType: [{ required: true, message: '报表类型不能为空', trigger: 'change' }],
  itemCategory: [{ required: true, message: '项目分类不能为空', trigger: 'change' }],
  itemCode: [{ required: true, message: '项目编码不能为空', trigger: 'blur' }],
  itemName: [{ required: true, message: '项目名称不能为空', trigger: 'blur' }],
  status: [{ required: true, message: '状态不能为空', trigger: 'change' }]
}

const resetForm = async () => {
  formData.value = createEmptyFormData()
  await nextTick()
  formRef.value?.clearValidate()
}

const loadOptions = async (ledgerId?: number) => {
  try {
    const [ledgers, subjects] = await Promise.all([
      FinanceLedgerApi.getLedgerSimpleList(),
      ledgerId ? FinanceSubjectApi.getSubjectSimpleList({ ledgerId, status: 0 }) : Promise.resolve([])
    ])
    ledgerOptions.value = ledgers?.length ? ledgers : import.meta.env.DEV ? reportItemDemoLedgers : []
    subjectOptions.value = subjects?.length ? subjects : import.meta.env.DEV ? getDemoSubjectOptions(ledgerId) : []
  } catch {
    ledgerOptions.value = import.meta.env.DEV ? reportItemDemoLedgers : []
    subjectOptions.value = import.meta.env.DEV ? getDemoSubjectOptions(ledgerId) : []
  }
}

const normalizeSubjects = (subjects?: ErpFinanceReportItemSaveReqVO['subjects']) =>
  (subjects || []).filter(Boolean).map((item) => ({
    subjectCode: item?.subjectCode,
    amountRule: item?.amountRule,
    amountSign: item?.amountSign ?? 1
  }))

const titleMap: Record<FormType, string> = {
  create: '新增报表项目',
  update: '编辑报表项目'
}

const open = async (type: FormType, id?: number) => {
  dialogVisible.value = true
  formType.value = type
  dialogTitle.value = titleMap[type]
  submitLoading.value = false
  await resetForm()
  dialogLoading.value = true
  try {
    const demoDetail = import.meta.env.DEV && id ? findDemoReportItem(id) : undefined
    if (id) {
      const detail = demoDetail || (await FinanceReportItemApi.getReportItem(id))
      formData.value = {
        ...createEmptyFormData(),
        ...detail,
        subjects: normalizeSubjects(detail?.subjects)
      }
    }
    await loadOptions(formData.value.ledgerId)
    if (!formData.value.subjects?.length) {
      formData.value.subjects = []
    }
  } finally {
    dialogLoading.value = false
  }
}

defineExpose({ open })

const emit = defineEmits<{
  (e: 'success'): void
}>()

watch(
  () => formData.value.ledgerId,
  async (ledgerId) => {
    if (!dialogVisible.value || !ledgerId) {
      subjectOptions.value = []
      return
    }
    try {
      const subjects = await FinanceSubjectApi.getSubjectSimpleList({ ledgerId, status: 0 })
      subjectOptions.value = subjects?.length ? subjects : import.meta.env.DEV ? getDemoSubjectOptions(ledgerId) : []
    } catch {
      subjectOptions.value = import.meta.env.DEV ? getDemoSubjectOptions(ledgerId) : []
    }
  }
)

const addMapping = () => {
  formData.value.subjects = [...(formData.value.subjects || []), { subjectCode: undefined, amountRule: undefined, amountSign: 1 }]
}

const removeMapping = (index: number) => {
  formData.value.subjects = (formData.value.subjects || []).filter((_, currentIndex) => currentIndex !== index)
}

const submitForm = async () => {
  if (submitLoading.value) {
    return
  }
  await formRef.value?.validate()
  const subjects = (formData.value.subjects || []).filter((item) => item.subjectCode && item.amountRule && item.amountSign)
  if (!subjects.length) {
    message.error('请至少添加一条取数映射')
    return
  }
  submitLoading.value = true
  try {
    const payload = { ...formData.value, subjects } as ErpFinanceReportItemSaveReqVO
    if (formType.value === 'create') {
      await FinanceReportItemApi.createReportItem(payload)
      message.success(t('common.createSuccess'))
    } else {
      await FinanceReportItemApi.updateReportItem(payload)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    submitLoading.value = false
  }
}

const clearDialogState = () => {
  subjectOptions.value = []
  ledgerOptions.value = []
  submitLoading.value = false
  dialogLoading.value = false
}
</script>

<style scoped>
.finance-shell__dialog-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 12px;
}

.finance-shell__dialog-card {
  margin-bottom: 12px;
}

.finance-shell__dialog-section {
  margin-top: 12px;
}

.finance-shell__mapping-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 12px;
}

.finance-shell__mapping-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 150px 108px auto;
  gap: 6px;
  align-items: center;
}

@media (max-width: 768px) {
  .finance-shell__dialog-grid,
  .finance-shell__mapping-row {
    grid-template-columns: 1fr;
  }
}

.finance-report-item-form :deep(.el-form-item) {
  margin-bottom: 12px;
}

.finance-report-item-form :deep(.el-input__wrapper),
.finance-report-item-form :deep(.el-select__wrapper),
.finance-report-item-form :deep(.el-textarea__inner),
.finance-report-item-form :deep(.el-input-number) {
  border-radius: 10px;
}
</style>
