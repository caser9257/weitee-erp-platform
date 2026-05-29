<template>
  <div class="cost-page-shell">
    <header class="cost-topbar">
      <div class="cost-topbar__inner">
        <div class="cost-topbar__brand">
          <div class="cost-topbar__brand-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 2v20M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6" />
            </svg>
          </div>
          <div>
            <h1 class="cost-topbar__title">生产成本归集核算系统</h1>
            <p class="cost-topbar__subtitle">制造核算与工单成本分析中心 v3.0</p>
          </div>
        </div>

        <div class="cost-topbar__metrics">
          <div class="metric-chip">
            <span class="metric-chip__label">当前条数</span>
            <span class="metric-chip__value">{{ costItems.length }} 项</span>
          </div>
          <div class="metric-chip">
            <span class="metric-chip__label">当前金额</span>
            <span class="metric-chip__value">{{ formatMoney(totalCostCollected) }}</span>
          </div>
          <div class="metric-chip">
            <span class="metric-chip__label">主测工单</span>
            <span class="metric-chip__plain">{{ primaryOrderNo }}</span>
          </div>
          <div class="metric-badge">核算月度: {{ displayMonth }}</div>
        </div>
      </div>
    </header>

    <main class="cost-main">
      <section class="cost-card cost-card--hero">
        <div class="hero-copy">
          <span class="hero-copy__badge">制造费用内控</span>
          <h2 class="hero-copy__title">生产工单成本合并稽核台</h2>
          <p class="hero-copy__desc">
            用于对量产工单进行直接人工、电力能源、固定资产折旧及分摊制造费用的归集复核，确保产品料工费分摊结转的合规性与准确性。
          </p>
        </div>
        <div class="hero-actions">
          <button class="btn btn--plain" :disabled="listLoading" @click="handleReset">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M4 4v5h.582m15.356 2A8.001 8.001 0 1 1 21.306 7M21 3v5h-5" />
            </svg>
            <span>重置</span>
          </button>
          <button class="btn btn--primary" :disabled="exportLoading" @click="handleExport">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M4 16v1a3 3 0 0 0 3 3h10a3 3 0 0 0 3-3v-1M12 2v12m0 0l-4-4m4 4l4-4" />
            </svg>
            <span>{{ exportLoading ? '导出中...' : '导出汇总明细' }}</span>
          </button>
        </div>
      </section>

      <section class="cost-card cost-card--filter">
        <div class="filter-grid">
          <div class="filter-field">
            <label class="filter-field__label">成本类型</label>
            <select v-model="costTypeFilter" class="filter-select">
              <option value="ALL">全部成本类型</option>
              <option v-for="item in costTypeOptions" :key="item.value" :value="String(item.value)">
                {{ item.label }}
              </option>
            </select>
          </div>

          <div class="filter-field">
            <label class="filter-field__label">归属月份</label>
            <select v-model="costMonthFilter" class="filter-select">
              <option value="ALL">全部归属月份</option>
              <option v-for="item in monthOptions" :key="item" :value="item">{{ item }}</option>
            </select>
          </div>

          <div class="filter-search">
            <div class="filter-search__input-wrap">
              <svg class="filter-search__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="11" cy="11" r="8" />
                <path d="m21 21-4.35-4.35" />
              </svg>
              <input
                v-model.trim="costSearchTerm"
                class="filter-search__input"
                type="text"
                placeholder="搜索工单编号、项目名称..."
                @keydown.enter="handleQuery"
              />
            </div>
            <button class="btn btn--primary btn--compact" :disabled="listLoading" @click="handleQuery">
              查询
            </button>
            <button class="btn btn--plain btn--compact" :disabled="listLoading" @click="resetFilters">
              清空
            </button>
          </div>
        </div>
      </section>

      <section class="cost-card cost-card--table">
        <div class="table-card__header">
          <h3 class="table-card__title">工单项目成本汇总表</h3>
        </div>

        <div v-if="listError" class="table-alert">
          <span>{{ listError }}</span>
          <button class="table-alert__btn" @click="getList">重新加载</button>
        </div>

        <div v-if="listLoading" class="table-loading">
          <div class="table-loading__spinner"></div>
          <span>成本数据加载中...</span>
        </div>

        <template v-else>
          <div v-if="aggregatedCosts.length" class="table-scroll custom-scrollbar">
            <table class="cost-table">
              <thead>
                <tr>
                  <th>工单编号</th>
                  <th>项目名称</th>
                  <th class="text-center">归属月份</th>
                  <th class="text-right">本期归集总成本</th>
                  <th class="text-center">合并数据项</th>
                  <th class="text-center">二级明细穿透</th>
                </tr>
              </thead>
              <tbody>
                <template v-for="item in aggregatedCosts" :key="item.key">
                  <tr class="cost-table__row">
                    <td>
                      <button class="order-link" type="button" @click="toggleCostExpand(item.orderNo)">
                        {{ item.orderNo }}
                      </button>
                    </td>
                    <td class="project-name">{{ item.projectName }}</td>
                    <td class="text-center month-text">{{ item.month }}</td>
                    <td class="text-right money-text">{{ formatMoney(item.totalAmount) }}</td>
                    <td class="text-center">
                      <span class="merge-chip">已聚合 {{ item.breakdown.length }} 类制造费用</span>
                    </td>
                    <td class="text-center">
                      <button
                        class="btn btn--table"
                        :class="{ 'btn--table-active': expandedCostOrders[item.orderNo] }"
                        @click="toggleCostExpand(item.orderNo)"
                      >
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <path v-if="!expandedCostOrders[item.orderNo]" d="M19 9l-7 7-7-7" />
                          <path v-else d="M5 15l7-7 7 7" />
                        </svg>
                        <span>{{ expandedCostOrders[item.orderNo] ? '收起' : '展开' }}</span>
                      </button>
                    </td>
                  </tr>

                  <tr v-if="expandedCostOrders[item.orderNo]" class="expand-row">
                    <td colspan="6" class="expand-row__cell">
                      <div class="expand-panel">
                        <div class="expand-panel__chart">
                          <div>
                            <h4 class="expand-panel__title">项目成本构成占比</h4>
                            <p class="expand-panel__subtitle">本期各项费用大项占比分析</p>
                          </div>

                          <div class="bar-list">
                            <div v-for="entry in item.breakdown" :key="entry.id || `${item.orderNo}-${entry.costType}-${entry.amount}`" class="bar-item">
                              <div class="bar-item__meta">
                                <span class="bar-item__name">{{ entry.costTypeName || getCostTypeLabel(entry.costType) }}</span>
                                <span class="bar-item__value">
                                  {{ formatMoney(entry.amount) }} ({{ getPercent(entry.amount, item.totalAmount) }}%)
                                </span>
                              </div>
                              <div class="bar-item__track">
                                <div class="bar-item__fill" :style="{ width: `${getPercent(entry.amount, item.totalAmount)}%` }"></div>
                              </div>
                            </div>
                          </div>
                        </div>

                        <div class="expand-panel__detail">
                          <div class="detail-head">
                            <span class="detail-head__title">工单费用构成明细</span>
                            <span class="detail-head__month">核算期: {{ item.month }}</span>
                          </div>

                          <div class="detail-table-wrap">
                            <table class="detail-table">
                              <thead>
                                <tr>
                                  <th>费用类型</th>
                                  <th class="text-center">核算机制</th>
                                  <th class="text-right">归集金额</th>
                                  <th>核算摘要与备注</th>
                                </tr>
                              </thead>
                              <tbody>
                                <tr
                                  v-for="entry in item.breakdown"
                                  :key="entry.id || `${item.orderNo}-${entry.costType}-${entry.amount}-detail`"
                                >
                                  <td class="detail-type">{{ entry.costTypeName || getCostTypeLabel(entry.costType) }}</td>
                                  <td class="text-center">
                                    <span class="source-chip" :class="isManualEntry(entry) ? 'source-chip--manual' : 'source-chip--auto'">
                                      {{ getSourceLabel(entry) }}
                                    </span>
                                  </td>
                                  <td
                                    class="text-right detail-amount"
                                    :class="{ 'detail-amount--editable': isManualEntry(entry) }"
                                    @click="startEditCost(entry)"
                                  >
                                    <template v-if="editingCostCell?.id === entry.id">
                                      <input
                                        v-model="editCostValue"
                                        v-focus
                                        class="detail-amount__input"
                                        type="text"
                                        @blur="saveCostEdit(entry)"
                                        @keydown.enter="saveCostEdit(entry)"
                                      />
                                    </template>
                                    <template v-else>
                                      <span>{{ formatMoney(entry.amount) }}</span>
                                    </template>
                                  </td>
                                  <td class="detail-remark" :title="entry.remark || '-'">{{ entry.remark || '-' }}</td>
                                </tr>
                              </tbody>
                            </table>
                          </div>
                        </div>
                      </div>
                    </td>
                  </tr>
                </template>
              </tbody>
            </table>
          </div>

          <div v-else class="empty-state">
            <div class="empty-state__icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                <path d="M9 12h6m-6 4h6m2 5H7a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5.586a1 1 0 0 1 .707.293l5.414 5.414a1 1 0 0 1 .293.707V19a2 2 0 0 1-2 2z" />
              </svg>
            </div>
            <p>暂无成本数据</p>
          </div>
        </template>

        <div v-if="total > 0" class="pagination-bar">
          <el-pagination
            v-model:current-page="queryParams.pageNo"
            v-model:page-size="queryParams.pageSize"
            :total="total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @current-change="handlePageChange"
            @size-change="handleSizeChange"
          />
        </div>
      </section>
    </main>

    <footer class="cost-footer">
      <div class="cost-footer__inner">
        <span>© 2026 生产工单财务费用分配及归集系统</span>
        <span>{{ footerStatus }}</span>
      </div>
    </footer>

    <transition name="toast-fade">
      <div v-if="showToast" class="toast-box">
        <div class="toast-box__icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="20 6 9 17 4 12" />
          </svg>
        </div>
        <span>{{ toastMessage }}</span>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import {
  FinanceCostApi,
  type FinanceCostEntryVO,
  type FinanceCostSummaryVO
} from '@/api/erp/finance/cost'
import { formatAmount } from '@/views/erp/finance/shared/accounting'
import { getCostTypeLabel, getCostTypeOptions } from './costPage.helpers'

