<template>
  <div class="finance-readonly-page">
    <ContentWrap>
      <el-form ref="queryFormRef" :model="reportQuery" label-width="88px" class="finance-readonly-page__query-form">
        <div class="finance-readonly-page__query-grid">
          <el-form-item label="账簿">
            <el-select
              v-model="reportQuery.ledgerId"
              placeholder="请选择账簿"
              clearable
              filterable
              :loading="ledgerLoading"
              class="!w-full"
              @change="handleLedgerChange"
            >
              <el-option
                v-for="item in ledgerOptions"
                :key="item.id"
                :label="displayLedgerName(item.name)"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="期间">
            <el-select
              v-model="reportQuery.periodId"
              placeholder="请选择期间"
              clearable
              filterable
              :loading="periodLoading"
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
          <el-form-item label="科目编码">
            <el-input
              v-model="reportQuery.subjectCode"
              placeholder="请输入科目编码"
              clearable
              class="!w-full"
              @keyup.enter="refreshReport"
            />
          </el-form-item>
          <el-form-item label="科目名称">
            <el-input
              v-model="reportQuery.subjectName"
              placeholder="请输入科目名称"
              clearable
              class="!w-full"
              @keyup.enter="refreshReport"
            />
          </el-form-item>
        </div>
        <div class="finance-readonly-page__query-actions">
          <el-button :disabled="currentTabLoading" @click="resetQuery">
            <Icon icon="ep:refresh-left" class="mr-5px" />
            重置
          </el-button>
          <el-button type="primary" :loading="currentTabLoading" :disabled="!canRefreshReport" @click="refreshReport">
            <Icon icon="ep:refresh" class="mr-5px" />
            刷新报表
          </el-button>
        </div>
      </el-form>
    </ContentWrap>

    <ContentWrap>
      <div class="finance-readonly-page__toolbar">
        <div class="finance-readonly-page__title">财务报表</div>
      </div>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="试算平衡" name="trial">
          <el-result v-if="reportErrorMessage && activeTab === 'trial'" icon="error" title="报表加载失败">
            <template #extra>
              <el-button type="primary" @click="refreshReport">重试</el-button>
            </template>
          </el-result>
          <template v-else>
            <div v-if="trialBalance" class="finance-readonly-page__metric-grid">
              <div class="finance-readonly-page__metric-card">
                <div class="finance-readonly-page__metric-label">本期借方合计</div>
                <div class="finance-readonly-page__metric-value">
                  {{ formatAmount(trialBalance.totalCurrentDebitAmount) }}
                </div>
              </div>
              <div class="finance-readonly-page__metric-card">
                <div class="finance-readonly-page__metric-label">本期贷方合计</div>
                <div class="finance-readonly-page__metric-value">
                  {{ formatAmount(trialBalance.totalCurrentCreditAmount) }}
                </div>
              </div>
              <div class="finance-readonly-page__metric-card">
                <div class="finance-readonly-page__metric-label">期末借方合计</div>
                <div class="finance-readonly-page__metric-value">
                  {{ formatAmount(trialBalance.totalEndingDebitAmount) }}
                </div>
              </div>
              <div class="finance-readonly-page__metric-card">
                <div class="finance-readonly-page__metric-label">期末贷方合计</div>
                <div class="finance-readonly-page__metric-value">
                  {{ formatAmount(trialBalance.totalEndingCreditAmount) }}
                </div>
              </div>
            </div>
            <div v-if="trialBalance" class="finance-readonly-page__toolbar">
              <el-tag :type="trialBalance.currentBalanced ? 'success' : 'danger'" effect="light" round>
                {{ trialBalance.currentBalanced ? '本期平衡' : '本期不平衡' }}
              </el-tag>
              <el-tag :type="trialBalance.endingBalanced ? 'success' : 'danger'" effect="light" round>
                {{ trialBalance.endingBalanced ? '期末平衡' : '期末不平衡' }}
              </el-tag>
            </div>
            <div v-if="statementLoading || trialBalance?.items?.length" class="finance-readonly-page__table-wrap">
              <el-table
                v-loading="statementLoading"
                :data="trialBalance?.items || []"
                stripe
                show-overflow-tooltip
              >
                <el-table-column label="科目" min-width="220">
                  <template #default="{ row }">
                    <div class="finance-readonly-page__primary-cell">
                      <span class="finance-readonly-page__primary-text">{{ row.subjectName || '-' }}</span>
                      <span class="finance-readonly-page__muted-text font-mono">{{ row.subjectCode || '-' }}</span>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="期初借方" align="right" min-width="130">
                  <template #default="{ row }">{{ formatAmount(row.openingDebitAmount) }}</template>
                </el-table-column>
                <el-table-column label="期初贷方" align="right" min-width="130">
                  <template #default="{ row }">{{ formatAmount(row.openingCreditAmount) }}</template>
                </el-table-column>
                <el-table-column label="本期借方" align="right" min-width="130">
                  <template #default="{ row }">{{ formatAmount(row.currentDebitAmount) }}</template>
                </el-table-column>
                <el-table-column label="本期贷方" align="right" min-width="130">
                  <template #default="{ row }">{{ formatAmount(row.currentCreditAmount) }}</template>
                </el-table-column>
                <el-table-column label="期末借方" align="right" min-width="130">
                  <template #default="{ row }">{{ formatAmount(row.endingDebitAmount) }}</template>
                </el-table-column>
                <el-table-column label="期末贷方" align="right" min-width="130">
                  <template #default="{ row }">{{ formatAmount(row.endingCreditAmount) }}</template>
                </el-table-column>
              </el-table>
            </div>
            <el-empty v-else description="暂无数据" />
          </template>
        </el-tab-pane>

        <el-tab-pane label="资产负债表" name="balance">
          <StatementPane
            :loading="statementLoading"
            :statement="balanceSheet"
            :error="activeTab === 'balance' ? reportErrorMessage : ''"
            @retry="refreshReport"
          />
        </el-tab-pane>

        <el-tab-pane label="利润表" name="income">
          <StatementPane
            :loading="statementLoading"
            :statement="incomeStatement"
            :error="activeTab === 'income' ? reportErrorMessage : ''"
            @retry="refreshReport"
          />
        </el-tab-pane>

        <el-tab-pane label="现金流量表" name="cash">
          <StatementPane
            :loading="statementLoading"
            :statement="cashFlowStatement"
            :error="activeTab === 'cash' ? reportErrorMessage : ''"
            @retry="refreshReport"
          />
        </el-tab-pane>

        <el-tab-pane label="科目余额" name="balanceList">
          <el-result v-if="reportErrorMessage && activeTab === 'balanceList'" icon="error" title="科目余额加载失败">
            <template #extra>
              <el-button type="primary" @click="loadSubjectBalance">重试</el-button>
            </template>
          </el-result>
          <template v-else>
            <div v-if="subjectBalanceLoading || subjectBalanceList.length" class="finance-readonly-page__table-wrap">
              <el-table
                v-loading="subjectBalanceLoading"
                :data="subjectBalanceList"
                row-key="id"
                stripe
                show-overflow-tooltip
              >
                <el-table-column label="科目" min-width="220">
                  <template #default="{ row }">
                    <div class="finance-readonly-page__primary-cell">
                      <span class="finance-readonly-page__primary-text">{{ row.subjectName || '-' }}</span>
                      <span class="finance-readonly-page__muted-text font-mono">{{ row.subjectCode || '-' }}</span>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="本期借方" align="right" min-width="130">
                  <template #default="{ row }">{{ formatAmount(row.currentDebitAmount) }}</template>
                </el-table-column>
                <el-table-column label="本期贷方" align="right" min-width="130">
                  <template #default="{ row }">{{ formatAmount(row.currentCreditAmount) }}</template>
                </el-table-column>
                <el-table-column label="期末借方" align="right" min-width="130">
                  <template #default="{ row }">{{ formatAmount(row.endingDebitAmount) }}</template>
                </el-table-column>
                <el-table-column label="期末贷方" align="right" min-width="130">
                  <template #default="{ row }">{{ formatAmount(row.endingCreditAmount) }}</template>
                </el-table-column>
                <el-table-column label="操作" fixed="right" align="center" width="120">
                  <template #default="{ row }">
                    <el-button link type="primary" :disabled="subjectBalanceLoading || detailLoading" @click="openDetailDrawer(row)">
                      查看明细
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
            <el-empty v-else description="暂无数据" />
            <Pagination
              v-if="subjectBalanceTotal > 0"
              v-model:limit="balanceQuery.pageSize"
              v-model:page="balanceQuery.pageNo"
              :total="subjectBalanceTotal"
              @pagination="loadSubjectBalance"
            />
          </template>
        </el-tab-pane>
      </el-tabs>
    </ContentWrap>

    <el-drawer
      v-model="detailDrawerOpen"
      title="查看明细"
      :size="drawerSize"
      destroy-on-close
      @closed="clearDetailDrawer"
    >
      <el-result v-if="detailErrorMessage" icon="error" title="明细加载失败">
        <template #extra>
          <el-button type="primary" @click="retryDetail">重试</el-button>
        </template>
      </el-result>
      <template v-else>
        <div v-if="detailData" class="finance-readonly-page__context-card">
          <div class="finance-readonly-page__context-main">
            <div class="finance-readonly-page__context-title">{{ detailData.subjectName || '-' }}</div>
            <div class="finance-readonly-page__context-subtitle font-mono">{{ detailData.subjectCode || '-' }}</div>
          </div>
          <div class="finance-readonly-page__context-meta">
            <div class="finance-readonly-page__context-meta-item">
              <span>账簿</span>
              <span>{{ displayLedgerName(detailData.ledgerName) }}</span>
            </div>
            <div class="finance-readonly-page__context-meta-item">
              <span>期间</span>
              <span>{{ detailData.periodCode || '-' }}</span>
            </div>
          </div>
        </div>
        <div v-if="detailData" class="finance-readonly-page__metric-grid">
          <div class="finance-readonly-page__metric-card">
            <div class="finance-readonly-page__metric-label">借方发生额</div>
            <div class="finance-readonly-page__metric-value">
              {{ formatAmount(detailData.totalDebitAmount) }}
            </div>
          </div>
          <div class="finance-readonly-page__metric-card">
            <div class="finance-readonly-page__metric-label">贷方发生额</div>
            <div class="finance-readonly-page__metric-value">
              {{ formatAmount(detailData.totalCreditAmount) }}
            </div>
          </div>
          <div class="finance-readonly-page__metric-card">
            <div class="finance-readonly-page__metric-label">期末借方</div>
            <div class="finance-readonly-page__metric-value">
              {{ formatAmount(detailData.endingDebitAmount) }}
            </div>
          </div>
          <div class="finance-readonly-page__metric-card">
            <div class="finance-readonly-page__metric-label">期末贷方</div>
            <div class="finance-readonly-page__metric-value">
              {{ formatAmount(detailData.endingCreditAmount) }}
            </div>
          </div>
        </div>
        <el-table
          v-loading="detailLoading"
          :data="detailData?.items || []"
          stripe
          show-overflow-tooltip
        >
          <el-table-column label="凭证" min-width="220">
            <template #default="{ row }">
              <div class="finance-readonly-page__primary-cell">
                <span class="finance-readonly-page__primary-text">{{ row.voucherNo || '-' }}</span>
                <span class="finance-readonly-page__muted-text">{{ formatDateTimeValue(row.voucherTime) }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="摘要" prop="summary" min-width="180" />
          <el-table-column label="借方" align="right" min-width="120">
            <template #default="{ row }">{{ formatAmount(row.debitAmount) }}</template>
          </el-table-column>
          <el-table-column label="贷方" align="right" min-width="120">
            <template #default="{ row }">{{ formatAmount(row.creditAmount) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="110">
            <template #default="{ row }">{{ row.voucherStatusName || '-' }}</template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!detailLoading && !detailData?.items?.length" description="暂无数据" />
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { useWindowSize } from '@vueuse/core'
import { ElMessage } from 'element-plus'
import {
  ErpFinanceStatementVO,
  ErpFinanceTrialBalanceVO,
  FinanceReportApi
} from '@/api/erp/finance/report'
import {
  ErpFinanceGeneralLedgerDetailVO,
  ErpFinanceSubjectBalanceVO,
  FinanceGeneralLedgerApi
} from '@/api/erp/finance/general-ledger'
import { ErpFinanceLedgerVO, FinanceLedgerApi } from '@/api/erp/finance/ledger'
import { ErpFinancePeriodVO, FinancePeriodApi } from '@/api/erp/finance/period'
import { formatAmount, formatDateTimeValue } from '@/views/erp/finance/shared/accounting'
import StatementPane from './StatementPane.vue'
import { displayLedgerName } from '@/utils/financeDisplay'

defineOptions({ name: 'ErpFinanceReports' })

type ReportTabName = 'trial' | 'balance' | 'income' | 'cash' | 'balanceList'

const { width } = useWindowSize()
const queryFormRef = ref()
const ledgerLoading = ref(false)
const periodLoading = ref(false)
const statementLoading = ref(false)
const subjectBalanceLoading = ref(false)
const detailLoading = ref(false)
const detailDrawerOpen = ref(false)
const reportErrorMessage = ref('')
const detailErrorMessage = ref('')
const activeTab = ref<ReportTabName>('trial')
const ledgerOptions = ref<ErpFinanceLedgerVO[]>([])
const periodOptions = ref<ErpFinancePeriodVO[]>([])
const trialBalance = ref<ErpFinanceTrialBalanceVO>()
const balanceSheet = ref<ErpFinanceStatementVO>()
const incomeStatement = ref<ErpFinanceStatementVO>()
const cashFlowStatement = ref<ErpFinanceStatementVO>()
const subjectBalanceList = ref<ErpFinanceSubjectBalanceVO[]>([])
const subjectBalanceTotal = ref(0)
const detailData = ref<ErpFinanceGeneralLedgerDetailVO>()
const currentSubjectCode = ref('')

const reportQuery = reactive({
  ledgerId: undefined as number | undefined,
  periodId: undefined as number | undefined,
  subjectCode: undefined as string | undefined,
  subjectName: undefined as string | undefined
})

const balanceQuery = reactive({
  pageNo: 1,
  pageSize: 10
})

const canQueryReport = computed(() => Boolean(reportQuery.ledgerId && reportQuery.periodId))
const currentTabLoading = computed(() =>
  activeTab.value === 'balanceList' ? subjectBalanceLoading.value : statementLoading.value
)
const canRefreshReport = computed(() => canQueryReport.value && !currentTabLoading.value)
const drawerSize = computed(() => {
  if (width.value < 768) {
    return '100%'
  }
  if (width.value < 1200) {
    return '92vw'
  }
  return '860px'
})

const clearReportData = () => {
  trialBalance.value = undefined
  balanceSheet.value = undefined
  incomeStatement.value = undefined
  cashFlowStatement.value = undefined
  subjectBalanceList.value = []
  subjectBalanceTotal.value = 0
  reportErrorMessage.value = ''
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
  if (!reportQuery.ledgerId) {
    periodOptions.value = []
    reportQuery.periodId = undefined
    return
  }
  periodLoading.value = true
  try {
    const data = await FinancePeriodApi.getPeriodPage({
      pageNo: 1,
      pageSize: 100,
      ledgerId: reportQuery.ledgerId
    })
    periodOptions.value = data?.list || []
    if (!periodOptions.value.some((item) => item.id === reportQuery.periodId)) {
      reportQuery.periodId = undefined
    }
  } finally {
    periodLoading.value = false
  }
}

const handleLedgerChange = async () => {
  reportQuery.periodId = undefined
  clearReportData()
  detailDrawerOpen.value = false
  await loadPeriods()
}

const buildReportParams = () => ({
  ledgerId: reportQuery.ledgerId,
  periodId: reportQuery.periodId,
  subjectCode: reportQuery.subjectCode,
  subjectName: reportQuery.subjectName
})

const refreshReport = async () => {
  if (!canQueryReport.value) {
    ElMessage.warning('请选择账簿和期间')
    return
  }
  if (activeTab.value === 'balanceList') {
    balanceQuery.pageNo = 1
    await loadSubjectBalance()
    return
  }
  await loadStatementReport()
}

const loadStatementReport = async () => {
  statementLoading.value = true
  reportErrorMessage.value = ''
  try {
    if (activeTab.value === 'trial') {
      trialBalance.value = await FinanceReportApi.getTrialBalance(buildReportParams())
    } else if (activeTab.value === 'balance') {
      balanceSheet.value = await FinanceReportApi.getBalanceSheet(buildReportParams())
    } else if (activeTab.value === 'income') {
      incomeStatement.value = await FinanceReportApi.getIncomeStatement(buildReportParams())
    } else if (activeTab.value === 'cash') {
      cashFlowStatement.value = await FinanceReportApi.getCashFlowStatement(buildReportParams())
    }
  } catch {
    reportErrorMessage.value = '报表加载失败'
  } finally {
    statementLoading.value = false
  }
}

const loadSubjectBalance = async () => {
  if (!canQueryReport.value) {
    ElMessage.warning('请选择账簿和期间')
    return
  }
  subjectBalanceLoading.value = true
  reportErrorMessage.value = ''
  try {
    const data = await FinanceGeneralLedgerApi.getSubjectBalancePage({
      pageNo: balanceQuery.pageNo,
      pageSize: balanceQuery.pageSize,
      ledgerId: reportQuery.ledgerId,
      periodId: reportQuery.periodId,
      subjectCode: reportQuery.subjectCode,
      subjectName: reportQuery.subjectName
    })
    subjectBalanceList.value = data?.list || []
    subjectBalanceTotal.value = data?.total || 0
  } catch {
    if (!subjectBalanceList.value.length) {
      reportErrorMessage.value = '科目余额加载失败'
    }
  } finally {
    subjectBalanceLoading.value = false
  }
}

const handleTabChange = async () => {
  if (canQueryReport.value) {
    await refreshReport()
  }
}

const resetQuery = async () => {
  reportQuery.ledgerId = undefined
  reportQuery.periodId = undefined
  reportQuery.subjectCode = undefined
  reportQuery.subjectName = undefined
  balanceQuery.pageNo = 1
  clearReportData()
  detailData.value = undefined
  detailErrorMessage.value = ''
  currentSubjectCode.value = ''
  detailDrawerOpen.value = false
  activeTab.value = 'trial'
  periodOptions.value = []
}

const openDetailDrawer = async (row: ErpFinanceSubjectBalanceVO) => {
  if (!row.subjectCode || !reportQuery.ledgerId || !reportQuery.periodId || subjectBalanceLoading.value) {
    return
  }
  currentSubjectCode.value = row.subjectCode
  detailDrawerOpen.value = true
  await loadDetail(row.subjectCode)
}

const loadDetail = async (subjectCode: string) => {
  detailLoading.value = true
  detailErrorMessage.value = ''
  try {
    detailData.value = await FinanceGeneralLedgerApi.getGeneralLedgerDetail({
      ledgerId: reportQuery.ledgerId,
      periodId: reportQuery.periodId,
      subjectCode
    })
  } catch {
    detailErrorMessage.value = '明细加载失败'
  } finally {
    detailLoading.value = false
  }
}

const retryDetail = async () => {
  if (currentSubjectCode.value) {
    await loadDetail(currentSubjectCode.value)
  }
}

const clearDetailDrawer = () => {
  detailData.value = undefined
  detailErrorMessage.value = ''
  currentSubjectCode.value = ''
}

onMounted(async () => {
  await loadLedgers()
})
</script>

<style scoped>
@import '../shared/readOnlyPage.css';
</style>
