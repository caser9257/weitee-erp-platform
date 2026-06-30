<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="640">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="dialogLoading"
      :disabled="dialogLoading || submitLoading"
    >
      <el-form-item label="名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入名称" />
      </el-form-item>
      <el-form-item label="编码" prop="no">
        <el-input v-model="formData.no" placeholder="请输入编码" />
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio
            v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
            :key="dict.value"
            :value="dict.value"
          >
            {{ dict.label }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="排序" prop="sort">
        <el-input-number v-model="formData.sort" :min="0" controls-position="right" class="!w-full" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button type="primary" :loading="submitLoading" :disabled="dialogLoading" @click="submitForm">
        确定
      </el-button>
      <el-button :disabled="submitLoading" @click="dialogVisible = false">取消</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { getIntDictOptions, DICT_TYPE } from '@/utils/dict'
import { AccountApi, AccountVO } from '@/api/erp/finance/account'

defineOptions({ name: 'AccountForm' })

interface AccountFormData {
  id?: number
  name?: string
  no?: string
  remark?: string
  status?: number
  sort?: number
  defaultStatus?: boolean
}

const { t } = useI18n()
const message = useMessage()

const createEmptyFormData = (): AccountFormData => ({
  id: undefined,
  name: undefined,
  no: undefined,
  remark: undefined,
  status: undefined,
  sort: undefined,
  defaultStatus: undefined
})

const dialogVisible = ref(false)
const dialogTitle = ref('')
const dialogLoading = ref(false)
const submitLoading = ref(false)
const formType = ref<'create' | 'update'>('create')
const formData = ref<AccountFormData>(createEmptyFormData())
const formRef = ref()

const formRules = reactive({
  name: [{ required: true, message: '名称不能为空', trigger: 'blur' }],
  status: [{ required: true, message: '启用状态不能为空', trigger: 'change' }],
  sort: [{ required: true, message: '排序不能为空', trigger: 'blur' }]
})

const resetForm = async () => {
  formData.value = createEmptyFormData()
  await nextTick()
  formRef.value?.resetFields()
}

const open = async (type: 'create' | 'update', id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  dialogLoading.value = false
  submitLoading.value = false
  await resetForm()
  if (!id) {
    return
  }
  dialogLoading.value = true
  try {
    formData.value = await AccountApi.getAccount(id)
  } finally {
    dialogLoading.value = false
  }
}

defineExpose({ open })

const emit = defineEmits<{
  (e: 'success'): void
}>()

const submitForm = async () => {
  if (submitLoading.value) {
    return
  }
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    const data = formData.value as AccountVO
    if (formType.value === 'create') {
      await AccountApi.createAccount(data)
      message.success(t('common.createSuccess'))
    } else {
      await AccountApi.updateAccount(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    submitLoading.value = false
  }
}
</script>
