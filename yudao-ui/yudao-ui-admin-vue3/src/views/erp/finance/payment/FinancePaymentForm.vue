<template>
  <Dialog
    v-model="dialogVisible"
    :title="dialogTitle"
    width="min(1120px, 96vw)"
    scroll
    maxHeight="78vh"
  >
    <div class="finance-payment-form">
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
        v-loading="dialogLoading"
        :disabled="disabled"
      >
        <div class="finance-payment-form__panel">
          <el-alert
            v-if="showApprovalRunningReminder"
            class="mb-16px"
            type="warning"
            :closable="false"
            show-icon
            title="当前付款单正在审批中，暂不可编辑。如需调整，请先回到列表页撤回审批。"
          />
          <el-alert
            v-else-if="showReadonlyReminder"
            class="mb-16px"
            type="success"
            :closable="false"
            show-icon
            title="当前付款单已审批完成，仅支持查看。"
          />
          <el-alert
            v-else-if="showRejectedReminder"
            class="mb-16px"
            type="error"
            :closable="false"
            show-icon
            title="当前付款单已驳回，可修改后重新提交审批。"
          />
          <el-alert
            v-if="showDetailOverview"
            class="mb-16px finance-payment-form__detail-alert"
            type="info"
            :closable="false"
            show-icon
          >
            <template #title>
              <div class="finance-payment-form__detail-alert-title">
                <span>当前状态：</span>
                <el-tag :type="currentStatusDescriptor.tagType" effect="light" size="small">
                  {{ currentStatusDescriptor.label }}
                </el-tag>
                <span>付款单号：{{ formData.no || '-' }}</span>
              </div>
            </template>
            <template #default>
              <div class="finance-payment-form__detail-alert-meta">
                <span>流程实例编号：{{ formData.processInstanceId || '未发起' }}</span>
                <el-button v-if="showProcessLink" link type="primary" @click="openProcessDetail">
                  查看审批流程
                </el-button>
              </div>
              <div v-if="formData.voidReason" class="finance-payment-form__detail-alert-meta">
                <span>作废原因：{{ formData.voidReason }}</span>
              </div>
              <div v-if="formData.voidTime || formData.voidByName || formData.voidBy" class="finance-payment-form__detail-alert-meta">
                <span>作废时间：{{ formatDateTimeValue(formData.voidTime) }}</span>
                <span>作废操作人：{{ formData.voidByName || formData.voidBy || '未记录' }}</span>
              </div>
            </template>
          </el-alert>
          <div v-if="showProcessLink && !showDetailOverview" class="finance-payment-form__process-link">
            <el-button link type="primary" @click="openProcessDetail">查看审批流程</el-button>
          </div>
          <div class="finance-payment-form__grid">
            <el-form-item label="付款单号" prop="no">
              <el-input v-model="formData.no" disabled placeholder="保存时自动生成" />
            </el-form-item>
            <el-form-item label="付款时间" prop="paymentTime">
              <el-date-picker
                v-model="formData.paymentTime"
                type="date"
                value-format="x"
                placeholder="请选择付款时间"
                class="!w-full"
              />
            </el-form-item>
            <el-form-item label="供应商" prop="supplierId">
              <el-select
                v-model="formData.supplierId"
                clearable
                filterable
                placeholder="请选择供应商"
                class="!w-full"
                :disabled="disabled || supplierLocked"
              >
                <el-option
                  v-for="item in supplierList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="财务人员" prop="financeUserId">
              <el-select
                v-model="formData.financeUserId"
                clearable
                filterable
                placeholder="请选择财务人员"
                class="!w-full"
              >
                <el-option
                  v-for="item in userList"
                  :key="item.id"
                  :label="item.nickname"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="付款账户" prop="accountId">
              <el-select
                v-model="formData.accountId"
                clearable
                filterable
                placeholder="请选择付款账户"
                class="!w-full"
              >
                <el-option
                  v-for="item in accountList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="优惠金额" prop="discountPrice">
              <el-input-number
                v-model="formData.discountPrice"
                controls-position="right"
                :precision="2"
                :min="0"
                :max="Math.max(Number(formData.totalPrice || 0), 0)"
                class="!w-full"
                placeholder="请输入优惠金额"
              />
            </el-form-item>
            <el-form-item label="合计付款" prop="totalPrice">
              <el-input
                v-model="formData.totalPrice"
                disabled
                :formatter="erpPriceInputFormatter"
                class="font-mono"
              />
            </el-form-item>
            <el-form-item label="实际付款" prop="paymentPrice">
              <el-input
                v-model="formData.paymentPrice"
                disabled
                :formatter="erpPriceInputFormatter"
                class="font-mono"
              />
            </el-form-item>
          </div>
          <div v-if="supplierLocked && !disabled" class="finance-payment-form__hint">
            已添加应付记录后，供应商不可修改。
          </div>
          <el-form-item label="备注" prop="remark" class="finance-payment-form__remark">
            <el-input
              v-model="formData.remark"
              type="textarea"
              :rows="2"
              placeholder="请输入备注"
            />
          </el-form-item>
        </div>

        <div class="finance-payment-form__panel">
          <div class="finance-payment-form__section-title">应付记录</div>
          <FinancePaymentItemForm
            ref="itemFormRef"
            :supplier-id="formData.supplierId"
            :supplier-name="currentSupplierName"
            :items="formData.items"
            :disabled="disabled"
          />
        </div>

        <div v-if="showDetailOverview" class="finance-payment-form__panel">
          <div class="finance-payment-form__section-title">关联应付台账</div>
          <div v-if="traceLoading" v-loading="true" class="finance-payment-form__trace-loading"></div>
          <div v-else-if="traceLoadFailed" class="finance-payment-form__trace-state">
            <span>{{ traceErrorMessage || '应付台账加载失败' }}</span>
            <el-button type="primary" link @click="retryLoadTrace">重试</el-button>
          </div>
          <div v-else-if="traceStatements.length" class="finance-payment-form__trace-list">
            <div
              v-for="statement in traceStatements"
              :key="statement.id"
              class="finance-payment-form__trace-card"
            >
              <div class="finance-payment-form__trace-head">
                <div class="finance-payment-form__trace-title">
                  <strong>{{ statement.statementNo || '-' }}</strong>
                  <span>{{ statement.bizNo || '-' }}</span>
                </div>
                <el-tag size="small" effect="light" type="info">
                  {{ getStatementStatusLabel(statement.status) }}
                </el-tag>
              </div>
              <div class="finance-payment-form__trace-grid">
                <div>
                  <span>应付金额</span>
                  <strong>{{ erpPriceInputFormatter(statement.amount) }}</strong>
                </div>
                <div>
                  <span>已核销</span>
                  <strong>{{ erpPriceInputFormatter(statement.paidAmount) }}</strong>
                </div>
                <div>
                  <span>剩余金额</span>
                  <strong class="finance-payment-form__trace-amount">
                    {{ erpPriceInputFormatter(statement.remainAmount) }}
                  </strong>
                </div>
                <div>
                  <span>供应商</span>
                  <strong>{{ statement.supplierName || '-' }}</strong>
                </div>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无关联应付台账" />
        </div>
      </el-form>
    </div>
    <template #footer>
      <el-button
        v-if="!disabled"
        type="primary"
        :loading="submitLoading"
        :disabled="submitDisabled"
        @click="submitForm"
      >
        确定
      </el-button>
      <el-button @click="dialogVisible = false">取消</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import type { FormInstance, FormRules } from 'element-plus'
