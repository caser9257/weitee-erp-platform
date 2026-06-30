import assert from 'node:assert/strict'

const {
  getPurchaseInRowActionDescriptor,
  getPurchaseInToolbarDescriptor,
  getPurchaseInStockStatusDescriptor
} = await import(new URL('./purchaseInStatus.helpers.ts', import.meta.url).href)

const runningApprovalActions = getPurchaseInRowActionDescriptor({
  status: 10,
  processInstanceId: 'pi_001',
  creator: '100',
  currentUserId: '100',
  qaStatus: 10,
  stockInStatus: 10,
  remainingStockInCount: 8
})

assert.equal(runningApprovalActions.isApprovalRunning, true)
assert.equal(runningApprovalActions.canEdit, false)
assert.equal(runningApprovalActions.canSubmit, false)
assert.equal(runningApprovalActions.canCancelApproval, true)
assert.equal(runningApprovalActions.canDelete, false)
assert.equal(runningApprovalActions.canViewProcess, true)
assert.equal(runningApprovalActions.canQualityCheck, false)
assert.equal(runningApprovalActions.canConfirmStockIn, false)

const approvedReadyActions = getPurchaseInRowActionDescriptor({
  status: 20,
  processInstanceId: 'pi_002',
  creator: '100',
  currentUserId: '200',
  qaStatus: 30,
  stockInStatus: 15,
  remainingStockInCount: 3
})

assert.equal(approvedReadyActions.canEdit, false)
assert.equal(approvedReadyActions.canSubmit, false)
assert.equal(approvedReadyActions.canCancelApproval, false)
assert.equal(approvedReadyActions.canViewProcess, true)
assert.equal(approvedReadyActions.canViewQualityDetail, true)
assert.equal(approvedReadyActions.canQualityCheck, false)
assert.equal(approvedReadyActions.canConfirmStockIn, true)

const approvedWaitingQa = getPurchaseInRowActionDescriptor({
  status: 20,
  processInstanceId: 'pi_003',
  creator: '100',
  currentUserId: '200',
  qaStatus: 10,
  stockInStatus: 10,
  remainingStockInCount: 6
})

assert.equal(approvedWaitingQa.canViewQualityDetail, false)
assert.equal(approvedWaitingQa.canQualityCheck, true)
assert.equal(approvedWaitingQa.canConfirmStockIn, false)

const toolbarWithSelection = getPurchaseInToolbarDescriptor({
  deletableSelectionCount: 2
})

assert.equal(toolbarWithSelection.showCreate, true)
assert.equal(toolbarWithSelection.showExport, true)
assert.equal(toolbarWithSelection.showTodo, true)
assert.equal(toolbarWithSelection.showBatchDelete, true)
assert.equal(toolbarWithSelection.disableBatchDelete, false)

const toolbarWithoutSelection = getPurchaseInToolbarDescriptor({
  deletableSelectionCount: 0
})

assert.equal(toolbarWithoutSelection.disableBatchDelete, true)

const partialStockDescriptor = getPurchaseInStockStatusDescriptor({
  qaStatus: 30,
  stockInStatus: 15,
  remainingStockInCount: 2
})

assert.equal(partialStockDescriptor.label, '部分入库')
assert.equal(partialStockDescriptor.tagType, 'warning')

const rejectedStockDescriptor = getPurchaseInStockStatusDescriptor({
  qaStatus: 40,
  stockInStatus: 30,
  remainingStockInCount: 0
})

assert.equal(rejectedStockDescriptor.label, '无需入库')
assert.equal(rejectedStockDescriptor.tagType, 'info')
