import assert from 'node:assert/strict'
import { createStockCheckListRequestGate } from './stockCheckListRequest.helpers'

const requestGate = createStockCheckListRequestGate()
const firstRequestId = requestGate.issue()

assert.equal(requestGate.isLatest(firstRequestId), true)

const secondRequestId = requestGate.issue()

assert.equal(requestGate.isLatest(firstRequestId), false)
assert.equal(requestGate.isLatest(secondRequestId), true)

const committedRequestIds: number[] = []
const commitIfLatest = (requestId: number) => {
  if (requestGate.isLatest(requestId)) {
    committedRequestIds.push(requestId)
  }
}

commitIfLatest(firstRequestId)
commitIfLatest(secondRequestId)

assert.deepEqual(committedRequestIds, [secondRequestId])
