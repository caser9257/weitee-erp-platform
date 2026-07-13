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
    <div class="purchase-order-item-table-wrap">
      <el-table
        :data="disabled ? detailRows : formData"
        show-summary
        :summary-method="getSummaries"
        table-layout="auto"
        class="-mt-10px purchase-order-item-table"
      >
        <el-table-column label="序号" type="index" align="center" width="64" />

        <template v-if="disabled">
          <el-table-column label="项目名称" min-width="180">
            <template #default="{ row }">
              <span>{{ row.projectName || '-' }}</span>
            </template>
          </el-table-column>

          <el-table-column label="物料长代码" min-width="160">
            <template #default="{ row }">
              <span class="mono-text">{{ row.productBarCode || '-' }}</span>
            </template>
          </el-table-column>

          <el-table-column label="物料名称" min-width="220">
            <template #default="{ row }">
              <span>{{ row.productName || '-' }}</span>
            </template>
          </el-table-column>

          <el-table-column label="规格型号" min-width="150">
            <template #default="{ row }">
              <span>{{ row.productStandard || '-' }}</span>
            </template>
          </el-table-column>

          <el-table-column label="数量" prop="count" min-width="104" align="right">
            <template #default="{ row }">
              <span class="mono-text">{{ formatCountValue(row.count) }}</span>
            </template>
          </el-table-column>

          <el-table-column label="含税单价" prop="taxIncludedPrice" min-width="120" align="right">
            <template #default="{ row }">
              <span class="money-text">{{ formatPriceValue(row.taxIncludedPrice) }}</span>
            </template>
          </el-table-column>

          <el-table-column label="开工费" prop="engineeringFee" min-width="110" align="right">
            <template #default="{ row }">
              <span class="money-text">{{ formatPriceValue(row.engineeringFee) }}</span>
            </template>
          </el-table-column>

          <el-table-column label="价税合计" prop="totalPrice" min-width="120" align="right">
            <template #default="{ row }">
              <span class="money-text is-strong">{{ formatPriceValue(row.totalPrice) }}</span>
            </template>
          </el-table-column>

          <el-table-column label="交货日期" min-width="120" align="center">
            <template #default="{ row }">
              <span>{{ formatDateValue(row.deliveryDate) }}</span>
            </template>
          </el-table-column>

          <el-table-column label="付款关联金额" prop="paymentAllocatedAmount" min-width="140" align="right">
            <template #default="{ row }">
              <span class="money-text">{{ formatPriceValue(row.paymentAllocatedAmount) }}</span>
            </template>
          </el-table-column>

          <el-table-column label="关联数量" prop="relatedCount" min-width="110" align="right">
            <template #default="{ row }">
              <span class="mono-text">{{ formatCountValue(row.relatedCount) }}</span>
            </template>
          </el-table-column>

          <el-table-column label="入库数量" prop="inCount" min-width="110" align="right">
            <template #default="{ row }">
              <span class="mono-text">{{ formatCountValue(row.inCount) }}</span>
            </template>
          </el-table-column>

          <el-table-column label="开票数量" prop="invoicedCount" min-width="110" align="right">
            <template #default="{ row }">
              <span class="mono-text">{{ formatCountValue(row.invoicedCount) }}</span>
            </template>
          </el-table-column>

          <el-table-column label="审核人" min-width="120">
            <template #default="{ row }">
              <span>{{ row.auditorName || '-' }}</span>
            </template>
          </el-table-column>

          <el-table-column label="备注" min-width="180">
            <template #default="{ row }">
              <span>{{ row.remark || '-' }}</span>
            </template>
          </el-table-column>
        </template>

        <el-table-column v-else label="产品名称" min-width="320">
          <template #default="{ row, $index }">
            <div class="product-cell">
              <el-form-item :prop="`${$index}.productId`" :rules="formRules.productId" class="mb-0px!">
                <el-select
                  v-model="row.productId"
                  clearable
                  filterable
                  placeholder="请选择产品"
                  @change="onChangeProduct($event, row, $index)"
                >
                  <el-option
                    v-for="item in productList"
                    :key="item.id"
                    :label="item.name"
                    :value="item.id"
                  />
                </el-select>
              </el-form-item>
              <div class="product-sub-row">
                <el-select
                  v-model="row.projectId"
                  clearable
                  filterable
                  placeholder="请选择项目"
                  class="project-select"
                >
                  <el-option
                    v-for="item in projectList"
                    :key="item.id"
                    :label="item.name"
                    :value="item.id"
                  />
                </el-select>
                <span class="product-meta">{{ row.productUnitName || '-' }}</span>
                <span class="product-meta">库存 {{ formatCountValue(row.stockCount) }}</span>
              </div>
              <div v-if="row.pricingSourceText || row.pricingHint" class="pricing-row">
                <el-tag
                  v-if="row.pricingSourceText"
                  size="small"
                  effect="plain"
                  :type="getPricingTagType(row.pricingSourceType)"
                >
                  {{ row.pricingSourceText }}
                </el-tag>
                <span
                  v-if="row.pricingHint"
                  class="pricing-hint"
                  :class="getPricingHintClass(row.pricingSourceType)"
                >
                  {{ row.pricingHint }}
                </span>
              </div>
              <el-input v-model="row.remark" placeholder="请输入备注" class="remark-input" />
            </div>
          </template>
        </el-table-column>

        <el-table-column v-if="!disabled" label="数量" prop="count" min-width="104" align="center">
          <template #default="{ row, $index }">
            <el-form-item :prop="`${$index}.count`" :rules="formRules.count" class="mb-0px!">
              <el-input-number
                v-model="row.count"
                controls-position="right"
                :min="0.001"
                :precision="3"
                class="!w-100% numeric-input"
              />
            </el-form-item>
          </template>
        </el-table-column>

        <el-table-column v-if="!disabled" label="产品单价" prop="productPrice" min-width="110" align="center">
          <template #default="{ row, $index }">
            <el-form-item :prop="`${$index}.productPrice`" :rules="formRules.productPrice" class="mb-0px!">
              <el-input-number
                v-model="row.productPrice"
                controls-position="right"
                :min="0.01"
                :precision="2"
                class="!w-100% numeric-input"
              />
            </el-form-item>
          </template>
        </el-table-column>

        <el-table-column v-if="!disabled" label="工程费" prop="engineeringFee" min-width="110" align="center">
          <template #default="{ row, $index }">
            <el-form-item :prop="`${$index}.engineeringFee`" class="mb-0px!">
              <el-input-number
                v-model="row.engineeringFee"
                controls-position="right"
                :min="0"
                :precision="2"
                class="!w-100% numeric-input"
              />
            </el-form-item>
          </template>
        </el-table-column>

        <el-table-column v-if="!disabled" label="税率(%)" prop="taxPercent" min-width="96" align="center">
          <template #default="{ row, $index }">
            <el-form-item :prop="`${$index}.taxPercent`" class="mb-0px!">
              <el-input-number
                v-model="row.taxPercent"
                controls-position="right"
                :min="0"
                :precision="2"
                class="!w-100% numeric-input"
              />
            </el-form-item>
          </template>
        </el-table-column>

        <el-table-column v-if="!disabled" label="金额(无税)" prop="totalProductPrice" min-width="110" align="right">
          <template #default="{ row }">
            <span class="money-text">{{ formatPriceValue(row.totalProductPrice) }}</span>
          </template>
        </el-table-column>

        <el-table-column v-if="!disabled" label="税额" prop="taxPrice" min-width="96" align="right">
          <template #default="{ row }">
            <span class="money-text">{{ formatPriceValue(row.taxPrice) }}</span>
          </template>
        </el-table-column>

        <el-table-column v-if="!disabled" label="价税合计" prop="totalPrice" min-width="110" align="right">
          <template #default="{ row }">
            <span class="money-text is-strong">{{ formatPriceValue(row.totalPrice) }}</span>
          </template>
        </el-table-column>

        <el-table-column v-if="!disabled" align="center" label="操作" width="72">
          <template #default="{ $index }">
            <el-tooltip content="删除当前行" placement="top">
              <el-button @click="handleDelete($index)" link type="danger" :disabled="disabled">
                <Icon icon="ep:delete" />
              </el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </el-form>

  <el-row justify="center" class="mt-3" v-if="!disabled">
    <el-button type="primary" plain @click="handleAdd">
      <Icon icon="ep:plus" class="mr-5px" />
      新增产品
    </el-button>
  </el-row>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { ProductApi, ProductVO } from '@/api/erp/product/product'
