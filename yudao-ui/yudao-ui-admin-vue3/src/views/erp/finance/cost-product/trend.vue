<template>
  <div class="cost-trend-page">
    <!-- 页头 -->
    <ContentWrap class="page-header-card">
      <div class="page-header">
        <div class="page-header__main">
          <div class="page-header__title">产品成本趋势分析</div>
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
          <el-button type="primary" :loading="loading" @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" /> 查询
          </el-button>
          <el-button @click="resetQuery">
            <Icon icon="ep:refresh-left" class="mr-5px" /> 重置
          </el-button>
        </div>
      </el-form>
    </ContentWrap>

    <!-- 趋势图卡片 -->
    <div class="chart-row">
      <ContentWrap class="chart-card chart-card--main">
        <div class="chart-header">
          <div class="chart-header__title">成本趋势</div>
          <div class="chart-header__legend">
            <span class="legend-dot" style="background: #3B82F6"></span> 直接材料
            <span class="legend-dot" style="background: #10B981"></span> 直接人工
            <span class="legend-dot" style="background: #F59E0B"></span> 制造费用
            <span class="legend-dot" style="background: #EF4444"></span> 总成本
          </div>
        </div>
        <div class="chart-container" ref="trendChartRef"></div>
      </ContentWrap>

      <ContentWrap class="chart-card chart-card--side">
        <div class="chart-header">
          <div class="chart-header__title">成本构成</div>
        </div>
        <div class="chart-container" ref="pieChartRef"></div>
        <div class="composition-legend">
          <div v-for="(item, index) in avgComposition" :key="index" class="legend-item">
            <div class="legend-dot" :style="{ backgroundColor: item.color }"></div>
            <div class="legend-label">{{ item.label }}</div>
            <div class="legend-value">{{ item.percentage }}%</div>
          </div>
        </div>
      </ContentWrap>
    </div>

    <!-- 数据表格 -->
    <ContentWrap class="cost-table-card">
      <div class="table-header">
        <div class="table-header__title">月度成本明细</div>
        <div class="table-header__count">当前共 <strong>{{ trendData.length }}</strong> 条</div>
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
            <span v-if="row.momChange !== null" :class="row.momChange > 0 ? 'text-red-500' : 'text-emerald-500'">
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
import { ref, reactive, computed, onMounted, nextTick, watch, onUnmounted } from 'vue'
import * as echarts from 'echarts'

defineOptions({ name: 'ErpProductCostTrend' })

const loading = ref(false)
const trendData = ref<any[]>([])
const productList = ref<any[]>([])

const queryParams = reactive({
  productId: undefined as number | undefined,
  compareProductIds: [] as number[]
})

const dateRange = ref<[string, string]>([
  (() => {
    const d = new Date()
    d.setMonth(d.getMonth() - 5)
    return d.toISOString().slice(0, 7)
  })(),
  new Date().toISOString().slice(0, 7)
])

// Chart refs
const trendChartRef = ref<HTMLElement>()
const pieChartRef = ref<HTMLElement>()
let trendChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null

// API
const CostApi = {
  getProductSummaryV2: async (params: any) => {
    const { request } = await import('@/config/axios')
    return await request.get({ url: '/erp/production-cost-entry/product-summary-v2', params })
  },
  getCostProductList: async () => {
    const { request } = await import('@/config/axios')
    return await request.get({ url: '/erp/production-cost-entry/cost-products' })
  }
}

// 平均成本构成
const avgComposition = computed(() => {
  if (trendData.value.length === 0) return []
  const totals = trendData.value.reduce(
    (acc, item) => ({
      material: acc.material + Number(item.materialCost || 0),
      labor: acc.labor + Number(item.laborCost || 0),
      depreciation: acc.depreciation + Number(item.depreciationCost || 0),
      power: acc.power + Number(item.powerCost || 0),
      other: acc.other + Number(item.otherCost || 0)
    }),
    { material: 0, labor: 0, depreciation: 0, power: 0, other: 0 }
  )
  const total = totals.material + totals.labor + totals.depreciation + totals.power + totals.other
  if (total === 0) return []

  const colors = ['#3B82F6', '#10B981', '#F59E0B', '#06B6D4', '#14B8A6']
  const items = [
    { label: '直接材料', value: totals.material, color: colors[0] },
    { label: '直接人工', value: totals.labor, color: colors[1] },
    { label: '折旧', value: totals.depreciation, color: colors[2] },
    { label: '电费', value: totals.power, color: colors[3] },
    { label: '其他', value: totals.other, color: colors[4] }
  ]

  return items.map(item => ({
    ...item,
    percentage: Math.round((item.value / total) * 100)
  }))
})

