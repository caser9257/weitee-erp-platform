<template>
  <div class="approval-page approval-delegation-page">
    <!-- 页头卡片 -->
    <ContentWrap class="approval-page__header-card">
      <div class="approval-page__page-header">
        <div class="approval-page__page-title">审批委托</div>
        <div class="approval-page__metric-grid">
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

    <!-- 筛选卡片 -->
    <ContentWrap class="approval-page__filter-card">
      <div class="approval-page__section-head">
        <div class="approval-page__section-title">筛选条件</div>
      </div>
      <el-form ref="queryFormRef" :model="queryParams" label-width="84px" class="approval-page__query-form" @submit.prevent>
        <div class="approval-page__query-grid approval-page__query-grid--2">
          <el-form-item label="委托场景" prop="sceneCode">
            <el-input v-model="queryParams.sceneCode" placeholder="留空表示全部场景" clearable class="!w-full" @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="委托原因" prop="reason">
            <el-input v-model="queryParams.reason" placeholder="请输入委托原因" clearable class="!w-full" @keyup.enter="handleQuery" />
          </el-form-item>
        </div>
        <div class="approval-page__query-actions">
          <el-button type="primary" :loading="loading" @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" />
            查询
          </el-button>
          <el-button @click="resetQuery">
            <Icon icon="ep:refresh-left" class="mr-5px" />
            重置
          </el-button>
        </div>
      </el-form>
    </ContentWrap>

    <!-- 表格卡片 -->
    <ContentWrap class="approval-page__table-card">
      <div class="approval-page__toolbar">
        <div class="approval-page__toolbar-main">
          <div class="approval-page__section-title">委托记录</div>
          <div class="approval-page__toolbar-count">当前共 <strong>{{ total }}</strong> 条</div>
        </div>
        <div class="approval-page__toolbar-actions">
          <el-button type="primary" plain @click="openForm('create')" v-hasPermi="['bpm:approval-delegation:create']">
            <Icon icon="ep:plus" class="mr-5px" />
            新增委托
          </el-button>
        </div>
      </div>

      <div class="approval-page__table-wrap">
        <el-table v-loading="loading" :data="list" row-key="id" stripe :show-overflow-tooltip="false" class="approval-page__table">
          <el-table-column min-width="200">
            <template #header>
              <span class="approval-page__column-header">
                <Icon icon="ep:user" class="approval-page__column-icon" />
                被委托人
              </span>
            </template>
            <template #default="{ row }">
              <div class="approval-page__primary-cell">
                <span class="approval-page__primary-text">{{ row.delegateUserName || `用户#${row.delegateUserId}` }}</span>
                <span class="approval-page__muted-text approval-page__mono">{{ row.delegateUserId ?? '-' }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column min-width="190" label="委托时间">
            <template #default="{ row }">
              <div class="approval-page__primary-cell">
                <span class="approval-page__muted-text approval-page__mono">{{ formatTime(row.startTime) }}</span>
                <span class="approval-page__muted-text approval-page__mono">至 {{ formatTime(row.endTime) }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column min-width="150" label="委托场景">
            <template #default="{ row }">
              <span v-if="row.sceneCode" class="approval-page__pill approval-page__pill--primary approval-page__mono">
                {{ row.sceneCode }}
              </span>
              <span v-else class="approval-page__pill approval-page__pill--info">全部场景</span>
            </template>
          </el-table-column>
          <el-table-column min-width="100" label="状态">
            <template #default="{ row }">
              <span class="approval-page__pill" :class="getDelegationStatus(row).tone">
                {{ getDelegationStatus(row).label }}
              </span>
            </template>
          </el-table-column>
          <el-table-column min-width="200" label="委托原因">
            <template #default="{ row }">
              <span class="approval-page__muted-text approval-page__ellipsis">{{ row.reason || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column min-width="170" label="创建时间" align="right">
            <template #default="{ row }">
              <span class="approval-page__mono approval-page__muted-text">{{ formatTime(row.createTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column min-width="140" label="操作" align="center" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openForm('update', row.id)" v-hasPermi="['bpm:approval-delegation:update']">
                编辑
              </el-button>
              <el-button link type="danger" @click="handleDelete(row.id)" v-hasPermi="['bpm:approval-delegation:delete']">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <Pagination
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </ContentWrap>

    <!-- 表单弹窗 -->
    <ApprovalDelegationForm ref="formRef" @success="getList" />
  </div>
</template>

<script setup lang="ts">
import { formatDate } from '@/utils/formatTime'
import { ApprovalDelegationApi, BpmApprovalDelegationVO } from '@/api/bpm/approval/delegation'
import ApprovalDelegationForm from './ApprovalDelegationForm.vue'

/** 审批委托列表 */
defineOptions({ name: 'BpmApprovalDelegation' })

const message = useMessage()

const loading = ref(true)
const list = ref<BpmApprovalDelegationVO[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  sceneCode: undefined,
  reason: undefined
})
const queryFormRef = ref()

const summaryCards = ref([
  { label: '全部委托', value: 0, icon: 'ep:connection', toneClass: 'approval-page__metric--blue' },
  { label: '生效中', value: 0, icon: 'ep:circle-check', toneClass: 'approval-page__metric--green' },
  { label: '已过期/未开始', value: 0, icon: 'ep:clock', toneClass: 'approval-page__metric--muted' }
])

const now = () => new Date().getTime()
const toTime = (value?: string) => (value ? new Date(value).getTime() : NaN)

/** 委托状态：0=未开始 1=生效中 2=已过期 */
const getDelegationStatus = (row: BpmApprovalDelegationVO) => {
  const start = toTime(row.startTime)
  const end = toTime(row.endTime)
  const current = now()
  if (current < start) {
    return { label: '未开始', tone: 'approval-page__pill--warning' }
  }
  if (current > end) {
    return { label: '已过期', tone: 'approval-page__pill--muted' }
  }
  return { label: '生效中', tone: 'approval-page__pill--success' }
}

const formatTime = (time?: string) => (time ? formatDate(time) : '-')

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await ApprovalDelegationApi.getDelegationPage(queryParams)
    list.value = data.list
    total.value = data.total
    refreshSummary(data.list)
  } finally {
    loading.value = false
  }
}

const refreshSummary = (rows: BpmApprovalDelegationVO[]) => {
  summaryCards.value[0].value = total.value
  summaryCards.value[1].value = rows.filter((item) => getDelegationStatus(item).label === '生效中').length
  summaryCards.value[2].value = rows.filter((item) => getDelegationStatus(item).label !== '生效中').length
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
  handleQuery()
}

/** 新增/编辑操作 */
const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await ApprovalDelegationApi.deleteDelegation(id)
    message.success('删除成功')
    await getList()
  } catch {}
}

/** 初始化 */
onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.approval-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: var(--erp-slate-50);
  padding: 16px;

  &__header-card,
  &__filter-card,
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

    @media (max-width: 768px) {
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

  &__metric--blue {
    background: var(--erp-stat-gradient-blue);
    color: var(--erp-primary-600);
  }

  &__metric--green {
    background: var(--erp-stat-gradient-green);
    color: var(--erp-success-600);
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

  &__section-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;
  }

  &__section-title {
    font-size: 14px;
    font-weight: 600;
    color: var(--erp-slate-800);
  }

  &__query-grid {
    display: grid;
    grid-template-columns: repeat(3, minmax(200px, 1fr));
    gap: 8px 16px;

    &--2 {
      grid-template-columns: repeat(2, minmax(240px, 1fr));
    }

    @media (max-width: 1280px) {
      grid-template-columns: repeat(2, 1fr);
    }

    @media (max-width: 768px) {
      grid-template-columns: 1fr;
    }
  }

  &__query-actions {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
    margin-top: 8px;
  }

  &__toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;
  }

  &__toolbar-main {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  &__toolbar-count {
    font-size: 12px;
    color: var(--erp-slate-500);

    strong {
      color: var(--erp-primary-600);
    }
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

    &--primary {
      background: var(--erp-primary-50);
      color: var(--erp-primary-600);
      border-color: var(--erp-primary-100);
    }

    &--info {
      background: var(--erp-slate-100);
      color: var(--erp-slate-600);
      border-color: var(--erp-slate-200);
    }

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

    &--muted {
      background: var(--erp-slate-100);
      color: var(--erp-slate-500);
      border-color: var(--erp-slate-200);
    }
  }
}
</style>
