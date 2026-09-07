<template>
  <Dialog
    :title="dialogTitle"
    v-model="dialogVisible"
    :width="dialogWidth"
    scroll
    :max-height="dialogMaxHeight"
  >
    <div class="purchase-order-form">
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
        v-loading="formDataLoading"
        :disabled="disabled"
      >
        <el-alert
          v-if="showRejectReminder"
          class="mb-16px"
          type="warning"
          :closable="false"
          show-icon
          :title="`最近驳回原因：${formData.lastRejectReason}`"
          :description="formData.lastRejectTime ? `驳回时间：${formatDateTime(formData.lastRejectTime)}` : ''"
        />
        <el-alert
          v-if="showApprovalRunningReminder"
          class="mb-16px"
          type="info"
          :closable="false"
          show-icon
          title="当前采购订单正在审批中，暂不可编辑。如需调整，请先回到列表页撤回审批。"
        />
        <el-alert
          v-else-if="showReadonlyReminder"
          class="mb-16px"
          type="success"
          :closable="false"
          show-icon
          title="当前采购订单已审批完成，仅支持查看。"
        />
        <div v-if="showProcessLink" class="mb-16px text-right">
          <el-button link type="primary" @click="openProcessDetail">查看审批流程</el-button>
        </div>

        <el-alert v-if="showDetailOverview" class="mb-16px" type="info" :closable="false" show-icon>
          <template #title>
            <div class="detail-alert-title">
              <span>当前状态：{{ resolveStatusLabel(formData.status) }}</span>
              <span>订单号：{{ formData.no || '-' }}</span>
            </div>
          </template>
          <template #default>
            <span>
              已入库 {{ formatCount(formData.inCount) }} / {{ formatCount(formData.totalCount) }}，
              已退货 {{ formatCount(formData.returnCount) }}
            </span>
          </template>
        </el-alert>

        <ContentWrap title="基础信息" class="form-block">
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12" :lg="8">
              <el-form-item label="订单单号" prop="no">
                <el-input v-model="formData.no" disabled placeholder="保存时自动生成" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="8">
              <el-form-item label="订单时间" prop="orderTime">
                <el-date-picker
                  v-model="formData.orderTime"
                  type="date"
                  value-format="x"
                  placeholder="选择订单时间"
                  class="!w-1/1"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="8">
              <el-form-item label="供应商" prop="supplierId">
                <el-select
                  v-model="formData.supplierId"
                  clearable
                  filterable
                  placeholder="请选择供应商"
                  class="!w-1/1"
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
            <el-col v-if="showDetailOverview" :xs="24" :sm="12" :lg="8">
              <el-form-item label="创建人">
                <el-input :model-value="formData.creatorName || '-'" disabled />
              </el-form-item>
            </el-col>
            <el-col v-if="showDetailOverview" :xs="24" :sm="12" :lg="8">
              <el-form-item label="业务归属">
                <el-input :model-value="formData.businessOwnerName || formData.creatorName || '-'" disabled />
              </el-form-item>
            </el-col>
            <el-col v-if="showDetailOverview" :xs="24" :sm="12" :lg="8">
              <el-form-item label="来源类型">
                <el-input :model-value="resolveSourceTypeLabel(formData.sourceType)" disabled />
              </el-form-item>
            </el-col>
            <el-col v-if="showDetailOverview" :xs="24" :sm="12" :lg="8">
              <el-form-item label="来源销售单">
                <el-input :model-value="formData.sourceOrderNos || '-'" disabled />
              </el-form-item>
            </el-col>
            <el-col v-if="showDetailOverview" :xs="24" :sm="12" :lg="8">
              <el-form-item label="创建时间">
                <el-input :model-value="formatDateTime(formData.createTime)" disabled />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="showDetailOverview ? 8 : 16">
              <el-form-item label="附件" prop="fileUrl">
                <UploadFile :is-show-tip="false" v-model="formData.fileUrl" :limit="1" />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="备注" prop="remark">
                <el-input
                  v-model="formData.remark"
                  type="textarea"
                  :rows="2"
                  placeholder="请输入备注"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </ContentWrap>

        <ContentWrap v-if="showDetailOverview" title="采购概览" class="form-block">
          <div class="overview-grid">
            <div class="metric-card">
              <div class="metric-label">审批状态</div>
              <div class="metric-value">
                <dict-tag :type="DICT_TYPE.ERP_AUDIT_STATUS" :value="formData.status" />
              </div>
              <div class="metric-note">供应商：{{ formData.supplierName || '-' }}</div>
            </div>
            <div class="metric-card">
              <div class="metric-label">入库进度</div>
              <div class="metric-value">
                {{ formatCount(formData.inCount) }} / {{ formatCount(formData.totalCount) }}
              </div>
              <el-progress
                :stroke-width="8"
                :percentage="getProgressPercent(formData.inCount, formData.totalCount)"
                :status="resolveProgressStatus(formData.inCount, formData.totalCount)"
              />
            </div>
            <div class="metric-card">
              <div class="metric-label">退货进度</div>
              <div class="metric-value">
                {{ formatCount(formData.returnCount) }} / {{ formatCount(formData.inCount || formData.totalCount) }}
              </div>
              <el-progress
                :stroke-width="8"
                :percentage="getReturnPercent(formData.returnCount, formData.inCount, formData.totalCount)"
                :status="resolveProgressStatus(formData.returnCount, formData.inCount || formData.totalCount)"
              />
            </div>
            <div class="metric-card">
              <div class="metric-label">含税金额</div>
              <div class="metric-value">{{ formatPrice(formData.totalPrice) }}</div>
              <div class="metric-note">订金：{{ formatPrice(formData.depositPrice) }}</div>
            </div>
            <div class="metric-card">
              <div class="metric-label">货款合计</div>
              <div class="metric-value">{{ formatPrice(formData.totalProductPrice) }}</div>
              <div class="metric-note">税额：{{ formatPrice(formData.totalTaxPrice) }}</div>
            </div>
            <div class="metric-card">
              <div class="metric-label">结算账户</div>
              <div class="metric-value">{{ resolveAccountName(formData.accountId) }}</div>
              <div class="metric-note">优惠率：{{ formatPercent(formData.discountPercent) }}</div>
            </div>
          </div>
        </ContentWrap>

        <ContentWrap title="订单产品" class="form-block">
          <el-tabs v-model="subTabsName" class="-mt-15px -mb-10px">
            <el-tab-pane label="订单产品清单" name="item">
              <PurchaseOrderItemForm ref="itemFormRef" :items="formData.items" :disabled="disabled" />
            </el-tab-pane>
          </el-tabs>
        </ContentWrap>

        <ContentWrap title="金额与结算" class="form-block">
          <div class="settlement-layout">
            <div class="settlement-form">
              <el-row :gutter="20">
                <el-col :xs="24" :md="12">
                  <el-form-item label="优惠率（%）" prop="discountPercent">
                    <el-input-number
                      v-model="formData.discountPercent"
                      controls-position="right"
                      :min="0"
                      :precision="2"
                      placeholder="请输入优惠率"
                      class="!w-1/1"
                    />
                  </el-form-item>
                </el-col>
                <el-col :xs="24" :md="12">
                  <el-form-item label="结算账户" prop="accountId">
                    <el-select
                      v-model="formData.accountId"
                      clearable
                      filterable
                      placeholder="请选择结算账户"
                      class="!w-1/1"
                    >
                      <el-option
                        v-for="item in accountList"
                        :key="item.id"
                        :label="item.name"
                        :value="item.id"
                      />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :xs="24" :md="12">
                  <el-form-item label="支付订金" prop="depositPrice">
                    <el-input-number
                      v-model="formData.depositPrice"
                      controls-position="right"
                      :min="0"
                      :precision="2"
                      placeholder="请输入支付订金"
                      class="!w-1/1"
                    />
                  </el-form-item>
                </el-col>
                <el-col v-if="showDetailOverview" :xs="24" :md="12">
                  <div class="settlement-field">
                    <div class="settlement-field__label">总数量</div>
                    <div class="settlement-field__value">{{ formatCount(formData.totalCount) }}</div>
                  </div>
                </el-col>
              </el-row>
            </div>

            <div class="settlement-summary">
              <div class="summary-card">
                <div class="summary-title">结算汇总</div>
                <div class="summary-row">
                  <span>货款合计</span>
                  <strong>{{ formatPrice(formData.totalProductPrice) }}</strong>
                </div>
                <div class="summary-row">
                  <span>税额合计</span>
                  <strong>{{ formatPrice(formData.totalTaxPrice) }}</strong>
                </div>
                <div class="summary-row">
                  <span>优惠金额</span>
                  <strong>{{ formatPrice(formData.discountPrice) }}</strong>
                </div>
                <div class="summary-row is-total">
                  <span>优惠后金额</span>
                  <strong>{{ formatPrice(formData.totalPrice) }}</strong>
                </div>
              </div>
            </div>
          </div>
        </ContentWrap>

        <ContentWrap v-if="showAuditHistory" title="审批历史" class="mt-16px">
          <el-empty v-if="displayAuditLogs.length === 0" description="暂无审批历史" />
          <el-timeline v-else>
            <el-timeline-item
              v-for="(item, index) in displayAuditLogs"
              :key="index"
              :timestamp="formatAuditLogTime(item)"
              placement="top"
            >
              <div class="text-13px leading-22px">
                <div class="mb-4px">
                  <span class="text-gray-500">动作：</span>
                  <el-tag size="small" :type="resolveAuditTagType(item.actionType)">
                    {{ formatAuditAction(item.actionType) }}
                  </el-tag>
                </div>
                <div class="mb-4px">
                  <span class="text-gray-500">操作人：</span>
                  <span>{{ formatAuditLogUser(item) }}</span>
                </div>
                <div v-if="item.taskName" class="mb-4px">
                  <span class="text-gray-500">节点：</span>
                  <span>{{ item.taskName }}</span>
                </div>
                <div v-if="item.beforeStatus != null || item.afterStatus != null" class="mb-4px">
                  <span class="text-gray-500">状态变化：</span>
                  <span>{{ formatAuditStatus(item.beforeStatus) }} -> {{ formatAuditStatus(item.afterStatus) }}</span>
                </div>
                <div v-if="item.reason">
                  <span class="text-gray-500">说明：</span>
                  <span>{{ item.reason }}</span>
                </div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </ContentWrap>
      </el-form>
    </div>
    <template #footer>
      <el-button
        v-if="!disabled"
        type="primary"
        :loading="formSubmitting"
        :disabled="!canSubmitForm"
        @click="submitForm"
      >
        确定
      </el-button>
      <el-button :disabled="formSubmitting" @click="dialogVisible = false">取消</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { normalizeItemsForUnitInput } from '@/utils/erpUnitConversion'
