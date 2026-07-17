<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="680px" scroll maxHeight="78vh">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="96px"
      v-loading="dialogLoading"
      :disabled="formDisabled"
    >
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
        <el-form-item :label="financeDisplayLabel('外部账账簿', '账目一账簿')" prop="externalLedgerId">
          <el-select
            v-model="formData.externalLedgerId"
            class="!w-full"
            clearable
            filterable
            :placeholder="financeDisplayLabel('请选择外部账账簿', '请选择账目一账簿')"
          >
            <el-option
              v-for="item in ledgerOptions"
              :key="item.id"
              :label="displayLedgerName(item.name)"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="financeDisplayLabel('内部账账簿', '账目二账簿')" prop="internalLedgerId">
          <el-select
            v-model="formData.internalLedgerId"
            class="!w-full"
            clearable
            filterable
            :placeholder="financeDisplayLabel('请选择内部账账簿', '请选择账目二账簿')"
          >
            <el-option
              v-for="item in ledgerOptions"
              :key="item.id"
              :label="displayLedgerName(item.name)"
              :value="item.id"
            />
          </el-select>
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
import type { FormInstance, FormRules } from 'element-plus'
import { type ErpFinanceLedgerVO, FinanceLedgerApi } from '@/api/erp/finance/ledger'
import { ERP_BIZ_TYPE_OPTIONS } from '@/api/erp/finance/dual-ledger-diff-config'
import {
  FinanceDualLedgerConfigApi,
  type ErpFinanceDualLedgerConfigSaveReqVO,
  type ErpFinanceDualLedgerConfigVO
} from '@/api/erp/finance/dual-ledger-config'
import { COMMON_STATUS_OPTIONS } from '@/views/erp/finance/shared/accounting'
import { displayLedgerName, financeDisplayLabel } from '@/utils/financeDisplay'

defineOptions({ name: 'DualLedgerConfigForm' })

type FormType = 'create' | 'update'

const createEmptyFormData = (): ErpFinanceDualLedgerConfigSaveReqVO => ({
  id: undefined,
  bizType: undefined,
  externalLedgerId: undefined,
  internalLedgerId: undefined,
  status: 0,
  remark: ''
})

const mapToFormData = (data: ErpFinanceDualLedgerConfigVO): ErpFinanceDualLedgerConfigSaveReqVO => ({
  id: data.id,
  bizType: data.bizType,
  externalLedgerId: data.externalLedgerId,
  internalLedgerId: data.internalLedgerId,
  status: data.status ?? 0,
  remark: data.remark || ''
})

const { t } = useI18n()
const message = useMessage()
const dialogVisible = ref(false)
const dialogTitle = ref('新增映射')
const dialogLoading = ref(false)
const submitLoading = ref(false)
const formType = ref<FormType>('create')
const formRef = ref<FormInstance>()
const formData = ref<ErpFinanceDualLedgerConfigSaveReqVO>(createEmptyFormData())
const ledgerOptions = ref<ErpFinanceLedgerVO[]>([])

const formDisabled = computed(() => dialogLoading.value || submitLoading.value)
const formRules: FormRules<ErpFinanceDualLedgerConfigSaveReqVO> = {
  bizType: [{ required: true, message: '业务类型不能为空', trigger: 'change' }],
  externalLedgerId: [{ required: true, message: financeDisplayLabel('外部账账簿不能为空', '账目一账簿不能为空'), trigger: 'change' }],
  internalLedgerId: [{ required: true, message: financeDisplayLabel('内部账账簿不能为空', '账目二账簿不能为空'), trigger: 'change' }],
  status: [{ required: true, message: '状态不能为空', trigger: 'change' }]
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

const open = async (type: FormType, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增映射' : '编辑映射'
  dialogLoading.value = false
  submitLoading.value = false
  formType.value = type
  await resetForm()
  dialogLoading.value = true
  try {
    await loadLedgerOptions()
    if (type === 'update' && id) {
      const data = await FinanceDualLedgerConfigApi.getDualLedgerConfig(id)
      formData.value = mapToFormData(data)
      await nextTick()
      formRef.value?.clearValidate()
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
    if (formType.value === 'create') {
      await FinanceDualLedgerConfigApi.createDualLedgerConfig({ ...formData.value })
      message.success(t('common.createSuccess'))
    } else {
      await FinanceDualLedgerConfigApi.updateDualLedgerConfig({ ...formData.value })
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
.dual-ledger-config-form__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

@media (max-width: 768px) {
  .dual-ledger-config-form__grid {
    grid-template-columns: 1fr;
  }
}
</style>
