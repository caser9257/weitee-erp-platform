<template>
  <PrintShell
    v-model="dialogVisible"
    title="委外入库打印"
    print-area-id="outsourceInboundPrintArea"
    :loading="loading"
    @closed="handleClosed"
  >
    <template #context>
      <template v-if="detail">
        <PrintContextCard
          :title="detail.inboundNo || `#${detail.id}`"
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
          <div class="finance-shell__section-title">基础信息</div>
        </div>
        <div class="grid gap-4 xl:grid-cols-[1.2fr,0.8fr]">
          <div class="rounded-2xl border border-slate-200 bg-slate-50 p-4 shadow-sm">
            <dl class="space-y-3 text-sm">
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">入库单号</dt>
                <dd class="text-right font-mono text-slate-800">{{ detail.inboundNo || '-' }}</dd>
              </div>
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">委外订单号</dt>
                <dd class="text-right font-mono text-slate-800">{{ detail.orderNo || '-' }}</dd>
              </div>
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">仓库</dt>
                <dd class="text-right text-slate-800">{{ detail.warehouseName || '-' }}</dd>
              </div>
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">批次号</dt>
                <dd class="text-right font-mono text-slate-800">{{ detail.batchNo || '-' }}</dd>
              </div>
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">入库时间</dt>
                <dd class="text-right text-slate-800">{{ formatDateValue(detail.inboundTime || detail.createTime) }}</dd>
              </div>
            </dl>
          </div>

          <div class="rounded-2xl border border-slate-200 bg-slate-50 p-4 shadow-sm">
            <dl class="space-y-3 text-sm">
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">入库数量</dt>
                <dd class="text-right font-mono text-slate-800">{{ formatNumber(detail.inboundQty) }}</dd>
              </div>
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">材料成本</dt>
                <dd class="text-right font-mono text-slate-800">{{ formatNumber(detail.materialCost) }}</dd>
              </div>
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">加工费</dt>
                <dd class="text-right font-mono text-slate-800">{{ formatNumber(detail.processFee) }}</dd>
              </div>
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">总成本</dt>
                <dd class="text-right font-mono text-slate-800">{{ formatNumber(detail.totalCost) }}</dd>
              </div>
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">单位成本</dt>
                <dd class="text-right font-mono text-slate-800">{{ formatNumber(detail.unitCost) }}</dd>
              </div>
            </dl>
          </div>
        </div>
      </div>

      <div class="finance-shell__section">
        <div class="finance-shell__section-head">
          <div class="finance-shell__section-title">财务关联</div>
        </div>
        <div class="grid gap-4 xl:grid-cols-[1.2fr,0.8fr]">
          <div class="rounded-2xl border border-slate-200 bg-slate-50 p-4 shadow-sm">
            <dl class="space-y-3 text-sm">
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">计划数量</dt>
                <dd class="text-right font-mono text-slate-800">{{ formatNumber(financialFacts?.plannedQty) }}</dd>
              </div>
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">已完工数量</dt>
                <dd class="text-right font-mono text-slate-800">{{ formatNumber(financialFacts?.finishedQty) }}</dd>
              </div>
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">损耗数量</dt>
                <dd class="text-right font-mono text-slate-800">{{ formatNumber(financialFacts?.lossQty) }}</dd>
              </div>
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">待入库数量</dt>
                <dd class="text-right font-mono text-slate-800">{{ formatNumber(financialFacts?.pendingInboundQty) }}</dd>
              </div>
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">未闭环数量</dt>
                <dd class="text-right font-mono text-slate-800">{{ formatNumber(financialFacts?.unresolvedQty) }}</dd>
              </div>
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">超入库数量</dt>
                <dd class="text-right font-mono text-slate-800">{{ formatNumber(financialFacts?.overInboundQty) }}</dd>
              </div>
            </dl>
          </div>

          <div class="rounded-2xl border border-slate-200 bg-slate-50 p-4 shadow-sm">
            <dl class="space-y-3 text-sm">
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">材料成本</dt>
                <dd class="text-right font-mono text-slate-800">{{ formatNumber(financialFacts?.materialCost) }}</dd>
              </div>
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">退料金额</dt>
                <dd class="text-right font-mono text-slate-800">{{ formatNumber(financialFacts?.returnMaterialCost) }}</dd>
              </div>
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">净材料成本</dt>
                <dd class="text-right font-mono text-slate-800">{{ formatNumber(financialFacts?.netMaterialCost) }}</dd>
              </div>
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">加工费</dt>
                <dd class="text-right font-mono text-slate-800">{{ formatNumber(financialFacts?.processFee) }}</dd>
              </div>
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">总成本</dt>
                <dd class="text-right font-mono text-slate-800">{{ formatNumber(financialFacts?.totalCost) }}</dd>
              </div>
              <div class="flex justify-between gap-4">
                <dt class="text-slate-500">单位成本</dt>
                <dd class="text-right font-mono text-slate-800">{{ formatNumber(financialFacts?.unitCost) }}</dd>
              </div>
            </dl>
          </div>
        </div>
        <div class="mt-4 grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
          <div class="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
            <div class="text-xs text-slate-500">发料单数</div>
            <div class="mt-2 text-xl font-semibold text-slate-800">{{ financialFacts?.issueCount ?? 0 }}</div>
          </div>
          <div class="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
            <div class="text-xs text-slate-500">退料单数</div>
            <div class="mt-2 text-xl font-semibold text-slate-800">{{ financialFacts?.returnCount ?? 0 }}</div>
          </div>
          <div class="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
            <div class="text-xs text-slate-500">费用单数</div>
            <div class="mt-2 text-xl font-semibold text-slate-800">{{ financialFacts?.feeCount ?? 0 }}</div>
          </div>
          <div class="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
            <div class="text-xs text-slate-500">入库单数</div>
            <div class="mt-2 text-xl font-semibold text-slate-800">{{ financialFacts?.inboundCount ?? 0 }}</div>
          </div>
        </div>
        <div class="mt-4 flex flex-wrap gap-2">
          <el-tag v-if="financialFacts?.hasSupplementIssue" effect="light" type="warning">存在补发料</el-tag>
          <el-tag effect="light" type="success">总成本已归集</el-tag>
        </div>
      </div>

      <div class="finance-shell__section">
        <div class="finance-shell__section-head">
          <div class="finance-shell__section-title">备注信息</div>
        </div>
        <div class="rounded-2xl border border-slate-200 bg-slate-50 p-4 text-sm text-slate-700 shadow-sm">
          {{ detail.remark || '-' }}
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
import PrintContextCard from '@/views/erp/finance/shared/PrintContextCard.vue'
import PrintStatePanel from '@/views/erp/finance/shared/PrintStatePanel.vue'
import PrintAttachmentList from '@/views/erp/finance/shared/PrintAttachmentList.vue'
import PrintShell from '@/views/erp/finance/shared/print-shell.vue'
import { OutsourceInboundApi, OutsourceInboundPrintDataVO } from '@/api/erp/mrp/outsource-inbound'

