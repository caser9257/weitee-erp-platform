<template>
  <section class="wt-hero">
    <div class="wt-hero__header">
      <div>
        <div class="wt-hero__breadcrumb">制造执行管理 / 工序任务</div>
        <div class="wt-page__title">工序任务</div>
      </div>
    </div>
  </section>

  <ContentWrap class="wt-page__filter-card">
    <el-form ref="queryFormRef" :model="queryParams" label-position="top" class="wt-query">
      <div class="wt-query__grid">
        <el-form-item label="生产工单号" prop="productionOrderNo">
          <el-input
            v-model="queryParams.productionOrderNo"
            placeholder="请输入生产工单号"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" clearable placeholder="请选择状态">
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
      </div>
      <div class="wt-query__footer">
        <div class="wt-query__actions">
          <el-button @click="resetQuery" :disabled="listLoading">
            <Icon icon="ep:refresh" class="mr-5px" /> 重置
          </el-button>
          <el-button type="primary" @click="handleQuery" :loading="listLoading">
            <Icon icon="ep:search" class="mr-5px" /> 查询
          </el-button>
        </div>
      </div>
    </el-form>
  </ContentWrap>

  <ContentWrap class="wt-page__list-card">
    <div class="wt-toolbar">
      <div class="wt-toolbar__actions">
        <el-button
          type="primary"
          plain
          v-hasPermi="['mes:work-task:update']"
          :loading="scheduleLoading"
          @click="handleSchedule"
        >
          <Icon icon="ep:magic-stick" class="mr-5px" /> 自动排程
        </el-button>
      </div>
      <div class="wt-toolbar__meta">
        <el-tag v-if="conflictCount > 0" type="danger" effect="light" size="small">
          检测到 {{ conflictCount }} 处计划时间冲突
        </el-tag>
      </div>
    </div>
    <el-table
      v-loading="listLoading"
      :data="list"
      :stripe="true"
      class="wt-ledger"
      :row-class-name="rowClassName"
    >
      <template #empty>
        <div class="wt-empty">
          <div class="wt-empty__icon">
            <Icon icon="ep:timer" />
          </div>
          <div class="wt-empty__title">暂无工序任务</div>
          <div class="wt-empty__hint">工单下达后自动生成工序任务</div>
        </div>
      </template>
      <el-table-column label="任务信息" min-width="200">
        <template #default="{ row }">
          <div class="wt-ledger__order">
            <div class="wt-ledger__order-top">
              <div class="wt-ledger__order-no">{{ row.taskNo }}</div>
              <span class="wt-badge" :class="resolveStatusBadgeClass(row.status)">
                {{ resolveStatusLabel(row.status) }}
              </span>
            </div>
            <div class="wt-ledger__order-meta">工单 {{ row.productionOrderNo || '-' }}</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="工序" min-width="160">
        <template #default="{ row }">
          <div class="wt-ledger__step">
            <div class="wt-ledger__step-name">{{ row.stepNo }} · {{ row.stepName }}</div>
            <div class="wt-ledger__step-meta">{{ row.stepCode }}</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="优先级" min-width="90" align="center">
        <template #default="{ row }">
          <el-input-number
            v-model="row.priority"
            :min="0"
            :max="999"
            :precision="0"
            size="small"
            controls-position="right"
            :disabled="!canUpdate(row)"
            @change="handlePriorityChange(row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="计划数量" min-width="100" align="right">
        <template #default="{ row }">{{ formatCount(row.planQty) }}</template>
      </el-table-column>
      <el-table-column label="计划时间窗" min-width="240">
        <template #default="{ row }">
          <div class="wt-ledger__time">
            {{ formatDateValue(row.planStartTime) }} ~ {{ formatDateValue(row.planEndTime) }}
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="140" align="center">
        <template #default="{ row }">
          <el-button
            v-if="canUpdate(row)"
            link
            type="primary"
            v-hasPermi="['mes:work-task:update']"
            @click="openPlanDialog(row)"
          >
            调整计划
          </el-button>
          <el-button
            v-if="canCancel(row)"
            link
            type="danger"
            v-hasPermi="['mes:work-task:update']"
            @click="handleCancel(row)"
          >
            取消
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>

  <Dialog v-model="planVisible" title="调整计划时间" width="480">
    <el-form label-width="110px">
      <div class="wt-plan-step">
        <div class="wt-plan-step__name">{{ planForm.stepNo }} · {{ planForm.stepName }}</div>
        <div class="wt-plan-step__meta">{{ planForm.taskNo }}</div>
      </div>
      <el-form-item label="计划开始时间" required>
        <el-date-picker
          v-model="planForm.planStartTime"
          type="datetime"
          value-format="YYYY-MM-DD HH:mm:ss"
          placeholder="请选择计划开始时间"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="计划结束时间" required>
        <el-date-picker
          v-model="planForm.planEndTime"
          type="datetime"
          value-format="YYYY-MM-DD HH:mm:ss"
          placeholder="请选择计划结束时间"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="planForm.remark" type="textarea" :rows="2" maxlength="255" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="planVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitLoading" @click="handlePlanSubmit">保存</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { formatDate } from '@/utils/formatTime'
