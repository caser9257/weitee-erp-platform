<template>
  <PrintShell
    v-model="dialogVisible"
    title="送检单打印"
    print-area-id="purchaseInQualityPrintArea"
    :loading="loading"
    @closed="handleClosed"
  >
    <template #context>
      <template v-if="detail">
        <div class="purchase-in-quality-print__context-card">
          <div class="purchase-in-quality-print__context-head">
            <div class="purchase-in-quality-print__context-titles">
              <div class="purchase-in-quality-print__context-title">{{ detail.no || `#${detail.id}` }}</div>
              <div class="purchase-in-quality-print__context-subtitle">送检单</div>
            </div>
            <div class="purchase-in-quality-print__context-tags">
              <el-tag :type="getStatusTagType(detail.status)" effect="light" round>
                {{ getStatusLabel(detail.status) }}
              </el-tag>
              <el-tag :type="getResultTagType(detail.result)" effect="light" round>
                {{ getResultLabel(detail.result) }}
              </el-tag>
            </div>
          </div>
          <div class="purchase-in-quality-print__context-summary">
            <div class="purchase-in-quality-print__summary-item">
              <div class="purchase-in-quality-print__summary-label">采购入库单</div>
              <div class="purchase-in-quality-print__summary-value">{{ detail.purchaseInNo || '-' }}</div>
            </div>
            <div class="purchase-in-quality-print__summary-item">
              <div class="purchase-in-quality-print__summary-label">供应商</div>
              <div class="purchase-in-quality-print__summary-value">{{ detail.supplierName || '-' }}</div>
            </div>
            <div class="purchase-in-quality-print__summary-item">
              <div class="purchase-in-quality-print__summary-label">当前轮次</div>
              <div class="purchase-in-quality-print__summary-value">{{ detail.currentRoundNo || '-' }}</div>
            </div>
            <div class="purchase-in-quality-print__summary-item">
              <div class="purchase-in-quality-print__summary-label">质检时间</div>
              <div class="purchase-in-quality-print__summary-value">{{ formatDateValue(detail.checkTime) }}</div>
            </div>
          </div>
          <div class="purchase-in-quality-print__context-meta">
            <span>{{ detail.orderNo || '-' }}</span>
            <span>{{ detail.assignedCheckerUserNickname || '-' }}</span>
            <span>{{ formatDateValue(detail.assignedCheckerTime) }}</span>
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
          <el-descriptions-item label="质检单号">{{ detail.no || '-' }}</el-descriptions-item>
          <el-descriptions-item label="采购入库单">{{ detail.purchaseInNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="采购单号">{{ detail.orderNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="供应商">{{ detail.supplierName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="质检状态">{{ getStatusLabel(detail.status) }}</el-descriptions-item>
          <el-descriptions-item label="质检结果">{{ getResultLabel(detail.result) }}</el-descriptions-item>
          <el-descriptions-item label="当前轮次">{{ detail.currentRoundNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="指派质检人">
            {{ detail.assignedCheckerUserNickname || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="最终质检人">{{ detail.checkerUserNickname || '-' }}</el-descriptions-item>
          <el-descriptions-item label="质检时间">{{ formatDateValue(detail.checkTime) }}</el-descriptions-item>
          <el-descriptions-item label="合格数量">{{ formatCount(detail.passCount) }}</el-descriptions-item>
          <el-descriptions-item label="不合格数量">{{ formatCount(detail.rejectCount) }}</el-descriptions-item>
          <el-descriptions-item label="复检原因" :span="4">{{ detail.recheckReason || '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="4">{{ detail.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="finance-shell__section">
        <div class="finance-shell__section-head">
          <div class="finance-shell__section-title">质检明细</div>
        </div>
        <div class="finance-shell__table-wrap">
          <el-table
            v-if="detail.items?.length"
            :data="detail.items"
            border
            stripe
            class="finance-shell__table finance-shell__table--dense"
          >
            <el-table-column label="产品" min-width="220">
              <template #default="{ row }">
                <div class="finance-shell__primary-cell">
                  <span class="finance-shell__primary-text">{{ row.productName || '-' }}</span>
                  <span class="finance-shell__muted-text">
                    {{ row.productBarCode || '-' }} / {{ row.productUnitName || '-' }}
                  </span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="入库数量" min-width="110" align="right">
              <template #default="{ row }">{{ formatCount(row.count) }}</template>
            </el-table-column>
            <el-table-column label="抽样数量" min-width="110" align="right">
              <template #default="{ row }">{{ formatCount(row.sampleCount) }}</template>
            </el-table-column>
            <el-table-column label="合格数量" min-width="110" align="right">
              <template #default="{ row }">{{ formatCount(row.qaPassCount) }}</template>
            </el-table-column>
            <el-table-column label="不合格数量" min-width="120" align="right">
              <template #default="{ row }">{{ formatCount(row.qaRejectCount) }}</template>
            </el-table-column>
            <el-table-column label="备注" min-width="180">
              <template #default="{ row }">{{ row.qaRemark || '-' }}</template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无质检明细" />
        </div>
      </div>

      <div class="finance-shell__section">
        <div class="finance-shell__section-head">
          <div class="finance-shell__section-title">质检轮次记录</div>
        </div>
        <div class="finance-shell__table-wrap">
          <el-table
            v-if="detail.rounds?.length"
            :data="detail.rounds"
            border
            stripe
            class="finance-shell__table finance-shell__table--dense"
          >
            <el-table-column label="轮次" min-width="100" align="center">
              <template #default="{ row }">{{ getRoundLabel(row.roundNo) }}</template>
            </el-table-column>
            <el-table-column label="类型" min-width="100" align="center">
              <template #default="{ row }">{{ getRoundTypeLabel(row.roundType) }}</template>
            </el-table-column>
            <el-table-column label="产品" min-width="180">
              <template #default="{ row }">{{ getItemName(row.qualityItemId) }}</template>
            </el-table-column>
            <el-table-column label="抽样数量" min-width="110" align="right">
              <template #default="{ row }">{{ formatCount(row.sampleCount) }}</template>
            </el-table-column>
            <el-table-column label="合格数量" min-width="110" align="right">
              <template #default="{ row }">{{ formatCount(row.passCount) }}</template>
            </el-table-column>
            <el-table-column label="不合格数量" min-width="120" align="right">
              <template #default="{ row }">{{ formatCount(row.rejectCount) }}</template>
            </el-table-column>
            <el-table-column label="质检人" min-width="120">
              <template #default="{ row }">{{ row.checkerUserNickname || '-' }}</template>
            </el-table-column>
            <el-table-column label="质检时间" min-width="180">
              <template #default="{ row }">{{ formatDateValue(row.checkTime) }}</template>
            </el-table-column>
            <el-table-column label="备注" min-width="180">
              <template #default="{ row }">{{ row.remark || '-' }}</template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无轮次记录" />
        </div>
      </div>

      <div class="finance-shell__section">
        <div class="finance-shell__section-head">
          <div class="finance-shell__section-title">缺陷明细记录</div>
        </div>
        <div class="finance-shell__table-wrap">
          <el-table
            v-if="detail.defects?.length"
            :data="detail.defects"
            border
            stripe
            class="finance-shell__table finance-shell__table--dense"
          >
            <el-table-column label="轮次" min-width="100" align="center">
              <template #default="{ row }">{{ getDefectRoundLabel(row.roundId) }}</template>
            </el-table-column>
            <el-table-column label="产品" min-width="180">
              <template #default="{ row }">{{ getItemName(row.qualityItemId) }}</template>
            </el-table-column>
            <el-table-column label="缺陷原因" min-width="160">
              <template #default="{ row }">{{ row.defectReasonName || '-' }}</template>
            </el-table-column>
            <el-table-column label="缺陷数量" min-width="110" align="right">
              <template #default="{ row }">{{ formatCount(row.defectCount) }}</template>
            </el-table-column>
            <el-table-column label="备注" min-width="180">
              <template #default="{ row }">{{ row.defectRemark || '-' }}</template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无缺陷记录" />
        </div>
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
import { erpCountInputFormatter } from '@/utils'
import { formatDate } from '@/utils/formatTime'
import PrintAttachmentList from '@/views/erp/finance/shared/PrintAttachmentList.vue'
import PrintStatePanel from '@/views/erp/finance/shared/PrintStatePanel.vue'
import PrintShell from '@/views/erp/finance/shared/print-shell.vue'
import {
  PurchaseInQualityApi,
  PURCHASE_IN_QUALITY_RESULT,
  PURCHASE_IN_QUALITY_ROUND_TYPE,
  PURCHASE_IN_QUALITY_STATUS,
  type PurchaseInQualityPrintDataVO,
  type PurchaseInQualityVO
} from '@/api/erp/purchase/in-quality'

defineOptions({ name: 'PurchaseInQualityPrintDialog' })

interface AttachmentLink {
  name: string
  url: string
}

const dialogVisible = ref(false)
const loading = ref(false)
const loadErrorMessage = ref('')
const currentId = ref<number>()
const detail = ref<PurchaseInQualityVO>()
const sourceAttachments = ref<AttachmentLink[]>([])

const isCompactAttachmentLayout = computed(
  () => sourceAttachments.value.length > 0 && sourceAttachments.value.length <= 2
)

const itemNameMap = computed<Record<number, string>>(() => {
  return (detail.value?.items || []).reduce<Record<number, string>>((acc, item) => {
    if (item.id) {
      acc[item.id] = item.productName || '-'
    }
    return acc
  }, {})
})

const roundNoMap = computed<Record<number, number | undefined>>(() => {
  return (detail.value?.rounds || []).reduce<Record<number, number | undefined>>((acc, item) => {
    if (item.id) {
      acc[item.id] = item.roundNo
    }
    return acc
  }, {})
})

const printObj = ref({
  id: 'purchaseInQualityPrintArea',
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

const formatCount = (value?: number | string | null) => {
  if (value === undefined || value === null || value === '') {
    return '-'
  }
  return erpCountInputFormatter(value)
}

const getStatusLabel = (status?: number) => {
  if (status === PURCHASE_IN_QUALITY_STATUS.FIRST_CHECKING) return '首检中'
  if (status === PURCHASE_IN_QUALITY_STATUS.WAIT_RECHECK) return '待复检'
  if (status === PURCHASE_IN_QUALITY_STATUS.RECHECKING) return '复检中'
  if (status === PURCHASE_IN_QUALITY_STATUS.DONE) return '已完成'
  if (status === PURCHASE_IN_QUALITY_STATUS.VOID) return '已作废'
  return '-'
}

const getStatusTagType = (status?: number) => {
  if (status === PURCHASE_IN_QUALITY_STATUS.FIRST_CHECKING) return 'warning'
  if (status === PURCHASE_IN_QUALITY_STATUS.WAIT_RECHECK) return 'danger'
  if (status === PURCHASE_IN_QUALITY_STATUS.RECHECKING) return 'primary'
  if (status === PURCHASE_IN_QUALITY_STATUS.DONE) return 'success'
  return 'info'
}

const getResultLabel = (result?: number) => {
  if (result === PURCHASE_IN_QUALITY_RESULT.PARTIAL) return '部分合格'
  if (result === PURCHASE_IN_QUALITY_RESULT.PASSED) return '全部合格'
  if (result === PURCHASE_IN_QUALITY_RESULT.REJECTED) return '全部不合格'
  return '待判定'
}

const getResultTagType = (result?: number) => {
  if (result === PURCHASE_IN_QUALITY_RESULT.PASSED) return 'success'
  if (result === PURCHASE_IN_QUALITY_RESULT.PARTIAL) return 'warning'
  if (result === PURCHASE_IN_QUALITY_RESULT.REJECTED) return 'danger'
  return 'info'
}

const getRoundLabel = (roundNo?: number) => {
  if (!roundNo) {
    return '-'
  }
  return `第 ${roundNo} 轮`
}

const getRoundTypeLabel = (roundType?: number) => {
  return roundType === PURCHASE_IN_QUALITY_ROUND_TYPE.RECHECK ? '复检' : '初检'
}

const getItemName = (qualityItemId?: number) => {
  if (!qualityItemId) {
    return '-'
  }
  return itemNameMap.value[qualityItemId] || `#${qualityItemId}`
}

const getDefectRoundLabel = (roundId?: number) => {
  if (!roundId) {
    return '-'
  }
  return getRoundLabel(roundNoMap.value[roundId])
}

const buildAttachmentLinks = (list?: Array<{ name?: string; url?: string }>) =>
  (list || [])
    .filter((item) => !!item?.url)
    .map((item, index) => ({
      name: item.name || `附件 ${index + 1}`,
      url: item.url as string
    }))

const resetState = () => {
  detail.value = undefined
  sourceAttachments.value = []
  loadErrorMessage.value = ''
}

const loadPrintData = async (id: number) => {
  currentId.value = id
  loading.value = true
  resetState()
  try {
    const data: PurchaseInQualityPrintDataVO = await PurchaseInQualityApi.getPurchaseInQualityPrintData(id)
    if (currentId.value !== id || !dialogVisible.value) {
      return
    }
    detail.value = data.purchaseInQuality
    sourceAttachments.value = buildAttachmentLinks(data.sourceAttachments)
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

const open = async (id?: number) => {
  if (!id || loading.value) {
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
  resetState()
  currentId.value = undefined
  loading.value = false
}

defineExpose({ open, retry })
</script>

<style scoped lang="scss">
.purchase-in-quality-print__context-card {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 16px 18px;
  border: 1px solid #dbe4f0;
  border-radius: 18px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
  box-shadow: 0 1px 2px rgb(15 23 42 / 4%);
}

.purchase-in-quality-print__context-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.purchase-in-quality-print__context-titles {
  min-width: 0;
}

.purchase-in-quality-print__context-title {
  color: #0f172a;
  font-size: 22px;
  font-weight: 700;
  line-height: 1.2;
}

.purchase-in-quality-print__context-subtitle {
  margin-top: 4px;
  color: #64748b;
  font-size: 13px;
}

.purchase-in-quality-print__context-tags {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.purchase-in-quality-print__context-summary {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.purchase-in-quality-print__summary-item {
  min-width: 0;
  padding: 12px 14px;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  background: #f8fafc;
}

.purchase-in-quality-print__summary-label {
  color: #64748b;
  font-size: 12px;
}

.purchase-in-quality-print__summary-value {
  margin-top: 6px;
  color: #0f172a;
  font-size: 16px;
  font-weight: 600;
  word-break: break-word;
}

.purchase-in-quality-print__context-meta {
  display: flex;
  align-items: center;
  gap: 8px 16px;
  flex-wrap: wrap;
  color: #475569;
  font-size: 12px;
}

@media (max-width: 1024px) {
  .purchase-in-quality-print__context-summary {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .purchase-in-quality-print__context-card {
    padding: 14px;
  }

  .purchase-in-quality-print__context-summary {
    grid-template-columns: 1fr;
  }
}
</style>