import { BomApi, BomPricingPreviewVO } from '@/api/erp/mrp/bom'
import { ProjectApi, ProjectSimpleVO } from '@/api/erp/project'
import { StockApi } from '@/api/erp/stock/stock'
import { applyProductToPurchaseOrderItem } from './purchaseOrderItemMapping.helpers'
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

type PricingSourceType = 'BOM' | 'FALLBACK' | 'ERROR'

type PurchaseOrderItemFormRow = {
  id?: number
  productId?: number
  projectId?: number
  productUnitId?: number
  projectName?: string
  productUnitName?: string
  productBarCode?: string
  productStandard?: string
  productPrice?: number
  taxIncludedPrice?: number
  engineeringFee?: number
  pricingBomId?: number
  pricingBomVersion?: string
  pricingSourceType?: PricingSourceType
  pricingSourceText?: string
  pricingHint?: string
  stockCount?: number
  count?: number
  totalProductPrice?: number
  taxPercent?: number
  taxPrice?: number
  totalPrice?: number
  deliveryDate?: string | Date
  paymentAllocatedAmount?: number
  relatedCount?: number
  invoicedCount?: number
  auditorName?: string
  remark?: string
  inCount?: number
  returnCount?: number
}

const formLoading = ref(false)
const formData = ref<PurchaseOrderItemFormRow[]>([])
const formRules = reactive({
  productId: [{ required: true, message: '产品不能为空', trigger: 'change' }],
  productPrice: [{ required: true, message: '产品单价不能为空', trigger: ['blur', 'change'] }],
  count: [{ required: true, message: '产品数量不能为空', trigger: ['blur', 'change'] }]
})
const formRef = ref()
const productList = ref<ProductVO[]>([])
const projectList = ref<ProjectSimpleVO[]>([])
const bomPreviewEnabled = ref(true)

