<template>
  <div class="finance-shell finance-general-ledger-page finance-shell__stack">
    <ContentWrap class="finance-shell__header-card">
      <div class="finance-shell__page-header">
        <div class="finance-shell__page-header-main">
          <div class="finance-shell__page-title">总账</div>
          <div class="finance-shell__page-subtitle">
            按账簿和期间查看科目余额，并穿透至凭证分录明细
          </div>
          <div class="finance-shell__page-metrics">
            <span class="finance-shell__metric-chip finance-shell__metric-chip--primary">
              账簿 {{ selectedLedgerLabel }}
            </span>
            <span class="finance-shell__metric-chip finance-shell__metric-chip--neutral">
              期间 {{ selectedPeriodLabel }}
            </span>
            <span class="finance-shell__metric-chip finance-shell__metric-chip--success">
              结果 {{ balanceTotal }}
            </span>
          </div>
        </div>
        <div class="finance-shell__page-header-actions">
          <el-button
            type="warning"
            plain
            :loading="rebuilding"
            :disabled="!canRebuild"
            v-hasPermi="['erp:finance-voucher:update']"
            @click="handleRebuild"
          >
            <Icon icon="ep:refresh-right" class="mr-5px" />
            重建余额
          </el-button>
        </div>
      </div>
    </ContentWrap>

    <ContentWrap class="finance-shell__filter-card">
      <div class="finance-shell__section-head">
        <div class="finance-shell__section-title">筛选条件</div>
      </div>
      <el-form
        ref="queryFormRef"
        :model="queryParams"
        label-width="76px"
        class="finance-shell__query-form"
        @submit.prevent
      >
        <div class="finance-shell__query-grid finance-shell__query-grid--wide">
          <el-form-item label="账簿" prop="ledgerId">
            <el-select
              v-model="queryParams.ledgerId"
              placeholder="请选择账簿"
              clearable
              filterable
              :loading="loadingLedgers"
              class="!w-full"
              @change="handleLedgerChange"
            >
              <el-option
                v-for="item in ledgerOptions"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="期间" prop="periodId">
            <el-select
              v-model="queryParams.periodId"
              placeholder="请选择期间"
              clearable
              filterable
              :loading="loadingPeriods"
              :disabled="!queryParams.ledgerId"
              class="!w-full"
            >
              <el-option
                v-for="item in periodOptions"
                :key="item.id"
                :label="item.periodCode"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="科目编码" prop="subjectCode">
            <el-input
              v-model="queryParams.subjectCode"
              placeholder="请输入科目编码"
              clearable
              class="!w-full"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="科目名称" prop="subjectName">
            <el-input
              v-model="queryParams.subjectName"
              placeholder="请输入科目名称"
              clearable
              class="!w-full"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
        </div>
        <div class="finance-shell__query-actions">
          <el-button type="primary" :loading="loadingList" :disabled="!canQuery" @click="handleQuery">
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

    <ContentWrap class="finance-shell__table-card">
      <div class="finance-shell__toolbar">
        <div class="finance-shell__table-toolbar-main">
          <div class="finance-shell__section-title">科目余额</div>
          <div class="finance-shell__toolbar-count">
            当前共 <strong>{{ balanceTotal }}</strong> 条
          </div>
        </div>
      </div>

      <el-alert
        v-if="listErrorMessage && !balanceList.length"
        type="error"
        :closable="false"
        show-icon
        class="mb-12px"
        :title="listErrorMessage"
      >
        <template #default>
          <el-button link type="primary" :disabled="loadingList" @click="getList">重新加载</el-button>
        </template>
      </el-alert>

      <template v-else>
        <div v-if="loadingList || balanceList.length" class="finance-shell__table-wrap">
          <el-table
            v-loading="loadingList"
            :data="balanceList"
            row-key="id"
            stripe
            class="finance-shell__table finance-shell__table--dense"
            :show-overflow-tooltip="false"
          >
            <el-table-column min-width="220">
              <template #header>
                <span class="finance-shell__column-header">
                  <Icon icon="ep:collection-tag" class="finance-shell__column-icon" />
                  科目
                </span>
              </template>
              <template #default="{ row }">
                <div class="finance-shell__primary-cell">
                  <span class="finance-shell__primary-text">{{ row.subjectName || '-' }}</span>
                  <span class="finance-shell__muted-text finance-shell__mono">
                    {{ row.subjectCode || '-' }}
                  </span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="期初借方" min-width="120" align="right">
              <template #default="{ row }">
                <span class="finance-shell__amount finance-shell__mono">
                  {{ formatAmount(row.openingDebitAmount) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="期初贷方" min-width="120" align="right">
              <template #default="{ row }">
                <span class="finance-shell__amount finance-shell__mono">
                  {{ formatAmount(row.openingCreditAmount) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="本期借方" min-width="120" align="right">
              <template #default="{ row }">
                <span class="finance-shell__amount finance-shell__mono">
                  {{ formatAmount(row.currentDebitAmount) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="本期贷方" min-width="120" align="right">
              <template #default="{ row }">
                <span class="finance-shell__amount finance-shell__mono">
                  {{ formatAmount(row.currentCreditAmount) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="期末借方" min-width="120" align="right">
              <template #default="{ row }">
                <span class="finance-shell__amount finance-shell__mono">
                  {{ formatAmount(row.endingDebitAmount) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="期末贷方" min-width="120" align="right">
              <template #default="{ row }">
                <span class="finance-shell__amount finance-shell__mono">
                  {{ formatAmount(row.endingCreditAmount) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="操作" fixed="right" align="center" width="120">
              <template #default="{ row }">
                <div class="finance-shell__row-actions">
                  <el-button
                    link
                    type="primary"
                    v-hasPermi="['erp:finance-voucher:query', 'erp:finance-report:query']"
                    :disabled="!canOpenDetail(row)"
                    @click="openDetailDrawer(row)"
                  >
                    查看明细
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <el-empty v-else description="暂无科目余额数据" />

        <Pagination
          v-if="balanceTotal > 0"
          v-model:page="queryParams.pageNo"
          v-model:limit="queryParams.pageSize"
          :total="balanceTotal"
          @pagination="getList"
        />
      </template>
    </ContentWrap>

    <el-drawer
      v-model="detailDrawerOpen"
      :size="drawerSize"
      destroy-on-close
      :with-header="false"
      @closed="clearDetailDrawer"
    >
      <div class="finance-general-ledger-page__drawer">
        <div v-if="detailData" class="finance-shell__context-card finance-general-ledger-page__detail-context">
          <div class="finance-shell__context-main">
            <div class="finance-shell__context-title">{{ detailData.subjectName || '总账明细' }}</div>
            <div class="finance-shell__context-subtitle">
              {{ detailData.subjectCode || '-' }} · {{ detailData.ledgerName || '-' }} ·
              {{ detailData.periodCode || '-' }}
            </div>
          </div>
          <div class="finance-shell__context-meta">
            <div class="finance-shell__context-meta-item">
              <span>期初借方</span>
              <span class="finance-shell__mono">{{ formatAmount(detailData.openingDebitAmount) }}</span>
            </div>
            <div class="finance-shell__context-meta-item">
              <span>期初贷方</span>
              <span class="finance-shell__mono">{{ formatAmount(detailData.openingCreditAmount) }}</span>
            </div>
            <div class="finance-shell__context-meta-item">
              <span>本期借方</span>
              <span class="finance-shell__mono">{{ formatAmount(detailData.totalDebitAmount) }}</span>
            </div>
            <div class="finance-shell__context-meta-item">
              <span>本期贷方</span>
              <span class="finance-shell__mono">{{ formatAmount(detailData.totalCreditAmount) }}</span>
            </div>
            <div class="finance-shell__context-meta-item">
              <span>期末借方</span>
              <span class="finance-shell__mono">{{ formatAmount(detailData.endingDebitAmount) }}</span>
            </div>
            <div class="finance-shell__context-meta-item">
              <span>期末贷方</span>
              <span class="finance-shell__mono">{{ formatAmount(detailData.endingCreditAmount) }}</span>
            </div>
          </div>
        </div>

        <div class="finance-general-ledger-page__drawer-body" v-loading="loadingDetail">
          <el-result
            v-if="detailErrorMessage"
            icon="error"
            title="总账明细加载失败"
            :sub-title="detailErrorMessage"
          >
            <template #extra>
              <el-button type="primary" :disabled="loadingDetail || !selectedRow" @click="retryDetail">
                重试
              </el-button>
            </template>
          </el-result>

          <template v-else>
            <div class="finance-shell__section-head finance-general-ledger-page__detail-head">
              <div class="finance-shell__section-title">凭证明细</div>
              <div class="finance-shell__toolbar-count">
                当前共 <strong>{{ detailData?.items?.length || 0 }}</strong> 条
              </div>
            </div>
            <div v-if="detailData?.items?.length" class="finance-shell__table-wrap">
              <el-table
                :data="detailData.items"
                stripe
                class="finance-shell__table finance-shell__table--dense"
                :show-overflow-tooltip="false"
              >
                <el-table-column label="凭证号" prop="voucherNo" min-width="150" />
                <el-table-column label="凭证时间" prop="voucherTime" min-width="170">
                  <template #default="{ row }">{{ formatDateTimeValue(row.voucherTime) }}</template>
                </el-table-column>
                <el-table-column label="业务类型" prop="bizTypeName" min-width="120" />
                <el-table-column label="业务单号" prop="bizNo" min-width="160" />
                <el-table-column label="摘要" prop="summary" min-width="220" />
                <el-table-column label="借方金额" min-width="120" align="right">
                  <template #default="{ row }">
                    <span class="finance-shell__amount finance-shell__mono">
                      {{ formatAmount(row.debitAmount) }}
                    </span>
                  </template>
                </el-table-column>
                <el-table-column label="贷方金额" min-width="120" align="right">
                  <template #default="{ row }">
                    <span class="finance-shell__amount finance-shell__mono">
                      {{ formatAmount(row.creditAmount) }}
                    </span>
                  </template>
                </el-table-column>
                <el-table-column label="凭证状态" prop="voucherStatusName" min-width="120" />
              </el-table>
            </div>
            <el-empty v-else description="暂无总账明细数据" />
          </template>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useWindowSize } from '@vueuse/core'
import { ElMessageBox } from 'element-plus'
import {
  ErpFinanceGeneralLedgerDetailVO,
  ErpFinanceGeneralLedgerRebuildRespVO,
  ErpFinanceSubjectBalanceVO,
  FinanceGeneralLedgerApi
} from '@/api/erp/finance/general-ledger'
import { ErpFinanceLedgerVO, FinanceLedgerApi } from '@/api/erp/finance/ledger'
import { ErpFinancePeriodVO, FinancePeriodApi } from '@/api/erp/finance/period'
import { formatAmount, formatDateTimeValue } from '@/views/erp/finance/shared/accounting'

defineOptions({ name: 'ErpFinanceGeneralLedger' })

const { width } = useWindowSize()
const message = useMessage()

const queryFormRef = ref()
const loadingLedgers = ref(false)
const loadingPeriods = ref(false)
const loadingList = ref(false)
const loadingDetail = ref(false)
const rebuilding = ref(false)
const detailDrawerOpen = ref(false)
const listErrorMessage = ref('')
const detailErrorMessage = ref('')

const ledgerOptions = ref<ErpFinanceLedgerVO[]>([])
const periodOptions = ref<ErpFinancePeriodVO[]>([])
const balanceList = ref<ErpFinanceSubjectBalanceVO[]>([])
const balanceTotal = ref(0)
const detailData = ref<ErpFinanceGeneralLedgerDetailVO>()
const selectedRow = ref<ErpFinanceSubjectBalanceVO>()

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  ledgerId: undefined as number | undefined,
  periodId: undefined as number | undefined,
  subjectCode: undefined as string | undefined,
  subjectName: undefined as string | undefined
})

const drawerSize = computed(() => {
  if (width.value < 768) return '100%'
  if (width.value < 1200) return '92vw'
  return '860px'
})

const selectedLedgerLabel = computed(
  () => ledgerOptions.value.find((item) => item.id === queryParams.ledgerId)?.name || '未选择'
)
const selectedPeriodLabel = computed(
  () => periodOptions.value.find((item) => item.id === queryParams.periodId)?.periodCode || '未选择'
)
const canQuery = computed(
  () => Boolean(queryParams.ledgerId && queryParams.periodId) && !loadingList.value
)
const canReset = computed(
  () =>
    !loadingList.value &&
    Boolean(
      queryParams.ledgerId ||
        queryParams.periodId ||
        queryParams.subjectCode ||
        queryParams.subjectName
    )
)
const canRebuild = computed(
  () => Boolean(queryParams.ledgerId) && !rebuilding.value && !loadingList.value
)

const canOpenDetail = (row: ErpFinanceSubjectBalanceVO) =>
  Boolean(row.subjectCode && queryParams.ledgerId && queryParams.periodId) && !loadingDetail.value

const getInitialLedgerId = () =>
  ledgerOptions.value.find((item) => item.defaultStatus)?.id ?? ledgerOptions.value[0]?.id

const getLatestPeriodId = () => periodOptions.value[0]?.id

const loadLedgers = async () => {
  loadingLedgers.value = true
  try {
    ledgerOptions.value = await FinanceLedgerApi.getLedgerSimpleList()
    if (!queryParams.ledgerId) {
      queryParams.ledgerId = getInitialLedgerId()
    }
  } finally {
    loadingLedgers.value = false
  }
}

const loadPeriods = async (autoSelectLatest = false) => {
  if (!queryParams.ledgerId) {
    periodOptions.value = []
    queryParams.periodId = undefined
    return
  }
  loadingPeriods.value = true
  try {
    const data = await FinancePeriodApi.getPeriodPage({
      pageNo: 1,
      pageSize: 100,
      ledgerId: queryParams.ledgerId
    })
    periodOptions.value = data?.list || []
    if (autoSelectLatest) {
      queryParams.periodId = getLatestPeriodId()
      return
    }
    if (!periodOptions.value.some((item) => item.id === queryParams.periodId)) {
      queryParams.periodId = getLatestPeriodId()
    }
  } finally {
    loadingPeriods.value = false
  }
}

const initializeDefaultSelection = async () => {
  if (!queryParams.ledgerId) {
    queryParams.ledgerId = getInitialLedgerId()
  }
  if (!queryParams.ledgerId) {
    return
  }
  await loadPeriods(true)
  await getList()
}

const clearDetailDrawer = () => {
  detailData.value = undefined
  detailErrorMessage.value = ''
  selectedRow.value = undefined
}

const getList = async () => {
  if (!queryParams.ledgerId || !queryParams.periodId) {
    balanceList.value = []
    balanceTotal.value = 0
    return
  }
  loadingList.value = true
  listErrorMessage.value = ''
  try {
    const data = await FinanceGeneralLedgerApi.getSubjectBalancePage({
      pageNo: queryParams.pageNo,
      pageSize: queryParams.pageSize,
      ledgerId: queryParams.ledgerId,
      periodId: queryParams.periodId,
      subjectCode: queryParams.subjectCode,
      subjectName: queryParams.subjectName
    })
    balanceList.value = data?.list || []
    balanceTotal.value = data?.total || 0
  } catch {
    if (!balanceList.value.length) {
      listErrorMessage.value = '科目余额加载失败，请稍后重试'
    }
  } finally {
    loadingList.value = false
  }
}

const handleQuery = async () => {
  queryParams.pageNo = 1
  await getList()
}

const resetQuery = async () => {
  queryFormRef.value?.resetFields()
  queryParams.pageNo = 1
  balanceList.value = []
  balanceTotal.value = 0
  listErrorMessage.value = ''
  detailDrawerOpen.value = false
  clearDetailDrawer()
  await initializeDefaultSelection()
}

const handleLedgerChange = async () => {
  queryParams.pageNo = 1
  balanceList.value = []
  balanceTotal.value = 0
  detailDrawerOpen.value = false
  clearDetailDrawer()
  await loadPeriods(true)
  await getList()
}

const loadDetail = async () => {
  if (!selectedRow.value?.subjectCode || !queryParams.ledgerId || !queryParams.periodId) {
    return
  }
  loadingDetail.value = true
  detailErrorMessage.value = ''
  try {
    detailData.value = await FinanceGeneralLedgerApi.getGeneralLedgerDetail({
      ledgerId: queryParams.ledgerId,
      periodId: queryParams.periodId,
      subjectCode: selectedRow.value.subjectCode
    })
  } catch {
    detailErrorMessage.value = '请检查网络或稍后重试'
  } finally {
    loadingDetail.value = false
  }
}

const openDetailDrawer = async (row: ErpFinanceSubjectBalanceVO) => {
  selectedRow.value = row
  detailDrawerOpen.value = true
  await loadDetail()
}

const retryDetail = async () => {
  await loadDetail()
}

const handleRebuild = async () => {
  if (!queryParams.ledgerId || rebuilding.value) {
    return
  }
  try {
    await ElMessageBox.confirm(
      '确认重建当前账簿科目余额吗？系统将按已过账凭证重新生成余额。',
      '重建余额',
      {
        confirmButtonText: '确认重建',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
  } catch {
    return
  }
  rebuilding.value = true
  try {
    const result: ErpFinanceGeneralLedgerRebuildRespVO =
      await FinanceGeneralLedgerApi.rebuildSubjectBalance({
        ledgerId: queryParams.ledgerId
      })
    message.success(
      `重建成功：重放凭证 ${result.voucherCount || 0} 条，分录 ${result.entryCount || 0} 条，科目 ${result.subjectCount || 0} 个`
    )
    await getList()
    if (detailDrawerOpen.value && selectedRow.value?.subjectCode) {
      await loadDetail()
    }
  } finally {
    rebuilding.value = false
  }
}

onMounted(async () => {
  await loadLedgers()
  await initializeDefaultSelection()
})
</script>

<style scoped>
@import '../shared/readOnlyPage.css';

.finance-general-ledger-page__drawer {
  display: flex;
  min-height: 100%;
  flex-direction: column;
  gap: 16px;
  background: var(--erp-slate-50);
}

.finance-general-ledger-page__drawer-body {
  display: flex;
  min-height: 0;
  flex: 1;
  flex-direction: column;
  gap: 12px;
}

.finance-general-ledger-page__detail-head {
  align-items: center;
  justify-content: space-between;
}
</style>
