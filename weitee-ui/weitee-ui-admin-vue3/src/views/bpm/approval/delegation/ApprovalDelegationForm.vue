<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="560px">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px" v-loading="formLoading">
      <el-form-item label="被委托人" prop="delegateUserId">
        <div class="approval-delegation-form__user-picker">
          <el-input
            :model-value="formData.delegateUserName || (formData.delegateUserId ? `用户#${formData.delegateUserId}` : '')"
            placeholder="请选择被委托人"
            readonly
          >
            <template #append>
              <el-button :disabled="!!formData.id" @click="openUserSelect">
                <Icon icon="ep:user" />
                选择
              </el-button>
            </template>
          </el-input>
        </div>
      </el-form-item>
      <el-form-item label="委托时间" prop="delegationTime">
        <el-date-picker
          v-model="delegationTime"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          value-format="YYYY-MM-DD HH:mm:ss"
          class="!w-full"
        />
      </el-form-item>
      <el-form-item label="委托场景" prop="sceneCode">
        <el-input v-model="formData.sceneCode" placeholder="留空表示全部场景" />
      </el-form-item>
      <el-form-item label="委托原因" prop="reason">
        <el-input v-model="formData.reason" type="textarea" :rows="3" placeholder="请输入委托原因" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button type="primary" :disabled="formLoading" @click="submitForm">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>

    <!-- 用户选择弹窗 -->
    <UserSelectForm ref="userSelectFormRef" @confirm="handleUserSelectConfirm" />
  </Dialog>
</template>

<script setup lang="ts">
import { ApprovalDelegationApi, BpmApprovalDelegationSaveReqVO } from '@/api/bpm/approval/delegation'
import UserSelectForm from '@/components/UserSelectForm/index.vue'

/** 审批委托表单 */
defineOptions({ name: 'ApprovalDelegationForm' })

const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const formData = ref<BpmApprovalDelegationSaveReqVO & { delegateUserName?: string }>({
  id: undefined,
  delegateUserId: undefined,
  delegateUserName: '',
  startTime: '',
  endTime: '',
  sceneCode: undefined,
  reason: undefined
})
const delegationTime = ref<[string, string] | undefined>()
const formRules = reactive({
  delegateUserId: [{ required: true, message: '被委托人不能为空', trigger: 'blur' }],
  delegationTime: [{ required: true, message: '委托时间不能为空', trigger: 'change' }]
})
const formRef = ref()

const emit = defineEmits<{ (e: 'success'): void }>()

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增审批委托' : '编辑审批委托'
  formType.value = type
  resetForm()
  if (type === 'update' && id) {
    formLoading.value = true
    try {
      const data = await ApprovalDelegationApi.getDelegation(id)
      formData.value = {
        id: data.id,
        delegateUserId: data.delegateUserId,
        delegateUserName: data.delegateUserName,
        startTime: data.startTime,
        endTime: data.endTime,
        sceneCode: data.sceneCode,
        reason: data.reason
      }
      delegationTime.value = data.startTime && data.endTime ? [data.startTime, data.endTime] : undefined
    } finally {
      formLoading.value = false
    }
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    id: undefined,
    delegateUserId: undefined,
    delegateUserName: '',
    startTime: '',
    endTime: '',
    sceneCode: undefined,
    reason: undefined
  }
  delegationTime.value = undefined
  formRef.value?.clearValidate()
}

/** 用户选择 */
const userSelectFormRef = ref()
const openUserSelect = () => {
  userSelectFormRef.value.open(0, [])
}
const handleUserSelectConfirm = (activityId: number, users: any[]) => {
  if (users.length > 0) {
    formData.value.delegateUserId = users[0].id
    formData.value.delegateUserName = users[0].nickname
  }
}

/** 提交表单 */
const submitForm = async () => {
  await formRef.value.validate()
  if (!delegationTime.value || delegationTime.value.length !== 2) {
    message.error('请选择委托时间')
    return
  }
  formData.value.startTime = delegationTime.value[0]
  formData.value.endTime = delegationTime.value[1]
  formLoading.value = true
  try {
    if (formType.value === 'create') {
      await ApprovalDelegationApi.createDelegation(formData.value)
    } else {
      await ApprovalDelegationApi.updateDelegation(formData.value)
    }
    message.success('操作成功')
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.approval-delegation-form {
  &__user-picker {
    width: 100%;
  }
}
</style>
