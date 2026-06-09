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
      <el-form-item label="方案名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入方案名称"
          clearable
          @keyup.enter="handleQuery"
          class="!w-200px"
        />
      </el-form-item>
      <el-form-item label="模块编码" prop="moduleCode">
        <el-input
          v-model="queryParams.moduleCode"
          placeholder="请输入模块编码"
          clearable
          @keyup.enter="handleQuery"
          class="!w-200px"
        />
      </el-form-item>
      <el-form-item label="版本状态" prop="latestVersionStatus">
        <el-select
          v-model="queryParams.latestVersionStatus"
          placeholder="请选择状态"
          clearable
          class="!w-200px"
        >
          <el-option label="草稿" :value="10" />
          <el-option label="待发布" :value="20" />
          <el-option label="生效中" :value="30" />
          <el-option label="已停用" :value="40" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button type="primary" plain @click="openForm('create')" v-hasPermi="['bpm:approval-scheme:create']">
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true">
      <el-table-column label="方案编码" align="center" prop="code" min-width="150" />
      <el-table-column label="方案名称" align="center" prop="name" min-width="150" />
      <el-table-column label="模块编码" align="center" prop="moduleCode" width="120" />
      <el-table-column label="业务类型" align="center" prop="bizType" width="100" />
      <el-table-column label="最新版本号" align="center" prop="latestVersionNo" width="100" />
      <el-table-column label="版本状态" align="center" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.latestVersionStatus === 10" type="info" effect="light">草稿</el-tag>
          <el-tag v-else-if="row.latestVersionStatus === 20" type="warning" effect="light">待发布</el-tag>
          <el-tag v-else-if="row.latestVersionStatus === 30" type="success" effect="light">生效中</el-tag>
          <el-tag v-else-if="row.latestVersionStatus === 40" type="danger" effect="light">已停用</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="170" />
      <el-table-column label="操作" align="center" width="280" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="openForm('update', scope.row.id)" v-hasPermi="['bpm:approval-scheme:update']">
            编辑
          </el-button>
          <el-button
            v-if="scope.row.latestVersionStatus === 10"
            link type="warning"
            @click="handleSubmit(scope.row)"
            v-hasPermi="['bpm:approval-scheme:update']"
          >提交</el-button>
          <el-button
            v-if="scope.row.latestVersionStatus === 20"
            link type="success"
            @click="handlePublish(scope.row)"
            v-hasPermi="['bpm:approval-scheme:publish']"
          >发布</el-button>
          <el-button
            v-if="scope.row.latestVersionStatus === 30"
            link type="danger"
            @click="handleDisable(scope.row)"
            v-hasPermi="['bpm:approval-scheme:publish']"
          >停用</el-button>
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
  <SchemeForm ref="formRef" @success="getList" />
</template>

<script lang="ts" setup>
import { ref, reactive } from 'vue'
import { ElMessageBox } from 'element-plus'
import { useMessage } from '@/hooks/web/useMessage'
import * as ApprovalSchemeApi from '@/api/bpm/approval/scheme'
import SchemeForm from './SchemeForm.vue'

defineOptions({ name: 'BpmApprovalScheme' })

const { message } = useMessage()

const loading = ref(true)
const list = ref<ApprovalSchemeApi.ApprovalSchemeVO[]>([])
const total = ref(0)
const queryParams = reactive<ApprovalSchemeApi.ApprovalSchemePageReqVO>({
  name: undefined,
  moduleCode: undefined,
  latestVersionStatus: undefined,
  pageNo: 1,
  pageSize: 10
})
const queryFormRef = ref()

const getList = async () => {
  loading.value = true
  try {
    const data = await ApprovalSchemeApi.getApprovalSchemePage(queryParams)
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

const handleSubmit = async (row: ApprovalSchemeApi.ApprovalSchemeVO) => {
  try {
    await ElMessageBox.confirm('确认提交该审批方案？', '提示', { type: 'warning' })
    await ApprovalSchemeApi.submitApprovalScheme({ versionId: row.latestVersionId! })
    message.success('提交成功')
    getList()
  } catch {}
}

const handlePublish = async (row: ApprovalSchemeApi.ApprovalSchemeVO) => {
  try {
    await ElMessageBox.confirm('确认发布该审批方案？发布后立即生效。', '提示', { type: 'warning' })
    await ApprovalSchemeApi.publishApprovalScheme({ versionId: row.latestVersionId! })
    message.success('发布成功')
    getList()
  } catch {}
}

const handleDisable = async (row: ApprovalSchemeApi.ApprovalSchemeVO) => {
  try {
    await ElMessageBox.confirm('确认停用该审批方案？', '提示', { type: 'warning' })
    await ApprovalSchemeApi.disableApprovalScheme(row.latestVersionId!)
    message.success('停用成功')
    getList()
  } catch {}
}

getList()
</script>
