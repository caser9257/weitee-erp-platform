<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px" v-loading="formLoading">
      <el-form-item label="班次名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入班次名称" />
      </el-form-item>
      <el-form-item label="上班时间" prop="startTime">
        <el-time-picker v-model="formData.startTime" format="HH:mm" value-format="HH:mm" placeholder="选择上班时间" />
      </el-form-item>
      <el-form-item label="下班时间" prop="endTime">
        <el-time-picker v-model="formData.endTime" format="HH:mm" value-format="HH:mm" placeholder="选择下班时间" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio :label="0">启用</el-radio>
          <el-radio :label="1">禁用</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" type="textarea" placeholder="请输入备注" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import * as ShiftApi from '@/api/hr/shift'

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const formData = ref({
  id: undefined,
  name: undefined,
  startTime: undefined,
  endTime: undefined,
  status: 0,
  remark: undefined
})
const formRules = reactive({
  name: [{ required: true, message: '班次名称不能为空', trigger: 'blur' }],
  startTime: [{ required: true, message: '上班时间不能为空', trigger: 'blur' }],
  endTime: [{ required: true, message: '下班时间不能为空', trigger: 'blur' }]
})
const formRef = ref()

const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  if (id) {
    formLoading.value = true
    try {
      formData.value = await ShiftApi.getShift(id)
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open })

const emit = defineEmits(['success'])
const submitForm = async () => {
  if (!formRef) return
  const valid = await formRef.value.validate()
  if (!valid) return
  
  formLoading.value = true
  try {
    const data = formData.value as unknown as ShiftApi.ShiftVO
    if (formType.value === 'create') {
      await ShiftApi.createShift(data)
      message.success(t('common.createSuccess'))
    } else {
      await ShiftApi.updateShift(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}

const resetForm = () => {
  formData.value = {
    id: undefined,
    name: undefined,
    startTime: undefined,
    endTime: undefined,
    status: 0,
    remark: undefined
  }
  formRef.value?.resetFields()
}
</script>