import {
  PurchaseOrderApi,
  PurchaseOrderAuditLogVO,
  PurchaseOrderItemVO,
  PurchaseOrderRejectLogVO,
  PurchaseOrderVO
} from '@/api/erp/purchase/order'
import PurchaseOrderItemForm from './components/PurchaseOrderItemForm.vue'
import { SupplierApi, SupplierVO } from '@/api/erp/purchase/supplier'
import { AccountApi, AccountVO } from '@/api/erp/finance/account'
import { formatDate } from '@/utils/formatTime'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { erpCountInputFormatter, erpPriceInputFormatter, erpPriceMultiply } from '@/utils'
import * as UserApi from '@/api/system/user'
import { resolvePurchaseOrderApprovalLogs } from './auditLogUtils'

defineOptions({ name: 'PurchaseOrderForm' })

const PURCHASE_ORDER_STATUS = {
  PROCESS: 10,
  APPROVE: 20,
  REJECT: 30
} as const

type PurchaseOrderFormData = PurchaseOrderVO & {
  id?: number
  no?: string
  supplierId?: number
  supplierName?: string
  accountId?: number
  orderTime?: Date | number | string
  remark?: string
  fileUrl?: string
  discountPercent?: number
  discountPrice?: number
  totalProductPrice?: number
  totalTaxPrice?: number
  totalPrice?: number
  depositPrice?: number
  creator?: string
  creatorName?: string
  businessOwnerName?: string
  sourceOrderNos?: string
  sourceType?: string
  createTime?: Date | string | number
  totalCount?: number
  inCount?: number
  returnCount?: number
  productNames?: string
  status?: number
  processInstanceId?: string
  lastRejectReason?: string
  lastRejectTime?: Date
  lastRejectUserId?: number
  rejectLogs?: PurchaseOrderRejectLogVO[]
  operationLogs?: PurchaseOrderAuditLogVO[]
  approvalLogs?: PurchaseOrderAuditLogVO[]
  auditLogs?: PurchaseOrderAuditLogVO[]
  items: PurchaseOrderItemVO[]
}

