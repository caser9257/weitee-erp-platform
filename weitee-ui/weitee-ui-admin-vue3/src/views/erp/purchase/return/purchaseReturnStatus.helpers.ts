export type PurchaseReturnRowActionInput = {
  status?: number | null
  processInstanceId?: string | null
  creator?: string | null
  currentUserId?: string | null
}

export type PurchaseReturnRowActionDescriptor = {
  isApprovalRunning: boolean
  canEdit: boolean
  canSubmit: boolean
  canCancelApproval: boolean
  canViewProcess: boolean
  canDelete: boolean
}

const PURCHASE_RETURN_STATUS = {
  DRAFT: 0,
  PROCESS: 10,
  APPROVE: 20,
  REJECT: 30,
  CARRY_FORWARD: 40,
  VOID: 50,
  FAILED: 60
} as const

const isEditableStatus = (status?: number | null) =>
  status === PURCHASE_RETURN_STATUS.DRAFT ||
  status === PURCHASE_RETURN_STATUS.REJECT ||
  status === PURCHASE_RETURN_STATUS.FAILED

export function getPurchaseReturnRowActionDescriptor(
  input: PurchaseReturnRowActionInput
): PurchaseReturnRowActionDescriptor {
  const isApprovalRunning =
    input.status === PURCHASE_RETURN_STATUS.PROCESS && !!input.processInstanceId
  const isProcessStarter =
    !!input.creator && !!input.currentUserId && input.creator === input.currentUserId

  return {
    isApprovalRunning,
    canEdit: isEditableStatus(input.status),
    canSubmit:
      isEditableStatus(input.status) ||
      (input.status === PURCHASE_RETURN_STATUS.PROCESS && !input.processInstanceId),
    canCancelApproval: isApprovalRunning && isProcessStarter,
    canViewProcess: !!input.processInstanceId,
    canDelete: isEditableStatus(input.status)
  }
}
