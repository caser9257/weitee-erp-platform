<template>
  <div class="market-ledger-page">
    <!-- 页头卡片 -->
    <ContentWrap class="market-ledger-page__header">
      <div class="page-header">
        <div class="page-header__main">
          <div class="page-header__title">市场执行台账</div>
        </div>
        <div class="page-header__actions">
          <el-button :loading="loading" @click="loadData" v-hasPermi="['erp:market-ledger:query']">
            <Icon icon="ep:refresh" class="mr-5px" /> 刷新
          </el-button>
          <el-button @click="handleExport" v-hasPermi="['erp:market-ledger:export']">
            <Icon icon="ep:download" class="mr-5px" /> 导出
          </el-button>
        </div>
      </div>
    </ContentWrap>
    <!-- 筛选卡片 -->
    <ContentWrap class="market-ledger-page__filters">
      <el-form ref="queryFormRef" :model="queryParams" label-position="top" class="query-form">
        <div class="query-form__grid query-form__grid--primary">
          <el-form-item label="项目编号" prop="projectNo">
            <el-input
              v-model="queryParams.projectNo"
              clearable
              placeholder="请输入项目编号"
              @keyup.enter="loadData"
            />
          </el-form-item>
          <el-form-item label="合同编号" prop="contractNo">
            <el-input
              v-model="queryParams.contractNo"
              clearable
              placeholder="请输入合同编号"
              @keyup.enter="loadData"
            />
          </el-form-item>
          <el-form-item label="客户名称" prop="customerName">
            <el-input
              v-model="queryParams.customerName"
              clearable
              placeholder="请输入客户名称"
              @keyup.enter="loadData"
            />
          </el-form-item>
          <el-form-item label="销售员" prop="saleUserId">
            <el-select
              v-model="queryParams.saleUserId"
              clearable
              filterable
              placeholder="请选择销售员"
            >
              <el-option
                v-for="item in userList"
                :key="item.id"
                :label="item.nickname"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </div>
        <transition name="query-collapse">
          <div v-if="advancedSearchVisible" class="query-form__grid query-form__grid--advanced">
            <el-form-item label="生命周期阶段" prop="lifecycleStage">
              <el-select v-model="queryParams.lifecycleStage" clearable placeholder="请选择阶段">
                <el-option label="合同" value="CONTRACT" />
                <el-option label="订单" value="ORDER" />
                <el-option label="收款" value="PAYMENT" />
                <el-option label="发货" value="SHIPMENT" />
                <el-option label="出库" value="OUTBOUND" />
                <el-option label="开票" value="INVOICE" />
                <el-option label="关闭" value="CLOSED" />
              </el-select>
            </el-form-item>
            <el-form-item label="放行状态" prop="releaseStatus">
              <el-select v-model="queryParams.releaseStatus" clearable placeholder="请选择放行状态">
                <el-option label="待校验" value="PENDING" />
                <el-option label="阻塞" value="BLOCKED" />
                <el-option label="待财务审核" value="FINANCE_REVIEW" />
                <el-option label="已放行" value="RELEASED" />
              </el-select>
            </el-form-item>
            <el-form-item label="开票状态" prop="invoiceStatus">
              <el-select v-model="queryParams.invoiceStatus" clearable placeholder="请选择开票状态">
                <el-option label="未开票" value="NOT_INVOICED" />
                <el-option label="部分开票" value="PARTIAL_INVOICED" />
                <el-option label="全额开票" value="FULLY_INVOICED" />
              </el-select>
            </el-form-item>
            <el-form-item label="验收状态" prop="acceptanceStatus">
              <el-select
                v-model="queryParams.acceptanceStatus"
                clearable
                placeholder="请选择验收状态"
              >
                <el-option label="无需验收" value="NOT_REQUIRED" />
                <el-option label="待验收" value="PENDING" />
                <el-option label="已验收" value="ACCEPTED" />
                <el-option label="验收不通过" value="REJECTED" />
              </el-select>
            </el-form-item>
            <el-form-item label="订单月份" prop="orderMonth">
              <el-date-picker
                v-model="queryParams.orderMonth"
                type="month"
                value-format="YYYY-MM"
                placeholder="请选择月份"
                class="!w-1/1"
              />
            </el-form-item>
            <el-form-item label="交期范围" prop="deliveryDateRange">
              <el-date-picker
                v-model="deliveryDateRange"
                type="daterange"
                value-format="YYYY-MM-DD"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                class="!w-1/1"
                @change="handleDeliveryDateChange"
              />
            </el-form-item>
          </div>
        </transition>
        <div class="query-form__actions">
          <el-button type="primary" :loading="loading" @click="loadData" v-hasPermi="['erp:market-ledger:query']">
            <Icon icon="ep:search" class="mr-5px" /> 查询
          </el-button>
          <el-button @click="resetQuery">
            <Icon icon="ep:refresh-left" class="mr-5px" /> 重置
          </el-button>
          <el-button link type="primary" @click="advancedSearchVisible = !advancedSearchVisible">
            {{ advancedSearchVisible ? '收起' : '高级筛选' }}
            <Icon :icon="advancedSearchVisible ? 'ep:arrow-up' : 'ep:arrow-down'" class="ml-5px" />
          </el-button>
        </div>
      </el-form>
    </ContentWrap>
    <!-- KPI 统计卡片 -->
    <ContentWrap class="market-ledger-page__stats">
      <div class="kpi-grid">
        <div class="kpi-card">
          <div class="kpi-card__label">总订单数</div>
          <div class="kpi-card__value">{{ stats.totalOrderCount || 0 }}</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-card__label">总金额</div>
          <div class="kpi-card__value">{{ formatMoney(stats.totalOrderAmount) }}</div>
        </div>
        <div class="kpi-card kpi-card--emerald">
          <div class="kpi-card__label">已收款金额</div>
          <div class="kpi-card__value">{{ formatMoney(stats.totalReceivedAmount) }}</div>
        </div>
        <div class="kpi-card kpi-card--amber">
          <div class="kpi-card__label">待放行订单</div>
          <div class="kpi-card__value">{{ stats.pendingReleaseCount || 0 }}</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-card__label">已出库订单</div>
          <div class="kpi-card__value">{{ stats.shippedOrderCount || 0 }}</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-card__label">待开票</div>
          <div class="kpi-card__value">{{ stats.pendingInvoiceCount || 0 }}</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-card__label">待验收</div>
          <div class="kpi-card__value">{{ stats.pendingAcceptanceCount || 0 }}</div>
        </div>
        <div class="kpi-card kpi-card--rose">
          <div class="kpi-card__label">异常订单</div>
          <div class="kpi-card__value">{{ stats.abnormalOrderCount || 0 }}</div>
        </div>
      </div>
    </ContentWrap>
    <!-- 台账列表 -->
    <ContentWrap class="market-ledger-page__table">
      <div class="table-toolbar">
        <div class="table-toolbar__title">台账列表</div>
        <div class="table-toolbar__meta">共 {{ total }} 条</div>
      </div>
      <el-table
        :data="list"
        v-loading="loading"
        stripe
        class="market-ledger-table"
        @row-click="openDrawer"
      >
        <el-table-column label="项目与合同" min-width="280">
          <template #default="{ row }">
            <div class="table-cell-main">
              <div class="table-cell-main__title">{{ row.projectNo || '-' }}</div>
              <div class="table-cell-main__name">{{ row.projectName || '-' }}</div>
              <div class="table-cell-main__meta">合同：{{ row.contractNo || '-' }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="订单与金额" min-width="200">
          <template #default="{ row }">
            <div class="table-cell-main">
              <div class="table-cell-main__title">{{ row.orderNo || '-' }}</div>
              <div class="table-cell-main__amount">{{ formatMoney(row.orderTotalPrice) }}</div>
              <div class="table-cell-main__meta">交期：{{ formatDate(row.deliveryDate) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="收款进度" min-width="180">
          <template #default="{ row }">
            <div class="progress-cell">
              <div class="progress-cell__bar">
                <div
                  class="progress-cell__fill progress-cell__fill--success"
                  :style="{ width: `${row.receiptProgress || 0}%` }"
                ></div>
              </div>
              <div class="progress-cell__text">
                <span>{{ row.receiptProgress || 0 }}%</span>
                <span class="muted">{{ formatMoney(row.receivedAmount) }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="发货状态" min-width="160">
          <template #default="{ row }">
            <div class="status-cell">
              <el-tag
                :type="releaseStatusType(row.shipmentReleaseStatus)"
                size="small"
                effect="light"
              >
                {{ formatReleaseStatus(row.shipmentReleaseStatus) }}
              </el-tag>
              <div class="status-cell__meta" v-if="row.shipmentReleaseReason">{{
                row.shipmentReleaseReason
              }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="开票" min-width="120">
          <template #default="{ row }">
            <el-tag :type="invoiceStatusType(row.invoiceStatus)" size="small" effect="light">
              {{ formatInvoiceStatus(row.invoiceStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click.stop="openDrawer(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="table-pagination">
        <el-pagination
          v-model:current-page="queryParams.pageNo"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </ContentWrap>
    <!-- 详情 Drawer -->
    <el-drawer
      v-model="drawerVisible"
      :size="480"
      :with-header="false"
      class="market-ledger-drawer"
    >
      <div class="drawer-context">
        <div class="drawer-context__title">{{ currentRow?.projectNo || '-' }}</div>
        <div class="drawer-context__subtitle">{{ currentRow?.projectName || '-' }}</div>
        <div class="drawer-context__meta">
          <el-tag
            :type="lifecycleStageType(currentRow?.lifecycleStage)"
            size="small"
            effect="light"
          >
            {{ formatLifecycleStage(currentRow?.lifecycleStage) }}
          </el-tag>
        </div>
      </div>
      <div class="drawer-card">
        <div class="drawer-card__title">基础信息</div>
        <div class="drawer-card__content">
          <div class="info-grid">
            <div class="info-item">
              <div class="info-item__label">客户</div>
              <div class="info-item__value">{{ currentRow?.customerName || '-' }}</div>
            </div>
            <div class="info-item">
              <div class="info-item__label">销售员</div>
              <div class="info-item__value">{{ currentRow?.saleUserName || '-' }}</div>
            </div>
            <div class="info-item">
              <div class="info-item__label">合同编号</div>
              <div class="info-item__value">{{ currentRow?.contractNo || '-' }}</div>
            </div>
            <div class="info-item">
              <div class="info-item__label">订单编号</div>
              <div class="info-item__value">{{ currentRow?.orderNo || '-' }}</div>
            </div>
          </div>
        </div>
      </div>
      <div class="drawer-card">
        <div class="drawer-card__title">合同条款</div>
        <div class="drawer-card__content">
          <div class="info-grid">
            <div class="info-item">
              <div class="info-item__label">发货放行</div>
              <div class="info-item__value">{{
                formatReleaseRule(currentRow?.shipmentReleaseRule)
              }}</div>
            </div>
            <div class="info-item">
              <div class="info-item__label">开票触发</div>
              <div class="info-item__value">{{
                formatInvoiceTrigger(currentRow?.invoiceTrigger)
              }}</div>
            </div>
            <div class="info-item">
              <div class="info-item__label">收款规则</div>
              <div class="info-item__value">{{
                formatCollectionRule(currentRow?.collectionRule)
              }}</div>
            </div>
          </div>
        </div>
      </div>
      <div class="drawer-card">
        <div class="drawer-card__title">执行进度</div>
        <div class="drawer-card__content">
          <div class="progress-list">
            <div class="progress-item">
              <div class="progress-item__label">收款进度</div>
              <div class="progress-item__bar">
                <div
                  class="progress-item__fill progress-item__fill--success"
                  :style="{ width: `${currentRow?.receiptProgress || 0}%` }"
                ></div>
              </div>
              <div class="progress-item__text">{{ currentRow?.receiptProgress || 0 }}%</div>
            </div>
            <div class="progress-item">
              <div class="progress-item__label">发货进度</div>
              <div class="progress-item__bar">
                <div
                  class="progress-item__fill progress-item__fill--primary"
                  :style="{ width: `${currentRow?.shipmentProgress || 0}%` }"
                ></div>
              </div>
              <div class="progress-item__text">{{ currentRow?.shipmentProgress || 0 }}%</div>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="drawer-footer">
          <el-button @click="drawerVisible = false">关闭</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>
<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import * as UserApi from '@/api/system/user'
import { formatDate } from '@/utils/formatTime'
import {
  MarketLedgerApi,
  type MarketLedgerStatsVO,
  type MarketLedgerVO
} from '@/api/erp/sale/market-ledger'

defineOptions({ name: 'ErpMarketLedgerPage' })

const loading = ref(false)
const drawerVisible = ref(false)
const advancedSearchVisible = ref(false)
const list = ref<MarketLedgerVO[]>([])
const total = ref(0)
const stats = ref<MarketLedgerStatsVO>({
  totalOrderCount: 0,
  totalOrderAmount: 0,
  totalReceivedAmount: 0,
  pendingReleaseCount: 0,
  shippedOrderCount: 0,
  pendingInvoiceCount: 0,
  pendingAcceptanceCount: 0,
  abnormalOrderCount: 0
})
const currentRow = ref<MarketLedgerVO | null>(null)
const userList = ref<UserApi.UserVO[]>([])
const queryFormRef = ref()
const deliveryDateRange = ref<[string, string] | null>(null)

const queryParams = reactive({
  pageNo: 1,
  pageSize: 20,
  projectNo: '',
  contractNo: '',
  customerName: '',
  saleUserId: undefined as number | undefined,
  lifecycleStage: '',
  releaseStatus: '',
  invoiceStatus: '',
  acceptanceStatus: '',
  orderMonth: '',
  deliveryDateStart: '',
  deliveryDateEnd: ''
})

const handleDeliveryDateChange = (val: [string, string] | null) => {
  if (val) {
    queryParams.deliveryDateStart = val[0]
    queryParams.deliveryDateEnd = val[1]
  } else {
    queryParams.deliveryDateStart = ''
    queryParams.deliveryDateEnd = ''
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const [pageRes, statsRes] = await Promise.all([
      MarketLedgerApi.getLedgerPage(queryParams),
      MarketLedgerApi.getLedgerStats()
    ])
    list.value = pageRes.list || []
    total.value = pageRes.total || 0
    stats.value = statsRes || stats.value
  } finally {
    loading.value = false
  }
}

const resetQuery = async () => {
  Object.assign(queryParams, {
    pageNo: 1,
    pageSize: 20,
    projectNo: '',
    contractNo: '',
    customerName: '',
    saleUserId: undefined,
    lifecycleStage: '',
    releaseStatus: '',
    invoiceStatus: '',
    acceptanceStatus: '',
    orderMonth: '',
    deliveryDateStart: '',
    deliveryDateEnd: ''
  })
  deliveryDateRange.value = null
  await loadData()
}

const openDrawer = (row: MarketLedgerVO) => {
  currentRow.value = row
  drawerVisible.value = true
}

const handleExport = () => {}

const formatMoney = (value?: number | string | null) => {
  const n = Number(value || 0)
  return `¥${n.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

const formatReleaseStatus = (status?: string) => {
  const map: Record<string, string> = {
    PENDING: '待校验',
    BLOCKED: '阻塞',
    FINANCE_REVIEW: '待财务审核',
    RELEASED: '已放行'
  }
  return map[status || ''] || status || '--'
}

const releaseStatusType = (status?: string) => {
  if (status === 'RELEASED') return 'success'
  if (status === 'FINANCE_REVIEW') return 'warning'
  if (status === 'BLOCKED') return 'danger'
  return 'info'
}

const formatInvoiceStatus = (status?: string) => {
  const map: Record<string, string> = {
    NOT_INVOICED: '未开票',
    PARTIAL_INVOICED: '部分开票',
    FULLY_INVOICED: '全额开票'
  }
  return map[status || ''] || status || '--'
}

const invoiceStatusType = (status?: string) => {
  if (status === 'FULLY_INVOICED') return 'success'
  if (status === 'PARTIAL_INVOICED') return 'warning'
  return 'info'
}

const formatLifecycleStage = (stage?: string) => {
  const map: Record<string, string> = {
    CONTRACT: '合同',
    ORDER: '订单',
    PAYMENT: '收款',
    SHIPMENT: '发货',
    OUTBOUND: '出库',
    INVOICE: '开票',
    CLOSED: '关闭',
    CONTRACT_REJECTED: '合同驳回',
    BLOCKED: '阻塞',
    RETURNED: '退货',
    DISPUTED: '争议'
  }
  return map[stage || ''] || stage || '--'
}

const lifecycleStageType = (stage?: string) => {
  if (stage === 'CLOSED') return 'success'
  if (stage === 'BLOCKED' || stage === 'CONTRACT_REJECTED' || stage === 'DISPUTED') return 'danger'
  if (stage === 'RETURNED') return 'warning'
  return 'primary'
}

const formatReleaseRule = (rule?: string) => {
  const map: Record<string, string> = {
    SIGN_AND_SHIP: '签约即发',
    AFTER_PAYMENT: '到账后发',
    AFTER_PREPAYMENT: '预付款后发',
    FINANCE_APPROVAL: '财务审核后发'
  }
  return map[rule || ''] || rule || '--'
}

const formatInvoiceTrigger = (trigger?: string) => {
  const map: Record<string, string> = {
    PREPAYMENT_FULL: '预付款全额开票',
    PREPAYMENT_PARTIAL: '预付款部分开票',
    PREPAYMENT_ONLY: '仅预付款开票',
    AFTER_SHIPMENT: '发货后开票',
    AFTER_DELIVERY_RECEIPT: '交付收款后开票',
    MANUAL: '手工决定'
  }
  return map[trigger || ''] || trigger || '--'
}

const formatCollectionRule = (rule?: string) => {
  const map: Record<string, string> = {
    BEFORE_SHIPMENT: '发货前付款',
    ON_SHIPMENT: '发货时付款',
    AFTER_SHIPMENT: '发货后约定期限付款'
  }
  return map[rule || ''] || rule || '--'
}

onMounted(async () => {
  userList.value = await UserApi.getSimpleUserList()
  await loadData()
})
</script>

<style scoped lang="scss">
.market-ledger-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px;
  background: var(--erp-slate-50);
  min-height: 100vh;
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .page-header__title {
    font-size: 20px;
    font-weight: 800;
    color: var(--erp-slate-900);
  }

  .page-header__actions {
    display: flex;
    gap: 12px;
  }

  .query-form__grid {
    display: grid;
    gap: 12px;
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }

  .query-form__actions {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    margin-top: 8px;
  }

  .kpi-grid {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 12px;
  }

  .kpi-card {
    padding: 16px;
    border: 1px solid var(--erp-slate-200);
    border-radius: 12px;
    background: #fff;
  }

  .kpi-card__label {
    color: var(--erp-slate-500);
    font-size: 12px;
  }

  .kpi-card__value {
    margin-top: 8px;
    font-size: 24px;
    font-weight: 800;
    color: var(--erp-slate-900);
  }

  .kpi-card--emerald .kpi-card__value {
    color: var(--erp-success-600);
  }
  .kpi-card--amber .kpi-card__value {
    color: var(--erp-warning-600);
  }
  .kpi-card--rose .kpi-card__value {
    color: var(--erp-danger-600);
  }

  .table-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
  }

  .table-toolbar__title {
    font-weight: 700;
    color: var(--erp-slate-900);
  }

  .table-toolbar__meta {
    color: var(--erp-slate-500);
    font-size: 12px;
  }

  .market-ledger-table {
    :deep(.el-table__header-wrapper th) {
      background: var(--erp-slate-50);
      color: var(--erp-slate-600);
    }
  }

  .table-cell-main__title {
    font-weight: 700;
    color: var(--erp-slate-900);
  }

  .table-cell-main__name {
    color: var(--erp-slate-700);
    font-size: 12px;
  }

  .table-cell-main__meta,
  .muted {
    color: var(--erp-slate-500);
    font-size: 12px;
  }

  .table-cell-main__amount {
    font-weight: 700;
    color: var(--erp-slate-900);
    font-variant-numeric: tabular-nums;
  }

  .progress-cell__bar {
    height: 6px;
    background: var(--erp-slate-200);
    border-radius: 3px;
    overflow: hidden;
  }

  .progress-cell__fill {
    height: 100%;
    border-radius: 3px;
    transition: width 0.3s ease;
  }

  .progress-cell__fill--success {
    background: var(--erp-success-500);
  }
  .progress-cell__fill--primary {
    background: var(--erp-primary-500);
  }

  .progress-cell__text {
    display: flex;
    justify-content: space-between;
    margin-top: 4px;
    font-size: 12px;
    color: var(--erp-slate-500);
  }

  .status-cell__meta {
    margin-top: 4px;
    color: var(--erp-slate-500);
    font-size: 12px;
  }

  .table-pagination {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }

  .market-ledger-drawer {
    .drawer-context {
      padding: 16px;
      margin: -16px -16px 16px;
      background: linear-gradient(135deg, var(--erp-slate-900), var(--erp-slate-800));
      color: #fff;
      border-radius: 0 0 16px 16px;
    }

    .drawer-context__title {
      font-size: 18px;
      font-weight: 800;
    }

    .drawer-context__subtitle {
      margin-top: 4px;
      color: var(--erp-slate-300);
    }

    .drawer-context__meta {
      margin-top: 8px;
    }

    .drawer-card {
      padding: 12px 0;
      border-bottom: 1px solid var(--erp-slate-200);
    }

    .drawer-card__title {
      margin-bottom: 12px;
      font-weight: 700;
      color: var(--erp-slate-900);
    }

    .info-grid {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 12px;
    }

    .info-item__label {
      color: var(--erp-slate-500);
      font-size: 12px;
    }

    .info-item__value {
      margin-top: 4px;
      font-weight: 600;
      color: var(--erp-slate-900);
    }

    .progress-list {
      display: flex;
      flex-direction: column;
      gap: 16px;
    }

    .progress-item__label {
      margin-bottom: 8px;
      font-size: 12px;
      color: var(--erp-slate-500);
    }

    .progress-item__bar {
      height: 8px;
      background: var(--erp-slate-200);
      border-radius: 4px;
      overflow: hidden;
    }

    .progress-item__fill {
      height: 100%;
      border-radius: 4px;
      transition: width 0.3s ease;
    }

    .progress-item__fill--success {
      background: var(--erp-success-500);
    }

    .progress-item__fill--primary {
      background: var(--erp-primary-500);
    }

    .progress-item__text {
      margin-top: 4px;
      font-size: 12px;
      color: var(--erp-slate-500);
      text-align: right;
    }

    .drawer-footer {
      display: flex;
      justify-content: flex-end;
      gap: 12px;
    }
  }
}
</style>
