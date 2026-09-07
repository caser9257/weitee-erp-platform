<template>
  <div v-loading="pageLoading">
    <el-empty v-if="!pageLoading && !formData.id" description="销售订单不存在或已删除" />
    <template v-else>
      <ContentWrap class="mb-12px">
        <div class="flex flex-wrap items-start justify-between gap-12px">
          <div>
            <div class="text-18px font-600">销售订单重新审批</div>
            <div class="mt-6px text-12px text-[var(--el-text-color-secondary)]">
              {{ formData.no ? `订单单号：${formData.no}` : `订单编号：#${formData.id || '-'}` }}
            </div>
          </div>
          <div class="flex flex-wrap items-center gap-8px">
            <dict-tag
              v-if="formData.status !== undefined"
              :type="DICT_TYPE.ERP_AUDIT_STATUS"
              :value="formData.status"
            />
            <el-tag :type="resolveDeliveryReadyTagType(formData.deliveryReadyStatus)" size="small">
              {{ resolveDeliveryReadyLabel(formData.deliveryReadyStatus) }}
            </el-tag>
            <el-button v-if="showProcessLink" link type="primary" @click="openProcessDetail">
              查看审批流程
            </el-button>
          </div>
        </div>
      </ContentWrap>

      <ContentWrap>
        <el-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-width="100px"
          :disabled="disabled"
        >
          <el-alert
            v-if="showRejectReminder"
            class="mb-16px"
            type="warning"
            :closable="false"
            show-icon
            :title="`最近驳回原因：${formData.lastRejectReason}`"
            :description="
              formData.lastRejectTime
                ? `驳回时间：${formatDate(formData.lastRejectTime, 'YYYY-MM-DD')}`
                : ''
            "
          />
          <el-alert
            v-if="showApprovalRunningReminder"
            class="mb-16px"
            type="info"
            :closable="false"
            show-icon
            :title="approvalRunningReminderTitle"
          />
          <el-alert
            v-else-if="showReadonlyReminder"
            class="mb-16px"
            type="success"
            :closable="false"
            show-icon
            :title="readonlyReminderTitle"
          />
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12" :lg="8">
              <el-form-item label="订单单号" prop="no">
                <el-input disabled v-model="formData.no" placeholder="保存时自动生成" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="8">
              <el-form-item label="交付就绪">
                <el-tag
                  :type="resolveDeliveryReadyTagType(formData.deliveryReadyStatus)"
                  size="small"
                >
                  {{ resolveDeliveryReadyLabel(formData.deliveryReadyStatus) }}
                </el-tag>
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
              <el-form-item label="客户" prop="customerId">
                <el-select
                  v-model="formData.customerId"
                  clearable
                  filterable
                  placeholder="请选择客户"
                  class="!w-1/1"
                >
                  <el-option
                    v-for="item in customerList"
                    :key="item.id"
                    :label="item.name"
                    :value="item.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="8">
              <el-form-item label="项目" prop="projectId">
                <el-select
                  v-model="formData.projectId"
                  clearable
                  filterable
                  placeholder="请选择项目"
                  class="!w-1/1"
                >
                  <el-option
                    v-for="item in projectList"
                    :key="item.id"
                    :label="item.name"
                    :value="item.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="8">
              <el-form-item label="业务类型" prop="businessType">
                <el-select
                  v-model="formData.businessType"
                  clearable
                  placeholder="请选择业务类型"
                  class="!w-1/1"
                >
                  <el-option
                    v-for="item in businessTypeOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="8">
              <el-form-item label="交期" prop="deliveryDate">
                <el-date-picker
                  v-model="formData.deliveryDate"
                  type="date"
                  value-format="x"
                  placeholder="选择交期"
                  class="!w-1/1"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="8">
              <el-form-item label="结算类型" prop="settlementType">
                <el-select
                  v-model="formData.settlementType"
                  placeholder="请选择结算类型"
                  class="!w-1/1"
                >
                  <el-option
                    v-for="item in settlementTypeOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="8">
              <el-form-item label="销售人员" prop="saleUserId">
                <el-select
                  v-model="formData.saleUserId"
                  clearable
                  filterable
                  placeholder="请选择销售人员"
                  class="!w-1/1"
                >
                  <el-option
                    v-for="item in userList"
                    :key="item.id"
                    :label="item.nickname"
                    :value="item.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="24" :lg="16">
              <el-form-item label="备注" prop="remark">
                <el-input
                  type="textarea"
                  v-model="formData.remark"
                  :rows="2"
                  placeholder="请输入备注"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="24" :lg="8">
              <el-form-item label="附件" prop="fileUrl">
                <UploadFile :is-show-tip="false" v-model="formData.fileUrl" :limit="1" />
              </el-form-item>
            </el-col>
          </el-row>

          <ContentWrap>
            <el-tabs v-model="subTabsName" class="-mt-15px -mb-10px">
              <el-tab-pane label="订单产品清单" name="item">
                <SaleOrderItemForm ref="itemFormRef" :items="formData.items" :disabled="disabled" />
              </el-tab-pane>
            </el-tabs>
          </ContentWrap>

          <el-row :gutter="20">
            <el-col :xs="24" :sm="12" :lg="8">
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
            <el-col :xs="24" :sm="12" :lg="8">
              <el-form-item label="收款优惠" prop="discountPrice">
                <el-input
                  disabled
                  v-model="formData.discountPrice"
                  :formatter="erpPriceInputFormatter"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="8">
              <el-form-item label="优惠后金额">
                <el-input
                  disabled
                  v-model="formData.totalPrice"
                  :formatter="erpPriceInputFormatter"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="8">
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
            <el-col :xs="24" :sm="12" :lg="8">
              <el-form-item label="收取订金" prop="depositPrice">
                <el-input-number
                  v-model="formData.depositPrice"
                  controls-position="right"
                  :min="0"
                  :precision="2"
                  placeholder="请输入收取订金"
                  class="!w-1/1"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </ContentWrap>

      <ContentWrap title="审批历史" class="mt-12px">
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
              <div v-if="item.beforeStatus != null || item.afterStatus != null" class="mb-4px">
                <span class="text-gray-500">状态变化：</span>
                <span
                  >{{ formatAuditStatus(item.beforeStatus) }} ->
                  {{ formatAuditStatus(item.afterStatus) }}</span
                >
              </div>
              <div v-if="item.reason">
                <span class="text-gray-500">说明：</span>
                <span>{{ item.reason }}</span>
              </div>
            </div>
          </el-timeline-item>
        </el-timeline>
      </ContentWrap>

      <ContentWrap class="mt-12px">
        <div class="flex flex-wrap justify-end gap-12px">
          <el-button @click="handleBack">返回</el-button>
          <el-button :loading="saveLoading" :disabled="!canSave" @click="handleSave">
            保存修改
          </el-button>
          <el-button
            type="primary"
            :loading="saveAndSubmitLoading"
            :disabled="!canSaveAndSubmit"
            @click="handleSaveAndSubmit"
          >
            保存并重新提交审批
          </el-button>
        </div>
      </ContentWrap>
      <SaleOrderSubmitDialog ref="submitDialogRef" @success="handleSubmitSuccess" />
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { formatDate } from '@/utils/formatTime'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import {
  SaleOrderApi,
  SaleOrderAuditLogVO,
  SaleOrderRejectLogVO,
  SaleOrderVO
} from '@/api/erp/sale/order'
import SaleOrderItemForm from '../../components/SaleOrderItemForm.vue'
import SaleOrderSubmitDialog from '../../SaleOrderSubmitDialog.vue'
import { CustomerApi, CustomerVO } from '@/api/erp/sale/customer'
import { AccountApi, AccountVO } from '@/api/erp/finance/account'
import { ProjectApi } from '@/api/erp/project'
import * as UserApi from '@/api/system/user'
import { erpPriceInputFormatter, erpPriceMultiply } from '@/utils'

