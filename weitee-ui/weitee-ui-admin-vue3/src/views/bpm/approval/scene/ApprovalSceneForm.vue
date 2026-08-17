<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="560px">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px" v-loading="formLoading">
      <el-form-item label="场景编码" prop="sceneCode">
        <el-input v-model="formData.sceneCode" placeholder="如 erp.finance.payment.submit" :disabled="formType === 'update'" />
      </el-form-item>
      <el-form-item label="场景名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入场景名称" />
      </el-form-item>
      <el-form-item label="模块编码" prop="moduleCode">
        <el-input v-model="formData.moduleCode" placeholder="如 erp_finance" />
      </el-form-item>
      <el-form-item label="业务类型" prop="bizType">
        <el-input v-model="formData.bizType" placeholder="如 payment" />
      </el-form-item>
      <el-form-item label="动作编码" prop="actionCode">
        <el-input v-model="formData.actionCode" placeholder="如 submit" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">禁用</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button type="primary" :disabled="formLoading" @click="submitForm">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { ApprovalSceneApi, BpmApprovalSceneSaveReqVO } from '@/api/bpm/approval/scene'

/** 审批场景表单 */
defineOptions({ name: 'ApprovalSceneForm' })

const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const formData = ref<BpmApprovalSceneSaveReqVO>({
  id: undefined,
  sceneCode: '',
  name: '',
  moduleCode: '',
  bizType: '',
  actionCode: '',
  status: 1,
  remark: undefined
})
const formRules = reactive({
  sceneCode: [{ required: true, message: '场景编码不能为空', trigger: 'blur' }],
  name: [{ required: true, message: '场景名称不能为空', trigger: 'blur' }],
  moduleCode: [{ required: true, message: '模块编码不能为空', trigger: 'blur' }],
  bizType: [{ required: true, message: '业务类型不能为空', trigger: 'blur' }],
  actionCode: [{ required: true, message: '动作编码不能为空', trigger: 'blur' }],
  status: [{ required: true, message: '状态不能为空', trigger: 'blur' }]
})
const formRef = ref()

const emit = defineEmits<{ (e: 'success'): void }>()

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增审批场景' : '编辑审批场景'
  formType.value = type
  resetForm()
  if (type === 'update' && id) {
    formLoading.value = true
    try {
      const data = await ApprovalSceneApi.getScene(id)
      formData.value = {
        id: data.id,
        sceneCode: data.sceneCode,
        name: data.name,
        moduleCode: data.moduleCode,
        bizType: data.bizType,
        actionCode: data.actionCode,
        status: data.status,
        remark: data.remark
      }
    } finally {
      formLoading.value = false
    }
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    id: undefined,
    sceneCode: '',
    name: '',
    moduleCode: '',
    bizType: '',
    actionCode: '',
    status: 1,
    remark: undefined
  }
  formRef.value?.clearValidate()
}

/** 提交表单 */
const submitForm = async () => {
  await formRef.value.validate()
  formLoading.value = true
  try {
    if (formType.value === 'create') {
      await ApprovalSceneApi.createScene(formData.value)
    } else {
      await ApprovalSceneApi.updateScene(formData.value)
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
