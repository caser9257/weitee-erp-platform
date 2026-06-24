<template>
  <ContentWrap title="文件操作日志">
    <!-- 搜索 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="80px"
    >
      <el-form-item label="文件ID" prop="fileId">
        <el-input
          v-model="queryParams.fileId"
          placeholder="请输入文件ID"
          clearable
          @keyup.enter="handleQuery"
          class="!w-160px"
        />
      </el-form-item>
      <el-form-item label="操作类型" prop="operation">
        <el-select v-model="queryParams.operation" placeholder="请选择" clearable class="!w-160px">
          <el-option label="上传" value="UPLOAD" />
          <el-option label="下载" value="DOWNLOAD" />
          <el-option label="删除" value="DELETE" />
          <el-option label="查看" value="VIEW" />
          <el-option label="恢复" value="RESTORE" />
          <el-option label="回滚" value="ROLLBACK" />
          <el-option label="永久删除" value="PERMANENT_DELETE" />
        </el-select>
      </el-form-item>
      <el-form-item label="操作结果" prop="result">
        <el-select v-model="queryParams.result" placeholder="请选择" clearable class="!w-120px">
          <el-option label="成功" :value="0" />
          <el-option label="失败" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="操作时间" prop="createTime">
        <el-date-picker
          v-model="queryParams.createTime"
          value-format="YYYY-MM-DD HH:mm:ss"
          type="daterange"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column label="日志ID" prop="id" width="80" align="center" />
      <el-table-column label="文件ID" prop="fileId" width="80" align="center">
        <template #default="{ row }">
          <el-link type="primary" @click="handleViewFile(row.fileId)">{{ row.fileId }}</el-link>
        </template>
      </el-table-column>
      <el-table-column label="文件名" prop="fileName" min-width="150" :show-overflow-tooltip="true" />
      <el-table-column label="操作类型" prop="operation" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getOperationTagType(row.operation)" size="small">
            {{ getOperationLabel(row.operation) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作人" prop="userName" width="100" align="center" />
      <el-table-column label="操作IP" prop="ip" width="130" align="center" />
      <el-table-column label="操作说明" prop="description" min-width="200" :show-overflow-tooltip="true" />
      <el-table-column label="操作结果" prop="result" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.result === 0 ? 'success' : 'danger'" size="small">
            {{ row.result === 0 ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="失败原因" prop="failReason" min-width="150" :show-overflow-tooltip="true">
        <template #default="{ row }">
          <span v-if="row.failReason" class="text-[var(--erp-danger-500)]">{{ row.failReason }}</span>
          <span v-else class="text-[var(--erp-slate-400)]">-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作时间" prop="createTime" width="180" align="center" :formatter="dateFormatter" />
    </el-table>
    <!-- 分页 -->
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>
</template>

<script lang="ts" setup>
import { dateFormatter } from '@/utils/formatTime'
import * as FileOperationLogApi from '@/api/infra/fileOperationLog'
import type { FileOperationLogVO, FileOperationLogPageReqVO } from '@/api/infra/fileOperationLog'

defineOptions({ name: 'InfraFileOperationLog' })

const router = useRouter()

const loading = ref(false)
const total = ref(0)
const list = ref<FileOperationLogVO[]>([])
const queryFormRef = ref()

const queryParams = reactive<FileOperationLogPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  fileId: undefined,
  operation: undefined,
  result: undefined,
  createTime: []
})

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await FileOperationLogApi.getFileOperationLogPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
  handleQuery()
}

/** 查看文件详情 */
const handleViewFile = (fileId: number) => {
  router.push({ path: '/infra/file', query: { id: fileId } })
}

/** 获取操作类型标签 */
const getOperationLabel = (operation: string) => {
  const map: Record<string, string> = {
    UPLOAD: '上传',
    DOWNLOAD: '下载',
    DELETE: '删除',
    VIEW: '查看',
    RESTORE: '恢复',
    ROLLBACK: '回滚',
    PERMANENT_DELETE: '永久删除'
  }
  return map[operation] || operation
}

/** 获取操作类型标签样式 */
const getOperationTagType = (operation: string) => {
  const map: Record<string, string> = {
    UPLOAD: 'success',
    DOWNLOAD: 'primary',
    DELETE: 'danger',
    VIEW: 'info',
    RESTORE: 'warning',
    ROLLBACK: 'warning',
    PERMANENT_DELETE: 'danger'
  }
  return map[operation] || 'info'
}

/** 初始化 */
onMounted(() => {
  getList()
})
</script>
