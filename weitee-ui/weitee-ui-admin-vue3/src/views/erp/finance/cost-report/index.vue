<template>
  <div class="cost-report-page">
    <ContentWrap class="page-header-card">
      <div class="page-header">
        <div class="page-header__main">
          <div class="page-header__title">产品成本分析报表</div>
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
      <div class="filter-header">
        <div class="filter-header__title">筛选条件</div>
      </div>
      <el-form :model="queryParams" label-width="88px" class="filter-form" @submit.prevent>
        <div class="filter-grid">
          <el-form-item label="产品">
            <el-select
              v-model="queryParams.productId"
              placeholder="请选择产品"
              filterable
              clearable
              class="!w-full"
              @change="handleQuery"
            >
              <el-option
                v-for="item in productOptions"
                :key="item.productId"
                :label="item.productName || `产品${item.productId}`"
                :value="item.productId"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="开始月份">
            <el-date-picker
              v-model="queryParams.startMonth"
              type="month"
              value-format="YYYY-MM"
              placeholder="开始月份"
              class="!w-full"
              @change="handleQuery"
            />
          </el-form-item>
          <el-form-item label="结束月份">
            <el-date-picker
              v-model="queryParams.endMonth"
              type="month"
              value-format="YYYY-MM"
              placeholder="结束月份"
              class="!w-full"
              @change="handleQuery"
            />
          </el-form-item>
          <el-form-item label="分析维度">
            <el-select v-model="queryParams.dimension" class="!w-full" @change="handleQuery">
              <el-option label="按月分析" value="month" />
              <el-option label="按季分析" value="quarter" />
            </el-select>
          </el-form-item>
        </div>
        <div class="filter-actions">
          <el-button type="primary" :loading="loading" @click="handleQuery" v-hasPermi="['erp:cost-report:query']">
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

    <div class="chart-grid">
      <ContentWrap class="chart-card">
        <div class="chart-header">
          <div class="chart-header__title">成本趋势分析</div>
          <div class="chart-header__desc">展示产品成本随时间变化趋势</div>
        </div>
        <div class="chart-container">
          <div ref="trendChartRef" class="chart-instance"></div>
        </div>
      </ContentWrap>

      <ContentWrap class="chart-card">
        <div class="chart-header">
          <div class="chart-header__title">成本构成分析</div>
          <div class="chart-header__desc">展示产品成本构成占比</div>
        </div>
        <div class="chart-container">
          <div ref="compositionChartRef" class="chart-instance"></div>
        </div>
      </ContentWrap>
    </div>

    <ContentWrap class="chart-card chart-card--full">
      <div class="chart-header">
        <div class="chart-header__title">产品成本对比</div>
        <div class="chart-header__desc">对比不同产品同一期间的成本</div>
      </div>
      <div class="chart-container">
        <div ref="compareChartRef" class="chart-instance"></div>
      </div>
    </ContentWrap>

    <ContentWrap class="data-table-card">
      <div class="table-header">
        <div class="table-header__title">成本数据明细</div>
        <div class="table-header__actions">
          <el-button type="primary" plain @click="handleExport" v-hasPermi="['erp:cost-report:query']">
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
            <span v-else class="text-slate-400 text-xs">无领料记录</span>
          </template>
        </el-table-column>
        <el-table-column label="直接人工" min-width="120" align="right">
          <template #default="{ row }">
            <span v-if="row.laborCost > 0" class="font-mono">{{ formatMoney(row.laborCost) }}</span>
            <span v-else class="text-slate-400 text-xs">-</span>
          </template>
        </el-table-column>
        <el-table-column label="折旧" min-width="100" align="right">
          <template #default="{ row }">
            <span v-if="row.depreciationCost > 0" class="font-mono">{{ formatMoney(row.depreciationCost) }}</span>
            <span v-else class="text-slate-400 text-xs">-</span>
          </template>
        </el-table-column>
        <el-table-column label="电费" min-width="100" align="right">
          <template #default="{ row }">
            <span v-if="row.powerCost > 0" class="font-mono">{{ formatMoney(row.powerCost) }}</span>
            <span v-else class="text-slate-400 text-xs">-</span>
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
import { nextTick, onMounted, onUnmounted, reactive, ref } from 'vue'
import * as echarts from 'echarts'
import request from '@/config/axios'

defineOptions({ name: 'ErpFinanceCostReport' })

type ProductOption = {
  productId: number
  productName?: string
}

type CompareItem = {
  productId: number
  productName?: string
  materialCost?: number | string
  laborCost?: number | string
  overheadCost?: number | string
}

type CompositionData = {
  materialCost?: number | string
  laborCost?: number | string
  depreciationCost?: number | string
  powerCost?: number | string
  otherCost?: number | string
}

type ProductOrderData = Record<string, { orderNo?: string | null }>

type TrendData = {
  periods?: string[]
  materialCosts?: Array<number | string>
  laborCosts?: Array<number | string>
  depreciationCosts?: Array<number | string>
  powerCosts?: Array<number | string>
  otherCosts?: Array<number | string>
  totalCosts?: Array<number | string>
  compositionData?: CompositionData
  productCompareData?: CompareItem[]
  productOrderData?: ProductOrderData
}