defineOptions({ name: 'OutsourceInboundPrintDialog' })

interface AttachmentLink {
  name: string
  url: string
}

const dialogVisible = ref(false)
const loading = ref(false)
const loadErrorMessage = ref('')
const currentId = ref<number>()
const detail = ref<OutsourceInboundPrintDataVO['outsourceInbound']>()
const financialFacts = ref<OutsourceInboundPrintDataVO['financialFacts']>()
const sourceAttachments = ref<AttachmentLink[]>([])
const isCompactAttachmentLayout = computed(
  () => sourceAttachments.value.length > 0 && sourceAttachments.value.length <= 2
)

const printObj = ref({
  id: 'outsourceInboundPrintArea',
  popTitle: '&nbsp',
  extraCss: '/print.css',
  extraHead: '',
  zIndex: 20003
})

const summaryItems = computed(() => [
  { label: '委外订单号', value: detail.value?.orderNo || '-' },
  { label: '入库数量', value: formatNumber(detail.value?.inboundQty) },
  { label: '总成本', value: formatNumber(detail.value?.totalCost) }
])

const metaItems = computed(() => [
  detail.value?.orderNo || '-',
  detail.value?.warehouseName || '-',
  formatDateValue(detail.value?.inboundTime || detail.value?.createTime)
])

const formatDateValue = (value?: string | Date | number) => {
  if (!value) {
    return '-'
  }
  return formatDate(new Date(value), 'YYYY-MM-DD HH:mm:ss')
}

const formatNumber = (value?: number) => (value == null ? '-' : Number(value).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }))

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
    const data = await OutsourceInboundApi.getOutsourceInboundPrintData(id)
    if (currentId.value !== id || !dialogVisible.value) {
      return
    }
    detail.value = data.outsourceInbound
    financialFacts.value = data.financialFacts
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
