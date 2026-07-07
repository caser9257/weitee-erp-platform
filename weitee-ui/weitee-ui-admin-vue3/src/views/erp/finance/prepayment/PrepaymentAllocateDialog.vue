<template>
  <el-dialog
    v-model="dialogVisible"
    title="预付款核销"
    width="880px"
    destroy-on-close
    class="finance-prepayment-allocate-dialog"
  >
    <div v-if="prepayment" class="finance-prepayment-allocate-dialog__summary">
      <span>{{ prepayment.no || '-' }}</span>
      <span>{{ prepayment.supplierName || '-' }}</span>
      <strong>剩余 {{ formatAmount(prepayment.remainPrice) }}</strong>
    </div>

    <div class="finance-prepayment-allocate-dialog__toolbar">
      <el-button :loading="statementLoading" @click="loadStatementOptions">刷新台账</el-button>
      <el-button type="primary" @click="addItem">新增核销</el-button>
    </div>

    <el-table :data="items" border class="finance-prepayment-allocate-dialog__table">
      <el-table-column label="应付台账" min-width="260">
        <template #default="{ row }">
          <el-select v-model="row.apStatementId" filterable placeholder="请选择应付台账">
            <el-option
              v-for="item in statementOptions"
              :key="item.id"
              :label="`${item.statementNo || '-'} / ${item.bizNo || '-'}`"
              :value="item.id"
            />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="可核销金额" width="140" align="right">
        <template #default="{ row }">
          <span class="finance-prepayment-allocate-dialog__amount">{{
            formatAmount(resolveRemainAmount(row.apStatementId))
          }}</span>
        </template>
      </el-table-column>
      <el-table-column label="核销金额" width="150" align="right">
        <template #default="{ row }">
          <el-input-number
            v-model="row.allocateAmount"
            :min="0.01"
            :precision="2"
            :controls="false"
            class="w-100%"
          />
        </template>
      </el-table-column>
      <el-table-column label="备注" min-width="160">
        <template #default="{ row }">
          <el-input v-model="row.remark" maxlength="100" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="80" align="center">
        <template #default="{ $index }">
          <el-button link type="danger" @click="removeItem($index)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitLoading" @click="submitForm">核销</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  ApStatementApi,
  type ApStatementPaymentEnableVO
} from '@/api/erp/finance/apar'
import {
  FinancePrepaymentApi,
  type ErpFinancePrepaymentVO
} from '@/api/erp/finance/prepayment'
import { formatAmount } from '@/views/erp/finance/shared/accounting'

defineOptions({ name: 'ErpFinancePrepaymentAllocateDialog' })

interface AllocateItemForm {
  apStatementId?: number
  allocateAmount?: number
  remark?: string
}

const emit = defineEmits<{
  success: []
}>()

const dialogVisible = ref(false)
const submitLoading = ref(false)
const statementLoading = ref(false)
const prepayment = ref<ErpFinancePrepaymentVO>()
const statementOptions = ref<ApStatementPaymentEnableVO[]>([])
const items = ref<AllocateItemForm[]>([])

const createDefaultItem = (): AllocateItemForm => ({
  allocateAmount: undefined,
  remark: ''
})

const open = async (row: ErpFinancePrepaymentVO) => {
  prepayment.value = row
  items.value = [createDefaultItem()]
  dialogVisible.value = true
  await loadStatementOptions()
}

const loadStatementOptions = async () => {
  if (!prepayment.value?.supplierId) {
    statementOptions.value = []
    return
  }
  statementLoading.value = true
  try {
    const data = await ApStatementApi.getPaymentEnablePage({
      pageNo: 1,
      pageSize: 100,
      supplierId: prepayment.value.supplierId
    })
    statementOptions.value = data?.list || []
  } finally {
    statementLoading.value = false
  }
}

const resolveRemainAmount = (statementId?: number) => {
  return Number(statementOptions.value.find((item) => item.id === statementId)?.remainAmount || 0)
}

const addItem = () => {
  items.value.push(createDefaultItem())
}

const removeItem = (index: number) => {
  if (items.value.length === 1) {
    items.value = [createDefaultItem()]
    return
  }
  items.value.splice(index, 1)
}

const validateItems = () =>
  items.value.every((item) => item.apStatementId && Number(item.allocateAmount || 0) > 0)

const submitForm = async () => {
  if (!prepayment.value?.id) {
    return
  }
  if (!validateItems()) {
    ElMessage.warning('请完善核销明细')
    return
  }
  submitLoading.value = true
  try {
    await FinancePrepaymentApi.allocateFinancePrepayment({
      prepaymentId: prepayment.value.id,
      items: items.value.map((item) => ({
        apStatementId: Number(item.apStatementId),
        allocateAmount: Number(item.allocateAmount),
        remark: item.remark
      }))
    })
    ElMessage.success('核销成功')
    dialogVisible.value = false
    emit('success')
  } finally {
    submitLoading.value = false
  }
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.finance-prepayment-allocate-dialog__summary,
.finance-prepayment-allocate-dialog__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--erp-space-3);
  margin-bottom: var(--erp-space-3);
}

.finance-prepayment-allocate-dialog__summary {
  padding: var(--erp-space-3);
  background: var(--erp-slate-50);
  border: 1px solid var(--erp-slate-100);
  border-radius: var(--erp-radius-lg);
  color: var(--erp-slate-700);

  strong {
    color: var(--erp-danger-600);
  }
}

.finance-prepayment-allocate-dialog__table {
  :deep(.el-input-number .el-input__inner) {
    text-align: right;
  }
}

.finance-prepayment-allocate-dialog__amount {
  font-family: var(--erp-font-mono);
  color: var(--erp-slate-900);
  font-weight: 700;
}

@media (max-width: 768px) {
  .finance-prepayment-allocate-dialog__summary,
  .finance-prepayment-allocate-dialog__toolbar {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>
