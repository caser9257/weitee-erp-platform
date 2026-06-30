<template>
  <PrintShell
    v-model="dialogVisible"
    title="生产领料打印"
    print-area-id="productionIssuePrintArea"
    :loading="loading"
    @closed="handleClosed"
  >
    <template #context>
      <template v-if="detail">
        <div class="finance-shell__context-card">
          <div class="finance-shell__context-main">
            <div class="finance-shell__context-head">
              <div class="finance-shell__context-titles">
                <div class="finance-shell__context-title">{{ detail.issueNo || `#${detail.id}` }}</div>
                <div class="finance-shell__context-subtitle">生产领料单</div>
              </div>
              <el-tag :type="getStatusTagType(detail.status)" effect="light" round>
                {{ detail.statusName || '已完成' }}
              </el-tag>
            </div>
            <div class="finance-shell__context-summary">
              <div class="finance-shell__context-summary-item">
                <div class="finance-shell__context-summary-label">生产工单号</div>
                <div class="finance-shell__context-summary-value">{{ detail.productionOrderNo || '-' }}</div>
              </div>
              <div class="finance-shell__context-summary-item">
                <div class="finance-shell__context-summary-label">领料金额</div>
                <div class="finance-shell__context-summary-value">{{ formatMoney(detail.issueAmount) }}</div>
              </div>
              <div class="finance-shell__context-summary-item">
                <div class="finance-shell__context-summary-label">领料时间</div>
                <div class="finance-shell__context-summary-value">{{ formatDateValue(detail.issueTime || detail.createTime) }}</div>
              </div>
            </div>
            <div class="finance-shell__context-meta">
              <span class="finance-shell__context-meta-item">{{ detail.creatorName || '-' }}</span>
              <span class="finance-shell__context-meta-item">{{ financialFacts?.voucherStatusName || '-' }}</span>
            </div>
          </div>
        </div>
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
        <el-descriptions class="finance-shell__descriptions finance-shell__descriptions--compact" :column="4" border>
          <el-descriptions-item label="领料单号">{{ detail.issueNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="生产工单号">{{ detail.productionOrderNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="领料时间">{{ formatDateValue(detail.issueTime) }}</el-descriptions-item>
          <el-descriptions-item label="创建人">{{ detail.creatorName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ detail.statusName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="领料金额">
            {{ formatMoney(detail.issueAmount) }}
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="4">{{ detail.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="finance-shell__section">
        <div class="finance-shell__section-head">
          <div class="finance-shell__section-title">财务关联</div>
        </div>
        <div class="finance-shell__finance-grid grid gap-4 xl:grid-cols-[1.2fr,0.8fr]">
          <div class="finance-shell__finance-card rounded-2xl border border-slate-200 bg-slate-50 p-4 shadow-sm">
            <dl class="space-y-3 text-sm">
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">出库凭证号</dt>
                <dd class="text-right font-mono text-slate-800">{{ financialFacts?.voucherNo || '-' }}</dd>
              </div>
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">凭证状态</dt>
                <dd class="text-right text-slate-800">{{ financialFacts?.voucherStatusName || '-' }}</dd>
              </div>
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">成本快照</dt>
                <dd class="text-right text-slate-800">{{ formatDateValue(financialFacts?.costSnapshotTime) }}</dd>
              </div>
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">领料明细数</dt>
                <dd class="text-right font-mono text-slate-800">{{ financialFacts?.issueItemCount ?? 0 }}</dd>
              </div>
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">批次数</dt>
                <dd class="text-right font-mono text-slate-800">{{ financialFacts?.issueBatchCount ?? 0 }}</dd>
              </div>
            </dl>
          </div>

          <div class="finance-shell__finance-card rounded-2xl border border-slate-200 bg-slate-50 p-4 shadow-sm">
            <dl class="space-y-3 text-sm">
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">材料成本</dt>
                <dd class="text-right font-mono text-slate-800">{{ formatMoney(financialFacts?.materialCost) }}</dd>
              </div>
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">总成本</dt>
                <dd class="text-right font-mono text-slate-800">{{ formatMoney(financialFacts?.totalCost) }}</dd>
              </div>
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">单位成本</dt>
                <dd class="text-right font-mono text-slate-800">{{ formatMoney(financialFacts?.unitCost) }}</dd>
              </div>
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">成本归集领料单数</dt>
                <dd class="text-right font-mono text-slate-800">{{ financialFacts?.costIssueCount ?? 0 }}</dd>
              </div>
            </dl>
          </div>
        </div>
      </div>

      <div class="finance-shell__section">
        <div class="finance-shell__section-head">
          <div class="finance-shell__section-title">领料明细</div>
        </div>
        <div class="finance-shell__table-wrap">
          <el-table
            v-if="detail.items?.length"
            :data="detail.items"
            border
            stripe
            class="finance-shell__table finance-shell__table--dense"
          >
            <el-table-column label="物料信息" min-width="220">
              <template #default="{ row }">
                <div class="finance-shell__primary-cell">
                  <span class="finance-shell__primary-text">{{ row.materialName || '-' }}</span>
                  <span class="finance-shell__muted-text">
                    {{ row.materialCode || '-' }} / {{ row.materialBarCode || '-' }}
                  </span>
                  <span class="finance-shell__muted-text">{{ row.productUnitName || '-' }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="仓库" min-width="140">
              <template #default="{ row }">{{ row.warehouseName || '-' }}</template>
            </el-table-column>
            <el-table-column label="领料数量" min-width="110" align="right">
              <template #default="{ row }">{{ erpCountInputFormatter(row.issueQty || 0) }}</template>
            </el-table-column>
            <el-table-column label="领料金额" min-width="120" align="right">
              <template #default="{ row }">{{ formatMoney(row.issueAmount) }}</template>
            </el-table-column>
            <el-table-column label="备注" min-width="180">
              <template #default="{ row }">{{ row.remark || '-' }}</template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无领料明细" />
        </div>
      </div>

      <div class="finance-shell__section">
        <div class="finance-shell__section-head">
          <div class="finance-shell__section-title">批次明细</div>
        </div>
        <template v-if="batchGroups.length">
          <div class="finance-shell__batch-group-list">
            <article
              v-for="(item, index) in batchGroups"
              :key="item.id ?? `${index}-${item.materialId ?? 'material'}`"
              class="finance-shell__batch-group-card"
            >
              <div class="finance-shell__batch-group-head">
                <div class="finance-shell__batch-group-main">
                  <div class="finance-shell__batch-group-index">{{ index + 1 }}</div>
                  <div class="finance-shell__batch-group-title-wrap">
                    <div class="finance-shell__batch-group-title">{{ item.materialName || '-' }}</div>
                    <div class="finance-shell__batch-group-subtitle">
                      {{ item.materialCode || '-' }} / {{ item.materialBarCode || '-' }} / {{ item.productUnitName || '-' }}
                    </div>
                  </div>
                </div>
                <div class="finance-shell__batch-group-meta">
                  <span class="finance-shell__batch-group-pill">仓库：{{ item.warehouseName || '-' }}</span>
                  <span class="finance-shell__batch-group-pill">数量：{{ erpCountInputFormatter(item.issueQty || 0) }}</span>
                  <span class="finance-shell__batch-group-pill">金额：{{ formatMoney(item.issueAmount) }}</span>
                </div>
              </div>

              <div class="finance-shell__table-wrap finance-shell__table-wrap--inner">
                <el-table
                  v-if="item.batches?.length"
                  :data="item.batches"
                  border
                  stripe
                  class="finance-shell__table finance-shell__table--dense"
                >
                  <el-table-column label="批次号" min-width="160">
                    <template #default="{ row }">{{ row.batchNo || '-' }}</template>
                  </el-table-column>
                  <el-table-column label="入库时间" min-width="160">
                    <template #default="{ row }">{{ formatDateValue(row.inboundTime) }}</template>
                  </el-table-column>
                  <el-table-column label="生产日期" min-width="120">
                    <template #default="{ row }">{{ row.produceDate || '-' }}</template>
                  </el-table-column>
                  <el-table-column label="失效日期" min-width="120">
                    <template #default="{ row }">{{ row.expireDate || '-' }}</template>
                  </el-table-column>
                  <el-table-column label="领料数量" min-width="120" align="right">
                    <template #default="{ row }">{{ erpCountInputFormatter(row.issueQty || 0) }}</template>
                  </el-table-column>
                  <el-table-column label="备注" min-width="180">
                    <template #default="{ row }">{{ row.remark || '-' }}</template>
                  </el-table-column>
                </el-table>
                <el-empty v-else description="暂无批次明细" />
              </div>
            </article>
          </div>
        </template>
        <el-empty v-else description="暂无批次明细" />
      </div>

      <div class="finance-shell__section">
        <div v-if="!isCompactAttachmentLayout" class="finance-shell__section-head">
          <div class="finance-shell__section-title">来源附件</div>
        </div>
        <PrintAttachmentList
          :attachments="sourceAttachments"
          :compact="isCompactAttachmentLayout"
          :loading="sourceAttachmentLoading"
          :error-message="sourceAttachmentError"
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
import { erpCountInputFormatter, erpPriceInputFormatter } from '@/utils'
import { formatDate } from '@/utils/formatTime'
import PrintStatePanel from '@/views/erp/finance/shared/PrintStatePanel.vue'
import PrintShell from '@/views/erp/finance/shared/print-shell.vue'
import PrintAttachmentList from '@/views/erp/finance/shared/PrintAttachmentList.vue'
import { ProductionIssueApi, ProductionIssuePrintDataVO } from '@/api/erp/manufacturing/material-issue'

defineOptions({ name: 'IssuePrintDialog' })

interface AttachmentLink {
  name: string
  url: string
}

const dialogVisible = ref(false)
const loading = ref(false)
const loadErrorMessage = ref('')
const detail = ref<NonNullable<ProductionIssuePrintDataVO['productionIssue']>>()
const financialFacts = ref<ProductionIssuePrintDataVO['financialFacts']>()
const sourceAttachments = ref<AttachmentLink[]>([])
const sourceAttachmentLoading = ref(false)
const sourceAttachmentError = ref('')
const currentId = ref<number>()
const isCompactAttachmentLayout = computed(
  () => sourceAttachments.value.length > 0 && sourceAttachments.value.length <= 2
)
const batchGroups = computed(() => detail.value?.items || [])

const printObj = ref({
  id: 'productionIssuePrintArea',
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

const formatMoney = (value?: number) => (value == null ? '-' : erpPriceInputFormatter(value))

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
  (list || [])
    .filter((item) => item.url)
    .map((item) => ({
      url: item.url!,
      name: item.name || resolveAttachmentName(item.url!)
    }))

const resetPrintState = () => {
  detail.value = undefined
  financialFacts.value = undefined
  sourceAttachments.value = []
  sourceAttachmentLoading.value = false
  sourceAttachmentError.value = ''
  loadErrorMessage.value = ''
}

const loadPrintData = async (id: number) => {
  if (loading.value) {
    return
  }
  currentId.value = id
  loading.value = true
  resetPrintState()
  try {
    const data = await ProductionIssueApi.getPrintData(id)
    if (currentId.value !== id || !dialogVisible.value) {
      return
    }
    detail.value = data.productionIssue
    financialFacts.value = data.financialFacts
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
  if (loading.value) {
    return
  }
  dialogVisible.value = true
  await loadPrintData(id)
}

const retry = async () => {
  if (!currentId.value || loading.value) {
    return
  }
  await loadPrintData(currentId.value)
}

const handleClosed = () => {
  resetPrintState()
  currentId.value = undefined
}

defineExpose({ open, retry })
</script>

<style scoped>
.finance-shell__batch-group-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-width: 0;
}

.finance-shell__context-card {
  position: relative;
  overflow: hidden;
  border: 1px solid #dbe4f0;
  border-radius: 18px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
  box-shadow: 0 1px 2px rgb(15 23 42 / 4%);
  color: #0f172a;
}

.finance-shell__context-main {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 14px 16px;
}

.finance-shell__context-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
}

.finance-shell__context-titles {
  min-width: 0;
  flex: 1;
}

.finance-shell__context-title {
  overflow: hidden;
  color: #0f172a;
  font-size: 16px;
  font-weight: 800;
  line-height: 1.35;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.finance-shell__context-subtitle {
  margin-top: 4px;
  color: #64748b;
  font-size: 12px;
  line-height: 1.4;
}

.finance-shell__context-summary {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}

.finance-shell__context-summary-item {
  min-width: 0;
  padding: 8px 10px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  background: #ffffff;
}

.finance-shell__context-summary-label {
  color: #64748b;
  font-size: 11px;
  line-height: 1.4;
}

.finance-shell__context-summary-value {
  overflow: hidden;
  margin-top: 3px;
  color: #0f172a;
  font-size: 13px;
  font-weight: 700;
  line-height: 1.45;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.finance-shell__context-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.finance-shell__context-meta-item {
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
  line-height: 1.4;
}

.finance-shell__batch-group-card {
  break-inside: avoid;
  page-break-inside: avoid;
  padding: 12px 14px;
  border: 1px solid #dbe4f0;
  border-radius: 16px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
  box-shadow: 0 1px 2px rgb(15 23 42 / 4%);
}

.finance-shell__batch-group-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.finance-shell__batch-group-main {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  min-width: 0;
}

.finance-shell__batch-group-index {
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

.finance-shell__batch-group-title-wrap {
  min-width: 0;
}

.finance-shell__batch-group-title {
  overflow: hidden;
  color: #0f172a;
  font-size: 14px;
  font-weight: 700;
  line-height: 1.45;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.finance-shell__batch-group-subtitle {
  overflow: hidden;
  margin-top: 3px;
  color: #64748b;
  font-size: 12px;
  line-height: 1.4;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.finance-shell__batch-group-meta {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 6px;
}

.finance-shell__batch-group-pill {
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

.finance-shell__table-wrap--inner {
  margin-top: 10px;
}

@media print {
  :deep(.finance-shell__print-body) {
    gap: 8px !important;
    padding: 8px 8px 12px !important;
  }

  :deep(.finance-shell__print-body .finance-shell__section) {
    margin-top: 8px !important;
    padding: 8px 10px 10px !important;
  }

  :deep(.finance-shell__print-body .finance-shell__section-head) {
    margin-bottom: 6px !important;
  }

  .finance-shell__descriptions :deep(.el-descriptions__cell) {
    padding: 4px 8px !important;
  }

  .finance-shell__descriptions :deep(.el-descriptions__label) {
    width: 88px !important;
  }

  .finance-shell__descriptions :deep(.el-descriptions__content),
  .finance-shell__descriptions :deep(.el-descriptions__label) {
    font-size: 12px !important;
    line-height: 1.35 !important;
  }

  .finance-shell__descriptions :deep(.el-descriptions__table) {
    table-layout: fixed !important;
  }

  .finance-shell__finance-grid {
    gap: 8px !important;
  }

  .finance-shell__finance-card {
    padding: 10px !important;
  }

  .finance-shell__finance-card dl {
    gap: 6px !important;
  }

  .finance-shell__finance-card dt,
  .finance-shell__finance-card dd {
    font-size: 12px !important;
    line-height: 1.35 !important;
  }

  .finance-shell__context-card {
    box-shadow: 0 1px 2px rgb(15 23 42 / 4%) !important;
  }

  .finance-shell__context-main {
    gap: 7px !important;
    padding: 12px 14px !important;
  }

  .finance-shell__context-title {
    font-size: 14px !important;
    white-space: normal !important;
  }

  .finance-shell__context-subtitle {
    font-size: 11px !important;
  }

  .finance-shell__context-summary {
    gap: 6px !important;
  }

  .finance-shell__context-summary-item {
    padding: 6px 8px !important;
  }

  .finance-shell__context-summary-label {
    font-size: 10px !important;
  }

  .finance-shell__context-summary-value {
    font-size: 12px !important;
    white-space: normal !important;
  }

  .finance-shell__context-meta-item {
    background: #f8fafc !important;
    color: #334155 !important;
  }

  .finance-shell__screen-only {
    display: none !important;
  }

  .finance-shell__batch-group-list {
    gap: 8px !important;
  }

  .finance-shell__batch-group-card {
    padding: 10px 12px !important;
  }

  .finance-shell__batch-group-title,
  .finance-shell__batch-group-subtitle,
  .finance-shell__batch-group-pill {
    white-space: normal !important;
  }
}
</style>
