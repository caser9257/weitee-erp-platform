<template>
  <ContentWrap>
    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px">
      <el-form-item label="文件ID" prop="fileId">
        <el-input v-model="queryParams.fileId" placeholder="请输入文件ID" clearable @keyup.enter="handleQuery" class="!w-200px" />
      </el-form-item>
      <el-form-item label="统计日期" prop="statsDate">
        <el-date-picker v-model="queryParams.statsDate" value-format="YYYY-MM-DD" type="daterange" start-placeholder="开始日期" end-placeholder="结束日期" class="!w-240px" />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 查询</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column label="文件ID" align="center" prop="fileId" width="80" />
      <el-table-column label="统计日期" align="center" prop="statsDate" width="120" />
      <el-table-column label="查看次数" align="center" prop="viewCount" width="120">
        <template #default="{ row }">
          <span class="font-mono text-right block">{{ row.viewCount }}</span>
        </template>
      </el-table-column>
      <el-table-column label="下载次数" align="center" prop="downloadCount" width="120">
        <template #default="{ row }">
          <span class="font-mono text-right block">{{ row.downloadCount }}</span>
        </template>
      </el-table-column>
      <el-table-column label="独立访客" align="center" prop="uniqueVisitorCount" width="120">
        <template #default="{ row }">
          <span class="font-mono text-right block">{{ row.uniqueVisitorCount }}</span>
        </template>
      </el-table-column>
    </el-table>
  </ContentWrap>
</template>

<script lang="ts" setup>
import * as FileAccessStatsApi from '@/api/infra/fileAccessStats'

defineOptions({ name: 'InfraFileAccessStats' })

const loading = ref(true)
const list = ref<FileAccessStatsApi.FileAccessStatsVO[]>([])
const queryParams = reactive({ fileId: undefined, statsDate: [] })
const queryFormRef = ref()

const getList = async () => {
  loading.value = true
  try {
    if (queryParams.fileId) {
      const data = await FileAccessStatsApi.getFileAccessStats(Number(queryParams.fileId))
      list.value = data
    } else {
      list.value = []
    }
  } finally {
    loading.value = false
  }
}

const handleQuery = () => { getList() }
const resetQuery = () => { queryFormRef.value.resetFields(); list.value = [] }

onMounted(() => { getList() })
</script>