<template>
  <doc-alert title="【库存】其他入库、其他出库" url="https://doc.iocoder.cn/erp/stock-in-out/" />

  <ContentWrap class="stock-out-page__filter-card">
    <div class="stock-out-page__title">其他出库台账</div>
    <el-form
      ref="queryFormRef"
      :model="queryParams"
      label-position="top"
      class="stock-out-query"
    >
      <div class="stock-out-query__grid stock-out-query__grid--primary">
        <el-form-item label="出库单号" prop="no">
          <el-input
            v-model="queryParams.no"
            placeholder="请输入出库单号"
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
        <el-form-item label="出库时间" prop="outTime">
          <el-date-picker
            v-model="queryParams.outTime"
            value-format="YYYY-MM-DD HH:mm:ss"
            type="daterange"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            range-separator="-"
            :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
          />
        </el-form-item>
        <el-form-item label="客户" prop="customerId">
          <el-select v-model="queryParams.customerId" clearable filterable placeholder="请选择客户">
            <el-option
              v-for="item in customerList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
      </div>

      <transition name="stock-out-query-collapse">
        <div
          v-if="advancedSearchVisible"
          class="stock-out-query__grid stock-out-query__grid--advanced"
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
              placeholder="请输入备注"
              clearable
              @keyup.enter="handleQuery"
            />
          </el-form-item>
        </div>
      </transition>

      <div class="stock-out-query__footer">
        <el-button link type="primary" @click="toggleAdvancedSearch">
          {{ advancedSearchVisible ? '收起高级筛选' : '展开高级筛选' }}
          <span v-if="advancedFilterCount" class="stock-out-query__filter-count">
            {{ advancedFilterCount }}
          </span>
          <Icon :icon="advancedSearchVisible ? 'ep:arrow-up' : 'ep:arrow-down'" class="ml-4px" />
        </el-button>
        <div class="stock-out-query__actions">
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

  <ContentWrap class="stock-out-page__list-card">
    <div class="stock-out-toolbar">
      <div class="stock-out-toolbar__actions">
        <el-button
          v-if="canCreateStockOut"
          type="primary"
          @click="openForm('create')"
          v-hasPermi="['erp:stock-out:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增出库单
        </el-button>
        <el-button
          v-if="canExportStockOut"
          plain
          :loading="exportLoading"
          @click="handleExport"
          v-hasPermi="['erp:stock-out:export']"
        >
          <Icon icon="ep:download" class="mr-5px" /> 导出数据
        </el-button>
      </div>
      <div class="stock-out-toolbar__meta">
        <el-button
          v-if="canDeleteStockOut"
          plain
          type="danger"
          :disabled="disableBatchDelete"
          @click="handleDelete(selectedIds)"
          v-hasPermi="['erp:stock-out:delete']"
        >
          <Icon icon="ep:delete" class="mr-5px" /> 批量删除
        </el-button>
      </div>
    </div>

    <div class="stock-out-page__table-shell">
      <el-table
        v-loading="loading"
        :data="list"
        :stripe="true"
        class="stock-out-ledger"
        @selection-change="handleSelectionChange"
      >
        <template #empty>
          <div v-if="listLoadFailed" class="stock-out-empty stock-out-empty--error">
            <div class="stock-out-empty__icon">
              <Icon icon="ep:warning-filled" />
            </div>
            <div class="stock-out-empty__title">列表加载失败</div>
            <el-button type="primary" plain :disabled="!canRetryList" @click="handleRetryList">
              重试加载
            </el-button>
          </div>
          <div v-else class="stock-out-empty">
            <div class="stock-out-empty__icon">
              <Icon icon="ep:box" />
            </div>
            <div class="stock-out-empty__title">暂无其他出库记录</div>
          </div>
        </template>

        <el-table-column width="36" type="selection" />
        <el-table-column label="出库信息" min-width="188">
          <template #default="{ row }">
            <div class="ledger-order">
              <div class="ledger-order__no">{{ row.no || '-' }}</div>
              <div class="ledger-order__meta">出库 {{ formatDateValue(row.outTime) }}</div>
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
        <el-table-column label="客户" min-width="160">
          <template #default="{ row }">
            <div class="ledger-party">{{ row.customerName || '-' }}</div>
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

    <div class="stock-out-page__footer">
      <div class="stock-out-page__record-count">共 {{ total }} 条记录</div>
      <Pagination
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </div>
  </ContentWrap>

  <StockOutForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import { getIntDictOptions, DICT_TYPE } from '@/utils/dict'
import { formatDate } from '@/utils/formatTime'
import download from '@/utils/download'
import { StockOutApi, StockOutVO } from '@/api/erp/stock/out'
import StockOutForm from './StockOutForm.vue'
import { ProductApi, ProductVO } from '@/api/erp/product/product'
import { WarehouseApi, WarehouseVO } from '@/api/erp/stock/warehouse'
import { UserVO } from '@/api/system/user'
import * as UserApi from '@/api/system/user'
import { checkPermi } from '@/utils/permission'
import { CustomerApi, CustomerVO } from '@/api/erp/sale/customer'
import { useUserStoreWithOut } from '@/store/modules/user'