defineOptions({ name: 'ErpSaleOrderBpmRestart' })

const props = defineProps<{
  id?: number | string
  from?: string
}>()

type SaleOrderFormData = SaleOrderVO & {
  accountId?: number
  saleUserId?: number
  fileUrl?: string
  discountPercent?: number
  discountPrice?: number
  depositPrice?: number
  items: any[]
}

const SALE_ORDER_STATUS = {
  PROCESS: 10,
  APPROVE: 20,
  REJECT: 30
} as const

const DELIVERY_READY_STATUS = {
  NOT_READY: 'NOT_READY',
  PART_READY: 'PART_READY',
  READY_TO_SHIP: 'READY_TO_SHIP'
} as const

const message = useMessage()
const router = useRouter()
const route = useRoute()
const { t } = useI18n()
const { push } = router

const detailLoading = ref(false)
const optionLoading = ref(false)
const saveLoading = ref(false)
const saveAndSubmitLoading = ref(false)
const formRef = ref()
const itemFormRef = ref()
const submitDialogRef = ref()
const subTabsName = ref('item')

const customerList = ref<CustomerVO[]>([])
const projectList = ref<any[]>([])
const accountList = ref<AccountVO[]>([])
const userList = ref<UserApi.UserVO[]>([])

const formData = ref<SaleOrderFormData>({
  id: undefined,
  no: undefined,
  customerId: undefined,
  orderTime: undefined,
  totalCount: 0,
  totalPrice: 0,
  status: undefined,
  remark: undefined,
  outCount: 0,
  returnCount: 0,
  deliveryReadyStatus: undefined,
  processInstanceId: undefined,
  projectId: undefined,
  projectName: undefined,
  businessType: undefined,
  sourceProjectId: undefined,
  settlementType: undefined,
  sourceProductId: undefined,
  deliveryDate: undefined,
  creator: undefined,
  lastRejectReason: undefined,
  lastRejectTime: undefined,
  rejectLogs: [],
  auditLogs: [],
  accountId: undefined,
  saleUserId: undefined,
  fileUrl: '',
  discountPercent: 0,
  discountPrice: 0,
  depositPrice: 0,
  items: []
})

