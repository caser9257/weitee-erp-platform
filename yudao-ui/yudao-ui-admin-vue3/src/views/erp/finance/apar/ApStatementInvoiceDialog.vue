<template>
  <Dialog
    v-model="dialogVisible"
    title="收票登记"
    width="min(760px, 96vw)"
    scroll
    maxHeight="72vh"
  >
    <div class="ap-statement-invoice-dialog">
      <div v-if="detail.statementNo" class="finance-shell__context-card finance-shell__dialog-card">
        <div class="finance-shell__context-main">
          <div class="finance-shell__context-title">{{ detail.statementNo || '-' }}</div>
          <div class="finance-shell__context-subtitle">{{ detail.supplierName || '-' }}</div>
          <div class="finance-shell__context-meta">
            <span>{{ detail.bizNo || '-' }}</span>
            <span>{{ detail.accountName || '-' }}</span>
          </div>
        </div>
        <span class="finance-shell__page-chip">{{ getInvoiceStatusLabel(formData.invoiceStatus) }}</span>
      </div>
      <el-form
        ref="formRef"
        v-loading="dialogLoading"
        :model="formData"
        :rules="formRules"
        label-width="92px"
      >
        <div class="ap-statement-invoice-dialog__panel">
          <div class="ap-statement-invoice-dialog__grid">
            <el-form-item label="台账编号">
              <el-input :model-value="detail.statementNo || '-'" disabled />
            </el-form-item>
            <el-form-item label="业务单号">
              <el-input :model-value="detail.bizNo || '-'" disabled />
            </el-form-item>
            <el-form-item label="供应商">
              <el-input :model-value="detail.supplierName || '-'" disabled />
            </el-form-item>
            <el-form-item label="收票状态" prop="invoiceStatus">
              <el-select
                v-model="formData.invoiceStatus"
                placeholder="请选择收票状态"
                class="!w-full"
              >
                <el-option
                  v-for="item in ERP_AP_INVOICE_STATUS_OPTIONS"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="发票号" prop="invoiceNo">
              <el-input
                v-model="formData.invoiceNo"
                :disabled="isInvoiceFieldDisabled"
                clearable
                placeholder="请输入发票号"
              />
            </el-form-item>
            <el-form-item label="发票金额" prop="invoiceAmount">
              <el-input-number
                v-model="formData.invoiceAmount"
                :disabled="isInvoiceFieldDisabled"
                :min="0"
                :precision="2"
                controls-position="right"
                class="!w-full"
                placeholder="请输入发票金额"
              />
            </el-form-item>
          </div>
          <el-form-item label="备注" prop="remark" class="ap-statement-invoice-dialog__remark">
            <el-input
              v-model="formData.remark"
              :disabled="dialogLoading"
              type="textarea"
              :rows="3"
              placeholder="请输入收票备注"
            />
          </el-form-item>
        </div>
      </el-form>
    </div>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button
        type="primary"
        :loading="submitLoading"
        :disabled="submitDisabled"
        @click="submitForm"
      >
        保存
      </el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import type { FormInstance, FormRules } from 'element-plus'
import {
  ApStatementApi,
  type ApStatementUpdateInvoiceReqVO,
  type ApStatementVO,
  ERP_AP_INVOICE_STATUS_OPTIONS
} from '@/api/erp/finance/apar'

defineOptions({ name: 'ApStatementInvoiceDialog' })

interface InvoiceFormData {
  id?: number
  invoiceStatus?: number
  invoiceNo?: string
  invoiceAmount?: number
  remark?: string
}

const emit = defineEmits<{
  (e: 'success', id: number): void
}>()

const message = useMessage()

const dialogVisible = ref(false)
const dialogLoading = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()

const detail = ref<ApStatementVO>({})
const formData = ref<InvoiceFormData>({
  id: undefined,
  invoiceStatus: 0,
  invoiceNo: '',
  invoiceAmount: undefined,
  remark: ''
})

const isInvoiceFieldDisabled = computed(() => formData.value.invoiceStatus === 0)
const submitDisabled = computed(
  () => dialogLoading.value || submitLoading.value || formData.value.id == null
)
const getInvoiceStatusLabel = (value?: number) =>
  ERP_AP_INVOICE_STATUS_OPTIONS.find((item) => item.value === value)?.label || '-'

const formRules: FormRules<InvoiceFormData> = {
  invoiceStatus: [{ required: true, message: '请选择收票状态', trigger: 'change' }]
}

const resetForm = async () => {
  detail.value = {}
  formData.value = {
    id: undefined,
    invoiceStatus: 0,
    invoiceNo: '',
    invoiceAmount: undefined,
    remark: ''
  }
  await nextTick()
  formRef.value?.clearValidate()
}

const syncFormData = (data: ApStatementVO) => {
  detail.value = data
  formData.value = {
    id: data.id,
    invoiceStatus: data.invoiceStatus ?? 0,
    invoiceNo: data.invoiceNo || '',
    invoiceAmount:
      data.invoiceAmount === undefined || data.invoiceAmount === null
        ? undefined
        : Number(data.invoiceAmount),
    remark: ''
  }
}

watch(
  () => formData.value.invoiceStatus,
  (invoiceStatus) => {
    if (invoiceStatus === 0) {
      formData.value.invoiceNo = ''
      formData.value.invoiceAmount = undefined
    }
  }
)

watch(dialogVisible, (visible) => {
  if (visible) {
    return
  }
  dialogLoading.value = false
  submitLoading.value = false
  void resetForm()
})

const open = async (id: number) => {
  dialogVisible.value = true
  await resetForm()
  dialogLoading.value = true
  try {
    const data = await ApStatementApi.getApStatement(id)
    if (!dialogVisible.value) {
      return
    }
    syncFormData(data)
  } finally {
    dialogLoading.value = false
  }
}

defineExpose({ open })

const submitForm = async () => {
  if (submitLoading.value) {
    return
  }
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    const payload: ApStatementUpdateInvoiceReqVO = {
      id: Number(formData.value.id),
      invoiceStatus: Number(formData.value.invoiceStatus),
      invoiceNo: isInvoiceFieldDisabled.value ? undefined : formData.value.invoiceNo?.trim(),
      invoiceAmount: isInvoiceFieldDisabled.value ? undefined : formData.value.invoiceAmount,
      remark: formData.value.remark?.trim()
    }
    await ApStatementApi.updateInvoice(payload)
    message.success('收票登记成功')
    dialogVisible.value = false
    emit('success', payload.id)
  } finally {
    submitLoading.value = false
  }
}
</script>

<style scoped>
.ap-statement-invoice-dialog {
  background: #f8fafc;
}

.ap-statement-invoice-dialog :deep(.el-form-item__label) {
  color: #475569;
  font-weight: 600;
}

.ap-statement-invoice-dialog__panel {
  padding: 18px 20px;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  background: #ffffff;
  box-shadow: 0 8px 22px rgba(148, 163, 184, 0.08);
}

.ap-statement-invoice-dialog__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

.ap-statement-invoice-dialog__remark :deep(.el-form-item__content) {
  display: block;
}

@media (max-width: 768px) {
  .ap-statement-invoice-dialog__grid {
    grid-template-columns: 1fr;
  }
}
</style>
