<template>
  <el-dialog
    v-model="detailDialogVisible"
    width="1480px"
    top="3vh"
    :show-close="false"
    :close-on-click-modal="true"
    :close-on-press-escape="true"
    :destroy-on-close="true"
    append-to-body
    class="purchase-order-detail-dialog"
    custom-class="purchase-order-detail-dialog"
    modal-class="purchase-order-detail-dialog__mask"
    @closed="handleDialogClosed"
  >
    <div class="purchase-order-dialog-shell">
      <div class="purchase-order-dialog-head">
        <div class="purchase-order-dialog-head__glow"></div>
        <div class="purchase-order-dialog-head__main">
          <div class="purchase-order-dialog-head__icon">
            <Icon icon="ep:document-copy" />
          </div>
          <div class="purchase-order-dialog-head__copy">
            <div class="purchase-order-dialog-head__eyebrow">采购订单详情</div>
            <div class="purchase-order-dialog-head__title-row">
              <h2 class="purchase-order-dialog-head__title">
                采购订单详情:
                <span class="purchase-order-dialog-head__title-code">{{ detailOrderNo }}</span>
              </h2>
              <span class="status-chip" :class="statusChipClass">{{ statusLabel }}</span>
            </div>
            <div class="purchase-order-dialog-head__subline">
              {{ sourceTypeLabel }} · 结算：{{ paymentTermText }}
            </div>
          </div>
        </div>

        <div class="purchase-order-dialog-head__actions">
          <el-button
            v-if="canEdit"
            class="head-action-button"
            :disabled="loadingDetail || navigatingEdit"
            @click="handleEdit"
          >
            <Icon icon="ep:edit" class="mr-6px" />
            修改明细
          </el-button>
          <el-button
            class="head-action-button head-action-button--ghost"
            :loading="printing"
            :disabled="loadingDetail || printing"
            @click="handlePrint"
          >
            <Icon icon="ep:printer" class="mr-6px" />
            打印单据
          </el-button>
          <el-dropdown trigger="click">
            <button type="button" class="head-icon-button">
              <Icon icon="ep:more-filled" />
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="copyOrderNo">复制单号</el-dropdown-item>
                <el-dropdown-item v-if="detailData.processInstanceId" @click="handleOpenProcessDetail">
                  查看审批
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <button type="button" class="head-icon-button" @click="close">
            <Icon icon="ep:close" />
          </button>
        </div>
      </div>

      <div class="purchase-order-dialog-body">
        <div v-if="loadingDetail" class="dialog-state dialog-state--loading">
          <el-skeleton animated :rows="10" />
        </div>

        <div v-else-if="detailLoadFailed" class="dialog-state dialog-state--error">
          <div class="dialog-state__icon">
            <Icon icon="ep:warning-filled" />
          </div>
          <div class="dialog-state__title">详情加载失败</div>
          <div class="dialog-state__desc">请检查网络后重试</div>
          <el-button type="primary" plain @click="retryLoad">重新加载</el-button>
        </div>

        <div v-else-if="detailMissing" class="dialog-state dialog-state--empty">
          <div class="dialog-state__icon">
            <Icon icon="ep:document-remove" />
          </div>
          <div class="dialog-state__title">采购订单不存在或已删除</div>
        </div>

        <div v-else class="purchase-order-dialog-content">
          <div class="purchase-order-metric-grid">
            <div class="metric-card metric-card--cyan">
              <div class="metric-card__main">
                <div class="metric-card__icon">
                  <Icon icon="ep:van" />
                </div>
                <div class="metric-card__copy">
                  <div class="metric-card__label">入库进度 (总数)</div>
                  <div class="metric-card__value-row">
                    <strong>{{ formatCount(detailData.inCount) }}</strong>
                    <span>/ {{ formatCount(detailData.totalCount) }}</span>
                  </div>
                </div>
              </div>
              <div class="metric-card__meta">待入库 {{ inboundPendingText }}</div>
            </div>

            <div class="metric-card metric-card--amber">
              <div class="metric-card__main">
                <div class="metric-card__icon">
                  <Icon icon="ep:refresh-right" />
                </div>
                <div class="metric-card__copy">
                  <div class="metric-card__label">退货进度 (总数)</div>
                  <div class="metric-card__value-row">
                    <strong>{{ formatCount(detailData.returnCount) }}</strong>
                    <span>/ {{ returnBaseText }}</span>
                  </div>
                </div>
              </div>
              <div class="metric-card__meta">{{ returnHintText }}</div>
            </div>

            <div class="metric-card metric-card--blue">
              <div class="metric-card__main">
                <div class="metric-card__icon">
                  <Icon icon="ep:credit-card" />
                </div>
                <div class="metric-card__copy">
                  <div class="metric-card__label">订单含税总额</div>
                  <div class="metric-card__currency">{{ formatCurrency(detailData.totalPrice) }}</div>
                </div>
              </div>
              <div class="metric-card__meta">含税金额</div>
            </div>

            <div class="metric-card metric-card--slate">
              <div class="metric-card__main">
                <div class="metric-card__icon">
                  <Icon icon="ep:user" />
                </div>
                <div class="metric-card__copy">
                  <div class="metric-card__label">业务归属人员</div>
                  <div class="metric-card__person">{{ businessOwnerName }}</div>
                </div>
              </div>
              <div class="metric-card__meta">优惠 {{ discountPercentLabel }}</div>
            </div>
          </div>

          <section class="detail-card detail-card--info">
            <div class="detail-card__header detail-card__header--clickable" @click="toggleInfoExpanded">
              <div class="section-title">
                <Icon icon="ep:document-copy" />
                <span>基础资料信息</span>
              </div>
              <button type="button" class="text-toggle-button">
                {{ infoExpanded ? '收起面板' : '展开面板' }}
              </button>
            </div>

            <div v-show="infoExpanded" class="detail-card__body">
              <div class="base-info-grid">
                <div
                  v-for="item in baseInfoEntries"
                  :key="item.label"
                  class="info-item"
                  :class="{
                    'info-item--highlight': item.highlight,
                    'info-item--mono': item.mono,
                    'info-item--file': item.file
                  }"
                >
                  <label>{{ item.label }}</label>
                  <button
                    v-if="item.file"
                    type="button"
                    class="info-item__value info-item__value--file info-item__file-button"
                    @click="openAttachment"
                  >
                    <span>{{ item.value }}</span>
                    <Icon icon="ep:download" />
                  </button>
                  <div v-else class="info-item__value">{{ item.value }}</div>
                </div>
              </div>

              <div class="detail-card__remark detail-card__remark--plain">
                <span class="detail-card__remark-label">备注：</span>
                <span>{{ detailData.remark || '-' }}</span>
              </div>
            </div>
          </section>

          <section class="detail-card detail-card--content">
            <div class="detail-tabs detail-tabs--toolbar">
              <div class="detail-tabs__nav">
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
              <span class="detail-tabs__meta">税额小计：{{ formatCurrency(detailData.totalTaxPrice) }}</span>
            </div>

            <template v-if="activeTab === 'items'">
              <div v-if="!detailItems.length" class="detail-empty detail-empty--table">
                <div class="dialog-state__icon">
                  <Icon icon="ep:box" />
                </div>
                <div class="dialog-state__title">暂无产品明细</div>
              </div>

              <div v-else class="detail-item-list">
                <div v-for="(row, index) in detailItems" :key="row.id || index" class="detail-item-card">
                  <div class="detail-item-card__accent"></div>

                  <div class="detail-item-card__bar">
                    <div class="detail-item-card__identity">
                      <div class="detail-item-card__index">{{ index + 1 }}</div>
                      <div class="detail-item-card__identity-copy">
                        <div class="detail-item-card__identity-row">
                          <h4 class="detail-item-card__name">{{ row.productName || '-' }}</h4>
                          <span class="status-pill" :class="resolveItemStatusClass(row)">
                            {{ resolveItemStatusLabel(row) }}
                          </span>
                        </div>
                        <div class="detail-item-card__subline">
                          物料编码:
                          <span class="detail-item-card__code">{{ row.productBarCode || '-' }}</span>
                          · 单位: {{ row.productUnitName || '-' }}
                        </div>
                      </div>
                    </div>

                    <div class="detail-item-field">
                      <label>规格型号</label>
                      <div>{{ row.productStandard || '-' }}</div>
                    </div>
                    <div class="detail-item-field detail-item-field--numeric">
                      <label>数量</label>
                      <div>{{ formatCount(row.count) }}</div>
                    </div>
                    <div class="detail-item-field detail-item-field--numeric">
                      <label>含税单价</label>
                      <div>{{ formatCurrency(row.taxIncludedPrice ?? row.productPrice) }}</div>
                    </div>
                    <div class="detail-item-field detail-item-field--numeric">
                      <label>合计价税金额</label>
                      <div class="detail-item-field__accent">{{ formatCurrency(row.totalPrice) }}</div>
                    </div>
                    <div class="detail-item-field detail-item-field--project">
                      <label>项目归属</label>
                      <div class="detail-item-field__project-badge">{{ row.projectName || '-' }}</div>
                    </div>
                  </div>

                  <div class="detail-item-card__footer">
                    <div class="detail-item-card__footer-grid">
                      <span>
                        交货日期：
                        <strong>{{ formatDateValue(row.deliveryDate) }}</strong>
                      </span>
                      <span>
                        付款关联金额：
                        <strong>{{ formatCurrency(row.paymentAllocatedAmount) }}</strong>
                      </span>
                      <span>
                        关联数量：
                        <strong>{{ formatCount(row.relatedCount) }}</strong>
                      </span>
                      <span>
                        开票数量：
                        <strong>{{ formatCount(row.invoicedCount) }}</strong>
                      </span>
                    </div>
                    <div v-if="row.remark" class="detail-item-card__remark">
                      备注：{{ row.remark }}
                    </div>
                  </div>
                </div>
              </div>
            </template>

            <template v-else>
              <div v-if="!displayAuditLogs.length" class="detail-empty detail-empty--table">
                <div class="dialog-state__icon">
                  <Icon icon="ep:document" />
                </div>
                <div class="dialog-state__title">暂无操作日志</div>
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

            <div class="detail-summary-footer">
              <div class="detail-summary-footer__meta">
                <span>
                  无税总计:
                  <strong>{{ formatCurrency(detailData.totalProductPrice) }}</strong>
                </span>
                <span>
                  税额小计:
                  <strong>{{ formatCurrency(detailData.totalTaxPrice) }}</strong>
                </span>
              </div>
              <div class="detail-summary-footer__total">
                <span>最终价税合计:</span>
                <strong>{{ formatCurrency(detailData.totalPrice) }}</strong>
              </div>
            </div>
          </section>
        </div>
      </div>

      <div class="purchase-order-dialog-footer">
        <el-button @click="close">关闭窗口</el-button>
        <div class="purchase-order-dialog-footer__actions">
          <el-button
            v-if="detailData.processInstanceId"
            plain
            :disabled="loadingDetail || printing"
            @click="handleOpenProcessDetail"
          >
            查看审批
          </el-button>
          <el-button plain :loading="printing" :disabled="loadingDetail || printing" @click="handlePrint">
            打印单据
          </el-button>
          <el-button
            v-if="canEdit"
            type="primary"
            :disabled="loadingDetail || navigatingEdit"
            @click="handleEdit"
          >
            修改明细
          </el-button>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { resolveErpAuditStatusTagType } from '@/utils/erpAuditStatus'
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