defineOptions({ name: 'ErpFinanceCost' })

type AggregatedCostRow = {
  key: string
  productionOrderId?: number
  orderNo: string
  projectName: string
  month: string
  totalAmount: number
  breakdown: FinanceCostEntryVO[]
}

const listLoading = ref(false)
const exportLoading = ref(false)
const listError = ref('')
const rawList = ref<FinanceCostSummaryVO[]>([])
const total = ref(0)
const expandedCostOrders = ref<Record<string, boolean>>({})
const editingCostCell = ref<FinanceCostEntryVO | null>(null)
const editCostValue = ref('')
const showToast = ref(false)
const toastMessage = ref('')
const toastTimer = ref<ReturnType<typeof setTimeout> | undefined>()

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10
})

const costTypeFilter = ref('ALL')
const costMonthFilter = ref('ALL')
const costSearchTerm = ref('')

const costTypeOptions = getCostTypeOptions()

const vFocus = {
  mounted: (el: HTMLInputElement) => el.focus(),
  updated: (el: HTMLInputElement) => el.focus()
}

const triggerToast = (message: string) => {
  toastMessage.value = message
  showToast.value = true
  if (toastTimer.value) {
    clearTimeout(toastTimer.value)
  }
  toastTimer.value = setTimeout(() => {
    showToast.value = false
  }, 2400)
}

