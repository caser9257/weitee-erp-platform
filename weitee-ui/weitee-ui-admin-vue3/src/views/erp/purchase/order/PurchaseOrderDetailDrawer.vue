<template>
  <el-drawer
    v-model="detailDrawerVisible"
    :size="detailDrawerSize"
    :with-header="false"
    :close-on-click-modal="true"
    :close-on-press-escape="true"
    :destroy-on-close="true"
    append-to-body
    modal-class="purchase-order-detail-drawer__mask"
    @closed="handleDrawerClosed"
  >
    <div class="purchase-order-drawer-shell">
      <div class="purchase-order-drawer-head">
        <div class="purchase-order-drawer-head__main">
          <div class="purchase-order-drawer-head__eyebrow">采购订单详情</div>
          <div class="purchase-order-drawer-head__title-row">
            <h2 class="purchase-order-drawer-head__title">{{ detailTitle }}</h2>
            <span class="status-chip status-chip--success">{{ statusLabel }}</span>
          </div>
          <div class="purchase-order-drawer-head__meta">
            <span class="purchase-order-drawer-head__mono">{{ detailOrderNo }}</span>
            <span>{{ supplierName }}</span>
            <span>{{ orderTimeText }}</span>
          </div>
          <div class="purchase-order-drawer-head__chips">
            <span class="status-chip status-chip--primary">{{ sourceTypeLabel }}</span>
            <span class="status-chip status-chip--slate">{{ paymentTermText }}</span>
          </div>
        </div>
        <div class="purchase-order-drawer-head__actions">
          <el-dropdown trigger="click">
            <el-button plain circle class="drawer-icon-button">
              <Icon icon="ep:more-filled" />
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="copyOrderNo">复制单号</el-dropdown-item>
                <el-dropdown-item v-if="detailData.processInstanceId" @click="handleOpenProcessDetail">
                  查看审批
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <button type="button" class="drawer-close-button" @click="close">
            <Icon icon="ep:close" />
          </button>
        </div>
      </div>

      <div class="purchase-order-drawer-summary">
        <div class="purchase-order-drawer-summary__metric">
          <div class="purchase-order-drawer-summary__label">入库进度</div>
          <div class="purchase-order-drawer-summary__value">
            {{ formatCount(detailData.inCount) }} / {{ formatCount(detailData.totalCount) }}
          </div>
          <div class="purchase-order-drawer-summary__hint">待入库 {{ inboundPendingText }}</div>
        </div>
        <div class="purchase-order-drawer-summary__metric">
          <div class="purchase-order-drawer-summary__label">退货进度</div>
          <div class="purchase-order-drawer-summary__value">
            {{ formatCount(detailData.returnCount) }} / {{ returnBaseText }}
          </div>
          <div class="purchase-order-drawer-summary__hint">{{ returnHintText }}</div>
        </div>
        <div class="purchase-order-drawer-summary__metric">
          <div class="purchase-order-drawer-summary__label">订单总额</div>
          <div class="purchase-order-drawer-summary__value purchase-order-drawer-summary__value--currency">
            {{ formatCurrency(detailData.totalPrice) }}
          </div>
          <div class="purchase-order-drawer-summary__hint">含税金额</div>
        </div>
        <div class="purchase-order-drawer-summary__metric">
          <div class="purchase-order-drawer-summary__label">业务归属</div>
          <div class="purchase-order-drawer-summary__value purchase-order-drawer-summary__value--stack">
            <span>{{ businessOwnerName }}</span>
            <span class="purchase-order-drawer-summary__hint">优惠 {{ discountPercentLabel }}</span>
          </div>
        </div>
      </div>

      <div class="purchase-order-drawer-body">
        <div v-if="loadingDetail" class="drawer-state drawer-state--loading">
          <el-skeleton animated :rows="10" />
        </div>

        <div v-else-if="detailLoadFailed" class="drawer-state drawer-state--error">
          <div class="drawer-state__icon">
            <Icon icon="ep:warning-filled" />
          </div>
          <div class="drawer-state__title">详情加载失败</div>
          <div class="drawer-state__desc">请检查网络后重试</div>
          <el-button type="primary" plain @click="retryLoad">重新加载</el-button>
        </div>

        <div v-else-if="detailMissing" class="drawer-state drawer-state--empty">
          <div class="drawer-state__icon">
            <Icon icon="ep:document-remove" />
          </div>
          <div class="drawer-state__title">采购订单不存在或已删除</div>
        </div>

        <div v-else class="purchase-order-drawer-content">
          <section class="detail-card">
            <div class="detail-card__header">
              <div class="section-title">
                <Icon icon="ep:document-copy" />
                <span>基础资料</span>
              </div>
            </div>
            <div class="base-info-grid">
              <div class="info-item">
                <label>订单单号</label>
                <div class="info-item__value info-item__value--mono">{{ detailOrderNo }}</div>
              </div>
              <div class="info-item">
                <label>订单时间</label>
                <div class="info-item__value">{{ orderTimeText }}</div>
              </div>
              <div class="info-item">
                <label>供应商</label>
                <div class="info-item__value info-item__value--accent">{{ supplierName }}</div>
              </div>
              <div class="info-item">
                <label>创建人员</label>
                <div class="info-item__value">{{ creatorName }}</div>
              </div>
              <div class="info-item">
                <label>业务归属</label>
                <div class="info-item__value">{{ businessOwnerName }}</div>
              </div>
              <div class="info-item">
                <label>来源类型</label>
                <div class="info-item__value">{{ sourceTypeLabel }}</div>
              </div>
              <div class="info-item">
                <label>交货日期</label>
                <div class="info-item__value">{{ deliveryDateText }}</div>
              </div>
              <div class="info-item">
                <label>付款方式</label>
                <div class="info-item__value">{{ paymentTermText }}</div>
              </div>
            </div>
            <div class="detail-card__remark">
              <span class="detail-card__remark-label">备注：</span>
              <span>{{ detailData.remark || '-' }}</span>
            </div>
          </section>

          <section class="detail-card detail-card--table">
            <div class="detail-tabs">
              <button
                type="button"
                class="detail-tab"
                :class="{ 'detail-tab--active': activeTab === 'items' }"
                @click="activeTab = 'items'"
              >
                产品清单（{{ itemCount }}）
              </button>
              <button
                type="button"
                class="detail-tab"
                :class="{ 'detail-tab--active': activeTab === 'logs' }"
                @click="activeTab = 'logs'"
              >
                操作日志
              </button>
            </div>

            <template v-if="activeTab === 'items'">
              <div v-if="!detailItems.length" class="detail-empty detail-empty--table">
                <div class="drawer-state__icon">
                  <Icon icon="ep:box" />
                </div>
                <div class="drawer-state__title">暂无产品明细</div>
              </div>
              <div v-else class="detail-table-wrap">
                <el-table :data="detailItems" class="detail-table" stripe>
                  <el-table-column label="物料详情" min-width="240">
                    <template #default="{ row }">
                      <div class="product-cell">
                        <div class="product-cell__name">{{ row.productName || '-' }}</div>
                        <div class="product-cell__code">{{ row.productBarCode || '-' }}</div>
                      </div>
                    </template>
                  </el-table-column>
                  <el-table-column label="规格型号" min-width="180">
                    <template #default="{ row }">
                      <div class="spec-cell">
                        <div>{{ row.productStandard || '-' }}</div>
                        <div class="spec-cell__sub">{{ row.projectName || row.productUnitName || '-' }}</div>
                      </div>
                    </template>
                  </el-table-column>
                  <el-table-column label="数量" min-width="120" align="right">
                    <template #default="{ row }">
                      <div class="numeric-cell">
                        <strong>{{ formatCount(row.count) }}</strong>
                      </div>
                    </template>
                  </el-table-column>
                  <el-table-column label="含税单价" min-width="140" align="right">
                    <template #default="{ row }">
                      <div class="numeric-cell">
                        {{ formatCurrency(row.taxIncludedPrice ?? row.productPrice) }}
                      </div>
                    </template>
                  </el-table-column>
                  <el-table-column label="合计金额" min-width="150" align="right">
                    <template #default="{ row }">
                      <div class="numeric-cell numeric-cell--accent">
                        {{ formatCurrency(row.totalPrice) }}
                      </div>
                    </template>
                  </el-table-column>
                  <el-table-column label="状态" min-width="120" align="center">
                    <template #default="{ row }">
                      <span class="status-pill" :class="resolveItemStatusClass(row)">
                        {{ resolveItemStatusLabel(row) }}
                      </span>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </template>

            <template v-else>
              <div v-if="!displayAuditLogs.length" class="detail-empty detail-empty--table">
                <div class="drawer-state__icon">
                  <Icon icon="ep:document" />
                </div>
                <div class="drawer-state__title">暂无操作日志</div>
              </div>
              <div v-else class="timeline-list">
                <div
                  v-for="(item, index) in displayAuditLogs"
                  :key="`${item.actionType}-${index}`"
                  class="timeline-item"
                >
                  <div class="timeline-item__dot"></div>
                  <div class="timeline-item__content">
                    <div class="timeline-item__head">
                      <span class="timeline-item__action">{{ formatAuditAction(item.actionType) }}</span>
                      <span class="timeline-item__time">{{ formatDateTimeValue(item.createTime) }}</span>
                    </div>
                    <div class="timeline-item__meta">
                      <span>操作人：{{ formatAuditUser(item) }}</span>
                      <span v-if="item.taskName">节点：{{ item.taskName }}</span>
                    </div>
                    <div
                      v-if="item.beforeStatus != null || item.afterStatus != null"
                      class="timeline-item__desc"
                    >
                      状态变化：{{ formatStatus(item.beforeStatus) }} -> {{ formatStatus(item.afterStatus) }}
                    </div>
                    <div v-if="item.reason" class="timeline-item__desc">说明：{{ item.reason }}</div>
                  </div>
                </div>
              </div>
            </template>
          </section>
        </div>
      </div>

      <div class="purchase-order-drawer-footer">
        <el-button @click="close">关闭</el-button>
        <div class="purchase-order-drawer-footer__actions">
          <el-button
            v-if="detailData.processInstanceId"
            plain
            :disabled="loadingDetail || printing"
            @click="handleOpenProcessDetail"
          >
            查看审批
          </el-button>
          <el-button plain :loading="printing" :disabled="loadingDetail || printing" @click="handlePrint">
            打印
          </el-button>
          <el-button
            v-if="canEdit"
            type="primary"
            :disabled="loadingDetail || navigatingEdit"
            @click="handleEdit"
          >
            编辑
          </el-button>
        </div>
      </div>
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useWindowSize } from '@vueuse/core'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { checkPermi } from '@/utils/permission'
import { formatDate } from '@/utils/formatTime'
import { useMessage } from '@/hooks/web/useMessage'
import {
  PurchaseOrderApi,
  type PurchaseOrderAuditLogVO,
  type PurchaseOrderItemVO,
  type PurchaseOrderVO
} from '@/api/erp/purchase/order'
import { AccountApi, type AccountVO } from '@/api/erp/finance/account'
import { getPurchaseOrderRowActionDescriptor } from './purchaseOrderStatus.helpers'
import { resolvePurchaseOrderOperationLogs } from './auditLogUtils'
import { useUserStoreWithOut } from '@/store/modules/user'

