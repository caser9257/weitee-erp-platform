<template>
  <div class="stock-page-shell">
    <ContentWrap class="stock-page__title-card">
      <div class="stock-page__title">即时库存查询</div>
    </ContentWrap>

    <ContentWrap class="stock-page__filter-card">
      <el-alert
        v-if="supportLoadFailed"
        :title="supportErrorMessage"
        type="warning"
        show-icon
        :closable="false"
        class="mb-12px"
      >
        <template #default>
          <el-button
            class="stock-action-btn"
            link
            type="primary"
            :disabled="supportLoading"
            @click="retrySupportOptions"
          >
            重新加载筛选项
          </el-button>
        </template>
      </el-alert>

      <el-alert
        v-if="stockListError"
        :title="stockListError"
        type="error"
        show-icon
        :closable="false"
        class="mb-12px"
      >
        <template #default>
          <el-button
            class="stock-action-btn"
            link
            type="primary"
            :disabled="!canQueryStock"
            @click="getList"
          >
            重试
          </el-button>
        </template>
      </el-alert>

      <el-form ref="queryFormRef" :model="queryParams" label-position="top" class="stock-query">
        <div class="stock-query__panel">
          <div class="stock-query__grid">
            <el-form-item label="产品" prop="productId">
              <el-select
                v-model="queryParams.productId"
                clearable
                filterable
                placeholder="请选择产品"
                :loading="supportLoading"
              >
                <el-option
                  v-for="item in productList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="仓库" prop="warehouseId">
              <el-select
                v-model="queryParams.warehouseId"
                clearable
                filterable
                placeholder="请选择仓库"
                :loading="supportLoading"
              >
                <el-option
                  v-for="item in warehouseList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </div>
          <div class="stock-query__actions">
            <el-button class="stock-action-btn" :disabled="!canQueryStock" @click="resetQuery">
              <Icon icon="ep:refresh" class="mr-5px" /> 重置
            </el-button>
            <el-button
              class="stock-action-btn"
              type="primary"
              :loading="stockListLoading"
              :disabled="!canQueryStock"
              @click="handleQuery"
            >
              <Icon icon="ep:search" class="mr-5px" /> 查询
            </el-button>
          </div>
        </div>
      </el-form>
    </ContentWrap>

    <ContentWrap class="stock-page__list-card">
      <div class="stock-page__overview-grid">
        <article
          v-for="(card, index) in stockSummaryCards"
          :key="card.label"
          class="stock-page__overview-card"
          :class="resolveSummaryCardClass(index)"
        >
          <div class="stock-page__overview-card__icon" :class="card.colorClass">
            <Icon :icon="card.icon" />
          </div>
          <div class="stock-page__overview-card__content">
            <div class="stock-page__overview-card__value">{{ card.value }}</div>
            <div class="stock-page__overview-card__label">{{ card.label }}</div>
          </div>
        </article>

        <article class="stock-page__overview-card stock-page__overview-card--export">
          <el-button
            class="stock-page__export-btn"
            plain
            :disabled="!canExportStock"
            :loading="exportLoading"
            @click="handleExport"
            v-hasPermi="['erp:stock:export']"
          >
            <Icon icon="ep:download" class="mr-5px" /> 导出台账数据
          </el-button>
        </article>
      </div>

      <div class="stock-page__table-shell">
        <el-table
          v-loading="stockListLoading"
          :data="list"
          :stripe="true"
          :show-overflow-tooltip="true"
          class="stock-ledger"
        >
        <el-table-column label="产品信息" min-width="260">
          <template #default="{ row }">
            <div class="ledger-product">
              <div class="ledger-product__icon">
                <Icon icon="ep:goods" />
              </div>
              <div class="ledger-product__info">
                <div class="ledger-product__name" :title="row.productName || '-'">
                  {{ row.productName || '-' }}
                </div>
                <div class="ledger-product__meta">
                  <span :title="row.categoryName || '-'">{{ row.categoryName || '-' }}</span>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
          <el-table-column label="所属仓库" min-width="180">
            <template #default="{ row }">
              <div class="ledger-warehouse">
                <span class="ledger-warehouse__name" :title="row.warehouseName || '-'">
                  {{ row.warehouseName || '-' }}
                </span>
                <span :class="['ledger-warehouse__chip', resolveWarehouseCategoryClass(row)]">
                  {{ resolveWarehouseCategoryName(row) }}
                </span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="库存量" align="right" min-width="140">
            <template #default="{ row }">
              <span class="ledger-number">{{ formatCount(row.count) }}</span>
            </template>
          </el-table-column>
        <el-table-column label="操作" align="center" fixed="right" width="120">
          <template #default="{ row }">
            <el-button
              class="stock-action-btn"
              type="primary"
              plain
              :disabled="isAnyBatchActionLoading || !row.productId || !row.warehouseId"
              @click="openBatchDrawer(row)"
              v-hasPermi="['erp:stock-batch:query']"
            >
              <Icon icon="ep:search" class="mr-4px" /> 批次追溯
            </el-button>
          </template>
        </el-table-column>
          <template #empty>
            <div v-if="showStockListErrorState" class="stock-empty stock-empty--error">
              <div class="stock-empty__icon">
                <Icon icon="ep:warning-filled" />
              </div>
              <div class="stock-empty__title">库存列表加载失败</div>
              <el-button type="primary" plain :disabled="!canQueryStock" @click="getList"
                >重试</el-button
              >
            </div>
            <div v-else class="stock-empty">
              <div class="stock-empty__icon">
                <Icon icon="ep:box" />
              </div>
              <div class="stock-empty__title">暂无符合条件的库存台账</div>
            </div>
          </template>
        </el-table>
      </div>

      <div class="stock-page__footer">
        <div class="stock-page__record-count">共 {{ total }} 条记录</div>
        <Pagination
          :total="total"
          v-model:page="queryParams.pageNo"
          v-model:limit="queryParams.pageSize"
          @pagination="getList"
        />
      </div>
    </ContentWrap>

    <el-drawer
      v-model="batchDrawerOpen"
      size="64%"
      destroy-on-close
      :with-header="false"
      custom-class="premium-drawer"
      modal-class="premium-drawer__mask"
      :close-on-click-modal="!isAnyBatchActionLoading"
      :close-on-press-escape="!isAnyBatchActionLoading"
      :before-close="handleBatchDrawerBeforeClose"
      @closed="handleBatchDrawerClosed"
    >
      <div class="batch-drawer-shell">
        <div class="batch-drawer-head">
          <h2 class="batch-drawer-head__title">
            <Icon icon="ep:files" class="mr-8px text-blue-600" /> 批次追溯
          </h2>
          <button
            type="button"
            class="batch-drawer-head__close"
            :disabled="!canCloseBatchDrawer"
            @click="handleCloseBatchDrawer"
          >
            <Icon icon="ep:close" />
          </button>
        </div>

        <div class="batch-drawer-body">
          <div class="batch-drawer-summary">
            <div class="batch-drawer-summary__main">
              <div class="batch-drawer-summary__head">
                <div class="batch-drawer-summary__eyebrow">{{ currentStock?.warehouseName || '-' }}</div>
                <div
                  :class="['batch-drawer-summary__chip', resolveWarehouseCategoryClass(currentStock)]"
                >
                  {{ resolveWarehouseCategoryName(currentStock) }}
                </div>
              </div>
              <div class="batch-drawer-summary__title">当前库存</div>
              <div class="batch-drawer-summary__meta">
                <span>{{ currentStock?.productName || '-' }}</span>
                <span class="batch-drawer-summary__mono"
                  >库存 {{ formatCount(currentStock?.count) }}</span
                >
              </div>
            </div>
            <div class="batch-drawer-summary__total batch-drawer-summary__total--plain">
              <div class="batch-drawer-summary__total-label">当前总账面库存</div>
              <div class="batch-drawer-summary__total-value">
                {{ formatCount(currentStock?.count) }}
              </div>
            </div>
          </div>

          <div class="batch-drawer-body__content">
            <div class="batch-drawer-query-card">
              <div class="batch-drawer-query-card__field">
                <el-input
                  v-model="batchQueryParams.batchNo"
                  clearable
                  placeholder="请输入批次号"
                  @keyup.enter="handleBatchQuery"
                />
              </div>
              <div class="batch-drawer-query-card__actions">
                <el-button
                  class="stock-action-btn"
                  :disabled="!canQueryBatch"
                  @click="resetBatchQuery"
                >
                  <Icon icon="ep:refresh" class="mr-5px" /> 重置批次
                </el-button>
                <el-button
                  class="stock-action-btn"
                  type="primary"
                  :disabled="!canQueryBatch"
                  @click="handleBatchQuery"
                >
                  <Icon icon="ep:search" class="mr-5px" /> 查询批次
                </el-button>
              </div>
            </div>

            <el-alert
              v-if="batchListError"
              :title="batchListError"
              type="error"
              show-icon
              :closable="false"
              class="mb-12px"
            >
              <template #default>
                <el-button
                  class="stock-action-btn"
                  link
                  type="primary"
                  :disabled="batchListLoading"
                  @click="getBatchList"
                >
                  重试
                </el-button>
              </template>
            </el-alert>

            <div class="batch-detail-card">
              <div class="batch-detail-card__header">
                <h3 class="batch-detail-card__title">批次余额明细</h3>
                <div class="batch-detail-card__meta">
                  <span class="batch-pill batch-pill--primary">批次 {{ batchTotal }}</span>
                  <span v-if="selectedBatch" class="batch-pill batch-pill--success"
                    >当前 {{ selectedBatch.batchNo }}</span
                  >
                </div>
              </div>
              <el-table
                v-loading="batchListLoading"
                :data="batchList"
                :stripe="true"
                :show-overflow-tooltip="true"
                highlight-current-row
                class="batch-ledger"
                @current-change="handleCurrentBatchChange"
              >
                <el-table-column label="批次号" min-width="180">
                  <template #default="{ row }">
                    <div class="font-mono text-slate-800">{{ row.batchNo }}</div>
                    <div class="mt-2px truncate text-12px text-slate-500">{{
                      row.purchaseSourceBatchNo || '-'
                    }}</div>
                  </template>
                </el-table-column>
                <el-table-column label="来源单号" prop="sourceBizNo" min-width="180" />
                <el-table-column label="入库时间" min-width="170">
                  <template #default="{ row }">{{ formatDateTime(row.inboundTime) }}</template>
                </el-table-column>
                <el-table-column label="总量" align="right" min-width="120">
                  <template #default="{ row }">
                    <span class="ledger-number">{{ formatCount(row.totalQty) }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="可用量" align="right" min-width="120">
                  <template #default="{ row }">
                    <span class="ledger-number batch-qty batch-qty--success">{{
                      formatCount(row.availableQty)
                    }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="锁定/占用" align="right" min-width="120">
                  <template #default="{ row }">
                    <span
                      class="ledger-number"
                      :class="row.lockedQty > 0 ? 'text-amber-600' : 'text-slate-400'"
                    >
                      {{ formatCount(row.lockedQty) }}
                    </span>
                  </template>
                </el-table-column>
                <el-table-column label="操作" align="center" fixed="right" width="110">
                  <template #default="{ row }">
                    <el-button
                      class="stock-action-btn"
                      link
                      type="primary"
                      :disabled="batchTraceLoading"
                      @click.stop="selectBatch(row)"
                    >
                      追踪
                    </el-button>
                  </template>
                </el-table-column>
                <template #empty>
                  <div class="batch-empty">
                    <div class="batch-empty__icon">
                      <Icon icon="ep:box" />
                    </div>
                    <div class="batch-empty__title">该产品暂无批次流水结存</div>
                    <div class="batch-empty__desc">系统未查询到可用的批次余额记录。</div>
                  </div>
                </template>
              </el-table>
              <div v-if="batchTotal > 0" class="batch-detail-card__footer">
                <Pagination
                  class="custom-pagination"
                  :total="batchTotal"
                  v-model:page="batchQueryParams.pageNo"
                  v-model:limit="batchQueryParams.pageSize"
                  @pagination="getBatchList"
                />
              </div>
            </div>

            <el-alert
              v-if="batchTraceError"
              :title="batchTraceError"
              type="error"
              show-icon
              :closable="false"
              class="mb-12px"
            >
              <template #default>
                <el-button
                  link
                  type="primary"
                  :disabled="!canViewBatchTrace"
                  @click="retryBatchTrace"
                >
                  重试
                </el-button>
              </template>
            </el-alert>

            <div
              v-if="selectedBatch || batchTraceLoading || batchTraceError"
              class="batch-trace-card"
            >
              <div class="batch-trace-card__header">
                <h3 class="batch-trace-card__title">批次追溯</h3>
              </div>
              <div v-if="!selectedBatch" class="batch-trace-empty">
                <div class="batch-trace-empty__icon">
                  <Icon icon="ep:files" />
                </div>
                <div class="batch-trace-empty__title">未选中批次</div>
              </div>
              <el-tabs v-else v-model="activeBatchTab" class="batch-trace-tabs">
                <el-tab-pane label="出库消耗" name="allocation">
                  <el-table
                    v-loading="batchTraceLoading"
                    :data="allocationList"
                    :stripe="true"
                    :show-overflow-tooltip="true"
                    class="batch-ledger"
                  >
                    <el-table-column label="业务单号" prop="bizNo" min-width="180" />
                    <el-table-column label="类型" align="center" min-width="120">
                      <template #default="{ row }">
                        <span :class="resolveBizToneClass(row.bizType)">
                          {{ resolveBizLabel(row.bizType) }}
                        </span>
                      </template>
                    </el-table-column>
                    <el-table-column label="数量" align="right" min-width="120">
                      <template #default="{ row }">
                        <span class="ledger-number text-rose-600">{{
                          formatCount(row.outQty)
                        }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column label="入库时间" min-width="170">
                      <template #default="{ row }">{{ formatDateTime(row.inboundTime) }}</template>
                    </el-table-column>
                    <el-table-column label="备注" prop="remark" min-width="180" />
                    <template #empty>
                      <div class="batch-pane-empty">
                        <div class="batch-pane-empty__icon">
                          <Icon icon="ep:box" />
                        </div>
                        <div class="batch-pane-empty__title">暂无出库消耗</div>
                      </div>
                    </template>
                  </el-table>
                </el-tab-pane>

                <el-tab-pane label="预占明细" name="reservation">
                  <el-table
                    v-loading="batchTraceLoading"
                    :data="reservationList"
                    :stripe="true"
                    :show-overflow-tooltip="true"
                    class="batch-ledger"
                  >
                    <el-table-column label="业务单号" prop="bizNo" min-width="180" />
                    <el-table-column label="类型" align="center" min-width="120">
                      <template #default="{ row }">
                        <span :class="resolveBizToneClass(row.bizType)">
                          {{ resolveBizLabel(row.bizType) }}
                        </span>
                      </template>
                    </el-table-column>
                    <el-table-column label="数量" align="right" min-width="120">
                      <template #default="{ row }">
                        <span class="ledger-number text-amber-600">{{
                          formatCount(row.reservedQty)
                        }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column label="入库时间" min-width="170">
                      <template #default="{ row }">{{ formatDateTime(row.inboundTime) }}</template>
                    </el-table-column>
                    <el-table-column label="备注" prop="remark" min-width="180" />
                    <template #empty>
                      <div class="batch-pane-empty">
                        <div class="batch-pane-empty__icon">
                          <Icon icon="ep:box" />
                        </div>
                        <div class="batch-pane-empty__title">暂无预占明细</div>
                      </div>
                    </template>
                  </el-table>
                </el-tab-pane>

                <el-tab-pane label="批次流水" name="record">
                  <el-table
                    v-loading="batchTraceLoading"
                    :data="batchRecordList"
                    :stripe="true"
                    :show-overflow-tooltip="true"
                    class="batch-ledger"
                  >
                    <el-table-column label="业务单号" prop="bizNo" min-width="180" />
                    <el-table-column label="类型" align="center" min-width="120">
                      <template #default="{ row }">
                        <span :class="resolveBizToneClass(row.bizType)">
                          {{ resolveBizLabel(row.bizType) }}
                        </span>
                      </template>
                    </el-table-column>
                    <el-table-column label="数量" align="right" min-width="120">
                      <template #default="{ row }">
                        <span class="ledger-number">{{ formatCount(row.count) }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column label="变动后可用量" align="right" min-width="150">
                      <template #default="{ row }">
                        <span class="ledger-number">{{ formatCount(row.afterAvailableQty) }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column label="时间" min-width="170">
                      <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
                    </el-table-column>
                    <el-table-column label="备注" prop="remark" min-width="180" />
                    <template #empty>
                      <div class="batch-pane-empty">
                        <div class="batch-pane-empty__icon">
                          <Icon icon="ep:box" />
                        </div>
                        <div class="batch-pane-empty__title">暂无批次流水</div>
                      </div>
                    </template>
                  </el-table>
                  <Pagination
                    v-if="batchRecordTotal > 0"
                    class="custom-pagination"
                    :total="batchRecordTotal"
                    v-model:page="batchRecordQueryParams.pageNo"
                    v-model:limit="batchRecordQueryParams.pageSize"
                    @pagination="handleBatchRecordPageChange"
                  />
                </el-tab-pane>
              </el-tabs>
            </div>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import download from '@/utils/download'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { formatDate } from '@/utils/formatTime'
import { StockApi, StockVO } from '@/api/erp/stock/stock'
import {
  StockBatchApi,
  StockBatchAllocationVO,
  StockBatchRecordVO,
  StockBatchReservationVO,
  StockBatchVO
} from '@/api/erp/stock/batch'
import { ProductApi, ProductVO } from '@/api/erp/product/product'
import { WarehouseApi, WarehouseVO } from '@/api/erp/stock/warehouse'
import { WarehouseCategoryApi, WarehouseCategoryVO } from '@/api/erp/stock/warehouse-category'
import { useRoute, useRouter } from 'vue-router'
import {
  getTonePillClass,
  resolveBizToneClass,
  resolveDictLabel,
  resolveSummaryCardClass,
  resolveWarehouseTone
} from '../shared/stockTone'
import {
  buildRouteOpenStockRowFromBatch,
  findRouteOpenBatch,
  resolveStockRouteOpen
} from './stockRouteOpen.helpers'

defineOptions({ name: 'ErpStock' })

type StockRow = StockVO & {
  productName?: string
  unitName?: string
  categoryName?: string
  warehouseName?: string
  warehouseCategoryName?: string
}

const message = useMessage()
const route = useRoute()
const { replace } = useRouter()

const stockListLoading = ref(true)
const stockListError = ref('')
const list = ref<StockRow[]>([])
const total = ref(0)
const supportLoading = ref(true)
const supportLoadFailed = ref(false)
const supportErrorMessage = ref('筛选条件加载失败，请重试')
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  productId: undefined as number | undefined,
  warehouseId: undefined as number | undefined
})
const queryFormRef = ref()
const exportLoading = ref(false)
const productList = ref<ProductVO[]>([])
const warehouseList = ref<WarehouseVO[]>([])
const warehouseCategoryList = ref<WarehouseCategoryVO[]>([])

const batchDrawerOpen = ref(false)
const currentStock = ref<StockRow>()
const batchListLoading = ref(false)
const batchTraceLoading = ref(false)
const batchListError = ref('')
const batchTraceError = ref('')
const batchList = ref<StockBatchVO[]>([])
const batchTotal = ref(0)
const selectedBatch = ref<StockBatchVO>()
const activeBatchTab = ref('allocation')
const batchSessionToken = ref(0)
const allocationList = ref<StockBatchAllocationVO[]>([])
const reservationList = ref<StockBatchReservationVO[]>([])
const batchRecordList = ref<StockBatchRecordVO[]>([])
const batchRecordTotal = ref(0)
const routeOpenSyncing = ref(false)
const batchQueryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  productId: undefined as number | undefined,
  warehouseId: undefined as number | undefined,
  batchNo: undefined as string | undefined
})
const batchRecordQueryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  stockBatchId: undefined as number | undefined
})

