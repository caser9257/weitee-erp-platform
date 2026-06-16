<template>
  <div class="cost-product-page">
    <!-- 页头 -->
    <ContentWrap class="page-header-card">
      <div class="page-header">
        <div class="page-header__main">
          <div class="page-header__title">产品成本汇总</div>
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
          <el-form-item label="归集月份">
            <el-date-picker
              v-model="queryParams.accountingMonth"
              type="month"
              value-format="YYYY-MM"
              placeholder="选择月份"
              class="!w-full"
              @change="handleQuery"
            />
          </el-form-item>
          <el-form-item label="产品名称">
            <el-input
              v-model="queryParams.productName"
              placeholder="请输入产品名称"
              clearable
              class="!w-full"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="产品编号">
            <el-input
              v-model="queryParams.productNo"
              placeholder="请输入产品编号"
              clearable
              class="!w-full"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="工单编号">
            <el-input
              v-model="queryParams.productionOrderNo"
              placeholder="请输入工单编号"
              clearable
              class="!w-full"
              @keyup.enter="handleQuery"
            />
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

    <!-- KPI 指标卡 -->
    <div class="kpi-grid">
      <div class="kpi-card">
        <div class="kpi-card__icon kpi-card__icon--primary">
          <Icon icon="ep:goods" />
        </div>
        <div class="kpi-card__content">
          <div class="kpi-card__label">产品数量</div>
          <div class="kpi-card__value">{{ list.length }}</div>
        </div>
      </div>
      <div class="kpi-card">
        <div class="kpi-card__icon kpi-card__icon--warning">
          <Icon icon="ep:box" />
        </div>
        <div class="kpi-card__content">
          <div class="kpi-card__label">材料成本合计</div>
          <div class="kpi-card__value">{{ formatMoney(totalMaterialCost) }}</div>
        </div>
      </div>
      <div class="kpi-card">
        <div class="kpi-card__icon kpi-card__icon--success">
          <Icon icon="ep:user" />
        </div>
        <div class="kpi-card__content">
          <div class="kpi-card__label">人工成本合计</div>
          <div class="kpi-card__value">{{ formatMoney(totalLaborCost) }}</div>
        </div>
      </div>
      <div class="kpi-card">
        <div class="kpi-card__icon kpi-card__icon--info">
          <Icon icon="ep:setting" />
        </div>
        <div class="kpi-card__content">
          <div class="kpi-card__label">制造费用合计</div>
          <div class="kpi-card__value">{{ formatMoney(totalOverheadCost) }}</div>
        </div>
      </div>
      <div class="kpi-card kpi-card--total">
        <div class="kpi-card__icon kpi-card__icon--danger">
          <Icon icon="ep:money" />
        </div>
        <div class="kpi-card__content">
          <div class="kpi-card__label">总成本</div>
          <div class="kpi-card__value">{{ formatMoney(totalCost) }}</div>
        </div>
      </div>
    </div>

    <!-- 产品成本表格 -->
    <ContentWrap class="cost-table-card">
      <div class="table-header">
        <div class="table-header__title">产品成本明细</div>
        <div class="table-header__count">当前共 <strong>{{ list.length }}</strong> 条</div>
      </div>
      <el-table
        :data="list"
        v-loading="loading"
        stripe
        class="cost-table"
        @row-click="handleRowClick"
      >
        <el-table-column label="产品信息" min-width="200">
          <template #default="{ row }">
            <div class="product-cell">
              <div class="product-cell__name">{{ row.productName || '-' }}</div>
              <div class="product-cell__meta">
                <span class="font-mono text-slate-400">ID: {{ row.productId }}</span>
                <span v-if="row.productSpec">规格：{{ row.productSpec }}</span>
                <span v-if="row.productUnit">单位：{{ row.productUnit }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="关联" width="120" align="center">
          <template #default="{ row }">
            <div class="relation-cell">
              <div>{{ row.projectCount }} 个项目</div>
              <div class="text-slate-400 text-xs">{{ row.productionOrderCount }} 个工单</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="总工时" width="100" align="right">
          <template #default="{ row }">
            <span class="font-mono">{{ formatNumber(row.totalManHour) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="直接材料" min-width="120" align="right">
          <template #default="{ row }">
            <span class="font-mono font-semibold">{{ formatMoney(row.materialCost) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="直接人工" min-width="120" align="right">
          <template #default="{ row }">
            <span class="font-mono">{{ formatMoney(row.laborCost) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="折旧" min-width="100" align="right">
          <template #default="{ row }">
            <span class="font-mono">{{ formatMoney(row.depreciationCost) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="电费" min-width="100" align="right">
          <template #default="{ row }">
            <span class="font-mono">{{ formatMoney(row.powerCost) }}</span>
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
        <el-table-column label="完工数量" width="100" align="right">
          <template #default="{ row }">
            <span class="font-mono">{{ formatCount(row.outputQty) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="单位成本" min-width="120" align="right">
          <template #default="{ row }">
            <span class="font-mono font-semibold text-blue-600">{{ formatMoney(row.unitCost) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" align="center" width="100">
          <template #default="{ row }">
            <el-button link type="primary" @click.stop="handleViewDetail(row)">
              查看明细
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </ContentWrap>

    <!-- 明细 Drawer -->
    <el-drawer
      v-model="detailDrawerVisible"
      size="600px"
      destroy-on-close
      :with-header="false"
      :modal-class="'cost-product-drawer-mask'"
      @closed="clearDetailDrawer"
    >
      <div class="detail-drawer">
        <!-- 上下文卡片 -->
        <div class="context-card">
          <div class="context-card__main">
            <div class="context-card__title">{{ detailData?.productName || '-' }}</div>
            <div class="context-card__subtitle">ID: {{ detailData?.productId || '-' }}</div>
          </div>
          <div class="context-card__meta">
            <span class="context-card__chip">{{ detailData?.productSpec || '-' }}</span>
            <span class="context-card__chip">{{ detailData?.productUnit || '-' }}</span>
          </div>
          <el-button class="context-card__close" link @click="detailDrawerVisible = false">
            <Icon icon="ep:close" />
          </el-button>
        </div>

        <!-- 指标卡 -->
        <div class="detail-metrics">
          <div class="metric-item">
            <div class="metric-item__label">材料成本</div>
            <div class="metric-item__value">{{ formatMoney(detailData?.materialCost) }}</div>
          </div>
          <div class="metric-item">
            <div class="metric-item__label">人工成本</div>
            <div class="metric-item__value">{{ formatMoney(detailData?.laborCost) }}</div>
          </div>
          <div class="metric-item">
            <div class="metric-item__label">折旧</div>
            <div class="metric-item__value">{{ formatMoney(detailData?.depreciationCost) }}</div>
          </div>
          <div class="metric-item">
            <div class="metric-item__label">电费</div>
            <div class="metric-item__value">{{ formatMoney(detailData?.powerCost) }}</div>
          </div>
          <div class="metric-item">
            <div class="metric-item__label">其他制造费</div>
            <div class="metric-item__value">{{ formatMoney(detailData?.otherCost) }}</div>
          </div>
          <div class="metric-item metric-item--total">
            <div class="metric-item__label">成本合计</div>
            <div class="metric-item__value">{{ formatMoney(detailData?.totalCost) }}</div>
          </div>
          <div class="metric-item">
            <div class="metric-item__label">完工数量</div>
            <div class="metric-item__value">{{ formatCount(detailData?.outputQty) }}</div>
          </div>
          <div class="metric-item">
            <div class="metric-item__label">单位成本</div>
            <div class="metric-item__value">{{ formatMoney(detailData?.unitCost) }}</div>
          </div>
        </div>

        <!-- 成本构成饼图 -->
        <div class="detail-section">
          <div class="detail-section__title">成本构成</div>
          <div class="cost-composition">
            <div class="composition-chart">
              <div
                v-for="(item, index) in costComposition"
                :key="index"
                class="composition-bar"
                :style="{ width: item.percentage + '%', backgroundColor: item.color }"
                :title="`${item.label}: ${item.percentage}%`"
              ></div>
            </div>
            <div class="composition-legend">
              <div v-for="(item, index) in costComposition" :key="index" class="legend-item">
                <div class="legend-dot" :style="{ backgroundColor: item.color }"></div>
                <div class="legend-label">{{ item.label }}</div>
                <div class="legend-value">{{ formatMoney(item.value) }}</div>
                <div class="legend-percentage">{{ item.percentage }}%</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 关联信息 -->
        <div class="detail-section">
          <div class="detail-section__title">关联信息</div>
          <div class="detail-info">
            <div class="info-item">
              <div class="info-item__label">关联项目数</div>
              <div class="info-item__value">{{ detailData?.projectCount || 0 }}</div>
            </div>
            <div class="info-item">
              <div class="info-item__label">关联工单数</div>
              <div class="info-item__value">{{ detailData?.productionOrderCount || 0 }}</div>
            </div>
            <div class="info-item">
              <div class="info-item__label">总工时</div>
              <div class="info-item__value">{{ formatNumber(detailData?.totalManHour) }}</div>
            </div>
          </div>
        </div>

        <!-- 底部操作栏 -->
        <div class="detail-footer">
          <el-button @click="detailDrawerVisible = false">关闭</el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'

defineOptions({ name: 'ErpProductionCostProductSummary' })

const loading = ref(false)
const list = ref<any[]>([])

// 查询参数
const queryParams = reactive({
  accountingMonth: '',
  productName: '',
  productNo: '',
  productionOrderNo: ''
})

// 明细抽屉
const detailDrawerVisible = ref(false)
const detailData = ref<any>(null)

// API
const CostApi = {
  getProductSummaryV2: async (params: any) => {
    const { request } = await import('@/config/axios')
    return await request.get({ url: '/erp/production-cost-entry/product-summary-v2', params })
  }
}

// 汇总计算
const totalMaterialCost = computed(() =>
  list.value.reduce((sum, item) => sum + Number(item.materialCost || 0), 0)
)
const totalLaborCost = computed(() =>
  list.value.reduce((sum, item) => sum + Number(item.laborCost || 0), 0)
)
const totalOverheadCost = computed(() =>
  list.value.reduce((sum, item) =>
    sum + Number(item.depreciationCost || 0) + Number(item.powerCost || 0) + Number(item.otherCost || 0), 0)
)
const totalCost = computed(() =>
  list.value.reduce((sum, item) => sum + Number(item.totalCost || 0), 0)
)

// 成本构成
const costComposition = computed(() => {
  if (!detailData.value) return []
  const material = Number(detailData.value.materialCost || 0)
  const labor = Number(detailData.value.laborCost || 0)
  const depreciation = Number(detailData.value.depreciationCost || 0)
  const power = Number(detailData.value.powerCost || 0)
  const other = Number(detailData.value.otherCost || 0)
  const total = material + labor + depreciation + power + other

  if (total === 0) return []

  const colors = ['#3B82F6', '#10B981', '#F59E0B', '#8B5CF6', '#EC4899']
  const items = [
    { label: '直接材料', value: material, color: colors[0] },
    { label: '直接人工', value: labor, color: colors[1] },
    { label: '折旧', value: depreciation, color: colors[2] },
    { label: '电费', value: power, color: colors[3] },
    { label: '其他制造费', value: other, color: colors[4] }
  ]

  return items.map(item => ({
    ...item,
    percentage: total > 0 ? Math.round((item.value / total) * 100) : 0
  }))
})

const formatMoney = (value?: number | string | null) => {
  const n = Number(value || 0)
  return `¥${n.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

const formatNumber = (value?: number | string | null) => {
  const n = Number(value || 0)
  return n.toLocaleString('zh-CN', { maximumFractionDigits: 2 })
}

const formatCount = (value?: number | string | null) => {
  const n = Number(value || 0)
  if (n === 0) return '-'
  return n.toLocaleString('zh-CN', { maximumFractionDigits: 3 })
}

const handleQuery = () => {
  loadData()
}

const resetQuery = () => {
  queryParams.accountingMonth = ''
  queryParams.productName = ''
  queryParams.productNo = ''
  queryParams.productionOrderNo = ''
  loadData()
}

const loadData = async () => {
  loading.value = true
  try {
    const params: any = {}
    if (queryParams.accountingMonth) params.accountingMonth = queryParams.accountingMonth
    if (queryParams.productName) params.productName = queryParams.productName
    if (queryParams.productNo) params.productNo = queryParams.productNo
    if (queryParams.productionOrderNo) params.productionOrderNo = queryParams.productionOrderNo
    list.value = await CostApi.getProductSummaryV2(params) || []
  } finally {
    loading.value = false
  }
}

const handleRowClick = (row: any) => {
  handleViewDetail(row)
}

const handleViewDetail = (row: any) => {
  detailData.value = row
  detailDrawerVisible.value = true
}

const clearDetailDrawer = () => {
  detailData.value = null
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.cost-product-page {
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

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 16px;
}

.kpi-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: white;
  border-radius: 10px;
  border: 1px solid var(--erp-slate-100);
  transition: all 0.2s;

  &:hover {
    box-shadow: var(--erp-shadow-sm);
  }

  &--total {
    background: var(--erp-primary-50);
    border-color: var(--erp-primary-200);
  }
}

.kpi-card__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 10px;
  font-size: 18px;

  &--primary {
    background: var(--erp-primary-50);
    color: var(--erp-primary-600);
  }
  &--success {
    background: var(--erp-success-50);
    color: var(--erp-success-600);
  }
  &--warning {
    background: var(--erp-warning-50);
    color: var(--erp-warning-600);
  }
  &--danger {
    background: var(--erp-danger-50);
    color: var(--erp-danger-600);
  }
  &--info {
    background: var(--erp-info-50);
    color: var(--erp-info-600);
  }
}

.kpi-card__content {
  flex: 1;
  min-width: 0;
}

.kpi-card__label {
  font-size: 12px;
  color: var(--erp-slate-500);
  margin-bottom: 4px;
}

.kpi-card__value {
  font-size: 18px;
  font-weight: 700;
  color: var(--erp-slate-900);
  font-variant-numeric: tabular-nums;
  font-family: 'Inter', 'SF Mono', monospace;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.kpi-card--total .kpi-card__value {
  color: var(--erp-primary-600);
  font-size: 20px;
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
  :deep(.el-table__row) {
    cursor: pointer;
  }
}

.product-cell__name {
  font-weight: 600;
  color: var(--erp-slate-900);
}

.product-cell__meta {
  display: flex;
  gap: 8px;
  margin-top: 4px;
  font-size: 11px;
  color: var(--erp-slate-500);
}

.relation-cell {
  font-size: 13px;
}

/* Drawer 样式 */
.cost-product-drawer-mask {
  backdrop-filter: blur(4px);
}

.detail-drawer {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--erp-slate-50);
}

.context-card {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 20px;
  background: linear-gradient(135deg, var(--erp-slate-900) 0%, var(--erp-primary-800) 100%);
  color: white;
}

.context-card__main {
  flex: 1;
}

.context-card__title {
  font-size: 20px;
  font-weight: 700;
}

.context-card__subtitle {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
  margin-top: 4px;
}

.context-card__meta {
  display: flex;
  gap: 8px;
}

.context-card__chip {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 6px;
  font-size: 12px;
}

.context-card__close {
  position: absolute;
  top: 12px;
  right: 12px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 18px;

  &:hover {
    color: white;
  }
}

.detail-metrics {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  padding: 16px;
  background: white;
}

.metric-item {
  padding: 12px;
  background: var(--erp-slate-50);
  border-radius: 8px;
  border: 1px solid var(--erp-slate-100);

  &--total {
    background: var(--erp-primary-50);
    border-color: var(--erp-primary-200);
    grid-column: span 2;
  }
}

.metric-item__label {
  font-size: 12px;
  color: var(--erp-slate-500);
  margin-bottom: 4px;
}

.metric-item__value {
  font-size: 16px;
  font-weight: 600;
  color: var(--erp-slate-900);
  font-variant-numeric: tabular-nums;
  font-family: 'Inter', 'SF Mono', monospace;
}

.metric-item--total .metric-item__value {
  color: var(--erp-primary-600);
  font-size: 20px;
}

.detail-section {
  padding: 16px;
  background: white;
  border-top: 1px solid var(--erp-slate-100);
}

.detail-section__title {
  font-size: 14px;
  font-weight: 600;
  color: var(--erp-slate-700);
  margin-bottom: 12px;
}

.cost-composition {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.composition-chart {
  display: flex;
  height: 8px;
  border-radius: 4px;
  overflow: hidden;
  background: var(--erp-slate-100);
}

.composition-bar {
  height: 100%;
  transition: width 0.3s ease;
}

.composition-legend {
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

.legend-dot {
  width: 8px;
  height: 8px;
  border-radius: 2px;
}

.legend-label {
  flex: 1;
  color: var(--erp-slate-600);
}

.legend-value {
  font-weight: 600;
  color: var(--erp-slate-900);
  font-variant-numeric: tabular-nums;
  font-family: 'Inter', 'SF Mono', monospace;
}

.legend-percentage {
  width: 40px;
  text-align: right;
  color: var(--erp-slate-500);
  font-size: 12px;
}

.detail-info {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.info-item {
  padding: 12px;
  background: var(--erp-slate-50);
  border-radius: 8px;
  text-align: center;
}

.info-item__label {
  font-size: 12px;
  color: var(--erp-slate-500);
  margin-bottom: 4px;
}

.info-item__value {
  font-size: 16px;
  font-weight: 600;
  color: var(--erp-slate-900);
  font-variant-numeric: tabular-nums;
  font-family: 'Inter', 'SF Mono', monospace;
}

.detail-footer {
  position: sticky;
  bottom: 0;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 12px 16px;
  border-top: 1px solid var(--erp-slate-200);
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(8px);
}
</style>