defineOptions({ name: 'PurchaseOrderDetailDrawer' })

const emit = defineEmits<{
  (e: 'edit', id: number): void
}>()

const router = useRouter()
const userStore = useUserStoreWithOut()
const message = useMessage()
const { width } = useWindowSize()

const detailDrawerVisible = ref(false)
const loadingDetail = ref(false)
const detailLoadFailed = ref(false)
const detailMissing = ref(false)
const printing = ref(false)
const navigatingEdit = ref(false)
const activeTab = ref<'items' | 'logs'>('items')
const purchaseOrder = ref<PurchaseOrderVO | null>(null)
const detailRowSnapshot = ref<Pick<PurchaseOrderVO, 'id'> & Partial<PurchaseOrderVO>>()
const accountList = ref<AccountVO[]>([])
const selectedOrderId = ref<number>()

const detailDrawerSize = computed(() =>
  width.value < 1024 ? Math.round(width.value * 0.92) : 520
)
const currentUserId = computed(() => String(userStore.getUser.id || ''))

const numberFormatter = new Intl.NumberFormat('zh-CN', {
  style: 'currency',
  currency: 'CNY',
  minimumFractionDigits: 2,
  maximumFractionDigits: 2
})

const actionState = computed(() =>
  getPurchaseOrderRowActionDescriptor({
    status: detailData.value.status,
    processInstanceId: detailData.value.processInstanceId,
    creator: detailData.value.creator,
    currentUserId: currentUserId.value,
    totalCount: detailData.value.totalCount,
    inCount: detailData.value.inCount,
    hasPendingPurchaseIn: detailData.value.hasPendingPurchaseIn,
    pendingPurchaseInId: detailData.value.pendingPurchaseInId
  })
)