const warehouseCategoryMap = computed(() =>
  new Map(warehouseCategoryList.value.map((item) => [item.id, item.name]))
)

const hasCurrentStock = computed(
  () => !!currentStock.value?.productId && !!currentStock.value?.warehouseId
)
const hasSelectedBatch = computed(() => !!selectedBatch.value?.id)
const canQueryStock = computed(() => !stockListLoading.value && !supportLoading.value)
const canExportStock = computed(() => !exportLoading.value)
const canQueryBatch = computed(() => hasCurrentStock.value && !batchListLoading.value)
const canViewBatchTrace = computed(() => hasSelectedBatch.value && !batchTraceLoading.value)
const isAnyBatchActionLoading = computed(() => batchListLoading.value || batchTraceLoading.value)
const canCloseBatchDrawer = computed(() => !isAnyBatchActionLoading.value)
const showStockListErrorState = computed(
  () => !!stockListError.value && !list.value.length && !stockListLoading.value
)
const isActiveBatchSession = (token: number) =>
  token === batchSessionToken.value && batchDrawerOpen.value
const normalizeRouteNumber = (value: unknown) => {
  if (typeof value !== 'string' || !value.trim()) {
    return undefined
  }
  const parsedValue = Number(value)
  return Number.isNaN(parsedValue) || parsedValue <= 0 ? undefined : parsedValue
}
const resolveBizLabel = (value?: number | string | boolean | null) =>
  resolveDictLabel(getIntDictOptions(DICT_TYPE.ERP_STOCK_RECORD_BIZ_TYPE), value)
