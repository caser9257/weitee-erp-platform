export type PurchaseInRouteOpenInput = {
  openType?: string | null
  openAction?: string | null
  openId?: number | null
  purchaseOrderId?: number | null
}

export type PurchaseInRouteOpenResolution =
  | {
      action: 'none'
      cleanupKeys: string[]
    }
  | {
      action: 'form'
      formType: string
      id?: number
      purchaseOrderId?: number
      cleanupKeys: string[]
    }
  | {
      action: 'stock-execute'
      id: number
      cleanupKeys: string[]
    }

export const PURCHASE_IN_ROUTE_CLEANUP_KEYS = [
  'openId',
  'openType',
  'openAction',
  'purchaseOrderId',
  'purchaseOrderNo',
  'from'
]

export function resolvePurchaseInRouteOpen(
  input: PurchaseInRouteOpenInput
): PurchaseInRouteOpenResolution {
  const openId = Number(input.openId || 0)
  const purchaseOrderId = Number(input.purchaseOrderId || 0)
  const openType = input.openType || ''
  const openAction = input.openAction || ''

  if (openAction === 'stock-execute' && openId > 0) {
    return {
      action: 'stock-execute',
      id: openId,
      cleanupKeys: PURCHASE_IN_ROUTE_CLEANUP_KEYS
    }
  }

  const shouldOpenCreate = openType === 'create' && purchaseOrderId > 0
  if (!openId && !shouldOpenCreate) {
    return {
      action: 'none',
      cleanupKeys: PURCHASE_IN_ROUTE_CLEANUP_KEYS
    }
  }

  return {
    action: 'form',
    formType: openType || 'detail',
    ...(openId > 0 ? { id: openId } : {}),
    ...(purchaseOrderId > 0 ? { purchaseOrderId } : {}),
    cleanupKeys: PURCHASE_IN_ROUTE_CLEANUP_KEYS
  }
}
