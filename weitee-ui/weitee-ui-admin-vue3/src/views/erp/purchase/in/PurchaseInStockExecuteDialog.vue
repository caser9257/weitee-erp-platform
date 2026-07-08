<template>
  <Dialog v-model="dialogVisible" title="执行入库" width="min(1240px, 96vw)">
    <div v-loading="detailLoading || submitLoading" class="stock-execute-dialog">
      <el-empty v-if="!formData.id" description="未找到采购入库单数据" />
      <template v-else>
        <div class="stock-execute-dialog__summary">
          <el-tag :type="statusTagType">{{ statusLabel }}</el-tag>
          <span>入库单号：{{ formData.no || '-' }}</span>
          <span>合格数量：{{ erpCountInputFormatter(formData.qaPassCount || 0) }}</span>
          <span>已入库数量：{{ erpCountInputFormatter(formData.stockInCount || 0) }}</span>
          <span>剩余待入库：{{ erpCountInputFormatter(formData.remainingStockInCount || 0) }}</span>
        </div>

        <el-form label-width="88px" class="mt-12px">
          <el-form-item label="本次备注">
            <el-input
              v-model="remark"
              type="textarea"
              :rows="2"
              maxlength="255"
              show-word-limit
              placeholder="请输入本次入库备注"
            />
          </el-form-item>
        </el-form>

        <el-table :data="editableItems" border stripe class="mt-12px">
          <el-table-column label="产品" min-width="220">
            <template #default="{ row }">
              <div class="font-600">{{ row.productName || '-' }}</div>
              <div class="text-[var(--el-text-color-secondary)]">{{ row.productBarCode || '-' }}</div>
              <div class="stock-execute-dialog__source-batch">
                来源批次：{{ getPurchaseSourceBatchLabel(row.purchaseSourceBatchNo) }}
              </div>
              <div v-if="row.batchControlFlag" class="stock-execute-dialog__batch-flag">批次管理</div>
            </template>
          </el-table-column>
          <el-table-column label="合格数量" min-width="110" align="right">
            <template #default="{ row }">{{ erpCountInputFormatter(row.qaPassCount || 0) }}</template>
          </el-table-column>
          <el-table-column label="已入库数量" min-width="110" align="right">
            <template #default="{ row }">{{ erpCountInputFormatter(row.stockInCount || 0) }}</template>
          </el-table-column>
          <el-table-column label="剩余待入库" min-width="120" align="right">
            <template #default="{ row }">{{ erpCountInputFormatter(row.remainingStockInCount || 0) }}</template>
          </el-table-column>
          <el-table-column label="本次入库数量" min-width="180">
            <template #default="{ row }">
              <el-input-number
                v-model="row.executeCount"
                controls-position="right"
                :min="0"
                :max="Number(row.remainingStockInCount || 0)"
                :precision="3"
                class="!w-100%"
              />
            </template>
          </el-table-column>
          <el-table-column label="批次录入" min-width="220">
            <template #default="{ row, $index }">
              <div class="stock-execute-dialog__batch-cell">
                <el-button v-if="row.batchControlFlag" link type="primary" @click="openBatchDialog($index)">
                  批次录入
                </el-button>
                <span v-else class="text-[var(--el-text-color-secondary)]">非批次物料</span>
                <div v-if="row.batchControlFlag" class="text-[12px] leading-18px">
                  <span :class="isBatchCountBalanced(row) ? 'text-emerald-600' : 'text-rose-600'">
                    已录入 {{ row.batches.length }} 条 / 合计 {{ erpCountInputFormatter(getBatchTotalCount(row.batches)) }}
                  </span>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="明细备注" min-width="180">
            <template #default="{ row }">
              <el-input v-model="row.executeRemark" maxlength="255" placeholder="请输入明细备注" />
            </template>
          </el-table-column>
        </el-table>

        <el-alert
          v-if="batchMismatchCount > 0"
          class="mt-12px"
          type="warning"
          :closable="false"
          show-icon
          title="存在批次管理物料的批次数量合计与本次入库数量不一致，请先补齐批次后再确认。"
        />

        <ContentWrap title="入库执行记录" class="mt-16px">
          <el-table :data="formData.stockExecuteList || []" border stripe>
            <el-table-column label="执行单号" prop="no" min-width="160" />
            <el-table-column label="执行时间" min-width="160">
              <template #default="{ row }">
                {{ row.createTime ? formatDate(row.createTime, 'YYYY-MM-DD HH:mm:ss') : '-' }}
              </template>
            </el-table-column>
            <el-table-column label="执行人" prop="creatorName" min-width="120" />
            <el-table-column label="执行明细" min-width="320">
              <template #default="{ row }">
                <div class="stock-execute-dialog__record-list">
                  <div v-for="item in row.items || []" :key="item.id" class="stock-execute-dialog__record-item">
                    <div>{{ item.productName || '-' }} / {{ erpCountInputFormatter(item.count || 0) }}</div>
                    <div
                      v-for="batch in item.batches || []"
                      :key="batch.id"
                      class="text-[12px] text-[var(--el-text-color-secondary)]"
                    >
                      批次 {{ batch.batchNo || '-' }} / 来源批次
                      {{ getPurchaseSourceBatchLabel(batch.purchaseSourceBatchNo) }} /
                      {{ erpCountInputFormatter(batch.count || 0) }}
                    </div>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="备注" prop="remark" min-width="160" />
          </el-table>
        </ContentWrap>
      </template>
    </div>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :disabled="submitDisabled" :loading="submitLoading" @click="handleSubmit">
        确认入库
      </el-button>
    </template>
  </Dialog>

  <Dialog v-model="batchDialogVisible" title="批次录入" width="min(920px, 96vw)">
    <div v-loading="batchDialogLoading">
      <el-empty v-if="!currentBatchItem" description="未找到当前物料" />
      <template v-else>
        <el-descriptions :column="2" border class="mb-12px">
          <el-descriptions-item label="物料">{{ currentBatchItem.productName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="本次入库数量">
            {{ erpCountInputFormatter(currentBatchItem.executeCount || 0) }}
          </el-descriptions-item>
          <el-descriptions-item label="条码">{{ currentBatchItem.productBarCode || '-' }}</el-descriptions-item>
          <el-descriptions-item label="采购来源批次">
            {{ getPurchaseSourceBatchLabel(currentBatchItem.purchaseSourceBatchNo) }}
          </el-descriptions-item>
          <el-descriptions-item label="批次合计">
            <span :class="currentBatchBalanced ? 'text-emerald-600' : 'text-rose-600'">
              {{ erpCountInputFormatter(getBatchTotalCount(batchDraftRows)) }}
            </span>
          </el-descriptions-item>
        </el-descriptions>

        <div class="stock-execute-dialog__batch-toolbar">
          <el-button @click="addBatchRow">新增批次</el-button>
          <span class="text-[12px] text-[var(--el-text-color-secondary)]">
            批次数量合计必须等于本次入库数量
          </span>
        </div>

        <el-table :data="batchDraftRows" border stripe class="mt-12px">
          <el-table-column label="批次号" min-width="150">
            <template #default="{ row }">
              <el-input v-model="row.batchNo" placeholder="请输入批次号" />
            </template>
          </el-table-column>
          <el-table-column label="入库时间" min-width="180">
            <template #default="{ row }">
              <el-date-picker
                v-model="row.inboundTime"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                format="YYYY-MM-DD HH:mm:ss"
                class="!w-100%"
                placeholder="请选择入库时间"
              />
            </template>
          </el-table-column>
          <el-table-column label="生产日期" min-width="150">
            <template #default="{ row }">
              <el-date-picker
                v-model="row.produceDate"
                type="date"
                value-format="YYYY-MM-DD"
                format="YYYY-MM-DD"
                class="!w-100%"
                placeholder="请选择生产日期"
              />
            </template>
          </el-table-column>
          <el-table-column label="失效日期" min-width="150">
            <template #default="{ row }">
              <el-date-picker
                v-model="row.expireDate"
                type="date"
                value-format="YYYY-MM-DD"
                format="YYYY-MM-DD"
                class="!w-100%"
                placeholder="请选择失效日期"
              />
            </template>
          </el-table-column>
          <el-table-column label="批次数量" min-width="140">
            <template #default="{ row }">
              <el-input-number
                v-model="row.count"
                controls-position="right"
                :min="0"
                :precision="3"
                class="!w-100%"
              />
            </template>
          </el-table-column>
          <el-table-column label="批次备注" min-width="180">
            <template #default="{ row }">
              <el-input v-model="row.remark" maxlength="255" placeholder="请输入批次备注" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="72" align="center" fixed="right">
            <template #default="{ $index }">
              <el-button link type="danger" @click="removeBatchRow($index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>
    </div>
    <template #footer>
      <el-button @click="batchDialogVisible = false">取消</el-button>
      <el-button type="primary" :disabled="!currentBatchBalanced" @click="saveBatchDialog">
        确认批次
      </el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { formatDate } from '@/utils/formatTime'
import { erpCountInputFormatter } from '@/utils'
import {
  PurchaseInApi,
  PurchaseInBatchVO,
  PurchaseInItemVO,
  PurchaseInStockExecuteCreateBatchVO,
  PurchaseInVO
} from '@/api/erp/purchase/in'

defineOptions({ name: 'PurchaseInStockExecuteDialog' })

type EditableBatch = PurchaseInStockExecuteCreateBatchVO & { rowKey: string }
type EditableItem = PurchaseInItemVO & {
  executeCount: number
  executeRemark?: string
  batches: EditableBatch[]
}

const emit = defineEmits(['success'])
const message = useMessage()

const dialogVisible = ref(false)
const detailLoading = ref(false)
const submitLoading = ref(false)
const batchDialogVisible = ref(false)
const batchDialogLoading = ref(false)
const currentBatchIndex = ref<number | null>(null)
const batchDraftRows = ref<EditableBatch[]>([])

const formData = ref<PurchaseInVO>({ items: [] } as PurchaseInVO)
const editableItems = ref<EditableItem[]>([])
const remark = ref('')

const currentBatchItem = computed(() =>
  currentBatchIndex.value === null ? null : editableItems.value[currentBatchIndex.value]
)

const statusLabel = computed(() => {
  if (formData.value.stockInStatus === 20) return '已入库'
  if (formData.value.stockInStatus === 15) return '部分入库'
  if (formData.value.stockInStatus === 30) return '无需入库'
  return '待入库'
})

const statusTagType = computed(() => {
  if (formData.value.stockInStatus === 20) return 'success'
  if (formData.value.stockInStatus === 15) return 'warning'
  if (formData.value.stockInStatus === 30) return 'info'
  return 'warning'
})

const batchMismatchCount = computed(() => {
  return editableItems.value.filter((item) => {
    return Number(item.executeCount || 0) > 0 && item.batchControlFlag && !isBatchCountBalanced(item)
  }).length
})

const submitDisabled = computed(() => {
  if (submitLoading.value) return true
  if (!editableItems.value.some((item) => Number(item.executeCount || 0) > 0)) return true
  return batchMismatchCount.value > 0
})

const currentBatchBalanced = computed(() => {
  if (!currentBatchItem.value) return false
  return getSafeCount(getBatchTotalCount(batchDraftRows.value)) === getSafeCount(Number(currentBatchItem.value.executeCount || 0))
})

const createBatchRow = (): EditableBatch => ({
  rowKey: `${Date.now()}-${Math.random()}`,
  batchNo: '',
  count: 0,
  inboundTime: formatDate(new Date(), 'YYYY-MM-DD HH:mm:ss'),
  produceDate: undefined,
  expireDate: undefined,
  remark: ''
})

const getSafeCount = (value?: number) => Number(Number(value || 0).toFixed(3))

const getPurchaseSourceBatchLabel = (value?: string) => value?.trim() || '未关联来源批次'

const getBatchTotalCount = (batches: Array<PurchaseInBatchVO | EditableBatch> = []) => {
  return batches.reduce((sum, item) => sum + Number(item.count || 0), 0)
}

const isBatchCountBalanced = (item: EditableItem) => {
  if (!item.batchControlFlag || Number(item.executeCount || 0) <= 0) return true
  return getSafeCount(getBatchTotalCount(item.batches)) === getSafeCount(Number(item.executeCount || 0))
}

const buildEditableItems = (items: PurchaseInItemVO[] = []) => {
  editableItems.value = items.map((item) => ({
    ...item,
    executeCount: 0,
    executeRemark: '',
    batches: []
  }))
}

const loadDetail = async (id: number) => {
  detailLoading.value = true
  try {
    const data = await PurchaseInApi.getPurchaseIn(id)
    formData.value = data
    remark.value = ''
    buildEditableItems(data.items || [])
  } finally {
    detailLoading.value = false
  }
}

const open = async (id: number) => {
  dialogVisible.value = true
  await loadDetail(id)
}

const openBatchDialog = (index: number) => {
  const row = editableItems.value[index]
  if (!row.batchControlFlag) return
  if (Number(row.executeCount || 0) <= 0) {
    message.warning('请先填写本次入库数量，再录入批次')
    return
  }
  currentBatchIndex.value = index
  batchDialogVisible.value = true
  batchDialogLoading.value = true
  try {
    batchDraftRows.value =
      row.batches.length > 0
        ? row.batches.map((batch) => ({ ...batch, rowKey: `${Date.now()}-${Math.random()}` }))
        : [createBatchRow()]
  } finally {
    batchDialogLoading.value = false
  }
}

const addBatchRow = () => {
  batchDraftRows.value.push(createBatchRow())
}

const removeBatchRow = (index: number) => {
  batchDraftRows.value.splice(index, 1)
  if (!batchDraftRows.value.length) {
    batchDraftRows.value.push(createBatchRow())
  }
}

const saveBatchDialog = () => {
  if (currentBatchIndex.value === null || !currentBatchItem.value) return
  const hasInvalidRow = batchDraftRows.value.some((row) => {
    return !row.batchNo?.trim() || !row.inboundTime || Number(row.count || 0) <= 0
  })
  if (hasInvalidRow) {
    message.warning('请完整填写批次号、入库时间和批次数量')
    return
  }
  if (!currentBatchBalanced.value) {
    message.warning('批次数量合计必须等于本次入库数量')
    return
  }
  editableItems.value[currentBatchIndex.value].batches = batchDraftRows.value.map(({ rowKey, ...row }) => ({
    rowKey,
    ...row,
    batchNo: row.batchNo.trim()
  }))
  batchDialogVisible.value = false
}

const handleSubmit = async () => {
  if (!formData.value.id) return
  const items = editableItems.value
    .filter((item) => Number(item.executeCount || 0) > 0)
    .map((item) => ({
      purchaseInItemId: Number(item.id),
      count: Number(item.executeCount),
      remark: item.executeRemark?.trim(),
      batches: item.batchControlFlag
        ? item.batches.map(({ rowKey, ...batch }) => ({
            ...batch,
            count: Number(batch.count || 0)
          }))
        : undefined
    }))
  if (!items.length) {
    message.warning('请至少填写一条本次入库数量')
    return
  }
  if (batchMismatchCount.value > 0) {
    message.warning('请先补齐批次管理物料的批次录入')
    return
  }
  submitLoading.value = true
  try {
    await PurchaseInApi.createStockExecute({
      purchaseInId: Number(formData.value.id),
      remark: remark.value.trim(),
      items
    })
    message.success('执行入库成功')
    dialogVisible.value = false
    emit('success')
  } finally {
    submitLoading.value = false
  }
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.stock-execute-dialog {
  &__summary {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    align-items: center;
  }

  &__batch-flag {
    display: inline-flex;
    margin-top: 4px;
    padding: 2px 8px;
    border-radius: 999px;
    font-size: 12px;
    color: #1d4ed8;
    background: #eff6ff;
  }

  &__source-batch {
    margin-top: 4px;
    color: #475569;
    font-size: 12px;
    line-height: 18px;
    word-break: break-word;
  }

  &__batch-cell {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  &__batch-toolbar {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    align-items: center;
    justify-content: space-between;
  }

  &__record-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  &__record-item {
    padding-bottom: 4px;
    border-bottom: 1px dashed var(--el-border-color-light);
  }
}

@media (max-width: 768px) {
  .stock-execute-dialog {
    &__summary,
    &__batch-toolbar {
      align-items: flex-start;
      flex-direction: column;
    }
  }
}
</style>
