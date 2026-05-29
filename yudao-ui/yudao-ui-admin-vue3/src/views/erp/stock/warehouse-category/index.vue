<template>
  <div class="warehouse-category-page">
    <ContentWrap class="warehouse-category-page__header-card" :body-style="{ padding: '24px' }" :class="getToneCardClass('blue')">
      <div class="warehouse-category-page__header">
        <div>
          <div class="warehouse-category-page__title">仓库分类管理</div>
          <div class="warehouse-category-page__count">共 {{ total }} 条记录</div>
        </div>
        <div class="warehouse-category-page__actions">
          <el-button class="stock-action-btn" type="primary" @click="openForm('create')" v-hasPermi="['erp:warehouse-category:create']">
            <Icon icon="ep:plus" class="mr-5px" /> 新增分类
          </el-button>
          <el-button class="stock-action-btn" plain :loading="exportLoading" :disabled="!canExport" @click="handleExport" v-hasPermi="['erp:warehouse-category:export']">
            <Icon icon="ep:download" class="mr-5px" /> 导出
          </el-button>
        </div>
      </div>
    </ContentWrap>

    <ContentWrap class="warehouse-category-page__filter-card" :body-style="{ padding: '24px' }" :class="getToneCardClass('emerald')">
      <div class="warehouse-category-page__section-title">筛选条件</div>
      <el-form ref="queryFormRef" :model="queryParams" label-position="top" class="warehouse-category-query">
        <div class="warehouse-category-query__grid">
          <el-form-item label="分类名称" prop="name">
            <el-input v-model="queryParams.name" placeholder="请输入分类名称" clearable @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="分类状态" prop="status">
            <el-select v-model="queryParams.status" clearable placeholder="请选择分类状态">
              <el-option v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </el-form-item>
        </div>
        <div class="warehouse-category-query__footer">
          <div></div>
          <div class="warehouse-category-query__actions">
            <el-button class="stock-action-btn" :disabled="!canQuery" @click="resetQuery">
              <Icon icon="ep:refresh" class="mr-5px" /> 重置
            </el-button>
            <el-button class="stock-action-btn" type="primary" :loading="loading" :disabled="!canQuery" @click="handleQuery">
              <Icon icon="ep:search" class="mr-5px" /> 查询
            </el-button>
          </div>
        </div>
      </el-form>
    </ContentWrap>

    <ContentWrap class="warehouse-category-page__list-card" :body-style="{ padding: '24px' }" :class="getToneCardClass('slate')">
      <div class="warehouse-category-page__overview-grid">
        <article
          v-for="(card, index) in summaryCards"
          :key="card.label"
          class="warehouse-category-page__overview-card"
          :class="resolveSummaryCardClass(index)"
        >
          <div class="warehouse-category-page__overview-card__icon" :class="card.colorClass">
            <Icon :icon="card.icon" />
          </div>
          <div class="warehouse-category-page__overview-card__content">
            <div class="warehouse-category-page__overview-card__value">{{ card.value }}</div>
            <div class="warehouse-category-page__overview-card__label">{{ card.label }}</div>
          </div>
        </article>
      </div>

      <div class="warehouse-category-page__table-shell">
        <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true" row-key="id" class="warehouse-category-ledger">
          <template #empty>
            <div v-if="loadFailed" class="warehouse-category-empty warehouse-category-empty--error">
              <div class="warehouse-category-empty__icon"><Icon icon="ep:warning-filled" /></div>
              <div class="warehouse-category-empty__title">列表加载失败</div>
              <el-button class="stock-action-btn" type="primary" plain :disabled="!canRetry" @click="handleRetry">重试加载</el-button>
            </div>
            <div v-else class="warehouse-category-empty">
              <div class="warehouse-category-empty__icon"><Icon icon="ep:folder" /></div>
              <div class="warehouse-category-empty__title">暂无仓库分类</div>
            </div>
          </template>
          <el-table-column label="分类信息" min-width="240">
            <template #default="{ row }">
              <div class="warehouse-category-cell">
                <div class="warehouse-category-cell__name">{{ row.name || '-' }}</div>
                <div class="warehouse-category-cell__meta">{{ row.code || '-' }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="上级分类" min-width="160">
            <template #default="{ row }">
              <span class="warehouse-category-cell__meta">{{ resolveParentName(row.parentId) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="排序" min-width="90" align="right">
            <template #default="{ row }"><span class="ledger-number">{{ row.sort ?? '-' }}</span></template>
          </el-table-column>
          <el-table-column label="状态" min-width="96" align="center">
            <template #default="{ row }">
              <span :class="['stock-status-pill', resolveCommonStatusClass(row.status)]">{{ resolveCommonStatusLabel(row.status) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" min-width="170">
            <template #default="{ row }"><span class="ledger-date">{{ formatDateValue(row.createTime) }}</span></template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right" align="center">
            <template #default="{ row }">
              <div class="ledger-actions">
                <el-button link type="primary" @click="openForm('update', row.id)" v-hasPermi="['erp:warehouse-category:update']">编辑</el-button>
                <el-button link type="danger" :loading="isDeleting(row.id)" :disabled="isDeleting(row.id)" @click="handleDelete(row.id)" v-hasPermi="['erp:warehouse-category:delete']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="warehouse-category-page__footer">
        <Pagination :total="total" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>
    </ContentWrap>

    <WarehouseCategoryForm ref="formRef" @success="getList" />
  </div>
</template>

<script setup lang="ts">
import { getIntDictOptions, DICT_TYPE } from '@/utils/dict'
import { formatDate } from '@/utils/formatTime'
import download from '@/utils/download'
import { WarehouseCategoryApi, WarehouseCategoryVO } from '@/api/erp/stock/warehouse-category'
import WarehouseCategoryForm from './WarehouseCategoryForm.vue'
import { getToneCardClass, getTonePillClass, resolveCommonStatusTone } from '../shared/stockTone'

defineOptions({ name: 'ErpWarehouseCategory' })

type WarehouseCategoryRow = WarehouseCategoryVO & {
  createTime?: string | Date
}

const message = useMessage()
const { t } = useI18n()
const loading = ref(true)
const loadFailed = ref(false)
const list = ref<WarehouseCategoryRow[]>([])
const total = ref(0)
const queryFormRef = ref()
const formRef = ref()
const exportLoading = ref(false)
const deletingIds = ref<number[]>([])
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: undefined as string | undefined,
  status: undefined as number | undefined
})

const canRetry = computed(() => loadFailed.value && !loading.value)
const canQuery = computed(() => !loading.value)
const canExport = computed(() => !exportLoading.value)
const isDeleting = (id?: number) => !!id && deletingIds.value.includes(id)
const categoryMap = computed(() => new Map(list.value.map((item) => [item.id, item])))

// 统计卡片
const totalCount = computed(() => list.value.length)
const enabledCount = computed(() => list.value.filter((item) => Number(item.status) === 0).length)
const topCategoryCount = computed(() => list.value.filter((item) => !item.parentId || item.parentId === 0).length)
const summaryCards = computed(() => [
  { label: '总分类数', value: formatCount(totalCount.value), icon: 'ep:folder', colorClass: 'stat-icon--blue' },
  { label: '启用分类', value: formatCount(enabledCount.value), icon: 'ep:circle-check', colorClass: 'stat-icon--green' },
  { label: '顶级分类', value: formatCount(topCategoryCount.value), icon: 'ep:folder-opened', colorClass: 'stat-icon--amber' },
  { label: '当前页', value: formatCount(list.value.length), icon: 'ep:document', colorClass: 'stat-icon--slate' }
])

const formatCount = (value?: number | string | null) => {
  const numberValue = Number(value || 0)
  if (Number.isInteger(numberValue)) {
    return numberValue.toLocaleString('zh-CN')
  }
  return numberValue.toFixed(3).replace(/\.?0+$/, '')
}

const resolveParentName = (parentId?: number) => {
  if (!parentId || parentId === 0) {
    return '顶级分类'
  }
  return categoryMap.value.get(parentId)?.name || '-'
}

const resolveCommonStatusLabel = (value?: number | string | boolean) => {
  const dict = getIntDictOptions(DICT_TYPE.COMMON_STATUS).find((item) => item.value === Number(value))
  return dict?.label || '-'
}

const resolveCommonStatusClass = (value?: number | string | boolean) => getTonePillClass(resolveCommonStatusTone(value))
const formatDateValue = (value?: string | Date) => (value ? formatDate(value) : '-')
const resolveSummaryCardClass = (index: number) => {
  const classes = ['warehouse-category-page__overview-card--blue', 'warehouse-category-page__overview-card--green', 'warehouse-category-page__overview-card--amber', 'warehouse-category-page__overview-card--slate']
  return classes[index % classes.length]
}

const setDeleting = (id: number, loadingState: boolean) => {
  deletingIds.value = loadingState ? Array.from(new Set([...deletingIds.value, id])) : deletingIds.value.filter((item) => item !== id)
}

const getList = async () => {
  loading.value = true
  loadFailed.value = false
  try {
    const data = await WarehouseCategoryApi.getWarehouseCategoryList(queryParams)
    list.value = Array.isArray(data) ? data : data.list || []
    total.value = list.value.length
  } catch {
    list.value = []
    total.value = 0
    loadFailed.value = true
  } finally {
    loading.value = false
  }
}

const handleQuery = async () => {
  if (!canQuery.value) return
  queryParams.pageNo = 1
  await getList()
}

const resetQuery = async () => {
  if (!canQuery.value) return
  queryFormRef.value?.resetFields()
  await handleQuery()
}

const handleRetry = async () => {
  await getList()
}

const openForm = (type: string, id?: number) => {
  formRef.value?.open(type, id)
}

const handleDelete = async (id?: number) => {
  if (!id || isDeleting(id)) return
  try {
    await message.delConfirm()
    setDeleting(id, true)
    await WarehouseCategoryApi.deleteWarehouseCategory(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {
  } finally {
    if (id) setDeleting(id, false)
  }
}

const handleExport = async () => {
  if (!canExport.value) return
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await WarehouseCategoryApi.exportWarehouseCategory(queryParams)
    download.excel(data, '仓库分类.xls')
  } finally {
    exportLoading.value = false
  }
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.warehouse-category-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.warehouse-category-page__header,
.warehouse-category-page__footer,
.warehouse-category-query__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.warehouse-category-page__actions,
.warehouse-category-query__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.warehouse-category-page__title {
  color: #0f172a;
  font-size: 22px;
  line-height: 30px;
  font-weight: 600;
}
.warehouse-category-page__count {
  margin-top: 8px;
  color: #64748b;
  font-size: 13px;
}
.warehouse-category-page__section-title {
  margin-bottom: 16px;
  color: #0f172a;
  font-size: 16px;
  font-weight: 600;
}
.warehouse-category-query__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}
/* 统计卡片 */
.warehouse-category-page__overview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 14px;
  margin-bottom: 16px;
}

.warehouse-category-page__overview-card {
  display: flex;
  min-height: 96px;
  align-items: center;
  gap: 14px;
  padding: 16px 18px;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.05);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.warehouse-category-page__overview-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.08);
}

.warehouse-category-page__overview-card__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 12px;
  font-size: 22px;
  flex-shrink: 0;
  transition: transform 0.2s ease;
}