const { t } = useI18n()
const message = useMessage()
const { push } = useRouter()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const dialogWidth = 'min(1280px, calc(100vw - 32px))'
const dialogMaxHeight = 'calc(100vh - 140px)'
const formDataLoading = ref(false)
const formSubmitting = ref(false)
const formType = ref('')
const formRef = ref()
const supplierList = ref<SupplierVO[]>([])
const accountList = ref<AccountVO[]>([])
const userList = ref<UserApi.SimpleUserVO[]>([])
const subTabsName = ref('item')
const itemFormRef = ref()

const createFormData = (): PurchaseOrderFormData => ({
  id: undefined,
  no: undefined,
  status: PURCHASE_ORDER_STATUS.PROCESS,
  supplierId: undefined,
  supplierName: undefined,
  accountId: undefined,
  orderTime: undefined,
  totalCount: 0,
  totalPrice: 0,
  totalProductPrice: 0,
  totalTaxPrice: 0,
  discountPercent: 0,
  discountPrice: 0,
  depositPrice: 0,
  fileUrl: '',
  remark: undefined,
  creator: undefined,
  creatorName: undefined,
  businessOwnerName: undefined,
  sourceOrderNos: undefined,
  sourceType: 'MANUAL',
  createTime: undefined,
  inCount: 0,
  returnCount: 0,
  productNames: undefined,
  processInstanceId: undefined,
  lastRejectReason: undefined,
  lastRejectTime: undefined,
  lastRejectUserId: undefined,
  rejectLogs: [],
  operationLogs: [],
  approvalLogs: [],
  auditLogs: [],
  items: []
})

