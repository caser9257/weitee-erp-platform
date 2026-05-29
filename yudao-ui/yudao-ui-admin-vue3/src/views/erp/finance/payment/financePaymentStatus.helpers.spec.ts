import assert from 'node:assert/strict'

const {
  getFinancePaymentRowActionDescriptor,
  getFinancePaymentStatusDescriptor
} = await import(new URL('./financePaymentStatus.helpers.ts', import.meta.url).href)

const runningApprovalActions = getFinancePaymentRowActionDescriptor({
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
assert.equal(runningApprovalActions.canVoid, false)

const rejectedActions = getFinancePaymentRowActionDescriptor({
  status: 30,
  processInstanceId: 'pi_002',
  creator: '100',
  currentUserId: '200'
})

assert.equal(rejectedActions.isApprovalRunning, false)
assert.equal(rejectedActions.canEdit, true)
assert.equal(rejectedActions.canSubmit, true)
assert.equal(rejectedActions.canCancelApproval, false)
assert.equal(rejectedActions.canViewProcess, true)
assert.equal(rejectedActions.canDelete, true)
assert.equal(rejectedActions.canVoid, false)

const approvedActions = getFinancePaymentRowActionDescriptor({
  status: 20,
  processInstanceId: 'pi_003',
  creator: '100',
  currentUserId: '200'
})

assert.equal(approvedActions.isApprovalRunning, false)
assert.equal(approvedActions.canEdit, false)
assert.equal(approvedActions.canSubmit, false)
assert.equal(approvedActions.canCancelApproval, false)
assert.equal(approvedActions.canViewProcess, true)
assert.equal(approvedActions.canDelete, false)
assert.equal(approvedActions.canVoid, true)

const voidedActions = getFinancePaymentRowActionDescriptor({
  status: 50,
  processInstanceId: '',
  creator: '100',
  currentUserId: '200'
})

assert.equal(voidedActions.isApprovalRunning, false)
assert.equal(voidedActions.canEdit, false)
assert.equal(voidedActions.canSubmit, false)
assert.equal(voidedActions.canCancelApproval, false)
assert.equal(voidedActions.canViewProcess, false)
assert.equal(voidedActions.canDelete, false)
assert.equal(voidedActions.canVoid, false)

const approvedStatus = getFinancePaymentStatusDescriptor({
  status: 20,
  processInstanceId: 'pi_003'
})

assert.equal(approvedStatus.label, '已审核')
assert.equal(approvedStatus.tagType, 'success')

const processingStatus = getFinancePaymentStatusDescriptor({
  status: 10,
  processInstanceId: 'pi_004'
})

assert.equal(processingStatus.label, '审批中')
assert.equal(processingStatus.tagType, 'warning')

const draftStatus = getFinancePaymentStatusDescriptor({
  status: 10,
  processInstanceId: ''
})

assert.equal(draftStatus.label, '未审核')
assert.equal(draftStatus.tagType, 'info')

const voidedStatus = getFinancePaymentStatusDescriptor({
  status: 50,
  processInstanceId: ''
})

assert.equal(voidedStatus.label, '已作废')
assert.equal(voidedStatus.tagType, 'info')

