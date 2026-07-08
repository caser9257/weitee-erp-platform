<template>
  <Dialog title="审批历史" v-model="dialogVisible" width="720">
    <el-empty v-if="!auditLogs.length" description="暂无审批历史" />
    <el-timeline v-else>
      <el-timeline-item
        v-for="(item, index) in auditLogs"
        :key="index"
        :timestamp="item.createTime ? dateFormatter2(undefined, undefined, item.createTime) : '-'"
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
  </Dialog>
</template>

<script setup lang="ts">
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { dateFormatter2 } from '@/utils/formatTime'
import { SaleOrderApi, SaleOrderAuditLogVO } from '@/api/erp/sale/order'

defineOptions({ name: 'SaleOrderAuditLogDialog' })

const dialogVisible = ref(false)
const auditLogs = ref<SaleOrderAuditLogVO[]>([])

const actionTextMap: Record<string, string> = {
  APPROVE: '审批通过',
  REJECT: '驳回',
  RESUBMIT: '重新提交',
  REVERSE_APPROVE: '反审核'
}

const resolveTagType = (actionType?: string) => {
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

const mapRejectLogsToAuditLogs = (rejectLogs: any[] = []) => {
  return rejectLogs.map((item) => ({
    actionType: 'REJECT',
    reason: item.reason,
    operatorId: item.rejectUserId,
    operatorName: item.rejectUserName,
    operatorNickname: item.rejectUserNickname || item.rejectUserName || item.creatorName,
    createTime: item.rejectTime || item.createTime
  })) as SaleOrderAuditLogVO[]
}

const open = async (id: number) => {
  dialogVisible.value = true
  const data = await SaleOrderApi.getSaleOrder(id)
  auditLogs.value =
    data.auditLogs && data.auditLogs.length ? data.auditLogs : mapRejectLogsToAuditLogs(data.rejectLogs || [])
}

defineExpose({ open })
</script>
