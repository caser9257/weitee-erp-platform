export type SaleOutRowActionInput = {
  status?: number | null
}

export type SaleOutToolbarInput = {
  deletableSelectionCount?: number
}

export type SaleOutRowActionDescriptor = {
  canEdit: boolean
  canApprove: boolean
  canReverseApprove: boolean
  canDelete: boolean
}

export type SaleOutToolbarDescriptor = {
  disableBatchDelete: boolean
}

const SALE_OUT_STATUS = {
  PROCESS: 10,
  APPROVE: 20
} as const

const normalizeNumber = (value?: number | string | null) => Number(value || 0)

export function getSaleOutRowActionDescriptor(
  input: SaleOutRowActionInput
): SaleOutRowActionDescriptor {
  return {
    canEdit: input.status !== SALE_OUT_STATUS.APPROVE,
    canApprove: input.status === SALE_OUT_STATUS.PROCESS,
    canReverseApprove: input.status === SALE_OUT_STATUS.APPROVE,
    canDelete: input.status !== SALE_OUT_STATUS.APPROVE
  }
}

export function getSaleOutToolbarDescriptor(
  input: SaleOutToolbarInput
): SaleOutToolbarDescriptor {
  return {
    disableBatchDelete: normalizeNumber(input.deletableSelectionCount) <= 0
  }
}
