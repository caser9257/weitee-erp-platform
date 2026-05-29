<template>
  <Dialog v-model="dialogVisible" title="生产领料" width="min(1080px, 96vw)">
    <div v-loading="candidateLoading || recommendLoading || submitLoading">
      <el-empty v-if="!currentMaterial" description="未找到工单物料" />
      <template v-else>
        <el-form label-width="96px" class="issue-dialog__form">
          <el-row :gutter="16">
            <el-col :xs="24" :md="12">
              <el-form-item label="物料">
                <div class="issue-dialog__readonly">
                  {{ currentMaterial.materialName || '-' }}
                  <span class="issue-dialog__sub">{{ currentMaterial.materialCode || '-' }}</span>
                </div>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12">
              <el-form-item label="默认仓库">
                <el-select
                  v-model="warehouseId"
                  filterable
                  clearable
                  class="!w-100%"
                  placeholder="请选择仓库"
                  @change="handleWarehouseChange"
                >
                  <el-option
                    v-for="warehouse in warehouseList"
                    :key="warehouse.id"
                    :label="warehouse.name"
                    :value="warehouse.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12">
              <el-form-item label="应领数量">
                <div class="issue-dialog__readonly">{{ erpCountInputFormatter(currentMaterial.requiredQty || 0) }}</div>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12">
              <el-form-item label="待领数量">
                <div class="issue-dialog__readonly">
                  {{ erpCountInputFormatter(currentMaterial.remainingIssueQty || 0) }}
                </div>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12">
              <el-form-item label="本次领料">
                <el-input-number
                  v-model="issueQty"
                  controls-position="right"
                  :min="0"
                  :precision="3"
                  :max="Number(currentMaterial.remainingIssueQty || 0)"
                  class="!w-100%"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12">
              <el-form-item label="领料备注">
                <el-input v-model="remark" maxlength="255" placeholder="请输入领料备注" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>

        <div class="issue-dialog__toolbar">
          <div class="issue-dialog__toolbar-summary">
            <span>批次合计：{{ erpCountInputFormatter(totalAssignedQty) }}</span>
            <span :class="isBalanced ? 'text-emerald-600' : 'text-rose-600'">
              {{ isBalanced ? '已匹配本次领料数量' : '批次数量合计必须等于本次领料数量' }}
            </span>
          </div>
          <div class="issue-dialog__toolbar-actions">
            <el-button @click="handleResetAllocations">重置分配</el-button>
            <el-button type="primary" plain :disabled="!canRecommend" @click="handleRecommend">
              自动推荐
            </el-button>
          </div>
        </div>

        <el-table :data="batchRows" border stripe>
          <el-table-column label="批次号" min-width="150" prop="batchNo" />
          <el-table-column label="入库时间" min-width="160">
            <template #default="{ row }">
              {{ row.inboundTime ? formatDate(row.inboundTime, 'YYYY-MM-DD HH:mm:ss') : '-' }}
            </template>
          </el-table-column>
          <el-table-column label="生产日期" min-width="130" prop="produceDate" />
          <el-table-column label="失效日期" min-width="130" prop="expireDate" />
          <el-table-column label="可用数量" min-width="120" align="right">
            <template #default="{ row }">{{ erpCountInputFormatter(row.availableQty || 0) }}</template>
          </el-table-column>
          <el-table-column label="推荐数量" min-width="120" align="right">
            <template #default="{ row }">{{ erpCountInputFormatter(row.recommendedQty || 0) }}</template>
          </el-table-column>
          <el-table-column label="本次领料数量" min-width="180">
            <template #default="{ row }">
              <el-input-number
                v-model="row.issueQty"
                controls-position="right"
                :min="0"
                :max="Number(row.availableQty || 0)"
                :precision="3"
                class="!w-100%"
              />
            </template>
          </el-table-column>
        </el-table>
      </template>
    </div>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :disabled="!canSubmit" :loading="submitLoading" @click="handleSubmit">
        确认领料
      </el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { formatDate } from '@/utils/formatTime'
import { erpCountInputFormatter } from '@/utils'
import { WarehouseApi, WarehouseVO } from '@/api/erp/stock/warehouse'
import {
  ProductionMaterialApi,
  ProductionMaterialBatchCandidateVO,
  ProductionMaterialVO
} from '@/api/erp/mrp/production-material'
import { ProductionIssueApi } from '@/api/erp/manufacturing/material-issue'

defineOptions({ name: 'IssueBatchDialog' })

type IssueBatchRow = ProductionMaterialBatchCandidateVO & {
  recommendedQty?: number
  issueQty?: number
}

const emit = defineEmits<{
  success: [issueId: number]
}>()

const message = useMessage()

