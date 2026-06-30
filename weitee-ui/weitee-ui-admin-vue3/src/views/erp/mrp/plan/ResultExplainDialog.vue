<template>
  <Dialog
    :title="dialogTitle"
    v-model="dialogVisible"
    width="min(860px, calc(100vw - 32px))"
    @close="resetDialogState"
  >
    <div v-loading="componentLoading" class="result-explain-dialog">
      <div
        v-if="currentResult"
        class="mb-16px rounded-4px border border-[var(--el-border-color-light)] p-12px"
      >
        <div class="result-explain-dialog__trace-head">追溯信息</div>
        <div class="result-explain-dialog__trace-grid">
          <div class="result-explain-dialog__trace-item">
            <span class="result-explain-dialog__trace-label">追溯节点</span>
            <span class="result-explain-dialog__trace-value">{{ formatTraceValue(currentResult.traceNodeId) }}</span>
          </div>
          <div class="result-explain-dialog__trace-item">
            <span class="result-explain-dialog__trace-label">追溯层级</span>
            <span class="result-explain-dialog__trace-value">{{ formatTraceLevel(currentResult.traceLevel) }}</span>
          </div>
          <div class="result-explain-dialog__trace-item">
            <span class="result-explain-dialog__trace-label">父件</span>
            <span class="result-explain-dialog__trace-value">{{ formatTraceValue(currentResult.parentMaterialId) }}</span>
          </div>
          <div class="result-explain-dialog__trace-item">
            <span class="result-explain-dialog__trace-label">BOM 项</span>
            <span class="result-explain-dialog__trace-value">{{ formatTraceValue(currentResult.bomItemId) }}</span>
          </div>
          <div class="result-explain-dialog__trace-item result-explain-dialog__trace-item--wide">
            <span class="result-explain-dialog__trace-label">追溯键</span>
            <span class="result-explain-dialog__trace-value result-explain-dialog__trace-value--mono">
              {{ formatTraceValue(currentResult.tracePathKey) }}
            </span>
          </div>
        </div>
      </div>
      <el-alert
        v-if="loadErrorMessage"
        :title="loadErrorMessage"
        type="error"
        :closable="false"
        show-icon
        class="mb-16px"
      />
      <div v-if="currentResult" class="mb-16px rounded-4px border border-[var(--el-border-color-light)] p-12px">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="母项">
            {{ currentResult.rootProductName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="物料">
            {{ currentResult.materialName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="策略编码">
            {{ currentResult.policyCode || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="策略版本">
            {{ currentResult.policyVersion ?? '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="毛需求">
            {{ formatQty(currentResult.grossDemandQty) }}
          </el-descriptions-item>
          <el-descriptions-item label="净需求">
            {{ formatQty(currentResult.netDemandQty) }}
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <el-alert
        v-if="currentResult?.skipReason && componentList.length === 0 && !loadErrorMessage"
        :title="`该结果未参与净需求运算，跳过原因：${getSkipReasonLabel(currentResult.skipReason)}`"
        type="warning"
        :closable="false"
        show-icon
        class="mb-16px"
      />

      <el-table
        :data="componentList"
        :stripe="true"
        :show-overflow-tooltip="true"
        :empty-text="componentEmptyText"
      >
        <el-table-column label="组件" prop="componentName" min-width="160" />
        <el-table-column label="口径类型" prop="componentRole" min-width="130">
          <template #default="{ row }">
            {{ getComponentRoleLabel(row.componentRole) }}
          </template>
        </el-table-column>
        <el-table-column label="顺序" prop="sequenceNo" width="90" align="center">
          <template #default="{ row }">
            {{ row.sequenceNo ?? '-' }}
          </template>
        </el-table-column>
        <el-table-column label="基准数量" prop="baseQty" min-width="120" align="right">
          <template #default="{ row }">
            {{ formatQty(row.baseQty) }}
          </template>
        </el-table-column>
        <el-table-column label="抵扣数量" prop="consumedQty" min-width="120" align="right">
          <template #default="{ row }">
            {{ formatQty(row.consumedQty) }}
          </template>
        </el-table-column>
        <el-table-column label="剩余数量" prop="remainingQty" min-width="120" align="right">
          <template #default="{ row }">
            {{ formatQty(row.remainingQty) }}
          </template>
        </el-table-column>
      </el-table>
    </div>
    <template #footer>
      <el-button v-if="loadErrorMessage" @click="reloadCurrentResult" :disabled="componentLoading">
        重新加载
      </el-button>
      <el-button @click="dialogVisible = false">关闭</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { MrpPlanApi, MrpResultComponentVO, MrpResultVO } from '@/api/erp/mrp/plan'

defineOptions({ name: 'ResultExplainDialog' })

const dialogVisible = ref(false)
const dialogTitle = ref('净需求解释')
const componentLoading = ref(false)
const loadErrorMessage = ref('')
const requestToken = ref(0)
const componentList = ref<MrpResultComponentVO[]>([])
const currentResult = ref<MrpResultVO>()

const componentEmptyText = computed(() => {
  if (loadErrorMessage.value) {
    return '解释明细加载失败'
  }
  return '暂无净需求解释明细'
})

const formatQty = (value?: number) => Number(value ?? 0).toFixed(2)

const formatTraceValue = (value?: string | number | null) => {
  if (value === undefined || value === null || value === '') {
    return '-'
  }
  return String(value)
}

const formatTraceLevel = (value?: number | null) => {
  if (value === undefined || value === null) {
    return '-'
  }
  return value === 0 ? '第 0 层' : `第 ${value} 层`
}

const getComponentRoleLabel = (role?: string) => {
  if (role === 'DEMAND_BASE') {
    return '需求基数'
  }
  if (role === 'DEMAND_ADJUST') {
    return '需求调整'
  }
  if (role === 'SUPPLY_CONSUME') {
    return '供给抵扣'
  }
  return role || '-'
}

const getSkipReasonLabel = (skipReason?: string) => {
  if (!skipReason) {
    return '-'
  }
  if (skipReason === 'MRP_DISABLED') {
    return '系统未参与'
  }
  if (skipReason === 'CUSTOMER_SUPPLIED_CUSTOMER_OWNED') {
    return '客供业务下客户供料不参与运算'
  }
  if (skipReason === 'TOLL_MANUFACTURING_DEFAULT_SKIP') {
    return '来料加工默认跳过非公司供料'
  }
  return skipReason
}

const isActiveRequest = (token: number) => token === requestToken.value && dialogVisible.value

const resetDialogState = () => {
  requestToken.value += 1
  componentLoading.value = false
  loadErrorMessage.value = ''
  componentList.value = []
  currentResult.value = undefined
}

const loadResultComponents = async (result: MrpResultVO, token: number) => {
  componentLoading.value = true
  loadErrorMessage.value = ''
  try {
    const data = await MrpPlanApi.getResultComponentList(result.id)
    if (!isActiveRequest(token)) {
      return
    }
    componentList.value = data
  } catch (error: any) {
    if (!isActiveRequest(token)) {
      return
    }
    loadErrorMessage.value = error?.message || '净需求解释加载失败，请重试'
  } finally {
    if (isActiveRequest(token)) {
      componentLoading.value = false
    }
  }
}

const reloadCurrentResult = async () => {
  if (!currentResult.value) {
    return
  }
  const token = requestToken.value
  await loadResultComponents(currentResult.value, token)
}

const open = async (result: MrpResultVO) => {
  const token = requestToken.value + 1
  requestToken.value = token
  currentResult.value = result
  componentList.value = []
  loadErrorMessage.value = ''
  dialogTitle.value = `净需求解释 - ${result.materialName || result.materialId}`
  dialogVisible.value = true
  await loadResultComponents(result, token)
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.result-explain-dialog {
  max-height: 70vh;
  overflow-y: auto;
}

.result-explain-dialog__trace-head {
  margin-bottom: 12px;
  color: #0f172a;
  font-size: 14px;
  font-weight: 600;
}

.result-explain-dialog__trace-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px 16px;
}

.result-explain-dialog__trace-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}

.result-explain-dialog__trace-item--wide {
  grid-column: 1 / -1;
}

.result-explain-dialog__trace-label {
  color: #64748b;
  font-size: 12px;
  font-weight: 600;
}

.result-explain-dialog__trace-value {
  color: #0f172a;
  font-size: 13px;
  line-height: 1.6;
  word-break: break-all;
}

.result-explain-dialog__trace-value--mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
}

@media (max-width: 768px) {
  .result-explain-dialog__trace-grid {
    grid-template-columns: 1fr;
  }
}
</style>
