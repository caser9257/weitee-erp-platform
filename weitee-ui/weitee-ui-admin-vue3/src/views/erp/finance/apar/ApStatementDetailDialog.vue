<template>
  <PrintShell
    v-model="dialogVisible"
    title="应付台账详情"
    print-area-id="apStatementPrintArea"
    :loading="loading"
    @closed="clearDialogState"
  >
    <template #context>
      <template v-if="detail">
        <PrintContextCard
          :title="detail.statementNo || `#${detail.id}`"
          :subtitle="detail.supplierName || '-'"
          :summary-items="[
            { label: '业务类型', value: getBizTypeLabel(detail.bizType) },
            { label: '应付金额', value: erpPriceInputFormatter(detail.amount) },
            { label: '剩余金额', value: erpPriceInputFormatter(detail.remainAmount) }
          ]"
          :meta-items="[
            getBizTypeLabel(detail.bizType),
            detail.bizNo || '-',
            detail.accountName || '-'
          ]"
          :status-label="getStatusLabel(detail.status)"
          :status-type="getStatusTagType(detail.status)"
        />
      </template>
      <template v-else-if="!loading">
        <PrintStatePanel
          title="台账数据加载失败"
          :description="detailErrorMessage || '请重试加载后再打印'"
          icon="ep:warning-filled"
          tone="danger"
        >
          <template #action>
            <el-button type="primary" plain :disabled="loading" @click="refresh()">重试加载</el-button>
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
          <el-descriptions-item label="台账编号">{{ detail.statementNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="业务类型">{{ getBizTypeLabel(detail.bizType) }}</el-descriptions-item>
          <el-descriptions-item label="业务单号">{{ detail.bizNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="来源采购单">{{ detail.sourceOrderNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="供应商">{{ detail.supplierName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="结算账户">{{ detail.accountName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="业务日期">{{ formatDateValue(detail.bizDate) }}</el-descriptions-item>
          <el-descriptions-item label="到期日期">{{ formatDateValue(detail.dueDate) }}</el-descriptions-item>
          <el-descriptions-item label="币种">{{ detail.currencyCode || 'CNY' }}</el-descriptions-item>
          <el-descriptions-item label="收票状态">{{ getInvoiceStatusLabel(detail.invoiceStatus) }}</el-descriptions-item>
          <el-descriptions-item label="发票号">{{ detail.invoiceNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="发票金额">
            {{ detail.invoiceAmount == null ? '-' : erpPriceInputFormatter(detail.invoiceAmount) }}
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="finance-shell__section">
        <div class="finance-shell__section-head">
          <div class="finance-shell__section-title">金额摘要</div>
        </div>
        <div class="finance-shell__metric-grid">
          <div class="finance-shell__metric-card">
            <div class="finance-shell__metric-label">应付金额</div>
            <div class="finance-shell__metric-value">{{ erpPriceInputFormatter(detail.amount) }}</div>
          </div>
          <div class="finance-shell__metric-card">
            <div class="finance-shell__metric-label">已核销金额</div>
            <div class="finance-shell__metric-value">{{ erpPriceInputFormatter(detail.paidAmount) }}</div>
          </div>
          <div class="finance-shell__metric-card">
            <div class="finance-shell__metric-label">剩余金额</div>
            <div class="finance-shell__metric-value finance-shell__metric-value--accent">
              {{ erpPriceInputFormatter(detail.remainAmount) }}
            </div>
          </div>
        </div>
      </div>

      <div class="finance-shell__section">
        <div class="finance-shell__section-head">
          <div class="finance-shell__section-title">来源附件</div>
        </div>
        <PrintAttachmentList
          :attachments="sourceAttachments"
          :loading="sourceAttachmentLoading"
          :error-message="sourceAttachmentError"
          @retry="retryLoadSourceAttachments"
        />
      </div>

      <div class="finance-shell__section">
        <div class="finance-shell__section-head">
          <div class="finance-shell__section-title">台账轨迹</div>
        </div>
        <div v-if="detail.items?.length" class="ap-statement-detail__timeline">
          <div v-for="item in detail.items" :key="item.id" class="ap-statement-detail__timeline-item">
            <div class="ap-statement-detail__timeline-dot"></div>
            <div class="ap-statement-detail__timeline-card">
              <div class="ap-statement-detail__timeline-head">
                <div class="ap-statement-detail__timeline-type">
                  {{ getItemTypeLabel(item.itemType) }}
                </div>
                <div class="ap-statement-detail__timeline-time">
                  {{ formatDateValue(item.createTime) }}
                </div>
              </div>
              <div class="ap-statement-detail__timeline-body">
                <div>来源单号：{{ item.refNo || '-' }}</div>
                <div>变动金额：{{ erpPriceInputFormatter(item.amount) }}</div>
                <div>变动后已核销：{{ erpPriceInputFormatter(item.afterPaidAmount) }}</div>
                <div>变动后剩余：{{ erpPriceInputFormatter(item.afterRemainAmount) }}</div>
                <div>备注：{{ formatItemRemark(item) }}</div>
              </div>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无台账轨迹" />
      </div>
    </template>

    <template #footer>
      <el-button type="primary" plain :disabled="loading || !detail" v-print="printObj">打印</el-button>
      <el-button @click="dialogVisible = false">关闭</el-button>
    </template>
  </PrintShell>
</template>

<script setup lang="ts">
import { ApStatementApi, ApStatementItemVO, ApStatementVO, ERP_AP_BIZ_TYPE_OPTIONS, ERP_AP_INVOICE_STATUS_OPTIONS, ERP_AP_STATEMENT_ITEM_TYPE_OPTIONS, ERP_AP_STATEMENT_STATUS_OPTIONS } from '@/api/erp/finance/apar'
import { PurchaseInApi } from '@/api/erp/purchase/in'
import { PurchaseReturnApi } from '@/api/erp/purchase/return'
import { erpPriceInputFormatter } from '@/utils'
import { formatDate } from '@/utils/formatTime'
import PrintContextCard from '@/views/erp/finance/shared/PrintContextCard.vue'
import PrintStatePanel from '@/views/erp/finance/shared/PrintStatePanel.vue'
import PrintAttachmentList from '@/views/erp/finance/shared/PrintAttachmentList.vue'
import PrintShell from '@/views/erp/finance/shared/print-shell.vue'

defineOptions({ name: 'ApStatementDetailDialog' })

interface AttachmentLink {
  name: string
  url: string
}

const dialogVisible = ref(false)
const loading = ref(false)
const detail = ref<ApStatementVO>()
const detailErrorMessage = ref('')
const currentId = ref<number>()
const sourceAttachmentLoading = ref(false)
const sourceAttachmentError = ref('')
const sourceAttachments = ref<AttachmentLink[]>([])

const printObj = ref({
  id: 'apStatementPrintArea',
  popTitle: '&nbsp',
  extraCss: '/print.css',
  extraHead: '',
  zIndex: 20003
})

const formatDateValue = (value?: string) => (value ? formatDate(new Date(value), 'YYYY-MM-DD') : '-')

const getLabel = (options: Array<{ label: string; value: number }>, value?: number) =>
  options.find((item) => item.value === value)?.label || '-'

const getBizTypeLabel = (value?: number) => getLabel(ERP_AP_BIZ_TYPE_OPTIONS, value)
const getStatusLabel = (value?: number) => getLabel(ERP_AP_STATEMENT_STATUS_OPTIONS, value)
const getInvoiceStatusLabel = (value?: number) => getLabel(ERP_AP_INVOICE_STATUS_OPTIONS, value)
const getItemTypeLabel = (value?: number) => getLabel(ERP_AP_STATEMENT_ITEM_TYPE_OPTIONS, value)

const getStatusTagType = (value?: number) =>
  (
    {
      10: 'warning',
      20: 'primary',
      30: 'success',
      40: 'info'
    } as Record<number, 'primary' | 'success' | 'warning' | 'info'>
  )[value || 0] || 'info'

const resetSourceAttachments = () => {
  sourceAttachmentLoading.value = false
  sourceAttachmentError.value = ''
  sourceAttachments.value = []
}

const clearDialogState = () => {
  currentId.value = undefined
  detail.value = undefined
  detailErrorMessage.value = ''
  resetSourceAttachments()
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

const buildAttachmentLinks = (fileUrl?: string | string[]) => {
  const urls = Array.isArray(fileUrl)
    ? fileUrl
    : String(fileUrl || '')
        .split(',')
        .map((item) => item.trim())
        .filter(Boolean)
  return urls.map((url) => ({
    url,
    name: resolveAttachmentName(url)
  }))
}

const formatItemRemark = (item: ApStatementItemVO) => {
  const remark = item.remark?.trim()
  if (!remark) {
    return '-'
  }
  if (item.itemType !== 50) {
    return remark
  }
  if (remark === 'clear invoice registration') {
    return '清空收票登记'
  }
  if (remark === 'update invoice status') {
    return '更新收票状态'
  }
  if (remark.startsWith('register invoice ')) {
    const invoiceNo = remark.slice('register invoice '.length).trim()
    return invoiceNo ? `登记发票：${invoiceNo}` : '登记发票'
  }
  return remark
}

const loadSourceAttachments = async (statement: ApStatementVO, requestId?: number) => {
  resetSourceAttachments()
  if (!statement.bizType || !statement.bizId) {
    return
  }
  sourceAttachmentLoading.value = true
  try {
    let sourceDetail: any
    if (statement.bizType === 11) {
      sourceDetail = await PurchaseInApi.getPurchaseIn(statement.bizId)
    } else if (statement.bizType === 12) {
      sourceDetail = await PurchaseReturnApi.getPurchaseReturn(statement.bizId)
    }
    if (requestId && (currentId.value !== requestId || !dialogVisible.value)) {
      return
    }
    sourceAttachments.value = buildAttachmentLinks(sourceDetail?.fileUrl)
  } catch {
    sourceAttachmentError.value = '附件加载失败'
  } finally {
    sourceAttachmentLoading.value = false
  }
}

const loadDetail = async (id: number) => {
  currentId.value = id
  loading.value = true
  resetSourceAttachments()
  try {
    const data = await ApStatementApi.getApStatement(id)
    if (currentId.value !== id || !dialogVisible.value) {
      return
    }
    detail.value = data
    detailErrorMessage.value = ''
    void loadSourceAttachments(data, id)
  } catch {
    detail.value = undefined
    detailErrorMessage.value = '请检查网络或稍后重试。'
  } finally {
    if (currentId.value === id) {
      loading.value = false
    }
  }
}

const retryLoadSourceAttachments = async () => {
  if (!detail.value) {
    return
  }
  await loadSourceAttachments(detail.value)
}

const open = async (id: number) => {
  dialogVisible.value = true
  await loadDetail(id)
}

const refresh = async (id?: number) => {
  if (!dialogVisible.value || !currentId.value) {
    return
  }
  if (id && currentId.value !== id) {
    return
  }
  await loadDetail(currentId.value)
}

defineExpose({ open, refresh })
</script>

<style scoped>
@import '../shared/readOnlyPage.css';

.ap-statement-detail__timeline {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding-left: 18px;
}

.ap-statement-detail__timeline::before {
  position: absolute;
  top: 8px;
  bottom: 8px;
  left: 6px;
  width: 1px;
  background: #dbe4f0;
  content: '';
}

.ap-statement-detail__timeline-item {
  position: relative;
  display: flex;
  gap: 12px;
}

.ap-statement-detail__timeline-dot {
  position: relative;
  z-index: 1;
  flex: none;
  width: 12px;
  height: 12px;
  margin-top: 6px;
  border: 2px solid #fff;
  border-radius: 999px;
  background: #3b82f6;
  box-shadow: 0 0 0 1px #bfdbfe;
}

.ap-statement-detail__timeline-card {
  flex: 1;
  min-width: 0;
  padding: 12px 14px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  background: #f8fafc;
}

.ap-statement-detail__timeline-head {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: 8px 12px;
}

.ap-statement-detail__timeline-type {
  color: #0f172a;
  font-weight: 600;
}

.ap-statement-detail__timeline-time {
  color: #64748b;
  font-size: 12px;
}

.ap-statement-detail__timeline-body {
  display: grid;
  gap: 6px;
  margin-top: 10px;
  color: #475569;
  font-size: 13px;
}

.finance-shell__metric-value--accent {
  color: #dc2626;
}

@media (max-width: 960px) {
  .ap-statement-detail__timeline-head {
    flex-direction: column;
  }
}
</style>
