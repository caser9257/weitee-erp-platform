<template>
  <Dialog v-model="dialogVisible" title="生产退料" width="min(1000px, 96vw)">
    <div v-loading="detailLoading || submitLoading">
      <el-empty v-if="!currentMaterial" description="未找到工单物料" />
      <template v-else>
        <el-form label-width="96px" class="return-dialog__form">
          <el-row :gutter="16">
            <el-col :xs="24" :md="12">
              <el-form-item label="物料">
                <div class="return-dialog__readonly">
                  {{ currentMaterial.materialName || '-' }}
                  <span class="return-dialog__sub">{{ currentMaterial.materialCode || '-' }}</span>
                </div>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12">
              <el-form-item label="退料备注">
                <el-input v-model="remark" maxlength="255" placeholder="请输入退料备注" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>

        <div class="return-dialog__toolbar">
          <span>批次合计：{{ erpCountInputFormatter(totalReturnQty) }}</span>
          <span :class="isBalanced ? 'text-emerald-600' : 'text-rose-600'">
            {{ isBalanced ? '已匹配本次退料数量' : '请至少录入一条退料批次' }}
          </span>
        </div>

        <el-table :data="batchRows" border stripe>
          <el-table-column label="批次号" min-width="150" prop="batchNo" />
          <el-table-column label="已领数量" min-width="120" align="right">
            <template #default="{ row }">{{ erpCountInputFormatter(row.issuedQty || 0) }}</template>
          </el-table-column>
          <el-table-column label="已退数量" min-width="120" align="right">
            <template #default="{ row }">{{ erpCountInputFormatter(row.returnedQty || 0) }}</template>
          </el-table-column>
          <el-table-column label="可退数量" min-width="120" align="right">
            <template #default="{ row }">{{ erpCountInputFormatter(row.returnableQty || 0) }}</template>
          </el-table-column>
          <el-table-column label="本次退料数量" min-width="180">
            <template #default="{ row }">
              <el-input-number
                v-model="row.returnQty"
                controls-position="right"
                :min="0"
                :max="Number(row.returnableQty || 0)"
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
        确认退料
      </el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { erpCountInputFormatter } from '@/utils'
import { ProductionMaterialVO } from '@/api/erp/mrp/production-material'
import {
  ProductionReturnApi,
  ProductionReturnableBatchVO
} from '@/api/erp/manufacturing/material-return'

defineOptions({ name: 'ReturnBatchDialog' })

type ReturnBatchRow = ProductionReturnableBatchVO & { returnQty?: number }

const emit = defineEmits<{
  success: []
}>()

const message = useMessage()

const dialogVisible = ref(false)
const detailLoading = ref(false)
const submitLoading = ref(false)
const currentMaterial = ref<ProductionMaterialVO>()
const currentOrderId = ref<number>()
const batchRows = ref<ReturnBatchRow[]>([])
const remark = ref('')

const totalReturnQty = computed(() =>
  batchRows.value.reduce((sum, item) => sum + Number(item.returnQty || 0), 0)
)

const isBalanced = computed(() => totalReturnQty.value > 0)

const canSubmit = computed(() => {
  return !submitLoading.value && !!currentMaterial.value && !!currentOrderId.value && isBalanced.value
})

const open = async (material: ProductionMaterialVO, orderId: number) => {
  currentMaterial.value = material
  currentOrderId.value = orderId
  remark.value = ''
  dialogVisible.value = true
  detailLoading.value = true
  try {
    const data = await ProductionReturnApi.getReturnableBatches(material.id)
    batchRows.value = (data.returnableBatches || []).map((item) => ({
      ...item,
      returnQty: 0
    }))
  } finally {
    detailLoading.value = false
  }
}

const handleSubmit = async () => {
  if (!currentMaterial.value || !currentOrderId.value) return
  const selectedRows = batchRows.value.filter((item) => Number(item.returnQty || 0) > 0)
  if (!selectedRows.length) {
    message.warning('请至少填写一条退料批次数量')
    return
  }
  const selectedWarehouseIds = Array.from(new Set(selectedRows.map((item) => item.warehouseId).filter(Boolean)))
  if (selectedWarehouseIds.length !== 1) {
    message.warning('一期退料只允许退回同一仓库的原领料批次')
    return
  }
  const batches = selectedRows.map((item) => ({
    issueBatchId: item.issueBatchId,
    stockBatchId: item.stockBatchId,
    batchNo: item.batchNo,
    returnQty: Number(item.returnQty || 0)
  }))
  const returnQty = batches.reduce((sum, item) => sum + Number(item.returnQty || 0), 0)
  submitLoading.value = true
  try {
    await ProductionReturnApi.create({
      productionOrderId: currentOrderId.value,
      remark: remark.value.trim(),
      items: [
        {
          productionMaterialId: currentMaterial.value.id,
          materialId: currentMaterial.value.materialId,
          warehouseId: Number(selectedWarehouseIds[0]),
          returnQty,
          remark: remark.value.trim(),
          batches
        }
      ]
    })
    message.success('生产退料成功')
    dialogVisible.value = false
    emit('success')
  } finally {
    submitLoading.value = false
  }
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.return-dialog {
  &__readonly {
    min-height: 32px;
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 8px;
  }

  &__sub {
    color: var(--el-text-color-secondary);
    font-size: 12px;
  }

  &__toolbar {
    display: flex;
    gap: 12px;
    flex-wrap: wrap;
    margin-bottom: 12px;
  }
}
</style>
