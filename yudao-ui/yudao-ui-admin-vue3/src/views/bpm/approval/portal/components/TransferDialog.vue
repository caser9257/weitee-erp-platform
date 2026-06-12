<template>
  <Dialog v-model="dialogVisible" title="转办" width="500px">
    <div v-if="task" class="transfer-dialog">
      <div class="mb-4 p-3 bg-gray-50 rounded-lg text-sm text-gray-600">
        <div class="font-bold text-gray-800 mb-1">{{ task.name || task.processInstanceName }}</div>
        <div>当前节点：{{ task.taskName }}</div>
      </div>
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="80px">
        <el-form-item label="转办给" prop="assigneeUserId">
          <el-select
            v-model="formData.assigneeUserId"
            filterable
            remote
            :remote-method="searchUser"
            :loading="userLoading"
            placeholder="搜索并选择转办目标"
            class="w-full"
          >
            <el-option
              v-for="user in userList"
              :key="user.id"
              :label="user.nickname"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="formData.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入转办备注"
          />
        </el-form-item>
      </el-form>
    </div>
    <template #footer>
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="loading">确 定</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import { ref, reactive } from 'vue'
import { useMessage } from '@/hooks/web/useMessage'
import * as TaskApi from '@/api/bpm/task'
import * as UserApi from '@/api/system/user'

defineOptions({ name: 'TransferDialog' })

const message = useMessage()
const emit = defineEmits(['success'])

const dialogVisible = ref(false)
const loading = ref(false)
const userLoading = ref(false)
const formRef = ref()
const task = ref<any>(null)
const userList = ref<any[]>([])

const formData = reactive({
  assigneeUserId: undefined as number | undefined,
  reason: ''
})

const rules = {
  assigneeUserId: [{ required: true, message: '请选择转办目标用户', trigger: 'change' }]
}

const open = async (taskData: any) => {
  task.value = taskData
  formData.assigneeUserId = undefined
  formData.reason = ''
  dialogVisible.value = true
  // 加载用户列表
  await searchUser('')
}

const searchUser = async (query: string) => {
  userLoading.value = true
  try {
    const users = await UserApi.getSimpleUserList()
    userList.value = query
      ? users.filter((u: any) => u.nickname?.includes(query))
      : users
  } finally {
    userLoading.value = false
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  if (!formData.assigneeUserId) {
    message.warning('请选择转办目标用户')
    return
  }
  loading.value = true
  try {
    await TaskApi.transferTask({
      id: task.value.id,
      assigneeUserId: formData.assigneeUserId,
      reason: formData.reason || undefined
    })
    message.success('转办成功')
    dialogVisible.value = false
    emit('success')
  } finally {
    loading.value = false
  }
}

defineExpose({ open })
</script>