const formRules = reactive({
  customerId: [{ required: true, message: '客户不能为空', trigger: 'blur' }],
  orderTime: [{ required: true, message: '订单时间不能为空', trigger: 'blur' }],
  businessType: [{ required: true, message: '业务类型不能为空', trigger: 'change' }],
  projectId: [
    {
      validator: (_rule, value, callback) => {
        if (formData.value.businessType === 'SELF_RESEARCH' && !value) {
          callback(new Error('自研业务请选择项目'))
          return
        }
        callback()
      },
      trigger: 'change'
    }
  ]
})

const pageLoading = computed(() => detailLoading.value || optionLoading.value)
const editable = computed(() => {
  return (
    formData.value.status === SALE_ORDER_STATUS.REJECT ||
    (formData.value.status === SALE_ORDER_STATUS.PROCESS && !formData.value.processInstanceId)
  )
})
const disabled = computed(() => !editable.value)
const showRejectReminder = computed(() => !!formData.value.lastRejectReason)
const showApprovalRunningReminder = computed(
  () => formData.value.status === SALE_ORDER_STATUS.PROCESS && !!formData.value.processInstanceId
)
const showReadonlyReminder = computed(() => formData.value.status === SALE_ORDER_STATUS.APPROVE)
const showProcessLink = computed(() => !!formData.value.processInstanceId)
const canSave = computed(
  () => !pageLoading.value && editable.value && !saveLoading.value && !saveAndSubmitLoading.value
)
const canSaveAndSubmit = computed(
  () => !pageLoading.value && editable.value && !saveLoading.value && !saveAndSubmitLoading.value
)

