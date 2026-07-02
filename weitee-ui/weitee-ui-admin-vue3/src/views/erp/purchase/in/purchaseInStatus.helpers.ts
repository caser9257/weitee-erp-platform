export type PurchaseInRowActionInput = {
  status?: number | null
  processInstanceId?: string | null
  creator?: string | null
  currentUserId?: string | null
  qaStatus?: number | null
  stockInStatus?: number | null
  remainingStockInCount?: number | string | null
}

export type PurchaseInToolbarInput = {
  deletableSelectionCount?: number
}

export type PurchaseInStockStatusInput = {
  status?: number | null
  qaStatus?: number | null
  stockInStatus?: number | null
}

export type PurchaseInRowActionDescriptor = {
  isApprovalRunning: boolean
  canEdit: boolean
  canSubmit: boolean
  canCancelApproval: boolean
  canViewProcess: boolean
  canViewQualityDetail: boolean
  canQualityCheck: boolean
  canConfirmStockIn: boolean
  canDelete: boolean
}

export type PurchaseInToolbarDescriptor = {
  showCreate: boolean
  showExport: boolean
  showTodo: boolean
  showBatchDelete: boolean
  disableBatchDelete: boolean
}

export type PurchaseInStockStatusDescriptor = {
  label: '已入库' | '部分入库' | '待入库' | '无需入库' | '待质检' | '-'
  tagType: 'success' | 'warning' | 'info'
}

const PURCHASE_IN_STATUS = {
  DRAFT: 0,
  PROCESS: 10,
  APPROVE: 20,
  REJECT: 30,
  CARRY_FORWARD: 40,
  VOID: 50,
  FAILED: 60
} as const

const isEditableStatus = (status?: number | null) =>
  status === PURCHASE_IN_STATUS.DRAFT ||
  status === PURCHASE_IN_STATUS.REJECT ||
  status === PURCHASE_IN_STATUS.FAILED

const PURCHASE_IN_QA_STATUS = {
  TO_INSPECT: 10,
  PARTIAL: 20,
  PASSED: 30,
  REJECTED: 40
} as const

const PURCHASE_IN_STOCK_IN_STATUS = {
  TO_STOCK_IN: 10,
  PARTIAL_STOCKED_IN: 15,
  STOCKED_IN: 20,
  NO_NEED_STOCK_IN: 30
} as const

const normalizeNumber = (value?: number | string | null) => Number(value || 0)

export function getPurchaseInRowActionDescriptor(
  input: PurchaseInRowActionInput
): PurchaseInRowActionDescriptor {
  const isApprovalRunning =
    input.status === PURCHASE_IN_STATUS.PROCESS && !!input.processInstanceId
  const isProcessStarter =
    !!input.creator && !!input.currentUserId && input.creator === input.currentUserId

  return {
    isApprovalRunning,
    canEdit: isEditableStatus(input.status),
    canSubmit: isEditableStatus(input.status),
    canCancelApproval: isApprovalRunning && isProcessStarter,
    canViewProcess: !!input.processInstanceId,
    canViewQualityDetail:
      input.status === PURCHASE_IN_STATUS.APPROVE &&
      input.qaStatus !== undefined &&
      input.qaStatus !== null &&
      input.qaStatus !== PURCHASE_IN_QA_STATUS.TO_INSPECT,
    canQualityCheck:
      input.status === PURCHASE_IN_STATUS.APPROVE &&
      (input.qaStatus === undefined || input.qaStatus === null || input.qaStatus === PURCHASE_IN_QA_STATUS.TO_INSPECT),
    canConfirmStockIn:
      input.status === PURCHASE_IN_STATUS.APPROVE &&
      (input.qaStatus === PURCHASE_IN_QA_STATUS.PARTIAL ||
        input.qaStatus === PURCHASE_IN_QA_STATUS.PASSED) &&
      (input.stockInStatus === PURCHASE_IN_STOCK_IN_STATUS.TO_STOCK_IN ||
        input.stockInStatus === PURCHASE_IN_STOCK_IN_STATUS.PARTIAL_STOCKED_IN) &&
      normalizeNumber(input.remainingStockInCount) > 0,
    canDelete: input.status !== PURCHASE_IN_STATUS.APPROVE && !isApprovalRunning
  }
}

export function getPurchaseInToolbarDescriptor(
  input: PurchaseInToolbarInput
): PurchaseInToolbarDescriptor {
  return {
    showCreate: true,
    showExport: true,
    showTodo: true,
    showBatchDelete: true,
    disableBatchDelete: normalizeNumber(input.deletableSelectionCount) <= 0
  }
}

export function getPurchaseInStockStatusDescriptor(
  input: PurchaseInStockStatusInput
): PurchaseInStockStatusDescriptor {
  if (input.stockInStatus === PURCHASE_IN_STOCK_IN_STATUS.STOCKED_IN) {
    return {
      label: '已入库',
      tagType: 'success'
    }
  }
  if (input.stockInStatus === PURCHASE_IN_STOCK_IN_STATUS.PARTIAL_STOCKED_IN) {
    return {
      label: '部分入库',
      tagType: 'warning'
    }
  }
  if (input.stockInStatus === PURCHASE_IN_STOCK_IN_STATUS.TO_STOCK_IN) {
    return {
      label: '待入库',
      tagType: 'warning'
    }
  }
  if (
    input.stockInStatus === PURCHASE_IN_STOCK_IN_STATUS.NO_NEED_STOCK_IN ||
    input.qaStatus === PURCHASE_IN_QA_STATUS.REJECTED
  ) {
    return {
      label: '无需入库',
      tagType: 'info'
    }
  }
  if (
    input.qaStatus === PURCHASE_IN_QA_STATUS.TO_INSPECT ||
    input.qaStatus === PURCHASE_IN_QA_STATUS.PARTIAL ||
    input.qaStatus === PURCHASE_IN_QA_STATUS.PASSED ||
    input.status === PURCHASE_IN_STATUS.APPROVE
  ) {
    return {
      label: '待质检',
      tagType: 'info'
    }
  }
  return {
    label: '-',
    tagType: 'info'
  }
}