const resolveWarehouseCategoryName = (row?: StockRow) => {
  if (!row) {
    return '未分类'
  }
  if (row.warehouseCategoryName) {
    return row.warehouseCategoryName
  }
  const warehouse = warehouseList.value.find((item) => item.id === row.warehouseId)
  if (!warehouse?.categoryId) {
    return '未分类'
  }
  return warehouseCategoryMap.value.get(warehouse.categoryId) || '未分类'
}
const resolveWarehouseCategoryClass = (row?: StockRow) =>
  getTonePillClass(resolveWarehouseTone(resolveWarehouseCategoryName(row)))
const stockSummaryCards = computed(() => [
  { label: '台账记录', value: formatCount(total.value), icon: 'ep:document', colorClass: 'stock-stat-icon--blue' },
  { label: '当前库存', value: formatCount(list.value.reduce((sum, row) => sum + Number(row.count || 0), 0)), icon: 'ep:box', colorClass: 'stock-stat-icon--emerald' },
  { label: '可追溯批次', value: formatCount(batchTotal.value), icon: 'ep:sort', colorClass: 'stock-stat-icon--amber' },
  { label: '已打开批次', value: formatCount(selectedBatch.value ? 1 : 0), icon: 'ep:folder-opened', colorClass: 'stock-stat-icon--slate' }
])

