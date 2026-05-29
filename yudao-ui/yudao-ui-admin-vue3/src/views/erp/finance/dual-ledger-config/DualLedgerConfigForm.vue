<template>
  <Dialog
    v-model="dialogVisible"
    :title="dialogTitle"
    width="min(720px, 96vw)"
    scroll
    maxHeight="76vh"
  >
    <div class="dual-ledger-config-form">
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="110px"
        v-loading="dialogLoading"
        :disabled="submitLoading"
      >
        <div class="dual-ledger-config-form__panel">
          <div class="dual-ledger-config-form__grid">
            <el-form-item label="业务类型" prop="bizType">
              <el-select v-model="formData.bizType" class="!w-full" clearable placeholder="请选择业务类型">
                <el-option
                  v-for="item in ERP_BIZ_TYPE_OPTIONS"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="对外账账簿" prop="externalLedgerId">
              <el-select
                v-model="formData.externalLedgerId"
                class="!w-full"
                clearable
                filterable
                placeholder="请选择对外账账簿"
              >
                <el-option
                  v-for="item in ledgerOptions"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="内部账账簿" prop="internalLedgerId">
              <el-select
                v-model="formData.internalLedgerId"
                class="!w-full"
                clearable
                filterable
                placeholder="请选择内部账账簿"
              >
                <el-option
                  v-for="item in ledgerOptions"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="formData.status">
                <el-radio
                  v-for="item in COMMON_STATUS_OPTIONS"
                  :key="item.value"
                  :label="item.value"
                >
                  {{ item.label }}
                </el-radio>
              </el-radio-group>
            </el-form-item>
          </div>

          <el-form-item label="备注" prop="remark" class="dual-ledger-config-form__remark">
            <el-input
              v-model="formData.remark"
              type="textarea"
              :rows="3"
              maxlength="255"
              show-word-limit
              placeholder="请输入备注"
            />
          </el-form-item>
        </div>
      </el-form>
    </div>
    <template #footer>
      <el-button :disabled="submitLoading" @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitLoading" :disabled="submitLoading" @click="submitForm">
        保存
      </el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import type { FormInstance, FormRules } from 'element-plus'
import { type ErpFinanceLedgerVO, FinanceLedgerApi } from '@/api/erp/finance/ledger'
import {
  FinanceDualLedgerConfigApi,
  type ErpFinanceDualLedgerConfigSaveReqVO,
  type ErpFinanceDualLedgerConfigVO
} from '@/api/erp/finance/dual-ledger-config'
import { ERP_BIZ_TYPE_OPTIONS } from '@/api/erp/finance/dual-ledger-diff-config'
import { COMMON_STATUS_OPTIONS } from '@/views/erp/finance/shared/accounting'

defineOptions({ name: 'DualLedgerConfigForm' })

type FormType = 'create' | 'update'

interface FormData {
  id?: number
  bizType?: number
  externalLedgerId?: number
  internalLedgerId?: number
  status?: number
  remark?: string
}

const message = useMessage()

const createEmptyFormData = (): FormData => ({
  id: undefined,
  bizType: undefined,
  externalLedgerId: undefined,
  internalLedgerId: undefined,
  status: 0,
  remark: ''
})

const dialogVisible = ref(false)
const dialogTitle = ref('')
const dialogLoading = ref(false)
const submitLoading = ref(false)
const formType = ref<FormType>('create')
const formData = ref<FormData>(createEmptyFormData())
const formRef = ref<FormInstance>()
const ledgerOptions = ref<ErpFinanceLedgerVO[]>([])

const formRules: FormRules<FormData> = {
  bizType: [{ required: true, message: '请选择业务类型', trigger: 'change' }],
  externalLedgerId: [{ required: true, message: '请选择对外账账簿', trigger: 'change' }],
  internalLedgerId: [{ required: true, message: '请选择内部账账簿', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const resetForm = async () => {
  formData.value = createEmptyFormData()
  await nextTick()
  formRef.value?.clearValidate()
}

const loadLedgerOptions = async () => {
  if (!ledgerOptions.value.length) {
    ledgerOptions.value = await FinanceLedgerApi.getLedgerSimpleList()
  }
}

const normalizeFormData = (detail?: ErpFinanceDualLedgerConfigVO): FormData => ({
  id: detail?.id,
  bizType: detail?.bizType,
  externalLedgerId: detail?.externalLedgerId,
  internalLedgerId: detail?.internalLedgerId,
  status: detail?.status,
  remark: detail?.remark || ''
})

const buildSubmitData = (): ErpFinanceDualLedgerConfigSaveReqVO => ({
  id: formData.value.id,
  bizType: formData.value.bizType,
  externalLedgerId: formData.value.externalLedgerId,
  internalLedgerId: formData.value.internalLedgerId,
  status: formData.value.status,
  remark: formData.value.remark
})

const titleMap: Record<FormType, string> = {
  create: '新增双账套账簿映射',
  update: '编辑双账套账簿映射'
}

const open = async (type: FormType, id?: number) => {
  dialogVisible.value = true
  dialogLoading.value = true
  submitLoading.value = false
  formType.value = type
  dialogTitle.value = titleMap[type]
  await resetForm()
  try {
    const [_, detail] = await Promise.all([
      loadLedgerOptions(),
      id ? FinanceDualLedgerConfigApi.getDualLedgerConfig(id) : Promise.resolve(undefined)
    ])
    if (detail) {
      formData.value = normalizeFormData(detail)
    }
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
    const payload = buildSubmitData()
    if (formType.value === 'create') {
      await FinanceDualLedgerConfigApi.createDualLedgerConfig(payload)
    } else {
      await FinanceDualLedgerConfigApi.updateDualLedgerConfig(payload)
    }
    message.success('保存成功')
    dialogVisible.value = false
    emit('success')
  } finally {
    submitLoading.value = false
  }
}
</script>

<style scoped>
.dual-ledger-config-form {
  background: #f8fafc;
}

.dual-ledger-config-form__panel {
  padding: 16px 18px;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  background: #ffffff;
  box-shadow: 0 8px 22px rgba(148, 163, 184, 0.08);
}

.dual-ledger-config-form__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

.dual-ledger-config-form__remark {
  margin-bottom: 0;
}

@media (max-width: 768px) {
  .dual-ledger-config-form__grid {
    grid-template-columns: 1fr;
  }
}
</style>
