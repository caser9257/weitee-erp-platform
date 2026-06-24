<template>
<ContentWrap class="sale-out-page__filter-card">
    <div class="sale-out-page__title">销售出库台�?/div>
    <el-form ref="queryFormRef" :model="queryParams" label-position="top" class="sale-out-query">
      <div class="sale-out-query__grid sale-out-query__grid--primary">
        <el-form-item label="出库单号" prop="no">
          <el-input
            v-model="queryParams.no"
            placeholder="请输入出库单�?
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="客户" prop="customerId">
          <el-select
            v-model="queryParams.customerId"
            clearable
            filterable
            placeholder="请选择客户"
          >
            <el-option
              v-for="item in customerList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="关联订单" prop="orderNo">
          <el-input
            v-model="queryParams.orderNo"
            placeholder="请输入关联订�?
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="出库时间" prop="outTime">
          <el-date-picker
            v-model="queryParams.outTime"
            value-format="YYYY-MM-DD HH:mm:ss"
            type="daterange"
            start-placeholder="开始日�?
            end-placeholder="结束日期"
            range-separator="-"
            :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
          />
        </el-form-item>
      </div>

      <transition name="sale-out-query-collapse">
        <div v-if="advancedSearchVisible" class="sale-out-query__grid sale-out-query__grid--advanced">
          <el-form-item label="产品" prop="productId">
            <el-select
              v-model="queryParams.productId"
              clearable
              filterable
              placeholder="请选择产品"
            >
              <el-option
                v-for="item in productList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="仓库" prop="warehouseId">
            <el-select
              v-model="queryParams.warehouseId"
              clearable
              filterable
              placeholder="请选择仓库"
            >
              <el-option
                v-for="item in warehouseList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="创建�? prop="creator">
            <el-select
              v-model="queryParams.creator"
              clearable
              filterable
              placeholder="请选择创建�?
            >
              <el-option
                v-for="item in userList"
                :key="item.id"
                :label="item.nickname"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="结算账户" prop="accountId">
            <el-select
              v-model="queryParams.accountId"
              clearable
              filterable
              placeholder="请选择结算账户"
            >
              <el-option
                v-for="item in accountList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="收款状�? prop="receiptStatus">
            <el-select v-model="queryParams.receiptStatus" placeholder="请选择收款状�? clearable>
              <el-option label="未收�? value="0" />
              <el-option label="部分收款" value="1" />
              <el-option label="全部收款" value="2" />
            </el-select>
          </el-form-item>
          <el-form-item label="审核状�? prop="status">
            <el-select v-model="queryParams.status" placeholder="请选择审核状�? clearable>
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

      <div class="sale-out-query__footer">
        <el-button link type="primary" @click="toggleAdvancedSearch">
          {{ advancedSearchVisible ? '收起高级筛�? : '展开高级筛�? }}
          <span v-if="advancedFilterCount" class="sale-out-query__filter-count">
            {{ advancedFilterCount }}
          </span>
          <Icon :icon="advancedSearchVisible ? 'ep:arrow-up' : 'ep:arrow-down'" class="ml-4px" />
        </el-button>
        <div class="sale-out-query__actions">
          <el-button @click="resetQuery" :disabled="loading">
            <Icon icon="ep:refresh" class="mr-5px" /> 重置
          </el-button>
          <el-button type="primary" @click="handleQuery" :loading="loading">
            <Icon icon="ep:search" class="mr-5px" /> 查询
          </el-button>
        </div>
      </div>
    </el-form>
  </ContentWrap>

  <ContentWrap class="sale-out-page__list-card">
    <div class="sale-out-toolbar">
      <div class="sale-out-toolbar__actions">
        <el-button type="primary" @click="openForm('create')" v-hasPermi="['erp:sale-out:create']">
          <Icon icon="ep:plus" class="mr-5px" /> 新增出库
        </el-button>
        <el-button
          plain
          @click="handleExport"
          :loading="exportLoading"
          v-hasPermi="['erp:sale-out:export']"
        >
          <Icon icon="ep:download" class="mr-5px" /> 导出数据
        </el-button>
      </div>
      <div class="sale-out-toolbar__meta">
        <el-button
          plain
          type="danger"
          :disabled="toolbarState.disableBatchDelete"
          @click="handleDelete(deletableSelectionIds)"
          v-hasPermi="['erp:sale-out:delete']"
        >
          <Icon icon="ep:delete" class="mr-5px" /> 批量删除
        </el-button>
      </div>
    </div>

    <el-table
      v-if="!isCompactLayout"
      v-loading="loading"
      :data="list"
      :stripe="true"
      class="sale-out-ledger"
      @selection-change="handleSelectionChange"
    >
      <template #empty>
        <div v-if="listLoadFailed" class="sale-out-empty sale-out-empty--error">
          <div class="sale-out-empty__icon">
            <Icon icon="ep:warning-filled" />
          </div>
          <div class="sale-out-empty__title">列表加载失败</div>
          <el-button type="primary" plain @click="handleRetryList" :disabled="!canRetryList">
            重试加载
          </el-button>
        </div>
        <div v-else class="sale-out-empty">
          <div class="sale-out-empty__icon">
            <Icon icon="ep:box" />
          </div>
          <div class="sale-out-empty__title">暂无出库记录</div>
        </div>
      </template>
      <el-table-column width="36" type="selection" :selectable="canDelete" />
      <el-table-column label="出库信息" min-width="144">
        <template #default="{ row }">
          <div class="ledger-order">
            <div class="ledger-order__no">{{ row.no || '-' }}</div>
            <div class="ledger-order__meta">出库 {{ formatDateValue(row.outTime) }}</div>
            <div class="ledger-order__meta">创建�?{{ row.creatorName || '-' }}</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="客户与订�? min-width="132">
        <template #default="{ row }">
          <div class="ledger-party">
            <div class="ledger-party__customer">{{ row.customerName || '-' }}</div>
            <div class="ledger-party__meta">
              <span class="ledger-party__tag">关联订单</span>
              <span class="ledger-party__text">{{ row.orderNo || '-' }}</span>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="产品摘要" min-width="108">
        <template #default="{ row }">
          <div class="ledger-product" :title="row.productNames || '-'">
            {{ row.productNames || '-' }}
          </div>
        </template>
      </el-table-column>
      <el-table-column label="数量 / 收款" min-width="152">
        <template #default="{ row }">
          <div class="ledger-progress">
            <div class="ledger-progress__section">
              <div class="ledger-progress__top">
                <span>出库数量</span>
                <strong>{{ formatCount(row.totalCount) }}</strong>
              </div>
            </div>
            <div class="ledger-progress__section">
              <div class="ledger-progress__top">
                <span>收款进度</span>
                <strong>{{ formatCurrency(row.receiptPrice) }} / {{ formatCurrency(row.totalPrice) }}</strong>
              </div>
              <el-progress
                :stroke-width="6"
                :show-text="false"
                :percentage="getReceiptPercent(row)"
                :color="resolveReceiptProgressColor(row)"
              />
            </div>
            <div class="ledger-progress__summary">
              <span>已收 {{ formatCurrency(row.receiptPrice) }}</span>
              <span>未收 {{ formatCurrency(getRemainingReceipt(row)) }}</span>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="金额结算（元�? min-width="116" align="right">
        <template #default="{ row }">
          <div class="ledger-finance">
            <div class="ledger-finance__amount">{{ formatCurrency(row.totalPrice) }}</div>
            <div class="ledger-finance__meta">已收 {{ formatCurrency(row.receiptPrice) }}</div>
            <div class="ledger-finance__sub">未收 {{ formatCurrency(getRemainingReceipt(row)) }}</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="状�? min-width="96">
        <template #default="{ row }">
          <div class="ledger-status">
            <div class="ledger-status__badges">
              <el-tag
                size="small"
                effect="light"
                :type="resolveErpAuditStatusTagType(row.status)"
              >
                {{ resolveErpAuditStatusLabel(row.status) }}
              </el-tag>
              <el-tag size="small" effect="light" :type="getReceiptStatusTagType(row)">
                {{ getReceiptStatusLabel(row) }}
              </el-tag>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" align="left">
        <template #default="{ row }">
          <div class="ledger-actions">
            <el-button
              v-if="canViewDetail"
              link
              type="primary"
              @click="openForm('detail', row.id)"
            >
              详情
            </el-button>
            <el-button
              v-if="canUpdate && canEdit(row)"
              link
              type="primary"
              @click="handleCommand('edit', row)"
            >
              编辑
            </el-button>
            <el-button
              v-if="canUpdateStatus && canApprove(row)"
              link
              type="success"
              :disabled="isUpdatingStatus(row.id)"
              @click="handleCommand('approve', row)"
            >
              审批
            </el-button>
            <el-button
              v-if="canUpdateStatus && canReverseApprove(row)"
              link
              type="warning"
              :disabled="isUpdatingStatus(row.id)"
              @click="handleCommand('reverseApprove', row)"
            >
              反审�?
            </el-button>
            <el-button
              v-if="canRemove"
              link
              type="danger"
              :disabled="isDeletingRow(row.id)"
              @click="handleCommand('delete', row)"
            >
              删除
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <div
      v-else
      v-loading="loading"
      class="sale-out-mobile-list"
      element-loading-background="rgba(248, 250, 252, 0.78)"
    >
      <template v-if="list.length">
        <article v-for="row in list" :key="row.id" class="sale-out-mobile-card">
          <div class="sale-out-mobile-card__head">
            <el-checkbox
              :model-value="selectedIdSet.has(row.id)"
              @change="(checked) => toggleSelection(row, checked)"
            />
            <div class="sale-out-mobile-card__identity">
              <div class="sale-out-mobile-card__no">{{ row.no || '-' }}</div>
              <div class="sale-out-mobile-card__meta">
                <span>出库 {{ formatDateValue(row.outTime) }}</span>
                <span>创建�?{{ row.creatorName || '-' }}</span>
              </div>
            </div>
            <div class="sale-out-mobile-card__status">
              <el-tag size="small" effect="light" :type="resolveErpAuditStatusTagType(row.status)">
                {{ resolveErpAuditStatusLabel(row.status) }}
              </el-tag>
              <el-tag size="small" effect="light" :type="getReceiptStatusTagType(row)">
                {{ getReceiptStatusLabel(row) }}
              </el-tag>
            </div>
          </div>

          <div class="sale-out-mobile-card__summary">
            <div class="sale-out-mobile-card__party">
              <div class="sale-out-mobile-card__customer">{{ row.customerName || '-' }}</div>
              <div class="sale-out-mobile-card__order">
                <span class="sale-out-mobile-card__order-tag">关联订单</span>
                <span>{{ row.orderNo || '-' }}</span>
              </div>
            </div>
            <div class="sale-out-mobile-card__finance">
              <span class="sale-out-mobile-card__finance-label">金额结算</span>
              <strong class="sale-out-mobile-card__finance-value">
                {{ formatCurrency(row.totalPrice) }}
              </strong>
              <span>已收 {{ formatCurrency(row.receiptPrice) }}</span>
              <span>未收 {{ formatCurrency(getRemainingReceipt(row)) }}</span>
            </div>
          </div>

          <div class="sale-out-mobile-card__product">{{ row.productNames || '-' }}</div>

          <div class="sale-out-mobile-card__metrics">
            <div class="sale-out-mobile-card__metric">
              <span class="sale-out-mobile-card__metric-label">出库数量</span>
              <strong>{{ formatCount(row.totalCount) }}</strong>
            </div>
            <div class="sale-out-mobile-card__metric sale-out-mobile-card__metric--receipt">
              <div class="sale-out-mobile-card__metric-top">
                <span>收款进度</span>
                <strong>{{ formatCurrency(row.receiptPrice) }} / {{ formatCurrency(row.totalPrice) }}</strong>
              </div>
              <el-progress
                :stroke-width="6"
                :show-text="false"
                :percentage="getReceiptPercent(row)"
                :color="resolveReceiptProgressColor(row)"
              />
              <div class="sale-out-mobile-card__metric-summary">
                <span>已收 {{ formatCurrency(row.receiptPrice) }}</span>
                <span>未收 {{ formatCurrency(getRemainingReceipt(row)) }}</span>
              </div>
            </div>
          </div>

          <div class="sale-out-mobile-card__actions">
            <el-button
              v-if="canViewDetail"
              link
              type="primary"
              @click="openForm('detail', row.id)"
            >
              详情
            </el-button>
            <el-button
              v-if="canUpdate && canEdit(row)"
              link
              type="primary"
              @click="handleCommand('edit', row)"
            >
              编辑
            </el-button>
            <el-button
              v-if="canUpdateStatus && canApprove(row)"
              link
              type="success"
              :disabled="isUpdatingStatus(row.id)"
              @click="handleCommand('approve', row)"
            >
              审批
            </el-button>
            <el-button
              v-if="canUpdateStatus && canReverseApprove(row)"
              link
              type="warning"
              :disabled="isUpdatingStatus(row.id)"
              @click="handleCommand('reverseApprove', row)"
            >
              反审�?
            </el-button>
            <el-button
              v-if="canRemove"
              link
              type="danger"
              :disabled="isDeletingRow(row.id)"
              @click="handleCommand('delete', row)"
            >
              删除
            </el-button>
          </div>
        </article>
      </template>
      <div v-else-if="listLoadFailed" class="sale-out-empty sale-out-empty--error">
        <div class="sale-out-empty__icon">
          <Icon icon="ep:warning-filled" />
        </div>
        <div class="sale-out-empty__title">列表加载失败</div>
        <el-button type="primary" plain @click="handleRetryList" :disabled="!canRetryList">
          重试加载
        </el-button>
      </div>
      <div v-else class="sale-out-empty">
        <div class="sale-out-empty__icon">
          <Icon icon="ep:box" />
        </div>
        <div class="sale-out-empty__title">暂无出库记录</div>
      </div>
    </div>

    <div class="sale-out-page__footer">
      <div class="sale-out-page__record-count">�?{{ total }} 条记�?/div>
      <Pagination
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </div>
  </ContentWrap>

  <SaleOutForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import { getIntDictOptions, DICT_TYPE } from '@/utils/dict'