defineOptions({ name: 'ErpStockOut' })

const STOCK_OUT_STATUS = {
  DRAFT: 0,
  PROCESS: 10,
  APPROVE: 20,
  REJECT: 30
} as const

type StockOutListRow = StockOutVO & {
  productNames?: string
  customerName?: string
  creatorName?: string
}

type StockOutActionKey = 'detail' | 'edit' | 'submit' | 'cancelApproval' | 'toggleStatus' | 'delete'

type StockOutActionDescriptor = {
  key: StockOutActionKey
  label: string
  type?: '' | 'primary' | 'danger'
  disabled?: boolean
  loading?: boolean
  danger?: boolean
}

const canQueryStockOut = checkPermi(['erp:stock-out:query'])
const canCreateStockOut = checkPermi(['erp:stock-out:create'])
const canUpdateStockOut = checkPermi(['erp:stock-out:update'])
const canUpdateStockOutStatus = checkPermi(['erp:stock-out:update-status'])
const canDeleteStockOut = checkPermi(['erp:stock-out:delete'])
const canExportStockOut = checkPermi(['erp:stock-out:export'])
const canSubmitStockOut = checkPermi(['erp:stock-out:submit'])
const canCancelStockOutApproval = checkPermi(['erp:stock-out:cancel-approval'])

const message = useMessage()
const { t } = useI18n()
const userStore = useUserStoreWithOut()
const currentUserId = computed(() => String(userStore.getUser.id || ''))

const loading = ref(true)
const listLoadFailed = ref(false)
const list = ref<StockOutListRow[]>([])
const total = ref(0)
const advancedSearchVisible = ref(false)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  no: undefined,
  productId: undefined,
  customerId: undefined,
  warehouseId: undefined,
  outTime: [],
  status: undefined,
  remark: undefined,
  creator: undefined
})
const queryFormRef = ref()
const exportLoading = ref(false)
const deletingIds = ref<number[]>([])
const cancelApprovalIds = ref<number[]>([])
const statusUpdatingIds = ref<number[]>([])
const productList = ref<ProductVO[]>([])
const warehouseList = ref<WarehouseVO[]>([])
const customerList = ref<CustomerVO[]>([])
const userList = ref<UserVO[]>([])
const selectionList = ref<StockOutListRow[]>([])
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

const canEdit = (row: StockOutListRow) => row.status !== 20
const canApprove = (row: StockOutListRow) => row.status === 10
const isDeletingRow = (id?: number) => !!id && deletingIds.value.includes(id)
const isCancelingApproval = (id?: number) => !!id && cancelApprovalIds.value.includes(id)
const isUpdatingStatus = (id?: number) => !!id && statusUpdatingIds.value.includes(id)

const getCanSubmit = (row: StockOutListRow) => {
  return (
    row.status === STOCK_OUT_STATUS.REJECT ||
    (row.status === STOCK_OUT_STATUS.DRAFT && !row.processInstanceId) ||
    (row.status === STOCK_OUT_STATUS.PROCESS && !row.processInstanceId)
  )
}

const getCanCancelApproval = (row: StockOutListRow) => {
  return (
    row.status === STOCK_OUT_STATUS.PROCESS &&
    !!row.processInstanceId &&
    row.creator === currentUserId.value
  )
}

const getAllActionDescriptors = (row: StockOutListRow): StockOutActionDescriptor[] => {
  const actions: StockOutActionDescriptor[] = []

  if (canQueryStockOut) {
    actions.push({ key: 'detail', label: '详情', type: 'primary' })
  }
  if (canUpdateStockOut && canEdit(row)) {
    actions.push({ key: 'edit', label: '编辑' })
  }
  if (canSubmitStockOut && getCanSubmit(row)) {
    actions.push({
      key: 'submit',
      label: row.status === STOCK_OUT_STATUS.REJECT ? '重新提交审批' : '提交审批'
    })
  }
  if (canCancelStockOutApproval && getCanCancelApproval(row)) {
    actions.push({
      key: 'cancelApproval',
      label: '撤回审批',
      disabled: isCancelingApproval(row.id),
      loading: isCancelingApproval(row.id)
    })
  }
  if (canDeleteStockOut) {
    actions.push({
      key: 'delete',
      label: '删除',
      disabled: isDeletingRow(row.id),
      danger: true
    })
  }

  return actions
}

const getInlineActionDescriptors = (row: StockOutListRow) => getAllActionDescriptors(row).slice(0, 3)

