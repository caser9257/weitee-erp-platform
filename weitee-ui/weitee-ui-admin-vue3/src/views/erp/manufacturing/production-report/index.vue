<template>
  <section class="pr-hero">
    <div class="pr-hero__header">
      <div>
        <div class="pr-hero__breadcrumb">生产执行 / 生产报工</div>
        <div class="pr-page__title">生产报工</div>
      </div>
    </div>
  </section>

  <ContentWrap class="pr-page__filter-card">
    <el-form label-position="top" class="pr-query">
      <div class="pr-query__grid">
        <el-form-item label="生产工单" prop="productionOrderId">
          <el-select
            v-model="selectedOrderId"
            filterable
            clearable
            placeholder="请选择生产工单"
            style="width: 100%"
            :loading="orderLoading"
            @change="handleOrderChange"
          >
            <el-option
              v-for="item in orderOptions"
              :key="item.id"
              :label="`${item.orderNo}（${item.productName || '-'}）`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
      </div>
      <div class="pr-query__footer">
        <div class="pr-query__actions">
          <el-button type="primary" :loading="orderLoading" @click="loadOrders">
            <Icon icon="ep:refresh" class="mr-5px" /> 刷新工单
          </el-button>
        </div>
      </div>
    </el-form>
  </ContentWrap>

  <ContentWrap v-if="currentOrder" class="pr-page__order-card">
    <div class="pr-order-summary">
      <div class="pr-order-summary__no">{{ currentOrder.orderNo || '-' }}</div>
      <div class="pr-order-summary__product">{{ currentOrder.productName || '-' }}</div>
      <div class="pr-order-summary__meta">计划数量 {{ formatCount(currentOrder.planQty) }}</div>
      <span class="pr-badge" :class="resolveOrderStatusBadgeClass(currentOrder.status)">
        {{ resolveOrderStatusLabel(currentOrder.status) }}
      </span>
    </div>

    <el-table v-loading="stepLoading" :data="stepList" :stripe="true" class="pr-step-ledger">
      <template #empty>
        <div class="pr-empty">
          <div class="pr-empty__icon">
            <Icon icon="ep:set-up" />
          </div>
          <div class="pr-empty__title">该工单暂无工序快照</div>
        </div>
      </template>
      <el-table-column label="工序" min-width="200">
        <template #default="{ row }">
          <div class="pr-ledger__order">
            <div class="pr-ledger__order-top">
              <div class="pr-ledger__order-no">{{ row.stepNo }} · {{ row.stepName }}</div>
              <span class="pr-badge" :class="resolveStepStatusBadgeClass(row.stepStatus)">
                {{ resolveStepStatusLabel(row.stepStatus) }}
              </span>
            </div>
            <div class="pr-ledger__order-meta">{{ row.stepCode }}</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="计划数量" min-width="100" align="right">
        <template #default="{ row }">{{ formatCount(row.planQty) }}</template>
      </el-table-column>
      <el-table-column label="累计报工" min-width="150">
        <template #default="{ row }">
          <div class="pr-ledger__progress">
            <div class="pr-ledger__progress-top">
              <strong>{{ formatCount(row.reportedQty) }}</strong>
              <span
                >合格 {{ formatCount(row.qualifiedQty) }} / 报废
                {{ formatCount(row.scrapQty) }}</span
              >
            </div>
            <el-progress
              :stroke-width="6"
              :show-text="false"
              :percentage="getStepPercent(row)"
              :color="resolveStepProgressColor(row)"
            />
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="196" align="center">
        <template #default="{ row }">
          <div class="pr-row-actions">
            <el-button
              v-if="canStart(row)"
              link
              type="primary"
              :loading="stepBusyIds.includes(row.id)"
              @click="handleStart(row)"
            >
              开工
            </el-button>
            <template v-if="canPauseOrResume(row)">
              <el-button
                v-if="row.stepStatus === STEP_STATUS.PROCESSING"
                link
                type="warning"
                :loading="stepBusyIds.includes(row.id)"
                @click="handlePause(row)"
              >
                暂停
              </el-button>
              <el-button
                v-else
                link
                type="warning"
                :loading="stepBusyIds.includes(row.id)"
                @click="handleResume(row)"
              >
                恢复
              </el-button>
            </template>
            <el-button v-if="canReport(row)" link type="success" @click="openReportDialog(row)">
              报工
            </el-button>
            <el-button v-if="hasQualityEntry(row)" link type="warning" @click="goStepQuality">
              质检
            </el-button>
            <el-button
              v-if="canFinish(row)"
              link
              type="primary"
              :loading="stepBusyIds.includes(row.id)"
              @click="handleStepFinish(row)"
            >
              完工
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
  </ContentWrap>

  <ContentWrap class="pr-page__list-card">
    <div class="pr-toolbar">
      <div class="pr-toolbar__title">报工记录</div>
    </div>
    <el-table v-loading="reportLoading" :data="reportList" :stripe="true">
      <template #empty>
        <div class="pr-empty">
          <div class="pr-empty__icon">
            <Icon icon="ep:document" />
          </div>
          <div class="pr-empty__title">暂无报工记录</div>
        </div>
      </template>
      <el-table-column label="报工单号" min-width="160">
        <template #default="{ row }">
          <div class="pr-ledger__order-no">{{ row.reportNo }}</div>
        </template>
      </el-table-column>
      <el-table-column label="生产工单" min-width="180" prop="productionOrderNo" />
      <el-table-column label="报工时间" min-width="150">
        <template #default="{ row }">{{ formatDateValue(row.reportDate) }}</template>
      </el-table-column>
      <el-table-column label="报工类型" min-width="100" align="center">
        <template #default="{ row }">
          <el-tag size="small" effect="light" :type="row.reportType === 1 ? 'primary' : 'success'">
            {{ resolveReportTypeLabel(row.reportType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="批次号" min-width="120" prop="batchNo" />
      <el-table-column label="备注" min-width="160" prop="remark" />
      <el-table-column label="操作" fixed="right" width="100" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="openReportDetail(row)">查看</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination
      :total="reportTotal"
      v-model:page="reportQueryParams.pageNo"
      v-model:limit="reportQueryParams.pageSize"
      @pagination="getReportList"
    />
  </ContentWrap>

  <Dialog v-model="reportDialogVisible" title="工序报工" width="520">
    <el-form label-width="100px">
      <div class="pr-report-step">
        <div class="pr-report-step__name"
          >{{ reportTarget?.stepNo }} · {{ reportTarget?.stepName }}</div
        >
        <div class="pr-report-step__meta">
          计划 {{ formatCount(reportTarget?.planQty) }}，已报
          {{ formatCount(reportTarget?.reportedQty) }}，可报
          {{ formatCount(getRemainingQty(reportTarget)) }}
        </div>
      </div>
      <el-form-item label="报工数量" required>
        <el-input-number
          v-model="reportForm.reportedQty"
          controls-position="right"
          :min="0.000001"
          :max="getRemainingQty(reportTarget)"
          :precision="quantityPrecision"
          :step="quantityStep"
          class="!w-100%"
          @change="syncReportCounts"
        />
      </el-form-item>
      <el-form-item label="合格数量" required>
        <el-input-number
          v-model="reportForm.qualifiedQty"
          controls-position="right"
          :min="0"
          :precision="quantityPrecision"
          :step="quantityStep"
          class="!w-100%"
          @change="syncReportCounts"
        />
      </el-form-item>
      <el-form-item label="报废数量" required>
        <el-input-number
          v-model="reportForm.scrapQty"
          controls-position="right"
          :min="0"
          :precision="quantityPrecision"
          :step="quantityStep"
          class="!w-100%"
          @change="syncReportCounts"
        />
      </el-form-item>
      <el-form-item label="工时">
        <el-input-number
          v-model="reportForm.workHour"
          controls-position="right"
          :min="0"
          :precision="3"
          class="!w-100%"
        />
      </el-form-item>
      <el-form-item label="批次号">
        <el-input v-model="reportForm.batchNo" placeholder="请输入批次号" maxlength="64" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="reportForm.remark" type="textarea" :rows="2" maxlength="255" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="reportDialogVisible = false">取消</el-button>
      <el-button type="success" :loading="reportSubmitLoading" @click="handleReportSubmit">
        提交报工
      </el-button>
    </template>
  </Dialog>

  <Dialog v-model="detailVisible" title="报工详情" width="640">
    <el-descriptions v-if="detailForm.reportNo" :column="2" border class="mb-12px">
      <el-descriptions-item label="报工单号">{{ detailForm.reportNo }}</el-descriptions-item>
      <el-descriptions-item label="生产工单">{{
        detailForm.productionOrderNo || '-'
      }}</el-descriptions-item>
      <el-descriptions-item label="报工时间">{{
        formatDateValue(detailForm.reportDate)
      }}</el-descriptions-item>
      <el-descriptions-item label="报工类型">
        {{ resolveReportTypeLabel(detailForm.reportType) }}
      </el-descriptions-item>
      <el-descriptions-item label="批次号">{{ detailForm.batchNo || '-' }}</el-descriptions-item>
      <el-descriptions-item label="备注">{{ detailForm.remark || '-' }}</el-descriptions-item>
    </el-descriptions>
    <el-table v-if="detailItems.length" :data="detailItems" :stripe="true">
      <el-table-column label="工序" min-width="140">
        <template #default="{ row }">{{
          row.stepNo ? `${row.stepNo} · ${row.stepName}` : row.stepName
        }}</template>
      </el-table-column>
      <el-table-column label="报工数量" min-width="100" align="right">
        <template #default="{ row }">{{ formatCount(row.reportedQty) }}</template>
      </el-table-column>
      <el-table-column label="合格" min-width="90" align="right">
        <template #default="{ row }">{{ formatCount(row.qualifiedQty) }}</template>
      </el-table-column>
      <el-table-column label="报废" min-width="90" align="right">
        <template #default="{ row }">{{ formatCount(row.scrapQty) }}</template>
      </el-table-column>
      <el-table-column label="工时" min-width="90" align="right">
        <template #default="{ row }">{{ row.workHour ?? '-' }}</template>
      </el-table-column>
    </el-table>
    <template #footer>
      <el-button @click="detailVisible = false">关闭</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { formatDate } from '@/utils/formatTime'
import { erpCountInputFormatter } from '@/utils'
import { getProductQuantityPrecision, getProductQuantityStep } from '@/utils/erpQuantityPrecision'
import { ProductApi, ProductVO } from '@/api/erp/product/product'
import { ProductionOrderApi, ProductionOrderVO } from '@/api/erp/mrp/production-order'
import {
  ProductionOrderStepVO,
  ProductionReportApi,
  ProductionReportItemVO,
  ProductionReportVO
} from '@/api/erp/manufacturing/production-report'

defineOptions({ name: 'ErpProductionReport' })

const STEP_STATUS = {
  WAIT: 0,
  PROCESSING: 1,
  FINISHED: 2,
  PAUSED: 3
} as const

const ORDER_STATUS = {
  CREATED: 0,
  RELEASED: 10,
  FINISHED: 20,
  CLOSED: 30
} as const

const message = useMessage()

const orderLoading = ref(false)
const stepLoading = ref(false)
const reportLoading = ref(false)
const reportSubmitLoading = ref(false)
const stepBusyIds = ref<number[]>([])
const reportDialogVisible = ref(false)
const detailVisible = ref(false)

const orderOptions = ref<ProductionOrderVO[]>([])
const selectedOrderId = ref<number>()
const currentOrder = ref<ProductionOrderVO>()
const stepList = ref<ProductionOrderStepVO[]>([])
const reportList = ref<ProductionReportVO[]>([])
const reportTotal = ref(0)
const productList = ref<ProductVO[]>([])
const reportTarget = ref<ProductionOrderStepVO>()
const detailForm = ref<Partial<ProductionReportVO>>({})
const detailItems = ref<ProductionReportItemVO[]>([])

const reportQueryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  productionOrderId: undefined as number | undefined
})