defineOptions({ name: 'PurchaseOrderDetailDialog' })

const emit = defineEmits<{
  (e: 'edit', id: number): void
}>()

const router = useRouter()
const userStore = useUserStoreWithOut()
const message = useMessage()

const detailDialogVisible = ref(false)
const loadingDetail = ref(false)
const detailLoadFailed = ref(false)
const detailMissing = ref(false)
const printing = ref(false)
const navigatingEdit = ref(false)
const activeTab = ref<'items' | 'logs'>('items')
const infoExpanded = ref(true)
const purchaseOrder = ref<PurchaseOrderVO | null>(null)
const detailRowSnapshot = ref<PurchaseOrderVO>()
const accountList = ref<AccountVO[]>([])
const selectedOrderId = ref<number>()

const currentUserId = computed(() => String(userStore.getUser.id || ''))

const numberFormatter = new Intl.NumberFormat('zh-CN', {
  style: 'currency',
  currency: 'CNY',
  minimumFractionDigits: 2,
  maximumFractionDigits: 2
})

const normalizeNumber = (value?: number | string | null) => Number(value || 0)

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

const detailOrderNo = computed(
  () => detailData.value.no || (detailData.value.id ? `PO-${detailData.value.id}` : '-')
)
const statusLabel = computed(() => formatStatus(detailData.value.status))
const statusChipClass = computed(() => {
  const tagType = resolveErpAuditStatusTagType(
    detailData.value.status,
    detailData.value.processInstanceId
  )
  if (tagType === 'success') {
    return 'status-chip--success'
  }
  if (tagType === 'warning') {
    return 'status-chip--warning'
  }
  if (tagType === 'danger') {
    return 'status-chip--danger'
  }
  return 'status-chip--primary'
})
const sourceTypeLabel = computed(() => formatSourceType(detailData.value.sourceType))
const supplierName = computed(() => detailData.value.supplierName || '-')
const creatorName = computed(() => detailData.value.creatorName || '-')
const businessOwnerName = computed(
  () => detailData.value.businessOwnerName || detailData.value.creatorName || '-'
)
const orderTimeText = computed(() => formatDateValue(detailData.value.orderTime))
const returnBaseText = computed(() => formatCount(detailData.value.inCount || detailData.value.totalCount))
const inboundPendingText = computed(() =>
  formatCount(
    Math.max(normalizeNumber(detailData.value.totalCount) - normalizeNumber(detailData.value.inCount), 0)
  )
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

const fileName = computed(() => {
  if (!detailData.value.fileUrl) {
    return '-'
  }
  const url = detailData.value.fileUrl
  const segments = url.split('/')
  const lastSegment = segments[segments.length - 1]
  return lastSegment || url
})

const baseInfoEntries = computed(() => [
  { label: '订单单号', value: detailOrderNo.value, mono: true },
  { label: '订单时间', value: orderTimeText.value },
  { label: '供应商', value: supplierName.value, highlight: true },
  { label: '创建人员', value: creatorName.value || '-' },
  { label: '业务归属', value: businessOwnerName.value },
  { label: '来源类型', value: sourceTypeLabel.value },
  { label: '交货日期', value: deliveryDateText.value },
  { label: '付款方式', value: paymentTermText.value },
  { label: '关联附件', value: fileName.value, file: detailData.value.fileUrl && fileName.value !== '-' }
])

const displayAuditLogs = computed(() => resolvePurchaseOrderOperationLogs(detailData.value))

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
  const dict = getIntDictOptions(DICT_TYPE.ERP_AUDIT_STATUS).find(
    (item) => Number(item.value) === status
  )
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

const toggleInfoExpanded = () => {
  infoExpanded.value = !infoExpanded.value
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
  detailDialogVisible.value = true
  await loadPurchaseOrder()
}

const close = () => {
  detailDialogVisible.value = false
}

const handleDialogClosed = () => {
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
  infoExpanded.value = true
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
  detailDialogVisible.value = false
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

const openAttachment = () => {
  if (!detailData.value.fileUrl) {
    return
  }
  try {
    window.open(detailData.value.fileUrl, '_blank', 'noopener,noreferrer')
  } catch {
    message.warning('附件打开失败')
  }
}

const handleEdit = () => {
  if (!canEdit.value || !detailData.value.id || navigatingEdit.value) {
    return
  }
  navigatingEdit.value = true
  detailDialogVisible.value = false
  emit('edit', detailData.value.id)
}

defineExpose({
  open,
  close
})
</script>

<style scoped lang="scss">
.purchase-order-dialog-shell {
  display: flex;
  flex-direction: column;
  min-height: 100%;
  background: #f8fafc;
}

.purchase-order-dialog-head {
  position: relative;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
  padding: 20px 24px;
  background: #020617;
  color: #fff;
  overflow: hidden;
}

.purchase-order-dialog-head__glow {
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at top right, rgba(8, 145, 178, 0.18), transparent 58%);
  pointer-events: none;
}

.purchase-order-dialog-head__main,
.purchase-order-dialog-head__actions {
  position: relative;
  z-index: 1;
}

.purchase-order-dialog-head__main {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  min-width: 0;
  flex: 1;
}

.purchase-order-dialog-head__icon {
  width: 46px;
  height: 46px;
  border-radius: 18px;
  background: rgba(6, 182, 212, 0.12);
  border: 1px solid rgba(34, 211, 238, 0.18);
  color: #22d3ee;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 22px;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.04);
}

.purchase-order-dialog-head__copy {
  min-width: 0;
}

.purchase-order-dialog-head__eyebrow {
  color: #cbd5e1;
  font-size: 12px;
  font-weight: 700;
}

.purchase-order-dialog-head__title-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 6px;
}