.warehouse-category-page__overview-card:hover .warehouse-category-page__overview-card__icon {
  transform: scale(1.08);
}

.stat-icon--blue {
  background: #eff6ff;
  color: #2563eb;
}

.stat-icon--green {
  background: #ecfdf5;
  color: #059669;
}

.stat-icon--amber {
  background: #fffbeb;
  color: #d97706;
}

.stat-icon--slate {
  background: #f1f5f9;
  color: #64748b;
}

.warehouse-category-page__overview-card__content {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.warehouse-category-page__overview-card__label {
  color: #64748b;
  font-size: 12px;
  line-height: 20px;
}

.warehouse-category-page__overview-card__value {
  color: #0f172a;
  font-size: 24px;
  line-height: 30px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace;
}

/* 表格 */
.warehouse-category-page__table-shell {
  overflow-x: auto;
}
.warehouse-category-ledger {
  min-width: 960px;
}
.warehouse-category-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.warehouse-category-cell__name {
  color: #0f172a;
  font-weight: 700;
}
.warehouse-category-cell__meta,
.ledger-date {
  color: #64748b;
  font-size: 12px;
}
.stock-status-pill {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 12px;
  line-height: 18px;
  font-weight: 600;
}
.ledger-number,
.ledger-actions {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace;
}
.ledger-number {
  font-weight: 600;
}
.ledger-actions {
  display: flex;
  justify-content: center;
  gap: 8px;
}
.warehouse-category-empty {
  min-height: 180px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 10px;
  color: #64748b;
}
.warehouse-category-empty__icon {
  display: inline-flex;
  width: 44px;
  height: 44px;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  background: #f1f5f9;
  color: #94a3b8;
}
.warehouse-category-empty__title {
  color: #334155;
  font-size: 14px;
}
@media (max-width: 1279px) {
  .warehouse-category-page__overview-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 959px) {
  .warehouse-category-query__grid,
  .warehouse-category-page__header,
  .warehouse-category-page__footer,
  .warehouse-category-query__footer {
    grid-template-columns: minmax(0, 1fr);
    flex-direction: column;
    align-items: stretch;
  }
  .warehouse-category-page__actions,
  .warehouse-category-query__actions {
    width: 100%;
  }
  .warehouse-category-page__overview-grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
