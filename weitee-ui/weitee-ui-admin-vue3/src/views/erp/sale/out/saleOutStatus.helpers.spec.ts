import assert from 'node:assert/strict'

const {
  getSaleOutRowActionDescriptor,
  getSaleOutToolbarDescriptor
} = await import(new URL('./saleOutStatus.helpers.ts', import.meta.url).href)

const processActions = getSaleOutRowActionDescriptor({
  status: 10
})

assert.equal(processActions.canEdit, true)
assert.equal(processActions.canApprove, true)
assert.equal(processActions.canReverseApprove, false)
assert.equal(processActions.canDelete, true)

const approvedActions = getSaleOutRowActionDescriptor({
  status: 20
})

assert.equal(approvedActions.canEdit, false)
assert.equal(approvedActions.canApprove, false)
assert.equal(approvedActions.canReverseApprove, true)
assert.equal(approvedActions.canDelete, false)

const unknownStatusActions = getSaleOutRowActionDescriptor({
  status: 30
})

assert.equal(unknownStatusActions.canEdit, true)
assert.equal(unknownStatusActions.canApprove, false)
assert.equal(unknownStatusActions.canReverseApprove, false)
assert.equal(unknownStatusActions.canDelete, true)

const toolbarWithDeletableSelection = getSaleOutToolbarDescriptor({
  deletableSelectionCount: 2
})

assert.equal(toolbarWithDeletableSelection.disableBatchDelete, false)

const toolbarWithoutDeletableSelection = getSaleOutToolbarDescriptor({
  deletableSelectionCount: 0
})

assert.equal(toolbarWithoutDeletableSelection.disableBatchDelete, true)
