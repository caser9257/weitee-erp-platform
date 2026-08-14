<template>
  <div class="sales-ledger-page">
    <doc-alert title="【销售】销售订单、出库、退货" url="https://doc.iocoder.cn/erp/sale/" />

    <ContentWrap class="sales-ledger-page__hero">
      <div class="ledger-hero">
        <div class="ledger-hero__main">
          <p class="ledger-hero__eyebrow">Sales Return Ledger</p>
          <h1 class="ledger-hero__title">销售退货</h1>
          <p class="ledger-hero__desc">
            把退货单、客户、退款进度、审核状态和应退金额统一为单据台账视图，减少字段平铺带来的噪音。
          </p>
        </div>
        <div class="ledger-hero__stats">
          <div class="stat-card">
            <span class="stat-card__label">当前结果</span>
            <strong class="stat-card__value">{{ total }}</strong>
            <span class="stat-card__meta">本次筛选退货单数</span>
          </div>
          <div class="stat-card">
            <span class="stat-card__label">应退总额</span>
            <strong class="stat-card__value stat-card__value--mono">
              {{ formatMoney(returnStats.totalPrice) }}
            </strong>
            <span class="stat-card__meta">当前页累计应退金额</span>
          </div>
          <div class="stat-card">
            <span class="stat-card__label">已退金额</span>
            <strong class="stat-card__value stat-card__value--mono">
              {{ formatMoney(returnStats.refundPrice) }}
            </strong>
            <span class="stat-card__meta">当前页累计已退款</span>
          </div>
          <div class="stat-card">
            <span class="stat-card__label">待审核</span>
            <strong class="stat-card__value">{{ returnStats.pendingAuditCount }}</strong>
            <span class="stat-card__meta">状态为待审核的单据数</span>
          </div>
        </div>
      </div>
    </ContentWrap>

    <ContentWrap class="sales-ledger-page__search-card">
      <div class="search-card__header">
        <div>
          <div class="search-card__title">筛选条件</div>
          <div class="search-card__subtitle">首屏只保留高频条件，其余条件折叠到更多筛选中。</div>
        </div>
        <el-button text type="primary" @click="advancedExpanded = !advancedExpanded">
          {{ advancedExpanded ? '收起高级筛选' : '展开高级筛选' }}
        </el-button>
      </div>

      <el-form ref="queryFormRef" :model="queryParams" label-position="top" class="query-form">
        <div class="query-form__grid">
          <el-form-item label="退货单号" prop="no">
            <el-input
              v-model="queryParams.no"
              clearable
              placeholder="请输入退货单号"
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
          <el-form-item label="退货时间" prop="returnTime">
            <el-date-picker
              v-model="queryParams.returnTime"
              value-format="YYYY-MM-DD HH:mm:ss"
              type="daterange"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
              class="!w-full"
            />
          </el-form-item>
        </div>

        <div v-show="advancedExpanded" class="query-form__grid query-form__grid--advanced">
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
          <el-form-item label="创建人" prop="creator">
            <el-select
              v-model="queryParams.creator"
              clearable
              filterable
              placeholder="请选择创建人"
            >
              <el-option
                v-for="item in userList"
                :key="item.id"
                :label="item.nickname"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="关联订单" prop="orderNo">
            <el-input
              v-model="queryParams.orderNo"
              clearable
              placeholder="请输入关联订单号"
              @keyup.enter="handleQuery"
            />
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
          <el-form-item label="退款状态" prop="refundStatus">
            <el-select v-model="queryParams.refundStatus" clearable placeholder="请选择退款状态">
              <el-option label="未退款" value="0" />
              <el-option label="部分退款" value="1" />
              <el-option label="全部退款" value="2" />
            </el-select>
          </el-form-item>
          <el-form-item label="审核状态" prop="status">
            <el-select v-model="queryParams.status" clearable placeholder="请选择审核状态">
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
              clearable
              placeholder="请输入备注"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
        </div>

        <div class="query-form__actions">
          <el-button :loading="loading" type="primary" @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" />
            查询
          </el-button>
          <el-button @click="resetQuery">
            <Icon icon="ep:refresh" class="mr-5px" />
            重置
          </el-button>
        </div>
      </el-form>
    </ContentWrap>

    <ContentWrap class="sales-ledger-page__table-card">
      <div class="table-toolbar">
        <div class="table-toolbar__main">
          <div class="table-toolbar__title">退货台账</div>
          <div class="table-toolbar__meta"
            >已选择 {{ selectionList.length }} 条，当前共 {{ total }} 条</div
          >
        </div>
        <div class="table-toolbar__actions">
          <el-button
            type="primary"
            @click="openForm('create')"
            v-hasPermi="['erp:sale-return:create']"
          >
            <Icon icon="ep:plus" class="mr-5px" />
            新增退货
          </el-button>
          <el-button
            plain
            type="success"
            @click="handleExport"
            :loading="exportLoading"
            v-hasPermi="['erp:sale-return:export']"
          >
            <Icon icon="ep:download" class="mr-5px" />
            导出
          </el-button>
          <el-button
            plain
            type="danger"
            @click="handleDelete(selectionList.map((item) => item.id))"
            v-hasPermi="['erp:sale-return:delete']"
            :disabled="selectionList.length === 0"
          >
            <Icon icon="ep:delete" class="mr-5px" />
            批量删除
          </el-button>
        </div>
      </div>

      <div class="table-card__body">
        <el-table
          v-loading="loading"
          :data="list"
          stripe
          class="sales-ledger-table"
          :show-overflow-tooltip="false"
          @selection-change="handleSelectionChange"
        >
          <el-table-column width="42" label="选择" type="selection" />

          <el-table-column label="退货单信息" min-width="230" fixed="left">
            <template #default="{ row }">
              <div class="cell-stack">
                <div class="cell-stack__title-row">
                  <span class="cell-stack__mono">{{ row.no || '-' }}</span>
                  <span class="soft-pill" :class="auditPillClass(row.status)">
                    {{ auditStatusLabel(row.status) }}
                  </span>
                </div>
                <div class="cell-stack__meta">
                  <span>退货：{{ formatDateText(row.returnTime, 'YYYY-MM-DD') }}</span>
                  <span>创建人：{{ row.creatorName || '-' }}</span>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="客户与订单" min-width="220">
            <template #default="{ row }">
              <div class="cell-stack">
                <div class="cell-stack__title cell-stack__title--plain">{{
                  row.customerName || '-'
                }}</div>
                <div class="cell-stack__meta">
                  <span class="cell-stack__label">关联订单</span>
                  <span>{{ row.orderNo || '未关联订单' }}</span>
                </div>
                <div class="cell-stack__meta">
                  <span class="cell-stack__label">结算账户</span>
                  <span>{{ row.accountName || '-' }}</span>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="产品与仓库" min-width="220">
            <template #default="{ row }">
              <div class="cell-stack">
                <div
                  class="cell-stack__meta cell-stack__meta--truncate"
                  :title="row.productNames || '-'"
                >
                  {{ row.productNames || '未选择产品' }}
                </div>
                <div class="cell-stack__meta">
                  <span class="cell-stack__label">仓库</span>
                  <span>{{ row.warehouseName || '-' }}</span>
                </div>
                <div class="cell-stack__meta">
                  <span class="cell-stack__label">备注</span>
                  <span>{{ row.remark || '暂无备注' }}</span>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="数量" min-width="120" align="right">
            <template #default="{ row }">
              <div class="amount-stack">
                <div class="amount-stack__main">{{ formatCount(row.totalCount) }}</div>
                <div class="amount-stack__meta">退货数量</div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="退款进度" min-width="200" align="right">
            <template #default="{ row }">
              <div class="amount-stack">
                <div class="amount-stack__main">{{ formatMoney(row.totalPrice) }}</div>
                <div class="amount-stack__meta">已退 {{ formatMoney(row.refundPrice) }}</div>
                <div class="amount-stack__meta amount-stack__meta--warning">
                  未退 {{ formatMoney(unrefundPrice(row)) }}
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="退款状态" min-width="140" align="center">
            <template #default="{ row }">
              <span class="soft-pill" :class="refundPillClass(row)">
                {{ refundStatusLabel(row) }}
              </span>
            </template>
          </el-table-column>

          <el-table-column label="操作" align="center" fixed="right" width="240">
            <template #default="{ row }">
              <el-button
                link
                @click="openForm('detail', row.id)"
                v-hasPermi="['erp:sale-return:query']"
              >
                详情
              </el-button>
              <el-button
                link
                type="primary"
                @click="openForm('update', row.id)"
                v-hasPermi="['erp:sale-return:update']"
                :disabled="row.status === 20"
              >
                编辑
              </el-button>
              <el-button
                v-if="row.status === 10"
                link
                type="primary"
                @click="handleUpdateStatus(row.id, 20)"
                v-hasPermi="['erp:sale-return:update-status']"
              >
                审核
              </el-button>
              <el-button
                v-else-if="row.status === 20"
                link
                type="danger"
                @click="handleUpdateStatus(row.id, 10)"
                v-hasPermi="['erp:sale-return:update-status']"
              >
                反审核
              </el-button>
              <el-button
                link
                type="danger"
                @click="handleDelete([row.id])"
                v-hasPermi="['erp:sale-return:delete']"
              >
                删除
              </el-button>
            </template>
          </el-table-column>

          <template #empty>
            <el-empty description="暂无销售退货数据，试试调整筛选条件" />
          </template>
        </el-table>
      </div>

      <Pagination
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </ContentWrap>

    <SaleReturnForm ref="formRef" @success="getList" />
  </div>