const getList = async () => {
  stockListLoading.value = true
  stockListError.value = ''
  try {
    const data = await StockApi.getStockPage(queryParams)
    list.value = data.list || []
    total.value = data.total || 0
  } catch {
    stockListError.value = '库存列表加载失败'
    message.error(stockListError.value)
  } finally {
    stockListLoading.value = false
  }
}

const loadFilterOptions = async () => {
  supportLoading.value = true
  supportLoadFailed.value = false
  supportErrorMessage.value = '筛选条件加载失败，请重试'
  try {
    const [productsResult, warehousesResult, categoriesResult] = await Promise.allSettled([
      ProductApi.getProductSimpleList(),
      WarehouseApi.getWarehouseSimpleList(),
      WarehouseCategoryApi.getWarehouseCategorySimpleList()
    ])
    if (productsResult.status === 'fulfilled') {
      productList.value = productsResult.value || []
    }
    if (warehousesResult.status === 'fulfilled') {
      warehouseList.value = warehousesResult.value || []
    }
    if (categoriesResult.status === 'fulfilled') {
      warehouseCategoryList.value = categoriesResult.value || []
    }
    if (productsResult.status === 'rejected' || warehousesResult.status === 'rejected' || categoriesResult.status === 'rejected') {
      supportLoadFailed.value = true
      supportErrorMessage.value = '筛选条件加载失败，请重试'
    }
  } finally {
    supportLoading.value = false
  }
}

