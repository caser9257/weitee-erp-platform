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
      <el-form-item label="场景名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入场景名称"
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
      <el-form-item label="状态" prop="status">
        <el-select
          v-model="queryParams.status"
          placeholder="请选择状态"
          clearable
          class="!w-200px"
        >
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button type="primary" plain @click="openForm('create')" v-hasPermi="['bpm:approval-scene:create']">
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true">
      <el-table-column label="场景编码" align="center" prop="sceneCode" min-width="200" />
      <el-table-column label="场景名称" align="center" prop="name" min-width="150" />
      <el-table-column label="模块编码" align="center" prop="moduleCode" width="120" />
      <el-table-column label="业务类型" align="center" prop="bizType" width="100" />
      <el-table-column label="动作编码" align="center" prop="actionCode" width="100" />
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" effect="light">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="生效方案" align="center" prop="activeSchemeId" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.activeSchemeId" type="success" effect="plain">已绑定</el-tag>
          <el-tag v-else type="info" effect="plain">未绑定</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="170" />
      <el-table-column label="操作" align="center" width="200" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="openForm('update', scope.row.id)" v-hasPermi="['bpm:approval-scene:update']">
            编辑
          </el-button>
          <el-button link type="primary" @click="handleBindScheme(scope.row)" v-hasPermi="['bpm:approval-scheme:create']">
            绑定方案
          </el-button>
          <el-button link type="danger" @click="handleDelete(scope.row.id)" v-hasPermi="['bpm:approval-scene:delete']">
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
  <ApprovalSceneForm ref="formRef" @success="getList" />
</template>

<script lang="ts" setup>
import { ref, reactive } from 'vue'
import { ElMessageBox } from 'element-plus'
import { useI18n } from '@/hooks/web/useI18n'
import { useMessage } from '@/hooks/web/useMessage'
import { DICT_TYPE } from '@/utils/dict'
import * as ApprovalSceneApi from '@/api/bpm/approval/scene'
import ApprovalSceneForm from './ApprovalSceneForm.vue'

defineOptions({ name: 'BpmApprovalScene' })

const { t } = useI18n()
const { message } = useMessage()

const loading = ref(true)
const list = ref<ApprovalSceneApi.ApprovalSceneVO[]>([])
const total = ref(0)
const queryParams = reactive<ApprovalSceneApi.ApprovalScenePageReqVO>({
  name: undefined,
  moduleCode: undefined,
  status: undefined,
  pageNo: 1,
  pageSize: 10
})
const queryFormRef = ref()

// 查询列表
const getList = async () => {
  loading.value = true
  try {
    const data = await ApprovalSceneApi.getApprovalScenePage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

// 搜索
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

// 重置
const resetQuery = () => {
  queryFormRef.value.resetFields()
  handleQuery()
}

// 新增/编辑
const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

// 绑定方案
const handleBindScheme = (row: ApprovalSceneApi.ApprovalSceneVO) => {
  // TODO: 跳转到方案编辑页或弹窗选择方案
  message.info('绑定方案功能开发中')
}

// 删除
const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确认删除该审批场景？', '提示', {
      type: 'warning'
    })
    await ApprovalSceneApi.deleteApprovalScene(id)
    message.success('删除成功')
    getList()
  } catch {
    // 取消操作
  }
}

// 初始化
getList()
</script>