import { formatDate } from '@/utils/formatTime'
import download from '@/utils/download'
import { SaleOutApi, SaleOutVO } from '@/api/erp/sale/out'
import SaleOutForm from './SaleOutForm.vue'
import { ProductApi, ProductVO } from '@/api/erp/product/product'
import { UserVO } from '@/api/system/user'
import * as UserApi from '@/api/system/user'
import { CustomerApi, CustomerVO } from '@/api/erp/sale/customer'
import { WarehouseApi, WarehouseVO } from '@/api/erp/stock/warehouse'
import { AccountApi, AccountVO } from '@/api/erp/finance/account'
import { resolveErpAuditStatusLabel, resolveErpAuditStatusTagType } from '@/utils/erpAuditStatus'
import { checkPermi } from '@/utils/permission'
import { useWindowSize } from '@vueuse/core'

interface SaleOutListRow extends SaleOutVO {
  customerName?: string
  creatorName?: string
  productNames?: string
  receiptPrice?: number
  orderNo?: string
}

defineOptions({ name: 'ErpSaleOut' })

const message = useMessage()
const { t } = useI18n()
const { width } = useWindowSize()

const loading = ref(true)
const listLoadFailed = ref(false)
const list = ref<SaleOutListRow[]>([])
const total = ref(0)
const advancedSearchVisible = ref(false)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  no: undefined,
  customerId: undefined,
  productId: undefined,
  warehouseId: undefined,
  outTime: [],
  orderNo: undefined,
  receiptStatus: undefined,
  accountId: undefined,
  status: undefined,
  remark: undefined,
  creator: undefined
})
const queryFormRef = ref()
const exportLoading = ref(false)
const deletingIds = ref<number[]>([])
const statusUpdatingIds = ref<number[]>([])
const productList = ref<ProductVO[]>([])
const customerList = ref<CustomerVO[]>([])
const userList = ref<UserVO[]>([])
const warehouseList = ref<WarehouseVO[]>([])
const accountList = ref<AccountVO[]>([])
const selectionList = ref<SaleOutListRow[]>([])
const formRef = ref()

