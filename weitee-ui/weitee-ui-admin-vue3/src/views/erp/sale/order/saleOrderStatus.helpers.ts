export type SaleOrderRowActionInput = {
  status?: number | null
  processInstanceId?: string | null
  creator?: string | null
  currentUserId?: string | null
}

export type SaleOrderToolbarInput = {
  batchEditableSelectionCount?: number
  deletableSelectionCount?: number
}

export type SaleOrderRowActionDescriptor = {
  isApprovalRunning: boolean
  canEdit: boolean
  canSubmit: boolean
  canCancelApproval: boolean
  canViewProcess: boolean
  canTraceDownstream: boolean
  canDelete: boolean
}

export type SaleOrderToolbarDescriptor = {
  showCreate: boolean
  showExport: boolean
  showTodo: boolean
  showBatchEdit: boolean
  showBatchDelete: boolean
  disableBatchEdit: boolean
  disableBatchDelete: boolean
}

const SALE_ORDER_STATUS = {
  DRAFT: 0,
  PROCESS: 10,
  APPROVE: 20,
  REJECT: 30,
  FAILED: 60
} as const

const normalizeNumber = (value?: number | string | null) => Number(value || 0)

export function getSaleOrderRowActionDescriptor(
  input: SaleOrderRowActionInput
): SaleOrderRowActionDescriptor {
  const isApprovalRunning =
    input.status === SALE_ORDER_STATUS.PROCESS && !!input.processInstanceId
  const isProcessStarter =
    !!input.creator && !!input.currentUserId && input.creator === input.currentUserId

  return {
    isApprovalRunning,
    canEdit:
      input.status === SALE_ORDER_STATUS.DRAFT ||
      input.status === SALE_ORDER_STATUS.REJECT ||
      input.status === SALE_ORDER_STATUS.FAILED ||
      (input.status === SALE_ORDER_STATUS.PROCESS && !input.processInstanceId),
    canSubmit:
      input.status === SALE_ORDER_STATUS.DRAFT ||
      input.status === SALE_ORDER_STATUS.REJECT ||
      input.status === SALE_ORDER_STATUS.FAILED ||
      (input.status === SALE_ORDER_STATUS.PROCESS && !input.processInstanceId),
    canCancelApproval: isApprovalRunning && isProcessStarter,
    canViewProcess: !!input.processInstanceId,
    canTraceDownstream: input.status === SALE_ORDER_STATUS.APPROVE,
    canDelete: input.status !== SALE_ORDER_STATUS.APPROVE && !isApprovalRunning
  }
}

export function getSaleOrderToolbarDescriptor(
  input: SaleOrderToolbarInput
): SaleOrderToolbarDescriptor {
  const batchEditableSelectionCount = normalizeNumber(input.batchEditableSelectionCount)
  const deletableSelectionCount = normalizeNumber(input.deletableSelectionCount)
  return {
    showCreate: true,
    showExport: true,
    showTodo: true,
    showBatchEdit: true,
    showBatchDelete: true,
    disableBatchEdit: batchEditableSelectionCount <= 0,
    disableBatchDelete: deletableSelectionCount <= 0
  }
}
