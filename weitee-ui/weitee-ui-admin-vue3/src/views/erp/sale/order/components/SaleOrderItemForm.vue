<template>
  <el-form
    ref="formRef"
    :model="formData"
    :rules="formRules"
    v-loading="formLoading"
    label-width="0px"
    :inline-message="true"
    :disabled="disabled"
    class="sale-order-item-form"
  >
    <el-table
      :data="formData"
      show-summary
      :summary-method="getSummaries"
      class="sale-order-item-table -mt-10px"
      table-layout="fixed"
      :fit="false"
    >
      <el-table-column label="序号" type="index" align="center" width="60" />
      <el-table-column label="产品名称" width="320">
        <template #default="{ row, $index }">
          <el-form-item :prop="`${$index}.productId`" :rules="formRules.productId" class="mb-0px!">
            <div
              class="product-picker-trigger"
              :class="{ 'is-disabled': disabled, 'is-filled': !!row.productId }"
              :title="row.productName || '请选择商品'"
              @click="openProductPicker(row)"
              @keydown.enter.prevent="openProductPicker(row)"
              @keydown.space.prevent="openProductPicker(row)"
              tabindex="0"
              role="button"
            >
              <div class="product-picker-trigger__main">
                <div class="product-picker-trigger__topline">
                  <Icon icon="ep:shopping-bag" class="product-picker-trigger__icon" />
                  <span class="product-picker-trigger__name">
                    {{ row.productName || '请选择商品' }}
                  </span>
                </div>
                <div class="product-picker-trigger__meta">
                  <span>条码：{{ row.productBarCode || '-' }}</span>
                  <span>单位：{{ row.productUnitName || '-' }}</span>
                </div>
              </div>
              <Icon icon="ep:arrow-right" class="product-picker-trigger__arrow" />
            </div>
          </el-form-item>
        </template>
      </el-table-column>
      <el-table-column label="库存" width="110" align="right">
        <template #default="{ row }">
          <el-form-item class="mb-0px!">
            <el-input disabled v-model="row.stockCount" :formatter="erpCountInputFormatter" />
          </el-form-item>
        </template>
      </el-table-column>
      <el-table-column label="条码" width="150">
        <template #default="{ row }">
          <el-form-item class="mb-0px!">
            <el-input disabled v-model="row.productBarCode" />
          </el-form-item>
        </template>
      </el-table-column>
      <el-table-column label="单位" width="100" align="center">
        <template #default="{ row }">
          <el-form-item class="mb-0px!">
            <el-select
              v-model="row.productUnitId"
              placeholder="单位"
              class="!w-100%"
              @change="handleUnitChange(row)"
            >
              <el-option
                v-for="unit in getUnitFamilyOptions(row)"
                :key="unit.id"
                :label="unit.name"
                :value="unit.id"
              />
            </el-select>
          </el-form-item>
        </template>
      </el-table-column>
      <el-table-column label="数量" prop="count" width="140" align="right">
        <template #default="{ row, $index }">
          <el-form-item :prop="`${$index}.count`" :rules="formRules.count" class="mb-0px!">
            <el-input-number
              v-model="row.count"
              controls-position="right"
              :min="getQuantityStep(row)"
              :step="getQuantityStep(row)"
              :precision="getQuantityPrecision(row)"
              class="!w-100%"
            />
          </el-form-item>
          <div v-if="isRowAuxiliaryUnit(row)" class="unit-convert-tip">
            ≈ {{ rowBaseCountText(row) }} {{ rowBaseUnitName(row) }}
          </div>
        </template>
      </el-table-column>
      <el-table-column label="产品单价" width="128" align="right">
        <template #default="{ row, $index }">
          <el-form-item :prop="`${$index}.productPrice`" class="mb-0px!">
            <el-input-number
              v-model="row.productPrice"
              controls-position="right"
              :min="0.01"
              :precision="2"
              class="!w-100%"
            />
          </el-form-item>
        </template>
      </el-table-column>
      <el-table-column label="金额" prop="totalProductPrice" width="112" align="right">
        <template #default="{ row, $index }">
          <el-form-item :prop="`${$index}.totalProductPrice`" class="mb-0px!">
            <el-input
              disabled
              v-model="row.totalProductPrice"
              :formatter="erpPriceInputFormatter"
            />
          </el-form-item>
        </template>
      </el-table-column>
      <el-table-column label="税率(%)" prop="taxPercent" width="112" align="right">
        <template #default="{ row, $index }">
          <el-form-item :prop="`${$index}.taxPercent`" class="mb-0px!">
            <el-input-number
              v-model="row.taxPercent"
              controls-position="right"
              :min="0"
              :precision="2"
              class="!w-100%"
            />
          </el-form-item>
        </template>
      </el-table-column>
      <el-table-column label="税额" prop="taxPrice" width="112" align="right">
        <template #default="{ row, $index }">
          <el-form-item :prop="`${$index}.taxPrice`" class="mb-0px!">
            <el-input disabled v-model="row.taxPrice" :formatter="erpPriceInputFormatter" />
          </el-form-item>
        </template>
      </el-table-column>
      <el-table-column label="价税合计" prop="totalPrice" width="112" align="right">
        <template #default="{ row, $index }">
          <el-form-item :prop="`${$index}.totalPrice`" class="mb-0px!">
            <el-input disabled v-model="row.totalPrice" :formatter="erpPriceInputFormatter" />
          </el-form-item>
        </template>
      </el-table-column>
      <el-table-column label="备注" width="172">
        <template #default="{ row, $index }">
          <el-form-item :prop="`${$index}.remark`" class="mb-0px!">
            <el-input v-model="row.remark" placeholder="请输入备注" />
          </el-form-item>
        </template>
      </el-table-column>
      <el-table-column align="center" label="操作" width="76">
        <template #default="{ $index }">
          <el-button @click="handleDelete($index)" link>删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-form>

  <el-row justify="center" class="mt-4" v-if="!disabled">
    <el-button @click="handleAdd" round class="sale-order-item-form__add-btn">
      + 添加销售产品
    </el-button>
  </el-row>

  <SaleOrderProductPickerDrawer ref="productPickerDrawerRef" @confirm="handleConfirmProduct" />