const monthOptions = computed(() => {
  const months = new Set<string>()
  rawList.value.forEach((item) => {
    if (item.accountingMonth) {
      months.add(item.accountingMonth)
    }
  })
  if (costMonthFilter.value !== 'ALL') {
    months.add(costMonthFilter.value)
  }
  return Array.from(months).sort((a, b) => b.localeCompare(a))
})

const costItems = computed(() => {
  const keyword = costSearchTerm.value.trim().toLowerCase()
  return rawList.value.filter((item) => {
    if (!keyword) {
      return true
    }
    const orderNo = item.productionOrderNo || ''
    const projectName = item.projectName || ''
    const productName = item.productName || ''
    return [orderNo, projectName, productName].some((field) => field.toLowerCase().includes(keyword))
  })
})

const aggregatedCosts = computed<AggregatedCostRow[]>(() => {
  return costItems.value.map((item) => ({
    key: `${item.productionOrderId || item.productionOrderNo || '-'}-${item.accountingMonth || '-'}`,
    productionOrderId: item.productionOrderId,
    orderNo: item.productionOrderNo || '-',
    projectName: item.projectName || item.projectNo || item.productName || '-',
    month: item.accountingMonth || '-',
    totalAmount: Number(item.totalAmount || 0),
    breakdown: item.breakdown || []
  }))
})

