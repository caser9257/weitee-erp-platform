<template>
  <div class="finance-shell finance-shell__stack finance-dual-ledger-page">
    <ContentWrap class="finance-shell__header-card">
      <div class="finance-shell__page-header">
        <div class="finance-shell__page-header-main">
          <div class="finance-shell__page-title">双账核对比对</div>
          <div class="finance-shell__page-metrics">
            <span class="finance-shell__metric-chip">缁撴灉 {{ total }}</span>
            <span class="finance-shell__metric-chip">涓€鑷?{{ consistentCount }}</span>
            <span class="finance-shell__metric-chip">涓嶄竴鑷?{{ inconsistentCount }}</span>
          </div>
        </div>
        <div class="finance-shell__page-header-actions">
          <el-button plain :loading="loadingList" :disabled="loadingList" @click="handleRefresh">
            <Icon icon="ep:refresh" class="mr-5px" />
            鍒锋柊
          </el-button>
        </div>
      </div>
    </ContentWrap>

    <ContentWrap class="finance-shell__filter-card">
      <div class="finance-shell__section-head">
        <div class="finance-shell__section-title">筛选条件</div>
      </div>
      <el-form ref="queryFormRef" :model="queryParams" label-width="88px" class="finance-shell__query-form" @submit.prevent>
        <div class="finance-shell__query-grid finance-shell__query-grid--wide">
          <el-form-item label="涓氬姟绫诲瀷" prop="bizType">
            <el-select v-model="queryParams.bizType" placeholder="璇烽€夋嫨涓氬姟绫诲瀷" clearable class="!w-full">
              <el-option v-for="item in bizTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="涓氬姟鍗曞彿" prop="bizNo">
            <el-input v-model="queryParams.bizNo" placeholder="请输入业务单号" clearable class="!w-full" @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="比对状态" prop="compareStatus">
            <el-select v-model="queryParams.compareStatus" placeholder="请选择比对状态" clearable class="!w-full">
              <el-option v-for="item in DUAL_LEDGER_COMPARE_STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="是否一致" prop="consistent">
            <el-select v-model="queryParams.consistent" placeholder="请选择是否一致" clearable class="!w-full">
              <el-option label="一致" :value="true" />
              <el-option label="不一致" :value="false" />
            </el-select>
          </el-form-item>
        </div>

        <div class="finance-shell__query-actions">
          <el-button type="primary" :loading="loadingList" @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" />
            鏌ヨ
          </el-button>
          <el-button @click="resetQuery">
            <Icon icon="ep:refresh-left" class="mr-5px" />
            閲嶇疆
          </el-button>
        </div>
      </el-form>
    </ContentWrap>

    <ContentWrap class="finance-shell__table-card">
      <div class="finance-shell__toolbar">
        <div class="finance-shell__table-toolbar-main">
          <div class="finance-shell__section-title">瀵规瘮缁撴灉鍒楄〃</div>
          <div class="finance-shell__toolbar-count">当前共 <strong>{{ total }}</strong> 条</div>
        </div>
      </div>

      <el-alert
        v-if="listError"
        class="mb-12px"
        :closable="false"
        show-icon
        type="error"
        :title="listError"
      >
        <template #default>
          <el-button link type="primary" :disabled="loadingList" @click="getList">閲嶆柊鍔犺浇</el-button>
        </template>
      </el-alert>

      <template v-if="loadingList || list.length">
        <div class="finance-shell__table-wrap">
          <el-table v-loading="loadingList" :data="list" row-key="bizId" stripe class="finance-shell__table finance-shell__table--dense" :show-overflow-tooltip="false">
            <el-table-column min-width="220">
              <template #header>
                <span class="finance-shell__column-header">
                  <Icon icon="ep:document" class="finance-shell__column-icon" />
                  涓氬姟淇℃伅
                </span>
              </template>
              <template #default="{ row }">
                <div class="finance-shell__primary-cell">
                  <span class="finance-shell__primary-text finance-shell__mono">{{ row.bizNo || '-' }}</span>
                  <span class="finance-shell__muted-text">{{ row.bizTypeName || '-' }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column min-width="200">
              <template #header>
                <span class="finance-shell__column-header finance-shell__column-header--pipeline">
                  <Icon icon="ep:office-building" class="finance-shell__column-icon" />
                  澶栬处鍑瘉
                </span>
              </template>
              <template #default="{ row }">
                <div class="finance-shell__primary-cell">
                  <span class="finance-shell__primary-text finance-shell__mono">{{ row.externalVoucherNo || '-' }}</span>
                  <span class="finance-shell__muted-text">{{ row.externalLedgerName || '-' }}</span>
                  <div class="finance-shell__row-tags">
                    <span class="finance-shell__metric-pill finance-shell__metric-pill--primary">
                      鍊?{{ formatAmount(row.externalDebitAmount) }}
                    </span>
                    <span class="finance-shell__metric-pill finance-shell__metric-pill--success">
                      璐?{{ formatAmount(row.externalCreditAmount) }}
                    </span>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column min-width="200">
              <template #header>
                <span class="finance-shell__column-header finance-shell__column-header--pipeline">
                  <Icon icon="ep:office-building" class="finance-shell__column-icon" />
                  鍐呰处鍑瘉
                </span>
              </template>
              <template #default="{ row }">
                <div class="finance-shell__primary-cell">
                  <span class="finance-shell__primary-text finance-shell__mono">{{ row.internalVoucherNo || '-' }}</span>
                  <span class="finance-shell__muted-text">{{ row.internalLedgerName || '-' }}</span>
                  <div class="finance-shell__row-tags">
                    <span class="finance-shell__metric-pill finance-shell__metric-pill--primary">
                      鍊?{{ formatAmount(row.internalDebitAmount) }}
                    </span>
                    <span class="finance-shell__metric-pill finance-shell__metric-pill--success">
                      璐?{{ formatAmount(row.internalCreditAmount) }}
                    </span>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column min-width="180" align="right">
              <template #header>
                <span class="finance-shell__column-header finance-shell__column-header--pipeline">
                  <Icon icon="ep:money" class="finance-shell__column-icon" />
                  宸
                </span>
              </template>
              <template #default="{ row }">
                <div class="finance-shell__primary-cell finance-shell__primary-cell--right">
                  <span class="finance-shell__amount finance-shell__mono" :class="{ 'text-red-500': row.debitAmountDiff !== 0 }">
                    鍊熷樊 {{ formatAmount(row.debitAmountDiff) }}
                  </span>
                  <span class="finance-shell__muted-text finance-shell__mono" :class="{ 'text-red-500': row.creditAmountDiff !== 0 }">
                    璐峰樊 {{ formatAmount(row.creditAmountDiff) }}
                  </span>
                </div>
              </template>
            </el-table-column>
            <el-table-column min-width="120" align="center">
              <template #header>
                <span class="finance-shell__column-header">
                  <Icon icon="ep:circle-check" class="finance-shell__column-icon" />
                  瀵规瘮鐘舵€?                </span>
              </template>
              <template #default="{ row }">
                <div class="finance-shell__primary-cell finance-shell__primary-cell--center">
                  <el-tag :type="row.consistent ? 'success' : 'danger'" effect="light">
                    {{ row.compareStatusName || (row.consistent ? '一致' : '不一致') }}
                  </el-tag>
                  <div v-if="row.issueMessages && row.issueMessages.length" class="mt-1">
                    <el-tooltip v-for="(msg, idx) in row.issueMessages.slice(0, 2)" :key="idx" :content="msg" placement="top">
                      <span class="finance-shell__muted-text text-xs block truncate">{{ msg }}</span>
                    </el-tooltip>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="鎿嶄綔" fixed="right" align="center" width="180">
              <template #default="{ row }">
                <div class="finance-shell__row-actions">
                  <el-button
                    link
                    type="primary"
                    :disabled="loadingDetail && detailBizId === row.bizId"
                    @click.stop="handleViewDetail(row)"
                  >
                    鏌ョ湅宸紓
                  </el-button>
                  <el-button
                    v-if="canRecompute"
                    link
                    type="primary"
                    :loading="recomputingBizId === row.bizId"
                    :disabled="recomputingBizId === row.bizId"
                    @click.stop="handleRecompute(row)"
                  >
                    閲嶇畻
                  </el-button>
                  <el-button
                    v-if="canExport && canOpenExport(row)"
                    link
                    type="primary"
                    :loading="isRowExporting(row)"
                    :disabled="isRowExporting(row)"
                    @click.stop="handleOpenExport(row)"
                  >
                    瀵煎嚭濂楄处
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <Pagination v-if="total > 0" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
      </template>
      <div v-else class="finance-dual-ledger-page__empty">
        <el-empty description="暂无双账核对比对数据">
          <template #image>
            <div class="finance-shell__empty-icon">
              <Icon icon="ep:files" />
            </div>
          </template>
        </el-empty>
      </div>
    </ContentWrap>

    <el-drawer
      v-model="detailDrawerVisible"
      size="520px"
      destroy-on-close
      :with-header="false"
      :modal-class="'finance-shell__drawer-mask'"
      @closed="clearDetailDrawer"
    >
      <div class="finance-dual-ledger-page__drawer">
        <div class="finance-shell__context-card">
          <div class="finance-shell__context-main">
            <div class="finance-shell__context-title">{{ detailData?.bizNo || '-' }}</div>
            <div class="finance-shell__context-subtitle">{{ detailData?.bizTypeName || '双账套差异明细' }}</div>
          </div>
          <div class="finance-shell__context-meta">
            <span class="finance-shell__page-chip">{{ detailData?.compareStatusName || '-' }}</span>
            <span class="finance-shell__page-chip">{{ detailData?.consistent ? '一致' : '不一致' }}</span>
          </div>
        </div>

        <div v-if="loadingDetail" class="finance-dual-ledger-page__drawer-loading">
          <el-skeleton :rows="8" animated />
        </div>
        <template v-else>
          <div v-if="detailError" class="finance-dual-ledger-page__drawer-error">
            <el-alert type="error" :closable="false" show-icon :title="detailError">
              <template #default>
                <el-button link type="primary" @click="loadDetail">閲嶆柊鍔犺浇</el-button>
              </template>
            </el-alert>
          </div>
          <div v-else class="finance-dual-ledger-page__drawer-body">
            <div class="finance-shell__metric-grid finance-dual-ledger-page__drawer-metrics">
              <div class="finance-shell__metric-card">
                <div class="finance-shell__metric-label">澶栭儴閲戦</div>
                <div class="finance-shell__metric-value">{{ formatAmount(detailData?.externalDebitAmount) }}</div>
              </div>
              <div class="finance-shell__metric-card">
                <div class="finance-shell__metric-label">鍐呴儴閲戦</div>
                <div class="finance-shell__metric-value">{{ formatAmount(detailData?.internalDebitAmount) }}</div>
              </div>
              <div class="finance-shell__metric-card">
                <div class="finance-shell__metric-label">鍊熸柟宸</div>
                <div class="finance-shell__metric-value">{{ formatAmount(detailData?.debitAmountDiff) }}</div>
              </div>
              <div class="finance-shell__metric-card">
                <div class="finance-shell__metric-label">璐锋柟宸</div>
                <div class="finance-shell__metric-value">{{ formatAmount(detailData?.creditAmountDiff) }}</div>
              </div>
            </div>

            <div v-if="detailData?.issueMessages?.length" class="finance-dual-ledger-page__issues">
              <div class="finance-shell__section-title">闂鎻愮ず</div>
              <div class="finance-dual-ledger-page__issue-list">
                <div v-for="(item, index) in detailData.issueMessages" :key="`${detailData.bizId}-${index}`" class="finance-dual-ledger-page__issue-item">
                  {{ item }}
                </div>
              </div>
            </div>

            <div class="finance-shell__section-title">宸紓鏄庣粏</div>
            <div v-if="selectedDiffItems.length" class="finance-dual-ledger-page__detail-table">
              <el-table :data="selectedDiffItems" stripe class="finance-shell__table finance-shell__table--dense" :show-overflow-tooltip="false">
                <el-table-column label="差异项" min-width="140">
                  <template #default="{ row }">
                    <div class="finance-shell__primary-cell">
                      <span class="finance-shell__primary-text">{{ row.diffItemTypeName || '-' }}</span>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="璁＄畻鍙ｅ緞" min-width="140">
                  <template #default="{ row }">
                    <span class="finance-shell__muted-text">{{ row.calculationTypeName || '-' }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="鍐呴儴閲戦" min-width="120" align="right">
                  <template #default="{ row }">
                    <span class="finance-shell__amount finance-shell__mono">{{ formatAmount(row.internalAmount) }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="澶栭儴閲戦" min-width="120" align="right">
                  <template #default="{ row }">
                    <span class="finance-shell__amount finance-shell__mono">{{ formatAmount(row.externalAmount) }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="宸紓閲戦" min-width="120" align="right">
                  <template #default="{ row }">
                    <span class="finance-shell__amount finance-shell__mono" :class="{ 'text-red-500': Number(row.diffAmount || 0) !== 0 }">
                      {{ formatAmount(row.diffAmount) }}
                    </span>
                  </template>
                </el-table-column>
                <el-table-column label="宸紓姣斾緥" min-width="100" align="right">
                  <template #default="{ row }">
                    <span class="finance-shell__muted-text finance-shell__mono">{{ formatRatio(row.diffRatio) }}</span>
                  </template>
                </el-table-column>
              </el-table>
            </div>
            <div v-else class="finance-dual-ledger-page__empty finance-dual-ledger-page__empty--drawer">
              <el-empty description="暂无差异项目明细">
                <template #image>
                  <div class="finance-shell__empty-icon">
                    <Icon icon="ep:document" />
                  </div>
                </template>
              </el-empty>
            </div>
          </div>
        </template>

        <div class="finance-dual-ledger-page__drawer-footer">
          <el-button @click="detailDrawerVisible = false">鍏抽棴</el-button>
        </div>
      </div>
    </el-drawer>

    <!-- 瀵煎嚭濂楄处閫夋嫨寮圭獥 -->
    <el-dialog
      v-model="exportDialogVisible"
      title="閫夋嫨瀵煎嚭璐︾翱"
      width="400px"
      :close-on-click-modal="!exportingBizId"
      :close-on-press-escape="!exportingBizId"
      destroy-on-close
    >
      <div class="finance-dual-ledger-page__export-options">
        <div
          class="finance-dual-ledger-page__export-option"
          :class="{
            'finance-dual-ledger-page__export-option--active': exportLedgerSide === 'external',
            'finance-dual-ledger-page__export-option--disabled': !hasExternalVoucher(exportRow!)
          }"
          @click="hasExternalVoucher(exportRow!) && (exportLedgerSide = 'external')"
        >
          <div class="finance-dual-ledger-page__export-option-icon">
            <Icon icon="ep:office-building" />
          </div>
          <div class="finance-dual-ledger-page__export-option-info">
            <div class="finance-dual-ledger-page__export-option-title">{{ exportRow?.externalLedgerName || '澶栭儴璐︾翱' }}</div>
            <div class="finance-dual-ledger-page__export-option-desc">
              <span v-if="exportRow?.externalVoucherNo" class="finance-shell__mono">鍑瘉鍙凤細{{ exportRow.externalVoucherNo }}</span>
              <span v-else>鏆傛棤鍑瘉</span>
            </div>
              <div v-if="!hasExternalVoucher(exportRow!)" class="finance-dual-ledger-page__export-option-hint">
                当前账簿暂无可导出凭证
              </div>
          </div>
          <div v-if="exportLedgerSide === 'external'" class="finance-dual-ledger-page__export-option-check">
            <Icon icon="ep:check" />
          </div>
        </div>

        <div
          class="finance-dual-ledger-page__export-option"
          :class="{
            'finance-dual-ledger-page__export-option--active': exportLedgerSide === 'internal',
            'finance-dual-ledger-page__export-option--disabled': !hasInternalVoucher(exportRow!)
          }"
          @click="hasInternalVoucher(exportRow!) && (exportLedgerSide = 'internal')"
        >
          <div class="finance-dual-ledger-page__export-option-icon">
            <Icon icon="ep:office-building" />
          </div>
          <div class="finance-dual-ledger-page__export-option-info">
            <div class="finance-dual-ledger-page__export-option-title">{{ exportRow?.internalLedgerName || '鍐呴儴璐︾翱' }}</div>
            <div class="finance-dual-ledger-page__export-option-desc">
              <span v-if="exportRow?.internalVoucherNo" class="finance-shell__mono">鍑瘉鍙凤細{{ exportRow.internalVoucherNo }}</span>
              <span v-else>鏆傛棤鍑瘉</span>
            </div>
            <div v-if="!hasInternalVoucher(exportRow!)" class="finance-dual-ledger-page__export-option-hint">
              褰撳墠璐︾翱鏆傛棤鍙鍑哄嚟璇?            </div>
          </div>
          <div v-if="exportLedgerSide === 'internal'" class="finance-dual-ledger-page__export-option-check">
            <Icon icon="ep:check" />
          </div>
        </div>
      </div>

      <template #footer>
        <el-button :disabled="!!exportingBizId" @click="exportDialogVisible = false">鍙栨秷</el-button>
        <el-button
          type="primary"
          :loading="!!exportingBizId"
          :disabled="!exportRow || (exportLedgerSide === 'external' && !hasExternalVoucher(exportRow)) || (exportLedgerSide === 'internal' && !hasInternalVoucher(exportRow))"
          @click="handleConfirmExport"
        >
          纭瀵煎嚭
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ErpBizType } from '@/utils/constants'
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from '@/hooks/web/useMessage'
import download from '@/utils/download'
import {
  DualLedgerResultApi,
  DualLedgerDiffItemDetailVO,
  DualLedgerResultVO,
  DualLedgerResultPageReqVO,
  DUAL_LEDGER_COMPARE_STATUS_OPTIONS
} from '@/api/erp/finance/dual-ledger-result'
import { canAccessDualLedgerResult, canRecomputeDualLedgerResult, canExportDualLedgerResult } from '@/utils/financePermission'

defineOptions({ name: 'ErpFinanceDualLedgerResult' })

const router = useRouter()
const message = useMessage()

const loadingList = ref(false)
const recomputingBizId = ref<number | undefined>(undefined)
const loadingDetail = ref(false)
const list = ref<DualLedgerResultVO[]>([])
const total = ref(0)
const listError = ref('')
const detailDrawerVisible = ref(false)
const detailError = ref('')
const detailBizType = ref<number | undefined>(undefined)
const detailBizId = ref<number | undefined>(undefined)
const detailData = ref<DualLedgerResultVO>()

// 瀵煎嚭濂楄处鐩稿叧鐘舵€?const exportDialogVisible = ref(false)
const exportLedgerSide = ref<'external' | 'internal'>('external')
const exportRow = ref<DualLedgerResultVO | null>(null)
const exportingBizId = ref<number | undefined>(undefined)

const queryParams = reactive<DualLedgerResultPageReqVO>({
  pageNo: 1,
  pageSize: 20,
  bizType: undefined,
  bizNo: undefined,
  compareStatus: undefined,
  consistent: undefined
})

const bizTypeOptions = [
  { label: '閲囪喘鍏ュ簱', value: ErpBizType.PURCHASE_IN },
  { label: '采购退货', value: ErpBizType.PURCHASE_RETURN },
  { label: '销售出库', value: ErpBizType.SALE_OUT },
  { label: '销售退货', value: ErpBizType.SALE_RETURN },
  { label: '濮斿鍏ュ簱', value: ErpBizType.OUTSOURCE_INBOUND },
  { label: '鑷埗鍏ュ簱', value: ErpBizType.PRODUCTION_INBOUND },
  { label: '璐圭敤鎶ラ攢', value: ErpBizType.FINANCE_EXPENSE }
]

const consistentCount = computed(() => list.value.filter(item => item.consistent).length)
const inconsistentCount = computed(() => list.value.filter(item => !item.consistent).length)
const selectedDiffItems = computed<DualLedgerDiffItemDetailVO[]>(() => detailData.value?.diffItemDetails || [])

const canAccess = computed(() => canAccessDualLedgerResult())
const canRecompute = computed(() => canRecomputeDualLedgerResult())
const canExport = computed(() => canExportDualLedgerResult())

const formatAmount = (value?: number) =>
  value == null ? '-' : Number(value).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
const formatRatio = (value?: number) => (value == null ? '-' : `${Number(value).toFixed(2)}%`)

// 瀵煎嚭濂楄处鐩稿叧鍑芥暟
const hasExternalVoucher = (row: DualLedgerResultVO) => !!row.externalVoucherId
const hasInternalVoucher = (row: DualLedgerResultVO) => !!row.internalVoucherId
const canOpenExport = (row: DualLedgerResultVO) => hasExternalVoucher(row) || hasInternalVoucher(row)
const isRowExporting = (row: DualLedgerResultVO) => exportingBizId.value === row.bizId

const handleOpenExport = (row: DualLedgerResultVO) => {
  if (!canOpenExport(row) || isRowExporting(row)) return
  exportRow.value = row
  // 默认选中第一个可用的账套侧
  if (hasExternalVoucher(row)) {
    exportLedgerSide.value = 'external'
  } else {
    exportLedgerSide.value = 'internal'
  }
  exportDialogVisible.value = true
}

const handleConfirmExport = async () => {
  if (!exportRow.value || exportingBizId.value) return
  const row = exportRow.value
  // 鏍￠獙閫変腑渚ф槸鍚︽湁鍑瘉
  if (exportLedgerSide.value === 'external' && !hasExternalVoucher(row)) {
    message.warning('当前账簿暂无可导出凭证')
    return
  }
  if (exportLedgerSide.value === 'internal' && !hasInternalVoucher(row)) {
    message.warning('当前账簿暂无可导出凭证')
    return
  }
  exportingBizId.value = row.bizId
  try {
    const data = await DualLedgerResultApi.exportSingleLedger({
      bizType: row.bizType!,
      bizId: row.bizId!,
      ledgerSide: exportLedgerSide.value
    })
    // 鏂囦欢鍚嶄娇鐢ㄤ腑鎬ф爣棰橈紝涓嶅惈璐︾翱鍚?    const bizNo = row.bizNo || '鍑瘉鏄庣粏瀵煎嚭'
    const fileName = bizNo === '鍑瘉鏄庣粏瀵煎嚭' ? '鍑瘉鏄庣粏瀵煎嚭.xlsx' : `${bizNo}-鍑瘉鏄庣粏瀵煎嚭.xlsx`
    download.excel(data, fileName)
    message.success('瀵煎嚭鎴愬姛')
    exportDialogVisible.value = false
  } catch {
    // 瀵煎嚭澶辫触锛屽脊绐椾繚鎸佹墦寮€
  } finally {
    exportingBizId.value = undefined
  }
}

const queryFormRef = ref()

const getList = async () => {
  if (!canAccess.value) {
    list.value = []
    total.value = 0
    listError.value = ''
    return
  }
  loadingList.value = true
  listError.value = ''
  try {
    const data = await DualLedgerResultApi.getDualLedgerResultPage(queryParams)
    list.value = data.list || []
    total.value = data.total || 0
  } catch {
    list.value = []
    total.value = 0
    listError.value = '双账套结果加载失败，请稍后重试。'
  } finally {
    loadingList.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryFormRef.value?.resetFields()
  queryParams.pageNo = 1
  getList()
}

const handleRefresh = () => {
  getList()
}

const handleViewDetail = async (row: DualLedgerResultVO) => {
  if (!row.bizType || !row.bizId) {
    return
  }
  detailBizType.value = row.bizType
  detailBizId.value = row.bizId
  detailDrawerVisible.value = true
  detailData.value = row
  await loadDetail()
}

const loadDetail = async () => {
  if (!detailBizType.value || !detailBizId.value) {
    return
  }
  loadingDetail.value = true
  detailError.value = ''
  try {
    detailData.value = await DualLedgerResultApi.getDualLedgerResult(detailBizType.value, detailBizId.value)
  } catch {
    detailError.value = '璇︽儏鍔犺浇澶辫触锛岃绋嶅悗閲嶈瘯'
  } finally {
    loadingDetail.value = false
  }
}

const clearDetailDrawer = () => {
  detailError.value = ''
  detailBizType.value = undefined
  detailBizId.value = undefined
  detailData.value = undefined
}

const handleRecompute = async (row: DualLedgerResultVO) => {
  if (!canRecompute.value || !row.bizType || !row.bizId || recomputingBizId.value === row.bizId) return
  recomputingBizId.value = row.bizId
  try {
    await DualLedgerResultApi.recomputeDualLedgerResult({
      bizType: row.bizType,
      bizId: row.bizId
    })
    message.success('閲嶇畻鎴愬姛')
    await getList()
    if (detailDrawerVisible.value && detailBizType.value === row.bizType && detailBizId.value === row.bizId) {
      await loadDetail()
    }
  } catch {
    // 閲嶇畻澶辫触
  } finally {
    recomputingBizId.value = undefined
  }
}

onMounted(() => {
  if (!canAccess.value) {
    message.error('当前角色无权限访问该页面')
    router.replace('/')
    return
  }
  getList()
})
</script>

<style scoped>
@import '../shared/readOnlyPage.css';

.finance-dual-ledger-page {
  min-height: 100%;
}

.finance-dual-ledger-page :deep(.finance-shell__header-card .el-card__body),
.finance-dual-ledger-page :deep(.finance-shell__filter-card .el-card__body),
.finance-dual-ledger-page :deep(.finance-shell__table-card .el-card__body) {
  padding: 14px 16px;
}

.finance-dual-ledger-page :deep(.finance-shell__page-header),
.finance-dual-ledger-page :deep(.finance-shell__toolbar),
.finance-dual-ledger-page :deep(.finance-shell__section-head) {
  gap: 10px;
}

.finance-dual-ledger-page :deep(.finance-shell__page-title) {
  font-size: 18px;
  letter-spacing: 0.01em;
}

.finance-dual-ledger-page :deep(.finance-shell__query-form) {
  gap: 10px;
}

.finance-dual-ledger-page :deep(.finance-shell__query-grid) {
  gap: 0 10px;
}

.finance-dual-ledger-page :deep(.finance-shell__query-grid .el-form-item) {
  margin-bottom: 8px;
}

.finance-dual-ledger-page :deep(.finance-shell__query-grid .el-form-item__label) {
  color: #64748b;
  font-size: 12px;
}

.finance-dual-ledger-page :deep(.el-input__wrapper),
.finance-dual-ledger-page :deep(.el-select__wrapper),
.finance-dual-ledger-page :deep(.el-date-editor.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: inset 0 0 0 1px #dbe4f0;
}

.finance-dual-ledger-page :deep(.finance-shell__table) {
  border-radius: 14px;
}

.finance-dual-ledger-page :deep(.finance-shell__table--dense .el-table__cell) {
  padding-top: 8px;
  padding-bottom: 8px;
}

.finance-dual-ledger-page .finance-shell__row-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.finance-dual-ledger-page .finance-shell__column-header {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #0f172a;
  font-size: 13px;
  font-weight: 700;
}

.finance-dual-ledger-page .finance-shell__column-header--pipeline,
.finance-dual-ledger-page .finance-shell__column-icon {
  color: #2563eb;
}

.finance-dual-ledger-page .finance-shell__column-icon {
  font-size: 14px;
}

.finance-dual-ledger-page .finance-shell__primary-cell--right {
  align-items: flex-end;
  text-align: right;
}

.finance-dual-ledger-page .finance-shell__primary-cell--center {
  align-items: center;
  text-align: center;
}

.finance-dual-ledger-page__empty {
  border: 1px dashed #cbd5e1;
  border-radius: 16px;
  background: #f8fafc;
  padding: 8px;
}

.finance-dual-ledger-page__empty--drawer {
  padding: 16px;
}

.finance-dual-ledger-page :deep(.el-drawer__body) {
  padding: 0;
}

.finance-dual-ledger-page__drawer {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #f8fafc;
}

.finance-dual-ledger-page__drawer-loading,
.finance-dual-ledger-page__drawer-error,
.finance-dual-ledger-page__drawer-body {
  flex: 1 1 auto;
  min-height: 0;
  overflow-y: auto;
  padding: 16px;
}

.finance-dual-ledger-page__drawer-metrics {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.finance-dual-ledger-page__issues {
  margin-bottom: 16px;
}

.finance-dual-ledger-page__issue-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 10px;
}

.finance-dual-ledger-page__issue-item {
  border: 1px solid #fecdd3;
  border-radius: 12px;
  background: #fff1f2;
  color: #be123c;
  font-size: 12px;
  line-height: 1.6;
  padding: 10px 12px;
}

.finance-dual-ledger-page__detail-table {
  margin-top: 12px;
}

.finance-dual-ledger-page__drawer-footer {
  position: sticky;
  bottom: 0;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 12px 16px;
  border-top: 1px solid #e2e8f0;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(8px);
}

@media (max-width: 1200px) {
  .finance-dual-ledger-page :deep(.finance-shell__query-grid--wide) {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .finance-dual-ledger-page__drawer-metrics {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .finance-dual-ledger-page :deep(.finance-shell__header-card .el-card__body),
  .finance-dual-ledger-page :deep(.finance-shell__filter-card .el-card__body),
  .finance-dual-ledger-page :deep(.finance-shell__table-card .el-card__body) {
    padding: 12px;
  }

  .finance-dual-ledger-page :deep(.finance-shell__query-grid--wide) {
    grid-template-columns: 1fr;
  }

  .finance-dual-ledger-page :deep(.finance-shell__query-actions .el-button),
  .finance-dual-ledger-page :deep(.finance-shell__page-header-actions .el-button) {
    flex: 1 1 0;
  }

  .finance-dual-ledger-page__drawer-loading,
  .finance-dual-ledger-page__drawer-error,
  .finance-dual-ledger-page__drawer-body,
  .finance-dual-ledger-page__drawer-footer {
    padding-left: 12px;
    padding-right: 12px;
  }
}

/* 瀵煎嚭濂楄处寮圭獥鏍峰紡 */
.finance-dual-ledger-page__export-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.finance-dual-ledger-page__export-option {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.finance-dual-ledger-page__export-option:hover:not(.finance-dual-ledger-page__export-option--disabled) {
  border-color: #93c5fd;
  background: #f0f7ff;
}

.finance-dual-ledger-page__export-option--active {
  border-color: #2563eb;
  background: #eff6ff;
}

.finance-dual-ledger-page__export-option--disabled {
  opacity: 0.5;
  cursor: not-allowed;
  background: #f8fafc;
}

.finance-dual-ledger-page__export-option-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: #f1f5f9;
  color: #64748b;
  font-size: 18px;
  flex-shrink: 0;
}

.finance-dual-ledger-page__export-option--active .finance-dual-ledger-page__export-option-icon {
  background: #dbeafe;
  color: #2563eb;
}

.finance-dual-ledger-page__export-option-info {
  flex: 1;
  min-width: 0;
}

.finance-dual-ledger-page__export-option-title {
  font-size: 14px;
  font-weight: 600;
  color: #0f172a;
}

.finance-dual-ledger-page__export-option-desc {
  font-size: 12px;
  color: #64748b;
  margin-top: 2px;
}

.finance-dual-ledger-page__export-option-hint {
  font-size: 12px;
  color: #ef4444;
  margin-top: 4px;
}

.finance-dual-ledger-page__export-option-check {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #2563eb;
  color: #fff;
  font-size: 14px;
  flex-shrink: 0;
}
</style>
