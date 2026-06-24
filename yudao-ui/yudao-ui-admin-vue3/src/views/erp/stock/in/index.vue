<template>
<ContentWrap class="stock-in-page__filter-card">
    <div class="stock-in-page__title">其他入库台账</div>
    <el-form
      ref="queryFormRef"
      :model="queryParams"
      label-position="top"
      class="stock-in-query"
    >
      <div class="stock-in-query__grid stock-in-query__grid--primary">
        <el-form-item label="入库单号" prop="no">
          <el-input
            v-model="queryParams.no"
            placeholder="请输入入库单�?
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="产品" prop="productId">
          <el-select v-model="queryParams.productId" clearable filterable placeholder="请选择产品">
            <el-option
              v-for="item in productList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="入库时间" prop="inTime">
          <el-date-picker
            v-model="queryParams.inTime"
            value-format="YYYY-MM-DD HH:mm:ss"
            type="daterange"
            start-placeholder="开始日�?
            end-placeholder="结束日期"
            range-separator="-"
            :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
          />
        </el-form-item>
        <el-form-item label="供应�? prop="supplierId">
          <el-select v-model="queryParams.supplierId" clearable filterable placeholder="请选择供应�?>
            <el-option
              v-for="item in supplierList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
      </div>

      <transition name="stock-in-query-collapse">
        <div
          v-if="advancedSearchVisible"
          class="stock-in-query__grid stock-in-query__grid--advanced"
        >
          <el-form-item label="仓库" prop="warehouseId">
            <el-select v-model="queryParams.warehouseId" clearable filterable placeholder="请选择仓库">
              <el-option
                v-for="item in warehouseList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="创建�? prop="creator">
            <el-select v-model="queryParams.creator" clearable filterable placeholder="请选择创建�?>
              <el-option
                v-for="item in userList"
                :key="item.id"
                :label="item.nickname"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="状�? prop="status">
            <el-select v-model="queryParams.status" clearable placeholder="请选择状�?>
              <el-option
                v-for="dict in getIntDictOptions(DICT_TYPE.ERP_AUDIT_STATUS)"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="备注" prop="remark">
            <el-input
              v-model="queryParams.remark"
              placeholder="请输入备�?
              clearable
              @keyup.enter="handleQuery"
            />
          </el-form-item>
        </div>
      </transition>

      <div class="stock-in-query__footer">
        <el-button link type="primary" @click="toggleAdvancedSearch">
          {{ advancedSearchVisible ? '收起高级筛�? : '展开高级筛�? }}
          <span v-if="advancedFilterCount" class="stock-in-query__filter-count">
            {{ advancedFilterCount }}
          </span>
          <Icon :icon="advancedSearchVisible ? 'ep:arrow-up' : 'ep:arrow-down'" class="ml-4px" />
        </el-button>
        <div class="stock-in-query__actions">
          <el-button @click="resetQuery" :disabled="loading">
            <Icon icon="ep:refresh" class="mr-5px" /> 重置
          </el-button>
          <el-button type="primary" :loading="loading" @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" /> 查询
          </el-button>
        </div>
      </div>
    </el-form>
  </ContentWrap>

  <ContentWrap class="stock-in-page__list-card">
    <div class="stock-in-toolbar">
      <div class="stock-in-toolbar__actions">
        <el-button
          v-if="canCreateStockIn"
          type="primary"
          @click="openForm('create')"
          v-hasPermi="['erp:stock-in:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增入库�?
        </el-button>
        <el-button
          v-if="canExportStockIn"
          plain
          :loading="exportLoading"
          @click="handleExport"
          v-hasPermi="['erp:stock-in:export']"
        >
          <Icon icon="ep:download" class="mr-5px" /> 导出数据
        </el-button>
      </div>
      <div class="stock-in-toolbar__meta">
        <el-button
          v-if="canDeleteStockIn"
          plain
          type="danger"
          :disabled="disableBatchDelete"
          @click="handleDelete(selectedIds)"
          v-hasPermi="['erp:stock-in:delete']"
        >
          <Icon icon="ep:delete" class="mr-5px" /> 批量删除
        </el-button>
      </div>
    </div>

    <div class="stock-in-page__table-shell">
      <el-table
        v-loading="loading"
        :data="list"
        :stripe="true"
        class="stock-in-ledger"
        @selection-change="handleSelectionChange"
      >
        <template #empty>
          <div v-if="listLoadFailed" class="stock-in-empty stock-in-empty--error">
            <div class="stock-in-empty__icon">
              <Icon icon="ep:warning-filled" />
            </div>
            <div class="stock-in-empty__title">列表加载失败</div>
            <el-button type="primary" plain :disabled="!canRetryList" @click="handleRetryList">
              重试加载
            </el-button>
          </div>
          <div v-else class="stock-in-empty">
            <div class="stock-in-empty__icon">
              <Icon icon="ep:box" />
            </div>
            <div class="stock-in-empty__title">暂无其他入库记录</div>
          </div>
        </template>

        <el-table-column width="36" type="selection" />
        <el-table-column label="入库信息" min-width="188">
          <template #default="{ row }">
            <div class="ledger-order">
              <div class="ledger-order__no">{{ row.no || '-' }}</div>
              <div class="ledger-order__meta">入库 {{ formatDateValue(row.inTime) }}</div>
              <div class="ledger-order__meta">创建 {{ row.creatorName || '-' }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="产品摘要" min-width="220">
          <template #default="{ row }">
            <div class="ledger-product" :title="row.productNames || '-'">
              {{ row.productNames || '-' }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="供应�? min-width="160">
          <template #default="{ row }">
            <div class="ledger-party">{{ row.supplierName || '-' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="数量" min-width="120" align="right">
          <template #default="{ row }">
            <div class="ledger-number">{{ formatCount(row.totalCount) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="金额" min-width="140" align="right">
          <template #default="{ row }">
            <div class="ledger-amount">{{ formatCurrency(row.totalPrice) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="状�? min-width="96" fixed="right">
          <template #default="{ row }">
            <dict-tag :type="DICT_TYPE.ERP_AUDIT_STATUS" :value="row.status" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right" align="left">
          <template #default="{ row }">
            <div class="ledger-actions">
              <template v-for="action in getInlineActionDescriptors(row)" :key="`${row.id}-${action.key}`">
                <el-button
                  link
                  :type="action.type"
                  :disabled="action.disabled"
                  :loading="action.loading"
                  @click="handleCommand(action.key, row)"
                >
                  {{ action.label }}
                </el-button>
              </template>
              <el-dropdown
                v-if="getOverflowActionDescriptors(row).length"
                @command="(command) => handleCommand(command, row)"
              >
                <el-tooltip content="更多操作" placement="top">
                  <el-button link type="primary" class="ledger-actions__more">
                    <Icon icon="ep:more-filled" />
                  </el-button>
                </el-tooltip>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item
                      v-for="action in getOverflowActionDescriptors(row)"
                      :key="`${row.id}-overflow-${action.key}`"
                      :command="action.key"
                      :disabled="action.disabled"
                      :class="{ 'text-red-500': action.danger }"
                      :divided="action.danger"
                    >
                      {{ action.label }}
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="stock-in-page__footer">
      <div class="stock-in-page__record-count">�?{{ total }} 条记�?/div>
      <Pagination
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </div>
  </ContentWrap>

  <StockInForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import { getIntDictOptions, DICT_TYPE } from '@/utils/dict'
import { formatDate } from '@/utils/formatTime'
import download from '@/utils/download'
import { StockInApi, StockInVO } from '@/api/erp/stock/in'
import StockInForm from './StockInForm.vue'
import { ProductApi, ProductVO } from '@/api/erp/product/product'
import { WarehouseApi, WarehouseVO } from '@/api/erp/stock/warehouse'
import { SupplierApi, SupplierVO } from '@/api/erp/purchase/supplier'
import { UserVO } from '@/api/system/user'
import * as UserApi from '@/api/system/user'
import { checkPermi } from '@/utils/permission'

defineOptions({ name: 'ErpStockIn' })

type StockInListRow = StockInVO & {
  productNames?: string
  supplierName?: string
  creatorName?: string
}

type StockInActionKey = 'detail' | 'edit' | 'toggleStatus' | 'delete'

type StockInActionDescriptor = {
  key: StockInActionKey
  label: string
  type?: '' | 'primary' | 'danger'
  disabled?: boolean
  loading?: boolean
  danger?: boolean
}

const canQueryStockIn = checkPermi(['erp:stock-in:query'])
const canCreateStockIn = checkPermi(['erp:stock-in:create'])
const canUpdateStockIn = checkPermi(['erp:stock-in:update'])
const canUpdateStockInStatus = checkPermi(['erp:stock-in:update-status'])
const canDeleteStockIn = checkPermi(['erp:stock-in:delete'])
const canExportStockIn = checkPermi(['erp:stock-in:export'])

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const listLoadFailed = ref(false)
const list = ref<StockInListRow[]>([])
const total = ref(0)
const advancedSearchVisible = ref(false)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  no: undefined,
  productId: undefined,
  supplierId: undefined,
  warehouseId: undefined,
  inTime: [],
  status: undefined,
  remark: undefined,
  creator: undefined
})
const queryFormRef = ref()
const exportLoading = ref(false)
const deletingIds = ref<number[]>([])
const statusUpdatingIds = ref<number[]>([])
const productList = ref<ProductVO[]>([])
const warehouseList = ref<WarehouseVO[]>([])
const supplierList = ref<SupplierVO[]>([])
const userList = ref<UserVO[]>([])
const selectionList = ref<StockInListRow[]>([])
const formRef = ref()

const selectedIds = computed(() => selectionList.value.map((item) => item.id))
const canRetryList = computed(() => listLoadFailed.value && !loading.value)
const advancedFilterCount = computed(() => {
  const fields = [queryParams.warehouseId, queryParams.creator, queryParams.status, queryParams.remark]
  return fields.filter((item) => item !== undefined && item !== null && item !== '').length
})
const disableBatchDelete = computed(
  () => selectedIds.value.length === 0 || deletingIds.value.length > 0
)

const formatDateValue = (value?: Date | string | number) =>
  value ? formatDate(value, 'YYYY-MM-DD') : '-'

const formatCount = (value?: number | string | null) => {
  const numberValue = Number(value || 0)
  if (Number.isInteger(numberValue)) {
    return numberValue.toLocaleString('zh-CN')
  }
  return numberValue.toFixed(3).replace(/\.?0+$/, '')
}

const formatCurrency = (value?: number | string | null) =>
  new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(Number(value || 0))

const canEdit = (row: StockInListRow) => row.status !== 20
const canApprove = (row: StockInListRow) => row.status === 10
const isDeletingRow = (id?: number) => !!id && deletingIds.value.includes(id)
const isUpdatingStatus = (id?: number) => !!id && statusUpdatingIds.value.includes(id)

const getAllActionDescriptors = (row: StockInListRow): StockInActionDescriptor[] => {
  const actions: StockInActionDescriptor[] = []

  if (canQueryStockIn) {
    actions.push({ key: 'detail', label: '详情', type: 'primary' })
  }
  if (canUpdateStockIn && canEdit(row)) {
    actions.push({ key: 'edit', label: '编辑' })
  }
  if (canUpdateStockInStatus) {
    actions.push({
      key: 'toggleStatus',
      label: canApprove(row) ? '审批' : '反审�?,
      type: canApprove(row) ? 'primary' : 'danger',
      disabled: isUpdatingStatus(row.id),
      loading: isUpdatingStatus(row.id)
    })
  }
  if (canDeleteStockIn) {
    actions.push({
      key: 'delete',
      label: '删除',
      disabled: isDeletingRow(row.id),
      danger: true
    })
  }

  return actions
}

const getInlineActionDescriptors = (row: StockInListRow) => getAllActionDescriptors(row).slice(0, 3)

const getOverflowActionDescriptors = (row: StockInListRow) => {
  const inlineKeys = new Set(getInlineActionDescriptors(row).map((item) => item.key))
  return getAllActionDescriptors(row).filter((item) => !inlineKeys.has(item.key))
}

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
    const data = await StockInApi.getStockInPage(queryParams)
    list.value = data.list || []
    total.value = data.total || 0
  } catch {
    list.value = []
    total.value = 0
    listLoadFailed.value = true
  } finally {
    loading.value = false
  }
}

const loadFilterOptions = async () => {
  const [products, warehouses, suppliers, users] = await Promise.all([
    ProductApi.getProductSimpleList(),
    WarehouseApi.getWarehouseSimpleList(),
    SupplierApi.getSupplierSimpleList(),
    UserApi.getSimpleUserList()
  ])
  productList.value = products
  warehouseList.value = warehouses
  supplierList.value = suppliers
  userList.value = users
}

const handleQuery = async () => {
  queryParams.pageNo = 1
  await getList()
}

const resetQuery = async () => {
  queryFormRef.value?.resetFields()
  await handleQuery()
}

const toggleAdvancedSearch = () => {
  advancedSearchVisible.value = !advancedSearchVisible.value
}

const handleRetryList = async () => {
  await getList()
}

const openForm = (type: string, id?: number) => {
  formRef.value?.open(type, id)
}

const handleDelete = async (ids: number[]) => {
  if (!ids.length) {
    return
  }
  try {
    await message.delConfirm()
    setIdsLoading(deletingIds, ids, true)
    await StockInApi.deleteStockIn(ids)
    message.success(t('common.delSuccess'))
    await getList()
    selectionList.value = selectionList.value.filter((item) => !ids.includes(item.id))
  } catch {
  } finally {
    setIdsLoading(deletingIds, ids, false)
  }
}

const handleUpdateStatus = async (row: StockInListRow) => {
  if (!row.id) {
    return
  }
  const nextStatus = canApprove(row) ? 20 : 10
  const actionText = nextStatus === 20 ? '审批' : '反审�?
  try {
    await message.confirm(`确定${actionText}该入库单吗？`)
    setIdsLoading(statusUpdatingIds, [row.id], true)
    await StockInApi.updateStockInStatus(row.id, nextStatus)
    message.success(`${actionText}成功`)
    await getList()
  } catch {
  } finally {
    setIdsLoading(statusUpdatingIds, [row.id], false)
  }
}

const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await StockInApi.exportStockIn(queryParams)
    download.excel(data, '其他入库�?xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

const handleSelectionChange = (rows: StockInListRow[]) => {
  selectionList.value = rows
}

const handleCommand = async (command: StockInActionKey | string, row: StockInListRow) => {
  switch (command) {
    case 'detail':
      openForm('detail', row.id)
      break
    case 'edit':
      openForm('update', row.id)
      break
    case 'toggleStatus':
      await handleUpdateStatus(row)
      break
    case 'delete':
      await handleDelete([row.id])
      break
  }
}

onMounted(async () => {
  await Promise.allSettled([getList(), loadFilterOptions()])
})
</script>

<style scoped>
.stock-in-page__filter-card,
.stock-in-page__list-card {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(248, 250, 252, 0.96));
}

.stock-in-page__title {
  margin-bottom: 18px;
  color: #0f172a;
  font-size: 24px;
  line-height: 32px;
  font-weight: 700;
}

.stock-in-query {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stock-in-query__grid {
  display: grid;
  gap: 16px;
}

.stock-in-query__grid--primary {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.stock-in-query__grid--advanced {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.stock-in-query__footer,
.stock-in-toolbar,
.stock-in-page__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.stock-in-query__actions,
.stock-in-toolbar__actions,
.stock-in-toolbar__meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.stock-in-query__filter-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 18px;
  padding: 0 6px;
  margin-left: 6px;
  border-radius: 999px;
  background: rgba(37, 99, 235, 0.1);
  color: #2563eb;
  font-size: 12px;
  line-height: 18px;
  font-weight: 600;
}

.stock-in-page__table-shell {
  overflow-x: auto;
}

.stock-in-ledger {
  min-width: 980px;
}

.stock-in-ledger :deep(.el-table__header-wrapper) {
  position: sticky;
  top: 0;
  z-index: 3;
}

.ledger-order,
.ledger-product,
.ledger-party,
.ledger-number,
.ledger-amount {
  min-width: 0;
}

.ledger-order__no,
.ledger-amount {
  color: #0f172a;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
}

.ledger-order__no {
  line-height: 22px;
  word-break: break-all;
}

.ledger-order__meta,
.ledger-party {
  color: #64748b;
  font-size: 12px;
  line-height: 18px;
}

.ledger-product {
  color: #0f172a;
  line-height: 22px;
  word-break: break-word;
}

.ledger-number,
.ledger-amount {
  width: 100%;
  text-align: right;
}

.ledger-actions {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 8px;
  flex-wrap: wrap;
  min-width: 0;
}

.ledger-actions__more {
  width: 28px;
  height: 28px;
  padding: 0;
  border-radius: 999px;
}

.stock-in-empty {
  min-height: 156px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #64748b;
}

.stock-in-empty__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 16px;
  background: linear-gradient(180deg, rgba(37, 99, 235, 0.08), rgba(14, 165, 233, 0.08));
  color: #2563eb;
  font-size: 22px;
}

.stock-in-empty__title {
  font-size: 14px;
  line-height: 22px;
  font-weight: 600;
}

.stock-in-empty--error .stock-in-empty__icon {
  background: linear-gradient(180deg, rgba(251, 113, 133, 0.12), rgba(251, 191, 36, 0.08));
  color: #e11d48;
}

.stock-in-page__record-count {
  color: #64748b;
  font-size: 13px;
  line-height: 20px;
}

.stock-in-query-collapse-enter-active,
.stock-in-query-collapse-leave-active {
  transition: all 0.2s ease;
}

.stock-in-query-collapse-enter-from,
.stock-in-query-collapse-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

@media (max-width: 1279px) {
  .stock-in-query__grid--primary,
  .stock-in-query__grid--advanced {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 959px) {
  .stock-in-query__grid--primary,
  .stock-in-query__grid--advanced {
    grid-template-columns: minmax(0, 1fr);
  }

  .stock-in-query__footer,
  .stock-in-toolbar,
  .stock-in-page__footer {
    align-items: stretch;
    flex-direction: column;
  }

  .stock-in-query__actions,
  .stock-in-toolbar__actions,
  .stock-in-toolbar__meta {
    width: 100%;
  }
}
</style>