.purchase-order-dialog-head__title {
  margin: 0;
  color: #fff;
  font-size: 20px;
  font-weight: 800;
  line-height: 1.25;
}

.purchase-order-dialog-head__title-code {
  color: #22d3ee;
  font-family: 'JetBrains Mono', 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  font-weight: 600;
}

.purchase-order-dialog-head__subline {
  margin-top: 8px;
  color: #94a3b8;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.purchase-order-dialog-head__actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.head-action-button,
.head-action-button:deep(.el-button) {
  height: 36px;
}

.head-action-button {
  margin: 0;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.12);
  color: #e2e8f0;
  font-size: 12px;
  font-weight: 800;
  box-shadow: none;
}

.head-action-button--ghost {
  background: rgba(255, 255, 255, 0.06);
}

.head-action-button:hover {
  color: #fff;
  border-color: rgba(255, 255, 255, 0.18);
  background: rgba(255, 255, 255, 0.18);
}

.head-icon-button {
  width: 36px;
  height: 36px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.08);
  color: #cbd5e1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  transition: background-color 0.2s ease, color 0.2s ease;
}

.head-icon-button:hover {
  background: rgba(255, 255, 255, 0.16);
  color: #fff;
}

.status-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 22px;
  padding: 0 10px;
  border-radius: 999px;
  border: 1px solid transparent;
  font-size: 11px;
  font-weight: 800;
}

