<template>
  <div class="cc-list">
    <el-table v-loading="loading" :data="list" :stripe="true">
      <el-table-column label="流程名称" prop="name" min-width="200">
        <template #default="{ row }">
          <el-link type="primary" @click="handleDetail(row)">
            {{ row.name }}
          </el-link>
        </template>
      </el-table-column>
      <el-table-column label="发起人" prop="startUser.nickname" width="120" />
      <el-table-column label="状态" prop="status" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)" effect="light">
            {{ getStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="抄送时间" width="170">
        <template #default="{ row }">
          {{ formatTime(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="100" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="handleDetail(scope.row)">
            详情
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
import { formatDate } from '@/utils/formatTime'
import { useMessage } from '@/hooks/web/useMessage'
import * as ApprovalPortalApi from '@/api/bpm/approval/portal'
import {
  BPM_MODULE_UNAVAILABLE_TEXT,
  getErrorMessage,
  isBpmModuleUnavailableError
} from '../moduleGuard'
import DetailDialog from './DetailDialog.vue'

defineOptions({ name: 'CcList' })
const emit = defineEmits<{
  (e: 'module-unavailable', message?: string): void
}>()
const message = useMessage()

const formatTime = (time: any) => {
  if (!time) return '-'
  const date = typeof time === 'number' ? new Date(time) : new Date(time)
  return formatDate(date, 'YYYY-MM-DD HH:mm:ss')
}

const loading = ref(true)
const list = ref<any[]>([])
const total = ref(0)
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
    const data = await ApprovalPortalApi.getApprovalCcPage(queryParams)
    console.log('[CcList] getList response:', data)
    // 确保 data.list 存在
    list.value = data?.list || []
    total.value = data?.total || 0
    console.log('[CcList] list.value:', list.value)
  } catch (error) {
    list.value = []
    total.value = 0
    if (isBpmModuleUnavailableError(error)) {
      emit('module-unavailable', BPM_MODULE_UNAVAILABLE_TEXT)
      return
    }
    message.error(getErrorMessage(error) || '抄送我的列表加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleDetail = (row: any) => {
  detailDialogRef.value.open(row.processInstanceId)
}

const refresh = () => getList()
onMounted(() => getList())
defineExpose({ refresh })
</script>