const formData = ref<PurchaseOrderFormData>(createFormData())

const formRules = reactive({
  supplierId: [{ required: true, message: '供应商不能为空', trigger: 'blur' }],
  orderTime: [{ required: true, message: '订单时间不能为空', trigger: 'blur' }]
})

const editable = computed(() => {
  if (formType.value === 'create') {
    return true
  }
  return (
    formData.value.status === PURCHASE_ORDER_STATUS.REJECT ||
    (formData.value.status === PURCHASE_ORDER_STATUS.PROCESS && !formData.value.processInstanceId)
  )
})
const disabled = computed(() => formType.value === 'detail' || (formType.value !== 'create' && !editable.value))
const showDetailOverview = computed(() => formType.value === 'detail')
const showRejectReminder = computed(() => formType.value === 'update' && !!formData.value.lastRejectReason)
const showApprovalRunningReminder = computed(
  () => formType.value === 'update' && formData.value.status === PURCHASE_ORDER_STATUS.PROCESS && !!formData.value.processInstanceId
)
const showReadonlyReminder = computed(
  () => formType.value === 'update' && formData.value.status === PURCHASE_ORDER_STATUS.APPROVE
)
const showProcessLink = computed(() => !!formData.value.processInstanceId)
const showAuditHistory = computed(() => formType.value === 'detail')
const canSubmitForm = computed(
  () => !disabled.value && !formDataLoading.value && !formSubmitting.value
)

const normalizeNumber = (value?: number | string | null) => Number(value || 0)

const formatDateTime = (value?: Date | string | number) => {
  return value ? formatDate(value as Date, 'YYYY-MM-DD') : '-'
}

const formatCount = (value?: number | string | null) => {
  return erpCountInputFormatter(normalizeNumber(value))
}

const formatPrice = (value?: number | string | null) => {
  return erpPriceInputFormatter(normalizeNumber(value))
}

const formatPercent = (value?: number | string | null) => {
  return `${normalizeNumber(value).toFixed(2)}%`
}

const resolveSourceTypeLabel = (sourceType?: string) => {
  if (sourceType === 'MRP') {
    return 'MRP 转单'
  }
  if (sourceType === 'MANUAL') {
    return '手工下单'
  }
  return sourceType || '-'
}

const getProgressPercent = (current?: number | string | null, totalValue?: number | string | null) => {
  const base = normalizeNumber(totalValue)
  if (base <= 0) {
    return 0
  }
  return Math.min(100, Math.round((normalizeNumber(current) / base) * 100))
}