const formatCountValue = (value?: number | string | null) => {
  return value == null ? '-' : erpCountInputFormatter(Number(value || 0))
}

const formatPriceValue = (value?: number | string | null) => {
  return erpPriceInputFormatter(Number(value || 0))
}

const formatDateValue = (value?: string | Date | null) => {
  if (!value) {
    return '-'
  }
  const date = typeof value === 'string' ? new Date(value) : value
  if (Number.isNaN(date.getTime())) {
    return '-'
  }
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

const getPricingTagType = (sourceType?: PricingSourceType) => {
  if (sourceType === 'BOM') {
    return 'success'
  }
  if (sourceType === 'ERROR') {
    return 'danger'
  }
  return 'warning'
}

const getPricingHintClass = (sourceType?: PricingSourceType) => {
  if (sourceType === 'ERROR') {
    return 'is-error'
  }
  if (sourceType === 'BOM') {
    return 'is-success'
  }
  return 'is-muted'
}

const setPricingState = (
  row: PurchaseOrderItemFormRow,
  sourceType?: PricingSourceType,
  sourceText?: string,
  hint?: string
) => {
  row.pricingSourceType = sourceType
  row.pricingSourceText = sourceText
  row.pricingHint = hint
}

const clearPricingState = (row: PurchaseOrderItemFormRow) => {
  setPricingState(row, undefined, undefined, undefined)
}

const hydrateExistingPricingState = (row: PurchaseOrderItemFormRow) => {
  if (row.pricingSourceText) {
    return
  }
  if (row.pricingBomId) {
    setPricingState(
      row,
      'BOM',
      'BOM计价',
      row.pricingBomVersion ? `已按 BOM ${row.pricingBomVersion} 自动反算` : '已按有效 BOM 自动反算'
    )
  }
}

const calculateRowPrice = (item: PurchaseOrderItemFormRow) => {
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

const detailRows = computed(() => {
  return (formData.value || []).map((item) => ({
    ...item,
    taxIncludedPrice:
      item.taxIncludedPrice ??
      (item.productPrice != null
        ? Number(((item.productPrice || 0) * (1 + (item.taxPercent || 0) / 100)).toFixed(2))
        : undefined)
  }))
})

watch(
  () => props.items,
  async (val) => {
    formData.value = (val as PurchaseOrderItemFormRow[]) || []
    formData.value.forEach((item) => hydrateExistingPricingState(item))
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
  const totalCount = getSumValue(data.map((item) => Number(item.count)))
  const totalTaxIncludedPrice = getSumValue(data.map((item) => Number(item.taxIncludedPrice)))
  const totalEngineeringFee = getSumValue(data.map((item) => Number(item.engineeringFee)))
  const totalPrice = getSumValue(data.map((item) => Number(item.totalPrice)))
  const totalPaymentAllocatedAmount = getSumValue(data.map((item) => Number(item.paymentAllocatedAmount)))
  const totalRelatedCount = getSumValue(data.map((item) => Number(item.relatedCount)))
  const totalInCount = getSumValue(data.map((item) => Number(item.inCount)))
  const totalInvoicedCount = getSumValue(data.map((item) => Number(item.invoicedCount)))
  const totalProductPrice = getSumValue(data.map((item) => Number(item.totalProductPrice)))
  const totalTaxPrice = getSumValue(data.map((item) => Number(item.taxPrice)))

  columns.forEach((column, index: number) => {
    if (index === 0) {
      sums[index] = '本页小计'
      return
    }
    if (column.property === 'count') {
      sums[index] = erpCountInputFormatter(totalCount)
      return
    }
    if (column.property === 'taxIncludedPrice') {
      sums[index] = erpPriceInputFormatter(totalTaxIncludedPrice)
      return
    }
    if (column.property === 'engineeringFee') {
      sums[index] = erpPriceInputFormatter(totalEngineeringFee)
      return
    }
    if (column.property === 'totalProductPrice') {
      sums[index] = erpPriceInputFormatter(totalProductPrice)
      return
    }
    if (column.property === 'taxPrice') {
      sums[index] = erpPriceInputFormatter(totalTaxPrice)
      return
    }
    if (column.property === 'totalPrice') {
      sums[index] = erpPriceInputFormatter(totalPrice)
      return
    }
    if (column.property === 'paymentAllocatedAmount') {
      sums[index] = erpPriceInputFormatter(totalPaymentAllocatedAmount)
      return
    }
    if (column.property === 'relatedCount') {
      sums[index] = erpCountInputFormatter(totalRelatedCount)
      return
    }
    if (column.property === 'inCount') {
      sums[index] = erpCountInputFormatter(totalInCount)
      return
    }
    if (column.property === 'invoicedCount') {
      sums[index] = erpCountInputFormatter(totalInvoicedCount)
      return
    }
    sums[index] = ''
  })
  return sums
}

const handleAdd = () => {
  const row = {
    id: undefined,
    productId: undefined,
    projectId: undefined,
    productUnitName: undefined,
    productBarCode: undefined,
    productPrice: undefined,
    engineeringFee: undefined,
    pricingBomId: undefined,
    pricingBomVersion: undefined,
    stockCount: undefined,
    count: 1,
    totalProductPrice: undefined,
    taxPercent: undefined,
    taxPrice: undefined,
    totalPrice: undefined,
    remark: undefined
  }
  formData.value.push(row)
}

const handleDelete = (index: number) => {
  formData.value.splice(index, 1)
}

const clearRowValidate = async (rowIndex: number, fields: string[]) => {
  await nextTick()
  formRef.value?.clearValidate(fields.map((field) => `${rowIndex}.${field}`))
}

const onChangeProduct = async (
  productId: number | undefined,
  row: PurchaseOrderItemFormRow,
  rowIndex: number
) => {
  const product = productList.value.find((item) => item.id === productId)
  if (!product) {
    row.productUnitName = undefined
    row.productUnitId = undefined
    row.productBarCode = undefined
    row.productPrice = undefined
    row.pricingBomId = undefined
    row.pricingBomVersion = undefined
    clearPricingState(row)
    await setStockCount(row)
    await clearRowValidate(rowIndex, ['productId', 'productPrice'])
    return
  }

  applyProductToPurchaseOrderItem(row, product)
  await fillPricingByProduct(productId, row, product.purchasePrice)
  await setStockCount(row)
  await clearRowValidate(rowIndex, ['productId', 'productPrice'])
}

const buildPricingStateByPreview = (
  preview: BomPricingPreviewVO,
  fallbackPrice?: number
): { sourceType: PricingSourceType; sourceText: string; hint?: string; warn?: boolean } => {
  switch (preview.calcStatus) {
    case 'SUCCESS':
      return {
        sourceType: 'BOM',
        sourceText: 'BOM计价',
        hint: preview.bomVersion
          ? `已按 BOM ${preview.bomVersion} 自动反算`
          : preview.calcMessage || '已按有效 BOM 自动反算'
      }
    case 'NO_EFFECTIVE_BOM':
      return {
        sourceType: 'FALLBACK',
        sourceText: '采购价回退',
        hint: preview.calcMessage || '未找到有效 BOM，已使用采购价'
      }
    case 'CYCLIC_BOM':
      return {
        sourceType: 'ERROR',
        sourceText: '计价异常',
        hint: preview.calcMessage || 'BOM 存在循环引用，已回退产品采购价',
        warn: true
      }
    case 'MISSING_PURCHASE_PRICE':
      return {
        sourceType: fallbackPrice != null ? 'FALLBACK' : 'ERROR',
        sourceText: fallbackPrice != null ? '计价异常' : '计价异常',
        hint: preview.calcMessage || 'BOM 子件存在未维护采购价，已回退产品采购价',
        warn: true
      }
    default:
      return {
        sourceType: fallbackPrice != null ? 'FALLBACK' : 'ERROR',
        sourceText: fallbackPrice != null ? '采购价回退' : '计价异常',
        hint: preview.calcMessage || (fallbackPrice != null ? 'BOM 预览不可用，已使用采购价' : 'BOM 预览不可用'),
        warn: false
      }
  }
}

const fillPricingByProduct = async (
  productId: number,
  row: PurchaseOrderItemFormRow,
  fallbackPrice?: number
) => {
  if (!bomPreviewEnabled.value) {
    row.productPrice = fallbackPrice
    row.pricingBomId = undefined
    row.pricingBomVersion = undefined
    setPricingState(row, 'FALLBACK', '采购价回退', 'BOM 预览不可用，已使用采购价')
    return
  }
  const requestPrice = row.productPrice
  try {
    const preview = await BomApi.getBomPricingPreviewSilent(productId)
    if (!preview) {
      bomPreviewEnabled.value = false
      row.productPrice = fallbackPrice
      row.pricingBomId = undefined
      row.pricingBomVersion = undefined
      setPricingState(row, 'FALLBACK', '采购价回退', 'BOM 预览不可用，已使用采购价')
      return
    }
    if (row.productId !== productId || row.productPrice !== requestPrice) {
      return
    }
    const pricingState = buildPricingStateByPreview(preview, fallbackPrice)
    if (preview?.materialUnitPrice != null) {
      row.productPrice = preview.materialUnitPrice
      row.pricingBomId = preview.calcStatus === 'SUCCESS' ? preview.bomId : undefined
      row.pricingBomVersion = preview.calcStatus === 'SUCCESS' ? preview.bomVersion : undefined
      setPricingState(row, pricingState.sourceType, pricingState.sourceText, pricingState.hint)
      if (pricingState.warn) {
        ElMessage.warning(pricingState.hint || 'BOM 计价预览失败，已回退产品采购价')
      }
      return
    }
    if (preview?.calcStatus) {
      setPricingState(row, pricingState.sourceType, pricingState.sourceText, pricingState.hint)
      if (pricingState.warn) {
        ElMessage.warning(pricingState.hint || 'BOM 计价预览失败，已回退产品采购价')
      }
    }
  } catch {
    bomPreviewEnabled.value = false
    setPricingState(row, 'FALLBACK', '采购价回退', 'BOM 预览不可用，已使用采购价')
  }
  row.productPrice = fallbackPrice
  row.pricingBomId = undefined
  row.pricingBomVersion = undefined
}

const setStockCount = async (row: PurchaseOrderItemFormRow) => {
  if (!row.productId) {
    row.stockCount = undefined
    return
  }
  const count = await StockApi.getStockCount(row.productId)
  row.stockCount = count || 0
}

const validate = () => {
  return formRef.value.validate()
}

const clearValidate = () => {
  formRef.value?.clearValidate()
}

defineExpose({ validate, clearValidate })

onMounted(async () => {
  productList.value = await ProductApi.getProductSimpleList()
  projectList.value = await ProjectApi.getProjectSimpleList()
  if (formData.value.length === 0) {
    handleAdd()
  }
})
</script>

<style scoped lang="scss">
.purchase-order-item-table-wrap {
  overflow-x: auto;
}

.purchase-order-item-table {
  :deep(.el-table__cell) {
    vertical-align: top;
  }

  :deep(.el-input.is-disabled .el-input__wrapper) {
    box-shadow: none;
    background: transparent;
    padding-left: 0;
    padding-right: 0;
  }

  :deep(.numeric-input .el-input__inner) {
    text-align: right;
    font-variant-numeric: tabular-nums;
  }
}

.product-cell {
  display: grid;
  gap: 8px;
  min-width: 0;
}

.product-sub-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.project-select {
  width: 148px;
}

.product-meta {
  color: var(--el-text-color-secondary);
  font-size: 12px;
  line-height: 18px;
}

.pricing-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.pricing-hint {
  font-size: 12px;
  line-height: 18px;
}

.pricing-hint.is-muted {
  color: var(--el-text-color-secondary);
}

.pricing-hint.is-success {
  color: var(--el-color-success);
}

.pricing-hint.is-error {
  color: var(--el-color-danger);
}

.remark-input {
  min-width: 0;
}

.money-text {
  display: inline-block;
  width: 100%;
  color: var(--el-text-color-primary);
  font-variant-numeric: tabular-nums;
  line-height: 32px;
  text-align: right;
}

.money-text.is-strong {
  font-weight: 600;
}

.mono-text {
  display: inline-block;
  width: 100%;
  text-align: right;
  font-variant-numeric: tabular-nums;
}

@media (max-width: 1280px) {
  .project-select {
    width: 100%;
  }
}
</style>