const totalCostCollected = computed(() =>
  costItems.value.reduce((sum, item) => sum + Number(item.totalAmount || 0), 0)
)

const primaryOrderNo = computed(() => aggregatedCosts.value[0]?.orderNo || '-')

const displayMonth = computed(() => {
  if (costMonthFilter.value !== 'ALL') {
    return costMonthFilter.value
  }
  return aggregatedCosts.value[0]?.month || '未选择'
})

const footerStatus = computed(() => {
  if (listLoading.value) {
    return '系统状态：数据加载中'
  }
  if (listError.value) {
    return '系统状态：数据连接异常'
  }
  return '系统状态：数据连接正常 | 成本核算轧账已就绪'
})

const buildPageParams = () => ({
  pageNo: queryParams.pageNo,
  pageSize: queryParams.pageSize,
  costType: costTypeFilter.value === 'ALL' ? undefined : Number(costTypeFilter.value),
  accountingMonth: costMonthFilter.value === 'ALL' ? undefined : costMonthFilter.value
})

const formatMoney = (value?: number | string) => {
  const formatted = formatAmount(value)
  return formatted === '-' ? '0.00' : formatted
}

const getSourceLabel = (entry: FinanceCostEntryVO) => {
  if (entry.sourceTypeName) {
    return entry.sourceTypeName
  }
  if (Number(entry.sourceType) === 10) {
    return '手工录入'
  }
  if (Number(entry.sourceType) === 20) {
    return '分摊生成'
  }
  return '-'
}

const isManualEntry = (entry: FinanceCostEntryVO) => Number(entry.sourceType) === 10

const getPercent = (value?: number | string, totalAmount?: number) => {
  const amount = Number(value || 0)
  const totalValue = Number(totalAmount || 0)
  if (!totalValue) {
    return 0
  }
  return Number(((amount / totalValue) * 100).toFixed(1))
}

const getList = async () => {
  listLoading.value = true
  listError.value = ''
  try {
    const data = await FinanceCostApi.getPage(buildPageParams())
    rawList.value = Array.isArray(data?.list) ? data.list : []
    total.value = Number(data?.total || 0)
  } catch {
    rawList.value = []
    total.value = 0
    listError.value = '成本明细加载失败，请稍后重试'
  } finally {
    listLoading.value = false
  }
}

const handleQuery = async () => {
  queryParams.pageNo = 1
  expandedCostOrders.value = {}
  await getList()
}

const resetFilters = async () => {
  costTypeFilter.value = 'ALL'
  costMonthFilter.value = 'ALL'
  costSearchTerm.value = ''
  queryParams.pageNo = 1
  expandedCostOrders.value = {}
  await getList()
  triggerToast('筛选条件已清空')
}

const handleReset = async () => {
  expandedCostOrders.value = {}
  editingCostCell.value = null
  await getList()
  triggerToast('账目数据已刷新')
}

const handleExport = async () => {
  try {
    exportLoading.value = true
    const data = await FinanceCostApi.exportExcel(buildPageParams())
    const { download } = await import('@/utils/download')
    download.excel(data, '生产成本归集明细.xlsx')
    triggerToast('生产成本总账报表导出成功')
  } catch {
    triggerToast('导出失败，请稍后重试')
  } finally {
    exportLoading.value = false
  }
}

const toggleCostExpand = (orderNo: string) => {
  expandedCostOrders.value[orderNo] = !expandedCostOrders.value[orderNo]
}

const startEditCost = (entry: FinanceCostEntryVO) => {
  if (!isManualEntry(entry)) {
    return
  }
  editingCostCell.value = entry
  editCostValue.value = String(entry.amount || '')
}

