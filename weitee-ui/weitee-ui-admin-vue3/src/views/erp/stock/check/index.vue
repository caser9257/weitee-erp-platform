<template>

  <ContentWrap class="stock-check-page__filter-card">
    <div class="stock-check-page__title">库存盘点台账</div>
    <el-form
      ref="queryFormRef"
      :model="queryParams"
      label-position="top"
      class="stock-check-query"
    >
      <div class="stock-check-query__grid stock-check-query__grid--primary">
        <el-form-item label="盘点单号" prop="no">
          <el-input
            v-model="queryParams.no"
            placeholder="请输入盘点单号"
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
        <el-form-item label="盘点时间" prop="checkTime">
          <el-date-picker
            v-model="queryParams.checkTime"
            value-format="YYYY-MM-DD HH:mm:ss"
            type="daterange"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            range-separator="-"
            :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
          />
        </el-form-item>
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
      </div>

      <transition name="stock-check-query-collapse">
        <div
          v-if="advancedSearchVisible"
          class="stock-check-query__grid stock-check-query__grid--advanced"
        >
          <el-form-item label="创建人" prop="creator">
            <el-select v-model="queryParams.creator" clearable filterable placeholder="请选择创建人">
              <el-option
                v-for="item in userList"
                :key="item.id"
                :label="item.nickname"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" clearable placeholder="请选择状态">
              <el-option
                v-for="option in stockCheckStatusOptions"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="备注" prop="remark">
            <el-input
              v-model="queryParams.remark"
              placeholder="请输入备注"
              clearable
              @keyup.enter="handleQuery"
            />
          </el-form-item>
        </div>
      </transition>

      <div class="stock-check-query__footer">
        <el-button link type="primary" @click="toggleAdvancedSearch">
          {{ advancedSearchVisible ? '收起高级筛选' : '展开高级筛选' }}
          <span v-if="advancedFilterCount" class="stock-check-query__filter-count">
            {{ advancedFilterCount }}
          </span>
          <Icon :icon="advancedSearchVisible ? 'ep:arrow-up' : 'ep:arrow-down'" class="ml-4px" />
        </el-button>
        <div class="stock-check-query__actions">
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

  <ContentWrap class="stock-check-page__list-card">
    <div class="stock-check-toolbar">
      <div class="stock-check-toolbar__actions">
        <el-button
          v-if="canCreateStockCheck"
          type="primary"
          @click="openForm('create')"
          v-hasPermi="['erp:stock-check:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增盘点单
        </el-button>
        <el-button
          v-if="canExportStockCheck"
          plain
          :loading="exportLoading"
          @click="handleExport"
          v-hasPermi="['erp:stock-check:export']"
        >
          <Icon icon="ep:download" class="mr-5px" /> 导出数据
        </el-button>
      </div>
      <div class="stock-check-toolbar__meta">
        <el-button
          v-if="canDeleteStockCheck"
          plain
          type="danger"
          :disabled="disableBatchDelete"
          @click="handleDelete(selectedIds)"
          v-hasPermi="['erp:stock-check:delete']"
        >
          <Icon icon="ep:delete" class="mr-5px" /> 批量删除
        </el-button>
      </div>
    </div>

    <div class="stock-check-page__table-shell">
      <el-table
        v-loading="loading"
        :data="list"
        :stripe="true"
        class="stock-check-ledger"
        @selection-change="handleSelectionChange"
      >
        <template #empty>
          <div v-if="listLoadFailed" class="stock-check-empty stock-check-empty--error">
            <div class="stock-check-empty__icon">
              <Icon icon="ep:warning-filled" />
            </div>
            <div class="stock-check-empty__title">列表加载失败</div>
            <el-button type="primary" plain :disabled="!canRetryList" @click="handleRetryList">
              重试加载
            </el-button>
          </div>
          <div v-else class="stock-check-empty">
            <div class="stock-check-empty__icon">
              <Icon icon="ep:box" />
            </div>
            <div class="stock-check-empty__title">暂无库存盘点记录</div>
          </div>
        </template>

        <el-table-column width="36" type="selection" :selectable="canSelectForDelete" />
        <el-table-column label="盘点信息" min-width="188">
          <template #default="{ row }">
            <div class="ledger-order">
              <div class="ledger-order__no">{{ row.no || '-' }}</div>
              <div class="ledger-order__meta">盘点 {{ formatDateValue(row.checkTime) }}</div>
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
        <el-table-column label="状态" min-width="96" fixed="right">
          <template #default="{ row }">
            <el-tag
              size="small"
              effect="light"
              :type="getStockCheckStatusTagType(row.status)"
            >
              {{ resolveStockCheckStatus(row.status).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right" align="left">
          <template #default="{ row }">
            <div class="ledger-actions">
              <template
                v-for="action in getInlineActionDescriptors(row)"
                :key="`${row.id}-${action.key}`"
              >
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
                :disabled="isRowBusy(row.id)"
                @command="(command) => handleCommand(command, row)"
              >
                <el-tooltip content="更多操作" placement="top">
                  <el-button
                    link
                    type="primary"
                    class="ledger-actions__more"
                    :disabled="isRowBusy(row.id)"
                    :loading="isOverflowActionLoading(row)"
                  >
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

    <div class="stock-check-page__footer">
      <div class="stock-check-page__record-count">共 {{ total }} 条记录</div>
      <Pagination
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </div>
  </ContentWrap>

  <StockCheckForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import { formatDate } from '@/utils/formatTime'
import {
  deriveStockCheckActions,
  resolveStockCheckStatus,
  STOCK_CHECK_STATUS
} from './stockCheckStatus.helpers'
import { createStockCheckListRequestGate } from './stockCheckListRequest.helpers'
import download from '@/utils/download'
import { StockCheckApi, StockCheckVO } from '@/api/erp/stock/check'
import StockCheckForm from './StockCheckForm.vue'
import { ProductApi, ProductVO } from '@/api/erp/product/product'
import { WarehouseApi, WarehouseVO } from '@/api/erp/stock/warehouse'
import { UserVO } from '@/api/system/user'
import * as UserApi from '@/api/system/user'
import { checkPermi } from '@/utils/permission'

defineOptions({ name: 'ErpStockCheck' })

type StockCheckListRow = StockCheckVO & {
  checkTime?: Date | string | number
  productNames?: string
  creatorName?: string
}

type StockCheckLifecycleActionKey = 'start' | 'submit' | 'approveAndClose' | 'reject'
type StockCheckActionKey = 'detail' | 'edit' | StockCheckLifecycleActionKey | 'delete'

type StockCheckActionDescriptor = {
  key: StockCheckActionKey
  label: string
  type?: '' | 'primary' | 'danger'
  disabled?: boolean
  loading?: boolean
  danger?: boolean
}

const canQueryStockCheck = checkPermi(['erp:stock-check:query'])
const canCreateStockCheck = checkPermi(['erp:stock-check:create'])
const canUpdateStockCheck = checkPermi(['erp:stock-check:update'])
const canManageStockCheckLifecycle = checkPermi(['erp:stock-check:update-status'])
const canDeleteStockCheck = checkPermi(['erp:stock-check:delete'])
const canExportStockCheck = checkPermi(['erp:stock-check:export'])

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const listLoadFailed = ref(false)
const list = ref<StockCheckListRow[]>([])
const total = ref(0)
const advancedSearchVisible = ref(false)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  no: undefined,
  productId: undefined,
  warehouseId: undefined,
  checkTime: [],
  status: undefined,
  remark: undefined,
  creator: undefined
})
const queryFormRef = ref()
const exportLoading = ref(false)
const deletingIds = ref<number[]>([])
const lifecycleLoadingById = ref<Record<number, StockCheckLifecycleActionKey | undefined>>({})
const productList = ref<ProductVO[]>([])
const warehouseList = ref<WarehouseVO[]>([])
const userList = ref<UserVO[]>([])
const selectionList = ref<StockCheckListRow[]>([])
const formRef = ref()
const listRequestGate = createStockCheckListRequestGate()

