<template>
  <Dialog
    title="提交审批"
    v-model="dialogVisible"
    :width="dialogWidth"
    scroll
    :max-height="dialogMaxHeight"
  >
    <div v-loading="flowLoading" class="purchase-order-submit-dialog">
      <el-alert
        class="mb-16px"
        type="info"
        :closable="false"
        show-icon
        title="提交后将进入 BPM 审批流，审批中不可编辑，请确认采购订单信息无误后再发起。"
      />
      <el-descriptions :column="2" border class="mb-16px purchase-order-submit-dialog__summary">
        <el-descriptions-item label="采购单号">{{ currentOrder?.no || '-' }}</el-descriptions-item>
        <el-descriptions-item label="供应商编号">{{ currentOrder?.supplierId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="来源类型">{{ formatSourceType(currentOrder?.sourceType) }}</el-descriptions-item>
        <el-descriptions-item label="含税金额">
          {{ erpPriceInputFormatter(currentOrder?.totalPrice) }}
        </el-descriptions-item>
      </el-descriptions>
      <ContentWrap title="审批流程" :bodyStyle="{ padding: '0 20px 0' }">
        <div v-if="flowLoadFailed" class="purchase-order-submit-dialog__state">
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
import { PurchaseOrderApi, PurchaseOrderVO } from '@/api/erp/purchase/order'
import { CandidateStrategy, NodeId } from '@/components/SimpleProcessDesignerV2/src/consts'
import ProcessInstanceTimeline from '@/views/bpm/processInstance/detail/ProcessInstanceTimeline.vue'
import { erpPriceInputFormatter } from '@/utils'

defineOptions({ name: 'PurchaseOrderSubmitDialog' })

const PROCESS_DEFINITION_KEY = 'erp_purchase_order'

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
const currentOrder = ref<PurchaseOrderVO>()
const canRetryApprovalFlow = computed(() => flowLoadFailed.value && !flowLoading.value)
const canSubmitApproval = computed(
  () => !!currentOrder.value && !flowLoading.value && !flowLoadFailed.value && !submitLoading.value
)

const emit = defineEmits(['success'])

const resetState = () => {
  processDefinitionId.value = ''
  activityNodes.value = []
  startUserSelectTasks.value = []
  startUserSelectAssignees.value = {}
  tempStartUserSelectAssignees.value = {}
  currentOrder.value = undefined
  flowLoadFailed.value = false
  flowLoadErrorMessage.value = ''
}

const buildProcessVariables = (order: PurchaseOrderVO) => {
  return {
    purchaseOrderId: order.id,
    purchaseOrderNo: order.no,
    totalPrice: order.totalPrice,
    supplierId: order.supplierId,
    sourceType: order.sourceType || 'MANUAL',
    creatorId: order.creator && /^\d+$/.test(order.creator) ? Number(order.creator) : undefined
  }
}

const formatSourceType = (sourceType?: string) => {
  if (sourceType === 'MRP') {
    return 'MRP 转单'
  }
  if (sourceType === 'MANUAL') {
    return '手工下单'
  }
  return sourceType || '-'
}

const restoreStartUserSelectAssignees = () => {
  startUserSelectAssignees.value = {}
  startUserSelectTasks.value.forEach((task) => {
    const cache = tempStartUserSelectAssignees.value[String(task.id)]
    startUserSelectAssignees.value[String(task.id)] = cache && cache.length ? [...cache] : []
  })
}

const getApprovalDetail = async () => {
  if (!processDefinitionId.value || !currentOrder.value) {
    return
  }
  const data = await ProcessInstanceApi.getApprovalDetail({
    processDefinitionId: processDefinitionId.value,
    activityId: NodeId.START_USER_NODE_ID,
    processVariablesStr: JSON.stringify(buildProcessVariables(currentOrder.value))
  })
  activityNodes.value = data?.activityNodes || []
  startUserSelectTasks.value =
    data?.activityNodes?.filter(
      (node: ApprovalNodeInfo) => node.candidateStrategy === CandidateStrategy.START_USER_SELECT
    ) || []
  restoreStartUserSelectAssignees()
}

const loadApprovalFlow = async () => {
  if (!currentOrder.value) {
    return
  }
  flowLoading.value = true
  flowLoadFailed.value = false
  flowLoadErrorMessage.value = ''
  try {
    const definition = await DefinitionApi.getProcessDefinition(undefined, PROCESS_DEFINITION_KEY)
    if (!definition?.id) {
      flowLoadFailed.value = true
      flowLoadErrorMessage.value = '采购订单审批流程未配置，请先发布 BPM 流程定义'
      return
    }
    processDefinitionId.value = definition.id
    await getApprovalDetail()
  } catch (error: any) {
    flowLoadFailed.value = true
    flowLoadErrorMessage.value =
      error?.message || error?.msg || '审批流程加载失败，请稍后重试'
  } finally {
    flowLoading.value = false
  }
}

const open = async (order: PurchaseOrderVO) => {
  dialogVisible.value = true
  resetState()
  currentOrder.value = { ...order }
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
  if (!currentOrder.value || flowLoadFailed.value || flowLoading.value) {
    return
  }
  if (!validateStartUserSelectAssignees()) {
    return
  }
  submitLoading.value = true
  try {
    await PurchaseOrderApi.submitPurchaseOrder({
      id: currentOrder.value.id,
      startUserSelectAssignees: startUserSelectAssignees.value
    })
    message.warning('提交请求已发送，列表将刷新校验状态')
    dialogVisible.value = false
    emit('success')
  } finally {
    submitLoading.value = false
  }
}

const retryLoadFlow = async () => {
  await loadApprovalFlow()
}

watch(dialogVisible, (visible) => {
  if (visible) {
    return
  }
  resetState()
})

defineExpose({ open })
</script>

<style scoped lang="scss">
.purchase-order-submit-dialog__summary {
  :deep(.el-descriptions__label) {
    width: 120px;
  }
}

.purchase-order-submit-dialog__state {
  min-height: 240px;
  display: flex;
  align-items: center;
  justify-content: center;
}

@media (max-width: 768px) {
  .purchase-order-submit-dialog__summary {
    :deep(.el-descriptions__body .el-descriptions__table) {
      display: block;
    }
  }
}
</style>