const canEdit = computed(() => checkPermi(['erp:purchase-order:update']) && actionState.value.canEdit)

const detailData = computed(() => ({
  ...(detailRowSnapshot.value || {}),
  ...(purchaseOrder.value || {}),
  items: purchaseOrder.value?.items || detailRowSnapshot.value?.items || [],
  operationLogs: purchaseOrder.value?.operationLogs || detailRowSnapshot.value?.operationLogs || [],
  approvalLogs: purchaseOrder.value?.approvalLogs || detailRowSnapshot.value?.approvalLogs || [],
  auditLogs: purchaseOrder.value?.auditLogs || detailRowSnapshot.value?.auditLogs || [],
  rejectLogs: purchaseOrder.value?.rejectLogs || detailRowSnapshot.value?.rejectLogs || []
} as PurchaseOrderVO))

const detailItems = computed(() => detailData.value.items || [])
const itemCount = computed(() => detailItems.value.length)
const detailTitle = computed(() => {
  if (detailData.value.no) {
    return detailData.value.no
  }
  if (detailData.value.id) {
    return `PO-${detailData.value.id}`
  }
  return '采购订单详情'
})
const detailOrderNo = computed(() => detailData.value.no || (detailData.value.id ? `PO-${detailData.value.id}` : '-'))
const statusLabel = computed(() => formatStatus(detailData.value.status))
const sourceTypeLabel = computed(() => formatSourceType(detailData.value.sourceType))
const supplierName = computed(() => detailData.value.supplierName || '-')
const creatorName = computed(() => detailData.value.creatorName || '-')
const businessOwnerName = computed(() => detailData.value.businessOwnerName || detailData.value.creatorName || '-')
const orderTimeText = computed(() => formatDateValue(detailData.value.orderTime))
const returnBaseText = computed(() => formatCount(detailData.value.inCount || detailData.value.totalCount))
const inboundPendingText = computed(() =>
  formatCount(Math.max(normalizeNumber(detailData.value.totalCount) - normalizeNumber(detailData.value.inCount), 0))
)
const returnHintText = computed(() => (normalizeNumber(detailData.value.returnCount) > 0 ? '退货中' : '未退货'))
const paymentTermText = computed(() => {
  const currentAccount = accountList.value.find((item) => item.id === detailData.value.accountId)
  if (currentAccount?.name) {
    return currentAccount.name
  }
  if (detailData.value.accountId) {
    return `账户 #${detailData.value.accountId}`
  }
  return '待确认'
})
const discountPercentLabel = computed(() => {
  const value = normalizeNumber(detailData.value.discountPercent)
  if (value <= 0) {
    return '0.00%'
  }
  return `${value.toFixed(2).replace(/\.?0+$/, '')}%`
})
const deliveryDateText = computed(() => {
  const deliveryValues = detailItems.value
    .map((item) => item.deliveryDate)
    .filter((item): item is Date | string => Boolean(item))
  if (!deliveryValues.length) {
    return '-'
  }
  const formattedDates = Array.from(
    new Set(
      deliveryValues
        .map((item) => formatDateValue(item))
        .filter((item) => item && item !== '-')
    )
  )
  if (!formattedDates.length) {
    return '-'
  }
  if (formattedDates.length === 1) {
    return formattedDates[0]
  }
  return `${formattedDates[0]} 等 ${formattedDates.length} 个日期`
})
const displayAuditLogs = computed(() => resolvePurchaseOrderOperationLogs(detailData.value))
const normalizeNumber = (value?: number | string | null) => Number(value || 0)

