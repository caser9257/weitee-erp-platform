<template>
  <div class="pending-list">
    <!-- 搜索栏 -->
    <el-form :inline="true" class="mb-4">
      <el-form-item label="关键字">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索流程名称/单据编号"
          clearable
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">
          <Icon icon="ep:search" class="mr-5px" /> 搜索
        </el-button>
      </el-form-item>
    </el-form>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list" :stripe="true">
      <el-table-column label="流程名称" prop="name" min-width="200">
        <template #default="{ row }">
          <el-link type="primary" @click="handleDetail(row)">
            {{ row.name }}
          </el-link>
        </template>
      </el-table-column>
      <el-table-column label="发起人" prop="startUser.nickname" width="120" />
      <el-table-column label="当前节点" prop="taskName" width="150" />
      <el-table-column label="到达时间" width="170">
        <template #default="{ row }">
          {{ formatTime(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="200" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="handleApprove(scope.row)">
            审批
          </el-button>
          <el-button link type="warning" @click="handleTransfer(scope.row)">
            转办
          </el-button>
          <el-button link type="info" @click="handleUrge(scope.row)">
            催办
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 审批弹窗 -->
    <ApproveDialog ref="approveDialogRef" @success="getList" />
    <!-- 转办弹窗 -->
    <TransferDialog ref="transferDialogRef" @success="getList" />
    <!-- 催办弹窗 -->
    <UrgeDialog ref="urgeDialogRef" @success="getList" />
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
import ApproveDialog from './ApproveDialog.vue'
import TransferDialog from './TransferDialog.vue'
import UrgeDialog from './UrgeDialog.vue'
import DetailDialog from './DetailDialog.vue'

defineOptions({ name: 'PendingList' })
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
const searchKeyword = ref('')
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: undefined as string | undefined
})

const approveDialogRef = ref()
const transferDialogRef = ref()
const urgeDialogRef = ref()
const detailDialogRef = ref()

const getList = async () => {
  loading.value = true
  try {
    queryParams.name = searchKeyword.value || undefined
    const data = await ApprovalPortalApi.getApprovalTodoPage(queryParams)
    list.value = data.list
    total.value = data.total
  } catch (error) {
    list.value = []
    total.value = 0
    if (isBpmModuleUnavailableError(error)) {
      emit('module-unavailable', BPM_MODULE_UNAVAILABLE_TEXT)
      return
    }
    message.error(getErrorMessage(error) || '待我审批列表加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.pageNo = 1
  getList()
}

const handleDetail = (row: any) => {
  detailDialogRef.value.open(row.processInstanceId)
}

const handleApprove = (row: any) => {
  approveDialogRef.value.open(row)
}

const handleTransfer = (row: any) => {
  transferDialogRef.value.open(row)
}

const handleUrge = (row: any) => {
  urgeDialogRef.value.open(row)
}

const refresh = () => {
  getList()
}

onMounted(() => {
  getList()
})

defineExpose({ refresh })
</script>

<style scoped>
.pending-list {
  min-height: 400px;
}
</style>