const stockCheckStatusOptions = Object.values(STOCK_CHECK_STATUS).map((value) => ({
  value,
  label: resolveStockCheckStatus(value).label
}))

const stockCheckTagTypeMap = {
  neutral: 'info',
  primary: 'primary',
  warning: 'warning',
  success: 'success'
} as const

const selectedIds = computed(() => selectionList.value.map((item) => item.id))
const canRetryList = computed(() => listLoadFailed.value && !loading.value)
const advancedFilterCount = computed(() => {
  const fields = [queryParams.creator, queryParams.status, queryParams.remark]
  return fields.filter((item) => item !== undefined && item !== null && item !== '').length
})
const getLifecycleLoadingAction = (id?: number) =>
  id ? lifecycleLoadingById.value[id] : undefined

const isDeletingRow = (id?: number) => !!id && deletingIds.value.includes(id)
const isRowBusy = (id?: number) =>
  !!id && (isDeletingRow(id) || !!getLifecycleLoadingAction(id))

const canSelectForDelete = (row: StockCheckListRow) =>
  canDeleteStockCheck && deriveStockCheckActions(row.status).canDelete && !isRowBusy(row.id)

const disableBatchDelete = computed(() => {
  if (!selectionList.value.length) {
    return true
  }
  return selectionList.value.some(
    (row) => !deriveStockCheckActions(row.status).canDelete || isRowBusy(row.id)
  )
})

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

const getStockCheckStatusTagType = (status?: number) =>
  stockCheckTagTypeMap[resolveStockCheckStatus(status).tone]