const reportForm = reactive({
  reportedQty: undefined as number | undefined,
  qualifiedQty: undefined as number | undefined,
  scrapQty: undefined as number | undefined,
  workHour: undefined as number | undefined,
  batchNo: undefined as string | undefined,
  remark: undefined as string | undefined
})

const quantityPrecision = computed(() =>
  getProductQuantityPrecision(productList.value, currentOrder.value?.productId)
)
const quantityStep = computed(() =>
  getProductQuantityStep(productList.value, currentOrder.value?.productId)
)

const formatCount = (value?: number) => erpCountInputFormatter(value || 0)

const formatDateValue = (value?: string | Date | number) =>
  value ? formatDate(value, 'YYYY-MM-DD HH:mm') : '-'

const resolveOrderStatusLabel = (status?: number) => {
  if (status === ORDER_STATUS.CREATED) return '已创建'
  if (status === ORDER_STATUS.RELEASED) return '已下达'
  if (status === ORDER_STATUS.FINISHED) return '已完工'
  if (status === ORDER_STATUS.CLOSED) return '已关闭'
  return '-'
}

const resolveOrderStatusBadgeClass = (status?: number) => {
  if (status === ORDER_STATUS.RELEASED) return 'pr-badge--primary'
  if (status === ORDER_STATUS.FINISHED) return 'pr-badge--success'
  if (status === ORDER_STATUS.CLOSED) return 'pr-badge--info'
  return 'pr-badge--slate'
}