const saveCostEdit = async (entry: FinanceCostEntryVO) => {
  if (!editingCostCell.value) {
    return
  }
  const value = parseFloat(editCostValue.value)
  if (Number.isNaN(value) || value <= 0) {
    triggerToast('请输入有效的金额数值')
    return
  }
  if (!entry.id || !entry.productionOrderId || !entry.costType) {
    triggerToast('当前记录缺少更新参数，无法保存')
    editingCostCell.value = null
    return
  }
  try {
    await FinanceCostApi.update({
      id: entry.id,
      productionOrderId: entry.productionOrderId,
      costType: entry.costType,
      accountingMonth: entry.accountingMonth,
      amount: value,
      remark: entry.remark
    })
    entry.amount = value
    editingCostCell.value = null
    triggerToast('账目调整成功，相关分摊比例已更新')
  } catch {
    triggerToast('账目调整失败，请稍后重试')
  }
}

const handlePageChange = async (pageNo: number) => {
  queryParams.pageNo = pageNo
  expandedCostOrders.value = {}
  await getList()
}

const handleSizeChange = async (pageSize: number) => {
  queryParams.pageSize = pageSize
  queryParams.pageNo = 1
  expandedCostOrders.value = {}
  await getList()
}

onMounted(async () => {
  await getList()
})

onBeforeUnmount(() => {
  if (toastTimer.value) {
    clearTimeout(toastTimer.value)
  }
})
</script>

<style scoped>
.cost-page-shell {
  min-height: 100%;
  background: #f8fafc;
  color: #334155;
}

.cost-topbar {
  position: sticky;
  top: 0;
  z-index: 20;
  border-bottom: 1px solid #1e293b;
  background: #1e293b;
  box-shadow: 0 1px 2px rgb(15 23 42 / 10%);
}

.cost-topbar__inner,
.cost-main,
.cost-footer__inner {
  width: min(1600px, calc(100% - 32px));
  margin: 0 auto;
}

.cost-topbar__inner {
  display: flex;
  min-height: 72px;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 10px 4px;
}

.cost-topbar__brand {
  display: flex;
  align-items: center;
  gap: 10px;
}

.cost-topbar__brand-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  border-radius: 6px;
  background: #0284c7;
  color: #fff;
}

.cost-topbar__brand-icon svg {
  width: 12px;
  height: 12px;
}

.cost-topbar__title {
  margin: 0;
  color: #f8fafc;
  font-size: 18px;
  font-weight: 700;
}

.cost-topbar__subtitle {
  margin: 2px 0 0;
  color: #94a3b8;
  font-size: 12px;
}

.cost-topbar__metrics {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 12px;
}

.metric-chip,
.metric-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-height: 38px;
  border: 1px solid rgb(51 65 85 / 80%);
  border-radius: 8px;
  background: rgb(30 41 59 / 75%);
  padding: 0 14px;
  color: #cbd5e1;
  font-size: 12px;
}

.metric-chip__label {
  color: #94a3b8;
}

.metric-chip__value,
.metric-badge {
  color: #22d3ee;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-weight: 700;
}

.metric-chip__plain {
  color: #cbd5e1;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-weight: 700;
}

.cost-main {
  padding: 20px 0 24px;
}

.cost-card {
  border: 1px solid #dbe3ef;
  border-radius: 14px;
  background: #fff;
  box-shadow: 0 1px 2px rgb(15 23 42 / 4%);
}

.cost-card + .cost-card {
  margin-top: 18px;
}

.cost-card--hero {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: 20px;
  padding: 28px 24px;
}

.hero-copy__badge {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  border: 1px solid #bfdbfe;
  border-radius: 6px;
  background: #eff6ff;
  padding: 0 10px;
  color: #0284c7;
  font-size: 12px;
  font-weight: 700;
}

.hero-copy__title {
  margin: 16px 0 12px;
  color: #020617;
  font-size: 18px;
  font-weight: 700;
}

.hero-copy__desc {
  max-width: 860px;
  margin: 0;
  color: #94a3b8;
  font-size: 14px;
  line-height: 1.7;
}

