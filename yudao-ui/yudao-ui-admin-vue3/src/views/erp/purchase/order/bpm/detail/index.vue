<template>
  <div class="purchase-order-detail-page" v-loading="loadingDetail">
    <template v-if="detailMissing">
      <div class="detail-empty-card">
        <el-empty description="采购订单不存在或已删除" />
      </div>
    </template>
    <template v-else-if="detailLoadFailed">
      <div class="detail-empty-card">
        <div class="detail-empty detail-empty--error">
          <div class="detail-empty__icon">
            <Icon icon="ep:warning-filled" />
          </div>
          <div class="detail-empty__title">详情加载失败</div>
          <div class="detail-empty__desc">请检查网络后重试</div>
          <el-button type="primary" @click="loadPurchaseOrder">重新加载</el-button>
        </div>
      </div>
    </template>
    <template v-else>
      <section class="detail-shell">
        <div class="detail-topbar">
          <div class="detail-topbar__left">
            <el-button circle plain class="detail-icon-button" @click="handleBack">
              <Icon icon="ep:arrow-left" />
            </el-button>
            <div class="detail-heading">
              <div class="detail-heading__title-row">
                <h1 class="detail-heading__title">{{ detailTitle }}</h1>
                <span class="status-chip status-chip--success">
                  {{ statusLabel }}
                </span>
              </div>
            </div>
          </div>
          <div class="detail-topbar__actions">
            <el-button
              v-if="canEdit"
              type="primary"
              class="detail-primary-button"
              :disabled="navigatingEdit"
              @click="handleEdit"
            >
              <Icon icon="ep:edit-pen" class="mr-6px" />
              编辑
            </el-button>
            <el-button
              plain
              class="detail-secondary-button"
              :loading="printing"
              :disabled="loadingDetail || printing"
              @click="handlePrint"
            >
              <Icon icon="ep:printer" class="mr-6px" />
              打印
            </el-button>
            <el-dropdown trigger="click">
              <el-button plain circle class="detail-icon-button">
                <Icon icon="ep:more-filled" />
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    v-if="detailData.processInstanceId"
                    @click="handleOpenProcessDetail"
                  >
                    查看审批
                  </el-dropdown-item>
                  <el-dropdown-item @click="copyOrderNo">复制单号</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>

        <div class="summary-grid">
          <article class="summary-card">
            <div class="summary-card__head">
              <div class="summary-card__icon summary-card__icon--cyan">
                <Icon icon="ep:van" />
              </div>
              <div class="summary-card__label-group">
                <div class="summary-card__label">入库进度</div>
                <div class="summary-card__hint">已入库 / 采购数量</div>
              </div>
              <div
                class="summary-card__ring summary-card__ring--cyan"
                :style="getProgressRingStyle(inboundPercent, '#06b6d4')"
              >
                <span>{{ inboundPercent }}%</span>
              </div>
            </div>
            <div class="summary-card__value-line">
              <strong>{{ formatCount(detailData.inCount) }}</strong>
              <span>/ {{ formatCount(detailData.totalCount) }}</span>
            </div>
            <div class="summary-card__foot">
              待入库 {{ formatCount(Math.max(normalizeNumber(detailData.totalCount) - normalizeNumber(detailData.inCount), 0)) }}
            </div>
          </article>

          <article class="summary-card">
            <div class="summary-card__head">
              <div class="summary-card__icon summary-card__icon--amber">
                <Icon icon="ep:refresh-left" />
              </div>
              <div class="summary-card__label-group">
                <div class="summary-card__label">退货进度</div>
                <div class="summary-card__hint">退货数量 / 已入库数量</div>
              </div>
              <div
                class="summary-card__ring summary-card__ring--amber"
                :style="getProgressRingStyle(returnPercent, '#f59e0b')"
              >
                <span>{{ returnPercent }}%</span>
              </div>
            </div>
            <div class="summary-card__value-line">
              <strong>{{ formatCount(detailData.returnCount) }}</strong>
              <span>{{ returnHintText }}</span>
            </div>
            <div class="summary-card__foot">
              当前{{ returnHintText }}
            </div>
          </article>

          <article class="summary-card">
            <div class="summary-card__head">
              <div class="summary-card__icon summary-card__icon--blue">
                <Icon icon="ep:money" />
              </div>
              <div class="summary-card__label-group">
                <div class="summary-card__label">订单总额</div>
                <div class="summary-card__hint">采购订单含税金额</div>
              </div>
            </div>
            <div class="summary-card__value-line summary-card__value-line--single">
              <strong>{{ formatCurrency(detailData.totalPrice) }}</strong>
            </div>
            <div class="summary-card__foot">含税金额</div>
          </article>

          <article class="summary-card">
            <div class="summary-card__head">
              <div class="summary-card__icon summary-card__icon--slate">
                <Icon icon="ep:wallet" />
              </div>
              <div class="summary-card__label-group">
                <div class="summary-card__label">结算账户</div>
                <div class="summary-card__hint">付款账户 / 折扣信息</div>
              </div>
            </div>
            <div class="summary-card__value-line summary-card__value-line--stack">
              <strong>{{ accountName }}</strong>
              <span>优惠 {{ discountPercentLabel }}</span>
            </div>
            <div class="summary-card__foot">付款方式：{{ paymentTermText }}</div>
          </article>
        </div>

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
              <div class="info-item__value info-item__value--mono">{{ detailData.no || '-' }}</div>
            </div>
            <div class="info-item">
              <label>订单时间</label>
              <div class="info-item__value">{{ formatDateValue(detailData.orderTime) }}</div>
            </div>
            <div class="info-item">
              <label>供应商</label>
              <div class="info-item__value info-item__value--accent">
                {{ detailData.supplierName || '-' }}
              </div>
            </div>
            <div class="info-item">
              <label>创建人员</label>
              <div class="info-item__value">{{ detailData.creatorName || '-' }}</div>
            </div>
            <div class="info-item">
              <label>业务归属</label>
              <div class="info-item__value">
                {{ detailData.businessOwnerName || detailData.creatorName || '-' }}
              </div>
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
              <div class="detail-empty__icon">
                <Icon icon="ep:box" />
              </div>
              <div class="detail-empty__title">暂无产品明细</div>
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
                    <div class="numeric-cell">{{ formatCurrency(row.taxIncludedPrice ?? row.productPrice) }}</div>
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
              <div class="detail-empty__icon">
                <Icon icon="ep:document" />
              </div>
              <div class="detail-empty__title">暂无操作日志</div>
            </div>
            <div v-else class="timeline-list">
              <div v-for="(item, index) in displayAuditLogs" :key="`${item.actionType}-${index}`" class="timeline-item">
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

          <div class="detail-summary-bar">
            <div class="detail-summary-bar__meta">
              <span>显示 {{ itemCount }} 条记录</span>
              <span>平均交付时效：{{ averageDeliveryLeadTime }}</span>
            </div>
            <div class="detail-summary-bar__total">
              <span>合计金额：</span>
              <strong>{{ formatCurrency(detailData.totalPrice) }}</strong>
            </div>
          </div>
        </section>
      </section>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { formatDate } from '@/utils/formatTime'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { checkPermi } from '@/utils/permission'
