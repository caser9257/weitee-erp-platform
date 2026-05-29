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
      
      <el-form-item label="发薪月份" prop="month">
        <el-date-picker v-model="formData.month" type="month" value-format="YYYY-MM" placeholder="选择月份" />
      </el-form-item>

      <el-form-item label="薪资类型" prop="salaryType">
        <el-radio-group v-model="formData.salaryType">
          <el-radio :label="1">固定薪资 (职能/管理)</el-radio>
          <el-radio :label="2">计件薪资 (制造组/生产车间)</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="基本工资/底薪" prop="baseSalary">
        <el-input-number v-model="formData.baseSalary" :precision="2" :step="100" :min="0" />
      </el-form-item>
      
      <el-form-item label="绩效奖金" prop="performanceSalary">
        <el-input-number v-model="formData.performanceSalary" :precision="2" :step="100" :min="0" />
      </el-form-item>
      
      <!-- 只有计件薪资类型(2)才显示计件薪水项 -->
      <el-form-item label="计件薪资录入" prop="pieceRateSalary" v-if="formData.salaryType === 2">
        <el-input-number v-model="formData.pieceRateSalary" :precision="2" :step="100" :min="0" />
        <span style="margin-left:10px; color:#999; font-size:12px;">自动读取或手工录入MES抛转的计件金额</span>
      </el-form-item>
      
      <el-form-item label="各项扣款" prop="deduction">
        <el-input-number v-model="formData.deduction" :precision="2" :step="10" :min="0" />
      </el-form-item>
      
      <el-form-item label="实发总额" prop="actualTotal">
        <el-input-number v-model="formData.actualTotal" :precision="2" disabled placeholder="自动计算" />
      </el-form-item>
      
      <el-form-item label="发放状态" prop="status">
        <el-radio-group v-model="formData.status" class="ml-4">
          <el-radio :label="0">待发</el-radio>
          <el-radio :label="1">已发</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="备注说明" prop="remark">
        <el-input v-model="formData.remark" type="textarea" placeholder="备注" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import * as SalaryApi from '@/api/hr/salary'
import * as EmployeeApi from '@/api/hr/employee'
import { watch, onMounted } from 'vue'

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
  month: undefined,
  salaryType: 1, // 默认固定工资
  baseSalary: 0,
  performanceSalary: 0,
  pieceRateSalary: 0,
  deduction: 0,
  actualTotal: 0,
  status: 0,
  remark: undefined
})

// 自动计算实发总额 (如果类型为1，即使有之前的计件也不算入)
watch(
  () => [formData.value.baseSalary, formData.value.performanceSalary, formData.value.pieceRateSalary, formData.value.deduction, formData.value.salaryType],
  ([base, perf, piece, ded, type]) => {
    let _piece = type === 2 ? (piece || 0) : 0
    formData.value.actualTotal = (base || 0) + (perf || 0) + _piece - (ded || 0)
  }
)

const formRules = reactive({
  employeeId: [{ required: true, message: '员工必选', trigger: 'change' }],
  month: [{ required: true, message: '月份必填', trigger: 'blur' }]
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
      formData.value = await SalaryApi.getSalary(id)
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
    const data = formData.value as unknown as SalaryApi.SalaryVO
    // 安全起见提交前格式化
    if (data.salaryType === 1) {
       data.pieceRateSalary = 0
    }

    if (formType.value === 'create') {
      await SalaryApi.createSalary(data)
      message.success(t('common.createSuccess'))
    } else {
      await SalaryApi.updateSalary(data)
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
    month: undefined,
    salaryType: 1,
    baseSalary: 0,
    performanceSalary: 0,
    pieceRateSalary: 0,
    deduction: 0,
    actualTotal: 0,
    status: 0,
    remark: undefined
  }
  formRef.value?.resetFields()
}
</script>