const retrySupportOptions = async () => {
  await loadFilterOptions()
}

const handleQuery = () => {
  if (!canQueryStock.value) {
    return
  }
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  if (!canQueryStock.value) {
    return
  }
  queryFormRef.value?.resetFields()
  handleQuery()
}

const handleExport = async () => {
  if (!canExportStock.value) {
    return
  }
  try {
    await message.exportConfirm()
  } catch {
    return
  }
  try {
    exportLoading.value = true
    const data = await StockApi.exportStock(queryParams)
    download.excel(data, '产品库存.xls')
  } catch {
    message.error('导出失败')
  } finally {
    exportLoading.value = false
  }
}

const openBatchDrawer = async (row: StockRow, batchNo?: string) => {
  if (isAnyBatchActionLoading.value) {
    return
  }
  batchSessionToken.value += 1
  const sessionToken = batchSessionToken.value
  currentStock.value = row
  batchDrawerOpen.value = true
  activeBatchTab.value = 'allocation'
  batchQueryParams.pageNo = 1
  batchQueryParams.pageSize = 10
  batchQueryParams.productId = row.productId
  batchQueryParams.warehouseId = row.warehouseId
  batchQueryParams.batchNo = batchNo
  clearBatchTrace()
  await getBatchList(sessionToken)
}

const openBatchDrawerFromRouteFallback = async (
  productId: number,
  warehouseId: number,
  routeBatchNo?: string
) => {
  try {
    const data = await StockBatchApi.getStockBatchPage({
      pageNo: 1,
      pageSize: 50,
      productId,
      warehouseId
    })
    const routeBatch = findRouteOpenBatch(data.list || [], productId, warehouseId, routeBatchNo)
    if (!routeBatch) {
      message.warning('暂无符合条件的库存批次')
      return
    }
    const fallbackRow = buildRouteOpenStockRowFromBatch(routeBatch, { productId, warehouseId })
    batchSessionToken.value += 1
    const sessionToken = batchSessionToken.value
    currentStock.value = fallbackRow as StockRow
    batchDrawerOpen.value = true
    activeBatchTab.value = 'allocation'
    batchQueryParams.pageNo = 1
    batchQueryParams.pageSize = 10
    batchQueryParams.productId = productId
    batchQueryParams.warehouseId = warehouseId
    batchQueryParams.batchNo = undefined
    clearBatchTrace()
    batchList.value = data.list || []
    batchTotal.value = data.total || 0
    await selectBatch(routeBatch, sessionToken)
  } catch {
    batchListError.value = '批次余额加载失败'
    message.error(batchListError.value)
  }
}

const getBatchList = async (token = batchSessionToken.value) => {
  if (!hasCurrentStock.value) {
    return
  }
  batchListLoading.value = true
  batchListError.value = ''
  try {
    const data = await StockBatchApi.getStockBatchPage(batchQueryParams)
    if (!isActiveBatchSession(token)) {
      return
    }
    batchList.value = data.list || []
    batchTotal.value = data.total || 0
    const nextBatch =
      batchList.value.find((item) => item.id === selectedBatch.value?.id) || batchList.value[0]
    if (nextBatch) {
      await selectBatch(nextBatch, token)
    } else {
      clearBatchTrace()
    }
  } catch {
    if (!isActiveBatchSession(token)) {
      return
    }
    batchListError.value = '批次余额加载失败'
    message.error(batchListError.value)
  } finally {
    if (isActiveBatchSession(token)) {
      batchListLoading.value = false
    }
  }
}

const handleBatchQuery = () => {
  if (!canQueryBatch.value) {
    return
  }
  batchQueryParams.pageNo = 1
  getBatchList()
}

const resetBatchQuery = () => {
  if (!canQueryBatch.value) {
    return
  }
  batchQueryParams.batchNo = undefined
  handleBatchQuery()
}

const handleCurrentBatchChange = (row?: StockBatchVO) => {
  if (!row) {
    return
  }
  selectBatch(row)
}

const selectBatch = async (row: StockBatchVO, token = batchSessionToken.value) => {
  if (!isActiveBatchSession(token)) {
    return
  }
  if (batchTraceLoading.value) {
    return
  }
  if (!row?.id) {
    message.warning('请选择批次')
    return
  }
  selectedBatch.value = row
  batchRecordQueryParams.pageNo = 1
  batchRecordQueryParams.stockBatchId = row.id
  await getBatchTrace(token)
}

const getBatchTrace = async (token = batchSessionToken.value) => {
  if (!isActiveBatchSession(token) || !selectedBatch.value?.id) {
    return
  }
  batchTraceLoading.value = true
  batchTraceError.value = ''
  try {
    const [allocations, reservations, records] = await Promise.all([
      StockBatchApi.getAllocationListByStockBatch(selectedBatch.value.id),
      StockBatchApi.getReservationListByStockBatch(selectedBatch.value.id),
      StockBatchApi.getStockBatchRecordPage(batchRecordQueryParams)
    ])
    if (!isActiveBatchSession(token)) {
      return
    }
    allocationList.value = allocations || []
    reservationList.value = reservations || []
    batchRecordList.value = records.list || []
    batchRecordTotal.value = records.total || 0
  } catch {
    if (!isActiveBatchSession(token)) {
      return
    }
    batchTraceError.value = '批次追溯加载失败'
    message.error(batchTraceError.value)
  } finally {
    if (isActiveBatchSession(token)) {
      batchTraceLoading.value = false
    }
  }
}

const retryBatchTrace = () => {
  if (!hasSelectedBatch.value) {
    return
  }
  getBatchTrace()
}

const handleBatchRecordPageChange = () => {
  getBatchTrace()
}

const clearBatchTrace = () => {
  selectedBatch.value = undefined
  allocationList.value = []
  reservationList.value = []
  batchRecordList.value = []
  batchRecordTotal.value = 0
  batchRecordQueryParams.pageNo = 1
  batchRecordQueryParams.stockBatchId = undefined
  batchTraceError.value = ''
}