const businessTypeOptions = [
  { label: '自研', value: 'SELF_RESEARCH' },
  { label: '客供', value: 'CUSTOMER_SUPPLIED' },
  { label: '代工', value: 'TOLL_MANUFACTURING' }
]
const settlementTypeOptions = [
  { label: '产品销售', value: 'PRODUCT_SALE' },
  { label: '加工费', value: 'PROCESSING_FEE' }
]
const actionTextMap: Record<string, string> = {
  APPROVE: '审批通过',
  REJECT: '驳回',
  RESUBMIT: '重新提交',
  REVERSE_APPROVE: '反审核'
}
const approvalRunningReminderTitle = computed(() => {
  if (props.from === 'processInstanceRestart') {
    return '该销售订单当前已有新的审批流程在进行中，不能基于历史流程重新发起。'
  }
  return '当前销售订单正在审批中，暂不可编辑。'
})
const readonlyReminderTitle = computed(() => {
  if (props.from === 'processInstanceRestart') {
    return '该销售订单当前已重新提交并审核完成，不能基于历史流程重新发起。'
  }
  return '当前销售订单已审批完成，仅支持查看。'
})

const currentOrderId = computed(() => {
  const id = Number(props.id || route.query.id)
  return Number.isNaN(id) || id <= 0 ? undefined : id
})

const displayAuditLogs = computed(() => {
  const auditLogs = formData.value.auditLogs || []
  if (auditLogs.length) {
    return auditLogs
  }
  return mapRejectLogsToAuditLogs(formData.value.rejectLogs || [])
})

watch(
  () => formData.value,
  (val) => {
    if (!val) {
      return
    }
    const totalPrice = val.items.reduce((prev, curr) => prev + (curr.totalPrice || 0), 0)
    const discountPrice =
      val.discountPercent != null ? erpPriceMultiply(totalPrice, val.discountPercent / 100.0) : 0
    formData.value.discountPrice = discountPrice
    formData.value.totalPrice = totalPrice - discountPrice
  },
  { deep: true }
)

watch(
  () => formData.value.businessType,
  (businessType) => {
    if (businessType === 'TOLL_MANUFACTURING') {
      formData.value.settlementType = 'PROCESSING_FEE'
      return
    }
    if (!businessType) {
      formData.value.settlementType = undefined
      return
    }
    if (!formData.value.settlementType || formData.value.settlementType === 'PROCESSING_FEE') {
      formData.value.settlementType = 'PRODUCT_SALE'
    }
  }
)

const resetForm = () => {
  formData.value = {
    id: undefined,
    no: undefined,
    customerId: undefined,
    orderTime: undefined,
    totalCount: 0,
    totalPrice: 0,
    status: undefined,
    remark: undefined,
    outCount: 0,
    returnCount: 0,
    deliveryReadyStatus: undefined,
    processInstanceId: undefined,
    projectId: undefined,
    projectName: undefined,
    businessType: undefined,
    sourceProjectId: undefined,
    settlementType: undefined,
    sourceProductId: undefined,
    deliveryDate: undefined,
    creator: undefined,
    lastRejectReason: undefined,
    lastRejectTime: undefined,
    rejectLogs: [],
    auditLogs: [],
    accountId: undefined,
    saleUserId: undefined,
    fileUrl: '',
    discountPercent: 0,
    discountPrice: 0,
    depositPrice: 0,
    items: []
  }
  formRef.value?.resetFields()
}

const mapRejectLogsToAuditLogs = (rejectLogs: SaleOrderRejectLogVO[] = []) => {
  return rejectLogs.map((item) => ({
    actionType: 'REJECT',
    reason: item.reason,
    operatorId: item.rejectUserId,
    operatorName: item.rejectUserName,
    operatorNickname: item.rejectUserNickname || item.rejectUserName || item.creatorName,
    createTime: item.rejectTime || item.createTime
  })) as SaleOrderAuditLogVO[]
}

