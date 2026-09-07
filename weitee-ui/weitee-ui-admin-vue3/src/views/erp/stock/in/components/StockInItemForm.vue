<template>
  <div class="stock-in-item">
    <div class="stock-in-item__table-shell">
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="0"
        :inline-message="true"
        :disabled="disabled"
      >
        <el-table
          :data="formData"
          show-summary
          :summary-method="getSummaries"
          class="stock-in-item__table"
        >
          <el-table-column label="序号" type="index" align="center" width="60" />
          <el-table-column label="仓库" min-width="160">
            <template #default="{ row, $index }">
              <el-form-item
                :prop="`${$index}.warehouseId`"
                :rules="formRules.warehouseId"
                class="stock-in-item__form-item"
              >
                <el-select
                  v-model="row.warehouseId"
                  clearable
                  filterable
                  placeholder="请选择仓库"
                  @change="onChangeWarehouse($event, row)"
                >
                  <el-option
                    v-for="item in warehouseList"
                    :key="item.id"
                    :label="item.name"
                    :value="item.id"
                  />
                </el-select>
              </el-form-item>
            </template>
          </el-table-column>
          <el-table-column label="产品信息" min-width="280">
            <template #default="{ row, $index }">
              <el-form-item
                :prop="`${$index}.productId`"
                :rules="formRules.productId"
                class="stock-in-item__form-item"
              >
                <ProductRemoteSelect v-model="row.productId" placeholder="请选择产品" @select="onChangeProduct($event, row)" />
              </el-form-item>
              <div class="stock-in-item__meta">
                <span class="stock-in-item__meta-item">库存 {{ formatCount(row.stockCount) }}</span>
                <span class="stock-in-item__meta-item">条码 {{ row.productBarCode || '-' }}</span>
                <el-select
                  v-model="row.productUnitId"
                  placeholder="单位"
                  class="stock-in-item__unit-select"
                >
                  <el-option
                    v-for="unit in getUnitFamilyOptions(row)"
                    :key="unit.id"
                    :label="unit.name"
                    :value="unit.id"
                  />
                </el-select>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="数量" prop="count" min-width="140" align="right">
            <template #default="{ row, $index }">
              <el-form-item
                :prop="`${$index}.count`"
                :rules="formRules.count"
                class="stock-in-item__form-item"
              >
                <el-input-number
                  v-model="row.count"
                  controls-position="right"
                  :min="getQuantityStep(row)"
                  :step="getQuantityStep(row)"
                  :precision="getQuantityPrecision(row)"
                  class="stock-in-item__number-input"
                />
              </el-form-item>
              <div v-if="isRowAuxiliaryUnit(row)" class="unit-convert-tip">
                ≈ {{ rowBaseCountText(row) }} {{ rowBaseUnitName(row) }}
              </div>
            </template>
          </el-table-column>
          <el-table-column label="产品单价" min-width="140" align="right">
            <template #default="{ row }">
              <el-form-item class="stock-in-item__form-item">
                <el-input-number
                  v-model="row.productPrice"
                  controls-position="right"
                  :min="0.01"
                  :precision="2"
                  class="stock-in-item__number-input"
                />
              </el-form-item>
            </template>
          </el-table-column>
          <el-table-column label="合计金额" prop="totalPrice" min-width="140" align="right">
            <template #default="{ row }">
              <div class="stock-in-item__readonly stock-in-item__readonly--amount">
                {{ formatCurrency(row.totalPrice) }}
              </div>
            </template>
          </el-table-column>
          <el-table-column label="备注" min-width="180">
            <template #default="{ row, $index }">
              <el-form-item :prop="`${$index}.remark`" class="stock-in-item__form-item">
                <el-input v-model="row.remark" placeholder="请输入备注" />
              </el-form-item>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="72" align="center" fixed="right">
            <template #default="{ $index }">
              <el-tooltip content="删除" placement="top">
                <el-button
                  link
                  type="danger"
                  class="stock-in-item__icon-button"
                  @click="handleDelete($index)"
                >
                  <Icon icon="ep:delete" />
                </el-button>
              </el-tooltip>
            </template>
          </el-table-column>
        </el-table>
      </el-form>
    </div>

    <div v-if="!disabled" class="stock-in-item__actions">
      <el-button plain @click="handleAdd">
        <Icon icon="ep:plus" class="mr-5px" /> 添加入库产品
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { SummaryMethod } from 'element-plus'
import type { ProductVO } from '@/api/erp/product/product'
import type { ProductUnitVO } from '@/api/erp/product/unit'
import {
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
import { WarehouseApi, WarehouseVO } from '@/api/erp/stock/warehouse'
import { StockApi } from '@/api/erp/stock/stock'
import { erpPriceMultiply, getSumValue } from '@/utils'

type StockInItemRow = {
  id?: number
  warehouseId?: number
  productId?: number
  productUnitName?: string
  productUnitId?: number
  productBarCode?: string
  productPrice?: number
  stockCount?: number
  count?: number
  totalPrice?: number
  remark?: string
}

const props = defineProps<{
  items: StockInItemRow[]
  disabled: boolean
}>()

const formData = ref<StockInItemRow[]>([])
const formRef = ref()
const productList = ref<ProductVO[]>([])
const unitList = ref<ProductUnitVO[]>([])
const warehouseList = ref<WarehouseVO[]>([])
const defaultWarehouse = ref<WarehouseVO>()

const formRules = reactive({
  warehouseId: [{ required: true, message: '仓库不能为空', trigger: 'blur' }],
  productId: [{ required: true, message: '产品不能为空', trigger: 'blur' }],
  count: [{ required: true, message: '产品数量不能为空', trigger: 'blur' }]
})

const formatCount = (value?: number | string | null) => {
  const numberValue = Number(value || 0)
  if (Number.isInteger(numberValue)) {
    return numberValue.toLocaleString('zh-CN')
  }
  return numberValue.toFixed(3).replace(/\.?0+$/, '')
}

const formatCurrency = (value?: number | string | null) =>
  new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(Number(value || 0))

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
    val.forEach((item) => {
      item.totalPrice = erpPriceMultiply(item.productPrice, item.count)
    })
  },
  { deep: true }
)

