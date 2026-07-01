import assert from 'node:assert/strict'

const {
  getSaleOrderRowActionDescriptor,
  getSaleOrderToolbarDescriptor
} = await import(new URL('./saleOrderStatus.helpers.ts', import.meta.url).href)

const runningApprovalActions = getSaleOrderRowActionDescriptor({
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
assert.equal(runningApprovalActions.canTraceDownstream, false)
assert.equal(runningApprovalActions.canDelete, false)

const approvedOrderActions = getSaleOrderRowActionDescriptor({
  status: 20,
  processInstanceId: 'pi_002',
  creator: '100',
  currentUserId: '100'
})

assert.equal(approvedOrderActions.canEdit, false)
assert.equal(approvedOrderActions.canSubmit, false)
assert.equal(approvedOrderActions.canCancelApproval, false)
assert.equal(approvedOrderActions.canViewProcess, true)
assert.equal(approvedOrderActions.canTraceDownstream, true)
assert.equal(approvedOrderActions.canDelete, false)

const rejectedDraftActions = getSaleOrderRowActionDescriptor({
  status: 30,
  processInstanceId: undefined,
  creator: '100',
  currentUserId: '200'
})

assert.equal(rejectedDraftActions.canEdit, true)
assert.equal(rejectedDraftActions.canSubmit, true)
assert.equal(rejectedDraftActions.canCancelApproval, false)
assert.equal(rejectedDraftActions.canDelete, true)

const draftActions = getSaleOrderRowActionDescriptor({
  status: 0,
  processInstanceId: undefined,
  creator: '100',
  currentUserId: '100'
})

assert.equal(draftActions.isApprovalRunning, false)
assert.equal(draftActions.canEdit, true)
assert.equal(draftActions.canSubmit, true)
assert.equal(draftActions.canCancelApproval, false)
assert.equal(draftActions.canViewProcess, false)
assert.equal(draftActions.canTraceDownstream, false)
assert.equal(draftActions.canDelete, true)

const failedRetryActions = getSaleOrderRowActionDescriptor({
  status: 60,
  processInstanceId: undefined,
  creator: '100',
  currentUserId: '100'
})

assert.equal(failedRetryActions.isApprovalRunning, false)
assert.equal(failedRetryActions.canEdit, true)
assert.equal(failedRetryActions.canSubmit, true)
assert.equal(failedRetryActions.canCancelApproval, false)
assert.equal(failedRetryActions.canViewProcess, false)
assert.equal(failedRetryActions.canTraceDownstream, false)
assert.equal(failedRetryActions.canDelete, true)

const toolbarWithSelection = getSaleOrderToolbarDescriptor({
  batchEditableSelectionCount: 2,
  deletableSelectionCount: 2
})

assert.equal(toolbarWithSelection.showCreate, true)
assert.equal(toolbarWithSelection.showExport, true)
assert.equal(toolbarWithSelection.showTodo, true)
assert.equal(toolbarWithSelection.showBatchEdit, true)
assert.equal(toolbarWithSelection.showBatchDelete, true)
assert.equal(toolbarWithSelection.disableBatchEdit, false)
assert.equal(toolbarWithSelection.disableBatchDelete, false)

const toolbarWithoutSelection = getSaleOrderToolbarDescriptor({
  batchEditableSelectionCount: 0,
  deletableSelectionCount: 0
})

assert.equal(toolbarWithoutSelection.disableBatchEdit, true)
assert.equal(toolbarWithoutSelection.disableBatchDelete, true)
