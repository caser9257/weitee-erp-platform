<template>
  <el-form
    ref="formRef"
    :model="formData"
    :rules="formRules"
    v-loading="formLoading"
    label-width="0px"
    :inline-message="true"
    :disabled="disabled"
    class="invoice-item-form"
  >
    <el-table
      :data="formData"
      show-summary
      :summary-method="getSummaries"
      class="invoice-item-table"
      table-layout="fixed"
      :fit="false"
    >
      <el-table-column label="序号" type="index" align="center" width="50" />
      <el-table-column label="产品名称" min-width="200">
        <template #default="{ row, $index }">
          <el-form-item :prop="`${$index}.productId`" :rules="formRules.productId" class="mb-0px!">
            <el-select
              v-model="row.productId"
              filterable
              placeholder="请选择产品"
              class="!w-1/1"
              @change="(val) => handleProductChange(row, val)"
            >
              <el-option
                v-for="item in productList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              >
                <div class="flex items-center justify-between">
                  <span>{{ item.name }}</span>
                  <span class="text-gray-400 text-xs font-mono">{{ item.barCode || '-' }}</span>
                </div>
              </el-option>
            </el-select>
          </el-form-item>
        </template>
      </el-table-column>
      <el-table-column label="规格" width="120">
        <template #default="{ row }">
          <el-form-item class="mb-0px!">
            <el-input disabled v-model="row.productSpec" placeholder="-" />
          </el-form-item>
        </template>
      </el-table-column>
      <el-table-column label="单位" width="80" align="center">
        <template #default="{ row }">
          <el-form-item class="mb-0px!">
            <el-input disabled v-model="row.unit" placeholder="-" />
          </el-form-item>
        </template>
      </el-table-column>
      <el-table-column label="数量" width="110" align="right">
        <template #default="{ row, $index }">
          <el-form-item :prop="`${$index}.count`" :rules="formRules.count" class="mb-0px!">
            <el-input-number
              v-model="row.count"
              controls-position="right"
              :min="0.001"
              :precision="3"
              class="!w-100%"
            />
          </el-form-item>
        </template>
      </el-table-column>
      <el-table-column label="单价" width="120" align="right">
        <template #default="{ row, $index }">
          <el-form-item :prop="`${$index}.price`" :rules="formRules.price" class="mb-0px!">
            <el-input-number
              v-model="row.price"
              controls-position="right"
              :min="0.01"
              :precision="2"
              class="!w-100%"
            />
          </el-form-item>
        </template>
      </el-table-column>
      <el-table-column label="金额" width="120" align="right">
        <template #default="{ row }">
          <el-form-item class="mb-0px!">
            <el-input disabled :model-value="formatAmount(row.amount)" />
          </el-form-item>
        </template>
      </el-table-column>
      <el-table-column label="税率(%)" width="100" align="right">
        <template #default="{ row }">
          <el-form-item class="mb-0px!">
            <el-input-number
              v-model="row.taxRate"
              controls-position="right"
              :min="0"
              :max="100"
              :precision="2"
              class="!w-100%"
            />
          </el-form-item>
        </template>
      </el-table-column>
      <el-table-column label="税额" width="110" align="right">
        <template #default="{ row }">
          <el-form-item class="mb-0px!">
            <el-input disabled :model-value="formatAmount(row.taxAmount)" />
          </el-form-item>
        </template>
      </el-table-column>
      <el-table-column label="价税合计" width="120" align="right">
        <template #default="{ row }">
          <el-form-item class="mb-0px!">
            <el-input disabled :model-value="formatAmount(row.totalAmount)" />
          </el-form-item>
        </template>
      </el-table-column>
      <el-table-column label="备注" width="150">
        <template #default="{ row }">
          <el-form-item class="mb-0px!">
            <el-input v-model="row.remark" placeholder="备注" />
          </el-form-item>
        </template>
      </el-table-column>
      <el-table-column align="center" label="操作" width="60" v-if="!disabled">
        <template #default="{ $index }">
          <el-button @click="handleDelete($index)" link type="danger">
            <Icon icon="ep:delete" />
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-form>

  <el-row justify="center" class="mt-4" v-if="!disabled">
    <el-button @click="handleAdd" round class="invoice-item-form__add-btn">
      <Icon icon="ep:plus" class="mr-5px" /> 添加发票明细
    </el-button>
  </el-row>
</template>

<script setup lang="ts">
import { erpPriceMultiply } from '@/utils'

type InvoiceItemRow = Record<string, any>

const props = withDefaults(
  defineProps<{
    items?: InvoiceItemRow[]
    disabled?: boolean
    productList?: any[]
  }>(),
  {
    items: () => [],
    disabled: false,
    productList: () => []
  }
)

const formLoading = ref(false)
const formData = ref<InvoiceItemRow[]>([])
const formRef = ref()

