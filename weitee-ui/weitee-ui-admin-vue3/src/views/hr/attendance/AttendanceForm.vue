<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px" v-loading="formLoading">
      <el-form-item label="员工姓名" prop="employeeId">
        <el-select v-model="formData.employeeId" placeholder="请选择员工" filterable clearable>
          <el-option
            v-for="user in employeeList"
            :key="user.id"
            :label="`${user.name} (${user.mobile || '工号:' + user.id})`"
            :value="user.id"
          >
            <span style="float: left">{{ user.name }}</span>
            <span style="float: right; color: #8492a6; font-size: 13px">手机: {{ user.mobile || '无' }}</span>
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="考勤日期" prop="recordDate">
        <el-date-picker v-model="formData.recordDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" />
      </el-form-item>
      <el-form-item label="上班打卡时间" prop="clockInTime">
        <el-time-picker v-model="formData.clockInTime" format="HH:mm:ss" value-format="HH:mm:ss" placeholder="上班时间" />
      </el-form-item>
      <el-form-item label="下班打卡时间" prop="clockOutTime">
        <el-time-picker v-model="formData.clockOutTime" format="HH:mm:ss" value-format="HH:mm:ss" placeholder="下班时间" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="formData.status" placeholder="请选择考勤状态">
          <el-option label="正常" :value="0" />
          <el-option label="迟到" :value="1" />
          <el-option label="早退" :value="2" />
          <el-option label="缺卡" :value="3" />
          <el-option label="请假" :value="4" />
        </el-select>
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" type="textarea" placeholder="请输入签卡或异常备注" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import * as AttendanceApi from '@/api/hr/attendance'
import * as EmployeeApi from '@/api/hr/employee'
import { onMounted } from 'vue'

const { t } = useI18n()
const employeeList = ref<any[]>([])
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const formData = ref({
  id: undefined,
  employeeId: undefined,
  recordDate: undefined,
  clockInTime: undefined,
  clockOutTime: undefined,
  status: 0,
  remark: undefined
})
const formRules = reactive({
  employeeId: [{ required: true, message: '员工必选', trigger: 'change' }],
  recordDate: [{ required: true, message: '日期必填', trigger: 'blur' }]
})
const formRef = ref()

onMounted(async () => {
  try {
    const res = await EmployeeApi.getEmployeePage({ pageNo: 1, pageSize: 1000 })
    employeeList.value = res.list
  } catch {}
})

const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  if (id) {
    formLoading.value = true
    try {
      formData.value = await AttendanceApi.getAttendance(id)
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
    const data = formData.value as unknown as AttendanceApi.AttendanceVO
    if (formType.value === 'create') {
      await AttendanceApi.createAttendance(data)
      message.success(t('common.createSuccess'))
    } else {
      await AttendanceApi.updateAttendance(data)
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
    employeeId: undefined,
    recordDate: undefined,
    clockInTime: undefined,
    clockOutTime: undefined,
    status: 0,
    remark: undefined
  }
  formRef.value?.resetFields()
}
</script>
