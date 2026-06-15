<template>
  <div class="cost-product-page">
    <!-- 页头 -->
    <ContentWrap class="page-header-card">
      <div class="page-header">
        <div class="page-header__main">
          <div class="page-header__title">产品成本汇总</div>
          <div class="page-header__desc">按产品维度汇总料工费成本，支持按月份筛选</div>
        </div>
        <div class="page-header__actions">
          <el-date-picker
            v-model="accountingMonth"
            type="month"
            value-format="YYYY-MM"
            placeholder="选择月份"
            class="!w-150px"
            @change="loadData"
          />
          <el-button :loading="loading" @click="loadData">
            <Icon icon="ep:refresh" class="mr-5px" /> 刷新
          </el-button>
        </div>
      </div>
    </ContentWrap>

    <!-- 产品成本表格 -->
    <ContentWrap class="cost-table-card" title="产品成本明细">
      <el-table :data="list" v-loading="loading" stripe class="cost-table">
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
      </el-table>
    </ContentWrap>

    <!-- 汇总卡片 -->
    <ContentWrap class="summary-card" title="汇总统计">
      <div class="summary-grid">
        <div class="summary-item">
          <div class="summary-item__label">产品数量</div>
          <div class="summary-item__value">{{ list.length }}</div>
        </div>
        <div class="summary-item">
          <div class="summary-item__label">材料成本合计</div>
          <div class="summary-item__value">{{ formatMoney(totalMaterialCost) }}</div>
        </div>
        <div class="summary-item">
          <div class="summary-item__label">人工成本合计</div>
          <div class="summary-item__value">{{ formatMoney(totalLaborCost) }}</div>
        </div>
        <div class="summary-item">
          <div class="summary-item__label">制造费用合计</div>
          <div class="summary-item__value">{{ formatMoney(totalOverheadCost) }}</div>
        </div>
        <div class="summary-item summary-item--total">
          <div class="summary-item__label">总成本</div>
          <div class="summary-item__value">{{ formatMoney(totalCost) }}</div>
        </div>
      </div>
    </ContentWrap>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'

defineOptions({ name: 'ErpProductionCostProductSummary' })

const loading = ref(false)
const list = ref<any[]>([])
const accountingMonth = ref('')

// API
const CostApi = {
  getProductSummary: async (month?: string) => {
    const { request } = await import('@/config/axios')
    const params: any = {}
    if (month) params.accountingMonth = month
    return await request.get({ url: '/erp/production-cost/product-summary', params })
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

const loadData = async () => {
  loading.value = true
  try {
    list.value = await CostApi.getProductSummary(accountingMonth.value || undefined) || []
  } finally {
    loading.value = false
  }
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

.cost-table {
  :deep(.el-table__header-wrapper th) {
    background: var(--erp-slate-50);
    color: var(--erp-slate-600);
    font-weight: 600;
    font-size: 12px;
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

.summary-card {
  .summary-grid {
    display: flex;
    gap: 16px;
    flex-wrap: wrap;
  }

  .summary-item {
    flex: 1;
    min-width: 140px;
    padding: 16px;
    background: var(--erp-slate-50);
    border-radius: 10px;
    border: 1px solid var(--erp-slate-100);
  }

  .summary-item--total {
    background: var(--erp-primary-50);
    border-color: var(--erp-primary-200);
  }

  .summary-item__label {
    font-size: 12px;
    color: var(--erp-slate-500);
    margin-bottom: 8px;
  }

  .summary-item__value {
    font-size: 18px;
    font-weight: 700;
    color: var(--erp-slate-900);
    font-variant-numeric: tabular-nums;
    font-family: 'Inter', 'SF Mono', monospace;
  }

  .summary-item--total .summary-item__value {
    color: var(--erp-primary-600);
    font-size: 22px;
  }
}
</style>
