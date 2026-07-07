<template>
  <div class="invoice-page">
    <ContentWrap class="invoice-page__header">
      <div class="page-header">
        <div class="page-header__main">
          <div class="page-header__title">销项发票管理</div>
        </div>
        <div class="page-header__actions">
          <el-button type="primary" @click="handleCreate">
            <Icon icon="ep:plus" class="mr-5px" />
            新开发票
          </el-button>
          <el-button :loading="loading" @click="loadData">
            <Icon icon="ep:refresh" class="mr-5px" />
            刷新
          </el-button>
        </div>
      </div>
    </ContentWrap>

    <ContentWrap class="invoice-page__filters">
      <el-form ref="queryFormRef" :model="queryParams" label-position="top" class="query-form">
        <div class="query-form__grid">
          <el-form-item label="发票号" prop="no">
            <el-input v-model="queryParams.no" clearable placeholder="请输入发票号" @keyup.enter="loadData" />
          </el-form-item>
          <el-form-item label="客户" prop="customerId">
            <el-select v-model="queryParams.customerId" clearable filterable placeholder="请选择客户">
              <el-option v-for="item in customerList" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="订单编号" prop="orderId">
            <el-input v-model="queryParams.orderId" clearable placeholder="请输入订单编号" @keyup.enter="loadData" />
          </el-form-item>
          <el-form-item label="发票状态" prop="status">
            <el-select v-model="queryParams.status" clearable placeholder="请选择状态">
              <el-option label="草稿" value="DRAFT" />
              <el-option label="已开票" value="ISSUED" />
              <el-option label="已作废" value="VOIDED" />
            </el-select>
          </el-form-item>
        </div>
        <div class="query-form__actions">
          <el-button type="primary" :loading="loading" @click="loadData">
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

    <ContentWrap class="invoice-page__table">
      <el-table :data="list" v-loading="loading" stripe class="invoice-table">
        <el-table-column label="发票信息" min-width="200">
          <template #default="{ row }">
            <div class="table-cell-main">
              <div class="table-cell-main__title">{{ row.no }}</div>
              <div class="table-cell-main__meta">{{ formatDate(row.invoiceTime, 'YYYY-MM-DD') }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="客户" min-width="150">
          <template #default="{ row }">
            <span>{{ getCustomerName(row.customerId) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="发票类型" min-width="100">
          <template #default="{ row }">
            <el-tag size="small" effect="light">{{ formatInvoiceType(row.invoiceType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="金额" min-width="150" align="right">
          <template #default="{ row }">
            <div class="amount-cell">
              <div class="amount-cell__main">{{ formatMoney(row.totalAmount) }}</div>
              <div class="amount-cell__meta">税额：{{ formatMoney(row.taxAmount) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small" effect="light">
              {{ formatStatus(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">查看</el-button>
            <el-button v-if="row.status === 'DRAFT'" link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 'DRAFT'" link type="success" @click="handleIssue(row)">开票</el-button>
            <el-button v-if="row.status === 'ISSUED'" link type="warning" @click="handleVoid(row)">作废</el-button>
            <el-button v-if="row.status === 'DRAFT'" link type="danger" @click="handleDelete(row)">删除</el-button>
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

    <el-drawer v-model="drawerVisible" :size="600" :with-header="false" class="invoice-drawer">
      <div class="drawer-context">
        <div class="drawer-context__title">{{ currentInvoice?.no || '-' }}</div>
        <div class="drawer-context__subtitle">{{ formatStatus(currentInvoice?.status) }}</div>
      </div>

      <div class="drawer-card" v-if="currentInvoice">
        <div class="drawer-card__title">发票信息</div>
        <div class="drawer-card__content">
          <div class="info-grid">
            <div class="info-item">
              <div class="info-item__label">客户</div>
              <div class="info-item__value">{{ getCustomerName(currentInvoice.customerId) }}</div>
            </div>
            <div class="info-item">
              <div class="info-item__label">发票类型</div>
              <div class="info-item__value">{{ formatInvoiceType(currentInvoice.invoiceType) }}</div>
            </div>
            <div class="info-item">
              <div class="info-item__label">发票抬头</div>
              <div class="info-item__value">{{ currentInvoice.invoiceTitle || '-' }}</div>
            </div>
            <div class="info-item">
              <div class="info-item__label">纳税人识别号</div>
              <div class="info-item__value">{{ currentInvoice.taxpayerNo || '-' }}</div>
            </div>
            <div class="info-item">
              <div class="info-item__label">开票时间</div>
              <div class="info-item__value">{{ formatDate(currentInvoice.invoiceTime, 'YYYY-MM-DD HH:mm') }}</div>
            </div>
          </div>
        </div>
      </div>

      <div class="drawer-card" v-if="currentInvoice">
        <div class="drawer-card__title">金额信息</div>
        <div class="drawer-card__content">
          <div class="amount-summary">
            <div class="amount-summary__item">
              <div class="amount-summary__label">不含税金额</div>
              <div class="amount-summary__value">{{ formatMoney(currentInvoice.amountWithoutTax) }}</div>
            </div>
            <div class="amount-summary__item">
              <div class="amount-summary__label">税额</div>
              <div class="amount-summary__value">{{ formatMoney(currentInvoice.taxAmount) }}</div>
            </div>
            <div class="amount-summary__item amount-summary__item--total">
              <div class="amount-summary__label">价税合计</div>
              <div class="amount-summary__value">{{ formatMoney(currentInvoice.totalAmount) }}</div>
            </div>
          </div>
        </div>
      </div>

      <div class="drawer-card" v-if="invoiceItems.length">
        <div class="drawer-card__title">发票明细</div>
        <div class="drawer-card__content">
          <el-table :data="invoiceItems" stripe size="small">
            <el-table-column label="产品名称" prop="productName" min-width="120" />
            <el-table-column label="规格" prop="productSpec" min-width="80" />
            <el-table-column label="单位" prop="unit" width="60" />
            <el-table-column label="数量" prop="count" width="80" align="right" />
            <el-table-column label="单价" min-width="100" align="right">
              <template #default="{ row }">{{ formatMoney(row.price) }}</template>
            </el-table-column>
            <el-table-column label="金额" min-width="100" align="right">
              <template #default="{ row }">{{ formatMoney(row.amount) }}</template>
            </el-table-column>
            <el-table-column label="税率" width="80" align="right">
              <template #default="{ row }">{{ row.taxRate }}%</template>
            </el-table-column>
            <el-table-column label="税额" min-width="100" align="right">
              <template #default="{ row }">{{ formatMoney(row.taxAmount) }}</template>
            </el-table-column>
            <el-table-column label="价税合计" min-width="100" align="right">
              <template #default="{ row }">{{ formatMoney(row.totalAmount) }}</template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <template #footer>
        <div class="drawer-footer">
          <el-button @click="drawerVisible = false">关闭</el-button>
        </div>
      </template>
    </el-drawer>

    <InvoiceForm ref="invoiceFormRef" @success="loadData" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDate } from '@/utils/formatTime'
import { erpPriceDisplayFormatter } from '@/utils'
import { CustomerApi } from '@/api/erp/sale/customer'
import InvoiceForm from './InvoiceForm.vue'

defineOptions({ name: 'ErpInvoicePage' })

const InvoiceApi = {
  getInvoicePage: async (params: any) => {
    const { request } = await import('@/config/axios')
    return await request.get({ url: '/erp/invoice/page', params })
  },
  getInvoiceItems: async (invoiceId: number) => {
    const { request } = await import('@/config/axios')
    return await request.get({ url: `/erp/invoice/list-items?invoiceId=${invoiceId}` })
  },
  updateInvoiceStatus: async (id: number, status: string) => {
    const { request } = await import('@/config/axios')
    return await request.put({ url: '/erp/invoice/update-status', params: { id, status } })
  },
  deleteInvoice: async (ids: number[]) => {
    const { request } = await import('@/config/axios')
    return await request.delete({ url: '/erp/invoice/delete', params: { ids: ids.join(',') } })
  }
}

const loading = ref(false)
const drawerVisible = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const currentInvoice = ref<any>(null)
const invoiceItems = ref<any[]>([])
const customerList = ref<any[]>([])

const queryParams = reactive({
  pageNo: 1,
  pageSize: 20,
  no: '',
  customerId: undefined as number | undefined,
  orderId: '',
  status: ''
})

const queryFormRef = ref()
const invoiceFormRef = ref()

const loadData = async () => {
  loading.value = true
  try {
    const res = await InvoiceApi.getInvoicePage(queryParams)
    list.value = res.list || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

const resetQuery = async () => {
  queryParams.no = ''
  queryParams.customerId = undefined
  queryParams.orderId = ''
  queryParams.status = ''
  queryParams.pageNo = 1
  await loadData()
}

const handleCreate = () => {
  invoiceFormRef.value?.open('create')
}

const handleView = async (row: any) => {
  currentInvoice.value = row
  invoiceItems.value = await InvoiceApi.getInvoiceItems(row.id)
  drawerVisible.value = true
}

const handleEdit = (row: any) => {
  invoiceFormRef.value?.open('update', row.id)
}

const handleIssue = async (row: any) => {
  await ElMessageBox.confirm('确认开票？', '提示', { type: 'warning' })
  await InvoiceApi.updateInvoiceStatus(row.id, 'ISSUED')
  ElMessage.success('开票成功')
  await loadData()
}

const handleVoid = async (row: any) => {
  await ElMessageBox.confirm('确认作废？', '提示', { type: 'warning' })
  await InvoiceApi.updateInvoiceStatus(row.id, 'VOIDED')
  ElMessage.success('已作废')
  await loadData()
}

const handleDelete = async (row: any) => {
  await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
  await InvoiceApi.deleteInvoice([row.id])
  ElMessage.success('删除成功')
  await loadData()
}

const getCustomerName = (customerId: number) => {
  const customer = customerList.value.find((item) => item.id === customerId)
  return customer?.name || '-'
}

const formatInvoiceType = (type?: string) => {
  const map: Record<string, string> = {
    NORMAL: '普通发票',
    SPECIAL: '增值税专用发票'
  }
  return map[type || ''] || type || '--'
}

const formatStatus = (status?: string) => {
  const map: Record<string, string> = {
    DRAFT: '草稿',
    ISSUED: '已开票',
    VOIDED: '已作废'
  }
  return map[status || ''] || status || '--'
}

const statusTagType = (status?: string) => {
  if (status === 'ISSUED') return 'success'
  if (status === 'VOIDED') return 'danger'
  return 'info'
}

const formatMoney = (value?: number | string | null) => {
  return `¥${erpPriceDisplayFormatter(value ?? 0)}`
}

onMounted(async () => {
  customerList.value = await CustomerApi.getCustomerSimpleList()
  await loadData()
})
</script>

<style scoped lang="scss">
.invoice-page {
  display: flex;
  min-height: 100vh;
  flex-direction: column;
  gap: 16px;
  padding: 16px;
  background: var(--erp-slate-50);
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.page-header__title {
  color: var(--erp-slate-900);
  font-size: 20px;
  font-weight: 800;
}

.page-header__actions {
  display: flex;
  gap: 12px;
}

.query-form__grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.query-form__actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 8px;
}

.invoice-table {
  :deep(.el-table__header-wrapper th) {
    background: var(--erp-slate-50);
    color: var(--erp-slate-600);
  }
}

.table-cell-main__title {
  font-weight: 700;
  color: var(--erp-slate-900);
}

.table-cell-main__meta,
.amount-cell__meta {
  color: var(--erp-slate-500);
  font-size: 12px;
}

.amount-cell {
  text-align: right;
}

.amount-cell__main {
  font-weight: 700;
  color: var(--erp-slate-900);
}

.table-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.invoice-drawer {
  :deep(.el-drawer__body) {
    padding: 0;
  }

  .drawer-context {
    padding: 16px;
    margin: -16px -16px 16px;
    background: linear-gradient(135deg, var(--erp-slate-900), var(--erp-slate-800));
    color: var(--erp-slate-50);
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

  .amount-summary {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .amount-summary__item {
    display: flex;
    justify-content: space-between;
    padding: 8px 12px;
    background: var(--erp-slate-50);
    border-radius: 8px;
  }

  .amount-summary__label {
    color: var(--erp-slate-500);
  }

  .amount-summary__value {
    font-weight: 700;
    color: var(--erp-slate-900);
  }

  .amount-summary__item--total {
    background: var(--erp-primary-50);
  }

  .amount-summary__item--total .amount-summary__value {
    color: var(--erp-primary-600);
    font-size: 18px;
  }

  .drawer-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
  }
}

@media (max-width: 1024px) {
  .query-form__grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>