const advancedFilterCount = computed(() => {
  const fields = [
    queryParams.productId,
    queryParams.warehouseId,
    queryParams.creator,
    queryParams.accountId,
    queryParams.receiptStatus,
    queryParams.status,
    queryParams.remark
  ]
  return fields.filter((item) => item !== undefined && item !== null && item !== '').length
})

const normalizeNumber = (value?: number | string | null) => Number(value || 0)

const formatDateValue = (value?: Date | string | number) => {
  return value ? formatDate(value as Date, 'YYYY-MM-DD') : '-'
}

const formatCount = (value?: number | string | null) => {
  const numberValue = normalizeNumber(value)
  if (Number.isInteger(numberValue)) {
    return numberValue.toLocaleString('zh-CN')
  }
  return numberValue.toFixed(3).replace(/\.?0+$/, '')
}

const formatCurrency = (value?: number | string | null) => {
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(normalizeNumber(value))
}

const getRemainingReceipt = (row: SaleOutListRow) =>
  Math.max(0, normalizeNumber(row.totalPrice) - normalizeNumber(row.receiptPrice))

const getReceiptPercent = (row: SaleOutListRow) => {
  const totalPrice = normalizeNumber(row.totalPrice)
  if (totalPrice <= 0) {
    return 0
  }
  return Math.min(100, Math.round((normalizeNumber(row.receiptPrice) / totalPrice) * 100))
}

