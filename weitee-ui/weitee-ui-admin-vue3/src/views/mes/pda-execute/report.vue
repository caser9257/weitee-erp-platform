<template>
  <section class="execution-page">
    <div class="execution-page__title">现场执行</div>

    <ContentWrap class="execution-card execution-query-card">
      <el-form class="execution-query" @submit.prevent>
        <el-form-item label="任务号">
          <el-input
            v-model="taskNo"
            clearable
            autofocus
            placeholder="请输入或扫描任务号"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <div class="execution-query__actions">
          <el-button :disabled="queryLoading" @click="clearTask">
            <Icon icon="ep:refresh-left" class="mr-5px" />清空
          </el-button>
          <el-button
            v-hasPermi="['mes:task-execution:query']"
            type="primary"
            :loading="queryLoading"
            :disabled="!taskNo.trim() || isActionBusy"
            @click="handleQuery"
          >
            <Icon icon="ep:search" class="mr-5px" />查询任务
          </el-button>
        </div>
      </el-form>
    </ContentWrap>

    <ContentWrap v-if="currentTask" class="execution-card execution-context-card">
      <div class="execution-context">
        <div class="execution-context__main">
          <div class="execution-context__task">{{ currentTask.taskNo }}</div>
          <div class="execution-context__step">
            {{ currentTask.stepNo || '-' }} · {{ currentTask.stepName || currentTask.stepCode || '-' }}
          </div>
        </div>
        <span class="execution-status" :class="resolveTaskStatusClass(currentTask.taskStatus)">
          {{ resolveTaskStatusLabel(currentTask.taskStatus) }}
        </span>
      </div>

      <div class="execution-facts">
        <div class="execution-fact">
          <span class="execution-fact__label">生产工单</span>
          <span class="execution-fact__value execution-mono">{{ currentTask.productionOrderNo || '-' }}</span>
        </div>
        <div class="execution-fact">
          <span class="execution-fact__label">计划数量</span>
          <span class="execution-fact__value execution-number">{{ formatCount(currentTask.planQty) }}</span>
        </div>
        <div class="execution-fact">
          <span class="execution-fact__label">已报数量</span>
          <span class="execution-fact__value execution-number">{{ formatCount(currentTask.reportedQty) }}</span>
        </div>
        <div class="execution-fact">
          <span class="execution-fact__label">合格数量</span>
          <span class="execution-fact__value execution-number">{{ formatCount(currentTask.qualifiedQty) }}</span>
        </div>
        <div class="execution-fact">
          <span class="execution-fact__label">报废数量</span>
          <span class="execution-fact__value execution-number">{{ formatCount(currentTask.scrapQty) }}</span>
        </div>
        <div class="execution-fact">
          <span class="execution-fact__label">工序状态</span>
          <span class="execution-status" :class="resolveStepStatusClass(currentTask.erpStepStatus)">
            {{ resolveStepStatusLabel(currentTask.erpStepStatus) }}
          </span>
        </div>
      </div>

      <div class="execution-assignment">
        <div class="execution-assignment__title">当前派工</div>
        <div class="execution-assignment__items">
          <span>班组：{{ currentTask.teamId || '-' }}</span>
          <span>执行人员：{{ currentTask.workerUserId || '-' }}</span>
          <span>设备：{{ currentTask.deviceId || '-' }}</span>
        </div>
      </div>

      <div class="execution-actions">
        <el-button
          v-hasPermi="['mes:task-execution:update']"
          type="primary"
          :loading="startLoading"
          :disabled="!canStart"
          @click="handleStart"
        >
          <Icon icon="ep:video-play" class="mr-5px" />开工
        </el-button>
        <el-button
          v-hasPermi="['mes:task-execution:update']"
          :loading="pauseLoading"
          :disabled="!canPause"
          @click="handlePause"
        >
          <Icon icon="ep:video-pause" class="mr-5px" />暂停
        </el-button>
        <el-button
          v-hasPermi="['mes:task-execution:update']"
          :loading="resumeLoading"
          :disabled="!canResume"
          @click="handleResume"
        >
          <Icon icon="ep:refresh" class="mr-5px" />恢复
        </el-button>
        <el-button
          v-hasPermi="['mes:task-execution:update']"
          type="success"
          :loading="finishLoading"
          :disabled="!canFinish"
          @click="handleFinish"
        >
          <Icon icon="ep:finished" class="mr-5px" />完工
        </el-button>
      </div>
    </ContentWrap>

    <ContentWrap v-if="currentTask" class="execution-card execution-report-card">
      <div class="execution-section-title">报工</div>
      <el-form class="execution-report-form" label-position="top" @submit.prevent>
        <el-form-item label="报工数量">
          <el-input-number v-model="reportForm.reportedQty" :min="0" :precision="6" controls-position="right" />
        </el-form-item>
        <el-form-item label="合格数量">
          <el-input-number v-model="reportForm.qualifiedQty" :min="0" :precision="6" controls-position="right" />
        </el-form-item>
        <el-form-item label="报废数量">
          <el-input-number v-model="reportForm.scrapQty" :min="0" :precision="6" controls-position="right" />
        </el-form-item>
        <el-form-item label="工时">
          <el-input-number v-model="reportForm.workHour" :min="0" :precision="2" controls-position="right" />
        </el-form-item>
        <el-form-item label="批次号">
          <el-input v-model="reportForm.batchNo" maxlength="64" clearable />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="reportForm.remark" maxlength="255" clearable />
        </el-form-item>
      </el-form>
      <div v-if="reportError" class="execution-form-error">{{ reportError }}</div>
      <div class="execution-report-actions">
        <el-button
          v-hasPermi="['mes:task-execution:report']"
          type="primary"
          :loading="reportLoading"
          :disabled="!canReport"
          @click="handleReport"
        >
          <Icon icon="ep:upload" class="mr-5px" />提交报工
        </el-button>
      </div>
    </ContentWrap>

    <ContentWrap v-else-if="!queryLoading" class="execution-card execution-empty-card">
      <Icon icon="ep:finished" class="execution-empty-card__icon" />
      <span>{{ taskError || '请输入任务号' }}</span>
      <el-button v-if="taskError" link type="primary" @click="handleQuery">重新加载</el-button>
    </ContentWrap>

    <div v-if="queryLoading" class="execution-loading">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>任务加载中</span>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import { erpCountInputFormatter } from '@/utils'
