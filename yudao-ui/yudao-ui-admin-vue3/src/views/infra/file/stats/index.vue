<template>
  <ContentWrap title="文件访问统计">
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
      <el-form-item label="统计日期" prop="dateRange">
        <el-date-picker
          v-model="queryParams.dateRange"
          type="daterange"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 统计卡片 -->
  <div class="stats-cards">
    <el-row :gutter="16">
      <el-col :span="6">
        <ContentWrap class="stats-card">
          <div class="stats-card__icon bg-[var(--erp-primary-50)]">
            <Icon icon="ep:view" class="text-2xl text-[var(--erp-primary-500)]" />
          </div>
          <div class="stats-card__content">
            <div class="stats-card__value">{{ totalViewCount }}</div>
            <div class="stats-card__label">总查看次数</div>
          </div>
        </ContentWrap>
      </el-col>
      <el-col :span="6">
        <ContentWrap class="stats-card">
          <div class="stats-card__icon bg-[var(--erp-success-50)]">
            <Icon icon="ep:download" class="text-2xl text-[var(--erp-success-500)]" />
          </div>
          <div class="stats-card__content">
            <div class="stats-card__value">{{ totalDownloadCount }}</div>
            <div class="stats-card__label">总下载次数</div>
          </div>
        </ContentWrap>
      </el-col>
      <el-col :span="6">
        <ContentWrap class="stats-card">
          <div class="stats-card__icon bg-[var(--erp-warning-50)]">
            <Icon icon="ep:user" class="text-2xl text-[var(--erp-warning-500)]" />
          </div>
          <div class="stats-card__content">
            <div class="stats-card__value">{{ totalUniqueVisitor }}</div>
            <div class="stats-card__label">独立访客数</div>
          </div>
        </ContentWrap>
      </el-col>
      <el-col :span="6">
        <ContentWrap class="stats-card">
          <div class="stats-card__icon bg-[var(--erp-danger-50)]">
            <Icon icon="ep:document" class="text-2xl text-[var(--erp-danger-500)]" />
          </div>
          <div class="stats-card__content">
            <div class="stats-card__value">{{ statsList.length }}</div>
            <div class="stats-card__label">统计记录数</div>
          </div>
        </ContentWrap>
      </el-col>
    </el-row>
  </div>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="statsList">
      <el-table-column label="统计ID" prop="id" width="80" align="center" />
      <el-table-column label="文件ID" prop="fileId" width="80" align="center">
        <template #default="{ row }">
          <el-link type="primary" @click="handleViewFile(row.fileId)">{{ row.fileId }}</el-link>
        </template>
      </el-table-column>
      <el-table-column label="统计日期" prop="statsDate" width="120" align="center" />
      <el-table-column label="查看次数" prop="viewCount" width="100" align="right">
        <template #default="{ row }">
          <span class="font-mono text-[var(--erp-primary-600)]">{{ row.viewCount || 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="下载次数" prop="downloadCount" width="100" align="right">
        <template #default="{ row }">
          <span class="font-mono text-[var(--erp-success-600)]">{{ row.downloadCount || 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="独立访客" prop="uniqueVisitorCount" width="100" align="right">
        <template #default="{ row }">
          <span class="font-mono text-[var(--erp-warning-600)]">{{ row.uniqueVisitorCount || 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="总访问量" min-width="120" align="right">
        <template #default="{ row }">
          <span class="font-mono font-bold text-[var(--erp-danger-600)]">
            {{ (row.viewCount || 0) + (row.downloadCount || 0) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" width="180" align="center" :formatter="dateFormatter" />
    </el-table>
  </ContentWrap>
</template>

<script lang="ts" setup>
import { dateFormatter } from '@/utils/formatTime'
import * as FileAccessStatsApi from '@/api/infra/fileAccessStats'

defineOptions({ name: 'InfraFileAccessStats' })

const router = useRouter()

const loading = ref(false)
const statsList = ref<any[]>([])
const queryFormRef = ref()

const queryParams = reactive({
  fileId: undefined as number | undefined,
  dateRange: [] as string[]
})

// 统计汇总
const totalViewCount = computed(() => {
  return statsList.value.reduce((sum, item) => sum + (item.viewCount || 0), 0)
})

const totalDownloadCount = computed(() => {
  return statsList.value.reduce((sum, item) => sum + (item.downloadCount || 0), 0)
})

const totalUniqueVisitor = computed(() => {
  return statsList.value.reduce((sum, item) => sum + (item.uniqueVisitorCount || 0), 0)
})

/** 查询列表 */
const getList = async () => {
  if (!queryParams.fileId) {
    statsList.value = []
    return
  }
  loading.value = true
  try {
    const data = await FileAccessStatsApi.getFileAccessStats(queryParams.fileId)
    statsList.value = data
  } finally {
    loading.value = false
  }
}

/** 搜索按钮操作 */
const handleQuery = () => {
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
  statsList.value = []
}

/** 查看文件详情 */
const handleViewFile = (fileId: number) => {
  router.push({ path: '/infra/file', query: { id: fileId } })
}
</script>

<style scoped lang="scss">
.stats-cards {
  margin-bottom: 16px;
}

.stats-card {
  :deep(.el-card__body) {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 20px;
  }

  &__icon {
    width: 56px;
    height: 56px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  &__content {
    flex: 1;
    min-width: 0;
  }

  &__value {
    font-size: 28px;
    font-weight: 700;
    color: var(--erp-slate-900);
    font-family: 'SF Mono', 'Monaco', 'Menlo', monospace;
    line-height: 1.2;
  }

  &__label {
    font-size: 14px;
    color: var(--erp-slate-500);
    margin-top: 4px;
  }
}
</style>
