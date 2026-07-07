<template>
  <Dialog
    title="审批历史"
    v-model="dialogVisible"
    :width="dialogWidth"
    scroll
    :max-height="dialogMaxHeight"
  >
    <div v-loading="loading" class="purchase-order-audit-log-dialog">
      <div v-if="loadFailed" class="purchase-order-audit-log-dialog__state">
        <el-empty :description="loadErrorMessage || '审批历史加载失败'">
          <el-button type="primary" plain :disabled="!canRetryAuditLog" @click="retryLoadAuditLog">
            重试加载
          </el-button>
        </el-empty>
      </div>
      <el-empty v-else-if="!auditLogs.length" description="暂无审批历史" />
      <el-timeline v-else>
      <el-timeline-item
        v-for="(item, index) in auditLogs"
        :key="index"
        :timestamp="item.createTime ? formatDate(item.createTime as Date, 'YYYY-MM-DD') : '-'"
        placement="top"
      >
        <div class="text-13px leading-22px">
          <div class="mb-4px">
            <span class="text-gray-500">动作：</span>
            <el-tag size="small" :type="resolveTagType(item.actionType)">
              {{ formatAction(item.actionType) }}
            </el-tag>
          </div>
          <div class="mb-4px">
            <span class="text-gray-500">操作人：</span>
            <span>{{ item.operatorNickname || item.operatorName || '未知' }}</span>
          </div>
          <div v-if="item.taskName" class="mb-4px">
            <span class="text-gray-500">节点：</span>
            <span>{{ item.taskName }}</span>
          </div>
          <div v-if="item.beforeStatus != null || item.afterStatus != null" class="mb-4px">
            <span class="text-gray-500">状态变化：</span>
            <span>{{ formatStatus(item.beforeStatus) }} -> {{ formatStatus(item.afterStatus) }}</span>
          </div>
          <div v-if="item.reason">
            <span class="text-gray-500">说明：</span>
            <span>{{ item.reason }}</span>
          </div>
        </div>
      </el-timeline-item>
      </el-timeline>
    </div>
  </Dialog>
</template>

<script setup lang="ts">
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { formatDate } from '@/utils/formatTime'
import { PurchaseOrderApi, PurchaseOrderAuditLogVO } from '@/api/erp/purchase/order'
import { resolvePurchaseOrderApprovalLogs } from './auditLogUtils'

defineOptions({ name: 'PurchaseOrderAuditLogDialog' })

const dialogVisible = ref(false)
const dialogWidth = 'min(720px, calc(100vw - 32px))'
const dialogMaxHeight = 'calc(100vh - 220px)'
const loading = ref(false)
const loadFailed = ref(false)
const loadErrorMessage = ref('')
const currentOrderId = ref<number>()
const auditLogs = ref<PurchaseOrderAuditLogVO[]>([])
const canRetryAuditLog = computed(() => !!currentOrderId.value && !loading.value)

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

const resolveTagType = (actionType?: string) => {
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

const formatAction = (actionType?: string) => {
  return (actionType && actionTextMap[actionType]) || actionType || '-'
}

const formatStatus = (status?: number) => {
  if (status == null) {
    return '-'
  }
  const dict = getIntDictOptions(DICT_TYPE.ERP_AUDIT_STATUS).find((item) => Number(item.value) === status)
  return dict?.label || String(status)
}

const resetState = () => {
  auditLogs.value = []
  loadFailed.value = false
  loadErrorMessage.value = ''
  currentOrderId.value = undefined
}

const loadAuditLogs = async (id: number) => {
  loading.value = true
  loadFailed.value = false
  loadErrorMessage.value = ''
  const data = await PurchaseOrderApi.getPurchaseOrder(id)
  auditLogs.value = resolvePurchaseOrderApprovalLogs(data)
}

const open = async (id: number) => {
  dialogVisible.value = true
  currentOrderId.value = id
  try {
    await loadAuditLogs(id)
  } catch (error: any) {
    auditLogs.value = []
    loadFailed.value = true
    loadErrorMessage.value =
      error?.message || error?.msg || '审批历史加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

const retryLoadAuditLog = async () => {
  if (!currentOrderId.value) {
    return
  }
  try {
    await loadAuditLogs(currentOrderId.value)
  } catch (error: any) {
    auditLogs.value = []
    loadFailed.value = true
    loadErrorMessage.value =
      error?.message || error?.msg || '审批历史加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
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
.purchase-order-audit-log-dialog__state {
  min-height: 220px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