</template>

<script setup lang="ts">
import type { SummaryMethod } from 'element-plus'
import SaleOrderProductPickerDrawer from './SaleOrderProductPickerDrawer.vue'
import { ProductApi, type ProductVO } from '@/api/erp/product/product'
import {
  erpCountInputFormatter,
  erpPriceInputFormatter,
  erpPriceMultiply,
  getSumValue
} from '@/utils'
import {
  getProductQuantityPrecision,
  normalizeQuantityPrecision
} from '@/utils/erpQuantityPrecision'
import {
  loadProductUnits,
  getUnitFamily,
  getUnitQuantityPrecision,
  getUnitName,
  resolveBaseUnitId,
  toBaseCount,
  isAuxiliaryUnit
} from '@/utils/erpUnitConversion'
import type { ProductUnitVO } from '@/api/erp/product/unit'

type SaleOrderItemRow = Record<string, any>

interface ProductPickerSelection {
  row: SaleOrderItemRow
  product: {
    productId: number
    productName: string
    productBarCode?: string
    productUnitId?: number
    productUnitName?: string
    quantityPrecision?: number
    salePrice?: number
  }
  stockCount?: number
}

const props = withDefaults(
  defineProps<{
    items?: SaleOrderItemRow[]
    disabled?: boolean
  }>(),
  {
    items: () => [],
    disabled: false
  }
)

const formLoading = ref(false)
const formData = ref<SaleOrderItemRow[]>([])
const formRef = ref()
const productPickerDrawerRef = ref<InstanceType<typeof SaleOrderProductPickerDrawer>>()
const productList = ref<ProductVO[]>([])
const unitList = ref<ProductUnitVO[]>([])

const formRules = reactive({
  productId: [{ required: true, message: '商品不能为空', trigger: 'blur' }],
  count: [{ required: true, message: '商品数量不能为空', trigger: 'blur' }]
})

watch(
  () => props.items,
  (val) => {
    formData.value = val || []
  },
  { immediate: true }
)

