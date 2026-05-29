<template>
  <ContentWrap>
    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="68px">
      <el-form-item label="员工姓名" prop="employeeName">
        <el-input v-model="queryParams.employeeName" placeholder="请输入员工姓名" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="考勤日期" prop="recordDate">
        <el-date-picker v-model="queryParams.recordDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option label="正常" :value="0" />
          <el-option label="迟到" :value="1" />
          <el-option label="早退" :value="2" />
          <el-option label="缺卡" :value="3" />
          <el-option label="请假" :value="4" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" />搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" />重置</el-button>
        <el-button type="primary" plain @click="openForm('create')">
          <Icon icon="ep:plus" /> 手工补卡
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list" :stripe="true">
      <el-table-column label="记录编号" align="center" prop="id" />
      <el-table-column label="员工姓名" align="center" prop="employeeName" />
      <el-table-column label="考勤日期" align="center" prop="recordDate" />
      <el-table-column label="上班打卡时间" align="center" prop="clockInTime" />
      <el-table-column label="下班打卡时间" align="center" prop="clockOutTime" />
      <el-table-column label="考勤状态" align="center" prop="status">
        <template #default="scope">
          <!-- 简单展示，实际可用 dict-tag 更完美 -->
          <el-tag v-if="scope.row.status === 0" type="success">正常</el-tag>
          <el-tag v-else-if="scope.row.status === 1" type="warning">迟到</el-tag>
          <el-tag v-else-if="scope.row.status === 2" type="warning">早退</el-tag>
          <el-tag v-else-if="scope.row.status === 3" type="danger">缺卡</el-tag>
          <el-tag v-else type="info">请假</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="150px">
        <template #default="scope">
          <el-button link type="primary" @click="openForm('update', scope.row.id)">修改</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </ContentWrap>

  <AttendanceForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import * as AttendanceApi from '@/api/hr/attendance'
import AttendanceForm from './AttendanceForm.vue'

defineOptions({ name: 'HrAttendance' })

const message = useMessage()
const { t } = useI18n()
const loading = ref(true)
const list = ref([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  employeeName: undefined,
  recordDate: undefined,
  status: undefined
})
const queryFormRef = ref()

const getList = async () => {
  loading.value = true
  try {
    const data = await AttendanceApi.getAttendancePage(queryParams)
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
    await AttendanceApi.deleteAttendance(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

onMounted(() => {
  getList()
})
</script>
