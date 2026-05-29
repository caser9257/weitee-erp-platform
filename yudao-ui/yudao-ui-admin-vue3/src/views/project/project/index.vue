<template>
  <div class="project-manage-page">
    <ContentWrap>
      <el-form ref="queryFormRef" :model="queryParams" :inline="true" label-width="84px">
        <el-form-item label="项目名称" prop="name">
          <el-input
            v-model="queryParams.name"
            clearable
            placeholder="请输入项目名称"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="归档状态" prop="archived">
          <el-select v-model="queryParams.archived" clearable placeholder="全部">
            <el-option label="未归档" :value="false" />
            <el-option label="已归档" :value="true" />
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
          <el-button type="primary" @click="handleCreate" v-hasPermi="['project:project:create']">
            <Icon class="mr-5px" icon="ep:plus" />
            新增项目
          </el-button>
        </el-col>
      </el-row>

      <el-table v-loading="loading" :data="list" :stripe="true">
        <el-table-column label="项目名称" prop="name" min-width="160" />
        <el-table-column label="负责人" prop="ownerUserName" width="120" />
        <el-table-column label="个人项目" prop="personal" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.personal" type="success" size="small">是</el-tag>
            <el-tag v-else type="info" size="small">否</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="归档状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.archivedAt" type="warning" size="small">已归档</el-tag>
            <el-tag v-else type="primary" size="small">进行中</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="180" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleDetail(row)">
              详情
            </el-button>
            <el-button link type="primary" @click="handleUpdate(row)" v-hasPermi="['project:project:update']">
              编辑
            </el-button>
            <el-button
              link
              type="primary"
              @click="handleArchive(row)"
              v-if="!row.archivedAt"
              v-hasPermi="['project:project:update']"
            >
              归档
            </el-button>
            <el-button
              link
              type="primary"
              @click="handleArchive(row)"
              v-else
              v-hasPermi="['project:project:update']"
            >
              取消归档
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)" v-hasPermi="['project:project:delete']">
              删除
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

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="项目名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入项目名称" />
        </el-form-item>
        <el-form-item label="项目描述" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="请输入项目描述" />
        </el-form-item>
        <el-form-item label="个人项目" prop="personal">
          <el-switch v-model="form.personal" />
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
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { ProjectManageApi, type ProjectVO } from '@/api/project/project'

defineOptions({ name: 'ProjectManage' })

const router = useRouter()
const message = useMessage()

const loading = ref(false)
const list = ref<ProjectVO[]>([])
const total = ref(0)
const queryFormRef = ref<FormInstance>()
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: undefined as string | undefined,
  archived: undefined as boolean | undefined
})

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const submitting = ref(false)
const form = reactive({
  id: undefined as number | undefined,
  name: '',
  description: '',
  personal: false
})
const rules = reactive<FormRules>({
  name: [
    { required: true, message: '请输入项目名称', trigger: 'blur' },
    { min: 2, max: 32, message: '长度在 2 到 32 个字符', trigger: 'blur' }
  ]
})

const getList = async () => {
  loading.value = true
  try {
    const data = await ProjectManageApi.getProjectPage(queryParams)
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
  dialogTitle.value = '新增项目'
  form.id = undefined
  form.name = ''
  form.description = ''
  form.personal = false
  dialogVisible.value = true
}

const handleDetail = (row: ProjectVO) => {
  router.push(`/project/project/detail/${row.id}`)
}

const handleUpdate = (row: ProjectVO) => {
  dialogTitle.value = '编辑项目'
  form.id = row.id
  form.name = row.name
  form.description = row.description
  form.personal = row.personal
  dialogVisible.value = true
}

const submitForm = async () => {
  const valid = await formRef.value?.validate().then(() => true).catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (form.id) {
      await ProjectManageApi.updateProject(form)
      message.success('更新成功')
    } else {
      await ProjectManageApi.createProject(form)
      message.success('创建成功')
    }
    dialogVisible.value = false
    getList()
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (row: ProjectVO) => {
  try {
    await message.delConfirm()
    await ProjectManageApi.deleteProject(row.id)
    message.success('删除成功')
    getList()
  } catch {}
}

const handleArchive = async (row: ProjectVO) => {
  const archive = !row.archivedAt
  const msg = archive ? '确认归档该项目？' : '确认取消归档该项目？'
  try {
    await message.confirm(msg)
    await ProjectManageApi.archiveProject(row.id, archive)
    message.success(archive ? '归档成功' : '取消归档成功')
    getList()
  } catch {}
}

onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.project-manage-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
</style>
