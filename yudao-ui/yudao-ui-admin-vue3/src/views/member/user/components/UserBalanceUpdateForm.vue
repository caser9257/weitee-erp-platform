<template>
  <Dialog v-model="dialogVisible" title="修改用户余额" width="640px" :scroll="true" :max-height="'60vh'">
    <el-form
      ref="formRef"
      v-loading="loading"
      :model="formData"
      :rules="formRules"
      label-width="120px"
    >
      <div class="grid gap-4">
        <el-form-item label="用户编号" prop="id">
          <el-input v-model="formData.id" disabled />
        </el-form-item>
        <el-form-item label="用户昵称" prop="nickname">
          <el-input v-model="formData.nickname" disabled />
        </el-form-item>
        <el-form-item label="变动前余额(元)" prop="balance">
          <el-input :model-value="formData.balance" disabled />
        </el-form-item>
        <el-form-item label="变动类型" prop="changeType">
          <el-radio-group v-model="formData.changeType">
            <el-radio :label="1">增加</el-radio>
            <el-radio :label="-1">减少</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="变动余额(元)" prop="changeBalance">
          <el-input-number
            v-model="formData.changeBalance"
            :min="0"
            :precision="2"
            :step="0.1"
          />
        </el-form-item>
        <el-form-item label="变动后余额(元)">
          <el-input :model-value="balanceResult" disabled />
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
import * as WalletApi from '@/api/pay/wallet/balance'
import { convertToInteger, formatToFraction, fenToYuan } from '@/utils'

defineOptions({ name: 'UpdateBalanceForm' })

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const loading = ref(false)
const submitting = ref(false)
const formRef = ref()
const formData = ref({
  id: undefined,
  nickname: undefined,
  balance: '0',
  changeBalance: 0,
  changeType: 1
})
const formRules = reactive({
  changeBalance: [{ required: true, message: '变动余额不能为空', trigger: 'blur' }]
})

const resetForm = () => {
  formData.value = {
    id: undefined,
    nickname: undefined,
    balance: '0',
    changeBalance: 0,
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
    const user = await UserApi.getUser(id)
    const wallet = await WalletApi.getWallet({ userId: user.id || 0 })
    formData.value.id = user.id
    formData.value.nickname = user.nickname
    formData.value.balance = fenToYuan(wallet.balance || 0)
    formData.value.changeType = 1
    formData.value.changeBalance = 0
  } finally {
    loading.value = false
  }
}

defineExpose({ open })

const emit = defineEmits(['success'])
const balanceResult = computed(() =>
  formatToFraction(
    convertToInteger(formData.value.balance) +
      convertToInteger(formData.value.changeBalance) * formData.value.changeType
  )
)

const submitForm = async () => {
  if (!formRef.value) {
    return
  }
  const valid = await formRef.value.validate()
  if (!valid) {
    return
  }
  if (formData.value.changeBalance <= 0) {
    message.error('变动余额不能为空')
    return
  }
  if (convertToInteger(balanceResult.value) < 0) {
    message.error('变动后的余额不能小于 0')
    return
  }
  if (submitting.value) {
    return
  }
  submitting.value = true
  try {
    await WalletApi.updateWalletBalance({
      userId: formData.value.id,
      balance: convertToInteger(formData.value.changeBalance) * formData.value.changeType
    })
    message.success(t('common.updateSuccess'))
    dialogVisible.value = false
    emit('success')
  } finally {
    submitting.value = false
  }
}
</script>
