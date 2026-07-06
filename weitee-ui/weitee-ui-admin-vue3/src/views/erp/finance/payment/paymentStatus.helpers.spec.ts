import assert from 'node:assert/strict'

const { FINANCE_PAYMENT_STATUS, getFinancePaymentRowActionDescriptor } = await import(
  new URL('./paymentStatus.helpers.ts', import.meta.url).href
)

const runningActions = getFinancePaymentRowActionDescriptor({
  status: FINANCE_PAYMENT_STATUS.PROCESS,
  processInstanceId: 'PI-PAYMENT-001',
  creator: '100',
  currentUserId: '100'
})

assert.equal(runningActions.isApprovalRunning, true)
assert.equal(runningActions.canEdit, false)
assert.equal(runningActions.canDelete, false)
assert.equal(runningActions.canSubmit, false)
assert.equal(runningActions.canCancelApproval, true)
assert.equal(runningActions.canViewProcess, true)

const transientProcessActions = getFinancePaymentRowActionDescriptor({
  status: FINANCE_PAYMENT_STATUS.PROCESS,
  processInstanceId: '',
  creator: '100',
  currentUserId: '100'
})

assert.equal(transientProcessActions.isApprovalRunning, false)
assert.equal(transientProcessActions.canEdit, true)
assert.equal(transientProcessActions.canDelete, true)
assert.equal(transientProcessActions.canSubmit, true)
assert.equal(transientProcessActions.canCancelApproval, false)

const failedActions = getFinancePaymentRowActionDescriptor({
  status: FINANCE_PAYMENT_STATUS.FAILED
})

assert.equal(failedActions.canEdit, true)
assert.equal(failedActions.canDelete, true)
assert.equal(failedActions.canSubmit, true)
assert.equal(failedActions.canCancelApproval, false)

const approvedActions = getFinancePaymentRowActionDescriptor({
  status: FINANCE_PAYMENT_STATUS.APPROVE
})

assert.equal(approvedActions.canEdit, false)
assert.equal(approvedActions.canDelete, false)
assert.equal(approvedActions.canSubmit, false)