const formatCount = (value?: number | string | null) => {
  const numberValue = normalizeNumber(value)
  if (Number.isInteger(numberValue)) {
    return numberValue.toLocaleString('zh-CN')
  }
  return numberValue.toFixed(3).replace(/\.?0+$/, '')
}

const formatCurrency = (value?: number | string | null) => numberFormatter.format(normalizeNumber(value))

const formatDateValue = (value?: Date | string | number) =>
  value ? formatDate(value as Date, 'YYYY-MM-DD') : '-'

const formatDateTimeValue = (value?: Date | string | number) =>
  value ? formatDate(value as Date, 'YYYY-MM-DD HH:mm:ss') : '-'

const formatSourceType = (value?: string) => {
  if (value === 'MRP') {
    return 'MRP 转单'
  }
  if (value === 'MANUAL') {
    return '手工下单'
  }
  return value || '-'
}

const formatAuditAction = (actionType?: string) =>
  (actionType === 'CREATE' && '创建') ||
  (actionType === 'UPDATE' && '修改') ||
  (actionType === 'DELETE' && '删除') ||
  (actionType === 'APPROVE' && '审批通过') ||
  (actionType === 'REJECT' && '驳回') ||
  (actionType === 'RESUBMIT' && '重新提交') ||
  (actionType === 'REVERSE_APPROVE' && '反审批') ||
  (actionType === 'CANCEL' && '撤回') ||
  actionType ||
  '-'

