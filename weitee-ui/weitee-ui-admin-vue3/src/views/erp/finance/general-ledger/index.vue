<template>
  <div class="finance-shell finance-general-ledger-page finance-shell__stack">
    <ContentWrap class="finance-shell__header-card">
      <div class="finance-shell__page-header">
        <div class="finance-shell__page-header-main">
          <div class="finance-shell__page-title">鎬昏处</div>
          <div class="finance-shell__page-subtitle">
            鎸夎处绨垮拰鏈熼棿鏌ョ湅绉戠洰浣欓锛屽苟绌块€忚嚦鍑瘉鍒嗗綍鏄庣粏
          </div>
          <div class="finance-shell__page-metrics">
            <span class="finance-shell__metric-chip finance-shell__metric-chip--primary">
              璐︾翱 {{ selectedLedgerLabel }}
            </span>
            <span class="finance-shell__metric-chip finance-shell__metric-chip--neutral">
              鏈熼棿 {{ selectedPeriodLabel }}
            </span>
            <span class="finance-shell__metric-chip finance-shell__metric-chip--success">
              缁撴灉 {{ balanceTotal }}
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
            閲嶅缓浣欓
          </el-button>
        </div>
      </div>
    </ContentWrap>

    <ContentWrap class="finance-shell__filter-card">
      <div class="finance-shell__section-head">
        <div class="finance-shell__section-title">绛涢€夋潯浠?</div>
      </div>
      <el-form
        ref="queryFormRef"
        :model="queryParams"
        label-width="76px"
        class="finance-shell__query-form"
        @submit.prevent
      >
        <div class="finance-shell__query-grid finance-shell__query-grid--wide">
          <el-form-item label="璐︾翱" prop="ledgerId">
            <el-select
              v-model="queryParams.ledgerId"
              placeholder="璇烽€夋嫨璐︾翱"
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
          <el-form-item label="鏈熼棿" prop="periodId">
            <el-select
              v-model="queryParams.periodId"
              placeholder="璇烽€夋嫨鏈熼棿"
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
          <el-form-item label="绉戠洰缂栫爜" prop="subjectCode">
            <el-input
              v-model="queryParams.subjectCode"
              placeholder="璇疯緭鍏ョ鐩紪鐮?"
              clearable
              class="!w-full"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="绉戠洰鍚嶇О" prop="subjectName">
            <el-input
              v-model="queryParams.subjectName"
              placeholder="璇疯緭鍏ョ鐩悕绉?"
              clearable
              class="!w-full"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
        </div>
        <div class="finance-shell__query-actions">
          <el-button type="primary" :loading="loadingList" :disabled="!canQuery" @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" />
            鏌ヨ
          </el-button>
          <el-button :disabled="!canReset" @click="resetQuery">
            <Icon icon="ep:refresh-left" class="mr-5px" />
            閲嶇疆
          </el-button>
        </div>
      </el-form>
    </ContentWrap>

    <ContentWrap class="finance-shell__table-card">
      <div class="finance-shell__toolbar">
        <div class="finance-shell__table-toolbar-main">
          <div class="finance-shell__section-title">绉戠洰浣欓</div>
          <div class="finance-shell__toolbar-count">
            褰撳墠鍏?<strong>{{ balanceTotal }}</strong> 鏉?          </div>
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
          <el-button link type="primary" :disabled="loadingList" @click="getList">閲嶆柊鍔犺浇</el-button>
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
                  绉戠洰
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
            <el-table-column label="鏈熷垵鍊熸柟" min-width="120" align="right">
              <template #default="{ row }">
                <span class="finance-shell__amount finance-shell__mono">
                  {{ formatAmount(row.openingDebitAmount) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="鏈熷垵璐锋柟" min-width="120" align="right">
              <template #default="{ row }">
                <span class="finance-shell__amount finance-shell__mono">
                  {{ formatAmount(row.openingCreditAmount) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="鏈湡鍊熸柟" min-width="120" align="right">
              <template #default="{ row }">
                <span class="finance-shell__amount finance-shell__mono">
                  {{ formatAmount(row.currentDebitAmount) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="鏈湡璐锋柟" min-width="120" align="right">
              <template #default="{ row }">
                <span class="finance-shell__amount finance-shell__mono">
                  {{ formatAmount(row.currentCreditAmount) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="鏈熸湯鍊熸柟" min-width="120" align="right">
              <template #default="{ row }">
                <span class="finance-shell__amount finance-shell__mono">
                  {{ formatAmount(row.endingDebitAmount) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="鏈熸湯璐锋柟" min-width="120" align="right">
              <template #default="{ row }">
                <span class="finance-shell__amount finance-shell__mono">
                  {{ formatAmount(row.endingCreditAmount) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="鎿嶄綔" fixed="right" align="center" width="120">
              <template #default="{ row }">
                <div class="finance-shell__row-actions">
                  <el-button
                    link
                    type="primary"
                    v-hasPermi="['erp:finance-voucher:query', 'erp:finance-report:query']"
                    :disabled="!canOpenDetail(row)"
                    @click="openDetailDrawer(row)"
                  >
                    鏌ョ湅鏄庣粏
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <el-empty v-else description="鏆傛棤绉戠洰浣欓鏁版嵁" />

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
            <div class="finance-shell__context-title">{{ detailData.subjectName || '鎬昏处鏄庣粏' }}</div>
            <div class="finance-shell__context-subtitle">
              {{ detailData.subjectCode || '-' }} 路 {{ detailData.ledgerName || '-' }} 路
              {{ detailData.periodCode || '-' }}
            </div>
          </div>
          <div class="finance-shell__context-meta">
            <div class="finance-shell__context-meta-item">
              <span>鏈熷垵鍊熸柟</span>
              <span class="finance-shell__mono">{{ formatAmount(detailData.openingDebitAmount) }}</span>
            </div>
            <div class="finance-shell__context-meta-item">
              <span>鏈熷垵璐锋柟</span>
              <span class="finance-shell__mono">{{ formatAmount(detailData.openingCreditAmount) }}</span>
            </div>
            <div class="finance-shell__context-meta-item">
              <span>鏈湡鍊熸柟</span>
              <span class="finance-shell__mono">{{ formatAmount(detailData.totalDebitAmount) }}</span>
            </div>
            <div class="finance-shell__context-meta-item">
              <span>鏈湡璐锋柟</span>
              <span class="finance-shell__mono">{{ formatAmount(detailData.totalCreditAmount) }}</span>
            </div>
            <div class="finance-shell__context-meta-item">
              <span>鏈熸湯鍊熸柟</span>
              <span class="finance-shell__mono">{{ formatAmount(detailData.endingDebitAmount) }}</span>
            </div>
            <div class="finance-shell__context-meta-item">
              <span>鏈熸湯璐锋柟</span>
              <span class="finance-shell__mono">{{ formatAmount(detailData.endingCreditAmount) }}</span>
            </div>
          </div>
        </div>

        <div class="finance-general-ledger-page__drawer-body" v-loading="loadingDetail">
          <el-result
            v-if="detailErrorMessage"
            icon="error"
            title="鎬昏处鏄庣粏鍔犺浇澶辫触"
            :sub-title="detailErrorMessage"
          >
            <template #extra>
              <el-button type="primary" :disabled="loadingDetail || !selectedRow" @click="retryDetail">
                閲嶈瘯
              </el-button>
            </template>
          </el-result>

          <template v-else>
            <div class="finance-shell__section-head finance-general-ledger-page__detail-head">
              <div class="finance-shell__section-title">鍑瘉鏄庣粏</div>
              <div class="finance-shell__toolbar-count">
                褰撳墠鍏?<strong>{{ detailData?.items?.length || 0 }}</strong> 鏉?              </div>
            </div>
            <div v-if="detailData?.items?.length" class="finance-shell__table-wrap">
              <el-table
                :data="detailData.items"
                stripe
                class="finance-shell__table finance-shell__table--dense"
                :show-overflow-tooltip="false"
              >
                <el-table-column label="鍑瘉鍙?" prop="voucherNo" min-width="150" />
                <el-table-column label="鍑瘉鏃堕棿" prop="voucherTime" min-width="170">
                  <template #default="{ row }">{{ formatDateTimeValue(row.voucherTime) }}</template>
                </el-table-column>
                <el-table-column label="涓氬姟绫诲瀷" prop="bizTypeName" min-width="120" />
                <el-table-column label="涓氬姟鍗曞彿" prop="bizNo" min-width="160" />
                <el-table-column label="鎽樿" prop="summary" min-width="220" />
                <el-table-column label="鍊熸柟閲戦" min-width="120" align="right">
                  <template #default="{ row }">
                    <span class="finance-shell__amount finance-shell__mono">
                      {{ formatAmount(row.debitAmount) }}
                    </span>
                  </template>
                </el-table-column>
                <el-table-column label="璐锋柟閲戦" min-width="120" align="right">
                  <template #default="{ row }">
                    <span class="finance-shell__amount finance-shell__mono">
                      {{ formatAmount(row.creditAmount) }}
                    </span>
                  </template>
                </el-table-column>
                <el-table-column label="鍑瘉鐘舵€?" prop="voucherStatusName" min-width="120" />
              </el-table>
            </div>
            <el-empty v-else description="鏆傛棤鎬昏处鏄庣粏鏁版嵁" />
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
  () => ledgerOptions.value.find((item) => item.id === queryParams.ledgerId)?.name || '鏈€夋嫨'
)
const selectedPeriodLabel = computed(
  () => periodOptions.value.find((item) => item.id === queryParams.periodId)?.periodCode || '鏈€夋嫨'
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
      listErrorMessage.value = '绉戠洰浣欓鍔犺浇澶辫触锛岃绋嶅悗閲嶈瘯'
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
    detailErrorMessage.value = '璇锋鏌ョ綉缁滄垨绋嶅悗閲嶈瘯'
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
      '閲嶅缓浣欓',
      {
        confirmButtonText: '纭閲嶅缓',
        cancelButtonText: '鍙栨秷',
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
  background: #f8fafc;
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
