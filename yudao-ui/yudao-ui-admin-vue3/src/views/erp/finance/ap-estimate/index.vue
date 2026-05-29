<template>
  <div class="finance-ap-estimate-page">
    <ContentWrap>
      <el-form
        ref="queryFormRef"
        :model="queryParams"
        label-width="88px"
        class="finance-ap-estimate-page__query-form"
      >
        <div class="finance-ap-estimate-page__card-header">
          <div class="finance-ap-estimate-page__card-title">基础筛选</div>
          <el-button link type="primary" @click="advancedExpanded = !advancedExpanded">
            <Icon :icon="advancedExpanded ? 'ep:arrow-up' : 'ep:arrow-down'" class="mr-5px" />
            {{ advancedExpanded ? '收起高级筛选' : '展开高级筛选' }}
          </el-button>
        </div>

        <div class="finance-ap-estimate-page__query-grid finance-ap-estimate-page__query-grid--basic">
          <el-form-item label="暂估单号" prop="estimateNo">
            <el-input
              v-model="queryParams.estimateNo"
              placeholder="请输入暂估单号"
              clearable
              class="!w-full"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="暂估月份" prop="estimateMonth">
            <el-date-picker
              v-model="queryParams.estimateMonth"
              type="month"
              value-format="YYYY-MM"
              placeholder="请选择暂估月份"
              class="!w-full"
            />
          </el-form-item>
          <el-form-item label="入库单号" prop="sourcePurchaseInNo">
            <el-input
              v-model="queryParams.sourcePurchaseInNo"
              placeholder="请输入入库单号"
              clearable
              class="!w-full"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="供应商" prop="supplierId">
            <el-select
              v-model="queryParams.supplierId"
              placeholder="请选择供应商"
              clearable
              filterable
              :loading="supplierLoading"
              class="!w-full"
            >
              <el-option
                v-for="item in supplierList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </div>

        <div v-if="advancedExpanded" class="finance-ap-estimate-page__advanced-block">
          <div class="finance-ap-estimate-page__section-title">高级筛选</div>
          <div class="finance-ap-estimate-page__query-grid finance-ap-estimate-page__query-grid--advanced">
            <el-form-item label="状态" prop="status">
              <el-select v-model="queryParams.status" placeholder="请选择状态" clearable class="!w-full">
                <el-option
                  v-for="item in ERP_AP_ESTIMATE_STATUS_OPTIONS"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </div>
        </div>

        <div class="finance-ap-estimate-page__query-actions">
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
      <div class="finance-ap-estimate-page__toolbar">
        <div class="finance-ap-estimate-page__toolbar-left">
          <el-button
            type="primary"
            plain
            :loading="scanLoading"
            :disabled="listLoading"
            v-hasPermi="['erp:ap-estimate:scan']"
            @click="openScanDialog"
          >
            <Icon icon="ep:calendar" class="mr-5px" />
            月末扫描
          </el-button>
          <el-button
            type="success"
            plain
            :loading="confirmLoading"
            :disabled="batchConfirmDisabled"
            v-hasPermi="['erp:ap-estimate:update']"
            @click="handleBatchConfirm"
          >
            <Icon icon="ep:select" class="mr-5px" />
            批量确认
          </el-button>
          <el-button
            type="warning"
            plain
            :loading="reverseLoading"
            :disabled="batchReverseDisabled"
            v-hasPermi="['erp:ap-estimate:update']"
            @click="handleBatchReverse"
          >
            <Icon icon="ep:refresh-left" class="mr-5px" />
            批量冲回
          </el-button>
        </div>

        <div class="finance-ap-estimate-page__toolbar-right">
          <el-button
            type="success"
            plain
            :loading="exportLoading"
            :disabled="listLoading"
            v-hasPermi="['erp:ap-estimate:export']"
            @click="handleExport"
          >
            <Icon icon="ep:download" class="mr-5px" />
            导出
          </el-button>
        </div>
      </div>

      <div v-if="listErrorMessage && !list.length" class="finance-ap-estimate-page__state">
        <el-result icon="error" title="暂估列表加载失败" :sub-title="listErrorMessage">
          <template #extra>
            <el-button type="primary" @click="getList">重试</el-button>
          </template>
        </el-result>
      </div>
      <template v-else>
        <div v-if="listLoading || list.length" class="finance-ap-estimate-page__table-wrap">
          <el-table
            ref="tableRef"
            v-loading="listLoading"
            :data="list"
            stripe
            row-key="id"
            show-overflow-tooltip
            @selection-change="handleSelectionChange"
          >
            <el-table-column type="selection" width="42" :selectable="canSelectRow" />
            <el-table-column label="暂估信息" min-width="200">
              <template #default="{ row }">
                <div class="finance-ap-estimate-page__primary-cell">
                  <span class="finance-ap-estimate-page__primary-text">{{ row.estimateNo || '-' }}</span>
                  <span class="finance-ap-estimate-page__muted-text">{{ row.estimateMonth || '-' }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="来源单据" min-width="220">
              <template #default="{ row }">
                <div class="finance-ap-estimate-page__primary-cell">
                  <span class="finance-ap-estimate-page__primary-text">
                    {{ row.sourcePurchaseInNo || '-' }}
                  </span>
                  <span class="finance-ap-estimate-page__muted-text" :title="row.sourceOrderNo || '-'">
                    {{ row.sourceOrderNo || '未关联采购订单' }}
                  </span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="业务信息" min-width="220">
              <template #default="{ row }">
                <div class="finance-ap-estimate-page__primary-cell">
                  <span class="finance-ap-estimate-page__primary-text">
                    {{ row.supplierName || '-' }}
                  </span>
                  <span class="finance-ap-estimate-page__muted-text">
                    {{ row.accountName || '-' }}
                  </span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="金额信息" align="right" min-width="160">
              <template #default="{ row }">
                <div class="finance-ap-estimate-page__amount-cell font-mono">
                  <span class="finance-ap-estimate-page__muted-text">
                    来源 {{ formatAmount(row.sourceAmount) }}
                  </span>
                  <span class="finance-ap-estimate-page__amount-text">
                    暂估 {{ formatAmount(row.amount) }}
                  </span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="状态" align="center" width="110">
              <template #default="{ row }">
                <el-tag :type="getStatusTagType(row.status)" effect="light" round>
                  {{ row.statusName || getStatusLabel(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="确认/冲回" min-width="200">
              <template #default="{ row }">
                <div class="finance-ap-estimate-page__primary-cell">
                  <span class="finance-ap-estimate-page__muted-text">
                    确认：{{ formatDateValue(row.confirmTime) }}
                  </span>
                  <span class="finance-ap-estimate-page__muted-text">
                    冲回：{{ formatDateValue(row.reverseTime) }}
                  </span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="创建信息" min-width="180">
              <template #default="{ row }">
                <div class="finance-ap-estimate-page__primary-cell">
                  <span class="finance-ap-estimate-page__muted-text">
                    {{ row.creatorName || row.creator || '-' }}
                  </span>
                  <span class="finance-ap-estimate-page__muted-text">
                    {{ formatDateValue(row.createTime) }}
                  </span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="操作" align="center" fixed="right" width="240">
              <template #default="{ row }">
                <el-button
                  link
                  :disabled="isActionBusy"
                  v-hasPermi="['erp:ap-estimate:query']"
                  @click="openDetailDialog(row.id)"
                >
                  详情
                </el-button>
                <el-button
                  v-if="row.status === 10"
                  link
                  type="success"
                  :disabled="isActionBusy"
                  v-hasPermi="['erp:ap-estimate:update']"
                  @click="handleSingleConfirm(row.id)"
                >
                  确认
                </el-button>
                <el-button
                  v-else-if="row.status === 20"
                  link
                  type="warning"
                  :disabled="isActionBusy"
                  v-hasPermi="['erp:ap-estimate:update']"
                  @click="handleSingleReverse(row.id)"
                >
                  冲回
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <el-empty v-else class="finance-ap-estimate-page__state" description="暂无暂估入库数据" />

        <Pagination
          v-if="total > 0"
          :total="total"
          v-model:page="queryParams.pageNo"
          v-model:limit="queryParams.pageSize"
          @pagination="handlePagination"
        />
      </template>
    </ContentWrap>

    <Dialog
      v-model="scanDialogVisible"
      title="月末扫描"
      width="min(720px, 96vw)"
      scroll
      maxHeight="72vh"
    >
      <el-form
        ref="scanFormRef"
        :model="scanFormData"
        :rules="scanFormRules"
        label-width="88px"
        class="finance-ap-estimate-page__dialog-form"
      >
        <el-form-item label="暂估月份" prop="estimateMonth">
          <el-date-picker
            v-model="scanFormData.estimateMonth"
            type="month"
            value-format="YYYY-MM"
            placeholder="请选择暂估月份"
            class="!w-full"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="scanDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="scanLoading" @click="submitScanMonth">
          扫描
        </el-button>
      </template>
    </Dialog>

    <Dialog
      v-model="detailDialogVisible"
      title="暂估详情"
      width="min(1120px, 96vw)"
      scroll
      maxHeight="82vh"
    >
      <div class="finance-ap-estimate-detail" v-loading="detailLoading">
        <el-result
          v-if="detailErrorMessage"
          icon="error"
          title="暂估详情加载失败"
          :sub-title="detailErrorMessage"
        >
          <template #extra>
            <el-button type="primary" :disabled="detailLoading || detailId == null" @click="loadDetail">
              重试
            </el-button>
          </template>
        </el-result>

        <template v-else-if="detailData">
          <div class="finance-ap-estimate-detail__hero">
            <div>
              <div class="finance-ap-estimate-detail__eyebrow">暂估入库</div>
              <div class="finance-ap-estimate-detail__title">
                {{ detailData.estimateNo || `#${detailData.id}` }}
              </div>
              <div class="finance-ap-estimate-detail__meta">
                <span>{{ detailData.estimateMonth || '-' }}</span>
                <span>{{ detailData.sourcePurchaseInNo || '-' }}</span>
                <span>{{ detailData.sourceOrderNo || '未关联采购订单' }}</span>
              </div>
            </div>
            <el-tag :type="getStatusTagType(detailData.status)" effect="light" round>
              {{ detailData.statusName || getStatusLabel(detailData.status) }}
            </el-tag>
          </div>

          <div class="finance-ap-estimate-detail__summary-grid">
            <div class="finance-ap-estimate-detail__summary-card">
              <div class="finance-ap-estimate-detail__summary-label">来源未税金额</div>
              <div class="finance-ap-estimate-detail__summary-value font-mono">
                {{ formatAmount(detailData.sourceAmount) }}
              </div>
            </div>
            <div class="finance-ap-estimate-detail__summary-card finance-ap-estimate-detail__summary-card--accent">
              <div class="finance-ap-estimate-detail__summary-label">暂估金额</div>
              <div class="finance-ap-estimate-detail__summary-value finance-ap-estimate-detail__summary-value--accent font-mono">
                {{ formatAmount(detailData.amount) }}
              </div>
            </div>
            <div class="finance-ap-estimate-detail__summary-card">
              <div class="finance-ap-estimate-detail__summary-label">状态</div>
              <div class="finance-ap-estimate-detail__summary-value">
                {{ detailData.statusName || getStatusLabel(detailData.status) }}
              </div>
            </div>
          </div>

          <div class="finance-ap-estimate-detail__panel">
            <div class="finance-ap-estimate-detail__panel-title">基础信息</div>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="暂估单号">
                {{ detailData.estimateNo || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="暂估月份">
                {{ detailData.estimateMonth || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="来源入库单">
                {{ detailData.sourcePurchaseInNo || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="来源采购订单">
                {{ detailData.sourceOrderNo || '未关联采购订单' }}
              </el-descriptions-item>
              <el-descriptions-item label="供应商">
                {{ detailData.supplierName || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="结算账户">
                {{ detailData.accountName || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="确认时间">
                {{ formatDateValue(detailData.confirmTime) }}
              </el-descriptions-item>
              <el-descriptions-item label="冲回时间">
                {{ formatDateValue(detailData.reverseTime) }}
              </el-descriptions-item>
              <el-descriptions-item label="冲回备注" :span="2">
                {{ detailData.reverseRemark || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="备注" :span="2">
                {{ detailData.remark || '-' }}
              </el-descriptions-item>
            </el-descriptions>
          </div>

          <div class="finance-ap-estimate-detail__panel">
            <div class="finance-ap-estimate-detail__panel-title">暂估明细</div>
            <div class="finance-ap-estimate-detail__table-wrap">
              <el-table :data="detailData.items || []" stripe show-overflow-tooltip>
                <el-table-column label="物料" min-width="220">
                  <template #default="{ row }">
                    <div class="finance-ap-estimate-page__primary-cell">
                      <span class="finance-ap-estimate-page__primary-text">{{ row.productName || '-' }}</span>
                      <span class="finance-ap-estimate-page__muted-text">
                        {{ row.productBarCode || '-' }}
                        <template v-if="row.productUnitName"> / {{ row.productUnitName }}</template>
                      </span>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="仓库/项目" min-width="220">
                  <template #default="{ row }">
                    <div class="finance-ap-estimate-page__primary-cell">
                      <span class="finance-ap-estimate-page__primary-text">{{ row.warehouseName || '-' }}</span>
                      <span class="finance-ap-estimate-page__muted-text">{{ row.projectName || '-' }}</span>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column
                  label="数量"
                  align="right"
                  width="110"
                  prop="count"
                  class-name="font-mono"
                  :formatter="formatCountColumn"
                />
                <el-table-column
                  label="来源未税金额"
                  align="right"
                  width="140"
                  prop="sourceAmount"
                  class-name="font-mono"
                  :formatter="formatPriceColumn"
                />
                <el-table-column
                  label="税额"
                  align="right"
                  width="140"
                  prop="taxAmount"
                  class-name="font-mono"
                  :formatter="formatPriceColumn"
                />
                <el-table-column
                  label="暂估金额"
                  align="right"
                  width="140"
                  prop="amount"
                  class-name="font-mono"
                  :formatter="formatPriceColumn"
                />
                <el-table-column label="备注" min-width="180" prop="remark" />
              </el-table>
            </div>
          </div>
        </template>

        <el-empty v-else description="暂无暂估详情" />
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </Dialog>
  </div>
</template>

<script setup lang="ts">
import { nextTick } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessageBox } from 'element-plus'
import { formatDate } from '@/utils/formatTime'
import { erpCountTableColumnFormatter, erpPriceInputFormatter } from '@/utils'
import { SupplierApi, type SupplierVO } from '@/api/erp/purchase/supplier'
import {
  ApEstimateApi,
  ERP_AP_ESTIMATE_STATUS_OPTIONS,
  type ErpApEstimateActionReqVO,
  type ErpApEstimatePageReqVO,
  type ErpApEstimateVO
} from '@/api/erp/finance/ap-estimate'

defineOptions({ name: 'ErpFinanceApEstimate' })

interface ScanFormData {
  estimateMonth: string
}

const message = useMessage()

const queryFormRef = ref<FormInstance>()
const scanFormRef = ref<FormInstance>()
const tableRef = ref()

const advancedExpanded = ref(false)

const queryParams = reactive<ErpApEstimatePageReqVO>({
  pageNo: 1,
  pageSize: 10,
  estimateNo: '',
  estimateMonth: '',
  sourcePurchaseInNo: '',
  supplierId: undefined,
  status: undefined
})

const list = ref<ErpApEstimateVO[]>([])
const total = ref(0)
const listLoading = ref(false)
const listErrorMessage = ref('')

const supplierList = ref<SupplierVO[]>([])
const supplierLoading = ref(false)

const selectedRows = ref<ErpApEstimateVO[]>([])

const scanDialogVisible = ref(false)
const scanLoading = ref(false)
const scanFormData = reactive<ScanFormData>({
  estimateMonth: formatDate(new Date(), 'YYYY-MM')
})

const detailDialogVisible = ref(false)
const detailLoading = ref(false)
const detailId = ref<number | null>(null)
const detailData = ref<ErpApEstimateVO | null>(null)
const detailErrorMessage = ref('')

const confirmLoading = ref(false)
const reverseLoading = ref(false)
const exportLoading = ref(false)

const scanFormRules: FormRules<ScanFormData> = {
  estimateMonth: [{ required: true, message: '请选择暂估月份', trigger: 'change' }]
}

const currentYearMonth = () => formatDate(new Date(), 'YYYY-MM')

const resetSelection = async () => {
  selectedRows.value = []
  await nextTick()
  tableRef.value?.clearSelection?.()
}

const formatAmount = (value?: number) => {
  if (value == null) {
    return '-'
  }
  return erpPriceInputFormatter(value)
}

const formatPriceColumn = (_row: unknown, _column: unknown, cellValue: unknown) => {
  return formatAmount(cellValue as number | undefined)
}

const formatCountColumn = (_row: unknown, _column: unknown, cellValue: unknown) => {
  return erpCountTableColumnFormatter(_row, _column, cellValue, undefined)
}

const formatDateValue = (value?: string) => {
  return value ? formatDate(new Date(value), 'YYYY-MM-DD HH:mm:ss') : '-'
}

const getStatusLabel = (status?: number) =>
  ERP_AP_ESTIMATE_STATUS_OPTIONS.find((item) => item.value === status)?.label || '-'

const getStatusTagType = (status?: number) => {
  return (
    {
      10: 'warning',
      20: 'success',
      30: 'info'
    } as Record<number, 'warning' | 'success' | 'info'>
  )[status || 0] || 'info'
}

const canSelectRow = (row: ErpApEstimateVO) => row.status !== 30

const selectedIds = computed(() => selectedRows.value.map((item) => Number(item.id)).filter(Boolean))
const hasSelection = computed(() => selectedIds.value.length > 0)
const allSelectedAreConfirmable = computed(
  () => hasSelection.value && selectedRows.value.every((item) => item.status === 10)
)
const allSelectedAreReversible = computed(
  () => hasSelection.value && selectedRows.value.every((item) => item.status === 20)
)

const batchConfirmDisabled = computed(
  () => !allSelectedAreConfirmable.value || listLoading.value || scanLoading.value || confirmLoading.value
)
const batchReverseDisabled = computed(
  () => !allSelectedAreReversible.value || listLoading.value || scanLoading.value || reverseLoading.value
)
const isActionBusy = computed(
  () => listLoading.value || scanLoading.value || confirmLoading.value || reverseLoading.value || detailLoading.value
)

const loadSupplierList = async () => {
  supplierLoading.value = true
  try {
    supplierList.value = await SupplierApi.getSupplierSimpleList()
  } catch (error: any) {
    message.error(error?.message || '供应商列表加载失败')
  } finally {
    supplierLoading.value = false
  }
}

const getList = async () => {
  listLoading.value = true
  listErrorMessage.value = ''
  try {
    const data = await ApEstimateApi.getApEstimatePage(queryParams)
    list.value = data?.list || []
    total.value = data?.total || 0
  } catch (error: any) {
    list.value = []
    total.value = 0
    listErrorMessage.value = error?.message || '暂估列表加载失败'
  } finally {
    listLoading.value = false
    await resetSelection()
  }
}

const handleQuery = async () => {
  queryParams.pageNo = 1
  await getList()
}

const resetQuery = async () => {
  Object.assign(queryParams, {
    pageNo: 1,
    pageSize: 10,
    estimateNo: '',
    estimateMonth: '',
    sourcePurchaseInNo: '',
    supplierId: undefined,
    status: undefined
  })
  queryFormRef.value?.clearValidate()
  await getList()
}

const handlePagination = async () => {
  await getList()
}

const openScanDialog = () => {
  scanFormData.estimateMonth = currentYearMonth()
  scanDialogVisible.value = true
}

const submitScanMonth = async () => {
  if (scanLoading.value) {
    return
  }
  await scanFormRef.value?.validate()
  scanLoading.value = true
  try {
    const count = await ApEstimateApi.scanMonth({
      estimateMonth: scanFormData.estimateMonth
    })
    message.success(`月末扫描完成，共生成 ${count || 0} 条暂估单`)
    scanDialogVisible.value = false
    await getList()
  } catch (error: any) {
    message.error(error?.message || '月末扫描失败')
  } finally {
    scanLoading.value = false
  }
}

const loadDetail = async () => {
  if (detailId.value == null) {
    return
  }
  detailLoading.value = true
  detailErrorMessage.value = ''
  try {
    detailData.value = await ApEstimateApi.getApEstimate(detailId.value)
  } catch (error: any) {
    detailData.value = null
    detailErrorMessage.value = error?.message || '暂估详情加载失败'
  } finally {
    detailLoading.value = false
  }
}

const openDetailDialog = async (id?: number) => {
  if (id == null) {
    return
  }
  detailId.value = id
  detailData.value = null
  detailErrorMessage.value = ''
  detailDialogVisible.value = true
  await loadDetail()
}

watch(detailDialogVisible, (visible) => {
  if (visible) {
    return
  }
  detailId.value = null
  detailData.value = null
  detailErrorMessage.value = ''
})

watch(scanDialogVisible, (visible) => {
  if (visible) {
    return
  }
  scanFormData.estimateMonth = currentYearMonth()
  scanFormRef.value?.clearValidate()
})

const submitAction = async (action: 'confirm' | 'reverse', ids: number[], remark?: string) => {
  if (action === 'confirm' && confirmLoading.value) {
    return
  }
  if (action === 'reverse' && reverseLoading.value) {
    return
  }

  const payload: ErpApEstimateActionReqVO = {
    ids,
    remark: remark?.trim()
  }

  if (action === 'confirm') {
    confirmLoading.value = true
  } else {
    reverseLoading.value = true
  }

  try {
    if (action === 'confirm') {
      await ApEstimateApi.confirm(payload)
      message.success('暂估确认成功')
    } else {
      await ApEstimateApi.reverse(payload)
      message.success('暂估冲回成功')
    }
    await getList()
    if (detailDialogVisible.value && detailId.value != null && ids.includes(detailId.value)) {
      await loadDetail()
    }
  } catch (error: any) {
    message.error(error?.message || (action === 'confirm' ? '暂估确认失败' : '暂估冲回失败'))
  } finally {
    if (action === 'confirm') {
      confirmLoading.value = false
    } else {
      reverseLoading.value = false
    }
  }
}

const handleSingleConfirm = async (id?: number) => {
  if (id == null || confirmLoading.value || reverseLoading.value) {
    return
  }
  try {
    await ElMessageBox.confirm('确定确认该暂估单吗？', '暂估确认', {
      type: 'warning',
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      closeOnClickModal: false,
      closeOnPressEscape: false
    })
  } catch {
    return
  }
  await submitAction('confirm', [id])
}

const handleSingleReverse = async (id?: number) => {
  if (id == null || confirmLoading.value || reverseLoading.value) {
    return
  }
  try {
    const { value } = await ElMessageBox.prompt('请输入冲回原因', '暂估冲回', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      inputType: 'textarea',
      inputPlaceholder: '请输入冲回原因',
      inputValidator: () => true,
      closeOnClickModal: false,
      closeOnPressEscape: false
    })
    await submitAction('reverse', [id], value)
  } catch {
    return
  }
}

const handleBatchConfirm = async () => {
  if (batchConfirmDisabled.value) {
    return
  }
  try {
    await ElMessageBox.confirm(`确定确认选中的 ${selectedIds.value.length} 条暂估单吗？`, '批量确认', {
      type: 'warning',
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      closeOnClickModal: false,
      closeOnPressEscape: false
    })
  } catch {
    return
  }
  await submitAction('confirm', [...selectedIds.value])
}

const handleBatchReverse = async () => {
  if (batchReverseDisabled.value) {
    return
  }
  try {
    const { value } = await ElMessageBox.prompt('请输入冲回原因', '批量冲回', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      inputType: 'textarea',
      inputPlaceholder: '请输入冲回原因',
      inputValidator: () => true,
      closeOnClickModal: false,
      closeOnPressEscape: false
    })
    await submitAction('reverse', [...selectedIds.value], value)
  } catch {
    return
  }
}

const handleSelectionChange = (rows: ErpApEstimateVO[]) => {
  selectedRows.value = rows
}

const handleExport = async () => {
  if (exportLoading.value) {
    return
  }
  exportLoading.value = true
  try {
    await ApEstimateApi.exportApEstimate(queryParams)
  } catch (error: any) {
    message.error(error?.message || '导出失败')
  } finally {
    exportLoading.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadSupplierList(), getList()])
})
</script>

<style scoped>
.finance-ap-estimate-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: #f8fafc;
}

.finance-ap-estimate-page__card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.finance-ap-estimate-page__card-title,
.finance-ap-estimate-page__section-title,
.finance-ap-estimate-detail__panel-title {
  font-size: 16px;
  font-weight: 600;
  color: #0f172a;
}

.finance-ap-estimate-page__query-form {
  padding: 4px 0 0;
}

.finance-ap-estimate-page__query-grid {
  display: grid;
  gap: 0 16px;
}

.finance-ap-estimate-page__query-grid--basic {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.finance-ap-estimate-page__query-grid--advanced {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.finance-ap-estimate-page__query-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 10px;
}

.finance-ap-estimate-page__advanced-block {
  margin-top: 8px;
  padding: 14px 16px 4px;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  background: #f8fafc;
}

.finance-ap-estimate-page__toolbar {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.finance-ap-estimate-page__toolbar-left,
.finance-ap-estimate-page__toolbar-right {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.finance-ap-estimate-page__state {
  padding: 18px 0 6px;
}

.finance-ap-estimate-page__table-wrap {
  overflow-x: auto;
}

.finance-ap-estimate-page__table-wrap :deep(.el-table__header-wrapper th),
.finance-ap-estimate-detail__table-wrap :deep(.el-table__header-wrapper th) {
  position: sticky;
  top: 0;
  z-index: 10;
  background: #ffffff;
}

.finance-ap-estimate-page__primary-cell,
.finance-ap-estimate-page__amount-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.finance-ap-estimate-page__primary-text {
  font-weight: 600;
  color: #0f172a;
}

.finance-ap-estimate-page__muted-text {
  font-size: 12px;
  color: #64748b;
  line-height: 1.35;
  word-break: break-all;
}

.finance-ap-estimate-page__amount-text {
  font-weight: 600;
  color: #1d4ed8;
}

.finance-ap-estimate-detail {
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: #f8fafc;
}

.finance-ap-estimate-detail__hero {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  background: #ffffff;
  box-shadow: 0 8px 22px rgba(148, 163, 184, 0.08);
}

.finance-ap-estimate-detail__eyebrow {
  font-size: 12px;
  color: #64748b;
}

.finance-ap-estimate-detail__title {
  margin-top: 6px;
  font-size: 24px;
  font-weight: 700;
  color: #0f172a;
  letter-spacing: 0.02em;
}

.finance-ap-estimate-detail__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 18px;
  margin-top: 10px;
  color: #64748b;
  font-size: 13px;
}

.finance-ap-estimate-detail__summary-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.finance-ap-estimate-detail__summary-card {
  padding: 16px 18px;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  background: #ffffff;
  box-shadow: 0 6px 18px rgba(148, 163, 184, 0.06);
}

.finance-ap-estimate-detail__summary-card--accent {
  border-color: #bfdbfe;
  background: linear-gradient(180deg, #eff6ff 0%, #ffffff 100%);
}

.finance-ap-estimate-detail__summary-label {
  font-size: 12px;
  color: #64748b;
}

.finance-ap-estimate-detail__summary-value {
  margin-top: 8px;
  font-size: 22px;
  font-weight: 700;
  color: #0f172a;
}

.finance-ap-estimate-detail__summary-value--accent {
  color: #dc2626;
}

.finance-ap-estimate-detail__panel {
  padding: 18px 20px;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  background: #ffffff;
  box-shadow: 0 8px 22px rgba(148, 163, 184, 0.08);
}

.finance-ap-estimate-detail__table-wrap {
  margin-top: 14px;
  overflow-x: auto;
}

.finance-ap-estimate-page__dialog-form {
  padding: 2px 0 0;
}

@media (max-width: 1280px) {
  .finance-ap-estimate-page__query-grid--basic,
  .finance-ap-estimate-page__query-grid--advanced {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .finance-ap-estimate-detail__summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .finance-ap-estimate-page__query-grid--basic,
  .finance-ap-estimate-page__query-grid--advanced,
  .finance-ap-estimate-detail__summary-grid {
    grid-template-columns: 1fr;
  }

  .finance-ap-estimate-detail__hero {
    flex-direction: column;
  }
}
</style>
