<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="760px" scroll maxHeight="78vh">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="96px"
      v-loading="dialogLoading"
      :disabled="formDisabled"
    >
      <div class="dual-ledger-diff-config-form__grid">
        <el-form-item label="业务类型" prop="bizType">
          <el-select
            v-model="formData.bizType"
            class="!w-full"
            clearable
            placeholder="请选择业务类型"
          >
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
        <el-form-item label="外部账来源" prop="externalSourceType">
          <el-select
            v-model="formData.externalSourceType"
            class="!w-full"
            clearable
            placeholder="请选择外部账来源"
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
          v-if="requiresExternalSourceValue"
          label="外部账来源值"
          prop="externalSourceValue"
        >
          <el-select
            v-model="formData.externalSourceValue"
            class="!w-full"
            clearable
            placeholder="请选择外部账来源值"
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
          v-if="requiresInternalSourceValue"
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
        <el-form-item v-if="requiresRatio" label="比例系数" prop="ratio">
          <el-input-number
            v-model="formData.ratio"
            class="!w-full"
            controls-position="right"
            :precision="4"
          />
        </el-form-item>
        <el-form-item v-if="requiresFixedAmount" label="固定差额" prop="fixedAmount">
          <el-input-number
            v-model="formData.fixedAmount"
            class="!w-full"
            controls-position="right"
            :precision="2"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="formData.status" class="!w-full" placeholder="请选择状态">
            <el-option
              v-for="item in COMMON_STATUS_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
      </div>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button
        type="primary"
        :loading="submitLoading"
        :disabled="dialogLoading"
        @click="submitForm"
      >
        确定
      </el-button>
      <el-button :disabled="submitLoading" @click="dialogVisible = false">取消</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import type { FormInstance, FormItemRule, FormRules } from 'element-plus'
import {
  DUAL_LEDGER_DIFF_CALCULATION_TYPE_OPTIONS,
  DUAL_LEDGER_DIFF_ITEM_OPTIONS,
  DUAL_LEDGER_DIFF_SOURCE_TYPE_OPTIONS,
  ERP_BIZ_TYPE_OPTIONS,
  FinanceDualLedgerDiffConfigApi,
  type ErpFinanceDualLedgerDiffConfigSaveReqVO,
  type ErpFinanceDualLedgerDiffConfigVO
} from '@/api/erp/finance/dual-ledger-diff-config'
import { COMMON_STATUS_OPTIONS } from '@/views/erp/finance/shared/accounting'

defineOptions({ name: 'DualLedgerDiffConfigForm' })

type FormType = 'create' | 'update'

const COST_ITEM_SOURCE_TYPE = 10
const PRO_RATA_CALCULATION_TYPE = 1
const FIXED_VARIANCE_CALCULATION_TYPE = 2

