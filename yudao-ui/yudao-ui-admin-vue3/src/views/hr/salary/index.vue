<template>
  <ContentWrap>
    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="68px">
      <el-form-item label="员工姓名" prop="employeeName">
        <el-input v-model="queryParams.employeeName" placeholder="请输入员工姓名" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="薪资类型" prop="salaryType">
        <el-select v-model="queryParams.salaryType" placeholder="薪资类型" clearable class="!w-150px">
          <el-option label="固定薪资" :value="1" />
          <el-option label="计件薪资" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="发薪月份" prop="month">
        <el-date-picker v-model="queryParams.month" type="month" value-format="YYYY-MM" placeholder="选择月份" class="!w-150px" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable class="!w-150px">
          <el-option label="待发" :value="0" />
          <el-option label="已发" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" />搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" />重置</el-button>
        <el-button type="primary" plain @click="openForm('create')">
          <Icon icon="ep:plus" /> 手工录单
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list" :stripe="true">
      <el-table-column label="工资单号" align="center" prop="id" />
      <el-table-column label="员工姓名" align="center" prop="employeeName" />
      <el-table-column label="薪资类型" align="center" prop="salaryType">
        <template #default="scope">
          <el-tag v-if="scope.row.salaryType === 1" type="primary">固定薪资</el-tag>
          <el-tag v-else-if="scope.row.salaryType === 2" type="warning">计件薪资</el-tag>
          <span v-else>未知</span>
        </template>
      </el-table-column>
      <el-table-column label="发薪月份" align="center" prop="month" />
      <el-table-column label="基本工资" align="center" prop="baseSalary" />
      <el-table-column label="绩效奖金" align="center" prop="performanceSalary" />
      <el-table-column label="计件薪水" align="center" prop="pieceRateSalary">
        <template #default="scope">
          <span v-if="scope.row.salaryType === 2">{{ scope.row.pieceRateSalary }}</span>
          <span v-else style="color: #999;">-</span>
        </template>
      </el-table-column>
      <el-table-column label="各项扣款" align="center" prop="deduction" />
      <el-table-column label="实发总额" align="center" prop="actualTotal" />
      <el-table-column label="发放状态" align="center" prop="status">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 0" type="info">待发放</el-tag>
          <el-tag v-else type="success">已发放</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="150px">
        <template #default="scope">
          <el-button link type="primary" @click="openForm('update', scope.row.id)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </ContentWrap>

  <SalaryForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import * as SalaryApi from '@/api/hr/salary'
import SalaryForm from './SalaryForm.vue'

defineOptions({ name: 'HrSalary' })

const message = useMessage()
const { t } = useI18n()
const loading = ref(true)
const list = ref([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  employeeName: undefined,
  salaryType: undefined,
  month: undefined,
  status: undefined
})
const queryFormRef = ref()

const getList = async () => {
  loading.value = true
  try {
    const data = await SalaryApi.getSalaryPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryFormRef.value.resetFields()
  handleQuery()
}

const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await SalaryApi.deleteSalary(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

onMounted(() => {
  getList()
})
</script>
