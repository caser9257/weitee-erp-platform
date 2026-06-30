<template>
  <div class="finance-cost-page">
    <ContentWrap>
      <div class="page-hero">
        <div>
          <div class="page-hero__title">项目与生产成本分析</div>
        </div>
        <div class="page-hero__actions">
          <el-tag effect="light" round type="primary">成本归集</el-tag>
          <el-tag effect="light" round :type="listLoading ? 'warning' : 'success'">
            {{ listLoading ? '加载中' : '可查看' }}
          </el-tag>
        </div>
      </div>
    </ContentWrap>

    <ContentWrap>
      <el-form
        ref="queryFormRef"
        :model="queryParams"
        label-width="92px"
        class="query-form"
        @submit.prevent
      >
        <div class="query-form__grid">
          <el-form-item label="生产工单" prop="productionOrderId">
            <el-select
              v-model="queryParams.productionOrderId"
              class="w-full"
              clearable
              filterable
              placeholder="请选择生产工单"
              :loading="optionsLoading"
            >
              <el-option
                v-for="item in productionOrderOptions"
                :key="item.id"
                :label="formatProductionOrderLabel(item)"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="成本类型" prop="costType">
            <el-select
              v-model="queryParams.costType"
              class="w-full"
              clearable
              placeholder="请选择成本类型"
            >
              <el-option
                v-for="item in costTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="归集月份" prop="accountingMonth">
            <el-date-picker
              v-model="queryParams.accountingMonth"
              class="w-full"
              clearable
              placeholder="请选择归集月份"
              type="month"
              value-format="YYYY-MM"
            />
          </el-form-item>
        </div>
        <div class="query-form__actions">
          <el-button :loading="listLoading" @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" />
            查询
          </el-button>
          <el-button :disabled="listLoading" @click="resetQuery">
            <Icon icon="ep:refresh" class="mr-5px" />
            重置
          </el-button>
        </div>
      </el-form>
    </ContentWrap>

    <ContentWrap>
      <div class="stats-row">
        <div class="stat-card stat-card--soft">
          <div class="stat-card__label">已选工单</div>
          <div class="stat-card__value">{{ selectedRow?.productionOrderNo || '未选择' }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-card__label">当前页金额</div>
          <div class="stat-card__value">{{ formatAmount(pageAmountTotal) }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-card__label">成本类型数</div>
          <div class="stat-card__value">{{ costTypeCount }}</div>
        </div>
        <div class="stat-card stat-card--success">
          <div class="stat-card__label">列表条数</div>
          <div class="stat-card__value">{{ total }}</div>
        </div>
      </div>

      <el-alert
        v-if="listError"
        class="mb-12px"
        :closable="false"
        show-icon
        type="error"
        :title="listError"
      />

      <div class="section-head">
        <div class="section-head__title">成本明细</div>
        <div class="section-head__actions">
          <el-button
            type="primary"
            plain
            :disabled="!hasSelectedRow || detailLoading"
            @click="openDetailDrawer(selectedRow)"
          >
            <Icon icon="ep:document" class="mr-5px" />
            穿透查看
          </el-button>
        </div>
      </div>

      <div class="table-shell">
        <el-table
          v-loading="listLoading"
          :data="list"
          row-key="id"
          stripe
          show-overflow-tooltip
          @current-change="handleCurrentChange"
          @row-click="handleRowClick"
        >
          <el-table-column label="工单信息" min-width="240">
            <template #default="{ row }">
              <div class="stack-cell">
                <span class="stack-cell__main">{{ row.productionOrderNo || '-' }}</span>
                <span class="stack-cell__sub">{{ row.creatorName || '系统归集' }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="成本类型" min-width="120">
            <template #default="{ row }">{{ row.costTypeName || getCostTypeLabel(row.costType) }}</template>
          </el-table-column>
          <el-table-column label="归集月份" min-width="110">
            <template #default="{ row }">{{ row.accountingMonth || '-' }}</template>
          </el-table-column>
          <el-table-column label="金额" align="right" min-width="130">
            <template #default="{ row }">{{ formatAmount(row.amount) }}</template>
          </el-table-column>
          <el-table-column label="来源类型" min-width="120">
            <template #default="{ row }">{{ row.sourceTypeName || '-' }}</template>
          </el-table-column>
          <el-table-column label="备注" min-width="180">
            <template #default="{ row }">{{ row.remark || '-' }}</template>
          </el-table-column>
          <template #empty>
            <el-empty :description="emptyText">
              <template #image>
                <div class="empty-icon">
                  <Icon icon="ep:document" />
                </div>
              </template>
            </el-empty>
          </template>
        </el-table>
      </div>

      <Pagination
        v-if="total > 0"
        v-model:limit="queryParams.pageSize"
        v-model:page="queryParams.pageNo"
        :total="total"
        @pagination="getList"
      />
    </ContentWrap>

    <el-drawer
      v-model="detailDrawerVisible"
      :append-to-body="true"
      :size="drawerWidth"
      destroy-on-close
      :with-header="false"
      class="finance-cost-detail-drawer"
    >
      <div class="drawer-shell">
        <div class="drawer-shell__header">
          <div>
            <div class="drawer-shell__title">成本穿透查看</div>
            <div class="drawer-shell__meta">{{ detailHeader }}</div>
          </div>
          <el-button text @click="detailDrawerVisible = false">
            <Icon icon="ep:close" class="mr-5px" />
            关闭
          </el-button>
        </div>

        <div class="drawer-shell__body">
          <div class="context-card">
            <div class="context-card__title">{{ detailData?.productionOrderNo || '-' }}</div>
            <div class="context-card__metrics">
              <div>
                <div class="context-card__label">完工数量</div>
                <div class="context-card__value">{{ formatAmount(detailData?.finishedQty) }}</div>
              </div>
              <div>
                <div class="context-card__label">总成本</div>
                <div class="context-card__value context-card__value--emphasis">
                  {{ formatAmount(detailData?.totalCost) }}
                </div>
              </div>
              <div>
                <div class="context-card__label">单位成本</div>
                <div class="context-card__value">{{ formatAmount(detailData?.unitCost) }}</div>
              </div>
            </div>
          </div>

          <el-alert
            v-if="detailError"
            class="mb-12px"
            :closable="false"
            show-icon
            type="error"
            :title="detailError"
          />

          <div v-loading="detailLoading" class="detail-grid">
            <div class="detail-card">
              <div class="detail-card__title">成本构成</div>
              <div class="detail-card__list">
                <div v-for="item in detailMetricList" :key="item.label" class="detail-card__item">
                  <span>{{ item.label }}</span>
                  <strong>{{ formatAmount(item.value) }}</strong>
                </div>
              </div>
            </div>

            <div class="detail-card">
              <div class="detail-card__title">材料明细</div>
              <el-table :data="detailData?.materialDetails || []" size="small" stripe>
                <el-table-column label="领料单号" prop="issueNo" min-width="140" />
                <el-table-column label="领料时间" prop="issueTime" min-width="160">
                  <template #default="{ row }">{{ formatDateTime(row.issueTime) }}</template>
                </el-table-column>
                <el-table-column label="领料金额" align="right" min-width="120">
                  <template #default="{ row }">{{ formatAmount(row.issueAmount) }}</template>
                </el-table-column>
                <el-table-column label="备注" prop="remark" min-width="120" />
              </el-table>
            </div>

            <div class="detail-card detail-card--full">
              <div class="detail-card__title">成本归集明细</div>
              <el-table :data="detailData?.costEntries || []" size="small" stripe>
                <el-table-column label="成本类型" prop="costTypeName" min-width="120" />
                <el-table-column label="来源类型" prop="sourceTypeName" min-width="120" />
                <el-table-column label="来源单号" prop="sourceAllocationNo" min-width="140" />
                <el-table-column label="归集月份" prop="accountingMonth" min-width="110" />
                <el-table-column label="金额" align="right" min-width="120">
                  <template #default="{ row }">{{ formatAmount(row.amount) }}</template>
                </el-table-column>
                <el-table-column label="备注" prop="remark" min-width="140" />
              </el-table>
            </div>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { formatAmount } from '@/views/erp/finance/shared/accounting'
import { FinanceCostApi, type FinanceCostDetailRespVO, type FinanceCostEntryVO } from '@/api/erp/finance/cost'
import { ProductionOrderApi, type ProductionOrderVO } from '@/api/erp/mrp/production-order'

defineOptions({ name: 'ErpFinanceCost' })

type CostTypeOption = { label: string; value: number }

const costTypeOptions: CostTypeOption[] = [
  { label: '直接人工', value: 20 },
  { label: '折旧成本', value: 30 },
  { label: '电费成本', value: 40 },
  { label: '其他制造费用', value: 50 }
]

const queryFormRef = ref()
const listLoading = ref(false)
const detailLoading = ref(false)
const optionsLoading = ref(false)
const listError = ref('')
const detailError = ref('')
const list = ref<FinanceCostEntryVO[]>([])
const total = ref(0)
const detailDrawerVisible = ref(false)
const detailData = ref<FinanceCostDetailRespVO>()
const selectedRow = ref<FinanceCostEntryVO>()
const productionOrderOptions = ref<ProductionOrderVO[]>([])

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  productionOrderId: undefined as number | undefined,
  costType: undefined as number | undefined,
  accountingMonth: undefined as string | undefined
})

const hasSelectedRow = computed(() => !!selectedRow.value?.productionOrderId)
const emptyText = computed(() => (listError.value ? '成本明细加载失败' : '暂无成本明细'))
const pageAmountTotal = computed(() =>
  list.value.reduce((sum, item) => sum + Number(item.amount || 0), 0)
)
const costTypeCount = computed(
  () => new Set(list.value.map((item) => item.costType).filter((value) => value !== undefined)).size
)
const detailHeader = computed(() =>
  detailData.value?.productionOrderNo
    ? `工单 ${detailData.value.productionOrderNo}`
    : '请选择一条成本明细'
)
const drawerWidth = '86%'

const detailMetricList = computed(() => [
  { label: '材料成本', value: detailData.value?.materialCost },
  { label: '人工成本', value: detailData.value?.laborCost },
  { label: '折旧成本', value: detailData.value?.depreciationCost },
  { label: '电费成本', value: detailData.value?.powerCost },
  { label: '其他制造费用', value: detailData.value?.otherCost }
])

const getCostTypeLabel = (value?: number) =>
  costTypeOptions.find((item) => item.value === value)?.label || '-'

const formatDateTime = (value?: string) => {
  if (!value) {
    return '-'
  }
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }
  return date.toLocaleString('zh-CN', { hour12: false })
}