const formatAuditUser = (item: PurchaseOrderAuditLogVO) =>
  item.operatorNickname || item.operatorName || '未知'

const formatStatus = (status?: number) => {
  if (status == null) {
    return '-'
  }
  const dict = getIntDictOptions(DICT_TYPE.ERP_AUDIT_STATUS).find((item) => Number(item.value) === status)
  return dict?.label || String(status)
}

const resolveItemStatusLabel = (item: PurchaseOrderItemVO) => {
  const totalCount = normalizeNumber(item.count)
  const inCount = normalizeNumber(item.inCount)
  if (totalCount > 0 && inCount >= totalCount) {
    return '已入库'
  }
  if (inCount > 0) {
    return '部分入库'
  }
  return '待入库'
}

const resolveItemStatusClass = (item: PurchaseOrderItemVO) => {
  const status = resolveItemStatusLabel(item)
  if (status === '已入库') {
    return 'status-pill--success'
  }
  if (status === '部分入库') {
    return 'status-pill--warning'
  }
  return 'status-pill--primary'
}

const retryLoad = async () => {
  if (!selectedOrderId.value || loadingDetail.value) {
    return
  }
  await loadPurchaseOrder()
}

const loadPurchaseOrder = async () => {
  if (!selectedOrderId.value) {
    detailMissing.value = true
    return
  }
  loadingDetail.value = true
  detailLoadFailed.value = false
  detailMissing.value = false
  try {
    const [detailResult, accountResult] = await Promise.allSettled([
      PurchaseOrderApi.getPurchaseOrder(selectedOrderId.value),
      AccountApi.getAccountSimpleList()
    ])
    accountList.value = accountResult.status === 'fulfilled' ? accountResult.value || [] : []
    if (detailResult.status === 'rejected') {
      detailLoadFailed.value = true
      return
    }
    if (!detailResult.value) {
      purchaseOrder.value = null
      detailMissing.value = true
      return
    }
    purchaseOrder.value = detailResult.value
  } catch {
    detailLoadFailed.value = true
  } finally {
    loadingDetail.value = false
  }
}

