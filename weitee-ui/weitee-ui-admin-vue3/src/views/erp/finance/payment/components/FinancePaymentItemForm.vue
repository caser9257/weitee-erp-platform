<template>
  <div class="finance-payment-item">
    <el-table
      :data="formData"
      row-key="apStatementId"
      show-summary
      empty-text="暂无应付记录"
      :summary-method="getSummaries"
      class="finance-payment-item__table"
    >
      <el-table-column label="序号" type="index" align="center" width="60" />
      <el-table-column label="台账编号" min-width="180">
        <template #default="{ row }">
          <span>{{ row.statementNo || `#${row.apStatementId}` }}</span>
        </template>
      </el-table-column>
      <el-table-column label="业务类型" width="110" align="center">
        <template #default="{ row }">
          {{ getBizTypeLabel(row.bizType) }}
        </template>
      </el-table-column>
      <el-table-column label="业务单号" min-width="140">
        <template #default="{ row }">
          <span>{{ row.bizNo || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="来源采购单" min-width="150">
        <template #default="{ row }">
          <span>{{ row.sourceOrderNo || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="应付金额" prop="totalPrice" min-width="120" align="right" class-name="font-mono">
        <template #default="{ row }">
          {{ erpPriceInputFormatter(row.totalPrice) }}
        </template>
      </el-table-column>
      <el-table-column label="已核销金额" prop="paidPrice" min-width="130" align="right" class-name="font-mono">
        <template #default="{ row }">
          {{ erpPriceInputFormatter(row.paidPrice) }}
        </template>
      </el-table-column>
      <el-table-column label="剩余金额" prop="remainAmount" min-width="120" align="right" class-name="font-mono">
        <template #default="{ row }">
          {{ erpPriceInputFormatter(row.remainAmount) }}
        </template>
      </el-table-column>
      <el-table-column label="本次付款" prop="paymentPrice" min-width="150" align="right">
        <template #default="{ row, $index }">
          <el-input-number
            v-model="row.paymentPrice"
            controls-position="right"
            :precision="2"
            :min="0.01"
            :max="getMaxPaymentPrice(row)"
            :disabled="disabled"
            class="!w-full"
          />
          <div v-if="validationErrors[$index]" class="finance-payment-item__error">
            {{ validationErrors[$index] }}
          </div>
        </template>
      </el-table-column>
      <el-table-column label="备注" min-width="180">
        <template #default="{ row }">
          <el-input v-model="row.remark" placeholder="请输入备注" :disabled="disabled" />
        </template>
      </el-table-column>
      <el-table-column v-if="!disabled" label="操作" width="88" align="center" fixed="right">
        <template #default="{ $index }">
          <el-button link type="danger" @click="handleDelete($index)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="!disabled" class="finance-payment-item__toolbar">
      <el-button type="primary" plain round @click="handleOpenSelector">
        <Icon icon="ep:plus" class="mr-5px" />
        添加应付记录
      </el-button>
    </div>

    <ApStatementPaymentEnableList ref="paymentEnableListRef" @success="handleAddStatements" />
  </div>
</template>

<script setup lang="ts">
import type { SummaryMethod } from 'element-plus'
import { ApStatementPaymentEnableVO, ERP_AP_BIZ_TYPE_OPTIONS } from '@/api/erp/finance/apar'
import ApStatementPaymentEnableList from '@/views/erp/finance/apar/ApStatementPaymentEnableList.vue'
import { erpPriceInputFormatter, getSumValue } from '@/utils'

interface PaymentAllocateRow {
  id?: number
  apStatementId?: number
  bizType?: number
  bizId?: number
  bizNo?: string
  statementNo?: string
  sourceOrderNo?: string
  totalPrice?: number
  paidPrice?: number
  remainAmount?: number
  paymentPrice?: number
  remark?: string
}

const props = defineProps<{
  items: PaymentAllocateRow[]
  supplierId?: number
  supplierName?: string
  disabled: boolean
}>()

const message = useMessage()

const formData = ref<PaymentAllocateRow[]>([])
const validationErrors = ref<Record<number, string>>({})
const paymentEnableListRef = ref()

watch(
  () => props.items,
  (val) => {
    formData.value = val || []
    validationErrors.value = {}
  },
  { immediate: true }
)

const getBizTypeLabel = (value?: number) =>
  ERP_AP_BIZ_TYPE_OPTIONS.find((item) => item.value === value)?.label || '-'

const getMaxPaymentPrice = (row: PaymentAllocateRow) =>
  Math.max(Math.abs(Number(row.remainAmount || 0)), 0.01)

const getSummaries: SummaryMethod<PaymentAllocateRow> = (param) => {
  const { columns, data } = param
  const sums: string[] = []
  columns.forEach((column, index) => {
    if (index === 0) {
      sums[index] = '合计'
      return
    }
    if (['totalPrice', 'paidPrice', 'remainAmount', 'paymentPrice'].includes(column.property)) {
      const sum = getSumValue(data.map((item) => Number(item[column.property] || 0)))
      sums[index] = erpPriceInputFormatter(sum)
      return
    }
    sums[index] = ''
  })
  return sums
}

const handleOpenSelector = () => {
  if (!props.supplierId) {
    message.error('请先选择供应商后再添加应付记录')
    return
  }
  paymentEnableListRef.value?.open(props.supplierId, props.supplierName)
}

const handleAddStatements = (rows: ApStatementPaymentEnableVO[]) => {
  const existingIds = new Set(formData.value.map((item) => item.apStatementId))
  const appendRows = rows.filter((row) => !existingIds.has(row.id))
  if (!appendRows.length) {
    message.warning('所选应付记录已全部存在')
    return
  }
  appendRows.forEach((row) => {
    formData.value.push({
      apStatementId: row.id,
      bizType: row.bizType,
      bizId: row.bizId,
      bizNo: row.bizNo,
      statementNo: row.statementNo,
      sourceOrderNo: row.sourceOrderNo,
      totalPrice: row.amount,
      paidPrice: row.paidAmount,
      remainAmount: row.remainAmount,
      paymentPrice: Math.abs(Number(row.remainAmount || 0)),
      remark: ''
    })
  })
}

const handleDelete = (index: number) => {
  formData.value.splice(index, 1)
  validationErrors.value = {}
}

const validate = async () => {
  validationErrors.value = {}
  if (!formData.value.length) {
    message.error('请至少添加一条应付记录')
    throw new Error('payment items empty')
  }

  formData.value.forEach((item, index) => {
    const paymentPrice = Number(item.paymentPrice || 0)
    const remainAmount = Math.abs(Number(item.remainAmount || 0))
    if (!item.apStatementId) {
      validationErrors.value[index] = '缺少应付台账编号'
      return
    }
    if (!item.bizType || !item.bizId) {
      validationErrors.value[index] = '缺少业务关联信息'
      return
    }
    if (paymentPrice <= 0) {
      validationErrors.value[index] = '本次付款必须大于 0'
      return
    }
    if (paymentPrice > remainAmount) {
      validationErrors.value[index] = '本次付款不能大于剩余金额'
    }
  })

  if (Object.keys(validationErrors.value).length) {
    message.error('请先修正应付记录中的校验问题')
    throw new Error('payment items invalid')
  }
}

defineExpose({ validate })
</script>

<style scoped>
.finance-payment-item {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.finance-payment-item__table {
  margin-top: -10px;
}

.finance-payment-item__toolbar {
  display: flex;
  justify-content: center;
}

.finance-payment-item__error {
  margin-top: 6px;
  color: #e11d48;
  font-size: 12px;
  line-height: 1.4;
}
</style>