const resolveStepStatusLabel = (status?: number) => {
  if (status === STEP_STATUS.WAIT) return '待开始'
  if (status === STEP_STATUS.PROCESSING) return '进行中'
  if (status === STEP_STATUS.FINISHED) return '已完成'
  if (status === STEP_STATUS.PAUSED) return '已暂停'
  return '-'
}

const resolveStepStatusBadgeClass = (status?: number) => {
  if (status === STEP_STATUS.WAIT) return 'pr-badge--slate'
  if (status === STEP_STATUS.PROCESSING) return 'pr-badge--primary'
  if (status === STEP_STATUS.FINISHED) return 'pr-badge--success'
  if (status === STEP_STATUS.PAUSED) return 'pr-badge--warning'
  return 'pr-badge--slate'
}

const resolveReportTypeLabel = (type?: number) => (type === 2 ? '完工报工' : '工序报工')

const getStepPercent = (row: ProductionOrderStepVO) => {
  if (Number(row.planQty || 0) <= 0) return 0
  return Math.min(Math.round((Number(row.reportedQty || 0) / Number(row.planQty)) * 100), 100)
}

const resolveStepProgressColor = (row: ProductionOrderStepVO) => {
  if (row.stepStatus === STEP_STATUS.FINISHED) return 'var(--erp-success-500)'
  if (row.stepStatus === STEP_STATUS.PROCESSING) return 'var(--erp-primary-500)'
  return 'var(--erp-slate-300)'
}

