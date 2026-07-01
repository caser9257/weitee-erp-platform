import assert from 'node:assert/strict'

const {
  getInboundStatusDescriptor,
  getPurchaseInActionDescriptor,
  getPurchaseOrderRowActionDescriptor,
  getPurchaseOrderToolbarDescriptor
} = await import(new URL('./purchaseOrderStatus.helpers.ts', import.meta.url).href)

assert.equal(
  getInboundStatusDescriptor({
    totalCount: 9992,
    inCount: 9968,
    hasPendingPurchaseIn: true
  }).processingLabel,
  '待继续入库'
)

assert.equal(
  getPurchaseInActionDescriptor({
    approved: true,
    totalCount: 9992,
    inCount: 9968,
    hasPendingPurchaseIn: true,
    pendingPurchaseInId: 12
  }).label,
  '去入库主控'
)

assert.equal(
  getInboundStatusDescriptor({
    totalCount: 10,
    inCount: 0,
    hasPendingPurchaseIn: false
  }).resultLabel,
  '未入库'
)

assert.equal(
  getInboundStatusDescriptor({
    totalCount: 10,
    inCount: 4,
    hasPendingPurchaseIn: false
  }).resultLabel,
  '部分入库'
)

assert.equal(
  getInboundStatusDescriptor({
    totalCount: 10,
    inCount: 10,
    hasPendingPurchaseIn: false
  }).resultLabel,
  '已全部入库'
)

assert.equal(
  getInboundStatusDescriptor({
    totalCount: 10,
    inCount: 10,
    hasPendingPurchaseIn: true
  }).processingLabel,
  undefined
)

assert.equal(
  getPurchaseInActionDescriptor({
    approved: true,
    totalCount: 10,
    inCount: 0,
    hasPendingPurchaseIn: false
  }).label,
  '去入库主控'
)

assert.equal(
  getPurchaseInActionDescriptor({
    approved: true,
    totalCount: 10,
    inCount: 10,
    hasPendingPurchaseIn: false
  }).visible,
  false
)

const runningApprovalActions = getPurchaseOrderRowActionDescriptor({
  status: 10,
  processInstanceId: 'pi_001',
  creator: '100',
  currentUserId: '100',
  isSaleTraceMode: false,
  totalCount: 10,
  inCount: 0,
  hasPendingPurchaseIn: false
})

assert.equal(runningApprovalActions.isApprovalRunning, true)
assert.equal(runningApprovalActions.canBatchEdit, false)
assert.equal(runningApprovalActions.canEdit, false)
assert.equal(runningApprovalActions.canSubmit, false)
assert.equal(runningApprovalActions.canCancelApproval, true)
assert.equal(runningApprovalActions.canDelete, false)
assert.equal(runningApprovalActions.canViewProcess, true)

const approvedTraceActions = getPurchaseOrderRowActionDescriptor({
  status: 20,
  processInstanceId: 'pi_002',
  creator: '100',
  currentUserId: '100',
  isSaleTraceMode: true,
  totalCount: 10,
  inCount: 4,
  hasPendingPurchaseIn: false
})

assert.equal(approvedTraceActions.purchaseInAction.visible, false)
assert.equal(approvedTraceActions.canBatchEdit, false)
assert.equal(approvedTraceActions.canEdit, false)
assert.equal(approvedTraceActions.canSubmit, false)
assert.equal(approvedTraceActions.canCancelApproval, false)
assert.equal(approvedTraceActions.canDelete, false)
assert.equal(approvedTraceActions.canViewProcess, true)

const toolbarInTraceMode = getPurchaseOrderToolbarDescriptor({
  isSaleTraceMode: true,
  deletableSelectionCount: 3
})

assert.equal(toolbarInTraceMode.showCreate, false)
assert.equal(toolbarInTraceMode.showExport, false)
assert.equal(toolbarInTraceMode.showTodo, false)
assert.equal(toolbarInTraceMode.showBatchEdit, false)
assert.equal(toolbarInTraceMode.showBatchDelete, false)

const toolbarInNormalMode = getPurchaseOrderToolbarDescriptor({
  isSaleTraceMode: false,
  batchEditableSelectionCount: 0,
  deletableSelectionCount: 0
})

assert.equal(toolbarInNormalMode.showCreate, true)
assert.equal(toolbarInNormalMode.showExport, true)
assert.equal(toolbarInNormalMode.showTodo, true)
assert.equal(toolbarInNormalMode.showBatchEdit, true)
assert.equal(toolbarInNormalMode.showBatchDelete, true)
assert.equal(toolbarInNormalMode.disableBatchEdit, true)
assert.equal(toolbarInNormalMode.disableBatchDelete, true)

const draftActions = getPurchaseOrderRowActionDescriptor({
  status: 0,
  processInstanceId: '',
  creator: '100',
  currentUserId: '100',
  isSaleTraceMode: false,
  totalCount: 10,
  inCount: 0,
  hasPendingPurchaseIn: false
})

assert.equal(draftActions.canBatchEdit, true)
assert.equal(draftActions.canEdit, true)
assert.equal(draftActions.canSubmit, true)
assert.equal(draftActions.canCancelApproval, false)
assert.equal(draftActions.canViewProcess, false)
assert.equal(draftActions.canDelete, true)

const failedRetryActions = getPurchaseOrderRowActionDescriptor({
  status: 60,
  processInstanceId: '',
  creator: '100',
  currentUserId: '100',
  isSaleTraceMode: false,
  totalCount: 10,
  inCount: 0,
  hasPendingPurchaseIn: false
})

assert.equal(failedRetryActions.isApprovalRunning, false)
assert.equal(failedRetryActions.canBatchEdit, true)
assert.equal(failedRetryActions.canEdit, true)
assert.equal(failedRetryActions.canSubmit, true)
assert.equal(failedRetryActions.canCancelApproval, false)
assert.equal(failedRetryActions.canViewProcess, false)
assert.equal(failedRetryActions.canDelete, true)
