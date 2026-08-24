<template>
  <div class="finance-shell finance-voucher-page finance-shell__stack">
    <ContentWrap class="finance-shell__header-card">
      <div class="finance-shell__page-header">
        <div class="finance-shell__page-header-main finance-voucher-page__page-header-main">
          <div class="finance-shell__page-title">财务凭证</div>
          <div class="finance-shell__metric-grid finance-voucher-page__summary-grid">
            <div class="finance-shell__metric-card finance-voucher-page__summary-card">
              <div class="finance-shell__metric-label">结果</div>
              <div class="finance-shell__metric-value">{{ total }}</div>
            </div>
            <div class="finance-shell__metric-card finance-voucher-page__summary-card">
              <div class="finance-shell__metric-label">已选</div>
              <div class="finance-shell__metric-value">{{ selectedIds.length }}</div>
            </div>
            <div class="finance-shell__metric-card finance-voucher-page__summary-card">
              <div class="finance-shell__metric-label">已过账</div>
              <div class="finance-shell__metric-value finance-voucher-page__summary-value--success">{{ postedCount }}</div>
            </div>
            <div class="finance-shell__metric-card finance-voucher-page__summary-card finance-voucher-page__summary-card--accent">
              <div class="finance-shell__metric-label">可冲销</div>
              <div class="finance-shell__metric-value finance-voucher-page__summary-value--accent">{{ reverseReadyCount }}</div>
            </div>
          </div>
        </div>
        <div class="finance-shell__page-header-actions finance-voucher-page__page-header-actions">
          <el-button size="small" type="primary" plain :loading="refreshing" :disabled="!canRefresh" @click="handleRefresh">
            <Icon icon="ep:refresh" class="mr-5px" />
            刷新
          </el-button>
        </div>
      </div>
    </ContentWrap>

    <ContentWrap class="finance-shell__filter-card">
      <div class="finance-shell__section-head">
        <div class="finance-shell__section-title">筛选条件</div>
      </div>
      <el-form ref="queryFormRef" :model="queryParams" label-width="80px" class="finance-shell__query-form" @submit.prevent>
        <div class="finance-shell__query-grid finance-shell__query-grid--wide">
          <el-form-item label="账簿" prop="ledgerId">
            <el-select
              v-model="queryParams.ledgerId"
              placeholder="请选择账簿"
              clearable
              filterable
              :loading="ledgerLoading"
              class="!w-full"
              @change="handleLedgerChange"
            >
              <el-option v-for="item in ledgerOptions" :key="item.id" :label="displayLedgerName(item.name)" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="期间" prop="periodId">
            <el-select v-model="queryParams.periodId" placeholder="请选择期间" clearable filterable :loading="periodLoading" class="!w-full">
              <el-option v-for="item in periodOptions" :key="item.id" :label="item.periodCode" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="凭证号" prop="voucherNo">
            <el-input v-model="queryParams.voucherNo" placeholder="请输入凭证号" clearable class="!w-full" @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="业务单号" prop="bizNo">
            <el-input v-model="queryParams.bizNo" placeholder="请输入业务单号" clearable class="!w-full" @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="请选择状态" clearable class="!w-full">
              <el-option v-for="item in VOUCHER_STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="凭证时间" prop="voucherTime">
            <el-date-picker
              v-model="queryParams.voucherTime"
              type="datetimerange"
              value-format="YYYY-MM-DD HH:mm:ss"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              class="!w-full"
            />
          </el-form-item>
        </div>
        <div class="finance-shell__query-actions">
          <el-button size="small" type="primary" :loading="listLoading" :disabled="!canQuery" @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" />
            查询
          </el-button>
          <el-button size="small" :disabled="!canReset" @click="resetQuery">
            <Icon icon="ep:refresh-left" class="mr-5px" />
            重置
          </el-button>
        </div>
      </el-form>
    </ContentWrap>

    <ContentWrap class="finance-shell__table-card finance-shell__toolbar-card">
      <div class="finance-shell__toolbar">
        <div class="finance-shell__table-toolbar-main">
          <div class="finance-shell__section-title">财务凭证列表</div>
          <div class="finance-shell__toolbar-count">当前共 <strong>{{ total }}</strong> 条</div>
        </div>
        <div class="finance-shell__toolbar-actions">
          <el-button
            size="small"
            type="primary"
            plain
            :disabled="listLoading || voucherBusy"
            @click="openGenerateDialog"
            v-hasPermi="['erp:finance-voucher:create']"
          >
            <Icon icon="ep:plus" class="mr-5px" />
            生成凭证
          </el-button>
          <el-button
            size="small"
            type="success"
            plain
            :disabled="!canBatchApprove"
            :loading="batchAction === 'approve'"
            @click="handleBatchAction('approve')"
            v-hasPermi="['erp:finance-voucher:update']"
          >
            <Icon icon="ep:select" class="mr-5px" />
            审核
          </el-button>
          <el-button
            size="small"
            type="warning"
            plain
            :disabled="!canBatchCancelApprove"
            :loading="batchAction === 'cancelApprove'"
            @click="handleBatchAction('cancelApprove')"
            v-hasPermi="['erp:finance-voucher:update']"
          >
            <Icon icon="ep:refresh-left" class="mr-5px" />
            反审核
          </el-button>
          <el-button
            size="small"
            type="success"
            plain
            :disabled="!canBatchPost"
            :loading="batchAction === 'post'"
            @click="handleBatchAction('post')"
            v-hasPermi="['erp:finance-voucher:update']"
          >
            <Icon icon="ep:finished" class="mr-5px" />
            过账
          </el-button>
          <el-button
            size="small"
            type="warning"
            plain
            :disabled="!canBatchCancelPost"
            :loading="batchAction === 'cancelPost'"
            @click="handleBatchAction('cancelPost')"
            v-hasPermi="['erp:finance-voucher:update']"
          >
            <Icon icon="ep:refresh-right" class="mr-5px" />
            反过账
          </el-button>
        </div>
      </div>

      <el-alert
        v-if="listErrorMessage && !list.length"
        type="error"
        :closable="false"
        show-icon
        class="mb-12px"
        :title="listErrorMessage"
      >
        <template #default>
          <el-button link type="primary" :disabled="!canRefresh" @click="handleRefresh">重新加载</el-button>
        </template>
      </el-alert>

      <template v-else>
        <div v-if="listLoading || list.length" class="finance-shell__table-wrap">
          <el-table
            v-loading="listLoading"
            :data="list"
            row-key="id"
            stripe
            class="finance-shell__table finance-shell__table--dense"
            :show-overflow-tooltip="false"
            @selection-change="handleSelectionChange"
          >
            <el-table-column width="42" type="selection" />
            <el-table-column min-width="250">
              <template #header>
                <span class="finance-shell__column-header">
                  <Icon icon="ep:document" class="finance-shell__column-icon" />
                  凭证信息
                </span>
              </template>
              <template #default="{ row }">
                <div class="finance-shell__primary-cell">
                  <span class="finance-shell__primary-text finance-shell__mono">{{ row.voucherNo || '-' }}</span>
                  <span class="finance-shell__muted-text">{{ formatDateTimeValue(row.voucherTime) }}</span>
                  <div class="finance-shell__row-tags">
                    <span class="finance-shell__metric-pill finance-shell__metric-pill--primary">{{ row.statusName || getVoucherStatusLabel(row.status) }}</span>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column min-width="220">
              <template #header>
                <span class="finance-shell__column-header finance-shell__column-header--pipeline">
                  <Icon icon="ep:link" class="finance-shell__column-icon" />
                  业务来源
                </span>
              </template>
              <template #default="{ row }">
                <div class="finance-shell__primary-cell">
                  <span class="finance-shell__primary-text">{{ row.bizTypeName || '-' }}</span>
                  <span class="finance-shell__muted-text finance-shell__mono">{{ row.bizNo || '-' }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column min-width="180">
              <template #header>
                <span class="finance-shell__column-header finance-shell__column-header--pipeline">
                  <Icon icon="ep:collection" class="finance-shell__column-icon" />
                  账簿期间
                </span>
              </template>
              <template #default="{ row }">
                <div class="finance-shell__primary-cell">
                  <span class="finance-shell__primary-text">{{ displayLedgerName(row.ledgerName) }}</span>
                  <span class="finance-shell__muted-text">{{ row.periodCode || '-' }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="借方合计" align="right" min-width="130">
              <template #default="{ row }">
                <span class="finance-shell__amount">{{ formatAmount(row.totalDebitAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="贷方合计" align="right" min-width="130">
              <template #default="{ row }">
                <span class="finance-shell__amount">{{ formatAmount(row.totalCreditAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" align="center" width="110">
              <template #default="{ row }">
                <span class="finance-shell__status-badge" :class="resolveVoucherStatusClass(row.status)">
                  {{ row.statusName || getVoucherStatusLabel(row.status) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="操作" fixed="right" align="center" width="280">
              <template #default="{ row }">
                <div class="finance-shell__row-actions">
                  <el-button
                    link
                    type="primary"
                    :disabled="detailLoading || rowBusy(row.id)"
                    @click="openDetailDrawer(row.id)"
                    v-hasPermi="['erp:finance-voucher:query']"
                  >
                    查看
                  </el-button>
                  <el-button
                    v-if="canRecompute(row)"
                    link
                    type="primary"
                    :disabled="rowBusy(row.id)"
                    :loading="rowActionLoading(row.id, 'recompute')"
                    @click="handleRecompute(row)"
                    v-hasPermi="['erp:finance-voucher:update']"
                  >
                    <Icon icon="ep:refresh" class="mr-3px" />
                    重算凭证
                  </el-button>
                  <el-button
                    v-if="canReverse(row)"
                    link
                    type="danger"
                    :disabled="rowBusy(row.id)"
                    :loading="rowActionLoading(row.id, 'reverse')"
                    @click="handleReverse(row)"
                    v-hasPermi="['erp:finance-voucher:update']"
                  >
                    冲销
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <template v-else>
          <div v-if="listErrorMessage" class="finance-shell__empty">
            <div class="finance-shell__empty-icon">
              <Icon icon="ep:warning-filled" />
            </div>
            <div class="finance-shell__empty-title">凭证列表加载失败</div>
            <el-button type="primary" plain :disabled="!canRefresh" @click="handleRefresh">重新加载</el-button>
          </div>
          <div v-else class="finance-shell__empty">
            <div class="finance-shell__empty-icon">
              <Icon icon="ep:document" />
            </div>
            <div class="finance-shell__empty-title">暂无凭证数据</div>
          </div>
        </template>
      </template>

      <Pagination v-if="total > 0" v-model:limit="queryParams.pageSize" v-model:page="queryParams.pageNo" :total="total" @pagination="getList" />
    </ContentWrap>

    <Dialog
      v-model="generateDialogVisible"
      title="生成凭证"
      :width="dialogWidth.generate"
      scroll
      maxHeight="76vh"
      @closed="clearGenerateDialog"
    >
      <div class="finance-shell__context-card finance-shell__dialog-card">
        <div class="finance-shell__context-main">
          <div class="finance-shell__context-title">生成凭证</div>
        </div>
      </div>
      <el-form ref="generateFormRef" :model="generateForm" :rules="generateFormRules" label-width="88px" v-loading="generateLoading" :disabled="generateSubmitting">
        <div class="finance-shell__dialog-grid">
          <el-form-item label="账簿" prop="ledgerId">
            <el-select v-model="generateForm.ledgerId" placeholder="请选择账簿" filterable class="!w-full" @change="handleGenerateLedgerChange">
              <el-option v-for="item in ledgerOptions" :key="item.id" :label="displayLedgerName(item.name)" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="业务类型" prop="bizType">
            <el-select v-model="generateForm.bizType" placeholder="请选择业务类型" class="!w-full" @change="loadTemplateOptions">
              <el-option v-for="item in bizTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="业务单据" prop="bizId">
            <el-input-number v-model="generateForm.bizId" :min="1" controls-position="right" class="!w-full" />
          </el-form-item>
          <el-form-item label="凭证模板" prop="templateId">
            <el-select v-model="generateForm.templateId" placeholder="请选择凭证模板" filterable class="!w-full" :loading="templateLoading">
              <el-option v-for="item in templateOptions" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="凭证时间" prop="voucherTime">
            <el-date-picker v-model="generateForm.voucherTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="不填则使用业务时间" class="!w-full" />
          </el-form-item>
        </div>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="generateForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" type="primary" :loading="generateSubmitting" @click="submitGenerateForm">确定</el-button>
        <el-button size="small" :disabled="generateSubmitting" @click="generateDialogVisible = false">取消</el-button>
      </template>
    </Dialog>

    <Dialog
      v-model="reverseDialogVisible"
      title="冲销凭证"
      :width="dialogWidth.reverse"
      scroll
      maxHeight="72vh"
      @closed="clearReverseDialog"
    >
      <el-form ref="reverseFormRef" :model="reverseForm" :rules="reverseFormRules" label-width="88px" v-loading="reverseSubmitting" :disabled="reverseSubmitting">
        <el-form-item label="凭证时间" prop="voucherTime">
          <el-date-picker v-model="reverseForm.voucherTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="不填则使用当前时间" class="!w-full" />
        </el-form-item>
        <el-form-item label="冲销说明" prop="remark">
          <el-input v-model="reverseForm.remark" type="textarea" :rows="3" placeholder="请输入冲销说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" type="danger" :loading="reverseSubmitting" @click="submitReverseForm">确定</el-button>
        <el-button size="small" :disabled="reverseSubmitting" @click="reverseDialogVisible = false">取消</el-button>
      </template>
    </Dialog>

    <el-drawer
      v-model="detailDrawerOpen"
      title="查看凭证"
      size="860px"
      destroy-on-close
      modal-class="finance-shell__drawer-modal"
      @closed="clearDetailDrawer"
    >
      <el-result v-if="detailErrorMessage" icon="error" title="凭证明细加载失败">
        <template #extra>
          <el-button type="primary" @click="retryDetail">重试</el-button>
        </template>
      </el-result>
      <template v-else>
        <div v-if="detailData" class="finance-shell__context-card">
          <div class="finance-shell__context-main">
            <div class="finance-shell__context-title">{{ detailData.voucherNo || '-' }}</div>
            <div class="finance-shell__context-meta-line">{{ detailData.bizTypeName || '-' }} {{ detailData.bizNo || '-' }}</div>
          </div>
          <div class="finance-shell__context-meta">
            <div class="finance-shell__context-meta-item">
              <span>状态</span>
              <span>{{ detailData.statusName || getVoucherStatusLabel(detailData.status) }}</span>
            </div>
            <div class="finance-shell__context-meta-item">
              <span>账簿</span>
              <span>{{ displayLedgerName(detailData.ledgerName) }}</span>
            </div>
            <div class="finance-shell__context-meta-item">
              <span>期间</span>
              <span>{{ detailData.periodCode || '-' }}</span>
            </div>
            <div class="finance-shell__context-meta-item">
              <span>时间</span>
              <span>{{ formatDateTimeValue(detailData.voucherTime) }}</span>
            </div>
          </div>
        </div>
        <div v-if="detailData" class="finance-shell__metric-grid">
          <div class="finance-shell__metric-card">
            <div class="finance-shell__metric-label">借方合计</div>
            <div class="finance-shell__metric-value">{{ formatAmount(detailData.totalDebitAmount) }}</div>
          </div>
          <div class="finance-shell__metric-card">
            <div class="finance-shell__metric-label">贷方合计</div>
            <div class="finance-shell__metric-value">{{ formatAmount(detailData.totalCreditAmount) }}</div>
          </div>
          <div class="finance-shell__metric-card">
            <div class="finance-shell__metric-label">审核时间</div>
            <div class="finance-shell__metric-value">{{ formatDateTimeValue(detailData.approveTime) }}</div>
          </div>
          <div class="finance-shell__metric-card">
            <div class="finance-shell__metric-label">过账时间</div>
            <div class="finance-shell__metric-value">{{ formatDateTimeValue(detailData.postTime) }}</div>
          </div>
        </div>
        <el-table v-loading="detailLoading" :data="detailData?.entries || []" stripe class="finance-shell__table finance-shell__table--dense" :show-overflow-tooltip="false">
          <el-table-column label="序号" prop="entryNo" align="right" width="80" />
          <el-table-column label="摘要" prop="summary" min-width="220" />
          <el-table-column min-width="220">
            <template #header>
              <span class="finance-shell__column-header">
                <Icon icon="ep:coin" class="finance-shell__column-icon" />
                科目
              </span>
            </template>
            <template #default="{ row }">
              <div class="finance-shell__primary-cell">
                <span class="finance-shell__primary-text">{{ row.subjectName || '-' }}</span>
                <span class="finance-shell__muted-text finance-shell__mono">{{ row.subjectCode || '-' }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="借方" align="right" min-width="130">
            <template #default="{ row }"><span class="finance-shell__amount">{{ formatAmount(row.debitAmount) }}</span></template>
          </el-table-column>
          <el-table-column label="贷方" align="right" min-width="130">
            <template #default="{ row }"><span class="finance-shell__amount">{{ formatAmount(row.creditAmount) }}</span></template>
          </el-table-column>
        </el-table>
        <div v-if="!detailLoading && !detailData?.entries?.length" class="finance-shell__empty">
          <div class="finance-shell__empty-icon">
            <Icon icon="ep:document" />
          </div>
          <div class="finance-shell__empty-title">暂无明细数据</div>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ErpBizType } from '@/utils/constants'
import {
  VOUCHER_STATUS_OPTIONS,
  formatAmount,
  formatDateTimeValue,
  getVoucherStatusLabel
} from '@/views/erp/finance/shared/accounting'
import { ErpFinanceLedgerVO, FinanceLedgerApi } from '@/api/erp/finance/ledger'
import { ErpFinancePeriodVO, FinancePeriodApi } from '@/api/erp/finance/period'
import { FinanceVoucherTemplateApi, ErpFinanceVoucherTemplateVO } from '@/api/erp/finance/voucher-template'
import { ErpFinanceVoucherPageReqVO, ErpFinanceVoucherVO, FinanceVoucherApi } from '@/api/erp/finance/voucher'
import { canRecomputeVoucher } from './voucherStatus.helpers'
import { displayLedgerName } from '@/utils/financeDisplay'

defineOptions({ name: 'ErpFinanceVoucher' })

const message = useMessage()
const queryFormRef = ref()
const generateFormRef = ref()
const reverseFormRef = ref()
const ledgerLoading = ref(false)
const periodLoading = ref(false)
const listLoading = ref(false)
const refreshing = ref(false)
const detailLoading = ref(false)
const generateLoading = ref(false)
const templateLoading = ref(false)
const generateSubmitting = ref(false)
const reverseSubmitting = ref(false)
const detailDrawerOpen = ref(false)
const generateDialogVisible = ref(false)
const reverseDialogVisible = ref(false)
const listErrorMessage = ref('')
const detailErrorMessage = ref('')
const ledgerOptions = ref<ErpFinanceLedgerVO[]>([])
const periodOptions = ref<ErpFinancePeriodVO[]>([])
const templateOptions = ref<ErpFinanceVoucherTemplateVO[]>([])
const list = ref<ErpFinanceVoucherVO[]>([])
const total = ref(0)
const detailData = ref<ErpFinanceVoucherVO>()
const reverseTarget = ref<ErpFinanceVoucherVO>()
const currentVoucherId = ref<number>()
const selectedIds = ref<number[]>([])
const rowActionLoadingId = ref<number>()
const rowActionLoadingType = ref<'reverse' | 'recompute' | ''>('')
const batchAction = ref<'approve' | 'cancelApprove' | 'post' | 'cancelPost' | ''>('')
const viewportWidth = ref(typeof window === 'undefined' ? 1440 : window.innerWidth)
const queryForm = reactive<ErpFinanceVoucherPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  ledgerId: undefined,
  periodId: undefined,
  bizNo: undefined,
  voucherNo: undefined,
  status: undefined,
  voucherTime: undefined
})
const generateForm = reactive({
  ledgerId: undefined as number | undefined,
  bizType: undefined as number | undefined,
  bizId: undefined as number | undefined,
  templateId: undefined as number | undefined,
  voucherTime: undefined as string | undefined,
  remark: ''
})
const reverseForm = reactive({
  voucherTime: undefined as string | undefined,
  remark: ''
})
const bizTypeOptions = [
  { label: '采购入库', value: ErpBizType.PURCHASE_IN },
  { label: '采购退货', value: ErpBizType.PURCHASE_RETURN },
  { label: '销售出库', value: ErpBizType.SALE_OUT },
  { label: '销售退货', value: ErpBizType.SALE_RETURN },
  { label: '委外加工费', value: 30 },
  { label: '费用报销', value: 40 }
]

const queryParams = queryForm
const generateFormRules = {
  ledgerId: [{ required: true, message: '账簿不能为空', trigger: 'change' }],
  bizType: [{ required: true, message: '业务类型不能为空', trigger: 'change' }],
  bizId: [{ required: true, message: '业务单据不能为空', trigger: 'blur' }],
  templateId: [{ required: true, message: '凭证模板不能为空', trigger: 'change' }]
}
const reverseFormRules = { remark: [{ required: false }] }

const isActionCanceled = (error: unknown) => error === 'cancel' || error === 'close'
const rowBusy = (id?: number) => id != null && rowActionLoadingId.value === id
const rowActionLoading = (id: number | undefined, action: 'reverse' | 'recompute') =>
  id != null && rowActionLoadingId.value === id && rowActionLoadingType.value === action
const selectedRows = computed(() => list.value.filter((row) => row.id && selectedIds.value.includes(row.id)))
const canBatchApprove = computed(() => selectedIds.value.length > 0 && !batchAction.value && selectedRows.value.every((row) => row.status === 10))
const canBatchCancelApprove = computed(() => selectedIds.value.length > 0 && !batchAction.value && selectedRows.value.every((row) => row.status === 20))
const canBatchPost = computed(() => selectedIds.value.length > 0 && !batchAction.value && selectedRows.value.every((row) => row.status === 20))
const canBatchCancelPost = computed(() => selectedIds.value.length > 0 && !batchAction.value && selectedRows.value.every((row) => row.status === 30))
const voucherBusy = computed(() => listLoading.value || detailLoading.value || generateSubmitting.value || reverseSubmitting.value || !!batchAction.value || !!rowActionLoadingId.value)
const postedCount = computed(() => list.value.filter((row) => row.status === 30).length)
const reverseReadyCount = computed(() => list.value.filter((row) => row.status === 30 && !row.reverseVoucherId && !row.reverseFromVoucherId).length)
const canQuery = computed(
  () => !listLoading.value && !refreshing.value && !batchAction.value && !generateSubmitting.value && !reverseSubmitting.value && !rowActionLoadingId.value
)
const canRefresh = computed(
  () => !listLoading.value && !refreshing.value && !batchAction.value && !generateSubmitting.value && !reverseSubmitting.value && !rowActionLoadingId.value
)
const hasActiveFilters = computed(() => !!queryParams.ledgerId || !!queryParams.periodId || !!queryParams.voucherNo || !!queryParams.bizNo || queryParams.status !== undefined || !!queryParams.voucherTime)
const canReset = computed(() => (hasActiveFilters.value || queryParams.pageNo !== 1) && canQuery.value)
const dialogWidth = computed(() => ({
  generate:
    viewportWidth.value <= 480 ? 'calc(100vw - 24px)' : viewportWidth.value <= 768 ? 'min(680px, calc(100vw - 32px))' : '720px',
  reverse:
    viewportWidth.value <= 480 ? 'calc(100vw - 24px)' : viewportWidth.value <= 768 ? 'min(520px, calc(100vw - 32px))' : '560px'
}))

const resolveVoucherStatusClass = (status?: number) => {
  if (status === 30) return 'finance-shell__status-badge--success'
  if (status === 40) return 'finance-shell__status-badge--danger'
  if (status === 20) return 'finance-shell__status-badge--warning'
  if (status === 10) return 'finance-shell__status-badge--primary'
  return 'finance-shell__status-badge--neutral'
}

const getVoucherTagType = (status?: number) => {
  if (status === 30) return 'success'
  if (status === 40) return 'danger'
  if (status === 20) return 'warning'
  return 'info'
}

const loadLedgers = async () => {
  ledgerLoading.value = true
  try {
    ledgerOptions.value = await FinanceLedgerApi.getLedgerSimpleList()
  } finally {
    ledgerLoading.value = false
  }
}

const loadPeriods = async () => {
  if (!queryParams.ledgerId) {
    periodOptions.value = []
    queryParams.periodId = undefined
    return
  }
  periodLoading.value = true
  try {
    const data = await FinancePeriodApi.getPeriodPage({ pageNo: 1, pageSize: 100, ledgerId: queryParams.ledgerId })
    periodOptions.value = data?.list || []
    if (!periodOptions.value.some((item) => item.id === queryParams.periodId)) {
      queryParams.periodId = undefined
    }
  } finally {
    periodLoading.value = false
  }
}

const handleLedgerChange = async () => {
  queryParams.periodId = undefined
  await loadPeriods()
}

const getList = async () => {
  if (listLoading.value) {
    return
  }
  listLoading.value = true
  listErrorMessage.value = ''
  try {
    const data = await FinanceVoucherApi.getVoucherPage(queryParams)
    list.value = data?.list || []
    total.value = data?.total || 0
  } catch {
    if (!list.value.length) {
      listErrorMessage.value = '凭证列表加载失败'
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
  await loadPeriods()
  await getList()
}

const handleSelectionChange = (rows: ErpFinanceVoucherVO[]) => {
  selectedIds.value = rows.map((row) => Number(row.id)).filter(Boolean)
}

const loadTemplateOptions = async () => {
  if (!generateForm.ledgerId || !generateForm.bizType) {
    templateOptions.value = []
    generateForm.templateId = undefined
    return
  }
  templateLoading.value = true
  try {
    templateOptions.value = await FinanceVoucherTemplateApi.getVoucherTemplateSimpleList({
      ledgerId: generateForm.ledgerId,
      bizType: generateForm.bizType
    })
    if (!templateOptions.value.some((item) => item.id === generateForm.templateId)) {
      generateForm.templateId = templateOptions.value.find((item) => item.autoGenerate)?.id ?? templateOptions.value[0]?.id
    }
  } finally {
    templateLoading.value = false
  }
}

const handleGenerateLedgerChange = async () => {
  generateForm.templateId = undefined
  await loadTemplateOptions()
}

const resetGenerateForm = () => {
  generateForm.ledgerId = queryParams.ledgerId
  generateForm.bizType = undefined
  generateForm.bizId = undefined
  generateForm.templateId = undefined
  generateForm.voucherTime = undefined
  generateForm.remark = ''
}

const openGenerateDialog = async () => {
  resetGenerateForm()
  generateDialogVisible.value = true
  generateLoading.value = true
  try {
    await loadTemplateOptions()
  } finally {
    generateLoading.value = false
  }
}

const submitGenerateForm = async () => {
  if (generateSubmitting.value) return
  await generateFormRef.value?.validate()
  generateSubmitting.value = true
  try {
    await FinanceVoucherApi.generateVoucher({
      ledgerId: generateForm.ledgerId!,
      bizType: generateForm.bizType!,
      bizId: generateForm.bizId!,
      templateId: generateForm.templateId!,
      voucherTime: generateForm.voucherTime,
      remark: generateForm.remark
    })
    message.success('生成成功')
    generateDialogVisible.value = false
    await getList()
  } finally {
    generateSubmitting.value = false
  }
}

const handleBatchAction = async (action: 'approve' | 'cancelApprove' | 'post' | 'cancelPost') => {
  if (!selectedIds.value.length || batchAction.value) return
  const actionTextMap = {
    approve: '审核',
    cancelApprove: '反审核',
    post: '过账',
    cancelPost: '反过账'
  } as const
  await message.confirm(`确认${actionTextMap[action]}选中的凭证吗？`)
  batchAction.value = action
  try {
    if (action === 'approve') {
      await FinanceVoucherApi.approveVoucher(selectedIds.value)
    } else if (action === 'cancelApprove') {
      await FinanceVoucherApi.cancelApproveVoucher(selectedIds.value)
    } else if (action === 'post') {
      await FinanceVoucherApi.postVoucher(selectedIds.value)
    } else {
      await FinanceVoucherApi.cancelPostVoucher(selectedIds.value)
    }
    message.success(`${actionTextMap[action]}成功`)
    selectedIds.value = []
    await getList()
  } finally {
    batchAction.value = ''
  }
}

const canRecompute = (row: ErpFinanceVoucherVO) =>
  row.id != null && canRecomputeVoucher(row) && [10, 20, 30].includes(row.status || 0)

const canReverse = (row: ErpFinanceVoucherVO) => row.status === 30 && !row.reverseFromVoucherId && !row.reverseVoucherId

const handleRecompute = async (row: ErpFinanceVoucherVO) => {
  if (!canRecompute(row) || rowActionLoadingId.value) return
  try {
    await message.confirm('重算将根据当前业务单据更新自动凭证，是否继续？')
  } catch (error) {
    if (isActionCanceled(error)) return
    throw error
  }
  rowActionLoadingId.value = row.id
  rowActionLoadingType.value = 'recompute'
  try {
    const recomputedVoucherId = await FinanceVoucherApi.recomputeVoucher({
      bizType: row.bizType!,
      bizId: row.bizId!
    })
    if (!recomputedVoucherId) {
      message.warning('未生成新凭证')
      return
    }
    message.success('重算成功')
    await getList()
    if (detailDrawerOpen.value && currentVoucherId.value === row.id) {
      currentVoucherId.value = recomputedVoucherId
      await loadDetail(recomputedVoucherId)
    }
  } catch (error: any) {
    message.error(error?.message || '凭证重算失败')
  } finally {
    rowActionLoadingId.value = undefined
    rowActionLoadingType.value = ''
  }
}

const handleReverse = async (row: ErpFinanceVoucherVO) => {
  if (!row.id || rowActionLoadingId.value) return
  rowActionLoadingId.value = row.id
  rowActionLoadingType.value = 'reverse'
  reverseTarget.value = row
  reverseForm.voucherTime = undefined
  reverseForm.remark = ''
  reverseDialogVisible.value = true
}

const submitReverseForm = async () => {
  if (!reverseTarget.value?.id || reverseSubmitting.value) return
  await reverseFormRef.value?.validate()
  reverseSubmitting.value = true
  try {
    await FinanceVoucherApi.reverseVoucher({
      id: reverseTarget.value.id,
      voucherTime: reverseForm.voucherTime,
      remark: reverseForm.remark
    })
    message.success('冲销成功')
    reverseDialogVisible.value = false
    await getList()
    if (detailDrawerOpen.value && currentVoucherId.value) {
      await loadDetail(currentVoucherId.value)
    }
  } finally {
    reverseSubmitting.value = false
  }
}

const openDetailDrawer = async (id?: number) => {
  if (!id) return
  currentVoucherId.value = id
  detailDrawerOpen.value = true
  await loadDetail(id)
}

const loadDetail = async (id: number) => {
  detailLoading.value = true
  detailErrorMessage.value = ''
  try {
    detailData.value = await FinanceVoucherApi.getVoucher(id)
  } catch {
    detailErrorMessage.value = '凭证明细加载失败'
  } finally {
    detailLoading.value = false
  }
}

const retryDetail = async () => {
  if (currentVoucherId.value) {
    await loadDetail(currentVoucherId.value)
  }
}

const clearDetailDrawer = () => {
  detailData.value = undefined
  detailErrorMessage.value = ''
  currentVoucherId.value = undefined
}

const clearGenerateDialog = () => {
  templateOptions.value = []
  generateLoading.value = false
  generateSubmitting.value = false
  generateFormRef.value?.clearValidate?.()
  generateForm.ledgerId = queryParams.ledgerId
  generateForm.bizType = undefined
  generateForm.bizId = undefined
  generateForm.templateId = undefined
  generateForm.voucherTime = undefined
  generateForm.remark = ''
}

const clearReverseDialog = () => {
  reverseTarget.value = undefined
  rowActionLoadingId.value = undefined
  rowActionLoadingType.value = ''
  reverseSubmitting.value = false
  reverseFormRef.value?.clearValidate?.()
  reverseForm.voucherTime = undefined
  reverseForm.remark = ''
}

const handleRefresh = async () => {
  if (!canRefresh.value) return
  refreshing.value = true
  try {
    await getList()
  } finally {
    refreshing.value = false
  }
}

const syncViewportWidth = () => {
  viewportWidth.value = window.innerWidth
}

onMounted(async () => {
  syncViewportWidth()
  window.addEventListener('resize', syncViewportWidth)
  await Promise.allSettled([loadLedgers(), getList()])
})

onUnmounted(() => {
  window.removeEventListener('resize', syncViewportWidth)
})
</script>

<style scoped>
@import '../shared/readOnlyPage.css';

.finance-voucher-page {
  background: #f8fafc;
}

.finance-voucher-page :deep(.finance-shell__header-card .el-card__body),
.finance-voucher-page :deep(.finance-shell__filter-card .el-card__body),
.finance-voucher-page :deep(.finance-shell__table-card .el-card__body) {
  padding: 12px 14px;
}

.finance-voucher-page :deep(.finance-shell__page-header),
.finance-voucher-page :deep(.finance-shell__toolbar),
.finance-voucher-page :deep(.finance-shell__section-head) {
  gap: 10px;
}

.finance-voucher-page__page-header-main {
  flex: 1 1 auto;
}

.finance-voucher-page__page-header-actions {
  align-self: flex-start;
}

.finance-voucher-page :deep(.finance-shell__page-title) {
  font-size: 17px;
  letter-spacing: 0.01em;
}

.finance-voucher-page__summary-grid {
  margin-top: 8px;
  gap: 8px;
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.finance-voucher-page__summary-card {
  min-height: 64px;
  padding: 9px 11px;
  border-color: #dbe4f0;
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
}

.finance-voucher-page__summary-card .finance-shell__metric-label {
  font-size: 11px;
}

.finance-voucher-page__summary-card .finance-shell__metric-value {
  margin-top: 4px;
  font-size: 15px;
  line-height: 1.2;
}

.finance-voucher-page__summary-card--accent {
  border-color: #fecdd3;
  background: linear-gradient(135deg, #fff1f2 0%, #ffffff 100%);
}

.finance-voucher-page__summary-value--success {
  color: #059669;
}

.finance-voucher-page__summary-value--accent {
  color: #e11d48;
}

.finance-voucher-page :deep(.el-card__body) {
  padding: 0;
}

.finance-voucher-page .finance-shell__page-header-actions :deep(.el-button),
.finance-voucher-page .finance-shell__toolbar-actions :deep(.el-button),
.finance-voucher-page .finance-shell__query-actions :deep(.el-button) {
  border-radius: 10px;
}

.finance-voucher-page :deep(.finance-shell__page-header-actions .el-button),
.finance-voucher-page :deep(.finance-shell__toolbar-actions .el-button),
.finance-voucher-page :deep(.finance-shell__query-actions .el-button) {
  padding-left: 11px;
  padding-right: 11px;
}

.finance-voucher-page :deep(.finance-shell__query-form) {
  gap: 10px;
}

.finance-voucher-page :deep(.finance-shell__query-grid) {
  gap: 0 10px;
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.finance-voucher-page :deep(.finance-shell__query-grid .el-form-item) {
  margin-bottom: 8px;
}

.finance-voucher-page :deep(.finance-shell__query-grid .el-form-item__label) {
  color: #64748b;
  font-size: 12px;
}

.finance-voucher-page :deep(.el-input__wrapper),
.finance-voucher-page :deep(.el-select__wrapper),
.finance-voucher-page :deep(.el-date-editor.el-input__wrapper) {
  box-shadow: inset 0 0 0 1px #dbe4f0;
  border-radius: 10px;
}

.finance-voucher-page :deep(.finance-shell__toolbar-card) {
  padding-top: 0;
}

.finance-voucher-page .finance-shell__row-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.finance-voucher-page .finance-shell__column-header {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #0f172a;
  font-size: 13px;
  font-weight: 700;
}

.finance-voucher-page .finance-shell__column-header--pipeline {
  color: #2563eb;
}

.finance-voucher-page .finance-shell__column-icon {
  color: #2563eb;
  font-size: 14px;
}

.finance-voucher-page :deep(.finance-shell__table--dense .el-table__cell) {
  padding-top: 7px;
  padding-bottom: 7px;
}

.finance-voucher-page :deep(.finance-shell__table) {
  border-radius: 14px;
}

.finance-voucher-page :deep(.finance-shell__table .el-table__header-wrapper th.el-table__cell) {
  padding-top: 8px;
  padding-bottom: 8px;
}

.finance-voucher-page :deep(.finance-shell__table .el-table__body-wrapper td.el-table__cell) {
  font-size: 13px;
}

.finance-voucher-page :deep(.finance-shell__drawer-modal) {
  backdrop-filter: blur(4px);
}

.finance-voucher-page :deep(.el-drawer__body) {
  padding: 0 16px 16px;
}

.finance-voucher-page :deep(.el-drawer__header) {
  margin-bottom: 0;
  padding: 16px 16px 12px;
}

.finance-voucher-page :deep(.finance-shell__context-card) {
  margin-bottom: 10px;
  padding: 14px 16px;
}

.finance-voucher-page :deep(.finance-shell__metric-grid) {
  gap: 8px;
}

.finance-voucher-page :deep(.finance-shell__metric-card) {
  padding: 9px 11px;
}

.finance-voucher-page .finance-shell__context-meta-line {
  overflow: hidden;
  margin-top: 4px;
  color: var(--erp-slate-300);
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

@media (max-width: 768px) {
  .finance-voucher-page :deep(.finance-shell__header-card .el-card__body),
  .finance-voucher-page :deep(.finance-shell__filter-card .el-card__body),
  .finance-voucher-page :deep(.finance-shell__table-card .el-card__body) {
    padding: 12px;
  }

  .finance-voucher-page :deep(.finance-shell__query-actions .el-button),
  .finance-voucher-page :deep(.finance-shell__toolbar-actions .el-button),
  .finance-voucher-page :deep(.finance-shell__page-header-actions .el-button) {
    flex: 1 1 0;
  }

  .finance-voucher-page :deep(.finance-shell__context-card) {
    padding: 12px 14px;
  }
}

@media (max-width: 1200px) {
  .finance-voucher-page__summary-grid,
  .finance-voucher-page :deep(.finance-shell__query-grid) {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 960px) {
  .finance-voucher-page :deep(.finance-shell__query-grid) {
    grid-template-columns: 1fr;
  }
}
</style>
