<template>
  <div class="stock-page-shell">
    <ContentWrap class="warehouse-page__header-card" :body-style="{ padding: '24px' }" :class="getToneCardClass('blue')">
      <div class="warehouse-page__header">
        <div class="warehouse-page__header-main">
          <div class="warehouse-page__title">仓库信息</div>
          <div class="warehouse-page__count">共 {{ total }} 条记录</div>
        </div>
        <div class="warehouse-page__header-actions">
          <el-button class="stock-action-btn" type="primary" @click="openForm('create')" v-hasPermi="['erp:warehouse:create']">
            <Icon icon="ep:plus" class="mr-5px" /> 新增仓库
          </el-button>
          <el-button
            class="stock-action-btn"
            plain
            :disabled="!canOpenBatchEdit"
            @click="openBatchEditDrawer"
            v-hasPermi="['erp:warehouse:update']"
          >
            <Icon icon="ep:edit" class="mr-5px" /> 批量编辑
          </el-button>
          <el-button
            class="stock-action-btn"
            plain
            :class="['warehouse-toolbar__export', { 'warehouse-toolbar__export--loading': exportLoading }]"
            :loading="exportLoading"
            :disabled="!canExportWarehouse"
            @click="handleExport"
            v-hasPermi="['erp:warehouse:export']"
          >
            <Icon icon="ep:download" class="mr-5px" /> 导出数据
          </el-button>
        </div>
      </div>
    </ContentWrap>

    <ContentWrap class="warehouse-page__filter-card" :body-style="{ padding: '24px' }" :class="getToneCardClass('emerald')">
      <div class="warehouse-page__section-head">
        <div class="warehouse-page__section-title">筛选条件</div>
      </div>
      <el-form ref="queryFormRef" :model="queryParams" label-position="top" class="warehouse-query">
        <div class="warehouse-query__grid">
          <el-form-item label="仓库名称" prop="name">
            <el-input
              v-model="queryParams.name"
              placeholder="请输入仓库名称"
              clearable
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="仓库状态" prop="status">
            <el-select v-model="queryParams.status" clearable placeholder="请选择仓库状态">
              <el-option
                v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              />
            </el-select>
          </el-form-item>
        </div>

        <div class="warehouse-query__footer">
          <div></div>
          <div class="warehouse-query__actions">
            <el-button class="stock-action-btn" @click="resetQuery" :disabled="!canQueryWarehouse">
              <Icon icon="ep:refresh" class="mr-5px" /> 重置
            </el-button>
            <el-button
              class="stock-action-btn"
              type="primary"
              :loading="loading"
              :disabled="!canQueryWarehouse"
              @click="handleQuery"
            >
              <Icon icon="ep:search" class="mr-5px" /> 查询
            </el-button>
          </div>
        </div>
      </el-form>
    </ContentWrap>

    <ContentWrap class="warehouse-page__list-card" :body-style="{ padding: '24px' }">
      <div class="warehouse-page__overview-grid">
        <article
          v-for="(card, index) in warehouseSummaryCards"
          :key="card.label"
          class="warehouse-page__overview-card"
          :class="resolveSummaryCardClass(index)"
        >
          <div class="warehouse-page__overview-card__icon" :class="card.colorClass">
            <Icon :icon="card.icon" />
          </div>
          <div class="warehouse-page__overview-card__content">
            <div class="warehouse-page__overview-card__value">{{ card.value }}</div>
            <div class="warehouse-page__overview-card__label">{{ card.label }}</div>
          </div>
        </article>
      </div>

      <div class="warehouse-summary-row">
        <div class="warehouse-summary-row__item" v-for="card in warehouseSummaryCards" :key="'sum-' + card.label">
          <span class="warehouse-summary-row__label">{{ card.label }}</span>
          <span class="warehouse-summary-row__value">{{ card.value }}</span>
        </div>
      </div>

      <div class="warehouse-page__table-shell">
        <el-table
          ref="tableRef"
          v-loading="loading"
          :data="list"
          :stripe="true"
          :show-overflow-tooltip="true"
          class="warehouse-ledger"
          row-key="id"
          @selection-change="handleSelectionChange"
        >
        <template #empty>
          <div v-if="listLoadFailed" class="warehouse-empty warehouse-empty--error">
            <div class="warehouse-empty__icon">
              <Icon icon="ep:warning-filled" />
            </div>
            <div class="warehouse-empty__title">列表加载失败</div>
            <el-button type="primary" plain :disabled="!canRetryList" @click="handleRetryList">
              重试加载
            </el-button>
          </div>
          <div v-else class="warehouse-empty">
            <div class="warehouse-empty__icon">
              <Icon icon="ep:office-building" />
            </div>
            <div class="warehouse-empty__title">暂无仓库记录</div>
          </div>
        </template>

        <el-table-column width="36" type="selection" />
        <el-table-column label="仓库信息" min-width="240">
          <template #default="{ row }">
            <div class="ledger-warehouse">
              <div class="ledger-warehouse__name">
                <Icon icon="ep:office-building" class="mr-4px text-blue-500" />
                {{ row.name || '-' }}
              </div>
              <div class="ledger-warehouse__meta">
                <Icon icon="ep:box" class="mr-2px" /> {{ row.categoryName || `#${row.id}` }}
              </div>
              <div class="ledger-warehouse__meta">
                <Icon icon="ep:user" class="mr-2px" /> 负责人 {{ row.principal || '-' }}
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="费用信息" min-width="180" align="right">
          <template #default="{ row }">
            <div class="ledger-price">
              <div class="ledger-price__value">
                <Icon icon="ep:coin" class="mr-2px" /> 仓储费: {{ formatCurrency(row.warehousePrice) }}
              </div>
              <div class="ledger-price__meta">
                <Icon icon="ep:van" class="mr-2px" /> 搬运费: {{ formatCurrency(row.truckagePrice) }}
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="备注" min-width="180">
          <template #default="{ row }">
            <div class="ledger-remark" :title="row.remark || '-'">{{ row.remark || '-' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="排序" min-width="90" align="right">
          <template #default="{ row }">
            <span class="ledger-number">{{ row.sort ?? '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="96" align="center">
          <template #default="{ row }">
            <span :class="['stock-status-pill', resolveCommonStatusClass(row.status)]">
              {{ resolveCommonStatusLabel(row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="是否默认" min-width="120" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.defaultStatus"
              :active-value="true"
              :inactive-value="false"
              :loading="isUpdatingDefault(row.id)"
              :disabled="isUpdatingDefault(row.id)"
              @change="handleDefaultStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="创建时间" min-width="170">
          <template #default="{ row }">
            <span class="ledger-date">{{ formatDateValue(row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template #default="{ row }">
            <div class="ledger-actions">
              <el-tooltip content="编辑" placement="top">
                <el-button
                  circle
                  type="primary"
                  plain
                  @click="openForm('update', row.id)"
                  v-hasPermi="['erp:warehouse:update']"
                >
                  <Icon icon="ep:edit" />
                </el-button>
              </el-tooltip>
              <el-tooltip content="删除" placement="top">
                <el-button
                  circle
                  type="danger"
                  plain
                  :loading="isDeletingRow(row.id)"
                  :disabled="isDeletingRow(row.id)"
                  @click="handleDelete(row.id)"
                  v-hasPermi="['erp:warehouse:delete']"
                >
                  <Icon icon="ep:delete" />
                </el-button>
              </el-tooltip>
              <el-tooltip content="复制" placement="top">
                <el-button
                  circle
                  plain
                  @click="handleCopy(row)"
                  v-hasPermi="['erp:warehouse:create']"
                >
                  <Icon icon="ep:copy-document" />
                </el-button>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>
        </el-table>
      </div>

      <div class="warehouse-page__footer">
        <Pagination
          :total="total"
          v-model:page="queryParams.pageNo"
          v-model:limit="queryParams.pageSize"
          @pagination="getList"
        />
      </div>
    </ContentWrap>
  </div>

  <WarehouseForm ref="formRef" @success="getList" />
  <WarehouseBatchEditDrawer ref="batchEditDrawerRef" @success="handleBatchEditSuccess" />
</template>

<script setup lang="ts">
import { getIntDictOptions, DICT_TYPE } from '@/utils/dict'
import { formatDate } from '@/utils/formatTime'
import download from '@/utils/download'
import { WarehouseApi, WarehouseVO } from '@/api/erp/stock/warehouse'
import WarehouseForm from './WarehouseForm.vue'
import WarehouseBatchEditDrawer from './components/WarehouseBatchEditDrawer.vue'
import {
  getToneCardClass,
  getTonePillClass,
  resolveCommonStatusTone,
  resolveSummaryCardClass
} from '../shared/stockTone'

defineOptions({ name: 'ErpWarehouse' })

type WarehouseRow = WarehouseVO & {
  createTime?: string | Date
}

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const listLoadFailed = ref(false)
const list = ref<WarehouseRow[]>([])
const total = ref(0)
const tableRef = ref()
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: undefined,
  status: undefined
})
const queryFormRef = ref()
const exportLoading = ref(false)
const deletingIds = ref<number[]>([])
const defaultUpdatingIds = ref<number[]>([])
const selectedRows = ref<WarehouseRow[]>([])
const batchEditDrawerRef = ref()
const formRef = ref()

const canRetryList = computed(() => listLoadFailed.value && !loading.value)
const canQueryWarehouse = computed(() => !loading.value)
const canExportWarehouse = computed(() => !exportLoading.value)
const selectedIds = computed(() => selectedRows.value.map((item) => item.id))
const canOpenBatchEdit = computed(() => selectedIds.value.length > 0 && !loading.value)
const warehousePageCount = computed(() => list.value.length)
const enabledWarehouseCount = computed(() => list.value.filter((item) => Number(item.status) === 0).length)
const defaultWarehouseCount = computed(() => list.value.filter((item) => item.defaultStatus).length)
const isDeletingRow = (id?: number) => !!id && deletingIds.value.includes(id)
const isUpdatingDefault = (id?: number) => !!id && defaultUpdatingIds.value.includes(id)
const warehouseSummaryCards = computed(() => [
  { label: '当前页仓库', value: formatCount(warehousePageCount.value), icon: 'ep:office-building', colorClass: 'wh-stat-icon--teal' },
  { label: '启用仓库', value: formatCount(enabledWarehouseCount.value), icon: 'ep:circle-check', colorClass: 'wh-stat-icon--green' },
  { label: '默认仓库', value: formatCount(defaultWarehouseCount.value), icon: 'ep:key', colorClass: 'wh-stat-icon--orange' },
  { label: '已选中', value: formatCount(selectedRows.value.length), icon: 'ep:ticket', colorClass: 'wh-stat-icon--gray' }
])

const formatCurrency = (value?: number | string | null) =>
  new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(Number(value || 0))

const formatCount = (value?: number | string | null) => {
  const numberValue = Number(value || 0)
  if (Number.isInteger(numberValue)) {
    return numberValue.toLocaleString('zh-CN')
  }
  return numberValue.toFixed(3).replace(/\.?0+$/, '')
}

const formatDateValue = (value?: string | Date) => (value ? formatDate(value) : '-')

const resolveCommonStatusLabel = (value?: number | string | boolean) => {
  const dict = getIntDictOptions(DICT_TYPE.COMMON_STATUS).find((item) => item.value === Number(value))
  return dict?.label || '-'
}

const resolveCommonStatusClass = (value?: number | string | boolean) => getTonePillClass(resolveCommonStatusTone(value))

const setIdsLoading = (source: Ref<number[]>, ids: number[], loadingState: boolean) => {
  if (loadingState) {
    source.value = Array.from(new Set([...source.value, ...ids]))
    return
  }
  source.value = source.value.filter((item) => !ids.includes(item))
}

const getList = async () => {
  loading.value = true
  listLoadFailed.value = false
  try {
    const data = await WarehouseApi.getWarehousePage(queryParams)
    list.value = data.list || []
    total.value = data.total || 0
    selectedRows.value = []
    await nextTick()
    tableRef.value?.clearSelection?.()
  } catch {
    list.value = []
    total.value = 0
    listLoadFailed.value = true
    selectedRows.value = []
  } finally {
    loading.value = false
  }
}

const handleQuery = async () => {
  if (!canQueryWarehouse.value) {
    return
  }
  queryParams.pageNo = 1
  await getList()
}

const resetQuery = async () => {
  if (!canQueryWarehouse.value) {
    return
  }
  queryFormRef.value?.resetFields()
  await handleQuery()
}

const handleRetryList = async () => {
  await getList()
}

const openForm = (type: string, id?: number) => {
  formRef.value?.open(type, id)
}

const handleSelectionChange = (rows: WarehouseRow[]) => {
  selectedRows.value = rows
}

const openBatchEditDrawer = () => {
  if (!selectedIds.value.length) {
    message.warning('请先选择要修改的仓库')
    return
  }
  batchEditDrawerRef.value?.open({
    ids: selectedIds.value,
    rows: selectedRows.value
  })
}

const handleBatchEditSuccess = async () => {
  await getList()
}

const handleDelete = async (id?: number) => {
  if (!id || isDeletingRow(id)) {
    return
  }
  try {
    await message.delConfirm()
    setIdsLoading(deletingIds, [id], true)
    await WarehouseApi.deleteWarehouse(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {
  } finally {
    setIdsLoading(deletingIds, [id], false)
  }
}

const handleCopy = (row: WarehouseRow) => {
  openForm('create')
  nextTick(() => {
    if (formRef.value?.setFields) {
      formRef.value.setFields({
        name: `${row.name} - 副本`,
        categoryId: row.categoryId,
        principal: row.principal,
        address: row.address,
        warehousePrice: row.warehousePrice,
        truckagePrice: row.truckagePrice,
        remark: row.remark,
        sort: row.sort
      })
    }
  })
}

const handleDefaultStatusChange = async (row: WarehouseRow) => {
  if (!row.id || isUpdatingDefault(row.id)) {
    return
  }
  const nextValue = row.defaultStatus
  const text = nextValue ? '设置' : '取消'
  try {
    await message.confirm(`确认${text}“${row.name}”默认仓库吗？`)
    setIdsLoading(defaultUpdatingIds, [row.id], true)
    await WarehouseApi.updateWarehouseDefaultStatus(row.id, nextValue)
    await getList()
  } catch {
    row.defaultStatus = !nextValue
  } finally {
    setIdsLoading(defaultUpdatingIds, [row.id], false)
  }
}

const handleExport = async () => {
  if (!canExportWarehouse.value) {
    return
  }
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await WarehouseApi.exportWarehouse(queryParams)
    download.excel(data, '仓库.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.warehouse-page__filter-card,
.warehouse-page__list-card {
  min-width: 0;
}

.stock-page-shell {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.warehouse-page__header {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: center;
  gap: 16px;
}

.warehouse-page__header-main {
  min-width: 0;
}

.warehouse-page__header-actions {
  display: flex;
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 10px;
}

.warehouse-page__title {
  margin-bottom: 8px;
  color: #0f172a;
  font-family: 'IBM Plex Sans', Inter, sans-serif;
  font-size: 22px;
  line-height: 28px;
  font-weight: 600;
}

.warehouse-query {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.warehouse-query__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.warehouse-query__footer,
.warehouse-toolbar,
.warehouse-page__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.warehouse-query__actions,
.warehouse-toolbar__actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.stock-action-btn {
  flex: 0 0 auto;
  min-width: 88px;
  min-height: 40px;
  padding-inline: 14px;
  white-space: nowrap;
}

.warehouse-toolbar--soft {
  margin: 0 0 16px;
  padding: 18px 20px;
  border: 1px solid #dbe4f0;
  border-radius: 18px;
  background: linear-gradient(180deg, rgba(248, 250, 252, 0.96), rgba(241, 245, 249, 0.82));
}

.warehouse-page__overview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 14px;
  margin-bottom: 16px;
}

.warehouse-page__overview-card {
  display: flex;
  min-height: 96px;
  align-items: center;
  gap: 14px;
  padding: 16px 18px;
}

.warehouse-page__overview-card__icon {
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

.warehouse-page__overview-card:hover .warehouse-page__overview-card__icon {
  transform: scale(1.08);
}

.wh-stat-icon--teal {
  background: #ccfbf1;
  color: #0d9488;
}

.wh-stat-icon--green {
  background: #dcfce7;
  color: #16a34a;
}

.wh-stat-icon--orange {
  background: #ffedd5;
  color: #ea580c;
}

.wh-stat-icon--gray {
  background: #f1f5f9;
  color: #64748b;
}

.warehouse-page__overview-card__content {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.warehouse-page__overview-card__label {
  color: #64748b;
  font-size: 12px;
  line-height: 20px;
}

.warehouse-page__overview-card__value {
  color: #0f172a;
  font-size: 24px;
  line-height: 30px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New',
    monospace;
}

.warehouse-summary-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  padding: 16px 0;
  margin-bottom: 16px;
  border-bottom: 1px solid #f1f5f9;
}

.warehouse-summary-row__item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.warehouse-summary-row__label {
  color: #64748b;
  font-size: 13px;
  line-height: 20px;
}

.warehouse-summary-row__value {
  color: #0f172a;
  font-size: 20px;
  line-height: 28px;
  font-weight: 700;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New',
    monospace;
}

.warehouse-toolbar__export {
  color: #475569;
  border-color: #e2e8f0;
}

.warehouse-toolbar__export--loading {
  color: #64748b;
}

.warehouse-page__table-shell {
  overflow-x: auto;
}

.warehouse-ledger {
  min-width: 1180px;
}

.warehouse-ledger :deep(.el-table) {
  --el-table-border-color: transparent;
  --el-table-header-bg-color: #f8fafc;
}

.warehouse-ledger :deep(.el-table td.el-table__cell),
.warehouse-ledger :deep(.el-table th.el-table__cell.is-leaf) {
  border-bottom: 1px solid #f1f5f9;
}

.warehouse-ledger :deep(.el-table__header-wrapper) {
  position: sticky;
  top: 0;
  z-index: 3;
}

.ledger-warehouse__name,
.ledger-price__value {
  color: #0f172a;
  font-weight: 700;
  display: flex;
  align-items: center;
}

.ledger-warehouse {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.ledger-warehouse__chips {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.ledger-warehouse__chip {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  border: 1px solid #dbe4f0;
  border-radius: 999px;
  background: #f8fafc;
  color: #475569;
  font-size: 12px;
  line-height: 18px;
  white-space: nowrap;
}

.ledger-warehouse__chip--slate {
  background: #fff;
}

.stock-status-pill {
  display: inline-flex;
  align-items: center;
  max-width: 100%;
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 12px;
  line-height: 18px;
  font-weight: 600;
  white-space: nowrap;
}

.ledger-warehouse__meta,
.ledger-price__meta,
.ledger-date {
  color: #64748b;
  font-size: 12px;
  line-height: 18px;
  display: flex;
  align-items: center;
}

.ledger-price,
.ledger-number,
.ledger-price__value {
  width: 100%;
  text-align: right;
  font-variant-numeric: tabular-nums;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New',
    monospace;
}

.ledger-price {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.ledger-price__meta {
  justify-content: flex-end;
}

.ledger-remark {
  color: #0f172a;
  line-height: 22px;
  word-break: break-word;
}

.ledger-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  gap: 8px;
}

.warehouse-empty {
  min-height: 192px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #64748b;
}

.warehouse-empty__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 999px;
  background: #f8fafc;
  color: #94a3b8;
  font-size: 24px;
}

.warehouse-empty__title {
  color: #0f172a;
  font-size: 14px;
  line-height: 22px;
  font-weight: 600;
}

.warehouse-empty--error .warehouse-empty__icon {
  background: rgba(244, 63, 94, 0.08);
  color: #e11d48;
}

.warehouse-page__record-count {
  color: #64748b;
  font-size: 13px;
  line-height: 20px;
}

.warehouse-page__section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.warehouse-page__section-title {
  color: #0f172a;
  font-size: 18px;
  line-height: 28px;
  font-weight: 600;
}

@media (max-width: 959px) {
  .warehouse-page__header {
    grid-template-columns: minmax(0, 1fr);
    align-items: stretch;
  }

  .warehouse-page__header-actions {
    justify-content: flex-start;
  }

  .warehouse-query__grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .warehouse-query__footer,
  .warehouse-toolbar,
  .warehouse-page__footer {
    align-items: stretch;
    flex-direction: column;
  }

  .warehouse-query__actions,
  .warehouse-toolbar__actions {
    width: 100%;
  }

  .warehouse-query__actions :deep(.el-button),
  .warehouse-toolbar__actions :deep(.el-button) {
    flex: 1 1 0;
    min-width: 0;
  }

  .warehouse-page__overview-grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .warehouse-summary-row {
    grid-template-columns: repeat(2, 1fr);
  }

  .warehouse-ledger {
    min-width: 1080px;
  }
}

@media (max-width: 1279px) {
  .warehouse-page__header {
    grid-template-columns: minmax(0, 1fr);
  }

  .warehouse-page__header-actions {
    justify-content: flex-start;
  }

  .warehouse-page__overview-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .warehouse-page__header-actions,
  .warehouse-query__actions,
  .warehouse-toolbar__actions {
    width: 100%;
  }

  .warehouse-page__header-actions :deep(.el-button),
  .warehouse-query__actions :deep(.el-button),
  .warehouse-toolbar__actions :deep(.el-button) {
    flex: 1 1 0;
    min-width: 0;
  }
}
</style>
