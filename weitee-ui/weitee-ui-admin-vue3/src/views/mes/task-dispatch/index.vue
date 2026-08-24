<template>
  <section class="dispatch-hero">
    <div class="dispatch-hero__title">派工管理</div>
  </section>

  <ContentWrap class="dispatch-card dispatch-filter-card">
    <el-form ref="queryFormRef" :model="queryParams" label-position="top" class="dispatch-query">
      <div class="dispatch-query__grid">
        <el-form-item label="任务单号" prop="taskNo">
          <el-input
            v-model="queryParams.taskNo"
            clearable
            placeholder="请输入任务单号"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="生产工单号" prop="productionOrderNo">
          <el-input
            v-model="queryParams.productionOrderNo"
            clearable
            placeholder="请输入生产工单号"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="工作中心" prop="workCenterId">
          <el-select v-model="queryParams.workCenterId" clearable placeholder="请选择工作中心">
            <el-option
              v-for="item in workCenterOptions"
              :key="item.id"
              :label="`${item.centerCode} · ${item.centerName}`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="任务状态" prop="taskStatus">
          <el-select v-model="queryParams.taskStatus" clearable placeholder="请选择任务状态">
            <el-option
              v-for="item in taskStatusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
      </div>
      <div class="dispatch-query__footer">
        <el-button :disabled="listLoading" @click="resetQuery">
          <Icon icon="ep:refresh" class="mr-5px" />重置
        </el-button>
        <el-button type="primary" :loading="listLoading" @click="handleQuery">
          <Icon icon="ep:search" class="mr-5px" />查询
        </el-button>
      </div>
    </el-form>
  </ContentWrap>

  <ContentWrap class="dispatch-card dispatch-list-card">
    <div class="dispatch-toolbar">
      <div class="dispatch-toolbar__title">工序任务派工</div>
      <div class="dispatch-toolbar__status">
        <el-tag v-if="lookupLoading" type="info" effect="light">基础数据加载中</el-tag>
        <template v-else-if="lookupError">
          <el-tag type="danger" effect="light">基础数据加载失败</el-tag>
          <el-button link type="primary" @click="loadLookups">重新加载</el-button>
        </template>
      </div>
    </div>
    <div class="dispatch-table-wrap">
      <el-table v-loading="listLoading" :data="list" class="dispatch-table" stripe>
        <template #empty>
          <div class="dispatch-empty">
            <Icon icon="ep:user-filled" class="dispatch-empty__icon" />
            <div>{{ listError || '暂无工序任务' }}</div>
            <el-button v-if="listError" link type="primary" @click="getList">重新加载</el-button>
          </div>
        </template>
        <el-table-column label="任务信息" min-width="205">
          <template #default="{ row }">
            <div class="dispatch-primary dispatch-mono">{{ row.taskNo }}</div>
            <div class="dispatch-secondary">工单 {{ row.productionOrderNo || '-' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="工序与工作中心" min-width="205">
          <template #default="{ row }">
            <div class="dispatch-primary">{{ row.stepNo || '-' }} · {{ row.stepName || '-' }}</div>
            <div class="dispatch-secondary">
              {{ resolveWorkCenterName(row.workCenterId) }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="计划数量" min-width="100" align="right">
          <template #default="{ row }">
            <span class="dispatch-number">{{ formatCount(row.planQty) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="任务状态" min-width="105">
          <template #default="{ row }">
            <span class="dispatch-badge" :class="resolveTaskStatusClass(row.taskStatus)">
              {{ resolveTaskStatusLabel(row.taskStatus) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="当前派工" min-width="205">
          <template #default="{ row }">
            <template v-if="row.dispatchStatus === TASK_DISPATCH_STATUS.ASSIGNED">
              <div class="dispatch-primary">{{ resolveAssignee(row) }}</div>
              <div class="dispatch-secondary">{{ formatDateValue(row.dispatchTime) }}</div>
            </template>
            <span v-else class="dispatch-unassigned">待派工</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" :fixed="isCompactViewport ? false : 'right'" width="230" align="center">
          <template #default="{ row }">
            <div class="dispatch-actions">
              <el-button
                v-if="canAssign(row)"
                v-hasPermi="['mes:task-dispatch:update']"
                link
                type="primary"
                :loading="isRowLoading(row.taskId)"
                @click="openDispatchDialog(row, false)"
              >
                {{ row.dispatchStatus === TASK_DISPATCH_STATUS.ASSIGNED ? '改派' : '派工' }}
              </el-button>
              <el-button
                v-if="canRevoke(row)"
                v-hasPermi="['mes:task-dispatch:update']"
                link
                type="danger"
                :loading="isRowLoading(row.taskId)"
                @click="handleRevoke(row)"
              >
                撤销
              </el-button>
              <el-button
                link
                type="info"
                v-hasPermi="['mes:task-dispatch:query']"
                :loading="historyLoading && historyTaskId === row.taskId"
                @click="openHistory(row)"
              >
                历史
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <Pagination
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      :total="total"
      @pagination="getList"
    />
  </ContentWrap>

  <Dialog
    v-model="dispatchDialogVisible"
    :title="dispatchMode === 'reassign' ? '改派工序任务' : '派工工序任务'"
    width="520"
    @closed="resetDispatchForm"
  >
    <div class="dispatch-context">
      <div class="dispatch-context__task">{{ dispatchForm.taskNo }}</div>
      <div class="dispatch-context__step">
        {{ dispatchForm.stepNo || '-' }} · {{ dispatchForm.stepName || '-' }}
      </div>
    </div>
    <el-form label-width="86px" class="dispatch-form">
      <el-form-item label="班组">
        <el-select v-model="dispatchForm.teamId" clearable placeholder="请选择班组">
          <el-option v-for="item in deptOptions" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="执行人员">
        <el-select v-model="dispatchForm.workerUserId" clearable placeholder="请选择执行人员">
          <el-option
            v-for="item in userOptions"
            :key="item.id"
            :label="item.nickname"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="设备">
        <el-select v-model="dispatchForm.deviceId" clearable placeholder="请选择设备">
          <el-option
            v-for="item in deviceOptions"
            :key="item.id"
            :label="`${item.deviceCode} · ${item.deviceName}`"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="dispatchForm.remark" type="textarea" :rows="3" maxlength="255" show-word-limit />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dispatchDialogVisible = false">取消</el-button>
      <el-button
        type="primary"
        :loading="submitLoading"
        :disabled="!hasAssignee"
        @click="submitDispatch"
      >
        确定
      </el-button>
    </template>
  </Dialog>

  <el-drawer v-model="historyVisible" :with-header="false" size="480px" destroy-on-close @closed="resetHistory">
    <div class="history-drawer">
      <div class="history-drawer__header">
        <div>
          <div class="history-drawer__task">{{ historyTask?.taskNo || '-' }}</div>
          <div class="history-drawer__step">{{ historyTask?.stepName || '-' }}</div>
        </div>
        <el-button circle text @click="historyVisible = false">
          <Icon icon="ep:close" />
        </el-button>
      </div>
      <div v-loading="historyLoading" class="history-drawer__body">
        <div v-if="!historyLoading && historyError" class="history-empty history-empty--error">
          <div>{{ historyError }}</div>
          <el-button link type="primary" @click="retryHistory">重新加载</el-button>
        </div>
        <div v-else-if="!historyLoading && !historyList.length" class="history-empty">暂无派工记录</div>
        <div v-for="item in historyList" :key="item.id" class="history-item">
          <div class="history-item__top">
            <span class="dispatch-badge" :class="item.activeFlag ? 'dispatch-badge--success' : 'dispatch-badge--slate'">
              {{ item.activeFlag ? '当前派工' : '已撤销' }}
            </span>
            <span class="dispatch-secondary">{{ formatDateValue(item.dispatchTime) }}</span>
          </div>
          <div class="history-item__assignee">{{ resolveAssignee(item) }}</div>
          <div v-if="item.remark" class="history-item__remark">{{ item.remark }}</div>
          <div v-if="item.revokeTime" class="dispatch-secondary">撤销时间：{{ formatDateValue(item.revokeTime) }}</div>
        </div>
      </div>
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useWindowSize } from '@vueuse/core'
import { formatDate } from '@/utils/formatTime'
import { erpCountInputFormatter } from '@/utils'
import { DeptVO, getSimpleDeptList } from '@/api/system/dept'
import { SimpleUserVO, getSimpleUserList } from '@/api/system/user'
import { WorkCenterApi, WorkCenterSimpleVO } from '@/api/erp/manufacturing/work-center'
import { DeviceApi, DeviceVO } from '@/api/erp/manufacturing/device'
import {
  TASK_DISPATCH_STATUS,
  TaskDispatchApi,
  TaskDispatchVO
} from '@/api/mes/task-dispatch'
import { WORK_TASK_STATUS } from '@/api/mes/work-task'

defineOptions({ name: 'MesTaskDispatch' })

const message = useMessage()
const { width } = useWindowSize()
const queryFormRef = ref()
const list = ref<TaskDispatchVO[]>([])
const total = ref(0)
const listLoading = ref(false)
const listError = ref('')
const lookupLoading = ref(false)
const lookupError = ref('')
const submitLoading = ref(false)
const historyLoading = ref(false)
const historyError = ref('')
const historyVisible = ref(false)
const dispatchDialogVisible = ref(false)
const historyTaskId = ref<number>()
const dispatchMode = ref<'assign' | 'reassign'>('assign')
const rowLoading = ref(new Set<number>())
const historyList = ref<TaskDispatchVO[]>([])
const historyTask = ref<TaskDispatchVO>()

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  taskNo: undefined as string | undefined,
  productionOrderNo: undefined as string | undefined,
  workCenterId: undefined as number | undefined,
  taskStatus: undefined as number | undefined
})

const dispatchForm = reactive<TaskDispatchVO & { remark: string }>({
  taskId: 0,
  taskNo: '',
  productionOrderId: 0,
  orderStepId: 0,
  remark: ''
})

const workCenterOptions = ref<WorkCenterSimpleVO[]>([])
const deptOptions = ref<DeptVO[]>([])
const userOptions = ref<SimpleUserVO[]>([])
const deviceOptions = ref<DeviceVO[]>([])

const taskStatusOptions = [
  { label: '待排程', value: WORK_TASK_STATUS.WAIT_SCHEDULE },
  { label: '已排程', value: WORK_TASK_STATUS.SCHEDULED },
  { label: '进行中', value: WORK_TASK_STATUS.PROCESSING },
  { label: '已完成', value: WORK_TASK_STATUS.FINISHED },
  { label: '已取消', value: WORK_TASK_STATUS.CANCELED }
]

const isCompactViewport = computed(() => width.value < 768)

const hasAssignee = computed(
  () => !!(dispatchForm.teamId || dispatchForm.workerUserId || dispatchForm.deviceId)
)

const getList = async () => {
  if (listLoading.value) return
  listLoading.value = true
  listError.value = ''
  try {
    const data = await TaskDispatchApi.getPage(queryParams)
    list.value = data.list || []
    total.value = data.total || 0
  } catch {
    list.value = []
    total.value = 0
    listError.value = '派工列表加载失败'
  } finally {
    listLoading.value = false
  }
}

const loadLookups = async () => {
  if (lookupLoading.value) return
  lookupLoading.value = true
  lookupError.value = ''
  const [workCenters, depts, users, devices] = await Promise.allSettled([
    WorkCenterApi.getWorkCenterSimpleList(),
    getSimpleDeptList(),
    getSimpleUserList(),
    DeviceApi.getDevicePage({ pageNo: 1, pageSize: 100, deviceStatus: 1 })
  ])
  workCenterOptions.value = workCenters.status === 'fulfilled' ? workCenters.value : []
  deptOptions.value = depts.status === 'fulfilled' ? depts.value : []
  userOptions.value = users.status === 'fulfilled' ? users.value : []
  deviceOptions.value = devices.status === 'fulfilled' ? devices.value.list || [] : []
  lookupError.value = [workCenters, depts, users, devices].some((item) => item.status === 'rejected')
    ? '基础数据加载失败'
    : ''
  lookupLoading.value = false
}

const handleQuery = () => {
  queryParams.pageNo = 1
  void getList()
}

const resetQuery = () => {
  queryFormRef.value?.resetFields()
  handleQuery()
}

const canDispatch = (row: TaskDispatchVO) =>
  row.taskStatus === WORK_TASK_STATUS.WAIT_SCHEDULE || row.taskStatus === WORK_TASK_STATUS.SCHEDULED

const canAssign = (row: TaskDispatchVO) => canDispatch(row)

const canRevoke = (row: TaskDispatchVO) =>
  canDispatch(row) && row.dispatchStatus === TASK_DISPATCH_STATUS.ASSIGNED

const isRowLoading = (taskId: number) => rowLoading.value.has(taskId)

const setRowLoading = (taskId: number, loading: boolean) => {
  const next = new Set(rowLoading.value)
  loading ? next.add(taskId) : next.delete(taskId)
  rowLoading.value = next
}

const openDispatchDialog = (row: TaskDispatchVO, reassign: boolean) => {
  dispatchMode.value = reassign || row.dispatchStatus === TASK_DISPATCH_STATUS.ASSIGNED ? 'reassign' : 'assign'
  Object.assign(dispatchForm, {
    taskId: row.taskId,
    taskNo: row.taskNo,
    stepNo: row.stepNo,
    stepName: row.stepName,
    teamId: row.teamId,
    workerUserId: row.workerUserId,
    deviceId: row.deviceId,
    remark: row.remark || ''
  })
  dispatchDialogVisible.value = true
}

const resetDispatchForm = () => {
  Object.assign(dispatchForm, {
    taskId: 0,
    taskNo: '',
    stepNo: undefined,
    stepName: undefined,
    teamId: undefined,
    workerUserId: undefined,
    deviceId: undefined,
    remark: ''
  })
}

const submitDispatch = async () => {
  if (!dispatchForm.taskId || !hasAssignee.value || submitLoading.value) return
  submitLoading.value = true
  try {
    const data = {
      taskId: dispatchForm.taskId,
      teamId: dispatchForm.teamId,
      workerUserId: dispatchForm.workerUserId,
      deviceId: dispatchForm.deviceId,
      remark: dispatchForm.remark?.trim()
    }
    if (dispatchMode.value === 'reassign') {
      await TaskDispatchApi.reassign(data)
      message.success('改派成功')
    } else {
      await TaskDispatchApi.assign(data)
      message.success('派工成功')
    }
    dispatchDialogVisible.value = false
    await getList()
  } finally {
    submitLoading.value = false
  }
}

const handleRevoke = async (row: TaskDispatchVO) => {
  if (!canRevoke(row) || isRowLoading(row.taskId)) return
  try {
    await message.confirm(`确定撤销任务「${row.taskNo}」的当前派工吗？`)
  } catch {
    return
  }
  setRowLoading(row.taskId, true)
  try {
    await TaskDispatchApi.revoke(row.taskId)
    message.success('撤销派工成功')
    await getList()
  } finally {
    setRowLoading(row.taskId, false)
  }
}

const openHistory = async (row: TaskDispatchVO) => {
  if (historyLoading.value) return
  historyTaskId.value = row.taskId
  historyTask.value = row
  historyList.value = []
  historyError.value = ''
  historyVisible.value = true
  historyLoading.value = true
  try {
    historyList.value = (await TaskDispatchApi.getHistory(row.taskId)) || []
  } catch {
    historyError.value = '派工历史加载失败'
  } finally {
    historyLoading.value = false
  }
}

const retryHistory = () => {
  if (historyTask.value) {
    void openHistory(historyTask.value)
  }
}

const resetHistory = () => {
  historyTaskId.value = undefined
  historyTask.value = undefined
  historyList.value = []
  historyError.value = ''
}

const resolveWorkCenterName = (id?: number) => {
  const item = workCenterOptions.value.find((option) => option.id === id)
  return item ? `${item.centerCode} · ${item.centerName}` : id ? `工作中心 ${id}` : '-'
}

const resolveAssignee = (row: TaskDispatchVO) => {
  const names = [
    row.teamId ? deptOptions.value.find((item) => item.id === row.teamId)?.name : undefined,
    row.workerUserId ? userOptions.value.find((item) => item.id === row.workerUserId)?.nickname : undefined,
    row.deviceId ? deviceOptions.value.find((item) => item.id === row.deviceId)?.deviceName : undefined
  ].filter(Boolean)
  return names.length ? names.join(' / ') : '未识别对象'
}

const resolveTaskStatusLabel = (status?: number) =>
  taskStatusOptions.find((item) => item.value === status)?.label || '-'

const resolveTaskStatusClass = (status?: number) => {
  if (status === WORK_TASK_STATUS.FINISHED) return 'dispatch-badge--success'
  if (status === WORK_TASK_STATUS.PROCESSING) return 'dispatch-badge--warning'
  if (status === WORK_TASK_STATUS.CANCELED) return 'dispatch-badge--slate'
  if (status === WORK_TASK_STATUS.SCHEDULED) return 'dispatch-badge--primary'
  return 'dispatch-badge--slate'
}

const formatCount = (value?: number) => erpCountInputFormatter(value || 0)
const formatDateValue = (value?: string | Date | number) => (value ? formatDate(value, 'YYYY-MM-DD HH:mm') : '-')

onMounted(() => {
  void getList()
  void loadLookups()
})
</script>

<style scoped>
.dispatch-hero {
  margin-bottom: 16px;
}

.dispatch-hero__title {
  color: var(--erp-slate-900);
  font-size: 28px;
  font-weight: 800;
  line-height: 36px;
}

.dispatch-card {
  border: 1px solid var(--erp-slate-200);
  border-radius: 12px;
  background: var(--erp-surface-white);
  box-shadow: var(--erp-shadow-sm);
}

.dispatch-query__grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.dispatch-query :deep(.el-form-item) {
  margin-bottom: 0;
}

.dispatch-query :deep(.el-form-item__label) {
  padding-bottom: 6px;
  color: var(--erp-slate-600);
  font-size: 12px;
  font-weight: 600;
}

.dispatch-query :deep(.el-input__wrapper),
.dispatch-query :deep(.el-select__wrapper) {
  min-height: 38px;
  border: 1px solid var(--erp-slate-200);
  border-radius: 8px;
  background: var(--erp-slate-50);
  box-shadow: none;
}

.dispatch-query__footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 16px;
}

.dispatch-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--erp-slate-200);
}

.dispatch-toolbar__status {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.dispatch-toolbar__title {
  color: var(--erp-slate-900);
  font-size: 16px;
  font-weight: 800;
}

.dispatch-table {
  width: 100%;
}

.dispatch-table-wrap {
  overflow-x: auto;
}

.dispatch-table :deep(.el-table__header-wrapper th) {
  background: var(--erp-slate-50);
  color: var(--erp-slate-600);
  font-size: 11px;
  font-weight: 700;
}

.dispatch-table :deep(.el-table__row td) {
  padding-top: 14px;
  padding-bottom: 14px;
  vertical-align: top;
}

.dispatch-primary,
.dispatch-secondary {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dispatch-primary {
  color: var(--erp-slate-900);
  font-weight: 700;
  line-height: 22px;
}

.dispatch-secondary {
  margin-top: 4px;
  color: var(--erp-slate-500);
  font-size: 12px;
  line-height: 18px;
}

.dispatch-mono,
.dispatch-number {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
}

.dispatch-number {
  color: var(--erp-slate-700);
}

.dispatch-badge {
  display: inline-flex;
  align-items: center;
  min-height: 22px;
  padding: 2px 9px;
  border: 1px solid transparent;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
  line-height: 16px;
  white-space: nowrap;
}

.dispatch-badge--slate {
  border-color: var(--erp-slate-200);
  background: var(--erp-slate-100);
  color: var(--erp-slate-600);
}

.dispatch-badge--primary {
  border-color: var(--erp-primary-200);
  background: var(--erp-primary-50);
  color: var(--erp-primary-600);
}

.dispatch-badge--success {
  border-color: var(--erp-success-200);
  background: var(--erp-success-50);
  color: var(--erp-success-600);
}

.dispatch-badge--warning {
  border-color: var(--erp-warning-200);
  background: var(--erp-warning-50);
  color: var(--erp-warning-600);
}

.dispatch-unassigned {
  color: var(--erp-warning-600);
  font-weight: 700;
}

.dispatch-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  flex-wrap: wrap;
}

.dispatch-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 150px;
  color: var(--erp-slate-500);
}

.dispatch-empty__icon {
  color: var(--erp-slate-400);
  font-size: 20px;
}

.dispatch-context {
  margin-bottom: 20px;
  padding: 16px;
  border-radius: 8px;
  background: var(--erp-slate-800);
  color: var(--erp-surface-white);
}

.dispatch-context__task {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 16px;
  font-weight: 800;
}

.dispatch-context__step {
  margin-top: 6px;
  color: var(--erp-slate-200);
  font-size: 13px;
}

.dispatch-form :deep(.el-form-item__content),
.dispatch-form :deep(.el-select),
.dispatch-form :deep(.el-input) {
  width: 100%;
}

.history-drawer {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.history-drawer__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 20px;
  background: var(--erp-slate-800);
  color: var(--erp-surface-white);
}

.history-drawer__task {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 18px;
  font-weight: 800;
}

.history-drawer__step {
  margin-top: 6px;
  color: var(--erp-slate-200);
  font-size: 13px;
}

.history-drawer__body {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.history-empty {
  padding: 52px 0;
  color: var(--erp-slate-500);
  text-align: center;
}

.history-empty--error {
  color: var(--erp-danger-600);
}

.history-item {
  padding: 14px 0;
  border-bottom: 1px solid var(--erp-slate-200);
}

.history-item__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.history-item__assignee {
  margin-top: 10px;
  color: var(--erp-slate-900);
  font-weight: 700;
}

.history-item__remark {
  margin-top: 8px;
  color: var(--erp-slate-600);
  font-size: 12px;
  line-height: 18px;
  overflow-wrap: anywhere;
}

@media (max-width: 1200px) {
  .dispatch-query__grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (width <= 767px) {
  .dispatch-table {
    min-width: 1050px;
  }
}

@media (max-width: 640px) {
  .dispatch-query__grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .dispatch-toolbar,
  .dispatch-query__footer {
    align-items: stretch;
    flex-direction: column;
  }

  .dispatch-query__footer :deep(.el-button) {
    width: 100%;
    margin-left: 0;
  }
}
</style>
