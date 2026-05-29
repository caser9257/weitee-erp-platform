<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item label="关联用户Id" prop="userId">
        <el-input v-model="formData.userId" placeholder="请输入关联的系统账号ID（可选）" />
      </el-form-item>
      <el-form-item label="员工姓名" prop="name">
        <el-input v-model="formData.name" placeholder="请输入员工姓名" />
      </el-form-item>
      <el-form-item label="手机号码" prop="mobile">
        <el-input v-model="formData.mobile" placeholder="请输入手机号码" />
      </el-form-item>
      <el-form-item label="身份证号" prop="idCard">
        <el-input v-model="formData.idCard" placeholder="请输入身份证号" />
      </el-form-item>
      <el-form-item label="入职日期" prop="joinDate">
        <el-date-picker
          v-model="formData.joinDate"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="选择入职日期"
        />
      </el-form-item>
      <el-form-item label="员工状态" prop="status">
        <el-radio-group v-model="formData.status" class="ml-4">
          <el-radio :label="0" size="large">在职</el-radio>
          <el-radio :label="1" size="large">离职</el-radio>
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
import * as EmployeeApi from '@/api/hr/employee'

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined,
  userId: undefined,
  name: undefined,
  mobile: undefined,
  idCard: undefined,
  joinDate: undefined,
  status: 0,
  remark: undefined
})
const formRules = reactive({
  name: [{ required: true, message: '员工姓名不能为空', trigger: 'blur' }],
  status: [{ required: true, message: '状态不能为空', trigger: 'change' }]
})
const formRef = ref() // 表单 Ref

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  // 修改时，设置数据
  if (id) {
    formLoading.value = true
    try {
      formData.value = await EmployeeApi.getEmployee(id)
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
const submitForm = async () => {
  // 校验表单
  if (!formRef) return
  const valid = await formRef.value.validate()
  if (!valid) return
  // 提交请求
  formLoading.value = true
  try {
    const data = formData.value as unknown as EmployeeApi.EmployeeVO
    if (formType.value === 'create') {
      await EmployeeApi.createEmployee(data)
      message.success(t('common.createSuccess'))
    } else {
      await EmployeeApi.updateEmployee(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    // 发送操作成功的事件
    emit('success')
  } finally {
    formLoading.value = false
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    id: undefined,
    userId: undefined,
    name: undefined,
    mobile: undefined,
    idCard: undefined,
    joinDate: undefined,
    status: 0,
    remark: undefined
  }
  formRef.value?.resetFields()
}
</script>
