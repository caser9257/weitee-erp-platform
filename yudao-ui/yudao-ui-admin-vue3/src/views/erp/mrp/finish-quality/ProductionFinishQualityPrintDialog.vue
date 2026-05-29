<template>
  <PrintShell
    v-model="dialogVisible"
    title="成品入库打印"
    print-area-id="productionFinishQualityPrintArea"
    :loading="loading"
    @closed="handleClosed"
  >
    <template #context>
      <template v-if="detail">
        <PrintContextCard
          :title="detail.no || `#${detail.id}`"
          subtitle="成品入库单"
          :summary-items="summaryItems"
          :meta-items="metaItems"
          :status-label="getStatusLabel(detail.status)"
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
          <el-descriptions-item label="质检单号">{{ detail.no || '-' }}</el-descriptions-item>
          <el-descriptions-item label="生产工单">{{ detail.productionOrderNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="产品">{{ productName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="质检状态">{{ getStatusLabel(detail.status) }}</el-descriptions-item>
          <el-descriptions-item label="质检时间">
            {{ formatDateValue(detail.checkTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDateValue(detail.createTime) }}
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="finance-shell__section">
        <div class="finance-shell__section-head">
          <div class="finance-shell__section-title">入库信息</div>
        </div>
        <div class="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
          <div class="rounded-2xl border border-slate-200 bg-slate-50 p-4 shadow-sm">
            <div class="text-xs text-slate-500">报工数量</div>
            <div class="mt-2 text-xl font-semibold text-slate-800">{{ formatCount(detail.reportQty) }}</div>
          </div>
          <div class="rounded-2xl border border-slate-200 bg-slate-50 p-4 shadow-sm">
            <div class="text-xs text-slate-500">合格数量</div>
            <div class="mt-2 text-xl font-semibold text-slate-800">{{ formatCount(detail.qualifiedQty) }}</div>
          </div>
          <div class="rounded-2xl border border-slate-200 bg-slate-50 p-4 shadow-sm">
            <div class="text-xs text-slate-500">不合格数量</div>
            <div class="mt-2 text-xl font-semibold text-slate-800">{{ formatCount(detail.unqualifiedQty) }}</div>
          </div>
          <div class="rounded-2xl border border-slate-200 bg-slate-50 p-4 shadow-sm">
            <div class="text-xs text-slate-500">质检人</div>
            <div class="mt-2 text-xl font-semibold text-slate-800">{{ checkerName || '-' }}</div>
          </div>
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
import PrintShell from '@/views/erp/finance/shared/print-shell.vue'
import {
  PRODUCTION_FINISH_QUALITY_STATUS,
  ProductionFinishQualityApi,
  type ProductionFinishQualityPrintDataVO,
  type ProductionFinishQualityVO
} from '@/api/erp/mrp/finish-quality'

defineOptions({ name: 'ProductionFinishQualityPrintDialog' })

const dialogVisible = ref(false)
const loading = ref(false)
const loadErrorMessage = ref('')
const currentId = ref<number>()
const detail = ref<ProductionFinishQualityVO>()
const productName = ref('')
const checkerName = ref('')

const printObj = ref({
  id: 'productionFinishQualityPrintArea',
  popTitle: '&nbsp',
  extraCss: '/print.css',
  extraHead: '',
  zIndex: 20003
})

const summaryItems = computed(() => [
  { label: '产品', value: productName.value || '-' },
  { label: '入库数量', value: formatCount(detail.value?.reportQty) },
  { label: '合格数量', value: formatCount(detail.value?.qualifiedQty) }
])

const metaItems = computed(() => [
  detail.value?.productionOrderNo || '-',
  checkerName.value || '-',
  formatDateValue(detail.value?.checkTime || detail.value?.createTime)
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

const getStatusLabel = (status?: number) => {
  if (status === PRODUCTION_FINISH_QUALITY_STATUS.TO_INSPECT) return '待质检'
  if (status === PRODUCTION_FINISH_QUALITY_STATUS.PARTIAL) return '部分合格'
  if (status === PRODUCTION_FINISH_QUALITY_STATUS.PASSED) return '全部合格'
  if (status === PRODUCTION_FINISH_QUALITY_STATUS.REJECTED) return '全部不合格'
  return '-'
}

const getStatusTagType = (status?: number) => {
  if (status === PRODUCTION_FINISH_QUALITY_STATUS.TO_INSPECT) return 'warning'
  if (status === PRODUCTION_FINISH_QUALITY_STATUS.PARTIAL) return 'primary'
  if (status === PRODUCTION_FINISH_QUALITY_STATUS.PASSED) return 'success'
  if (status === PRODUCTION_FINISH_QUALITY_STATUS.REJECTED) return 'danger'
  return 'info'
}

const resetState = () => {
  detail.value = undefined
  productName.value = ''
  checkerName.value = ''
  loadErrorMessage.value = ''
}

const loadPrintData = async (id: number) => {
  currentId.value = id
  loading.value = true
  resetState()
  try {
    const data: ProductionFinishQualityPrintDataVO = await ProductionFinishQualityApi.getProductionFinishQualityPrintData(id)
    if (currentId.value !== id || !dialogVisible.value) {
      return
    }
    detail.value = data.productionFinishQuality
    productName.value = data.productName || ''
    checkerName.value = data.checkerName || ''
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
  loading.value = false
}

defineExpose({ open, retry })
</script>