const getReturnPercent = (
  current?: number | string | null,
  inbound?: number | string | null,
  totalValue?: number | string | null
) => {
  const base = normalizeNumber(inbound) || normalizeNumber(totalValue)
  return getProgressPercent(current, base)
}

const resolveProgressStatus = (
  current?: number | string | null,
  totalValue?: number | string | null
): '' | 'success' | 'warning' => {
  const percentage = getProgressPercent(current, totalValue)
  if (percentage >= 100) {
    return 'success'
  }
  if (percentage > 0) {
    return 'warning'
  }
  return ''
}

const resolveStatusLabel = (status?: number) => {
  if (status == null) {
    return '-'
  }
  const dict = getIntDictOptions(DICT_TYPE.ERP_AUDIT_STATUS).find(
    (item) => Number(item.value) === Number(status)
  )
  return dict?.label || String(status)
}

const resolveAccountName = (accountId?: number) => {
  if (!accountId) {
    return '-'
  }
  return accountList.value.find((item) => item.id === accountId)?.name || `账户 #${accountId}`
}

watch(
  () => formData.value,
  (val) => {
    if (!val) {
      return
    }
    const totalProductPrice = val.items.reduce(
      (prev, curr) => prev + normalizeNumber(curr.totalProductPrice),
      0
    )
    const totalTaxPrice = val.items.reduce((prev, curr) => prev + normalizeNumber(curr.taxPrice), 0)
    const beforeDiscountPrice = totalProductPrice + totalTaxPrice
    const discountPrice =
      val.discountPercent != null
        ? erpPriceMultiply(beforeDiscountPrice, normalizeNumber(val.discountPercent) / 100.0)
        : 0

    formData.value.totalCount = val.items.reduce((prev, curr) => prev + normalizeNumber(curr.count), 0)
    formData.value.totalProductPrice = totalProductPrice
    formData.value.totalTaxPrice = totalTaxPrice
    formData.value.discountPrice = discountPrice || 0
    formData.value.totalPrice = beforeDiscountPrice - normalizeNumber(discountPrice)
  },
  { deep: true }
)

const actionTextMap: Record<string, string> = {
  CREATE: '创建',
  UPDATE: '修改',
  DELETE: '删除',
  APPROVE: '审批通过',
  REJECT: '驳回',
  RESUBMIT: '重新提交',
  CANCEL: '撤回审批',
  REVERSE_APPROVE: '反审核'
}

const displayAuditLogs = computed(() => resolvePurchaseOrderApprovalLogs(formData.value))

const formatAuditLogTime = (item: PurchaseOrderAuditLogVO) => {
  return item.createTime ? formatDate(item.createTime as Date, 'YYYY-MM-DD') : '-'
}

const formatAuditLogUser = (item: PurchaseOrderAuditLogVO) => {
  return item.operatorNickname || item.operatorName || '未知'
}

const formatAuditAction = (actionType?: string) => {
  return (actionType && actionTextMap[actionType]) || actionType || '-'
}

const resolveAuditTagType = (actionType?: string) => {
  if (actionType === 'APPROVE') {
    return 'success'
  }
  if (actionType === 'REJECT') {
    return 'danger'
  }
  if (actionType === 'RESUBMIT' || actionType === 'CANCEL') {
    return 'warning'
  }
  return 'info'
}

const formatAuditStatus = (status?: number) => {
  if (status == null) {
    return '-'
  }
  const dict = getIntDictOptions(DICT_TYPE.ERP_AUDIT_STATUS).find((item) => Number(item.value) === status)
  return dict?.label || String(status)
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

const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  formDataLoading.value = true
  try {
    if (id) {
      formData.value = await PurchaseOrderApi.getPurchaseOrder(id)
      await normalizeItemsForUnitInput(formData.value.items)
    }

    const [suppliers, users, accounts] = await Promise.all([
      SupplierApi.getSupplierSimpleList(),
      UserApi.getSimpleUserList(),
      AccountApi.getAccountSimpleList()
    ])
    supplierList.value = suppliers
    userList.value = users
    accountList.value = accounts

    const defaultAccount = accountList.value.find((item) => item.defaultStatus)
    if (formType.value === 'create' && defaultAccount && !formData.value.accountId) {
      formData.value.accountId = defaultAccount.id
    }
  } finally {
    formDataLoading.value = false
  }
}
defineExpose({ open })

