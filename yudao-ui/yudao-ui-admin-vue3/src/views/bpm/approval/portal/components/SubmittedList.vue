<template>
  <div class="submitted-list">
    <el-table v-loading="loading" :data="list" :stripe="true">
      <el-table-column label="流程名称" prop="name" min-width="200">
        <template #default="{ row }">
          <el-link type="primary" @click="handleDetail(row)">
            {{ row.name }}
          </el-link>
        </template>
      </el-table-column>
      <el-table-column label="当前节点" width="150">
        <template #default="{ row }">
          {{ row.tasks?.[0]?.name || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="当前处理人" width="120">
        <template #default="{ row }">
          {{ row.tasks?.[0]?.assigneeUser?.nickname || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="状态" prop="status" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)" effect="light">
            {{ getStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="发起时间" width="170">
        <template #default="{ row }">
          {{ formatTime(row.startTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="150" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="handleDetail(scope.row)">
            详情
          </el-button>
          <el-button
            v-if="scope.row.status === 1"
            :loading="cancellingProcessInstanceId === (scope.row.id || scope.row.processInstanceId)"
            :disabled="!!cancellingProcessInstanceId"
            link
            type="danger"
            @click="handleCancel(scope.row)"
          >
            撤回
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
    <!-- 详情弹窗 -->
    <DetailDialog ref="detailDialogRef" />
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessageBox } from 'element-plus'
import { formatDate } from '@/utils/formatTime'
import { useMessage } from '@/hooks/web/useMessage'
import * as ApprovalPortalApi from '@/api/bpm/approval/portal'
import * as ProcessInstanceApi from '@/api/bpm/processInstance'
import {
  BPM_MODULE_UNAVAILABLE_TEXT,
  getErrorMessage,
  isBpmModuleUnavailableError
} from '../moduleGuard'
import DetailDialog from './DetailDialog.vue'

defineOptions({ name: 'SubmittedList' })
const emit = defineEmits<{
  (e: 'module-unavailable', message?: string): void
}>()

const message = useMessage()

const formatTime = (time: any) => {
  if (!time) return '-'
  // 兼容时间戳数字和 ISO 字符串
  const date = typeof time === 'number' ? new Date(time) : new Date(time)
  return formatDate(date, 'YYYY-MM-DD HH:mm:ss')
}

const loading = ref(true)
const list = ref<any[]>([])
const total = ref(0)
const cancellingProcessInstanceId = ref<string>('')
const queryParams = reactive({ pageNo: 1, pageSize: 10 })
const detailDialogRef = ref()

const getStatusType = (status: number) => {
  const map: Record<number, string> = { 1: 'warning', 2: 'success', 3: 'danger', 4: 'info' }
  return map[status] || 'info'
}

const getStatusLabel = (status: number) => {
  const map: Record<number, string> = { 1: '审批中', 2: '已通过', 3: '已拒绝', 4: '已撤回' }
  return map[status] || '未知'
}

const getList = async () => {
  loading.value = true
  try {
    const data = await ApprovalPortalApi.getApprovalMyPage(queryParams)
    console.log('[SubmittedList] getList response:', data)
    // 确保 data.list 存在
    list.value = data?.list || []
    total.value = data?.total || 0
    console.log('[SubmittedList] list.value:', list.value)
  } catch (error) {
    list.value = []
    total.value = 0
    if (isBpmModuleUnavailableError(error)) {
      emit('module-unavailable', BPM_MODULE_UNAVAILABLE_TEXT)
      return
    }
    message.error(getErrorMessage(error) || '我发起的列表加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleDetail = (row: any) => {
  // 我发起的列表返回的是 BpmProcessInstanceRespVO，流程实例 ID 在 id 字段
  detailDialogRef.value.open(row.id || row.processInstanceId)
}

const handleCancel = async (row: any) => {
  const processInstanceId = row.id || row.processInstanceId
  if (!processInstanceId || cancellingProcessInstanceId.value) {
    return
  }
  try {
    await ElMessageBox.confirm('确认撤回该审批？', '提示', { type: 'warning' })
    cancellingProcessInstanceId.value = processInstanceId
    await ProcessInstanceApi.cancelProcessInstanceByStartUser(processInstanceId, '用户主动撤回')
    message.success('撤回成功')
    await getList()
  } catch (error) {
    if (error !== 'cancel') {
      const errorMessage = getErrorMessage(error)
      if (errorMessage) {
        message.error(errorMessage)
      }
    }
  } finally {
    cancellingProcessInstanceId.value = ''
  }
}

const refresh = () => getList()
onMounted(() => getList())
defineExpose({ refresh })
</script>