.status-chip--success {
  background: rgba(16, 185, 129, 0.16);
  color: #6ee7b7;
  border-color: rgba(16, 185, 129, 0.22);
}

.status-chip--primary {
  background: rgba(14, 165, 233, 0.12);
  color: #38bdf8;
  border-color: rgba(14, 165, 233, 0.18);
}

.status-chip--warning {
  background: rgba(245, 158, 11, 0.16);
  color: #fbbf24;
  border-color: rgba(245, 158, 11, 0.22);
}

.status-chip--danger {
  background: rgba(244, 63, 94, 0.16);
  color: #fda4af;
  border-color: rgba(244, 63, 94, 0.22);
}

.status-chip--slate {
  background: rgba(148, 163, 184, 0.12);
  color: #e2e8f0;
  border-color: rgba(148, 163, 184, 0.18);
}

.purchase-order-dialog-body {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 20px 22px 18px;
}

.purchase-order-dialog-content {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.purchase-order-metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.metric-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 16px 18px;
  border-radius: 18px;
  border: 1px solid #e2e8f0;
  background: #fff;
  box-shadow: 0 4px 14px rgba(15, 23, 42, 0.04);
}

.metric-card__main {
  display: flex;
  align-items: center;
  gap: 14px;
}

.metric-card__icon {
  width: 40px;
  height: 40px;
  border-radius: 14px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 18px;
}