import {
  FinancePaymentApi,
  FinancePaymentItemVO,
  FinancePaymentSaveReqVO,
  FinancePaymentTraceVO,
  FinancePaymentVO
} from '@/api/erp/finance/payment'
import type { ApStatementVO } from '@/api/erp/finance/apar'
import FinancePaymentItemForm from './components/FinancePaymentItemForm.vue'
import { SupplierApi, SupplierVO } from '@/api/erp/purchase/supplier'
import { erpPriceInputFormatter } from '@/utils'
import { AccountApi, AccountVO } from '@/api/erp/finance/account'
import { getSimpleUserList, type SimpleUserVO } from '@/api/system/user'
import { getFinancePaymentStatusDescriptor } from './financePaymentStatus.helpers'
import { ERP_AP_STATEMENT_STATUS_OPTIONS, } from '@/api/erp/finance/apar'
import { formatDate } from '@/utils/formatTime'

defineOptions({ name: 'FinancePaymentForm' })

type FormType = 'create' | 'update' | 'detail'

const FINANCE_PAYMENT_STATUS = {
  PROCESS: 10,
  APPROVE: 20,
  REJECT: 30
} as const

interface FinancePaymentFormData {
  id?: number
  no?: string
  status?: number
  processInstanceId?: string
  supplierId?: number
  supplierName?: string
  accountId?: number
  financeUserId?: number
  paymentTime?: Date | string | number
  remark?: string
  totalPrice: number
  discountPrice: number
  paymentPrice: number
  items: FinancePaymentItemVO[]
  voidReason?: string
  voidTime?: Date | string | number
  voidBy?: number
  voidByName?: string
}