watch(
  () => formData.value,
  (val) => {
    if (!val || val.length === 0) {
      return
    }
    val.forEach((item) => {
      item.totalProductPrice = erpPriceMultiply(item.productPrice, item.count)
      item.taxPrice = erpPriceMultiply(item.totalProductPrice, item.taxPercent / 100.0)
      if (item.totalProductPrice != null) {
        item.totalPrice = item.totalProductPrice + (item.taxPrice || 0)
      } else {
        item.totalPrice = undefined
      }
    })
  },
  { deep: true }
)

const getSummaries: SummaryMethod<SaleOrderItemRow> = (param) => {
  const { columns, data } = param
  const sums: string[] = []
  columns.forEach((column, index: number) => {
    if (index === 0) {
      sums[index] = '合计'
      return
    }
    if (['count', 'totalProductPrice', 'taxPrice', 'totalPrice'].includes(column.property)) {
      const sum = getSumValue(data.map((item) => Number(item[column.property])))
      sums[index] =
        column.property === 'count' ? erpCountInputFormatter(sum) : erpPriceInputFormatter(sum)
    } else {
      sums[index] = ''
    }
  })

  return sums
}

const getQuantityPrecision = (row: SaleOrderItemRow) => {
  const unitPrecision = getUnitQuantityPrecision(unitList.value, row.productUnitId)
  if (unitPrecision != null) {
    return normalizeQuantityPrecision(unitPrecision)
  }
  if (row.quantityPrecision != null) {
    return normalizeQuantityPrecision(row.quantityPrecision)
  }
  return getProductQuantityPrecision(productList.value, row.productId)
}

const getQuantityStep = (row: SaleOrderItemRow) => {
  const precision = getQuantityPrecision(row)
  return precision === 0 ? 1 : Number((10 ** -precision).toFixed(precision))
}

/** 当前行可选单位族（产品基本单位 + 辅助单位） */
const getUnitFamilyOptions = (row: SaleOrderItemRow): ProductUnitVO[] =>
  getUnitFamily(unitList.value, row.baseUnitId ?? resolveBaseUnitId(unitList.value, row.productUnitId))

/** 切换录入单位时，按新单位精度收敛数量 */
const handleUnitChange = (row: SaleOrderItemRow) => {
  if (row.count != null) {
    row.count = Number(Number(row.count).toFixed(getQuantityPrecision(row)))
  }
}

const isRowAuxiliaryUnit = (row: SaleOrderItemRow) => isAuxiliaryUnit(unitList.value, row.productUnitId)

const rowBaseUnitName = (row: SaleOrderItemRow) =>
  getUnitName(unitList.value, row.baseUnitId ?? resolveBaseUnitId(unitList.value, row.productUnitId))

const rowBaseCountText = (row: SaleOrderItemRow) => {
  const baseCount = toBaseCount(unitList.value, row.productUnitId, row.count)
  return baseCount == null
    ? '-'
    : Number(baseCount).toLocaleString('zh-CN', { maximumFractionDigits: 6 })
}

const handleAdd = () => {
  formData.value.push({
    id: undefined,
    productId: undefined,
    productName: undefined,
    baseUnitId: undefined,
    productUnitId: undefined,
    productUnitName: undefined,
    productBarCode: undefined,
    productPrice: undefined,
    stockCount: undefined,
    count: 1,
    totalProductPrice: undefined,
    taxPercent: undefined,
    taxPrice: undefined,
    totalPrice: undefined,
    remark: undefined
  })
}

const handleDelete = (index: number) => {
  formData.value.splice(index, 1)
}

const openProductPicker = (row: SaleOrderItemRow) => {
  if (props.disabled) {
    return
  }
  productPickerDrawerRef.value?.open(row)
}

const handleConfirmProduct = ({ row, product, stockCount }: ProductPickerSelection) => {
  if (!formData.value.includes(row)) {
    return
  }

  Object.assign(row, {
    productId: product.productId,
    productName: product.productName,
    baseUnitId: product.productUnitId,
    productUnitId: product.productUnitId,
    productUnitName: product.productUnitName,
    productBarCode: product.productBarCode,
    quantityPrecision: product.quantityPrecision,
    productPrice: product.salePrice,
    stockCount: stockCount ?? 0
  })

  const rowIndex = formData.value.indexOf(row)
  if (rowIndex >= 0) {
    formRef.value?.clearValidate([`${rowIndex}.productId`])
  }
}

const validate = () => {
  return formRef.value.validate()
}