const handleBatchDrawerClosed = () => {
  batchSessionToken.value += 1
  currentStock.value = undefined
  activeBatchTab.value = 'allocation'
  batchList.value = []
  batchTotal.value = 0
  batchListError.value = ''
  batchListLoading.value = false
  batchTraceLoading.value = false
  batchQueryParams.batchNo = undefined
  batchQueryParams.productId = undefined
  batchQueryParams.warehouseId = undefined
  clearBatchTrace()
}

const handleBatchDrawerBeforeClose = (done: () => void) => {
  if (isAnyBatchActionLoading.value) {
    return
  }
  done()
}

const handleCloseBatchDrawer = () => {
  if (!canCloseBatchDrawer.value) {
    return
  }
  batchDrawerOpen.value = false
}

const formatCount = (value?: number | string) => {
  if (value === undefined || value === null || value === '') {
    return '0'
  }
  const numberValue = Number(value)
  if (!Number.isFinite(numberValue)) {
    return String(value)
  }
  return numberValue.toLocaleString('zh-CN', {
    minimumFractionDigits: 0,
    maximumFractionDigits: 6
  })
}

const formatDateTime = (value?: string) => (value ? formatDate(value) : '-')

const syncRouteOpen = async () => {
  if (routeOpenSyncing.value) {
    return
  }
  const routeOpen = resolveStockRouteOpen({
    productId: normalizeRouteNumber(route.query.productId),
    warehouseId: normalizeRouteNumber(route.query.warehouseId),
    batchNo: typeof route.query.batchNo === 'string' ? route.query.batchNo : '',
    openAction: typeof route.query.openAction === 'string' ? route.query.openAction : ''
  })
  if (routeOpen.action === 'none') {
    return
  }

  routeOpenSyncing.value = true
  try {
    queryParams.productId = routeOpen.productId
    queryParams.warehouseId = routeOpen.warehouseId
    queryParams.pageNo = 1
    await getList()

    if (routeOpen.action === 'batch-trace') {
      const targetRow = list.value.find(
        (item) =>
          item.productId === routeOpen.productId && item.warehouseId === routeOpen.warehouseId
      )
      if (targetRow) {
        await openBatchDrawer(targetRow, routeOpen.batchNo)
      } else {
        await openBatchDrawerFromRouteFallback(
          routeOpen.productId,
          routeOpen.warehouseId,
          routeOpen.batchNo
        )
      }
      return
    }

    const nextQuery = { ...route.query }
    routeOpen.cleanupKeys.forEach((key) => delete nextQuery[key])
    await replace({ path: route.path, query: nextQuery })
  } finally {
    routeOpenSyncing.value = false
  }
}

onMounted(async () => {
  await Promise.allSettled([getList(), loadFilterOptions()])
  await syncRouteOpen()
})

watch(
  () => [route.query.productId, route.query.warehouseId, route.query.batchNo, route.query.openAction],
  async () => {
    await syncRouteOpen()
  }
)
</script>

<style scoped>
.stock-page-shell {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.stock-page__title-card,
.stock-page__filter-card,
.stock-page__list-card {
  min-width: 0;
}

.stock-page__title-card {
  padding: 20px 20px 6px;
}

.stock-page__title {
  margin-bottom: 0;
  color: #0f172a;
  font-family: 'IBM Plex Sans', Inter, sans-serif;
  font-size: 22px;
  line-height: 30px;
  font-weight: 700;
}

.stock-query__panel,
.stock-page__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.stock-query,
.stock-query__actions,
.batch-drawer-query-card__actions,
.batch-drawer-summary__pills {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.stock-query {
  flex-direction: column;
  gap: 12px;
}

.stock-query__panel {
  align-items: end;
}

.stock-query__grid {
  display: grid;
  flex: 1 1 560px;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.stock-action-btn {
  flex: 0 0 auto;
  min-width: 88px;
  min-height: 40px;
  padding-inline: 14px;
  white-space: nowrap;
}

.stock-action-btn :deep(.el-button) {
  flex: 0 0 auto;
  white-space: nowrap;
}

.stock-page__overview-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 14px;
  margin-bottom: 16px;
}

.stock-page__overview-card {
  display: flex;
  min-height: 92px;
  align-items: center;
  gap: 14px;
  padding: 16px 18px;
}

.stock-page__overview-card:hover {
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  transform: translateY(-1px);
}

.stock-stat-icon--blue {
  background: #eff6ff;
  color: #2563eb;
}

.stock-stat-icon--emerald {
  background: #ecfdf5;
  color: #059669;
}

.stock-stat-icon--amber {
  background: #fffbeb;
  color: #d97706;
}

.stock-page__overview-card__content {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 2px;
  min-width: 0;
  flex: 1;
}

.stock-page__overview-card__label {
  color: #64748b;
  font-size: 13px;
  line-height: 20px;
}

.stock-page__overview-card__value {
  color: #0f172a;
  font-size: 28px;
  line-height: 34px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  font-family:
    ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New',
    monospace;
}

.stock-page__overview-card__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 12px;
  font-size: 22px;
  flex-shrink: 0;
}

.stock-page__overview-card--export {
  justify-content: center;
  padding: 16px;
}

.stock-page__export-btn {
  width: 100%;
  min-height: 40px;
  border-color: #dbe4f0;
  background: #fff;
}

.stock-page__table-shell {
  overflow-x: auto;
}

.stock-ledger {
  min-width: 860px;
}

.stock-ledger :deep(.el-table) {
  --el-table-border-color: transparent;
  --el-table-header-bg-color: #0f4c5c;
  --el-table-header-text-color: #fff;
}

.stock-ledger :deep(.el-table th.el-table__cell) {
  background-color: #0f4c5c;
  color: #fff;
  font-weight: 600;
  font-size: 13px;
  border-bottom: none;
}

.stock-ledger :deep(.el-table__header),
.batch-ledger :deep(.el-table__header) {
  background: #1f2937;
}

.stock-ledger :deep(.el-table__header-wrapper th),
.batch-ledger :deep(.el-table__header-wrapper th) {
  background: #1f2937;
}

.stock-ledger :deep(.el-table__header th),
.batch-ledger :deep(.el-table__header th) {
  color: #fff;
  font-weight: 600;
}

.stock-ledger :deep(.el-table__cell) {
  height: 64px;
}

.stock-ledger :deep(.el-table td.el-table__cell),
.stock-ledger :deep(.el-table th.el-table__cell.is-leaf),
.batch-ledger :deep(.el-table td.el-table__cell),
.batch-ledger :deep(.el-table th.el-table__cell.is-leaf) {
  border-bottom: 1px solid #f1f5f9;
}

.stock-ledger :deep(.el-table__header-wrapper) {
  position: sticky;
  top: 0;
  z-index: 3;
}

.ledger-product {
  display: flex;
  align-items: center;
  gap: 10px;
}

.ledger-product__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: #f1f5f9;
  color: #64748b;
  font-size: 16px;
  flex-shrink: 0;
}

