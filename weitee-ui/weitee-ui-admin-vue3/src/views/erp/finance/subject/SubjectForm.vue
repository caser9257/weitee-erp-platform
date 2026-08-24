<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="720px" scroll maxHeight="78vh">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="96px"
      v-loading="dialogLoading"
      :disabled="formDisabled"
    >
      <div class="finance-subject-form__grid">
        <el-form-item label="账簿" prop="ledgerId">
          <el-select v-model="formData.ledgerId" placeholder="请选择账簿" filterable class="!w-full">
            <el-option v-for="item in ledgerOptions" :key="item.id" :label="displayLedgerName(item.name)" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="上级科目" prop="parentId">
          <el-select v-model="formData.parentId" placeholder="请选择上级科目" clearable filterable class="!w-full">
            <el-option v-for="item in parentOptions" :key="item.id" :label="item.subjectName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="科目编码" prop="subjectCode">
          <el-input v-model="formData.subjectCode" placeholder="请输入科目编码" />
        </el-form-item>
        <el-form-item label="科目名称" prop="subjectName">
          <el-input v-model="formData.subjectName" placeholder="请输入科目名称" />
        </el-form-item>
        <el-form-item label="科目类型" prop="subjectType">
          <el-select v-model="formData.subjectType" placeholder="请选择科目类型" class="!w-full">
            <el-option v-for="item in SUBJECT_TYPE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="余额方向" prop="balanceDirection">
          <el-select v-model="formData.balanceDirection" placeholder="请选择余额方向" class="!w-full">
            <el-option v-for="item in DIRECTION_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="启用状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio v-for="item in COMMON_STATUS_OPTIONS" :key="item.value" :value="item.value">
              {{ item.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="末级科目" prop="leaf">
          <el-switch v-model="formData.leaf" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="formData.sort" :min="0" controls-position="right" class="!w-full" />
        </el-form-item>
      </div>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button type="primary" :loading="submitLoading" :disabled="dialogLoading" @click="submitForm">
        确定
      </el-button>
      <el-button :disabled="submitLoading" @click="dialogVisible = false">取消</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import type { FormRules } from 'element-plus'
import {
  COMMON_STATUS_OPTIONS,
  SUBJECT_TYPE_OPTIONS
} from '@/views/erp/finance/shared/accounting'
import { ErpFinanceLedgerVO, FinanceLedgerApi } from '@/api/erp/finance/ledger'
import { displayLedgerName } from '@/utils/financeDisplay'
import {
  ErpFinanceSubjectSaveReqVO,
  ErpFinanceSubjectVO,
  FinanceSubjectApi
} from '@/api/erp/finance/subject'

defineOptions({ name: 'SubjectForm' })

type FormType = 'create' | 'update'

const DIRECTION_OPTIONS = [
  { label: '借方', value: 10 },
  { label: '贷方', value: 20 }
]

const createEmptyFormData = (): ErpFinanceSubjectSaveReqVO => ({
  id: undefined,
  ledgerId: undefined,
  parentId: undefined,
  subjectCode: undefined,
  subjectName: undefined,
  subjectType: undefined,
  balanceDirection: undefined,
  leaf: false,
  status: 0,
  sort: 1,
  remark: ''
})

const message = useMessage()
const { t } = useI18n()
const dialogVisible = ref(false)
const dialogTitle = ref('')
const dialogLoading = ref(false)
const submitLoading = ref(false)
const formType = ref<FormType>('create')
const formData = ref<ErpFinanceSubjectSaveReqVO>(createEmptyFormData())
const formRef = ref()
const ledgerOptions = ref<ErpFinanceLedgerVO[]>([])
const parentOptions = ref<ErpFinanceSubjectVO[]>([])

const formDisabled = computed(() => dialogLoading.value || submitLoading.value)
const formRules: FormRules<ErpFinanceSubjectSaveReqVO> = {
  ledgerId: [{ required: true, message: '账簿不能为空', trigger: 'change' }],
  subjectCode: [{ required: true, message: '科目编码不能为空', trigger: 'blur' }],
  subjectName: [{ required: true, message: '科目名称不能为空', trigger: 'blur' }],
  subjectType: [{ required: true, message: '科目类型不能为空', trigger: 'change' }],
  balanceDirection: [{ required: true, message: '余额方向不能为空', trigger: 'change' }],
  status: [{ required: true, message: '状态不能为空', trigger: 'change' }],
  sort: [{ required: true, message: '排序不能为空', trigger: 'blur' }]
}

const resetForm = async () => {
  formData.value = createEmptyFormData()
  await nextTick()
  formRef.value?.clearValidate()
}

const loadOptions = async (ledgerId?: number) => {
  const [ledgers, parents] = await Promise.all([
    FinanceLedgerApi.getLedgerSimpleList(),
    ledgerId ? FinanceSubjectApi.getSubjectSimpleList({ ledgerId, status: 0 }) : Promise.resolve([])
  ])
  ledgerOptions.value = ledgers
  parentOptions.value = parents
}

const titleMap: Record<FormType, string> = {
  create: '新增科目',
  update: '编辑科目'
}

const open = async (type: FormType, id?: number) => {
  dialogVisible.value = true
  formType.value = type
  dialogTitle.value = titleMap[type]
  submitLoading.value = false
  await resetForm()
  dialogLoading.value = true
  try {
    if (id) {
      const detail = await FinanceSubjectApi.getSubject(id)
      formData.value = { ...createEmptyFormData(), ...detail }
    }
    await loadOptions(formData.value.ledgerId)
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
      parentOptions.value = []
      return
    }
    parentOptions.value = await FinanceSubjectApi.getSubjectSimpleList({ ledgerId, status: 0 })
  }
)

const submitForm = async () => {
  if (submitLoading.value) {
    return
  }
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    const payload = { ...formData.value } as ErpFinanceSubjectSaveReqVO
    if (formType.value === 'create') {
      await FinanceSubjectApi.createSubject(payload)
      message.success(t('common.createSuccess'))
    } else {
      await FinanceSubjectApi.updateSubject(payload)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    submitLoading.value = false
  }
}
</script>

<style scoped>
.finance-subject-form__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

@media (max-width: 768px) {
  .finance-subject-form__grid {
    grid-template-columns: 1fr;
  }
}
</style>
