<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="720px">
    <div class="plan-rule-form-body" v-loading="formLoading">
      <el-alert
        v-if="loadErrorMessage"
        :title="loadErrorMessage"
        type="error"
        :closable="false"
        show-icon
        class="mb-16px"
      />
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="110px">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12">
            <el-form-item label="产品" prop="productId">
              <el-select
                v-model="formData.productId"
                clearable
                filterable
                placeholder="请选择产品"
                class="!w-full"
                :loading="optionLoading"
              >
                <el-option
                  v-for="item in productList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="供给方式" prop="supplyType">
              <el-select
                v-model="formData.supplyType"
                clearable
                placeholder="请选择供给方式"
                class="!w-full"
                @change="handleSupplyTypeChange"
              >
                <el-option
                  v-for="dict in getStrDictOptions(DICT_TYPE.ERP_SUPPLY_TYPE)"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="补货策略" prop="replenishMode">
              <el-select
                v-model="formData.replenishMode"
                placeholder="请选择补货策略"
                class="!w-full"
                @change="handleReplenishModeChange"
              >
                <el-option
                  v-for="item in REPLENISH_MODE_OPTIONS"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="formData.status">
                <el-radio
                  v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
                  :key="dict.value"
                  :value="dict.value"
                >
                  {{ dict.label }}
                </el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="安全库存" prop="safetyStock">
              <el-input-number v-model="formData.safetyStock" :min="0" :precision="2" class="!w-full" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item :label="fieldLabels.minOrderQty" prop="minOrderQty">
              <el-input-number v-model="formData.minOrderQty" :min="0" :precision="2" class="!w-full" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item :label="fieldLabels.orderMultiple" prop="orderMultiple">
              <el-input-number v-model="formData.orderMultiple" :min="1" :precision="2" class="!w-full" />
            </el-form-item>
          </el-col>
          <el-col v-if="showFixedOrderQty" :xs="24" :sm="12">
            <el-form-item label="固定批量" prop="fixedOrderQty">
              <el-input-number v-model="formData.fixedOrderQty" :min="0.01" :precision="2" class="!w-full" />
            </el-form-item>
          </el-col>
          <el-col v-if="isPurchaseSupply" :xs="24" :sm="12">
            <el-form-item label="采购提前期" prop="purchaseLeadDay">
              <el-input-number v-model="formData.purchaseLeadDay" :min="0" :precision="0" class="!w-full" />
            </el-form-item>
          </el-col>
          <el-col v-if="isMakeSupply" :xs="24" :sm="12">
            <el-form-item label="生产提前期" prop="makeLeadDay">
              <el-input-number v-model="formData.makeLeadDay" :min="0" :precision="0" class="!w-full" />
            </el-form-item>
          </el-col>
          <el-col v-if="isPurchaseSupply" :xs="24" :sm="12">
            <el-form-item label="默认供应商" prop="defaultSupplierId">
              <el-select
                v-model="formData.defaultSupplierId"
                clearable
                filterable
                placeholder="请选择默认供应商"
                class="!w-full"
                :loading="optionLoading"
              >
                <el-option
                  v-for="item in supplierList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="缺料预警" prop="shortageWarnFlag">
              <el-switch
                v-model="formData.shortageWarnFlag"
                inline-prompt
                active-text="开启"
                inactive-text="关闭"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24">
            <el-form-item label="备注" prop="remark">
              <el-input
                v-model="formData.remark"
                type="textarea"
                :rows="3"
                maxlength="255"
                show-word-limit
                placeholder="请输入备注"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </div>
    <template #footer>
      <div class="plan-rule-form-footer">
        <el-button v-if="loadErrorMessage" @click="handleRetryLoad" :disabled="formLoading || submitting">
          重新加载
        </el-button>
        <el-button type="primary" :loading="submitting" :disabled="submitDisabled" @click="submitForm">
          确定
        </el-button>
        <el-button :disabled="submitting" @click="dialogVisible = false">取消</el-button>
      </div>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { CommonStatusEnum } from '@/utils/constants'
