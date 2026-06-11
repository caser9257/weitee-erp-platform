<template>
  <Dialog v-model="dialogVisible" title="审批详情" width="700px">
    <div v-loading="loading" class="detail-dialog">
      <template v-if="detail">
        <!-- 头部信息 -->
        <div class="mb-4 p-4 bg-gray-50 rounded-lg">
          <div class="flex items-center gap-2 mb-2">
            <span class="text-lg font-bold">{{ detail.processInstance?.name }}</span>
            <el-tag
              :type="getStatusType(detail.processInstance?.status)"
              effect="light"
              size="small"
            >
              {{ getStatusLabel(detail.processInstance?.status) }}
            </el-tag>
          </div>
          <div class="flex items-center gap-4 text-sm text-gray-500">
            <div class="flex items-center gap-1">
              <el-avatar :size="22" v-if="detail.processInstance?.startUser?.avatar">
                <img :src="detail.processInstance?.startUser?.avatar" />
              </el-avatar>
              <el-avatar :size="22" v-else>
                {{ (detail.processInstance?.startUser?.nickname || '').substring(0, 1) }}
              </el-avatar>
              <span>{{ detail.processInstance?.startUser?.nickname }}</span>
            </div>
            <span>{{ formatTime(detail.processInstance?.startTime) }}</span>
          </div>
        </div>

        <!-- 审批时间线 -->
        <div class="text-sm font-bold mb-3">审批流程</div>
        <div class="timeline-wrapper">
          <ProcessInstanceTimeline
            v-if="detail.activityNodes?.length"
            :activity-nodes="detail.activityNodes"
          />
          <el-empty v-else description="暂无审批记录" :image-size="60" />
        </div>
      </template>
    </div>
    <template #footer>
      <el-button @click="dialogVisible = false">关 闭</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import { ref } from 'vue'
import { formatDate } from '@/utils/formatTime'
import { TaskStatusEnum } from '@/api/bpm/task'
import * as ProcessInstanceApi from '@/api/bpm/processInstance'
import ProcessInstanceTimeline from '@/views/bpm/processInstance/detail/ProcessInstanceTimeline.vue'

defineOptions({ name: 'DetailDialog' })

const dialogVisible = ref(false)
const loading = ref(false)
const detail = ref<any>(null)

const formatTime = (time: number | string | undefined) => {
  if (!time) return '-'
  return formatDate(new Date(time), 'YYYY-MM-DD HH:mm:ss')
}

const getStatusType = (status: number | undefined) => {
  const map: Record<number, string> = {
    [TaskStatusEnum.RUNNING]: 'warning',
    [TaskStatusEnum.APPROVE]: 'success',
    [TaskStatusEnum.REJECT]: 'danger',
    [TaskStatusEnum.CANCEL]: 'info'
  }
  return (status !== undefined ? map[status] : '') || 'info'
}

const getStatusLabel = (status: number | undefined) => {
  const map: Record<number, string> = {
    [TaskStatusEnum.RUNNING]: '审批中',
    [TaskStatusEnum.APPROVE]: '已通过',
    [TaskStatusEnum.REJECT]: '已拒绝',
    [TaskStatusEnum.CANCEL]: '已撤回'
  }
  return (status !== undefined ? map[status] : '') || '未知'
}

const open = async (processInstanceId: string) => {
  if (!processInstanceId) {
    return
  }
  dialogVisible.value = true
  loading.value = true
  detail.value = null
  try {
    const data = await ProcessInstanceApi.getApprovalDetail({
      processInstanceId
    })
    detail.value = data
  } catch {
    // 加载失败时保持空状态
  } finally {
    loading.value = false
  }
}

defineExpose({ open })
</script>

<style scoped>
.detail-dialog {
  min-height: 200px;
}
.timeline-wrapper {
  padding-left: 20px;
  padding-bottom: 8px;
}
</style>