const getRemainingQty = (row?: ProductionOrderStepVO) =>
  Math.max(Number(row?.planQty || 0) - Number(row?.reportedQty || 0), 0)

const canStart = (row: ProductionOrderStepVO) =>
  row.stepStatus === STEP_STATUS.WAIT && !stepBusyIds.value.includes(row.id)

const canPauseOrResume = (row: ProductionOrderStepVO) =>
  (row.stepStatus === STEP_STATUS.PROCESSING || row.stepStatus === STEP_STATUS.PAUSED) &&
  !stepBusyIds.value.includes(row.id)

const canReport = (row: ProductionOrderStepVO) =>
  row.stepStatus === STEP_STATUS.PROCESSING && getRemainingQty(row) > 0

const canFinish = (row: ProductionOrderStepVO) =>
  row.stepStatus === STEP_STATUS.PROCESSING && !stepBusyIds.value.includes(row.id)

const hasQualityEntry = (row: ProductionOrderStepVO) =>
  row.qcFlag === true && Number(row.reportedQty || 0) > 0

const goStepQuality = () => {
  const { push } = useRouter()
  push('/qms/step-quality')
}

const loadOrders = async () => {
  orderLoading.value = true
  try {
    const data = await ProductionOrderApi.getProductionOrderPage({
      pageNo: 1,
      pageSize: 200
    })
    orderOptions.value = data.list || []
  } finally {
    orderLoading.value = false
  }
}

const handleOrderChange = async (orderId?: number) => {
  currentOrder.value = orderId ? orderOptions.value.find((item) => item.id === orderId) : undefined
  stepList.value = []
  reportQueryParams.pageNo = 1
  reportQueryParams.productionOrderId = orderId
  if (!orderId) {
    reportList.value = []
    reportTotal.value = 0
    return
  }
  stepLoading.value = true
  try {
    stepList.value = await ProductionReportApi.getProductionOrderStepList(orderId)
  } catch {
    stepList.value = []
    message.error('工序列表加载失败，请重试')
  } finally {
    stepLoading.value = false
  }
  await getReportList()
}

const getReportList = async () => {
  if (!reportQueryParams.productionOrderId) return
  reportLoading.value = true
  try {
    const data = await ProductionReportApi.getProductionReportPage(reportQueryParams)
    reportList.value = data.list || []
    reportTotal.value = data.total || 0
  } catch {
    reportList.value = []
    reportTotal.value = 0
  } finally {
    reportLoading.value = false
  }
}

const runStepAction = async (
  row: ProductionOrderStepVO,
  action: () => Promise<unknown>,
  successMsg: string
) => {
  stepBusyIds.value = [...stepBusyIds.value, row.id]
  try {
    await action()
    message.success(successMsg)
    stepList.value = await ProductionReportApi.getProductionOrderStepList(currentOrder.value!.id)
  } catch {
  } finally {
    stepBusyIds.value = stepBusyIds.value.filter((id) => id !== row.id)
  }
}

