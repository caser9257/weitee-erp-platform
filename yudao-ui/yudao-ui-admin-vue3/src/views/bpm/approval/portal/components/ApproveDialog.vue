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
        <el-form-item v-if="!showReturnForm" label="审批意见">
          <el-input
            v-model="formData.comment"
            type="textarea"
            :rows="3"
            placeholder="请输入审批意见"
          />
        </el-form-item>
      </el-form>

      <!-- 驳回表单 -->
      <el-form v-if="showReturnForm" ref="returnFormRef" :model="returnFormData" :rules="returnFormRules" label-width="80px">
        <el-form-item label="退回节点" prop="targetTaskDefinitionKey">
          <el-select v-model="returnFormData.targetTaskDefinitionKey" placeholder="请选择退回节点" style="width: 100%">
            <el-option
              v-for="item in returnNodes"
              :key="item.taskDefinitionKey"
              :label="item.name"
              :value="item.taskDefinitionKey"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="退回原因" prop="reason">
          <el-input
            v-model="returnFormData.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入退回原因"
          />
        </el-form-item>
      </el-form>
    </div>
    <template #footer>
      <el-button @click="dialogVisible = false">取 消</el-button>
      <template v-if="showReturnForm">
        <el-button @click="showReturnForm = false">返 回</el-button>
        <el-button type="warning" @click="handleReturnSubmit" :loading="loading">确认驳回</el-button>
      </template>
      <template v-else>
        <el-button type="danger" @click="handleReject" :loading="loading">拒 绝</el-button>
        <el-button type="warning" @click="handleReturn" :loading="returnLoading">驳 回</el-button>
        <el-button type="primary" @click="handleApprove" :loading="loading">同 意</el-button>
      </template>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import { ref, reactive } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { useMessage } from '@/hooks/web/useMessage'
import * as TaskApi from '@/api/bpm/task'

defineOptions({ name: 'ApproveDialog' })

const message = useMessage()
const emit = defineEmits(['success'])

const dialogVisible = ref(false)
const loading = ref(false)
const returnLoading = ref(false)
const task = ref<any>(null)
const formRef = ref<FormInstance>()

const formData = reactive({
  comment: ''
})

// 驳回相关
const showReturnForm = ref(false)
const returnNodes = ref<any[]>([])
const returnFormRef = ref<FormInstance>()
const returnFormData = reactive({
  targetTaskDefinitionKey: undefined as string | undefined,
  reason: ''
})
const returnFormRules: FormRules = {
  targetTaskDefinitionKey: [{ required: true, message: '请选择退回节点', trigger: 'change' }],
  reason: [{ required: true, message: '请输入退回原因', trigger: 'blur' }]
}

const open = (taskData: any) => {
  task.value = taskData
  formData.comment = ''
  showReturnForm.value = false
  returnNodes.value = []
  returnFormData.targetTaskDefinitionKey = undefined
  returnFormData.reason = ''
  dialogVisible.value = true
}

const handleApprove = async () => {
  loading.value = true
  try {
    await TaskApi.approveTask({ id: task.value.id, reason: formData.comment })
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
    await TaskApi.rejectTask({ id: task.value.id, reason: formData.comment })
    message.success('已拒绝')
    dialogVisible.value = false
    emit('success')
  } finally {
    loading.value = false
  }
}

const handleReturn = async () => {
  returnLoading.value = true
  try {
    returnNodes.value = await TaskApi.getTaskListByReturn(task.value.id)
    if (returnNodes.value.length === 0) {
      message.warning('当前没有可退回的节点')
      return
    }
    showReturnForm.value = true
  } finally {
    returnLoading.value = false
  }
}

const handleReturnSubmit = async () => {
  if (!returnFormRef.value) return
  await returnFormRef.value.validate()
  loading.value = true
  try {
    await TaskApi.returnTask({
      id: task.value.id,
      reason: returnFormData.reason,
      targetTaskDefinitionKey: returnFormData.targetTaskDefinitionKey
    })
    message.success('已驳回')
    dialogVisible.value = false
    emit('success')
  } finally {
    loading.value = false
  }
}

defineExpose({ open })
</script>

<style scoped>
.approve-dialog {
  min-height: 200px;
}
</style>
