<template>
  <PrintShell
    v-model="dialogVisible"
    title="成品出库打印"
    print-area-id="saleOutPrintArea"
    :loading="loading"
    @closed="handleClosed"
  >
    <template #context>
      <template v-if="detail">
        <PrintContextCard
          :title="detail.no || `#${detail.id}`"
          subtitle="成品出库单"
          :summary-items="summaryItems"
          :meta-items="metaItems"
          :status-label="getAuditStatusLabel(detail.status)"
          :status-type="getAuditStatusType(detail.status)"
        />
      </template>
      <template v-else-if="!loading">
        <PrintStatePanel
          title="打印数据加载失败"
          :description="loadErrorMessage || '请重试加载后再打印'"
          icon="ep:warning-filled"
          tone="danger"
        >
          <template #action>
            <el-button type="primary" plain :disabled="loading" @click="retry">重试加载</el-button>
          </template>
        </PrintStatePanel>
      </template>
    </template>

    <template v-if="detail">
      <div class="finance-shell__section">
        <div class="finance-shell__section-head">
          <div class="finance-shell__section-title">基础信息</div>
        </div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="出库单号">{{ detail.no || '-' }}</el-descriptions-item>
          <el-descriptions-item label="客户">
            {{ resolveCustomerName(detail.customerId, detail.customerName) }}
          </el-descriptions-item>
          <el-descriptions-item label="关联订单">{{ detail.orderNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="结算账户">{{ resolveAccountName(detail.accountId) }}</el-descriptions-item>
          <el-descriptions-item label="出库时间">{{ formatDateValue(detail.outTime) }}</el-descriptions-item>
          <el-descriptions-item label="创建人">{{ resolveCreatorName(detail.creator, detail.creatorName) }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="finance-shell__section">
        <div class="finance-shell__section-head">
          <div class="finance-shell__section-title">财务关联</div>
        </div>
        <div class="grid gap-4 xl:grid-cols-[1.2fr,0.8fr]">
          <div class="rounded-2xl border border-slate-200 bg-slate-50 p-4 shadow-sm">
            <dl class="space-y-3 text-sm">
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">收款状态</dt>
                <dd class="text-right text-slate-800">{{ getReceiptStatusLabel(detail) }}</dd>
              </div>
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">已收金额</dt>
                <dd class="text-right font-mono text-slate-800">{{ formatMoney(detail.receiptPrice) }}</dd>
              </div>
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">未收金额</dt>
                <dd class="text-right font-mono text-slate-800">{{ formatMoney(getRemainingReceipt(detail)) }}</dd>
              </div>
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">审核状态</dt>
                <dd class="text-right text-slate-800">{{ getAuditStatusLabel(detail.status) }}</dd>
              </div>
            </dl>
          </div>

          <div class="rounded-2xl border border-slate-200 bg-slate-50 p-4 shadow-sm">
            <dl class="space-y-3 text-sm">
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">产品金额</dt>
                <dd class="text-right font-mono text-slate-800">{{ formatMoney(detail.totalProductPrice) }}</dd>
              </div>
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">税额</dt>
                <dd class="text-right font-mono text-slate-800">{{ formatMoney(detail.totalTaxPrice) }}</dd>
              </div>
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">优惠金额</dt>
                <dd class="text-right font-mono text-slate-800">{{ formatMoney(detail.discountPrice) }}</dd>
              </div>
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">其它费用</dt>
                <dd class="text-right font-mono text-slate-800">{{ formatMoney(detail.otherPrice) }}</dd>
              </div>
            </dl>
          </div>
        </div>
      </div>

      <div class="finance-shell__section">
        <div class="finance-shell__section-head">
          <div class="finance-shell__section-title">出库明细</div>
        </div>
        <div class="finance-shell__table-wrap">
          <el-table
            v-if="detail.items?.length"
            :data="detail.items"
            border
            stripe
            class="finance-shell__table finance-shell__table--dense"
          >
            <el-table-column label="产品信息" min-width="220">
              <template #default="{ row }">
                <div class="finance-shell__primary-cell">
                  <span class="finance-shell__primary-text">{{ row.productName || '-' }}</span>
                  <span class="finance-shell__muted-text">
                    {{ row.productBarCode || '-' }} / {{ row.productUnitName || '-' }}
                  </span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="仓库" min-width="140">
              <template #default="{ row }">{{ resolveWarehouseName(row.warehouseId) }}</template>
            </el-table-column>
            <el-table-column label="数量" min-width="110" align="right">
              <template #default="{ row }">{{ formatCount(row.count) }}</template>
            </el-table-column>
            <el-table-column label="单价" min-width="120" align="right">
              <template #default="{ row }">{{ formatMoney(row.productPrice) }}</template>
            </el-table-column>
            <el-table-column label="税额" min-width="120" align="right">
              <template #default="{ row }">{{ formatMoney(row.taxPrice) }}</template>
            </el-table-column>
            <el-table-column label="备注" min-width="180">
              <template #default="{ row }">{{ row.remark || '-' }}</template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无出库明细" />
        </div>
      </div>

      <div class="finance-shell__section">
        <div class="finance-shell__section-head">
          <div class="finance-shell__section-title">金额汇总</div>
        </div>
        <el-descriptions :column="4" border>
          <el-descriptions-item label="合计数量">{{ formatCount(detail.totalCount) }}</el-descriptions-item>
          <el-descriptions-item label="出库金额">{{ formatMoney(detail.totalPrice) }}</el-descriptions-item>
          <el-descriptions-item label="已收金额">{{ formatMoney(detail.receiptPrice) }}</el-descriptions-item>
          <el-descriptions-item label="未收金额">{{ formatMoney(getRemainingReceipt(detail)) }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="finance-shell__section">
        <div v-if="!isCompactAttachmentLayout" class="finance-shell__section-head">
          <div class="finance-shell__section-title">来源附件</div>
        </div>
        <PrintAttachmentList :attachments="sourceAttachments" :compact="isCompactAttachmentLayout" />
      </div>
    </template>

    <template #footer>
      <el-button type="primary" plain :disabled="loading || !detail" v-print="printObj">打印</el-button>
      <el-button @click="dialogVisible = false">关闭</el-button>
    </template>
  </PrintShell>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { formatDate } from '@/utils/formatTime'
import { resolveErpAuditStatusLabel, resolveErpAuditStatusTagType } from '@/utils/erpAuditStatus'
import { SaleOutApi, type SaleOutVO } from '@/api/erp/sale/out'
import { CustomerApi, type CustomerVO } from '@/api/erp/sale/customer'
import { AccountApi, type AccountVO } from '@/api/erp/finance/account'
import { WarehouseApi, type WarehouseVO } from '@/api/erp/stock/warehouse'
import { getSimpleUserList, type SimpleUserVO } from '@/api/system/user'
import PrintAttachmentList from '@/views/erp/finance/shared/PrintAttachmentList.vue'
import PrintContextCard from '@/views/erp/finance/shared/PrintContextCard.vue'
import PrintStatePanel from '@/views/erp/finance/shared/PrintStatePanel.vue'
import PrintShell from '@/views/erp/finance/shared/print-shell.vue'

defineOptions({ name: 'SaleOutPrintDialog' })

interface AttachmentLink {
  name: string
  url: string
}

const dialogVisible = ref(false)
const loading = ref(false)
const loadErrorMessage = ref('')
const currentId = ref<number>()
const detail = ref<SaleOutVO>()
const sourceAttachments = ref<AttachmentLink[]>([])
const customerList = ref<CustomerVO[]>([])
const accountList = ref<AccountVO[]>([])
const warehouseList = ref<WarehouseVO[]>([])
const userList = ref<SimpleUserVO[]>([])

const isCompactAttachmentLayout = computed(
  () => sourceAttachments.value.length > 0 && sourceAttachments.value.length <= 2
)

const printObj = ref({
  id: 'saleOutPrintArea',
  popTitle: '&nbsp',
  extraCss: '/print.css',
  extraHead: '',
  zIndex: 20003
})

const summaryItems = computed(() => [
  { label: '客户', value: resolveCustomerName(detail.value?.customerId, detail.value?.customerName) },
  { label: '出库金额', value: formatMoney(detail.value?.totalPrice) },
  { label: '已收金额', value: formatMoney(detail.value?.receiptPrice) }
])

const metaItems = computed(() => [
  detail.value?.orderNo || '-',
  resolveCreatorName(detail.value?.creator, detail.value?.creatorName),
  formatDateValue(detail.value?.outTime || detail.value?.createTime)
])

const formatDateValue = (value?: string | Date | number) => {
  if (!value) {
    return '-'
  }
  return formatDate(new Date(value), 'YYYY-MM-DD HH:mm:ss')
}

const formatCount = (value?: number | string | null) => {
  if (value === undefined || value === null || value === '') {
    return '-'
  }
  const numberValue = Number(value)
  if (Number.isNaN(numberValue)) {
    return '-'
  }
  return numberValue.toLocaleString('zh-CN', {
    minimumFractionDigits: 0,
    maximumFractionDigits: 3
  })
}

const formatMoney = (value?: number | string | null) => {
  if (value === undefined || value === null || value === '') {
    return '-'
  }
  const numberValue = Number(value)
  if (Number.isNaN(numberValue)) {
    return '-'
  }
  return numberValue.toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

const getRemainingReceipt = (saleOut?: SaleOutVO) => {
  const total = Number(saleOut?.totalPrice || 0)
  const receipt = Number(saleOut?.receiptPrice || 0)
  return Math.max(0, total - receipt)
}

const getReceiptStatusLabel = (saleOut?: SaleOutVO) => {
  const total = Number(saleOut?.totalPrice || 0)
  const receipt = Number(saleOut?.receiptPrice || 0)
  if (receipt <= 0 || total <= 0) {
    return '未收款'
  }
  if (receipt >= total) {
    return '已收清'
  }
  return '部分收款'
}

const getAuditStatusLabel = (status?: number) => resolveErpAuditStatusLabel(status)
const getAuditStatusType = (status?: number) => resolveErpAuditStatusTagType(status)

const resolveCustomerName = (customerId?: number, customerName?: string) => {
  if (customerName) {
    return customerName
  }
  if (!customerId) {
    return '-'
  }
  return customerList.value.find((item) => item.id === customerId)?.name || `#${customerId}`
}

const resolveAccountName = (accountId?: number) => {
  if (!accountId) {
    return '-'
  }
  return accountList.value.find((item) => item.id === accountId)?.name || `#${accountId}`
}

const resolveWarehouseName = (warehouseId?: number) => {
  if (!warehouseId) {
    return '-'
  }
  return warehouseList.value.find((item) => item.id === warehouseId)?.name || `#${warehouseId}`
}

const resolveCreatorName = (creator?: string, creatorName?: string) => {
  if (creatorName) {
    return creatorName
  }
  const creatorId = Number(creator || 0)
  if (!creatorId) {
    return '-'
  }
  return userList.value.find((item) => item.id === creatorId)?.nickname || `#${creatorId}`
}

const resolveAttachmentName = (url: string) => {
  const cleanUrl = url.split('?')[0]?.split('#')[0] || ''
  const rawName = cleanUrl.split('/').filter(Boolean).pop()
  if (!rawName) {
    return '附件'
  }
  try {
    return decodeURIComponent(rawName)
  } catch {
    return rawName
  }
}

const buildAttachmentLinks = (list?: Array<{ name?: string; url?: string }>) =>
  (list || [])
    .filter((item) => item.url)
    .map((item) => ({
      url: item.url!,
      name: item.name || resolveAttachmentName(item.url!)
    }))

const resetState = () => {
  detail.value = undefined
  sourceAttachments.value = []
  loadErrorMessage.value = ''
}

const loadReferenceLists = async () => {
  if (!customerList.value.length) {
    void CustomerApi.getCustomerSimpleList().then((data) => (customerList.value = data)).catch(() => {})
  }
  if (!accountList.value.length) {
    void AccountApi.getAccountSimpleList().then((data) => (accountList.value = data)).catch(() => {})
  }
  if (!warehouseList.value.length) {
    void WarehouseApi.getWarehouseSimpleList().then((data) => (warehouseList.value = data)).catch(() => {})
  }
  if (!userList.value.length) {
    void getSimpleUserList().then((data) => (userList.value = data)).catch(() => {})
  }
}

const loadPrintData = async (id: number) => {
  if (loading.value) {
    return
  }
  currentId.value = id
  loading.value = true
  resetState()
  try {
    const data = await SaleOutApi.getSaleOutPrintData(id)
    if (currentId.value !== id || !dialogVisible.value) {
      return
    }
    if (!data?.saleOut) {
      loadErrorMessage.value = '未找到可打印的出库数据。'
      return
    }
    detail.value = data.saleOut
    sourceAttachments.value = buildAttachmentLinks(data.sourceAttachments)
    loadErrorMessage.value = ''
  } catch {
    if (currentId.value === id) {
      loadErrorMessage.value = '请检查网络或稍后重试。'
    }
  } finally {
    if (currentId.value === id) {
      loading.value = false
    }
  }
}

const open = async (id: number) => {
  if (loading.value) {
    return
  }
  dialogVisible.value = true
  loadReferenceLists()
  await loadPrintData(id)
}

const retry = async () => {
  if (!currentId.value || loading.value) {
    return
  }
  await loadPrintData(currentId.value)
}

const handleClosed = () => {
  resetState()
  currentId.value = undefined
  loading.value = false
}

defineExpose({ open, retry })
</script>