const formatProductionOrderLabel = (item: ProductionOrderVO) =>
  `${item.orderNo || '-'}${item.productName ? ` · ${item.productName}` : ''}`

const loadProductionOrderOptions = async () => {
  if (productionOrderOptions.value.length > 0) {
    return
  }
  optionsLoading.value = true
  try {
    const data = await ProductionOrderApi.getProductionOrderPage({ pageNo: 1, pageSize: 50 })
    productionOrderOptions.value = data.list || []
  } finally {
    optionsLoading.value = false
  }
}

const getList = async () => {
  listLoading.value = true
  listError.value = ''
  selectedRow.value = undefined
  try {
    const data = await FinanceCostApi.getPage({ ...queryParams })
    list.value = data.list || []
    total.value = data.total || 0
    if (list.value.length > 0) {
      selectedRow.value = list.value[0]
    }
  } catch {
    list.value = []
    total.value = 0
    listError.value = '成本明细加载失败，请稍后重试'
    selectedRow.value = undefined
  } finally {
    listLoading.value = false
  }
}

const handleQuery = async () => {
  queryParams.pageNo = 1
  await getList()
}

const resetQuery = async () => {
  queryFormRef.value?.resetFields()
  queryParams.pageNo = 1
  selectedRow.value = undefined
  await getList()
}

