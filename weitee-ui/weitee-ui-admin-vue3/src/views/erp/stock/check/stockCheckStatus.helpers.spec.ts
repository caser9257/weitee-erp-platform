import assert from 'node:assert/strict'
import { deriveStockCheckActions, resolveStockCheckStatus } from './stockCheckStatus.helpers'

assert.deepEqual(resolveStockCheckStatus(20), { label: '审核中', tone: 'warning' })
assert.deepEqual(resolveStockCheckStatus(30), { label: '已审核', tone: 'primary' })
assert.deepEqual(resolveStockCheckStatus(40), { label: '已关闭', tone: 'success' })
assert.deepEqual(deriveStockCheckActions(20), {
  canEdit: false,
  canDelete: false,
  canStart: false,
  canSubmit: false,
  canApproveAndClose: true,
  canReject: true
})
