<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="640px" scroll maxHeight="78vh">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="96px"
      v-loading="dialogLoading"
      :disabled="formDisabled"
    >
      <el-form-item label="账簿编码" prop="no">
        <el-input v-model="formData.no" placeholder="请输入账簿编码" />
      </el-form-item>
      <el-form-item label="账簿名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入账簿名称" />
      </el-form-item>
      <el-form-item label="启用状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio v-for="item in COMMON_STATUS_OPTIONS" :key="item.value" :value="item.value">
            {{ item.label }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="排序" prop="sort">
        <el-input-number v-model="formData.sort" :min="0" controls-position="right" class="!w-full" />
      </el-form-item>
      <el-form-item label="默认账簿" prop="defaultStatus">
        <el-switch v-model="formData.defaultStatus" />
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注" />
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
import type { FormRules } from 'element-plus'
import { COMMON_STATUS_OPTIONS } from '@/views/erp/finance/shared/accounting'
import {
  ErpFinanceLedgerSaveReqVO,
  ErpFinanceLedgerVO,
  FinanceLedgerApi
} from '@/api/erp/finance/ledger'

defineOptions({ name: 'LedgerForm' })

type FormType = 'create' | 'update'

const createEmptyFormData = (): ErpFinanceLedgerSaveReqVO => ({
  id: undefined,
  no: undefined,
  name: undefined,
  status: 0,
  sort: 1,
  defaultStatus: false,
  remark: ''
})

const message = useMessage()
const { t } = useI18n()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const dialogLoading = ref(false)
const submitLoading = ref(false)
const formType = ref<FormType>('create')
const formData = ref<ErpFinanceLedgerSaveReqVO>(createEmptyFormData())
const formRef = ref()

const formDisabled = computed(() => dialogLoading.value || submitLoading.value)
const formRules: FormRules<ErpFinanceLedgerSaveReqVO> = {
  no: [{ required: true, message: '账簿编码不能为空', trigger: 'blur' }],
  name: [{ required: true, message: '账簿名称不能为空', trigger: 'blur' }],
  status: [{ required: true, message: '启用状态不能为空', trigger: 'change' }],
  sort: [{ required: true, message: '排序不能为空', trigger: 'blur' }]
}

const resetForm = async () => {
  formData.value = createEmptyFormData()
  await nextTick()
  formRef.value?.clearValidate()
}

const titleMap: Record<FormType, string> = {
  create: '新增账簿',
  update: '编辑账簿'
}

const open = async (type: FormType, id?: number) => {
  dialogVisible.value = true
  formType.value = type
  dialogTitle.value = titleMap[type]
  submitLoading.value = false
  await resetForm()
  if (!id) {
    return
  }
  dialogLoading.value = true
  try {
    formData.value = await FinanceLedgerApi.getLedger(id)
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
    const payload = { ...formData.value } as ErpFinanceLedgerVO
    if (formType.value === 'create') {
      await FinanceLedgerApi.createLedger(payload)
      message.success(t('common.createSuccess'))
    } else {
      await FinanceLedgerApi.updateLedger(payload)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    submitLoading.value = false
  }
}
</script>
