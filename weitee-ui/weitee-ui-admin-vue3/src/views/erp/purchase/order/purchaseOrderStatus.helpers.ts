export type PurchaseOrderStatusInput = {
  totalCount?: number | string | null
  inCount?: number | string | null
  hasPendingPurchaseIn?: boolean
  pendingPurchaseInId?: number | string | null
}

export type PurchaseOrderRowActionInput = PurchaseOrderStatusInput & {
  status?: number | null
  processInstanceId?: string | null
  creator?: string | null
  currentUserId?: string | null
  isSaleTraceMode?: boolean
}

export type PurchaseOrderToolbarInput = {
  isSaleTraceMode?: boolean
  batchEditableSelectionCount?: number
  deletableSelectionCount?: number
}

export type PurchaseInActionInput = PurchaseOrderStatusInput & {
  approved: boolean
}

export type InboundStatusDescriptor = {
  resultLabel: '未入库' | '部分入库' | '已全部入库'
  processingLabel?: '待继续入库'
}

export type PurchaseInActionDescriptor =
  | {
      visible: false
      label?: undefined
      action?: undefined
    }
  | {
      visible: true
      label: '去入库主控'
      action: 'create' | 'detail' | 'view'
    }

export type PurchaseOrderRowActionDescriptor = {
  isApprovalRunning: boolean
  canBatchEdit: boolean
  canEdit: boolean
  canSubmit: boolean
  canCancelApproval: boolean
  canViewProcess: boolean
  canDelete: boolean
  purchaseInAction: PurchaseInActionDescriptor
}

export type PurchaseOrderToolbarDescriptor = {
  showCreate: boolean
  showExport: boolean
  showTodo: boolean
  showBatchEdit: boolean
  showBatchDelete: boolean
  disableBatchEdit: boolean
  disableBatchDelete: boolean
}

const normalizeNumber = (value?: number | string | null) => Number(value || 0)

const PURCHASE_ORDER_STATUS = {
  DRAFT: 0,
  PROCESS: 10,
  APPROVE: 20,
  REJECT: 30,
  CARRY_FORWARD: 40,
  VOID: 50,
  FAILED: 60
} as const

const isEditableStatus = (status?: number | null) =>
  status === PURCHASE_ORDER_STATUS.DRAFT ||
  status === PURCHASE_ORDER_STATUS.REJECT ||
  status === PURCHASE_ORDER_STATUS.FAILED

const isInboundCompleted = (totalCount: number, inCount: number) => totalCount > 0 && inCount >= totalCount

export function getInboundStatusDescriptor(
  input: PurchaseOrderStatusInput
): InboundStatusDescriptor {
  const totalCount = normalizeNumber(input.totalCount)
  const inCount = normalizeNumber(input.inCount)
  const resultLabel =
    inCount <= 0 ? '未入库' : isInboundCompleted(totalCount, inCount) ? '已全部入库' : '部分入库'

  return {
    resultLabel,
    processingLabel:
      input.hasPendingPurchaseIn && !isInboundCompleted(totalCount, inCount) ? '待继续入库' : undefined
  }
}

export function getPurchaseInActionDescriptor(
  input: PurchaseInActionInput
): PurchaseInActionDescriptor {
  const totalCount = normalizeNumber(input.totalCount)
  const inCount = normalizeNumber(input.inCount)
  const pendingPurchaseInId = normalizeNumber(input.pendingPurchaseInId)
  const canContinue = input.approved && totalCount > 0 && inCount < totalCount
  if (!canContinue) {
    return { visible: false }
  }
  if (input.hasPendingPurchaseIn && pendingPurchaseInId > 0) {
    return {
      visible: true,
      label: '去入库主控',
      action: 'detail'
    }
  }
  if (input.hasPendingPurchaseIn) {
    return {
      visible: true,
      label: '去入库主控',
      action: 'view'
    }
  }
  return {
    visible: true,
    label: '去入库主控',
    action: 'create'
  }
}

export function getPurchaseOrderRowActionDescriptor(
  input: PurchaseOrderRowActionInput
): PurchaseOrderRowActionDescriptor {
  const isApprovalRunning =
    input.status === PURCHASE_ORDER_STATUS.PROCESS && !!input.processInstanceId
  const isProcessStarter =
    !!input.creator && !!input.currentUserId && input.creator === input.currentUserId
  const isSaleTraceMode = !!input.isSaleTraceMode
  const canBatchEdit =
    !isSaleTraceMode &&
    input.status !== PURCHASE_ORDER_STATUS.APPROVE &&
    !isApprovalRunning
  const canEdit =
    !isSaleTraceMode && isEditableStatus(input.status)
  const canSubmit =
    !isSaleTraceMode && isEditableStatus(input.status)
  const canCancelApproval = !isSaleTraceMode && isApprovalRunning && isProcessStarter
  const canViewProcess = !!input.processInstanceId
  const canDelete = !isSaleTraceMode && input.status !== PURCHASE_ORDER_STATUS.APPROVE && !isApprovalRunning
  const purchaseInAction = isSaleTraceMode
    ? { visible: false as const }
    : getPurchaseInActionDescriptor({
        approved: input.status === PURCHASE_ORDER_STATUS.APPROVE,
        totalCount: input.totalCount,
        inCount: input.inCount,
        hasPendingPurchaseIn: input.hasPendingPurchaseIn,
        pendingPurchaseInId: input.pendingPurchaseInId
      })

  return {
    isApprovalRunning,
    canBatchEdit,
    canEdit,
    canSubmit,
    canCancelApproval,
    canViewProcess,
    canDelete,
    purchaseInAction
  }
}

export function getPurchaseOrderToolbarDescriptor(
  input: PurchaseOrderToolbarInput
): PurchaseOrderToolbarDescriptor {
  const isSaleTraceMode = !!input.isSaleTraceMode
  const batchEditableSelectionCount = normalizeNumber(input.batchEditableSelectionCount)
  const deletableSelectionCount = normalizeNumber(input.deletableSelectionCount)

  return {
    showCreate: !isSaleTraceMode,
    showExport: !isSaleTraceMode,
    showTodo: !isSaleTraceMode,
    showBatchEdit: !isSaleTraceMode,
    showBatchDelete: !isSaleTraceMode,
    disableBatchEdit: batchEditableSelectionCount <= 0,
    disableBatchDelete: deletableSelectionCount <= 0
  }
}
