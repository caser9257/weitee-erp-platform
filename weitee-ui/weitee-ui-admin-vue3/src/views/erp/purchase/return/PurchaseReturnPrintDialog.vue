<template>
  <PrintShell
    v-model="dialogVisible"
    title="采购退货打印"
    print-area-id="purchaseReturnPrintArea"
    :loading="loading"
    @closed="handleClosed"
  >
    <template #context>
      <template v-if="detail">
        <PrintContextCard
          :title="detail.no || `#${detail.id}`"
          :summary-items="summaryItems"
          :meta-items="metaItems"
          :status-label="detail.statusName || '已完成'"
          :status-type="getStatusTagType(detail.status)"
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
          <div class="finance-shell__section-title">单据信息</div>
        </div>
        <div class="grid gap-3 lg:grid-cols-[1fr_1fr]">
          <div class="rounded-2xl border border-slate-200 bg-slate-50 p-3 shadow-sm">
            <dl class="space-y-2 text-sm">
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">退货单号</dt>
                <dd class="text-right font-mono text-slate-800">{{ detail.no || '-' }}</dd>
              </div>
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">关联订单</dt>
                <dd class="text-right font-mono text-slate-800">{{ detail.orderNo || '-' }}</dd>
              </div>
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">供应商</dt>
                <dd class="text-right text-slate-800">{{ detail.supplierName || '-' }}</dd>
              </div>
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">结算账户</dt>
                <dd class="text-right text-slate-800">{{ financialFacts?.accountName || formatAccountFallback(detail.accountId) }}</dd>
              </div>
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">退货时间</dt>
                <dd class="text-right text-slate-800">{{ formatDateValue(detail.returnTime || detail.createTime) }}</dd>
              </div>
            </dl>
          </div>

          <div class="rounded-2xl border border-slate-200 bg-slate-50 p-3 shadow-sm">
            <dl class="space-y-2 text-sm">
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">合计数量</dt>
                <dd class="text-right font-mono text-slate-800">{{ formatNumber(detail.totalCount) }}</dd>
              </div>
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">合计产品价格</dt>
                <dd class="text-right font-mono text-slate-800">{{ formatMoney(detail.totalProductPrice) }}</dd>
              </div>
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">合计税额</dt>
                <dd class="text-right font-mono text-slate-800">{{ formatMoney(detail.totalTaxPrice) }}</dd>
              </div>
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">应退金额</dt>
                <dd class="text-right font-mono text-slate-800">{{ formatMoney(detail.totalPrice) }}</dd>
              </div>
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">已退金额</dt>
                <dd class="text-right font-mono text-slate-800">{{ formatMoney(detail.refundPrice) }}</dd>
              </div>
            </dl>
          </div>
        </div>

        <div class="mt-3 grid gap-3 lg:grid-cols-[1fr_1fr]">
          <div class="rounded-2xl border border-slate-200 bg-slate-50 p-3 shadow-sm">
            <dl class="space-y-2 text-sm">
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">优惠率</dt>
                <dd class="text-right font-mono text-slate-800">{{ formatPercent(financialFacts?.discountPercent) }}</dd>
              </div>
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">优惠金额</dt>
                <dd class="text-right font-mono text-slate-800">{{ formatMoney(financialFacts?.discountPrice) }}</dd>
              </div>
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">其它金额</dt>
                <dd class="text-right font-mono text-slate-800">{{ formatMoney(financialFacts?.otherPrice) }}</dd>
              </div>
            </dl>
          </div>

          <div class="rounded-2xl border border-slate-200 bg-slate-50 p-3 shadow-sm">
            <dl class="space-y-2 text-sm">
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">未退金额</dt>
                <dd class="text-right font-mono text-slate-800">{{ formatMoney(financialFacts?.remainingPrice) }}</dd>
              </div>
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">退款进度</dt>
                <dd class="text-right text-slate-800">{{ financialFacts?.refundProgressName || '-' }}</dd>
              </div>
            </dl>
          </div>
        </div>
      </div>

      <div class="finance-shell__section">
        <div class="finance-shell__section-head">
          <div class="finance-shell__section-title">退货明细</div>
        </div>
        <template v-if="detail.items?.length">
          <div class="finance-shell__item-list finance-shell__item-list--purchase-return">
            <article
              v-for="(row, index) in detail.items"
              :key="row.id ?? `${index}-${row.productId ?? 'item'}`"
              class="finance-shell__item-card"
            >
              <div class="finance-shell__item-head">
                <div class="finance-shell__item-head-main">
                  <div class="finance-shell__item-index">{{ index + 1 }}</div>
                  <div class="finance-shell__item-title-wrap">
                    <div class="finance-shell__item-title">{{ row.productName || '-' }}</div>
                    <div class="finance-shell__item-subtitle">
                      {{ row.productBarCode || '-' }} / {{ row.productUnitName || '-' }}
                    </div>
                  </div>
                </div>
                <div class="finance-shell__item-badge">
                  {{ row.warehouseName || row.warehouseId || '-' }}
                </div>
              </div>

              <div class="finance-shell__item-grid">
                <div class="finance-shell__item-metric">
                  <span>数量</span>
                  <strong>{{ formatNumber(row.count) }}</strong>
                </div>
                <div class="finance-shell__item-metric">
                  <span>单价</span>
                  <strong>{{ formatMoney(row.productPrice) }}</strong>
                </div>
                <div class="finance-shell__item-metric">
                  <span>金额小计</span>
                  <strong>{{ formatMoney(formatLineProductPrice(row)) }}</strong>
                </div>
                <div class="finance-shell__item-metric">
                  <span>税率</span>
                  <strong>{{ formatPercent(row.taxPercent) }}</strong>
                </div>
                <div class="finance-shell__item-metric">
                  <span>税额</span>
                  <strong>{{ formatMoney(row.taxPrice) }}</strong>
                </div>
                <div class="finance-shell__item-metric">
                  <span>含税金额</span>
                  <strong>{{ formatMoney(formatLineTotalPrice(row)) }}</strong>
                </div>
              </div>
              <div v-if="row.remark" class="finance-shell__item-remark">
                备注：{{ row.remark }}
              </div>
            </article>
          </div>
        </template>
        <el-empty v-else description="暂无退货明细" />
      </div>

      <div v-if="sourceAttachments.length" class="finance-shell__section">
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
import { erpPriceInputFormatter } from '@/utils'
import PrintContextCard from '@/views/erp/finance/shared/PrintContextCard.vue'
import PrintStatePanel from '@/views/erp/finance/shared/PrintStatePanel.vue'
import PrintAttachmentList from '@/views/erp/finance/shared/PrintAttachmentList.vue'
import PrintShell from '@/views/erp/finance/shared/print-shell.vue'
import { PurchaseReturnApi, PurchaseReturnPrintDataVO } from '@/api/erp/purchase/return'

