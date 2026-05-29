<template>
  <PrintShell
    v-model="dialogVisible"
    title="库存调拨打印"
    print-area-id="stockMovePrintArea"
    :loading="loading"
    @closed="handleClosed"
  >
    <template #context>
      <template v-if="detail">
        <PrintContextCard
          :title="detail.no || `#${detail.id}`"
          :summary-items="summaryItems"
          :meta-items="metaItems"
          :status-label="detail.statusName || getStatusLabel(detail.status)"
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
          <div class="finance-shell__section-title">基础信息</div>
        </div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="调拨单号">{{ detail.no || '-' }}</el-descriptions-item>
          <el-descriptions-item label="调拨时间">{{ formatDateValue(detail.moveTime) }}</el-descriptions-item>
          <el-descriptions-item label="创建人">{{ detail.creatorName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ detail.statusName || getStatusLabel(detail.status) }}</el-descriptions-item>
          <el-descriptions-item label="合计数量">{{ formatCount(detail.totalCount) }}</el-descriptions-item>
          <el-descriptions-item label="合计金额">{{ formatMoney(detail.totalPrice) }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="finance-shell__section">
        <div class="finance-shell__section-head">
          <div class="finance-shell__section-title">调拨明细</div>
        </div>
        <div class="finance-shell__table-wrap">
          <el-table
            v-if="detail.items?.length"
            :data="detail.items"
            border
            stripe
            class="finance-shell__table finance-shell__table--dense"
          >
            <el-table-column label="产品信息" min-width="240">
              <template #default="{ row }">
                <div class="finance-shell__primary-cell">
                  <span class="finance-shell__primary-text">{{ row.productName || '-' }}</span>
                  <span class="finance-shell__muted-text">
                    {{ row.productBarCode || '-' }} / {{ row.productUnitName || '-' }}
                  </span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="调出仓库" min-width="140">
              <template #default="{ row }">{{ row.fromWarehouseName || `#${row.fromWarehouseId || '-'}` }}</template>
            </el-table-column>
            <el-table-column label="调入仓库" min-width="140">
              <template #default="{ row }">{{ row.toWarehouseName || `#${row.toWarehouseId || '-'}` }}</template>
            </el-table-column>
            <el-table-column label="数量" min-width="110" align="right">
              <template #default="{ row }">{{ formatCount(row.count) }}</template>
            </el-table-column>
            <el-table-column label="单价" min-width="120" align="right">
              <template #default="{ row }">{{ formatMoney(row.productPrice) }}</template>
            </el-table-column>
            <el-table-column label="金额" min-width="120" align="right">
              <template #default="{ row }">{{ formatMoney(row.totalPrice) }}</template>
            </el-table-column>
            <el-table-column label="备注" min-width="180">
              <template #default="{ row }">{{ row.remark || '-' }}</template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无调拨明细" />
        </div>
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
import PrintAttachmentList from '@/views/erp/finance/shared/PrintAttachmentList.vue'
import PrintContextCard from '@/views/erp/finance/shared/PrintContextCard.vue'
import PrintStatePanel from '@/views/erp/finance/shared/PrintStatePanel.vue'
import PrintShell from '@/views/erp/finance/shared/print-shell.vue'
import { StockMoveApi, StockMovePrintDataVO } from '@/api/erp/stock/move'

defineOptions({ name: 'StockMovePrintDialog' })

interface AttachmentLink {
  name: string
  url: string
}

const dialogVisible = ref(false)
const loading = ref(false)
const loadErrorMessage = ref('')
const currentId = ref<number>()
const detail = ref<NonNullable<StockMovePrintDataVO['stockMove']>>()
const sourceAttachments = ref<AttachmentLink[]>([])
const isCompactAttachmentLayout = computed(
  () => sourceAttachments.value.length > 0 && sourceAttachments.value.length <= 2
)

const printObj = ref({
  id: 'stockMovePrintArea',
  popTitle: '&nbsp',
  extraCss: '/print.css',
  extraHead: '',
  zIndex: 20003
})

const summaryItems = computed(() => [
  { label: '合计数量', value: formatCount(detail.value?.totalCount) },
  { label: '合计金额', value: formatMoney(detail.value?.totalPrice) },
  { label: '调拨产品', value: detail.value?.productNames || '-' }
])

const metaItems = computed(() => [
  detail.value?.creatorName || '-',
  formatDateValue(detail.value?.moveTime),
  detail.value?.statusName || getStatusLabel(detail.value?.status)
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
  return erpPriceInputFormatter(numberValue)
}

const getStatusTagType = (status?: number) => resolveErpAuditStatusTagType(status)
const getStatusLabel = (status?: number) => resolveErpAuditStatusLabel(status)

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

const loadPrintData = async (id: number) => {
  currentId.value = id
  loading.value = true
  resetState()
  try {
    const data = await StockMoveApi.getStockMovePrintData(id)
    if (currentId.value !== id || !dialogVisible.value) {
      return
    }
    detail.value = data.stockMove
    sourceAttachments.value = buildAttachmentLinks(data.sourceAttachments)
    loadErrorMessage.value = ''
  } catch {
    detail.value = undefined
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
  resetState()
  loading.value = false
  currentId.value = undefined
}

defineExpose({ open, retry })
</script>
