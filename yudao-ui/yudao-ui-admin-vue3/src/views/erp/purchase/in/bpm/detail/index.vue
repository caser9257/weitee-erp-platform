<template>
  <div v-loading="loading">
    <el-empty v-if="!purchaseIn.id" description="采购入库单不存在或已删除" />
    <template v-else>
      <ContentWrap class="mb-12px">
        <div class="flex items-center justify-between gap-12px">
          <div>
            <div class="text-18px font-600">{{ purchaseIn.no || `采购入库单 #${purchaseIn.id}` }}</div>
            <div class="mt-6px text-12px text-[var(--el-text-color-secondary)]">
              入库单编号：{{ purchaseIn.id }}
            </div>
          </div>
          <div class="flex items-center gap-8px">
            <dict-tag :type="DICT_TYPE.ERP_AUDIT_STATUS" :value="purchaseIn.status" />
            <el-tag :type="getQaStatusTagType(purchaseIn.qaStatus)">
              {{ getQaStatusLabel(purchaseIn.qaStatus) }}
            </el-tag>
            <el-tag :type="getStockInStatusTagType(purchaseIn)">
              {{ getStockInStatusLabel(purchaseIn) }}
            </el-tag>
          </div>
        </div>
      </ContentWrap>

      <ContentWrap title="基础信息" class="mb-12px">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="入库单号">{{ purchaseIn.no || '-' }}</el-descriptions-item>
          <el-descriptions-item label="入库时间">
            {{ purchaseIn.inTime ? formatDate(purchaseIn.inTime as Date, 'YYYY-MM-DD') : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="供应商">
            {{ purchaseIn.supplierName || purchaseIn.supplierId || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="创建人">{{ purchaseIn.creatorName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="关联采购单">{{ purchaseIn.orderNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="流程实例">
            {{ purchaseIn.processInstanceId || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="合计数量">{{ purchaseIn.totalCount ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="合计金额">
            {{ formatPrice(purchaseIn.totalPrice) }}
          </el-descriptions-item>
          <el-descriptions-item label="已付金额">
            {{ formatPrice(purchaseIn.paymentPrice) }}
          </el-descriptions-item>
          <el-descriptions-item label="最近驳回原因">
            {{ purchaseIn.lastRejectReason || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="质检人">{{ purchaseIn.qaUserNickname || '-' }}</el-descriptions-item>
          <el-descriptions-item label="质检时间">
            {{ purchaseIn.qaTime ? formatDate(purchaseIn.qaTime as Date, 'YYYY-MM-DD HH:mm:ss') : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="合格数">{{ purchaseIn.qaPassCount ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="不合格数">{{ purchaseIn.qaRejectCount ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="入库状态">
            {{ getStockInStatusLabel(purchaseIn) }}
          </el-descriptions-item>
          <el-descriptions-item label="最终入库时间">
            {{ purchaseIn.stockInTime ? formatDate(purchaseIn.stockInTime as Date, 'YYYY-MM-DD HH:mm:ss') : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="最终入库人">
            {{ purchaseIn.stockInUserNickname || purchaseIn.stockInUserId || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="库存生效数量">
            {{ purchaseIn.stockInStatus === STOCK_IN_STATUS.STOCKED_IN ? purchaseIn.qaPassCount ?? 0 : 0 }}
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ purchaseIn.remark || '-' }}</el-descriptions-item>
          <el-descriptions-item label="质检备注" :span="2">{{ purchaseIn.qaRemark || '-' }}</el-descriptions-item>
        </el-descriptions>
      </ContentWrap>

      <ContentWrap title="入库产品" class="mb-12px">
        <el-table :data="purchaseIn.items || []" stripe>
          <el-table-column label="产品名称" prop="productName" min-width="160" />
          <el-table-column label="产品编码" prop="productBarCode" min-width="140" />
          <el-table-column label="仓库" prop="warehouseId" min-width="100" />
          <el-table-column label="数量" prop="count" min-width="100" />
          <el-table-column label="合格数" prop="qaPassCount" min-width="100" />
          <el-table-column label="不合格数" prop="qaRejectCount" min-width="100" />
          <el-table-column label="采购单价" min-width="120">
            <template #default="{ row }">{{ formatPrice(row.productPrice) }}</template>
          </el-table-column>
          <el-table-column label="税额" min-width="120">
            <template #default="{ row }">{{ formatPrice(row.taxPrice) }}</template>
          </el-table-column>
          <el-table-column label="库存" min-width="120">
            <template #default="{ row }">{{ row.stockCount ?? '-' }}</template>
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
            :timestamp="item.createTime ? formatDate(item.createTime as Date, 'YYYY-MM-DD HH:mm:ss') : '-'"
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
              <div v-if="item.taskName" class="mb-4px">
                <span class="text-[var(--el-text-color-secondary)]">节点：</span>
                <span>{{ item.taskName }}</span>
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
import { DICT_TYPE } from '@/utils/dict'
import { PurchaseInApi, PurchaseInAuditLogVO, PurchaseInVO } from '@/api/erp/purchase/in'

defineOptions({ name: 'ErpPurchaseInBpmDetail' })

const props = defineProps<{ id?: number | string }>()
const route = useRoute()
const message = useMessage()

const loading = ref(false)
const purchaseIn = ref<PurchaseInVO>({ items: [] })

const QA_STATUS = {
  TO_INSPECT: 10,
  PARTIAL: 20,
  PASSED: 30,
  REJECTED: 40
} as const
const STOCK_IN_STATUS = {
  TO_STOCK_IN: 10,
  STOCKED_IN: 20
} as const

const actionTextMap: Record<string, string> = {
  APPROVE: '审批通过',
  REJECT: '驳回',
  RESUBMIT: '重新提交',
  CANCEL: '撤回',
  QUALITY_CHECK_PARTIAL: '质检部分合格',
  QUALITY_CHECK_PASSED: '质检全部合格',
  QUALITY_CHECK_REJECTED: '质检全部不合格',
  STOCK_IN_CONFIRMED: '最终入库确认'
}

const displayAuditLogs = computed(() => purchaseIn.value.auditLogs || [])

const loadPurchaseIn = async () => {
  const currentId = Number(props.id || route.query.id || route.params.id)
  if (!currentId) {
    message.warning('采购入库单编号不能为空')
    return
  }
  loading.value = true
  try {
    purchaseIn.value = await PurchaseInApi.getPurchaseIn(currentId)
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

const getQaStatusLabel = (qaStatus?: number) => {
  if (qaStatus === QA_STATUS.PASSED) {
    return '全部合格'
  }
  if (qaStatus === QA_STATUS.PARTIAL) {
    return '部分合格'
  }
  if (qaStatus === QA_STATUS.REJECTED) {
    return '全部不合格'
  }
  return '待质检'
}

const getQaStatusTagType = (qaStatus?: number) => {
  if (qaStatus === QA_STATUS.PASSED) {
    return 'success'
  }
  if (qaStatus === QA_STATUS.PARTIAL) {
    return 'warning'
  }
  if (qaStatus === QA_STATUS.REJECTED) {
    return 'danger'
  }
  return 'info'
}

const getStockInStatusLabel = (data: PurchaseInVO) => {
  if (data.stockInStatus === STOCK_IN_STATUS.STOCKED_IN) {
    return '已入库'
  }
  if (data.stockInStatus === STOCK_IN_STATUS.TO_STOCK_IN) {
    return '待入库'
  }
  if (data.qaStatus === QA_STATUS.REJECTED) {
    return '无需入库'
  }
  if (data.status === 20) {
    return '待质检'
  }
  return '-'
}

const getStockInStatusTagType = (data: PurchaseInVO) => {
  if (data.stockInStatus === STOCK_IN_STATUS.STOCKED_IN) {
    return 'success'
  }
  if (data.stockInStatus === STOCK_IN_STATUS.TO_STOCK_IN) {
    return 'warning'
  }
  return 'info'
}

const formatAuditAction = (actionType?: string) => {
  return (actionType && actionTextMap[actionType]) || actionType || '-'
}

const formatAuditUser = (item: PurchaseInAuditLogVO) => {
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
  if (actionType === 'QUALITY_CHECK_PASSED') {
    return 'success'
  }
  if (actionType === 'QUALITY_CHECK_PARTIAL') {
    return 'warning'
  }
  if (actionType === 'QUALITY_CHECK_REJECTED') {
    return 'danger'
  }
  if (actionType === 'STOCK_IN_CONFIRMED') {
    return 'success'
  }
  return 'info'
}

onMounted(() => {
  loadPurchaseIn()
})
</script>