const handleStart = (row: ProductionOrderStepVO) =>
  runStepAction(row, () => ProductionReportApi.startStep(row.id), '工序开工成功')

const handlePause = (row: ProductionOrderStepVO) =>
  runStepAction(row, () => ProductionReportApi.pauseStep(row.id), '工序已暂停')

const handleResume = (row: ProductionOrderStepVO) =>
  runStepAction(row, () => ProductionReportApi.resumeStep(row.id), '工序已恢复')

const handleStepFinish = async (row: ProductionOrderStepVO) => {
  try {
    await message.confirm(`确定完工工序「${row.stepNo} · ${row.stepName}」吗？`)
  } catch {
    return
  }
  await runStepAction(row, () => ProductionReportApi.finishStep(row.id), '工序完工成功')
}

const openReportDialog = (row: ProductionOrderStepVO) => {
  reportTarget.value = row
  reportForm.reportedQty = undefined
  reportForm.qualifiedQty = undefined
  reportForm.scrapQty = undefined
  reportForm.workHour = undefined
  reportForm.batchNo = undefined
  reportForm.remark = undefined
  reportDialogVisible.value = true
}

const syncReportCounts = () => {
  const reported = Number(reportForm.reportedQty || 0)
  const qualified = Number(reportForm.qualifiedQty || 0)
  const scrap = Number(reportForm.scrapQty || 0)
  if (qualified + scrap !== reported) {
    if (reported > 0) {
      reportForm.scrapQty = round(reported - qualified)
    }
  }
}

const round = (value: number) => Number(value.toFixed(quantityPrecision.value))

const handleReportSubmit = async () => {
  const reported = Number(reportForm.reportedQty || 0)
  const qualified = Number(reportForm.qualifiedQty || 0)
  const scrap = Number(reportForm.scrapQty || 0)
  if (reported <= 0) {
    message.warning('报工数量必须大于 0')
    return
  }
  if (!reportTarget.value || !currentOrder.value?.id) return
  reportSubmitLoading.value = true
  try {
    await ProductionReportApi.createProductionReport({
      productionOrderId: currentOrder.value.id,
      reportType: 1,
      batchNo: reportForm.batchNo,
      remark: reportForm.remark,
      items: [
        {
          productionOrderStepId: reportTarget.value.id,
          reportedQty: round(reported),
          qualifiedQty: round(qualified),
          scrapQty: round(scrap),
          workHour: reportForm.workHour ? round(reportForm.workHour) : undefined,
          batchNo: reportForm.batchNo
        }
      ]
    })
    message.success('提交报工成功')
    reportDialogVisible.value = false
    stepList.value = await ProductionReportApi.getProductionOrderStepList(currentOrder.value.id)
    await getReportList()
  } finally {
    reportSubmitLoading.value = false
  }
}

const openReportDetail = async (row: ProductionReportVO) => {
  detailVisible.value = true
  detailForm.value = {}
  detailItems.value = []
  try {
    const [detail, items] = await Promise.all([
      ProductionReportApi.getProductionReport(row.id),
      ProductionReportApi.getProductionReportItems(row.id)
    ])
    detailForm.value = detail
    detailItems.value = items || []
  } catch {
    message.error('报工详情加载失败')
  }
}

onMounted(async () => {
  loadOrders()
  try {
    productList.value = await ProductApi.getProductSimpleList()
  } catch {
    productList.value = []
  }
})
</script>

<style scoped>
.pr-hero {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 16px;
}

.pr-hero__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.pr-hero__breadcrumb {
  color: var(--erp-slate-400);
  font-size: 12px;
  font-weight: 600;
  line-height: 18px;
  margin-bottom: 6px;
}

.pr-page__title {
  color: var(--erp-slate-900);
  font-size: 28px;
  font-weight: 800;
  line-height: 36px;
}

.pr-page__filter-card,
.pr-page__order-card,
.pr-page__list-card {
  border: 1px solid var(--erp-slate-200);
  border-radius: 20px;
  background: var(--erp-surface-white);
  box-shadow: var(--erp-shadow-sm);
}

