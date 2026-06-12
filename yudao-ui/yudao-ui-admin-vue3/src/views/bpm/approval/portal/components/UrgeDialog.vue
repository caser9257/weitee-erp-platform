<template>
  <Dialog v-model="dialogVisible" title="催办" width="500px">
    <div v-if="task" class="urge-dialog">
      <div class="mb-4 p-3 bg-gray-50 rounded-lg text-sm text-gray-600">
        <div class="font-bold text-gray-800 mb-1">{{ task.name || task.processInstanceName }}</div>
        <div>当前节点：{{ task.taskName }}</div>
      </div>
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="80px">
        <el-form-item label="催办消息" prop="message">
          <el-input
            v-model="formData.message"
            type="textarea"
            :rows="4"
            placeholder="请输入催办消息"
          />
        </el-form-item>
      </el-form>
    </div>
    <template #footer>
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="loading">发 送</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import { ref, reactive } from 'vue'
import { useMessage } from '@/hooks/web/useMessage'
import * as ApprovalApi from '@/api/bpm/approval'

defineOptions({ name: 'UrgeDialog' })

const message = useMessage()
const emit = defineEmits(['success'])

const dialogVisible = ref(false)
const loading = ref(false)
const formRef = ref()
const task = ref<any>(null)

const formData = reactive({
  message: ''
})

const rules = {
  message: [{ required: true, message: '请输入催办消息', trigger: 'blur' }]
}

const open = (taskData: any) => {
  task.value = taskData
  formData.message = ''
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  loading.value = true
  try {
    await ApprovalApi.urgeApproval({
      approvalId: task.value.processInstanceId,
      taskId: task.value.id,
      message: formData.message
    })
    message.success('催办成功')
    dialogVisible.value = false
    emit('success')
  } finally {
    loading.value = false
  }
}

defineExpose({ open })
</script>
