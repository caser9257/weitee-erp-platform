<template>
  <div class="report-page">
    <ContentWrap>
      <el-row :gutter="10" class="mb-10px">
        <el-col :span="1.5">
          <el-button type="primary" @click="handleCreate">
            <Icon class="mr-5px" icon="ep:plus" />
            提交日报
          </el-button>
        </el-col>
      </el-row>

      <el-table v-loading="loading" :data="list" :stripe="true">
        <el-table-column label="类型" prop="type" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.type === 'daily'" size="small">日报</el-tag>
            <el-tag v-else-if="row.type === 'weekly'" type="warning" size="small">周报</el-tag>
            <el-tag v-else type="success" size="small">月报</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="内容" prop="content" min-width="200" show-overflow-tooltip />
        <el-table-column label="提交时间" prop="createTime" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">查看</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination
        v-model:limit="queryParams.pageSize"
        v-model:page="queryParams.pageNo"
        :total="total"
        @pagination="getList"
      />
    </ContentWrap>

    <el-dialog title="提交日报" v-model="dialogVisible" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type">
            <el-option label="日报" value="daily" />
            <el-option label="周报" value="weekly" />
            <el-option label="月报" value="monthly" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="6"
            placeholder="请输入日报内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ReportApi } from '@/api/project/report'

defineOptions({ name: 'MyReport' })

const message = useMessage()

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const submitting = ref(false)
const queryParams = reactive({ pageNo: 1, pageSize: 10 })
const form = reactive({ type: 'daily', content: '' })
const rules = reactive<FormRules>({
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
})

const getList = async () => {
  loading.value = true
  try {
    const data = await ReportApi.getReportPage(queryParams)
    list.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const handleCreate = () => {
  form.type = 'daily'
  form.content = ''
  dialogVisible.value = true
}

const submitForm = async () => {
  const valid = await formRef.value?.validate().then(() => true).catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await ReportApi.createReport(form)
    message.success('提交成功')
    dialogVisible.value = false
    getList()
  } finally {
    submitting.value = false
  }
}

const handleView = (row: any) => {
  message.alert(row.content, '日报内容')
}

const handleDelete = async (row: any) => {
  try {
    await message.delConfirm()
    await ReportApi.deleteReport(row.id)
    message.success('删除成功')
    getList()
  } catch {}
}

onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.report-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
</style>
