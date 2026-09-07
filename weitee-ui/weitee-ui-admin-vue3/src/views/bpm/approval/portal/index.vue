<template>
  <div class="approval-page approval-portal-page">
    <!-- 页头卡片 -->
    <ContentWrap class="approval-page__header-card">
      <div class="approval-page__page-header">
        <div class="approval-page__page-title">审批门户</div>
        <div class="approval-page__metric-grid approval-page__metric-grid--4">
          <div v-for="card in summaryCards" :key="card.label" class="approval-page__metric-card" :class="card.toneClass">
            <div class="approval-page__metric-icon" :class="card.toneClass">
              <Icon :icon="card.icon" />
            </div>
            <div class="approval-page__metric-body">
              <div class="approval-page__metric-label">{{ card.label }}</div>
              <div class="approval-page__metric-value">{{ card.value }}</div>
            </div>
          </div>
        </div>
      </div>
    </ContentWrap>

    <!-- Tab 列表卡片 -->
    <ContentWrap class="approval-page__table-card">
      <el-tabs v-model="activeTab" class="approval-portal-page__tabs">
        <el-tab-pane v-for="tab in tabs" :key="tab.key" :name="tab.key" :label="tab.label">
          <div class="approval-portal-page__tab-head">
            <span class="approval-page__toolbar-count">当前共 <strong>{{ tabCounts[tab.key] ?? 0 }}</strong> 条</span>
          </div>
          <div class="approval-page__table-wrap">
            <el-table
              v-loading="loading"
              :data="tabLists[tab.key]"
              row-key="id"
              stripe
              :show-overflow-tooltip="false"
              class="approval-page__table"
            >
              <template v-if="tab.key === 'todo'">
                <el-table-column min-width="220">
                  <template #header>
                    <span class="approval-page__column-header">
                      <Icon icon="ep:bell" class="approval-page__column-icon" />
                      当前任务
                    </span>
                  </template>
                  <template #default="{ row }">
                    <div class="approval-page__primary-cell">
                      <span class="approval-page__primary-text">{{ row.name || '-' }}</span>
                      <span class="approval-page__muted-text approval-page__mono">{{ row.processInstanceId || '-' }}</span>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column min-width="200" label="所属流程">
                  <template #default="{ row }">
                    <span class="approval-page__muted-text approval-page__ellipsis">{{ row.processInstance?.name || '-' }}</span>
                  </template>
                </el-table-column>
                <el-table-column min-width="140" label="发起人">
                  <template #default="{ row }">
                    {{ row.processInstance?.startUser?.nickname || '-' }}
                  </template>
                </el-table-column>
                <el-table-column min-width="170" label="任务时间" align="right">
                  <template #default="{ row }">
                    <span class="approval-page__mono approval-page__muted-text">{{ formatTime(row.createTime) }}</span>
                  </template>
                </el-table-column>
                <el-table-column min-width="120" label="操作" align="center" fixed="right">
                  <template #default="{ row }">
                    <el-button link type="primary" @click="handleTodo(row)">处理</el-button>
                  </template>
                </el-table-column>
              </template>

              <template v-else-if="tab.key === 'done'">
                <el-table-column min-width="200" label="任务名称">
                  <template #default="{ row }">
                    <span class="approval-page__muted-text approval-page__ellipsis">{{ row.name || '-' }}</span>
                  </template>
                </el-table-column>
                <el-table-column min-width="200" label="所属流程">
                  <template #default="{ row }">
                    <span class="approval-page__muted-text approval-page__ellipsis">{{ row.processInstance?.name || '-' }}</span>
                  </template>
                </el-table-column>
                <el-table-column min-width="120" label="处理结果">
                  <template #default="{ row }">
                    <span class="approval-page__pill" :class="row.status === 4 ? 'approval-page__pill--success' : 'approval-page__pill--muted'">
                      {{ row.status === 4 ? '已通过' : row.statusName || '-' }}
                    </span>
                  </template>
                </el-table-column>
                <el-table-column min-width="170" label="完成时间" align="right">
                  <template #default="{ row }">
                    <span class="approval-page__mono approval-page__muted-text">{{ formatTime(row.endTime || row.createTime) }}</span>
                  </template>
                </el-table-column>
                <el-table-column min-width="120" label="操作" align="center" fixed="right">
                  <template #default="{ row }">
                    <el-button link type="primary" @click="goDetail(row)">查看</el-button>
                  </template>
                </el-table-column>
              </template>

              <template v-else>
                <el-table-column min-width="200">
                  <template #header>
                    <span class="approval-page__column-header">
                      <Icon icon="ep:document" class="approval-page__column-icon" />
                      流程名称
                    </span>
                  </template>
                  <template #default="{ row }">
                    <div class="approval-page__primary-cell">
                      <span class="approval-page__primary-text">{{ row.name || '-' }}</span>
                      <span class="approval-page__muted-text approval-page__mono">{{ row.id }}</span>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column min-width="220" label="摘要">
                  <template #default="{ row }">
                    <span class="approval-page__muted-text approval-page__ellipsis">{{ row.summary || '-' }}</span>
                  </template>
                </el-table-column>
                <el-table-column min-width="150" label="流程状态">
                  <template #default="{ row }">
                    <span class="approval-page__pill" :class="instanceStatusMeta(row.status).tone">
                      {{ instanceStatusMeta(row.status).label }}
                    </span>
                  </template>
                </el-table-column>
                <el-table-column min-width="170" label="发起时间" align="right">
                  <template #default="{ row }">
                    <!-- 流程实例的时间字段是 startTime（接口不返回 createTime），兜底 createTime 兼容 -->
                    <span class="approval-page__mono approval-page__muted-text">{{ formatTime(row.startTime || row.createTime) }}</span>
                  </template>
                </el-table-column>
                <el-table-column min-width="120" label="操作" align="center" fixed="right">
                  <template #default="{ row }">
                    <el-button link type="primary" @click="goDetail(row)">查看</el-button>
                  </template>
                </el-table-column>
              </template>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
    </ContentWrap>
  </div>