.hero-actions {
  display: flex;
  gap: 10px;
  align-items: flex-end;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  min-height: 40px;
  border: 1px solid transparent;
  border-radius: 8px;
  padding: 0 18px;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn svg {
  width: 14px;
  height: 14px;
}

.btn:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.btn--plain {
  border-color: #dbe3ef;
  background: #f8fafc;
  color: #475569;
}

.btn--plain:hover:not(:disabled) {
  background: #f1f5f9;
}

.btn--primary,
.btn--table {
  background: #0284c7;
  color: #fff;
}

.btn--primary:hover:not(:disabled),
.btn--table:hover:not(:disabled) {
  background: #0369a1;
}

.btn--compact {
  min-height: 38px;
  padding: 0 16px;
}

.btn--table {
  min-height: 34px;
  border-radius: 8px;
  padding: 0 14px;
}

.btn--table-active {
  border-color: #cbd5e1;
  background: #f1f5f9;
  color: #0f172a;
}

.cost-card--filter {
  padding: 24px;
}

.filter-grid {
  display: grid;
  grid-template-columns: 240px 240px minmax(320px, 1fr);
  gap: 14px;
  align-items: end;
}

.filter-field__label {
  display: block;
  margin-bottom: 10px;
  color: #94a3b8;
  font-size: 13px;
  font-weight: 700;
}

.filter-select,
.filter-search__input {
  width: 100%;
  min-height: 40px;
  border: 1px solid #dbe3ef;
  border-radius: 8px;
  background: #f8fafc;
  color: #334155;
  font-size: 14px;
  outline: none;
  transition: all 0.2s ease;
}

.filter-select {
  padding: 0 12px;
}

.filter-search {
  display: grid;
  grid-template-columns: minmax(220px, 1fr) auto auto;
  gap: 10px;
  align-items: end;
}

.filter-search__input-wrap {
  position: relative;
}

.filter-search__icon {
  position: absolute;
  top: 50%;
  left: 14px;
  width: 16px;
  height: 16px;
  color: #94a3b8;
  transform: translateY(-50%);
}

.filter-search__input {
  padding: 0 14px 0 42px;
}

.filter-select:focus,
.filter-search__input:focus {
  border-color: #38bdf8;
  background: #fff;
}

.cost-card--table {
  overflow: hidden;
}

.table-card__header {
  display: flex;
  align-items: center;
  min-height: 52px;
  border-bottom: 1px solid #edf2f7;
  background: #f8fafc;
  padding: 0 20px;
}

.table-card__title {
  position: relative;
  margin: 0;
  padding-left: 12px;
  color: #0f172a;
  font-size: 15px;
  font-weight: 700;
}

.table-card__title::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  width: 4px;
  height: 16px;
  border-radius: 999px;
  background: #0284c7;
  transform: translateY(-50%);
}

.table-alert {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin: 16px 20px 0;
  border: 1px solid #fecaca;
  border-radius: 10px;
  background: #fff1f2;
  padding: 12px 14px;
  color: #be123c;
  font-size: 13px;
}

.table-alert__btn {
  border: 0;
  background: none;
  color: #0284c7;
  font-weight: 700;
  cursor: pointer;
}

.table-loading,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 56px 20px;
  color: #94a3b8;
}

.table-loading__spinner {
  width: 28px;
  height: 28px;
  border: 3px solid #dbeafe;
  border-top-color: #0284c7;
  border-radius: 999px;
  animation: cost-spin 0.8s linear infinite;
}

.empty-state__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  border-radius: 14px;
  background: #f1f5f9;
}

.empty-state__icon svg {
  width: 24px;
  height: 24px;
}

.table-scroll {
  overflow-x: auto;
}

.cost-table,
.detail-table {
  width: 100%;
  border-collapse: collapse;
}

.cost-table th,
.cost-table td,
.detail-table th,
.detail-table td {
  border-bottom: 1px solid #edf2f7;
  padding: 16px 20px;
  font-size: 14px;
}

.cost-table th {
  color: #94a3b8;
  font-weight: 700;
  background: #fff;
}

.cost-table__row:hover {
  background: #f8fafc;
}

.text-center {
  text-align: center;
}

.text-right {
  text-align: right;
}

.order-link {
  border: 0;
  background: none;
  padding: 0;
  color: #0369a1;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
}

.project-name,
.money-text {
  color: #0f172a;
  font-weight: 700;
}

.month-text,
.money-text {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
}

