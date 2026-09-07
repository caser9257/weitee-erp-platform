<template>
  <div class="stock-record-page">
    <ContentWrap class="stock-record-page__title-card" :body-style="{ padding: '24px' }" :class="getToneCardClass('blue')">
      <div class="stock-record-page__title">库存明细账</div>
    </ContentWrap>

    <ContentWrap class="stock-record-page__filter-card" :body-style="{ padding: '24px' }" :class="getToneCardClass('slate')">
      <div class="stock-record-page__section-head">
        <div class="stock-record-page__section-title">筛选条件</div>
      </div>

      <el-alert
        v-if="supportLoadFailed"
        :title="supportErrorMessage"
        type="warning"
        show-icon
        :closable="false"
        class="mb-12px"
      >
        <template #default>
          <el-button class="stock-action-btn" link type="primary" :disabled="supportLoading" @click="retrySupportOptions">
            重新加载
          </el-button>
        </template>
      </el-alert>

      <el-form ref="queryFormRef" :model="queryParams" label-position="top" class="stock-record-query">
        <div class="stock-record-query__grid stock-record-query__grid--primary">
          <el-form-item label="产品" prop="productId">
            <ProductRemoteSelect v-model="queryParams.productId" placeholder="请选择产品" />
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
          <el-form-item label="类型" prop="bizType">
            <el-select v-model="queryParams.bizType" clearable placeholder="请选择类型">
              <el-option
                v-for="dict in getIntDictOptions(DICT_TYPE.ERP_STOCK_RECORD_BIZ_TYPE)"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="业务单号" prop="bizNo">
            <el-input
              v-model="queryParams.bizNo"
              placeholder="请输入业务单号"
              clearable
              @keyup.enter="handleQuery"
            />
          </el-form-item>
        </div>

        <transition name="stock-record-query-collapse">
          <div
            v-if="advancedSearchVisible"
            class="stock-record-query__grid stock-record-query__grid--advanced"
          >
            <el-form-item label="创建时间" prop="createTime">
              <el-date-picker
                v-model="queryParams.createTime"
                value-format="YYYY-MM-DD HH:mm:ss"
                type="daterange"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                range-separator="-"
                :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
              />
            </el-form-item>
          </div>
        </transition>

        <div class="stock-record-query__footer">
          <el-button link type="primary" class="stock-record-query__toggle" @click="toggleAdvancedSearch">
            {{ advancedSearchVisible ? '收起高级筛选' : '展开高级筛选' }}
            <Icon :icon="advancedSearchVisible ? 'ep:arrow-up' : 'ep:arrow-down'" class="ml-4px" />
          </el-button>
          <div class="stock-record-query__actions">
            <el-button class="stock-action-btn" :disabled="!canQueryRecord" @click="resetQuery">
              <Icon icon="ep:refresh" class="mr-5px" /> 重置
            </el-button>
            <el-button class="stock-action-btn" type="primary" :loading="listLoading" :disabled="!canQueryRecord" @click="handleQuery">
              <Icon icon="ep:search" class="mr-5px" /> 查询
            </el-button>
          </div>
        </div>
      </el-form>
    </ContentWrap>

    <ContentWrap class="stock-record-page__summary-card" :body-style="{ padding: '24px' }" :class="getToneCardClass('emerald')">
      <div class="stock-record-summary-grid">
        <article v-for="card in summaryCards" :key="card.label" class="stock-record-summary-card" :class="card.toneClass">
          <div class="stock-record-summary-card__main">
            <div class="stock-record-summary-card__label">{{ card.label }}</div>
            <div class="stock-record-summary-card__value">{{ card.value }}</div>
          </div>
          <div class="stock-record-summary-card__icon" :class="card.iconClass">
            <Icon :icon="card.icon" />
          </div>
        </article>
      </div>
    </ContentWrap>

    <ContentWrap class="stock-record-page__list-card" :body-style="{ padding: '24px' }" :class="getToneCardClass('slate')">
      <div class="stock-record-toolbar">
        <div></div>
        <div class="stock-record-toolbar__actions">
          <el-button
            class="stock-action-btn"
            plain
            :loading="exportLoading"
            :disabled="!canExportRecord"
            @click="handleExport"
            v-hasPermi="['erp:stock-record:export']"
          >
            <Icon icon="ep:download" class="mr-5px" /> 导出明细账
          </el-button>
        </div>
      </div>

      <div class="stock-record-page__table-shell">
        <el-table
          v-loading="listLoading"
          :data="recordList"
          :stripe="true"
          :show-overflow-tooltip="true"
          class="stock-record-ledger"
        >
          <template #empty>
            <div v-if="listLoadFailed" class="stock-record-empty stock-record-empty--error">
              <div class="stock-record-empty__icon">
                <Icon icon="ep:warning-filled" />
              </div>
              <div class="stock-record-empty__title">列表加载失败</div>
              <el-button class="stock-action-btn" type="primary" plain :disabled="!canRetryList" @click="handleRetryList">
                重试加载
              </el-button>
            </div>
            <div v-else class="stock-record-empty">
              <div class="stock-record-empty__icon">
                <Icon icon="ep:document" />
              </div>
              <div class="stock-record-empty__title">暂无库存流水</div>
            </div>
          </template>

          <el-table-column label="流水号 & 日期" min-width="220">
            <template #default="{ row }">
              <div class="ledger-record">
                <div class="ledger-record__no">{{ row.bizNo || '-' }}</div>
                <div class="ledger-record__meta">{{ formatDateValue(row.createTime) }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="产品摘要" min-width="250">
            <template #default="{ row }">
              <div class="ledger-product">
                <div class="ledger-product__name">{{ row.productName || '-' }}</div>
                <div class="ledger-product__chips">
                  <span class="ledger-product__chip">{{ row.categoryName || '-' }}</span>
                  <span class="ledger-product__chip ledger-product__chip--slate">{{ row.unitName || '-' }}</span>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="所属仓库" min-width="180">
            <template #default="{ row }">
              <div class="ledger-warehouse">
                <div class="ledger-warehouse__name">{{ row.warehouseName || '-' }}</div>
                <div :class="['ledger-warehouse__chip', resolveWarehouseCategoryClass(row)]">
                  {{ resolveWarehouseCategoryName(row) }}
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="业务类型" min-width="120" align="center">
            <template #default="{ row }">
              <span :class="resolveBizToneClass(row.bizType)">
                {{ resolveBizLabel(row.bizType) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="出入库数量" min-width="140" align="right">
            <template #default="{ row }">
              <span class="ledger-number" :class="Number(row.count || 0) < 0 ? 'ledger-number--negative' : 'ledger-number--positive'">
                {{ formatCount(row.count) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="库存量" min-width="140" align="right">
            <template #default="{ row }">
              <span class="ledger-number">{{ formatCount(row.totalCount) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" fixed="right" width="120">
            <template #default="{ row }">
              <el-button class="stock-action-btn" plain type="primary" size="small" @click="openRecordDrawer(row)">
                <Icon icon="ep:view" class="mr-5px" /> 查看
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="stock-record-page__footer">
        <div class="stock-record-page__record-count">共 {{ total }} 条记录</div>
        <Pagination
          :total="total"
          v-model:page="queryParams.pageNo"
          v-model:limit="queryParams.pageSize"
          @pagination="handlePagination"
        />
      </div>
    </ContentWrap>

    <el-drawer
      v-model="recordDrawerOpen"
      size="64%"
      destroy-on-close
      :with-header="false"
      modal-class="stock-record-drawer__mask"
      :close-on-click-modal="true"
      :close-on-press-escape="true"
      @closed="handleRecordDrawerClosed"
    >
      <div v-if="selectedRecord" class="record-drawer-shell">
        <div class="record-drawer-head">
          <h2 class="record-drawer-head__title">流水详情</h2>
          <button type="button" class="record-drawer-head__close" @click="handleCloseRecordDrawer">
            <Icon icon="ep:close" />
          </button>
        </div>

        <div class="record-drawer-body">
          <div class="record-drawer-summary">
            <div class="record-drawer-summary__context">
              <div class="record-drawer-summary__eyebrow">
                <span
                  :class="[
                    'record-drawer-summary__chip',
                    resolveWarehouseCategoryClass(selectedRecord)
                  ]"
                >
                  {{ resolveWarehouseCategoryName(selectedRecord) }}
                </span>
              </div>
              <div class="record-drawer-summary__title">{{ selectedRecord.productName || '-' }}</div>
              <div class="record-drawer-summary__meta">
                <span class="record-drawer-summary__mono">{{ selectedRecord.bizNo || '-' }}</span>
                <span>{{ formatDateValue(selectedRecord.createTime) }}</span>
              </div>
              <div class="record-drawer-summary__chips">
                <span :class="resolveBizToneClass(selectedRecord.bizType)">{{ resolveBizLabel(selectedRecord.bizType) }}</span>
              </div>
            </div>
            <div class="record-drawer-summary__metrics">
              <div class="record-drawer-summary__metric">
                <div class="record-drawer-summary__metric-label">出入库数量</div>
                <div class="record-drawer-summary__metric-value" :class="getToneTextClass(resolveCountTone(selectedRecord.count))">
                  {{ formatCount(selectedRecord.count) }}
                </div>
              </div>
              <div class="record-drawer-summary__metric">
                <div class="record-drawer-summary__metric-label">库存量</div>
                <div class="record-drawer-summary__metric-value">
                  {{ formatCount(selectedRecord.totalCount) }}
                </div>
              </div>
            </div>
          </div>

          <div class="record-drawer-grid">
            <div class="record-detail-card">
              <div class="record-detail-card__header">
                <h3 class="record-detail-card__title">业务信息</h3>
              </div>
              <div class="record-detail-card__body record-detail-card__body--grid">
                <div class="record-detail-item">
                  <span class="record-detail-item__label">业务单号</span>
                  <span class="record-detail-item__value record-detail-item__value--mono">{{ selectedRecord.bizNo || '-' }}</span>
                </div>
                <div class="record-detail-item">
                  <span class="record-detail-item__label">业务类型</span>
                  <span class="record-detail-item__value">
                    <span :class="resolveBizToneClass(selectedRecord.bizType)">
                      {{ resolveBizLabel(selectedRecord.bizType) }}
                    </span>
                  </span>
                </div>
                <div class="record-detail-item">
                  <span class="record-detail-item__label">创建时间</span>
                  <span class="record-detail-item__value record-detail-item__value--mono">
                    {{ formatDateValue(selectedRecord.createTime) }}
                  </span>
                </div>
              </div>
            </div>

            <div class="record-detail-card">
              <div class="record-detail-card__header">
                <h3 class="record-detail-card__title">产品与仓库</h3>
              </div>
              <div class="record-detail-card__body record-detail-card__body--grid">
                <div class="record-detail-item">
                  <span class="record-detail-item__label">产品</span>
                  <span class="record-detail-item__value">{{ selectedRecord.productName || '-' }}</span>
                </div>
                <div class="record-detail-item">
                  <span class="record-detail-item__label">分类</span>
                  <span class="record-detail-item__value">{{ selectedRecord.categoryName || '-' }}</span>
                </div>
                <div class="record-detail-item">
                  <span class="record-detail-item__label">单位</span>
                  <span class="record-detail-item__value">{{ selectedRecord.unitName || '-' }}</span>
                </div>
                <div class="record-detail-item">
                  <span class="record-detail-item__label">仓库</span>
                  <span class="record-detail-item__value record-detail-item__value--stacked">
                    <span>{{ selectedRecord.warehouseName || '-' }}</span>
                    <span class="record-detail-item__sub">{{ resolveWarehouseCategoryName(selectedRecord) }}</span>
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { getIntDictOptions, DICT_TYPE } from '@/utils/dict'
import { formatDate } from '@/utils/formatTime'
import download from '@/utils/download'
import { StockRecordApi, StockRecordVO } from '@/api/erp/stock/record'
import { ProductApi, ProductVO } from '@/api/erp/product/product'
import { WarehouseApi, WarehouseVO } from '@/api/erp/stock/warehouse'
import {
  getToneCardClass,
  getTonePillClass,
  getToneTextClass,
  resolveBizToneClass,
  resolveCountTone,
  resolveDictLabel,
  resolveWarehouseTone
} from '../shared/stockTone'

defineOptions({ name: 'ErpStockRecord' })

type StockRecordRow = StockRecordVO & {
  productName?: string
  categoryName?: string
  unitName?: string
  warehouseName?: string
  warehouseCategoryName?: string
  creatorName?: string
  createTime?: string | Date
  totalCount?: number
}

type SummaryCard = {
  label: string
  value: string
  toneClass: string
  icon: string
  iconClass: string
}

const message = useMessage()

const listLoading = ref(true)
const listLoadFailed = ref(false)
const recordList = ref<StockRecordRow[]>([])
const total = ref(0)
const supportLoading = ref(true)
const supportLoadFailed = ref(false)
const supportErrorMessage = ref('筛选条件加载失败，请重试')
const advancedSearchVisible = ref(false)
const recordDrawerOpen = ref(false)
const selectedRecord = ref<StockRecordRow>()
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  productId: undefined as number | undefined,
  warehouseId: undefined as number | undefined,
  bizType: undefined as number | undefined,
  bizNo: undefined as string | undefined,
  createTime: [] as string[]
})
const queryFormRef = ref()
const exportLoading = ref(false)
const productList = ref<ProductVO[]>([])
const warehouseList = ref<WarehouseVO[]>([])

const canRetryList = computed(() => listLoadFailed.value && !listLoading.value)
const canQueryRecord = computed(() => !listLoading.value && !supportLoading.value)
const canExportRecord = computed(() => !exportLoading.value && !listLoading.value)

const summaryCards = computed<SummaryCard[]>(() => {
  const records = recordList.value
  const inboundTotal = records
    .filter((row) => Number(row.count || 0) > 0)
    .reduce((sum, row) => sum + Number(row.count || 0), 0)
  const outboundTotal = records
    .filter((row) => Number(row.count || 0) < 0)
    .reduce((sum, row) => sum + Math.abs(Number(row.count || 0)), 0)
  const firstStock = records.length
    ? Number(records[records.length - 1].totalCount || 0) - Number(records[records.length - 1].count || 0)
    : 0
  const lastStock = records.length ? Number(records[0].totalCount || 0) : 0
  return [
    {
      label: '期初库存',
      value: formatCount(firstStock),
      toneClass: 'stock-record-summary-card--blue',
      icon: 'ep:lock',
      iconClass: 'stock-record-summary-card__icon--blue'
    },
    {
      label: '总入库数量',
      value: formatCount(inboundTotal),
      toneClass: 'stock-record-summary-card--emerald',
      icon: 'ep:top-right',
      iconClass: 'stock-record-summary-card__icon--emerald'
    },
    {
      label: '总出库数量',
      value: formatCount(outboundTotal),
      toneClass: 'stock-record-summary-card--amber',
      icon: 'ep:bottom-right',
      iconClass: 'stock-record-summary-card__icon--amber'
    },
    {
      label: '期末库存',
      value: formatCount(lastStock),
      toneClass: 'stock-record-summary-card--rose',
      icon: 'ep:trend-charts',
      iconClass: 'stock-record-summary-card__icon--rose'
    }
  ]
})

const resolveBizLabel = (value?: number | string | boolean | null) =>
  resolveDictLabel(getIntDictOptions(DICT_TYPE.ERP_STOCK_RECORD_BIZ_TYPE), value)

const formatCount = (value?: number | string | null) => {
  const numberValue = Number(value || 0)
  if (Number.isInteger(numberValue)) {
    return numberValue.toLocaleString('zh-CN')
  }
  return numberValue.toFixed(3).replace(/\.?0+$/, '')
}

const formatDateValue = (value?: string | Date) => (value ? formatDate(value) : '-')

const resolveWarehouseCategoryName = (row: StockRecordRow) => {
  if (row.warehouseCategoryName) {
    return row.warehouseCategoryName
  }
  const warehouse = warehouseList.value.find((item) => item.id === row.warehouseId)
  return warehouse?.categoryName || '未分类'
}
const resolveWarehouseCategoryClass = (row: StockRecordRow) =>
  getTonePillClass(resolveWarehouseTone(resolveWarehouseCategoryName(row)))

const getList = async () => {
  listLoading.value = true
  listLoadFailed.value = false
  try {
    const data = await StockRecordApi.getStockRecordPage({
      ...queryParams,
      pageNo: queryParams.pageNo,
      pageSize: queryParams.pageSize
    })
    recordList.value = data.list || []
    total.value = data.total || 0
  } catch {
    recordList.value = []
    total.value = 0
    listLoadFailed.value = true
  } finally {
    listLoading.value = false
  }
}

const loadFilterOptions = async () => {
  supportLoading.value = true
  supportLoadFailed.value = false
  supportErrorMessage.value = '筛选条件加载失败，请重试'
  try {
    const [productsResult, warehousesResult] = await Promise.allSettled([
      Promise.resolve([]),
      WarehouseApi.getWarehouseSimpleList()
    ])
    if (productsResult.status === 'fulfilled') {
      productList.value = productsResult.value || []
    }
    if (warehousesResult.status === 'fulfilled') {
      warehouseList.value = warehousesResult.value || []
    }
    if (productsResult.status === 'rejected' || warehousesResult.status === 'rejected') {
      supportLoadFailed.value = true
    }
  } finally {
    supportLoading.value = false
  }
}

const retrySupportOptions = async () => {
  await loadFilterOptions()
}

const handleQuery = async () => {
  if (!canQueryRecord.value) {
    return
  }
  queryParams.pageNo = 1
  await getList()
}

const resetQuery = async () => {
  if (!canQueryRecord.value) {
    return
  }
  queryFormRef.value?.resetFields()
  advancedSearchVisible.value = false
  await handleQuery()
}

const handleRetryList = async () => {
  await getList()
}

const handlePagination = async ({ page, limit }: { page: number; limit: number }) => {
  queryParams.pageNo = page
  queryParams.pageSize = limit
  await getList()
}

const toggleAdvancedSearch = () => {
  advancedSearchVisible.value = !advancedSearchVisible.value
}

const handleExport = async () => {
  if (!canExportRecord.value) {
    return
  }
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await StockRecordApi.exportStockRecord({
      ...queryParams,
      pageNo: queryParams.pageNo,
      pageSize: queryParams.pageSize
    })
    download.excel(data, '库存明细账.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

const openRecordDrawer = (row: StockRecordRow) => {
  selectedRecord.value = row
  recordDrawerOpen.value = true
}

const handleCloseRecordDrawer = () => {
  recordDrawerOpen.value = false
}

const handleRecordDrawerClosed = () => {
  selectedRecord.value = undefined
}

onMounted(async () => {
  await Promise.allSettled([getList(), loadFilterOptions()])
})
</script>

<style scoped>
.stock-record-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stock-record-page__title-card,
.stock-record-page__filter-card,
.stock-record-page__summary-card,
.stock-record-page__list-card {
  min-width: 0;
}

.stock-record-page__title {
  color: #0f172a;
  font-family: 'IBM Plex Sans', Inter, sans-serif;
  font-size: 22px;
  line-height: 30px;
  font-weight: 600;
}

.stock-record-page__section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.stock-record-page__section-title {
  color: #0f172a;
  font-size: 16px;
  line-height: 24px;
  font-weight: 600;
}

.stock-record-query {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stock-record-query__grid {
  display: grid;
  gap: 14px;
}

.stock-record-query__grid--primary {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.stock-record-query__grid--advanced {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.stock-record-query__footer,
.stock-record-toolbar,
.stock-record-page__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.stock-record-query__actions,
.stock-record-toolbar__actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
}

.stock-record-query__actions :deep(.el-button),
.stock-record-toolbar__actions :deep(.el-button) {
  flex: 0 0 auto;
  min-width: 88px;
  white-space: nowrap;
}

.stock-record-summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.stock-record-summary-card {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: stretch;
  min-width: 0;
  overflow: hidden;
  border: 1px solid #e2e8f0;
  border-radius: 20px;
  background: #fff;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.05);
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease;
}

.stock-record-summary-card--blue {
  border-color: #bfdbfe;
  background: linear-gradient(90deg, #eff6ff 0%, #eff6ff 82%, #93c5fd 82%, #3b82f6 100%);
}

.stock-record-summary-card--emerald {
  border-color: #bbf7d0;
  background: linear-gradient(90deg, #ecfdf5 0%, #ecfdf5 82%, #86efac 82%, #22c55e 100%);
}

.stock-record-summary-card--amber {
  border-color: #fde68a;
  background: linear-gradient(90deg, #fffbeb 0%, #fffbeb 82%, #fde68a 82%, #f59e0b 100%);
}

.stock-record-summary-card--rose {
  border-color: #fecdd3;
  background: linear-gradient(90deg, #fff1f2 0%, #fff1f2 82%, #fda4af 82%, #f43f5e 100%);
}

.stock-record-summary-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.08);
}

.stock-record-summary-card__main {
  display: flex;
  min-width: 0;
  flex-direction: column;
  justify-content: center;
  gap: 6px;
  padding: 16px 18px;
}

.stock-record-summary-card__label {
  color: #334155;
  font-size: 13px;
  line-height: 20px;
  font-weight: 500;
}

.stock-record-summary-card__value {
  color: #0f172a;
  font-size: 30px;
  line-height: 34px;
  font-weight: 800;
  font-variant-numeric: tabular-nums;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace;
}

.stock-record-summary-card__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 68px;
  color: #fff;
  font-size: 20px;
  flex-shrink: 0;
}

.stock-record-summary-card__icon--blue {
  background: linear-gradient(180deg, #93c5fd, #3b82f6);
}

.stock-record-summary-card__icon--emerald {
  background: linear-gradient(180deg, #86efac, #16a34a);
}

.stock-record-summary-card__icon--amber {
  background: linear-gradient(180deg, #fcd34d, #ea580c);
}

.stock-record-summary-card__icon--rose {
  background: linear-gradient(180deg, #fda4af, #e11d48);
}

@media (max-width: 1280px) {
  .stock-record-summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .stock-record-summary-grid {
    grid-template-columns: 1fr;
  }

  .stock-record-summary-card {
    grid-template-columns: minmax(0, 1fr) 60px;
  }

  .stock-record-summary-card__icon {
    width: 60px;
  }
}

.stock-record-page__table-shell {
  overflow-x: auto;
}

.stock-record-ledger {
  width: 100%;
}

.ledger-record,
.ledger-product {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.ledger-record__no,
.ledger-product__name {
  color: #0f172a;
  font-weight: 600;
}

.ledger-record__meta {
  color: #64748b;
  font-size: 12px;
  line-height: 18px;
}

.ledger-product__chips {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.ledger-product__chip {
  display: inline-flex;
  align-items: center;
  border: 1px solid #e2e8f0;
  border-radius: 999px;
  padding: 2px 10px;
  background: #f8fafc;
  color: #475569;
  font-size: 12px;
  line-height: 18px;
  white-space: nowrap;
}

.ledger-warehouse {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.ledger-warehouse__name {
  color: #0f172a;
  font-weight: 600;
}

.ledger-warehouse__chip,
.record-drawer-summary__chip,
.record-detail-item__sub {
  display: inline-flex;
  align-items: center;
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

.ledger-warehouse__chip.bg-blue-50,
.record-drawer-summary__chip.bg-blue-50,
.record-detail-item__sub.bg-blue-50 {
  border-color: #bfdbfe;
  background: #eff6ff;
  color: #2563eb;
}

.ledger-warehouse__chip.bg-emerald-50,
.record-drawer-summary__chip.bg-emerald-50,
.record-detail-item__sub.bg-emerald-50 {
  border-color: #bbf7d0;
  background: #ecfdf5;
  color: #059669;
}

.ledger-warehouse__chip.bg-amber-50,
.record-drawer-summary__chip.bg-amber-50,
.record-detail-item__sub.bg-amber-50 {
  border-color: #fde68a;
  background: #fffbeb;
  color: #d97706;
}

.ledger-warehouse__chip.bg-rose-50,
.record-drawer-summary__chip.bg-rose-50,
.record-detail-item__sub.bg-rose-50 {
  border-color: #fecdd3;
  background: #fff1f2;
  color: #e11d48;
}

.ledger-warehouse__chip.bg-slate-100,
.record-drawer-summary__chip.bg-slate-100,
.record-detail-item__sub.bg-slate-100 {
  border-color: #e2e8f0;
  background: #f8fafc;
  color: #475569;
}

.record-drawer-summary__eyebrow :deep(.inline-flex) {
  max-width: 100%;
}

.ledger-number {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace;
  font-weight: 600;
}

.ledger-number--negative {
  color: #be123c;
}

.ledger-number--positive {
  color: #059669;
}

.stock-record-page__record-count {
  color: #64748b;
  font-size: 13px;
}

.stock-record-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 10px;
  min-height: 180px;
  color: #64748b;
}

.stock-record-empty__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  border-radius: 999px;
  background: #f1f5f9;
  color: #94a3b8;
  font-size: 20px;
}

.stock-record-empty__title {
  color: #334155;
  font-size: 14px;
}

.record-drawer-shell {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #f8fafc;
}

.record-drawer-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 20px;
  border-bottom: 1px solid #e2e8f0;
  background: #fff;
}

.record-drawer-head__title {
  margin: 0;
  color: #0f172a;
  font-size: 16px;
  line-height: 24px;
  font-weight: 600;
}

.record-drawer-head__close {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  background: #fff;
  color: #475569;
}

.record-drawer-body {
  overflow: auto;
  padding: 20px;
}

.record-drawer-summary {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 280px;
  gap: 16px;
  margin-bottom: 16px;
}

.record-drawer-summary__context {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 18px 20px;
  border: 1px solid #1e293b;
  border-radius: 18px;
  background: #0f172a;
  box-shadow: 0 18px 42px rgba(15, 23, 42, 0.18);
}

.record-drawer-summary__eyebrow {
  color: #93c5fd;
  font-size: 12px;
}

.record-drawer-summary__title {
  color: #fff;
  font-size: 22px;
  line-height: 30px;
  font-weight: 700;
}

.record-drawer-summary__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 12px;
  color: #cbd5e1;
  font-size: 13px;
}

.record-drawer-summary__mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace;
}

.record-drawer-summary__chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.record-drawer-summary__metrics {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.record-drawer-summary__metric {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 16px 18px;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  background: #fff;
}

.record-drawer-summary__metric-label {
  color: #64748b;
  font-size: 12px;
}

.record-drawer-summary__metric-value {
  color: #0f172a;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace;
  font-size: 28px;
  line-height: 1;
  font-weight: 700;
}

.record-drawer-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.record-detail-card {
  border: 1px solid #e2e8f0;
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
}

.record-detail-card__header {
  padding: 14px 18px;
  border-bottom: 1px solid #f1f5f9;
  background: #f8fafc;
}

.record-detail-card__title {
  margin: 0;
  color: #0f172a;
  font-size: 14px;
  line-height: 22px;
  font-weight: 600;
}

.record-detail-card__body {
  padding: 16px 18px;
}

.record-detail-card__body--grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 18px;
}

.record-detail-item {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 10px 0;
  border-bottom: 1px solid #f8fafc;
}

.record-detail-item:last-child {
  border-bottom: 0;
}

.record-detail-item__label {
  color: #64748b;
  font-size: 13px;
}

.record-detail-item__value {
  color: #0f172a;
  font-size: 13px;
  font-weight: 500;
  text-align: right;
}

.record-detail-item__value--stacked {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.record-detail-item__sub {
  margin-left: auto;
}

.record-detail-item__value--mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace;
}

.stock-record-query-collapse-enter-active,
.stock-record-query-collapse-leave-active {
  transition: all 0.2s ease;
}

.stock-record-query-collapse-enter-from,
.stock-record-query-collapse-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

@media (max-width: 1439px) {
  .stock-record-query__grid--primary,
  .stock-record-summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .record-drawer-summary {
    grid-template-columns: minmax(0, 1fr);
  }
}

@media (max-width: 1279px) {
  .stock-record-query__grid--primary,
  .stock-record-query__grid--advanced,
  .stock-record-summary-grid,
  .record-drawer-grid,
  .record-drawer-summary__metrics,
  .record-drawer-summary__context,
  .record-detail-card__body--grid {
    grid-template-columns: minmax(0, 1fr);
  }
}

@media (max-width: 959px) {
  .stock-record-query__footer,
  .stock-record-toolbar,
  .stock-record-page__footer {
    flex-direction: column;
    align-items: stretch;
  }

  .stock-record-query__actions,
  .stock-record-toolbar__actions {
    width: 100%;
  }

  .stock-record-summary-card {
    grid-template-columns: minmax(0, 1fr);
  }

  .stock-record-summary-card__icon {
    width: 100%;
    min-height: 44px;
  }

  .record-detail-item__value {
    text-align: left;
  }
}

@media (max-width: 1279px) {
  .stock-record-query__actions,
  .stock-record-toolbar__actions {
    width: 100%;
  }

  .stock-record-query__actions :deep(.el-button),
  .stock-record-toolbar__actions :deep(.el-button) {
    flex: 1 1 0;
    min-width: 0;
  }
}
</style>

<style lang="scss">
.stock-record-drawer__mask {
  backdrop-filter: blur(8px);
  background: rgba(15, 23, 42, 0.34);
}

.stock-record-drawer__mask .el-drawer__body {
  padding: 0;
}
</style>