</template>

<script setup lang="ts">
import { formatDate } from '@/utils/formatTime'
import { getTaskTodoPage, getTaskDonePage } from '@/api/bpm/task'
import { getProcessInstanceMyPage, getProcessInstanceCopyPage } from '@/api/bpm/processInstance'

/** 审批门户 */
defineOptions({ name: 'BpmApprovalPortal' })

const router = useRouter()
const route = useRoute()

const activeTab = ref<string>('todo')
const loading = ref(false)
const tabLists = reactive<Record<string, any[]>>({ todo: [], done: [], submitted: [], cc: [] })
const tabCounts = reactive<Record<string, number>>({ todo: 0, done: 0, submitted: 0, cc: 0 })

const tabs = [
  { key: 'todo', label: '待我审批' },
  { key: 'done', label: '我已审批' },
  { key: 'submitted', label: '我发起的' },
  { key: 'cc', label: '抄送我的' }
]

const summaryCards = ref([
  { label: '待我审批', value: 0, icon: 'ep:bell', toneClass: 'approval-page__metric--amber' },
  { label: '我已审批', value: 0, icon: 'ep:select', toneClass: 'approval-page__metric--blue' },
  { label: '我发起的', value: 0, icon: 'ep:upload', toneClass: 'approval-page__metric--teal' },
  { label: '抄送我的', value: 0, icon: 'ep:message', toneClass: 'approval-page__metric--muted' }
])

const INSTANCE_STATUS: Record<number, { label: string; tone: string }> = {
  1: { label: '审批中', tone: 'approval-page__pill--warning' },
  2: { label: '已通过', tone: 'approval-page__pill--success' },
  3: { label: '已驳回', tone: 'approval-page__pill--danger' },
  4: { label: '已撤回', tone: 'approval-page__pill--muted' },
  5: { label: '已取消', tone: 'approval-page__pill--muted' }
}
const instanceStatusMeta = (status?: number) =>
  INSTANCE_STATUS[status ?? -1] ?? { label: '-', tone: 'approval-page__pill--muted' }

const formatTime = (time?: string) => (time ? formatDate(time) : '-')

/** 加载指定 tab 数据 */
const loadTab = async (key: string) => {
  loading.value = true
  try {
    if (key === 'todo') {
      const data = await getTaskTodoPage({ pageNo: 1, pageSize: 20 })
      tabLists.todo = data.list
      tabCounts.todo = data.total
    } else if (key === 'done') {
      const data = await getTaskDonePage({ pageNo: 1, pageSize: 20 })
      tabLists.done = data.list
      tabCounts.done = data.total
    } else if (key === 'submitted') {
      const data = await getProcessInstanceMyPage({ pageNo: 1, pageSize: 20 })
      tabLists.submitted = data.list
      tabCounts.submitted = data.total
    } else if (key === 'cc') {
      const data = await getProcessInstanceCopyPage({ pageNo: 1, pageSize: 20 })
      tabLists.cc = data.list
      tabCounts.cc = data.total
    }
  } finally {
    loading.value = false
  }
}

/** tab 切换 */
watch(activeTab, (key) => {
  loadTab(key)
})

/** 待办处理：打开流程详情执行审批 */
const handleTodo = (row: any) => {
  goDetail(row)
}