.pr-query {
  :deep(.el-form-item) {
    margin-bottom: 0;
  }

  :deep(.el-form-item__label) {
    padding-bottom: 6px;
    color: var(--erp-slate-600);
    font-size: 12px;
    font-weight: 600;
    line-height: 18px;
  }

  :deep(.el-input__wrapper),
  :deep(.el-select__wrapper) {
    min-height: 38px;
    padding: 0 12px;
    border: 1px solid var(--erp-slate-200);
    border-radius: 10px;
    background: var(--erp-slate-50);
    box-shadow: inset 0 1px 1px rgba(15, 23, 42, 0.02);
  }

  :deep(.el-input__wrapper.is-focus),
  :deep(.el-select__wrapper.is-focused) {
    border-color: var(--erp-primary-300);
    background: var(--erp-surface-white);
    box-shadow: 0 0 0 3px var(--erp-primary-50);
  }
}

.pr-query__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.pr-query__footer {
  margin-top: 14px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
}

.pr-order-summary {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 14px;
}

.pr-order-summary__no {
  color: var(--erp-slate-900);
  font-size: 18px;
  font-weight: 800;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono',
    'Courier New', monospace;
}

.pr-order-summary__product {
  color: var(--erp-slate-700);
  font-size: 14px;
  font-weight: 600;
}

.pr-order-summary__meta {
  color: var(--erp-slate-500);
  font-size: 12px;
}

.pr-badge {
  display: inline-flex;
  align-items: center;
  min-height: 22px;
  padding: 2px 10px;
  border-radius: 999px;
  border: 1px solid transparent;
  font-size: 11px;
  font-weight: 700;
  line-height: 16px;
  white-space: nowrap;
}

.pr-badge--slate {
  background: var(--erp-slate-100);
  border-color: var(--erp-slate-200);
  color: var(--erp-slate-600);
}

.pr-badge--primary {
  background: var(--erp-primary-50);
  border-color: var(--erp-primary-200);
  color: var(--erp-primary-600);
}

.pr-badge--success {
  background: var(--erp-success-50);
  border-color: var(--erp-success-200);
  color: var(--erp-success-600);
}

.pr-badge--warning {
  background: var(--erp-warning-50);
  border-color: var(--erp-warning-200);
  color: var(--erp-warning-600);
}

.pr-badge--info {
  background: var(--erp-slate-100);
  border-color: var(--erp-slate-200);
  color: var(--erp-slate-500);
}

.pr-step-ledger {
  width: 100%;

  :deep(.el-table__header-wrapper th) {
    background: var(--erp-slate-50);
    color: var(--erp-slate-500);
    font-size: 11px;
    font-weight: 700;
  }

  :deep(.el-table__cell) {
    padding-right: 8px;
    padding-left: 8px;
  }

  :deep(.el-table__row td) {
    padding-top: 14px;
    padding-bottom: 14px;
    vertical-align: top;
  }

  :deep(.el-progress-bar__outer) {
    background: var(--erp-slate-200);
  }
}

.pr-ledger__order,
.pr-ledger__progress {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 6px;
}

.pr-ledger__order-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 8px;
  flex-wrap: wrap;
}

.pr-ledger__order-no {
  color: var(--erp-slate-900);
  font-weight: 800;
  line-height: 22px;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono',
    'Courier New', monospace;
}

.pr-ledger__order-meta {
  color: var(--erp-slate-500);
  font-size: 11px;
  line-height: 17px;
}

.pr-ledger__progress-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: var(--erp-slate-500);
  font-size: 11px;
  line-height: 17px;
}

.pr-ledger__progress-top strong {
  color: var(--erp-slate-900);
  font-weight: 700;
}

.pr-row-actions {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  flex-wrap: nowrap;
}

.pr-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--erp-slate-200);
  flex-wrap: wrap;
}

.pr-toolbar__title {
  color: var(--erp-slate-800);
  font-size: 14px;
  font-weight: 700;
  line-height: 22px;
}

.pr-empty {
  min-height: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--erp-slate-500);
}

.pr-empty__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 16px;
  background: var(--erp-stat-gradient-blue);
  color: var(--erp-primary-600);
  font-size: 22px;
}

.pr-empty__title {
  color: var(--erp-slate-900);
  font-size: 14px;
  font-weight: 600;
  line-height: 22px;
}

.pr-report-step {
  margin-bottom: 16px;
  padding: 12px 14px;
  border: 1px solid var(--erp-slate-200);
  border-radius: 12px;
  background: var(--erp-slate-50);
}

.pr-report-step__name {
  color: var(--erp-slate-900);
  font-size: 14px;
  font-weight: 700;
  line-height: 22px;
  margin-bottom: 4px;
}

.pr-report-step__meta {
  color: var(--erp-slate-500);
  font-size: 12px;
  line-height: 18px;
}

@media (max-width: 1024px) {
  .pr-query__grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