.merge-chip {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  border: 1px solid #dbe3ef;
  border-radius: 8px;
  background: #f8fafc;
  padding: 0 10px;
  color: #475569;
  font-size: 13px;
  font-weight: 700;
}

.expand-row__cell {
  padding: 0 !important;
  background: #f8fafc;
}

.expand-panel {
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr);
  gap: 16px;
  padding: 16px;
  border-left: 4px solid #0284c7;
}

.expand-panel__chart,
.expand-panel__detail {
  border: 1px solid #dbe3ef;
  border-radius: 12px;
  background: #fff;
  padding: 16px;
}

.expand-panel__title {
  margin: 0;
  color: #0f172a;
  font-size: 14px;
  font-weight: 700;
}

.expand-panel__subtitle,
.detail-head__month {
  margin: 4px 0 0;
  color: #94a3b8;
  font-size: 12px;
}

.bar-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 14px;
}

.bar-item__meta {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  color: #475569;
  font-size: 12px;
}

.bar-item__name {
  font-weight: 700;
}

.bar-item__value {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
}

.bar-item__track {
  height: 6px;
  border-radius: 999px;
  background: #e2e8f0;
  overflow: hidden;
}

.bar-item__fill {
  height: 100%;
  border-radius: 999px;
  background: #64748b;
}

.detail-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.detail-head__title {
  color: #0f172a;
  font-size: 14px;
  font-weight: 700;
}

.detail-table-wrap {
  overflow-x: auto;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
}

.detail-table th {
  background: #f8fafc;
  color: #64748b;
  font-weight: 700;
}

.detail-type {
  color: #0f172a;
  font-weight: 700;
}

.detail-amount {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  color: #0f172a;
  font-weight: 700;
}

.detail-amount--editable {
  cursor: pointer;
}

.detail-amount__input {
  width: 100%;
  min-height: 30px;
  border: 1px solid #38bdf8;
  border-radius: 8px;
  padding: 0 8px;
  text-align: right;
  outline: none;
}

.detail-remark {
  max-width: 280px;
  overflow: hidden;
  color: #94a3b8;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.source-chip {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  border: 1px solid #e2e8f0;
  border-radius: 999px;
  padding: 0 10px;
  font-size: 12px;
  font-weight: 700;
}

.source-chip--manual {
  border-color: #fde68a;
  background: #fffbeb;
  color: #b45309;
}

.source-chip--auto {
  background: #f8fafc;
  color: #475569;
}

.pagination-bar {
  display: flex;
  justify-content: flex-end;
  padding: 16px 20px 20px;
}

.cost-footer {
  border-top: 1px solid #dbe3ef;
  background: #fff;
}

.cost-footer__inner {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 4px;
  color: #94a3b8;
  font-size: 12px;
}

.toast-box {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 60;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  border-radius: 12px;
  background: #0f172a;
  padding: 12px 16px;
  color: #fff;
  font-size: 13px;
  font-weight: 700;
  box-shadow: 0 16px 32px rgb(15 23 42 / 22%);
}

.toast-box__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  border-radius: 999px;
  background: #0284c7;
}

.toast-box__icon svg {
  width: 12px;
  height: 12px;
}

.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: #f1f5f9;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  border-radius: 999px;
  background: #cbd5e1;
}

.toast-fade-enter-active,
.toast-fade-leave-active {
  transition: all 0.2s ease;
}

.toast-fade-enter-from,
.toast-fade-leave-to {
  opacity: 0;
  transform: translateY(8px);
}

@keyframes cost-spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 1200px) {
  .filter-grid {
    grid-template-columns: 1fr 1fr;
  }

  .filter-search {
    grid-column: 1 / -1;
  }

  .expand-panel {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 900px) {
  .cost-topbar__inner,
  .cost-main,
  .cost-footer__inner {
    width: calc(100% - 24px);
  }

  .cost-topbar__inner,
  .cost-card--hero,
  .cost-footer__inner,
  .detail-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .hero-actions,
  .cost-topbar__metrics {
    width: 100%;
  }

  .filter-grid,
  .filter-search {
    grid-template-columns: 1fr;
  }

  .cost-table th,
  .cost-table td,
  .detail-table th,
  .detail-table td {
    padding: 12px;
  }

  .cost-footer__inner {
    align-items: flex-start;
  }
}
</style>
