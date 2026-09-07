<template>
  <ContentWrap>
    <div class="trace-header">
      <div class="trace-header__content">
        <div class="trace-header__title">销售单 MRP 追踪</div>
        <div class="trace-header__meta">
          <span>订单编号：{{ displayOrderNo }}</span>
          <span v-if="hasSourceOrder">销售单 ID：{{ sourceOrderId }}</span>
        </div>
      </div>
      <div class="trace-header__actions">
        <el-button :disabled="isNavigating('back')" @click="backToClosureWorkbench">
          返回闭环工作台
        </el-button>
        <el-button
          type="primary"
          plain
          :loading="pageRefreshing"
          :disabled="!hasSourceOrder || pageRefreshing"
          @click="refreshAll"
        >
          <Icon icon="ep:refresh" class="mr-5px" /> 刷新全部
        </el-button>
      </div>
    </div>
  </ContentWrap>

  <ContentWrap>
    <el-alert
      v-if="contextError"
      type="error"
      :closable="false"
      show-icon
      class="mb-12px"
      :title="contextError"
    />

    <div v-loading="contextLoading" class="overview-layout">
      <section class="overview-panel">
        <div class="overview-panel__header">
          <div class="overview-panel__title">销售订单信息</div>
          <div class="overview-panel__actions">
            <el-button
              link
              type="primary"
              :disabled="!canOpenPurchaseTrace || isNavigating('purchase-trace')"
              v-hasPermi="['erp:purchase-order:query']"
              @click="openPurchaseTrace"
            >
              查看采购订单
            </el-button>
          </div>
        </div>

        <div class="info-grid">
          <div class="info-item">
            <span class="info-item__label">订单编号</span>
            <span class="info-item__value">{{ displayOrderNo }}</span>
          </div>
          <div class="info-item">
            <span class="info-item__label">项目</span>
            <span class="info-item__value">
              {{ saleOrderDetail?.projectName || closureSnapshot?.projectName || '-' }}
            </span>
          </div>
          <div class="info-item">
            <span class="info-item__label">客户</span>
            <span class="info-item__value">{{ closureSnapshot?.customerName || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-item__label">销售状态</span>
            <span class="info-item__value">
              <el-tag
                size="small"
                :type="
                  resolveErpAuditStatusTagType(
                    saleOrderDetail?.status,
                    saleOrderDetail?.processInstanceId
                  )
                "
              >
                {{
                  resolveErpAuditStatusLabel(
                    saleOrderDetail?.status,
                    saleOrderDetail?.processInstanceId
                  )
                }}
              </el-tag>
            </span>
          </div>
          <div class="info-item">
            <span class="info-item__label">交付准备</span>
            <span class="info-item__value">
              <el-tag
                size="small"
                :type="
                  resolveDeliveryReadyTagType(
                    closureSummary?.deliveryReadyStatus || saleOrderDetail?.deliveryReadyStatus
                  )
                "
              >
                {{
                  resolveDeliveryReadyLabel(
                    closureSummary?.deliveryReadyStatus || saleOrderDetail?.deliveryReadyStatus
                  )
                }}
              </el-tag>
            </span>
          </div>
          <div class="info-item">
            <span class="info-item__label">交期</span>
            <span class="info-item__value">{{ displayDeliveryDate }}</span>
          </div>
        </div>
      </section>

      <section class="overview-panel">
        <div class="overview-panel__header">
          <div class="overview-panel__title">闭环摘要</div>
        </div>
        <div class="summary-top">
          <div class="summary-top__item">
            <span class="summary-top__label">当前闭环阶段</span>
            <el-tag
              size="small"
              effect="plain"
              :type="resolveClosureStageTagType(closureSummary?.closureStage)"
            >
              {{ resolveClosureStageLabel(closureSummary?.closureStage) }}
            </el-tag>
          </div>
          <div class="summary-top__item">
            <span class="summary-top__label">阻塞节点</span>
            <div class="blocker-tags">
              <el-tag
                v-for="blocker in blockerLabels"
                :key="blocker"
                size="small"
                type="warning"
                effect="plain"
              >
                {{ blocker }}
              </el-tag>
              <el-tag v-if="!blockerLabels.length" size="small" type="success" effect="plain">
                无阻塞
              </el-tag>
            </div>
          </div>
        </div>
        <div class="metric-grid">
          <div class="metric-card">
            <span class="metric-card__label">待发货数量</span>
            <strong class="metric-card__value">
              {{ formatMetric(closureSummary?.remainingShipQty) }}
            </strong>
          </div>
          <div class="metric-card">
            <span class="metric-card__label">成品质检合格数量</span>
            <strong class="metric-card__value">
              {{ formatMetric(closureSummary?.productionQualifiedQty) }}
            </strong>
          </div>
          <div class="metric-card">
            <span class="metric-card__label">采购订单</span>
            <strong class="metric-card__value">
              {{ formatMetric(closureSummary?.purchaseOrderCount) }}
            </strong>
          </div>
          <div class="metric-card">
            <span class="metric-card__label">采购入库</span>
            <strong class="metric-card__value">
              {{ formatMetric(closureSummary?.purchaseInCount) }}
            </strong>
          </div>
        </div>
      </section>
    </div>
  </ContentWrap>

  <ContentWrap>
    <div class="section-header">
      <div>
        <div class="section-header__title">采购建议</div>
        <div class="section-header__meta">
          <span>总 {{ formatMetric(closureSummary?.purchaseSuggestCount) }}</span>
          <span>已确认 {{ formatMetric(closureSummary?.purchaseConfirmedSuggestCount) }}</span>
          <span>已转单 {{ formatMetric(closureSummary?.purchaseConvertedSuggestCount) }}</span>
        </div>
      </div>
      <div class="section-header__actions">
        <el-button
          link
          type="primary"
          :disabled="!canOpenPurchaseTrace || isNavigating('purchase-trace')"
          v-hasPermi="['erp:purchase-order:query']"
          @click="openPurchaseTrace"
        >
          查看采购订单
        </el-button>
      </div>
    </div>

    <el-form
      ref="purchaseQueryFormRef"
      :model="purchaseQueryParams"
      label-width="68px"
      class="filter-form"
      @submit.prevent
    >
      <div class="filter-form__grid">
        <el-form-item label="物料" prop="materialId">
          <ProductRemoteSelect v-model="purchaseQueryParams.materialId" placeholder="请选择物料" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="purchaseQueryParams.status" clearable placeholder="请选择状态">
            <el-option
              v-for="item in suggestStatusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
      </div>
      <div class="filter-form__actions">
        <el-button :disabled="purchaseLoading" @click="handlePurchaseQuery">
          <Icon icon="ep:search" class="mr-5px" /> 搜索
        </el-button>
        <el-button
          :disabled="!canResetPurchaseFilters || purchaseLoading"
          @click="resetPurchaseQuery"
        >
          <Icon icon="ep:refresh-left" class="mr-5px" /> 重置
        </el-button>
      </div>
    </el-form>

    <el-alert
      v-if="purchaseError"
      type="error"
      :closable="false"
      show-icon
      class="mb-12px"
      :title="purchaseError"
    />

    <el-table
      v-loading="purchaseLoading"
      :data="purchaseList"
      stripe
      border
      :show-overflow-tooltip="true"
      :empty-text="purchaseEmptyText"
    >
      <el-table-column prop="id" label="建议编号" min-width="100" align="center" />
      <el-table-column prop="planId" label="计划编号" min-width="100" align="center" />
      <el-table-column prop="materialName" label="物料" min-width="180" />
      <el-table-column prop="suggestQty" label="建议数量" min-width="110" align="right" />
      <el-table-column prop="netDemandQty" label="净需求" min-width="110" align="right" />
      <el-table-column
        prop="suggestArrivalDate"
        label="建议到货日期"
        min-width="130"
        align="center"
        :formatter="dateFormatter2"
      />
      <el-table-column label="状态" min-width="110" align="center">
        <template #default="{ row }">
          <el-tag size="small" :type="getSuggestStatusType(row.status)">
            {{ getSuggestStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        prop="convertPurchaseOrderId"
        label="采购订单"
        min-width="110"
        align="center"
      />
      <el-table-column
        prop="createTime"
        label="创建时间"
        min-width="170"
        align="center"
        :formatter="dateFormatter"
      />
    </el-table>

    <Pagination
      :total="purchaseTotal"
      v-model:page="purchaseQueryParams.pageNo"
      v-model:limit="purchaseQueryParams.pageSize"
      @pagination="getPurchaseList"
    />
  </ContentWrap>

  <ContentWrap>
    <div class="section-header">
      <div>
        <div class="section-header__title">生产建议</div>
        <div class="section-header__meta">
          <span>总 {{ formatMetric(closureSummary?.productionSuggestCount) }}</span>
          <span>已确认 {{ formatMetric(closureSummary?.productionConfirmedSuggestCount) }}</span>
          <span>已转单 {{ formatMetric(closureSummary?.productionConvertedSuggestCount) }}</span>
        </div>
      </div>
    </div>

    <el-form
      ref="productionQueryFormRef"
      :model="productionQueryParams"
      label-width="68px"
      class="filter-form"
      @submit.prevent
    >
      <div class="filter-form__grid">
        <el-form-item label="产品" prop="productId">
          <ProductRemoteSelect v-model="productionQueryParams.productId" placeholder="请选择产品" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="productionQueryParams.status" clearable placeholder="请选择状态">
            <el-option
              v-for="item in suggestStatusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
      </div>
      <div class="filter-form__actions">
        <el-button :disabled="productionLoading" @click="handleProductionQuery">
          <Icon icon="ep:search" class="mr-5px" /> 搜索
        </el-button>
        <el-button
          :disabled="!canResetProductionFilters || productionLoading"
          @click="resetProductionQuery"
        >
          <Icon icon="ep:refresh-left" class="mr-5px" /> 重置
        </el-button>
      </div>
    </el-form>

    <el-alert
      v-if="productionError"
      type="error"
      :closable="false"
      show-icon
      class="mb-12px"
      :title="productionError"
    />

    <el-table
      v-loading="productionLoading"
      :data="productionList"
      stripe
      border
      :show-overflow-tooltip="true"
      :empty-text="productionEmptyText"
    >
      <el-table-column prop="id" label="建议编号" min-width="100" align="center" />
      <el-table-column prop="planId" label="计划编号" min-width="100" align="center" />
      <el-table-column prop="productName" label="产品" min-width="180" />
      <el-table-column prop="suggestQty" label="建议数量" min-width="110" align="right" />
      <el-table-column prop="netDemandQty" label="净需求" min-width="110" align="right" />
      <el-table-column
        prop="suggestStartDate"
        label="建议开工日期"
        min-width="130"
        align="center"
        :formatter="dateFormatter2"
      />
      <el-table-column
        prop="suggestEndDate"
        label="建议完工日期"
        min-width="130"
        align="center"
        :formatter="dateFormatter2"
      />
      <el-table-column label="状态" min-width="110" align="center">
        <template #default="{ row }">
          <el-tag size="small" :type="getSuggestStatusType(row.status)">
            {{ getSuggestStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        prop="convertProductionOrderId"
        label="生产工单"
        min-width="110"
        align="center"
      />
      <el-table-column
        prop="createTime"
        label="创建时间"
        min-width="170"
        align="center"
        :formatter="dateFormatter"
      />
    </el-table>

    <Pagination
      :total="productionTotal"
      v-model:page="productionQueryParams.pageNo"
      v-model:limit="productionQueryParams.pageSize"
      @pagination="getProductionList"
    />
  </ContentWrap>
</template>

<script setup lang="ts">
import { dateFormatter, dateFormatter2, formatDate } from '@/utils/formatTime'
import {
  MrpSuggestApi,
  type ProductionSuggestPageReqVO,
  type ProductionSuggestVO,
  type PurchaseSuggestPageReqVO,
  type PurchaseSuggestVO
} from '@/api/erp/mrp/suggest'
import {
  SaleOrderApi,
  type SaleOrderClosurePageVO,
  type SaleOrderClosureSummaryVO,
  type SaleOrderVO
} from '@/api/erp/sale/order'
import { resolveErpAuditStatusLabel, resolveErpAuditStatusTagType } from '@/utils/erpAuditStatus'
import { checkPermi } from '@/utils/permission'

defineOptions({ name: 'ErpSaleOrderMrpTraceView' })

type SuggestTagType = 'info' | 'warning' | 'primary' | 'success' | 'danger'
type ElementTagType = 'primary' | 'success' | 'warning' | 'danger' | 'info'

const DELIVERY_READY_STATUS = {
  NOT_READY: 'NOT_READY',
  PART_READY: 'PART_READY',
  READY_TO_SHIP: 'READY_TO_SHIP'
} as const

const closureStageLabelMap: Record<string, string> = {
  WAIT_SALE_APPROVAL: '待销售审核',
  WAIT_PURCHASE_SUGGEST_CONFIRM: '待采购建议确认',
  WAIT_PURCHASE_ORDER_CONVERT: '待采购转单',
  WAIT_PURCHASE_APPROVAL: '待采购审核',
  WAIT_PURCHASE_IQC: '待来料质检',
  WAIT_PURCHASE_STOCK_IN: '待采购入库',
  WAIT_PRODUCTION_SUGGEST_CONFIRM: '待生产建议确认',
  WAIT_PRODUCTION_ORDER_CONVERT: '待生产转单',
  WAIT_DELIVERY_READY: '待交付准备',
  PART_READY: '部分就绪',
  READY_TO_SHIP: '可发货',
  CLOSED: '已闭环'
}

const closureStageTagTypeMap: Record<string, ElementTagType> = {
  WAIT_SALE_APPROVAL: 'danger',
  WAIT_PURCHASE_SUGGEST_CONFIRM: 'warning',
  WAIT_PURCHASE_ORDER_CONVERT: 'warning',
  WAIT_PURCHASE_APPROVAL: 'warning',
  WAIT_PURCHASE_IQC: 'warning',
  WAIT_PURCHASE_STOCK_IN: 'warning',
  WAIT_PRODUCTION_SUGGEST_CONFIRM: 'warning',
  WAIT_PRODUCTION_ORDER_CONVERT: 'warning',
  WAIT_DELIVERY_READY: 'info',
  PART_READY: 'primary',
  READY_TO_SHIP: 'success',
  CLOSED: 'success'
}

const suggestStatusOptions = [
  { label: '待确认', value: 0, type: 'warning' as SuggestTagType },
  { label: '已确认', value: 10, type: 'primary' as SuggestTagType },
  { label: '已转单', value: 20, type: 'success' as SuggestTagType },
  { label: '已驳回', value: 30, type: 'danger' as SuggestTagType }
]

const buildPurchaseQueryParams = (sourceOrderId?: number): PurchaseSuggestPageReqVO => ({
  pageNo: 1,
  pageSize: 10,
  materialId: undefined,
  sourceOrderId,
  status: undefined
})

const buildProductionQueryParams = (sourceOrderId?: number): ProductionSuggestPageReqVO => ({
  pageNo: 1,
  pageSize: 10,
  productId: undefined,
  sourceOrderId,
  status: undefined
})

const route = useRoute()
const router = useRouter()
const canQueryPurchaseOrder = checkPermi(['erp:purchase-order:query'])

const saleOrderDetail = ref<SaleOrderVO>()
const closureSnapshot = ref<SaleOrderClosurePageVO>()

const contextLoading = ref(false)
const pageRefreshing = ref(false)
const purchaseLoading = ref(false)
const productionLoading = ref(false)

const contextError = ref('')
const purchaseError = ref('')
const productionError = ref('')
const navigatingAction = ref('')

const purchaseList = ref<PurchaseSuggestVO[]>([])
const purchaseTotal = ref(0)
const productionList = ref<ProductionSuggestVO[]>([])
const productionTotal = ref(0)

const purchaseQueryFormRef = ref()
const productionQueryFormRef = ref()
const purchaseQueryParams = reactive<PurchaseSuggestPageReqVO>(buildPurchaseQueryParams())
const productionQueryParams = reactive<ProductionSuggestPageReqVO>(buildProductionQueryParams())

const normalizeRouteNumber = (value: unknown) => {
  if (typeof value !== 'string' || !value.trim()) {
    return undefined
  }
  const parsedValue = Number(value)
  return Number.isNaN(parsedValue) || parsedValue <= 0 ? undefined : parsedValue
}

const sourceOrderId = computed(() => normalizeRouteNumber(route.query.sourceOrderId))
const routeSourceOrderNo = computed(() =>
  typeof route.query.sourceOrderNo === 'string' ? route.query.sourceOrderNo : ''
)
const hasSourceOrder = computed(() => !!sourceOrderId.value)
const displayOrderNo = computed(() => saleOrderDetail.value?.no || routeSourceOrderNo.value || '-')
const closureSummary = computed<SaleOrderClosureSummaryVO | null | undefined>(
  () => closureSnapshot.value?.closureSummary
)
const blockerLabels = computed(() => resolveBlockerLabels(closureSummary.value?.blockerCodes))
const canOpenPurchaseTrace = computed(() => hasSourceOrder.value && canQueryPurchaseOrder)
const canResetPurchaseFilters = computed(
  () =>
    purchaseQueryParams.pageNo !== 1 ||
    purchaseQueryParams.materialId !== undefined ||
    purchaseQueryParams.status !== undefined
)
const canResetProductionFilters = computed(
  () =>
    productionQueryParams.pageNo !== 1 ||
    productionQueryParams.productId !== undefined ||
    productionQueryParams.status !== undefined
)
const purchaseEmptyText = computed(() =>
  purchaseError.value ? '采购建议加载失败' : '暂无采购建议'
)
const productionEmptyText = computed(() =>
  productionError.value ? '生产建议加载失败' : '暂无生产建议'
)
const displayDeliveryDate = computed(() => {
  const deliveryDate = saleOrderDetail.value?.deliveryDate || closureSnapshot.value?.deliveryDate
  if (!deliveryDate) {
    return '-'
  }
  return formatDate(deliveryDate as Date, 'YYYY-MM-DD')
})

const syncTraceQueryParams = () => {
  purchaseQueryParams.sourceOrderId = sourceOrderId.value
  productionQueryParams.sourceOrderId = sourceOrderId.value
}

const formatMetric = (value?: number | string | null) => {
  if (value === undefined || value === null || value === '') {
    return '0'
  }
  return String(value)
}

const getSuggestStatusLabel = (status?: number) => {
  return suggestStatusOptions.find((item) => item.value === status)?.label || '未知'
}

const getSuggestStatusType = (status?: number): SuggestTagType => {
  return suggestStatusOptions.find((item) => item.value === status)?.type || 'info'
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

const resolveDeliveryReadyTagType = (status?: string): ElementTagType => {
  if (status === DELIVERY_READY_STATUS.PART_READY) {
    return 'warning'
  }
  if (status === DELIVERY_READY_STATUS.READY_TO_SHIP) {
    return 'success'
  }
  return 'info'
}

const resolveClosureStageLabel = (stage?: string) => {
  if (!stage) {
    return '未识别'
  }
  return closureStageLabelMap[stage] || stage
}

const resolveClosureStageTagType = (stage?: string): ElementTagType => {
  if (!stage) {
    return 'info'
  }
  return closureStageTagTypeMap[stage] || 'info'
}

const resolveBlockerLabels = (blockerCodes?: string[] | null) => {
  if (!blockerCodes?.length) {
    return []
  }
  return blockerCodes.map((code) => closureStageLabelMap[code] || code)
}

const isNavigating = (action: string) => navigatingAction.value === action

const navigateWithLock = async (action: string, handler: () => Promise<void>) => {
  if (navigatingAction.value === action) {
    return
  }
  navigatingAction.value = action
  try {
    await handler()
  } finally {
    navigatingAction.value = ''
  }
}

const loadContext = async () => {
  if (!sourceOrderId.value) {
    saleOrderDetail.value = undefined
    closureSnapshot.value = undefined
    contextError.value = '缺少销售订单参数，无法追踪 MRP'
    return
  }
  contextLoading.value = true
  contextError.value = ''
  try {
    const detail = await SaleOrderApi.getSaleOrder(sourceOrderId.value)
    saleOrderDetail.value = detail
    closureSnapshot.value = undefined
    const orderNo = detail?.no || routeSourceOrderNo.value
    if (!orderNo) {
      return
    }
    try {
      const closurePage = await SaleOrderApi.getSaleOrderClosureSummaryPage({
        pageNo: 1,
        pageSize: 10,
        no: orderNo
      })
      closureSnapshot.value =
        closurePage.list?.find((item) => item.id === sourceOrderId.value) || closurePage.list?.[0]
    } catch {
      contextError.value = '销售单上下文加载失败，请刷新重试'
    }
  } catch {
    saleOrderDetail.value = undefined
    closureSnapshot.value = undefined
    contextError.value = '销售单上下文加载失败，请刷新重试'
  } finally {
    contextLoading.value = false
  }
}

const getPurchaseList = async () => {
  syncTraceQueryParams()
  if (!purchaseQueryParams.sourceOrderId) {
    purchaseList.value = []
    purchaseTotal.value = 0
    purchaseError.value = '缺少销售订单参数，无法加载采购建议'
    return
  }
  purchaseLoading.value = true
  purchaseError.value = ''
  try {
    const data = await MrpSuggestApi.getPurchaseSuggestPage({ ...purchaseQueryParams })
    purchaseList.value = data.list || []
    purchaseTotal.value = data.total || 0
  } catch {
    purchaseError.value = '采购建议加载失败，请稍后重试'
    purchaseList.value = []
    purchaseTotal.value = 0
  } finally {
    purchaseLoading.value = false
  }
}

const getProductionList = async () => {
  syncTraceQueryParams()
  if (!productionQueryParams.sourceOrderId) {
    productionList.value = []
    productionTotal.value = 0
    productionError.value = '缺少销售订单参数，无法加载生产建议'
    return
  }
  productionLoading.value = true
  productionError.value = ''
  try {
    const data = await MrpSuggestApi.getProductionSuggestPage({ ...productionQueryParams })
    productionList.value = data.list || []
    productionTotal.value = data.total || 0
  } catch {
    productionError.value = '生产建议加载失败，请稍后重试'
    productionList.value = []
    productionTotal.value = 0
  } finally {
    productionLoading.value = false
  }
}

const handlePurchaseQuery = async () => {
  purchaseQueryParams.pageNo = 1
  await getPurchaseList()
}

const resetPurchaseQuery = async () => {
  Object.assign(purchaseQueryParams, buildPurchaseQueryParams(sourceOrderId.value))
  purchaseQueryFormRef.value?.resetFields()
  await getPurchaseList()
}

const handleProductionQuery = async () => {
  productionQueryParams.pageNo = 1
  await getProductionList()
}

const resetProductionQuery = async () => {
  Object.assign(productionQueryParams, buildProductionQueryParams(sourceOrderId.value))
  productionQueryFormRef.value?.resetFields()
  await getProductionList()
}

const refreshAll = async () => {
  if (!hasSourceOrder.value) {
    contextError.value = '缺少销售订单参数，无法追踪 MRP'
    return
  }
  pageRefreshing.value = true
  try {
    await Promise.allSettled([loadContext(), getPurchaseList(), getProductionList()])
  } finally {
    pageRefreshing.value = false
  }
}

const backToClosureWorkbench = async () => {
  await navigateWithLock('back', async () => {
    await router.push({ path: '/erp/sale/order/closure-workbench' })
  })
}

const openPurchaseTrace = async () => {
  if (!sourceOrderId.value || !canQueryPurchaseOrder) {
    return
  }
  await navigateWithLock('purchase-trace', async () => {
    await router.push({
      name: 'ErpSaleOrderPurchaseOrderTracePage',
      query: {
        sourceOrderId: String(sourceOrderId.value),
        sourceOrderNo: saleOrderDetail.value?.no || routeSourceOrderNo.value || undefined,
        traceFrom: 'mrp-trace'
      }
    })
  })
}

onMounted(async () => {
  Object.assign(purchaseQueryParams, buildPurchaseQueryParams(sourceOrderId.value))
  Object.assign(productionQueryParams, buildProductionQueryParams(sourceOrderId.value))
  await refreshAll()
})

watch(
  () => [route.query.sourceOrderId, route.query.sourceOrderNo],
  async () => {
    Object.assign(purchaseQueryParams, buildPurchaseQueryParams(sourceOrderId.value))
    Object.assign(productionQueryParams, buildProductionQueryParams(sourceOrderId.value))
    await refreshAll()
  }
)
</script>

<style scoped lang="scss">
.trace-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.trace-header__content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.trace-header__title {
  font-size: 20px;
  font-weight: 700;
  line-height: 1.4;
  color: var(--el-text-color-primary);
}

.trace-header__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 16px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
  line-height: 1.6;
}

.trace-header__actions,
.overview-panel__actions,
.section-header__actions,
.filter-form__actions,
.blocker-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 12px;
}

.overview-layout {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.overview-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-width: 0;
}

.overview-panel__header,
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.overview-panel__title,
.section-header__title {
  font-size: 16px;
  font-weight: 700;
  line-height: 1.5;
  color: var(--el-text-color-primary);
}

.section-header__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 16px;
  margin-top: 4px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.info-grid,
.metric-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.info-item,
.metric-card,
.summary-top {
  padding: 14px 16px;
  border-radius: 12px;
  border: 1px solid var(--el-border-color-light);
  background: var(--el-bg-color-page);
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 0;
}

.info-item__label,
.summary-top__label,
.metric-card__label {
  color: var(--el-text-color-secondary);
  font-size: 12px;
  line-height: 1.5;
}

.info-item__value,
.metric-card__value {
  color: var(--el-text-color-primary);
  font-size: 14px;
  line-height: 1.6;
  font-weight: 600;
  word-break: break-word;
}

.summary-top {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.summary-top__item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.metric-card__value {
  font-size: 22px;
  line-height: 1.2;
}

.filter-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 16px;
  margin-bottom: 16px;
}

.filter-form__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 6px 16px;
}

.filter-form__grid :deep(.el-form-item) {
  margin-bottom: 0;
}

.filter-form__grid :deep(.el-select),
.filter-form__grid :deep(.el-input-number) {
  width: 100%;
}

@media (max-width: 1279px) {
  .overview-layout,
  .info-grid,
  .metric-grid {
    grid-template-columns: minmax(0, 1fr);
  }
}

@media (max-width: 767px) {
  .trace-header,
  .overview-panel__header,
  .section-header {
    flex-direction: column;
  }

  .trace-header__actions,
  .overview-panel__actions,
  .section-header__actions,
  .filter-form__actions {
    width: 100%;
  }

  .trace-header__actions :deep(.el-button),
  .filter-form__actions :deep(.el-button) {
    flex: 1 1 auto;
  }

  .filter-form__grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