import { erpCountInputFormatter } from '@/utils'
import { WORK_TASK_STATUS, WorkTaskApi, WorkTaskVO } from '@/api/mes/work-task'

defineOptions({ name: 'MesWorkTask' })

const message = useMessage()

const queryFormRef = ref()
const listLoading = ref(false)
const submitLoading = ref(false)
const planVisible = ref(false)

const list = ref<WorkTaskVO[]>([])
const total = ref(0)
const scheduleLoading = ref(false)
const conflictCount = ref(0)
const conflictTaskIds = ref<Set<number>>(new Set())

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  productionOrderNo: undefined as string | undefined,
  status: undefined as number | undefined
})

const planForm = reactive<Partial<WorkTaskVO>>({})

const statusOptions = [
  { label: '待排程', value: WORK_TASK_STATUS.WAIT_SCHEDULE },
  { label: '已排程', value: WORK_TASK_STATUS.SCHEDULED },
  { label: '进行中', value: WORK_TASK_STATUS.PROCESSING },
  { label: '已完成', value: WORK_TASK_STATUS.FINISHED },
  { label: '已取消', value: WORK_TASK_STATUS.CANCELED }
]

const formatCount = (value?: number) => erpCountInputFormatter(value || 0)

const formatDateValue = (value?: string | Date | number) =>
  value ? formatDate(value, 'YYYY-MM-DD HH:mm') : '-'

const resolveStatusLabel = (status?: number) => {
  if (status === WORK_TASK_STATUS.WAIT_SCHEDULE) return '待排程'
  if (status === WORK_TASK_STATUS.SCHEDULED) return '已排程'
  if (status === WORK_TASK_STATUS.PROCESSING) return '进行中'
  if (status === WORK_TASK_STATUS.FINISHED) return '已完成'
  if (status === WORK_TASK_STATUS.CANCELED) return '已取消'
  return '-'
}

const resolveStatusBadgeClass = (status?: number) => {
  if (status === WORK_TASK_STATUS.WAIT_SCHEDULE) return 'wt-badge--slate'
  if (status === WORK_TASK_STATUS.SCHEDULED) return 'wt-badge--primary'
  if (status === WORK_TASK_STATUS.PROCESSING) return 'wt-badge--warning'
  if (status === WORK_TASK_STATUS.FINISHED) return 'wt-badge--success'
  if (status === WORK_TASK_STATUS.CANCELED) return 'wt-badge--info'
  return 'wt-badge--slate'
}

const canUpdate = (row: WorkTaskVO) =>
  row.status === WORK_TASK_STATUS.WAIT_SCHEDULE || row.status === WORK_TASK_STATUS.SCHEDULED

const canCancel = (row: WorkTaskVO) =>
  row.status === WORK_TASK_STATUS.WAIT_SCHEDULE || row.status === WORK_TASK_STATUS.SCHEDULED

const getList = async () => {
  listLoading.value = true
  try {
    const data = await WorkTaskApi.getWorkTaskPage(queryParams)
    list.value = data.list || []
    total.value = data.total || 0
    await loadConflicts()
  } catch {
    list.value = []
    total.value = 0
  } finally {
    listLoading.value = false
  }
}