const dialogVisible = ref(false)
const candidateLoading = ref(false)
const recommendLoading = ref(false)
const submitLoading = ref(false)
const warehouseList = ref<WarehouseVO[]>([])
const currentMaterial = ref<ProductionMaterialVO>()
const currentOrderId = ref<number>()
const warehouseId = ref<number>()
const issueQty = ref<number>(0)
const remark = ref('')
const batchRows = ref<IssueBatchRow[]>([])

const totalAssignedQty = computed(() =>
  batchRows.value.reduce((sum, row) => sum + Number(row.issueQty || 0), 0)
)

const isBalanced = computed(() => Number(totalAssignedQty.value.toFixed(3)) === Number((issueQty.value || 0).toFixed(3)))

const canRecommend = computed(() => Number(issueQty.value || 0) > 0 && Number(warehouseId.value || 0) > 0)

const canSubmit = computed(() => {
  if (submitLoading.value || !currentMaterial.value || !currentOrderId.value) return false
  if (!warehouseId.value || Number(issueQty.value || 0) <= 0) return false
  return isBalanced.value
})

const ensureWarehouseList = async () => {
  if (warehouseList.value.length > 0) return
  warehouseList.value = await WarehouseApi.getWarehouseSimpleList()
}

const loadBatchCandidates = async () => {
  if (!currentMaterial.value || !warehouseId.value) {
    batchRows.value = []
    return
  }
  candidateLoading.value = true
  try {
    const data = await ProductionMaterialApi.getBatchCandidates(currentMaterial.value.id, warehouseId.value)
    batchRows.value = (data.batchCandidates || []).map((item) => ({
      ...item,
      recommendedQty: 0,
      issueQty: 0
    }))
  } finally {
    candidateLoading.value = false
  }
}

const open = async (material: ProductionMaterialVO, orderId: number) => {
  await ensureWarehouseList()
  currentMaterial.value = material
  currentOrderId.value = orderId
  warehouseId.value = material.supplyWarehouseId
  issueQty.value = Number(material.remainingIssueQty || 0)
  remark.value = ''
  dialogVisible.value = true
  await loadBatchCandidates()
}

const handleWarehouseChange = async () => {
  await loadBatchCandidates()
}

const handleResetAllocations = () => {
  batchRows.value = batchRows.value.map((item) => ({
    ...item,
    recommendedQty: 0,
    issueQty: 0
  }))
}

const handleRecommend = async () => {
  if (!currentMaterial.value || !warehouseId.value || Number(issueQty.value || 0) <= 0) {
    message.warning('请先选择仓库并填写本次领料数量')
    return
  }
  recommendLoading.value = true
  try {
    const data = await ProductionIssueApi.recommend({
      productionMaterialId: currentMaterial.value.id,
      warehouseId: warehouseId.value,
      issueQty: Number(issueQty.value || 0)
    })
    batchRows.value = (data.batchAllocations || []).map((item) => ({
      ...item,
      issueQty: Number(item.recommendedQty || 0)
    }))
    if (Number(data.gapQty || 0) > 0) {
      message.warning(`可用批次不足，还差 ${erpCountInputFormatter(data.gapQty || 0)}`)
    }
  } finally {
    recommendLoading.value = false
  }
}

const handleSubmit = async () => {
  if (!currentMaterial.value || !currentOrderId.value || !warehouseId.value) return
  if (!isBalanced.value) {
    message.warning('批次数量合计必须等于本次领料数量')
    return
  }
  const batches = batchRows.value
    .filter((item) => Number(item.issueQty || 0) > 0)
    .map((item) => ({
      stockBatchId: item.stockBatchId,
      batchNo: item.batchNo,
      issueQty: Number(item.issueQty || 0)
    }))
  if (!batches.length) {
    message.warning('请至少填写一条领料批次')
    return
  }
  submitLoading.value = true
  try {
    const issueId = await ProductionIssueApi.create({
      productionOrderId: currentOrderId.value,
      remark: remark.value.trim(),
      items: [
        {
          productionMaterialId: currentMaterial.value.id,
          materialId: currentMaterial.value.materialId,
          warehouseId: warehouseId.value,
          issueQty: Number(issueQty.value || 0),
          remark: remark.value.trim(),
          batches
        }
      ]
    })
    message.success('生产领料成功')
    dialogVisible.value = false
    emit('success', issueId)
  } finally {
    submitLoading.value = false
  }
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.issue-dialog {
  &__readonly {
    min-height: 32px;
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 8px;
    color: var(--el-text-color-regular);
  }

  &__sub {
    color: var(--el-text-color-secondary);
    font-size: 12px;
  }

  &__toolbar {
    display: flex;
    justify-content: space-between;
    gap: 16px;
    margin-bottom: 12px;
    flex-wrap: wrap;
  }

  &__toolbar-summary,
  &__toolbar-actions {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    align-items: center;
  }
}
</style>