const normalizeSaleOrderFormData = (data: Partial<SaleOrderFormData>) => {
  return {
    ...data,
    fileUrl: data.fileUrl || '',
    discountPercent: data.discountPercent ?? 0,
    discountPrice: data.discountPrice ?? 0,
    depositPrice: data.depositPrice ?? 0,
    items: data.items || [],
    rejectLogs: data.rejectLogs || [],
    auditLogs: data.auditLogs || []
  } as SaleOrderFormData
}

const loadOptions = async () => {
  optionLoading.value = true
  try {
    const [customers, projects, users, accounts] = await Promise.all([
      CustomerApi.getCustomerSimpleList(),
      ProjectApi.getProjectSimpleList(),
      UserApi.getSimpleUserList(),
      AccountApi.getAccountSimpleList()
    ])
    customerList.value = customers
    projectList.value = projects
    userList.value = users
    accountList.value = accounts
    const defaultAccount = accountList.value.find((item) => item.defaultStatus)
    if (defaultAccount && !formData.value.accountId) {
      formData.value.accountId = defaultAccount.id
    }
  } finally {
    optionLoading.value = false
  }
}

const loadSaleOrder = async () => {
  const id = currentOrderId.value
  if (!id) {
    message.warning('销售订单编号不能为空')
    return
  }
  detailLoading.value = true
  try {
    formData.value = normalizeSaleOrderFormData(await SaleOrderApi.getSaleOrder(id))
  } finally {
    detailLoading.value = false
  }
}

const initializePage = async () => {
  resetForm()
  if (!currentOrderId.value) {
    return
  }
  await Promise.all([loadSaleOrder(), loadOptions()])
}

watch(
  () => currentOrderId.value,
  async () => {
    await initializePage()
  },
  { immediate: true }
)

const resolveDeliveryReadyLabel = (status?: string) => {
  if (status === DELIVERY_READY_STATUS.PART_READY) {
    return '部分就绪'
  }
  if (status === DELIVERY_READY_STATUS.READY_TO_SHIP) {
    return '可发货'
  }
  return '暂无可发'
}

const resolveDeliveryReadyTagType = (status?: string) => {
  if (status === DELIVERY_READY_STATUS.PART_READY) {
    return 'warning'
  }
  if (status === DELIVERY_READY_STATUS.READY_TO_SHIP) {
    return 'success'
  }
  return 'info'
}

const formatAuditLogTime = (item: SaleOrderAuditLogVO) => {
  return item.createTime ? formatDate(item.createTime, 'YYYY-MM-DD') : '-'
}

const formatAuditLogUser = (item: SaleOrderAuditLogVO) => {
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
  if (actionType === 'RESUBMIT') {
    return 'warning'
  }
  return 'info'
}

const formatAuditStatus = (status?: number) => {
  if (status == null) {
    return '-'
  }
  const dict = getIntDictOptions(DICT_TYPE.ERP_AUDIT_STATUS).find(
    (item) => Number(item.value) === status
  )
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

const persistOrder = async (loadingRef: typeof saveLoading) => {
  await formRef.value?.validate()
  await itemFormRef.value?.validate()
  loadingRef.value = true
  try {
    const data = formData.value as unknown as SaleOrderVO
    await SaleOrderApi.updateSaleOrder(data)
    message.success(t('common.updateSuccess'))
    await loadSaleOrder()
    return { ...formData.value } as SaleOrderVO
  } finally {
    loadingRef.value = false
  }
}

const handleSave = async () => {
  await persistOrder(saveLoading)
}

const handleSaveAndSubmit = async () => {
  const latestOrder = await persistOrder(saveAndSubmitLoading)
  if (!latestOrder?.id) {
    return
  }
  submitDialogRef.value?.open(latestOrder)
}

const handleSubmitSuccess = async () => {
  await loadSaleOrder()
}

const handleBack = () => {
  if (window.history.length > 1) {
    router.back()
    return
  }
  push({
    path: props.from === 'processInstanceRestart' ? '/approval/submitted' : '/sales/order'
  })
}
</script>