.metric-card__copy {
  min-width: 0;
}

.metric-card__label {
  color: #94a3b8;
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.metric-card__value-row {
  display: flex;
  align-items: baseline;
  gap: 4px;
  margin-top: 4px;
}

.metric-card__value-row strong,
.metric-card__currency,
.metric-card__person {
  color: #0f172a;
  font-size: 22px;
  font-weight: 900;
  line-height: 1.1;
}

.metric-card__value-row span {
  color: #94a3b8;
  font-size: 12px;
  font-weight: 700;
}

.metric-card__currency {
  color: #2563eb;
  font-size: 20px;
  font-family: 'JetBrains Mono', 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
}

.metric-card__person {
  font-size: 16px;
}

.metric-card__meta {
  color: #94a3b8;
  font-size: 11px;
  font-weight: 700;
}

.metric-card--cyan .metric-card__icon {
  background: #ecfeff;
  color: #0891b2;
}

.metric-card--amber .metric-card__icon {
  background: #fffbeb;
  color: #d97706;
}

.metric-card--blue .metric-card__icon {
  background: #eff6ff;
  color: #2563eb;
}

.metric-card--slate .metric-card__icon {
  background: #f8fafc;
  color: #475569;
}

.detail-card {
  overflow: hidden;
  border: 1px solid #e2e8f0;
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.04);
}

.detail-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 18px;
  border-bottom: 1px solid #f1f5f9;
  background: rgba(248, 250, 252, 0.5);
}

.detail-card__header--clickable {
  cursor: pointer;
}

.detail-card__body {
  padding: 18px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #0f172a;
  font-size: 13px;
  font-weight: 800;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.text-toggle-button {
  border: none;
  background: transparent;
  color: #0891b2;
  font-size: 11px;
  font-weight: 800;
  cursor: pointer;
}

.base-info-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 18px 22px;
}

.info-item {
  min-width: 0;
}

