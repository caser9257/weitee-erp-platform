<template>
  <ContentWrap>
    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px">
      <el-form-item label="文件ID" prop="fileId">
        <el-input v-model="queryParams.fileId" placeholder="请输入文件ID" clearable @keyup.enter="handleQuery" class="!w-200px" />
      </el-form-item>
      <el-form-item label="操作类型" prop="operation">
        <el-select v-model="queryParams.operation" placeholder="请选择" clearable class="!w-160px">
          <el-option label="上传" value="UPLOAD" />
          <el-option label="下载" value="DOWNLOAD" />
          <el-option label="删除" value="DELETE" />
          <el-option label="查看" value="VIEW" />
          <el-option label="恢复" value="RESTORE" />
          <el-option label="回滚" value="ROLLBACK" />
        </el-select>
      </el-form-item>
      <el-form-item label="结果" prop="result">
        <el-select v-model="queryParams.result" placeholder="请选择" clearable class="!w-160px">
          <el-option label="成功" :value="0" />
          <el-option label="失败" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="时间" prop="createTime">
        <el-date-picker v-model="queryParams.createTime" value-format="YYYY-MM-DD HH:mm:ss" type="daterange" start-placeholder="开始日期" end-placeholder="结束日期" :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]" class="!w-240px" />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column label="编号" align="center" prop="id" width="80" />
      <el-table-column label="文件ID" align="center" prop="fileId" width="80" />
      <el-table-column label="文件名称" align="center" prop="fileName" min-width="180" :show-overflow-tooltip="true" />
      <el-table-column label="操作类型" align="center" width="120">
        <template #default="{ row }">
          <el-tag :type="operationType(row.operation) as any" size="small">{{ operationLabel(row.operation) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作人" align="center" prop="userName" width="120" />
      <el-table-column label="操作IP" align="center" prop="ip" width="140" />
      <el-table-column label="结果" align="center" width="80">
        <template #default="{ row }">
          <el-tag :type="row.result === 0 ? 'success' : 'danger'" size="small">{{ row.result === 0 ? '成功' : '失败' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作说明" align="center" prop="description" min-width="200" :show-overflow-tooltip="true" />
      <el-table-column label="操作时间" align="center" prop="createTime" width="180" :formatter="dateFormatter" />
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </ContentWrap>
</template>

<script lang="ts" setup>
import { dateFormatter } from '@/utils/formatTime'
import * as FileOperationLogApi from '@/api/infra/fileOperationLog'

defineOptions({ name: 'InfraFileOperationLog' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const total = ref(0)
const list = ref<FileOperationLogApi.FileOperationLogVO[]>([])
const queryParams = reactive({ pageNo: 1, pageSize: 10, fileId: undefined, operation: undefined, result: undefined, createTime: [] })
const queryFormRef = ref()

const getList = async () => {
  loading.value = true
  try {
    const data = await FileOperationLogApi.getFileOperationLogPage(queryParams as any)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => { queryParams.pageNo = 1; getList() }
const resetQuery = () => { queryFormRef.value.resetFields(); handleQuery() }

const operationType = (op: string) => {
  const map: Record<string, string> = { UPLOAD: 'success', DOWNLOAD: 'primary', DELETE: 'danger', VIEW: 'info', RESTORE: 'warning', ROLLBACK: 'warning' }
  return map[op] || 'info'
}

const operationLabel = (op: string) => {
  const map: Record<string, string> = { UPLOAD: '上传', DOWNLOAD: '下载', DELETE: '删除', VIEW: '查看', RESTORE: '恢复', ROLLBACK: '回滚' }
  return map[op] || op
}

onMounted(() => { getList() })
</script>