const resolveReceiptProgressColor = (row: SaleOutListRow) => {
  const percent = getReceiptPercent(row)
  if (percent >= 100) {
    return '#10b981'
  }
  if (percent > 0) {
    return '#3b82f6'
  }
  return '#cbd5e1'
}

const getReceiptStatusLabel = (row: SaleOutListRow) => {
  const totalPrice = normalizeNumber(row.totalPrice)
  const receiptPrice = normalizeNumber(row.receiptPrice)
  if (receiptPrice <= 0 || totalPrice <= 0) {
    return '未收�?
  }
  if (receiptPrice >= totalPrice) {
    return '已收�?
  }
  return '部分收款'
}

const getReceiptStatusTagType = (row: SaleOutListRow): 'info' | 'warning' | 'success' => {
  const totalPrice = normalizeNumber(row.totalPrice)
  const receiptPrice = normalizeNumber(row.receiptPrice)
  if (receiptPrice <= 0 || totalPrice <= 0) {
    return 'info'
  }
  if (receiptPrice >= totalPrice) {
    return 'success'
  }
  return 'warning'
}

const canEdit = (row: SaleOutListRow) => row.status !== 20
const canApprove = (row: SaleOutListRow) => row.status === 10
const canReverseApprove = (row: SaleOutListRow) => row.status !== 10
const canDelete = () => true
const canViewDetail = checkPermi(['erp:sale-out:query'])
const canUpdate = checkPermi(['erp:sale-out:update'])
const canUpdateStatus = checkPermi(['erp:sale-out:update-status'])
const canRemove = checkPermi(['erp:sale-out:delete'])
const isCompactLayout = computed(() => width.value < 1180)
const canRetryList = computed(() => listLoadFailed.value && !loading.value)
const selectedIdSet = computed(() => new Set(selectionList.value.map((item) => item.id)))