defineExpose({ validate })

onMounted(async () => {
  productList.value = await ProductApi.getProductSimpleList()
  unitList.value = await loadProductUnits()
  if (formData.value.length === 0) {
    handleAdd()
  }
})
</script>

<style scoped lang="scss">
.sale-order-item-form {
  :deep(.el-form-item) {
    margin-bottom: 0;
  }

  :deep(.el-form-item__content) {
    width: 100%;
    min-width: 0;
  }

  .unit-convert-tip {
    margin-top: 2px;
    font-size: 11px;
    line-height: 1.2;
    text-align: right;
    color: var(--erp-slate-400);
  }
}

.sale-order-item-table {
  border: 1px solid rgba(226, 232, 240, 0.95);
  border-radius: 18px;
  overflow: hidden;
  background: #fff;

  :deep(.el-table__header-wrapper th) {
    background:
      linear-gradient(180deg, rgba(248, 250, 252, 0.98), rgba(241, 245, 249, 0.98)),
      #fff;
    color: #64748b;
    font-weight: 600;
    font-size: 12px;
  }

  :deep(.el-table__body tr:hover > td) {
    background: rgba(239, 246, 255, 0.65);
  }

  :deep(.el-table__row td) {
    padding-top: 10px;
    padding-bottom: 10px;
    vertical-align: middle;
  }

  :deep(.el-table__cell) {
    padding-left: 8px;
    padding-right: 8px;
  }

  :deep(.el-table .cell) {
    overflow: visible;
  }

  :deep(.el-table__body-wrapper),
  :deep(.el-table__footer-wrapper) {
    overflow-x: auto;
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

  :deep(.is-center .cell) {
    justify-content: center;
    text-align: center;
  }

  :deep(.el-input__wrapper),
  :deep(.el-input-number__wrapper) {
    min-height: 40px;
    border-radius: 12px;
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

.product-picker-trigger {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  width: 100%;
  min-height: 52px;
  padding: 10px 12px;
  border: 1px solid rgba(148, 163, 184, 0.24);
  border-radius: 14px;
  background:
    linear-gradient(135deg, rgba(248, 250, 252, 0.98), rgba(255, 255, 255, 0.98)),
    #fff;
  cursor: pointer;
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease,
    transform 0.2s ease,
    background-color 0.2s ease;
}

.product-picker-trigger:hover {
  border-color: rgba(64, 158, 255, 0.4);
  box-shadow: 0 8px 18px rgba(15, 23, 42, 0.06);
  transform: translateY(-1px);
}

.product-picker-trigger:focus-visible {
  outline: none;
  border-color: rgba(64, 158, 255, 0.62);
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.12);
}

.product-picker-trigger.is-filled {
  border-color: rgba(64, 158, 255, 0.34);
  background:
    linear-gradient(180deg, rgba(64, 158, 255, 0.06), rgba(255, 255, 255, 0.98)),
    #fff;
}

.product-picker-trigger.is-disabled {
  cursor: not-allowed;
  opacity: 0.72;
}

.product-picker-trigger.is-disabled:hover {
  box-shadow: none;
  transform: none;
}

.product-picker-trigger__main {
  min-width: 0;
  flex: 1;
}

.product-picker-trigger__topline {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.product-picker-trigger__name {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.product-picker-trigger__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 4px 12px;
  margin-top: 4px;
  font-size: 11px;
  color: var(--el-text-color-secondary);
}

.product-picker-trigger__icon {
  flex-shrink: 0;
  color: var(--el-color-primary);
  padding: 6px;
  border-radius: 9px;
  background: rgba(64, 158, 255, 0.1);
}

.product-picker-trigger__arrow {
  flex-shrink: 0;
  color: var(--el-text-color-placeholder);
}

.sale-order-item-form__add-btn {
  min-width: 164px;
  height: 40px;
  border-color: rgba(59, 130, 246, 0.18);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(239, 246, 255, 0.92));
  color: #2563eb;
  font-weight: 600;
}

@media (max-width: 768px) {
  .sale-order-item-table {
    :deep(.el-table__cell) {
      padding-left: 8px;
      padding-right: 8px;
    }
  }

  .product-picker-trigger {
    min-height: 48px;
    padding: 9px 10px;
  }
}
</style>
