import assert from 'node:assert/strict'
import {
  getPurchaseInMainControlActions,
  getPurchaseInMainControlStageQuery,
  getPurchaseInPrimaryStockContext,
  PURCHASE_IN_QA_STATUS,
  PURCHASE_IN_STATUS,
  PURCHASE_IN_STOCK_IN_STATUS
} from './purchaseInMainControl.helpers'

assert.deepEqual(getPurchaseInMainControlStageQuery('pendingQuality'), {
  status: PURCHASE_IN_STATUS.APPROVE,
  qaStatus: PURCHASE_IN_QA_STATUS.TO_INSPECT
})

assert.deepEqual(getPurchaseInMainControlStageQuery('partialStockIn'), {
  status: PURCHASE_IN_STATUS.APPROVE,
  stockInStatus: PURCHASE_IN_STOCK_IN_STATUS.PARTIAL_STOCKED_IN
})

assert.deepEqual(getPurchaseInMainControlStageQuery('unknown'), {})

const completeRow = {
  id: 18,
  no: 'CGRK-001',
  orderId: 12,
  orderNo: 'CGDD-001',
  items: [
    {
      productId: 101,
      warehouseId: 7,
      purchaseSourceBatchId: 66,
      purchaseSourceBatchNo: 'B-001'
    }
  ]
}

assert.equal(
  getPurchaseInMainControlActions(completeRow).every((action) => !action.disabled),
  true
)

assert.deepEqual(getPurchaseInPrimaryStockContext(completeRow), {
  productId: 101,
  warehouseId: 7,
  batchNo: 'B-001'
})

const missingContextActions = getPurchaseInMainControlActions({
  id: undefined,
  no: '',
  orderNo: '',
  items: []
})

assert.deepEqual(
  missingContextActions.map((action) => [action.key, action.disabled, action.reason]),
  [
    ['viewPurchaseOrder', true, 'missing-order'],
    ['viewStock', true, 'missing-stock-context'],
    ['traceBatch', true, 'missing-source-batch'],
    ['viewStockFlow', true, 'missing-flow-context']
  ]
)