.info-item label {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 6px;
  color: #94a3b8;
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.info-item__value {
  color: #0f172a;
  font-size: 13px;
  font-weight: 800;
  line-height: 1.55;
  word-break: break-all;
}

.info-item--mono .info-item__value {
  font-family: 'JetBrains Mono', 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
}

.info-item--highlight .info-item__value {
  color: #0891b2;
}

.info-item__value--file {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #0891b2;
}

.info-item__file-button {
  padding: 0;
  border: none;
  background: transparent;
  font: inherit;
  text-align: left;
  cursor: pointer;
}

.detail-card__remark {
  margin-top: 16px;
  padding-top: 14px;
  border-top: 1px solid #f1f5f9;
  color: #64748b;
  font-size: 12px;
  line-height: 1.7;
}

.detail-card__remark-label {
  margin-right: 8px;
  color: #94a3b8;
  font-weight: 700;
}

.detail-tabs {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 0 16px;
  border-bottom: 1px solid #f1f5f9;
  background: rgba(248, 250, 252, 0.35);
}

.detail-tabs__nav {
  display: flex;
  gap: 18px;
}

.detail-tabs__meta {
  color: #94a3b8;
  font-size: 10px;
  font-weight: 800;
}

.detail-tab {
  position: relative;
  border: none;
  background: transparent;
  color: #64748b;
  height: 50px;
  padding: 0;
  font-size: 12px;
  font-weight: 800;
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
  color: #0891b2;
}

.detail-tab--active::after {
  background: #0891b2;
}

.detail-item-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px 18px;
}

.detail-item-card {
  position: relative;
  border: 1px solid #e2e8f0;
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 4px 14px rgba(15, 23, 42, 0.04);
  padding: 8px 14px 8px 18px;
  overflow: hidden;
}

.detail-item-card__accent {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: #06b6d4;
}

.detail-item-card__bar {
  display: grid;
  grid-template-columns: minmax(220px, 1.6fr) repeat(4, minmax(96px, 0.78fr)) minmax(88px, 0.85fr);
  gap: 10px 14px;
  align-items: center;
}

.detail-item-card__identity {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.detail-item-card__index {
  width: 30px;
  height: 30px;
  border-radius: 12px;
  background: #f8fafc;
  color: #94a3b8;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 900;
  font-family: 'JetBrains Mono', 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  flex-shrink: 0;
}

.detail-item-card__identity-copy {
  min-width: 0;
  flex: 1;
}

.detail-item-card__identity-row {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
  flex-wrap: wrap;
}

.detail-item-card__name {
  margin: 0;
  color: #0f172a;
  font-size: 14px;
  font-weight: 800;
  line-height: 1.3;
}

.detail-item-card__subline {
  margin-top: 3px;
  color: #94a3b8;
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 0.02em;
}

.detail-item-card__code {
  color: #475569;
  font-family: 'JetBrains Mono', 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
}

.detail-item-field {
  min-width: 0;
}

.detail-item-field label {
  display: block;
  margin-bottom: 4px;
  color: #94a3b8;
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.detail-item-field div {
  color: #334155;
  font-size: 12px;
  font-weight: 700;
  line-height: 1.35;
  word-break: break-all;
}

.detail-item-field--project {
  text-align: right;
}

.detail-item-field--project label {
  text-align: right;
}

.detail-item-field__project-badge {
  display: inline-flex;
  align-items: center;
  justify-content: flex-end;
  max-width: 100%;
  padding: 3px 8px;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  color: #475569;
  font-size: 11px;
  font-weight: 800;
  line-height: 1.35;
}

.detail-item-field--numeric div,
.detail-item-field__accent {
  color: #0f172a;
  font-family: 'JetBrains Mono', 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  font-weight: 900;
}

.detail-item-field__accent {
  color: #0891b2;
}

.detail-item-card__footer {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px dashed #e2e8f0;
}

.detail-item-card__footer-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 6px 12px;
  color: #64748b;
  font-size: 10px;
  line-height: 1.4;
}

.detail-item-card__footer-grid strong {
  color: #0f172a;
  font-weight: 800;
}

.detail-item-card__remark {
  margin-top: 4px;
  color: #64748b;
  font-size: 10px;
  line-height: 1.5;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 60px;
  min-height: 22px;
  padding: 0 8px;
  border-radius: 999px;
  border: 1px solid transparent;
  font-size: 10px;
  font-weight: 800;
}

.status-pill--primary {
  background: #eff6ff;
  color: #2563eb;
  border-color: #dbeafe;
}

.status-pill--warning {
  background: #fffbeb;
  color: #d97706;
  border-color: #fde68a;
}

.status-pill--success {
  background: #ecfdf5;
  color: #059669;
  border-color: #bbf7d0;
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
  background: #0891b2;
  flex-shrink: 0;
  box-shadow: 0 0 0 6px rgba(8, 145, 178, 0.12);
}

.timeline-item__content {
  flex: 1;
  min-width: 0;
  padding-bottom: 16px;
  border-bottom: 1px solid #eef2f7;
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
  color: #0f172a;
  font-size: 14px;
  font-weight: 800;
}

.timeline-item__time,
.timeline-item__meta,
.timeline-item__desc {
  color: #64748b;
  font-size: 12px;
}

.timeline-item__meta {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 6px;
}

.detail-summary-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px;
  border-top: 1px solid #f1f5f9;
  background: rgba(248, 250, 252, 0.45);
}

