<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item label="场景编码" prop="sceneCode">
        <el-input
          v-model="formData.sceneCode"
          placeholder="请输入场景编码，如 erp.finance.payment.submit"
          :disabled="!!formData.id"
        />
      </el-form-item>
      <el-form-item label="场景名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入场景名称" />
      </el-form-item>
      <el-form-item label="模块编码" prop="moduleCode">
        <el-input v-model="formData.moduleCode" placeholder="请输入模块编码，如 erp_finance" />
      </el-form-item>
      <el-form-item label="业务类型" prop="bizType">
        <el-input v-model="formData.bizType" placeholder="请输入业务类型，如 payment" />
      </el-form-item>
      <el-form-item label="动作编码" prop="actionCode">
        <el-input v-model="formData.actionCode" placeholder="请输入动作编码，如 submit" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">禁用</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" type="textarea" placeholder="请输入备注" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="formLoading">确 定</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import { ref, reactive } from 'vue'
import type { FormRules } from 'element-plus'
import { useI18n } from '@/hooks/web/useI18n'
import { useMessage } from '@/hooks/web/useMessage'
import * as ApprovalSceneApi from '@/api/bpm/approval/scene'

defineOptions({ name: 'ApprovalSceneForm' })

const { t } = useI18n()
const message = useMessage()

const emit = defineEmits(['success'])

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formRef = ref()

const formData = reactive<ApprovalSceneApi.ApprovalSceneSaveReqVO>({
  id: undefined,
  sceneCode: '',
  name: '',
  moduleCode: '',
  bizType: '',
  actionCode: '',
  status: 1,
  remark: ''
})

const formRules: FormRules = {
  sceneCode: [{ required: true, message: '场景编码不能为空', trigger: 'blur' }],
  name: [{ required: true, message: '场景名称不能为空', trigger: 'blur' }],
  moduleCode: [{ required: true, message: '模块编码不能为空', trigger: 'blur' }],
  bizType: [{ required: true, message: '业务类型不能为空', trigger: 'blur' }],
  actionCode: [{ required: true, message: '动作编码不能为空', trigger: 'blur' }],
  status: [{ required: true, message: '状态不能为空', trigger: 'change' }]
}

// 打开弹窗
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增审批场景' : '编辑审批场景'
  resetForm()

  if (id) {
    formLoading.value = true
    try {
      const data = await ApprovalSceneApi.getApprovalScene(id)
      Object.assign(formData, data)
    } finally {
      formLoading.value = false
    }
  }
}

// 提交表单
const handleSubmit = async () => {
  await formRef.value.validate()
  formLoading.value = true
  try {
    if (formData.id) {
      await ApprovalSceneApi.updateApprovalScene(formData)
      message.success('修改成功')
    } else {
      await ApprovalSceneApi.createApprovalScene(formData)
      message.success('新增成功')
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}

// 重置表单
const resetForm = () => {
  formData.id = undefined
  formData.sceneCode = ''
  formData.name = ''
  formData.moduleCode = ''
  formData.bizType = ''
  formData.actionCode = ''
  formData.status = 1
  formData.remark = ''
  formRef.value?.resetFields()
}

defineExpose({ open })
</script>