const createEmptyFormData = (): ErpFinanceDualLedgerDiffConfigSaveReqVO => ({
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

const mapToFormData = (
  data: ErpFinanceDualLedgerDiffConfigVO
): ErpFinanceDualLedgerDiffConfigSaveReqVO => ({
  id: data.id,
  bizType: data.bizType,
  diffItemType: data.diffItemType,
  externalSourceType: data.externalSourceType,
  externalSourceValue: data.externalSourceValue,
  internalSourceType: data.internalSourceType,
  internalSourceValue: data.internalSourceValue,
  calculationType: data.calculationType,
  ratio: data.ratio,
  fixedAmount: data.fixedAmount,
  status: data.status ?? 0,
  remark: data.remark || ''
})

const createSourceValueValidator = (
  shouldRequire: () => boolean,
  messageText: string
): FormItemRule['validator'] => {
  return (_rule, value, callback) => {
    if (shouldRequire() && (value === undefined || value === null || value === '')) {
      callback(new Error(messageText))
      return
    }
    callback()
  }
}

const createDirectionValidator = (
  shouldRequire: () => boolean,
  isValid: (value: number) => boolean,
  messageText: string
): FormItemRule['validator'] => {
  return (_rule, value, callback) => {
    if (!shouldRequire()) {
      callback()
      return
    }
    const numberValue = Number(value)
    if (value === undefined || value === null || value === '' || !Number.isFinite(numberValue)) {
      callback(new Error(messageText))
      return
    }
    if (!isValid(numberValue)) {
      callback(new Error(messageText))
      return
    }
    callback()
  }
}

const { t } = useI18n()
const message = useMessage()
const dialogVisible = ref(false)
const dialogTitle = ref('新增配置')
const dialogLoading = ref(false)
const submitLoading = ref(false)
const formType = ref<FormType>('create')
const formRef = ref<FormInstance>()
const formData = ref<ErpFinanceDualLedgerDiffConfigSaveReqVO>(createEmptyFormData())

const requiresExternalSourceValue = computed(
  () => formData.value.externalSourceType === COST_ITEM_SOURCE_TYPE
)
const requiresInternalSourceValue = computed(
  () => formData.value.internalSourceType === COST_ITEM_SOURCE_TYPE
)
const requiresRatio = computed(() => formData.value.calculationType === PRO_RATA_CALCULATION_TYPE)
const requiresFixedAmount = computed(
  () => formData.value.calculationType === FIXED_VARIANCE_CALCULATION_TYPE
)
const formDisabled = computed(() => dialogLoading.value || submitLoading.value)

const formRules: FormRules<ErpFinanceDualLedgerDiffConfigSaveReqVO> = {
  bizType: [{ required: true, message: '业务类型不能为空', trigger: 'change' }],
  diffItemType: [{ required: true, message: '差异项不能为空', trigger: 'change' }],
  externalSourceType: [{ required: true, message: '外部账来源不能为空', trigger: 'change' }],
  externalSourceValue: [
    {
      validator: createSourceValueValidator(
        () => requiresExternalSourceValue.value,
        '外部账来源值不能为空'
      ),
      trigger: 'change'
    }
  ],
  internalSourceType: [{ required: true, message: '内部账来源不能为空', trigger: 'change' }],
  internalSourceValue: [
    {
      validator: createSourceValueValidator(
        () => requiresInternalSourceValue.value,
        '内部账来源值不能为空'
      ),
      trigger: 'change'
    }
  ],
  calculationType: [{ required: true, message: '计算类型不能为空', trigger: 'change' }],
  ratio: [
    {
      validator: createDirectionValidator(
        () => requiresRatio.value,
        (value) => value > 1,
        '比例系数必须大于 1，才能保证内账金额小于外账金额'
      ),
      trigger: 'change'
    }
  ],
  fixedAmount: [
    {
      validator: createDirectionValidator(
        () => requiresFixedAmount.value,
        (value) => value < 0,
        '固定差额必须小于 0，才能保证内账金额小于外账金额'
      ),
      trigger: 'change'
    }
  ],
  status: [{ required: true, message: '状态不能为空', trigger: 'change' }]
}

const resetForm = async () => {
  formData.value = createEmptyFormData()
  await nextTick()
  formRef.value?.clearValidate()
}

watch(
  () => formData.value.externalSourceType,
  (value) => {
    if (value !== COST_ITEM_SOURCE_TYPE) {
      formData.value.externalSourceValue = undefined
    }
  }
)

watch(
  () => formData.value.internalSourceType,
  (value) => {
    if (value !== COST_ITEM_SOURCE_TYPE) {
      formData.value.internalSourceValue = undefined
    }
  }
)

watch(
  () => formData.value.calculationType,
  (value) => {
    if (value !== PRO_RATA_CALCULATION_TYPE) {
      formData.value.ratio = undefined
    }
    if (value !== FIXED_VARIANCE_CALCULATION_TYPE) {
      formData.value.fixedAmount = undefined
    }
  }
)

const open = async (type: FormType, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增配置' : '编辑配置'
  dialogLoading.value = false
  submitLoading.value = false
  formType.value = type
  await resetForm()
  if (type !== 'update' || !id) {
    return
  }
  dialogLoading.value = true
  try {
    const data = await FinanceDualLedgerDiffConfigApi.getDualLedgerDiffConfig(id)
    formData.value = mapToFormData(data)
    await nextTick()
    formRef.value?.clearValidate()
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
    if (formType.value === 'create') {
      await FinanceDualLedgerDiffConfigApi.createDualLedgerDiffConfig({ ...formData.value })
      message.success(t('common.createSuccess'))
    } else {
      await FinanceDualLedgerDiffConfigApi.updateDualLedgerDiffConfig({ ...formData.value })
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
.dual-ledger-diff-config-form__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

@media (max-width: 768px) {
  .dual-ledger-diff-config-form__grid {
    grid-template-columns: 1fr;
  }
}
</style>