type TableRow = {
  period: string
  productName: string
  orderNo: string | null
  materialCost: number
  laborCost: number
  depreciationCost: number
  powerCost: number
  otherCost: number
  totalCost: number
}

const loading = ref(false)
const productOptions = ref<ProductOption[]>([])
const tableData = ref<TableRow[]>([])

const queryParams = reactive({
  productId: undefined as number | undefined,
  startMonth: '2026-01',
  endMonth: '2026-06',
  dimension: 'month'
})

const trendChartRef = ref<HTMLElement>()
const compositionChartRef = ref<HTMLElement>()
const compareChartRef = ref<HTMLElement>()

let trendChart: echarts.ECharts | null = null
let compositionChart: echarts.ECharts | null = null
let compareChart: echarts.ECharts | null = null

const trendData = ref<TrendData>({})

const formatMoney = (value?: number | string | null) => {
  const amount = Number(value || 0)
  return `¥${amount.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

const getProductLabel = (productId?: number) => {
  const target = productOptions.value.find((item) => item.productId === productId)
  return target?.productName || `产品${productId}`
}

const disposeChart = (chart: echarts.ECharts | null) => {
  if (chart) {
    chart.dispose()
  }
}

const loadProductOptions = async () => {
  try {
    const data = await request.get({
      url: '/erp/production-cost-entry/product-summary',
      params: {}
    })
    productOptions.value = data || []
  } catch (error) {
    console.error('加载产品列表失败', error)
  }
}

const loadTrendData = async () => {
  const params: Record<string, unknown> = {
    startMonth: queryParams.startMonth,
    endMonth: queryParams.endMonth,
    dimension: queryParams.dimension
  }
  if (queryParams.productId) {
    params.productId = queryParams.productId
  }
  const data = await request.get({ url: '/erp/production-cost-entry/cost-trend', params })
  trendData.value = data || {}
  return data
}

const initTrendChart = () => {
  if (!trendChartRef.value || !trendData.value.periods?.length) {
    disposeChart(trendChart)
    trendChart = null
    return
  }

  disposeChart(trendChart)
  trendChart = echarts.init(trendChartRef.value)
  trendChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    legend: {
      data: ['直接材料', '直接人工', '折旧', '电费', '其他'],
      bottom: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      top: '5%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: trendData.value.periods
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
        type: 'line',
        stack: 'Total',
        areaStyle: { opacity: 0.3 },
        emphasis: { focus: 'series' },
        data: trendData.value.materialCosts || [],
        itemStyle: { color: '#3B82F6' }
      },
      {
        name: '直接人工',
        type: 'line',
        stack: 'Total',
        areaStyle: { opacity: 0.3 },
        emphasis: { focus: 'series' },
        data: trendData.value.laborCosts || [],
        itemStyle: { color: '#10B981' }
      },
      {
        name: '折旧',
        type: 'line',
        stack: 'Total',
        areaStyle: { opacity: 0.3 },
        emphasis: { focus: 'series' },
        data: trendData.value.depreciationCosts || [],
        itemStyle: { color: '#F59E0B' }
      },
      {
        name: '电费',
        type: 'line',
        stack: 'Total',
        areaStyle: { opacity: 0.3 },
        emphasis: { focus: 'series' },
        data: trendData.value.powerCosts || [],
        itemStyle: { color: '#06B6D4' }
      },
      {
        name: '其他',
        type: 'line',
        stack: 'Total',
        areaStyle: { opacity: 0.3 },
        emphasis: { focus: 'series' },
        data: trendData.value.otherCosts || [],
        itemStyle: { color: '#14B8A6' }
      }
    ]
  })
}

const initCompositionChart = () => {
  if (!compositionChartRef.value || !trendData.value.compositionData) {
    disposeChart(compositionChart)
    compositionChart = null
    return
  }

  disposeChart(compositionChart)
  compositionChart = echarts.init(compositionChartRef.value)
  const composition = trendData.value.compositionData
  compositionChart.setOption({
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      top: 'center'
    },
    series: [
      {
        name: '成本构成',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          { name: '直接材料', value: Number(composition.materialCost || 0), itemStyle: { color: '#3B82F6' } },
          { name: '直接人工', value: Number(composition.laborCost || 0), itemStyle: { color: '#10B981' } },
          { name: '折旧', value: Number(composition.depreciationCost || 0), itemStyle: { color: '#F59E0B' } },
          { name: '电费', value: Number(composition.powerCost || 0), itemStyle: { color: '#06B6D4' } },
          { name: '其他', value: Number(composition.otherCost || 0), itemStyle: { color: '#14B8A6' } }
        ]
      }
    ]
  })
}

const initCompareChart = () => {
  if (!compareChartRef.value || !trendData.value.productCompareData?.length) {
    disposeChart(compareChart)
    compareChart = null
    return
  }

  disposeChart(compareChart)
  compareChart = echarts.init(compareChartRef.value)
  const compareData = trendData.value.productCompareData
  compareChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    legend: {
      data: ['直接材料', '直接人工', '制造费用'],
      bottom: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      top: '5%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: compareData.map((item) => item.productName || `产品${item.productId}`)
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
        emphasis: { focus: 'series' },
        data: compareData.map((item) => Number(item.materialCost || 0)),
        itemStyle: { color: '#3B82F6' }
      },
      {
        name: '直接人工',
        type: 'bar',
        stack: 'Total',
        emphasis: { focus: 'series' },
        data: compareData.map((item) => Number(item.laborCost || 0)),
        itemStyle: { color: '#10B981' }
      },
      {
        name: '制造费用',
        type: 'bar',
        stack: 'Total',
        emphasis: { focus: 'series' },
        data: compareData.map((item) => Number(item.overheadCost || 0)),
        itemStyle: { color: '#F59E0B' }
      }
    ]
  })
}

const buildTableData = () => {
  const periods = trendData.value.periods || []
  const rows: TableRow[] = []

  if (!queryParams.productId) {
    const compareData = trendData.value.productCompareData || []
    for (const product of compareData) {
      const materialCost = Number(product.materialCost || 0)
      const laborCost = Number(product.laborCost || 0)
      const overheadCost = Number(product.overheadCost || 0)
      rows.push({
        period: `${queryParams.startMonth} ~ ${queryParams.endMonth}`,
        productName: product.productName || `产品${product.productId}`,
        orderNo: null,
        materialCost,
        laborCost,
        depreciationCost: overheadCost * 0.4,
        powerCost: overheadCost * 0.3,
        otherCost: overheadCost * 0.3,
        totalCost: materialCost + laborCost + overheadCost
      })
    }
  } else {
    const orderData = trendData.value.productOrderData || {}
    for (let index = 0; index < periods.length; index++) {
      const period = periods[index]
      const orderInfo = orderData[period] || {}
      rows.push({
        period,
        productName: getProductLabel(queryParams.productId),
        orderNo: orderInfo.orderNo || null,
        materialCost: Number(trendData.value.materialCosts?.[index] || 0),
        laborCost: Number(trendData.value.laborCosts?.[index] || 0),
        depreciationCost: Number(trendData.value.depreciationCosts?.[index] || 0),
        powerCost: Number(trendData.value.powerCosts?.[index] || 0),
        otherCost: Number(trendData.value.otherCosts?.[index] || 0),
        totalCost: Number(trendData.value.totalCosts?.[index] || 0)
      })
    }
  }

  tableData.value = rows
}

const handleQuery = () => {
  loadData()
}

const resetQuery = () => {
  queryParams.productId = undefined
  queryParams.startMonth = '2026-01'
  queryParams.endMonth = '2026-06'
  queryParams.dimension = 'month'
  loadData()
}

const loadData = async () => {
  loading.value = true
  try {
    await loadTrendData()
    buildTableData()
    await nextTick()
    initTrendChart()
    initCompositionChart()
    initCompareChart()
  } catch (error) {
    console.error('加载数据失败', error)
  } finally {
    loading.value = false
  }
}

const handleExport = () => {
  console.log('导出报表')
}

const handleResize = () => {
  trendChart?.resize()
  compositionChart?.resize()
  compareChart?.resize()
}

onMounted(() => {
  loadProductOptions()
  loadData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  disposeChart(trendChart)
  disposeChart(compositionChart)
  disposeChart(compareChart)
})
</script>

<style scoped lang="scss">
.cost-report-page {
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

  .page-header__desc {
    margin-top: 4px;
    color: var(--erp-slate-500);
    font-size: 12px;
  }

  .page-header__actions {
    display: flex;
    align-items: center;
    gap: 12px;
  }
}

.filter-card {
  .filter-header {
    margin-bottom: 16px;
  }

  .filter-header__title {
    color: var(--erp-slate-700);
    font-size: 14px;
    font-weight: 600;
  }

  .filter-form {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .filter-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
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
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.chart-card {
  &--full {
    grid-column: span 2;
  }
}

.chart-header {
  margin-bottom: 16px;
}

.chart-header__title {
  color: var(--erp-slate-700);
  font-size: 14px;
  font-weight: 600;
}

.chart-header__desc {
  margin-top: 4px;
  color: var(--erp-slate-500);
  font-size: 12px;
}

.chart-container {
  width: 100%;
}

.chart-instance {
  width: 100%;
  height: 350px;
}

.data-table-card {
  .table-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 16px;
  }

  .table-header__title {
    color: var(--erp-slate-700);
    font-size: 14px;
    font-weight: 600;
  }
}

.data-table {
  :deep(.el-table__header-wrapper th) {
    background: var(--erp-slate-50);
    color: var(--erp-slate-600);
    font-size: 12px;
    font-weight: 600;
  }
}

@media (max-width: 1024px) {
  .chart-grid {
    grid-template-columns: 1fr;
  }

  .chart-card--full {
    grid-column: span 1;
  }
}
</style>
