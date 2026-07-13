import assert from 'node:assert/strict'

const { applyProductToPurchaseOrderItem } = await import(
  new URL('./purchaseOrderItemMapping.helpers.ts', import.meta.url).href
)

const row = { productId: 3, productUnitName: undefined as string | undefined }

applyProductToPurchaseOrderItem(row, {
  id: 3,
  name: '测试产品',
  unitId: 3,
  unitName: '根',
  barCode: 'MRP1775035126'
} as any)

assert.equal(row.productUnitName, '根')
assert.equal(row.productUnitId, 3)