const open = async (row: Pick<PurchaseOrderVO, 'id'> & Partial<PurchaseOrderVO>) => {
  if (!row?.id) {
    return
  }
  resetDetailState()
  detailRowSnapshot.value = row
  selectedOrderId.value = row.id
  detailDrawerVisible.value = true
  await loadPurchaseOrder()
}

const close = () => {
  detailDrawerVisible.value = false
}

const handleDrawerClosed = () => {
  resetDetailState()
}

const resetDetailState = () => {
  detailRowSnapshot.value = undefined
  selectedOrderId.value = undefined
  purchaseOrder.value = null
  accountList.value = []
  loadingDetail.value = false
  detailLoadFailed.value = false
  detailMissing.value = false
  printing.value = false
  navigatingEdit.value = false
  activeTab.value = 'items'
}

const handlePrint = async () => {
  if (printing.value || loadingDetail.value) {
    return
  }
  printing.value = true
  try {
    window.print()
  } catch {
    message.error('打印失败')
  } finally {
    printing.value = false
  }
}

const handleOpenProcessDetail = async () => {
  if (!detailData.value.processInstanceId) {
    return
  }
  detailDrawerVisible.value = false
  await router.push({
    name: 'BpmProcessInstanceDetail',
    query: {
      id: detailData.value.processInstanceId
    }
  })
}

const copyOrderNo = async () => {
  if (!detailData.value.no) {
    return
  }
  try {
    await navigator.clipboard.writeText(detailData.value.no)
    message.success('单号已复制')
  } catch {
    message.warning('当前环境暂不支持复制单号')
  }
}

const handleEdit = () => {
  if (!canEdit.value || !detailData.value.id || navigatingEdit.value) {
    return
  }
  navigatingEdit.value = true
  detailDrawerVisible.value = false
  emit('edit', detailData.value.id)
}

defineExpose({
  open,
  close
})
</script>

<style scoped lang="scss">
.purchase-order-drawer-shell {
  display: flex;
  flex-direction: column;
  min-height: 100%;
  background: var(--erp-slate-50);
}

.purchase-order-drawer-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px;
  background: var(--erp-slate-900);
  color: var(--erp-surface-white);
}

.purchase-order-drawer-head__main {
  min-width: 0;
  flex: 1;
}

.purchase-order-drawer-head__eyebrow {
  color: rgb(226 232 240 / 88%);
  font-size: 12px;
  font-weight: 600;
}

.purchase-order-drawer-head__title-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 6px;
}

.purchase-order-drawer-head__title {
  margin: 0;
  color: var(--erp-surface-white);
  font-size: 22px;
  line-height: 1.25;
  font-weight: 700;
}

.purchase-order-drawer-head__meta,
.purchase-order-drawer-head__chips {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.purchase-order-drawer-head__meta {
  margin-top: 10px;
  color: rgb(226 232 240 / 82%);
  font-size: 12px;
}

.purchase-order-drawer-head__mono {
  font-family: 'JetBrains Mono', 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
}

.purchase-order-drawer-head__chips {
  margin-top: 12px;
}

.purchase-order-drawer-head__actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.purchase-order-drawer-summary {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  padding: 16px 20px;
  background: #fff;
  border-bottom: 1px solid var(--erp-slate-100);
}

.purchase-order-drawer-summary__metric {
  min-width: 0;
  padding: 12px;
  border: 1px solid var(--erp-slate-100);
  border-radius: 12px;
  background: var(--erp-slate-50);
}

.purchase-order-drawer-summary__label {
  color: var(--erp-slate-500);
  font-size: 12px;
  font-weight: 600;
}

.purchase-order-drawer-summary__value {
  margin-top: 6px;
  color: var(--erp-slate-900);
  font-size: 18px;
  line-height: 1.3;
  font-weight: 700;
}

.purchase-order-drawer-summary__value--currency {
  color: var(--erp-teal-600);
  font-family: 'JetBrains Mono', 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
}

.purchase-order-drawer-summary__value--stack {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 15px;
}

.purchase-order-drawer-summary__hint {
  margin-top: 4px;
  color: var(--erp-slate-400);
  font-size: 12px;
}

.purchase-order-drawer-body {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 16px 20px 18px;
}

.purchase-order-drawer-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-card {
  overflow: hidden;
  border: 1px solid var(--erp-slate-200);
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.03);
}

