<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="68px"
    >
      <el-form-item label="状态" prop="status">
        <el-select
          v-model="queryParams.status"
          placeholder="请选择状态"
          clearable
          class="!w-200px"
        >
          <el-option label="启用" :value="0" />
          <el-option label="停用" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button type="primary" plain @click="openForm('create')">
          <Icon icon="ep:plus" class="mr-5px" /> 新增委托
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true">
      <el-table-column label="委托人" align="center" prop="userId" width="120">
        <template #default="{ row }">
          {{ row.userId }}
        </template>
      </el-table-column>
      <el-table-column label="代理人" align="center" prop="delegateUserId" width="120">
        <template #default="{ row }">
          {{ row.delegateUserId }}
        </template>
      </el-table-column>
      <el-table-column label="开始时间" align="center" prop="startTime" width="170" />
      <el-table-column label="结束时间" align="center" prop="endTime" width="170" />
      <el-table-column label="适用场景" align="center" prop="sceneCode" min-width="150">
        <template #default="{ row }">
          <el-tag v-if="row.sceneCode" type="info" effect="plain">{{ row.sceneCode }}</el-tag>
          <el-tag v-else type="success" effect="plain">所有场景</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="委托原因" align="center" prop="reason" min-width="150" />
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'danger'" effect="light">
            {{ row.status === 0 ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="170" />
      <el-table-column label="操作" align="center" width="150" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="openForm('update', scope.row.id)">
            编辑
          </el-button>
          <el-button link type="danger" @click="handleDelete(scope.row.id)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页 -->
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>

  <!-- 表单弹窗 -->
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item label="代理人" prop="delegateUserId">
        <el-select
          v-model="formData.delegateUserId"
          placeholder="请选择代理人"
          filterable
          class="!w-full"
        >
          <!-- 用户列表需要从后端获取 -->
          <el-option label="用户1" :value="1" />
          <el-option label="用户2" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="开始时间" prop="startTime">
        <el-date-picker
          v-model="formData.startTime"
          type="datetime"
          placeholder="请选择开始时间"
          class="!w-full"
        />
      </el-form-item>
      <el-form-item label="结束时间" prop="endTime">
        <el-date-picker
          v-model="formData.endTime"
          type="datetime"
          placeholder="请选择结束时间"
          class="!w-full"
        />
      </el-form-item>
      <el-form-item label="适用场景" prop="sceneCode">
        <el-select
          v-model="formData.sceneCode"
          placeholder="请选择场景（留空表示所有场景）"
          clearable
          filterable
          class="!w-full"
        >
          <!-- 场景列表需要从后端获取 -->
          <el-option label="所有场景" value="" />
        </el-select>
      </el-form-item>
      <el-form-item label="委托原因" prop="reason">
        <el-input
          v-model="formData.reason"
          type="textarea"
          placeholder="请输入委托原因"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="formLoading">确 定</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue'
import type { FormRules } from 'element-plus'
import { ElMessageBox } from 'element-plus'
import { useMessage } from '@/hooks/web/useMessage'
import * as ApprovalDelegationApi from '@/api/bpm/approval/delegation'

defineOptions({ name: 'BpmApprovalDelegation' })

const { message } = useMessage()

const loading = ref(true)
const list = ref<ApprovalDelegationApi.ApprovalDelegationVO[]>([])
const total = ref(0)
const queryParams = reactive<ApprovalDelegationApi.ApprovalDelegationPageReqVO>({
  status: undefined,
  pageNo: 1,
  pageSize: 10
})
const queryFormRef = ref()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formRef = ref()

const formData = reactive<ApprovalDelegationApi.ApprovalDelegationSaveReqVO>({
  id: undefined,
  userId: 0, // 当前登录用户
  delegateUserId: 0,
  startTime: '',
  endTime: '',
  sceneCode: undefined,
  reason: ''
})

const formRules: FormRules = {
  delegateUserId: [{ required: true, message: '请选择代理人', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

const getList = async () => {
  loading.value = true
  try {
    const data = await ApprovalDelegationApi.getApprovalDelegationPage(queryParams)
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

const openForm = (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增审批委托' : '编辑审批委托'
  resetForm()
  if (id) {
    formLoading.value = true
    try {
      // TODO: 加载委托详情
    } finally {
      formLoading.value = false
    }
  }
}

const handleSubmit = async () => {
  await formRef.value.validate()
  formLoading.value = true
  try {
    if (formData.id) {
      await ApprovalDelegationApi.updateApprovalDelegation(formData)
      message.success('修改成功')
    } else {
      await ApprovalDelegationApi.createApprovalDelegation(formData)
      message.success('新增成功')
    }
    dialogVisible.value = false
    getList()
  } finally {
    formLoading.value = false
  }
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确认删除该委托？', '提示', { type: 'warning' })
    await ApprovalDelegationApi.deleteApprovalDelegation(id)
    message.success('删除成功')
    getList()
  } catch {}
}

const resetForm = () => {
  formData.id = undefined
  formData.delegateUserId = 0
  formData.startTime = ''
  formData.endTime = ''
  formData.sceneCode = undefined
  formData.reason = ''
  formRef.value?.resetFields()
}

onMounted(() => {
  getList()
})
</script>
