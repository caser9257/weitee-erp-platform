<template>
  <Dialog v-model="dialogVisible" title="修改用户积分" width="640px" :scroll="true" :max-height="'60vh'">
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
        <el-form-item label="变动前积分" prop="point">
          <el-input :model-value="formData.point" disabled />
        </el-form-item>
        <el-form-item label="变动类型" prop="changeType">
          <el-radio-group v-model="formData.changeType">
            <el-radio :value="1">增加</el-radio>
            <el-radio :value="-1">减少</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="变动积分" prop="changePoint">
          <el-input-number v-model="formData.changePoint" :min="0" :precision="0" />
        </el-form-item>
        <el-form-item label="变动后积分">
          <el-input :model-value="pointResult" disabled />
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

<script lang="ts" setup>
import * as UserApi from '@/api/member/user'

defineOptions({ name: 'UpdatePointForm' })

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const loading = ref(false)
const submitting = ref(false)
const formRef = ref()
const formData = ref({
  id: undefined,
  nickname: undefined,
  point: 0,
  changePoint: 0,
  changeType: 1
})
const formRules = reactive({
  changePoint: [{ required: true, message: '变动积分不能为空', trigger: 'blur' }]
})

const resetForm = () => {
  formData.value = {
    id: undefined,
    nickname: undefined,
    point: 0,
    changePoint: 0,
    changeType: 1
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
    formData.value.changeType = 1
    formData.value.changePoint = 0
  } finally {
    loading.value = false
  }
}

defineExpose({ open })

const emit = defineEmits(['success'])
const pointResult = computed(
  () => (formData.value.point || 0) + formData.value.changePoint * formData.value.changeType
)

const submitForm = async () => {
  if (!formRef.value) {
    return
  }
  const valid = await formRef.value.validate()
  if (!valid) {
    return
  }
  if (formData.value.changePoint < 1) {
    message.error('变动积分不能小于 1')
    return
  }
  if (pointResult.value < 0) {
    message.error('变动后的积分不能小于 0')
    return
  }
  if (submitting.value) {
    return
  }
  submitting.value = true
  try {
    await UserApi.updateUserPoint({
      id: formData.value.id,
      point: formData.value.changePoint * formData.value.changeType
    })
    message.success(t('common.updateSuccess'))
    dialogVisible.value = false
    emit('success')
  } finally {
    submitting.value = false
  }
}
</script>
