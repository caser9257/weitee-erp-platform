export type PurchaseOrderRouteContextInput = {
  sourceOrderId?: unknown
  sourceOrderNo?: unknown
  purchaseOrderNo?: unknown
  from?: unknown
}

export type PurchaseOrderRouteContextMode = 'none' | 'sale-trace' | 'purchase-in-focus'

export type PurchaseOrderRouteContextResolution = {
  mode: PurchaseOrderRouteContextMode
  traceOrderId?: number
  traceOrderNo: string
  purchaseOrderNo: string
}

const normalizeRouteNumber = (value: unknown) => {
  if (typeof value !== 'string' || !value.trim()) {
    return undefined
  }
  const parsedValue = Number(value)
  return Number.isNaN(parsedValue) || parsedValue <= 0 ? undefined : parsedValue
}

const normalizeRouteText = (value: unknown) => {
  return typeof value === 'string' ? value.trim() : ''
}

export function resolvePurchaseOrderRouteContext(
  input: PurchaseOrderRouteContextInput
): PurchaseOrderRouteContextResolution {
  const purchaseOrderNo = normalizeRouteText(input.purchaseOrderNo)
  const legacyPurchaseOrderNo = normalizeRouteText(input.sourceOrderNo)
  if (input.from === 'purchase-in') {
    return {
      mode: 'purchase-in-focus',
      traceOrderId: undefined,
      traceOrderNo: '',
      purchaseOrderNo: purchaseOrderNo || legacyPurchaseOrderNo
    }
  }

  const traceOrderId = normalizeRouteNumber(input.sourceOrderId)
  return {
    mode: traceOrderId ? 'sale-trace' : 'none',
    traceOrderId,
    traceOrderNo: normalizeRouteText(input.sourceOrderNo),
    purchaseOrderNo
  }
}