const handleCurrentChange = (row?: FinanceCostEntryVO) => {
  selectedRow.value = row
}

const handleRowClick = (row: FinanceCostEntryVO) => {
  selectedRow.value = row
}

const openDetailDrawer = async (row?: FinanceCostEntryVO) => {
  if (!row?.productionOrderId || detailLoading.value) {
    return
  }
  detailDrawerVisible.value = true
  detailLoading.value = true
  detailError.value = ''
  try {
    detailData.value = await FinanceCostApi.getDetail(row.productionOrderId)
  } catch {
    detailData.value = undefined
    detailError.value = '成本穿透加载失败，请重试'
  } finally {
    detailLoading.value = false
  }
}

onMounted(async () => {
  await loadProductionOrderOptions()
  await getList()
})
</script>

<style scoped>
.finance-cost-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-hero,
.section-head,
.drawer-shell__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.page-hero__title,
.section-head__title,
.drawer-shell__title {
  color: #0f172a;
  font-size: 18px;
  font-weight: 700;
}

.page-hero__actions,
.section-head__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.query-form {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.query-form__grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px 16px;
}

.query-form__grid :deep(.el-form-item) {
  margin-bottom: 0;
}

.query-form__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: flex-end;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.stat-card {
  border: 1px solid #d9d9d9;
  border-radius: 12px;
  background: #fff;
  padding: 12px 14px;
}