const formRules = reactive({
  productId: [{ required: true, message: '产品不能为空', trigger: 'change' }],
  count: [{ required: true, message: '数量不能为空', trigger: 'blur' }],
  price: [{ required: true, message: '单价不能为空', trigger: 'blur' }]
})

watch(
  () => props.items,
  (val) => {
    formData.value = val || []
  },
  { immediate: true }
)

// 金额自动计算
watch(
  () => formData.value,
  (val) => {
    if (!val || val.length === 0) return
    val.forEach((item) => {
      // 不含税金额 = 数量 × 单价
      item.amount = erpPriceMultiply(item.price || 0, item.count || 0)
      // 税额 = 不含税金额 × 税率 / 100
      item.taxAmount = erpPriceMultiply(item.amount, (item.taxRate || 0) / 100.0)
      // 价税合计 = 不含税金额 + 税额
      item.totalAmount = item.amount + (item.taxAmount || 0)
    })
  },
  { deep: true }
)

const formatAmount = (value?: number | string | null) => {
  const n = Number(value || 0)
  return n.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const getSummaries = (param: SummaryMethodProps) => {
  const { columns, data } = param
  const sums: string[] = []
  columns.forEach((column, index: number) => {
    if (index === 0) {
      sums[index] = '合计'
      return
    }
    if (['count', 'amount', 'taxAmount', 'totalAmount'].includes(column.property)) {
      const sum = data.reduce((acc, item) => acc + Number(item[column.property] || 0), 0)
      sums[index] = column.property === 'count'
        ? sum.toLocaleString('zh-CN', { maximumFractionDigits: 3 })
        : `¥${sum.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
    } else {
      sums[index] = ''
    }
  })
  return sums
}

const handleAdd = () => {
  formData.value.push({
    id: undefined,
    productId: undefined,
    productName: undefined,
    productSpec: undefined,
    unit: undefined,
    count: 1,
    price: undefined,
    amount: undefined,
    taxRate: 13, // 默认税率 13%
    taxAmount: undefined,
    totalAmount: undefined,
    remark: undefined
  })
}

const handleDelete = (index: number) => {
  formData.value.splice(index, 1)
}

const handleProductChange = (row: InvoiceItemRow, productId: number) => {
  const product = props.productList.find((p) => p.id === productId)
  if (product) {
    row.productName = product.name
    row.productSpec = product.spec || product.standard || ''
    row.unit = product.unitName || product.unit || ''
    // 如果产品有默认单价，自动填充
    if (product.salePrice) {
      row.price = product.salePrice
    }
  }
}

const validate = () => {
  return formRef.value.validate()
}

defineExpose({ validate })

onMounted(() => {
  if (formData.value.length === 0) {
    handleAdd()
  }
})
</script>

<style scoped lang="scss">
.invoice-item-form {
  :deep(.el-form-item) {
    margin-bottom: 0;
  }
  :deep(.el-form-item__content) {
    width: 100%;
    min-width: 0;
  }
}

.invoice-item-table {
  border: 1px solid rgba(226, 232, 240, 0.95);
  border-radius: 12px;
  overflow: hidden;
  background: #fff;

  :deep(.el-table__header-wrapper th) {
    background: linear-gradient(180deg, rgba(248, 250, 252, 0.98), rgba(241, 245, 249, 0.98));
    color: #64748b;
    font-weight: 600;
    font-size: 12px;
  }

  :deep(.el-table__body tr:hover > td) {
    background: rgba(239, 246, 255, 0.65);
  }

  :deep(.el-table__row td) {
    padding-top: 8px;
    padding-bottom: 8px;
    vertical-align: middle;
  }

  :deep(.el-table__cell) {
    padding-left: 6px;
    padding-right: 6px;
  }

  :deep(.el-table .cell) {
    overflow: visible;
  }

  :deep(.el-table__footer-wrapper td) {
    background: rgba(248, 250, 252, 0.95);
    color: var(--el-text-color-primary);
    font-weight: 600;
  }

  :deep(.is-right .cell) {
    justify-content: flex-end;
    text-align: right;
  }

  :deep(.el-input__wrapper),
  :deep(.el-input-number__wrapper) {
    min-height: 36px;
    border-radius: 8px;
    box-shadow: none;
    border: 1px solid rgba(203, 213, 225, 0.88);
    background: linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(248, 250, 252, 0.98));
  }

  :deep(.el-input.is-disabled .el-input__wrapper),
  :deep(.el-input-number.is-disabled .el-input-number__wrapper) {
    border-color: transparent;
    background: #f1f5f9;
    color: #64748b;
  }

  :deep(.el-input__inner),
  :deep(.el-input-number .el-input__inner) {
    color: #0f172a;
    font-variant-numeric: tabular-nums;
  }

  :deep(.el-input-number .el-input__inner) {
    text-align: right;
  }
}

.invoice-item-form__add-btn {
  min-width: 140px;
  height: 36px;
  border-color: rgba(59, 130, 246, 0.18);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(239, 246, 255, 0.92));
  color: #2563eb;
  font-weight: 600;
}
</style>