const getSummaries: SummaryMethod<StockInItemRow> = ({ columns, data }) => {
  const sums: string[] = []
  columns.forEach((column, index) => {
    if (index === 0) {
      sums[index] = '合计'
      return
    }
    if (column.property === 'count') {
      sums[index] = formatCount(getSumValue(data.map((item) => Number(item.count || 0))))
      return
    }
    if (column.property === 'totalPrice') {
      sums[index] = formatCurrency(getSumValue(data.map((item) => Number(item.totalPrice || 0))))
      return
    }
    sums[index] = ''
  })
  return sums
}

const handleAdd = () => {
  formData.value.push({
    id: undefined,
    warehouseId: defaultWarehouse.value?.id,
    productId: undefined,
    productUnitName: undefined,
    productUnitId: undefined,
    productBarCode: undefined,
    productPrice: undefined,
    stockCount: undefined,
    count: 1,
    totalPrice: undefined,
    remark: undefined
  })
}

const handleDelete = (index: number) => {
  formData.value.splice(index, 1)
}

const setStockCount = async (row: StockInItemRow) => {
  if (!row.productId || !row.warehouseId) {
    row.stockCount = undefined
    return
  }
  const stock = await StockApi.getStock2(row.productId, row.warehouseId)
  row.stockCount = stock ? stock.count : 0
}

const onChangeWarehouse = async (_warehouseId: number | undefined, row: StockInItemRow) => {
  await setStockCount(row)
}

const onChangeProduct = async (product: ProductVO | null, row: StockInItemRow) => {
  if (product) {
    productList.value = [...productList.value.filter((item) => item.id !== product.id), product]
  }
  if (product) {
    row.productUnitName = product.unitName
    row.productUnitId = product.unitId
    row.productBarCode = product.barCode
    row.productPrice = product.minPrice
  } else {
    row.productUnitName = undefined
    row.productUnitId = undefined
    row.productBarCode = undefined
    row.productPrice = undefined
  }
  await setStockCount(row)
}

const getQuantityPrecision = (row: any) => {
  const unitPrecision = getUnitQuantityPrecision(unitList.value, row.productUnitId)
  if (unitPrecision != null) {
    return normalizeQuantityPrecision(unitPrecision)
  }
  const precision = productList.value.find((item) => item.id === row.productId)?.quantityPrecision
  return Number.isInteger(precision) && precision! >= 0 && precision! <= 6 ? precision! : 3
}

const getQuantityStep = (row: any) => {
  const precision = getQuantityPrecision(row)
  return precision === 0 ? 1 : Number((10 ** -precision).toFixed(precision))
}

/** 当前行可选单位族（产品基本单位 + 辅助单位） */
const getUnitFamilyOptions = (row: any) =>
  getUnitFamily(unitList.value, resolveBaseUnitId(unitList.value, row.productUnitId))

const isRowAuxiliaryUnit = (row: any) => isAuxiliaryUnit(unitList.value, row.productUnitId)

const rowBaseUnitName = (row: any) =>
  getUnitName(unitList.value, resolveBaseUnitId(unitList.value, row.productUnitId))

const rowBaseCountText = (row: any) => {
  const baseCount = toBaseCount(unitList.value, row.productUnitId, row.count)
  return baseCount == null
    ? '-'
    : Number(baseCount).toLocaleString('zh-CN', { maximumFractionDigits: 6 })
}

const validate = () => formRef.value?.validate()

const clearValidate = () => {
  formRef.value?.clearValidate()
}

defineExpose({ validate, clearValidate })

onMounted(async () => {
  const [warehouses, units] = await Promise.all([
    WarehouseApi.getWarehouseSimpleList(),
    loadProductUnits()
  ])
  warehouseList.value = warehouses
  unitList.value = units
  defaultWarehouse.value = warehouseList.value.find((item) => item.defaultStatus)
  if (formData.value.length === 0) {
    handleAdd()
  }
})
</script>

<style scoped>
.stock-in-item {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.stock-in-item__table-shell {
  overflow-x: auto;
}

.stock-in-item__table {
  min-width: 980px;
}

.stock-in-item__table :deep(.el-table__footer-wrapper td) {
  font-weight: 700;
}

.stock-in-item__form-item {
  margin-bottom: 0 !important;
}

.stock-in-item__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}


.stock-in-item__unit-select {
  width: 88px;
  vertical-align: middle;
}

.stock-in-item__meta-item {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 999px;
  background: rgba(226, 232, 240, 0.75);
  color: #475569;
  font-size: 12px;
  line-height: 18px;
}

.stock-in-item__number-input {
  width: 100%;
}

.stock-in-item__readonly {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  min-height: 32px;
  padding: 0 10px;
  border-radius: 10px;
  background: rgba(248, 250, 252, 0.96);
  color: #0f172a;
  font-variant-numeric: tabular-nums;
}

.stock-in-item__readonly--amount {
  font-weight: 700;
}

.stock-in-item__icon-button {
  width: 28px;
  height: 28px;
  padding: 0;
}

.stock-in-item__actions {
  display: flex;
  justify-content: center;
}
</style>

<style scoped>
.unit-convert-tip {
  margin-top: 2px;
  font-size: 11px;
  line-height: 1.2;
  text-align: right;
  color: var(--erp-slate-400);
}
</style>
