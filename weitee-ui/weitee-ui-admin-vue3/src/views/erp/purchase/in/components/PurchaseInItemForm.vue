<template>
  <el-form
    ref="formRef"
    :model="formData"
    :rules="formRules"
    v-loading="formLoading"
    label-width="0px"
    :inline-message="true"
    :disabled="disabled"
  >
    <el-table :data="formData" show-summary :summary-method="getSummaries" class="-mt-10px">
      <el-table-column label="&#24207;&#21495;" type="index" align="center" width="60" />
      <el-table-column label="&#20179;&#24211;&#21517;&#31216;" min-width="125">
        <template #default="{ row, $index }">
          <el-form-item :prop="`${$index}.warehouseId`" :rules="formRules.warehouseId" class="mb-0px!">
            <el-select v-model="row.warehouseId" clearable filterable placeholder="&#35831;&#36873;&#25321;&#20179;&#24211;">
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
      <el-table-column label="&#20135;&#21697;&#21517;&#31216;" min-width="180">
        <template #default="{ row }">
          <el-form-item class="mb-0px!">
            <el-input disabled v-model="row.productName" />
          </el-form-item>
        </template>
      </el-table-column>
      <el-table-column label="&#24211;&#23384;" min-width="100">
        <template #default="{ row }">
          <el-form-item class="mb-0px!">
            <el-input disabled v-model="row.stockCount" :formatter="erpCountInputFormatter" />
          </el-form-item>
        </template>
      </el-table-column>
      <el-table-column label="&#26465;&#30721;" min-width="150">
        <template #default="{ row }">
          <el-form-item class="mb-0px!">
            <el-input disabled v-model="row.productBarCode" />
          </el-form-item>
        </template>
      </el-table-column>
      <el-table-column label="&#21333;&#20301;" min-width="96">
        <template #default="{ row }">
          <el-form-item class="mb-0px!">
            <el-select v-model="row.productUnitId" placeholder="&#21333;&#20301;" class="!w-100%">
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
      <el-table-column
        v-if="formData[0]?.totalCount != null"
        label="&#21407;&#25968;&#37327;"
        fixed="right"
        min-width="80"
      >
        <template #default="{ row }">
          <el-form-item class="mb-0px!">
            <el-input disabled v-model="row.totalCount" :formatter="erpCountInputFormatter" />
          </el-form-item>
        </template>
      </el-table-column>
      <el-table-column
        v-if="formData[0]?.inCount != null"
        label="&#24050;&#20837;&#24211;"
        fixed="right"
        min-width="80"
      >
        <template #default="{ row }">
          <el-form-item class="mb-0px!">
            <el-input disabled v-model="row.inCount" :formatter="erpCountInputFormatter" />
          </el-form-item>
        </template>
      </el-table-column>
      <el-table-column label="&#25968;&#37327;" prop="count" fixed="right" min-width="140">
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
            &#8776; {{ rowBaseCountText(row) }} {{ rowBaseUnitName(row) }}
          </div>
        </template>
      </el-table-column>
      <el-table-column label="&#20135;&#21697;&#21333;&#20215;" fixed="right" min-width="120">
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
      <el-table-column label="&#24037;&#31243;&#36153;" fixed="right" min-width="120">
        <template #default="{ row, $index }">
          <el-form-item :prop="`${$index}.engineeringFee`" class="mb-0px!">
            <el-input-number
              v-model="row.engineeringFee"
              controls-position="right"
              :min="0"
              :precision="2"
              class="!w-100%"
            />
          </el-form-item>
        </template>
      </el-table-column>
      <el-table-column label="&#37329;&#39069;" prop="totalProductPrice" fixed="right" min-width="100">
        <template #default="{ row, $index }">
          <el-form-item :prop="`${$index}.totalProductPrice`" class="mb-0px!">
            <el-input disabled v-model="row.totalProductPrice" :formatter="erpPriceInputFormatter" />
          </el-form-item>
        </template>
      </el-table-column>
      <el-table-column label="&#31246;&#29575;&#65288;%&#65289;" prop="taxPercent" fixed="right" min-width="115">
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
      <el-table-column label="&#31246;&#39069;" prop="taxPrice" fixed="right" min-width="120">
        <template #default="{ row, $index }">
          <el-form-item :prop="`${$index}.taxPrice`" class="mb-0px!">
            <el-input disabled v-model="row.taxPrice" :formatter="erpPriceInputFormatter" />
          </el-form-item>
        </template>
      </el-table-column>
      <el-table-column label="&#31246;&#39069;&#21512;&#35745;" prop="totalPrice" fixed="right" min-width="100">
        <template #default="{ row, $index }">
          <el-form-item :prop="`${$index}.totalPrice`" class="mb-0px!">
            <el-input disabled v-model="row.totalPrice" :formatter="erpPriceInputFormatter" />
          </el-form-item>
        </template>
      </el-table-column>
      <el-table-column label="&#22791;&#27880;" min-width="150">
        <template #default="{ row, $index }">
          <el-form-item :prop="`${$index}.remark`" class="mb-0px!">
            <el-input v-model="row.remark" placeholder="&#35831;&#36755;&#20837;&#22791;&#27880;" />
          </el-form-item>
        </template>
      </el-table-column>
      <el-table-column align="center" fixed="right" label="&#25805;&#20316;" width="60">
        <template #default="{ $index }">
          <el-button :disabled="formData.length === 1" @click="handleDelete($index)" link>-</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-form>
