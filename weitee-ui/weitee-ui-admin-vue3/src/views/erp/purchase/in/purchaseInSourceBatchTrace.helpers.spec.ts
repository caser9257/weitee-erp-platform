import assert from 'node:assert/strict'
import { isCurrentPurchaseInSourceBatchTraceRequest } from './purchaseInSourceBatchTrace.helpers'

assert.equal(
  isCurrentPurchaseInSourceBatchTraceRequest({
    requestToken: 1,
    activeRequestToken: 2,
    requestSourceBatchId: 101,
    activeSourceBatchId: 102
  }),
  false
)

assert.equal(
  isCurrentPurchaseInSourceBatchTraceRequest({
    requestToken: 2,
    activeRequestToken: 2,
    requestSourceBatchId: 101,
    activeSourceBatchId: 102
  }),
  false
)

assert.equal(
  isCurrentPurchaseInSourceBatchTraceRequest({
    requestToken: 2,
    activeRequestToken: 2,
    requestSourceBatchId: 102,
    activeSourceBatchId: 102
  }),
  true
)

assert.equal(
  isCurrentPurchaseInSourceBatchTraceRequest({
    requestToken: 2,
    activeRequestToken: 2,
    requestSourceBatchId: 102,
    activeSourceBatchId: undefined
  }),
  false
)