import { DICT_TYPE, getIntDictOptions, getStrDictOptions } from '@/utils/dict'
import { PlanRuleApi, PlanRuleVO } from '@/api/erp/mrp/plan-rule'
import { ProductApi, ProductVO } from '@/api/erp/product/product'
import { SupplierApi, SupplierVO } from '@/api/erp/purchase/supplier'
import {
  buildPlanRulePayload,
  createDefaultPlanRuleFormData,
  getPlanRuleFieldLabels,
  isFixedLotMode,
  isMakeSupplyType,
  isPurchaseSupplyType,
  LOT_FOR_LOT_REPLENISH_MODE,
  normalizePlanRuleFormData,
  PlanRuleFormData,
  REPLENISH_MODE_OPTIONS
} from './planRuleForm.helpers'

defineOptions({ name: 'PlanRuleForm' })

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formType = ref<'create' | 'update'>('create')
const currentRuleId = ref<number>()
const requestToken = ref(0)

const optionLoading = ref(false)
const detailLoading = ref(false)
const submitting = ref(false)
const loadErrorMessage = ref('')

const formRef = ref()
const productList = ref<ProductVO[]>([])
const supplierList = ref<SupplierVO[]>([])
const formData = ref<PlanRuleFormData>(createDefaultPlanRuleFormData())

const formLoading = computed(() => optionLoading.value || detailLoading.value)
const isPurchaseSupply = computed(() => isPurchaseSupplyType(formData.value.supplyType))
const isMakeSupply = computed(() => isMakeSupplyType(formData.value.supplyType))
const showFixedOrderQty = computed(() => isFixedLotMode(formData.value.replenishMode))
const fieldLabels = computed(() => getPlanRuleFieldLabels(formData.value.supplyType))
const submitDisabled = computed(
  () => optionLoading.value || detailLoading.value || submitting.value || !!loadErrorMessage.value
)

const validateNumber =
  (label: string | (() => string), min: number, required = () => true) =>
  (_rule: any, value: number | undefined, callback: (error?: Error) => void) => {
    if (!required()) {
      callback()
      return
    }
    const resolvedLabel = typeof label === 'function' ? label() : label
    if (value === undefined || value === null) {
      callback(new Error(`请输入${resolvedLabel}`))
      return
    }
    if (value < min) {
      callback(new Error(`${resolvedLabel}不能小于 ${min}`))
      return
    }
    callback()
  }