</template>

<script setup lang="ts">
import { getIntDictOptions, DICT_TYPE } from '@/utils/dict'
import { formatDate } from '@/utils/formatTime'
import download from '@/utils/download'
import { SaleReturnApi, SaleReturnVO } from '@/api/erp/sale/return'
import SaleReturnForm from './SaleReturnForm.vue'
import { ProductApi, ProductVO } from '@/api/erp/product/product'
import { UserVO } from '@/api/system/user'
import * as UserApi from '@/api/system/user'
import { CustomerApi, CustomerVO } from '@/api/erp/sale/customer'
import { WarehouseApi, WarehouseVO } from '@/api/erp/stock/warehouse'
import { AccountApi, AccountVO } from '@/api/erp/finance/account'

defineOptions({ name: 'ErpSaleReturn' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const list = ref<SaleReturnVO[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  no: undefined,
  customerId: undefined,
  productId: undefined,
  warehouseId: undefined,
  returnTime: [],
  orderNo: undefined,
  accountId: undefined,
  status: undefined,
  remark: undefined,
  creator: undefined,
  refundStatus: undefined
})
const queryFormRef = ref()
const exportLoading = ref(false)
const advancedExpanded = ref(false)
const productList = ref<ProductVO[]>([])
const customerList = ref<CustomerVO[]>([])
const userList = ref<UserVO[]>([])
const warehouseList = ref<WarehouseVO[]>([])
const accountList = ref<AccountVO[]>([])

const returnStats = computed(() => {
  return list.value.reduce(
    (acc, item) => {
      acc.totalPrice += Number(item.totalPrice || 0)
      acc.refundPrice += Number(item.refundPrice || 0)
      if (Number(item.status) === 10) {
        acc.pendingAuditCount += 1
      }
      return acc
    },
    {
      totalPrice: 0,
      refundPrice: 0,
      pendingAuditCount: 0
    }
  )
})

const formatDateText = (value?: string, pattern = 'YYYY-MM-DD HH:mm') => {
  if (!value) {
    return '-'
  }
  return formatDate(new Date(value), pattern)
}

const formatMoney = (value?: number | string | null) => {
  const amount = Number(value || 0)
  return `¥${amount.toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })}`
}

const formatCount = (value?: number | string | null) => {
  const count = Number(value || 0)
  return count.toLocaleString('zh-CN', { maximumFractionDigits: 3 }).replace(/\.?0+$/, '')
}

const unrefundPrice = (row: SaleReturnVO) => {
  return Number(row.totalPrice || 0) - Number(row.refundPrice || 0)
}

const auditStatusLabel = (status?: number) => {
  switch (status) {
    case 10:
      return '待审核'
    case 20:
      return '已审核'
    default:
      return '未知'
  }
}

const auditPillClass = (status?: number) => {
  return Number(status) === 20 ? 'soft-pill--emerald' : 'soft-pill--amber'
}

const refundStatusLabel = (row: SaleReturnVO) => {
  if (Number(row.refundPrice || 0) === Number(row.totalPrice || 0)) {
    return '已全部退款'
  }
  if (Number(row.refundPrice || 0) > 0) {
    return '部分退款'
  }
  return '未退款'
}

const refundPillClass = (row: SaleReturnVO) => {
  if (Number(row.refundPrice || 0) === Number(row.totalPrice || 0)) {
    return 'soft-pill--emerald'
  }
  if (Number(row.refundPrice || 0) > 0) {
    return 'soft-pill--blue'
  }
  return 'soft-pill--rose'
}

const getList = async () => {
  loading.value = true
  try {
    const data = await SaleReturnApi.getSaleReturnPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryFormRef.value?.resetFields()
  handleQuery()
}

const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

const handleDelete = async (ids: number[]) => {
  try {
    await message.delConfirm()
    await SaleReturnApi.deleteSaleReturn(ids)
    message.success(t('common.delSuccess'))
    await getList()
    selectionList.value = selectionList.value.filter((item) => !ids.includes(item.id))
  } catch {}
}

const handleUpdateStatus = async (id: number, status: number) => {
  try {
    await message.confirm(`确定${status === 20 ? '审核' : '反审核'}该退货单吗？`)
    await SaleReturnApi.updateSaleReturnStatus(id, status)
    message.success(`${status === 20 ? '审核' : '反审核'}成功`)
    await getList()
  } catch {}
}

const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await SaleReturnApi.exportSaleReturn(queryParams)
    download.excel(data, '销售退货.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

const selectionList = ref<SaleReturnVO[]>([])
const handleSelectionChange = (rows: SaleReturnVO[]) => {
  selectionList.value = rows
}

onMounted(async () => {
  await getList()
  productList.value = await ProductApi.getProductSimpleList()
  customerList.value = await CustomerApi.getCustomerSimpleList()
  userList.value = await UserApi.getSimpleUserList()
  warehouseList.value = await WarehouseApi.getWarehouseSimpleList()
  accountList.value = await AccountApi.getAccountSimpleList()
})
</script>

<style scoped lang="scss">
.sales-ledger-page {
  display: flex;
  min-height: 100%;
  flex-direction: column;
  gap: 16px;
  padding: 4px 4px 20px;
  background: #f5f7fa;
}

.sales-ledger-page__hero,
.sales-ledger-page__search-card,
.sales-ledger-page__table-card {
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 8px 24px rgb(15 23 42 / 0.04);
}

.ledger-hero {
  display: flex;
  gap: 20px;
  align-items: stretch;
  justify-content: space-between;
}

.ledger-hero__main {
  min-width: 0;
  flex: 1;
}

.ledger-hero__eyebrow {
  margin: 0 0 10px;
  color: #64748b;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.ledger-hero__title {
  margin: 0;
  color: #0f172a;
  font-size: 28px;
  font-weight: 800;
}

.ledger-hero__desc {
  max-width: 720px;
  margin: 12px 0 0;
  color: #475569;
  font-size: 14px;
  line-height: 1.7;
}

.ledger-hero__stats {
  display: grid;
  width: min(540px, 100%);
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.stat-card {
  display: flex;
  min-height: 116px;
  flex-direction: column;
  justify-content: space-between;
  padding: 16px 18px;
  border: 1px solid #dbeafe;
  border-radius: 14px;
  background: linear-gradient(180deg, #f8fbff 0%, #f1f5f9 100%);
}

.stat-card__label {
  color: #64748b;
  font-size: 13px;
  font-weight: 600;
}

.stat-card__value {
  color: #0f172a;
  font-size: 28px;
  font-weight: 800;
  line-height: 1.1;
}

.stat-card__value--mono {
  font-size: 22px;
  font-family: 'DIN Alternate', 'Roboto Mono', 'Courier New', monospace;
}

.stat-card__meta {
  color: #94a3b8;
  font-size: 12px;
}

.search-card__header,
.table-toolbar {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  justify-content: space-between;
}

.search-card__title,
.table-toolbar__title {
  color: #0f172a;
  font-size: 16px;
  font-weight: 700;
}

.search-card__subtitle,
.table-toolbar__meta {
  margin-top: 4px;
  color: #64748b;
  font-size: 13px;
  line-height: 1.6;
}

.query-form {
  margin-top: 16px;
}

.query-form__grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px 16px;
}

.query-form__grid--advanced {
  margin-top: 14px;
}

.query-form__actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 6px;
}

.table-toolbar {
  margin-bottom: 16px;
}

.table-toolbar__actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
}

.table-card__body {
  overflow-x: auto;
}

.sales-ledger-table {
  min-width: 1180px;
}

.cell-stack {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 6px;
}

.cell-stack__title-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.cell-stack__mono {
  color: #0f172a;
  font-family: 'DIN Alternate', 'Roboto Mono', 'Courier New', monospace;
  font-size: 14px;
  font-weight: 700;
}

.cell-stack__title {
  color: #0f172a;
  font-size: 14px;
  font-weight: 700;
}

.cell-stack__title--plain {
  color: #0f172a;
}

.cell-stack__meta {
  display: flex;
  min-width: 0;
  flex-wrap: wrap;
  gap: 6px 10px;
  color: #64748b;
  font-size: 12px;
  line-height: 1.5;
}

.cell-stack__meta--truncate {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cell-stack__label {
  color: #94a3b8;
}

.amount-stack {
  text-align: right;
}

.amount-stack__main {
  color: #0f172a;
  font-family: 'DIN Alternate', 'Roboto Mono', 'Courier New', monospace;
  font-size: 18px;
  font-weight: 800;
}

.amount-stack__meta {
  margin-top: 4px;
  color: #64748b;
  font-size: 12px;
}

.amount-stack__meta--warning {
  color: #b45309;
}

.soft-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 4px 10px;
  border: 1px solid transparent;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.soft-pill--amber {
  border-color: #fde68a;
  background: #fffbeb;
  color: #b45309;
}

.soft-pill--emerald {
  border-color: #bbf7d0;
  background: #ecfdf5;
  color: #047857;
}

.soft-pill--blue {
  border-color: #bfdbfe;
  background: #eff6ff;
  color: #1d4ed8;
}

.soft-pill--rose {
  border-color: #fecdd3;
  background: #fff1f2;
  color: #be123c;
}

:deep(.el-form-item) {
  margin-bottom: 0;
}

:deep(.el-form-item__label) {
  padding-bottom: 8px;
  color: #475569;
  font-weight: 600;
}

:deep(.el-input__wrapper),
:deep(.el-select__wrapper),
:deep(.el-date-editor.el-input__wrapper) {
  min-height: 40px;
  border-radius: 12px;
  box-shadow: 0 0 0 1px #dbe3ef inset;
}

:deep(.el-table) {
  --el-table-header-bg-color: #f8fafc;
  --el-table-border-color: #e2e8f0;
  --el-table-row-hover-bg-color: #f8fbff;
}

:deep(.el-table th.el-table__cell) {
  color: #64748b;
  font-weight: 700;
}

:deep(.el-table td.el-table__cell) {
  padding-top: 14px;
  padding-bottom: 14px;
  vertical-align: top;
}

@media (max-width: 1280px) {
  .ledger-hero {
    flex-direction: column;
  }

  .ledger-hero__stats {
    width: 100%;
  }

  .query-form__grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .sales-ledger-page {
    padding: 0 0 18px;
  }

  .ledger-hero__stats,
  .query-form__grid {
    grid-template-columns: repeat(1, minmax(0, 1fr));
  }

  .search-card__header,
  .table-toolbar,
  .table-toolbar__actions {
    flex-direction: column;
    align-items: stretch;
  }

  .query-form__actions {
    justify-content: stretch;
  }

  .query-form__actions :deep(.el-button),
  .table-toolbar__actions :deep(.el-button) {
    width: 100%;
    margin-left: 0;
  }
}
</style>
