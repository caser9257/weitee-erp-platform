<template>
  <Dialog v-model="dialogVisible" title="按年生成期间" width="640px" scroll maxHeight="78vh">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="96px" v-loading="dialogLoading" :disabled="formDisabled">
      <el-form-item label="账簿" prop="ledgerId">
        <el-select v-model="formData.ledgerId" placeholder="请选择账簿" filterable class="!w-full">
          <el-option v-for="item in ledgerOptions" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="会计年度" prop="periodYear">
        <el-input-number v-model="formData.periodYear" :min="2000" :max="2099" controls-position="right" class="!w-full" />
      </el-form-item>
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
import {
  ErpFinancePeriodCreateYearReqVO,
  FinancePeriodApi
} from '@/api/erp/finance/period'

defineOptions({ name: 'PeriodCreateYearForm' })

const createEmptyFormData = (): ErpFinancePeriodCreateYearReqVO => ({
  ledgerId: undefined,
  periodYear: undefined,
  remark: ''
})

const message = useMessage()
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const submitLoading = ref(false)
const formData = ref<ErpFinancePeriodCreateYearReqVO>(createEmptyFormData())
const formRef = ref()
const ledgerOptions = ref<ErpFinanceLedgerVO[]>([])

const formDisabled = computed(() => dialogLoading.value || submitLoading.value)
const formRules: FormRules<ErpFinancePeriodCreateYearReqVO> = {
  ledgerId: [{ required: true, message: '账簿不能为空', trigger: 'change' }],
  periodYear: [{ required: true, message: '会计年度不能为空', trigger: 'change' }]
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
    await FinancePeriodApi.createPeriodsByYear({ ...formData.value })
    message.success('按年生成成功')
    dialogVisible.value = false
    emit('success')
  } finally {
    submitLoading.value = false
  }
}
</script>
