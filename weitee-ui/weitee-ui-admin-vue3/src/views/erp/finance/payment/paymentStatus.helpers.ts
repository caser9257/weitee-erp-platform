export type FinancePaymentRowActionInput = {
  status?: number | null
  processInstanceId?: string | null
  creator?: string | null
  currentUserId?: string | null
}

export type FinancePaymentRowActionDescriptor = {
  isApprovalRunning: boolean
  canEdit: boolean
  canDelete: boolean
  canSubmit: boolean
  canCancelApproval: boolean
  canViewProcess: boolean
}

export const FINANCE_PAYMENT_STATUS = {
  DRAFT: 0,
  PROCESS: 10,
  APPROVE: 20,
  REJECT: 30,
  VOID: 50,
  FAILED: 60
} as const

const isEditableStatus = (status?: number | null, processInstanceId?: string | null) =>
  status === FINANCE_PAYMENT_STATUS.DRAFT ||
  status === FINANCE_PAYMENT_STATUS.REJECT ||
  status === FINANCE_PAYMENT_STATUS.FAILED ||
  (status === FINANCE_PAYMENT_STATUS.PROCESS && !processInstanceId)

export function getFinancePaymentRowActionDescriptor(
  input: FinancePaymentRowActionInput
): FinancePaymentRowActionDescriptor {
  const isApprovalRunning =
    input.status === FINANCE_PAYMENT_STATUS.PROCESS && !!input.processInstanceId
  const isProcessStarter =
    !!input.creator && !!input.currentUserId && input.creator === input.currentUserId

  return {
    isApprovalRunning,
    canEdit: isEditableStatus(input.status, input.processInstanceId),
    canDelete: input.status !== FINANCE_PAYMENT_STATUS.APPROVE && !isApprovalRunning,
    canSubmit: isEditableStatus(input.status, input.processInstanceId),
    canCancelApproval: isApprovalRunning && isProcessStarter,
    canViewProcess: !!input.processInstanceId
  }
}
