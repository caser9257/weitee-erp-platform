<template>
  <div class="approval-page approval-scheme-page">
    <!-- 页头卡片 -->
    <ContentWrap class="approval-page__header-card">
      <div class="approval-page__page-header">
        <div class="approval-page__page-title">审批方案</div>
        <div class="approval-page__metric-grid">
          <div v-for="card in summaryCards" :key="card.label" class="approval-page__metric-card" :class="card.toneClass">
            <div class="approval-page__metric-icon" :class="card.toneClass">
              <Icon :icon="card.icon" />
            </div>
            <div class="approval-page__metric-body">
              <div class="approval-page__metric-label">{{ card.label }}</div>
              <div class="approval-page__metric-value">{{ card.value }}</div>
            </div>
          </div>
        </div>
      </div>
    </ContentWrap>

    <!-- 筛选卡片 -->
    <ContentWrap class="approval-page__filter-card">
      <div class="approval-page__section-head">
        <div class="approval-page__section-title">筛选条件</div>
      </div>
      <el-form ref="queryFormRef" :model="queryParams" label-width="84px" class="approval-page__query-form" @submit.prevent>
        <div class="approval-page__query-grid">
          <el-form-item label="方案编码" prop="code">
            <el-input v-model="queryParams.code" placeholder="请输入方案编码" clearable class="!w-full" @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="方案名称" prop="name">
            <el-input v-model="queryParams.name" placeholder="请输入方案名称" clearable class="!w-full" @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="模块编码" prop="moduleCode">
            <el-input v-model="queryParams.moduleCode" placeholder="请输入模块编码" clearable class="!w-full" @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="最新版本状态" prop="latestVersionStatus">
            <el-select v-model="queryParams.latestVersionStatus" placeholder="请选择状态" clearable class="!w-full">
              <el-option v-for="item in SCHEME_STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </div>
        <div class="approval-page__query-actions">
          <el-button type="primary" :loading="loading" @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" />
            查询
          </el-button>
          <el-button @click="resetQuery">
            <Icon icon="ep:refresh-left" class="mr-5px" />
            重置
          </el-button>
        </div>
      </el-form>
    </ContentWrap>

    <!-- 表格卡片 -->
    <ContentWrap class="approval-page__table-card">
      <div class="approval-page__toolbar">
        <div class="approval-page__toolbar-main">
          <div class="approval-page__section-title">方案列表</div>
          <div class="approval-page__toolbar-count">当前共 <strong>{{ total }}</strong> 条</div>
        </div>
        <div class="approval-page__toolbar-actions">
          <el-button type="primary" plain @click="openForm('create')" v-hasPermi="['bpm:approval-scheme:create']">
            <Icon icon="ep:plus" class="mr-5px" />
            新增方案
          </el-button>
        </div>
      </div>

      <div class="approval-page__table-wrap">
        <el-table v-loading="loading" :data="list" row-key="id" stripe :show-overflow-tooltip="false" class="approval-page__table">
          <el-table-column min-width="260">
            <template #header>
              <span class="approval-page__column-header">
                <Icon icon="ep:connection" class="approval-page__column-icon" />
                审批方案
              </span>
            </template>
            <template #default="{ row }">
              <div class="approval-page__primary-cell">
                <span class="approval-page__primary-text">
                  {{ row.name || '-' }}
                  <el-tag v-if="row.activeVersionId" size="small" type="success" effect="light" class="ml-8px">生效中</el-tag>
                </span>
                <span class="approval-page__muted-text approval-page__mono">{{ row.code || '-' }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column min-width="180">
            <template #header>
              <span class="approval-page__column-header approval-page__column-header--pipeline">
                <Icon icon="ep:connection" class="approval-page__column-icon" />
                业务定位
              </span>
            </template>
            <template #default="{ row }">
              <div class="approval-page__primary-cell">
                <span class="approval-page__primary-text approval-page__mono">{{ row.moduleCode || '-' }}</span>
                <span class="approval-page__muted-text">{{ row.bizType || '-' }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column min-width="110" label="最新版本" align="right">
            <template #default="{ row }">
              <span class="approval-page__mono">v{{ row.latestVersionNo ?? '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column min-width="110" label="版本状态">
            <template #default="{ row }">
              <span class="approval-page__pill" :class="statusMeta(row.latestVersionStatus).tone">
                {{ statusMeta(row.latestVersionStatus).label }}
              </span>
            </template>
          </el-table-column>
          <el-table-column min-width="200" label="备注">
            <template #default="{ row }">
              <span class="approval-page__muted-text approval-page__ellipsis">{{ row.remark || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column min-width="170" label="创建时间" align="right">
            <template #default="{ row }">
              <span class="approval-page__mono approval-page__muted-text">{{ formatDate(row.createTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column min-width="280" label="操作" align="center" fixed="right">
            <template #default="{ row }">
              <el-button
                v-if="row.latestVersionStatus === 10"
                link
                type="primary"
                @click="openForm('update', row.id)"
                v-hasPermi="['bpm:approval-scheme:update']"
              >
                编辑
              </el-button>
              <el-button
                v-if="row.latestVersionStatus === 10"
                link
                type="primary"
                @click="handleSubmit(row)"
                v-hasPermi="['bpm:approval-scheme:update']"
              >
                提交
              </el-button>
              <el-button
                v-if="row.latestVersionStatus === 20"
                link
                type="success"
                @click="handlePublish(row)"
                v-hasPermi="['bpm:approval-scheme:publish']"
              >
                发布
              </el-button>
              <el-button
                v-if="row.latestVersionStatus === 30"
                link
                type="warning"
                @click="handleDisable(row)"
                v-hasPermi="['bpm:approval-scheme:publish']"
              >
                停用
              </el-button>
              <el-button
                v-if="row.latestVersionStatus === 30 && row.activeVersionId !== row.latestVersionId"
                link
                type="primary"
                @click="handleSwitch(row)"
                v-hasPermi="['bpm:approval-scheme:publish']"
              >
                切换生效
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <Pagination
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </ContentWrap>

    <!-- 表单弹窗 -->
    <ApprovalSchemeForm ref="formRef" @success="getList" />
  </div>
</template>

<script setup lang="ts">
import { formatDate } from '@/utils/formatTime'
import { ApprovalSchemeApi, BpmApprovalSchemeVO } from '@/api/bpm/approval/scheme'
import ApprovalSchemeForm from './ApprovalSchemeForm.vue'

/** 审批方案列表 */
defineOptions({ name: 'BpmApprovalScheme' })

const message = useMessage()

const loading = ref(true)
const list = ref<BpmApprovalSchemeVO[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  code: undefined,
  name: undefined,
  moduleCode: undefined,
  latestVersionStatus: undefined
})
const queryFormRef = ref()

const SCHEME_STATUS_OPTIONS = [
  { value: 10, label: '草稿' },
  { value: 20, label: '待发布' },
  { value: 30, label: '生效中' },
  { value: 40, label: '已停用' }
]

const STATUS_META: Record<number, { label: string; tone: string }> = {
  10: { label: '草稿', tone: 'approval-page__pill--info' },
  20: { label: '待发布', tone: 'approval-page__pill--warning' },
  30: { label: '生效中', tone: 'approval-page__pill--success' },
  40: { label: '已停用', tone: 'approval-page__pill--muted' }
}

const statusMeta = (status?: number) =>
  STATUS_META[status ?? -1] ?? { label: '-', tone: 'approval-page__pill--muted' }

const summaryCards = ref([
  { label: '全部方案', value: 0, icon: 'ep:connection', toneClass: 'approval-page__metric--blue' },
  { label: '生效中', value: 0, icon: 'ep:circle-check', toneClass: 'approval-page__metric--green' },
  { label: '草稿/待发布', value: 0, icon: 'ep:edit-pen', toneClass: 'approval-page__metric--muted' }
])



/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await ApprovalSchemeApi.getSchemePage(queryParams)
    list.value = data.list
    total.value = data.total
    refreshSummary(data.list)
  } finally {
    loading.value = false
  }
}

const refreshSummary = (rows: BpmApprovalSchemeVO[]) => {
  summaryCards.value[0].value = total.value
  summaryCards.value[1].value = rows.filter((item) => item.latestVersionStatus === 30).length
  summaryCards.value[2].value = rows.filter((item) => item.latestVersionStatus === 10 || item.latestVersionStatus === 20).length
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

/** 新增/编辑操作 */
const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

/** 提交方案（草稿 → 待发布） */
const handleSubmit = async (row: BpmApprovalSchemeVO) => {
  try {
    await message.confirm(`确定提交方案「${row.name}」待发布吗？`)
    await ApprovalSchemeApi.submit(row.latestVersionId!)
    message.success('提交成功')
    await getList()
  } catch {}
}

/** 发布方案（待发布 → 生效中） */
const handlePublish = async (row: BpmApprovalSchemeVO) => {
  try {
    await message.confirm(`确定发布方案「${row.name}」吗？发布后立即生效`)
    await ApprovalSchemeApi.publish(row.latestVersionId!)
    message.success('发布成功')
    await getList()
  } catch {}
}

/** 停用方案（生效中 → 已停用） */
const handleDisable = async (row: BpmApprovalSchemeVO) => {
  try {
    await message.confirm(`确定停用方案「${row.name}」吗？`)
    await ApprovalSchemeApi.disable(row.latestVersionId!)
    message.success('停用成功')
    await getList()
  } catch {}
}

/** 切换生效版本 */
const handleSwitch = async (row: BpmApprovalSchemeVO) => {
  try {
    await message.confirm(`确定将方案「${row.name}」的最新版本 v${row.latestVersionNo} 切换为生效版本吗？`)
    await ApprovalSchemeApi.switchActiveVersion(row.latestVersionId!)
    message.success('切换成功')
    await getList()
  } catch {}
}

/** 初始化 */
onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.approval-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: var(--erp-slate-50);
  padding: 16px;

  &__header-card,
  &__filter-card,
  &__table-card {
    border-radius: 12px;
    box-shadow: var(--erp-shadow-sm);
  }

  &__page-header {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  &__page-title {
    font-size: 18px;
    font-weight: 600;
    color: var(--erp-slate-800);
  }

  &__metric-grid {
    display: grid;
    grid-template-columns: repeat(3, minmax(160px, 1fr));
    gap: 16px;

    @media (max-width: 768px) {
      grid-template-columns: 1fr;
    }
  }

  &__metric-card {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px;
    border: 1px solid var(--erp-slate-100);
    border-radius: 8px;
    background: var(--erp-surface-white);
  }

  &__metric-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 36px;
    height: 36px;
    border-radius: 8px;
    font-size: 18px;
  }

  &__metric--blue {
    background: var(--erp-stat-gradient-blue);
    color: var(--erp-primary-600);
  }

  &__metric--green {
    background: var(--erp-stat-gradient-green);
    color: var(--erp-success-600);
  }

  &__metric--muted {
    background: var(--erp-stat-gradient-slate);
    color: var(--erp-slate-500);
  }

  &__metric-label {
    font-size: 12px;
    color: var(--erp-slate-500);
  }

  &__metric-value {
    font-size: 20px;
    font-weight: 600;
    color: var(--erp-slate-800);
  }

  &__section-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;
  }

  &__section-title {
    font-size: 14px;
    font-weight: 600;
    color: var(--erp-slate-800);
  }

  &__query-grid {
    display: grid;
    grid-template-columns: repeat(4, minmax(200px, 1fr));
    gap: 8px 16px;

    @media (max-width: 1280px) {
      grid-template-columns: repeat(2, 1fr);
    }

    @media (max-width: 768px) {
      grid-template-columns: 1fr;
    }
  }

  &__query-actions {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
    margin-top: 8px;
  }

  &__toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;
  }

  &__toolbar-main {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  &__toolbar-count {
    font-size: 12px;
    color: var(--erp-slate-500);

    strong {
      color: var(--erp-primary-600);
    }
  }

  &__table-wrap {
    overflow-x: auto;
  }

  &__table {
    width: 100%;
  }

  &__column-header {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    font-weight: 600;
    color: var(--erp-slate-600);

    &--pipeline {
      color: var(--erp-primary-600);
    }
  }

  &__column-icon {
    font-size: 14px;
  }

  &__primary-cell {
    display: flex;
    flex-direction: column;
    gap: 2px;
    min-width: 0;
  }

  &__primary-text {
    font-size: 14px;
    font-weight: 500;
    color: var(--erp-slate-800);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__muted-text {
    font-size: 12px;
    color: var(--erp-slate-400);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__mono {
    font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  }

  &__ellipsis {
    display: inline-block;
    max-width: 100%;
  }

  &__pill {
    display: inline-flex;
    align-items: center;
    padding: 2px 10px;
    border-radius: 999px;
    font-size: 12px;
    border: 1px solid transparent;

    &--info {
      background: var(--erp-slate-100);
      color: var(--erp-slate-600);
      border-color: var(--erp-slate-200);
    }

    &--success {
      background: var(--erp-success-50);
      color: var(--erp-success-600);
      border-color: var(--erp-success-100);
    }

    &--warning {
      background: var(--erp-warning-50);
      color: var(--erp-warning-600);
      border-color: var(--erp-warning-100);
    }

    &--muted {
      background: var(--erp-slate-100);
      color: var(--erp-slate-500);
      border-color: var(--erp-slate-200);
    }
  }
}
</style>