.detail-card__header {
  padding: 18px 20px 0;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--erp-slate-900);
  font-size: 16px;
  font-weight: 700;
}

.base-info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20px 18px;
  padding: 20px 20px 18px;
}

.info-item {
  min-width: 0;
}

.info-item label {
  display: block;
  margin-bottom: 8px;
  color: var(--erp-slate-400);
  font-size: 12px;
}

.info-item__value {
  color: var(--erp-slate-800);
  font-size: 14px;
  font-weight: 600;
  line-height: 1.5;
  word-break: break-all;
}

.info-item__value--mono {
  font-family: 'JetBrains Mono', 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
}

.info-item__value--accent {
  color: var(--erp-teal-600);
}

.detail-card__remark {
  border-top: 1px solid var(--erp-slate-100);
  padding: 16px 20px 18px;
  color: var(--erp-slate-500);
  font-size: 13px;
  line-height: 1.7;
}

.detail-card__remark-label {
  margin-right: 8px;
  color: var(--erp-slate-400);
}

.detail-tabs {
  display: flex;
  gap: 18px;
  border-bottom: 1px solid var(--erp-slate-100);
  padding: 0 20px;
}

.detail-tab {
  position: relative;
  border: none;
  background: transparent;
  color: var(--erp-slate-500);
  height: 48px;
  padding: 0;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: color 0.2s ease;
}

.detail-tab::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 2px;
  border-radius: 999px;
  background: transparent;
  transition: background-color 0.2s ease;
}

.detail-tab--active {
  color: var(--erp-teal-500);
}

.detail-tab--active::after {
  background: var(--erp-teal-500);
}

.detail-table-wrap {
  padding: 0 20px;
  overflow-x: auto;
}

.detail-table {
  --el-table-border-color: var(--erp-slate-100);
  --el-table-row-hover-bg-color: var(--erp-slate-50);
  --el-table-header-bg-color: var(--erp-slate-50);
  --el-table-current-row-bg-color: var(--erp-slate-50);
}

.detail-table :deep(.el-table__header-wrapper th) {
  background: var(--erp-slate-50);
  color: var(--erp-slate-400);
  font-weight: 600;
  height: 46px;
}

.detail-table :deep(.el-table__header-wrapper .cell) {
  font-size: 12px;
}

.detail-table :deep(.el-table td),
.detail-table :deep(.el-table th) {
  border-bottom-color: var(--erp-slate-100);
}

.detail-table :deep(.el-table__row td) {
  padding-top: 18px;
  padding-bottom: 18px;
}

.detail-table :deep(.el-table::before) {
  display: none;
}

.product-cell,
.spec-cell {
  min-width: 0;
}

.product-cell__name {
  color: var(--erp-slate-900);
  font-size: 14px;
  font-weight: 600;
  line-height: 1.4;
}

.product-cell__code,
.spec-cell__sub {
  margin-top: 4px;
  color: var(--erp-slate-400);
  font-size: 12px;
}

.product-cell__code {
  font-family: 'JetBrains Mono', 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
}

.numeric-cell {
  color: var(--erp-slate-600);
  font-family: 'JetBrains Mono', 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  font-size: 13px;
}

.numeric-cell--accent {
  color: var(--erp-teal-600);
  font-weight: 700;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 68px;
  min-height: 24px;
  padding: 0 10px;
  border-radius: 999px;
  border: 1px solid transparent;
  font-size: 12px;
  font-weight: 600;
}

