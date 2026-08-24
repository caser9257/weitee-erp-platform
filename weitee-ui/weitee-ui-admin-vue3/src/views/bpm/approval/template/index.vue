<template>
  <div class="approval-page approval-template-page">
    <!-- 页头卡片 -->
    <ContentWrap class="approval-page__header-card">
      <div class="approval-page__page-header">
        <div class="approval-page__page-title">审批模板</div>
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
          <el-form-item label="模板名称" prop="name">
            <el-input v-model="queryParams.name" placeholder="请输入模板名称" clearable class="!w-full" @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="模板分类" prop="category">
            <el-input v-model="queryParams.category" placeholder="请输入模板分类" clearable class="!w-full" @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="请选择状态" clearable class="!w-full">
              <el-option v-for="item in TEMPLATE_STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
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
          <div class="approval-page__section-title">模板列表</div>
          <div class="approval-page__toolbar-count">当前共 <strong>{{ total }}</strong> 条</div>
        </div>
      </div>

      <div class="approval-page__table-wrap">
        <el-table v-loading="loading" :data="list" row-key="id" stripe :show-overflow-tooltip="false" class="approval-page__table">
          <el-table-column min-width="240">
            <template #header>
              <span class="approval-page__column-header">
                <Icon icon="ep:document" class="approval-page__column-icon" />
                审批模板
              </span>
            </template>
            <template #default="{ row }">
              <div class="approval-page__primary-cell">
                <span class="approval-page__primary-text">{{ row.name || '-' }}</span>
                <span class="approval-page__muted-text approval-page__mono">{{ row.code || '-' }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column min-width="120" label="分类">
            <template #default="{ row }">
              <span class="approval-page__pill approval-page__pill--primary">{{ row.category || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column min-width="240" label="描述">
            <template #default="{ row }">
              <span class="approval-page__muted-text approval-page__ellipsis">{{ row.description || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column min-width="110" label="使用次数" align="right">
            <template #default="{ row }">
              <span class="approval-page__mono">{{ row.useCount ?? 0 }}</span>
            </template>
          </el-table-column>
          <el-table-column min-width="100" label="状态">
            <template #default="{ row }">
              <span class="approval-page__pill" :class="row.status === 1 ? 'approval-page__pill--success' : 'approval-page__pill--muted'">
                {{ row.status === 1 ? '启用' : '禁用' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column min-width="170" label="创建时间" align="right">
            <template #default="{ row }">
              <span class="approval-page__mono approval-page__muted-text">{{ formatDate(row.createTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column min-width="180" label="操作" align="center" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openDetail(row.id)">查看</el-button>
              <el-button link type="success" @click="openUse(row)" v-hasPermi="['bpm:approval-template:use']">
                使用模板
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

    <!-- 模板详情弹窗 -->
    <el-dialog v-model="detailVisible" title="模板详情" width="680px" append-to-body>
      <div v-loading="detailLoading" class="approval-template-page__detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="模板名称">{{ detailData.name || '-' }}</el-descriptions-item>
          <el-descriptions-item label="模板编码">
            <span class="approval-page__mono">{{ detailData.code || '-' }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="分类">{{ detailData.category || '-' }}</el-descriptions-item>
          <el-descriptions-item label="使用次数">{{ detailData.useCount ?? 0 }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <span class="approval-page__pill" :class="detailData.status === 1 ? 'approval-page__pill--success' : 'approval-page__pill--muted'">
              {{ detailData.status === 1 ? '启用' : '禁用' }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDate(detailData.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">{{ detailData.description || '-' }}</el-descriptions-item>
          <el-descriptions-item label="表单配置" :span="2">
            <pre class="approval-template-page__json">{{ formatJson(detailData.formConfig) }}</pre>
          </el-descriptions-item>
          <el-descriptions-item label="流程配置" :span="2">
            <pre class="approval-template-page__json">{{ formatJson(detailData.flowConfig) }}</pre>
          </el-descriptions-item>
          <el-descriptions-item label="通知配置" :span="2">
            <pre class="approval-template-page__json">{{ formatJson(detailData.notifyConfig) }}</pre>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- 使用模板弹窗 -->
    <el-dialog v-model="useVisible" title="使用模板" width="460px" append-to-body>
      <el-alert
        type="info"
        :closable="false"
        show-icon
        title="使用后将为该模板创建对应的审批场景与默认审批方案，可在「审批场景」与「审批方案」中继续配置。"
      />
      <div class="approval-template-page__use-info">
        <div class="approval-template-page__use-name">{{ useTarget.name || '-' }}</div>
        <div class="approval-page__muted-text approval-page__mono">{{ useTarget.code || '-' }}</div>
      </div>
      <template #footer>
        <el-button type="primary" :loading="useLoading" @click="submitUse">确 定</el-button>
        <el-button @click="useVisible = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { formatDate } from '@/utils/formatTime'
import { ApprovalTemplateApi, BpmApprovalTemplateVO } from '@/api/bpm/approval/template'

/** 审批模板列表 */
defineOptions({ name: 'BpmApprovalTemplate' })

const message = useMessage()

const loading = ref(true)
const list = ref<BpmApprovalTemplateVO[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: undefined,
  category: undefined,
  status: undefined
})
const queryFormRef = ref()

const TEMPLATE_STATUS_OPTIONS = [
  { value: 1, label: '启用' },
  { value: 0, label: '禁用' }
]

const summaryCards = ref([
  { label: '全部模板', value: 0, icon: 'ep:document', toneClass: 'approval-page__metric--blue' },
  { label: '已启用', value: 0, icon: 'ep:circle-check', toneClass: 'approval-page__metric--green' },
  { label: '累计使用', value: 0, icon: 'ep:magic-stick', toneClass: 'approval-page__metric--teal' }
])


const formatJson = (value?: Record<string, any>) => (value && Object.keys(value).length ? JSON.stringify(value, null, 2) : '-')

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await ApprovalTemplateApi.getTemplatePage(queryParams)
    list.value = data.list
    total.value = data.total
    refreshSummary(data.list)
  } finally {
    loading.value = false
  }
}

const refreshSummary = (rows: BpmApprovalTemplateVO[]) => {
  summaryCards.value[0].value = total.value
  summaryCards.value[1].value = rows.filter((item) => item.status === 1).length
  summaryCards.value[2].value = rows.reduce((sum, item) => sum + (item.useCount ?? 0), 0)
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

/** 模板详情 */
const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref<BpmApprovalTemplateVO>({})
const openDetail = async (id: number) => {
  detailVisible.value = true
  detailLoading.value = true
  try {
    detailData.value = await ApprovalTemplateApi.getTemplate(id)
  } finally {
    detailLoading.value = false
  }
}

/** 使用模板 */
const useVisible = ref(false)
const useLoading = ref(false)
const useTarget = ref<BpmApprovalTemplateVO>({})
const openUse = (row: BpmApprovalTemplateVO) => {
  useTarget.value = row
  useVisible.value = true
}
const submitUse = async () => {
  useLoading.value = true
  try {
    await ApprovalTemplateApi.useTemplate(useTarget.value.id!)
    message.success('使用成功，已创建审批场景与方案')
    useVisible.value = false
    await getList()
  } finally {
    useLoading.value = false
  }
}

/** 初始化 */
onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.approval-template-page {
  &__detail {
    max-height: 60vh;
    overflow-y: auto;
  }

  &__json {
    margin: 0;
    padding: 12px;
    border-radius: 8px;
    background: var(--erp-slate-50);
    font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
    font-size: 12px;
    line-height: 1.6;
    color: var(--erp-slate-600);
    white-space: pre-wrap;
    word-break: break-all;
  }

  &__use-info {
    display: flex;
    flex-direction: column;
    gap: 4px;
    margin-top: 16px;
    padding: 16px;
    border: 1px solid var(--erp-slate-100);
    border-radius: 8px;
  }

  &__use-name {
    font-size: 14px;
    font-weight: 600;
    color: var(--erp-slate-800);
  }
}

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

  &__metric--teal {
    background: var(--erp-stat-gradient-teal);
    color: var(--erp-teal-600, #0d9488);
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
    grid-template-columns: repeat(3, minmax(200px, 1fr));
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

    &--primary {
      background: var(--erp-primary-50);
      color: var(--erp-primary-600);
      border-color: var(--erp-primary-100);
    }

    &--success {
      background: var(--erp-success-50);
      color: var(--erp-success-600);
      border-color: var(--erp-success-100);
    }

    &--muted {
      background: var(--erp-slate-100);
      color: var(--erp-slate-500);
      border-color: var(--erp-slate-200);
    }
  }
}
</style>
