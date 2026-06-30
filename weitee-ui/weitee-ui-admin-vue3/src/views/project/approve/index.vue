<template>
  <div class="approve-page">
    <ContentWrap>
      <el-form ref="queryFormRef" :model="queryParams" :inline="true" label-width="84px">
        <el-form-item label="审批标题" prop="name">
          <el-input
            v-model="queryParams.name"
            clearable
            placeholder="请输入审批标题"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="审批状态" prop="status">
          <el-select v-model="queryParams.status" clearable placeholder="全部">
            <el-option label="待审批" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已拒绝" :value="2" />
          </el-select>
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
            发起审批
          </el-button>
        </el-col>
      </el-row>

      <el-table v-loading="loading" :data="list" :stripe="true">
        <el-table-column label="审批标题" prop="name" min-width="160" />
        <el-table-column label="发起人" prop="creatorName" width="120" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="warning" size="small">待审批</el-tag>
            <el-tag v-else-if="row.status === 1" type="success" size="small">已通过</el-tag>
            <el-tag v-else type="danger" size="small">已拒绝</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">查看</el-button>
            <el-button
              link
              type="success"
              @click="handleApprove(row, 1)"
              v-if="row.status === 0"
            >
              通过
            </el-button>
            <el-button
              link
              type="danger"
              @click="handleApprove(row, 2)"
              v-if="row.status === 0"
            >
              拒绝
            </el-button>
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

    <el-dialog title="发起审批" v-model="dialogVisible" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="审批标题" prop="name">
          <el-input v-model="form.name" placeholder="请输入审批标题" />
        </el-form-item>
        <el-form-item label="审批内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="4"
            placeholder="请输入审批内容"
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
import { ApproveApi } from '@/api/project/approve'

defineOptions({ name: 'ApproveManage' })

const message = useMessage()

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const queryFormRef = ref<FormInstance>()
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: undefined as string | undefined,
  status: undefined as number | undefined
})

const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const submitting = ref(false)
const form = reactive({ name: '', content: '' })
const rules = reactive<FormRules>({
  name: [{ required: true, message: '请输入审批标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入审批内容', trigger: 'blur' }]
})

const getList = async () => {
  loading.value = true
  try {
    const data = await ApproveApi.getApprovePage(queryParams)
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
  form.name = ''
  form.content = ''
  dialogVisible.value = true
}

const submitForm = async () => {
  const valid = await formRef.value?.validate().then(() => true).catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await ApproveApi.createApprove(form)
    message.success('提交成功')
    dialogVisible.value = false
    getList()
  } finally {
    submitting.value = false
  }
}

const handleView = (row: any) => {
  message.alert(row.content || '无内容', '审批详情')
}

const handleApprove = async (row: any, status: number) => {
  const msg = status === 1 ? '确认通过该审批？' : '确认拒绝该审批？'
  try {
    await message.confirm(msg)
    await ApproveApi.approveAction({ id: row.id, status })
    message.success(status === 1 ? '已通过' : '已拒绝')
    getList()
  } catch {}
}

onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.approve-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
</style>