.status-pill--primary {
  background: var(--erp-primary-50);
  color: var(--erp-primary-600);
  border-color: var(--erp-primary-100);
}

.status-pill--warning {
  background: var(--erp-warning-50);
  color: var(--erp-warning-600);
  border-color: var(--erp-warning-100);
}

.status-pill--success {
  background: var(--erp-success-50);
  color: var(--erp-success-600);
  border-color: var(--erp-success-100);
}

.status-pill--slate {
  background: var(--erp-slate-50);
  color: var(--erp-slate-600);
  border-color: var(--erp-slate-200);
}

.timeline-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 20px;
}

.timeline-item {
  display: flex;
  gap: 14px;
}

.timeline-item__dot {
  width: 10px;
  height: 10px;
  margin-top: 8px;
  border-radius: 999px;
  background: var(--erp-teal-600);
  flex-shrink: 0;
  box-shadow: 0 0 0 6px rgba(14, 165, 198, 0.12);
}

.timeline-item__content {
  flex: 1;
  min-width: 0;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--erp-slate-100);
}

.timeline-item:last-child .timeline-item__content {
  border-bottom: none;
  padding-bottom: 0;
}

.timeline-item__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}

.timeline-item__action {
  color: var(--erp-slate-900);
  font-size: 15px;
  font-weight: 700;
}

.timeline-item__time,
.timeline-item__meta,
.timeline-item__desc {
  color: var(--erp-slate-500);
  font-size: 13px;
}

.timeline-item__meta {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 6px;
}

.drawer-state {
  min-height: 260px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  text-align: center;
}

.drawer-state__icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: var(--erp-primary-50);
  color: var(--erp-primary-600);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.drawer-state--error .drawer-state__icon {
  background: var(--erp-danger-50);
  color: var(--erp-danger-600);
}

.drawer-state__title {
  color: var(--erp-slate-900);
  font-size: 16px;
  font-weight: 700;
}

.drawer-state__desc {
  color: var(--erp-slate-400);
  font-size: 13px;
}

.purchase-order-drawer-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 20px 20px;
  border-top: 1px solid var(--erp-slate-100);
  background: #fff;
}

.purchase-order-drawer-footer__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: flex-end;
}

.drawer-icon-button,
.drawer-close-button {
  width: 36px;
  height: 36px;
  padding: 0;
  border-radius: 10px;
  border: 1px solid var(--erp-slate-600);
  color: #fff;
  background: rgba(255, 255, 255, 0.08);
}

.drawer-icon-button {
  border-color: rgba(255, 255, 255, 0.18);
}

.drawer-close-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

@media (max-width: 1024px) {
  .purchase-order-drawer-summary,
  .base-info-grid {
    grid-template-columns: 1fr;
  }

  .purchase-order-drawer-footer {
    align-items: stretch;
    flex-direction: column;
  }

  .purchase-order-drawer-footer__actions {
    width: 100%;
    justify-content: stretch;
  }

  .purchase-order-drawer-footer__actions :deep(.el-button) {
    width: 100%;
  }
}

@media (max-width: 640px) {
  .purchase-order-drawer-head,
  .purchase-order-drawer-summary,
  .purchase-order-drawer-body,
  .purchase-order-drawer-footer {
    padding-left: 12px;
    padding-right: 12px;
  }

  .purchase-order-drawer-head {
    flex-direction: column;
  }

  .purchase-order-drawer-head__actions {
    width: 100%;
    justify-content: flex-end;
  }

  .purchase-order-drawer-head__title {
    font-size: 20px;
  }

  .purchase-order-drawer-summary__value {
    font-size: 16px;
  }

  .detail-tabs {
    gap: 16px;
  }
}
</style>

<style lang="scss">
.purchase-order-detail-drawer__mask {
  backdrop-filter: blur(4px);
}

.purchase-order-detail-drawer__mask .el-drawer__body {
  display: flex;
  flex-direction: column;
  padding: 0;
  overflow: hidden;
}
</style>
