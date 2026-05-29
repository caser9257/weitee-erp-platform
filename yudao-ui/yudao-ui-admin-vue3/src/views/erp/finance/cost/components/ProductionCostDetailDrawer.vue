<template>
  <el-drawer
    v-model="drawerVisible"
    :append-to-body="true"
    :size="drawerWidth"
    destroy-on-close
    :with-header="false"
    custom-class="production-cost-detail-drawer"
    modal-class="production-cost-detail-drawer__mask"
    :close-on-click-modal="true"
    :close-on-press-escape="true"
    @closed="handleDrawerClosed"
    @opened="handleDrawerOpened"
  >
    <div class="drawer-shell">
      <div class="drawer-shell__header">
        <div>
          <div class="drawer-shell__title">成本穿透</div>
          <div class="drawer-shell__meta">{{ drawerHeader }}</div>
        </div>
        <el-button text @click="drawerVisible = false">
          <Icon icon="ep:close" class="mr-5px" />
          关闭
        </el-button>
      </div>

      <div class="drawer-shell__body">
        <el-alert
          v-if="detailError"
          class="mb-12px"
          :closable="false"
          show-icon
          type="error"
          :title="detailError"
        >
          <template #default>
            <el-button
              link
              type="primary"
              :disabled="detailLoading || !productionOrderId"
              @click="loadDetail"
            >
              重新加载
            </el-button>
          </template>
        </el-alert>

        <template v-else>
          <div v-if="hasRenderableDetail" class="context-card">
            <div class="context-card__title">{{ detailData.productionOrderNo || '-' }}</div>
            <div class="context-card__subtitle">
              {{ detailData.productName || '-' }} / {{ detailData.projectName || detailData.projectNo || '-' }}
            </div>
            <div class="context-card__metrics">
              <div>
                <div class="context-card__label">产品</div>
                <div class="context-card__value">{{ detailData.productName || '-' }}</div>
              </div>
              <div>
                <div class="context-card__label">项目</div>
                <div class="context-card__value">
                  {{ detailData.projectName || detailData.projectNo || '-' }}
                </div>
              </div>
              <div>
                <div class="context-card__label">完工数量</div>
                <div class="context-card__value">{{ formatAmount(detailData.finishedQty) }}</div>
              </div>
              <div>
                <div class="context-card__label">快照时间</div>
                <div class="context-card__value">{{ formatDateTimeValue(detailData.costSnapshotTime) }}</div>
              </div>
            </div>
          </div>

          <div v-if="hasRenderableDetail" v-loading="detailLoading" class="detail-grid">
            <div class="detail-card">
              <div class="detail-card__title">成本构成</div>
              <div class="detail-card__list">
                <div v-for="item in detailMetricList" :key="item.label" class="detail-card__item">
                  <span>{{ item.label }}</span>
                  <strong>{{ formatAmount(item.value) }}</strong>
                </div>
              </div>
            </div>

            <div class="detail-card">
              <div class="detail-card__title">材料明细</div>
              <el-table :data="detailData?.materialDetails || []" size="small" stripe>
                <el-table-column label="领料单号" prop="issueNo" min-width="140" />
                <el-table-column label="领料时间" min-width="160">
                  <template #default="{ row }">{{ formatDateTimeValue(row.issueTime) }}</template>
                </el-table-column>
                <el-table-column label="领料金额" align="right" min-width="120">
                  <template #default="{ row }">{{ formatAmount(row.issueAmount) }}</template>
                </el-table-column>
                <el-table-column label="备注" prop="remark" min-width="120" />
                <template #empty>
                  <el-empty description="暂无材料明细" />
                </template>
              </el-table>
            </div>

            <div class="detail-card detail-card--full">
              <div class="detail-card__title">归集明细</div>
              <el-table :data="detailData?.costEntries || []" size="small" stripe>
                <el-table-column label="成本类型" prop="costTypeName" min-width="120" />
                <el-table-column label="来源类型" prop="sourceTypeName" min-width="120" />
                <el-table-column label="来源单号" prop="sourceAllocationNo" min-width="140" />
                <el-table-column label="归集月份" prop="accountingMonth" min-width="110" />
                <el-table-column label="金额" align="right" min-width="120">
                  <template #default="{ row }">{{ formatAmount(row.amount) }}</template>
                </el-table-column>
                <el-table-column label="备注" prop="remark" min-width="140" />
                <template #empty>
                  <el-empty description="暂无归集明细" />
                </template>
              </el-table>
            </div>
          </div>

          <div
            v-if="!detailLoading && !detailError && !hasRenderableDetail"
            class="drawer-shell__empty"
          >
            <el-empty :description="detailEmptyMessage || '暂无可展示的成本明细'">
              <template #image>
                <div class="finance-shell__empty-icon">
                  <Icon icon="ep:document" />
                </div>
              </template>
            </el-empty>
          </div>
        </template>
      </div>

      <div class="drawer-shell__footer">
        <el-button @click="drawerVisible = false">关闭</el-button>
      </div>
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useWindowSize } from '@vueuse/core'
import { FinanceCostApi, type FinanceCostDetailRespVO } from '@/api/erp/finance/cost'
import { formatAmount, formatDateTimeValue } from '@/views/erp/finance/shared/accounting'
import { getDefaultCostDetailHeader } from '../costPage.helpers'

const props = defineProps<{
  modelValue: boolean
  productionOrderId?: number
  headerText?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const { width } = useWindowSize()
const detailLoading = ref(false)
const detailError = ref('')
const detailEmptyMessage = ref('')
const detailData = ref<FinanceCostDetailRespVO>()

const drawerVisible = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value)
})

