<template>
  <div class="cost-report-page">
    <!-- 页头 -->
    <ContentWrap class="page-header-card">
      <div class="page-header">
        <div class="page-header__main">
          <div class="page-header__title">产品成本分析报表</div>
        </div>
        <div class="page-header__actions">
          <el-button :loading="loading" @click="loadData">
            <Icon icon="ep:refresh" class="mr-5px" /> 刷新
          </el-button>
        </div>
      </div>
    </ContentWrap>

    <!-- 筛选区 -->
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
          <el-button type="primary" :loading="loading" @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" /> 查询
          </el-button>
          <el-button @click="resetQuery">
            <Icon icon="ep:refresh-left" class="mr-5px" /> 重置
          </el-button>
        </div>
      </el-form>
    </ContentWrap>

    <!-- 图表区 -->
    <div class="chart-grid">
      <!-- 趋势图 -->
      <ContentWrap class="chart-card">
        <div class="chart-header">
          <div class="chart-header__title">成本趋势分析</div>
          <div class="chart-header__desc">展示产品成本随时间变化趋势</div>
        </div>
        <div class="chart-container">
          <div ref="trendChartRef" class="chart-instance"></div>
        </div>
      </ContentWrap>

      <!-- 构成饼图 -->
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

    <!-- 对比图 -->
    <ContentWrap class="chart-card chart-card--full">
      <div class="chart-header">
        <div class="chart-header__title">产品成本对比</div>
        <div class="chart-header__desc">对比不同产品同一期间的成本</div>
      </div>
      <div class="chart-container">
        <div ref="compareChartRef" class="chart-instance"></div>
      </div>
    </ContentWrap>

    <!-- 数据表格 -->
    <ContentWrap class="data-table-card">
      <div class="table-header">
        <div class="table-header__title">成本数据明细</div>
        <div class="table-header__actions">
          <el-button type="primary" plain @click="handleExport">
            <Icon icon="ep:download" class="mr-5px" /> 导出报表
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
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import request from '@/config/axios'

defineOptions({ name: 'ErpFinanceCostReport' })

const loading = ref(false)
const productOptions = ref<any[]>([])
const tableData = ref<any[]>([])

// 查询参数
const queryParams = reactive({
  productId: undefined as number | undefined,
  startMonth: '2026-01',
  endMonth: '2026-06',
  dimension: 'month'
})

// 图表实例
const trendChartRef = ref<HTMLElement>()
const compositionChartRef = ref<HTMLElement>()
const compareChartRef = ref<HTMLElement>()
let trendChart: echarts.ECharts | null = null
let compositionChart: echarts.ECharts | null = null
let compareChart: echarts.ECharts | null = null

// 趋势数据
const trendData = ref<any>({})