const createEmptyFormData = (): FinancePaymentFormData => ({
  id: undefined,
  no: undefined,
  status: undefined,
  processInstanceId: undefined,
  supplierId: undefined,
  supplierName: undefined,
  accountId: undefined,
  financeUserId: undefined,
  paymentTime: undefined,
  remark: '',
  totalPrice: 0,
  discountPrice: 0,
  paymentPrice: 0,
  items: []
})

const message = useMessage()
const { push } = useRouter()

const dialogVisible = ref(false)
const dialogTitle = ref('新增付款单')
const dialogLoading = ref(false)
const submitLoading = ref(false)
const traceLoading = ref(false)
const traceErrorMessage = ref('')
const traceStatements = ref<ApStatementVO[]>([])
const formType = ref<FormType>('create')
const formData = ref<FinancePaymentFormData>(createEmptyFormData())
const formRef = ref<FormInstance>()
const itemFormRef = ref<InstanceType<typeof FinancePaymentItemForm>>()

const supplierList = ref<SupplierVO[]>([])
const accountList = ref<AccountVO[]>([])
const userList = ref<SimpleUserVO[]>([])

const showDetailOverview = computed(() => formType.value === 'detail' && !!formData.value.id)
const showApprovalRunningReminder = computed(
  () =>
    formType.value === 'update' &&
    !!formData.value.id &&
    formData.value.status === FINANCE_PAYMENT_STATUS.PROCESS &&
    !!formData.value.processInstanceId
)
const showReadonlyReminder = computed(
  () => formType.value === 'update' && !!formData.value.id && formData.value.status === FINANCE_PAYMENT_STATUS.APPROVE
)
const showRejectedReminder = computed(
  () => formType.value === 'update' && !!formData.value.id && formData.value.status === FINANCE_PAYMENT_STATUS.REJECT
)
const showProcessLink = computed(() => !!formData.value.processInstanceId)
const currentStatusDescriptor = computed(() =>
  getFinancePaymentStatusDescriptor({
    status: formData.value.status,
    processInstanceId: formData.value.processInstanceId
  })
)
const canEditForm = computed(
  () =>
    formType.value === 'create' ||
    formData.value.status === FINANCE_PAYMENT_STATUS.REJECT ||
    (formData.value.status === FINANCE_PAYMENT_STATUS.PROCESS && !formData.value.processInstanceId)
)
const disabled = computed(() => formType.value === 'detail' || (formType.value !== 'create' && !canEditForm.value))
const supplierLocked = computed(() => !disabled.value && formData.value.items.length > 0)
const submitDisabled = computed(() => disabled.value || dialogLoading.value || submitLoading.value)
const traceLoadFailed = computed(() => !!traceErrorMessage.value && !traceLoading.value)
const currentSupplierName = computed(() => {
  const supplier = supplierList.value.find((item) => item.id === formData.value.supplierId)
  return supplier?.name || formData.value.supplierName || ''
})
const getStatementStatusLabel = (value?: number) =>
  ERP_AP_STATEMENT_STATUS_OPTIONS.find((item) => item.value === value)?.label || '-'
const formatDateTimeValue = (value?: string | number | Date) =>
  value ? formatDate(new Date(value), 'YYYY-MM-DD HH:mm:ss') : '未记录'

const validateDiscountPrice = (_rule: any, value: number, callback: (error?: Error) => void) => {
  const discountPrice = Number(value || 0)
  const totalPrice = Number(formData.value.totalPrice || 0)
  if (discountPrice < 0) {
    callback(new Error('优惠金额不能小于 0'))
    return
  }
  if (discountPrice > totalPrice) {
    callback(new Error('优惠金额不能大于合计付款'))
    return
  }
  callback()
}