const loadConflicts = async () => {
  // 对当前列表中的已排程任务按工作中心做冲突检测（并发请求）
  const centerIds = Array.from(
    new Set(
      list.value
        .filter((t) => t.workCenterId && t.status === WORK_TASK_STATUS.SCHEDULED)
        .map((t) => t.workCenterId)
    )
  ) as number[]
  conflictTaskIds.value = new Set()
  conflictCount.value = 0
  if (!centerIds.length) return
  const results = await Promise.allSettled(
    centerIds.map((centerId) => WorkTaskApi.detectConflicts(centerId))
  )
  results.forEach((r) => {
    if (r.status !== 'fulfilled') return
    ;(r.value as unknown as { taskIdA: number; taskIdB: number }[]).forEach((c) => {
      conflictTaskIds.value.add(c.taskIdA)
      conflictTaskIds.value.add(c.taskIdB)
    })
    conflictCount.value += (r.value as unknown[]).length
  })
}

const rowClassName = ({ row }: { row: WorkTaskVO }) =>
  conflictTaskIds.value.has(row.id) ? 'wt-row--conflict' : ''

const handleSchedule = async () => {
  try {
    await message.confirm('自动排程将按优先级与交期为待排程任务分配计划时间，已排程任务保持不变。')
  } catch {
    return
  }
  scheduleLoading.value = true
  try {
    const count = await WorkTaskApi.schedule()
    message.success(`自动排程完成，共排程 ${count} 个任务`)
    await getList()
  } finally {
    scheduleLoading.value = false
  }
}

const handlePriorityChange = async (row: WorkTaskVO) => {
  if (!row.id || row.priority === undefined) return
  try {
    await WorkTaskApi.updatePriority(row.id, Number(row.priority))
    message.success('更新优先级成功')
  } catch {
    row.priority = 0
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryFormRef.value?.resetFields()
  handleQuery()
}

const openPlanDialog = (row: WorkTaskVO) => {
  Object.assign(planForm, {
    id: row.id,
    taskNo: row.taskNo,
    stepNo: row.stepNo,
    stepName: row.stepName,
    planStartTime: row.planStartTime,
    planEndTime: row.planEndTime,
    remark: row.remark
  })
  planVisible.value = true
}

const handlePlanSubmit = async () => {
  if (!planForm.id) return
  if (!planForm.planStartTime || !planForm.planEndTime) {
    message.warning('请选择计划开始和结束时间')
    return
  }
  if (planForm.planStartTime > planForm.planEndTime) {
    message.warning('计划结束时间不能早于计划开始时间')
    return
  }
  submitLoading.value = true
  try {
    await WorkTaskApi.updatePlanTime({
      id: planForm.id,
      planStartTime: planForm.planStartTime as string,
      planEndTime: planForm.planEndTime as string,
      remark: planForm.remark?.trim()
    })
    message.success('调整计划时间成功')
    planVisible.value = false
    await getList()
  } finally {
    submitLoading.value = false
  }
}

const handleCancel = async (row: WorkTaskVO) => {
  if (!row.id) return
  try {
    await message.confirm(`确定取消任务「${row.taskNo}」吗？`)
  } catch {
    return
  }
  await WorkTaskApi.cancelTask(row.id)
  message.success('取消任务成功')
  await getList()
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.wt-hero {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 16px;
}

.wt-hero__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.wt-hero__breadcrumb {
  color: var(--erp-slate-400);
  font-size: 12px;
  font-weight: 600;
  line-height: 18px;
  margin-bottom: 6px;
}

.wt-page__title {
  color: var(--erp-slate-900);
  font-size: 28px;
  font-weight: 800;
  line-height: 36px;
}

.wt-page__filter-card,
.wt-page__list-card {
  border: 1px solid var(--erp-slate-200);
  border-radius: 20px;
  background: var(--erp-surface-white);
  box-shadow: var(--erp-shadow-sm);
}

.wt-query {
  :deep(.el-form-item) {
    margin-bottom: 0;
  }

  :deep(.el-form-item__label) {
    padding-bottom: 6px;
    color: var(--erp-slate-600);
    font-size: 12px;
    font-weight: 600;
    line-height: 18px;
  }

  :deep(.el-input__wrapper),
  :deep(.el-select__wrapper) {
    min-height: 38px;
    padding: 0 12px;
    border: 1px solid var(--erp-slate-200);
    border-radius: 10px;
    background: var(--erp-slate-50);
    box-shadow: inset 0 1px 1px rgba(15, 23, 42, 0.02);
  }

  :deep(.el-input__wrapper.is-focus),
  :deep(.el-select__wrapper.is-focused) {
    border-color: var(--erp-primary-300);
    background: var(--erp-surface-white);
    box-shadow: 0 0 0 3px var(--erp-primary-50);
  }
}

.wt-query__grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.wt-query__footer {
  margin-top: 14px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
}

.wt-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--erp-slate-200);
  flex-wrap: wrap;
}

