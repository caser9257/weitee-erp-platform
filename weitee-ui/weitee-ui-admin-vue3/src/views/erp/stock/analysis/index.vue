<template>
  <div class="stock-analysis-workspace">
    <ContentWrap
      class="stock-analysis-page__title-card"
      :body-style="{ padding: '22px 24px' }"
      :class="getToneCardClass('blue')"
    >
      <div class="stock-analysis-page__title-bar">
        <div class="stock-analysis-page__title-group">
          <div class="stock-analysis-page__eyebrow">SCM 分析看板</div>
          <div class="stock-analysis-page__title">库存分析</div>
          <div class="stock-analysis-page__hero-meta">
            <span class="stock-analysis-page__hero-pill">库存结构</span>
            <span class="stock-analysis-page__hero-pill">仓库分布</span>
            <span class="stock-analysis-page__hero-pill">库存流水</span>
          </div>
        </div>

        <div class="stock-analysis-page__hero-actions">
          <el-button
            class="stock-action-btn stock-action-btn--primary"
            type="primary"
            :loading="isNavigatingStock"
            :disabled="isAnyNavigationPending"
            @click="handleNavigate('stock')"
          >
            <Icon icon="ep:goods" class="mr-5px" /> 库存台账
          </el-button>
          <el-button
            class="stock-action-btn stock-action-btn--secondary"
            :loading="isNavigatingRecord"
            :disabled="isAnyNavigationPending"
            @click="handleNavigate('record')"
          >
            <Icon icon="ep:document" class="mr-5px" /> 库存明细
          </el-button>
        </div>
      </div>
    </ContentWrap>

    <ContentWrap
      class="stock-analysis-page__filter-card"
      :body-style="{ padding: '24px' }"
      :class="getToneCardClass('slate')"
    >
      <div class="stock-analysis-page__section-head">
        <div class="stock-analysis-page__section-title">分析条件</div>
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
          <el-button
            link
            type="primary"
            class="stock-inline-action stock-action-btn"
            :disabled="supportLoading"
            @click="retrySupportOptions"
          >
            重试加载
          </el-button>
        </template>
      </el-alert>

      <el-form :model="queryParams" label-position="top" class="stock-analysis-query">
        <div class="stock-analysis-query__grid">
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

          <el-form-item label="时间范围" prop="createTime">
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

        <div class="stock-analysis-query__footer">
          <div class="stock-analysis-query__meta">
            <span class="stock-analysis-query__meta-item">按产品、仓库、业务类型筛选当前库存与流水</span>
          </div>
          <div class="stock-analysis-query__actions">
            <el-button class="stock-action-btn" :disabled="!canReset" @click="resetQuery">
              <Icon icon="ep:refresh" class="mr-5px" /> 重置
            </el-button>
            <el-button
              class="stock-action-btn stock-action-btn--primary"
              type="primary"
              :loading="isAnyLoading"
              :disabled="!canQuery"
              @click="handleQuery"
            >
              <Icon icon="ep:search" class="mr-5px" /> 查询
            </el-button>
          </div>
        </div>
      </el-form>
    </ContentWrap>

    <div class="stock-analysis-summary-grid">
      <article
        v-for="(card, index) in summaryCards"
        :key="card.label"
        class="stock-analysis-summary-card"
        :class="resolveSummaryCardClass(index)"
      >
        <div class="stock-analysis-summary-card__head">
          <div class="stock-analysis-summary-card__icon">
            <Icon :icon="card.icon" />
          </div>
          <div class="stock-analysis-summary-card__label">{{ card.label }}</div>
        </div>
        <div class="stock-analysis-summary-card__body">
          <div class="stock-analysis-summary-card__value-wrap">
            <div class="stock-analysis-summary-card__value">{{ card.value }}</div>
            <div v-if="card.unit" class="stock-analysis-summary-card__unit">{{ card.unit }}</div>
          </div>
          <div class="stock-analysis-summary-card__hint">{{ card.hint }}</div>
        </div>
      </article>
    </div>

    <div class="stock-analysis-board-grid">
      <ContentWrap
        class="stock-analysis-page__panel"
        :body-style="{ padding: '24px' }"
        :class="getToneCardClass('amber')"
      >
        <div class="stock-analysis-page__section-head">
          <div class="stock-analysis-page__section-title">库存结构</div>
          <div class="stock-analysis-page__section-meta">
            <span class="stock-analysis-page__section-chip">当前页 {{ formatCount(stockRows.length) }} 条</span>
            <span class="stock-analysis-page__section-chip">仓库 {{ formatCount(currentWarehouseTotal) }} 个</span>
          </div>
        </div>
        <el-alert
          v-if="stockLoadFailed"
          :title="stockErrorMessage"
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
              :disabled="stockLoading"
              @click="retryStockList"
            >
              重试加载
            </el-button>
          </template>
        </el-alert>

        <div class="stock-analysis-page__table-shell">
          <el-table
            v-loading="stockLoading"
            :data="stockRows"
            :stripe="true"
            :show-overflow-tooltip="true"
            class="stock-analysis-ledger"
          >
            <template #empty>
              <div v-if="stockLoadFailed" class="stock-analysis-empty stock-analysis-empty--error">
                <div class="stock-analysis-empty__icon">
                  <Icon icon="ep:warning-filled" />
                </div>
                <div class="stock-analysis-empty__title">库存结构加载失败</div>
              </div>
              <div v-else class="stock-analysis-empty">
                <div class="stock-analysis-empty__icon">
                  <Icon icon="ep:goods" />
                </div>
                <div class="stock-analysis-empty__title">暂无库存结构数据</div>
              </div>
            </template>

            <el-table-column label="产品信息" min-width="300">
              <template #default="{ row }">
                <div class="stock-analysis-product">
                  <div class="stock-analysis-product__name">{{ row.productName || '-' }}</div>
                  <div class="stock-analysis-product__meta">
                    <span>{{ row.categoryName || '-' }}</span>
                    <span>{{ row.unitName || '-' }}</span>
                  </div>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="所属仓库" min-width="170">
              <template #default="{ row }">
                <span
                  class="stock-analysis-warehouse-tag"
                  :class="resolveWarehouseTagClass(row.warehouseName)"
                >
                  {{ row.warehouseName || '-' }}
                </span>
              </template>
            </el-table-column>

            <el-table-column label="库存数量" align="right" min-width="140">
              <template #default="{ row }">
                <span class="stock-analysis-number">{{ formatCount(row.count) }}</span>
              </template>
            </el-table-column>

            <el-table-column label="库存占比" min-width="220">
              <template #default="{ row }">
                <div class="stock-analysis-share">
                  <div class="stock-analysis-share__meta">
                    <strong>{{ formatShare(row.count) }}</strong>
                    <span>当前页库存占比</span>
                  </div>
                  <div class="stock-analysis-share__bar">
                    <div
                      class="stock-analysis-share__bar-inner"
                      :style="{ width: `${resolveSharePercent(row.count)}%` }"
                    ></div>
                  </div>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div class="stock-analysis-page__footer">
          <div class="stock-analysis-page__record-count">共 {{ stockTotal }} 条记录</div>
          <Pagination
            :total="stockTotal"
            v-model:page="queryParams.stockPageNo"
            v-model:limit="queryParams.stockPageSize"
            @pagination="handleStockPageChange"
          />
        </div>
      </ContentWrap>

      <ContentWrap
        class="stock-analysis-page__distribution-card"
        :body-style="{ padding: '24px' }"
        :class="getToneCardClass('rose')"
      >
        <div class="stock-analysis-page__section-head">
          <div class="stock-analysis-page__section-title">仓库分布</div>
          <div class="stock-analysis-page__section-meta">
            <span class="stock-analysis-page__section-chip">当前查询结果概览</span>
          </div>
        </div>

        <div class="stock-analysis-distribution">
          <div class="stock-analysis-distribution__chart-shell">
            <Echart :options="warehouseChartOptions" height="240px" />
            <div class="stock-analysis-distribution__chart-center">
              <div class="stock-analysis-distribution__chart-center-label">仓库数</div>
              <div class="stock-analysis-distribution__chart-center-value">
                {{ formatCount(currentWarehouseTotal) }}
              </div>
            </div>
          </div>

          <div class="stock-analysis-distribution__legend">
            <div
              v-for="item in topWarehouseSummary"
              :key="item.warehouseId"
              class="stock-analysis-distribution__legend-item"
            >
              <div class="stock-analysis-distribution__legend-main">
                <span class="stock-analysis-distribution__legend-dot" :style="{ background: item.color }"></span>
                <span class="stock-analysis-distribution__legend-name" :title="item.warehouseName">
                  {{ item.warehouseName }}
                </span>
              </div>
              <div class="stock-analysis-distribution__legend-metrics">
                <span>{{ formatCount(item.totalCount) }}</span>
                <span>{{ formatWarehouseShare(item.totalCount) }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="stock-analysis-page__table-shell">
          <el-table
            :data="warehouseSummaryRows"
            :stripe="true"
            :show-overflow-tooltip="true"
            class="stock-analysis-ledger stock-analysis-ledger--distribution"
          >
            <template #empty>
              <div class="stock-analysis-empty">
                <div class="stock-analysis-empty__icon">
                  <Icon icon="ep:house" />
                </div>
                <div class="stock-analysis-empty__title">暂无仓库分布数据</div>
              </div>
            </template>

            <el-table-column label="仓库" min-width="180">
              <template #default="{ row }">
                <span class="stock-analysis-warehouse-tag" :class="resolveWarehouseTagClass(row.warehouseName)">
                  {{ row.warehouseName }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="SKU数" align="right" min-width="100">
              <template #default="{ row }">
                <span class="stock-analysis-number">{{ formatCount(row.skuCount) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="库存数量" align="right" min-width="120">
              <template #default="{ row }">
                <span class="stock-analysis-number">{{ formatCount(row.totalCount) }}</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </ContentWrap>
    </div>

    <ContentWrap
      class="stock-analysis-page__flow-card"
      :body-style="{ padding: '24px' }"
      :class="getToneCardClass('slate')"
    >
      <div class="stock-analysis-page__section-head">
        <div class="stock-analysis-page__section-title">库存流水</div>
        <div class="stock-analysis-page__section-meta">
          <span class="stock-analysis-page__section-chip">共 {{ formatCount(recordTotal) }} 条</span>
        </div>
      </div>

      <el-alert
        v-if="recordLoadFailed"
        :title="recordErrorMessage"
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
            :disabled="recordLoading"
            @click="retryRecordList"
          >
            重试加载
          </el-button>
        </template>
      </el-alert>

      <div class="stock-analysis-page__table-shell">
        <el-table
          v-loading="recordLoading"
          :data="recordRows"
          :stripe="true"
          :show-overflow-tooltip="true"
          class="stock-analysis-ledger stock-analysis-ledger--flow"
        >
          <template #empty>
            <div v-if="recordLoadFailed" class="stock-analysis-empty stock-analysis-empty--error">
              <div class="stock-analysis-empty__icon">
                <Icon icon="ep:warning-filled" />
              </div>
              <div class="stock-analysis-empty__title">库存流水加载失败</div>
            </div>
            <div v-else class="stock-analysis-empty">
              <div class="stock-analysis-empty__icon">
                <Icon icon="ep:document" />
              </div>
              <div class="stock-analysis-empty__title">暂无库存流水</div>
            </div>
          </template>

          <el-table-column label="流水信息" min-width="260">
            <template #default="{ row }">
              <div class="stock-analysis-record">
                <div class="stock-analysis-record__no">{{ row.bizNo || '-' }}</div>
                <div class="stock-analysis-record__meta">
                  <span>{{ formatDateValue(row.createTime) }}</span>
                  <span>{{ row.creatorName || '-' }}</span>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="产品摘要" min-width="240">
            <template #default="{ row }">
              <div class="stock-analysis-product">
                <div class="stock-analysis-product__name">{{ row.productName || '-' }}</div>
                <div class="stock-analysis-product__meta">
                  <span>{{ row.categoryName || '-' }}</span>
                  <span>{{ row.unitName || '-' }}</span>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="所属仓库" min-width="150">
            <template #default="{ row }">
              <span
                class="stock-analysis-warehouse-tag"
                :class="resolveWarehouseTagClass(row.warehouseName)"
              >
                {{ row.warehouseName || '-' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="业务类型" min-width="140" align="center">
            <template #default="{ row }">
              <span :class="resolveBizToneClass(row.bizType)">
                {{ resolveBizLabel(row.bizType) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="数量" min-width="130" align="right">
            <template #default="{ row }">
              <span class="stock-analysis-number" :class="resolveCountTextClass(row.count)">
                {{ formatSignedCount(row.count) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="库存量" min-width="130" align="right">
            <template #default="{ row }">
              <span class="stock-analysis-number">{{ formatCount(row.totalCount) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="stock-analysis-page__footer">
        <div class="stock-analysis-page__record-count">共 {{ recordTotal }} 条记录</div>
        <Pagination
          :total="recordTotal"
          v-model:page="queryParams.recordPageNo"
          v-model:limit="queryParams.recordPageSize"
          @pagination="handleRecordPageChange"
        />
      </div>
    </ContentWrap>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import type { EChartsOption } from 'echarts'
import { Echart } from '@/components/Echart'
import { getIntDictOptions, DICT_TYPE } from '@/utils/dict'
import { formatDate } from '@/utils/formatTime'
import { ProductApi, ProductVO } from '@/api/erp/product/product'
import { WarehouseApi, WarehouseVO } from '@/api/erp/stock/warehouse'
import { StockApi, StockVO } from '@/api/erp/stock/stock'
import { StockRecordApi, StockRecordVO } from '@/api/erp/stock/record'
import {
  getToneCardClass,
  resolveBizToneClass,
  resolveDictLabel,
  resolveSummaryCardClass,
  resolveWarehouseTone
} from '../shared/stockTone'

defineOptions({ name: 'ErpStockAnalysis' })

type AnalysisQueryForm = {
  productId?: number
  warehouseId?: number
  bizType?: number
  createTime: string[]
  stockPageNo: number
  stockPageSize: number
  recordPageNo: number
  recordPageSize: number
}

type StockRow = StockVO & {
  productName?: string
  categoryName?: string
  unitName?: string
  warehouseName?: string
}

type StockRecordRow = StockRecordVO & {
  productName?: string
  categoryName?: string
  unitName?: string
  warehouseName?: string
  creatorName?: string
  createTime?: string | Date
}

type WarehouseSummaryRow = {
  warehouseId: number
  warehouseName: string
  skuCount: number
  totalCount: number
  color: string
}

const WAREHOUSE_CHART_COLORS = ['#2563eb', '#38bdf8', '#14b8a6', '#f59e0b', '#ef4444']

const router = useRouter()
const message = useMessage()

const createDefaultQueryForm = (): AnalysisQueryForm => ({
  productId: undefined,
  warehouseId: undefined,
  bizType: undefined,
  createTime: [],
  stockPageNo: 1,
  stockPageSize: 10,
  recordPageNo: 1,
  recordPageSize: 10
})

const queryParams = reactive<AnalysisQueryForm>(createDefaultQueryForm())

const supportLoading = ref(true)
const supportLoadFailed = ref(false)
const supportErrorMessage = ref('筛选条件加载失败，请重试')
const productList = ref<ProductVO[]>([])
const warehouseList = ref<WarehouseVO[]>([])

const stockLoading = ref(true)
const stockLoadFailed = ref(false)
const stockErrorMessage = ref('库存结构加载失败，请重试')
const stockTotal = ref(0)
const stockRows = ref<StockRow[]>([])

const recordLoading = ref(true)
const recordLoadFailed = ref(false)
const recordErrorMessage = ref('库存流水加载失败，请重试')
const recordTotal = ref(0)
const recordRows = ref<StockRecordRow[]>([])

const navigatingAction = ref<'' | 'stock' | 'record'>('')

const isAnyNavigationPending = computed(() => !!navigatingAction.value)
const isNavigatingStock = computed(() => navigatingAction.value === 'stock')
const isNavigatingRecord = computed(() => navigatingAction.value === 'record')
const isAnyLoading = computed(
  () => supportLoading.value || stockLoading.value || recordLoading.value || isAnyNavigationPending.value
)
const canQuery = computed(() => !isAnyLoading.value)
const canReset = computed(() => !isAnyLoading.value)
const resolveBizLabel = (value?: number | string | boolean | null) =>
  resolveDictLabel(getIntDictOptions(DICT_TYPE.ERP_STOCK_RECORD_BIZ_TYPE), value)

const currentStockTotal = computed(() => {
  return stockRows.value.reduce((total, row) => total + Number(row.count || 0), 0)
})

const currentWarehouseTotal = computed(() => {
  return new Set(stockRows.value.map((item) => item.warehouseId)).size
})

const warehouseSummaryRows = computed<WarehouseSummaryRow[]>(() => {
  const warehouseMap = new Map<number, WarehouseSummaryRow>()

  stockRows.value.forEach((row) => {
    const current = warehouseMap.get(row.warehouseId)
    const count = Number(row.count || 0)
    if (current) {
      current.skuCount += 1
      current.totalCount += count
      return
    }
    warehouseMap.set(row.warehouseId, {
      warehouseId: row.warehouseId,
      warehouseName: row.warehouseName || '-',
      skuCount: 1,
      totalCount: count,
      color: WAREHOUSE_CHART_COLORS[warehouseMap.size % WAREHOUSE_CHART_COLORS.length]
    })
  })

  return [...warehouseMap.values()].sort((left, right) => right.totalCount - left.totalCount)
})

const warehouseSummaryTotal = computed(() =>
  warehouseSummaryRows.value.reduce((total, row) => total + Number(row.totalCount || 0), 0)
)

const topWarehouseSummary = computed(() => warehouseSummaryRows.value.slice(0, 5))

const summaryCards = computed(() => [
  {
    label: '库存记录数',
    value: formatCount(stockTotal.value),
    unit: '条',
    hint: '当前筛选下库存结构总记录',
    icon: 'ep:document'
  },
  {
    label: '当前库存总量',
    value: formatCount(currentStockTotal.value),
    unit: '件 / 根',
    hint: '按当前库存结构页汇总',
    icon: 'ep:box'
  },
  {
    label: '当前页仓库数',
    value: formatCount(currentWarehouseTotal.value),
    unit: '个',
    hint: '本页出现的仓库数量',
    icon: 'ep:location'
  },
  {
    label: '库存流水数',
    value: formatCount(recordTotal.value),
    unit: '次',
    hint: '当前筛选下库存变动记录',
    icon: 'ep:switch'
  }
])

const warehouseChartOptions = computed<EChartsOption>(() => {
  const chartData = topWarehouseSummary.value.map((item) => ({
    name: item.warehouseName,
    value: Number(item.totalCount || 0),
    itemStyle: {
      color: item.color
    }
  }))

  return {
    tooltip: {
      trigger: 'item',
      formatter: ({ name, value }: { name?: string; value?: number }) =>
        `${name || '-'}<br/>库存数量：${formatCount(value)}<br/>占比：${formatWarehouseShare(value)}`
    },
    legend: {
      show: false
    },
    series: [
      {
        type: 'pie',
        radius: ['64%', '82%'],
        center: ['50%', '50%'],
        avoidLabelOverlap: true,
        label: {
          show: false
        },
        labelLine: {
          show: false
        },
        emphasis: {
          scale: true,
          scaleSize: 4
        },
        data: chartData.length
          ? chartData
          : [
              {
                value: 1,
                name: '暂无数据',
                itemStyle: {
                  color: '#e2e8f0'
                }
              }
            ]
      }
    ]
  }
})

const formatCount = (value?: number | string | null) => {
  const numberValue = Number(value || 0)
  if (Number.isInteger(numberValue)) {
    return numberValue.toLocaleString('zh-CN')
  }
  return numberValue.toFixed(3).replace(/\.?0+$/, '')
}

const formatSignedCount = (value?: number | string | null) => {
  const normalized = Number(value || 0)
  const formatted = formatCount(normalized)
  if (normalized > 0) {
    return `+${formatted}`
  }
  return formatted
}

const formatShare = (value?: number | string | null) => `${resolveSharePercent(value)}%`

const formatWarehouseShare = (value?: number | string | null) => `${resolveWarehouseSharePercent(value)}%`

const resolveSharePercent = (value?: number | string | null) => {
  const stockTotalValue = currentStockTotal.value
  if (!stockTotalValue) {
    return 0
  }
  const percent = (Number(value || 0) / stockTotalValue) * 100
  return Number(percent.toFixed(1))
}

const resolveWarehouseSharePercent = (value?: number | string | null) => {
  const warehouseTotalValue = warehouseSummaryTotal.value
  if (!warehouseTotalValue) {
    return 0
  }
  const percent = (Number(value || 0) / warehouseTotalValue) * 100
  return Number(percent.toFixed(1))
}

const formatDateValue = (value?: string | Date) => (value ? formatDate(value) : '-')

const resolveCountTextClass = (value?: number | string | null) => {
  const normalized = Number(value || 0)
  if (normalized < 0) {
    return 'text-rose-600'
  }
  if (normalized > 0) {
    return 'text-emerald-600'
  }
  return 'text-slate-500'
}

const resolveWarehouseTagClass = (name?: string | null) => {
  const tone = resolveWarehouseTone(name)
  return `stock-analysis-warehouse-tag--${tone}`
}

const buildStockQuery = () => ({
  productId: queryParams.productId,
  warehouseId: queryParams.warehouseId,
  pageNo: queryParams.stockPageNo,
  pageSize: queryParams.stockPageSize
})

const buildRecordQuery = () => ({
  productId: queryParams.productId,
  warehouseId: queryParams.warehouseId,
  bizType: queryParams.bizType,
  createTime: queryParams.createTime?.length ? queryParams.createTime : undefined,
  pageNo: queryParams.recordPageNo,
  pageSize: queryParams.recordPageSize
})

const loadSupportOptions = async () => {
  supportLoading.value = true
  supportLoadFailed.value = false
  supportErrorMessage.value = '筛选条件加载失败，请重试'
  try {
    const [productsResult, warehousesResult] = await Promise.allSettled([
      ProductApi.getProductSimpleList(),
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
      supportErrorMessage.value = '筛选条件加载失败，请重试'
    }
  } finally {
    supportLoading.value = false
  }
}

const loadStockList = async () => {
  stockLoading.value = true
  stockLoadFailed.value = false
  stockErrorMessage.value = '库存结构加载失败，请重试'
  try {
    const data = await StockApi.getStockPage(buildStockQuery())
    stockRows.value = data.list || []
    stockTotal.value = data.total || 0
  } catch {
    stockRows.value = []
    stockTotal.value = 0
    stockLoadFailed.value = true
  } finally {
    stockLoading.value = false
  }
}

const loadRecordList = async () => {
  recordLoading.value = true
  recordLoadFailed.value = false
  recordErrorMessage.value = '库存流水加载失败，请重试'
  try {
    const data = await StockRecordApi.getStockRecordPage(buildRecordQuery())
    recordRows.value = data.list || []
    recordTotal.value = data.total || 0
  } catch {
    recordRows.value = []
    recordTotal.value = 0
    recordLoadFailed.value = true
  } finally {
    recordLoading.value = false
  }
}

const reloadAnalysis = async () => {
  await Promise.allSettled([loadStockList(), loadRecordList()])
}

const handleQuery = async () => {
  if (!canQuery.value) {
    return
  }
  queryParams.stockPageNo = 1
  queryParams.recordPageNo = 1
  await reloadAnalysis()
}

const resetQuery = async () => {
  if (!canReset.value) {
    return
  }
  Object.assign(queryParams, createDefaultQueryForm())
  await reloadAnalysis()
}

const retrySupportOptions = async () => {
  await loadSupportOptions()
}

const retryStockList = async () => {
  await loadStockList()
}

const retryRecordList = async () => {
  await loadRecordList()
}

const handleStockPageChange = async () => {
  await loadStockList()
}

const handleRecordPageChange = async () => {
  await loadRecordList()
}

const handleNavigate = async (action: 'stock' | 'record') => {
  if (isAnyNavigationPending.value) {
    return
  }
  navigatingAction.value = action
  try {
    await router.push(action === 'stock' ? '/scm/stock' : '/scm/stock-record')
  } catch {
    message.error('页面跳转失败，请重试')
  } finally {
    navigatingAction.value = ''
  }
}

onMounted(async () => {
  await Promise.allSettled([loadSupportOptions(), reloadAnalysis()])
})
</script>

<style scoped>
.stock-analysis-workspace {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 14px;
}

.stock-analysis-page__title-card,
.stock-analysis-page__filter-card,
.stock-analysis-page__panel,
.stock-analysis-page__distribution-card,
.stock-analysis-page__flow-card {
  min-width: 0;
}

.stock-analysis-page__title-bar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.stock-analysis-page__title-group {
  display: flex;
  flex: 1;
  min-width: 0;
  flex-direction: column;
  gap: 8px;
}

.stock-analysis-page__eyebrow {
  color: #2563eb;
  font-size: 12px;
  line-height: 18px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.stock-analysis-page__title,
.stock-analysis-page__section-title,
.stock-analysis-summary-card__value,
.stock-analysis-product__name,
.stock-analysis-record__no,
.stock-analysis-number,
.stock-analysis-distribution__chart-center-value {
  color: #0f172a;
  font-weight: 700;
}

.stock-analysis-page__title {
  font-family: 'IBM Plex Sans', Inter, sans-serif;
  font-size: 22px;
  line-height: 30px;
  font-weight: 700;
}

.stock-analysis-page__hero-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.stock-analysis-page__hero-pill,
.stock-analysis-page__section-chip {
  display: inline-flex;
  align-items: center;
  padding: 3px 10px;
  border: 1px solid #dbeafe;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.8);
  color: #2563eb;
  font-size: 12px;
  line-height: 18px;
  font-weight: 600;
}

.stock-analysis-page__section-chip {
  border-color: #e2e8f0;
  color: #475569;
}

.stock-analysis-page__hero-actions,
.stock-analysis-query__footer,
.stock-analysis-page__section-head,
.stock-analysis-page__footer,
.stock-analysis-page__section-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.stock-action-btn {
  flex: 0 0 auto;
  min-width: 96px;
  min-height: 38px;
  padding-inline: 14px;
  white-space: nowrap;
}

.stock-action-btn--primary {
  box-shadow: 0 8px 18px rgba(37, 99, 235, 0.16);
}

.stock-action-btn--secondary {
  border-color: #cbd5e1;
  color: #334155;
  background: rgba(255, 255, 255, 0.92);
}

.stock-inline-action {
  flex: 0 0 auto;
  white-space: nowrap;
}

.stock-analysis-page__section-head {
  justify-content: space-between;
  margin-bottom: 12px;
}

.stock-analysis-page__section-title {
  font-family: 'IBM Plex Sans', Inter, sans-serif;
  font-size: 18px;
  line-height: 28px;
  font-weight: 600;
}

.stock-analysis-query {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stock-analysis-query__grid {
  display: grid;
  gap: 14px;
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.stock-analysis-query__meta {
  color: #64748b;
  font-size: 12px;
  line-height: 20px;
}

.stock-analysis-query__meta-item {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  border-radius: 999px;
  background: #f8fafc;
}

.stock-analysis-query__actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.stock-analysis-summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.stock-analysis-summary-card {
  display: flex;
  min-height: 122px;
  flex-direction: column;
  justify-content: space-between;
  gap: 14px;
  padding: 18px;
}

.stock-analysis-summary-card__head,
.stock-analysis-summary-card__body {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.stock-analysis-summary-card__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.82);
  color: #2563eb;
  font-size: 20px;
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.55);
}

.stock-analysis-summary-card__label,
.stock-analysis-summary-card__hint,
.stock-analysis-share__meta span,
.stock-analysis-distribution__chart-center-label {
  color: #64748b;
  font-size: 12px;
  line-height: 20px;
}

.stock-analysis-summary-card__value-wrap {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  flex-wrap: wrap;
}

.stock-analysis-summary-card__value {
  font-size: 28px;
  line-height: 34px;
  font-variant-numeric: tabular-nums;
}

.stock-analysis-summary-card__unit {
  color: #334155;
  font-size: 13px;
  line-height: 22px;
  font-weight: 600;
}

.stock-analysis-board-grid {
  display: grid;
  gap: 14px;
  grid-template-columns: minmax(0, 1.65fr) minmax(320px, 0.95fr);
  align-items: start;
}

.stock-analysis-page__flow-card {
  margin-top: 0;
}

.stock-analysis-page__table-shell {
  overflow-x: auto;
}

.stock-analysis-ledger {
  min-width: 960px;
}

.stock-analysis-ledger--distribution {
  min-width: 100%;
}

.stock-analysis-ledger :deep(.el-table) {
  --el-table-border-color: transparent;
  --el-table-header-bg-color: #f8fafc;
}

.stock-analysis-ledger :deep(.el-table td.el-table__cell),
.stock-analysis-ledger :deep(.el-table th.el-table__cell.is-leaf) {
  border-bottom: 1px solid #f1f5f9;
}

.stock-analysis-ledger :deep(.el-table__header-wrapper) {
  position: sticky;
  top: 0;
  z-index: 3;
}

.stock-analysis-product,
.stock-analysis-record {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 8px;
}

.stock-analysis-product__name {
  font-size: 14px;
  line-height: 22px;
}

.stock-analysis-record__no {
  display: inline-flex;
  width: fit-content;
  max-width: 100%;
  padding: 3px 8px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #f8fafc;
  font-size: 13px;
  line-height: 20px;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Courier New', monospace;
}

.stock-analysis-product__meta,
.stock-analysis-record__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  color: #64748b;
  font-size: 12px;
  line-height: 18px;
}

.stock-analysis-number,
.stock-analysis-distribution__legend-metrics {
  width: 100%;
  text-align: right;
  font-variant-numeric: tabular-nums;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New',
    monospace;
}

.stock-analysis-warehouse-tag {
  display: inline-flex;
  align-items: center;
  padding: 3px 10px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  background: #f8fafc;
  color: #475569;
  font-size: 12px;
  line-height: 18px;
  font-weight: 600;
}

.stock-analysis-warehouse-tag--blue {
  border-color: #bfdbfe;
  background: #eff6ff;
  color: #2563eb;
}

.stock-analysis-warehouse-tag--emerald {
  border-color: #bbf7d0;
  background: #ecfdf5;
  color: #059669;
}

.stock-analysis-warehouse-tag--amber {
  border-color: #fde68a;
  background: #fffbeb;
  color: #d97706;
}

.stock-analysis-warehouse-tag--rose {
  border-color: #fecdd3;
  background: #fff1f2;
  color: #e11d48;
}

.stock-analysis-warehouse-tag--slate {
  border-color: #e2e8f0;
  background: #f8fafc;
  color: #475569;
}

.stock-analysis-share {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.stock-analysis-share__meta {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 8px;
  flex-wrap: wrap;
}

.stock-analysis-share__meta strong {
  color: #0f172a;
  font-size: 13px;
  line-height: 20px;
}

.stock-analysis-share__bar {
  overflow: hidden;
  height: 6px;
  border-radius: 999px;
  background: #e2e8f0;
}

.stock-analysis-share__bar-inner {
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #2563eb, #38bdf8);
}

.stock-analysis-distribution {
  display: grid;
  grid-template-columns: minmax(0, 220px) minmax(0, 1fr);
  gap: 14px;
  margin-bottom: 14px;
}

.stock-analysis-distribution__chart-shell {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 240px;
}

.stock-analysis-distribution__chart-center {
  position: absolute;
  inset: 50% auto auto 50%;
  display: flex;
  min-width: 110px;
  transform: translate(-50%, -50%);
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.stock-analysis-distribution__chart-center-value {
  font-size: 28px;
  line-height: 34px;
}

.stock-analysis-distribution__legend {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 10px;
}

.stock-analysis-distribution__legend-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  background: rgba(248, 250, 252, 0.88);
}

.stock-analysis-distribution__legend-main {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 10px;
}

.stock-analysis-distribution__legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  flex-shrink: 0;
}

.stock-analysis-distribution__legend-name {
  overflow: hidden;
  color: #0f172a;
  font-size: 13px;
  line-height: 20px;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.stock-analysis-distribution__legend-metrics {
  display: flex;
  align-items: center;
  gap: 10px;
  width: auto;
  color: #475569;
  font-size: 12px;
  line-height: 18px;
}

.stock-analysis-empty {
  min-height: 192px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #64748b;
}

.stock-analysis-empty__icon {
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

.stock-analysis-empty__title {
  color: #0f172a;
  font-size: 14px;
  line-height: 22px;
  font-weight: 600;
}

.stock-analysis-empty--error .stock-analysis-empty__icon {
  background: rgba(244, 63, 94, 0.08);
  color: #e11d48;
}

.stock-analysis-page__record-count {
  color: #64748b;
  font-size: 13px;
  line-height: 20px;
}

@media (max-width: 1439px) {
  .stock-analysis-summary-grid,
  .stock-analysis-query__grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .stock-analysis-board-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 1279px) {
  .stock-analysis-page__hero-actions,
  .stock-analysis-query__actions {
    width: 100%;
  }

  .stock-analysis-page__hero-actions :deep(.el-button),
  .stock-analysis-query__actions :deep(.el-button) {
    flex: 1 1 0;
    min-width: 0;
  }

  .stock-analysis-distribution {
    grid-template-columns: 1fr;
  }

  .stock-analysis-distribution__chart-shell {
    min-height: 220px;
  }
}

@media (max-width: 959px) {
  .stock-analysis-summary-grid,
  .stock-analysis-query__grid {
    grid-template-columns: 1fr;
  }

  .stock-analysis-page__title-bar,
  .stock-analysis-query__footer {
    flex-direction: column;
    align-items: stretch;
  }

  .stock-analysis-page__hero-actions {
    width: 100%;
  }
}
</style>