const getAllActionDescriptors = (row: StockCheckListRow): StockCheckActionDescriptor[] => {
  const actions: StockCheckActionDescriptor[] = []
  const actionState = deriveStockCheckActions(row.status)
  const rowBusy = isRowBusy(row.id)
  const loadingAction = getLifecycleLoadingAction(row.id)

  if (canQueryStockCheck) {
    actions.push({ key: 'detail', label: '详情', type: 'primary', disabled: rowBusy })
  }
  if (canUpdateStockCheck && actionState.canEdit) {
    actions.push({ key: 'edit', label: '编辑', disabled: rowBusy })
  }
  if (canManageStockCheckLifecycle && actionState.canStart) {
    actions.push({
      key: 'start',
      label: '开始盘点',
      type: 'primary',
      disabled: rowBusy,
      loading: loadingAction === 'start'
    })
  }
  if (canManageStockCheckLifecycle && actionState.canSubmit) {
    actions.push({
      key: 'submit',
      label: '提交审核',
      type: 'primary',
      disabled: rowBusy,
      loading: loadingAction === 'submit'
    })
  }
  if (canManageStockCheckLifecycle && actionState.canApproveAndClose) {
    actions.push({
      key: 'approveAndClose',
      label: '审核并关闭',
      type: 'primary',
      disabled: rowBusy,
      loading: loadingAction === 'approveAndClose'
    })
  }
  if (canManageStockCheckLifecycle && actionState.canReject) {
    actions.push({
      key: 'reject',
      label: '驳回',
      type: 'danger',
      disabled: rowBusy,
      loading: loadingAction === 'reject',
      danger: true
    })
  }
  if (canDeleteStockCheck && actionState.canDelete) {
    actions.push({
      key: 'delete',
      label: '删除',
      disabled: rowBusy,
      loading: isDeletingRow(row.id),
      danger: true
    })
  }

  return actions
}

const getInlineActionDescriptors = (row: StockCheckListRow) => getAllActionDescriptors(row).slice(0, 3)

const getOverflowActionDescriptors = (row: StockCheckListRow) => {
  const inlineKeys = new Set(getInlineActionDescriptors(row).map((item) => item.key))
  return getAllActionDescriptors(row).filter((item) => !inlineKeys.has(item.key))
}

const isOverflowActionLoading = (row: StockCheckListRow) =>
  getOverflowActionDescriptors(row).some((action) => action.loading)

const setIdsLoading = (source: Ref<number[]>, ids: number[], loadingState: boolean) => {
  if (loadingState) {
    source.value = Array.from(new Set([...source.value, ...ids]))
    return
  }
  source.value = source.value.filter((item) => !ids.includes(item))
}

const setLifecycleLoading = (id: number, action?: StockCheckLifecycleActionKey) => {
  const nextLoadingById = { ...lifecycleLoadingById.value }
  if (action) {
    nextLoadingById[id] = action
  } else {
    delete nextLoadingById[id]
  }
  lifecycleLoadingById.value = nextLoadingById
}

const getList = async () => {
  const requestId = listRequestGate.issue()
  loading.value = true
  listLoadFailed.value = false
  try {
    const data = await StockCheckApi.getStockCheckPage(queryParams)
    if (!listRequestGate.isLatest(requestId)) {
      return false
    }
    list.value = data.list || []
    total.value = data.total || 0
    return true
  } catch {
    if (!listRequestGate.isLatest(requestId)) {
      return false
    }
    list.value = []
    total.value = 0
    listLoadFailed.value = true
    return false
  } finally {
    if (listRequestGate.isLatest(requestId)) {
      loading.value = false
    }
  }
}

const loadFilterOptions = async () => {
  const [products, warehouses, users] = await Promise.all([
    ProductApi.getProductSimpleList(),
    WarehouseApi.getWarehouseSimpleList(),
    UserApi.getSimpleUserList()
  ])
  productList.value = products
  warehouseList.value = warehouses
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
  const targetRows = ids.map((id) => list.value.find((row) => row.id === id))
  if (
    !ids.length ||
    targetRows.some(
      (row) => !row || !deriveStockCheckActions(row.status).canDelete || isRowBusy(row.id)
    )
  ) {
    return
  }
  setIdsLoading(deletingIds, ids, true)
  try {
    await message.delConfirm()
    await StockCheckApi.deleteStockCheck(ids)
    const refreshed = await getList()
    selectionList.value = selectionList.value.filter((item) => !ids.includes(item.id))
    if (refreshed) {
      message.success(t('common.delSuccess'))
    }
  } catch {
  } finally {
    setIdsLoading(deletingIds, ids, false)
  }
}

