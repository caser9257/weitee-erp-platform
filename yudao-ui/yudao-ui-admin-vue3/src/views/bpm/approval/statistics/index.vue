<template>
  <div class="approval-statistics-page">
    <!-- 页头 -->
    <ContentWrap class="page-header-card">
      <div class="page-header">
        <div class="page-header__main">
          <div class="page-header__title">审批统计看板</div>
          <div class="page-header__desc">实时查看审批数据和用户审批效率</div>
        </div>
        <el-button :loading="loading" @click="loadData">
          <Icon icon="ep:refresh" class="mr-5px" /> 刷新
        </el-button>
      </div>
    </ContentWrap>

    <!-- KPI 卡片区 -->
    <div class="kpi-grid">
      <div v-for="kpi in kpiCards" :key="kpi.key" class="kpi-card" :style="{ '--accent': kpi.color }">
        <div class="kpi-card__accent"></div>
        <div class="kpi-card__content">
          <div class="kpi-card__icon" :style="{ background: kpi.bgColor }">
            <Icon :icon="kpi.icon" :style="{ color: kpi.color }" />
          </div>
          <div class="kpi-card__info">
            <div class="kpi-card__value">{{ formatNumber(kpi.value) }}</div>
            <div class="kpi-card__label">{{ kpi.label }}</div>
          </div>
          <div v-if="kpi.percent" class="kpi-card__percent" :style="{ color: kpi.color }">
            {{ kpi.percent }}
          </div>
        </div>
      </div>
    </div>

    <!-- 状态分布总览条 -->
    <ContentWrap class="status-bar-card">
      <div class="status-bar-label">状态分布</div>
      <div class="status-bar-container">
        <div
          v-for="segment in statusBarSegments"
          :key="segment.key"
          class="status-bar-segment"
          :style="{ width: segment.percent + '%', background: segment.color }"
          :title="`${segment.label}: ${segment.value}`"
        ></div>
      </div>
      <div class="status-bar-legend">
        <div v-for="segment in statusBarSegments" :key="segment.key" class="legend-item">
          <span class="legend-dot" :style="{ background: segment.color }"></span>
          <span class="legend-label">{{ segment.label }}</span>
          <span class="legend-value">{{ segment.value }}</span>
        </div>
      </div>
    </ContentWrap>

    <!-- 用户审批排行表格 -->
    <ContentWrap class="user-table-card" title="用户审批排行">
      <el-table :data="userStats" v-loading="loading" stripe class="user-stats-table">
        <el-table-column label="排名" width="70" align="center">
          <template #default="{ $index }">
            <div v-if="$index < 3" class="rank-badge" :class="`rank-${$index + 1}`">
              {{ $index + 1 }}
            </div>
            <span v-else class="rank-normal">{{ $index + 1 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="审批人" min-width="150">
          <template #default="{ row }">
            <div class="user-cell">
              <div class="user-cell__name">用户 {{ row.userId }}</div>
              <div class="user-cell__id font-mono text-slate-400 text-xs">ID: {{ row.userId }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="总审批数" prop="totalCount" width="100" align="right">
          <template #default="{ row }">
            <span class="font-mono font-semibold">{{ formatNumber(row.totalCount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态分布" min-width="300">
          <template #default="{ row }">
            <div class="distribution-cell">
              <div class="mini-bar">
                <div
                  class="mini-bar__segment"
                  :style="{ width: getPercent(row.processingCount, row.totalCount) + '%', background: 'var(--erp-warning-500)' }"
                ></div>
                <div
                  class="mini-bar__segment"
                  :style="{ width: getPercent(row.approvedCount, row.totalCount) + '%', background: 'var(--erp-success-500)' }"
                ></div>
                <div
                  class="mini-bar__segment"
                  :style="{ width: getPercent(row.rejectedCount, row.totalCount) + '%', background: 'var(--erp-danger-500)' }"
                ></div>
                <div
                  class="mini-bar__segment"
                  :style="{ width: getPercent(row.cancelledCount, row.totalCount) + '%', background: 'var(--erp-slate-400)' }"
                ></div>
              </div>
              <div class="pills">
                <span class="pill pill--processing">{{ row.processingCount }} 审批中</span>
                <span class="pill pill--approved">{{ row.approvedCount }} 通过</span>
                <span class="pill pill--rejected">{{ row.rejectedCount }} 驳回</span>
                <span class="pill pill--cancelled">{{ row.cancelledCount }} 撤回</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="待处理" width="90" align="right">
          <template #default="{ row }">
            <span :class="row.processingCount > 0 ? 'text-amber-600 font-semibold' : 'text-slate-400'">
              {{ row.processingCount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="通过率" width="100" align="right">
          <template #default="{ row }">
            <span :class="getPassRateClass(row)">
              {{ getPassRate(row) }}
            </span>
          </template>
        </el-table-column>
      </el-table>
    </ContentWrap>

    <!-- 快捷操作区 -->
    <div class="quick-actions">
      <div class="quick-action-card" @click="goTo('/bpm/approval/pending')">
        <Icon icon="ep:clock" class="quick-action-icon" style="color: var(--erp-warning-600)" />
        <div class="quick-action-info">
          <div class="quick-action-title">待审批列表</div>
          <div class="quick-action-desc">查看和处理待审批的单据</div>
        </div>
        <Icon icon="ep:arrow-right" class="quick-action-arrow" />
      </div>
      <div class="quick-action-card" @click="goTo('/bpm/approval/approved')">
        <Icon icon="ep:circle-check" class="quick-action-icon" style="color: var(--erp-success-600)" />
        <div class="quick-action-info">
          <div class="quick-action-title">已审批列表</div>
          <div class="quick-action-desc">查看已处理的审批记录</div>
        </div>
        <Icon icon="ep:arrow-right" class="quick-action-arrow" />
      </div>
      <div class="quick-action-card" @click="goTo('/bpm/approval/runtime')">
        <Icon icon="ep:connection" class="quick-action-icon" style="color: var(--erp-primary-600)" />
        <div class="quick-action-info">
          <div class="quick-action-title">流程实例</div>
          <div class="quick-action-desc">查看所有运行中的流程</div>
        </div>
        <Icon icon="ep:arrow-right" class="quick-action-arrow" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import * as StatisticsApi from '@/api/bpm/approval/statistics'
import type { ApprovalStatisticsVO, UserApprovalStatisticsVO } from '@/api/bpm/approval/statistics'

defineOptions({ name: 'ApprovalStatistics' })

const { push } = useRouter()
const loading = ref(false)
const stats = ref<ApprovalStatisticsVO>({
  totalCount: 0,
  processingCount: 0,
  approvedCount: 0,
  rejectedCount: 0,
  cancelledCount: 0
})
const userStats = ref<UserApprovalStatisticsVO[]>([])

// KPI 卡片配置
const kpiCards = computed(() => [
  {
    key: 'total',
    label: '审批总数',
    value: stats.value.totalCount,
    icon: 'ep:document',
    color: 'var(--erp-primary-600)',
    bgColor: 'var(--erp-primary-50)',
    percent: null
  },
  {
    key: 'processing',
    label: '审批中',
    value: stats.value.processingCount,
    icon: 'ep:clock',
    color: 'var(--erp-warning-600)',
    bgColor: 'var(--erp-warning-50)',
    percent: getPercentStr(stats.value.processingCount, stats.value.totalCount)
  },
  {
    key: 'approved',
    label: '已通过',
    value: stats.value.approvedCount,
    icon: 'ep:circle-check',
    color: 'var(--erp-success-600)',
    bgColor: 'var(--erp-success-50)',
    percent: getPercentStr(stats.value.approvedCount, stats.value.totalCount)
  },
  {
    key: 'rejected',
    label: '已拒绝',
    value: stats.value.rejectedCount,
    icon: 'ep:circle-close',
    color: 'var(--erp-danger-600)',
    bgColor: 'var(--erp-danger-50)',
    percent: getPercentStr(stats.value.rejectedCount, stats.value.totalCount)
  },
  {
    key: 'cancelled',
    label: '已撤回',
    value: stats.value.cancelledCount,
    icon: 'ep:back',
    color: 'var(--erp-slate-500)',
    bgColor: 'var(--erp-slate-50)',
    percent: getPercentStr(stats.value.cancelledCount, stats.value.totalCount)
  }
])

// 状态分布条数据
const statusBarSegments = computed(() => {
  const total = stats.value.totalCount || 1
  return [
    { key: 'processing', label: '审批中', value: stats.value.processingCount, color: 'var(--erp-warning-500)', percent: (stats.value.processingCount / total) * 100 },
    { key: 'approved', label: '已通过', value: stats.value.approvedCount, color: 'var(--erp-success-500)', percent: (stats.value.approvedCount / total) * 100 },
    { key: 'rejected', label: '已拒绝', value: stats.value.rejectedCount, color: 'var(--erp-danger-500)', percent: (stats.value.rejectedCount / total) * 100 },
    { key: 'cancelled', label: '已撤回', value: stats.value.cancelledCount, color: 'var(--erp-slate-400)', percent: (stats.value.cancelledCount / total) * 100 }
  ]
})

const formatNumber = (value: number) => {
  return Number(value || 0).toLocaleString('zh-CN')
}

const getPercentStr = (part: number, total: number) => {
  if (!total) return '0%'
  return ((part / total) * 100).toFixed(1) + '%'
}

const getPercent = (part: number, total: number) => {
  if (!total) return 0
  return (part / total) * 100
}

const getPassRate = (row: UserApprovalStatisticsVO) => {
  const reviewed = row.approvedCount + row.rejectedCount
  if (!reviewed) return '--'
  return ((row.approvedCount / reviewed) * 100).toFixed(1) + '%'
}

const getPassRateClass = (row: UserApprovalStatisticsVO) => {
  const reviewed = row.approvedCount + row.rejectedCount
  if (!reviewed) return 'text-slate-400'
  const rate = (row.approvedCount / reviewed) * 100
  if (rate >= 80) return 'text-emerald-600 font-semibold'
  if (rate >= 50) return 'text-amber-600 font-semibold'
  return 'text-red-600 font-semibold'
}

const goTo = (path: string) => {
  push({ path })
}

const loadData = async () => {
  loading.value = true
  try {
    const [summary, userList] = await Promise.all([
      StatisticsApi.getApprovalStatistics(),
      StatisticsApi.getAllUserApprovalStatistics()
    ])
    stats.value = summary
    userStats.value = userList.sort((a, b) => b.totalCount - a.totalCount)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.approval-statistics-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px;
  background: var(--erp-slate-50);
  min-height: 100vh;
}

.page-header-card {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  .page-header__title {
    font-size: 20px;
    font-weight: 800;
    color: var(--erp-slate-900);
  }
  .page-header__desc {
    margin-top: 4px;
    color: var(--erp-slate-500);
    font-size: 12px;
  }
}

// KPI 卡片
.kpi-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
}

@media (max-width: 1200px) {
  .kpi-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .kpi-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

.kpi-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid var(--erp-slate-200);
  overflow: hidden;
  transition: transform 0.2s, box-shadow 0.2s;
  &:hover {
    transform: translateY(-2px);
    box-shadow: var(--erp-shadow-md);
  }
}

.kpi-card__accent {
  height: 3px;
  background: var(--accent);
}

.kpi-card__content {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
}

.kpi-card__icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}

.kpi-card__info {
  flex: 1;
  min-width: 0;
}

.kpi-card__value {
  font-size: 24px;
  font-weight: 800;
  color: var(--erp-slate-900);
  font-variant-numeric: tabular-nums;
  font-family: 'Inter', 'SF Mono', monospace;
}

.kpi-card__label {
  font-size: 12px;
  color: var(--erp-slate-500);
  margin-top: 2px;
}

.kpi-card__percent {
  font-size: 13px;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}

// 状态分布条
.status-bar-card {
  .status-bar-label {
    font-size: 13px;
    font-weight: 600;
    color: var(--erp-slate-600);
    margin-bottom: 12px;
  }
}

.status-bar-container {
  display: flex;
  height: 8px;
  border-radius: 4px;
  overflow: hidden;
  background: var(--erp-slate-100);
  gap: 2px;
}

.status-bar-segment {
  border-radius: 4px;
  transition: width 0.3s ease;
  min-width: 4px;
}

.status-bar-legend {
  display: flex;
  gap: 24px;
  margin-top: 12px;
  flex-wrap: wrap;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.legend-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.legend-label {
  font-size: 12px;
  color: var(--erp-slate-500);
}

.legend-value {
  font-size: 13px;
  font-weight: 600;
  color: var(--erp-slate-700);
  font-variant-numeric: tabular-nums;
}

// 用户表格
.user-stats-table {
  :deep(.el-table__header-wrapper th) {
    background: var(--erp-slate-50);
    color: var(--erp-slate-600);
    font-weight: 600;
    font-size: 12px;
  }
}

.rank-badge {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 13px;
  color: #fff;
  margin: 0 auto;
}

.rank-1 {
  background: linear-gradient(135deg, #f59e0b, #d97706);
}
.rank-2 {
  background: linear-gradient(135deg, #94a3b8, #64748b);
}
.rank-3 {
  background: linear-gradient(135deg, #b45309, #92400e);
}

.rank-normal {
  font-size: 13px;
  color: var(--erp-slate-400);
}

.user-cell__name {
  font-weight: 600;
  color: var(--erp-slate-900);
}

.distribution-cell {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.mini-bar {
  display: flex;
  height: 6px;
  border-radius: 3px;
  overflow: hidden;
  background: var(--erp-slate-100);
  gap: 1px;
}

.mini-bar__segment {
  border-radius: 3px;
  transition: width 0.3s ease;
  min-width: 2px;
}

.pills {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.pill {
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 500;
}

.pill--processing {
  background: var(--erp-warning-50);
  color: var(--erp-warning-600);
  border: 1px solid var(--erp-warning-200);
}

.pill--approved {
  background: var(--erp-success-50);
  color: var(--erp-success-600);
  border: 1px solid var(--erp-success-200);
}

.pill--rejected {
  background: var(--erp-danger-50);
  color: var(--erp-danger-600);
  border: 1px solid var(--erp-danger-200);
}

.pill--cancelled {
  background: var(--erp-slate-50);
  color: var(--erp-slate-500);
  border: 1px solid var(--erp-slate-200);
}

// 快捷操作
.quick-actions {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

@media (max-width: 900px) {
  .quick-actions {
    grid-template-columns: 1fr;
  }
}

.quick-action-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  border: 1px solid var(--erp-slate-200);
  cursor: pointer;
  transition: all 0.2s ease;
  &:hover {
    border-color: var(--erp-primary-400);
    background: var(--erp-primary-50);
    .quick-action-arrow {
      transform: translateX(4px);
      color: var(--erp-primary-600);
    }
  }
}

.quick-action-icon {
  font-size: 24px;
  flex-shrink: 0;
}

.quick-action-info {
  flex: 1;
  min-width: 0;
}

.quick-action-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--erp-slate-900);
}

.quick-action-desc {
  font-size: 12px;
  color: var(--erp-slate-500);
  margin-top: 4px;
}

.quick-action-arrow {
  font-size: 16px;
  color: var(--erp-slate-400);
  transition: all 0.2s ease;
}
</style>