.detail-summary-footer__meta {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
  color: #94a3b8;
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.detail-summary-footer__meta strong {
  color: #0f172a;
  margin-left: 6px;
  font-size: 12px;
  font-family: 'JetBrains Mono', 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
}

.detail-summary-footer__total {
  display: flex;
  align-items: baseline;
  gap: 10px;
  color: #94a3b8;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.detail-summary-footer__total strong {
  color: #0f172a;
  font-size: 28px;
  font-weight: 900;
  font-family: 'JetBrains Mono', 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  letter-spacing: 0;
}

.dialog-state {
  min-height: 320px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  text-align: center;
}

.dialog-state__icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: #eff6ff;
  color: #2563eb;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.dialog-state--error .dialog-state__icon {
  background: #fff1f2;
  color: #e11d48;
}

.dialog-state__title {
  color: #0f172a;
  font-size: 16px;
  font-weight: 800;
}

.dialog-state__desc {
  color: #94a3b8;
  font-size: 13px;
}

.purchase-order-dialog-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 22px 20px;
  border-top: 1px solid #e2e8f0;
  background: #fff;
}

.purchase-order-dialog-footer__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: flex-end;
}

:deep(.purchase-order-detail-dialog) {
  width: min(1480px, calc(100vw - 32px));
  max-width: 1480px;
  overflow: hidden;
  border: 1px solid rgba(226, 232, 240, 0.95);
  border-radius: 24px;
  background: #fff;
  box-shadow: 0 24px 64px rgba(15, 23, 42, 0.18);
}

:deep(.purchase-order-detail-dialog .el-dialog__header) {
  display: none;
}

:deep(.purchase-order-detail-dialog .el-dialog__body) {
  padding: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  max-height: min(94vh, 1180px);
}

:deep(.purchase-order-detail-dialog__mask) {
  backdrop-filter: blur(6px);
}

@media (max-width: 1280px) {
  .purchase-order-metric-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .base-info-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .detail-item-card__bar,
  .detail-item-card__footer-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .detail-item-field--project,
  .detail-item-field--project label {
    text-align: left;
  }
}

@media (max-width: 768px) {
  :deep(.purchase-order-detail-dialog) {
    width: calc(100vw - 16px);
    border-radius: 18px;
  }

  .purchase-order-dialog-head,
  .purchase-order-dialog-body,
  .purchase-order-dialog-footer {
    padding-left: 14px;
    padding-right: 14px;
  }

  .purchase-order-dialog-head {
    flex-direction: column;
  }

  .purchase-order-dialog-head__actions {
    width: 100%;
    justify-content: flex-end;
  }

  .purchase-order-metric-grid,
  .base-info-grid,
  .detail-item-card__bar,
  .detail-item-card__footer-grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .detail-item-card__identity {
    align-items: flex-start;
  }

  .detail-tabs {
    flex-direction: column;
    align-items: flex-start;
    padding-top: 10px;
  }

  .detail-tabs__nav {
    width: 100%;
    overflow-x: auto;
  }

  .detail-summary-footer,
  .purchase-order-dialog-footer {
    align-items: stretch;
    flex-direction: column;
  }

  .purchase-order-dialog-footer__actions {
    width: 100%;
  }

  .purchase-order-dialog-footer__actions :deep(.el-button) {
    width: 100%;
  }

  .detail-summary-footer__total strong {
    font-size: 24px;
  }
}
</style>
