<template>
  <div class="finance-ar-statement-page">
    <ContentWrap class="finance-ar-statement-page__header">
      <div class="finance-ar-statement-page__header-main">
        <h1>应收台账</h1>
        <div class="finance-ar-statement-page__header-actions">
          <el-button :loading="refreshing" :disabled="refreshing" @click="handleRefresh">
            <Icon icon="ep:refresh" class="mr-5px" />刷新
          </el-button>
          <el-button :loading="exporting" :disabled="exporting || listLoading" @click="handleExport">
            <Icon icon="ep:download" class="mr-5px" />导出
          </el-button>
        </div>
      </div>
    </ContentWrap>

    <div class="finance-ar-statement-page__summary" v-loading="summaryLoading">
      <div v-for="card in summaryCards" :key="card.key" class="finance-ar-statement-page__summary-card" :class="card.className">
        <div class="finance-ar-statement-page__summary-label">{{ card.label }}</div>
        <div class="finance-ar-statement-page__summary-value">{{ card.value }}</div>
      </div>
    </div>

    <ContentWrap class="finance-ar-statement-page__filter">
      <div class="finance-ar-statement-page__section-header">
        <span>筛选条件</span>
        <el-button link type="primary" @click="advancedExpanded = !advancedExpanded">
          <Icon :icon="advancedExpanded ? 'ep:arrow-up' : 'ep:arrow-down'" class="mr-5px" />
          {{ advancedExpanded ? '收起高级筛选' : '展开高级筛选' }}
        </el-button>
      </div>
      <el-form :model="queryParams" label-width="92px" class="finance-ar-statement-page__form">
        <div class="finance-ar-statement-page__form-grid">
          <el-form-item label="台账编号">
            <el-input v-model="queryParams.statementNo" placeholder="请输入台账编号" clearable @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="业务类型">
            <el-select v-model="queryParams.bizType" placeholder="请选择业务类型" clearable class="!w-full">
              <el-option v-for="item in AR_STATEMENT_BIZ_TYPE_OPTIONS" :key="item.value" v-bind="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="客户 ID">
            <el-input v-model.number="queryParams.customerId" type="number" placeholder="请输入客户 ID" clearable />
          </el-form-item>
          <el-form-item label="台账状态">
            <el-select v-model="queryParams.status" placeholder="请选择台账状态" clearable class="!w-full">
              <el-option v-for="item in AR_STATEMENT_STATUS_OPTIONS" :key="item.value" v-bind="item" />
            </el-select>
          </el-form-item>
        </div>
        <el-collapse-transition>
          <div v-if="advancedExpanded" class="finance-ar-statement-page__advanced-grid">
            <el-form-item label="业务单号">
              <el-input v-model="queryParams.bizNo" placeholder="请输入业务单号" clearable />
            </el-form-item>
            <el-form-item label="来源订单 ID">
              <el-input v-model.number="queryParams.sourceOrderId" type="number" placeholder="请输入订单 ID" clearable />
            </el-form-item>
            <el-form-item label="业务日期">
              <el-date-picker
                v-model="queryParams.bizDate"
                type="daterange"
                value-format="YYYY-MM-DD"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                class="!w-full"
              />
            </el-form-item>
            <el-form-item label="到期日期">
              <el-date-picker
                v-model="queryParams.dueDate"
                type="daterange"
                value-format="YYYY-MM-DD"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                class="!w-full"
              />
            </el-form-item>
            <el-form-item label="开票状态">
              <el-select v-model="queryParams.invoiceStatus" placeholder="请选择开票状态" clearable class="!w-full">
                <el-option v-for="item in AR_STATEMENT_INVOICE_STATUS_OPTIONS" :key="item.value" v-bind="item" />
              </el-select>
            </el-form-item>
          </div>
        </el-collapse-transition>
        <div class="finance-ar-statement-page__query-actions">
          <el-button type="primary" :loading="listLoading" :disabled="listLoading" @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" />查询
          </el-button>
          <el-button :disabled="listLoading || !hasFilters" @click="handleReset">
            <Icon icon="ep:refresh-left" class="mr-5px" />重置
          </el-button>
        </div>
      </el-form>
    </ContentWrap>

    <ContentWrap class="finance-ar-statement-page__table">
      <el-table v-loading="listLoading" :data="tableData" row-key="id" stripe>
        <el-table-column label="台账信息" min-width="220">
          <template #default="{ row }">
            <div class="finance-ar-statement-page__statement">
              <span class="finance-ar-statement-page__statement-no">{{ row.statementNo || '-' }}</span>
              <span class="finance-ar-statement-page__statement-biz">{{ getBizTypeLabel(row.bizType) }} · {{ row.bizNo || '-' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="客户与账户" min-width="180">
          <template #default="{ row }">
            <div>{{ row.customerName || '-' }}</div>
            <div class="finance-ar-statement-page__muted">{{ row.accountName || '-' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="应收金额" min-width="130" align="right">
          <template #default="{ row }"><span class="finance-ar-statement-page__amount">{{ formatAmount(row.amount) }}</span></template>
        </el-table-column>
        <el-table-column label="已收金额" min-width="130" align="right">
          <template #default="{ row }"><span class="finance-ar-statement-page__amount">{{ formatAmount(row.receivedAmount) }}</span></template>
        </el-table-column>
        <el-table-column label="剩余金额" min-width="130" align="right">
          <template #default="{ row }"><span class="finance-ar-statement-page__amount finance-ar-statement-page__amount--remain">{{ formatAmount(row.remainAmount) }}</span></template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }"><el-tag :type="getStatusType(row.status)" effect="light">{{ getStatusLabel(row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column label="开票状态" width="110" align="center">
          <template #default="{ row }"><el-tag :type="getInvoiceStatusType(row.invoiceStatus)" effect="light">{{ getInvoiceStatusLabel(row.invoiceStatus) }}</el-tag></template>
        </el-table-column>
        <el-table-column label="业务日期" width="125" align="center">
          <template #default="{ row }">{{ formatDateValue(row.bizDate) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :disabled="detailLoading" @click="handleViewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty :image-size="56" description="暂无数据">
            <template #image><Icon icon="ep:document" :size="28" /></template>
          </el-empty>
        </template>
      </el-table>
      <div class="finance-ar-statement-page__pagination">
        <el-pagination
          v-model:current-page="queryParams.pageNo"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handlePageChange"
          @current-change="handlePageChange"
        />
      </div>
    </ContentWrap>

    <el-drawer
      v-model="detailDrawerOpen"
      size="520px"
      :with-header="false"
      append-to-body
      :z-index="3000"
      destroy-on-close
    >
      <div v-if="detailData" class="finance-ar-statement-page__drawer">
        <div class="finance-ar-statement-page__drawer-context">
          <div class="finance-ar-statement-page__drawer-title">{{ detailData.statementNo || '-' }}</div>
          <div>{{ getBizTypeLabel(detailData.bizType) }} · {{ detailData.bizNo || '-' }}</div>
          <el-tag :type="getStatusType(detailData.status)" effect="light">{{ getStatusLabel(detailData.status) }}</el-tag>
        </div>
        <div class="finance-ar-statement-page__drawer-body">
          <div class="finance-ar-statement-page__detail-section">
            <h2>金额信息</h2>
            <div class="finance-ar-statement-page__detail-grid">
              <span>应收金额</span><strong>{{ formatAmount(detailData.amount) }}</strong>
              <span>已收金额</span><strong>{{ formatAmount(detailData.receivedAmount) }}</strong>
              <span>剩余金额</span><strong>{{ formatAmount(detailData.remainAmount) }}</strong>
              <span>币种</span><strong>{{ detailData.currencyCode || '-' }}</strong>
            </div>
          </div>
          <div class="finance-ar-statement-page__detail-section">
            <h2>业务信息</h2>
            <div class="finance-ar-statement-page__detail-grid">
              <span>客户</span><strong>{{ detailData.customerName || '-' }}</strong>
              <span>结算账户</span><strong>{{ detailData.accountName || '-' }}</strong>
              <span>业务日期</span><strong>{{ formatDateValue(detailData.bizDate) }}</strong>
              <span>到期日期</span><strong>{{ formatDateValue(detailData.dueDate) }}</strong>
              <span>开票状态</span><strong>{{ getInvoiceStatusLabel(detailData.invoiceStatus) }}</strong>
              <span>发票号</span><strong>{{ detailData.invoiceNo || '-' }}</strong>
            </div>
          </div>
          <div v-if="detailData.items?.length" class="finance-ar-statement-page__detail-section">
            <h2>台账明细</h2>
            <el-timeline>
              <el-timeline-item v-for="item in detailData.items" :key="item.id" :timestamp="item.createTime || ''">
                <div class="finance-ar-statement-page__item-line">
                  <span>{{ getItemTypeLabel(item.itemType) }}</span>
                  <strong>{{ formatAmount(item.amount) }}</strong>
                </div>
                <div class="finance-ar-statement-page__muted">{{ item.refNo || item.remark || '-' }}</div>
              </el-timeline-item>
            </el-timeline>
          </div>
          <div v-if="detailData.remark" class="finance-ar-statement-page__detail-section">
            <h2>备注</h2>
            <p>{{ detailData.remark }}</p>
          </div>
        </div>
        <div class="finance-ar-statement-page__drawer-footer">
          <el-button @click="detailDrawerOpen = false">关闭</el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  AR_STATEMENT_BIZ_TYPE_OPTIONS,
  AR_STATEMENT_INVOICE_STATUS_OPTIONS,
  AR_STATEMENT_ITEM_TYPE_OPTIONS,
  AR_STATEMENT_STATUS_OPTIONS,
  ArStatementApi,
  type ArStatementPageReqVO,
  type ArStatementSummaryVO,
  type ArStatementVO
} from '@/api/erp/finance/ar-statement'
import { normalizeArStatementSummary } from './arStatement.helpers'

defineOptions({ name: 'ErpArStatement' })

const tableData = ref<ArStatementVO[]>([])
const total = ref(0)
const detailData = ref<ArStatementVO>()
const summaryData = ref<ArStatementSummaryVO[]>([])
const detailDrawerOpen = ref(false)
const advancedExpanded = ref(false)
const listLoading = ref(false)
const summaryLoading = ref(false)
const detailLoading = ref(false)
const refreshing = ref(false)
const exporting = ref(false)

const queryParams = reactive<ArStatementPageReqVO>({ pageNo: 1, pageSize: 10 })

const hasFilters = computed(() => Object.entries(queryParams).some(([key, value]) => key !== 'pageNo' && key !== 'pageSize' && value != null && value !== ''))

const summaryCards = computed(() => {
  const totalAmount = summaryData.value.reduce((sum, item) => sum + Number(item.totalAmount || 0), 0)
  const totalReceivedAmount = summaryData.value.reduce((sum, item) => sum + Number(item.totalReceivedAmount || 0), 0)
  const totalRemainAmount = summaryData.value.reduce((sum, item) => sum + Number(item.totalRemainAmount || 0), 0)
  const statementCount = summaryData.value.reduce((sum, item) => sum + Number(item.statementCount || 0), 0)
  return [
    { key: 'amount', label: '应收总额', value: formatAmount(totalAmount), className: 'finance-ar-statement-page__summary-card--blue' },
    { key: 'received', label: '已收总额', value: formatAmount(totalReceivedAmount), className: 'finance-ar-statement-page__summary-card--green' },
    { key: 'remain', label: '待收总额', value: formatAmount(totalRemainAmount), className: 'finance-ar-statement-page__summary-card--amber' },
    { key: 'count', label: '台账数量', value: String(statementCount), className: 'finance-ar-statement-page__summary-card--slate' }
  ]
})

const loadList = async () => {
  if (listLoading.value) return
  listLoading.value = true
  try {
    const response = await ArStatementApi.getPage(queryParams)
    tableData.value = response.list || []
    total.value = response.total || 0
  } catch {
    ElMessage.error('应收台账加载失败')
  } finally {
    listLoading.value = false
  }
}

const loadSummary = async () => {
  if (summaryLoading.value) return
  summaryLoading.value = true
  try {
    summaryData.value = normalizeArStatementSummary(await ArStatementApi.getSummary(queryParams.customerId))
  } catch {
    ElMessage.error('应收汇总加载失败')
  } finally {
    summaryLoading.value = false
  }
}

const handleRefresh = async () => {
  if (refreshing.value) return
  refreshing.value = true
  await Promise.all([loadList(), loadSummary()])
  refreshing.value = false
}

const handleQuery = async () => {
  if (listLoading.value) return
  queryParams.pageNo = 1
  await loadList()
}

const handleReset = async () => {
  if (listLoading.value) return
  Object.assign(queryParams, {
    statementNo: undefined,
    bizType: undefined,
    bizNo: undefined,
    customerId: undefined,
    sourceOrderId: undefined,
    accountId: undefined,
    currencyCode: undefined,
    invoiceStatus: undefined,
    status: undefined,
    bizDate: undefined,
    dueDate: undefined,
    pageNo: 1
  })
  await loadList()
}

const handlePageChange = () => loadList()

const handleViewDetail = async (row: ArStatementVO) => {
  if (!row.id || detailLoading.value) return
  detailLoading.value = true
  try {
    detailData.value = await ArStatementApi.get(row.id)
    detailDrawerOpen.value = true
  } catch {
    ElMessage.error('应收台账详情加载失败')
  } finally {
    detailLoading.value = false
  }
}

const handleExport = async () => {
  if (exporting.value || listLoading.value) return
  exporting.value = true
  try {
    await ArStatementApi.exportExcel(queryParams)
  } catch {
    ElMessage.error('应收台账导出失败')
  } finally {
    exporting.value = false
  }
}

const formatAmount = (value?: number) => Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
const formatDateValue = (value?: string | number[]) => {
  if (Array.isArray(value)) {
    return value.map((item) => String(item).padStart(2, '0')).join('-')
  }
  return value || '-'
}
const getOptionLabel = (options: { label: string; value: number }[], value?: number) => options.find((item) => item.value === value)?.label || '未知'
const getBizTypeLabel = (value?: number) => getOptionLabel(AR_STATEMENT_BIZ_TYPE_OPTIONS, value)
const getStatusLabel = (value?: number) => getOptionLabel(AR_STATEMENT_STATUS_OPTIONS, value)
const getInvoiceStatusLabel = (value?: number) => getOptionLabel(AR_STATEMENT_INVOICE_STATUS_OPTIONS, value)
const getItemTypeLabel = (value?: number) => getOptionLabel(AR_STATEMENT_ITEM_TYPE_OPTIONS, value)
const getStatusType = (value?: number) => ({ 0: 'warning', 1: 'primary', 2: 'success', 3: 'info' }[value ?? 3] || 'info')
const getInvoiceStatusType = (value?: number) => ({ 0: 'info', 1: 'warning', 2: 'success' }[value ?? 0] || 'info')

onMounted(handleRefresh)
</script>

<style scoped lang="scss">
.finance-ar-statement-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 100%;
  padding: 16px;
  background: var(--erp-slate-50);

  &__header-main,
  &__header-actions,
  &__section-header,
  &__query-actions,
  &__item-line,
  &__drawer-footer {
    display: flex;
    align-items: center;
  }

  &__header-main,
  &__section-header {
    justify-content: space-between;
  }

  &__header-main h1 {
    margin: 0;
    color: var(--erp-slate-900);
    font-size: 20px;
    font-weight: 700;
  }

  &__header-actions,
  &__query-actions {
    gap: 8px;
  }

  &__summary {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 16px;
  }

  &__summary-card {
    min-width: 0;
    padding: 16px;
    border: 1px solid var(--erp-slate-200);
    border-radius: 8px;
    box-shadow: var(--erp-shadow-sm);
    background: var(--erp-surface-white);
  }

  &__summary-card--blue { background: var(--erp-stat-gradient-blue); border-color: var(--erp-stat-border-blue); }
  &__summary-card--green { background: var(--erp-stat-gradient-green); border-color: var(--erp-stat-border-green); }
  &__summary-card--amber { background: var(--erp-stat-gradient-amber); border-color: var(--erp-stat-border-amber); }
  &__summary-card--slate { background: var(--erp-stat-gradient-slate); border-color: var(--erp-stat-border-slate); }

  &__summary-label,
  &__muted,
  &__statement-biz {
    color: var(--erp-slate-500);
    font-size: 12px;
  }

  &__summary-value {
    margin-top: 8px;
    color: var(--erp-slate-900);
    font-family: 'SF Mono', 'Monaco', 'Menlo', monospace;
    font-size: 24px;
    font-weight: 700;
  }

  &__section-header {
    margin-bottom: 16px;
    color: var(--erp-slate-800);
    font-weight: 600;
  }

  &__form-grid,
  &__advanced-grid {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 0 16px;
  }

  &__advanced-grid { margin-top: 4px; }
  &__query-actions { justify-content: flex-end; }
  &__table { overflow-x: auto; }
  &__statement { display: flex; flex-direction: column; gap: 4px; }
  &__statement-no { color: var(--erp-slate-900); font-weight: 600; }
  &__amount { font-family: 'SF Mono', 'Monaco', 'Menlo', monospace; }
  &__amount--remain { color: var(--erp-primary-600); font-weight: 600; }
  &__pagination { display: flex; justify-content: flex-end; padding-top: 16px; }

  &__drawer { display: flex; flex-direction: column; height: 100%; }
  &__drawer-context { display: flex; flex-direction: column; gap: 8px; padding: 24px; color: var(--erp-surface-white); background: var(--erp-slate-800); }
  &__drawer-title { font-size: 20px; font-weight: 700; }
  &__drawer-body { flex: 1; overflow-y: auto; padding: 16px; }
  &__detail-section { padding: 16px 0; border-bottom: 1px solid var(--erp-slate-100); }
  &__detail-section:first-child { padding-top: 0; }
  &__detail-section h2 { margin: 0 0 12px; color: var(--erp-slate-800); font-size: 14px; }
  &__detail-grid { display: grid; grid-template-columns: 90px minmax(0, 1fr); gap: 10px 12px; color: var(--erp-slate-500); font-size: 13px; }
  &__detail-grid strong { color: var(--erp-slate-800); font-weight: 500; text-align: right; word-break: break-word; }
  &__item-line { justify-content: space-between; color: var(--erp-slate-800); }
  &__drawer-footer {
    position: sticky;
    bottom: 0;
    z-index: 1001;
    flex-shrink: 0;
    justify-content: flex-end;
    padding: 12px 64px 12px 16px;
    border-top: 1px solid var(--erp-slate-200);
    background: var(--erp-surface-white);
    pointer-events: auto;
  }

  @media (max-width: 1200px) {
    &__form-grid,
    &__advanced-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  }

  @media (max-width: 768px) {
    padding: 8px;
    gap: 8px;
    &__summary { grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 8px; }
    &__summary-value { font-size: 18px; }
    &__header-main { align-items: flex-start; flex-direction: column; gap: 8px; }
    &__header-actions { width: 100%; }
    &__header-actions .el-button { flex: 1; }
    &__form-grid,
    &__advanced-grid { grid-template-columns: minmax(0, 1fr); }
    &__query-actions { justify-content: stretch; }
    &__query-actions .el-button { flex: 1; }
  }
}
</style>