import {
  MES_STEP_STATUS,
  TaskExecutionApi,
  TaskExecutionReportReqVO,
  TaskExecutionVO
} from '@/api/mes/task-execution'
import { WORK_TASK_STATUS } from '@/api/mes/work-task'

defineOptions({ name: 'MesPdaReport' })

const message = useMessage()
const taskNo = ref('')
const currentTask = ref<TaskExecutionVO>()
const taskError = ref('')
const queryLoading = ref(false)
const startLoading = ref(false)
const pauseLoading = ref(false)
const resumeLoading = ref(false)
const finishLoading = ref(false)
const reportLoading = ref(false)
const reportError = ref('')

const reportForm = reactive<TaskExecutionReportReqVO>({
  taskNo: '',
  reportedQty: 0,
  qualifiedQty: 0,
  scrapQty: 0,
  workHour: undefined,
  batchNo: '',
  remark: ''
})

const isTerminal = computed(
  () => currentTask.value?.taskStatus === WORK_TASK_STATUS.FINISHED || currentTask.value?.taskStatus === WORK_TASK_STATUS.CANCELED
)
const canStart = computed(
  () => !!currentTask.value && !isTerminal.value && currentTask.value.erpStepStatus === MES_STEP_STATUS.WAIT && !isActionBusy.value
)
const canPause = computed(
  () => !!currentTask.value && !isTerminal.value && currentTask.value.erpStepStatus === MES_STEP_STATUS.PROCESSING && !isActionBusy.value
)
const canResume = computed(
  () => !!currentTask.value && !isTerminal.value && currentTask.value.erpStepStatus === MES_STEP_STATUS.PAUSED && !isActionBusy.value
)
const canFinish = computed(
  () => !!currentTask.value && !isTerminal.value && currentTask.value.erpStepStatus === MES_STEP_STATUS.PROCESSING && !isActionBusy.value
)
const reportQtyValid = computed(() => {
  const reported = Number(reportForm.reportedQty || 0)
  const qualified = Number(reportForm.qualifiedQty || 0)
  const scrap = Number(reportForm.scrapQty || 0)
  return reported > 0 && Math.abs(qualified + scrap - reported) < 0.000001
})
const isActionBusy = computed(
  () => startLoading.value || pauseLoading.value || resumeLoading.value || finishLoading.value || reportLoading.value
)
const canReport = computed(
  () => !!currentTask.value && !isTerminal.value && currentTask.value.erpStepStatus === MES_STEP_STATUS.PROCESSING && !isActionBusy.value
)

