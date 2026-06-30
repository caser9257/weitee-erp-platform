<template>
  <div class="project-log-page">
    <ContentWrap class="search-card">
      <el-form
        ref="queryFormRef"
        :model="queryParams"
        class="search-form"
        :inline="true"
        label-width="72px"
      >
        <el-form-item label="任务" prop="taskId">
          <el-select
            v-model="queryParams.taskId"
            clearable
            filterable
            placeholder="全部任务"
            class="!w-240px"
            :loading="taskListLoading"
            :disabled="logListLoading || taskListLoading"
          >
            <el-option
              v-for="task in taskList"
              :key="task.id"
              :label="task.name"
              :value="task.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item class="search-actions">
          <el-button type="primary" :loading="logListLoading" @click="handleQuery">
            <Icon class="mr-5px" icon="ep:search" />
            查询
          </el-button>
          <el-button :disabled="logListLoading || taskListLoading" @click="resetQuery">
            <Icon class="mr-5px" icon="ep:refresh" />
            重置
          </el-button>
          <el-button :loading="logListLoading || taskListLoading" @click="handleRefresh">
            <Icon class="mr-5px" icon="ep:refresh-right" />
            刷新
          </el-button>
        </el-form-item>
      </el-form>
    </ContentWrap>

    <ContentWrap class="log-card">
      <div v-if="logListLoading && !hasLogData" class="state-panel">
        <el-skeleton :rows="4" animated />
      </div>

      <el-alert
        v-else-if="logListError"
        :title="logListError"
        type="error"
        show-icon
        :closable="false"
      >
        <template #default>
          <div class="mt-12px">
            <el-button type="primary" size="small" :loading="logListLoading" @click="loadLogList">
              重试
            </el-button>
          </div>
        </template>
      </el-alert>

      <template v-else>
        <el-empty v-if="!hasLogData" description="暂无操作日志" />

        <el-timeline v-else class="log-timeline">
          <el-timeline-item
            v-for="log in logList"
            :key="log.id"
            :timestamp="formatTime(log.createTime)"
            placement="top"
            :color="getTimelineColor(log.detail)"
          >
            <div class="log-item">
              <el-avatar
                :size="36"
                :src="log.userAvatar || undefined"
                class="log-avatar"
              >
                {{ getAvatarText(log.userName, log.userId) }}
              </el-avatar>

              <div class="log-body">
                <div class="log-header">
                  <div class="log-user">
                    <span class="log-user-name">{{ log.userName || `用户${log.userId}` }}</span>
                    <span class="log-user-id">#{{ log.userId }}</span>
                  </div>
                  <el-tag size="small" :type="getDetailTagType(log.detail)">
                    {{ log.detail }}
                  </el-tag>
                </div>

                <div class="log-meta">
                  <span>项目 {{ log.projectId }}</span>
                  <span v-if="log.taskId">任务 {{ log.taskId }}</span>
                  <span v-if="log.columnId">列表 {{ log.columnId }}</span>
                </div>

                <div v-if="hasRecord(log.record)" class="log-record">
                  <div class="log-record-title">变更记录</div>
                  <pre>{{ formatRecord(log.record) }}</pre>
                </div>
              </div>
            </div>
          </el-timeline-item>
        </el-timeline>
      </template>

      <div v-if="total > 0" class="table-footer">
        <Pagination
          v-model:page="queryParams.pageNo"
          v-model:limit="queryParams.pageSize"
          :total="total"
          @pagination="handlePageChange"
        />
      </div>
    </ContentWrap>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import dayjs from 'dayjs'
import { TaskApi, type TaskVO } from '@/api/project/task'
import * as LogApi from '@/api/project/log'

defineOptions({ name: 'ProjectLogDetail' })

const props = defineProps<{
  projectId?: number
}>()

const route = useRoute()
const queryFormRef = ref()
const taskListLoading = ref(false)
const logListLoading = ref(false)
const logListError = ref('')
const total = ref(0)
const taskList = ref<TaskVO[]>([])
const logList = ref<LogApi.LogResp[]>([])
const currentProjectId = computed(() => props.projectId ?? Number(route.params.id))

const queryParams = reactive({
  taskId: undefined as number | undefined,
  pageNo: 1,
  pageSize: 10
})

const hasLogData = computed(() => logList.value.length > 0)

const loadTaskList = async () => {
  if (!currentProjectId.value || Number.isNaN(currentProjectId.value)) {
    return
  }
  if (taskListLoading.value) {
    return
  }
  taskListLoading.value = true
  try {
    const data = await TaskApi.getTaskPage({
      projectId: currentProjectId.value,
      pageNo: 1,
      pageSize: 200
    })
    taskList.value = data.list || []
  } catch (error) {
    console.error('加载任务列表失败', error)
    taskList.value = []
  } finally {
    taskListLoading.value = false
  }
}