</template>

<script setup lang="ts">
import { StockApi } from '@/api/erp/stock/stock'
import { ProductApi, ProductVO } from '@/api/erp/product/product'
import { WarehouseApi, WarehouseVO } from '@/api/erp/stock/warehouse'
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
import {
  erpCountInputFormatter,
  erpPriceInputFormatter,
  erpPriceMultiply,
  getSumValue
} from '@/utils'

const props = defineProps<{
  items: any[] | undefined
  disabled: boolean
}>()

const formLoading = ref(false)
const formData = ref<any[]>([])
const formRules = reactive({
  warehouseId: [{ required: true, message: '\u4ed3\u5e93\u4e0d\u80fd\u4e3a\u7a7a', trigger: 'blur' }],
  productId: [{ required: true, message: '\u4ea7\u54c1\u4e0d\u80fd\u4e3a\u7a7a', trigger: 'blur' }],
  count: [{ required: true, message: '\u4ea7\u54c1\u6570\u91cf\u4e0d\u80fd\u4e3a\u7a7a', trigger: 'blur' }]
})
const formRef = ref()
const warehouseList = ref<WarehouseVO[]>([])
const defaultWarehouse = ref<WarehouseVO>(undefined)
const productList = ref<ProductVO[]>([])
const unitList = ref<ProductUnitVO[]>([])

const calculateRowPrice = (item: any) => {
  const materialTotalPrice = erpPriceMultiply(item.productPrice, item.count)
  if (materialTotalPrice == null) {
    item.totalProductPrice = undefined
    item.taxPrice = undefined
    item.totalPrice = undefined
    return
  }
  const engineeringFee = item.engineeringFee ?? 0
  item.totalProductPrice = materialTotalPrice + engineeringFee
  if (item.taxPercent == null) {
    item.taxPrice = undefined
    item.totalPrice = item.totalProductPrice
    return
  }
  item.taxPrice = erpPriceMultiply(item.totalProductPrice, item.taxPercent / 100.0)
  item.totalPrice = item.totalProductPrice + (item.taxPrice || 0)
}

watch(
  () => props.items,
  async (val) => {
    const nextVal = val || []
    nextVal.forEach((item) => {
      if (item.warehouseId == null) {
        item.warehouseId = defaultWarehouse.value?.id
      }
      if (item.stockCount === null && item.warehouseId != null) {
        setStockCount(item)
      }
    })
    formData.value = nextVal
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
      calculateRowPrice(item)
    })
  },
  { deep: true }
)

const getSummaries = (param: any) => {
  const { columns, data } = param
  const sums: string[] = []
  columns.forEach((column, index: number) => {
    if (index === 0) {
      sums[index] = '\u5408\u8ba1'
      return
    }
    if (['count', 'engineeringFee', 'totalProductPrice', 'taxPrice', 'totalPrice'].includes(column.property)) {
      const sum = getSumValue(data.map((item) => Number(item[column.property])))
      sums[index] =
        column.property === 'count' ? erpCountInputFormatter(sum) : erpPriceInputFormatter(sum)
    } else {
      sums[index] = ''
    }
  })
  return sums
}

const handleDelete = (index: number) => {
  formData.value.splice(index, 1)
}

const setStockCount = async (row: any) => {
  if (!row.productId) {
    return
  }
  const count = await StockApi.getStockCount(row.productId)
  row.stockCount = count || 0
}

const getQuantityPrecision = (row: any) => {
  const unitPrecision = getUnitQuantityPrecision(unitList.value, row.productUnitId)
  if (unitPrecision != null) {
    return normalizeQuantityPrecision(unitPrecision)
  }
  return getProductQuantityPrecision(productList.value, row.productId)
}

const getQuantityStep = (row: any) => {
  const precision = getQuantityPrecision(row)
  return precision === 0 ? 1 : Number((10 ** -precision).toFixed(precision))
}

/** &#24403;&#21069;&#34892;&#21487;&#36873;&#21333;&#20301;&#26063;&#65288;&#20135;&#21697;&#22522;&#26412;&#21333;&#20301; + &#36741;&#21161;&#21333;&#20301;&#65289; */
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

const validate = () => {
  return formRef.value.validate()
}

defineExpose({ validate })

onMounted(async () => {
  const [warehouses, products, units] = await Promise.all([
    WarehouseApi.getWarehouseSimpleList(),
    ProductApi.getProductSimpleList(),
    loadProductUnits()
  ])
  warehouseList.value = warehouses
  productList.value = products
  unitList.value = units
  defaultWarehouse.value = warehouseList.value.find((item) => item.defaultStatus)
})
</script>

<style scoped>
.unit-convert-tip {
  margin-top: 2px;
  font-size: 11px;
  line-height: 1.2;
  text-align: right;
  color: var(--erp-slate-400);
}
</style>
