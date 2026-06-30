<template>
  <PrintShell
    v-model="dialogVisible"
    title="采购入库打印"
    print-area-id="purchaseInPrintArea"
    :loading="loading"
    @closed="handleClosed"
  >
    <template #context>
      <template v-if="detail">
        <PrintContextCard
          :title="detail.no || `#${detail.id}`"
          subtitle="采购入库单"
          :summary-items="[
            { label: '供应商', value: detail.supplierName || '-' },
            { label: '付款状态', value: formatStatus(detail.paymentPrice, detail.totalPrice) },
            { label: '应付金额', value: formatMoney(detail.paymentPrice) }
          ]"
          :meta-items="[
            detail.supplierName || '-',
            detail.orderNo || '-',
            formatDateValue(detail.inTime || detail.createTime)
          ]"
          :status-label="getStatusLabel(detail.status, detail.processInstanceId)"
          :status-type="getStatusTagType(detail.status, detail.processInstanceId)"
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
          <el-descriptions-item label="入库单号">{{ detail.no || '-' }}</el-descriptions-item>
          <el-descriptions-item label="供应商">{{ detail.supplierName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="关联订单">{{ detail.orderNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="入库时间">{{ formatDateValue(detail.inTime) }}</el-descriptions-item>
          <el-descriptions-item label="创建人">{{ detail.creatorName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateValue(detail.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="付款状态">{{ formatStatus(detail.paymentPrice, detail.totalPrice) }}</el-descriptions-item>
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
                <dt class="text-slate-500">结算账户</dt>
                <dd class="text-right text-slate-800">{{ resolveAccountName(detail.accountId) }}</dd>
              </div>
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">优惠率</dt>
                <dd class="text-right font-mono text-slate-800">{{ formatPercent(detail.discountPercent) }}</dd>
              </div>
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">付款优惠</dt>
                <dd class="text-right font-mono text-slate-800">{{ formatMoney(detail.discountPrice) }}</dd>
              </div>
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">其它费用</dt>
                <dd class="text-right font-mono text-slate-800">{{ formatMoney(detail.otherPrice) }}</dd>
              </div>
            </dl>
          </div>

          <div class="rounded-2xl border border-slate-200 bg-slate-50 p-4 shadow-sm">
            <dl class="space-y-3 text-sm">
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">应付金额</dt>
                <dd class="text-right font-mono text-slate-800">{{ formatMoney(detail.totalPrice) }}</dd>
              </div>
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">已付金额</dt>
                <dd class="text-right font-mono text-slate-800">{{ formatMoney(detail.paymentPrice) }}</dd>
              </div>
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">未付金额</dt>
                <dd class="text-right font-mono text-slate-800">{{ formatMoney(resolveRemainingPay(detail.totalPrice, detail.paymentPrice)) }}</dd>
              </div>
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">付款状态</dt>
                <dd class="text-right text-slate-800">{{ formatStatus(detail.paymentPrice, detail.totalPrice) }}</dd>
              </div>
            </dl>
          </div>
        </div>
      </div>

      <div class="finance-shell__section">
        <div class="finance-shell__section-head">
          <div class="finance-shell__section-title">入库明细</div>
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
                  <span class="finance-shell__muted-text">{{ row.purchaseSourceBatchNo || '-' }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="仓库" min-width="140">
              <template #default="{ row }">{{ resolveWarehouseName(row) }}</template>
            </el-table-column>
            <el-table-column label="数量" min-width="110" align="right">
              <template #default="{ row }">{{ formatNumber(row.count) }}</template>
            </el-table-column>
            <el-table-column label="质检合格" min-width="110" align="right">
              <template #default="{ row }">{{ formatNumber(row.qaPassCount) }}</template>
            </el-table-column>
            <el-table-column label="已入库" min-width="110" align="right">
              <template #default="{ row }">{{ formatNumber(row.stockInCount) }}</template>
            </el-table-column>
            <el-table-column label="金额" min-width="120" align="right">
              <template #default="{ row }">{{ formatMoney(row.totalPrice) }}</template>
            </el-table-column>
            <el-table-column label="备注" min-width="180">
              <template #default="{ row }">{{ row.remark || '-' }}</template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无入库明细" />
        </div>
      </div>

      <div class="finance-shell__section">
        <div class="finance-shell__section-head">
          <div class="finance-shell__section-title">金额汇总</div>
        </div>
        <el-descriptions :column="4" border>
          <el-descriptions-item label="合计数量">{{ formatNumber(detail.totalCount) }}</el-descriptions-item>
          <el-descriptions-item label="应付金额">{{ formatMoney(detail.paymentPrice) }}</el-descriptions-item>
          <el-descriptions-item label="总金额">{{ formatMoney(detail.totalPrice) }}</el-descriptions-item>
          <el-descriptions-item label="优惠后金额">{{ formatMoney(detail.totalPrice && detail.discountPrice ? detail.totalPrice - detail.discountPrice : detail.totalPrice) }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="finance-shell__section">
        <div v-if="!isCompactAttachmentLayout" class="finance-shell__section-head">
          <div class="finance-shell__section-title">来源附件</div>
        </div>
        <PrintAttachmentList
          :attachments="sourceAttachments"
          :compact="isCompactAttachmentLayout"
        />
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
import { erpPriceInputFormatter } from '@/utils'
import { resolveErpAuditStatusLabel, resolveErpAuditStatusTagType } from '@/utils/erpAuditStatus'
import PrintContextCard from '@/views/erp/finance/shared/PrintContextCard.vue'
import PrintStatePanel from '@/views/erp/finance/shared/PrintStatePanel.vue'
import PrintAttachmentList from '@/views/erp/finance/shared/PrintAttachmentList.vue'
import PrintShell from '@/views/erp/finance/shared/print-shell.vue'
import { PurchaseInApi, PurchaseInPrintDataVO, PurchaseInVO } from '@/api/erp/purchase/in'
import { AccountApi, AccountVO } from '@/api/erp/finance/account'
import { WarehouseApi, WarehouseVO } from '@/api/erp/stock/warehouse'

defineOptions({ name: 'PurchaseInPrintDialog' })

interface AttachmentLink {
  name: string
  url: string
}

const dialogVisible = ref(false)
const loading = ref(false)
const loadErrorMessage = ref('')
const currentId = ref<number>()
const detail = ref<PurchaseInVO>()
const sourceAttachments = ref<AttachmentLink[]>([])
const warehouseList = ref<WarehouseVO[]>([])
const accountList = ref<AccountVO[]>([])

const isCompactAttachmentLayout = computed(
  () => sourceAttachments.value.length > 0 && sourceAttachments.value.length <= 2
)

const printObj = ref({
  id: 'purchaseInPrintArea',
  popTitle: '&nbsp',
  extraCss: '/print.css',
  extraHead: '',
  zIndex: 20003
})

const formatDateValue = (value?: string | Date | number) => {
  if (!value) {
    return '-'
  }
  return formatDate(new Date(value), 'YYYY-MM-DD HH:mm:ss')
}

const formatNumber = (value?: number | string | null) => {
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
  return erpPriceInputFormatter(numberValue)
}

const formatPercent = (value?: number | string | null) => {
  if (value === undefined || value === null || value === '') {
    return '-'
  }
  const numberValue = Number(value)
  if (Number.isNaN(numberValue)) {
    return '-'
  }
  return `${numberValue.toLocaleString('zh-CN', {
    minimumFractionDigits: 0,
    maximumFractionDigits: 2
  })}%`
}

const formatStatus = (paymentPrice?: number, totalPrice?: number) => {
  if (paymentPrice === undefined || totalPrice === undefined) {
    return '-'
  }
  return Number(paymentPrice) >= Number(totalPrice) ? '全部付款' : '未付款'
}

const getStatusTagType = (status?: number, processInstanceId?: string) => {
  return resolveErpAuditStatusTagType(status, processInstanceId)
}

const getStatusLabel = (status?: number, processInstanceId?: string) =>
  resolveErpAuditStatusLabel(status, processInstanceId)

const resolveWarehouseName = (row: PurchaseInVO['items'][number]) => {
  if (!row.warehouseId) {
    return '-'
  }
  return warehouseList.value.find((item) => item.id === row.warehouseId)?.name || `#${row.warehouseId}`
}

const resolveAccountName = (accountId?: number | null) => {
  if (!accountId) {
    return '-'
  }
  return accountList.value.find((item) => item.id === accountId)?.name || `#${accountId}`
}

const resolveRemainingPay = (totalPrice?: number | string | null, paymentPrice?: number | string | null) => {
  const total = Number(totalPrice ?? 0)
  const paid = Number(paymentPrice ?? 0)
  const remaining = total - paid
  return remaining > 0 ? remaining : 0
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

const loadWarehouseList = async () => {
  if (warehouseList.value.length) {
    return
  }
  try {
    warehouseList.value = await WarehouseApi.getWarehouseSimpleList()
  } catch {
  }
}

const loadAccountList = async () => {
  if (accountList.value.length) {
    return
  }
  try {
    accountList.value = await AccountApi.getAccountSimpleList()
  } catch {
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
    const data: PurchaseInPrintDataVO = await PurchaseInApi.getPurchaseInPrintData(id)
    if (currentId.value !== id || !dialogVisible.value) {
      return
    }
    detail.value = data.purchaseIn
    sourceAttachments.value = buildAttachmentLinks(data.sourceAttachments)
    loadErrorMessage.value = ''
  } catch {
    if (currentId.value === id) {
      detail.value = undefined
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
  void loadWarehouseList()
  void loadAccountList()
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
  loading.value = false
  currentId.value = undefined
}

defineExpose({ open, retry })
</script>