.wt-toolbar__actions,
.wt-toolbar__meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.wt-ledger {
  width: 100%;

  :deep(.el-table__header-wrapper th) {
    background: var(--erp-slate-50);
    color: var(--erp-slate-500);
    font-size: 11px;
    font-weight: 700;
  }

  :deep(.el-table__cell) {
    padding-right: 8px;
    padding-left: 8px;
  }

  :deep(.el-table__row td) {
    padding-top: 14px;
    padding-bottom: 14px;
    vertical-align: top;
  }

  :deep(.wt-row--conflict td) {
    background: var(--erp-danger-50);
  }

  :deep(.wt-row--conflict:hover > td) {
    background: var(--erp-danger-50) !important;
  }
}

.wt-ledger__order,
.wt-ledger__step,
.wt-ledger__time {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 6px;
}

.wt-ledger__order-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 8px;
  flex-wrap: wrap;
}

.wt-ledger__order-no,
.wt-ledger__step-name {
  color: var(--erp-slate-900);
  font-weight: 800;
  line-height: 22px;
  font-family:
    ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New',
    monospace;
}

.wt-ledger__order-meta,
.wt-ledger__step-meta {
  color: var(--erp-slate-500);
  font-size: 11px;
  line-height: 17px;
}

.wt-ledger__time {
  color: var(--erp-slate-600);
  font-size: 12px;
  line-height: 20px;
}

.wt-badge {
  display: inline-flex;
  align-items: center;
  min-height: 22px;
  padding: 2px 10px;
  border-radius: 999px;
  border: 1px solid transparent;
  font-size: 11px;
  font-weight: 700;
  line-height: 16px;
  white-space: nowrap;
}

.wt-badge--slate {
  background: var(--erp-slate-100);
  border-color: var(--erp-slate-200);
  color: var(--erp-slate-600);
}

.wt-badge--primary {
  background: var(--erp-primary-50);
  border-color: var(--erp-primary-200);
  color: var(--erp-primary-600);
}

.wt-badge--success {
  background: var(--erp-success-50);
  border-color: var(--erp-success-200);
  color: var(--erp-success-600);
}

.wt-badge--warning {
  background: var(--erp-warning-50);
  border-color: var(--erp-warning-200);
  color: var(--erp-warning-600);
}

.wt-badge--info {
  background: var(--erp-slate-100);
  border-color: var(--erp-slate-200);
  color: var(--erp-slate-500);
}

.wt-plan-step {
  margin-bottom: 16px;
  padding: 12px 14px;
  border: 1px solid var(--erp-slate-200);
  border-radius: 12px;
  background: var(--erp-slate-50);
}

.wt-plan-step__name {
  color: var(--erp-slate-900);
  font-size: 14px;
  font-weight: 700;
  line-height: 22px;
  margin-bottom: 4px;
}

.wt-plan-step__meta {
  color: var(--erp-slate-500);
  font-size: 12px;
}

.wt-empty {
  min-height: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--erp-slate-500);
}

.wt-empty__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 16px;
  background: var(--erp-stat-gradient-blue);
  color: var(--erp-primary-600);
  font-size: 22px;
}

.wt-empty__title {
  color: var(--erp-slate-900);
  font-size: 14px;
  font-weight: 600;
  line-height: 22px;
}

.wt-empty__hint {
  color: var(--erp-slate-400);
  font-size: 12px;
}

@media (max-width: 1024px) {
  .wt-query__grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
