<template>
  <Dialog v-model="dialogVisible" title="初始化模板" width="560px" scroll maxHeight="72vh" @closed="clearDialogState">
    <div class="finance-shell__context-card finance-shell__dialog-card">
      <div class="finance-shell__context-main">
        <div class="finance-shell__context-title">初始化模板</div>
      </div>
      <div class="finance-shell__page-chip">初始化</div>
    </div>

    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="88px" v-loading="dialogLoading" :disabled="formDisabled" class="finance-report-item-init-form">
      <el-form-item label="账簿" prop="ledgerId">
        <el-select v-model="formData.ledgerId" placeholder="请选择账簿" filterable class="!w-full">
          <el-option v-for="item in ledgerOptions" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="覆盖已有" prop="overrideExisting">
        <el-switch v-model="formData.overrideExisting" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button type="primary" :loading="submitLoading" :disabled="dialogLoading" @click="submitForm">确定</el-button>
      <el-button :disabled="submitLoading" @click="dialogVisible = false">取消</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import type { FormRules } from 'element-plus'
import { ErpFinanceLedgerVO, FinanceLedgerApi } from '@/api/erp/finance/ledger'
import { FinanceReportItemApi } from '@/api/erp/finance/report-item'
import { reportItemDemoLedgers } from './demo'

defineOptions({ name: 'ReportItemTemplateInitForm' })

const createEmptyFormData = () => ({
  ledgerId: undefined as number | undefined,
  overrideExisting: false
})

const message = useMessage()
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const submitLoading = ref(false)
const formData = ref(createEmptyFormData())
const formRef = ref()
const ledgerOptions = ref<ErpFinanceLedgerVO[]>([])

const formDisabled = computed(() => dialogLoading.value || submitLoading.value)
const formRules: FormRules<typeof formData.value> = {
  ledgerId: [{ required: true, message: '账簿不能为空', trigger: 'change' }]
}

const resetForm = async () => {
  formData.value = createEmptyFormData()
  await nextTick()
  formRef.value?.clearValidate()
}

const open = async () => {
  dialogVisible.value = true
  submitLoading.value = false
  await resetForm()
  dialogLoading.value = true
  try {
    const data = await FinanceLedgerApi.getLedgerSimpleList()
    ledgerOptions.value = data?.length ? data : import.meta.env.DEV ? reportItemDemoLedgers : []
  } catch {
    ledgerOptions.value = import.meta.env.DEV ? reportItemDemoLedgers : []
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
    await FinanceReportItemApi.initStandardTemplate({ ...formData.value })
    message.success('初始化成功')
    dialogVisible.value = false
    emit('success')
  } finally {
    submitLoading.value = false
  }
}

const clearDialogState = () => {
  ledgerOptions.value = []
  submitLoading.value = false
  dialogLoading.value = false
  formRef.value?.clearValidate?.()
}
</script>

<style scoped>
.finance-report-item-init-form :deep(.el-form-item) {
  margin-bottom: 12px;
}

.finance-report-item-init-form :deep(.el-select__wrapper),
.finance-report-item-init-form :deep(.el-input__wrapper) {
  border-radius: 10px;
}
</style>
