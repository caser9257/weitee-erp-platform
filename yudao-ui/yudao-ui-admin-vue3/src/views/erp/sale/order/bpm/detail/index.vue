<template>
  <div v-loading="loading">
    <el-empty v-if="!saleOrder.id" description="销售订单不存在或已删除" />
    <template v-else>
      <ContentWrap class="mb-12px">
        <div class="flex items-center justify-between gap-12px">
          <div>
            <div class="text-18px font-600">{{ saleOrder.no || `销售订单 #${saleOrder.id}` }}</div>
            <div class="mt-6px text-12px text-[var(--el-text-color-secondary)]">
              订单编号：{{ saleOrder.id }}
            </div>
          </div>
          <div class="flex items-center gap-8px">
            <dict-tag :type="DICT_TYPE.ERP_AUDIT_STATUS" :value="saleOrder.status" />
            <el-tag :type="resolveDeliveryReadyTagType(saleOrder.deliveryReadyStatus)" size="small">
              {{ resolveDeliveryReadyLabel(saleOrder.deliveryReadyStatus) }}
            </el-tag>
          </div>
        </div>
      </ContentWrap>

      <ContentWrap title="基础信息" class="mb-12px">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单单号">{{ saleOrder.no || '-' }}</el-descriptions-item>
          <el-descriptions-item label="订单时间">
            {{ saleOrder.orderTime ? formatDate(saleOrder.orderTime, 'YYYY-MM-DD') : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="客户编号">{{
            saleOrder.customerId || '-'
          }}</el-descriptions-item>
          <el-descriptions-item label="项目">
            {{ saleOrder.projectName || saleOrder.projectId || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="交期">
            {{ saleOrder.deliveryDate ? formatDate(saleOrder.deliveryDate, 'YYYY-MM-DD') : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="流程实例">
            {{ saleOrder.processInstanceId || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="合计数量">{{
            saleOrder.totalCount ?? '-'
          }}</el-descriptions-item>
          <el-descriptions-item label="合计金额">
            {{ formatPrice(saleOrder.totalPrice) }}
          </el-descriptions-item>
          <el-descriptions-item label="交付就绪">
            <el-tag :type="resolveDeliveryReadyTagType(saleOrder.deliveryReadyStatus)" size="small">
              {{ resolveDeliveryReadyLabel(saleOrder.deliveryReadyStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="已出库数量">{{
            saleOrder.outCount ?? '-'
          }}</el-descriptions-item>
          <el-descriptions-item label="已退货数量">{{
            saleOrder.returnCount ?? '-'
          }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{
            saleOrder.remark || '-'
          }}</el-descriptions-item>
        </el-descriptions>
      </ContentWrap>

      <ContentWrap title="订单产品" class="mb-12px">
        <el-table :data="saleOrder.items || []" stripe>
          <el-table-column label="产品编号" prop="productId" min-width="120" />
          <el-table-column label="数量" prop="count" min-width="100" />
          <el-table-column label="销售单价" min-width="120">
            <template #default="{ row }">{{ formatPrice(row.productPrice) }}</template>
          </el-table-column>
          <el-table-column label="税额" min-width="120">
            <template #default="{ row }">{{ formatPrice(row.taxPrice) }}</template>
          </el-table-column>
          <el-table-column label="小计" min-width="120">
            <template #default="{ row }">{{ formatPrice(row.totalPrice) }}</template>
          </el-table-column>
          <el-table-column label="备注" prop="remark" min-width="180" show-overflow-tooltip />
        </el-table>
      </ContentWrap>

      <ContentWrap title="审批历史">
        <el-empty v-if="displayAuditLogs.length === 0" description="暂无审批历史" />
        <el-timeline v-else>
          <el-timeline-item
            v-for="(item, index) in displayAuditLogs"
            :key="index"
            :timestamp="item.createTime ? formatDate(item.createTime, 'YYYY-MM-DD') : '-'"
            placement="top"
          >
            <div class="text-13px leading-22px">
              <div class="mb-4px">
                <span class="text-[var(--el-text-color-secondary)]">动作：</span>
                <el-tag size="small" :type="resolveAuditTagType(item.actionType)">
                  {{ formatAuditAction(item.actionType) }}
                </el-tag>
              </div>
              <div class="mb-4px">
                <span class="text-[var(--el-text-color-secondary)]">操作人：</span>
                <span>{{ formatAuditUser(item) }}</span>
              </div>
              <div v-if="item.beforeStatus != null || item.afterStatus != null" class="mb-4px">
                <span class="text-[var(--el-text-color-secondary)]">状态变化：</span>
                <span
                  >{{ formatStatus(item.beforeStatus) }} ->
                  {{ formatStatus(item.afterStatus) }}</span
                >
              </div>
              <div v-if="item.reason">
                <span class="text-[var(--el-text-color-secondary)]">说明：</span>
                <span>{{ item.reason }}</span>
              </div>
            </div>
          </el-timeline-item>
        </el-timeline>
      </ContentWrap>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { formatDate } from '@/utils/formatTime'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import {
  SaleOrderApi,
  SaleOrderAuditLogVO,
  SaleOrderRejectLogVO,
  SaleOrderVO
} from '@/api/erp/sale/order'

defineOptions({ name: 'ErpSaleOrderBpmDetail' })

const props = defineProps<{ id?: number }>()
const route = useRoute()
const message = useMessage()

const loading = ref(false)
const saleOrder = ref<(SaleOrderVO & { items?: any[] }) | Record<string, never>>({})
const DELIVERY_READY_STATUS = {
  NOT_READY: 'NOT_READY',
  PART_READY: 'PART_READY',
  READY_TO_SHIP: 'READY_TO_SHIP'
} as const

const actionTextMap: Record<string, string> = {
  APPROVE: '审批通过',
  REJECT: '驳回',
  RESUBMIT: '重新提交',
  REVERSE_APPROVE: '反审核'
}

const displayAuditLogs = computed(() => {
  const data = saleOrder.value as SaleOrderVO
  if (data.auditLogs?.length) {
    return data.auditLogs
  }
  return (data.rejectLogs || []).map((item: SaleOrderRejectLogVO) => ({
    actionType: 'REJECT',
    reason: item.reason,
    operatorId: item.rejectUserId,
    operatorName: item.rejectUserName,
    operatorNickname: item.rejectUserNickname || item.rejectUserName || item.creatorName,
    createTime: item.rejectTime || item.createTime
  })) as SaleOrderAuditLogVO[]
})

const loadSaleOrder = async () => {
  const id = Number(props.id || route.params.id)
  if (!id) {
    message.warning('销售订单编号不能为空')
    return
  }
  loading.value = true
  try {
    saleOrder.value = await SaleOrderApi.getSaleOrder(id)
  } finally {
    loading.value = false
  }
}

const formatPrice = (value?: number) => {
  if (value == null) {
    return '-'
  }
  return Number(value).toFixed(2)
}

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

const formatAuditAction = (actionType?: string) => {
  return (actionType && actionTextMap[actionType]) || actionType || '-'
}

const formatAuditUser = (item: SaleOrderAuditLogVO) => {
  return item.operatorNickname || item.operatorName || '未知'
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

const formatStatus = (status?: number) => {
  if (status == null) {
    return '-'
  }
  const dict = getIntDictOptions(DICT_TYPE.ERP_AUDIT_STATUS).find(
    (item) => Number(item.value) === status
  )
  return dict?.label || String(status)
}

onMounted(() => {
  loadSaleOrder()
})
</script>
