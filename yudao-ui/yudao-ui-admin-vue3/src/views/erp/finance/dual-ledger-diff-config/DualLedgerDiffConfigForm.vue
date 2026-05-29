<template>
  <Dialog
    v-model="dialogVisible"
    :title="dialogTitle"
    width="min(760px, 96vw)"
    scroll
    maxHeight="78vh"
  >
    <div class="dual-ledger-diff-config-form">
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="110px"
        v-loading="dialogLoading"
        :disabled="submitLoading"
      >
        <div class="dual-ledger-diff-config-form__panel">
          <div class="dual-ledger-diff-config-form__grid">
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
            <el-form-item label="差异项" prop="diffItemType">
              <el-select
                v-model="formData.diffItemType"
                class="!w-full"
                clearable
                placeholder="请选择差异项"
              >
                <el-option
                  v-for="item in DUAL_LEDGER_DIFF_ITEM_OPTIONS"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="对外账来源" prop="externalSourceType">
              <el-select
                v-model="formData.externalSourceType"
                class="!w-full"
                clearable
                placeholder="请选择对外账来源"
              >
                <el-option
                  v-for="item in DUAL_LEDGER_DIFF_SOURCE_TYPE_OPTIONS"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item
              v-if="showExternalSourceValueField"
              label="对外账来源值"
              prop="externalSourceValue"
            >
              <el-select
                v-model="formData.externalSourceValue"
                class="!w-full"
                clearable
                placeholder="请选择对外账来源值"
              >
                <el-option
                  v-for="item in DUAL_LEDGER_DIFF_ITEM_OPTIONS"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="内部账来源" prop="internalSourceType">
              <el-select
                v-model="formData.internalSourceType"
                class="!w-full"
                clearable
                placeholder="请选择内部账来源"
              >
                <el-option
                  v-for="item in DUAL_LEDGER_DIFF_SOURCE_TYPE_OPTIONS"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item
              v-if="showInternalSourceValueField"
              label="内部账来源值"
              prop="internalSourceValue"
            >
              <el-select
                v-model="formData.internalSourceValue"
                class="!w-full"
                clearable
                placeholder="请选择内部账来源值"
              >
                <el-option
                  v-for="item in DUAL_LEDGER_DIFF_ITEM_OPTIONS"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="计算类型" prop="calculationType">
              <el-select
                v-model="formData.calculationType"
                class="!w-full"
                clearable
                placeholder="请选择计算类型"
              >
                <el-option
                  v-for="item in DUAL_LEDGER_DIFF_CALCULATION_TYPE_OPTIONS"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item v-if="showRatioField" label="比例系数" prop="ratio">
              <el-input-number
                v-model="formData.ratio"
                class="!w-full"
                :precision="4"
                :min="0.0001"
                controls-position="right"
                placeholder="请输入比例系数"
              />
            </el-form-item>
            <el-form-item v-if="showFixedAmountField" label="固定差额" prop="fixedAmount">
              <el-input-number
                v-model="formData.fixedAmount"
                class="!w-full"
                :precision="2"
                controls-position="right"
                placeholder="请输入固定差额"
              />
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

          <el-form-item label="备注" prop="remark" class="dual-ledger-diff-config-form__remark">
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
import {
  DUAL_LEDGER_DIFF_CALCULATION_TYPE_OPTIONS,
  DUAL_LEDGER_DIFF_ITEM_OPTIONS,
  DUAL_LEDGER_DIFF_SOURCE_TYPE_OPTIONS,
  ERP_BIZ_TYPE_OPTIONS,
  FinanceDualLedgerDiffConfigApi,
  requiresDualLedgerSourceValue,
  type ErpFinanceDualLedgerDiffConfigSaveReqVO,
  type ErpFinanceDualLedgerDiffConfigVO
} from '@/api/erp/finance/dual-ledger-diff-config'
import { COMMON_STATUS_OPTIONS } from '@/views/erp/finance/shared/accounting'

defineOptions({ name: 'DualLedgerDiffConfigForm' })

type FormType = 'create' | 'update'

interface FormData {
  id?: number
  bizType?: number
  diffItemType?: number
  externalSourceType?: number
  externalSourceValue?: number
  internalSourceType?: number
  internalSourceValue?: number
  calculationType?: number
  ratio?: number
  fixedAmount?: number
  status?: number
  remark?: string
}

const message = useMessage()