const deletableSelectionIds = computed(() => selectionList.value.map((item) => item.id))

const toolbarState = computed(() => ({
  disableBatchDelete: deletableSelectionIds.value.length <= 0 || deletingIds.value.length > 0
}))

const toggleAdvancedSearch = () => {
  advancedSearchVisible.value = !advancedSearchVisible.value
}

const setIdsLoading = (source: Ref<number[]>, ids: number[], loadingState: boolean) => {
  if (loadingState) {
    source.value = Array.from(new Set([...source.value, ...ids]))
    return
  }
  source.value = source.value.filter((item) => !ids.includes(item))
}

const isDeletingRow = (id?: number) => !!id && deletingIds.value.includes(id)
const isUpdatingStatus = (id?: number) => !!id && statusUpdatingIds.value.includes(id)

const resolveSettledValue = <T,>(result: PromiseSettledResult<T>, fallback: T) =>
  result.status === 'fulfilled' ? result.value : fallback

const getList = async () => {
  loading.value = true
  listLoadFailed.value = false
  try {
    const data = await SaleOutApi.getSaleOutPage(queryParams)
    list.value = data.list
    total.value = data.total
    selectionList.value = []
  } catch {
    list.value = []
    total.value = 0
    selectionList.value = []
    listLoadFailed.value = true
  } finally {
    loading.value = false
  }
}

const loadFilterOptions = async () => {
  const results = await Promise.allSettled([
    ProductApi.getProductSimpleList(),
    CustomerApi.getCustomerSimpleList(),
    UserApi.getSimpleUserList(),
    WarehouseApi.getWarehouseSimpleList(),
    AccountApi.getAccountSimpleList()
  ])

  productList.value = resolveSettledValue(results[0], [])
  customerList.value = resolveSettledValue(results[1], [])
  userList.value = resolveSettledValue(results[2], [])
  warehouseList.value = resolveSettledValue(results[3], [])
  accountList.value = resolveSettledValue(results[4], [])
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryFormRef.value.resetFields()
  advancedSearchVisible.value = false
  handleQuery()
}

const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

const handleRetryList = () => {
  getList()
}

const handleDelete = async (ids: number[]) => {
  const executableIds = Array.from(new Set(ids.filter((id) => !deletingIds.value.includes(id))))
  if (!executableIds.length) {
    return
  }
  try {
    await message.delConfirm()
    setIdsLoading(deletingIds, executableIds, true)
    await SaleOutApi.deleteSaleOut(executableIds)
    message.success(t('common.delSuccess'))
    await getList()
    selectionList.value = selectionList.value.filter((item) => !executableIds.includes(item.id))
  } catch {
  } finally {
    setIdsLoading(deletingIds, executableIds, false)
  }
}

const handleUpdateStatus = async (id: number, status: number) => {
  if (statusUpdatingIds.value.includes(id)) {
    return
  }
  try {
    await message.confirm(`确定${status === 20 ? '审批' : '反审�?}该出库吗？`)
    setIdsLoading(statusUpdatingIds, [id], true)
    await SaleOutApi.updateSaleOutStatus(id, status)
    message.success(`${status === 20 ? '审批' : '反审�?}成功`)
    await getList()
  } catch {
  } finally {
    setIdsLoading(statusUpdatingIds, [id], false)
  }
}

const handleCommand = (command: string, row: SaleOutListRow) => {
  switch (command) {
    case 'edit':
      openForm('update', row.id)
      break
    case 'approve':
      handleUpdateStatus(row.id, 20)
      break
    case 'reverseApprove':
      handleUpdateStatus(row.id, 10)
      break
    case 'delete':
      handleDelete([row.id])
      break
  }
}