const formRules: FormRules<FinancePaymentFormData> = {
  supplierId: [{ required: true, message: '请选择供应商', trigger: 'change' }],
  paymentTime: [{ required: true, message: '请选择付款时间', trigger: 'change' }],
  accountId: [{ required: true, message: '请选择付款账户', trigger: 'change' }],
  discountPrice: [{ validator: validateDiscountPrice, trigger: ['change', 'blur'] }]
}

const syncAmountFields = () => {
  const totalPrice = formData.value.items.reduce(
    (sum, item) => sum + Number(item.paymentPrice || 0),
    0
  )
  const discountPrice = Number(formData.value.discountPrice || 0)
  formData.value.totalPrice = Number(totalPrice.toFixed(2))
  formData.value.paymentPrice = Number(Math.max(totalPrice - discountPrice, 0).toFixed(2))
}

watch(
  () => formData.value.items,
  () => {
    syncAmountFields()
  },
  { deep: true, immediate: true }
)

watch(
  () => formData.value.discountPrice,
  () => {
    syncAmountFields()
  }
)

watch(
  () => formData.value.supplierId,
  (supplierId) => {
    const supplier = supplierList.value.find((item) => item.id === supplierId)
    if (supplier) {
      formData.value.supplierName = supplier.name
    }
  }
)

const normalizeItems = (items?: FinancePaymentItemVO[]) =>
  (items || []).map((item) => ({
    ...item,
    remainAmount:
      item.remainAmount ?? Number(item.totalPrice || 0) - Number(item.paidPrice || 0),
    paymentPrice: Number(item.paymentPrice || 0),
    paidPrice: Number(item.paidPrice || 0),
    totalPrice: Number(item.totalPrice || 0)
  }))

const normalizeFormData = (detail?: FinancePaymentVO): FinancePaymentFormData => ({
  id: detail?.id,
  no: detail?.no,
  status: detail?.status,
  processInstanceId: detail?.processInstanceId,
  supplierId: detail?.supplierId,
  supplierName: detail?.supplierName,
  accountId: detail?.accountId,
  financeUserId: detail?.financeUserId,
  paymentTime: detail?.paymentTime,
  remark: detail?.remark || '',
  totalPrice: Number(detail?.totalPrice || 0),
  discountPrice: Number(detail?.discountPrice || 0),
  paymentPrice: Number(detail?.paymentPrice || 0),
  items: normalizeItems(detail?.items),
  voidReason: detail?.voidReason,
  voidTime: detail?.voidTime,
  voidBy: detail?.voidBy,
  voidByName: detail?.voidByName
})

const buildSaveData = (): FinancePaymentSaveReqVO => ({
  id: formData.value.id,
  paymentTime: formData.value.paymentTime,
  financeUserId: formData.value.financeUserId,
  supplierId: formData.value.supplierId,
  accountId: formData.value.accountId,
  discountPrice: Number(formData.value.discountPrice || 0),
  remark: formData.value.remark,
  items: formData.value.items.map((item) => ({
    id: item.id,
    apStatementId: Number(item.apStatementId),
    bizType: Number(item.bizType),
    bizId: Number(item.bizId),
    paidPrice: Number(item.paidPrice || 0),
    paymentPrice: Number(item.paymentPrice || 0),
    remark: item.remark
  }))
})

const resetForm = async () => {
  formData.value = createEmptyFormData()
  traceStatements.value = []
  traceErrorMessage.value = ''
  traceLoading.value = false
  await nextTick()
  formRef.value?.clearValidate()
}

const applyTraceData = (trace?: FinancePaymentTraceVO) => {
  traceStatements.value = trace?.statements || []
}

const loadTrace = async (id: number) => {
  traceLoading.value = true
  traceErrorMessage.value = ''
  try {
    const trace = await FinancePaymentApi.getFinancePaymentTrace(id)
    applyTraceData(trace)
  } catch (error: any) {
    traceStatements.value = []
    traceErrorMessage.value = error?.message || '应付台账加载失败'
  } finally {
    traceLoading.value = false
  }
}

