type PurchaseOrderItemProductMappingRow = {
  productUnitId?: number
  productUnitName?: string
  productBarCode?: string
}

type PurchaseOrderProduct = {
  unitId?: number
  unitName?: string
  barCode?: string
}

export const applyProductToPurchaseOrderItem = (
  row: PurchaseOrderItemProductMappingRow,
  product: PurchaseOrderProduct
) => {
  row.productUnitId = product.unitId
  row.productUnitName = product.unitName
  row.productBarCode = product.barCode
}