const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await SaleOutApi.exportSaleOut(queryParams)
    download.excel(data, '销售出�?xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

const handleSelectionChange = (rows: SaleOutListRow[]) => {
  selectionList.value = rows
}

const toggleSelection = (row: SaleOutListRow, checked: unknown) => {
  if (row.id === undefined) {
    return
  }
  if (checked === true) {
    if (!selectedIdSet.value.has(row.id)) {
      selectionList.value = [...selectionList.value, row]
    }
    return
  }
  selectionList.value = selectionList.value.filter((item) => item.id !== row.id)
}

onMounted(async () => {
  await Promise.allSettled([getList(), loadFilterOptions()])
})
</script>

<style scoped lang="scss">
.sale-out-page__filter-card,
.sale-out-page__list-card {
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(226, 232, 240, 0.92);
  border-radius: 22px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.99), rgba(248, 250, 252, 0.97)),
    linear-gradient(135deg, rgba(255, 255, 255, 0.9), rgba(241, 245, 249, 0.88));
  box-shadow: 0 16px 34px rgba(15, 23, 42, 0.06);
}

.sale-out-page__filter-card::before,
.sale-out-page__list-card::before {
  content: '';
  position: absolute;
  inset: 0 0 auto;
  height: 4px;
  background: linear-gradient(90deg, rgba(249, 115, 22, 0.9), rgba(59, 130, 246, 0.35));
}

.sale-out-page__filter-card {
  :deep(.el-card__body) {
    padding: 24px 24px 22px;
  }
}

.sale-out-page__list-card {
  :deep(.el-card__body) {
    padding: 18px 18px 16px;
  }
}

.sale-out-page__title {
  margin-bottom: 22px;
  color: #0f172a;
  font-size: 30px;
  font-weight: 800;
  line-height: 1.1;
  letter-spacing: 0.015em;
}

.sale-out-query {
  :deep(.el-form-item) {
    margin-bottom: 0;
  }

  :deep(.el-form-item__label) {
    padding-bottom: 8px;
    color: #475569;
    font-size: 13px;
    font-weight: 600;
    line-height: 20px;
  }

  :deep(.el-input__wrapper),
  :deep(.el-select__wrapper),
  :deep(.el-date-editor.el-input__wrapper) {
    min-height: 42px;
    border-radius: 12px;
    box-shadow: none;
    border: 1px solid rgba(203, 213, 225, 0.9);
    background: linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(248, 250, 252, 0.98));
  }

  :deep(.el-input__wrapper.is-focus),
  :deep(.el-select__wrapper.is-focused),
  :deep(.el-date-editor.el-input__wrapper.is-focus) {
    border-color: rgba(249, 115, 22, 0.48);
    box-shadow: 0 0 0 3px rgba(249, 115, 22, 0.08);
  }
}

.sale-out-query__grid {
  display: grid;
  gap: 18px 16px;
}

