<template>
  <Dialog v-model="dialogVisible" title="修改用户等级" width="640px" :scroll="true" :max-height="'60vh'">
    <el-form
      ref="formRef"
      v-loading="loading"
      :model="formData"
      :rules="formRules"
      label-width="110px"
    >
      <div class="grid gap-4">
        <el-form-item label="用户编号" prop="id">
          <el-input v-model="formData.id" disabled />
        </el-form-item>
        <el-form-item label="用户昵称" prop="nickname">
          <el-input v-model="formData.nickname" disabled />
        </el-form-item>
        <el-form-item label="用户等级" prop="levelId">
          <MemberLevelSelect v-model="formData.levelId" />
        </el-form-item>
        <el-form-item label="修改原因" prop="reason">
          <el-input
            v-model="formData.reason"
            :autosize="{ minRows: 3, maxRows: 5 }"
            placeholder="请输入修改原因"
            type="textarea"
          />
        </el-form-item>
      </div>
    </el-form>
    <template #footer>
      <div class="flex items-center justify-end gap-2">
        <el-button :disabled="submitting || loading" type="primary" @click="submitForm">
          确定
        </el-button>
        <el-button :disabled="submitting" @click="dialogVisible = false">取消</el-button>
      </div>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import * as UserApi from '@/api/member/user'
import MemberLevelSelect from '@/views/member/level/components/MemberLevelSelect.vue'

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const loading = ref(false)
const submitting = ref(false)
const formRef = ref()
const formData = ref({
  id: undefined,
  nickname: undefined,
  levelId: undefined,
  reason: undefined
})
const formRules = reactive({
  levelId: [{ required: true, message: '用户等级不能为空', trigger: 'change' }],
  reason: [{ required: true, message: '修改原因不能为空', trigger: 'blur' }]
})

const resetForm = () => {
  formData.value = {
    id: undefined,
    nickname: undefined,
    levelId: undefined,
    reason: undefined
  }
  formRef.value?.resetFields()
}

const open = async (id?: number) => {
  dialogVisible.value = true
  resetForm()
  if (!id) {
    return
  }
  loading.value = true
  try {
    formData.value = await UserApi.getUser(id)
  } finally {
    loading.value = false
  }
}

defineExpose({ open })

const emit = defineEmits(['success'])
const submitForm = async () => {
  if (!formRef.value) {
    return
  }
  const valid = await formRef.value.validate()
  if (!valid) {
    return
  }
  if (submitting.value) {
    return
  }
  submitting.value = true
  try {
    await UserApi.updateUserLevel(formData.value)
    message.success(t('common.updateSuccess'))
    dialogVisible.value = false
    emit('success')
  } finally {
    submitting.value = false
  }
}
</script>