defineOptions({ name: 'PurchaseReturnPrintDialog' })

interface AttachmentLink {
  name: string
  url: string
}

const dialogVisible = ref(false)
const loading = ref(false)
const loadErrorMessage = ref('')
const currentId = ref<number>()
const detail = ref<PurchaseReturnPrintDataVO['purchaseReturn']>()
const financialFacts = ref<PurchaseReturnPrintDataVO['financialFacts']>()
const sourceAttachments = ref<AttachmentLink[]>([])

const printObj = ref({
  id: 'purchaseReturnPrintArea',
  popTitle: '&nbsp',
  extraCss: '/print.css',
  extraHead: '',
  zIndex: 20003
})

const summaryItems = computed(() => [
  {
    label: '供应商',
    value: detail.value?.supplierName || '-'
  },
  { label: '应退金额', value: formatNumber(detail.value?.totalPrice) },
  { label: '已退金额', value: formatNumber(detail.value?.refundPrice) }
])

const metaItems = computed(() => [
  detail.value?.orderNo || '-',
  detail.value?.creatorName || detail.value?.creator || '-',
  formatDateValue(detail.value?.returnTime || detail.value?.createTime)
])

const isCompactAttachmentLayout = computed(() => sourceAttachments.value.length > 0 && sourceAttachments.value.length <= 2)

const formatDateValue = (value?: string | Date | number) => {
  if (!value) {
    return '-'
  }
  return formatDate(new Date(value), 'YYYY-MM-DD HH:mm:ss')
}

const formatNumber = (value?: number) => (value == null ? '-' : Number(value).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }))

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

const formatLineProductPrice = (row: NonNullable<PurchaseReturnPrintDataVO['purchaseReturn']>['items'][number]) => {
  const productPrice = Number(row?.productPrice)
  const count = Number(row?.count)
  if (Number.isNaN(productPrice) || Number.isNaN(count)) {
    return undefined
  }
  return productPrice * count
}

const formatLineTotalPrice = (row: NonNullable<PurchaseReturnPrintDataVO['purchaseReturn']>['items'][number]) => {
  const productPrice = formatLineProductPrice(row)
  const taxPrice = Number(row?.taxPrice)
  if ((productPrice == null || Number.isNaN(productPrice)) && Number.isNaN(taxPrice)) {
    return undefined
  }
  return (productPrice || 0) + (Number.isNaN(taxPrice) ? 0 : taxPrice)
}

const formatAccountFallback = (accountId?: number) => (accountId ? `结算账户ID #${accountId}` : '-')

const buildFinancialFacts = (purchaseReturn?: PurchaseReturnPrintDataVO['purchaseReturn']) => {
  if (!purchaseReturn) {
    return undefined
  }
  const totalPrice = Number(purchaseReturn.totalPrice ?? 0)
  const refundPrice = Number(purchaseReturn.refundPrice ?? 0)
  const remainingPrice = Math.max(totalPrice - refundPrice, 0)
  return {
    accountId: purchaseReturn.accountId,
    accountName: formatAccountFallback(purchaseReturn.accountId),
    totalCount: purchaseReturn.totalCount,
    totalProductPrice: purchaseReturn.totalProductPrice,
    totalTaxPrice: purchaseReturn.totalTaxPrice,
    discountPercent: purchaseReturn.discountPercent,
    discountPrice: purchaseReturn.discountPrice,
    otherPrice: purchaseReturn.otherPrice,
    totalPrice: purchaseReturn.totalPrice,
    refundPrice: purchaseReturn.refundPrice,
    remainingPrice,
    refundProgressName: totalPrice <= 0 ? '-' : refundPrice <= 0 ? '待退款' : refundPrice >= totalPrice ? '已退清' : '部分退款'
  }
}

