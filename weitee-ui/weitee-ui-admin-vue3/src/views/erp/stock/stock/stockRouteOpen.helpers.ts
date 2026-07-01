export type StockRouteOpenInput = {
  productId?: number | null
  warehouseId?: number | null
  batchNo?: string | null
  openAction?: string | null
}

export type StockRouteOpenBatch = {
  productId?: number | null
  warehouseId?: number | null
  productName?: string
  unitName?: string
  warehouseName?: string
  batchNo?: string | null
  purchaseSourceBatchNo?: string | null
  totalQty?: number | string | null
}

export type StockRouteOpenRow = {
  productId?: number | null
  warehouseId?: number | null
  productName?: string
  unitName?: string
  warehouseName?: string
  count?: number | string | null
}

export type StockRouteOpenResolution =
  | {
      action: 'none'
      cleanupKeys: string[]
    }
  | {
      action: 'filter'
      productId?: number
      warehouseId?: number
      cleanupKeys: string[]
    }
  | {
      action: 'batch-trace'
      productId: number
      warehouseId: number
      batchNo?: string
      cleanupKeys: string[]
    }

export const STOCK_ROUTE_CLEANUP_KEYS = [
  'openAction',
  'batchNo',
  'returnFrom',
  'purchaseInId'
]

const normalizeNumber = (value?: number | null) => Number(value || 0)

export function resolveStockRouteOpen(input: StockRouteOpenInput): StockRouteOpenResolution {
  const productId = normalizeNumber(input.productId)
  const warehouseId = normalizeNumber(input.warehouseId)
  const batchNo = input.batchNo?.trim() || undefined

  if (input.openAction === 'batch-trace') {
    if (productId <= 0 || warehouseId <= 0) {
      return {
        action: 'none',
        cleanupKeys: STOCK_ROUTE_CLEANUP_KEYS
      }
    }
    return {
      action: 'batch-trace',
      productId,
      warehouseId,
      batchNo,
      cleanupKeys: STOCK_ROUTE_CLEANUP_KEYS
    }
  }

  if (productId > 0 || warehouseId > 0) {
    return {
      action: 'filter',
      ...(productId > 0 ? { productId } : {}),
      ...(warehouseId > 0 ? { warehouseId } : {}),
      cleanupKeys: STOCK_ROUTE_CLEANUP_KEYS
    }
  }

  return {
    action: 'none',
    cleanupKeys: STOCK_ROUTE_CLEANUP_KEYS
  }
}

export function findRouteOpenBatch(
  batches: StockRouteOpenBatch[],
  productId: number,
  warehouseId: number,
  routeBatchNo?: string
) {
  const normalizedRouteBatchNo = routeBatchNo?.trim()
  const matchedBatches = batches.filter(
    (item) => item.productId === productId && item.warehouseId === warehouseId
  )
  if (!matchedBatches.length) {
    return undefined
  }
  if (!normalizedRouteBatchNo) {
    return matchedBatches[0]
  }
  return (
    matchedBatches.find(
      (item) =>
        item.batchNo === normalizedRouteBatchNo ||
        item.purchaseSourceBatchNo === normalizedRouteBatchNo
    ) || matchedBatches[0]
  )
}

export function buildRouteOpenStockRowFromBatch(
  batch: StockRouteOpenBatch,
  fallback: {
    productId: number
    warehouseId: number
  }
): StockRouteOpenRow {
  return {
    productId: batch.productId || fallback.productId,
    warehouseId: batch.warehouseId || fallback.warehouseId,
    productName: batch.productName,
    unitName: batch.unitName,
    warehouseName: batch.warehouseName,
    count: batch.totalQty || 0
  }
}