const formatMoney = (value?: number | string | null) => {
  const n = Number(value || 0)
  return `¥${n.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

const handleQuery = () => {
  loadData()
}

const resetQuery = () => {
  queryParams.productId = undefined
  queryParams.compareProductIds = []
  const d = new Date()
  d.setMonth(d.getMonth() - 5)
  dateRange.value = [d.toISOString().slice(0, 7), new Date().toISOString().slice(0, 7)]
  loadData()
}

const loadData = async () => {
  if (!dateRange.value || !dateRange.value[0] || !dateRange.value[1]) return

  loading.value = true
  try {
    const [startMonth, endMonth] = dateRange.value
    const months = generateMonths(startMonth, endMonth)

    // 查询所有月份的数据
    const allData: any[] = []
    for (const month of months) {
      const params: any = { accountingMonth: month }
      if (queryParams.productId) params.productId = queryParams.productId
      const data = await CostApi.getProductSummaryV2(params)
      if (data) {
        data.forEach((item: any) => {
          allData.push({ ...item, accountingMonth: month })
        })
      }
    }

    // 计算环比
    const sortedMonths = [...months].sort()
    trendData.value = allData.sort((a, b) => {
      if (a.accountingMonth !== b.accountingMonth) return a.accountingMonth.localeCompare(b.accountingMonth)
      return (a.productId || 0) - (b.productId || 0)
    })

    // 计算环比变化
    for (let i = 0; i < trendData.value.length; i++) {
      const current = trendData.value[i]
      const prevMonth = getPreviousMonth(current.accountingMonth)
      const prev = trendData.value.find(
        (item: any) => item.accountingMonth === prevMonth && item.productId === current.productId
      )
      if (prev && Number(prev.totalCost) > 0) {
        current.momChange = Math.round(
          ((Number(current.totalCost) - Number(prev.totalCost)) / Number(prev.totalCost)) * 100
        )
      } else {
        current.momChange = null
      }
    }

    await nextTick()
    renderTrendChart()
    renderPieChart()
  } finally {
    loading.value = false
  }
}

const generateMonths = (start: string, end: string): string[] => {
  const months: string[] = []
  let current = new Date(start + '-01')
  const endDate = new Date(end + '-01')
  while (current <= endDate) {
    months.push(current.toISOString().slice(0, 7))
    current.setMonth(current.getMonth() + 1)
  }
  return months
}

const getPreviousMonth = (month: string): string => {
  const d = new Date(month + '-01')
  d.setMonth(d.getMonth() - 1)
  return d.toISOString().slice(0, 7)
}

const renderTrendChart = () => {
  if (!trendChartRef.value) return

  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value)
  }

  const months = [...new Set(trendData.value.map((item: any) => item.accountingMonth))].sort()

  // 按产品分组
  const productIds = queryParams.productId
    ? [queryParams.productId]
    : [...new Set(trendData.value.map((item: any) => item.productId))].slice(0, 5)

  const series: any[] = []
  const colors = ['#3B82F6', '#10B981', '#F59E0B', '#EF4444', '#06B6D4']

  productIds.forEach((productId, index) => {
    const productData = trendData.value.filter((item: any) => item.productId === productId)
    const productName = productData[0]?.productName || `产品${productId}`

    // 材料成本
    series.push({
      name: `${productName} - 材料`,
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      lineStyle: { width: 2, color: colors[index % colors.length] },
      itemStyle: { color: colors[index % colors.length] },
      data: months.map(month => {
        const item = productData.find((d: any) => d.accountingMonth === month)
        return item ? Number(item.materialCost || 0) : null
      })
    })

    // 总成本（虚线）
    series.push({
      name: `${productName} - 总成本`,
      type: 'line',
      smooth: true,
      symbol: 'diamond',
      symbolSize: 8,
      lineStyle: { width: 2, type: 'dashed', color: colors[index % colors.length] },
      itemStyle: { color: colors[index % colors.length] },
      data: months.map(month => {
        const item = productData.find((d: any) => d.accountingMonth === month)
        return item ? Number(item.totalCost || 0) : null
      })
    })
  })

  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        if (!params || params.length === 0) return ''
        let html = `<div style="font-weight:600;margin-bottom:4px">${params[0].axisValue}</div>`
        params.forEach((p: any) => {
          html += `<div>${p.marker} ${p.seriesName}: ¥${Number(p.value || 0).toLocaleString()}</div>`
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
        formatter: (val: number) => `¥${(val / 1000).toFixed(0)}k`
      }
    },
    series
  }

  trendChart.setOption(option, true)
}

const renderPieChart = () => {
  if (!pieChartRef.value || avgComposition.value.length === 0) return

  if (!pieChart) {
    pieChart = echarts.init(pieChartRef.value)
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
          borderRadius: 6,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: { show: false },
        emphasis: {
          label: { show: true, fontSize: 14, fontWeight: 'bold' }
        },
        data: avgComposition.value.map(item => ({
          value: item.value,
          name: item.label,
          itemStyle: { color: item.color }
        }))
      }
    ]
  }

  pieChart.setOption(option, true)
}

// 响应式调整
const handleResize = () => {
  trendChart?.resize()
  pieChart?.resize()
}

onMounted(async () => {
  // 加载有成本记录的产品列表
  // 注意：必须用 getCostProductList（只返回有成本记录的成品），禁止换成 getProductSimpleList（会混入物料）
  try {
    productList.value = await CostApi.getCostProductList()
  } catch {}

  await loadData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  pieChart?.dispose()
})

watch(() => dateRange.value, () => {
  if (dateRange.value && dateRange.value[0] && dateRange.value[1]) {
    loadData()
  }
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
  }
}

.filter-card {
  .filter-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
    gap: 16px;
  }
  .filter-actions {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
    margin-top: 16px;
  }
}

.chart-row {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 16px;
}

.chart-card {
  background: white;
  border-radius: 10px;
  border: 1px solid var(--erp-slate-100);
  padding: 16px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.chart-header__title {
  font-size: 14px;
  font-weight: 600;
  color: var(--erp-slate-700);
}

.chart-header__legend {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: var(--erp-slate-500);
}

.legend-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 2px;
  margin-right: 4px;
}

.chart-container {
  width: 100%;
  height: 300px;
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

.cost-table-card {
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
  .table-header__count {
    font-size: 13px;
    color: var(--erp-slate-500);
  }
}

.cost-table {
  :deep(.el-table__header-wrapper th) {
    background: var(--erp-slate-50);
    color: var(--erp-slate-600);
    font-weight: 600;
    font-size: 12px;
  }
}
</style>
