<template>
  <div class="cost-trend-page">
    <ContentWrap class="page-header-card">
      <div class="page-header">
        <div class="page-header__main">
          <div class="page-header__title">产品成本趋势分析</div>
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
                :key="item.id"
                :label="item.name"
                :value="item.id"
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
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </div>

        <div class="filter-actions">
          <el-button type="primary" :loading="loading" @click="handleQuery" v-hasPermi="['erp:cost-product:query']">
            <Icon icon="ep:search" class="mr-5px" />
            查询
          </el-button>
          <el-button @click="resetQuery">
            <Icon icon="ep:refresh-left" class="mr-5px" />
            重置
          </el-button>
        </div>
      </el-form>
    </ContentWrap>

    <div class="chart-row">
      <ContentWrap class="chart-card chart-card--main">
        <div class="chart-header">
          <div class="chart-header__title">成本趋势</div>
          <div class="chart-header__legend">
            <span class="legend-dot legend-dot--blue"></span>
            直接材料
            <span class="legend-dot legend-dot--green"></span>
            直接人工
            <span class="legend-dot legend-dot--amber"></span>
            制造费用
            <span class="legend-dot legend-dot--red"></span>
            总成本
          </div>
        </div>
        <div ref="trendChartRef" class="chart-container"></div>
      </ContentWrap>

      <ContentWrap class="chart-card chart-card--side">
        <div class="chart-header">
          <div class="chart-header__title">成本构成</div>
        </div>
        <div ref="pieChartRef" class="chart-container"></div>
        <div class="composition-legend">
          <div v-for="item in avgComposition" :key="item.label" class="legend-item">
            <div class="legend-dot" :style="{ backgroundColor: item.color }"></div>
            <div class="legend-label">{{ item.label }}</div>
            <div class="legend-value">{{ item.percentage }}%</div>
          </div>
        </div>
      </ContentWrap>
    </div>

    <ContentWrap class="cost-table-card">
      <div class="table-header">
        <div class="table-header__title">月度成本明细</div>
        <div class="table-header__count">
          当前 <strong>{{ trendData.length }}</strong> 条
        </div>
      </div>

      <el-table :data="trendData" v-loading="loading" stripe class="cost-table">
        <el-table-column label="月份" width="100" prop="accountingMonth" />
        <el-table-column label="产品" min-width="150" prop="productName" />
        <el-table-column label="直接材料" min-width="120" align="right">
          <template #default="{ row }">
            <span class="font-mono">{{ formatMoney(row.materialCost) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="直接人工" min-width="120" align="right">
          <template #default="{ row }">
            <span class="font-mono">{{ formatMoney(row.laborCost) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="制造费用" min-width="120" align="right">
          <template #default="{ row }">
            <span class="font-mono">{{ formatMoney(row.overheadCost) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="总成本" min-width="130" align="right">
          <template #default="{ row }">
            <span class="font-mono font-bold text-red-600">{{ formatMoney(row.totalCost) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="环比" width="100" align="right">
          <template #default="{ row }">
            <span
              v-if="row.momChange !== null && row.momChange !== undefined"
              :class="row.momChange > 0 ? 'text-red-500' : 'text-emerald-500'"
            >
              {{ row.momChange > 0 ? '+' : '' }}{{ row.momChange }}%
            </span>
            <span v-else class="text-slate-400">-</span>
          </template>
        </el-table-column>
      </el-table>
    </ContentWrap>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import * as echarts from 'echarts'
import { ProductionCostApi } from '@/api/erp/production-cost'

defineOptions({ name: 'ErpProductCostTrend' })

type ProductOption = {
  id: number
  name: string
}

type TrendRow = {
  accountingMonth: string
  productId?: number
  productName?: string
  materialCost?: number | string
  laborCost?: number | string
  depreciationCost?: number | string
  powerCost?: number | string
  otherCost?: number | string
  overheadCost?: number | string
  totalCost?: number | string
  momChange?: number | null
}

type CompositionItem = {
  label: string
  value: number
  color: string
  percentage: number
}

const loading = ref(false)
const trendData = ref<TrendRow[]>([])
const productList = ref<ProductOption[]>([])

const queryParams = reactive({
  productId: undefined as number | undefined,
  compareProductIds: [] as number[]
})

const dateRange = ref<[string, string]>([
  (() => {
    const date = new Date()
    date.setMonth(date.getMonth() - 5)
    return date.toISOString().slice(0, 7)
  })(),
  new Date().toISOString().slice(0, 7)
])

const trendChartRef = ref<HTMLElement>()
const pieChartRef = ref<HTMLElement>()

let trendChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null

const avgComposition = computed<CompositionItem[]>(() => {
  if (trendData.value.length === 0) {
    return []
  }

  const totals = trendData.value.reduce(
    (accumulator, item) => ({
      material: accumulator.material + Number(item.materialCost || 0),
      labor: accumulator.labor + Number(item.laborCost || 0),
      depreciation: accumulator.depreciation + Number(item.depreciationCost || 0),
      power: accumulator.power + Number(item.powerCost || 0),
      other: accumulator.other + Number(item.otherCost || 0)
    }),
    { material: 0, labor: 0, depreciation: 0, power: 0, other: 0 }
  )

  const total = totals.material + totals.labor + totals.depreciation + totals.power + totals.other
  if (total === 0) {
    return []
  }

  const items = [
    { label: '直接材料', value: totals.material, color: '#3B82F6' },
    { label: '直接人工', value: totals.labor, color: '#10B981' },
    { label: '折旧', value: totals.depreciation, color: '#F59E0B' },
    { label: '电费', value: totals.power, color: '#06B6D4' },
    { label: '其他', value: totals.other, color: '#14B8A6' }
  ]

  return items.map((item) => ({
    ...item,
    percentage: Math.round((item.value / total) * 100)
  }))
})

const formatMoney = (value?: number | string | null) => {
  const amount = Number(value || 0)
  return `¥${amount.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

const getPreviousMonth = (month: string) => {
  const date = new Date(`${month}-01`)
  date.setMonth(date.getMonth() - 1)
  return date.toISOString().slice(0, 7)
}

const generateMonths = (start: string, end: string) => {
  const months: string[] = []
  const current = new Date(`${start}-01`)
  const endDate = new Date(`${end}-01`)

  while (current <= endDate) {
    months.push(current.toISOString().slice(0, 7))
    current.setMonth(current.getMonth() + 1)
  }

  return months
}

const getDisplayProductIds = () => {
  if (queryParams.productId) {
    return [queryParams.productId]
  }

  if (queryParams.compareProductIds.length > 0) {
    return queryParams.compareProductIds
  }

  return [...new Set(trendData.value.map((item) => item.productId).filter(Boolean) as number[])].slice(0, 5)
}

const handleQuery = () => {
  loadData()
}

const resetQuery = () => {
  queryParams.productId = undefined
  queryParams.compareProductIds = []
  const date = new Date()
  date.setMonth(date.getMonth() - 5)
  dateRange.value = [date.toISOString().slice(0, 7), new Date().toISOString().slice(0, 7)]
  loadData()
}

const renderTrendChart = () => {
  if (!trendChartRef.value) {
    return
  }

  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value)
  }

  const months = [...new Set(trendData.value.map((item) => item.accountingMonth))].sort()
  const productIds = getDisplayProductIds()
  const series: echarts.SeriesOption[] = []
  const colors = ['#3B82F6', '#10B981', '#F59E0B', '#EF4444', '#06B6D4']

  productIds.forEach((productId, index) => {
    const productData = trendData.value.filter((item) => item.productId === productId)
    const productName = productData[0]?.productName || `产品${productId}`
    const color = colors[index % colors.length]

    series.push({
      name: `${productName} - 材料`,
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      lineStyle: { width: 2, color },
      itemStyle: { color },
      data: months.map((month) => {
        const item = productData.find((entry) => entry.accountingMonth === month)
        return item ? Number(item.materialCost || 0) : null
      })
    })

    series.push({
      name: `${productName} - 总成本`,
      type: 'line',
      smooth: true,
      symbol: 'diamond',
      symbolSize: 8,
      lineStyle: { width: 2, type: 'dashed', color },
      itemStyle: { color },
      data: months.map((month) => {
        const item = productData.find((entry) => entry.accountingMonth === month)
        return item ? Number(item.totalCost || 0) : null
      })
    })
  })

  trendChart.setOption(
    {
      tooltip: {
        trigger: 'axis',
        formatter: (params: any) => {
          if (!params || params.length === 0) {
            return ''
          }
          let html = `<div style="font-weight:600;margin-bottom:4px">${params[0].axisValue}</div>`
          params.forEach((item: any) => {
            html += `<div>${item.marker} ${item.seriesName}: ¥${Number(item.value || 0).toLocaleString()}</div>`
          })
          return html
        }
      },
      grid: {
        left: 60,
        right: 20,
        top: 20,
        bottom: 30
      },
      xAxis: {
        type: 'category',
        data: months,
        axisLine: { lineStyle: { color: '#E2E8F0' } },
        axisLabel: { color: '#64748B', fontSize: 12 }
      },
      yAxis: {
        type: 'value',
        axisLine: { show: false },
        splitLine: { lineStyle: { color: '#F1F5F9' } },
        axisLabel: {
          color: '#64748B',
          fontSize: 12,
          formatter: (value: number) => `¥${(value / 1000).toFixed(0)}k`
        }
      },
      series
    },
    true
  )
}

const renderPieChart = () => {
  if (!pieChartRef.value || avgComposition.value.length === 0) {
    return
  }

  if (!pieChart) {
    pieChart = echarts.init(pieChartRef.value)
  }

  pieChart.setOption(
    {
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
            borderRadius: 6,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: { show: false },
          emphasis: {
            label: { show: true, fontSize: 14, fontWeight: 'bold' }
          },
          data: avgComposition.value.map((item) => ({
            value: item.value,
            name: item.label,
            itemStyle: { color: item.color }
          }))
        }
      ]
    },
    true
  )
}

const loadData = async () => {
  if (!dateRange.value?.[0] || !dateRange.value?.[1]) {
    return
  }

  loading.value = true
  try {
    const [startMonth, endMonth] = dateRange.value
    const months = generateMonths(startMonth, endMonth)
    const paramsList = months.map((month) => {
      const params: Record<string, unknown> = { accountingMonth: month }
      if (queryParams.productId) {
        params.productId = queryParams.productId
      }
      return params
    })

    const results = await Promise.all(
      paramsList.map((params) => ProductionCostApi.getProductSummaryV2(params))
    )

    const allData: TrendRow[] = []
    results.forEach((data, index) => {
      if (Array.isArray(data)) {
        data.forEach((item: TrendRow) => {
          allData.push({ ...item, accountingMonth: months[index] })
        })
      }
    })

    trendData.value = allData.sort((left, right) => {
      if (left.accountingMonth !== right.accountingMonth) {
        return left.accountingMonth.localeCompare(right.accountingMonth)
      }
      return (left.productId || 0) - (right.productId || 0)
    })

    trendData.value.forEach((current) => {
      const prevMonth = getPreviousMonth(current.accountingMonth)
      const prev = trendData.value.find(
        (item) => item.accountingMonth === prevMonth && item.productId === current.productId
      )

      if (prev && Number(prev.totalCost) > 0) {
        current.momChange = Math.round(
          ((Number(current.totalCost) - Number(prev.totalCost)) / Number(prev.totalCost)) * 100
        )
      } else {
        current.momChange = null
      }
    })

    await nextTick()
    renderTrendChart()
    renderPieChart()
  } finally {
    loading.value = false
  }
}

const handleResize = () => {
  trendChart?.resize()
  pieChart?.resize()
}

onMounted(async () => {
  try {
    const costProducts = await ProductionCostApi.getCostProductList()
    productList.value = costProducts
      .filter((item): item is ProductOption => !!item.id && !!item.name)
      .map((item) => ({
        id: item.id,
        name: item.name
      }))
  } catch {
    productList.value = []
  }

  await loadData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  pieChart?.dispose()
})

watch(
  () => dateRange.value,
  () => {
    if (dateRange.value?.[0] && dateRange.value?.[1]) {
      loadData()
    }
  }
)
</script>

<style scoped lang="scss">
.cost-trend-page {
  display: flex;
  min-height: 100vh;
  flex-direction: column;
  gap: 16px;
  padding: 16px;
  background: var(--erp-slate-50);
}

.page-header-card {
  .page-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    flex-wrap: wrap;
    gap: 12px;
  }

  .page-header__title {
    color: var(--erp-slate-900);
    font-size: 20px;
    font-weight: 800;
  }

  .page-header__actions {
    display: flex;
    gap: 12px;
  }
}

.filter-card {
  .filter-form {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .filter-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
    gap: 16px;
  }

  .filter-actions {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
  }
}

.chart-row {
  display: grid;
  grid-template-columns: minmax(0, 2fr) minmax(320px, 1fr);
  gap: 16px;
}

.chart-card {
  min-width: 0;
}

.chart-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.chart-header__title {
  color: var(--erp-slate-700);
  font-size: 14px;
  font-weight: 600;
}

.chart-header__legend {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  color: var(--erp-slate-500);
  font-size: 12px;
}

.legend-dot {
  display: inline-flex;
  width: 10px;
  height: 10px;
  flex: none;
  border-radius: 999px;
}

.legend-dot--blue {
  background: #3b82f6;
}

.legend-dot--green {
  background: #10b981;
}

.legend-dot--amber {
  background: #f59e0b;
}

.legend-dot--red {
  background: #ef4444;
}

.chart-container {
  width: 100%;
  height: 360px;
}

.composition-legend {
  display: grid;
  gap: 10px;
  margin-top: 16px;
}

.legend-item {
  display: grid;
  grid-template-columns: 10px 1fr auto;
  align-items: center;
  gap: 10px;
}

.legend-label,
.legend-value {
  color: var(--erp-slate-600);
  font-size: 12px;
}

.legend-value {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
}

.cost-table-card {
  .table-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 16px;
    gap: 12px;
  }

  .table-header__title {
    color: var(--erp-slate-700);
    font-size: 14px;
    font-weight: 600;
  }

  .table-header__count {
    color: var(--erp-slate-500);
    font-size: 12px;
  }
}

.cost-table {
  :deep(.el-table__header-wrapper th) {
    background: var(--erp-slate-50);
    color: var(--erp-slate-600);
    font-size: 12px;
    font-weight: 600;
  }
}

@media (max-width: 1024px) {
  .chart-row {
    grid-template-columns: 1fr;
  }

  .chart-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
