<template>
  <div class="approval-statistics-page">
    <ContentWrap
      v-loading="pageLoading"
      class="approval-statistics-page__header-card"
      :body-style="{ padding: '24px' }"
    >
      <div class="approval-statistics-page__header">
        <div class="approval-statistics-page__header-main">
          <div class="approval-statistics-page__title">审批统计看板</div>
        </div>
        <el-button
          type="primary"
          plain
          :loading="summaryRefreshing"
          :disabled="!canRefresh"
          @click="handleRefresh"
        >
          <Icon icon="ep:refresh" class="mr-5px" /> 刷新
        </el-button>
      </div>
    </ContentWrap>

    <el-alert
      v-if="pageError"
      type="error"
      :closable="false"
      show-icon
      class="approval-statistics-page__alert"
      :title="pageError"
    >
      <template #default>
        <el-button link type="primary" :disabled="!canRefresh" @click="handleRefresh">
          重新加载
        </el-button>
      </template>
    </el-alert>

    <ContentWrap
      v-loading="pageLoading"
      class="approval-statistics-page__section-card"
      :body-style="{ padding: '24px' }"
    >
      <div class="approval-statistics-page__section-title">历史结果统计</div>
      <div class="approval-statistics-page__stat-grid">
        <article
          v-for="card in historySummaryCards"
          :key="card.key"
          class="approval-statistics-page__stat-card"
          :class="card.cardClass"
        >
          <div class="approval-statistics-page__stat-card-main">
            <div class="approval-statistics-page__stat-card-label">{{ card.label }}</div>
            <div class="approval-statistics-page__stat-card-value">{{ formatCount(card.value) }}</div>
          </div>
          <div class="approval-statistics-page__stat-card-icon" :class="card.iconClass">
            <Icon :icon="card.icon" />
          </div>
        </article>
      </div>
    </ContentWrap>

    <ContentWrap
      v-loading="pageLoading"
      class="approval-statistics-page__section-card"
      :body-style="{ padding: '24px' }"
    >
      <div class="approval-statistics-page__section-title">当前有效统计</div>
      <div class="approval-statistics-page__stat-grid">
        <article
          v-for="card in currentSummaryCards"
          :key="card.key"
          class="approval-statistics-page__stat-card"
          :class="card.cardClass"
        >
          <div class="approval-statistics-page__stat-card-main">
            <div class="approval-statistics-page__stat-card-label">{{ card.label }}</div>
            <div class="approval-statistics-page__stat-card-value">{{ formatCount(card.value) }}</div>
          </div>
          <div class="approval-statistics-page__stat-card-icon" :class="card.iconClass">
            <Icon :icon="card.icon" />
          </div>
        </article>
      </div>
    </ContentWrap>

    <ContentWrap
      v-loading="tableLoading"
      class="approval-statistics-page__section-card"
      :body-style="{ padding: '24px' }"
    >
      <div class="approval-statistics-page__table-header">
        <div class="approval-statistics-page__section-title approval-statistics-page__section-title--compact">
          当前发起人分布
        </div>
        <div class="approval-statistics-page__table-count">
          共 {{ formatCount(currentUserList.length) }} 条
        </div>
      </div>

      <div class="approval-statistics-page__table-shell">
        <el-table
          v-loading="tableLoading"
          :data="currentUserList"
          stripe
          row-key="userId"
          class="approval-statistics-table"
        >
          <template #empty>
            <div
              v-if="showErrorState"
              class="approval-statistics-page__empty approval-statistics-page__empty--error"
            >
              <div class="approval-statistics-page__empty-icon">
                <Icon icon="ep:warning-filled" />
              </div>
              <div class="approval-statistics-page__empty-title">统计数据加载失败，请重试</div>
              <el-button type="primary" plain :disabled="!canRefresh" @click="handleRefresh">
                重新加载
              </el-button>
            </div>
            <div v-else class="approval-statistics-page__empty">
              <div class="approval-statistics-page__empty-icon">
                <Icon icon="ep:data-analysis" />
              </div>
              <div class="approval-statistics-page__empty-title">暂无统计数据</div>
            </div>
          </template>

          <el-table-column label="发起人" min-width="220">
            <template #default="{ row }">
              <div class="approval-statistics-page__user-cell">
                <div class="approval-statistics-page__user-name">
                  {{ resolveUserName(row) }}
                </div>
                <div class="approval-statistics-page__user-id">
                  用户ID {{ formatCount(row.userId) }}
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="用户ID" min-width="120" align="right">
            <template #default="{ row }">
              <span class="approval-statistics-page__number-cell">
                {{ formatCount(row.userId) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="当前有效总数" min-width="140" align="right">
            <template #default="{ row }">
              <span class="approval-statistics-page__number-cell">
                {{ formatCount(row.totalCount) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="处理中" min-width="120" align="right">
            <template #default="{ row }">
              <span class="approval-statistics-page__number-cell approval-statistics-page__number-cell--primary">
                {{ formatCount(row.processingCount) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="已通过" min-width="120" align="right">
            <template #default="{ row }">
              <span class="approval-statistics-page__number-cell approval-statistics-page__number-cell--success">
                {{ formatCount(row.approvedCount) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="已驳回" min-width="120" align="right">
            <template #default="{ row }">
              <span class="approval-statistics-page__number-cell approval-statistics-page__number-cell--danger">
                {{ formatCount(row.rejectedCount) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="已撤回" min-width="120" align="right">
            <template #default="{ row }">
              <span class="approval-statistics-page__number-cell approval-statistics-page__number-cell--slate">
                {{ formatCount(row.cancelledCount) }}
              </span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </ContentWrap>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import {
  getApprovalStatisticsSummary,
  getCurrentApprovalStatisticsSummary,
  getCurrentApprovalStatisticsUserList,
  type ApprovalStatisticsSummaryVO,
  type ApprovalStatisticsUserVO
} from '@/api/bpm/approvalStatistics'

defineOptions({ name: 'BpmApprovalStatistics' })

type StatisticCardTone = 'blue' | 'teal' | 'green' | 'amber' | 'rose' | 'slate'

type StatisticCard = {
  key: string
  label: string
  value: number
  icon: string
  cardClass: string
  iconClass: string
}

const message = useMessage()

const buildEmptySummary = (): ApprovalStatisticsSummaryVO => ({
  totalCount: 0,
  processingCount: 0,
  approvedCount: 0,
  rejectedCount: 0,
  cancelledCount: 0
})

const pageLoading = ref(true)
const summaryRefreshing = ref(false)
const tableLoading = ref(false)
const pageError = ref('')
const historySummary = ref<ApprovalStatisticsSummaryVO>(buildEmptySummary())
const currentSummary = ref<ApprovalStatisticsSummaryVO>(buildEmptySummary())
const currentUserList = ref<ApprovalStatisticsUserVO[]>([])

const canRefresh = computed(() => !pageLoading.value && !summaryRefreshing.value)
const hasHistoryData = computed(() =>
  Object.values(historySummary.value).some((value) => Number(value || 0) > 0)
)
const hasCurrentData = computed(() =>
  Object.values(currentSummary.value).some((value) => Number(value || 0) > 0)
)
const hasUserRows = computed(() => currentUserList.value.length > 0)
const showErrorState = computed(
  () => !!pageError.value && !hasHistoryData.value && !hasCurrentData.value && !hasUserRows.value
)

const formatCount = (value?: number | string | null) => Number(value || 0).toLocaleString('zh-CN')

const buildCardClass = (tone: StatisticCardTone) =>
  `approval-statistics-page__stat-card--${tone}`

const buildIconClass = (tone: StatisticCardTone) =>
  `approval-statistics-page__stat-card-icon--${tone}`

const buildSummaryCards = (summary: ApprovalStatisticsSummaryVO): StatisticCard[] => [
  {
    key: 'total',
    label: '总数',
    value: Number(summary.totalCount || 0),
    icon: 'ep:document',
    cardClass: buildCardClass('blue'),
    iconClass: buildIconClass('blue')
  },
  {
    key: 'processing',
    label: '处理中',
    value: Number(summary.processingCount || 0),
    icon: 'ep:loading',
    cardClass: buildCardClass('teal'),
    iconClass: buildIconClass('teal')
  },
  {
    key: 'approved',
    label: '已通过',
    value: Number(summary.approvedCount || 0),
    icon: 'ep:circle-check',
    cardClass: buildCardClass('green'),
    iconClass: buildIconClass('green')
  },
  {
    key: 'rejected',
    label: '已驳回',
    value: Number(summary.rejectedCount || 0),
    icon: 'ep:close-bold',
    cardClass: buildCardClass('rose'),
    iconClass: buildIconClass('rose')
  },
  {
    key: 'cancelled',
    label: '已撤回',
    value: Number(summary.cancelledCount || 0),
    icon: 'ep:refresh-left',
    cardClass: buildCardClass('slate'),
    iconClass: buildIconClass('slate')
  }
]

const historySummaryCards = computed(() => buildSummaryCards(historySummary.value))
const currentSummaryCards = computed(() => buildSummaryCards(currentSummary.value))

const resolveUserName = (row: ApprovalStatisticsUserVO) => row.userName?.trim() || '未命名用户'

const loadDashboard = async (mode: 'initial' | 'refresh' = 'initial') => {
  if (mode === 'initial') {
    pageLoading.value = true
  } else {
    summaryRefreshing.value = true
  }
  tableLoading.value = true
  pageError.value = ''
  try {
    const [historyData, currentData, userData] = await Promise.all([
      getApprovalStatisticsSummary(),
      getCurrentApprovalStatisticsSummary(),
      getCurrentApprovalStatisticsUserList()
    ])
    historySummary.value = historyData || buildEmptySummary()
    currentSummary.value = currentData || buildEmptySummary()
    currentUserList.value = Array.isArray(userData) ? userData : []
  } catch (error: any) {
    pageError.value = error?.message || '统计数据加载失败，请重试'
    message.error(pageError.value)
  } finally {
    pageLoading.value = false
    summaryRefreshing.value = false
    tableLoading.value = false
  }
}

const handleRefresh = async () => {
  if (!canRefresh.value) {
    return
  }
  await loadDashboard('refresh')
}

onMounted(async () => {
  await loadDashboard()
})
</script>

<style scoped>
.approval-statistics-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.approval-statistics-page__header,
.approval-statistics-page__table-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.approval-statistics-page__header-main {
  min-width: 0;
}

.approval-statistics-page__title {
  color: var(--erp-slate-900);
  font-size: 22px;
  line-height: 30px;
  font-weight: 600;
}

.approval-statistics-page__alert {
  margin-bottom: 0;
}

.approval-statistics-page__section-title {
  margin-bottom: 16px;
  color: var(--erp-slate-900);
  font-size: 16px;
  font-weight: 600;
}

.approval-statistics-page__section-title--compact {
  margin-bottom: 0;
}

.approval-statistics-page__table-count {
  color: var(--erp-slate-500);
  font-size: 13px;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono',
    'Courier New', monospace;
}

.approval-statistics-page__stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 14px;
}

.approval-statistics-page__stat-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  min-height: 100px;
  padding: 18px;
  border: 1px solid var(--erp-slate-200);
  border-radius: 14px;
  background: var(--erp-surface-white);
  box-shadow: var(--erp-shadow-sm);
  transition: transform var(--transition-time-02) ease, box-shadow var(--transition-time-02) ease;
}

.approval-statistics-page__stat-card:hover {
  transform: translateY(-1px);
  box-shadow: var(--erp-shadow-md);
}

.approval-statistics-page__stat-card--blue {
  background: var(--erp-stat-gradient-blue);
  border-color: var(--erp-stat-border-blue);
}

.approval-statistics-page__stat-card--teal {
  background: var(--erp-stat-gradient-teal);
  border-color: var(--erp-stat-border-teal);
}

.approval-statistics-page__stat-card--green {
  background: var(--erp-stat-gradient-green);
  border-color: var(--erp-stat-border-green);
}

.approval-statistics-page__stat-card--rose {
  background: var(--erp-stat-gradient-rose);
  border-color: var(--erp-stat-border-rose);
}

.approval-statistics-page__stat-card--slate {
  background: var(--erp-stat-gradient-slate);
  border-color: var(--erp-stat-border-slate);
}

.approval-statistics-page__stat-card-main {
  min-width: 0;
}

.approval-statistics-page__stat-card-label {
  color: var(--erp-slate-600);
  font-size: 12px;
  line-height: 20px;
}

.approval-statistics-page__stat-card-value {
  margin-top: 8px;
  color: var(--erp-slate-900);
  font-size: 28px;
  line-height: 34px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono',
    'Courier New', monospace;
}

.approval-statistics-page__stat-card-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 12px;
  font-size: 22px;
  flex-shrink: 0;
}

.approval-statistics-page__stat-card-icon--blue {
  color: var(--erp-primary-600);
  background: var(--erp-primary-50);
}

.approval-statistics-page__stat-card-icon--teal {
  color: var(--erp-teal-600);
  background: var(--erp-teal-50);
}

.approval-statistics-page__stat-card-icon--green {
  color: var(--erp-success-600);
  background: var(--erp-success-50);
}

.approval-statistics-page__stat-card-icon--rose {
  color: var(--erp-danger-600);
  background: var(--erp-danger-50);
}

.approval-statistics-page__stat-card-icon--slate {
  color: var(--erp-disabled-600);
  background: var(--erp-disabled-50);
}

.approval-statistics-page__table-shell {
  overflow-x: auto;
}

.approval-statistics-table {
  min-width: 920px;
}

.approval-statistics-page__user-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.approval-statistics-page__user-name {
  color: var(--erp-slate-900);
  font-size: 14px;
  font-weight: 700;
}

.approval-statistics-page__user-id {
  color: var(--erp-slate-500);
  font-size: 12px;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono',
    'Courier New', monospace;
}

.approval-statistics-page__number-cell {
  color: var(--erp-slate-900);
  font-weight: 600;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono',
    'Courier New', monospace;
}

.approval-statistics-page__number-cell--primary {
  color: var(--erp-primary-600);
}

.approval-statistics-page__number-cell--success {
  color: var(--erp-success-600);
}

.approval-statistics-page__number-cell--danger {
  color: var(--erp-danger-600);
}

.approval-statistics-page__number-cell--slate {
  color: var(--erp-disabled-600);
}

.approval-statistics-page__empty {
  min-height: 220px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: var(--erp-slate-500);
}

.approval-statistics-page__empty-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  border-radius: 12px;
  color: var(--erp-primary-600);
  background: var(--erp-primary-50);
}

.approval-statistics-page__empty--error .approval-statistics-page__empty-icon {
  color: var(--erp-danger-600);
  background: var(--erp-danger-50);
}

.approval-statistics-page__empty-title {
  color: var(--erp-slate-800);
  font-size: 14px;
  font-weight: 600;
}

@media (max-width: 1279px) {
  .approval-statistics-page__stat-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 767px) {
  .approval-statistics-page__header,
  .approval-statistics-page__table-header {
    align-items: stretch;
    flex-direction: column;
  }

  .approval-statistics-page__stat-grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
