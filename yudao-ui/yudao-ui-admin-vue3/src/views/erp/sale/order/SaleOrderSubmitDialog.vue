<template>
  <Dialog title="提交审批" v-model="dialogVisible" width="960">
    <div v-loading="loading">
      <el-alert
        class="mb-16px"
        type="info"
        :closable="false"
        show-icon
        title="提交后将进入 BPM 审批流，审批中不可编辑，请确认订单信息无误后再发起。"
      />
      <el-descriptions :column="2" border class="mb-16px">
        <el-descriptions-item label="订单单号">{{ currentOrder?.no || '-' }}</el-descriptions-item>
        <el-descriptions-item label="客户编号">{{ currentOrder?.customerId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="项目编号">{{ currentOrder?.projectId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="订单金额">
          {{ erpPriceInputFormatter(currentOrder?.totalPrice) }}
        </el-descriptions-item>
      </el-descriptions>
      <ContentWrap title="审批流程" :bodyStyle="{ padding: '0 20px 0' }">
        <el-empty v-if="activityNodes.length === 0" description="未获取到审批流程，请检查流程定义配置" />
        <ProcessInstanceTimeline
          v-else
          :activity-nodes="activityNodes"
          :show-status-icon="false"
          @select-user-confirm="selectUserConfirm"
        />
      </ContentWrap>
    </div>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitLoading" @click="submit">提交审批</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import type { ApprovalNodeInfo } from '@/api/bpm/processInstance'
import * as DefinitionApi from '@/api/bpm/definition'
import * as ProcessInstanceApi from '@/api/bpm/processInstance'
import { SaleOrderApi, SaleOrderVO } from '@/api/erp/sale/order'
import { CandidateStrategy, NodeId } from '@/components/SimpleProcessDesignerV2/src/consts'
import ProcessInstanceTimeline from '@/views/bpm/processInstance/detail/ProcessInstanceTimeline.vue'
import { erpPriceInputFormatter } from '@/utils'

defineOptions({ name: 'SaleOrderSubmitDialog' })

const PROCESS_DEFINITION_KEY = 'erp_sale_order'

const message = useMessage()

const dialogVisible = ref(false)
const loading = ref(false)
const submitLoading = ref(false)
const processDefinitionId = ref('')
const activityNodes = ref<ApprovalNodeInfo[]>([])
const startUserSelectTasks = ref<ApprovalNodeInfo[]>([])
const startUserSelectAssignees = ref<Record<string, number[]>>({})
const tempStartUserSelectAssignees = ref<Record<string, number[]>>({})
const currentOrder = ref<SaleOrderVO>()

const emit = defineEmits(['success'])

const resetState = () => {
  processDefinitionId.value = ''
  activityNodes.value = []
  startUserSelectTasks.value = []
  startUserSelectAssignees.value = {}
  tempStartUserSelectAssignees.value = {}
  currentOrder.value = undefined
}

const buildProcessVariables = (order: SaleOrderVO) => {
  return {
    saleOrderId: order.id,
    saleOrderNo: order.no,
    totalPrice: order.totalPrice,
    customerId: order.customerId,
    projectId: order.projectId,
    businessType: order.businessType
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

const open = async (order: SaleOrderVO) => {
  dialogVisible.value = true
  loading.value = true
  resetState()
  currentOrder.value = { ...order }
  try {
    const definition = await DefinitionApi.getProcessDefinition(undefined, PROCESS_DEFINITION_KEY)
    if (!definition?.id) {
      message.error('销售订单审批流程未配置，请先发布 BPM 流程定义')
      dialogVisible.value = false
      return
    }
    processDefinitionId.value = definition.id
    await getApprovalDetail()
  } finally {
    loading.value = false
  }
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
  if (!currentOrder.value) {
    return
  }
  if (!validateStartUserSelectAssignees()) {
    return
  }
  submitLoading.value = true
  try {
    await SaleOrderApi.submitSaleOrder({
      id: currentOrder.value.id,
      startUserSelectAssignees: startUserSelectAssignees.value
    })
    message.success('提交审批成功')
    dialogVisible.value = false
    emit('success')
  } finally {
    submitLoading.value = false
  }
}

defineExpose({ open })
</script>