const formatMoney = (value?: number | string | null) => {
  const n = Number(value || 0)
  return `¥${n.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

const formatCount = (value?: number | string | null) => {
  const n = Number(value || 0)
  if (n === 0) return '-'
  return n.toLocaleString('zh-CN', { maximumFractionDigits: 3 })
}

// 加载产品列表（用于筛选下拉）
const loadProductOptions = async () => {
  try {
    // 查询有生产成本数据的产品
    const data = await request.get({ 
      url: '/erp/production-cost-entry/product-summary',
      params: {}
    })
    productOptions.value = data || []
  } catch (e) {
    console.error('加载产品列表失败', e)
  }
}

// 加载趋势数据
const loadTrendData = async () => {
  const params: any = {
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
  if (!trendChartRef.value || !trendData.value.periods) return
  trendChart = echarts.init(trendChartRef.value)
  const option = {
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
  }
  trendChart.setOption(option)
}

const initCompositionChart = () => {
  if (!compositionChartRef.value || !trendData.value.compositionData) return
  compositionChart = echarts.init(compositionChartRef.value)
  const comp = trendData.value.compositionData
  const pieData = [
    { name: '直接材料', value: Number(comp.materialCost || 0), itemStyle: { color: '#3B82F6' } },
    { name: '直接人工', value: Number(comp.laborCost || 0), itemStyle: { color: '#10B981' } },
    { name: '折旧', value: Number(comp.depreciationCost || 0), itemStyle: { color: '#F59E0B' } },
    { name: '电费', value: Number(comp.powerCost || 0), itemStyle: { color: '#06B6D4' } },
    { name: '其他', value: Number(comp.otherCost || 0), itemStyle: { color: '#14B8A6' } }
  ]
  const option = {
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
        data: pieData
      }
    ]
  }
  compositionChart.setOption(option)
}

const initCompareChart = () => {
  if (!compareChartRef.value || !trendData.value.productCompareData) return
  compareChart = echarts.init(compareChartRef.value)
  const compareData = trendData.value.productCompareData
  const option = {
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
      data: compareData.map((item: any) => item.productName || `产品${item.productId}`)
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
        data: compareData.map((item: any) => Number(item.materialCost || 0)),
        itemStyle: { color: '#3B82F6' }
      },
      {
        name: '直接人工',
        type: 'bar',
        stack: 'Total',
        emphasis: { focus: 'series' },
        data: compareData.map((item: any) => Number(item.laborCost || 0)),
        itemStyle: { color: '#10B981' }
      },
      {
        name: '制造费用',
        type: 'bar',
        stack: 'Total',
        emphasis: { focus: 'series' },
        data: compareData.map((item: any) => Number(item.overheadCost || 0)),
        itemStyle: { color: '#F59E0B' }
      }
    ]
  }
  compareChart.setOption(option)
}

const buildTableData = () => {
  if (!trendData.value.periods) return
  const periods = trendData.value.periods
  const rows: any[] = []

  // 如果没有选择具体产品，按产品维度展示每个产品的数据
  if (!queryParams.productId) {
    // 从compareData中获取各产品的数据
    const compareData = trendData.value.productCompareData || []
    for (const product of compareData) {
      rows.push({
        period: `${queryParams.startMonth} ~ ${queryParams.endMonth}`,
        productName: product.productName || `产品${product.productId}`,
        orderNo: null,
        materialCost: Number(product.materialCost || 0),
        laborCost: Number(product.laborCost || 0),
        depreciationCost: Number(product.overheadCost || 0) * 0.4,
        powerCost: Number(product.overheadCost || 0) * 0.3,
        otherCost: Number(product.overheadCost || 0) * 0.3,
        totalCost: Number(product.materialCost || 0) + Number(product.laborCost || 0) + Number(product.overheadCost || 0)
      })
    }
  } else {
    // 选择了具体产品，展示该产品按月的成本趋势
    const orderData = trendData.value.productOrderData || {}
    for (let i = 0; i < periods.length; i++) {
      const period = periods[i]
      const orderInfo = orderData[period] || {}
      rows.push({
        period: period,
        productName: productOptions.value.find(p => p.productId === queryParams.productId)?.productName || `产品${queryParams.productId}`,
        orderNo: orderInfo.orderNo || null,
        materialCost: trendData.value.materialCosts?.[i] || 0,
        laborCost: trendData.value.laborCosts?.[i] || 0,
        depreciationCost: trendData.value.depreciationCosts?.[i] || 0,
        powerCost: trendData.value.powerCosts?.[i] || 0,
        otherCost: trendData.value.otherCosts?.[i] || 0,
        totalCost: trendData.value.totalCosts?.[i] || 0
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
  } catch (e) {
    console.error('加载数据失败', e)
  } finally {
    loading.value = false
  }
}

const handleExport = () => {
  // TODO: 实现导出功能
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
  trendChart?.dispose()
  compositionChart?.dispose()
  compareChart?.dispose()
})
</script>

<style scoped lang="scss">
.cost-report-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px;
  background: var(--erp-slate-50);
  min-height: 100vh;
}

.page-header-card {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
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
    align-items: center;
  }
}

.filter-card {
  .filter-header {
    margin-bottom: 16px;
  }
  .filter-header__title {
    font-size: 14px;
    font-weight: 600;
    color: var(--erp-slate-700);
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
  font-size: 14px;
  font-weight: 600;
  color: var(--erp-slate-700);
}

.chart-header__desc {
  font-size: 12px;
  color: var(--erp-slate-500);
  margin-top: 4px;
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
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
  }
  .table-header__title {
    font-size: 14px;
    font-weight: 600;
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
    grid-column: span 1;
  }
}
</style>