const handleQuery = async (allowBusy = false) => {
  const value = taskNo.value.trim()
  if (!value || queryLoading.value || (!allowBusy && isActionBusy.value)) return
  queryLoading.value = true
  taskError.value = ''
  reportError.value = ''
  currentTask.value = undefined
  try {
    const data = await TaskExecutionApi.getTask(value)
    currentTask.value = data
    reportForm.taskNo = data.taskNo
    resetReportForm()
  } catch (error) {
    taskError.value = resolveErrorMessage(error, '任务加载失败')
  } finally {
    queryLoading.value = false
  }
}

const clearTask = () => {
  if (queryLoading.value || isActionBusy.value) return
  taskNo.value = ''
  currentTask.value = undefined
  taskError.value = ''
  reportError.value = ''
  resetReportForm()
}

const runTaskAction = async (
  loading: typeof startLoading,
  action: (taskNo: string) => Promise<unknown>,
  successText: string
) => {
  if (!currentTask.value || loading.value) return
  loading.value = true
  taskError.value = ''
  try {
    await action(currentTask.value.taskNo)
    message.success(successText)
    await handleQuery(true)
  } catch (error) {
    taskError.value = resolveErrorMessage(error, '操作失败')
    message.error(taskError.value)
  } finally {
    loading.value = false
  }
}

const handleStart = () => runTaskAction(startLoading, TaskExecutionApi.start, '开工成功')
const handlePause = () => runTaskAction(pauseLoading, TaskExecutionApi.pause, '暂停成功')
const handleResume = () => runTaskAction(resumeLoading, TaskExecutionApi.resume, '恢复成功')
const handleFinish = () => runTaskAction(finishLoading, TaskExecutionApi.finish, '完工成功')

const handleReport = async () => {
  if (!currentTask.value || !canReport.value || reportLoading.value) return
  reportError.value = ''
  if (!reportQtyValid.value) {
    reportError.value = '报工数量必须等于合格数量与报废数量之和'
    return
  }
  reportLoading.value = true
  const payload: TaskExecutionReportReqVO = {
    ...reportForm,
    taskNo: currentTask.value.taskNo,
    batchNo: reportForm.batchNo?.trim(),
    remark: reportForm.remark?.trim()
  }
  try {
    await TaskExecutionApi.report(payload)
    message.success('报工成功')
    resetReportForm()
    await handleQuery()
  } catch (error) {
    reportError.value = resolveErrorMessage(error, '报工失败')
    message.error(reportError.value)
  } finally {
    reportLoading.value = false
  }
}

const resetReportForm = () => {
  reportForm.taskNo = currentTask.value?.taskNo || ''
  reportForm.reportedQty = 0
  reportForm.qualifiedQty = 0
  reportForm.scrapQty = 0
  reportForm.workHour = undefined
  reportForm.batchNo = ''
  reportForm.remark = ''
}

const resolveErrorMessage = (error: any, fallback: string) =>
  error?.msg || error?.response?.data?.msg || error?.message || fallback
const formatCount = (value?: number) => erpCountInputFormatter(value || 0)

const resolveTaskStatusLabel = (status?: number) => {
  const labels: Record<number, string> = {
    [WORK_TASK_STATUS.WAIT_SCHEDULE]: '待排程',
    [WORK_TASK_STATUS.SCHEDULED]: '已排程',
    [WORK_TASK_STATUS.PROCESSING]: '进行中',
    [WORK_TASK_STATUS.FINISHED]: '已完成',
    [WORK_TASK_STATUS.CANCELED]: '已取消'
  }
  return labels[status || 0] || '-'
}

const resolveTaskStatusClass = (status?: number) => {
  if (status === WORK_TASK_STATUS.FINISHED) return 'execution-status--success'
  if (status === WORK_TASK_STATUS.PROCESSING) return 'execution-status--warning'
  if (status === WORK_TASK_STATUS.CANCELED) return 'execution-status--slate'
  return 'execution-status--primary'
}

const resolveStepStatusLabel = (status?: number) => {
  const labels: Record<number, string> = {
    [MES_STEP_STATUS.WAIT]: '待开工',
    [MES_STEP_STATUS.PROCESSING]: '进行中',
    [MES_STEP_STATUS.FINISHED]: '已完成',
    [MES_STEP_STATUS.PAUSED]: '已暂停'
  }
  return labels[status || 0] || '-'
}

const resolveStepStatusClass = (status?: number) => {
  if (status === MES_STEP_STATUS.FINISHED) return 'execution-status--success'
  if (status === MES_STEP_STATUS.PROCESSING) return 'execution-status--warning'
  if (status === MES_STEP_STATUS.PAUSED) return 'execution-status--primary'
  return 'execution-status--slate'
}
</script>

<style scoped>
.execution-page {
  min-height: 100%;
  padding-bottom: 24px;
}