const getStatusTagType = (status?: number) => {
  if (status === 20) return 'success'
  if (status === 30) return 'info'
  return 'warning'
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
  (list || []).reduce<AttachmentLink[]>((result, item) => {
    if (!item.url) {
      return result
    }
    const urls = item.url
      .split(',')
      .map((url) => url.trim())
      .filter(Boolean)
    if (urls.length <= 1) {
      result.push({
        url: item.url,
        name: item.name || resolveAttachmentName(item.url)
      })
      return result
    }
    urls.forEach((url) => {
      result.push({
        url,
        name: resolveAttachmentName(url)
      })
    })
    return result
  }, [])

const resetState = () => {
  detail.value = undefined
  financialFacts.value = undefined
  sourceAttachments.value = []
  loadErrorMessage.value = ''
}

const loadPrintData = async (id: number) => {
  currentId.value = id
  loading.value = true
  resetState()
  try {
    const data = await PurchaseReturnApi.getPurchaseReturnPrintData(id)
    if (currentId.value !== id || !dialogVisible.value) {
      return
    }
    detail.value = data.purchaseReturn
    financialFacts.value = data.financialFacts || buildFinancialFacts(data.purchaseReturn)
    sourceAttachments.value = buildAttachmentLinks(data.sourceAttachments)
    loadErrorMessage.value = ''
  } catch {
    detail.value = undefined
    financialFacts.value = undefined
    loadErrorMessage.value = '请检查网络或稍后重试。'
  } finally {
    if (currentId.value === id) {
      loading.value = false
    }
  }
}

const open = async (id: number) => {
  dialogVisible.value = true
  await loadPrintData(id)
}

const retry = async () => {
  if (!currentId.value) {
    return
  }
  await loadPrintData(currentId.value)
}

const handleClosed = () => {
  resetState()
  currentId.value = undefined
}

defineExpose({ open, retry })
</script>

<style scoped>
.finance-shell__item-list--purchase-return {
  min-width: 0;
}

.finance-shell__item-list--purchase-return {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.finance-shell__item-card {
  break-inside: avoid;
  page-break-inside: avoid;
  padding: 12px 14px;
  border: 1px solid #dbe4f0;
  border-radius: 16px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
  box-shadow: 0 1px 2px rgb(15 23 42 / 4%);
}

.finance-shell__item-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.finance-shell__item-head-main {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  min-width: 0;
}

.finance-shell__item-index {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: none;
  width: 24px;
  height: 24px;
  border: 1px solid #bfdbfe;
  border-radius: 999px;
  background: #eff6ff;
  color: #2563eb;
  font-size: 12px;
  font-weight: 700;
}

.finance-shell__item-title-wrap {
  min-width: 0;
}

.finance-shell__item-title {
  overflow: hidden;
  color: #0f172a;
  font-size: 14px;
  font-weight: 700;
  line-height: 1.45;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.finance-shell__item-subtitle {
  overflow: hidden;
  margin-top: 3px;
  color: #64748b;
  font-size: 12px;
  line-height: 1.4;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.finance-shell__item-badge {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  padding: 0 10px;
  border: 1px solid #dbe4f0;
  border-radius: 999px;
  background: #f8fafc;
  color: #334155;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}

.finance-shell__item-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
  margin-top: 12px;
}

.finance-shell__item-metric {
  min-width: 0;
  padding: 10px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  background: #ffffff;
}

.finance-shell__item-metric span {
  color: #64748b;
  font-size: 12px;
  line-height: 1.4;
}

.finance-shell__item-metric strong {
  display: block;
  margin-top: 4px;
  color: #0f172a;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 13px;
  font-weight: 700;
  line-height: 1.4;
  word-break: break-word;
}

.finance-shell__item-remark {
  margin-top: 10px;
  color: #475569;
  font-size: 12px;
  line-height: 1.6;
}

@media print {
  .finance-shell__item-list--purchase-return {
    gap: 8px !important;
  }

  .finance-shell__item-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr)) !important;
  }

  .finance-shell__section + .finance-shell__section {
    margin-top: 8px !important;
  }

  .print-attachment--compact .print-attachment__list {
    gap: 2px 10px !important;
  }

  .finance-shell__item-card {
    padding: 10px 12px !important;
  }

  .finance-shell__item-grid {
    gap: 6px !important;
    margin-top: 10px !important;
  }

  .finance-shell__item-metric {
    padding: 8px 10px !important;
  }

  .finance-shell__item-metric span {
    font-size: 11px !important;
  }

  .finance-shell__item-metric strong,
  .finance-shell__item-remark {
    font-size: 11px !important;
  }

  .finance-shell__item-title {
    white-space: normal !important;
  }

  .finance-shell__item-subtitle,
  .finance-shell__item-badge {
    white-space: normal !important;
  }
}
</style>
