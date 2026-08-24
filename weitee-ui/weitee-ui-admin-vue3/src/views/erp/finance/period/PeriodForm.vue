<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="640px" scroll maxHeight="78vh">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="96px"
      v-loading="dialogLoading"
      :disabled="formDisabled"
    >
      <el-form-item label="账簿" prop="ledgerId">
        <el-select v-model="formData.ledgerId" placeholder="请选择账簿" filterable class="!w-full">
          <el-option v-for="item in ledgerOptions" :key="item.id" :label="displayLedgerName(item.name)" :value="item.id" />
        </el-select>
      </el-form-item>
      <div class="finance-period-form__grid">
        <el-form-item label="会计年度" prop="periodYear">
          <el-input-number v-model="formData.periodYear" :min="2000" :max="2099" controls-position="right" class="!w-full" />
        </el-form-item>
        <el-form-item label="会计月份" prop="periodMonth">
          <el-input-number v-model="formData.periodMonth" :min="1" :max="12" controls-position="right" class="!w-full" />
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
import { ErpFinanceLedgerVO, FinanceLedgerApi } from '@/api/erp/finance/ledger'
import { ErpFinancePeriodSaveReqVO, FinancePeriodApi } from '@/api/erp/finance/period'
import { displayLedgerName } from '@/utils/financeDisplay'

defineOptions({ name: 'PeriodForm' })

type FormType = 'create'

const createEmptyFormData = (): ErpFinancePeriodSaveReqVO => ({
  id: undefined,
  ledgerId: undefined,
  periodYear: undefined,
  periodMonth: undefined,
  remark: ''
})

const message = useMessage()
const { t } = useI18n()
const dialogVisible = ref(false)
const dialogTitle = ref('新增期间')
const dialogLoading = ref(false)
const submitLoading = ref(false)
const formData = ref<ErpFinancePeriodSaveReqVO>(createEmptyFormData())
const formRef = ref()
const ledgerOptions = ref<ErpFinanceLedgerVO[]>([])

const formDisabled = computed(() => dialogLoading.value || submitLoading.value)
const formRules: FormRules<ErpFinancePeriodSaveReqVO> = {
  ledgerId: [{ required: true, message: '账簿不能为空', trigger: 'change' }],
  periodYear: [{ required: true, message: '会计年度不能为空', trigger: 'change' }],
  periodMonth: [{ required: true, message: '会计月份不能为空', trigger: 'change' }]
}

const resetForm = async () => {
  formData.value = createEmptyFormData()
  await nextTick()
  formRef.value?.clearValidate()
}

const open = async (_type?: 'create', _id?: number) => {
  dialogVisible.value = true
  submitLoading.value = false
  await resetForm()
  dialogLoading.value = true
  try {
    ledgerOptions.value = await FinanceLedgerApi.getLedgerSimpleList()
  } finally {
    dialogLoading.value = false
  }
}

defineExpose({ open })

const emit = defineEmits<{
  (e: 'success'): void
}>()

const submitForm = async () => {
  if (submitLoading.value) {
    return
  }
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    await FinancePeriodApi.createPeriod({ ...formData.value })
    message.success(t('common.createSuccess'))
    dialogVisible.value = false
    emit('success')
  } finally {
    submitLoading.value = false
  }
}
</script>

<style scoped>
.finance-period-form__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

@media (max-width: 768px) {
  .finance-period-form__grid {
    grid-template-columns: 1fr;
  }
}
</style>