const drawerWidth = computed(() => {
  if (width.value < 768) {
    return '100%'
  }
  const viewportWidth = Number(width.value || window.innerWidth || 0)
  if (viewportWidth < 1200) {
    return `${Math.max(Math.floor(viewportWidth - 24), 360)}px`
  }
  return `${Math.max(Math.min(Math.floor(viewportWidth - 32), 1280), 420)}px`
})

const drawerHeader = computed(() => {
  if (props.headerText) {
    return props.headerText
  }
  return getDefaultCostDetailHeader(detailData.value?.productionOrderNo)
})

const detailMetricList = computed(() => [
  { label: '材料成本', value: detailData.value?.materialCost },
  { label: '人工成本', value: detailData.value?.laborCost },
  { label: '折旧成本', value: detailData.value?.depreciationCost },
  { label: '电费成本', value: detailData.value?.powerCost },
  { label: '其他制造费用', value: detailData.value?.otherCost },
  { label: '总成本', value: detailData.value?.totalCost },
  { label: '单位成本', value: detailData.value?.unitCost }
])

const hasRenderableDetail = computed(() => isRenderableDetail(detailData.value))

const resetDetailState = () => {
  detailData.value = undefined
  detailError.value = ''
  detailEmptyMessage.value = ''
}

const loadDetail = async () => {
  if (!props.productionOrderId || detailLoading.value) {
    return
  }
  detailLoading.value = true
  detailError.value = ''
  detailEmptyMessage.value = ''
  detailData.value = undefined
  try {
    const data = await FinanceCostApi.getDetail(props.productionOrderId)
    if (isRenderableDetail(data)) {
      detailData.value = data
      return
    }
    detailEmptyMessage.value = '暂无可展示的成本明细'
  } catch {
    detailError.value = '成本穿透加载失败，请稍后重试'
  } finally {
    detailLoading.value = false
  }
}

const handleDrawerClosed = () => {
  resetDetailState()
}

const handleDrawerOpened = () => {
  if (typeof window === 'undefined') {
    return
  }
  requestAnimationFrame(() => {
    window.dispatchEvent(new Event('resize'))
  })
}

const isRenderableDetail = (data?: FinanceCostDetailRespVO) => {
  if (!data) {
    return false
  }
  return Boolean(
    data.productionOrderId ||
      data.productionOrderNo ||
      data.productName ||
      data.projectName ||
      data.projectNo ||
      data.materialDetails?.length ||
      data.costEntries?.length
  )
}

watch(
  () => [props.modelValue, props.productionOrderId] as const,
  async ([visible, productionOrderId], oldValue) => {
    const [oldVisible, oldProductionOrderId] = oldValue || []
    if (!visible) {
      resetDetailState()
      return
    }
    if (!productionOrderId) {
      detailError.value = '未找到可穿透的生产工单'
      detailData.value = undefined
      detailEmptyMessage.value = ''
      return
    }
    if (!oldVisible || productionOrderId !== oldProductionOrderId) {
      await loadDetail()
    }
  },
  { immediate: true }
)
</script>

<style scoped>
:deep(.production-cost-detail-drawer) {
  display: flex;
  flex-direction: column;
  background: #f8fafc;
}

:deep(.production-cost-detail-drawer .el-drawer__header) {
  display: none;
}

:deep(.production-cost-detail-drawer .el-drawer__body) {
  flex: 1;
  overflow: hidden;
  padding: 0;
}

.drawer-shell {
  display: flex;
  height: 100%;
  flex-direction: column;
  background: #f8fafc;
}

.drawer-shell__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  border-bottom: 1px solid #e2e8f0;
  background: #fff;
  padding: 18px 20px 14px;
}

.drawer-shell__title {
  color: #0f172a;
  font-size: 18px;
  font-weight: 700;
}

.drawer-shell__meta {
  margin-top: 4px;
  color: #64748b;
  font-size: 12px;
}

.drawer-shell__body {
  flex: 1;
  overflow: auto;
  padding: 16px 20px 20px;
}

.drawer-shell__footer {
  position: sticky;
  bottom: 0;
  z-index: 5;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  border-top: 1px solid #e2e8f0;
  background: rgb(255 255 255 / 94%);
  padding: 12px 20px;
  backdrop-filter: blur(6px);
}

.drawer-shell__empty {
  margin-top: 16px;
}

.context-card {
  border-radius: 16px;
  background: linear-gradient(135deg, #0f172a 0%, #1e3a8a 100%);
  padding: 16px 18px;
  color: #fff;
  box-shadow: 0 12px 28px rgb(15 23 42 / 18%);
}

.context-card__title {
  font-size: 18px;
  font-weight: 700;
}

.context-card__subtitle {
  margin-top: 4px;
  color: #cbd5e1;
  font-size: 12px;
}

.context-card__metrics {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-top: 16px;
}

.context-card__label {
  color: #cbd5e1;
  font-size: 12px;
}

.context-card__value {
  margin-top: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
  font-weight: 700;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  margin-top: 16px;
}

.detail-card {
  min-width: 0;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  background: #fff;
  padding: 16px;
  box-shadow: 0 1px 2px rgb(15 23 42 / 6%);
}

.detail-card--full {
  grid-column: 1 / -1;
}

.detail-card__title {
  margin-bottom: 12px;
  color: #0f172a;
  font-weight: 700;
}

.detail-card__list {
  display: grid;
  gap: 10px;
}

.detail-card__item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  border-radius: 12px;
  background: #f8fafc;
  padding: 10px 12px;
}

@media (max-width: 1200px) {
  .context-card__metrics,
  .detail-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .context-card__metrics,
  .detail-grid {
    grid-template-columns: 1fr;
  }

  .drawer-shell__header {
    flex-direction: column;
  }
}
</style>
