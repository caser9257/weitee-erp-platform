<template>
  <Dialog v-model="dialogVisible" title="审批" width="600px">
    <div v-if="task" class="approve-dialog">
      <!-- 任务信息 -->
      <div class="task-info mb-4 p-4 bg-gray-50 rounded-lg">
        <div class="flex items-center mb-2">
          <Icon icon="ep:document" class="mr-2 text-blue-500" />
          <span class="font-bold">{{ task.processInstanceName }}</span>
        </div>
        <div class="text-sm text-gray-500">
          <span>发起人：{{ task.startUserName }}</span>
          <span class="mx-2">|</span>
          <span>当前节点：{{ task.taskName }}</span>
        </div>
      </div>

      <!-- 审批表单 -->
      <el-form ref="formRef" :model="formData" label-width="80px">
        <el-form-item label="审批意见">
          <el-input
            v-model="formData.comment"
            type="textarea"
            :rows="3"
            placeholder="请输入审批意见"
          />
        </el-form-item>
      </el-form>
    </div>
    <template #footer>
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="danger" @click="handleReject" :loading="loading">拒 绝</el-button>
      <el-button type="warning" @click="handleReturn" :loading="loading">驳 回</el-button>
      <el-button type="primary" @click="handleApprove" :loading="loading">同 意</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import { ref, reactive } from 'vue'
import { useMessage } from '@/hooks/web/useMessage'
import * as ApprovalApi from '@/api/bpm/approval'

defineOptions({ name: 'ApproveDialog' })

const { message } = useMessage()
const emit = defineEmits(['success'])

const dialogVisible = ref(false)
const loading = ref(false)
const task = ref<any>(null)
const formRef = ref()

const formData = reactive({
  comment: ''
})

const open = (taskData: any) => {
  task.value = taskData
  formData.comment = ''
  dialogVisible.value = true
}

const handleApprove = async () => {
  loading.value = true
  try {
    // TODO: 调用审批接口
    // await approveTask(task.value.id, formData.comment)
    message.success('审批通过')
    dialogVisible.value = false
    emit('success')
  } finally {
    loading.value = false
  }
}

const handleReject = async () => {
  loading.value = true
  try {
    // TODO: 调用拒绝接口
    // await rejectTask(task.value.id, formData.comment)
    message.success('已拒绝')
    dialogVisible.value = false
    emit('success')
  } finally {
    loading.value = false
  }
}

const handleReturn = async () => {
  // TODO: 打开驳回节点选择弹窗
  message.info('驳回功能开发中')
}

defineExpose({ open })
</script>

<style scoped>
.approve-dialog {
  min-height: 200px;
}
</style>