.ledger-product__info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.ledger-product__name,
.ledger-number {
  color: #0f172a;
  font-weight: 700;
}

.ledger-product__name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ledger-product__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px 10px;
  color: #64748b;
  font-size: 12px;
  line-height: 18px;
}

.ledger-warehouse {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.ledger-warehouse__name {
  color: #0f172a;
  font-size: 13px;
  font-weight: 500;
}

.ledger-warehouse__chip {
  display: inline-flex;
  align-items: center;
  align-self: flex-start;
  justify-content: center;
  width: fit-content;
  max-width: 100%;
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 12px;
  line-height: 18px;
  font-weight: 600;
  white-space: nowrap;
  transition:
    background-color 0.2s ease,
    border-color 0.2s ease,
    color 0.2s ease;
}

.ledger-warehouse-tag {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  border: 1px solid #a7f3d0;
  border-radius: 8px;
  background: #ecfdf5;
  color: #059669;
  font-size: 12px;
  line-height: 18px;
  font-weight: 500;
}

.ledger-warehouse__chip.bg-blue-50 {
  border-color: #bfdbfe;
  background: #eff6ff;
  color: #2563eb;
}

.ledger-warehouse__chip.bg-emerald-50 {
  border-color: #bbf7d0;
  background: #ecfdf5;
  color: #059669;
}

.ledger-warehouse__chip.bg-amber-50 {
  border-color: #fde68a;
  background: #fffbeb;
  color: #d97706;
}

.ledger-warehouse__chip.bg-rose-50 {
  border-color: #fecdd3;
  background: #fff1f2;
  color: #e11d48;
}

.ledger-warehouse__chip.bg-slate-100 {
  border-color: #e2e8f0;
  background: #f8fafc;
  color: #475569;
}

.batch-drawer-summary__chip.bg-blue-50,
.record-drawer-summary__chip.bg-blue-50 {
  border-color: #bfdbfe;
  background: #eff6ff;
  color: #2563eb;
}

.batch-drawer-summary__chip.bg-emerald-50,
.record-drawer-summary__chip.bg-emerald-50 {
  border-color: #bbf7d0;
  background: #ecfdf5;
  color: #059669;
}

.batch-drawer-summary__chip.bg-amber-50,
.record-drawer-summary__chip.bg-amber-50 {
  border-color: #fde68a;
  background: #fffbeb;
  color: #d97706;
}

.batch-drawer-summary__chip.bg-rose-50,
.record-drawer-summary__chip.bg-rose-50 {
  border-color: #fecdd3;
  background: #fff1f2;
  color: #e11d48;
}

.batch-drawer-summary__chip.bg-slate-100,
.record-drawer-summary__chip.bg-slate-100 {
  border-color: #e2e8f0;
  background: #f8fafc;
  color: #475569;
}

.ledger-number,
.batch-drawer-summary__mono,
.batch-drawer-summary__total-value {
  font-family:
    ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New',
    monospace;
}

.stock-page__record-count {
  color: #64748b;
  font-size: 13px;
  line-height: 20px;
}

.stock-empty,
.batch-empty {
  display: flex;
  min-height: 160px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #64748b;
}

.stock-empty__icon,
.batch-empty__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 999px;
  background: #f8fafc;
  color: #94a3b8;
  font-size: 24px;
}

.stock-empty--error .stock-empty__icon {
  background: rgba(244, 63, 94, 0.08);
  color: #e11d48;
}

.stock-empty__title,
.batch-empty__title,
.batch-drawer-head__title,
.batch-drawer-summary__title,
.batch-detail-card__title {
  color: #0f172a;
  font-weight: 700;
}

.stock-empty__title,
.batch-empty__title {
  font-size: 14px;
  line-height: 22px;
}

.batch-empty__desc {
  color: #94a3b8;
  font-size: 12px;
  line-height: 18px;
}

.batch-trace-empty {
  display: flex;
  min-height: 120px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #64748b;
}

.batch-trace-empty__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  border-radius: 999px;
  background: #f8fafc;
  color: #94a3b8;
  font-size: 18px;
}

.batch-trace-empty__title {
  color: #475569;
  font-size: 13px;
  line-height: 20px;
  font-weight: 600;
}

.batch-pane-empty {
  display: flex;
  min-height: 108px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #64748b;
}

.batch-pane-empty__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 999px;
  background: #f8fafc;
  color: #94a3b8;
  font-size: 16px;
}

.batch-pane-empty__title {
  color: #64748b;
  font-size: 13px;
  line-height: 20px;
  font-weight: 500;
}

.batch-drawer-shell {
  display: flex;
  height: 100%;
  flex-direction: column;
  background: #f8fafc;
}

.batch-drawer-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 20px;
  border-bottom: 1px solid #e2e8f0;
  background: #fff;
}

.batch-drawer-head__title {
  display: inline-flex;
  align-items: center;
  margin: 0;
  color: #0f172a;
  font-size: 18px;
  line-height: 28px;
}

.batch-drawer-head__close {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: 0;
  border-radius: 10px;
  background: transparent;
  color: #94a3b8;
  transition:
    background-color 0.2s ease,
    color 0.2s ease;
}