const lifecycleActionConfig = {
  start: {
    confirmText: '确定开始盘点该盘点单吗？',
    successText: '已开始盘点',
    execute: StockCheckApi.startCounting
  },
  submit: {
    confirmText: '确定将该盘点单提交审核吗？',
    successText: '已提交审核',
    execute: StockCheckApi.submitForReview
  },
  approveAndClose: {
    confirmText: '确定审核并关闭该盘点单吗？关闭后将写入库存并生成财务凭证。',
    successText: '盘点单已审核并关闭',
    execute: StockCheckApi.approveAndClose
  },
  reject: {
    confirmText: '确定驳回该盘点单吗？',
    successText: '盘点单已驳回',
    execute: StockCheckApi.reject
  }
} as const

const canExecuteLifecycleAction = (
  action: StockCheckLifecycleActionKey,
  row: StockCheckListRow
) => {
  const actionState = deriveStockCheckActions(row.status)
  return {
    start: actionState.canStart,
    submit: actionState.canSubmit,
    approveAndClose: actionState.canApproveAndClose,
    reject: actionState.canReject
  }[action]
}

const handleLifecycleAction = async (
  action: StockCheckLifecycleActionKey,
  row: StockCheckListRow
) => {
  if (
    !row.id ||
    !canManageStockCheckLifecycle ||
    !canExecuteLifecycleAction(action, row) ||
    isRowBusy(row.id)
  ) {
    return
  }
  const config = lifecycleActionConfig[action]
  setLifecycleLoading(row.id, action)
  try {
    await message.confirm(config.confirmText)
    await config.execute(row.id)
    const refreshed = await getList()
    if (refreshed) {
      message.success(config.successText)
    }
  } catch {
  } finally {
    setLifecycleLoading(row.id)
  }
}

const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await StockCheckApi.exportStockCheck(queryParams)
    download.excel(data, '库存盘点单.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

const handleSelectionChange = (rows: StockCheckListRow[]) => {
  selectionList.value = rows
}

const handleCommand = async (command: StockCheckActionKey | string, row: StockCheckListRow) => {
  if (isRowBusy(row.id)) {
    return
  }
  switch (command) {
    case 'detail':
      openForm('detail', row.id)
      break
    case 'edit':
      openForm('update', row.id)
      break
    case 'start':
    case 'submit':
    case 'approveAndClose':
    case 'reject':
      await handleLifecycleAction(command, row)
      break
    case 'delete':
      await handleDelete(row.id ? [row.id] : [])
      break
  }
}

onMounted(async () => {
  await Promise.allSettled([getList(), loadFilterOptions()])
})
</script>

<style scoped>
.stock-check-page__filter-card,
.stock-check-page__list-card {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(248, 250, 252, 0.96));
}

.stock-check-page__title {
  margin-bottom: 18px;
  color: #0f172a;
  font-size: 24px;
  line-height: 32px;
  font-weight: 700;
}

.stock-check-query {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stock-check-query__grid {
  display: grid;
  gap: 16px;
}

.stock-check-query__grid--primary {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.stock-check-query__grid--advanced {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.stock-check-query__footer,
.stock-check-toolbar,
.stock-check-page__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.stock-check-query__actions,
.stock-check-toolbar__actions,
.stock-check-toolbar__meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.stock-check-query__filter-count {
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

.stock-check-page__table-shell {
  overflow-x: auto;
}

.stock-check-ledger {
  min-width: 920px;
}

.stock-check-ledger :deep(.el-table__header-wrapper) {
  position: sticky;
  top: 0;
  z-index: 3;
}

.ledger-order,
.ledger-product,
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

.ledger-order__meta {
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

.stock-check-empty {
  min-height: 156px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #64748b;
}

.stock-check-empty__icon {
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

.stock-check-empty__title {
  font-size: 14px;
  line-height: 22px;
  font-weight: 600;
}

.stock-check-empty--error .stock-check-empty__icon {
  background: linear-gradient(180deg, rgba(251, 113, 133, 0.12), rgba(251, 191, 36, 0.08));
  color: #e11d48;
}

.stock-check-page__record-count {
  color: #64748b;
  font-size: 13px;
  line-height: 20px;
}

.stock-check-query-collapse-enter-active,
.stock-check-query-collapse-leave-active {
  transition: all 0.2s ease;
}

.stock-check-query-collapse-enter-from,
.stock-check-query-collapse-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

@media (max-width: 1279px) {
  .stock-check-query__grid--primary,
  .stock-check-query__grid--advanced {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 959px) {
  .stock-check-query__grid--primary,
  .stock-check-query__grid--advanced {
    grid-template-columns: minmax(0, 1fr);
  }

  .stock-check-query__footer,
  .stock-check-toolbar,
  .stock-check-page__footer {
    align-items: stretch;
    flex-direction: column;
  }

  .stock-check-query__actions,
  .stock-check-toolbar__actions,
  .stock-check-toolbar__meta {
    width: 100%;
  }
}
</style>
