<template>
  <div class="meeting-page">
    <ContentWrap>
      <el-form ref="queryFormRef" :model="queryParams" :inline="true" label-width="84px">
        <el-form-item label="会议名称" prop="name">
          <el-input
            v-model="queryParams.name"
            clearable
            placeholder="请输入会议名称"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <Icon class="mr-5px" icon="ep:search" />
            搜索
          </el-button>
          <el-button @click="resetQuery">
            <Icon class="mr-5px" icon="ep:refresh" />
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </ContentWrap>

    <ContentWrap>
      <el-row :gutter="10" class="mb-10px">
        <el-col :span="1.5">
          <el-button type="primary" @click="handleCreate">
            <Icon class="mr-5px" icon="ep:plus" />
            创建会议
          </el-button>
        </el-col>
      </el-row>

      <el-table v-loading="loading" :data="list" :stripe="true">
        <el-table-column label="会议名称" prop="name" min-width="160" />
        <el-table-column label="描述" prop="description" min-width="200" show-overflow-tooltip />
        <el-table-column label="开始时间" prop="startAt" width="180" />
        <el-table-column label="结束时间" prop="endAt" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleUpdate(row)">编辑</el-button>
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

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="会议名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入会议名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="时间范围" prop="timeRange">
          <el-date-picker
            v-model="form.timeRange"
            type="datetimerange"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { MeetingApi } from '@/api/project/meeting'

defineOptions({ name: 'MeetingManage' })

const message = useMessage()

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const queryFormRef = ref<FormInstance>()
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: undefined as string | undefined
})

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const submitting = ref(false)
const form = reactive({
  id: undefined as number | undefined,
  name: '',
  description: '',
  timeRange: [] as any[]
})
const rules = reactive<FormRules>({
  name: [{ required: true, message: '请输入会议名称', trigger: 'blur' }]
})

const getList = async () => {
  loading.value = true
  try {
    const data = await MeetingApi.getMeetingPage(queryParams)
    list.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryFormRef.value?.resetFields()
  handleQuery()
}

const handleCreate = () => {
  dialogTitle.value = '创建会议'
  form.id = undefined
  form.name = ''
  form.description = ''
  form.timeRange = []
  dialogVisible.value = true
}

const handleUpdate = (row: any) => {
  dialogTitle.value = '编辑会议'
  form.id = row.id
  form.name = row.name
  form.description = row.description
  dialogVisible.value = true
}

const submitForm = async () => {
  const valid = await formRef.value?.validate().then(() => true).catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const data: any = { ...form }
    if (form.timeRange?.length === 2) {
      data.startAt = form.timeRange[0]
      data.endAt = form.timeRange[1]
    }
    if (form.id) {
      await MeetingApi.updateMeeting(data)
      message.success('更新成功')
    } else {
      await MeetingApi.createMeeting(data)
      message.success('创建成功')
    }
    dialogVisible.value = false
    getList()
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (row: any) => {
  try {
    await message.delConfirm()
    await MeetingApi.deleteMeeting(row.id)
    message.success('删除成功')
    getList()
  } catch {}
}

onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.meeting-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
</style>