.batch-drawer-head__close:hover:not(:disabled) {
  background: #f1f5f9;
  color: #475569;
}

.batch-drawer-head__close:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.batch-drawer-body {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 16px 20px 20px;
}

.batch-drawer-summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  border: 1px solid #dbe4f0;
  border-radius: 14px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
}

.batch-drawer-summary__main {
  display: flex;
  min-width: 0;
  flex: 1;
  flex-direction: column;
  gap: 6px;
}

.batch-drawer-summary__head {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.batch-drawer-summary__eyebrow {
  color: #64748b;
  font-size: 12px;
  line-height: 18px;
  font-weight: 600;
}

.batch-drawer-summary__title {
  color: #0f172a;
  font-size: 15px;
  line-height: 22px;
}

.batch-drawer-summary__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px 10px;
  color: #64748b;
  font-size: 12px;
  line-height: 18px;
}

.batch-drawer-summary__mono {
  color: #2563eb;
  font-weight: 600;
}

.batch-drawer-summary__total {
  text-align: right;
  flex-shrink: 0;
}

.batch-drawer-summary__total-label {
  color: #64748b;
  font-size: 12px;
  line-height: 18px;
}

.batch-drawer-summary__total-value {
  margin-top: 4px;
  color: #2563eb;
  font-size: 22px;
  line-height: 1;
  font-weight: 700;
}

.batch-drawer-body__content {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 12px;
}

.batch-drawer-query-card,
.batch-detail-card,
.batch-trace-card {
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  background: #fff;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
}

.batch-drawer-query-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 12px 14px;
}

.batch-drawer-query-card__field {
  width: 100%;
  max-width: 360px;
}

.batch-detail-card__header,
.batch-detail-card__footer,
.batch-trace-card__header {
  padding: 10px 14px;
}

.batch-detail-card__header,
.batch-trace-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  flex-wrap: wrap;
  border-bottom: 1px solid #f1f5f9;
  background: rgba(248, 250, 252, 0.62);
}

.batch-detail-card__title,
.batch-trace-card__title {
  margin: 0;
  font-size: 14px;
  line-height: 22px;
}

.batch-detail-card__meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.batch-detail-card__footer {
  display: flex;
  justify-content: flex-end;
  border-top: 1px solid #f1f5f9;
  background: #fff;
}

.batch-trace-tabs {
  padding: 0 12px 12px;
}

.batch-trace-card :deep(.el-tabs__header) {
  margin: 0;
  padding: 0 2px;
}

.batch-trace-card :deep(.el-tabs__nav-wrap::after) {
  background-color: #f1f5f9;
}

.batch-trace-card :deep(.el-tabs__item) {
  height: 42px;
  line-height: 42px;
  color: #475569;
}

.batch-trace-card :deep(.el-tabs__item.is-active) {
  color: #2563eb;
  font-weight: 600;
}

.batch-trace-card :deep(.el-tabs__active-bar) {
  background-color: #2563eb;
}

.batch-trace-card :deep(.el-tabs__content) {
  padding-top: 10px;
}

.batch-pill {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  padding: 0 8px;
  border: 1px solid transparent;
  border-radius: 999px;
  font-size: 11px;
  line-height: 16px;
  font-weight: 600;
}

.batch-pill--primary {
  border-color: #bfdbfe;
  background: #eff6ff;
  color: #2563eb;
}

.batch-pill--success,
.batch-qty--success {
  border-color: #a7f3d0;
  background: #ecfdf5;
  color: #059669;
}

.batch-pill--warning {
  border-color: #fde68a;
  background: #fffbeb;
  color: #d97706;
}

.batch-pill--danger {
  border-color: #fecdd3;
  background: #fff1f2;
  color: #e11d48;
}

.batch-pill--neutral {
  border-color: #e2e8f0;
  background: #f8fafc;
  color: #475569;
}

.stock-biz-pill {
  display: inline-flex;
  align-items: center;
  max-width: 100%;
  padding: 2px 10px;
  border-radius: 999px;
  border: 1px solid transparent;
  font-size: 12px;
  line-height: 18px;
  font-weight: 600;
  white-space: nowrap;
}

.batch-qty {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border: 1px solid transparent;
  border-radius: 8px;
}

.batch-ledger {
  min-width: 880px;
}

.custom-pagination :deep(.el-pager li.is-active) {
  background: #eff6ff;
  color: #2563eb;
  border: 1px solid #bfdbfe;
}

:deep(.premium-drawer .el-drawer__body) {
  padding: 0;
  overflow: hidden;
}

:deep(.premium-drawer__mask) {
  backdrop-filter: blur(8px);
  background: rgba(15, 23, 42, 0.34);
}

@media (max-width: 1439px) {
  .stock-page__overview-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 1279px) {
  .stock-query__panel {
    align-items: stretch;
    flex-direction: column;
  }

  .stock-query__actions,
  .batch-drawer-query-card__actions {
    width: 100%;
  }

  .stock-query__actions :deep(.el-button),
  .batch-drawer-query-card__actions :deep(.el-button) {
    flex: 1 1 0;
    min-width: 0;
  }

  .stock-page__overview-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 1439px) {
  .batch-drawer-summary,
  .batch-drawer-query-card {
    align-items: stretch;
    flex-direction: column;
  }

  .batch-drawer-summary__total {
    text-align: left;
  }

  .batch-drawer-query-card__field {
    max-width: none;
  }
}

@media (max-width: 959px) {
  .stock-page-shell {
    gap: 12px;
  }

  .stock-query__grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .stock-page__footer,
  .batch-drawer-summary,
  .batch-drawer-query-card {
    align-items: stretch;
    flex-direction: column;
  }

  .stock-page__overview-grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .batch-drawer-query-card__field {
    max-width: none;
  }

  .batch-drawer-summary__total {
    text-align: left;
  }
}

@media (max-width: 767px) {
  .stock-page__title {
    font-size: 22px;
    line-height: 30px;
  }

  .batch-drawer-head,
  .batch-drawer-body {
    padding-left: 16px;
    padding-right: 16px;
  }

  .batch-drawer-summary__total {
    text-align: left;
  }
}
</style>
