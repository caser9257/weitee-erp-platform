<template>
  <Dialog
    title="提交审批"
    v-model="dialogVisible"
    :width="dialogWidth"
    scroll
    :max-height="dialogMaxHeight"
  >
    <div v-loading="flowLoading" class="finance-payment-submit-dialog">
      <el-alert
        class="mb-16px"
        type="info"
        :closable="false"
        show-icon
        title="提交后将进入 BPM 审批流，审批中不可编辑，请确认付款单信息无误后再发起。"
      />
      <el-descriptions :column="2" border class="mb-16px finance-payment-submit-dialog__summary">
        <el-descriptions-item label="付款单号">{{ currentPayment?.no || '-' }}</el-descriptions-item>
        <el-descriptions-item label="付款时间">{{ formatPaymentTime(currentPayment?.paymentTime) }}</el-descriptions-item>
        <el-descriptions-item label="供应商">{{ currentPayment?.supplierName || currentPayment?.supplierId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="付款账户">{{ currentPayment?.accountName || currentPayment?.accountId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="实际付款">{{ erpPriceInputFormatter(currentPayment?.paymentPrice) }}</el-descriptions-item>
        <el-descriptions-item label="财务人员">{{ currentPayment?.financeUserName || currentPayment?.financeUserId || '-' }}</el-descriptions-item>
      </el-descriptions>
      <ContentWrap title="审批流程" :bodyStyle="{ padding: '0 20px 0' }">
        <div v-if="flowLoadFailed" class="finance-payment-submit-dialog__state">
          <el-empty :description="flowLoadErrorMessage || '审批流程加载失败'">
            <el-button type="primary" plain :disabled="!canRetryApprovalFlow" @click="retryLoadFlow">
              重试加载
            </el-button>
          </el-empty>
        </div>
        <el-empty v-else-if="activityNodes.length === 0" description="未获取到审批流程，请检查流程定义配置" />
        <ProcessInstanceTimeline
          v-else
          :activity-nodes="activityNodes"
          :show-status-icon="false"
          @select-user-confirm="selectUserConfirm"
        />
      </ContentWrap>
    </div>
    <template #footer>
      <el-button :disabled="submitLoading" @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitLoading" :disabled="!canSubmitApproval" @click="submit">
        提交审批
      </el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import type { ApprovalNodeInfo } from '@/api/bpm/processInstance'
import * as DefinitionApi from '@/api/bpm/definition'
import * as ProcessInstanceApi from '@/api/bpm/processInstance'
import { FinancePaymentApi, type FinancePaymentVO } from '@/api/erp/finance/payment'
import { CandidateStrategy, NodeId } from '@/components/SimpleProcessDesignerV2/src/consts'
import ProcessInstanceTimeline from '@/views/bpm/processInstance/detail/ProcessInstanceTimeline.vue'
import { erpPriceInputFormatter } from '@/utils'
import { dateFormatter } from '@/utils/formatTime'

defineOptions({ name: 'FinancePaymentSubmitDialog' })

const PROCESS_DEFINITION_KEY = 'erp_finance_payment'

const message = useMessage()

const dialogVisible = ref(false)
const dialogWidth = 'min(960px, calc(100vw - 32px))'
const dialogMaxHeight = 'calc(100vh - 180px)'
const flowLoading = ref(false)
const flowLoadFailed = ref(false)
const flowLoadErrorMessage = ref('')
const submitLoading = ref(false)
const processDefinitionId = ref('')
const activityNodes = ref<ApprovalNodeInfo[]>([])
const startUserSelectTasks = ref<ApprovalNodeInfo[]>([])
const startUserSelectAssignees = ref<Record<string, number[]>>({})
const tempStartUserSelectAssignees = ref<Record<string, number[]>>({})
const currentPayment = ref<FinancePaymentVO>()

const canRetryApprovalFlow = computed(() => flowLoadFailed.value && !flowLoading.value)
const canSubmitApproval = computed(
  () => !!currentPayment.value && !flowLoading.value && !flowLoadFailed.value && !submitLoading.value
)

const emit = defineEmits<{
  (e: 'success'): void
}>()

const resetState = () => {
  processDefinitionId.value = ''
  activityNodes.value = []
  startUserSelectTasks.value = []
  startUserSelectAssignees.value = {}
  tempStartUserSelectAssignees.value = {}
  currentPayment.value = undefined
  flowLoadFailed.value = false
  flowLoadErrorMessage.value = ''
}

const buildProcessVariables = (payment: FinancePaymentVO) => {
  return {
    paymentId: payment.id,
    paymentNo: payment.no,
    paymentTime: payment.paymentTime,
    totalPrice: payment.totalPrice,
    discountPrice: payment.discountPrice,
    paymentPrice: payment.paymentPrice,
    supplierId: payment.supplierId,
    accountId: payment.accountId,
    financeUserId: payment.financeUserId,
    creatorId:
      payment.creator && /^\d+$/.test(payment.creator) ? Number(payment.creator) : undefined
  }
}

const restoreStartUserSelectAssignees = () => {
  startUserSelectAssignees.value = {}
  startUserSelectTasks.value.forEach((task) => {
    const cache = tempStartUserSelectAssignees.value[String(task.id)]
    startUserSelectAssignees.value[String(task.id)] = cache && cache.length ? [...cache] : []
  })
}

const getApprovalDetail = async () => {
  if (!processDefinitionId.value || !currentPayment.value) {
    return
  }
  const data = await ProcessInstanceApi.getApprovalDetail({
    processDefinitionId: processDefinitionId.value,
    activityId: NodeId.START_USER_NODE_ID,
    processVariablesStr: JSON.stringify(buildProcessVariables(currentPayment.value))
  })
  activityNodes.value = data?.activityNodes || []
  startUserSelectTasks.value =
    data?.activityNodes?.filter(
      (node: ApprovalNodeInfo) => node.candidateStrategy === CandidateStrategy.START_USER_SELECT
    ) || []
  restoreStartUserSelectAssignees()
}

const loadApprovalFlow = async () => {
  if (!currentPayment.value) {
    return
  }
  flowLoading.value = true
  flowLoadFailed.value = false
  flowLoadErrorMessage.value = ''
  try {
    const definition = await DefinitionApi.getProcessDefinition(undefined, PROCESS_DEFINITION_KEY)
    if (!definition?.id) {
      flowLoadFailed.value = true
      flowLoadErrorMessage.value = '付款单审批流程未配置，请先发布 BPM 流程定义'
      return
    }
    processDefinitionId.value = definition.id
    await getApprovalDetail()
  } catch (error: any) {
    flowLoadFailed.value = true
    flowLoadErrorMessage.value = error?.message || error?.msg || '审批流程加载失败，请稍后重试'
  } finally {
    flowLoading.value = false
  }
}

const open = async (payment: FinancePaymentVO) => {
  dialogVisible.value = true
  resetState()
  currentPayment.value = { ...payment }
  await loadApprovalFlow()
}

const selectUserConfirm = (id: string, userList: any[]) => {
  startUserSelectAssignees.value[id] = userList?.map((item: any) => item.id) || []
  tempStartUserSelectAssignees.value = { ...startUserSelectAssignees.value }
}

const validateStartUserSelectAssignees = () => {
  for (const task of startUserSelectTasks.value) {
    const assignees = startUserSelectAssignees.value[String(task.id)]
    if (!Array.isArray(assignees) || assignees.length === 0) {
      message.warning(`请选择 ${task.name} 的审批人`)
      return false
    }
  }
  return true
}

const submit = async () => {
  if (!currentPayment.value || flowLoadFailed.value || flowLoading.value) {
    return
  }
  if (!validateStartUserSelectAssignees()) {
    return
  }
  submitLoading.value = true
  try {
    await FinancePaymentApi.submitFinancePayment({
      id: currentPayment.value.id!,
      startUserSelectAssignees: startUserSelectAssignees.value
    })
    message.success('提交审批成功')
    dialogVisible.value = false
    emit('success')
  } finally {
    submitLoading.value = false
  }
}

const retryLoadFlow = async () => {
  await loadApprovalFlow()
}

const formatPaymentTime = (value?: Date | string | number) => {
  return value ? dateFormatter(undefined as any, undefined as any, value) || '-' : '-'
}

watch(dialogVisible, (visible) => {
  if (visible) {
    return
  }
  resetState()
})

defineExpose({ open })
</script>

<style scoped>
.finance-payment-submit-dialog__summary {
  :deep(.el-descriptions__label) {
    width: 120px;
  }
}

.finance-payment-submit-dialog__state {
  min-height: 240px;
  display: flex;
  align-items: center;
  justify-content: center;
}

@media (max-width: 768px) {
  .finance-payment-submit-dialog__summary {
    :deep(.el-descriptions__body .el-descriptions__table) {
      display: block;
    }
  }
}
</style>
