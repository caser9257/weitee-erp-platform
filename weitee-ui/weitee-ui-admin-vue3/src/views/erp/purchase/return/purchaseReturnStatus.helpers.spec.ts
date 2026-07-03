import assert from 'node:assert/strict'

const { getPurchaseReturnRowActionDescriptor } = await import(
  new URL('./purchaseReturnStatus.helpers.ts', import.meta.url).href
)

const runningApprovalActions = getPurchaseReturnRowActionDescriptor({
  status: 10,
  processInstanceId: 'pi_001',
  creator: '100',
  currentUserId: '100'
})

assert.equal(runningApprovalActions.isApprovalRunning, true)
assert.equal(runningApprovalActions.canEdit, false)
assert.equal(runningApprovalActions.canSubmit, false)
assert.equal(runningApprovalActions.canCancelApproval, true)
assert.equal(runningApprovalActions.canViewProcess, true)
assert.equal(runningApprovalActions.canDelete, false)

const draftActions = getPurchaseReturnRowActionDescriptor({
  status: 0,
  processInstanceId: '',
  creator: '100',
  currentUserId: '100'
})

assert.equal(draftActions.isApprovalRunning, false)
assert.equal(draftActions.canEdit, true)
assert.equal(draftActions.canSubmit, true)
assert.equal(draftActions.canCancelApproval, false)
assert.equal(draftActions.canViewProcess, false)
assert.equal(draftActions.canDelete, true)

const failedRetryActions = getPurchaseReturnRowActionDescriptor({
  status: 60,
  processInstanceId: '',
  creator: '100',
  currentUserId: '100'
})

assert.equal(failedRetryActions.canEdit, true)
assert.equal(failedRetryActions.canSubmit, true)
assert.equal(failedRetryActions.canDelete, true)

const processWithoutBindingActions = getPurchaseReturnRowActionDescriptor({
  status: 10,
  processInstanceId: '',
  creator: '100',
  currentUserId: '200'
})

assert.equal(processWithoutBindingActions.isApprovalRunning, false)
assert.equal(processWithoutBindingActions.canEdit, false)
assert.equal(processWithoutBindingActions.canSubmit, true)
assert.equal(processWithoutBindingActions.canCancelApproval, false)
assert.equal(processWithoutBindingActions.canViewProcess, false)
assert.equal(processWithoutBindingActions.canDelete, false)