const loadLogList = async () => {
  if (!currentProjectId.value || Number.isNaN(currentProjectId.value)) {
    logListError.value = '项目编号无效'
    return
  }
  if (logListLoading.value) {
    return
  }
  logListLoading.value = true
  logListError.value = ''
  try {
    const data = await LogApi.getLogPage({
      projectId: currentProjectId.value,
      taskId: queryParams.taskId,
      pageNo: queryParams.pageNo,
      pageSize: queryParams.pageSize
    })
    logList.value = data.list || []
    total.value = data.total || 0
  } catch (error) {
    console.error('加载操作日志失败', error)
    logList.value = []
    total.value = 0
    logListError.value = '获取操作日志失败，请稍后重试'
  } finally {
    logListLoading.value = false
  }
}

const handleRefresh = async () => {
  if (logListLoading.value || taskListLoading.value) {
    return
  }
  await Promise.all([loadTaskList(), loadLogList()])
}

const handleQuery = async () => {
  queryParams.pageNo = 1
  await loadLogList()
}

const resetQuery = async () => {
  queryFormRef.value?.resetFields()
  queryParams.taskId = undefined
  queryParams.pageNo = 1
  await loadLogList()
}

const handlePageChange = async () => {
  await loadLogList()
}

const formatTime = (time?: string) => {
  if (!time) {
    return '-'
  }
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const getDetailTagType = (detail: string) => {
  if (!detail) {
    return 'info'
  }
  if (detail.includes('删除') || detail.includes('移除')) {
    return 'danger'
  }
  if (detail.includes('归档')) {
    return 'warning'
  }
  if (detail.includes('创建') || detail.includes('完成') || detail.includes('添加')) {
    return 'success'
  }
  return 'primary'
}

const getTimelineColor = (detail: string) => {
  if (!detail) {
    return '#94a3b8'
  }
  if (detail.includes('删除') || detail.includes('移除')) {
    return '#f43f5e'
  }
  if (detail.includes('归档')) {
    return '#f59e0b'
  }
  if (detail.includes('创建') || detail.includes('完成') || detail.includes('添加')) {
    return '#10b981'
  }
  return '#3b82f6'
}

const getAvatarText = (userName?: string, userId?: number) => {
  if (userName) {
    return userName.slice(0, 1)
  }
  return userId ? String(userId).slice(-1) : '人'
}

const hasRecord = (record: unknown) => {
  if (record === null || record === undefined) {
    return false
  }
  if (typeof record === 'string') {
    return record.trim().length > 0
  }
  if (Array.isArray(record)) {
    return record.length > 0
  }
  if (typeof record === 'object') {
    return Object.keys(record as Record<string, unknown>).length > 0
  }
  return true
}

const formatRecord = (record: unknown) => {
  if (typeof record === 'string') {
    return record
  }
  return JSON.stringify(record, null, 2)
}

watch(
  currentProjectId,
  async (projectId) => {
    if (!projectId || Number.isNaN(projectId)) {
      logListError.value = '项目编号无效'
      return
    }
    queryFormRef.value?.resetFields()
    queryParams.taskId = undefined
    queryParams.pageNo = 1
    taskList.value = []
    logList.value = []
    total.value = 0
    logListError.value = ''
    await Promise.all([loadTaskList(), loadLogList()])
  },
  { immediate: true }
)
</script>

<style scoped lang="scss">
.project-log-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.search-card,
.log-card {
  border-radius: 12px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: 8px 16px;
}

.search-actions {
  margin-left: auto;
}

.log-card {
  min-height: 480px;
}

.state-panel {
  padding: 16px 0;
}

.log-timeline {
  padding-top: 8px;
}

.log-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.log-avatar {
  flex: 0 0 auto;
  background: #e2e8f0;
  color: #475569;
  font-weight: 600;
}

.log-body {
  flex: 1;
  min-width: 0;
  padding: 12px 14px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  background: #fff;
}

.log-header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  margin-bottom: 8px;
}

.log-user {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.log-user-name {
  font-weight: 600;
  color: #0f172a;
}

.log-user-id {
  color: #94a3b8;
  font-size: 12px;
}

.log-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 12px;
  color: #64748b;
  font-size: 12px;
  margin-bottom: 10px;
}

.log-record {
  border-top: 1px dashed #e2e8f0;
  padding-top: 10px;
}

.log-record-title {
  margin-bottom: 6px;
  font-size: 12px;
  color: #475569;
  font-weight: 600;
}

.log-record pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 12px;
  line-height: 1.6;
  color: #334155;
}

.table-footer {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 1200px) {
  .search-form {
    justify-content: flex-start;
  }

  .search-actions {
    margin-left: 0;
  }
}

@media (max-width: 768px) {
  .log-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .log-item {
    gap: 10px;
  }

  .table-footer {
    justify-content: center;
  }
}
</style>
