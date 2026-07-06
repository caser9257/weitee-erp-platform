import assert from 'node:assert/strict'

const { FINANCE_EXPENSE_STATUS, getFinanceExpenseRowActionDescriptor } = await import(
  new URL('./expenseStatus.helpers.ts', import.meta.url).href
)

const runningActions = getFinanceExpenseRowActionDescriptor({
  status: FINANCE_EXPENSE_STATUS.PROCESS,
  processInstanceId: 'PI-EXPENSE-001',
  creator: '200',
  currentUserId: '200'
})

assert.equal(runningActions.isApprovalRunning, true)
assert.equal(runningActions.canEdit, false)
assert.equal(runningActions.canDelete, false)
assert.equal(runningActions.canSubmit, false)
assert.equal(runningActions.canCancelApproval, true)
assert.equal(runningActions.canViewProcess, true)

const transientProcessActions = getFinanceExpenseRowActionDescriptor({
  status: FINANCE_EXPENSE_STATUS.PROCESS,
  processInstanceId: null
})

assert.equal(transientProcessActions.isApprovalRunning, false)
assert.equal(transientProcessActions.canEdit, true)
assert.equal(transientProcessActions.canDelete, true)
assert.equal(transientProcessActions.canSubmit, true)

const failedActions = getFinanceExpenseRowActionDescriptor({
  status: FINANCE_EXPENSE_STATUS.FAILED
})

assert.equal(failedActions.canEdit, true)
assert.equal(failedActions.canDelete, true)
assert.equal(failedActions.canSubmit, true)
assert.equal(failedActions.canCancelApproval, false)

const approvedActions = getFinanceExpenseRowActionDescriptor({
  status: FINANCE_EXPENSE_STATUS.APPROVE
})

assert.equal(approvedActions.canEdit, false)
assert.equal(approvedActions.canDelete, false)
assert.equal(approvedActions.canSubmit, false)
