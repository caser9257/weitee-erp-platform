export type FinancePaymentRowActionInput = {
  status?: number | null
  processInstanceId?: string | null
  creator?: string | null
  currentUserId?: string | null
}

export type FinancePaymentStatusInput = {
  status?: number | null
  processInstanceId?: string | null
}

export type FinancePaymentRowActionDescriptor = {
  isApprovalRunning: boolean
  canEdit: boolean
  canSubmit: boolean
  canCancelApproval: boolean
  canViewProcess: boolean
  canDelete: boolean
  canVoid: boolean
}

export type FinancePaymentStatusDescriptor = {
  label: '未审核' | '审批中' | '已审核' | '已驳回' | '已作废'
  tagType: 'info' | 'warning' | 'success' | 'danger' | 'info'
}

const FINANCE_PAYMENT_STATUS = {
  PROCESS: 10,
  APPROVE: 20,
  REJECT: 30,
  VOID: 50
} as const

export function getFinancePaymentRowActionDescriptor(
  input: FinancePaymentRowActionInput
): FinancePaymentRowActionDescriptor {
  const isApprovalRunning =
    input.status === FINANCE_PAYMENT_STATUS.PROCESS && !!input.processInstanceId
  const isProcessStarter =
    !!input.creator && !!input.currentUserId && input.creator === input.currentUserId

  return {
    isApprovalRunning,
    canEdit:
      input.status === FINANCE_PAYMENT_STATUS.REJECT ||
      (input.status === FINANCE_PAYMENT_STATUS.PROCESS && !input.processInstanceId),
    canSubmit:
      input.status === FINANCE_PAYMENT_STATUS.REJECT ||
      (input.status === FINANCE_PAYMENT_STATUS.PROCESS && !input.processInstanceId),
    canCancelApproval: isApprovalRunning && isProcessStarter,
    canViewProcess: !!input.processInstanceId,
    canDelete:
      input.status !== FINANCE_PAYMENT_STATUS.APPROVE &&
      input.status !== FINANCE_PAYMENT_STATUS.VOID &&
      !isApprovalRunning,
    canVoid: input.status === FINANCE_PAYMENT_STATUS.APPROVE
  }
}

export function getFinancePaymentStatusDescriptor(
  input: FinancePaymentStatusInput
): FinancePaymentStatusDescriptor {
  if (input.status === FINANCE_PAYMENT_STATUS.APPROVE) {
    return {
      label: '已审核',
      tagType: 'success'
    }
  }
  if (input.status === FINANCE_PAYMENT_STATUS.REJECT) {
    return {
      label: '已驳回',
      tagType: 'danger'
    }
  }
  if (input.status === FINANCE_PAYMENT_STATUS.VOID) {
    return {
      label: '已作废',
      tagType: 'info'
    }
  }
  if (input.status === FINANCE_PAYMENT_STATUS.PROCESS && input.processInstanceId) {
    return {
      label: '审批中',
      tagType: 'warning'
    }
  }
  return {
    label: '未审核',
    tagType: 'info'
  }
}