.sale-out-query__grid--primary {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.sale-out-query__grid--advanced {
  margin-top: 18px;
  padding-top: 18px;
  border-top: 1px solid rgba(226, 232, 240, 0.85);
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.sale-out-query__footer {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid rgba(226, 232, 240, 0.85);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.sale-out-query__actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.sale-out-query__actions :deep(.el-button) {
  min-width: 94px;
  min-height: 40px;
  padding: 0 16px;
  border-radius: 12px;
  font-weight: 600;
}

.sale-out-query__actions :deep(.el-button--primary) {
  box-shadow: 0 10px 20px rgba(249, 115, 22, 0.16);
}

.sale-out-query__filter-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 18px;
  margin-left: 6px;
  padding: 0 5px;
  border-radius: 999px;
  background: rgba(59, 130, 246, 0.12);
  color: #2563eb;
  font-size: 11px;
  font-weight: 700;
}

.sale-out-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(226, 232, 240, 0.82);
}

.sale-out-toolbar__actions,
.sale-out-toolbar__meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.sale-out-toolbar :deep(.el-button) {
  min-height: 40px;
  padding: 0 16px;
  border-radius: 12px;
  font-weight: 600;
}

.sale-out-toolbar__actions :deep(.el-button--primary) {
  box-shadow: 0 10px 20px rgba(249, 115, 22, 0.16);
}

.sale-out-toolbar__meta :deep(.el-button.is-disabled) {
  border-color: rgba(248, 113, 113, 0.18);
  background: rgba(254, 242, 242, 0.9);
  color: #f2a8a8;
}

.sale-out-ledger {
  :deep(.el-table__header-wrapper th) {
    background: linear-gradient(180deg, rgba(248, 250, 252, 0.98), rgba(255, 255, 255, 0.98));
    color: #64748b;
    font-size: 12px;
    font-weight: 700;
  }

  :deep(.el-table__cell) {
    padding-right: 7px;
    padding-left: 7px;
  }

  :deep(.cell) {
    overflow: visible;
  }

  :deep(.el-table__row td) {
    padding-top: 16px;
    padding-bottom: 16px;
    vertical-align: top;
  }

  :deep(.el-progress-bar__outer) {
    background: rgba(226, 232, 240, 0.92);
  }

  :deep(.el-table__empty-block) {
    min-height: 156px;
  }
}

.ledger-order,
.ledger-party,
.ledger-progress,
.ledger-status {
  display: flex;
  flex-direction: column;
}

.ledger-order {
  gap: 6px;
}

.ledger-order__no {
  color: #0f172a;
  font-size: 15px;
  font-weight: 800;
  line-height: 22px;
  word-break: break-all;
}

.ledger-order__meta,
.ledger-party__meta,
.ledger-finance__meta,
.ledger-finance__sub {
  color: #64748b;
  font-size: 11px;
  line-height: 17px;
}

.ledger-party {
  gap: 8px;
}

.ledger-party__customer {
  color: #0f172a;
  font-size: 14px;
  font-weight: 700;
  line-height: 20px;
}

.ledger-party__meta {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.ledger-party__tag {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 999px;
  background: rgba(226, 232, 240, 0.9);
  color: #475569;
  font-size: 11px;
  font-weight: 700;
}

.ledger-party__text {
  min-width: 0;
  display: -webkit-box;
  overflow: hidden;
  word-break: break-word;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.ledger-product {
  display: -webkit-box;
  color: #0f172a;
  font-size: 14px;
  font-weight: 600;
  line-height: 20px;
  overflow: hidden;
  word-break: break-word;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.ledger-progress {
  gap: 12px;
}

.ledger-progress__section {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.ledger-progress__top,
.ledger-progress__summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  color: #475569;
  font-size: 11px;
  line-height: 17px;
}

.ledger-progress__top strong {
  color: #0f172a;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
}

.ledger-finance {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
}

.ledger-finance__amount {
  color: #0f172a;
  font-size: 20px;
  font-weight: 800;
  line-height: 1;
  white-space: nowrap;
  font-variant-numeric: tabular-nums;
}

.ledger-finance__meta {
  color: #059669;
  font-weight: 600;
  text-align: right;
}

.ledger-finance__sub {
  text-align: right;
}

.ledger-status {
  gap: 8px;
}

.ledger-status__badges {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.ledger-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2px;
  flex-wrap: wrap;
}

.sale-out-empty {
  min-height: 132px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #64748b;
}

.sale-out-empty__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 16px;
  background: linear-gradient(180deg, rgba(249, 115, 22, 0.08), rgba(59, 130, 246, 0.08));
  color: #f97316;
  font-size: 22px;
}

.sale-out-empty__title {
  font-size: 14px;
  font-weight: 600;
  line-height: 22px;
}

.sale-out-empty--error .sale-out-empty__icon {
  background: linear-gradient(180deg, rgba(248, 113, 113, 0.12), rgba(251, 191, 36, 0.08));
  color: #ef4444;
}

.sale-out-mobile-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.sale-out-mobile-card {
  border: 1px solid rgba(226, 232, 240, 0.92);
  border-radius: 20px;
  padding: 16px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(248, 250, 252, 0.96)),
    linear-gradient(135deg, rgba(249, 115, 22, 0.03), rgba(59, 130, 246, 0.04));
  box-shadow: 0 14px 28px rgba(15, 23, 42, 0.05);
}

.sale-out-mobile-card__head,
.sale-out-mobile-card__meta,
.sale-out-mobile-card__order,
.sale-out-mobile-card__metric-top,
.sale-out-mobile-card__metric-summary,
.sale-out-mobile-card__actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.sale-out-mobile-card__head {
  align-items: flex-start;
}

.sale-out-mobile-card__identity,
.sale-out-mobile-card__party {
  min-width: 0;
  flex: 1;
}

.sale-out-mobile-card__no {
  color: #0f172a;
  font-size: 17px;
  font-weight: 800;
  line-height: 24px;
  word-break: break-all;
}

.sale-out-mobile-card__meta,
.sale-out-mobile-card__finance,
.sale-out-mobile-card__metric-label,
.sale-out-mobile-card__metric-summary {
  color: #64748b;
  font-size: 12px;
  line-height: 18px;
}

.sale-out-mobile-card__status {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  flex-wrap: wrap;
}

.sale-out-mobile-card__summary {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(0, 0.85fr);
  gap: 14px;
  margin-top: 14px;
}

.sale-out-mobile-card__customer,
.sale-out-mobile-card__product {
  color: #0f172a;
  font-size: 15px;
  font-weight: 700;
  line-height: 22px;
}

.sale-out-mobile-card__order {
  margin-top: 8px;
  color: #475569;
  font-size: 12px;
  line-height: 18px;
}

.sale-out-mobile-card__order-tag {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 999px;
  background: rgba(226, 232, 240, 0.9);
  color: #475569;
  font-size: 11px;
  font-weight: 700;
}

.sale-out-mobile-card__finance {
  align-items: flex-end;
  text-align: right;
}

.sale-out-mobile-card__finance-label {
  color: #64748b;
}

.sale-out-mobile-card__finance-value {
  color: #0f172a;
  font-size: 24px;
  line-height: 1.05;
  font-weight: 800;
  font-variant-numeric: tabular-nums;
  white-space: nowrap;
}

.sale-out-mobile-card__product {
  margin-top: 14px;
}

.sale-out-mobile-card__metrics {
  margin-top: 14px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.sale-out-mobile-card__metric {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px 14px;
  border-radius: 16px;
  background: rgba(248, 250, 252, 0.96);
  border: 1px solid rgba(226, 232, 240, 0.86);
}

.sale-out-mobile-card__metric strong {
  color: #0f172a;
  font-size: 20px;
  line-height: 1.1;
  font-weight: 800;
  font-variant-numeric: tabular-nums;
}

.sale-out-mobile-card__metric--receipt {
  grid-column: 1 / -1;
}

.sale-out-mobile-card__metric-top,
.sale-out-mobile-card__metric-summary {
  justify-content: space-between;
}

.sale-out-mobile-card__metric-top {
  color: #475569;
  font-size: 12px;
  line-height: 18px;
}

.sale-out-mobile-card__metric-top strong {
  font-size: 13px;
  line-height: 18px;
}

.sale-out-mobile-card__actions {
  justify-content: flex-end;
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid rgba(226, 232, 240, 0.86);
}

.sale-out-page__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 18px;
  padding-top: 14px;
  border-top: 1px solid rgba(226, 232, 240, 0.85);
}

.sale-out-page__record-count {
  color: #475569;
  font-size: 13px;
  line-height: 20px;
  font-weight: 600;
}

.sale-out-query-collapse-enter-active,
.sale-out-query-collapse-leave-active {
  transition: all 0.2s ease;
}

.sale-out-query-collapse-enter-from,
.sale-out-query-collapse-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

@media (max-width: 1359px) {
  .sale-out-page__filter-card {
    :deep(.el-card__body) {
      padding: 22px 20px 20px;
    }
  }

  .sale-out-page__list-card {
    :deep(.el-card__body) {
      padding: 16px 16px 14px;
    }
  }

  .sale-out-query__grid--primary,
  .sale-out-query__grid--advanced {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .sale-out-page__title {
    font-size: 24px;
  }

  .ledger-finance__amount {
    font-size: 18px;
  }
}

@media (max-width: 1179px) {
  .sale-out-page__list-card {
    :deep(.el-card__body) {
      padding: 16px 14px 14px;
    }
  }

  .sale-out-mobile-card__summary {
    grid-template-columns: minmax(0, 1fr);
  }

  .sale-out-mobile-card__finance {
    align-items: flex-start;
    text-align: left;
  }
}

@media (max-width: 1023px) {
  .sale-out-page__filter-card {
    :deep(.el-card__body) {
      padding: 18px 16px 18px;
    }
  }

  .sale-out-page__list-card {
    :deep(.el-card__body) {
      padding: 14px 12px 14px;
    }
  }

  .sale-out-query__grid--primary,
  .sale-out-query__grid--advanced {
    grid-template-columns: minmax(0, 1fr);
  }

  .sale-out-query__footer,
  .sale-out-toolbar,
  .sale-out-page__footer {
    flex-direction: column;
    align-items: stretch;
  }

  .sale-out-query__actions,
  .sale-out-toolbar__actions,
  .sale-out-toolbar__meta {
    width: 100%;
  }

  .sale-out-query__actions {
    gap: 12px;
  }

  .sale-out-query__actions :deep(.el-button),
  .sale-out-toolbar__actions :deep(.el-button),
  .sale-out-toolbar__meta :deep(.el-button) {
    flex: 1;
  }

  .sale-out-mobile-card {
    padding: 14px;
  }

  .sale-out-mobile-card__metrics {
    grid-template-columns: minmax(0, 1fr);
  }

  .sale-out-mobile-card__metric--receipt {
    grid-column: auto;
  }
}

@media (max-width: 767px) {
  .sale-out-mobile-card__head {
    gap: 12px;
  }

  .sale-out-mobile-card__status,
  .sale-out-mobile-card__actions {
    justify-content: flex-start;
  }

  .sale-out-mobile-card__finance-value {
    font-size: 22px;
  }
}
</style>