import { AccountApi, type AccountVO } from '@/api/erp/finance/account'
import {
  PurchaseOrderApi,
  type PurchaseOrderAuditLogVO,
  type PurchaseOrderItemVO,
  type PurchaseOrderRejectLogVO,
  type PurchaseOrderVO
} from '@/api/erp/purchase/order'
import { getPurchaseOrderRowActionDescriptor } from '@/views/erp/purchase/order/purchaseOrderStatus.helpers'
import { useUserStoreWithOut } from '@/store/modules/user'

defineOptions({ name: 'ErpPurchaseOrderBpmDetail' })

const props = defineProps<{ id?: number | string }>()
const route = useRoute()
const router = useRouter()
const message = useMessage()
const userStore = useUserStoreWithOut()

const loadingDetail = ref(true)
const detailLoadFailed = ref(false)
const detailMissing = ref(false)
const printing = ref(false)
const navigatingEdit = ref(false)
const activeTab = ref<'items' | 'logs'>('items')
const purchaseOrder = ref<PurchaseOrderVO | null>(null)
const accountList = ref<AccountVO[]>([])

const actionTextMap: Record<string, string> = {
  APPROVE: '审批通过',
  REJECT: '驳回',
  RESUBMIT: '重新提交',
  REVERSE_APPROVE: '反审批',
  CANCEL: '撤回'
}