const getOverflowActionDescriptors = (row: StockOutListRow) => {
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
    const data = await StockOutApi.getStockOutPage(queryParams)
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
  const [products, warehouses, customers, users] = await Promise.all([
    ProductApi.getProductSimpleList(),
    WarehouseApi.getWarehouseSimpleList(),
    CustomerApi.getCustomerSimpleList(),
    UserApi.getSimpleUserList()
  ])
  productList.value = products
  warehouseList.value = warehouses
  customerList.value = customers
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
    await StockOutApi.deleteStockOut(ids)
    message.success(t('common.delSuccess'))
    await getList()
    selectionList.value = selectionList.value.filter((item) => !ids.includes(item.id))
  } catch {
  } finally {
    setIdsLoading(deletingIds, ids, false)
  }
}

const handleUpdateStatus = async (row: StockOutListRow) => {
  if (!row.id) {
    return
  }
  const nextStatus = canApprove(row) ? 20 : 10
  const actionText = nextStatus === 20 ? '审批' : '反审批'
  try {
    await message.confirm(`确定${actionText}该出库单吗？`)
    setIdsLoading(statusUpdatingIds, [row.id], true)
    await StockOutApi.updateStockOutStatus(row.id, nextStatus)
    message.success(`${actionText}成功`)
    await getList()
  } catch {
  } finally {
    setIdsLoading(statusUpdatingIds, [row.id], false)
  }
}

/** 提交审批 */
const handleSubmit = async (row: StockOutListRow) => {
  if (!row.id) return
  try {
    await message.confirm(`确定提交审批该出库单吗？`)
    await StockOutApi.submitStockOut({ id: row.id })
    message.success('提交审批成功')
    await getList()
  } catch {}
}

/** 撤回审批 */
const handleCancelApproval = async (row: StockOutListRow) => {
  if (!row.id || cancelApprovalIds.value.includes(row.id)) return
  try {
    const { value } = await ElMessageBox.prompt('请输入撤回原因', '撤回审批', {
      confirmButtonText: t('common.ok'),
      cancelButtonText: t('common.cancel'),
      inputPattern: /^[\s\S]*.*\S[\s\S]*$/,
      inputErrorMessage: '撤回原因不能为空'
    })
    setIdsLoading(cancelApprovalIds, [row.id], true)
    await StockOutApi.cancelStockOutApproval({
      id: row.id,
      reason: value
    })
    message.success('撤回审批成功')
    await getList()
  } catch {
  } finally {
    setIdsLoading(cancelApprovalIds, [row.id], false)
  }
}

const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await StockOutApi.exportStockOut(queryParams)
    download.excel(data, '其他出库单.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

const handleSelectionChange = (rows: StockOutListRow[]) => {
  selectionList.value = rows
}

const handleCommand = async (command: StockOutActionKey | string, row: StockOutListRow) => {
  switch (command) {
    case 'detail':
      openForm('detail', row.id)
      break
    case 'edit':
      openForm('update', row.id)
      break
    case 'submit':
      await handleSubmit(row)
      break
    case 'cancelApproval':
      await handleCancelApproval(row)
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
.stock-out-page__filter-card,
.stock-out-page__list-card {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(248, 250, 252, 0.96));
}

.stock-out-page__title {
  margin-bottom: 18px;
  color: #0f172a;
  font-size: 24px;
  line-height: 32px;
  font-weight: 700;
}

.stock-out-query {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stock-out-query__grid {
  display: grid;
  gap: 16px;
}

.stock-out-query__grid--primary,
.stock-out-query__grid--advanced {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.stock-out-query__footer,
.stock-out-toolbar,
.stock-out-page__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.stock-out-query__actions,
.stock-out-toolbar__actions,
.stock-out-toolbar__meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.stock-out-query__filter-count {
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

.stock-out-page__table-shell {
  overflow-x: auto;
}

.stock-out-ledger {
  min-width: 980px;
}

.stock-out-ledger :deep(.el-table__header-wrapper) {
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

.stock-out-empty {
  min-height: 156px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #64748b;
}

.stock-out-empty__icon {
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

.stock-out-empty__title {
  font-size: 14px;
  line-height: 22px;
  font-weight: 600;
}

.stock-out-empty--error .stock-out-empty__icon {
  background: linear-gradient(180deg, rgba(251, 113, 133, 0.12), rgba(251, 191, 36, 0.08));
  color: #e11d48;
}

.stock-out-page__record-count {
  color: #64748b;
  font-size: 13px;
  line-height: 20px;
}

.stock-out-query-collapse-enter-active,
.stock-out-query-collapse-leave-active {
  transition: all 0.2s ease;
}

.stock-out-query-collapse-enter-from,
.stock-out-query-collapse-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

@media (max-width: 1279px) {
  .stock-out-query__grid--primary,
  .stock-out-query__grid--advanced {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 959px) {
  .stock-out-query__grid--primary,
  .stock-out-query__grid--advanced {
    grid-template-columns: minmax(0, 1fr);
  }

  .stock-out-query__footer,
  .stock-out-toolbar,
  .stock-out-page__footer {
    align-items: stretch;
    flex-direction: column;
  }

  .stock-out-query__actions,
  .stock-out-toolbar__actions,
  .stock-out-toolbar__meta {
    width: 100%;
  }
}
</style>
