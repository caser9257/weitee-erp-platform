<template>
  <div class="finance-shell finance-report-item-page finance-shell__stack">
    <ContentWrap class="finance-shell__header-card finance-report-item-page__header-card">
      <div class="finance-shell__page-header finance-report-item-page__page-header">
        <div class="finance-shell__page-header-main finance-report-item-page__page-header-main">
          <div class="finance-shell__page-title">报表项目</div>
          <div class="finance-shell__metric-grid finance-report-item-page__summary-grid">
            <div
              v-for="card in summaryCards"
              :key="card.label"
              class="finance-shell__metric-card finance-report-item-page__summary-card"
            >
              <div class="finance-report-item-page__summary-icon" :class="card.toneClass">
                <Icon :icon="card.icon" />
              </div>
              <div class="finance-report-item-page__summary-body">
                <div class="finance-shell__metric-label">{{ card.label }}</div>
                <div class="finance-shell__metric-value">{{ card.value }}</div>
              </div>
            </div>
          </div>
        </div>
        <div class="finance-shell__page-header-actions">
          <span v-if="isDemoMode" class="finance-shell__page-demo-badge">示例数据</span>
        </div>
      </div>
    </ContentWrap>

    <ContentWrap class="finance-shell__filter-card">
      <div class="finance-shell__section-head">
        <div class="finance-shell__section-title">筛选条件</div>
      </div>
      <el-form ref="queryFormRef" :model="queryParams" label-width="76px" class="finance-shell__query-form" @submit.prevent>
        <div class="finance-shell__query-grid finance-shell__query-grid--wide finance-report-item-page__query-grid">
          <el-form-item label="账簿" prop="ledgerId">
            <el-select v-model="queryParams.ledgerId" placeholder="请选择账簿" clearable filterable :loading="ledgerLoading" class="!w-full">
              <el-option v-for="item in ledgerOptions" :key="item.id" :label="displayLedgerName(item.name)" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="报表类型" prop="reportType">
            <el-select v-model="queryParams.reportType" placeholder="请选择报表类型" clearable class="!w-full">
              <el-option v-for="item in REPORT_TYPE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="项目分类" prop="itemCategory">
            <el-select v-model="queryParams.itemCategory" placeholder="请选择项目分类" clearable class="!w-full">
              <el-option v-for="item in REPORT_ITEM_CATEGORY_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="项目编码" prop="itemCode">
            <el-input v-model="queryParams.itemCode" placeholder="请输入项目编码" clearable class="!w-full" @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="项目名称" prop="itemName">
            <el-input v-model="queryParams.itemName" placeholder="请输入项目名称" clearable class="!w-full" @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="请选择状态" clearable class="!w-full">
              <el-option v-for="item in COMMON_STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </div>
        <div class="finance-shell__query-actions">
          <el-button type="primary" :loading="listLoading" :disabled="!canQuery" @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" />
            查询
          </el-button>
          <el-button :disabled="!canReset" @click="resetQuery">
            <Icon icon="ep:refresh-left" class="mr-5px" />
            重置
          </el-button>
        </div>
      </el-form>
    </ContentWrap>

    <ContentWrap class="finance-shell__table-card finance-shell__toolbar-card finance-report-item-page__table-card">
      <div class="finance-shell__toolbar">
        <div class="finance-shell__table-toolbar-main">
          <div class="finance-shell__section-title">报表项目列表</div>
          <div class="finance-shell__toolbar-count">当前共 <strong>{{ total }}</strong> 条</div>
        </div>
        <div class="finance-shell__toolbar-actions">
          <el-button type="primary" plain @click="openForm('create')" v-hasPermi="['erp:finance-report-item:create']">
            <Icon icon="ep:plus" class="mr-5px" />
            新增
          </el-button>
          <el-button type="success" plain @click="openInitTemplate()" v-hasPermi="['erp:finance-report-item:create']">
            <Icon icon="ep:magic-stick" class="mr-5px" />
            初始化模板
          </el-button>
        </div>
      </div>

      <template v-if="listLoading || list.length">
        <div class="finance-shell__table-wrap">
          <el-table class="finance-shell__table finance-shell__table--dense" v-loading="listLoading" :data="list" row-key="id" stripe :show-overflow-tooltip="false">
            <el-table-column min-width="260">
              <template #header>
                <span class="finance-shell__column-header">
                  <Icon icon="ep:document" class="finance-shell__column-icon" />
                  报表项目
                </span>
              </template>
              <template #default="{ row }">
                <div class="finance-shell__primary-cell">
                  <span class="finance-shell__primary-text">{{ row.itemName || '-' }}</span>
                  <span class="finance-shell__muted-text finance-shell__mono">{{ row.itemCode || '-' }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column min-width="160">
              <template #header>
                <span class="finance-shell__column-header finance-shell__column-header--pipeline">
                  <Icon icon="ep:collection" class="finance-shell__column-icon" />
                  账簿
                </span>
              </template>
              <template #default="{ row }">
                <div class="finance-shell__primary-cell">
                  <span class="finance-shell__primary-text">{{ displayLedgerName(row.ledgerName) }}</span>
                  <span class="finance-shell__muted-text finance-shell__mono">{{ row.ledgerId || '-' }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column min-width="140" label="报表类型">
              <template #default="{ row }">
                <span class="finance-shell__metric-pill finance-shell__metric-pill--primary">
                  {{ row.reportTypeName || getReportTypeLabel(row.reportType) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column min-width="120" label="项目分类">
              <template #default="{ row }">
                <span class="finance-report-item-page__category-pill" :class="resolveCategoryClass(row.itemCategory)">
                  {{ row.itemCategoryName || getReportItemCategoryLabel(row.itemCategory) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="取数映射" align="right" width="120">
              <template #default="{ row }">
                <span class="finance-shell__amount">{{ row.subjects?.length || 0 }}</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" align="center" width="110">
              <template #default="{ row }">
                <span class="finance-shell__status-badge" :class="row.status === 0 ? 'finance-shell__status-badge--success' : 'finance-shell__status-badge--neutral'">
                  <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="row.status" />
                </span>
              </template>
            </el-table-column>
            <el-table-column label="排序" prop="sort" align="right" width="90" />
            <el-table-column label="操作" fixed="right" align="center" width="220">
              <template #default="{ row }">
                <div class="finance-shell__row-actions">
                  <el-button size="small" link type="primary" :disabled="isRowBusy(row.id)" @click="openForm('update', row.id)">编辑</el-button>
                  <el-button size="small" link type="primary" :disabled="isRowBusy(row.id)" @click="openMappingDrawer(row)">查看映射</el-button>
                  <el-button size="small" link type="danger" :loading="deleteLoadingId === row.id" :disabled="isRowBusy(row.id)" @click="handleDelete(row.id)">删除</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </template>
      <div v-else-if="listErrorMessage" class="finance-shell__empty">
        <div class="finance-shell__empty-icon">
          <Icon icon="ep:warning-filled" />
        </div>
        <div class="finance-shell__empty-title">报表项目加载失败</div>
        <div class="finance-shell__empty-desc">{{ listErrorMessage }}</div>
        <el-button type="primary" plain @click="getList">重试</el-button>
      </div>
      <div v-else class="finance-shell__empty">
        <div class="finance-shell__empty-icon">
          <Icon icon="ep:document" />
        </div>
        <div class="finance-shell__empty-title">暂无报表项目</div>
      </div>

      <Pagination v-if="total > 0" v-model:limit="queryParams.pageSize" v-model:page="queryParams.pageNo" :total="total" @pagination="getList" />
    </ContentWrap>

    <ReportItemForm ref="formRef" @success="handleFormSuccess" />

    <el-drawer
      v-model="mappingDrawerOpen"
      :with-header="false"
      size="760px"
      destroy-on-close
      modal-class="finance-report-item-page__drawer-modal"
      @closed="clearMappingDrawer"
    >
      <div class="finance-report-item-page__drawer-shell">
        <div class="finance-report-item-page__drawer-head">
          <div class="finance-shell__page-header-main">
            <div class="finance-report-item-page__drawer-title">查看映射</div>
            <div class="finance-report-item-page__drawer-subtitle">当前项目的取数科目与规则</div>
          </div>
          <el-button text class="finance-report-item-page__drawer-close" @click="mappingDrawerOpen = false">
            <Icon icon="ep:close" />
          </el-button>
        </div>

        <div v-if="currentRow" class="finance-shell__context-card finance-report-item-page__drawer-context">
          <div class="finance-shell__context-main">
            <div class="finance-shell__context-title">{{ currentRow.itemName || '-' }}</div>
            <div class="finance-shell__context-subtitle">
              {{ currentRow.itemCode || '-' }} · {{ displayLedgerName(currentRow.ledgerName) }} · {{ currentRow.reportTypeName || getReportTypeLabel(currentRow.reportType) }}
            </div>
          </div>
          <div class="finance-shell__context-meta">
            <div class="finance-shell__context-meta-item">
              <span>项目分类</span>
              <span>{{ currentRow.itemCategoryName || getReportItemCategoryLabel(currentRow.itemCategory) }}</span>
            </div>
            <div class="finance-shell__context-meta-item">
              <span>映射条数</span>
              <span>{{ currentRow.subjects?.length || 0 }}</span>
            </div>
            <div class="finance-shell__context-meta-item">
              <span>状态</span>
              <span><dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="currentRow.status" /></span>
            </div>
          </div>
        </div>

        <div class="finance-report-item-page__drawer-body">
          <div class="finance-shell__section-head finance-report-item-page__drawer-section-head">
            <div class="finance-shell__section-title">科目映射</div>
            <div class="finance-shell__toolbar-count">当前共 <strong>{{ currentRow?.subjects?.length || 0 }}</strong> 条</div>
          </div>
          <div class="finance-report-item-page__drawer-table-wrap">
            <el-table class="finance-shell__table finance-shell__table--dense" :data="currentRow?.subjects || []" stripe :show-overflow-tooltip="false">
              <el-table-column label="科目编码" prop="subjectCode" min-width="140" />
              <el-table-column label="科目名称" prop="subjectName" min-width="180" />
              <el-table-column label="取数规则" min-width="140">
                <template #default="{ row }">{{ row.amountRuleName || '-' }}</template>
              </el-table-column>
              <el-table-column label="符号" align="center" width="90">
                <template #default="{ row }">{{ row.amountSign === -1 ? '减' : '加' }}</template>
              </el-table-column>
            </el-table>
            <div v-if="!currentRow?.subjects?.length" class="finance-shell__empty">
              <div class="finance-shell__empty-icon">
                <Icon icon="ep:document" />
              </div>
              <div class="finance-shell__empty-title">暂无映射数据</div>
            </div>
          </div>
        </div>
      </div>
    </el-drawer>

    <ReportItemTemplateInitForm ref="initFormRef" @success="handleInitTemplateSuccess" />
  </div>
</template>

<script setup lang="ts">
import { DICT_TYPE } from '@/utils/dict'
import {
  COMMON_STATUS_OPTIONS,
  REPORT_ITEM_CATEGORY_OPTIONS,
  REPORT_TYPE_OPTIONS,
  getReportItemCategoryLabel,
  getReportTypeLabel
} from '@/views/erp/finance/shared/accounting'
import { ErpFinanceLedgerVO, FinanceLedgerApi } from '@/api/erp/finance/ledger'
import { ErpFinanceReportItemPageReqVO, ErpFinanceReportItemVO, FinanceReportItemApi } from '@/api/erp/finance/report-item'
import ReportItemForm from './ReportItemForm.vue'
import ReportItemTemplateInitForm from './ReportItemTemplateInitForm.vue'
import { reportItemDemoLedgers, reportItemDemoRows } from './demo'
import { displayLedgerName } from '@/utils/financeDisplay'

defineOptions({ name: 'ErpFinanceReportItem' })

const message = useMessage()
const queryFormRef = ref()
const formRef = ref<InstanceType<typeof ReportItemForm>>()
const initFormRef = ref<InstanceType<typeof ReportItemTemplateInitForm>>()
const ledgerLoading = ref(false)
const listLoading = ref(false)
const listErrorMessage = ref('')
const deleteLoadingId = ref<number>()
const mappingDrawerOpen = ref(false)
const currentRow = ref<ErpFinanceReportItemVO>()
const ledgerOptions = ref<ErpFinanceLedgerVO[]>([])
const list = ref<ErpFinanceReportItemVO[]>([])
const total = ref(0)
const isDemoMode = computed(() => import.meta.env.DEV)

const queryParams = reactive<ErpFinanceReportItemPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  ledgerId: undefined,
  reportType: undefined,
  itemCategory: undefined,
  itemCode: undefined,
  itemName: undefined,
  status: undefined
})

const isActionCanceled = (error: unknown) => error === 'cancel' || error === 'close'
const isRowBusy = (id?: number) => id != null && deleteLoadingId.value === id
const hasActiveFilters = computed(() => !!queryParams.ledgerId || !!queryParams.reportType || !!queryParams.itemCategory || !!queryParams.itemCode || !!queryParams.itemName || queryParams.status !== undefined)
const canQuery = computed(() => !listLoading.value && !deleteLoadingId.value)
const canReset = computed(() => (hasActiveFilters.value || queryParams.pageNo !== 1) && canQuery.value)
const enabledCount = computed(() => list.value.filter((item) => item.status === 0).length)
const mappedItemCount = computed(() => list.value.filter((item) => (item.subjects?.length || 0) > 0).length)
const mappingTotalCount = computed(() => list.value.reduce((sum, item) => sum + (item.subjects?.length || 0), 0))
const summaryCards = computed(() => [
  {
    label: '报表项目总数',
    value: total.value,
    icon: 'ep:document',
    toneClass: 'finance-report-item-page__summary-icon--primary'
  },
  {
    label: '启用项目',
    value: enabledCount.value,
    icon: 'ep:select',
    toneClass: 'finance-report-item-page__summary-icon--success'
  },
  {
    label: '有映射项目',
    value: mappedItemCount.value,
    icon: 'ep:link',
    toneClass: 'finance-report-item-page__summary-icon--warning'
  },
  {
    label: '取数映射数',
    value: mappingTotalCount.value,
    icon: 'ep:coin',
    toneClass: 'finance-report-item-page__summary-icon--neutral'
  }
])

const loadLedgers = async () => {
  ledgerLoading.value = true
  try {
    if (isDemoMode.value) {
      ledgerOptions.value = reportItemDemoLedgers
      return
    }
    const data = await FinanceLedgerApi.getLedgerSimpleList()
    ledgerOptions.value = data?.length ? data : []
  } catch {
    ledgerOptions.value = []
  } finally {
    ledgerLoading.value = false
  }
}

const getList = async () => {
  if (listLoading.value) return
  listLoading.value = true
  listErrorMessage.value = ''
  try {
    if (isDemoMode.value) {
      list.value = reportItemDemoRows
      total.value = reportItemDemoRows.length
      return
    }
    const data = await FinanceReportItemApi.getReportItemPage(queryParams)
    const rows = data?.list || []
    list.value = rows
    total.value = rows.length ? data?.total || rows.length : 0
  } catch {
    if (!list.value.length) {
      listErrorMessage.value = '报表项目加载失败'
    }
  } finally {
    listLoading.value = false
  }
}

const handleQuery = async () => {
  if (!canQuery.value) return
  queryParams.pageNo = 1
  await getList()
}

const resetQuery = async () => {
  if (!canReset.value) return
  queryFormRef.value?.resetFields()
  queryParams.pageNo = 1
  await getList()
}

const openForm = (type: 'create' | 'update', id?: number) => {
  formRef.value?.open(type, id)
}

const handleDelete = async (id?: number) => {
  if (!id || deleteLoadingId.value) {
    return
  }
  try {
    await message.delConfirm()
    deleteLoadingId.value = id
    await FinanceReportItemApi.deleteReportItem(id)
    message.success('删除成功')
    if (list.value.length === 1 && queryParams.pageNo > 1) {
      queryParams.pageNo -= 1
    }
    await getList()
  } catch (error) {
    if (!isActionCanceled(error)) {
      throw error
    }
  } finally {
    deleteLoadingId.value = undefined
  }
}

const openMappingDrawer = (row: ErpFinanceReportItemVO) => {
  currentRow.value = {
    ...row,
    subjects: row.subjects ? row.subjects.map((item) => ({ ...item })) : []
  }
  mappingDrawerOpen.value = true
}

const clearMappingDrawer = () => {
  currentRow.value = undefined
}

const resolveCategoryClass = (value?: number) => {
  if (value === 10) return 'finance-report-item-page__category-pill--primary'
  if (value === 20) return 'finance-report-item-page__category-pill--warning'
  if (value === 30) return 'finance-report-item-page__category-pill--success'
  return 'finance-report-item-page__category-pill--neutral'
}

const handleFormSuccess = async () => {
  queryParams.pageNo = 1
  await getList()
}

const openInitTemplate = () => {
  initFormRef.value?.open()
}

const handleInitTemplateSuccess = async () => {
  queryParams.pageNo = 1
  await getList()
}

onMounted(async () => {
  await Promise.allSettled([loadLedgers(), getList()])
})
</script>

<style scoped>
@import '../shared/readOnlyPage.css';

.finance-report-item-page {
  background: #f8fafc;
}

.finance-report-item-page :deep(.el-card__body) {
  padding: 0;
}

.finance-report-item-page__header-card :deep(.el-card__body),
.finance-report-item-page__table-card :deep(.el-card__body) {
  padding: 12px 14px;
}

.finance-report-item-page__header-card,
.finance-report-item-page__table-card {
  overflow: hidden;
}

.finance-report-item-page__page-header {
  align-items: flex-start;
}

.finance-report-item-page__page-header-main {
  flex: 1 1 auto;
  min-width: 0;
}

.finance-report-item-page__summary-grid {
  margin-top: 10px;
  gap: 10px;
}

.finance-report-item-page__summary-card {
  min-height: 68px;
  padding: 10px 12px;
  border-color: #dbe4f0;
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
}

.finance-report-item-page__summary-card :deep(.finance-shell__metric-label) {
  font-size: 11px;
}

.finance-report-item-page__summary-card :deep(.finance-shell__metric-value) {
  margin-top: 4px;
  font-size: 16px;
  line-height: 1.15;
}

.finance-report-item-page__summary-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  margin-bottom: 8px;
  border-radius: 10px;
  font-size: 16px;
}

.finance-report-item-page__summary-icon--primary {
  color: #2563eb;
  background: #eff6ff;
}

.finance-report-item-page__summary-icon--success {
  color: #059669;
  background: #ecfdf5;
}

.finance-report-item-page__summary-icon--warning {
  color: #d97706;
  background: #fffbeb;
}

.finance-report-item-page__summary-icon--neutral {
  color: #475569;
  background: #f8fafc;
}

.finance-report-item-page__summary-body {
  min-width: 0;
}

.finance-report-item-page__query-grid {
  gap: 0 10px;
}

.finance-report-item-page__table-card {
  padding-top: 0;
}

.finance-report-item-page__category-pill {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  padding: 0 9px;
  border: 1px solid transparent;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}

.finance-report-item-page__category-pill--primary {
  color: #2563eb;
  background: #eff6ff;
  border-color: #bfdbfe;
}

.finance-report-item-page__category-pill--success {
  color: #059669;
  background: #ecfdf5;
  border-color: #bbf7d0;
}

.finance-report-item-page__category-pill--warning {
  color: #d97706;
  background: #fffbeb;
  border-color: #fde68a;
}

.finance-report-item-page__category-pill--neutral {
  color: #475569;
  background: #f8fafc;
  border-color: #e2e8f0;
}

.finance-report-item-page__drawer-modal {
  backdrop-filter: blur(4px);
}

.finance-report-item-page__drawer-shell {
  display: flex;
  height: 100%;
  flex-direction: column;
  padding: 16px 16px 18px;
}

.finance-report-item-page__drawer-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.finance-report-item-page__drawer-title {
  color: #0f172a;
  font-size: 18px;
  font-weight: 700;
}

.finance-report-item-page__drawer-subtitle {
  margin-top: 4px;
  color: #64748b;
  font-size: 12px;
}

.finance-report-item-page__drawer-close {
  color: #64748b;
}

.finance-report-item-page__drawer-context {
  margin-bottom: 12px;
}

.finance-report-item-page__drawer-body {
  display: flex;
  flex: 1 1 auto;
  min-height: 0;
  flex-direction: column;
  gap: 12px;
  overflow: hidden;
}

.finance-report-item-page__drawer-table-wrap {
  display: flex;
  flex: 1 1 auto;
  min-height: 0;
  flex-direction: column;
  overflow: auto;
}

.finance-report-item-page__drawer-table-wrap .finance-shell__empty {
  min-height: 180px;
}

.finance-report-item-page__drawer-section-head {
  margin-bottom: 0;
}

@media (max-width: 1200px) {
  .finance-report-item-page__summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 960px) {
  .finance-report-item-page__query-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .finance-report-item-page__header-card :deep(.el-card__body),
  .finance-report-item-page__table-card :deep(.el-card__body) {
    padding: 12px;
  }

  .finance-report-item-page__summary-grid,
  .finance-report-item-page__query-grid {
    grid-template-columns: 1fr;
  }

  .finance-report-item-page__drawer-shell {
    padding: 12px;
  }
}
</style>