.stat-card--soft {
  background: #f8fbff;
}

.stat-card--success {
  border-color: #b7eb8f;
  background: #f6ffed;
}

.stat-card__label {
  color: #64748b;
  font-size: 12px;
  line-height: 1.4;
}

.stat-card__value {
  margin-top: 6px;
  color: #0f172a;
  font-size: 16px;
  font-weight: 700;
  line-height: 1.4;
}

.table-shell {
  overflow-x: auto;
}

.stack-cell {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 4px;
}

.stack-cell__main {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 600;
  color: #0f172a;
}

.stack-cell__sub {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #64748b;
  font-size: 12px;
}

.empty-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: #eff6ff;
  color: #3b82f6;
  font-size: 26px;
}

.drawer-shell {
  display: flex;
  height: 100%;
  flex-direction: column;
  background: #f8fafc;
}

.drawer-shell__header {
  padding: 20px 24px 16px;
  border-bottom: 1px solid #e2e8f0;
  background: #fff;
}

.drawer-shell__body {
  overflow: auto;
  padding: 16px 24px 24px;
}

.context-card {
  border-radius: 14px;
  background: linear-gradient(135deg, #0f172a 0%, #1f2937 100%);
  color: #fff;
  padding: 16px 18px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.18);
}

.context-card__title {
  font-size: 18px;
  font-weight: 700;
}

.context-card__metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-top: 16px;
}

.context-card__label {
  font-size: 12px;
  color: #cbd5e1;
}

.context-card__value {
  margin-top: 4px;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 18px;
  font-weight: 700;
}

.context-card__value--emphasis {
  color: #f87171;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  margin-top: 16px;
}

.detail-card {
  min-width: 0;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  background: #fff;
  padding: 16px;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.05);
}

.detail-card--full {
  grid-column: 1 / -1;
}

.detail-card__title {
  margin-bottom: 12px;
  font-weight: 700;
  color: #0f172a;
}

.detail-card__list {
  display: grid;
  gap: 10px;
}

.detail-card__item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 12px;
  background: #f8fafc;
}

@media (max-width: 1279px) {
  .stats-row,
  .query-form__grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .query-form__grid,
  .detail-grid,
  .context-card__metrics {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 767px) {
  .page-hero,
  .section-head,
  .drawer-shell__header {
    flex-direction: column;
  }

  .stats-row,
  .query-form__grid,
  .detail-grid,
  .context-card__metrics {
    grid-template-columns: 1fr;
  }
}
</style>
