import assert from 'node:assert/strict'

import {
  buildRouteOpenStockRowFromBatch,
  findRouteOpenBatch,
  resolveStockRouteOpen
} from './stockRouteOpen.helpers'

assert.deepEqual(
  resolveStockRouteOpen({
    productId: 10,
    warehouseId: 2,
    batchNo: ' B-001 ',
    openAction: 'batch-trace'
  }),
  {
    action: 'batch-trace',
    productId: 10,
    warehouseId: 2,
    batchNo: 'B-001',
    cleanupKeys: ['openAction', 'batchNo', 'returnFrom', 'purchaseInId']
  }
)

assert.deepEqual(
  resolveStockRouteOpen({
    productId: 10,
    warehouseId: null
  }),
  {
    action: 'filter',
    productId: 10,
    cleanupKeys: ['openAction', 'batchNo', 'returnFrom', 'purchaseInId']
  }
)

assert.deepEqual(
  resolveStockRouteOpen({
    openAction: 'batch-trace',
    productId: 10
  }),
  {
    action: 'none',
    cleanupKeys: ['openAction', 'batchNo', 'returnFrom', 'purchaseInId']
  }
)

const batches = [
  {
    id: 1,
    productId: 10,
    warehouseId: 2,
    batchNo: 'REAL-B-001',
    purchaseSourceBatchNo: 'SRC-B-001',
    productName: '绝缘垫片',
    warehouseName: '原料仓',
    totalQty: 10
  },
  {
    id: 2,
    productId: 10,
    warehouseId: 2,
    batchNo: 'REAL-B-002',
    purchaseSourceBatchNo: 'SRC-B-002',
    totalQty: 20
  },
  {
    id: 3,
    productId: 11,
    warehouseId: 2,
    batchNo: 'REAL-B-003'
  }
]

assert.equal(findRouteOpenBatch(batches, 10, 2, 'SRC-B-001')?.batchNo, 'REAL-B-001')
assert.equal(findRouteOpenBatch(batches, 10, 2, 'REAL-B-002')?.batchNo, 'REAL-B-002')
assert.equal(findRouteOpenBatch(batches, 10, 2)?.batchNo, 'REAL-B-001')
assert.equal(findRouteOpenBatch(batches, 12, 2), undefined)

assert.deepEqual(
  buildRouteOpenStockRowFromBatch(batches[0], {
    productId: 10,
    warehouseId: 2
  }),
  {
    productId: 10,
    warehouseId: 2,
    productName: '绝缘垫片',
    unitName: undefined,
    warehouseName: '原料仓',
    count: 10
  }
)