const createEmptyFormData = (): FormData => ({
  id: undefined,
  bizType: undefined,
  diffItemType: undefined,
  externalSourceType: undefined,
  externalSourceValue: undefined,
  internalSourceType: undefined,
  internalSourceValue: undefined,
  calculationType: undefined,
  ratio: undefined,
  fixedAmount: undefined,
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

const showRatioField = computed(() => Number(formData.value.calculationType) === 1)
const showFixedAmountField = computed(() => Number(formData.value.calculationType) === 2)
const showExternalSourceValueField = computed(() =>
  requiresDualLedgerSourceValue(formData.value.externalSourceType)
)
const showInternalSourceValueField = computed(() =>
  requiresDualLedgerSourceValue(formData.value.internalSourceType)
)

watch(
  () => formData.value.externalSourceType,
  (value) => {
    if (!requiresDualLedgerSourceValue(value)) {
      formData.value.externalSourceValue = undefined
    }
  }
)

watch(
  () => formData.value.internalSourceType,
  (value) => {
    if (!requiresDualLedgerSourceValue(value)) {
      formData.value.internalSourceValue = undefined
    }
  }
)

watch(
  () => formData.value.calculationType,
  (value) => {
    if (Number(value) !== 1) {
      formData.value.ratio = undefined
    }
    if (Number(value) !== 2) {
      formData.value.fixedAmount = undefined
    }
  }
)

const formRules: FormRules<FormData> = {
  bizType: [{ required: true, message: '请选择业务类型', trigger: 'change' }],
  diffItemType: [{ required: true, message: '请选择差异项', trigger: 'change' }],
  externalSourceType: [{ required: true, message: '请选择对外账来源', trigger: 'change' }],
  internalSourceType: [{ required: true, message: '请选择内部账来源', trigger: 'change' }],
  calculationType: [{ required: true, message: '请选择计算类型', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
  externalSourceValue: [
    {
      validator: (_rule, value, callback) => {
        if (showExternalSourceValueField.value && value == null) {
          callback(new Error('请选择对外账来源值'))
          return
        }
        callback()
      },
      trigger: 'change'
    }
  ],
  internalSourceValue: [
    {
      validator: (_rule, value, callback) => {
        if (showInternalSourceValueField.value && value == null) {
          callback(new Error('请选择内部账来源值'))
          return
        }
        callback()
      },
      trigger: 'change'
    }
  ],
  ratio: [
    {
      validator: (_rule, value, callback) => {
        if (showRatioField.value && (value == null || Number(value) <= 0)) {
          callback(new Error('比例系数必须大于 0'))
          return
        }
        callback()
      },
      trigger: ['change', 'blur']
    }
  ],
  fixedAmount: [
    {
      validator: (_rule, value, callback) => {
        if (showFixedAmountField.value && value == null) {
          callback(new Error('请输入固定差额'))
          return
        }
        callback()
      },
      trigger: ['change', 'blur']
    }
  ]
}

const resetForm = async () => {
  formData.value = createEmptyFormData()
  await nextTick()
  formRef.value?.clearValidate()
}

const normalizeFormData = (detail?: ErpFinanceDualLedgerDiffConfigVO): FormData => ({
  id: detail?.id,
  bizType: detail?.bizType,
  diffItemType: detail?.diffItemType,
  externalSourceType: detail?.externalSourceType,
  externalSourceValue: detail?.externalSourceValue,
  internalSourceType: detail?.internalSourceType,
  internalSourceValue: detail?.internalSourceValue,
  calculationType: detail?.calculationType,
  ratio: detail?.ratio == null ? undefined : Number(detail.ratio),
  fixedAmount: detail?.fixedAmount == null ? undefined : Number(detail.fixedAmount),
  status: detail?.status,
  remark: detail?.remark || ''
})

const buildSubmitData = (): ErpFinanceDualLedgerDiffConfigSaveReqVO => ({
  id: formData.value.id,
  bizType: formData.value.bizType,
  diffItemType: formData.value.diffItemType,
  externalSourceType: formData.value.externalSourceType,
  externalSourceValue: formData.value.externalSourceValue,
  internalSourceType: formData.value.internalSourceType,
  internalSourceValue: formData.value.internalSourceValue,
  calculationType: formData.value.calculationType,
  ratio: formData.value.ratio,
  fixedAmount: formData.value.fixedAmount,
  status: formData.value.status,
  remark: formData.value.remark
})

const titleMap: Record<FormType, string> = {
  create: '新增双账套口径配置',
  update: '编辑双账套口径配置'
}

const open = async (type: FormType, id?: number) => {
  dialogVisible.value = true
  dialogLoading.value = true
  submitLoading.value = false
  formType.value = type
  dialogTitle.value = titleMap[type]
  await resetForm()
  try {
    if (id) {
      const detail = await FinanceDualLedgerDiffConfigApi.getDualLedgerDiffConfig(id)
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
      await FinanceDualLedgerDiffConfigApi.createDualLedgerDiffConfig(payload)
    } else {
      await FinanceDualLedgerDiffConfigApi.updateDualLedgerDiffConfig(payload)
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
.dual-ledger-diff-config-form {
  background: #f8fafc;
}

.dual-ledger-diff-config-form__panel {
  padding: 16px 18px;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  background: #ffffff;
  box-shadow: 0 8px 22px rgba(148, 163, 184, 0.08);
}

.dual-ledger-diff-config-form__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

.dual-ledger-diff-config-form__remark {
  margin-bottom: 0;
}

@media (max-width: 768px) {
  .dual-ledger-diff-config-form__grid {
    grid-template-columns: 1fr;
  }
}
</style>