const formRules = reactive({
  productId: [{ required: true, message: '请选择产品', trigger: 'change' }],
  supplyType: [{ required: true, message: '请选择供给方式', trigger: 'change' }],
  replenishMode: [{ required: true, message: '请选择补货策略', trigger: 'change' }],
  safetyStock: [{ validator: validateNumber('安全库存', 0), trigger: 'blur' }],
  minOrderQty: [{ validator: validateNumber(() => fieldLabels.value.minOrderQty, 0), trigger: 'blur' }],
  orderMultiple: [{ validator: validateNumber(() => fieldLabels.value.orderMultiple, 1), trigger: 'blur' }],
  fixedOrderQty: [
    {
      validator: validateNumber('固定批量', 0.01, () => showFixedOrderQty.value),
      trigger: 'blur'
    }
  ],
  purchaseLeadDay: [
    { validator: validateNumber('采购提前期', 0, () => isPurchaseSupply.value), trigger: 'blur' }
  ],
  makeLeadDay: [{ validator: validateNumber('生产提前期', 0, () => isMakeSupply.value), trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
})

const toStatus = (enableFlag?: boolean) =>
  enableFlag ? CommonStatusEnum.ENABLE : CommonStatusEnum.DISABLE

const isActiveRequest = (token: number) => token === requestToken.value && dialogVisible.value

const resetForm = () => {
  formData.value = createDefaultPlanRuleFormData()
  nextTick(() => {
    formRef.value?.resetFields()
    formRef.value?.clearValidate()
  })
}

const clearDynamicValidation = () => {
  nextTick(() => {
    formRef.value?.clearValidate([
      'minOrderQty',
      'orderMultiple',
      'fixedOrderQty',
      'purchaseLeadDay',
      'makeLeadDay'
    ])
  })
}

const handleSupplyTypeChange = () => {
  formData.value = normalizePlanRuleFormData(formData.value)
  clearDynamicValidation()
}

const handleReplenishModeChange = () => {
  formData.value = normalizePlanRuleFormData(formData.value)
  clearDynamicValidation()
}

const applyRuleDetail = (data: PlanRuleVO) => {
  formData.value = normalizePlanRuleFormData({
    ...createDefaultPlanRuleFormData(),
    id: data.id,
    productId: data.productId,
    supplyType: data.supplyType,
    replenishMode: data.replenishMode || LOT_FOR_LOT_REPLENISH_MODE,
    safetyStock: data.safetyStock ?? 0,
    minOrderQty: data.minOrderQty ?? 0,
    orderMultiple: data.orderMultiple ?? 1,
    fixedOrderQty: data.fixedOrderQty,
    purchaseLeadDay: data.purchaseLeadDay ?? 0,
    makeLeadDay: data.makeLeadDay ?? 0,
    shortageWarnFlag: data.shortageWarnFlag ?? true,
    defaultSupplierId: data.defaultSupplierId,
    remark: data.remark || '',
    status: toStatus(data.enableFlag)
  })
  if (!data.replenishMode) {
    formData.value.replenishMode = LOT_FOR_LOT_REPLENISH_MODE
  }
  clearDynamicValidation()
}

const loadOptions = async (token: number) => {
  optionLoading.value = true
  try {
    const [products, suppliers] = await Promise.all([
      ProductApi.getProductSimpleList(),
      SupplierApi.getSupplierSimpleList()
    ])
    if (!isActiveRequest(token)) {
      return
    }
    productList.value = products
    supplierList.value = suppliers
  } catch {
    if (isActiveRequest(token)) {
      loadErrorMessage.value = '基础选项加载失败，请重试'
    }
  } finally {
    if (isActiveRequest(token)) {
      optionLoading.value = false
    }
  }
}

const loadDetail = async (id: number, token: number) => {
  detailLoading.value = true
  try {
    const data = await PlanRuleApi.getPlanRule(id)
    if (!isActiveRequest(token)) {
      return
    }
    applyRuleDetail(data)
  } catch {
    if (isActiveRequest(token)) {
      loadErrorMessage.value = '计划参数详情加载失败，请重试'
    }
  } finally {
    if (isActiveRequest(token)) {
      detailLoading.value = false
    }
  }
}

const loadDialogData = async () => {
  const token = requestToken.value + 1
  requestToken.value = token
  loadErrorMessage.value = ''
  await loadOptions(token)
  if (currentRuleId.value && !loadErrorMessage.value) {
    await loadDetail(currentRuleId.value, token)
  }
}

const open = async (type: 'create' | 'update', id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  currentRuleId.value = id
  resetForm()
  await loadDialogData()
}

defineExpose({ open })

const emit = defineEmits(['success'])

const handleRetryLoad = async () => {
  resetForm()
  await loadDialogData()
}

const submitForm = async () => {
  if (submitDisabled.value) {
    return
  }
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }
  submitting.value = true
  try {
    const data = buildPlanRulePayload(formData.value)
    if (formType.value === 'create') {
      await PlanRuleApi.createPlanRule(data)
      message.success(t('common.createSuccess'))
    } else {
      await PlanRuleApi.updatePlanRule(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    submitting.value = false
  }
}

watch(dialogVisible, (visible) => {
  if (!visible) {
    currentRuleId.value = undefined
    optionLoading.value = false
    detailLoading.value = false
    submitting.value = false
    loadErrorMessage.value = ''
    resetForm()
  }
})
</script>

<style scoped>
.plan-rule-form-body {
  max-height: 70vh;
  overflow-y: auto;
  padding-right: 4px;
}

.plan-rule-form-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  flex-wrap: wrap;
}

@media (max-width: 768px) {
  .plan-rule-form-body {
    max-height: 62vh;
    padding-right: 0;
  }

  .plan-rule-form-footer {
    justify-content: stretch;
  }

  .plan-rule-form-footer :deep(.el-button) {
    flex: 1 1 140px;
  }
}
</style>
