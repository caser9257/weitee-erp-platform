<template>
  <div class="cost-trend-page">
    <ContentWrap class="page-header-card">
      <div class="page-header">
        <div>
          <div class="page-header__title">产品成本趋势分析</div>
          <div class="page-header__desc">按月查看产品成本结构、趋势和对比结果</div>
        </div>
        <div class="page-header__actions">
          <el-button :loading="loading" @click="loadData">
            <Icon icon="ep:refresh" class="mr-5px" />
            刷新
          </el-button>
        </div>
      </div>
    </ContentWrap>

    <ContentWrap class="filter-card">
      <el-form :model="queryParams" label-width="88px" class="filter-form" @submit.prevent>
        <div class="filter-grid">
          <el-form-item label="产品">
            <el-select
              v-model="queryParams.productId"
              filterable
              clearable
              placeholder="请选择产品"
              class="!w-full"
              @change="handleQuery"
            >
              <el-option
                v-for="item in productList"
                :key="item.productId"
                :label="item.productName || `产品${item.productId}`"
                :value="item.productId"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="时间范围">
            <el-date-picker
              v-model="dateRange"
              type="monthrange"
              value-format="YYYY-MM"
              start-placeholder="开始月份"
              end-placeholder="结束月份"
              class="!w-full"
              @change="handleQuery"
            />
          </el-form-item>
          <el-form-item label="对比产品">
            <el-select
              v-model="queryParams.compareProductIds"
              multiple
              filterable
              clearable
              placeholder="选择对比产品（可多选）"
              class="!w-full"
              @change="handleQuery"
            >
              <el-option
                v-for="item in productList"
                :key="item.productId"
                :label="item.productName || `产品${item.productId}`"
                :value="item.productId"
              />
            </el-select>
          </el-form-item>
        </div>
        <div class="filter-actions">
          <el-button type="primary" :loading="loading" @click="handleQuery" v-hasPermi="['erp:cost-product:query']">
            <Icon icon="ep:search" class="mr-5px" />
            查询
          </el-button>
          <el-button :disabled="loading" @click="resetQuery">
            <Icon icon="ep:refresh-left" class="mr-5px" />
            重置
          </el-button>
        </div>
      </el-form>
    </ContentWrap>

    <div class="chart-grid">
      <ContentWrap class="chart-card">
        <div class="chart-header">
          <div>
            <div class="chart-header__title">成本趋势</div>
            <div class="chart-header__desc">展示所选产品在所选期间内的成本变化</div>
          </div>
          <div class="chart-header__legend">
            <span><i class="legend-dot" style="background: #3b82f6"></i> 直接材料</span>
            <span><i class="legend-dot" style="background: #10b981"></i> 直接人工</span>
            <span><i class="legend-dot" style="background: #f59e0b"></i> 制造费用</span>
            <span><i class="legend-dot" style="background: #ef4444"></i> 总成本</span>
          </div>
        </div>
        <div ref="trendChartRef" class="chart-instance"></div>
      </ContentWrap>

      <ContentWrap class="chart-card">
        <div class="chart-header">
          <div>
            <div class="chart-header__title">成本构成</div>
            <div class="chart-header__desc">展示当前期间的成本结构占比</div>
          </div>
        </div>
        <div ref="compositionChartRef" class="chart-instance"></div>
        <div class="composition-legend">
          <div v-for="item in avgComposition" :key="item.label" class="legend-item">
            <div class="legend-dot" :style="{ backgroundColor: item.color }"></div>
            <div class="legend-label">{{ item.label }}</div>
            <div class="legend-value">{{ item.percentage }}%</div>
          </div>
        </div>
      </ContentWrap>
    </div>

    <ContentWrap class="chart-card chart-card--full">
      <div class="chart-header">
        <div>
          <div class="chart-header__title">产品成本对比</div>
          <div class="chart-header__desc">比较同一期间内不同产品的成本差异</div>
        </div>
      </div>
      <div ref="compareChartRef" class="chart-instance chart-instance--wide"></div>
    </ContentWrap>

    <ContentWrap class="data-table-card">
      <div class="table-header">
        <div class="table-header__title">成本数据明细</div>
        <div class="table-header__actions">
          <el-button type="primary" plain @click="handleExport">
            <Icon icon="ep:download" class="mr-5px" />
            导出报表
          </el-button>
        </div>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe class="data-table">
        <el-table-column label="期间" min-width="100" align="center">
          <template #default="{ row }">
            <span class="font-mono">{{ row.period }}</span>
          </template>
        </el-table-column>
        <el-table-column label="产品名称" min-width="150">
          <template #default="{ row }">
            <span class="font-semibold">{{ row.productName || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="关联工单" min-width="120" align="center">
          <template #default="{ row }">
            <span v-if="row.orderNo" class="font-mono text-blue-600">{{ row.orderNo }}</span>
            <span v-else class="text-slate-400">-</span>
          </template>
        </el-table-column>
        <el-table-column label="直接材料" min-width="120" align="right">
          <template #default="{ row }">
            <span v-if="row.materialCost > 0" class="font-mono">{{ formatMoney(row.materialCost) }}</span>
            <span v-else class="text-xs text-slate-400">-</span>
          </template>
        </el-table-column>
        <el-table-column label="直接人工" min-width="120" align="right">
          <template #default="{ row }">
            <span v-if="row.laborCost > 0" class="font-mono">{{ formatMoney(row.laborCost) }}</span>
            <span v-else class="text-xs text-slate-400">-</span>
          </template>
        </el-table-column>
        <el-table-column label="折旧" min-width="100" align="right">
          <template #default="{ row }">
            <span v-if="row.depreciationCost > 0" class="font-mono">{{ formatMoney(row.depreciationCost) }}</span>
            <span v-else class="text-xs text-slate-400">-</span>
          </template>
        </el-table-column>
        <el-table-column label="电费" min-width="100" align="right">
          <template #default="{ row }">
            <span v-if="row.powerCost > 0" class="font-mono">{{ formatMoney(row.powerCost) }}</span>
            <span v-else class="text-xs text-slate-400">-</span>
          </template>
        </el-table-column>
        <el-table-column label="其他制造费" min-width="120" align="right">
          <template #default="{ row }">
            <span class="font-mono">{{ formatMoney(row.otherCost) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="成本合计" min-width="130" align="right">
          <template #default="{ row }">
            <span class="font-mono font-bold text-red-600">{{ formatMoney(row.totalCost) }}</span>
          </template>
        </el-table-column>
      </el-table>
    </ContentWrap>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, reactive, ref } from 'vue'
import * as echarts from 'echarts'
import {
  FinanceCostAnalysisApi,
  type ProductionCostProductVO,
  type ProductionCostTrendRespVO
} from '@/api/erp/finance/cost-analysis'

defineOptions({ name: 'ErpProductCostTrend' })

type TrendRow = {
  period: string
  productId?: number
  productName?: string
  orderNo?: string | null
  materialCost: number
  laborCost: number
  depreciationCost: number
  powerCost: number
  otherCost: number
  totalCost: number
}

const loading = ref(false)
const productList = ref<ProductionCostProductVO[]>([])
const tableData = ref<TrendRow[]>([])
const trendResp = ref<ProductionCostTrendRespVO>({})

const queryParams = reactive({
  productId: undefined as number | undefined,
  compareProductIds: [] as number[],
  startMonth: (() => {
    const d = new Date()
    d.setMonth(d.getMonth() - 5)
    return d.toISOString().slice(0, 7)
  })(),
  endMonth: new Date().toISOString().slice(0, 7),
  dimension: 'month' as 'month' | 'quarter'
})

const dateRange = ref<[string, string]>([queryParams.startMonth, queryParams.endMonth])

const trendChartRef = ref<HTMLElement>()
const compositionChartRef = ref<HTMLElement>()
const compareChartRef = ref<HTMLElement>()
let trendChart: echarts.ECharts | null = null
let compositionChart: echarts.ECharts | null = null
let compareChart: echarts.ECharts | null = null

const colorMap = ['#3B82F6', '#10B981', '#F59E0B', '#06B6D4', '#14B8A6']

const formatMoney = (value?: number | string | null) => {
  const n = Number(value || 0)
  return `¥${n.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

const toNumber = (value?: number | string | null) => Number(value || 0)

const productNameMap = computed(() => {
  return new Map(
    productList.value
      .filter((item) => item.productId != null)
      .map((item) => [item.productId as number, item.productName || `产品${item.productId}`])
  )
})

const avgComposition = computed(() => {
  const comp = trendResp.value.compositionData
  if (!comp) return []
  const items = [
    { label: '直接材料', value: toNumber(comp.materialCost), color: colorMap[0] },
    { label: '直接人工', value: toNumber(comp.laborCost), color: colorMap[1] },
    { label: '折旧', value: toNumber(comp.depreciationCost), color: colorMap[2] },
    { label: '电费', value: toNumber(comp.powerCost), color: colorMap[3] },
    { label: '其他', value: toNumber(comp.otherCost), color: colorMap[4] }
  ]
  const total = items.reduce((sum, item) => sum + item.value, 0)
  if (total <= 0) return []
  return items.map((item) => ({
    ...item,
    percentage: Math.round((item.value / total) * 100)
  }))
})

const syncDateRange = () => {
  if (!dateRange.value?.[0] || !dateRange.value?.[1]) return
  queryParams.startMonth = dateRange.value[0]
  queryParams.endMonth = dateRange.value[1]
}

const handleQuery = () => {
  loadData()
}

const resetQuery = () => {
  queryParams.productId = undefined
  queryParams.compareProductIds = []
  const d = new Date()
  d.setMonth(d.getMonth() - 5)
  queryParams.startMonth = d.toISOString().slice(0, 7)
  queryParams.endMonth = new Date().toISOString().slice(0, 7)
  queryParams.dimension = 'month'
  dateRange.value = [queryParams.startMonth, queryParams.endMonth]
  loadData()
}

const loadProductOptions = async () => {
  const data = await FinanceCostAnalysisApi.getCostProducts()
  productList.value = data || []
}

const buildTableData = () => {
  const periods = trendResp.value.periods || []
  const rows: TrendRow[] = []

  if (!queryParams.productId) {
    for (const product of trendResp.value.productCompareData || []) {
      rows.push({
        period: `${queryParams.startMonth} ~ ${queryParams.endMonth}`,
        productId: product.productId,
        productName:
          product.productName ||
          (product.productId != null ? productNameMap.value.get(product.productId) : '-') ||
          '-',
        orderNo: null,
        materialCost: toNumber(product.materialCost),
        laborCost: toNumber(product.laborCost),
        depreciationCost: 0,
        powerCost: 0,
        otherCost: toNumber(product.overheadCost),
        totalCost: toNumber(product.totalCost)
      })
    }
    tableData.value = rows
    return
  }

  const orderData = trendResp.value.productOrderData || {}
  for (let i = 0; i < periods.length; i++) {
    const period = periods[i]
    const orderInfo = orderData[period] || {}
    rows.push({
      period,
      productId: queryParams.productId,
      productName:
        productNameMap.value.get(queryParams.productId) || `产品${queryParams.productId}`,
      orderNo: orderInfo.orderNo || null,
      materialCost: toNumber(trendResp.value.materialCosts?.[i]),
      laborCost: toNumber(trendResp.value.laborCosts?.[i]),
      depreciationCost: toNumber(trendResp.value.depreciationCosts?.[i]),
      powerCost: toNumber(trendResp.value.powerCosts?.[i]),
      otherCost: toNumber(trendResp.value.otherCosts?.[i]),
      totalCost: toNumber(trendResp.value.totalCosts?.[i])
    })
  }
  tableData.value = rows
}

const renderTrendChart = () => {
  if (!trendChartRef.value || !trendResp.value.periods?.length) return
  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value)
  }

  const option = {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: {
      data: ['直接材料', '直接人工', '折旧', '电费', '其他', '总成本'],
      bottom: 0
    },
    grid: { left: '3%', right: '4%', bottom: '12%', top: '6%', containLabel: true },
    xAxis: { type: 'category', data: trendResp.value.periods },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: (value: number) => `¥${(value / 10000).toFixed(1)}万`
      }
    },
    series: [
      {
        name: '直接材料',
        type: 'line',
        smooth: true,
        areaStyle: { opacity: 0.18 },
        data: trendResp.value.materialCosts || [],
        itemStyle: { color: colorMap[0] }
      },
      {
        name: '直接人工',
        type: 'line',
        smooth: true,
        areaStyle: { opacity: 0.18 },
        data: trendResp.value.laborCosts || [],
        itemStyle: { color: colorMap[1] }
      },
      {
        name: '折旧',
        type: 'line',
        smooth: true,
        areaStyle: { opacity: 0.18 },
        data: trendResp.value.depreciationCosts || [],
        itemStyle: { color: colorMap[2] }
      },
      {
        name: '电费',
        type: 'line',
        smooth: true,
        areaStyle: { opacity: 0.18 },
        data: trendResp.value.powerCosts || [],
        itemStyle: { color: colorMap[3] }
      },
      {
        name: '其他',
        type: 'line',
        smooth: true,
        areaStyle: { opacity: 0.18 },
        data: trendResp.value.otherCosts || [],
        itemStyle: { color: colorMap[4] }
      },
      {
        name: '总成本',
        type: 'line',
        smooth: true,
        symbol: 'diamond',
        symbolSize: 8,
        lineStyle: { width: 2, type: 'dashed' },
        data: trendResp.value.totalCosts || [],
        itemStyle: { color: '#ef4444' }
      }
    ]
  }

  trendChart.setOption(option, true)
}

const renderCompositionChart = () => {
  if (!compositionChartRef.value || !avgComposition.value.length) return
  if (!compositionChart) {
    compositionChart = echarts.init(compositionChartRef.value)
  }

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['50%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: { show: false },
        data: avgComposition.value.map((item) => ({
          name: item.label,
          value: item.value,
          itemStyle: { color: item.color }
        }))
      }
    ]
  }

  compositionChart.setOption(option, true)
}

const renderCompareChart = () => {
  if (!compareChartRef.value || !trendResp.value.productCompareData?.length) return
  if (!compareChart) {
    compareChart = echarts.init(compareChartRef.value)
  }

  const compareData = trendResp.value.productCompareData || []
  const selectedCompareIds = queryParams.compareProductIds.length
    ? new Set(queryParams.compareProductIds)
    : null
  const visibleData = selectedCompareIds
    ? compareData.filter((item) => item.productId != null && selectedCompareIds.has(item.productId))
    : compareData.slice(0, 5)

  const option = {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: {
      data: ['直接材料', '直接人工', '制造费用'],
      bottom: 0
    },
    grid: { left: '3%', right: '4%', bottom: '12%', top: '6%', containLabel: true },
    xAxis: {
      type: 'category',
      data: visibleData.map((item) => item.productName || `产品${item.productId}`)
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: (value: number) => `¥${(value / 10000).toFixed(1)}万`
      }
    },
    series: [
      {
        name: '直接材料',
        type: 'bar',
        stack: 'Total',
        data: visibleData.map((item) => toNumber(item.materialCost)),
        itemStyle: { color: colorMap[0] }
      },
      {
        name: '直接人工',
        type: 'bar',
        stack: 'Total',
        data: visibleData.map((item) => toNumber(item.laborCost)),
        itemStyle: { color: colorMap[1] }
      },
      {
        name: '制造费用',
        type: 'bar',
        stack: 'Total',
        data: visibleData.map((item) => toNumber(item.overheadCost)),
        itemStyle: { color: colorMap[2] }
      }
    ]
  }

  compareChart.setOption(option, true)
}

const handleResize = () => {
  trendChart?.resize()
  compositionChart?.resize()
  compareChart?.resize()
}

const loadData = async () => {
  if (!dateRange.value?.[0] || !dateRange.value?.[1]) return
  syncDateRange()

  loading.value = true
  try {
    const data = await FinanceCostAnalysisApi.getCostTrend({
      productId: queryParams.productId,
      startMonth: queryParams.startMonth,
      endMonth: queryParams.endMonth,
      dimension: queryParams.dimension
    })
    trendResp.value = data || {}
    buildTableData()
    await nextTick()
    renderTrendChart()
    renderCompositionChart()
    renderCompareChart()
  } finally {
    loading.value = false
  }
}

const handleExport = () => {
  console.info('成本趋势导出暂由统一导出能力接管')
}

onMounted(async () => {
  await loadProductOptions()
  await loadData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  compositionChart?.dispose()
  compareChart?.dispose()
})
</script>

<style scoped lang="scss">
.cost-trend-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px;
  background: var(--erp-slate-50);
  min-height: 100vh;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}

.page-header__title {
  font-size: 20px;
  font-weight: 800;
  color: var(--erp-slate-900);
}

.page-header__desc {
  margin-top: 4px;
  color: var(--erp-slate-500);
  font-size: 12px;
}

.page-header__actions {
  display: flex;
  gap: 12px;
}

.filter-card {
  .filter-form {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .filter-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
    gap: 16px;
  }

  .filter-actions {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
  }
}

.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.chart-card--full {
  grid-column: 1 / -1;
}

.chart-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.chart-header__title {
  font-size: 14px;
  font-weight: 700;
  color: var(--erp-slate-700);
}

.chart-header__desc {
  margin-top: 4px;
  color: var(--erp-slate-500);
  font-size: 12px;
}

.chart-header__legend {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  color: var(--erp-slate-500);
  font-size: 12px;
}

.legend-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 2px;
  margin-right: 4px;
}

.chart-instance {
  width: 100%;
  height: 350px;
}

.chart-instance--wide {
  height: 340px;
}

.composition-legend {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.legend-label {
  flex: 1;
  color: var(--erp-slate-600);
}

.legend-value {
  font-weight: 600;
  color: var(--erp-slate-900);
}

.data-table-card {
  .table-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 16px;
  }

  .table-header__title {
    font-size: 14px;
    font-weight: 700;
    color: var(--erp-slate-700);
  }
}

.data-table {
  :deep(.el-table__header-wrapper th) {
    background: var(--erp-slate-50);
    color: var(--erp-slate-600);
    font-weight: 600;
    font-size: 12px;
  }
}

@media (max-width: 1024px) {
  .chart-grid {
    grid-template-columns: 1fr;
  }

  .chart-card--full {
    grid-column: 1 / -1;
  }
}
</style>