const loadOptions = async () => {
  const [suppliers, users, accounts] = await Promise.all([
    SupplierApi.getSupplierSimpleList(),
    getSimpleUserList(),
    AccountApi.getAccountSimpleList()
  ])
  supplierList.value = suppliers
  userList.value = users
  accountList.value = accounts
}

const applyDefaultAccount = () => {
  if (formData.value.accountId) {
    return
  }
  const defaultAccount = accountList.value.find((item) => item.defaultStatus)
  if (defaultAccount) {
    formData.value.accountId = defaultAccount.id
  }
}

const titleMap: Record<FormType, string> = {
  create: '新增付款单',
  update: '编辑付款单',
  detail: '付款单详情'
}

const open = async (type: FormType, id?: number) => {
  dialogVisible.value = true
  formType.value = type
  dialogTitle.value = titleMap[type]
  await resetForm()
  dialogLoading.value = true
  try {
    const [_, detail] = await Promise.all([
      loadOptions(),
      id ? FinancePaymentApi.getFinancePayment(id) : Promise.resolve(undefined)
    ])
    if (detail) {
      formData.value = normalizeFormData(detail)
    } else {
      applyDefaultAccount()
    }
    syncAmountFields()
    if (type === 'detail' && id) {
      await loadTrace(id)
    }
    await nextTick()
    formRef.value?.clearValidate()
  } finally {
    dialogLoading.value = false
  }
}

const retryLoadTrace = async () => {
  if (!formData.value.id) {
    return
  }
  await loadTrace(formData.value.id)
}

const openProcessDetail = () => {
  if (!formData.value.processInstanceId) {
    return
  }
  push({
    name: 'BpmProcessInstanceDetail',
    query: {
      id: formData.value.processInstanceId
    }
  })
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
  await itemFormRef.value?.validate()
  submitLoading.value = true
  try {
    const payload = buildSaveData()
    if (formType.value === 'create') {
      await FinancePaymentApi.createFinancePayment(payload)
      message.success('新增成功')
    } else {
      await FinancePaymentApi.updateFinancePayment(payload)
      message.success('修改成功')
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    submitLoading.value = false
  }
}
</script>

<style scoped>
.finance-payment-form {
  background: #f8fafc;
}

.finance-payment-form__panel {
  padding: 16px 18px;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  background: #ffffff;
  box-shadow: 0 8px 22px rgba(148, 163, 184, 0.08);
}

.finance-payment-form__panel + .finance-payment-form__panel {
  margin-top: 16px;
}

.finance-payment-form__detail-alert-title {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px 12px;
}

.finance-payment-form__detail-alert-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 8px 12px;
}

.finance-payment-form__process-link {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
}

.finance-payment-form__grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 0 16px;
}

.finance-payment-form__hint {
  margin: 4px 0 14px;
  color: #64748b;
  font-size: 12px;
}

.finance-payment-form__remark :deep(.el-form-item__content) {
  display: block;
}

.finance-payment-form__section-title {
  margin-bottom: 12px;
  color: #0f172a;
  font-size: 15px;
  font-weight: 600;
}

.finance-payment-form__trace-loading {
  min-height: 120px;
}

.finance-payment-form__trace-state {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: #475569;
}

.finance-payment-form__trace-list {
  display: grid;
  gap: 12px;
}

.finance-payment-form__trace-card {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  background: #f8fafc;
  padding: 14px 16px;
}

.finance-payment-form__trace-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.finance-payment-form__trace-title {
  display: flex;
  flex-direction: column;
  gap: 4px;
  color: #0f172a;
}

.finance-payment-form__trace-title span {
  color: #64748b;
  font-size: 12px;
}

.finance-payment-form__trace-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.finance-payment-form__trace-grid div {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.finance-payment-form__trace-grid span {
  color: #64748b;
  font-size: 12px;
}

.finance-payment-form__trace-grid strong {
  color: #0f172a;
  font-size: 14px;
  font-weight: 600;
}

.finance-payment-form__trace-amount {
  color: #0f766e;
}

@media (max-width: 1280px) {
  .finance-payment-form__grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .finance-payment-form__trace-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .finance-payment-form__grid {
    grid-template-columns: 1fr;
  }

  .finance-payment-form__trace-grid {
    grid-template-columns: 1fr;
  }
}
</style>