const emit = defineEmits(['success'])

const sanitizeOrderItemForSubmit = (item: PurchaseOrderItemVO & Record<string, any>): PurchaseOrderItemVO => {
  const { pricingSourceType, pricingSourceText, pricingHint, ...rest } = item
  return rest as PurchaseOrderItemVO
}

const buildSubmitPayload = (): PurchaseOrderVO => {
  return {
    ...(formData.value as PurchaseOrderVO),
    items: (formData.value.items || []).map((item) => sanitizeOrderItemForSubmit(item as PurchaseOrderItemVO & Record<string, any>))
  }
}

const submitForm = async () => {
  await formRef.value.validate()
  await itemFormRef.value.validate()
  formSubmitting.value = true
  try {
    const data = buildSubmitPayload()
    if (formType.value === 'create') {
      await PurchaseOrderApi.createPurchaseOrder(data)
      message.success(t('common.createSuccess'))
    } else {
      await PurchaseOrderApi.updatePurchaseOrder(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formSubmitting.value = false
  }
}

const resetForm = () => {
  formData.value = createFormData()
  subTabsName.value = 'item'
  formRef.value?.clearValidate()
  itemFormRef.value?.clearValidate?.()
}

watch(dialogVisible, (visible) => {
  if (visible) {
    return
  }
  resetForm()
})
</script>

<style scoped lang="scss">
.purchase-order-form {
  :deep(.el-alert__content) {
    width: 100%;
  }
}

.form-block {
  margin-bottom: 16px;
}

.settlement-field {
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-height: 76px;
  padding: 11px 14px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 12px;
  background: linear-gradient(180deg, var(--el-fill-color-light) 0%, #fff 100%);
}

.settlement-field__label {
  color: var(--el-text-color-secondary);
  font-size: 12px;
  line-height: 18px;
}

.settlement-field__value {
  margin-top: 6px;
  color: var(--el-text-color-primary);
  font-size: 18px;
  font-weight: 600;
  line-height: 28px;
  font-variant-numeric: tabular-nums;
}

.detail-alert-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  width: 100%;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 12px;
}

.metric-card {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-height: 124px;
  padding: 16px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 12px;
  background: linear-gradient(180deg, var(--el-fill-color-light) 0%, #fff 100%);
}

.metric-label {
  color: var(--el-text-color-secondary);
  font-size: 12px;
  line-height: 18px;
}

.metric-value {
  color: var(--el-text-color-primary);
  font-size: 18px;
  font-weight: 600;
  line-height: 28px;
}

.metric-note {
  color: var(--el-text-color-secondary);
  font-size: 12px;
  line-height: 18px;
}

.settlement-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.5fr) minmax(320px, 0.9fr);
  gap: 20px;
}

.settlement-form,
.summary-card {
  padding: 16px 18px;
  border-radius: 12px;
}

.settlement-form {
  border: 1px solid var(--el-border-color-lighter);
  background: linear-gradient(180deg, var(--el-fill-color-light) 0%, #fff 100%);
}

.summary-card {
  display: grid;
  gap: 12px;
  border: 1px solid var(--erp-slate-900);
  background: linear-gradient(180deg, var(--erp-slate-900) 0%, var(--erp-slate-800) 100%);
  color: var(--erp-slate-200);
}

.summary-title {
  color: var(--erp-slate-400);
  font-size: 13px;
  font-weight: 600;
  line-height: 20px;
}

.summary-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  color: var(--erp-slate-400);
  font-size: 14px;
  line-height: 22px;
  font-variant-numeric: tabular-nums;
}

.summary-row strong {
  color: var(--erp-surface-white);
  font-size: 16px;
  font-weight: 600;
}

.summary-row.is-total {
  margin-top: 4px;
  padding-top: 12px;
  border-top: 1px solid rgba(203, 213, 225, 0.24);
}

.summary-row.is-total strong {
  color: var(--erp-rose-400);
  font-size: 28px;
}

@media (max-width: 1200px) {
  .settlement-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 992px) {
  .detail-alert-title {
    flex-direction: column;
    align-items: flex-start;
  }

  .overview-grid {
    grid-template-columns: 1fr;
  }
}
</style>