.execution-page__title {
  margin-bottom: 16px;
  color: var(--erp-slate-900);
  font-size: 28px;
  font-weight: 800;
  line-height: 36px;
}

.execution-card {
  border: 1px solid var(--erp-slate-200);
  border-radius: 12px;
  background: var(--erp-surface-white);
  box-shadow: var(--erp-shadow-sm);
}

.execution-query {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: end;
  gap: 16px;
}

.execution-query :deep(.el-form-item) {
  margin-bottom: 0;
}

.execution-query :deep(.el-input__wrapper),
.execution-report-form :deep(.el-input__wrapper),
.execution-report-form :deep(.el-input-number),
.execution-report-form :deep(.el-input-number .el-input__wrapper) {
  width: 100%;
  min-height: 38px;
  border-color: var(--erp-slate-200);
  border-radius: 8px;
  background: var(--erp-slate-50);
  box-shadow: none;
}

.execution-query__actions,
.execution-actions,
.execution-report-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.execution-context-card,
.execution-report-card {
  margin-top: 16px;
}

.execution-context {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 20px;
  border-radius: 8px;
  background: var(--erp-slate-800);
  color: var(--erp-surface-white);
}

.execution-context__task {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 20px;
  font-weight: 800;
}

.execution-context__step {
  margin-top: 6px;
  color: var(--erp-slate-200);
  font-size: 14px;
}

.execution-status {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  padding: 2px 10px;
  border: 1px solid transparent;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  white-space: nowrap;
}

.execution-status--primary {
  border-color: var(--erp-primary-200);
  background: var(--erp-primary-50);
  color: var(--erp-primary-600);
}

.execution-status--success {
  border-color: var(--erp-success-200);
  background: var(--erp-success-50);
  color: var(--erp-success-600);
}

.execution-status--warning {
  border-color: var(--erp-warning-200);
  background: var(--erp-warning-50);
  color: var(--erp-warning-600);
}

.execution-status--slate {
  border-color: var(--erp-slate-200);
  background: var(--erp-slate-100);
  color: var(--erp-slate-600);
}

.execution-facts {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 12px;
  padding: 20px 0;
}

.execution-fact {
  min-width: 0;
  padding: 12px 16px;
  border-right: 1px solid var(--erp-slate-200);
}

.execution-fact:last-child {
  border-right: 0;
}

.execution-fact__label {
  display: block;
  color: var(--erp-slate-500);
  font-size: 12px;
}

.execution-fact__value {
  display: block;
  margin-top: 6px;
  overflow: hidden;
  color: var(--erp-slate-900);
  font-size: 16px;
  font-weight: 800;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.execution-mono,
.execution-number {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
}

.execution-assignment {
  padding: 14px 16px;
  border-top: 1px solid var(--erp-slate-200);
  border-bottom: 1px solid var(--erp-slate-200);
}

.execution-assignment__title,
.execution-section-title {
  color: var(--erp-slate-900);
  font-size: 16px;
  font-weight: 800;
}

.execution-assignment__items {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-top: 8px;
  color: var(--erp-slate-600);
  font-size: 13px;
}

.execution-actions {
  padding-top: 16px;
}

.execution-report-card {
  padding: 20px;
}

.execution-report-form {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 0 16px;
  margin-top: 16px;
}

.execution-report-form :deep(.el-form-item) {
  margin-bottom: 16px;
}

.execution-report-actions {
  justify-content: flex-end;
}

.execution-form-error {
  margin-bottom: 12px;
  color: var(--erp-danger-600);
  font-size: 13px;
}

.execution-empty-card,
.execution-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 180px;
  color: var(--erp-slate-500);
}

.execution-empty-card {
  margin-top: 16px;
}

.execution-empty-card__icon {
  color: var(--erp-slate-400);
  font-size: 20px;
}

.execution-loading {
  min-height: 160px;
}

@media (max-width: 1200px) {
  .execution-facts {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .execution-fact:nth-child(3n) {
    border-right: 0;
  }
}

@media (max-width: 767px) {
  .execution-query {
    grid-template-columns: minmax(0, 1fr);
  }

  .execution-query__actions,
  .execution-actions,
  .execution-report-actions {
    width: 100%;
  }

  .execution-query__actions .el-button,
  .execution-actions .el-button,
  .execution-report-actions .el-button {
    flex: 1 1 140px;
  }

  .execution-facts,
  .execution-report-form {
    grid-template-columns: minmax(0, 1fr);
  }

  .execution-fact {
    border-right: 0;
    border-bottom: 1px solid var(--erp-slate-200);
  }

  .execution-fact:last-child {
    border-bottom: 0;
  }
}
</style>