/** 查看流程详情 */
const goDetail = (row: any) => {
  const id = row.processInstanceId || row.id
  if (id) {
    router.push('/bpm/process-instance/detail?id=' + id)
  }
}

/** 初始化：根据菜单入口确定初始 tab */
onMounted(async () => {
  const entry = String(route.path).split('/').pop() || 'todo'
  const keyMap: Record<string, string> = { todo: 'todo', approved: 'done', submitted: 'submitted', cc: 'cc' }
  activeTab.value = keyMap[entry] ?? 'todo'
  await loadTab(activeTab.value)
  await loadSummary()
})

const loadSummary = async () => {
  try {
    const [todo, done, my, cc] = await Promise.all([
      getTaskTodoPage({ pageNo: 1, pageSize: 1 }),
      getTaskDonePage({ pageNo: 1, pageSize: 1 }),
      getProcessInstanceMyPage({ pageNo: 1, pageSize: 1 }),
      getProcessInstanceCopyPage({ pageNo: 1, pageSize: 1 })
    ])
    summaryCards.value[0].value = todo.total
    summaryCards.value[1].value = done.total
    summaryCards.value[2].value = my.total
    summaryCards.value[3].value = cc.total
  } catch {}
}
</script>

<style scoped lang="scss">
.approval-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: var(--erp-slate-50);
  padding: 16px;

  &__header-card,
  &__table-card {
    border-radius: 12px;
    box-shadow: var(--erp-shadow-sm);
  }

  &__page-header {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  &__page-title {
    font-size: 18px;
    font-weight: 600;
    color: var(--erp-slate-800);
  }

  &__metric-grid {
    display: grid;
    grid-template-columns: repeat(3, minmax(160px, 1fr));
    gap: 16px;

    &--4 {
      grid-template-columns: repeat(4, minmax(160px, 1fr));
    }

    @media (max-width: 1024px) {
      grid-template-columns: repeat(2, 1fr);
    }

    @media (max-width: 640px) {
      grid-template-columns: 1fr;
    }
  }

  &__metric-card {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px;
    border: 1px solid var(--erp-slate-100);
    border-radius: 8px;
    background: var(--erp-surface-white);
  }

  &__metric-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 36px;
    height: 36px;
    border-radius: 8px;
    font-size: 18px;
  }

  &__metric--amber {
    background: var(--erp-stat-gradient-amber);
    color: var(--erp-warning-600);
  }

  &__metric--blue {
    background: var(--erp-stat-gradient-blue);
    color: var(--erp-primary-600);
  }

  &__metric--teal {
    background: var(--erp-stat-gradient-teal);
    color: var(--erp-teal-600);
  }

  &__metric--muted {
    background: var(--erp-stat-gradient-slate);
    color: var(--erp-slate-500);
  }

  &__metric-label {
    font-size: 12px;
    color: var(--erp-slate-500);
  }

  &__metric-value {
    font-size: 20px;
    font-weight: 600;
    color: var(--erp-slate-800);
  }

  &__table-wrap {
    overflow-x: auto;
  }

  &__table {
    width: 100%;
  }

  &__column-header {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    font-weight: 600;
    color: var(--erp-slate-600);
  }

  &__column-icon {
    font-size: 14px;
  }

  &__primary-cell {
    display: flex;
    flex-direction: column;
    gap: 2px;
    min-width: 0;
  }

  &__primary-text {
    font-size: 14px;
    font-weight: 500;
    color: var(--erp-slate-800);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__muted-text {
    font-size: 12px;
    color: var(--erp-slate-400);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__mono {
    font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  }

  &__ellipsis {
    display: inline-block;
    max-width: 100%;
  }

  &__pill {
    display: inline-flex;
    align-items: center;
    padding: 2px 10px;
    border-radius: 999px;
    font-size: 12px;
    border: 1px solid transparent;

    &--success {
      background: var(--erp-success-50);
      color: var(--erp-success-600);
      border-color: var(--erp-success-100);
    }

    &--warning {
      background: var(--erp-warning-50);
      color: var(--erp-warning-600);
      border-color: var(--erp-warning-100);
    }

    &--danger {
      background: var(--erp-danger-50);
      color: var(--erp-danger-600);
      border-color: var(--erp-danger-100);
    }

    &--muted {
      background: var(--erp-slate-100);
      color: var(--erp-slate-500);
      border-color: var(--erp-slate-200);
    }
  }
}

.approval-portal-page {
  &__tabs {
    :deep(.el-tabs__header) {
      margin-bottom: 16px;
    }
  }

  &__tab-head {
    margin-bottom: 12px;
  }
}
</style>