const numberFormatter = new Intl.NumberFormat('zh-CN', {
  style: 'currency',
  currency: 'CNY',
  minimumFractionDigits: 2,
  maximumFractionDigits: 2
})

const detailData = computed(() => purchaseOrder.value || ({ items: [] } as PurchaseOrderVO))
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

const displayAuditLogs = computed(() => {
  const data = detailData.value
  if (data.auditLogs?.length) {
    return data.auditLogs
  }
  return (data.rejectLogs || []).map((item: PurchaseOrderRejectLogVO) => ({
    actionType: 'REJECT',
    reason: item.reason,
    operatorId: item.rejectUserId,
    operatorName: item.rejectUserName,
    operatorNickname: item.rejectUserNickname || item.rejectUserName || item.creatorName,
    createTime: item.rejectTime || item.createTime
  })) as PurchaseOrderAuditLogVO[]
})

const currentUserId = computed(() => String(userStore.getUser.id || ''))
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

const canEdit = computed(
  () => checkPermi(['erp:purchase-order:update']) && actionState.value.canEdit
)

const statusLabel = computed(() => formatStatus(detailData.value.status))
const sourceTypeLabel = computed(() => formatSourceType(detailData.value.sourceType))
const inboundPercent = computed(() => getProgressPercent(detailData.value.inCount, detailData.value.totalCount))
const returnPercent = computed(() =>
  getProgressPercent(detailData.value.returnCount, detailData.value.inCount || detailData.value.totalCount)
)
const returnHintText = computed(() => {
  const hasReturn = normalizeNumber(detailData.value.returnCount) > 0
  return hasReturn ? '退货中' : '未退货'
})
const accountName = computed(() => {
  const currentAccount = accountList.value.find((item) => item.id === detailData.value.accountId)
  return currentAccount?.name || (detailData.value.accountId ? `账户 #${detailData.value.accountId}` : '-')
})
const discountPercentLabel = computed(() => {
  const value = normalizeNumber(detailData.value.discountPercent)
  if (value <= 0) {
    return '0.00%'
  }
  return `${value.toFixed(2).replace(/\.?0+$/, '')}%`
})
const paymentTermText = computed(() => {
  const account = accountName.value
  if (account !== '-') {
    return account
  }
  return '待确认'
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
const averageDeliveryLeadTime = computed(() => {
  const leadTimes = detailItems.value
    .map((item) => calculateLeadTimeDays(detailData.value.orderTime, item.deliveryDate))
    .filter((item): item is number => item !== null)
  if (!leadTimes.length) {
    return '-'
  }
  const average = leadTimes.reduce((sum, current) => sum + current, 0) / leadTimes.length
  return `${average.toFixed(1).replace(/\.0$/, '')}天`
})

const loadPurchaseOrder = async () => {
  const currentId = Number(props.id || route.query.id || route.params.id)
  if (!currentId) {
    message.warning('采购订单编号不能为空')
    detailMissing.value = true
    return
  }
  loadingDetail.value = true
  detailLoadFailed.value = false
  detailMissing.value = false
  try {
    const [detail, accounts] = await Promise.all([
      PurchaseOrderApi.getPurchaseOrder(currentId),
      AccountApi.getAccountSimpleList()
    ])
    accountList.value = accounts || []
    if (!detail) {
      purchaseOrder.value = null
      detailMissing.value = true
      return
    }
    purchaseOrder.value = detail
  } catch {
    detailLoadFailed.value = true
  } finally {
    loadingDetail.value = false
  }
}

const handleBack = async () => {
  const backPath = typeof route.query.from === 'string' ? route.query.from : ''
  if (backPath) {
    await router.push(backPath)
    return
  }
  await router.push({ path: '/erp/purchase/order' })
}

const handleEdit = async () => {
  if (!detailData.value.id || navigatingEdit.value) {
    return
  }
  navigatingEdit.value = true
  try {
    const from = route.fullPath
    await router.push({
      path: '/erp/purchase/order',
      query: {
        openId: String(detailData.value.id),
        openType: 'update',
        from
      }
    })
  } finally {
    navigatingEdit.value = false
  }
}

const handlePrint = async () => {
  if (printing.value) {
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
    message.warning('当前采购订单暂无审批流程')
    return
  }
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
  (actionType && actionTextMap[actionType]) || actionType || '-'

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

const getProgressPercent = (
  current?: number | string | null,
  totalValue?: number | string | null
) => {
  const base = normalizeNumber(totalValue)
  if (base <= 0) {
    return 0
  }
  return Math.min(100, Math.round((normalizeNumber(current) / base) * 100))
}

const getProgressRingStyle = (percent: number, color: string) => ({
  background: `conic-gradient(${color} 0deg, ${color} ${percent * 3.6}deg, #e8eef5 ${percent * 3.6}deg, #e8eef5 360deg)`
})

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

const calculateLeadTimeDays = (
  orderTime?: Date | string,
  deliveryDate?: Date | string
) => {
  if (!orderTime || !deliveryDate) {
    return null
  }
  const orderDate = new Date(orderTime)
  const delivery = new Date(deliveryDate)
  if (Number.isNaN(orderDate.getTime()) || Number.isNaN(delivery.getTime())) {
    return null
  }
  const diff = delivery.getTime() - orderDate.getTime()
  if (diff < 0) {
    return 0
  }
  return diff / (1000 * 60 * 60 * 24)
}

onMounted(() => {
  loadPurchaseOrder()
})
</script>

<style scoped lang="scss">
.purchase-order-detail-page {
  min-height: calc(100vh - 132px);
  background: #f5f7fa;
  padding: 16px;
}

.detail-shell {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-empty-card,
.detail-card,
.summary-card {
  border: 1px solid #e6edf5;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.03);
}

.detail-empty-card {
  min-height: 280px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.detail-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 4px 2px;
}

.detail-topbar__left,
.detail-topbar__actions,
.detail-heading__title-row,
.section-title,
.detail-summary-bar {
  display: flex;
  align-items: center;
}

.detail-topbar__left {
  gap: 12px;
}

.detail-topbar__actions {
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.detail-heading__title {
  margin: 0;
  color: #1e293b;
  font-size: 24px;
  line-height: 1.25;
  font-weight: 700;
}

.detail-heading__title-row {
  gap: 10px;
  flex-wrap: wrap;
}

.detail-primary-button {
  height: 36px;
  padding: 0 18px;
  border: none;
  border-radius: 10px;
  background: #14b8c4;
  box-shadow: none;
}

.detail-secondary-button,
.detail-icon-button {
  height: 36px;
  border-radius: 10px;
  border-color: #dbe4ee;
  color: #475569;
  box-shadow: none;
}

.detail-icon-button {
  width: 36px;
  padding: 0;
}

.status-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 24px;
  padding: 0 10px;
  border-radius: 999px;
  border: 1px solid transparent;
  font-size: 12px;
  font-weight: 600;
}

.status-chip--success {
  background: #ecfdf3;
  color: #1f8f5f;
  border-color: #cdeedc;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.summary-card {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 18px;
  min-width: 0;
}

.summary-card__head {
  display: flex;
  align-items: center;
  gap: 12px;
}

.summary-card__icon {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}

.summary-card__icon--cyan {
  background: #eefbfd;
  color: #0891b2;
}

.summary-card__icon--amber {
  background: #fff7e9;
  color: #d97706;
}

.summary-card__icon--blue {
  background: #eef5ff;
  color: #2563eb;
}

.summary-card__icon--slate {
  background: #f8fafc;
  color: #475569;
}

.summary-card__label-group {
  flex: 1;
  min-width: 0;
}

.summary-card__label {
  color: #64748b;
  font-size: 13px;
  font-weight: 600;
}

.summary-card__hint,
.summary-card__foot {
  color: #94a3b8;
  font-size: 12px;
  line-height: 1.4;
}

.summary-card__ring {
  position: relative;
  width: 42px;
  height: 42px;
  border-radius: 50%;
  flex-shrink: 0;
}

.summary-card__ring::after {
  content: '';
  position: absolute;
  inset: 4px;
  border-radius: 50%;
  background: #fff;
}

.summary-card__ring span {
  position: absolute;
  inset: 0;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #0f172a;
  font-size: 10px;
  font-weight: 700;
}

.summary-card__value-line {
  display: flex;
  align-items: baseline;
  gap: 6px;
  min-width: 0;
  color: #64748b;
  font-size: 13px;
}

.summary-card__value-line strong {
  color: #0f172a;
  font-size: 24px;
  line-height: 1;
  font-weight: 700;
}

.summary-card__value-line--single strong {
  color: #0f7490;
  font-size: 22px;
  font-family: 'JetBrains Mono', 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
}

.summary-card__value-line--stack {
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
}

.summary-card__value-line--stack strong {
  font-size: 18px;
  line-height: 1.3;
}

.detail-card {
  overflow: hidden;
}

.detail-card__header {
  padding: 18px 20px 0;
}

.section-title {
  gap: 8px;
  color: #0f172a;
  font-size: 16px;
  font-weight: 700;
}

.base-info-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 22px 20px;
  padding: 20px 20px 18px;
}

.info-item {
  min-width: 0;
}

.info-item label {
  display: block;
  margin-bottom: 8px;
  color: #94a3b8;
  font-size: 12px;
}

.info-item__value {
  color: #1e293b;
  font-size: 15px;
  font-weight: 600;
  line-height: 1.4;
  word-break: break-all;
}

.info-item__value--mono {
  font-family: 'JetBrains Mono', 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
}

.info-item__value--accent {
  color: #0891b2;
}

.detail-card__remark {
  border-top: 1px solid #edf2f7;
  padding: 16px 20px 18px;
  color: #64748b;
  font-size: 14px;
  line-height: 1.7;
}

.detail-card__remark-label {
  margin-right: 8px;
  color: #94a3b8;
}

.detail-tabs {
  display: flex;
  gap: 20px;
  border-bottom: 1px solid #e8eef5;
  padding: 0 20px;
}

.detail-tab {
  position: relative;
  border: none;
  background: transparent;
  color: #64748b;
  height: 50px;
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
  color: #14b8c4;
}

.detail-tab--active::after {
  background: #14b8c4;
}

.detail-table-wrap {
  padding: 0 20px;
  overflow-x: auto;
}

.detail-table {
  --el-table-border-color: #eef2f7;
  --el-table-row-hover-bg-color: #f8fbfd;
  --el-table-header-bg-color: #fbfcfe;
  --el-table-current-row-bg-color: #f8fbfd;
}

.detail-table :deep(.el-table__header-wrapper th) {
  background: #fbfcfe;
  color: #94a3b8;
  font-weight: 600;
  height: 46px;
}

.detail-table :deep(.el-table__header-wrapper .cell) {
  font-size: 12px;
}

.detail-table :deep(.el-table td),
.detail-table :deep(.el-table th) {
  border-bottom-color: #eef2f7;
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
  color: #0f172a;
  font-size: 14px;
  font-weight: 600;
  line-height: 1.4;
}

.product-cell__code,
.spec-cell__sub {
  margin-top: 4px;
  color: #94a3b8;
  font-size: 12px;
}

.product-cell__code {
  font-family: 'JetBrains Mono', 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
}

.numeric-cell {
  font-family: 'JetBrains Mono', 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  color: #334155;
  font-size: 13px;
}

.numeric-cell--accent {
  color: #0f7490;
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
  background: #eef6ff;
  color: #2563eb;
  border-color: #d8e7ff;
}

.status-pill--warning {
  background: #fff8e8;
  color: #d97706;
  border-color: #f9e5b5;
}

.status-pill--success {
  background: #ecfdf3;
  color: #059669;
  border-color: #caeedf;
}

.timeline-list {
  display: flex;
  flex-direction: column;
  gap: 18px;
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
  background: #0ea5c6;
  flex-shrink: 0;
  box-shadow: 0 0 0 6px rgba(14, 165, 198, 0.12);
}

.timeline-item__content {
  flex: 1;
  min-width: 0;
  padding-bottom: 18px;
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
  font-size: 15px;
  font-weight: 700;
}

.timeline-item__time,
.timeline-item__meta,
.timeline-item__desc {
  color: #64748b;
  font-size: 13px;
}

.timeline-item__meta {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 6px;
}

.timeline-item__desc + .timeline-item__desc {
  margin-top: 4px;
}

.detail-summary-bar {
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  padding: 16px 20px 18px;
  border-top: 1px solid #eef2f7;
}

.detail-summary-bar__meta {
  display: flex;
  align-items: center;
  gap: 18px;
  flex-wrap: wrap;
  color: #94a3b8;
  font-size: 13px;
}

.detail-summary-bar__total {
  display: flex;
  align-items: baseline;
  gap: 8px;
  color: #64748b;
  font-size: 14px;
}

.detail-summary-bar__total strong {
  color: #0f7490;
  font-size: 24px;
  font-family: 'JetBrains Mono', 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
}

.detail-empty {
  min-height: 220px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  text-align: center;
}

.detail-empty--table {
  min-height: 300px;
}

.detail-empty__icon {
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

.detail-empty--error .detail-empty__icon {
  background: #fff1f2;
  color: #e11d48;
}

.detail-empty__title {
  color: #0f172a;
  font-size: 16px;
  font-weight: 700;
}

.detail-empty__desc {
  color: #94a3b8;
  font-size: 13px;
}

@media (max-width: 1440px) {
  .summary-grid,
  .base-info-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 1024px) {
  .purchase-order-detail-page {
    padding: 14px;
  }

  .detail-topbar {
    flex-direction: column;
    align-items: flex-start;
  }

  .detail-topbar__actions {
    width: 100%;
    justify-content: flex-start;
  }

  .summary-grid,
  .base-info-grid {
    grid-template-columns: 1fr;
  }

  .detail-heading__title {
    font-size: 22px;
  }

  .detail-summary-bar {
    align-items: flex-start;
  }
}

@media (max-width: 640px) {
  .purchase-order-detail-page {
    padding: 12px;
  }

  .detail-topbar__left {
    align-items: flex-start;
  }

  .detail-topbar__actions {
    gap: 8px;
  }

  .detail-primary-button,
  .detail-secondary-button {
    flex: 1 1 auto;
  }

  .detail-tabs {
    gap: 16px;
  }

  .detail-summary-bar__meta {
    gap: 10px;
  }
}
</style>
